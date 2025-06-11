/**
 * Airco
 */	
public class J_EAAirco extends zero_engine.J_EAEV implements Serializable {

	int remainingONtimesteps = 0;
    /**
     * Default constructor
     */
    public J_EAAirco(Agent parentAgent, double capacityElectric_kW, double timestep_h) {
    	this.parentAgent = parentAgent;
    	this.capacityElectric_kW = capacityElectric_kW;
    	this.timestep_h = timestep_h;	
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
	}
    
	@Override
    public void operate(double ratioOfCapacity) {
		if( remainingONtimesteps > 0) {
			remainingONtimesteps--;
		}
		else {
			ratioOfCapacity = 0;
		}
		double electricityConsumption_kW = ratioOfCapacity * capacityElectric_kW;
    	energyUse_kW = electricityConsumption_kW;
    	energyUsed_kWh += energyUse_kW * timestep_h;
    	flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);
   	}

	public void turnOnAirco(int nbTimesteps) {
		remainingONtimesteps = nbTimesteps;
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