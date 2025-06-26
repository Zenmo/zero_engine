/**
 * J_EACharger
 */	
public class J_EACharger extends zero_engine.J_EA implements Serializable {
		public double discharged_kWh;
		public double charged_kWh;
		public double capacityElectric_kW;
		public List<J_ChargingSession> chargerProfile;
		public boolean V1GCapable;
		public boolean V2GCapable;
		
		private int currentChargingSessionIndexSocket1;
		private J_ChargingSession currentChargingSessionSocket1;
		private int currentChargingSessionIndexSocket2;
		private J_ChargingSession currentChargingSessionSocket2;
		
		private double totalShiftedLoadV1G_kWh;
		private double totalShiftedLoadV2G_kWh;
		
		
		private double dischargedStored_kWh;
		private double chargedStored_kWh;
		private J_ChargingSession currentChargingSessionSocket1Stored;
		private int currentChargingSessionIndexSocket1Stored;
		private J_ChargingSession currentChargingSessionSocket2Stored;
		private int currentChargingSessionIndexSocket2Stored;
		private double totalShiftedLoadV1GStored_kWh;
		private double totalShiftedLoadV2GStored_kWh;
		
	    /**
	     * Default constructor
	     */
	    public J_EACharger(Agent parentAgent, double electricCapacity_kW, double timestep_h, List<J_ChargingSession> chargerProfile, boolean V1GCapable, boolean V2GCapable) {
	    	this.parentAgent = parentAgent;
	    	this.capacityElectric_kW = electricCapacity_kW;
	    	this.timestep_h = timestep_h;
	    	this.chargerProfile = chargerProfile;
		    this.V1GCapable = V1GCapable;
		    this.V2GCapable = V2GCapable;
	    	this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);		
			this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
			this.registerEnergyAsset();
	    }
	    
	    
	    public void f_updateAllFlows( double t_h, boolean doV1G, boolean doV2G) {
	    	// Powerfraction is calculated below, argument is the current time (energyModel.t_h)
	    	
	    	// Check if the charger is capable of smart charging
	    	doV1G = doV1G && this.V1GCapable;
	    	doV2G = doV2G && this.V2GCapable;
	    	
	    	// Update the J_ChargingSessions of the sockets
			this.manageSocket1();
			this.manageSocket2();
	    	
			// Calculate the power output of the sockets
			double power_kW = 0.0;
			int currentTimeInQuarterHours = roundToInt(t_h * 4);
			if ( this.currentChargingSessionSocket1 != null && currentTimeInQuarterHours >= this.currentChargingSessionSocket1.startTime && currentTimeInQuarterHours < this.currentChargingSessionSocket1.endTime ) {		
				//null check for currentChargingSessionSocket1 (and 2) is required for end of year when there are no more scheduled sessions
				power_kW += this.operateChargerSocket1(doV1G, doV2G);
			}
			if ( this.currentChargingSessionSocket2 != null && currentTimeInQuarterHours >= this.currentChargingSessionSocket2.startTime && currentTimeInQuarterHours < this.currentChargingSessionSocket2.endTime ) {	
				power_kW += this.operateChargerSocket2(doV1G, doV2G);
			}
			
			double powerFraction_fr = power_kW / this.capacityElectric_kW;
			
			// Call the regular J_EA updateAllFlows and operate
			super.f_updateAllFlows( powerFraction_fr );
	    	return;
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
		
		private void manageSocket1() {
			if ( this.currentChargingSessionSocket1 == null ) { // || energyModel.t_h >= v_currentChargingSessionSocket1.endTime / 4.0) {
				 // check if we are not already past the last charging session
				if (this.currentChargingSessionIndexSocket1 >= this.chargerProfile.size()) {
					//v_currentChargingSessionSocket1 = null;
					return;
				}
				while (this.chargerProfile.get(this.currentChargingSessionIndexSocket1).socket != 1) {
					this.currentChargingSessionIndexSocket1++;
					if (this.currentChargingSessionIndexSocket1 >= this.chargerProfile.size()) {
						return;
					}
				}
				this.currentChargingSessionSocket1 = this.chargerProfile.get(this.currentChargingSessionIndexSocket1).getClone();
				this.currentChargingSessionIndexSocket1++;
			}
		}
		
		private void manageSocket2() {		
			if ( this.currentChargingSessionSocket2 == null ) {
				 // check if we are not already past the last charging session
				if (this.currentChargingSessionIndexSocket2 >= this.chargerProfile.size()) {
					return;
				}
				while (this.chargerProfile.get(this.currentChargingSessionIndexSocket2).socket != 2) {
					this.currentChargingSessionIndexSocket2++;
					if (this.currentChargingSessionIndexSocket2 >= this.chargerProfile.size()) {
						return;
					}
				}
				this.currentChargingSessionSocket2 = this.chargerProfile.get(this.currentChargingSessionIndexSocket2).getClone();
				this.currentChargingSessionIndexSocket2++;
			}
		}

		private double operateChargerSocket1(boolean doV1G, boolean doV2G) {
			double chargingPower_kW = this.currentChargingSessionSocket1.operate( doV1G, doV2G );
			this.totalShiftedLoadV1G_kWh += this.currentChargingSessionSocket1.getShiftedLoadV1GCurrentTimestep();
			this.totalShiftedLoadV2G_kWh += this.currentChargingSessionSocket1.getShiftedLoadV2GCurrentTimestep();
			if ( this.currentChargingSessionSocket1.timeStepsToDisconnect == 0 ){
				this.currentChargingSessionSocket1 = null;
			}
			return chargingPower_kW;
		}
		
		private double operateChargerSocket2(boolean doV1G, boolean doV2G) {
			double chargingPower_kW = this.currentChargingSessionSocket2.operate( doV1G, doV2G );
			this.totalShiftedLoadV1G_kWh += this.currentChargingSessionSocket2.getShiftedLoadV1GCurrentTimestep();
			this.totalShiftedLoadV2G_kWh += this.currentChargingSessionSocket2.getShiftedLoadV2GCurrentTimestep();
			if ( this.currentChargingSessionSocket2.timeStepsToDisconnect == 0 ){
				this.currentChargingSessionSocket2 = null;
			}
			return chargingPower_kW;
		}
		
		@Override
		public void storeStatesAndReset() {
	    	energyUsedStored_kWh = energyUsed_kWh;
	    	energyUsed_kWh = 0.0;
			dischargedStored_kWh = discharged_kWh;
			discharged_kWh = 0.0;
			chargedStored_kWh = charged_kWh;
			charged_kWh = 0.0;
			
			currentChargingSessionSocket1Stored = currentChargingSessionSocket1;
			currentChargingSessionSocket1 = null;
			currentChargingSessionIndexSocket1Stored = currentChargingSessionIndexSocket1;
			currentChargingSessionIndexSocket1 = 0;
			
			currentChargingSessionSocket2Stored = currentChargingSessionSocket2;
			currentChargingSessionSocket2 = null;
			currentChargingSessionIndexSocket2Stored = currentChargingSessionIndexSocket2;
			currentChargingSessionIndexSocket2 = 0;
			
			totalShiftedLoadV1GStored_kWh = totalShiftedLoadV1G_kWh;
			totalShiftedLoadV1G_kWh = 0.0;
			totalShiftedLoadV2GStored_kWh = totalShiftedLoadV2G_kWh;
			totalShiftedLoadV2G_kWh = 0.0;
	    	clear();
		}
		
		@Override
		public void restoreStates() {
	    	energyUsed_kWh = energyUsedStored_kWh;
			discharged_kWh = dischargedStored_kWh;
			charged_kWh = chargedStored_kWh;
			
			currentChargingSessionSocket1 = currentChargingSessionSocket1Stored;
			currentChargingSessionIndexSocket1 = currentChargingSessionIndexSocket1Stored;
			
			currentChargingSessionSocket2 = currentChargingSessionSocket2Stored;
			currentChargingSessionIndexSocket2 = currentChargingSessionIndexSocket2Stored;
			
			totalShiftedLoadV1G_kWh = totalShiftedLoadV1GStored_kWh;
			totalShiftedLoadV2G_kWh = totalShiftedLoadV2GStored_kWh;
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
