/**
 * J_ElectrolyserManagementPrice
 */	
public class J_ElectrolyserManagementPrice implements I_ElectrolyserManagement {
	
	private GridConnection gc;
	private J_TimeParameters timeParameters;
	
	private double electricityPriceMaxForProfit_eurpkWh;
    
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
     * Default constructor initializing electricity prize limit
     */
    public J_ElectrolyserManagementPrice(GridConnection gc, double electricityPriceMaxForProfit_eurpkWh, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.electricityPriceMaxForProfit_eurpkWh = electricityPriceMaxForProfit_eurpkWh;
    }
    
    public void manageElectrolyser(J_TimeVariables timeVariables) {
    	//Get the electrolyser asset
    	J_EAConversionElectrolyser electrolyserAsset = (J_EAConversionElectrolyser)findFirst(gc.c_conversionAssets, asset -> asset.getEAType() == OL_EnergyAssetType.ELECTROLYSER);
    	
    	//Set electrolyser state
    	if(electrolyserAsset.usesElectrolyserStates()) {
    		f_electrolyserStateControl_Price(electrolyserAsset);
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
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getIdlePowerLoadRatio_r()*electrolyserAsset.getInputCapacity_kW();
	    			break;
	    		case FUNCTIONAL:
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW()*electrolyserAsset.getMininumProductionRatio_r();
	    			break;
	    		case FULLCAPACITY:
	    			electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW();
	    			break;
	    	}
    	}
    	else if(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() < this.electricityPriceMaxForProfit_eurpkWh) {
    		electrolyserSetpointElectric_kW = electrolyserAsset.getInputCapacity_kW();
    	}
    	
    	//Limit to the GC limit
    	electrolyserSetpointElectric_kW = min(availableElectricPower_kW, electrolyserSetpointElectric_kW);
    	
    	//Run the asset
    	gc.f_updateFlexAssetFlows(electrolyserAsset, electrolyserSetpointElectric_kW/electrolyserAsset.getInputCapacity_kW(), timeVariables);
    }
    
    private void f_electrolyserStateControl_Price(J_EAConversionElectrolyser electrolyserAsset){
	    double currentElectricityPriceEPEX_eurpkWh = gc.energyModel.pf_dayAheadElectricityPricing_eurpMWh.getForecast();

	    switch (electrolyserAsset.getState()){
	    	case SHUTDOWN:
	    		if (currentElectricityPriceEPEX_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.SHUTDOWN);
	    		}
	    		break;
	    	case STANDBY: 
	    		if (currentElectricityPriceEPEX_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState( OL_ElectrolyserState.STANDBY);
	    		}
	    		break;
    		case POWER_UP:
    			if(electrolyserAsset.getRemainingPowerUpDuration_timesteps() == 0){
    				electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
    			}
    			break;
	    	case IDLE: 
	    		if (currentElectricityPriceEPEX_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
	    		}
	    		break;
	    	case FUNCTIONAL:
	    		if (currentElectricityPriceEPEX_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
	    		}
	    		break;
	    	case FULLCAPACITY:
	    		if (currentElectricityPriceEPEX_eurpkWh < this.electricityPriceMaxForProfit_eurpkWh){
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
	    		}
	    		else{
	    			electrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
	    		}
	    		break;
	    }
    }
    
    public void setElectricityPriceMaxForProfit_eurpkWh(double electricityPriceMaxForProfit_eurpkWh) {
    	this.electricityPriceMaxForProfit_eurpkWh = electricityPriceMaxForProfit_eurpkWh;
    }
    
    ////Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

}