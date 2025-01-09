/**
 * J_EAConversionFuelCell
 */
public class J_EAConversionFuelCell extends zero_engine.J_EAConversion implements Serializable {

    /**
	/**
     * Default constructor
     */
    public J_EAConversionFuelCell() {
    }

    /**
     * Constructor initializing the fields
     */

    public J_EAConversionFuelCell(Agent parentAgent, double outputElectricCapacity_kW, double efficiency_r, double timestep_h) {
    	this.parentAgent = parentAgent;
	    this.outputCapacity_kW = outputElectricCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.timestep_h = timestep_h;	    

	    this.energyAssetType = OL_EnergyAssetType.FUEL_CELL;

	    this.energyCarrierProduced = OL_EnergyCarriers.ELECTRICITY;
	    this.energyCarrierConsumed = OL_EnergyCarriers.HYDROGEN;
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
	}
    
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}                         