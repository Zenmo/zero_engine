/**
 * J_EAConversionGasCHP
 */
public class J_EAConversionGasCHP extends zero_engine.J_EAConversion implements Serializable {

	protected double outputTemperature_degC;
	protected double outputHeatCapacity_kW;
	protected double outputElectricCapacity_kW;
	protected List<OL_EnergyCarriers> energyCarriersProduced = new ArrayList<>();
    
	/**
     * Default constructor
     */
	
    public J_EAConversionGasCHP() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAConversionGasCHP(Agent parentAgent, double outputElectricCapacity_kW, double outputHeatCapacity_kW, double efficiency, double timestep_h, double outputTemperature_degC ) {
	    if (outputElectricCapacity_kW < 0 || outputHeatCapacity_kW < 0 || (outputElectricCapacity_kW == 0 && outputHeatCapacity_kW == 0)) {
	    	throw new RuntimeException("Impossible capacities for J_EAConversionGasCHP. outputElectricCapacity_kW: " + outputElectricCapacity_kW + ", outputHeatCapacity_kW: " + outputHeatCapacity_kW);
	    }
    	this.parentAgent = parentAgent;
	    this.outputElectricCapacity_kW = outputElectricCapacity_kW;
	    this.outputHeatCapacity_kW = outputHeatCapacity_kW; 
	    this.eta_r = efficiency;
	    this.inputCapacity_kW = (outputElectricCapacity_kW + outputHeatCapacity_kW) / this.eta_r ;
	    this.timestep_h = timestep_h;
	    this.outputTemperature_degC = outputTemperature_degC;
	    
	    this.energyAssetType = OL_EnergyAssetType.METHANE_CHP;
	    
    	this.energyCarriersProduced.add( OL_EnergyCarriers.ELECTRICITY );
    	this.energyCarriersProduced.add( OL_EnergyCarriers.HEAT );
    	this.energyCarrierConsumed= OL_EnergyCarriers.METHANE;
    	
        this.activeProductionEnergyCarriers.addAll(energyCarriersProduced);
    	this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
	    registerEnergyAsset();
	}

	@Override
    public void operate(double ratioOfCapacity) {
    	double electricityProduction_kW = this.outputElectricCapacity_kW * ratioOfCapacity;
		double heatProduction_kW = this.outputHeatCapacity_kW * ratioOfCapacity;
		double methaneConsumption_kW = this.inputCapacity_kW * ratioOfCapacity;
		
		this.energyUse_kW = methaneConsumption_kW - heatProduction_kW - electricityProduction_kW ;
		this.energyUsed_kWh += energyUse_kW * timestep_h;

		this.heatProduced_kWh += heatProduction_kW * timestep_h;
		this.electricityProduced_kWh += electricityProduction_kW * timestep_h;
		
		flowsMap.put(OL_EnergyCarriers.HEAT, -heatProduction_kW);		
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, -electricityProduction_kW);
		flowsMap.put(OL_EnergyCarriers.METHANE, methaneConsumption_kW);
    }

	@Override
	public void setInputCapacity_kW( double inputCapacity_kW) {
		double scaling_fr = inputCapacity_kW / this.inputCapacity_kW;
		this.inputCapacity_kW = inputCapacity_kW;
		this.outputHeatCapacity_kW = scaling_fr * this.outputHeatCapacity_kW;
		this.outputElectricCapacity_kW = scaling_fr * this.outputElectricCapacity_kW;
	}
	
	@Override
	public void setEta_r( double efficiency) {
	    this.eta_r = efficiency;
	    this.inputCapacity_kW = (this.outputElectricCapacity_kW + this.outputHeatCapacity_kW) / this.eta_r ;
	}
	
	@Override
	public OL_EnergyCarriers getEnergyCarrierProduced() {
		throw new RuntimeException("Can not get EnergyCarrierProduced from J_EAConversionGasCHP, use getEnergyCarriersProduced() instead.");
	}
	
	public List<OL_EnergyCarriers> getEnergyCarriersProduced() {
		return this.energyCarriersProduced;
	}
	
	@Override
	public String toString() {	
		return  this.energyAssetType + " in GC: " + this.parentAgent + ", "				
				+ "OutputElectricCapacity: " + this.outputElectricCapacity_kW + " kW, " 
				+ "OutputHeatCapacity: " + this.outputHeatCapacity_kW + " kW, "
				+ "with efficiency: " + this.eta_r + ", "
				+ "Current electric output: " + -this.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY) + " kW, "
				+ "Current heat output: " + -this.getLastFlows().get(OL_EnergyCarriers.HEAT) + " kW";
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