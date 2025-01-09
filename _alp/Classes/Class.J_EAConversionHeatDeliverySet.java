/**
 * J_EAConversionHeatDeliverySet
 */
public class J_EAConversionHeatDeliverySet extends zero_engine.J_EAConversion implements Serializable {

	protected double outputTemperature_degC;
    /**
     * Default constructor
     */
    public J_EAConversionHeatDeliverySet() {
    }

    /**
     * Constructor initializing the fields
     */
    
    public J_EAConversionHeatDeliverySet(Agent parentAgent, double outputHeatCapacity_kW, double efficiency_r, double timestep_h, double outputTemperature_degC) {

    	this.parentAgent = parentAgent;
	    this.outputCapacity_kW = outputHeatCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.timestep_h = timestep_h;	    
	    this.outputTemperature_degC = outputTemperature_degC;

	    this.energyAssetType = OL_EnergyAssetType.HEAT_DELIVERY_SET;

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

