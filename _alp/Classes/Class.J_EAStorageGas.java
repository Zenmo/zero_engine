/**
 * J_EAStorageGas
 */
public class J_EAStorageGas extends zero_engine.J_EAStorage implements Serializable {

	private OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.METHANE;
	private double lossFactor_r = 0;
	protected double capacityGas_kW;
	//public double capacityGas_kW = 0;

    /**
     * Default constructor
     */
    public J_EAStorageGas() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAStorageGas(Agent parentAgent, double capacityGas_kW, double storageCapacity_kWh, double stateOfCharge_r, double timestep_h ) {
		this.parentAgent = parentAgent;
		this.capacityGas_kW = capacityGas_kW;
		this.storageCapacity_kWh = storageCapacity_kWh;
		this.stateOfCharge_r = stateOfCharge_r;
		this.timestep_h = timestep_h;
	    this.activeProductionEnergyCarriers.add(this.energyCarrier);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
		registerEnergyAsset();
    }

	@Override
	public void calculateLoss() {
		//no loss for gas storage modeled.
		energyUse_kW = 0;
		energyUsed_kWh += energyUse_kW * timestep_h;
	}

	@Override
	//public Pair<J_FlowsMap, Double> operate(double ratioOfChargeCapacity_r) {
	public void operate(double ratioOfChargeCapacity_r) {
    	double deltaEnergy_kWh;   // to check the request with the energy currently in storage
    	double inputPower_kW = ratioOfChargeCapacity_r * capacityGas_kW; // Gas power going into Storage, negative if going out.

    	deltaEnergy_kWh = inputPower_kW * timestep_h;
    	deltaEnergy_kWh = - min( -deltaEnergy_kWh, (stateOfCharge_r * storageCapacity_kWh) ); // Prevent negative charge
    	deltaEnergy_kWh = min(deltaEnergy_kWh, (1 - stateOfCharge_r) * storageCapacity_kWh ); // Prevent overcharge

    	inputPower_kW = deltaEnergy_kWh / timestep_h;

		double methaneProduction_kW = max(-inputPower_kW, 0);
		double methaneConsumption_kW = max(inputPower_kW, 0);
		discharged_kWh += methaneProduction_kW * timestep_h;
		charged_kWh += methaneConsumption_kW * timestep_h;
		
		updateStateOfCharge( deltaEnergy_kWh );
		//double[] arr = {electricityProduction_kW, methaneProduction_kW, hydrogenProduction_kW, heatProduction_kW, electricityConsumption_kW, methaneConsumption_kW, hydrogenConsumption_kW, heatConsumption_kW };
    	//return arr;
		flowsMap.put(OL_EnergyCarriers.METHANE, methaneConsumption_kW-methaneProduction_kW);		
		
		//return this.flowsMap;

	}

	@Override
	public String toString() {
		return 
			"type = " + this.getClass().toString() + " " +
			"parentAgent = " + parentAgent +" " +
			"stateOfCharge_r = " + this.stateOfCharge_r+" "+
			"storageCapacity_kWh = " + this.storageCapacity_kWh +" "+
			"capacityGas_kW = " + this.capacityGas_kW +" "+
			"discharged_kWh " + this.discharged_kWh+" "+
			"charged_kWh " + this.charged_kWh+" ";
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		stateOfCharge_r += deltaEnergy_kWh / storageCapacity_kWh;
	}

	@Override
	public double getCurrentStateOfCharge() {
    	return stateOfCharge_r;
	}

	public double getCapacityAvailable_kW() {
		double availableCapacity_kW;
		if ( stateOfCharge_r * storageCapacity_kWh  > capacityGas_kW * timestep_h) {
			availableCapacity_kW = capacityGas_kW;
		}
		else {
			availableCapacity_kW =  stateOfCharge_r * storageCapacity_kWh / timestep_h; // Allow to drain completely
		}
		return availableCapacity_kW;
	}
	
    public double getCapacityGas_kW() {
    	return capacityGas_kW;
    }
	
	public double getTotalChargeAmount_kWh() {
		return charged_kWh;
	}
	public double getTotalDischargeAmount_kWh() {
		return discharged_kWh;
	}


	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
} 
 
