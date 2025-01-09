/**
 * J_EAConversion
 */
public class J_EAConversion extends zero_engine.J_EA implements Serializable {
	protected OL_EnergyCarriers energyCarrierProduced;
	protected OL_EnergyCarriers energyCarrierConsumed;
	protected double eta_r;
	protected double inputCapacity_kW;
	protected double outputCapacity_kW;
	/**
     * Default constructor
     */
    public J_EAConversion() {
    }

    /**
     * Constructor initializing the fields
     */
    
    public J_EAConversion(Agent parentAgent, OL_EnergyAssetType energyAssetType, double outputCapacity_kW, double efficiency_r, OL_EnergyCarriers energyCarrierProduced, OL_EnergyCarriers energyCarrierConsumed, double timestep_h) {
	    this.parentAgent = parentAgent;
	    this.energyAssetType = energyAssetType;
	    this.outputCapacity_kW = outputCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.energyCarrierProduced = energyCarrierProduced;
	    this.energyCarrierConsumed = energyCarrierConsumed;
	    this.timestep_h = timestep_h;	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
	}

    @Override
    public void f_updateAllFlows(double powerFraction_fr) {
    	if ( powerFraction_fr < 0 ) {
			throw new RuntimeException("Impossible to operate conversion asset with negative powerfraction.");    		
    	}
    	else if ( powerFraction_fr == 0 ) {
    		this.lastFlowsMap.clear();
    		this.lastEnergyUse_kW = 0;
    		return;
    	}
    	else {
    		super.f_updateAllFlows( powerFraction_fr );
    	}
    }
    
	@Override
	public void operate(double ratioOfCapacity) {
		this.energyUse_kW = ratioOfCapacity * this.inputCapacity_kW * (1 - this.eta_r);
		this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;
    	this.flowsMap.put(this.energyCarrierConsumed, ratioOfCapacity * this.inputCapacity_kW);
    	this.flowsMap.addFlow(this.energyCarrierProduced, -ratioOfCapacity * this.outputCapacity_kW); // We don't put here, in case the energy carrier is the same
	}
	
	public void setInputCapacity_kW ( double inputCapacity_kW ) {
		this.inputCapacity_kW = inputCapacity_kW;
		this.outputCapacity_kW = this.inputCapacity_kW * this.eta_r;
	}
	
	public void setOutputCapacity_kW ( double outputCapacity_kW ) {
		this.outputCapacity_kW = outputCapacity_kW;
		this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	}
	
	public void setEta_r( double efficiency_r) {
		this.eta_r = efficiency_r;
		this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	}
	
	public double getInputCapacity_kW() {
		return this.inputCapacity_kW;
	}

	public double getOutputCapacity_kW() {
		return this.outputCapacity_kW;
	}
	
	public double getEta_r() {
		return this.eta_r;
	}
	
	public OL_EnergyCarriers getEnergyCarrierProduced() {
		return this.energyCarrierProduced;
	}
	
	public OL_EnergyCarriers getEnergyCarrierConsumed() {
		return this.energyCarrierConsumed;
	}
	
	@Override
	public String toString() {	
		return  this.energyAssetType + " in GC: " + this.parentAgent + ", "			
				+ this.energyCarrierConsumed + " -> " + this.energyCarrierProduced + ", "
				+ "OutputCapacity: " + this.outputCapacity_kW + " kW, " 
				+ "with efficiency: " + this.eta_r + ", "
				+ "Current output: " + -this.getLastFlows().get(this.energyCarrierProduced) + " kW";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}                         