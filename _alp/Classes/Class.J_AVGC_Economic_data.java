/**
 * J_AVGC_Economic_data
 */	
public class J_AVGC_Economic_data {

	//Energy costs
	public Map<OL_EnergyCarriers, Double> map_avgCostOfEnergyCarrier_eur_p_kWh;
	
	//CAPEX & OPEX
	public Map<OL_EnergyAssetType, Double> map_avgAssetCAPEX_eur_p_kW;
	public Map<OL_EnergyAssetType, Double> map_avgAssetLifeTime_yr;
	public Map<OL_EnergyAssetType, Double> map_avgAssetOPEX_eur_p_kW;
	
	public I_GridOperatorTariffs gridOperatorTariffs;
	
    /**
     * Default constructor
     */
    public J_AVGC_Economic_data() {
    }

}