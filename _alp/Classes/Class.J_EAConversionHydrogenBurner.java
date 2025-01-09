/**
* J_EAConversionGasBurner
*/
public class J_EAConversionHydrogenBurner extends zero_engine.J_EAConversion implements Serializable {
 
	protected double outputTemperature_degC;
    /**
     * Default constructor
     */
    public J_EAConversionHydrogenBurner() {
    }
 
    /**
     * Constructor initializing the fields
     */    
    public J_EAConversionHydrogenBurner(Agent parentAgent, double outputHeatCapacity_kW, double efficiency_r, double timestep_h, double outputTemperature_degC) {
    	this.parentAgent = parentAgent;
	    this.outputCapacity_kW = outputHeatCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.timestep_h = timestep_h;	    
	    this.outputTemperature_degC = outputTemperature_degC;

	    this.energyAssetType = OL_EnergyAssetType.HYDROGEN_BURNER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
	    this.energyCarrierConsumed = OL_EnergyCarriers.HYDROGEN;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
	}
	
	@Override
	public double getCurrentTemperature() {
		return outputTemperature_degC;
	}
 
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
 
}