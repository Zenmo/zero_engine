/**
 * I_ChargingRequest
 */	
public interface I_ChargingRequest {
	
	// The maximal capacity for the vehicle
	public double getVehicleChargingCapacity_kW();
	
	public double getCurrentSOC_kWh();
	
	public double getStorageCapacity_kWh();
	
	public double getLeaveTime_h();
	
	public boolean getV2GCapable();
	
	// The total energy needed in the battery (SOC) before the leave time
	public double getEnergyNeedForNextTrip_kWh();
	
	// The remaining energy needed before the leave time
	public double getRemainingChargeDemand_kWh();
	
	// The remaining average charging needed before the leave time
	public double getRemainingAverageChargingDemand_kW(double t_h);
	
	public double getVehicleScaling_fr();
	
	// Sends the power into the EV/ChargingSession to update the SOC, ChargeDeadline etc...
    public J_FlowPacket f_updateAllFlows(double powerFraction_fr, J_TimeVariables timeVariables);
}