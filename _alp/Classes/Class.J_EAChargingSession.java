/**
 * J_EAChargingSession
 */	
public class J_EAChargingSession extends zero_engine.J_EA implements I_ChargingRequest {
	double startTime_h;
	double endTime_h;
	double timeStep_h;
	double chargingDemand_kWh;
	double avgPowerDemand_kW;
	double batterySize_kWh;
	double stateOfCharge_kWh;
	double vehicleMaxChargingPower_kW;
	int socketNb;

	boolean V2GCapable = true;
	private boolean V2GActive = false;
	double chargedDuringSession_kWh = 0;
	double dischargedDuringSession_kWh = 0;
	
    /**
     * Default constructor
     */
	public J_EAChargingSession(double startTime_quarterhours, double endTime_quarterhours, double chargingDemand_kWh, double batterySize_kWh, double chargingPower_kW, int socketNb, double timeStep_h) {
		    
	    	this.startTime_h = 0.25 * startTime_quarterhours;
	    	this.endTime_h = 0.25 * endTime_quarterhours;
	    	this.timeStep_h = timeStep_h;
	    	this.chargingDemand_kWh = chargingDemand_kWh;
	    	this.avgPowerDemand_kW = this.chargingDemand_kWh / (this.endTime_h - this.startTime_h);
	    	this.batterySize_kWh = batterySize_kWh;
	    	this.socketNb = socketNb-1; 
	    	stateOfCharge_kWh = 0;//0.5*(batterySize_kWh - chargingDemand_kWh); // Assumption: has to be completely empty at the start, to prevent discontinuous energy flow in the model
	    	this.vehicleMaxChargingPower_kW = chargingPower_kW; 
	    	
	    	if(this.startTime_h > this.endTime_h){
	    		new RuntimeException("StartTime is later then the endtime for J_ChargingSession");
	    	}
		    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);   	
			this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
			if(V2GCapable && this.V2GActive) {
				this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
			} else {
				this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
			}
	}
	
    public double charge_kW(double chargeSetpoint_kW) {    	
    	double actualChargePower_kW = max(min(chargeSetpoint_kW, (batterySize_kWh - stateOfCharge_kWh) / timeStep_h), -stateOfCharge_kWh  / timeStep_h); // Limit charge power to stay within SoC 0-100    
    	stateOfCharge_kWh += actualChargePower_kW * timeStep_h;
    	chargedDuringSession_kWh+=max(0, actualChargePower_kW*this.timeStep_h);
    	dischargedDuringSession_kWh+=max(0, -actualChargePower_kW*this.timeStep_h);     	
    	return actualChargePower_kW;
    }
    
    
    

    
	public double getLeaveTime_h() {
		return this.endTime_h;
	}

	public double getChargingCapacity_kW() {
		return this.vehicleMaxChargingPower_kW;
	}
	
	public double getCurrentSOC_kWh() {
		return this.stateOfCharge_kWh;
	}
	
	public double getStorageCapacity_kWh() {
		return this.batterySize_kWh;
	}

	public double getChargeDeadline_h() {
		double chargeNeedForNextTrip_kWh = max(0, this.getEnergyNeedForNextTrip_kWh() - this.getCurrentSOC_kWh());
		double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
		double chargeDeadline_h = this.endTime_h - chargeNeedForNextTrip_kWh / this.vehicleMaxChargingPower_kW - chargeTimeMargin_h;
		return chargeDeadline_h;
	}

	
    public double getEnergyNeedForNextTrip_kWh() {
    	return this.chargingDemand_kWh;
    }
    
    public double getRemainingChargeDemand_kWh() {
    	return chargingDemand_kWh - chargedDuringSession_kWh + dischargedDuringSession_kWh;
    }
    
    public J_EAChargingSession getClone() {
    	return new J_EAChargingSession((this.startTime_h*4), (this.endTime_h*4), this.chargingDemand_kWh, this.batterySize_kWh, this.vehicleMaxChargingPower_kW, this.socketNb, this.timeStep_h);
    }
    
	public double getVehicleScaling_fr() {
		return 1.0;
	}
	

	
	//V2G capabilities
	public void setV2GCapable(boolean isV2GCapable) {
		this.V2GCapable = isV2GCapable;
		setV2GActive(getV2GActive());
	}
	
	protected void setV2GActive(boolean activateV2G) { // Should only be called by the chargingManagement class or J_EAEV during initialization itself. (No such thing as friend class in java, so only can put on protected).
		this.V2GActive = activateV2G;
		if(this.V2GCapable && activateV2G) {
			this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
		}
		else {
			this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
		}
	}
	
	public boolean getV2GCapable() {
		return this.V2GCapable;
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	
	
	
    public void operate(double ratioOfCapacity) {
    	throw new RuntimeException( "J_EAChargingSession operate! This is a dummy function, doing nothing!");
    }	
	
    
    
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	clear();    	
    }
    
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;
    }
    
	@Override
	public String toString() {
		return super.toString();
	}

}