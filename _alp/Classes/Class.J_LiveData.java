/**
 * J_LiveData
 */	
public class J_LiveData {
	
	
	private J_DataSetMap dsm_liveDemand_kW = new J_DataSetMap(); 
	private J_DataSetMap dsm_liveSupply_kW = new J_DataSetMap(); 
	private DataSet data_baseloadElectricityDemand_kW = new DataSet(672);
	private DataSet data_hydrogenElectricityDemand_kW = new DataSet(672);
	private DataSet data_heatPumpElectricityDemand_kW = new DataSet(672);
	private DataSet data_electricVehicleDemand_kW = new DataSet(672);
	private DataSet data_batteryCharging_kW = new DataSet(672);
	private DataSet data_PVGeneration_kW = new DataSet(672);
	private DataSet data_windGeneration_kW = new DataSet(672);
	private DataSet data_batteryDischarging_kW = new DataSet(672);
	private DataSet data_V2GSupply_kW = new DataSet(672);
	private DataSet data_CHPElectricityProductionLiveWeek_kW = new DataSet(672);
	private DataSet data_totalDemand_kW = new DataSet(672); 
	private DataSet data_totalSupply_kW = new DataSet(672);
	private DataSet data_liveElectricityBalance_kW = new DataSet(672);
	private DataSet data_gridCapacityDemand_kW = new DataSet(672); 
	private DataSet data_gridCapacitySupply_kW = new DataSet(672);
	private DataSet data_cookingElectricityDemand_kW = new DataSet(672);
	private DataSet data_districtHeatDelivery_kW = new DataSet(672);
	private DataSet data_batteryStoredEnergyLiveWeek_MWh = new DataSet(672);
	private DataSet data_batterySOC_fr = new DataSet(672);
    /**
     * Default constructor
     */
    public J_LiveData() {
    }
	
	protected void update() {
		
	}
	@Override
	public String toString() {
		return super.toString();
	}

}