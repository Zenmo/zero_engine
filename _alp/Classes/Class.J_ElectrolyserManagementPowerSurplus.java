/**
 * J_ElectrolyserManagementPowerSurplus
 */	
public class J_ElectrolyserManagementPowerSurplus implements I_ElectrolyserManagement {
	
	private GridConnection gc;
	private J_TimeParameters timeParameters;

	//Management specific
	private Agent target;
	private List<Double> forecastRES_kW = new ArrayList<>();
	private List<Double> forecastTargetPowerFlows_kW = new ArrayList<>();
	private DataSet lastWeekTargetPowerFlows_kW = new DataSet(672);
	
	private boolean b_forecast_lastWeekBased = false; // EMS functionality option: Determine forcast based on last weeks power.
	
	//Storing
	private List<Double> storedForecastRES_kW = new ArrayList<>();
	private List<Double> storedForecastTargetPowerFlows_kW = new ArrayList<>();
	private DataSet storedLastWeekTargetPowerFlows_kW; 
	
	/**
     * Empty constructor for serialization
     */
    public J_ElectrolyserManagementPowerSurplus() {
    }
    
	/**
     * Default constructor
     */
    public J_ElectrolyserManagementPowerSurplus(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.setTarget(gc);
    }
	/**
     * Default constructor setting target
     */
    public J_ElectrolyserManagementPowerSurplus(GridConnection gc, Agent target, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	setTarget(target);
    }
    
    public void manageElectrolyser(J_TimeVariables timeVariables) {
    	J_EAConversionElectrolyser electrolyserAsset = (J_EAConversionElectrolyser)findFirst(gc.c_conversionAssets, asset -> asset.getEAType() == OL_EnergyAssetType.ELECTROLYSER);

    	//Consider GC its own limits
    	double allowedDeliveryCapacity_kW = gc.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW();
    	double availableElectricPower_kW = max(0, allowedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
    	
    	//Define forecast time
    	double forecast_time_h = electrolyserAsset.getStartUpTimeStandby_h()+ 2*timeParameters.getTimeStep_h();

    	//Get current target values
    	J_FlowsMap flowsMap = electrolyserAsset.getLastFlows();
    	double previousElectrolyserConsumption_kW = max(0,flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
    	double currentTargetPowerFlow_kW = getTargetCurrentPowerFlow_kW() - previousElectrolyserConsumption_kW;
    	lastWeekTargetPowerFlows_kW.add(timeVariables.getT_h(), currentTargetPowerFlow_kW);
    	
    	//Set the electrolyser state
    	if(electrolyserAsset.usesElectrolyserStates()) {
    		electrolyserStateControl_Surplus(electrolyserAsset, currentTargetPowerFlow_kW, forecast_time_h, timeParameters, timeVariables);
    	}
    	
    	//Determine electrolyser setpoint based on electrolyser state
    	double electrolyserSetpointElectric_kW = 0;
    	if(electrolyserAsset.usesElectrolyserStates()) {
	    	switch (electrolyserAsset.getState()){
	    		case SHUTDOWN:
	    		case STANDBY: 
	    			electrolyserSetpointElectric_kW = 0;
	    			break;
	    		case POWER_UP:
	    		case IDLE: 
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getIdlePowerLoadRatio_r()*electrolyserAsset.getInputCapacity_kW();
	    			break;
	    		case FUNCTIONAL:
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW()*electrolyserAsset.getMininumProductionRatio_r();
	    			break;
	    		case FULLCAPACITY:
	    			electrolyserSetpointElectric_kW = min(electrolyserAsset.getInputCapacity_kW(), abs(-currentTargetPowerFlow_kW));
	    			break;
	    	}
    	}
    	else {
    		electrolyserSetpointElectric_kW = min(electrolyserAsset.getInputCapacity_kW(), abs(-currentTargetPowerFlow_kW));
    	}
    	
    	//Limit the electrolyser setpoint
    	electrolyserSetpointElectric_kW = min(availableElectricPower_kW, electrolyserSetpointElectric_kW);
    	
    	gc.f_updateFlexAssetFlows(electrolyserAsset, electrolyserSetpointElectric_kW/electrolyserAsset.getInputCapacity_kW(), timeVariables);
    }
    
    
    
    private void electrolyserStateControl_Surplus(J_EAConversionElectrolyser electrolyserAsset, double currentTargetPowerFlow_kW, double forecast_time_h, J_TimeParameters timeParameters, J_TimeVariables timeVariables){
    	double solar_forecast_kW;
    	double wind_forecast_kW;

    	//Initialize limitation values
    	if (forecastRES_kW.size() == 0){
    		for(int i = timeVariables.getTimeStepsElapsed(); i < timeVariables.getTimeStepsElapsed() + roundToInt(forecast_time_h/timeParameters.getTimeStep_h()); i++){
    			solar_forecast_kW = - gc.energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h()) * getTargetTotalInstalledPVPower_kW();
    			wind_forecast_kW = - gc.energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h()) * getTargetTotalInstalledWindPower_kW();
    			forecastRES_kW.add(solar_forecast_kW + wind_forecast_kW);
    			forecastTargetPowerFlows_kW.add(currentTargetPowerFlow_kW - forecastRES_kW.get(0) + solar_forecast_kW + wind_forecast_kW);
    		}
    	}
    	else if(timeVariables.getTimeStepsElapsed() < (8760-forecast_time_h)/timeParameters.getTimeStep_h()){//Get future limitation values
    		
    		//Get current RES production
    		double currentRESProduction_kW = forecastRES_kW.get(0);
    		
    		//Update forecast array RES
    		forecastRES_kW.remove(0);
    		
    		solar_forecast_kW = - gc.energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + forecast_time_h) * getTargetTotalInstalledPVPower_kW();
    		wind_forecast_kW = - gc.energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + forecast_time_h) * getTargetTotalInstalledWindPower_kW();
    		
    		forecastRES_kW.add(solar_forecast_kW + wind_forecast_kW); 
    		
    		//Update forecast array Target power flow
    		forecastTargetPowerFlows_kW.remove(0);
    		
    		//Get past target power flow and weather (last week) if last week forecast prediction is selected.
    		if (b_forecast_lastWeekBased && lastWeekTargetPowerFlows_kW.size() > 672 - roundToInt(forecast_time_h/timeParameters.getTimeStep_h())){ // Use last week to create the forecast	
    		
    			double lastWeekTargetPowerFlow_kW = lastWeekTargetPowerFlows_kW.getY(roundToInt(forecast_time_h/timeParameters.getTimeStep_h()));
    			double solar_lastWeek_kW = - gc.energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + forecast_time_h - 168) * getTargetTotalInstalledPVPower_kW();
    			double wind_lastWeek_kW = - gc.energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + forecast_time_h - 168) * getTargetTotalInstalledWindPower_kW();
    				
    			forecastTargetPowerFlows_kW.add(lastWeekTargetPowerFlow_kW - solar_lastWeek_kW - wind_lastWeek_kW + solar_forecast_kW + wind_forecast_kW);
    		}
    		else{//use current power flow to predict forecast
    			forecastTargetPowerFlows_kW.add(currentTargetPowerFlow_kW - currentRESProduction_kW + forecastRES_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-1));
    		}
    	}


    	//Set state based on current state and forecast.
    	switch (electrolyserAsset.getState()){

    		case SHUTDOWN: //Not ready to be powerd up and complete shut down (when broken, maintenance, etc.)
    			break;
    		case STANDBY: // Ready to be powered on, but no electricity consumption.
    			//Check if electrolyser will be able to be functional at least two time steps when powering up, if so: power_up = true.
    			if (forecastTargetPowerFlows_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-2) < 0 && forecastTargetPowerFlows_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-1) < 0){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.POWER_UP);
    				electrolyserAsset.setRemainingPowerUpDuration_timesteps(roundToInt(electrolyserAsset.getStartUpTimeStandby_h()/timeParameters.getTimeStep_h()));
    			}
    			break;
    		case POWER_UP:
    			if(electrolyserAsset.getRemainingPowerUpDuration_timesteps() == 0){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
    		case IDLE: // Ready to start producing hydrogen, heated up, so consuming electricity, but not producing hydrogen yet.
    			if ( currentTargetPowerFlow_kW < 0 ){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
    			}
    			else{
    				boolean power_down = true;
    				for(int i = 0; i < forecastTargetPowerFlows_kW.size() - 2; i++){
    					if (forecastTargetPowerFlows_kW.get(i) < 0){
    						power_down = false;
    					}
    				}
    			
    				//Set mode based on power down or not.
    				if (power_down){
    					electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.STANDBY);
    				}
    			}
    		break;
    		case FUNCTIONAL: // Producing hydrogen at minimum possible amount (to make sure system is working correctly, no leaks).
    			if ( currentTargetPowerFlow_kW < 0 ){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
    			}
    			else{
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
    		case FULLCAPACITY:	// Producing hydrogen as much as possible.
    			if ( currentTargetPowerFlow_kW < 0 ){
    				// Stay at full capacity
    			}
    			else{ // Skip FUNCTIONAL — go directly to IDLE when surplus is gone
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
    	}
    }
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		OL_ForecastStatus status = OL_ForecastStatus.NOT_FORECASTABLE;
		String reason = "Not yet implemented.";
		return new J_AssetTypeForecast(I_ElectrolyserManagement.class, loadMap, status, reason);
	}
	
    public void setTarget(Agent target) {
    	this.target = target;
    }
    
    public double getTargetTotalInstalledPVPower_kW() {
    	if ( this.target == null) {
    		return 0;
    	}
    	else if (this.target == this.gc) {
    		return gc.v_liveAssetsMetaData.totalInstalledPVPower_kW;
    	}
    	else if (this.target instanceof GridNode targetGN) {
    		return targetGN.v_totalInstalledPVPower_kW;
    	}
    	else if (this.target instanceof EnergyCoop targetCoop) {
    		return targetCoop.v_liveAssetsMetaData.totalInstalledPVPower_kW;
    	}
    	else {
    		throw new RuntimeException("Target found for J_ElectrolyserManagementPowerSurplus that is not supported ( " + this.target + " )!");
    	}
    }
    public double getTargetTotalInstalledWindPower_kW() {
    	if ( this.target == null) {
    		return 0;
    	}
    	else if (this.target == this.gc) {
    		return gc.v_liveAssetsMetaData.totalInstalledWindPower_kW;
    	}
    	else if (this.target instanceof GridNode targetGN) {
    		return targetGN.v_totalInstalledWindPower_kW;
    	}
    	else if (this.target instanceof EnergyCoop targetCoop) {
    		return targetCoop.v_liveAssetsMetaData.totalInstalledWindPower_kW;
    	}
    	else {
    		throw new RuntimeException("Target found for J_ElectrolyserManagementPowerSurplus that is not supported ( " + this.target + " )!");
    	}
    }
    public double getTargetCurrentPowerFlow_kW() {
    	if ( this.target == null) {
    		return 0;
    	}
    	else if (this.target == this.gc) {
    		return gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	}
    	else if (this.target instanceof GridNode targetGN) {
    		return targetGN.v_currentLoad_kW;
    	}
    	else if (this.target instanceof EnergyCoop targetCoop) {
    		return targetCoop.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	}
    	else {
    		throw new RuntimeException("Target found for J_ElectrolyserManagementPowerSurplus that is not supported ( " + this.target + " )!");
    	}
    }
    
    ////Store and reset states
	public void storeStatesAndReset() {
		this.storedForecastRES_kW = this.forecastRES_kW;
		this.storedForecastTargetPowerFlows_kW = this.forecastTargetPowerFlows_kW;
		this.storedLastWeekTargetPowerFlows_kW = this.lastWeekTargetPowerFlows_kW;
		this.forecastRES_kW = new ArrayList<>();
		this.forecastTargetPowerFlows_kW = new ArrayList<>();
		this.lastWeekTargetPowerFlows_kW = new DataSet(672);
	}
	public void restoreStates() {
		this.forecastRES_kW = storedForecastRES_kW;
		this.forecastTargetPowerFlows_kW = this.storedForecastTargetPowerFlows_kW;
		this.lastWeekTargetPowerFlows_kW = this.storedLastWeekTargetPowerFlows_kW;
	}
	
	@Override
	public String toString() {
		return "J_ElectrolyserManagementPowerSurplus: \n" +
				"Target: " + this.target + "\n" +
				"b_forecast_lastWeekBased: " + this.b_forecast_lastWeekBased;
	}
}