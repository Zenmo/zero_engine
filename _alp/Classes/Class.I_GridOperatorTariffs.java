public interface I_GridOperatorTariffs
{
	public double getPhysicalCapacityCost_eurpyr(J_ConnectionMetaData connectionMetaData);
    public double getContractCapacityCost_eurpyr(J_ConnectionMetaData connectionMetaData);
    public double getTransportCost_eur(J_ConnectionMetaData connectionMetaData, double transportedElectricity_kWh);
    public double getMonthlyPeakCost_eur(J_ConnectionMetaData connectionMetaData, double monthlyPeakLoad_kW);
}