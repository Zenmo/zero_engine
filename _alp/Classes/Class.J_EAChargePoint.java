/**
 * J_EAChargePoint
 */	
public class J_EAChargePoint extends zero_engine.J_EA implements Serializable {
		public double discharged_kWh;
		public double charged_kWh;
		public double capacityElectric_kW;
		public List<J_ChargingSession> chargeSessionList;
		public boolean V1GCapable;
		public boolean V2GCapable;
		private boolean V2GActive = false;
		private int nbSockets;
		private int nextSessionIdx = 0;
		
		private J_ChargingSession[] currentChargingSessions;
		
		// For filtered price
		private double electricityPriceLowPassed_eurpkWh = 0.1;
	    private double priceFilterTimeScale_h = 5*24;
		
		//
		private double dischargedStored_kWh;
		private double chargedStored_kWh;
		private J_ChargingSession[] currentChargingSessionsStored;
		private int nextSessionIdxStored;
		
		public boolean gridAwareMode = true;
		
	    /**
	     * Default constructor
	     */
	    public J_EAChargePoint(Agent parentAgent, double electricCapacity_kW, double timestep_h, List<J_ChargingSession> chargeSessionList, boolean V1GCapable, boolean V2GCapable, int nbSockets) {
	    	this.parentAgent = parentAgent;
	    	this.capacityElectric_kW = electricCapacity_kW;
	    	this.timestep_h = timestep_h;
	    	this.chargeSessionList = chargeSessionList;
		    this.V1GCapable = V1GCapable;
		    this.V2GCapable = V2GCapable;
	    	this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);		
			this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
			if(V2GCapable && this.V2GActive) {
				this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
			} else {
				this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
			}
			this.nbSockets = nbSockets;
			this.currentChargingSessions = new J_ChargingSession[nbSockets];
			this.registerEnergyAsset();
	    }
	    
	    public void f_updateAllFlows( double t_h, boolean smartCharging, boolean V2G) {

	    	double currentElectricityPriceConsumption_eurpkWh = ((GridConnection)parentAgent).energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() * 0.001;	    	
	    	this.electricityPriceLowPassed_eurpkWh += (currentElectricityPriceConsumption_eurpkWh-electricityPriceLowPassed_eurpkWh) / (priceFilterTimeScale_h/timestep_h);
	    	
	    	/*if (gridAwareMode) { // Add gridload-factor to current price
	    		currentElectricityPriceConsumption_eurpkWh = currentElectricityPriceConsumption_eurpkWh + (max(0,((GridConnection)parentAgent).p_parentNodeElectric.v_currentLoad_kW/((GridConnection)parentAgent).p_parentNodeElectric.p_capacity_kW-0.5))*0.1;
	    		if ( ((GridConnection)parentAgent).p_parentNodeElectric.v_currentLoad_kW/((GridConnection)parentAgent).p_parentNodeElectric.p_capacity_kW > 0.5) {
	    			//traceln("Adding %s eurpkWh to charging price to prevent congestion", (((GridConnection)parentAgent).p_parentNodeElectric.v_currentLoad_kW/((GridConnection)parentAgent).p_parentNodeElectric.p_capacity_kW-0.5)*1);
	    		}
	    	}*/
	    
	    	// Check if the charger is capable of smart charging
	    	boolean doV1G = smartCharging && this.V1GCapable;
	    	boolean doV2G = V2G && this.V2GCapable;
	    	
	    	// Update the J_ChargingSessions of the sockets
			for (int i = 0; i<this.nbSockets; i++) {				
				this.manageSocket(i, t_h); // Should also 'remove' finished sessions.
			}
	    	
			// Calculate the power output of the sockets
			double power_kW = 0.0;
			
			for (int i = 0; i<this.nbSockets; i++) {				
				if (currentChargingSessions[i] != null && t_h >= currentChargingSessions[i].startTime_h) {
					power_kW += this.operateChargerSocket(i, t_h, currentElectricityPriceConsumption_eurpkWh, doV1G, doV2G);
					
					discharged_kWh += min(0,-power_kW) * timestep_h;
					charged_kWh += max(0,power_kW) * timestep_h;
				}
			}
			
			// Call the regular J_EA updateAllFlows and operate
			this.v_powerFraction_fr = power_kW / this.capacityElectric_kW;
			super.f_updateAllFlows( );
	    	return;
	    }
	    
		@Override
	    public void operate(double ratioOfCapacity) {			
			
			double charge_kW = ratioOfCapacity * capacityElectric_kW;
	    	
			double electricityProduction_kW = max(-charge_kW, 0);
			double electricityConsumption_kW = max(charge_kW, 0);
			
	    	energyUse_kW = electricityConsumption_kW - electricityProduction_kW;
	    	energyUsed_kWh += energyUse_kW * timestep_h;
	    	
			flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW - electricityProduction_kW);	
			// Split charging and discharing power 'at the source'!
			
			if (charge_kW > 0) { // charging
				assetFlowsMap.put(OL_AssetFlowCategories.evChargingPower_kW, electricityConsumption_kW);
			} else if(charge_kW < 0){
				if(this.V2GCapable && this.V2GActive) {
					assetFlowsMap.put(OL_AssetFlowCategories.V2GPower_kW, electricityProduction_kW);
				}
				else {
					traceln("Charge power in J_EAChargePoint negative: %s", charge_kW);
					throw new RuntimeException("Trying to discharge into a charger, that does not have the capability or where v2g is not activated!");
				}
			}			
	   	}
	    
		private double operateChargerSocket(int socketNo, double t_h, double currentElectricityPriceConsumption_eurpkWh, boolean doV1G, boolean doV2G) {
			double maxChargePower = capacityElectric_kW;//min(currentChargingSessions[socketNo].vehicleMaxChargingPower_kW, capacityElectric_kW);
			double remainingChargeDemand_kWh = currentChargingSessions[socketNo].getRemainingChargeDemand_kWh(); // Can be negative if recharging is not needed for next trip!
			double chargePower_kW = 0;
			if (!doV1G) {
				chargePower_kW = min(maxChargePower, remainingChargeDemand_kWh/timestep_h); // just max power charging to start with
				//traceln("ChargePoint simple charging active");
			} else {
				//traceln("Smart charging active at chargePoint");
				double nextTripStartTime_h = currentChargingSessions[socketNo].endTime_h;
				double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
				double chargeDeadline_h =  nextTripStartTime_h - remainingChargeDemand_kWh / maxChargePower - chargeTimeMargin_h;
				double remainingFlexTime_h = chargeDeadline_h - t_h; // measure of flexiblity left in current charging session.

				if ( t_h >= chargeDeadline_h && remainingChargeDemand_kWh > 0) { // Must-charge time at max charging power
					//traceln("Urgency charging on charge point GC: %s! May exceed connection capacity!", this.parentAgent);
					chargePower_kW = min(maxChargePower, remainingChargeDemand_kWh/timestep_h);
				} else {
					double WTPoffset_eurpkW = 0.01; // Drops willingness to pay price for charging, combined with remainingFlexTime_h.
					double WTPCharging_eurpkWh = electricityPriceLowPassed_eurpkWh - WTPoffset_eurpkW * remainingFlexTime_h;  //+ urgencyGain_eurpkWh * ( max(0,maxSpreadChargingPower_kW) / ev.getCapacityElectric_kW() ); // Scale WTP based on flexibility expressed in terms of power-fraction
					//WTPprice_eurpkWh = WTPoffset_eurpkWh + (main.v_epexNext24hours_eurpkWh+v_electricityPriceLowPassed_eurpkWh)/2 + flexibilityGain_eurpkWh * sqrt(maxSpreadChargingPower_kW/maxChargingPower_kW); 
					double priceGain_r = 0.5; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
					chargePower_kW = max(0, maxChargePower * min(1,(WTPCharging_eurpkWh / currentElectricityPriceConsumption_eurpkWh - 1) * priceGain_r));
					if ( doV2G && remainingFlexTime_h > 1 && chargePower_kW == 0 ) { // Surpluss SOC and high energy price
						//traceln("Conditions for V2G met in chargePoint");
		    			double V2G_WTS_offset_eurpkWh = 0.02; // Price must be at least this amount above the moving average to decide to discharge EV battery.
						double WTSV2G_eurpkWh = V2G_WTS_offset_eurpkWh + electricityPriceLowPassed_eurpkWh; // Scale WillingnessToSell based on flexibility expressed in terms of power-fraction
						chargePower_kW = min(0, -maxChargePower * min(1,(currentElectricityPriceConsumption_eurpkWh / WTSV2G_eurpkWh - 1) * priceGain_r));
						//if (chargeSetpoint_kW < 0) {traceln(" V2G Active! Power: " + chargeSetpoint_kW );}
					} 
				}

			}
			currentChargingSessions[socketNo].charge(chargePower_kW);
			return chargePower_kW;
		}
				
		private void manageSocket(int socketNo, double t_h) {
			if ( this.currentChargingSessions[socketNo] == null ) { // socket currently free
				 // check if we are not already past the last charging session.
				if (this.nextSessionIdx >= this.chargeSessionList.size()) { // no more sessions available
					//traceln("Reached end of charging session list!");
					return;					
				}
				// Get next charging session
				this.currentChargingSessions[socketNo] = this.chargeSessionList.get(this.nextSessionIdx);
				if (t_h > this.currentChargingSessions[socketNo].startTime_h) { 
					traceln("Chargesession %s started too late!", this.nextSessionIdx);	
					if (t_h >= this.currentChargingSessions[socketNo].endTime_h) { 
						traceln("!!Chargesession started after its endTime_h!! WTF?");
					}
				}
				this.nextSessionIdx++;

			} else { // socket in use, update status
				if (t_h >= this.currentChargingSessions[socketNo].endTime_h) { // end session
					if (this.currentChargingSessions[socketNo].getRemainingChargeDemand_kWh() > 0.001 ) { traceln("!!Chargesession ended but charge demand not fullfilled!! Remaining demand: %s kWh", this.currentChargingSessions[socketNo].getRemainingChargeDemand_kWh()); }
					this.currentChargingSessions[socketNo] = null;
				}
			}
		}
		
		@Override
		public void storeStatesAndReset() {
	    	energyUsedStored_kWh = energyUsed_kWh;
	    	energyUsed_kWh = 0.0;
			dischargedStored_kWh = discharged_kWh;
			discharged_kWh = 0.0;
			chargedStored_kWh = charged_kWh;
			charged_kWh = 0.0;
			
			currentChargingSessionsStored = currentChargingSessions.clone();
			Arrays.fill(currentChargingSessions, null);
			
			nextSessionIdxStored = nextSessionIdx;
			nextSessionIdx = 0;

	    	clear();
		}
		
		@Override
		public void restoreStates() {
	    	energyUsed_kWh = energyUsedStored_kWh;
			discharged_kWh = dischargedStored_kWh;
			charged_kWh = chargedStored_kWh;
			
			currentChargingSessions = currentChargingSessionsStored;
			nextSessionIdx = nextSessionIdxStored;
		}
		
		public  void setV2GActive(boolean activateV2G) {
			this.V2GActive = activateV2G;
			if(this.V2GCapable && activateV2G) {
				this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
			}
			else {
				this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
			}
		}
		
		@Override
		public String toString() {
			return "Power: " + getLastFlows().get(OL_EnergyCarriers.ELECTRICITY) + " kW, capacity: " + capacityElectric_kW + " kW" +
			"/n Smart charging capacble: " + this.V1GCapable +
			",/n V2G capable: " + this.V2GCapable +
			"/n V2G active: " + this.V2GActive;
			
		}

		/**
		 * This number is here for model snapshot storing purpose<br>
		 * It needs to be changed when this class gets changed
		 */ 
		private static final long serialVersionUID = 1L;

}
