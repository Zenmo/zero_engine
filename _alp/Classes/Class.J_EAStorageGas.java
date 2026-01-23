/**
 * J_EAStorageGas
 */
public class J_EAStorageGas extends zero_engine.J_EAStorage implements Serializable {

	private OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.METHANE;
	private double lossFactor_r = 0;
	protected double capacityGas_kW;

    /**
     * Default constructor
     */
    public J_EAStorageGas() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAStorageGas(I_AssetOwner owner, double capacityGas_kW, double storageCapacity_kWh, double stateOfCharge_fr, double timestep_h ) {
		this.setOwner(owner);
		this.capacityGas_kW = capacityGas_kW;
		this.storageCapacity_kWh = storageCapacity_kWh;
		this.stateOfCharge_fr = stateOfCharge_fr;
		this.timestep_h = timestep_h;
	    this.activeProductionEnergyCarriers.add(this.energyCarrier);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
		registerEnergyAsset();
    }

	@Override
	public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
    	double inputPower_kW = powerFraction_fr * capacityGas_kW; // Gas power going into Storage, negative if going out.

    	double deltaEnergy_kWh = inputPower_kW * timestep_h;
    	deltaEnergy_kWh = - min( -deltaEnergy_kWh, (stateOfCharge_fr * storageCapacity_kWh) ); // Prevent negative charge
    	deltaEnergy_kWh = min(deltaEnergy_kWh, (1 - stateOfCharge_fr) * storageCapacity_kWh ); // Prevent overcharge

    	inputPower_kW = deltaEnergy_kWh / timestep_h;

		double methaneProduction_kW = max(-inputPower_kW, 0);
		double methaneConsumption_kW = max(inputPower_kW, 0);
		discharged_kWh += methaneProduction_kW * timestep_h;
		charged_kWh += methaneConsumption_kW * timestep_h;
		
		updateStateOfCharge( deltaEnergy_kWh );
		flowsMap.put(OL_EnergyCarriers.METHANE, methaneConsumption_kW-methaneProduction_kW);
	}

	@Override
	public String toString() {
		return 
			"type = " + this.getClass().toString() + " " +
			"stateOfCharge_fr = " + this.stateOfCharge_fr+" "+
			"storageCapacity_kWh = " + this.storageCapacity_kWh +" "+
			"capacityGas_kW = " + this.capacityGas_kW +" "+
			"discharged_kWh " + this.discharged_kWh+" "+
			"charged_kWh " + this.charged_kWh+" ";
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		stateOfCharge_fr += deltaEnergy_kWh / storageCapacity_kWh;
	}

	public double getCapacityAvailable_kW() {
		double availableCapacity_kW;
		if ( stateOfCharge_fr * storageCapacity_kWh  > capacityGas_kW * timestep_h) {
			availableCapacity_kW = capacityGas_kW;
		}
		else {
			availableCapacity_kW =  stateOfCharge_fr * storageCapacity_kWh / timestep_h; // Allow to drain completely
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
 
