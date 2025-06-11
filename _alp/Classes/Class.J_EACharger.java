/**
 * J_EACharger
 */	
public class J_EACharger extends zero_engine.J_EA implements Serializable {
		double discharged_kWh;
		double charged_kWh;
		double capacityElectric_kW;
		List<J_ChargingSession> chargerProfile;
		
	    /**
	     * Default constructor
	     */
	    public J_EACharger(Agent parentAgent, double electricCapacity_kW, double timestep_h, List<J_ChargingSession> chargerProfile) {
	    	this.parentAgent = parentAgent;
	    	this.capacityElectric_kW = electricCapacity_kW;
	    	this.timestep_h = timestep_h;
	    	this.chargerProfile = chargerProfile;
		    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);		
			this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
	    }
	    
	    
		@Override
	    public void operate(double ratioOfCapacity) {
			
			double charge_kW = ratioOfCapacity * capacityElectric_kW;
	    	
			double electricityProduction_kW = max(-charge_kW, 0);
			double electricityConsumption_kW = max(charge_kW, 0);
			
			discharged_kWh += electricityProduction_kW * timestep_h;
			charged_kWh += electricityConsumption_kW * timestep_h;
			
	    	energyUse_kW = electricityConsumption_kW - electricityProduction_kW;
	    	energyUsed_kWh += energyUse_kW * timestep_h;
	    	
			flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW - electricityProduction_kW);	
	   	}

		@Override
		public String toString() {
			return "Power: " + getLastFlows().get(OL_EnergyCarriers.ELECTRICITY) + " kW, capacity: " + capacityElectric_kW + " kW" ;
		}

		/**
		 * This number is here for model snapshot storing purpose<br>
		 * It needs to be changed when this class gets changed
		 */ 
		private static final long serialVersionUID = 1L;

}
