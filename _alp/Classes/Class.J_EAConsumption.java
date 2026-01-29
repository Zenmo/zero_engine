/**
 * J_EAConsumption
 */
public class J_EAConsumption extends zero_engine.J_EAProfile implements Serializable {

	private double yearlyDemand_kWh;
	//public double loadLoad_kWh = 0;
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

    public String getAssetName() {
    	return this.energyAssetName;
    }
    
    public void setConsumptionScaling_fr(double consumptionScaling_fr) {
    	this.profileScaling_fr = consumptionScaling_fr;
    }
    
    public double getConsumptionScaling_fr() {
    	return this.profileScaling_fr;
    }
    
    
    /*@Override
    public void f_updateAllFlows(double v_powerFraction_fr) {
		throw new RuntimeException("J_EAConsumption.f_updateAllFlows() should be called without arguments!");
	}
	
	public void f_updateAllFlows() {
		double ratioOfCapacity = profilePointer.getCurrentValue();		
		this.operate(ratioOfCapacity);
		if (ratioOfCapacity>0.0) { // Skip when there is no consumption -> saves time?
			if (parentAgent instanceof GridConnection) {    		
	    		//((GridConnection)parentAgent).f_addFlows(arr, this);
	    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
	    	}

		}
		this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }
    
	@Override
	public void operate(double ratioOfCapacity) {

    	double consumption_kW = ratioOfCapacity * this.yearlyDemand_kWh * this.consumptionScaling_fr;
		
    	this.energyUse_kW = consumption_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;

		flowsMap.put(this.energyCarrier, consumption_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, consumption_kW);
		}
   	}*/

    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    
    public double getYearlyDemand_kWh() {
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
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
