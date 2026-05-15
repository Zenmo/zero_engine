import org.apache.commons.lang3.tuple.Triple;

/**
 * J_ISIE_EMS
 */	

public class J_ISIE_EMS implements I_EnergyManagement {

	/*
	 * The ISIE EMS works as follows:
	 * For all the flexible assets a 'virtual' asset is created. This asset stores information such as:
	 * The allowed times to operate the asset,
	 * The minimal/maximal power,
	 * 
	 * For each flex asset a J_Market object is created, which contains the feedback term for that specific asset.
	 * 
	 * One by one these assets are scheduled, each schedule updates the price curve, which should be passed into the next market object.
	 * 
	 * The 'merit' order of the assets is: EV, HP, Battery
	 * Other assets are currently not supported. TODO: Add curtailment of PV
	 * 
	 * Currently the EMS always assumes: 
	 * 1. You are simulating a full year,
	 * 2. You are scheduling for one full day, at midnight, which will be executed exactly,
	 * 3. Your vehicle is a single J_EAEV with a single triptracker,
	 * 4. Your heating asset is a single J_EAConversionHeatPump,
	 * 5. Your heating asset is heating a J_EABuilding.
	 */
	
	private GridConnection GC;
    private J_TimeParameters timeParameters;
	List<Class<? extends I_AssetManagement>> internalAssetManagements  = new ArrayList<>(Arrays.asList(
													I_HeatingManagement.class,
													I_ChargingManagement.class, 
													I_BatteryManagement.class
												));
	List<Class<? extends I_AssetManagement>> supportedExternalAssetManagements = new ArrayList<>();
	Map<Class<? extends I_AssetManagement>, I_AssetManagement> activeExternalAssetManagements = new HashMap();	
	
	boolean isChecked = false;
	
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
	
	private I_Vehicle ev; // sometimes assumed to be of type J_EAEV
	private List<J_VirtualFlexAsset> virtual_evs = new ArrayList<>();
	private J_EAConversionHeatPump hp;
	private List<J_VirtualFlexAsset> virtual_hps = new ArrayList<>();
	private J_EAStorageElectric battery;
	private J_VirtualBattery virtual_battery;
	
	private J_ActivityTrackerTrips tripTracker;
	private J_ChargePoint chargePoint;

	/*
	private J_Market market_ev;
	private J_Market market_hp;
	private J_Market market_battery;
	 */
	
	private J_EABuilding building;
	private J_HeatingPreferences heatingPreferences;
	
	private List<J_EAProfile> electricityProfiles = new ArrayList<>();
	private J_ProfilePointer pp_ambientTemperature_degC;
	private J_ProfileForecaster pf_ambientTemperature_degC;
	private J_ProfilePointer pp_dayAheadElectricityPricing_eurpMWh;
	
	private double HP_COP_atStartOfDay;
    /**
     * Default constructor
     */
    public J_ISIE_EMS(GridConnection GC, J_TimeParameters timeParameters) {
    	this.GC = GC;
    	this.timeParameters = timeParameters;
    	this.chargePoint = GC.f_getChargePoint();
    	this.ev = GC.c_electricVehicles.get(0);
   		this.tripTracker = GC.c_tripTrackers.get(0);
   		this.hp = (J_EAConversionHeatPump)GC.c_heatingAssets.get(0);
   		this.battery = GC.p_batteryAsset;
   		
   		this.building = GC.p_BuildingThermalAsset;
   		// heating preferences?
   		
		this.electricityProfiles.addAll(findAll(GC.c_profileAssets, j_ea -> j_ea.energyCarrier == OL_EnergyCarriers.ELECTRICITY));
		this.pp_ambientTemperature_degC = GC.energyModel.pp_ambientTemperature_degC;
		// forcasters not yet initialized in the loader
		//this.pf_ambientTemperature_degC = GC.energyModel.pf_ambientTemperature_degC;
		this.pp_dayAheadElectricityPricing_eurpMWh = GC.energyModel.pp_dayAheadElectricityPricing_eurpMWh;
		
		//this.congestionDeadzone_kW = this.CONGESTIONDEADZONE_FR * GC.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW();
		this.congestionDeadzone_kW = this.CONGESTIONDEADZONE_FR * 2.0;
    }
    
    /*
     * Main method that is called every timestep to operate all the assets,
     * Every day this method also calls the method to create new schedules.
     */
    public void manageFlexAssets(J_TimeVariables timeVariables) {
    	if ( !isChecked ) {
    		this.checkConfiguration(GC.c_flexAssets);
    	}
    	
    	if ( timeVariables.getTimeOfDay_h() == 0 ) {
    		this.createFlexAssetSchedules(timeVariables);
    	}
    	
    	int currentTimeStepOfDay = roundToInt(timeVariables.getTimeOfDay_h() / this.timeParameters.getTimeStep_h());
    	for (J_VirtualFlexAsset virtual_ev : this.virtual_evs) {
    		if (virtual_ev.allowedOperatingTimes[currentTimeStepOfDay]) {
    			this.GC.f_updateFlexAssetFlows( (J_EAEV)this.ev, virtual_ev.profile_kW[currentTimeStepOfDay] / ((J_EAEV)this.ev).capacityElectric_kW, timeVariables );
    		}
    	}
    	
    	for (J_VirtualFlexAsset virtual_hp : this.virtual_hps) {
    		if (virtual_hp.allowedOperatingTimes[currentTimeStepOfDay]) {
    			this.GC.f_updateFlexAssetFlows( this.hp, virtual_hp.profile_kW[currentTimeStepOfDay] / this.hp.getInputCapacity_kW(), timeVariables );
    	    	double heatIntoBuilding_kW = virtual_hp.profile_kW[currentTimeStepOfDay] * this.hp.getCOP();
    	    	this.GC.f_updateFlexAssetFlows( this.building, heatIntoBuilding_kW / this.building.getCapacityHeat_kW(), timeVariables );
    		}
    	}

		if (this.battery != null) {
			this.GC.f_updateFlexAssetFlows( this.battery, virtual_battery.profile_kW[currentTimeStepOfDay] / this.battery.getCapacityElectric_kW(), timeVariables );
		}
    }
    
    /*
     * Method that is called every day to create all the schedules.
     * 
     */
    private void createFlexAssetSchedules(J_TimeVariables timeVariables) {
    	// TEMPORARY. might need to move the initialization of this forecasting before engine intialization to the loader?
    	this.pf_ambientTemperature_degC = GC.energyModel.pf_ambientTemperature_degC;
    	
    	// Some basic parameters
	    double length_h = 24.0;
    	int timeStepsInADay = roundToInt(24.0 / this.timeParameters.getTimeStep_h());
		double timeAtStartForecast_h = timeVariables.getT_h();
		int indexAtStartForecast = roundToInt(timeAtStartForecast_h/this.timeParameters.getTimeStep_h());

		// Start with the original price curve
		double[] dailyPriceCurve_eurpMWh = Arrays.copyOfRange( this.pp_dayAheadElectricityPricing_eurpMWh.getAllValues(), indexAtStartForecast, indexAtStartForecast + timeStepsInADay );
		J_Market market;
		
    	// TODO: Make a function in the GC that calculates this baseload
    	double[] dailyLoad_kW = new double[timeStepsInADay];
    	for (J_EAProfile profile : electricityProfiles) {
			double scalar = profile.getProfileScaling_fr()*profile.getProfileUnitScaler_fr();
			if (profile instanceof J_EAProduction) {
				scalar *= -1;
			}
			for (int i = 0; i < dailyLoad_kW.length; i++) {
				dailyLoad_kW[i] += scalar * profile.profilePointer.getValue((i + indexAtStartForecast)*timeParameters.getTimeStep_h());
			}
		}

    	// Delete any old evs
    	this.virtual_evs.clear();
    	// We ask the triptracker what trips are start/ending in the next 24 hours and the distances of each trip.
    	List<Triple<Double, Double, Double>> trips = this.tripTracker.getTripsNext24Hours( timeVariables );
    	// We create a virtual consumption asset for each trip, we can't allow the asset to delay the charging need of one trip until the next
    	// We translate the distance to an amount of energy and the start/end time become allowedOperatingTimes
    	
    	double currentStorageCapacity_kWh = ((J_EAEV)this.ev).getCurrentSOC_kWh();
    	double maximalStorageCapacity_kWh = ((J_EAEV)this.ev).getStorageCapacity_kWh();
    	
    	double endTimeLastTrip_h = ev.getAvailability() ? 0.0 : (this.tripTracker.endtimes_min.get(tripTracker.eventIndex)%24) / 60.0; // assumes the current trip ends today (no trips > 24 hours exist)
    	for (Triple<Double, Double, Double> trip : trips) {
    		double startTime_h = trip.getLeft();
    		double endTime_h = trip.getMiddle();
    		double distance_km = trip.getRight();
    		// TODO: BUGFIX: Take the SOC into account? at least for the first/current trip
    		double work_kWh = distance_km * this.ev.getEnergyConsumption_kWhpkm();
    		
    		boolean[] allowedOperatingTimes = new boolean[timeStepsInADay];
    		Arrays.fill(allowedOperatingTimes, true);
    		for (int i = 0; i < timeStepsInADay; i++) {
    			if (i*this.timeParameters.getTimeStep_h() < endTimeLastTrip_h) {
    				allowedOperatingTimes[i] = false;
    			}
    			if ( i*this.timeParameters.getTimeStep_h() >= startTime_h - 2*this.timeParameters.getTimeStep_h()) {  // >= or > ?
    				allowedOperatingTimes[i] = false;
    			}
    		}
    	    double maxPower_kW = ((J_EAEV)ev).capacityElectric_kW;
    		    	    
    	    // bound the work by the amount of charging time & power available
    		work_kWh = min(work_kWh, max(0,(endTime_h - startTime_h - this.timeParameters.getTimeStep_h())) * maxPower_kW);
    		    		
    	    J_VirtualFlexAsset trip_ev = new J_VirtualFlexAsset( maxPower_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), length_h, allowedOperatingTimes );
    		this.virtual_evs.add(trip_ev);
    		
    		double marketFeedback_eurpMWhpkW = this.NATIONAL_PRICE_ELASTICITY_EURPMWHPGW * (this.NATIONAL_COMMUTER_EV_POWER_GW + this.NATIONAL_NON_COMMUTER_EV_POWER_GW) / maxPower_kW;
    		market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedback_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);
    		
    		// Create a schedule for the asset and update the total load & price curve with market feedback.
    		dailyLoad_kW = J_FlexAssetScheduler.scheduleWrapper(dailyLoad_kW, trip_ev, work_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
    		dailyPriceCurve_eurpMWh = market.getDailyPriceCurve_eurpMWh();
    		
    		endTimeLastTrip_h = endTime_h;
    	}
    	
    	
    	this.virtual_hps.clear();
    	
    	// We estimate the heat demand of the thermal building and give the heatpump the entire day to fulfill this demand.
    	// Morning HP
    	// Afternoon HP
    	// Evening HP
    	double dayStartTime_h = this.heatingPreferences.getStartOfDayTime_h();
    	double nightStartTime_h = this.heatingPreferences.getStartOfNightTime_h();
    	
    	double averageMorningTemperature_degC = calculateAverageAmbientTemperature_degC(0.0, dayStartTime_h, timeVariables);
    	double averageAfternoonTemperature_degC = calculateAverageAmbientTemperature_degC(dayStartTime_h, nightStartTime_h, timeVariables);
    	double averageEveningTemperature_degC = calculateAverageAmbientTemperature_degC(nightStartTime_h, 24.0, timeVariables);
    	
    	double setpointIndoorTemperatureDayTime_DegC = this.heatingPreferences.getDayTimeSetPoint_degC();
    	double setpointIndoorTemperatureNightTime_DegC = this.heatingPreferences.getNightTimeSetPoint_degC();
    	
        double morningHPWork_kWh = calculateElectricityDemandForSpaceHeating_kWh( this.building.getCurrentTemperature(), setpointIndoorTemperatureNightTime_DegC, averageMorningTemperature_degC, dayStartTime_h );
        double afternoonHPWork_kWh = calculateElectricityDemandForSpaceHeating_kWh( setpointIndoorTemperatureNightTime_DegC, setpointIndoorTemperatureDayTime_DegC, averageAfternoonTemperature_degC, nightStartTime_h - dayStartTime_h );
        double eveningHPWork_kWh = calculateElectricityDemandForSpaceHeating_kWh( setpointIndoorTemperatureDayTime_DegC, setpointIndoorTemperatureNightTime_DegC, averageEveningTemperature_degC, 24.0 - nightStartTime_h );

    	double maxPowerHP_kW = this.hp.getInputCapacity_kW();

    	boolean[] allowedOperatingTimesMorning = getAllowedOperatingTimesArray(length_h, 0.0, dayStartTime_h);
    	boolean[] allowedOperatingTimesAfternoon = getAllowedOperatingTimesArray(length_h, dayStartTime_h, nightStartTime_h);
    	boolean[] allowedOperatingTimesEvening = getAllowedOperatingTimesArray(length_h, nightStartTime_h, 24.0);
    	
    	J_VirtualFlexAsset morningHP = new J_VirtualFlexAsset( maxPowerHP_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), length_h, allowedOperatingTimesMorning );
    	J_VirtualFlexAsset afternoonHP = new J_VirtualFlexAsset( maxPowerHP_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), length_h, allowedOperatingTimesAfternoon );
    	J_VirtualFlexAsset eveningHP = new J_VirtualFlexAsset( maxPowerHP_kW, this.POWERSTEPS_NR, this.timeParameters.getTimeStep_h(), length_h, allowedOperatingTimesEvening );

    	this.virtual_hps.add(morningHP);
    	this.virtual_hps.add(afternoonHP);
    	this.virtual_hps.add(eveningHP);
    	
		double marketFeedbackHP_eurpMWhpkW = this.NATIONAL_PRICE_ELASTICITY_EURPMWHPGW * this.NATIONAL_HEATPUMP_POWER_GW / maxPowerHP_kW;
	
		// Create a schedule for the asset and update the total load & price curve with market feedback.
		market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedbackHP_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);		
		dailyLoad_kW = J_FlexAssetScheduler.scheduleWrapper(dailyLoad_kW, morningHP, morningHPWork_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
		dailyPriceCurve_eurpMWh = market.getDailyPriceCurve_eurpMWh();
		
		market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedbackHP_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);		
		dailyLoad_kW = J_FlexAssetScheduler.scheduleWrapper(dailyLoad_kW, afternoonHP, afternoonHPWork_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
		dailyPriceCurve_eurpMWh = market.getDailyPriceCurve_eurpMWh();
		
		market = new J_Market(dailyPriceCurve_eurpMWh, marketFeedbackHP_eurpMWhpkW, this.SELFCONSUMPTIONSAVING_EURPMWH, this.congestionDeadzone_kW, this.CONGESTIONFACTOR_EURPMWHPKW);		
		dailyLoad_kW = J_FlexAssetScheduler.scheduleWrapper(dailyLoad_kW, eveningHP, eveningHPWork_kWh, market, this.timeParameters.getTimeStep_h(), this.SEPERATE_MARKET_AND_CONGESTION);
		dailyPriceCurve_eurpMWh = market.getDailyPriceCurve_eurpMWh();
				
    	
    	// Battery
		if (this.battery != null) {
			double maxPowerBattery_kW = this.battery.getCapacityElectric_kW();
			double batteryPowerStep_kW = maxPowerBattery_kW / this.POWERSTEPS_NR;
			double storageCapacity_kWh = this.battery.getStorageCapacity_kWh();
			double initialSOC_fr = this.battery.getCurrentStateOfCharge_fr();
			double etaCharge_fr = this.battery.etaCharge_r;
			double etaDischarge_fr = this.battery.etaDischarge_r;
			
	    	this.virtual_battery = new J_VirtualBattery( 
	    			maxPowerBattery_kW, 
	    			batteryPowerStep_kW,
	    			storageCapacity_kWh,
	    			initialSOC_fr,
	    			etaCharge_fr,
	    			etaDischarge_fr,
	    			this.timeParameters.getTimeStep_h(),
	    			length_h,
	    			null);
	
			// Create a schedule for the asset and update the total load & price curve with market feedback.
			dailyLoad_kW = J_BatteryScheduler.scheduleWrapper(dailyLoad_kW, this.virtual_battery, market, this.timeParameters.getTimeStep_h(), this.PRICE_SPREAD_THRESHOLD_EURPMWH, this.SEPERATE_MARKET_AND_CONGESTION);
			
			// not needed if this is the final asset? But perhaps useful when this curve is passed to other GCs?
			dailyPriceCurve_eurpMWh = market.getDailyPriceCurve_eurpMWh();
		}
		
    }
    
    private boolean[] getAllowedOperatingTimesArray( double length_h, double startTime_h, double endTime_h) {
    	int timeSteps = roundToInt(length_h / this.timeParameters.getTimeStep_h());
    	boolean[] allowedOperatingTimes = new boolean[timeSteps];
		Arrays.fill(allowedOperatingTimes, true);
		for (int i = 0; i < timeSteps; i++) {
			if (i*this.timeParameters.getTimeStep_h() < startTime_h) {
				allowedOperatingTimes[i] = false;
			}
			if ( i*this.timeParameters.getTimeStep_h() >= endTime_h) {
				allowedOperatingTimes[i] = false;
			}
		}
    	return allowedOperatingTimes;		
    }
    
    private double calculateAverageAmbientTemperature_degC(double startTime_h, double endTime_h, J_TimeVariables timeVariables) {
    	
    	double averageTemperature_degC = 0;
    	for (double t = timeVariables.getT_h() + startTime_h; t < timeVariables.getT_h() + endTime_h; t += this.timeParameters.getTimeStep_h()) {
    		averageTemperature_degC += this.pp_ambientTemperature_degC.getValue(t);
    	}
    	averageTemperature_degC /= ((endTime_h - startTime_h) / this.timeParameters.getTimeStep_h());
    	
    	return averageTemperature_degC;
    }
    
    /*
     * For a given current indoor temperature this method calculates the average heat demand for the next timespan_h hours to achieve an indoor setpoint temperature for some average outdoor temperature
     */
    private double calculateElectricityDemandForSpaceHeating_kWh( double currentIndoorTemperature_degC, double setpointIndoorTemperature_degC, double averageAmbientTemperature_degC, double timespan_h) {
   	
    	double lossFactor_WpK = this.building.getLossFactor_WpK();
    	double lossScalingFactor_fr = this.building.getLossScalingFactor_fr();
    	double averageCOP = this.hp.calculateCOP(this.hp.getOutputTemperature_degC(), averageAmbientTemperature_degC);

		double heatLoss_kW = (lossFactor_WpK * (  setpointIndoorTemperature_degC - averageAmbientTemperature_degC ) / 1000) * lossScalingFactor_fr;
		double heatDelta_kWh = this.building.getHeatCapacity_JpK() / 3.6e6 * ( setpointIndoorTemperature_degC - currentIndoorTemperature_degC);
		   	
    	return max(0, timespan_h * heatLoss_kW + heatDelta_kWh) / averageCOP;
    }
    
    /*
     * Helper method to calculate the daily (electric) demand of the heatpump. This is a very rough approximation and will need to be improved.
     * Currently this method ignores:
     * 1. Different setpoints during the day,
     * 2. Different COP efficiencies during the day,
     * 3. Different losses to the outside during the day,
     * 4. Solar irradiance.
     */
    //private double calculateDailyWorkHeatPump_kWh(J_TimeVariables timeVariables) {
    	// First improvement. Split the heat demand into 3 parts. 
    	// 00:00 - Start of Day
    	// Start of Day - Start of Night
    	// Start of Night - 24:00
    	
    	/*
    	double dayStartTime_h = this.heatingPreferences.getStartOfDayTime_h();
    	double nightStartTime_h = this.heatingPreferences.getStartOfNightTime_h();

    	double averageMorningTemperature_degC = calculateAverageAmbientTemperature_degC(0.0, dayStartTime_h, timeVariables);
    	double averageAfternoonTemperature_degC = calculateAverageAmbientTemperature_degC(dayStartTime_h, nightStartTime_h, timeVariables);
    	double averageEveningTemperature_degC = calculateAverageAmbientTemperature_degC(nightStartTime_h, 24.0, timeVariables);
    	*/
    	
    	// We will create 3 seperate virtual assets and we calculate/estimate the heat demand for each.
    	// For the first asset we can look at the current temperature, for the second and third we assume the previous schedule achieves the setpoint.
    	

    	
    	
    	/*
    	// The forecasting time is hardcoded in the energyModel ! 
    	double averageAmbientTemperature_degC = this.pf_ambientTemperature_degC.getForecast();
    	double currentIndoorTemperature_degC = this.building.getCurrentTemperature();
    	double setpointIndoorTemperature_degC = this.heatingPreferences.getDayTimeSetPoint_degC();
    	double lossFactor_WpK = this.building.getLossFactor_WpK();
    	double lossScalingFactor_fr = this.building.getLossScalingFactor_fr();
    	this.HP_COP_atStartOfDay = this.hp.getCOP();
    	
		double heatLoss_kW = (lossFactor_WpK * (  setpointIndoorTemperature_degC - averageAmbientTemperature_degC ) / 1000) * lossScalingFactor_fr;
		double heatDelta_kWh = this.building.getHeatCapacity_JpK() / 3.6e6 * ( setpointIndoorTemperature_degC - currentIndoorTemperature_degC);
		
    	return max(0, 24.0 * heatLoss_kW + heatDelta_kWh) / this.HP_COP_atStartOfDay;
    	*/
    //}

	//Specific child management activation
	public void setV2GActive(boolean enableV2G) {
		if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
			this.getExternalAssetManagement(I_ChargingManagement.class).setV2GActive(enableV2G);
		}
	}
    public boolean getV2GActive() {
		if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
			return this.getExternalAssetManagement(I_ChargingManagement.class).getV2GActive();
		}
		else {
			return false;
		}
    }
    
    //Get child management types
  	public OL_GridConnectionHeatingType getCurrentHeatingType() {
  		if(this.getExternalAssetManagement(I_HeatingManagement.class) != null) {
  			return this.getExternalAssetManagement(I_HeatingManagement.class).getCurrentHeatingType();
  		}
  		else {
  			return OL_GridConnectionHeatingType.NONE;
  		}
  	}
  	public OL_ChargingAttitude getCurrentChargingType() {
  		if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
  			return this.getExternalAssetManagement(I_ChargingManagement.class).getCurrentChargingType();
  		}
  		else {
  			return OL_ChargingAttitude.NONE;
  		}
  	}
  	

	//Get inherent, supported and active Asset management classes
	public List<Class<? extends I_AssetManagement>> getInternalAssetManagements(){
		return this.internalAssetManagements;
	}
	public List<Class<? extends I_AssetManagement>> getSupportedExternalAssetManagements(){
		return this.supportedExternalAssetManagements;
	}
	public Map<Class<? extends I_AssetManagement>, I_AssetManagement> getActiveExternalAssetManagements(){
		return this.activeExternalAssetManagements;
	}    
	
	////Checks
	public void checkConfigurationEMSSpecific(List<J_EAFlex> flexAssetsGCList) {
		
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

    ////Store and reset states
	public void storeStatesAndReset() {
		activeExternalAssetManagements.values().forEach(subManagement -> subManagement.storeStatesAndReset());
	}
	public void restoreStates() {
		activeExternalAssetManagements.values().forEach(subManagement -> subManagement.restoreStates());
	}
	
  	public void f_setHeatingPreferences(J_HeatingPreferences heatingPreferences) {
  		this.heatingPreferences = heatingPreferences;
  	}
  	
  	@Override
	public String toString() {
		return super.toString();
	}

}