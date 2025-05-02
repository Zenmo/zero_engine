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
	
    public void resetLiveDatasets(double startTime, double endTime, double timeStep_h) {
    	for(OL_EnergyCarriers EC : activeConsumptionEnergyCarriers){
    		DataSet dsDemand = new DataSet( (int)(168 / timeStep_h) );
    		for (double t = startTime; t <= endTime; t += timeStep_h) {
    			dsDemand.add( t, 0);
    		}
    		dsm_liveDemand_kW.put( EC, dsDemand);
    	}
    	
    	for(OL_EnergyCarriers EC : activeProductionEnergyCarriers){
    		DataSet dsSupply = new DataSet( (int)(168 / timeStep_h) );
    		for (double t = startTime; t <= endTime; t += timeStep_h) {
    			dsSupply.add( t, 0);
    		}
    		dsm_liveSupply_kW.put( EC, dsSupply);
    	}
    	
		for (double t = startTime; t <= endTime; t += timeStep_h) {
	    	data_baseloadElectricityDemand_kW.add( t, 0);
	    	data_hydrogenElectricityDemand_kW.add( t, 0);
	    	data_heatPumpElectricityDemand_kW.add( t, 0);
	    	data_electricVehicleDemand_kW.add( t, 0);
	    	data_batteryCharging_kW.add( t, 0);
	    	data_PVGeneration_kW.add( t, 0);
	    	data_windGeneration_kW.add( t, 0);
	    	data_batteryDischarging_kW.add( t, 0);
	    	data_V2GSupply_kW.add( t, 0);
	    	data_CHPElectricityProductionLiveWeek_kW.add( t, 0);
	    	data_totalDemand_kW.add( t, 0); 
	    	data_totalSupply_kW.add( t, 0);
	    	data_liveElectricityBalance_kW.add( t, 0);
	    	data_gridCapacityDemand_kW.add( t, 0); 
	    	data_gridCapacitySupply_kW.add( t, 0);
	    	data_cookingElectricityDemand_kW.add( t, 0);
	    	data_districtHeatDelivery_kW.add( t, 0);
	    	data_batteryStoredEnergyLiveWeek_MWh.add( t, 0);
	    	data_batterySOC_fr.add( t, 0);	
    	}
    }
    
	public String toString() {
		return super.toString();
	}

}