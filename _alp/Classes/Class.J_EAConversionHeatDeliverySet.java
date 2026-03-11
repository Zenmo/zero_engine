/**
 * J_EAConversionHeatDeliverySet
 */
public class J_EAConversionHeatDeliverySet extends zero_engine.J_EAConversion implements Serializable, I_HeatingAsset {

	protected double outputTemperature_degC;
    /**
     * Default constructor
     */
    public J_EAConversionHeatDeliverySet() {
    }

    /**
     * Constructor initializing the fields
     */
    
    public J_EAConversionHeatDeliverySet(I_AssetOwner owner, double outputHeatCapacity_kW, double efficiency_r, J_TimeParameters timeParameters, double outputTemperature_degC) {
    	this.setOwner(owner);
    	this.timeParameters = timeParameters;
    	this.outputCapacity_kW = outputHeatCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    
	    this.outputTemperature_degC = outputTemperature_degC;
	    
	    this.energyAssetType = OL_EnergyAssetType.HEAT_DELIVERY_SET;
	    this.assetFlowCategory = OL_AssetFlowCategories.districtHeatDelivery_kW;

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
	    this.energyCarrierConsumed = OL_EnergyCarriers.HEAT;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
	    
	    if (outputHeatCapacity_kW == 0) {
	    	throw new RuntimeException(String.format("Exception: J_EAConversionHeatDeliverySet with capacityHeat_kW = 0, invalid state! Energy Asset: %s", this));
	    }
	    
		registerEnergyAsset(timeParameters);
	}
    
    @Override
	public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
		this.energyUse_kW = powerFraction_fr * this.inputCapacity_kW * (1 - this.eta_r);
		this.energyUsed_kWh += this.energyUse_kW * this.timeParameters.getTimeStep_h();
    	this.flowsMap.put(this.energyCarrierConsumed, powerFraction_fr * this.inputCapacity_kW);
    	this.flowsMap.addFlow(this.energyCarrierProduced, -powerFraction_fr * this.outputCapacity_kW); // We don't put here, in case the energy carrier is the same
    	if (this.assetFlowCategory != null) {
    		this.assetFlowsMap.put(this.assetFlowCategory, powerFraction_fr * this.inputCapacity_kW);
    	}
	}

}

