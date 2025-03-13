import java.util.EnumSet;
/**
 * J_LiveData
 */	
public class J_LiveData {
	
	public Agent parentAgent;
	public EnumSet<OL_EnergyCarriers> activeEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers;
	
	public J_AssetsMetaData assetsMetaData;
	public J_ConnectionMetaData connectionMetaData;

	public J_DataSetMap dsm_liveDemand_kW = new J_DataSetMap(); 
	public J_DataSetMap dsm_liveSupply_kW = new J_DataSetMap(); 
	public DataSet data_baseloadElectricityDemand_kW = new DataSet(672);
	public DataSet data_hydrogenElectricityDemand_kW = new DataSet(672);
	public DataSet data_heatPumpElectricityDemand_kW = new DataSet(672);
	public DataSet data_electricVehicleDemand_kW = new DataSet(672);
	public DataSet data_batteryCharging_kW = new DataSet(672);
	public DataSet data_PVGeneration_kW = new DataSet(672);
	public DataSet data_windGeneration_kW = new DataSet(672);
	public DataSet data_batteryDischarging_kW = new DataSet(672);
	public DataSet data_V2GSupply_kW = new DataSet(672);
	public DataSet data_CHPElectricityProductionLiveWeek_kW = new DataSet(672);
	public DataSet data_totalDemand_kW = new DataSet(672); 
	public DataSet data_totalSupply_kW = new DataSet(672);
	public DataSet data_liveElectricityBalance_kW = new DataSet(672);
	public DataSet data_gridCapacityDemand_kW = new DataSet(672); 
	public DataSet data_gridCapacitySupply_kW = new DataSet(672);
	public DataSet data_cookingElectricityDemand_kW = new DataSet(672);
	public DataSet data_districtHeatDelivery_kW = new DataSet(672);
	public DataSet data_batteryStoredEnergyLiveWeek_MWh = new DataSet(672);
	public DataSet data_batterySOC_fr = new DataSet(672);
    /**
     * Default constructor
     */
    public J_LiveData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
	
	public String toString() {
		return super.toString();
	}

}