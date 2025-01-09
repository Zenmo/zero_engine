/**
 * J_EAConversionCurtailer
 */
public class J_EAConversionCurtailer extends J_EAConversion implements Serializable {

	private OL_EnergyCarriers energyCarrierProduced = OL_EnergyCarriers.HEAT;
	private OL_EnergyCarriers energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;
    /**
     * Default constructor
     */
    public J_EAConversionCurtailer(Agent parentAgent, double capacityElectric_kW, double efficiency, double timestep_h) {
	    this.parentAgent = parentAgent;
	    this.capacityElectric_kW = capacityElectric_kW;
	    this.eta_r = efficiency;
	    this.capacityHeat_kW = this.capacityElectric_kW * this.eta_r;
	    this.timestep_h = timestep_h;
		registerEnergyAsset();
    }

    @Override
    public double[] operate(double ratioOfCapacity) {
		//traceln("I convert now! GasBurner @ " + (ratioOfCapacity * 100) + " %");
		this.heatProduction_kW = capacityElectric_kW * ratioOfCapacity * eta_r;
		this.electricityConsumption_kW = capacityElectric_kW * ratioOfCapacity;
		this.energyUse_kW = (electricityConsumption_kW - heatProduction_kW);
		this.energyUsed_kWh += timestep_h * (energyUse_kW); // This represents losses!
		//double[] arr = {this.electricityProduction_kW, this.methaneProduction_kW, this.hydrogenProduction_kW, this.heatProduction_kW, this.electricityConsumption_kW, this.methaneConsumption_kW, this.hydrogenConsumption_kW, this.heatConsumption_kW };
    	//return arr;
		return returnEnergyFlows();
    }

	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}

	public void setCapacityElectric_kW(double capacityElectric_kW) {
		this.capacityElectric_kW = capacityElectric_kW;
	}
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}