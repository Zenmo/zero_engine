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
	    
		registerEnergyAsset();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}

