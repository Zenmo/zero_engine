/**
 * J_AVGC_Economic_data
 */	
public class J_AVGC_Economic_data {

	//Energy costs
	public Map<OL_EnergyCarriers, Double> map_avgCostOfEnergyCarrier_eurpkWh;
	public Map<OL_EnergyCarriers, Double> map_energyTaxesECImport_eurpkWh;
	public double VAT_energy_fr;
	
	//CAPEX & OPEX
	public Map<OL_EnergyAssetType, Double> map_avgAssetCAPEX_eurpkW;
	public Map<OL_EnergyAssetType, Double> map_avgAssetLifeTime_yr;
	public Map<OL_EnergyAssetType, Double> map_avgAssetOPEX_eurpkWpyr;
	public double VAT_CAPEXAndOPEX_fr;
	
	//Connection costs
	public I_GridOperatorTariffs gridOperatorTariffs;
	
    /**
     * Default constructor
     */
    public J_AVGC_Economic_data() {
    }

}