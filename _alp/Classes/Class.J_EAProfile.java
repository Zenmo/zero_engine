/**
 * J_EAProfile
 */
public class J_EAProfile extends zero_engine.J_EA implements Serializable {

	//private OL_EnergyCarriers energyCarrierProduced = OL_EnergyCarriers.METHANE;
	public OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	//public double capacityMethane_kW;
	public double[] a_energyProfile_kWh;
	public OL_ProfileAssetType profileType;
	private double profileTimestep_h;
    private double profileStarTime_h = 0;
	//protected double outputTemperature_degC;
	public double loadLoad_kWh = 0;
	private double profileScaling_fr = 1;
	private boolean enableProfileLooping = true;
	
    /**
     * Default constructor
     */
    public J_EAProfile() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAProfile(Agent parentAgent, OL_EnergyCarriers energyCarrier, double[] profile_kWh, OL_ProfileAssetType profileType, double profileTimestep_h) {
	    this.parentAgent= parentAgent;
	    this.energyCarrier = energyCarrier;
	    this.a_energyProfile_kWh = profile_kWh;
	    this.profileType = profileType;
	    this.profileTimestep_h = profileTimestep_h;

	    if (parentAgent instanceof GridConnection) {
	    	this.timestep_h = ((GridConnection)parentAgent).energyModel.p_timeStep_h;
	    } else {
	    	this.timestep_h = profileTimestep_h;
	    }
	    
	    if (profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.fixedConsumptionElectric_kW;
	    } else if (profileType == OL_ProfileAssetType.CHARGING) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
	    } else if (profileType == OL_ProfileAssetType.WINDTURBINE) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.windProductionElectric_kW;
	    } else if (profileType == OL_ProfileAssetType.SOLARPANELS) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.pvProductionElectric_kW;	    	
	    } else if (profileType == OL_ProfileAssetType.HEATPUMP_ELECTRICITY_CONSUMPTION) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.heatPumpElectricityConsumption_kW;
	    } 
	    
	    //this.activeProductionEnergyCarriers.add(this.energyCarrier);
	    this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
	    
		registerEnergyAsset();
	}
    
    public void setStartTime_h(double startTime_h) {    	
    	this.profileStarTime_h = startTime_h;
    }

    @Override
    //public Pair<J_FlowsMap, Double> f_updateAllFlows(double time_h) {
    public void f_updateAllFlows(double time_h) {
    	//clear();    	
    	//double[] arr=operate(time_h);
    	//Pair<J_FlowsMap, Double> flowsPair = operate(time_h);
    	operate(time_h-this.profileStarTime_h);
    	//J_FlowsMap flowsMap = flowsPair.getFirst();
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
    	}
    	//if (ui_energyAsset!= null) {
    		//ui_energyAsset.f_addFlows(flowsMap);
    	//}
    	//this.lastFlowsArray = arr;
    	this.lastFlowsMap.cloneMap(flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	/*traceln("flowsMap: %s", flowsMap);
    	flowsMap.clear();
    	traceln("flowsMap after reset: %s", flowsMap);
    	traceln("lastflowsMap after flowsmap reset: %s", lastFlowsMap);*/
    	
    	//return flowsMap;
    }
    
    @Override
    //public Pair<J_FlowsMap, Double> operate(double time_h) {
    public void operate(double time_h) {
    	if (enableProfileLooping && time_h >= a_energyProfile_kWh.length * profileTimestep_h) {
    		time_h = time_h % a_energyProfile_kWh.length * profileTimestep_h;
    	} else if ( (int)floor(time_h/profileTimestep_h) >= a_energyProfile_kWh.length ) {
    		traceln("Time out of upper bound for evaluating J_EAProfile power in profile asset %s!", this.energyAssetName);
//    		time_h = a_energyProfile_kWh.length * profileTimestep_h - 1;
    		throw new RuntimeException(String.format("Time out of upper bound for evaluating J_EAProfile power! Time is: %s", time_h));
    	}
    	if ( time_h < 0 ) {
    		traceln("Time out of lower bound for evaluating J_EAProfile power in profile asset %s!", this.energyAssetName);
    		throw new RuntimeException(String.format("Time out of lower bound for evaluating J_EAProfile power! Time is: %s", time_h));
    	}

    	double currentPower_kW = a_energyProfile_kWh[(int)floor(time_h/profileTimestep_h)]/profileTimestep_h;
    	this.energyUse_kW = currentPower_kW;
		this.energyUsed_kWh += timestep_h * energyUse_kW; 
		this.flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			this.assetFlowsMap.put(this.assetFlowCategory, currentPower_kW);
		}
    }

	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}

    public void curtailElectricityConsumption(double curtailmentSetpoint_kW) {
    	//double currentElectricityProduction_kW = lastFlowsArray[4];
    	double currentElectricityConsumption_kW = this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY);
    	double curtailmentPower_kW = max(0,min(currentElectricityConsumption_kW, curtailmentSetpoint_kW));
    	energyUsed_kWh -= curtailmentPower_kW * timestep_h;
    	loadLoad_kWh += curtailmentPower_kW * timestep_h;
    	//double[] arr = {0, 0, 0, 0, -curtailmentPower_kW, 0, 0, 0, 0, -curtailmentPower_kW};
    	J_FlowsMap flowsMap = new J_FlowsMap();
    	flowsMap.put(OL_EnergyCarriers.ELECTRICITY, -curtailmentPower_kW);    	
    	J_ValueMap<OL_AssetFlowCategories> assetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
    	assetFlows_kW.put(this.assetFlowCategory, -curtailmentPower_kW);
    	
    	this.energyUse_kW = -curtailmentPower_kW;
    	//flowsMap.put(OL_EnergyCarriers.ENERGY_USE, -curtailmentPower_kW);

    	//lastFlowsArray[4] -= curtailmentPower_kW;
    	this.lastFlowsMap.put(OL_EnergyCarriers.ELECTRICITY, this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY) - curtailmentPower_kW);
    	//lastFlowsArray[9] -= curtailmentPower_kW;
    	this.lastEnergyUse_kW -= curtailmentPower_kW;
    	//lastFlowsMap.put(OL_EnergyCarriers.ENERGY_USE, lastFlowsMap.get(OL_EnergyCarriers.ENERGY_USE) - curtailmentPower_kW);

    	//traceln("Electricity production of asset %s curtailed by %s kW!", this, curtailmentPower_kW);
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_removeFlows(flowsMap, this.energyUse_kW, assetFlows_kW, this);
    	}
    	//if (ui_energyAsset!= null) {
    		//ui_energyAsset.f_removeFlows(flowsMap);
    	//}
    	//return new Pair(flowsMap, this.energyUse_kW);
    }

    public void scaleEnergyProfile(double scaling_fr) {
    	if (scaling_fr == 0) {
    		traceln("Failed to scale J_EAProfile with factor of 0 to prevent loss of information.");
    		return;
    	}
    	this.profileScaling_fr *= scaling_fr;
    	for (int i = 0; i < a_energyProfile_kWh.length; i++) {
    		a_energyProfile_kWh[i] = a_energyProfile_kWh[i] * scaling_fr;
    	}
    	return;
    }
    
    public void resetEnergyProfile() {
    	if (this.profileScaling_fr == 1) {
    		return;
    	}
    	for (int i = 0; i < a_energyProfile_kWh.length; i++) {
    		a_energyProfile_kWh[i] = a_energyProfile_kWh[i] / this.profileScaling_fr;
    	}
    	this.profileScaling_fr = 1;
    	return;
    }
	
    public double getProfileScaling_fr() {
    	return profileScaling_fr;
    }
    
	@Override
	public String toString() {
		return
			"parentAgent = " + parentAgent +", Energy consumed = " + this.energyUsed_kWh +
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " ";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
