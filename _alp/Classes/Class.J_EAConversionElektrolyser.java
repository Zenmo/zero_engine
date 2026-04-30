/**
 * J_EAConversionElektrolyser
 */
public class J_EAConversionElektrolyser extends zero_engine.J_EAConversion implements Serializable {

    /**
	/**
     * Default constructor
     */
    public J_EAConversionElektrolyser() {
    }

    /**
     * Constructor initializing the fields
     */
    
    public J_EAConversionElektrolyser(Agent parentAgent, double inputElectricCapacity_kW, double efficiency_r, double timestep_h) {
    	this.parentAgent = parentAgent;
	    this.inputCapacity_kW = inputElectricCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.outputCapacity_kW = this.inputCapacity_kW * this.eta_r;
	    this.timestep_h = timestep_h;	    

	    this.energyAssetType = OL_EnergyAssetType.ELECTROLYSER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HYDROGEN;
	    this.energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
	}
    
    @Override
	public void setEta_r( double efficiency_r) {
		this.eta_r = efficiency_r;
		this.outputCapacity_kW = this.inputCapacity_kW * this.eta_r;
	}
    
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}                         