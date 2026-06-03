import org.apache.commons.lang3.tuple.Triple;
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
	
	// These parameters contain the (for now hardcoded) values of the national energy system.
	// The market feedback parameters are based on these values.
	private double NATIONAL_PRICE_ELASTICITY_EURPMWHPGW = 5.0;
	private double NATIONAL_COMMUTER_EV_POWER_GW = 33.0;
	private double NATIONAL_NON_COMMUTER_EV_POWER_GW = 22.0;
	private double NATIONAL_HEATPUMP_POWER_GW = 8.0;
	private double NATIONAL_BATTERY_POWER_GW = 20.0;
	
	private int POWERSTEPS_NR = 20;
	
	private double SELFCONSUMPTIONSAVING_EURPMWH = 0.0;
	private double CONGESTIONFACTOR_EURPMWHPKW = 100.0;
	private double CONGESTIONDEADZONE_FR = 0.9;
	private double congestionDeadzone_kW;
	
	private double PRICE_SPREAD_THRESHOLD_EURPMWH = 10.0;
	private boolean SEPERATE_MARKET_AND_CONGESTION = false;
		
	private Map<String, double[]> evProfilesMap = new HashMap<String, double[]>();
    /**
     * Default constructor
     */
    public J_ISIE_Aggregator_EMS(EnergyCoop energyCoop, J_TimeParameters timeParameters) {
        	this.energyCoop = energyCoop;
        	this.timeParameters = timeParameters;
    }
    
    
    public void operateAggregatorEnergyManagement(J_TimeVariables timeVariables) {
    	// We make a schedule every day at midnight (00:00), after the start of the first timestep of that day. That means our assets will not have any defined behaviour at the first timestep of the model.
    	// The task is given to the External Setpoint AssetManagement to handle the make sure the asset is operated (with powerFraction 0).
    	// All other timesteps, we don't do anything here. Once the schedule is created, it is passed to the GC to be executed on its own.
    	
    	if (timeVariables.getTimeOfDay_h() == 0.0) {
    		this.evProfilesMap.clear();
    		this.createFlexAssetSchedules( timeVariables );
    	}
    	
    	for (GridConnection gc : this.energyCoop.f_getAllChildMemberGridConnections()) {
    		J_ChargePoint chargePoint = gc.f_getChargePoint();
    		if (chargePoint != null) {
	    		if (chargePoint.getCurrentActiveChargingRequests().size() > 0) {
	    			// for now assume max 1 ev per gc
	    			I_ChargingRequest chargingRequest = chargePoint.getCurrentActiveChargingRequests().get(0);
		   		
		    		double[] evProfile_kW = evProfilesMap.get(gc.p_gridConnectionID);
		    		int timeStepOfToday = roundToInt(timeVariables.getTimeOfDay_h() / this.timeParameters.getTimeStep_h());
		    		
		    		Map<I_ChargingRequest, Double> chargingSetpoints = new HashMap<I_ChargingRequest, Double>();
		    		
		    		chargingSetpoints.put(chargingRequest, evProfile_kW[timeStepOfToday]);
		    		
		    		J_ChargingManagementExternalSetpoint chargingManagement = (J_ChargingManagementExternalSetpoint)gc.f_getChargingManagement();
		    		chargingManagement.setChargingSetpoints(chargingSetpoints);
	    		}
    		}
    	}
    	
    	// In external charging management:
    	//   setChargingSetpoints(Map<I_ChargingRequest, Double> map_chargingSetpoints_kW) ?
    }
    
    
    private void createFlexAssetSchedules(J_TimeVariables timeVariables) {
    	// TEMPORARY. might need to move the initialization of this forecasting before engine intialization to the loader? Then we could set this in the constructor or something
		J_ProfileForecaster pf_ambientTemperature_degC = this.energyCoop.energyModel.pf_ambientTemperature_degC;
		J_ProfilePointer pp_dayAheadElectricityPricing_eurpMWh = this.energyCoop.energyModel.pp_dayAheadElectricityPricing_eurpMWh;
		// Some basic parameters
		int timeStepsInForecast = roundToInt(this.FORECAST_TIME_H / this.timeParameters.getTimeStep_h());
		double timeAtStartForecast_h = timeVariables.getT_h();
		int indexAtStartForecast = roundToInt(timeAtStartForecast_h/this.timeParameters.getTimeStep_h());
	
		// Start with the original price curve
		double[] dailyPriceCurve_eurpMWh = Arrays.copyOfRange( pp_dayAheadElectricityPricing_eurpMWh.getAllValues(), indexAtStartForecast, indexAtStartForecast + timeStepsInForecast );
		J_Market market;
		
    	double[] baseLoad_kW = this.estimateTotalBaseLoad( timeVariables );
    	double[] totalLoad_kW = Arrays.copyOf(baseLoad_kW, baseLoad_kW.length);

    	// Make a copy of the list of GCs and shuffle it, that way it is randomized which GCs have 'the most' flexibility.
    	List<GridConnection> gcList = new ArrayList<GridConnection>(this.energyCoop.f_getAllChildMemberGridConnections());
    	Collections.shuffle(gcList);
    	
    	for (GridConnection gc : gcList) {
    		
    		if (gc.c_electricVehicles.size() > 0) {
	    		double[] evProfile_kW = new double[baseLoad_kW.length];
	    		List<J_VirtualFlexAsset> virtual_evs = new ArrayList<>();
	
	    		
	    		//for (J_EAEV ev : gc.c_electricVehicles) {
	    		// for now assume 1 ev per household
	    		J_EAEV ev = gc.c_electricVehicles.get(0);
			
				J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
				List<Triple<Double, Double, Double>> trips = tripTracker.getTripsNext24Hours( timeVariables );
		    	// We create a virtual consumption asset for each trip, we can't allow the asset to delay the charging need of one trip until the next
		    	// We translate the distance to an amount of energy and the start/end time become allowedOperatingTimes
		    	    			
		    	double currentStorageCapacity_kWh = ev.getCurrentSOC_kWh();
		    	double maximalStorageCapacity_kWh = ev.getStorageCapacity_kWh();
		    	double endTimeLastTrip_h = ev.getAvailability() ? 0.0 : (tripTracker.endtimes_min.get(tripTracker.eventIndex)%24) / 60.0; // assumes the current trip ends today (no trips > 24 hours exist)
	
	
		    	for (Triple<Double, Double, Double> trip : trips) {
		    		double startTime_h = trip.getLeft();
		    		double endTime_h = trip.getMiddle();
		    		double distance_km = trip.getRight();
		    		// TODO: BUGFIX: Take the SOC into account? at least for the first/current trip
		    		double work_kWh = distance_km * ev.getEnergyConsumption_kWhpkm();
		    		
		    		boolean[] allowedOperatingTimes = new boolean[timeStepsInForecast];
		    		Arrays.fill(allowedOperatingTimes, true);
		    		for (int i = 0; i < timeStepsInForecast; i++) {
		    			if (i*this.timeParameters.getTimeStep_h() < endTimeLastTrip_h) {
		    				allowedOperatingTimes[i] = false;
		    			}
		    			if ( i*this.timeParameters.getTimeStep_h() >= startTime_h - 2*this.timeParameters.getTimeStep_h()) {  // >= or > ?
		    				allowedOperatingTimes[i] = false;
		    			}
		    		}
		    	    double maxPower_kW = ev.capacityElectric_kW;
		    		    	    
		    	    // bound the work by the amount of charging time & power available
		    		work_kWh = min(work_kWh, max(0,(endTime_h - startTime_h - this.timeParameters.getTimeStep_h())) * maxPower_kW);
		    		    		
		    	    J_VirtualFlexAsset trip_ev = new J_VirtualFlexAsset( maxPower_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), this.FORECAST_TIME_H, allowedOperatingTimes );
		    		virtual_evs.add(trip_ev);
		    		
		    		double marketFeedback_eurpMWhpkW = this.NATIONAL_PRICE_ELASTICITY_EURPMWHPGW * (this.NATIONAL_COMMUTER_EV_POWER_GW + this.NATIONAL_NON_COMMUTER_EV_POWER_GW) / maxPower_kW;
		    		market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedback_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);
		    		
		    		// Create a schedule for the asset and update the total load & price curve with market feedback.
		    		evProfile_kW = J_FlexAssetScheduler.scheduleWrapper(totalLoad_kW, trip_ev, work_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
		    		dailyPriceCurve_eurpMWh = market.getDailyPriceCurve_eurpMWh();
		    		
		    		endTimeLastTrip_h = endTime_h;
		    	}
		    	
		    	evProfilesMap.put(gc.p_gridConnectionID, evProfile_kW);
		    	
	        	for (int i = 0; i < totalLoad_kW.length; i++) {
	        		totalLoad_kW[i] += evProfile_kW[i];
	        	}
	
	        	//}
    		}
    	}
    	
	}


    private double[] estimateTotalBaseLoad(J_TimeVariables timeVariables) {
    	// First Stage (Currently):
    	// Perfect prediction of base load, including base electricty consumption, pv production and hp consumption for dhw and space heating (both are profiles)
    	
    	// Second Stage (TODO):
    	// We can make a 'perfect' prediction of the base electricity consumption, PV production.
    	// We can not make a perfect prediction of the HP consumption. We assume the houses have a PI control management, and thus that the HP load at a timestep is equal to the average load over the day.
    	// The average load over the day is estimated from the parameters of the heatbuilding & 
    	
		int timeStepsInForecast = roundToInt(this.FORECAST_TIME_H / this.timeParameters.getTimeStep_h());
    	double[] baseLoad_kW = new double[timeStepsInForecast];
    	
    	for (GridConnection gc : this.energyCoop.f_getAllChildMemberGridConnections()) {
        	double[] gcLoad_kW = this.estimateGridConnectionBaseLoad( timeVariables, gc );

        	for (int i = 0; i < baseLoad_kW.length; i++) {
        		baseLoad_kW[i] += gcLoad_kW[i];
        	}
    	}
    	
    	return baseLoad_kW;
    }
    
    private double[] estimateGridConnectionBaseLoad( J_TimeVariables timeVariables, GridConnection gc ) {
    	// TODO: Move this method as a function to the GC Agent class?
		int timeStepsInForecast = roundToInt(this.FORECAST_TIME_H / this.timeParameters.getTimeStep_h());
    	double[] baseLoad_kW = new double[timeStepsInForecast];
    	
		double timeAtStartForecast_h = timeVariables.getT_h(); // TODO: Check this! off by one error likely
		int indexAtStartForecast = roundToInt(timeAtStartForecast_h/timeParameters.getTimeStep_h());
		
		
		// ELECTRICITY
		List<J_EAProfile> electricityProfiles = new ArrayList<>();
		electricityProfiles.addAll(findAll(gc.c_profileAssets, j_ea -> j_ea.energyCarrier == OL_EnergyCarriers.ELECTRICITY));

		for (J_EAProfile profile : electricityProfiles) {
			double scalar = profile.getProfileScaling_fr()*profile.getProfileUnitScaler_fr();
			if (profile instanceof J_EAProduction) {
				scalar *= -1;
			}
			for (int i = 0; i < baseLoad_kW.length; i++) {
				baseLoad_kW[i] += scalar * profile.profilePointer.getValue((i + indexAtStartForecast)*timeParameters.getTimeStep_h());
			}
		}
		
		// HEAT
		List<J_EAProfile> heatProfiles = new ArrayList<>();
		heatProfiles.addAll(findAll(gc.c_profileAssets, j_ea -> j_ea.energyCarrier == OL_EnergyCarriers.HEAT));
		
		if ( heatProfiles.size() > 0 ) {
			// Now we can 'safely' assume there is a heating asset.
			J_EAConversion heatingAsset = gc.c_heatingAssets.get(0);
			if (heatingAsset != null && heatingAsset.activeConsumptionEnergyCarriers.contains(OL_EnergyCarriers.ELECTRICITY)) {
				J_ProfilePointer ambientTemperatures = this.energyCoop.energyModel.pp_ambientTemperature_degC;
				for (J_EAProfile profile : heatProfiles) {
					for (int i = 0; i < baseLoad_kW.length; i++) {
						double t = timeAtStartForecast_h + i * timeParameters.getTimeStep_h();
						double efficiency = 1.0;
						if (heatingAsset instanceof J_EAConversionElectricHeater electricHeater) {
							efficiency = electricHeater.getEta_r();
						}
						else if (heatingAsset instanceof J_EAConversionHeatPump heatPump) {
							// TODO: Fix this for other ambientTempTypes
							efficiency = heatPump.calculateCOP(heatPump.getOutputTemperature_degC(), ambientTemperatures.getValue(t));
						}
						else {
							throw new RuntimeException("Unknown heating asset type in J_ISIE_Aggregator_EMS, GC with ID: " + gc.p_gridConnectionID + " has a heating asset of type: " + heatingAsset.getClass());
						}
						baseLoad_kW[i] += profile.profilePointer.getValue((i + indexAtStartForecast)*timeParameters.getTimeStep_h()) / efficiency;
					}
				}
			}
		}

    	return baseLoad_kW;
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

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}