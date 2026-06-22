import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.Pair;

/**
 * J_ISIE_Aggregator_EMS
 */	
public class J_ISIE_Aggregator_EMS implements I_AggregatorEnergyManagement {

    private EnergyCoop energyCoop;
    private J_TimeParameters timeParameters;
    
	List<Class<? extends I_AggregatorAssetManagement>> internalAggregatorAssetManagements = new ArrayList<>(Arrays.asList(
			I_AggregatorChargingManagement.class, 
			I_AggregatorBatteryManagement.class
			)); //Inherent asset management that the EMS handles itself
	List<Class<? extends I_AggregatorAssetManagement>> supportedExternalAggregatorAssetManagements = new ArrayList<>();
	Map<Class<? extends I_AggregatorAssetManagement>, I_AggregatorAssetManagement> activeExternalAggregatorAssetManagements = new HashMap();		
	
	private final double FORECAST_TIME_H = 24.0; // Changing this requires updating triptracker
	private final int TIMESTEPS_IN_FORECAST = 96;
	
	// These parameters contain the (for now hardcoded) values of the national energy system.
	// The market feedback parameters are based on these values.
	private double NATIONAL_PRICE_ELASTICITY_EURPMWHPGW = 5.0;
	private double NATIONAL_COMMUTER_EV_POWER_GW = 33.0;
	private double NATIONAL_NON_COMMUTER_EV_POWER_GW = 22.0;
	private double NATIONAL_HEATPUMP_POWER_GW = 8.0;
	private double NATIONAL_BATTERY_POWER_GW = 20.0;
	
	private int POWERSTEPS_NR = 20;
	
	private double SELFCONSUMPTIONSAVING_EURPMWH = 0.0;
	private double CONGESTIONFACTOR_EURPMWHPKW = 1000.0;
	private double CONGESTIONDEADZONE_FR = 0.9;
	private double congestionDeadzone_kW;
	
	private double PRICE_SPREAD_THRESHOLD_EURPMWH = 10.0;
	private boolean SEPERATE_MARKET_AND_CONGESTION = true;
	
	// Maps GridConnection IDs to (24 hour) profiles for EV charging. Current implementation only support 1 EV per GC.
	private Map<String, double[]> evProfilesMap = new HashMap<String, double[]>();
	// Maps GridConnection IDs to (24 hour) profiles for batteries. Current implementation only support 1 Battery per GC.	
	private Map<String, double[]> batteryProfilesMap = new HashMap<String, double[]>();
	
    /**
     * Default constructor
     */
    public J_ISIE_Aggregator_EMS(EnergyCoop energyCoop, J_TimeParameters timeParameters) {
    	this.energyCoop = energyCoop;
    	EnergyModel energyModel = energyCoop.energyModel;
    	this.timeParameters = timeParameters;
    	if (energyCoop.f_getAllChildMemberGridConnections().size() > 0) {
    		String parentNodeId = energyCoop.f_getAllChildMemberGridConnections().get(0).p_parentNodeElectricID;
    		GridNode gn = findFirst(energyModel.pop_gridNodes, x -> x.p_gridNodeID.equals(parentNodeId));
    		if (gn != null) {
    			this.congestionDeadzone_kW = this.CONGESTIONDEADZONE_FR * gn.p_capacity_kW;
    		}
    	}
    	//this.congestionDeadzone_kW = 150;
    }
    
    private record EnergyPosition(double[] load_kW, double[] marketPriceCurve_eurpMWh) {}

    public void setCongestionFactor( double CongestionFactor_eurpMWhpkW ) {
    	this.CONGESTIONFACTOR_EURPMWHPKW = CongestionFactor_eurpMWhpkW;
    }
    
    /*
     * Main method that is called every timestep. Creates & Executes the flex schedule.
     * We make a schedule every day one timestep before midnight (And at the very start of the simulation). That means our assets will not have any defined behaviour at the first timestep of the model.
     * The task is given to the External Setpoint AssetManagement to handle this first timestep to make sure the asset is operated (with powerFraction 0 by default).
     */
    public void operateAggregatorEnergyManagement(J_TimeVariables timeVariables) {
    	double timeAtStartForecast_h;
    	if (timeVariables.getT_h() == 0.0) {
    		timeAtStartForecast_h = 0.0;
    		this.evProfilesMap.clear();
    		this.createFlexAssetSchedules( timeAtStartForecast_h );
    	}
    	else if ( timeVariables.getTimeOfDay_h() == 24.0 - this.timeParameters.getTimeStep_h() ) {
    		timeAtStartForecast_h = timeVariables.getT_h() + this.timeParameters.getTimeStep_h();
    		this.evProfilesMap.clear();
    		this.createFlexAssetSchedules( timeAtStartForecast_h );
    	}
    	
		int timeStepOfToday = (roundToInt(timeVariables.getTimeOfDay_h() / this.timeParameters.getTimeStep_h()) + 1) % this.TIMESTEPS_IN_FORECAST;
    	for (GridConnection gc : this.energyCoop.f_getAllChildMemberGridConnections()) {
    		// EVs
    		if (gc.c_electricVehicles.size() > 0) {
    			if (gc.f_getChargingManagement() instanceof J_ChargingManagementExternalSetpoint chargingManagement) {
    	    		J_ChargePoint chargePoint = gc.f_getChargePoint();
    	    		// Check if the vehicle is at the GC, and not on a trip
    	    		if (chargePoint.getCurrentActiveChargingRequests().size() > 0) {
    	    			I_ChargingRequest chargingRequest = chargePoint.getCurrentActiveChargingRequests().get(0);
    		    		double[] evProfile_kW = evProfilesMap.get(gc.p_gridConnectionID);
    		    		Map<I_ChargingRequest, Double> chargingSetpoints = new HashMap<I_ChargingRequest, Double>();		    		
    		    		chargingSetpoints.put(chargingRequest, evProfile_kW[timeStepOfToday]);
    		    		chargingManagement.setChargingSetpoints(chargingSetpoints);
    	    		}
    			}
    		}
    		
    		// Batteries
    		if (gc.p_batteryAsset != null) {
    			if (gc.f_getBatteryManagement() instanceof J_BatteryManagementExternalSetpoint batteryManagement) {
		    		double[] batteryProfile_kW = batteryProfilesMap.get(gc.p_gridConnectionID);
    				batteryManagement.setChargeSetpoint_kW(batteryProfile_kW[timeStepOfToday]);
    			}
    		}    		
    	}
    }
    
    private void createFlexAssetSchedules( double timeAtStartForecast_h ) {
    	// TEMPORARY? might need to move the initialization of this forecasting before engine intialization to the loader? Then we could set this in the constructor or something
		J_ProfileForecaster pf_ambientTemperature_degC = this.energyCoop.energyModel.pf_ambientTemperature_degC;
		double[] dailyPriceCurve_eurpMWh = this.getDailyPriceCurve( timeAtStartForecast_h );
	
		this.energyCoop.priceDebugPlot.removeAll();
		this.energyCoop.f_fillPriceDebugPlot(dailyPriceCurve_eurpMWh, "original curve");
		
		// We start with the load of the fixed assets and the gridconnections that are not managed by the aggregator
		double[] totalLoad_kW = this.estimateTotalLoadOtherGridConnectionsAndAssets( timeAtStartForecast_h );
		
    	// Make a copy of the list of GCs and shuffle it, that way it is randomized which GCs have 'the most' flexibility.
    	List<GridConnection> gcList = new ArrayList<GridConnection>(this.energyCoop.f_getAllChildMemberGridConnections());
    	Collections.shuffle(gcList);
    	
		EnergyPosition energyPosition = new EnergyPosition(totalLoad_kW, dailyPriceCurve_eurpMWh);
		
		energyPosition = this.createEVSchedules(timeAtStartForecast_h, gcList, energyPosition);
		
		dailyPriceCurve_eurpMWh = energyPosition.marketPriceCurve_eurpMWh();
		totalLoad_kW = energyPosition.load_kW();
		this.energyCoop.f_fillPriceDebugPlot(energyPosition.marketPriceCurve_eurpMWh(), "resulting curve w EV marketfeedback");
		J_Market m = new J_Market(dailyPriceCurve_eurpMWh, 0, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);
		double[] c = m.getCongestionDailyPriceCurve_eurpMWh(totalLoad_kW);
		this.energyCoop.f_fillPriceDebugPlot(c, "resulting curve w EV marketfeedback & congestion");
		this.energyCoop.f_fillLoadDebugPlot(totalLoad_kW, "total load after EVs");
		
		energyPosition = this.createBatterySchedules(timeAtStartForecast_h, gcList, energyPosition);
		
		dailyPriceCurve_eurpMWh = energyPosition.marketPriceCurve_eurpMWh();
		totalLoad_kW = energyPosition.load_kW();
		this.energyCoop.f_fillPriceDebugPlot(energyPosition.marketPriceCurve_eurpMWh(), "resulting curve w battery marketfeedback");
		m = new J_Market(dailyPriceCurve_eurpMWh, 0, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);
		c = m.getCongestionDailyPriceCurve_eurpMWh(totalLoad_kW);
		this.energyCoop.f_fillPriceDebugPlot(c, "resulting curve w battery marketfeedback & congestion");
		this.energyCoop.f_fillLoadDebugPlot(totalLoad_kW, "total load after batteries");

	}
    
    private EnergyPosition createEVSchedules( double timeAtStartForecast_h, List<GridConnection> gcList, EnergyPosition energyPosition ) {
       	for (GridConnection gc : gcList) {
    		if (gc.c_electricVehicles.size() > 0) {
    			energyPosition = createGridConnectionEVSchedule( timeAtStartForecast_h, gc, energyPosition );
    		}
    	}
       	return energyPosition;
    }
    
    private EnergyPosition createGridConnectionEVSchedule(double timeAtStartForecast_h, GridConnection gc, EnergyPosition energyPosition) {   	
    	double[] evProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];
    	// We use the assumption of 1 EV per GC here
		J_EAEV ev = gc.c_electricVehicles.get(0);
	
		J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
		List<Triple<Double, Double, Double>> trips = tripTracker.getTripsNext24Hours( timeAtStartForecast_h );
		// We translate the distance to an amount of energy and the start/end time become allowedOperatingTimes

		// Are these values like current SOC off by one timestep? The schedule is made at 23:45, not 00:00?
    	double currentSOC_kWh = ev.getCurrentSOC_kWh();
    	double maximalStorageCapacity_kWh = ev.getStorageCapacity_kWh();
    	double endTimeLastTrip_h = ev.getAvailability() ? 0.0 : (tripTracker.endtimes_min.get(tripTracker.eventIndex)%24) / 60.0; // assumes the current trip ends today (no trips > 24 hours exist)
	    double maxPower_kW = ev.capacityElectric_kW;
	    
	    if (trips.size() > 0) {
	    	Pair<EnergyPosition, double[]> pair = createGridConnectionEVScheduleWithTrips( timeAtStartForecast_h, energyPosition, ev, trips, currentSOC_kWh, maximalStorageCapacity_kWh, endTimeLastTrip_h, maxPower_kW);
	    	energyPosition = pair.getLeft();
	    	evProfile_kW = pair.getRight();
	    }
	    else {
	    	Pair<EnergyPosition, double[]> pair = createGridConnectionEVScheduleWithoutTrips( timeAtStartForecast_h, energyPosition, ev, currentSOC_kWh, maximalStorageCapacity_kWh, endTimeLastTrip_h, maxPower_kW);
	    	energyPosition = pair.getLeft();
	    	evProfile_kW = pair.getRight();
	    }

		// Should this be a copy?
    	evProfilesMap.put(gc.p_gridConnectionID, evProfile_kW);

    	return energyPosition;
    }
    
    /*
     * Returns the energyposition (total load and market price with feedback) & EV profile
     */
    private Pair<EnergyPosition, double[]> createGridConnectionEVScheduleWithTrips(double timeAtStartForecast_h, EnergyPosition energyPosition, J_EAEV ev, List<Triple<Double, Double, Double>> trips, double currentSOC_kWh, double maximalStorageCapacity_kWh, double endTimeLastTrip_h, double maxPower_kW) {
    	double[] totalLoad_kW = energyPosition.load_kW();
    	double[] dailyPriceCurve_eurpMWh = energyPosition.marketPriceCurve_eurpMWh();
    	double[] evProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];
		double marketFeedback_eurpMWhpkW = this.NATIONAL_PRICE_ELASTICITY_EURPMWHPGW * (this.NATIONAL_COMMUTER_EV_POWER_GW + this.NATIONAL_NON_COMMUTER_EV_POWER_GW) / maxPower_kW;

    	for (Triple<Double, Double, Double> trip : trips) {
    		double startTime_h = trip.getLeft();
    		double endTime_h = trip.getMiddle();
    		double distance_km = trip.getRight();

    		double work_kWh = maximalStorageCapacity_kWh - currentSOC_kWh;
    		
    		// check for 'impossible trip'
    		double firstAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.ceil(endTimeLastTrip_h / this.timeParameters.getTimeStep_h());
    		double lastAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.floor(startTime_h / this.timeParameters.getTimeStep_h());
    		double maxWork_kWh = (lastAvailableChargingTime_h - firstAvailableChargingTime_h) * maxPower_kW;
    		if (maxWork_kWh < work_kWh) {
    			work_kWh = max(0, maxWork_kWh);
    		}
    		
    		boolean[] allowedOperatingTimes = new boolean[this.TIMESTEPS_IN_FORECAST];
    		Arrays.fill(allowedOperatingTimes, true);
    		for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
    			double t = i*this.timeParameters.getTimeStep_h();
    			if (t < firstAvailableChargingTime_h) {
    				allowedOperatingTimes[i] = false;
    			}
    			if (t > lastAvailableChargingTime_h) {
    				allowedOperatingTimes[i] = false;
    			}
    		}

    	    J_VirtualFlexAsset trip_ev = new J_VirtualFlexAsset( maxPower_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), this.FORECAST_TIME_H, allowedOperatingTimes );
    		//virtual_evs.add(trip_ev);
    		
    		J_Market market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedback_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);
    		
    		// Create a schedule for the asset and update the load profiles & price curve with market feedback.
    		totalLoad_kW = J_FlexAssetScheduler.scheduleWrapper(totalLoad_kW, trip_ev, work_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
    		// Update the EV schedule with this charging session.
    		for (int i =0; i < this.TIMESTEPS_IN_FORECAST; i++) {
    			evProfile_kW[i] += trip_ev.profile_kW[i];
    		}
    		// Update parameters relevant for other flex assets.
    		dailyPriceCurve_eurpMWh = market.getMarketFeedbackDailyPriceCurve_eurpMWh(trip_ev.profile_kW);
    		// Update parameters relevant for other trips of this ev.
    		currentSOC_kWh += work_kWh - distance_km * ev.getEnergyConsumption_kWhpkm();
    		endTimeLastTrip_h = endTime_h;
    	}
    	
    	energyPosition = new EnergyPosition(totalLoad_kW, dailyPriceCurve_eurpMWh);    	
    	return Pair.of(energyPosition, evProfile_kW);
    }
    
    /*
     * Returns the energyposition (total load and market price with feedback) & EV profile
     */
    private Pair<EnergyPosition, double[]> createGridConnectionEVScheduleWithoutTrips(double timeAtStartForecast_h, EnergyPosition energyPosition, J_EAEV ev, double currentSOC_kWh, double maximalStorageCapacity_kWh, double endTimeLastTrip_h, double maxPower_kW) {
	    // Very analogous to createGridConnectionEVScheduleWithTrips, except there is only one charging sessions, from the arrive time till the end of the day.
    	double[] totalLoad_kW = energyPosition.load_kW();
    	double[] dailyPriceCurve_eurpMWh = energyPosition.marketPriceCurve_eurpMWh();
    	double marketFeedback_eurpMWhpkW = this.NATIONAL_PRICE_ELASTICITY_EURPMWHPGW * (this.NATIONAL_COMMUTER_EV_POWER_GW + this.NATIONAL_NON_COMMUTER_EV_POWER_GW) / maxPower_kW;
	    J_Market market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedback_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);

    	double work_kWh = maximalStorageCapacity_kWh - currentSOC_kWh;
		double firstAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.ceil(endTimeLastTrip_h / this.timeParameters.getTimeStep_h());
		double lastAvailableChargingTime_h = 24.0;
		double maxWork_kWh = (lastAvailableChargingTime_h - firstAvailableChargingTime_h) * maxPower_kW;
		if (maxWork_kWh < work_kWh) {
			work_kWh = max(0, maxWork_kWh);
		}
		boolean[] allowedOperatingTimes = new boolean[this.TIMESTEPS_IN_FORECAST];
		Arrays.fill(allowedOperatingTimes, true);
		for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
			double t = i*this.timeParameters.getTimeStep_h();
			if (t < firstAvailableChargingTime_h) {
				allowedOperatingTimes[i] = false;
			}
		}
	    J_VirtualFlexAsset trip_ev = new J_VirtualFlexAsset( maxPower_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), this.FORECAST_TIME_H, allowedOperatingTimes );
		//virtual_evs.add(trip_ev);
		totalLoad_kW = J_FlexAssetScheduler.scheduleWrapper(totalLoad_kW, trip_ev, work_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
		double[] evProfile_kW = trip_ev.profile_kW;
		dailyPriceCurve_eurpMWh = market.getMarketFeedbackDailyPriceCurve_eurpMWh(evProfile_kW);
		
		energyPosition = new EnergyPosition(totalLoad_kW, dailyPriceCurve_eurpMWh);
		return Pair.of(energyPosition, evProfile_kW);
    }
    
    private EnergyPosition createBatterySchedules( double timeAtStartForecast_h, List<GridConnection> gcList, EnergyPosition energyPosition ) {
       	for (GridConnection gc : gcList) {
    		if (gc.p_batteryAsset != null) {
    			energyPosition = createGridConnectionBatterySchedule( timeAtStartForecast_h, gc, energyPosition );
    		}
    	}
       	return energyPosition;
    }
    
    private EnergyPosition createGridConnectionBatterySchedule(double timeAtStartForecast_h, GridConnection gc, EnergyPosition energyPosition) {
    	J_EAStorageElectric battery = gc.p_batteryAsset;
    	double[] totalLoad_kW = energyPosition.load_kW();
    	double[] dailyPriceCurve_eurpMWh = energyPosition.marketPriceCurve_eurpMWh();

    	// Are these values like current SOC off by one timestep? The schedule is made at 23:45, not 00:00?
		double maxPower_kW = battery.getCapacityElectric_kW();
		double batteryPowerStep_kW = maxPower_kW / this.POWERSTEPS_NR;
		double storageCapacity_kWh = battery.getStorageCapacity_kWh();
		double initialSOC_fr = battery.getCurrentStateOfCharge_fr();
		double etaCharge_fr = battery.getChargingEfficiency_r();
		double etaDischarge_fr = battery.getDischargingEfficiency_r();
		
		boolean[] allowedOperatingTimes = new boolean[this.TIMESTEPS_IN_FORECAST];
		Arrays.fill(allowedOperatingTimes, true);

		J_VirtualBattery virtualBattery = new J_VirtualBattery(
				maxPower_kW,
				batteryPowerStep_kW,
				storageCapacity_kWh,
				initialSOC_fr,
				etaCharge_fr,
				etaDischarge_fr,
				this.timeParameters.getTimeStep_h(),
				this.FORECAST_TIME_H,
				allowedOperatingTimes
				);

		double marketFeedback_eurpMWhpkW = this.NATIONAL_PRICE_ELASTICITY_EURPMWHPGW * this.NATIONAL_BATTERY_POWER_GW / maxPower_kW;
		J_Market market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedback_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);

		// Inconsistency in HOLON Scripts, FlexAssetScheduler returns total load, battery scheduler returns battery load.
		double[] batteryProfile_kW = J_BatteryScheduler.scheduleWrapper(totalLoad_kW, virtualBattery, market, this.timeParameters.getTimeStep_h(), this.PRICE_SPREAD_THRESHOLD_EURPMWH, this.SEPERATE_MARKET_AND_CONGESTION);
		for (int i =0; i < this.TIMESTEPS_IN_FORECAST; i++) {
			totalLoad_kW[i] += batteryProfile_kW[i];
		}

		batteryProfilesMap.put(gc.p_gridConnectionID, batteryProfile_kW);
    	
    	dailyPriceCurve_eurpMWh = market.getMarketFeedbackDailyPriceCurve_eurpMWh(batteryProfile_kW);
    	
    	energyPosition = new EnergyPosition(totalLoad_kW, dailyPriceCurve_eurpMWh);
    	return energyPosition;
    }
    
    private double[] getDailyPriceCurve( double timeAtStartForecast_h) {
    	J_ProfilePointer pp_dayAheadElectricityPricing_eurpMWh = this.energyCoop.energyModel.pp_dayAheadElectricityPricing_eurpMWh;

		int priceProfileTimeStepsInForecast = roundToInt(this.FORECAST_TIME_H / pp_dayAheadElectricityPricing_eurpMWh.getDataTimeStep_h());
		int indexAtStartForecast = roundToInt(timeAtStartForecast_h / pp_dayAheadElectricityPricing_eurpMWh.getDataTimeStep_h());
		int indexAtEndForecast = indexAtStartForecast + priceProfileTimeStepsInForecast;

		double[] dailyPriceCurve_eurpMWh = Arrays.copyOfRange( pp_dayAheadElectricityPricing_eurpMWh.getAllValues(), indexAtStartForecast, indexAtEndForecast);
		if (priceProfileTimeStepsInForecast != this.TIMESTEPS_IN_FORECAST) {
			int profileTimeStep_min = roundToInt(pp_dayAheadElectricityPricing_eurpMWh.getDataTimeStep_h()*60);
			int modelTimeStep_min = roundToInt(this.timeParameters.getTimeStep_h()*60);
			dailyPriceCurve_eurpMWh = this.resample(dailyPriceCurve_eurpMWh, profileTimeStep_min, modelTimeStep_min);
		}
		
		return dailyPriceCurve_eurpMWh;
    }
    
    private double[] estimateTotalLoadOtherGridConnectionsAndAssets( double timeAtStartForecast_h ) { 
    	// First Stage (Currently):
    	// Perfect prediction of base load, including base electricty consumption, pv production and hp consumption for dhw and space heating (both are profiles)
    	
    	// Second Stage (TODO):
    	// We can make a 'perfect' prediction of the base electricity consumption, PV production.
    	// We can not make a perfect prediction of the HP consumption. We assume the houses have a PI control management, and thus that the HP load at a timestep is equal to the average load over the day.
    	// The average load over the day is estimated from the parameters of the heatbuilding & 
    	
    	double[] totalLoad_kW = this.estimateTotalBaseLoad( timeAtStartForecast_h );
    	double[] heatingAssetsLoad_kW = this.estimateTotalHeatingLoad_kW( timeAtStartForecast_h );   	
    	double[] simpleChargingEVsLoad_kW = this.estimateSimpleChargingEVsLoad( timeAtStartForecast_h );
    	
    	this.energyCoop.loadDebugPlot.removeAll();
    	this.energyCoop.f_fillLoadDebugPlot(totalLoad_kW, "base load");
    	this.energyCoop.f_fillLoadDebugPlot(heatingAssetsLoad_kW, "heating");
    	this.energyCoop.f_fillLoadDebugPlot(simpleChargingEVsLoad_kW, "simple charging");
    	
    	for (int i = 0; i < totalLoad_kW.length; i++) {
    		totalLoad_kW[i] += heatingAssetsLoad_kW[i] + simpleChargingEVsLoad_kW[i];
    	}
    	
    	// TODO: Forecasting of SELFCONSUMPTION batteries
    	
    	this.energyCoop.f_fillLoadDebugPlot(totalLoad_kW, "total forecasted load");
    	
    	return totalLoad_kW;
    }

	
	
    private double[] estimateTotalBaseLoad(double timeAtStartForecast_h) {
    	double[] loadProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];
    	
    	for (GridConnection gc : this.energyCoop.f_getAllChildMemberGridConnections()) {
        	double[] gcLoad_kW = this.estimateGridConnectionBaseLoad( timeAtStartForecast_h, gc );
        	for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
        		loadProfile_kW[i] += gcLoad_kW[i];
        	}
    	}
    	
    	return loadProfile_kW;
    }
    
    private double[] estimateGridConnectionBaseLoad( double timeAtStartForecast_h, GridConnection gc ) {
    	double[] loadProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];
    	
		int indexAtStartForecast = roundToInt(timeAtStartForecast_h/timeParameters.getTimeStep_h());
		
		List<J_EAProfile> electricityProfiles = new ArrayList<>();
		electricityProfiles.addAll(findAll(gc.c_profileAssets, j_ea -> j_ea.energyCarrier == OL_EnergyCarriers.ELECTRICITY));

		for (J_EAProfile profile : electricityProfiles) {
			double scalar = profile.getProfileScaling_fr()*profile.getProfileUnitScaler_fr();
			if (profile instanceof J_EAProduction) {
				scalar *= -1;
			}
			for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
				loadProfile_kW[i] += scalar * profile.profilePointer.getValue((i + indexAtStartForecast)*timeParameters.getTimeStep_h());
			}
		}

    	return loadProfile_kW;
    }

    private double[] estimateTotalHeatingLoad_kW( double timeAtStartForecast_h ) {
    	double[] loadProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];
    	
    	for (GridConnection gc : this.energyCoop.f_getAllChildMemberGridConnections()) {
        	double[] gcLoad_kW = this.estimateGridConnectionHeatingLoad_kW( timeAtStartForecast_h, gc );

        	for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
        		loadProfile_kW[i] += gcLoad_kW[i];
        	}
    	}
    	
    	return loadProfile_kW;
    }
    
    private double[] estimateGridConnectionHeatingLoad_kW( double timeAtStartForecast_h, GridConnection gc ) {
    	double[] loadProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];

    	// If the GC is not heating using electricity, we return a profile of zeros
		switch (gc.f_getCurrentHeatingType()) {
			case ELECTRIC_HEATPUMP:
			case HYBRID_HEATPUMP:
				break;
			case CUSTOM:
				for (J_EAConversion heatingAsset : gc.c_heatingAssets) {
					if (heatingAsset.activeConsumptionEnergyCarriers.contains(OL_EnergyCarriers.ELECTRICITY)) {
						throw new RuntimeException(
							String.format("J_ISIE_Aggregator_EMS can not forecast the custom heating strategy in GridConnection: %s",
									gc.p_gridConnectionID)
							);
					}
				}
				break;
			default:
				return loadProfile_kW;
		}
		
		J_EAConversionHeatPump hp = (J_EAConversionHeatPump)findFirst( gc.c_heatingAssets, ea -> ea instanceof J_EAConversionHeatPump );
		J_ProfilePointer ambientTemperatures = this.energyCoop.energyModel.pp_ambientTemperature_degC;
		
		int indexAtStartForecast = roundToInt(timeAtStartForecast_h/timeParameters.getTimeStep_h());
		
    	List<J_EAProfile> heatProfiles = new ArrayList<>();
		heatProfiles.addAll(findAll(gc.c_profileAssets, ea -> ea.energyCarrier == OL_EnergyCarriers.HEAT));
		
		double[] buildingHeatDemandProfile_kW = this.getBuildingHeatDemandProfile( timeAtStartForecast_h, gc, ambientTemperatures);
		
		for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
			double t = timeAtStartForecast_h + i * timeParameters.getTimeStep_h();
			double heatDemand_kW = buildingHeatDemandProfile_kW[i];
			for (J_EAProfile profile : heatProfiles) {
				heatDemand_kW += profile.profilePointer.getValue(t);
			}
			double efficiency_fr = hp.calculateCOP(hp.getOutputTemperature_degC(), ambientTemperatures.getValue(t));
			double load_kW = heatDemand_kW / efficiency_fr;
			if (gc.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.HYBRID_HEATPUMP) {
				J_EAConversionGasBurner gasBurner = (J_EAConversionGasBurner)findFirst( gc.c_heatingAssets, ea -> ea instanceof J_EAConversionGasBurner );
				double gasBurnerOutputCapacity_kW = gasBurner.getOutputCapacity_kW();
				double heatPumpOutputCapacity_kW = hp.getInputCapacity_kW() * efficiency_fr; // Can't call getOutputCapacity_kW as it is dependend on the ambient temperature
				if (efficiency_fr < 3.0) {
					load_kW = max(0, heatDemand_kW - gasBurnerOutputCapacity_kW) / efficiency_fr;
				}
				else {
					load_kW = min(heatDemand_kW, heatPumpOutputCapacity_kW) / efficiency_fr;
				}
			}
			loadProfile_kW[i] += load_kW;
		}
		
		return loadProfile_kW;
    }
    
    private double[] getBuildingHeatDemandProfile( double timeAtStartForecast_h, GridConnection gc, J_ProfilePointer ambientTemperatures) {
		double[] buildingHeatDemandProfile_kW = new double[this.TIMESTEPS_IN_FORECAST];
		if (gc.p_BuildingThermalAsset != null) {
			J_EABuilding building = gc.p_BuildingThermalAsset;
			
			J_HeatingPreferences heatingPreferences = gc.f_getHeatingPreferences();
			double dayStartTime_h = heatingPreferences.getStartOfDayTime_h();
	    	double nightStartTime_h = heatingPreferences.getStartOfNightTime_h();
	    	
	    	double averageMorningTemperature_degC = calculateAverageAmbientTemperature_degC(0.0, dayStartTime_h, timeAtStartForecast_h, ambientTemperatures);
	    	double averageAfternoonTemperature_degC = calculateAverageAmbientTemperature_degC(dayStartTime_h, nightStartTime_h, timeAtStartForecast_h, ambientTemperatures);
	    	double averageEveningTemperature_degC = calculateAverageAmbientTemperature_degC(nightStartTime_h, 24.0, timeAtStartForecast_h, ambientTemperatures);
	    	
	    	double setpointIndoorTemperatureDayTime_DegC = heatingPreferences.getDayTimeSetPoint_degC();
	    	double setpointIndoorTemperatureNightTime_DegC = heatingPreferences.getNightTimeSetPoint_degC();
	    	
	        double morningHeatDemand_kW = calculateAverageDemandForSpaceHeating_kW( building, building.getCurrentTemperature(), setpointIndoorTemperatureNightTime_DegC, averageMorningTemperature_degC, dayStartTime_h );
	        double afternoonHeatDemand_kW = calculateAverageDemandForSpaceHeating_kW( building, setpointIndoorTemperatureNightTime_DegC, setpointIndoorTemperatureDayTime_DegC, averageAfternoonTemperature_degC, nightStartTime_h - dayStartTime_h );
	        double eveningHeatDemand_kW = calculateAverageDemandForSpaceHeating_kW( building, setpointIndoorTemperatureDayTime_DegC, setpointIndoorTemperatureNightTime_DegC, averageEveningTemperature_degC, 24.0 - nightStartTime_h );
	        
	        for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
	        	if (i * this.timeParameters.getTimeStep_h() < dayStartTime_h) {
	        		buildingHeatDemandProfile_kW[i] = morningHeatDemand_kW;
	        	}
	        	else if (i * this.timeParameters.getTimeStep_h() >= dayStartTime_h && i * this.timeParameters.getTimeStep_h() < nightStartTime_h) {
	        		buildingHeatDemandProfile_kW[i] = afternoonHeatDemand_kW;
	        	}
	        	else {
	        		buildingHeatDemandProfile_kW[i] = eveningHeatDemand_kW;
	        	}
	        }
		}
		return buildingHeatDemandProfile_kW;
    }
    private double calculateAverageAmbientTemperature_degC(double startTime_h, double endTime_h, double timeAtStartForecast_h, J_ProfilePointer ambientTemperatures) {
    	
    	double averageTemperature_degC = 0;
    	for (double t = timeAtStartForecast_h + startTime_h; t < timeAtStartForecast_h + endTime_h; t += this.timeParameters.getTimeStep_h()) {
    		averageTemperature_degC += ambientTemperatures.getValue(t);
    	}
    	averageTemperature_degC /= ((endTime_h - startTime_h) / this.timeParameters.getTimeStep_h());
    	
    	return averageTemperature_degC;
    }
 
    private double calculateAverageDemandForSpaceHeating_kW( J_EABuilding building, double currentIndoorTemperature_degC, double setpointIndoorTemperature_degC, double averageAmbientTemperature_degC, double timespan_h) {
       	
    	double lossFactor_WpK = building.getLossFactor_WpK();
    	double lossScalingFactor_fr = building.getLossScalingFactor_fr();
    	
		double heatLoss_kW = (lossFactor_WpK * (  setpointIndoorTemperature_degC - averageAmbientTemperature_degC ) / 1000) * lossScalingFactor_fr;
		double heatDelta_kWh = building.getHeatCapacity_JpK() / 3.6e6 * ( setpointIndoorTemperature_degC - currentIndoorTemperature_degC);
		
    	return  max(0, heatLoss_kW + heatDelta_kWh / timespan_h);
    }
    
    private double[] estimateSimpleChargingEVsLoad(double timeAtStartForecast_h) {
    	double[] EVsLoad_kW = new double[this.TIMESTEPS_IN_FORECAST];
    	
    	for (GridConnection gc : this.energyCoop.f_getAllChildMemberGridConnections()) {
    		if (gc.f_getChargingManagement() instanceof J_ChargingManagementSimple) {
	        	double[] EVLoad_kW = this.estimateSimpleChargingEVLoad( timeAtStartForecast_h, gc );
	
	        	for (int i = 0; i < EVsLoad_kW.length; i++) {
	        		EVsLoad_kW[i] += EVLoad_kW[i];
	        	}
    		}
    	}
    	
    	return EVsLoad_kW;
    }
    
    private double[] estimateSimpleChargingEVLoad( double timeAtStartForecast_h, GridConnection gc) {
    	double[] EVLoad_kW = new double[this.TIMESTEPS_IN_FORECAST];
    	
    	J_EAEV ev = gc.c_electricVehicles.get(0);
		J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
		List<Triple<Double, Double, Double>> trips = tripTracker.getTripsNext24Hours( timeAtStartForecast_h );
    	
		double currentSOC_kWh = ev.getCurrentSOC_kWh();
    	double maximalStorageCapacity_kWh = ev.getStorageCapacity_kWh();
    	double endTimeLastTrip_h = ev.getAvailability() ? 0.0 : (tripTracker.endtimes_min.get(tripTracker.eventIndex)%24) / 60.0; // assumes the current trip ends today (no trips > 24 hours exist)
	    double maxPower_kW = ev.capacityElectric_kW;
	    
		if (trips.size() > 0) {
	    	for (Triple<Double, Double, Double> trip : trips) {
	    		double startTime_h = trip.getLeft();
	    		double endTime_h = trip.getMiddle();
	    		double distance_km = trip.getRight();

	    		double work_kWh = maximalStorageCapacity_kWh - currentSOC_kWh;
	    		double firstAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.ceil(endTimeLastTrip_h / this.timeParameters.getTimeStep_h());
	    		double lastAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.floor(startTime_h / this.timeParameters.getTimeStep_h());
	    		double maxWork_kWh = (lastAvailableChargingTime_h - firstAvailableChargingTime_h) * maxPower_kW;
	    		if (maxWork_kWh < work_kWh) {
	    			work_kWh = max(0, maxWork_kWh);
	    		}
	    		for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
	    			double t = i*this.timeParameters.getTimeStep_h();
	    			if (t >= firstAvailableChargingTime_h && t <= lastAvailableChargingTime_h) {  // >= or > ?
	    				EVLoad_kW[i] = max(0, min(maxPower_kW, work_kWh / this.timeParameters.getTimeStep_h()));
	    				work_kWh -= EVLoad_kW[i] * this.timeParameters.getTimeStep_h();
	    				if (work_kWh == 0 || DoubleCompare.lessThanZero(work_kWh)) {
	    					break;
	    				}
	    			}
	    		}
	    		if (DoubleCompare.greaterThanZero((work_kWh))) {
	    			throw new RuntimeException("charging requirement of EV with simple charging strategy exceeded limit, when it should have been capped.");
	    		}
	    		// update parameters relevant for other trips of this ev.
	    		currentSOC_kWh += work_kWh - distance_km * ev.getEnergyConsumption_kWhpkm();
	    		endTimeLastTrip_h = endTime_h;
	    	}
	    }
	    else {
    		double work_kWh = maximalStorageCapacity_kWh - currentSOC_kWh;
    		double firstAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.ceil(endTimeLastTrip_h / this.timeParameters.getTimeStep_h());
    		double lastAvailableChargingTime_h = 24.0;
    		double maxWork_kWh = (lastAvailableChargingTime_h - firstAvailableChargingTime_h) * maxPower_kW;
    		if (maxWork_kWh < work_kWh) {
    			work_kWh = max(0, maxWork_kWh);
    		}
    		for (int i = 0; i < this.TIMESTEPS_IN_FORECAST; i++) {
    			double t = i*this.timeParameters.getTimeStep_h();
    			if (t >= firstAvailableChargingTime_h && t <= lastAvailableChargingTime_h) {  // >= or > ?
    				EVLoad_kW[i] = max(0, min(maxPower_kW, work_kWh / this.timeParameters.getTimeStep_h()));
    				work_kWh -= EVLoad_kW[i] * this.timeParameters.getTimeStep_h();
    				if (work_kWh == 0 || DoubleCompare.lessThanZero(work_kWh)) {
    					break;
    				}
    			}
    		}
    		if (DoubleCompare.greaterThanZero((work_kWh))) {
    			throw new RuntimeException("charging requirement of EV with simple charging strategy exceeded limit, when it should have been capped.");
    		}
	    }
    	return EVLoad_kW;
    }
    
    
    
	//Get inherent, supported and active Asset management classes
	public List<Class<? extends I_AggregatorAssetManagement>> getInternalAggregatorAssetManagements(){
		return this.internalAggregatorAssetManagements;
	}
	public List<Class<? extends I_AggregatorAssetManagement>> getSupportedExternalAggregatorAssetManagements(){
		return this.supportedExternalAggregatorAssetManagements;
	}
	public Map<Class<? extends I_AggregatorAssetManagement>, I_AggregatorAssetManagement> getActiveExternalAggregatorAssetManagements(){
		return this.activeExternalAggregatorAssetManagements;
	}  
	
    ////Store and reset states
	public void storeStatesAndReset() {
		// something with the schedules?
	}
	
	public void restoreStates() {
		// something with the schedules?
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

	public static double[] resample(double[] values, int sourceMinutes, int targetMinutes) {
        if (values == null || values.length == 0) return new double[0];
        if (sourceMinutes == targetMinutes) return values.clone();

        int resultLength = values.length * sourceMinutes / targetMinutes;
        double[] result = new double[resultLength];

        for (int i = 0; i < resultLength; i++) {
            double srcIndex = (double) i * targetMinutes / sourceMinutes;
            int lo = (int) srcIndex;
            int hi = Math.min(lo + 1, values.length - 1);
            double t = srcIndex - lo;

            result[i] = values[lo] + t * (values[hi] - values[lo]);
        }

        return result;
    }
}