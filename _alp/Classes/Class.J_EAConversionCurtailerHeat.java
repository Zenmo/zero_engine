/**
 * J_EAConversionCurtailerHeat
 */
public class J_EAConversionCurtailerHeat extends J_EAConversion implements Serializable {

	private OL_EnergyCarriers energyCarrierProduced = OL_EnergyCarriers.HEAT;
	private OL_EnergyCarriers energyCarrierConsumed = OL_EnergyCarriers.HEAT;
    /**
     * Default constructor
     */
    public J_EAConversionCurtailerHeat(Agent parentAgent, double capacityHeat_kW, double efficiency, double timestep_h) {
	    this.parentAgent = parentAgent;
	    this.capacityHeat_kW = capacityHeat_kW;
	    this.eta_r = efficiency;
//	    this.capacityHeat_kW = this.capacityHeat_kW * this.eta_r;
	    this.timestep_h = timestep_h;
		registerEnergyAsset();
    }

    @Override
    public double[] operate(double ratioOfCapacity) {
		//traceln("I convert now! GasBurner @ " + (ratioOfCapacity * 100) + " %");
		heatProduction_kW = capacityHeat_kW * ratioOfCapacity * eta_r;
		heatConsumption_kW = capacityHeat_kW * ratioOfCapacity;
		energyUse_kW = (heatConsumption_kW - heatProduction_kW);
		energyUsed_kWh += timestep_h * energyUse_kW; // This represents losses!
		//double[] arr = {this.electricityProduction_kW, this.methaneProduction_kW, this.hydrogenProduction_kW, this.heatProduction_kW, this.electricityConsumption_kW, this.methaneConsumption_kW, this.hydrogenConsumption_kW, this.heatConsumption_kW };
    	//return arr;
		heatConsumed_kWh += heatConsumption_kW * timestep_h;
		heatProduced_kWh += heatProduction_kW * timestep_h;
		
		return returnEnergyFlows();
    }
    
    @Override
	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}
	
	@Override
	public double getHeatCapacity_kW() {
		return capacityHeat_kW;
	}

	@Override
	public String toString() {
		return
			"parentAgent = " + parentAgent +", Energy consumed = " + energyUsed_kWh + " " +
			"capacityHeat_kW = " + capacityHeat_kW +" "+
			"eta_r = " + eta_r+" " +
			"energyUsed_kWh (losses) = " + energyUsed_kWh + " "+
			"heatConsumed_kWh = " + heatConsumed_kWh + " "+
			"heatProduced_kWh = " + heatProduced_kWh + " ";
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}