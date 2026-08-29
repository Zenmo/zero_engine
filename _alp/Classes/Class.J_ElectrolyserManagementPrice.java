/**
 * J_ElectrolyserManagementPrice
 */	
public class J_ElectrolyserManagementPrice implements I_ElectrolyserManagement {
	
	private GridConnection gc;
	private J_TimeParameters timeParameters;
	

	//Management specific
	private double electricityPriceMaxForProfit_eurpkWh = 0; //Default only produce hydrogen when electricity price is negative. Possible to set differently.
	private List<Double> forecast_electricityPrice_eurpkWh = new ArrayList<>();
	
	//Storing
	private List<Double> storedForecast_electricityPrice_eurpkWh = new ArrayList<>();
    
	/**
     * Empty constructor for serialization
     */
    public J_ElectrolyserManagementPrice() {
    }
    
	/**
     * Default constructor
     */
    public J_ElectrolyserManagementPrice(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
	/**
     * Default constructor initializing electricity price limit
     */
    public J_ElectrolyserManagementPrice(GridConnection gc, double electricityPriceMaxForProfit_eurpkWh, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.electricityPriceMaxForProfit_eurpkWh = electricityPriceMaxForProfit_eurpkWh;
    }
    
    public void manageElectrolyser(J_TimeVariables timeVariables) {
    	//Get the electrolyser asset
    	J_EAConversionElectrolyser electrolyserAsset = (J_EAConversionElectrolyser)findFirst(gc.c_conversionAssets, asset -> asset.getEAType() == OL_EnergyAssetType.ELECTROLYSER);

    	//Define forecast time
    	double forecast_time_h = electrolyserAsset.getStartUpTimeStandby_h() + 2*timeParameters.getTimeStep_h();
    	
    	//Set electrolyser state
    	if(electrolyserAsset.usesElectrolyserStates()) {
    		f_electrolyserStateControl_Price(electrolyserAsset, forecast_time_h, timeVariables);
    	}

    	//Get the limit of the GC itself
    	double v_allowedDeliveryCapacity_kW = gc.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW();
    	double availableElectricPower_kW = max(0, v_allowedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));

    	//Define the electrolyserSetpoint_kW based on the current state
    	double electrolyserSetpointElectric_kW = 0;
    	if(electrolyserAsset.usesElectrolyserStates()) {
	    	switch (electrolyserAsset.getState()){
	    		case SHUTDOWN:
	    		case STANDBY: 
	    			electrolyserSetpointElectric_kW = 0;
	    			break;
	    		case POWER_UP:
	    		case IDLE: 
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW()*electrolyserAsset.getIdlePowerLoadRatio_r();
	    			break;
	    		case FUNCTIONAL:
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW()*electrolyserAsset.getMininumProductionRatio_r();
	    			break;
	    		case FULLCAPACITY:
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW();
	    			break;	
	    	}
    	}
    	else if(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue()/1000.0 < this.electricityPriceMaxForProfit_eurpkWh) {
    		electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW();
    	}
    	
    	//Limit to the GC limit
    	electrolyserSetpointElectric_kW = min(availableElectricPower_kW, electrolyserSetpointElectric_kW);
    	
    	//Run the asset
    	gc.f_updateFlexAssetFlows(electrolyserAsset, electrolyserSetpointElectric_kW/electrolyserAsset.getInputCapacity_kW(), timeVariables);
    }
    
    private void f_electrolyserStateControl_Price(J_EAConversionElectrolyser electrolyserAsset, double forecast_time_h, J_TimeVariables timeVariables){
    	double currentElectricityPrice_eurpkWh = gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue()/1000.0;
    	int forecastSteps = roundToInt(forecast_time_h/timeParameters.getTimeStep_h());

    	// Initialize forecast list
    	if (forecast_electricityPrice_eurpkWh.size() == 0){
    		for(int i = timeVariables.getTimeStepsElapsed(); i < timeVariables.getTimeStepsElapsed() + forecastSteps; i++){
    			forecast_electricityPrice_eurpkWh.add(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h())/1000.0);
    		}
    	}
    	// Roll forecast forward
    	else {
    		forecast_electricityPrice_eurpkWh.remove(0);
    		forecast_electricityPrice_eurpkWh.add(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getValue(timeVariables.getT_h() + forecast_time_h)/1000.0);
    	}

    	switch (electrolyserAsset.getState()){
	    	case SHUTDOWN:
	    	case STANDBY:
	    		// Only power up if price is profitable for at least 2 timesteps after startup
	    		if (forecast_electricityPrice_eurpkWh.get(forecastSteps-2) < this.electricityPriceMaxForProfit_eurpkWh && forecast_electricityPrice_eurpkWh.get(forecastSteps-1) < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.POWER_UP);
	    			electrolyserAsset.setRemainingPowerUpDuration_timesteps(roundToInt(electrolyserAsset.getStartUpTimeStandby_h()/timeParameters.getTimeStep_h()));
	    		}
	    		break;
    		case POWER_UP:
    			if(electrolyserAsset.getRemainingPowerUpDuration_timesteps() == 0){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
	    	case IDLE:
	    		if (currentElectricityPrice_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.STANDBY);
	    		}
	    		break;
	    	case FUNCTIONAL:
	    		if (currentElectricityPrice_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
	    		}
	    		break;
	    	case FULLCAPACITY:
	    		if (currentElectricityPrice_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
    				// Stay at full capacity
	    		}
	    		else{
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
    
    public void setElectricityPriceMaxForProfit_eurpkWh(double electricityPriceMaxForProfit_eurpkWh) {
    	this.electricityPriceMaxForProfit_eurpkWh = electricityPriceMaxForProfit_eurpkWh;
    }
    
    ////Store and reset states
	public void storeStatesAndReset() {
		this.storedForecast_electricityPrice_eurpkWh = this.forecast_electricityPrice_eurpkWh;
		this.forecast_electricityPrice_eurpkWh = new ArrayList<>();
	}
	public void restoreStates() {
		this.forecast_electricityPrice_eurpkWh = this.storedForecast_electricityPrice_eurpkWh;
	}
	
	@Override
	public String toString() {
		return "J_ElectrolyserManagementPrice: \n" +
				"electricityPriceMaxForProfit_eurpkWh: " + this.electricityPriceMaxForProfit_eurpkWh;
	}
}