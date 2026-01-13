/**
* J_EAConversionGasBurner
*/
public class J_EAConversionHydrogenBurner extends zero_engine.J_EAConversion implements Serializable, I_HeatingAsset {
 
	protected double outputTemperature_degC;
    /**
     * Default constructor
     */
    public J_EAConversionHydrogenBurner() {
    }
 
    /**
     * Constructor initializing the fields
     */    
    public J_EAConversionHydrogenBurner(Agent parentAgent, double outputHeatCapacity_kW, double efficiency_r, J_TimeParameters timeParameters, double outputTemperature_degC) {
    	this.parentAgent = parentAgent;
	    this.outputCapacity_kW = outputHeatCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.timeParameters = timeParameters;	    
	    this.outputTemperature_degC = outputTemperature_degC;

	    this.energyAssetType = OL_EnergyAssetType.HYDROGEN_BURNER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
	    this.energyCarrierConsumed = OL_EnergyCarriers.HYDROGEN;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
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