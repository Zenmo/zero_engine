/**
 * J_EAConsumption
 */
public class J_EAConsumption extends J_EAProfile{

	private double yearlyDemand_kWh;
	/**
     * Default constructor
     */
    public J_EAConsumption() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAConsumption(I_AssetOwner owner, OL_EnergyAssetType type, String name, double yearlyDemand_kWh, OL_EnergyCarriers energyCarrier, J_TimeParameters timeParameters, J_ProfilePointer profile) {
		/*if (yearlyDemand_kWh == 0.0) {
			throw new RuntimeException("Unable to construct J_EAConsumption: " + name + " because consumption is zero." );
		}*/
    	this.setOwner(owner);
	    this.timeParameters = timeParameters;	    
		
    	this.energyAssetName = name;
		this.energyAssetType = type;
    	
		this.yearlyDemand_kWh = yearlyDemand_kWh;
		if (profile.getProfileUnits() == OL_ProfileUnits.YEARLYTOTALFRACTION) {
			this.profileUnitScaler_r = yearlyDemand_kWh;
			this.profilePointer = profile;
		} else {
			throw new RuntimeException("Invalid OL_ProfileUnits type for J_EAConsumption!");
		}
		this.energyCarrier =  energyCarrier;
		
		
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
    
    //Setters
    public void setConsumptionScaling_fr(double consumptionScaling_fr) {
    	this.profileScaling_fr = consumptionScaling_fr;
    }
    
    
    //Getters
    public String getAssetName() {
    	return this.energyAssetName;
    }
    public double getConsumptionScaling_fr() {
    	return this.profileScaling_fr;
    }

    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    @Override
    public double getBaseConsumption_kWh() {
    	return yearlyDemand_kWh;
    }
    
	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"owner = " + this.getOwner() +" " +
			"energyCarrier = " + this.energyCarrier + " " + 
			"yearlyDemand_kWh = " + this.yearlyDemand_kWh;
	}
}
