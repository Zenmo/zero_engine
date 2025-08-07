/**
 * J_EAConversionElectricHeater
 */
public class J_EAConversionElectricHeater extends J_EAConversion implements Serializable, I_HeatingAsset {

	protected double outputTemperature_degC;
    /**
     * Default constructor
     */

    public J_EAConversionElectricHeater(Agent parentAgent, double outputHeatCapacity_kW, double efficiency_r, double timestep_h, double outputTemperature_degC) {
	    this.parentAgent = parentAgent;
	    this.outputCapacity_kW = outputHeatCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.inputCapacity_kW = this.outputCapacity_kW / this.eta_r;
	    this.timestep_h = timestep_h;	    
	    this.outputTemperature_degC = outputTemperature_degC;

	    this.energyAssetType = OL_EnergyAssetType.ELECTRIC_HEATER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
	    this.energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		this.assetFlowCategory = OL_AssetFlowCategories.heatPumpElectricityConsumption_kW;
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