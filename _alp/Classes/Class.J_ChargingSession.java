/**
 * J_ChargingSession
 */	
public class J_ChargingSession implements Serializable {

	double startTime_h;
	double endTime_h;
	double timeStep_h;
	double chargingDemand_kWh;
	double avgPowerDemand_kW;
	double batterySize_kWh;
	double stateOfCharge_kWh;
	double vehicleMaxChargingPower_kW;
	int socketNb;
	
	boolean V1GCapable = true;
	boolean V2GCapable = true;
	double chargedDuringSession_kWh = 0;
	double dischargedDuringSession_kWh = 0;
	
	
    /**
     * Default constructor
     */
    public J_ChargingSession(double startTime_quarterhours, double endTime_quarterhours, double chargingDemand_kWh, double batterySize_kWh, double chargingPower_kW, int socketNb, double timeStep_h) {
    
    	this.startTime_h = 0.25 * startTime_quarterhours;
    	this.endTime_h = 0.25 * endTime_quarterhours;
    	this.timeStep_h = timeStep_h;
    	this.chargingDemand_kWh = chargingDemand_kWh;
    	this.avgPowerDemand_kW = this.chargingDemand_kWh / (this.endTime_h - this.startTime_h);
    	this.batterySize_kWh = batterySize_kWh;
    	this.socketNb = socketNb-1;
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
    
	public double getLeaveTime_h() {
		return this.endTime_h;
	}

	public double getChargingCapacity_kW() {
		return this.vehicleMaxChargingPower_kW;
	}
	
	public double getCurrentSOC_kWh() {
		return this.stateOfCharge_kWh;
	}
	
	public double getMaxSOC_kWh() {
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
    
    public J_ChargingSession getClone() {
    	return new J_ChargingSession((this.startTime_h*4), (this.endTime_h*4), this.chargingDemand_kWh, this.batterySize_kWh, this.vehicleMaxChargingPower_kW, this.socketNb, this.timeStep_h);
    }
    
	public double getVehicleScaling_fr() {
		return 1.0;
	}
	
	public boolean getV2GCapable() {
		return this.V2GCapable;
	}
    
   
	@Override
	public String toString() {
		return "StartTime_h: " + startTime_h + ", endTime_h: " + endTime_h + ", Pmax: " + vehicleMaxChargingPower_kW + "kW, demand: " + chargingDemand_kWh + "kWh";
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}