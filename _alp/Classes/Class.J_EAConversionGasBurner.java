/**
 * J_EAConversionGasBurner
 */
public class J_EAConversionGasBurner extends zero_engine.J_EAConversion implements Serializable, I_HeatingAsset {

	protected double outputTemperature_degC;

	/**
     * Default constructor
     */
    public J_EAConversionGasBurner() {
    }

    /**
     * Constructor initializing the fields
     */

    public J_EAConversionGasBurner(I_AssetOwner owner, double outputHeatCapacity_kW, double efficiency_r, J_TimeParameters timeParameters, double outputTemperature_degC) {
		this.setOwner(owner);
	    this.timeParameters = timeParameters;	    
		this.outputCapacity_kW = outputHeatCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.outputTemperature_degC = outputTemperature_degC;

	    this.energyAssetType = OL_EnergyAssetType.GAS_BURNER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
	    this.energyCarrierConsumed = OL_EnergyCarriers.METHANE;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
	    
	    if (outputHeatCapacity_kW == 0) {
	    	throw new RuntimeException(String.format("Exception: J_EAGasBurner with capacityHeat_kW = 0, invalid state! Energy Asset: %s", this));
	    }
	    
	    registerEnergyAsset();
	}
    
    @Override
    public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
    	((GridConnection)this.parentAgent).fm_heatFromEnergyCarrier_kW.addFlow(this.energyCarrierConsumed, powerFraction_fr * this.outputCapacity_kW);
    	((GridConnection)this.parentAgent).fm_consumptionForHeating_kW.addFlow(this.energyCarrierConsumed, powerFraction_fr * this.inputCapacity_kW);
    	super.operate(powerFraction_fr, timeVariables);
    }
    
	/*@Override
	public double getCurrentTemperature() {
		return outputTemperature_degC;
	}*/
    

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}