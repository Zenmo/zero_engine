/**
 * J_EAConsumption
 */
public class J_EAConsumption extends zero_engine.J_EA implements Serializable {
	protected J_ProfilePointer profilePointer;
	public double yearlyDemand_kWh;
	protected OL_EnergyCarriers energyCarrier;
	private double consumptionScaling_fr = 1;
	public double loadLoad_kWh = 0;
	//private J_profilePointer profilePointer;
	/**
     * Default constructor
     */
    public J_EAConsumption() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAConsumption(Agent parentAgent, OL_EnergyAssetType type, String name, double yearlyDemand_kWh, OL_EnergyCarriers energyCarrier, double timestep_h, J_ProfilePointer profile) {
		/*if (yearlyDemand_kWh == 0.0) {
			throw new RuntimeException("Unable to construct J_EAConsumption: " + name + " because consumption is zero." );
		}*/
    	
    	this.energyAssetName = name;
		this.energyAssetType = type;
    	this.parentAgent = parentAgent;
		this.yearlyDemand_kWh = yearlyDemand_kWh;
		this.energyCarrier =  energyCarrier;
		
		this.timestep_h = timestep_h;
		if (profile == null) {
			profilePointer = ((GridConnection)parentAgent).energyModel.f_findProfile(name);
		} else {
			profilePointer = profile;
		}		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
		this.assetFlowCategory = OL_AssetFlowCategories.fixedConsumptionElectric_kW; //
		registerEnergyAsset();
    }

    public String getAssetName() {
    	return this.energyAssetName;
    }
    
    public void setConsumptionScaling_fr(double consumptionScaling_fr) {
    	this.consumptionScaling_fr = consumptionScaling_fr;
    }
    
    public double getConsumptionScaling_fr() {
    	return this.consumptionScaling_fr;
    }
    
	@Override
	public void operate(double ratioOfCapacity) {
		
		if (this.profilePointer != null) {			
			ratioOfCapacity = this.profilePointer.getCurrentValue();
		}
		
    	double consumption_kW = ratioOfCapacity * this.yearlyDemand_kWh * this.consumptionScaling_fr;
		
    	this.energyUse_kW = consumption_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;

		flowsMap.put(this.energyCarrier, consumption_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, consumption_kW);
		}
   	}
	
    public Pair<J_FlowsMap, Double> curtailElectricityConsumption(double curtailmentSetpoint_kW) {
    	if (this.energyCarrier != OL_EnergyCarriers.ELECTRICITY) {
    		throw new RuntimeException("Unable to curtail the Consumption asset with energycarrier: " + this.energyCarrier);
    	}

    	double currentElectricityConsumption_kW = this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY);
    	double curtailmentPower_kW = max(0,min(currentElectricityConsumption_kW, curtailmentSetpoint_kW));
    	energyUsed_kWh -= curtailmentPower_kW * timestep_h;
    	loadLoad_kWh += curtailmentPower_kW * timestep_h;

    	J_FlowsMap flowsMap = new J_FlowsMap();
    	flowsMap.put(OL_EnergyCarriers.ELECTRICITY, -curtailmentPower_kW);
    	J_ValueMap<OL_AssetFlowCategories> assetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
    	assetFlows_kW.put(this.assetFlowCategory, -curtailmentPower_kW);
    	
    	this.energyUse_kW = -curtailmentPower_kW;

    	this.lastFlowsMap.put(OL_EnergyCarriers.ELECTRICITY, this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY) - curtailmentPower_kW);

    	this.lastEnergyUse_kW -= curtailmentPower_kW;
    	//traceln("Electricity production of asset %s curtailed by %s kW!", this, curtailmentPower_kW);
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_removeFlows(flowsMap, this.energyUse_kW, assetFlows_kW, this);
    	}
    	return new Pair(flowsMap, this.energyUse_kW);
    }

    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    
	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"parentAgent = " + this.parentAgent +" " +
			"energyCarrier = " + this.energyCarrier + " " + 
			"yearlyDemand_kWh = " + this.yearlyDemand_kWh;
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
