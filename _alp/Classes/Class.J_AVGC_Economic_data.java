/**
 * J_AVGC_Economic_data
 */	
public class J_AVGC_Economic_data {

	//Energy costs
	Map<OL_EnergyCarriers, Double> map_avgCostOfEnergyCarrier_eur_p_kWh;
	
	//Connection costs
	Map<OL_GridOperator, Double>  map_connectionCost_eur_p_kW;	
	Map<OL_GridOperator, Double>  map_peakConsumptionCost_eur_p_kW;
	Map<OL_GridOperator, Double>  map_transportCost_eur_p_kWhyr;
	
	//CAPEX & OPEX
	Map<OL_EnergyAssetType, Double> map_avgAssetCAPEX_eur_p_kW;
	Map<OL_EnergyAssetType, Double> map_avgAssetLifeTime_yr;
	Map<OL_EnergyAssetType, Double> map_avgAssetOPEX_eur_p_kW;
	

	
	
    /**
     * Default constructor
     */
    public J_AVGC_Economic_data() {
    }

}