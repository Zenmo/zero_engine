/**
 * I_ChargingRequest
 */	
public interface I_ChargingRequest {
	
	// The maximal capacity for the vehicle
	public double getChargingCapacity_kW();
	
	public double getCurrentSOC_kWh();
	
	public double getStorageCapacity_kWh();
	
	// Last moment before full power charging is required
	public double getChargeDeadline_h();
	
	public double getLeaveTime_h();
	
	public boolean getV2GCapable();
	
	// The total energy needed before the leave time
	public double getEnergyNeedForNextTrip_kWh();
	
	// The remaining energy needed before the leave time
	public double getRemainingChargeDemand_kWh();
	
	public double getVehicleScaling_fr();
	
	// Sends the power into the EV/ChargingSession to update the SOC, ChargeDeadline etc...
    public void f_updateAllFlows(double powerFraction_fr);
	//public double charge_kW( double charge_kW );

}