/**
 * J_ElectrolyserManagementPowerSurplus
 */	
public class J_ElectrolyserManagementPowerSurplus implements I_ElectrolyserManagement {
	
	private GridConnection gc;
	private J_TimeParameters timeParameters;

	//Management specific
	private Agent target;
	private List<Double> c_forecast_RES_kW = new ArrayList<>();
	private List<Double> c_forecast_gridNodePowerFlow_kW = new ArrayList<>();
	private DataSet data_liveWeekGridNodePowerFlow_kW = new DataSet(672);
	private DataSet data_liveWeekElectrolyserPower_kW = new DataSet(672);
	
	private boolean b_forecast_lastWeekBased = false; // EMS functionality option: Determine forcast based on last weeks power.
	
	//Storing
	private DataSet storedData_liveWeekGridNodePowerFlow_kW;
	private DataSet storedData_liveWeekElectrolyserPower_kW;
	
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
    	double targetDeliveryCapacityLimit_kW = getTargetDeliveryCapacityLimit_kW();
    	
    	//Consider GC its own limits
    	double v_allowedDeliveryCapacity_kW = gc.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW();
    	double availableElectricPower_kW = max(0, v_allowedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
    	
    	double forecast_time_h = electrolyserAsset.getStartUpTimeStandby_h()+ 2*timeParameters.getTimeStep_h();

    	//Get current limitation values
    	J_FlowsMap flowsMap = electrolyserAsset.getLastFlows();
    	double previousElectrolyserConsumption_kW = max(0,flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
    	double currentGridNodePowerFlow_kW = gc.p_parentNodeElectric.v_currentLoad_kW - previousElectrolyserConsumption_kW;
    	data_liveWeekGridNodePowerFlow_kW.add(timeVariables.getT_h(), gc.p_parentNodeElectric.v_currentLoad_kW);
    	
    	//Set the electrolyser state
    	electrolyserStateControl_Surplus(electrolyserAsset, currentGridNodePowerFlow_kW, forecast_time_h, timeParameters, timeVariables);
    	
    	//Determine electrolyser setpoint based on electrolyser state
    	double electrolyserSetpointElectric_kW = 0;
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
    			electrolyserSetpointElectric_kW = min(electrolyserAsset.getInputCapacity_kW(), abs(targetDeliveryCapacityLimit_kW - currentGridNodePowerFlow_kW));
    			break;
    	}
    	
    	//Limit the electrolyser setpoint
    	electrolyserSetpointElectric_kW = min(availableElectricPower_kW, electrolyserSetpointElectric_kW);
    	
    	//Store Electrolyser power in dataset
    	data_liveWeekElectrolyserPower_kW.add(timeVariables.getT_h(), electrolyserSetpointElectric_kW);
    	
    	gc.f_updateFlexAssetFlows(electrolyserAsset, electrolyserSetpointElectric_kW/electrolyserAsset.getInputCapacity_kW(), timeVariables);
    }
    
    
    
    private void electrolyserStateControl_Surplus(J_EAConversionElectrolyser electrolyserAsset, double currentGridNodePowerFlow_kW, double forecast_time_h, J_TimeParameters timeParameters, J_TimeVariables timeVariables){
    	double solar_forecast_kW;
    	double wind_forecast_kW;
    	double targetDeliveryCapacityLimit_kW = getTargetDeliveryCapacityLimit_kW();
    	
    	//Initialize limitation values
    	if (c_forecast_RES_kW.size() == 0){
    			
    		for(int i = timeVariables.getTimeStepsElapsed(); i < timeVariables.getTimeStepsElapsed() + roundToInt(forecast_time_h/timeParameters.getTimeStep_h()); i++){
    			solar_forecast_kW = - gc.energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h()) * getTargetTotalInstalledPVPower_kW();
    			wind_forecast_kW = - gc.energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h()) * getTargetTotalInstalledWindPower_kW();
    			c_forecast_RES_kW.add(solar_forecast_kW + wind_forecast_kW);
    			c_forecast_gridNodePowerFlow_kW.add(currentGridNodePowerFlow_kW - c_forecast_RES_kW.get(0) + solar_forecast_kW + wind_forecast_kW);
    		}
    	}

    	//Get future limitation values
    	else if(timeVariables.getTimeStepsElapsed() < (8760-forecast_time_h)/timeParameters.getTimeStep_h()){
    		
    		//Get current RES production
    		double currentRESProduction_kW = c_forecast_RES_kW.get(0);
    		
    		//Update forecast array RES
    		c_forecast_RES_kW.remove(0);
    		
    		solar_forecast_kW = - gc.energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + forecast_time_h) * getTargetTotalInstalledPVPower_kW();
    		wind_forecast_kW = - gc.energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + forecast_time_h) * getTargetTotalInstalledWindPower_kW();
    		
    		c_forecast_RES_kW.add(solar_forecast_kW + wind_forecast_kW); 
    		
    		//Update forecast array Grid node power flow
    		c_forecast_gridNodePowerFlow_kW.remove(0);
    		
    		//Get past grid node power flow and weather (last week) if last week forecast prediction is selected.
    		if (b_forecast_lastWeekBased && data_liveWeekElectrolyserPower_kW.size() > 672 - roundToInt(forecast_time_h/timeParameters.getTimeStep_h())){ // Use last week to create the forecast	
    		
    			double lastWeekGridNodePowerFlow_kW = data_liveWeekGridNodePowerFlow_kW.getY(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())) - data_liveWeekElectrolyserPower_kW.getY(roundToInt(forecast_time_h/timeParameters.getTimeStep_h()));
    			double solar_lastWeek_kW = - gc.energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + forecast_time_h - 168) * getTargetTotalInstalledPVPower_kW();
    			double wind_lastWeek_kW = - gc.energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + forecast_time_h - 168) * getTargetTotalInstalledWindPower_kW();
    				
    			c_forecast_gridNodePowerFlow_kW.add(lastWeekGridNodePowerFlow_kW - solar_lastWeek_kW - wind_lastWeek_kW + solar_forecast_kW + wind_forecast_kW);
    		}
    		else{//use current power flow to predict forecast
    			c_forecast_gridNodePowerFlow_kW.add(currentGridNodePowerFlow_kW - currentRESProduction_kW + c_forecast_RES_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-1));
    		}
    	}


    	//Set state based on current state and forecast.
    	switch (electrolyserAsset.getState()){

    		case SHUTDOWN: //Not ready to be powerd up and complete shut down (when broken, maintenance, etc.)
    			break;
    		case STANDBY: // Ready to be powered on, but no electricity consumption.
    			//Check if electrolyser will be able to be functional at least two time steps when powering up, if so: power_up = true.
    			if (c_forecast_gridNodePowerFlow_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-2) < targetDeliveryCapacityLimit_kW && c_forecast_gridNodePowerFlow_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-1) < targetDeliveryCapacityLimit_kW){
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
    			if ( currentGridNodePowerFlow_kW < targetDeliveryCapacityLimit_kW ){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
    			}
    			else{
    				boolean power_down = true;
    				for(int i = 0; i < c_forecast_gridNodePowerFlow_kW.size() - 2; i++){
    					if (c_forecast_gridNodePowerFlow_kW.get(i) < targetDeliveryCapacityLimit_kW){
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
    			if ( currentGridNodePowerFlow_kW < targetDeliveryCapacityLimit_kW ){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
    			}
    			else{
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
    		case FULLCAPACITY:	// Producing hydrogen as much as possible.
    			if ( currentGridNodePowerFlow_kW < targetDeliveryCapacityLimit_kW ){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
    			}
    			else{
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
    	}
    }
    
    public void setTarget(Agent target) {
    	this.target = target;
    }
    
    public double getTargetTotalInstalledPVPower_kW() {
    	if ( this.target == null) {
    		return 0;
    	}
    	else if (this.target instanceof GridConnection targetGC) {
    		return targetGC.v_liveAssetsMetaData.totalInstalledPVPower_kW;
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
    	else if (this.target instanceof GridConnection targetGC) {
    		return targetGC.v_liveAssetsMetaData.totalInstalledWindPower_kW;
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
    public double getTargetDeliveryCapacityLimit_kW() {
    	if ( this.target == null) {
    		return 0;
    	}
    	else if (this.target instanceof GridConnection targetGC) {
    		return targetGC.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW();
    	}
    	else if (this.target instanceof GridNode targetGN) {
    		return targetGN.p_capacity_kW;
    	}
    	else if (this.target instanceof EnergyCoop targetCoop) {
    		return targetCoop.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW();
    	}
    	else {
    		throw new RuntimeException("Target found for J_ElectrolyserManagementPowerSurplus that is not supported ( " + this.target + " )!");
    	}
    }
    
    ////Store and reset states
	public void storeStatesAndReset() {
		this.storedData_liveWeekGridNodePowerFlow_kW = data_liveWeekGridNodePowerFlow_kW;
		this.storedData_liveWeekElectrolyserPower_kW = data_liveWeekElectrolyserPower_kW;
		this.data_liveWeekGridNodePowerFlow_kW = new DataSet(672);
		this.data_liveWeekElectrolyserPower_kW = new DataSet(672);
	}
	public void restoreStates() {
		this.data_liveWeekGridNodePowerFlow_kW = storedData_liveWeekGridNodePowerFlow_kW;
		this.data_liveWeekElectrolyserPower_kW = storedData_liveWeekElectrolyserPower_kW;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
}