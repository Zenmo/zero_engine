/**
 * J_EAElectricHob
 */	
public class J_EAElectricHob extends J_EAConversion implements Serializable {

	protected double outputTemperature_degC;

    /**
     * Default constructor
     */
	// The efficiency is the amount of heat that is retained within the building
    public J_EAElectricHob(Agent ownerAgent, double inputCapacity_kW, double efficiency, double timestep_h, double outputTemperature_degC) {
    	this.parentAgent= ownerAgent;
	    this.inputCapacity_kW = inputCapacity_kW;
	    this.eta_r = efficiency;
	    this.outputCapacity_kW = inputCapacity_kW * efficiency;
	    this.timestep_h = timestep_h;
	    this.outputTemperature_degC = outputTemperature_degC;
		this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
		this.energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;
	    this.energyAssetType = OL_EnergyAssetType.ELECTRIC_HOB;
	    this.assetFlowCategory = OL_AssetFlowCategories.electricHobConsumption_kW;
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
    }

    @Override
    public void operate( double ratioOfCapacity ) {
    	double heatProduction_kW = this.inputCapacity_kW * ratioOfCapacity * eta_r;
		double electricityConsumption_kW = this.inputCapacity_kW * ratioOfCapacity;
		this.energyUse_kW = (electricityConsumption_kW - heatProduction_kW);
		this.energyUsed_kWh += timestep_h * (electricityConsumption_kW - heatProduction_kW); // This represents losses!
		this.heatProduced_kWh += heatProduction_kW * timestep_h;
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);		
		flowsMap.put(OL_EnergyCarriers.HEAT, -heatProduction_kW);	
		assetFlowsMap.put(this.assetFlowCategory, electricityConsumption_kW);
    }
    
	@Override
	public String toString() {
		return
			"AssetType = " + energyAssetType + 
			" parentAgent = " + parentAgent +", Energy consumed = " + this.energyUsed_kWh +
			" capacityElectric_kW = " + this.inputCapacity_kW +" "+
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