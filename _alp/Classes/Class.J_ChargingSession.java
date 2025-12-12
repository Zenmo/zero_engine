/**
 * J_ChargingSession
 */	
public class J_ChargingSession {

	private double startTime_h;
	private double endTime_h;
	private double timeStep_h;
	private double chargingDemand_kWh;
	private double avgPowerDemand_kW;
	private double batterySize_kWh;
	private double stateOfCharge_kWh;
	private double vehicleMaxChargingPower_kW;
	private int socketNb;
	
	private boolean V2GCapable = true;
	private double chargedDuringSession_kWh = 0;
	private double dischargedDuringSession_kWh = 0;
	
	private boolean V2GCapabilityIsOverriden = false;
	private boolean V2GCapableOverride = true;
	
    /**
     * Default constructor
     */
    public J_ChargingSession(double startTime_quarterhours, double endTime_quarterhours, double chargingDemand_kWh, double batterySize_kWh, double chargingPower_kW, int socketNb, boolean V2GCapable, double timeStep_h) {
    
    	this.startTime_h = 0.25 * startTime_quarterhours;
    	this.endTime_h = 0.25 * endTime_quarterhours;
    	this.timeStep_h = timeStep_h;
    	this.chargingDemand_kWh = chargingDemand_kWh;
    	this.avgPowerDemand_kW = this.chargingDemand_kWh / (this.endTime_h - this.startTime_h);
    	this.batterySize_kWh = batterySize_kWh;
    	this.socketNb = socketNb-1;
    	this.V2GCapable = V2GCapable;
    	//stateOfCharge_kWh = batterySize_kWh - chargingDemand_kWh; // bold assumption... basically means every vehicle ends full. The reality is somewhere between: vehicle starts empty and vehicle ends full. 
    	stateOfCharge_kWh = 0.5*(batterySize_kWh - chargingDemand_kWh); // Assumption: battery is not completely empty at start, and not completely full when leaving
    	this.vehicleMaxChargingPower_kW = chargingPower_kW; 
    	
    	if(this.startTime_h > this.endTime_h){
    		new RuntimeException("StartTime is later then the endtime for J_ChargingSession");
    	}
    }

	
    public double charge_kW(double chargeSetpoint_kW) {    	
    	double actualChargePower_kW = max(min(chargeSetpoint_kW, (batterySize_kWh - stateOfCharge_kWh) / timeStep_h), -stateOfCharge_kWh  / timeStep_h); // Limit charge power to stay within SoC 0-100    
    	stateOfCharge_kWh += actualChargePower_kW * timeStep_h;
    	chargedDuringSession_kWh+=max(0, actualChargePower_kW*this.timeStep_h);
    	dischargedDuringSession_kWh+=max(0, -actualChargePower_kW*this.timeStep_h);     	
    	return actualChargePower_kW;
    }
    
    public double getStartTime_h() {
    	return this.startTime_h;
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
		double chargeNeedForNextTrip_kWh = max(0, this.getEnergyNeededForNextTrip_kWh() - this.getCurrentSOC_kWh());
		double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
		double chargeDeadline_h = this.endTime_h - chargeNeedForNextTrip_kWh / this.vehicleMaxChargingPower_kW - chargeTimeMargin_h;
		return chargeDeadline_h;
	}

	
    public double getEnergyNeededForNextTrip_kWh() {
    	return this.chargingDemand_kWh;
    }
    
    public double getRemainingChargeDemand_kWh() {
    	return chargingDemand_kWh - chargedDuringSession_kWh + dischargedDuringSession_kWh;
    }
    
	public double getRemainingAverageChargingDemand_kW(double t_h) {
		return getLeaveTime_h() > t_h ? getRemainingChargeDemand_kWh() / (getLeaveTime_h() - t_h) : 0;
	}
    
    public J_ChargingSession getClone() {
    	return new J_ChargingSession((this.startTime_h*4), (this.endTime_h*4), this.chargingDemand_kWh, this.batterySize_kWh, this.vehicleMaxChargingPower_kW, this.socketNb, this.V2GCapable, this.timeStep_h);
    }
    
	public double getVehicleScaling_fr() {
		return 1.0;
	}
	
	public boolean getV2GCapable() {
		if(this.V2GCapabilityIsOverriden) {
			return this.V2GCapableOverride;
		}
		else {
			return this.V2GCapable;
		}
	}
    
	public int getSocketNb() {
	    return this.socketNb;
	}
	
	public boolean getAvailability(double t_h) {
		return this.startTime_h < t_h && t_h < this.endTime_h;
	}

	
	public void overrideV2GCapability(boolean V2GCapabilityIsOverriden, boolean V2GCapableOverride) {
		this.V2GCapabilityIsOverriden = V2GCapabilityIsOverriden;
		if(this.V2GCapabilityIsOverriden) {
			this.V2GCapableOverride = V2GCapableOverride;
		}
	}
   
	@Override
	public String toString() {
		return "StartTime_h: " + startTime_h + ", endTime_h: " + endTime_h + ", Pmax: " + vehicleMaxChargingPower_kW + "kW, demand: " + chargingDemand_kWh + "kWh";
	}
}