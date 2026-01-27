public interface I_HeatingAsset
{
	public J_FlowsMap get_heatFromEnergyCarrier_kW(); // Map with a single entry for the energycarrier that is 'burned'
	public J_FlowsMap get_consumptionForHeating_kW(); // Map with a single entry for the energycarrier that is 'burned'
}