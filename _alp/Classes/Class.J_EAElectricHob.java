/**
 * J_EAElectricHob
 */	
public class J_EAElectricHob extends J_EAConversion implements Serializable {

	private OL_EnergyCarriers energyCarrierProduced = OL_EnergyCarriers.HEAT;
	private OL_EnergyCarriers energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;
	protected double outputTemperature_degC;
	protected double capacityElectric_kW;

    /**
     * Default constructor
     */
    public J_EAElectricHob(Agent ownerAgent, double capacityThermal_kW, double efficiency, double timestep_h, double outputTemperature_degC) {
    	this.parentAgent= ownerAgent;
	    //this.capacityHeat_kW = capacityThermal_kW;
	    this.eta_r = efficiency;
	    //this.capacityElectric_kW = capacityThermal_kW / eta_r;
	    this.timestep_h = timestep_h;
	    this.outputTemperature_degC = outputTemperature_degC;
	    this.energyAssetType = OL_EnergyAssetType.ELECTRIC_HOB;
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
    }

    @Override 
    public void operate( double ratioOfCapacity ) {
    	//traceln("I convert now! GasBurner @ " + (ratioOfCapacity * 100) + " %");
    	double heatProduction_kW = capacityElectric_kW * ratioOfCapacity * eta_r;
		double electricityConsumption_kW = capacityElectric_kW * ratioOfCapacity;
		this.energyUse_kW = (electricityConsumption_kW - heatProduction_kW);
		this.energyUsed_kWh += timestep_h * (electricityConsumption_kW - heatProduction_kW); // This represents losses!
		//double[] arr = {this.electricityProduction_kW, this.methaneProduction_kW, this.hydrogenProduction_kW, this.heatProduction_kW, this.electricityConsumption_kW, this.methaneConsumption_kW, this.hydrogenConsumption_kW, this.heatConsumption_kW };
    	//return arr;
		this.heatProduced_kWh += heatProduction_kW * timestep_h;

		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);		
		flowsMap.put(OL_EnergyCarriers.HEAT, -heatProduction_kW);		
		
		//return new Pair(this.flowsMap, this.energyUse_kW);

    }
    
	@Override
	public String toString() {
		return
			"AssetType = " + energyAssetType + 
			" parentAgent = " + parentAgent +", Energy consumed = " + this.energyUsed_kWh +
			" capacityElectric_kW = " + this.capacityElectric_kW +" "+
			//"capacityHeat_kW = " + this.capacityHeat_kW +" "+
			"eta_r = " + this.eta_r+" " +
			"outputTemperature_degC = " + this.outputTemperature_degC + " "+
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " "+
			"heatProducted_kWh = " +this.heatProduced_kWh + " ";
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