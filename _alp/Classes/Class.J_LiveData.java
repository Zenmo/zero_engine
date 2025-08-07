import java.util.EnumSet;
/**
 * J_LiveData
 */	
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_LiveData {
	
	public Agent parentAgent;
	public EnumSet<OL_EnergyCarriers> activeEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers;
	
	public J_AssetsMetaData assetsMetaData;
	public J_ConnectionMetaData connectionMetaData;

	public J_DataSetMap dsm_liveDemand_kW = new J_DataSetMap(OL_EnergyCarriers.class); 
	public J_DataSetMap dsm_liveSupply_kW = new J_DataSetMap(OL_EnergyCarriers.class); 
	
	public DataSet data_totalDemand_kW = new DataSet(672); 
	public DataSet data_totalSupply_kW = new DataSet(672);
	public DataSet data_liveElectricityBalance_kW = new DataSet(672);
	public DataSet data_gridCapacityDemand_kW = new DataSet(672); 
	public DataSet data_gridCapacitySupply_kW = new DataSet(672);

	public J_DataSetMap<OL_AssetFlowCategories> dsm_liveAssetFlows_kW = new J_DataSetMap(OL_AssetFlowCategories.class); 
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
    	
    	for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) { // First add missing assetFlow datasets if there are any
			if (!dsm_liveAssetFlows_kW.keySet().contains(AC)) {
				DataSet dsAsset = new DataSet((int)(168 / timeStep_h));
				dsm_liveAssetFlows_kW.put(AC, dsAsset);
			}
    	}
    	
		for (double t = startTime; t <= endTime; t += timeStep_h) {
			
			for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) {
				dsm_liveAssetFlows_kW.get(AC).add(t, 0);
			}

	    	data_totalDemand_kW.add( t, 0); 
	    	data_totalSupply_kW.add( t, 0);
	    	data_liveElectricityBalance_kW.add( t, 0);
	    	data_gridCapacityDemand_kW.add( t, 0); 
	    	data_gridCapacitySupply_kW.add( t, 0);


	    	data_batteryStoredEnergyLiveWeek_MWh.add( t, 0);
	    	data_batterySOC_fr.add( t, 0);	
		}
    }
    
    public void addTimeStep(double currentTime_h, J_FlowsMap fm_currentBalanceFlows_kW, J_FlowsMap fm_currentConsumptionFlows_kW, J_FlowsMap fm_currentProductionFlows_kW, J_ValueMap<OL_AssetFlowCategories> assetFlowsMap, double v_currentPrimaryEnergyProduction_kW, double v_currentFinalEnergyConsumption_kW, double v_currentPrimaryEnergyProductionHeatpumps_kW, double v_currentEnergyCurtailed_kW, double currentStoredEnergyBatteries_MWh) {

    	//Energy carrier flows
    	for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
    		this.dsm_liveDemand_kW.get(EC).add( currentTime_h, roundToDecimal(fm_currentConsumptionFlows_kW.get(EC), 3) );
    	}
    	for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
    		this.dsm_liveSupply_kW.get(EC).add( currentTime_h, roundToDecimal(fm_currentProductionFlows_kW.get(EC), 3) );
    	}

    	//Electricity balance
    	this.data_liveElectricityBalance_kW.add(currentTime_h, fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));

    	//Total demand and supply
    	this.data_totalDemand_kW.add(currentTime_h, v_currentFinalEnergyConsumption_kW);
    	this.data_totalSupply_kW.add(currentTime_h, v_currentPrimaryEnergyProduction_kW);

    	//Live capacity datasets
    	this.data_gridCapacityDemand_kW.add(currentTime_h, connectionMetaData.contractedDeliveryCapacity_kW);
    	this.data_gridCapacitySupply_kW.add(currentTime_h, -connectionMetaData.contractedFeedinCapacity_kW);

    	//// Gather specific electricity flows from corresponding energy assets
		//for (OL_AssetFlowCategories AC : assetFlowsMap.keySet()) {
		for (OL_AssetFlowCategories AC : dsm_liveAssetFlows_kW.keySet()) {	
			//traceln("Assetsflows in dsm_liveAssetflows_kW: %s", dsm_liveAssetFlows_kW.keySet());
			/*if (!dsm_liveAssetFlows_kW.keySet().contains(AC)) {
				traceln("Trying to add assetflow: %s", AC);
				traceln("Parent GC: %s", ((GridConnection)parentAgent).p_gridConnectionID);
			}*/
			dsm_liveAssetFlows_kW.get(AC).add(currentTime_h, roundToDecimal(assetFlowsMap.get(AC),3));
		}
    	
		//Batteries    
    	this.data_batteryStoredEnergyLiveWeek_MWh.add(currentTime_h, currentStoredEnergyBatteries_MWh);
    	if(assetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
    		this.data_batterySOC_fr.add(currentTime_h, currentStoredEnergyBatteries_MWh/assetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
    	}
    	else{
    		this.data_batterySOC_fr.add(currentTime_h, 0);	
    	}	
	

    }
    
	public String toString() {
		return super.toString();
	}

}