public interface I_ChargePointRegistration
{
	public void registerChargingRequest(I_ChargingRequest chargingRequest);
	public void deregisterChargingRequest(I_ChargingRequest chargingRequest);
	public boolean isRegistered(I_ChargingRequest chargingRequest);
}