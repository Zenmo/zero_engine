/**
 * J_EAConversionBiogasMethane
 */
public class J_EAConversionBiogasMethane extends zero_engine.J_EAConversion implements Serializable {

	//private OL_EnergyCarriers energyCarrierProduced = OL_EnergyCarriers.METHANE;
	//private OL_EnergyCarriers energyCarrierConsumed = OL_EnergyCarriers.METHANE;
	public double capacityMethane_kW;
	//protected double outputTemperature_degC;
    /**
     * Default constructor
     */
    public J_EAConversionBiogasMethane() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAConversionBiogasMethane(Agent parentAgent, double capacityMethane_kW, double efficiency, double timestep_h) {
	    this.parentAgent= parentAgent;
	    this.capacityMethane_kW = capacityMethane_kW;
	    this.eta_r = efficiency;
	    this.timestep_h = timestep_h;
		registerEnergyAsset();
	}

    @Override
    public Pair<J_FlowsMap, Double> operate(double ratioOfCapacity) {
		//traceln("I convert now! GasBurner @ " + (ratioOfCapacity * 100) + " %");
    	this.methaneProduction_kW = capacityMethane_kW * ratioOfCapacity;
		this.methaneConsumption_kW = methaneProduction_kW / eta_r;
		energyUse_kW = methaneConsumption_kW - methaneProduction_kW;   	
		this.energyUsed_kWh += timestep_h * energyUse_kW; // This represents losses!
		//double[] arr = {this.electricityProduction_kW, this.methaneProduction_kW, this.hydrogenProduction_kW, this.heatProduction_kW, this.electricityConsumption_kW, this.methaneConsumption_kW, this.hydrogenConsumption_kW, this.heatConsumption_kW };
    	//return arr;
		return returnEnergyFlows();
    }

	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}

	@Override
	public String toString() {
		return
			"parentAgent = " + parentAgent +", Energy consumed = " + this.energyUsed_kWh +
			"capacityMethane_kW = " + this.capacityMethane_kW +" "+
			"eta_r = " + this.eta_r+" " +
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " ";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}