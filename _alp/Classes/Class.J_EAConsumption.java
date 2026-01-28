/**
 * J_EAConsumption
 */
public class J_EAConsumption extends zero_engine.J_EAFixed implements Serializable {
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
    public J_EAConsumption(I_AssetOwner owner, OL_EnergyAssetType type, String name, double yearlyDemand_kWh, OL_EnergyCarriers energyCarrier, J_TimeParameters timeParameters, J_ProfilePointer profile) {
		if (profile == null) {
			throw new RuntimeException("profile pointer for J_EAConsumption " + name + " is is null");
		}
    	this.setOwner(owner);
		this.timeParameters = timeParameters;
    	
    	this.energyAssetName = name;
		this.energyAssetType = type;
		this.yearlyDemand_kWh = yearlyDemand_kWh;
		this.energyCarrier =  energyCarrier;
		
		profilePointer = profile;
		this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
		
		if (this.energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			if (this.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB) {
				this.assetFlowCategory = OL_AssetFlowCategories.electricHobConsumption_kW; //
			}
			else {
				this.assetFlowCategory = OL_AssetFlowCategories.fixedConsumptionElectric_kW; //
			}
		}
		else if (this.energyCarrier == OL_EnergyCarriers.HEAT) {
			if (this.energyAssetType == OL_EnergyAssetType.HOT_WATER_CONSUMPTION) {
				this.assetFlowCategory = OL_AssetFlowCategories.hotWaterConsumption_kW;
			}
			else {
				this.assetFlowCategory = OL_AssetFlowCategories.spaceHeating_kW;
			}
		}

		registerEnergyAsset(timeParameters);
    }
    
    public void setConsumptionScaling_fr(double consumptionScaling_fr) {
    	this.consumptionScaling_fr = consumptionScaling_fr;
    }
    
    public double getConsumptionScaling_fr() {
    	return this.consumptionScaling_fr;
    }
        
	@Override
	public void operate(J_TimeVariables timeVariables) {
		double ratioOfCapacity = profilePointer.getCurrentValue();		

    	double consumption_kW = ratioOfCapacity * this.yearlyDemand_kWh * this.consumptionScaling_fr;
		
    	this.energyUse_kW = consumption_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timeParameters.getTimeStep_h();

		flowsMap.put(this.energyCarrier, consumption_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, consumption_kW);
		}
   	}

    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    
	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"energyCarrier = " + this.energyCarrier + " " + 
			"yearlyDemand_kWh = " + this.yearlyDemand_kWh;
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
