import java.util.EnumSet;
/**
 * J_LiveData
 */	

public class J_LiveData {
	
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
	
	public J_LiveData() {
	
	}

	/*
	 * Used at model initialization.
	 */
	public void createNewLiveDataSets(J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
		dsm_liveDemand_kW.createNewLiveDataSets(activeConsumptionEnergyCarriers, timeParameters, timeVariables);
		dsm_liveSupply_kW.createNewLiveDataSets(activeProductionEnergyCarriers, timeParameters, timeVariables);
		dsm_liveAssetFlows_kW.createNewLiveDataSets(assetsMetaData.activeAssetFlows, timeParameters, timeVariables);
		
		data_totalDemand_kW = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);
    	data_totalSupply_kW = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);
    	data_liveElectricityBalance_kW = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);
    	data_gridCapacityDemand_kW = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);
    	data_gridCapacitySupply_kW = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);

    	data_batteryStoredEnergyLiveWeek_MWh = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);
    	data_batterySOC_fr = DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables);
	}
	
	/*
	 * Used by the 'looping' live simulation.
	 */
	public void resetLiveDatasets() {
    	for(OL_EnergyCarriers EC : activeConsumptionEnergyCarriers){
    		if (dsm_liveDemand_kW.get(EC) != null ) {
    			dsm_liveDemand_kW.get(EC).reset();
    		}
    	}
    	
    	for(OL_EnergyCarriers EC : activeProductionEnergyCarriers){
    		if (dsm_liveSupply_kW.get(EC) != null ) {
    			dsm_liveSupply_kW.get(EC).reset();
    		}
    	}
    	
    	for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) { // First add missing assetFlow datasets if there are any
    		if (dsm_liveAssetFlows_kW.get(AC) != null ) {
    			dsm_liveAssetFlows_kW.get(AC).reset();
    		}
    	}

    	data_totalDemand_kW.reset();
    	data_totalSupply_kW.reset();
    	data_liveElectricityBalance_kW.reset();
    	data_gridCapacityDemand_kW.reset();
    	data_gridCapacitySupply_kW.reset();

    	data_batteryStoredEnergyLiveWeek_MWh.reset();
    	data_batterySOC_fr.reset();
    	
    }
    public void addTimeStep(double AnyLogicTime_h, J_FlowsMap fm_currentBalanceFlows_kW, J_FlowsMap fm_currentConsumptionFlows_kW, J_FlowsMap fm_currentProductionFlows_kW, J_ValueMap<OL_AssetFlowCategories> assetFlowsMap, double v_currentPrimaryEnergyProduction_kW, double v_currentFinalEnergyConsumption_kW, double v_currentPrimaryEnergyProductionHeatpumps_kW, double v_currentEnergyCurtailed_kW, double currentStoredEnergyBatteries_MWh) {

    	//Energy carrier flows
    	for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
    		this.dsm_liveDemand_kW.get(EC).add( AnyLogicTime_h, roundToDecimal(fm_currentConsumptionFlows_kW.get(EC), 3) );
    	}
    	for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
    		this.dsm_liveSupply_kW.get(EC).add( AnyLogicTime_h, roundToDecimal(fm_currentProductionFlows_kW.get(EC), 3) );
    	}

    	//Electricity balance
    	this.data_liveElectricityBalance_kW.add(AnyLogicTime_h, fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));

    	//Total demand and supply
    	this.data_totalDemand_kW.add(AnyLogicTime_h, v_currentFinalEnergyConsumption_kW);
    	this.data_totalSupply_kW.add(AnyLogicTime_h, v_currentPrimaryEnergyProduction_kW);

    	//Live capacity datasets
    	this.data_gridCapacityDemand_kW.add(AnyLogicTime_h, connectionMetaData.getContractedDeliveryCapacity_kW());
    	this.data_gridCapacitySupply_kW.add(AnyLogicTime_h, -connectionMetaData.getContractedFeedinCapacity_kW());

    	//// Gather specific electricity flows from corresponding energy assets
		for (OL_AssetFlowCategories AC : dsm_liveAssetFlows_kW.keySet()) {	
			dsm_liveAssetFlows_kW.get(AC).add(AnyLogicTime_h, roundToDecimal(assetFlowsMap.get(AC),3));
		}
    	
		//Batteries    
    	this.data_batteryStoredEnergyLiveWeek_MWh.add(AnyLogicTime_h, currentStoredEnergyBatteries_MWh);
    	if(assetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
    		this.data_batterySOC_fr.add(AnyLogicTime_h, roundToDecimal(currentStoredEnergyBatteries_MWh/assetsMetaData.totalInstalledBatteryStorageCapacity_MWh, 3) );	
    	}
    	else{
    		this.data_batterySOC_fr.add(AnyLogicTime_h, 0);	
    	}	
    }
    
    public void addEnergyCarriersAndAssetFlowCategoriesFromEA(J_EA j_ea, boolean isInitialized, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
	    for (OL_EnergyCarriers EC : j_ea.getActiveProductionEnergyCarriers()) {
	    	this.addProductionEnergyCarrier(EC, isInitialized, timeParameters, timeVariables);
	    }
	    
	    for (OL_EnergyCarriers EC : j_ea.getActiveConsumptionEnergyCarriers()) {
	    	this.addConsumptionEnergyCarrier(EC, isInitialized, timeParameters, timeVariables);
	    }
	    
	    if (j_ea.getAssetFlowCategory() != null) {
	    	this.addAssetFlowCategory(j_ea.getAssetFlowCategory(), isInitialized, timeParameters, timeVariables);
	    }
    }
    
    public void addProductionEnergyCarrier(OL_EnergyCarriers EC, boolean isInitialized, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
    	if (!this.activeProductionEnergyCarriers.contains(EC)) {
    		this.activeEnergyCarriers.add(EC);
    		this.activeProductionEnergyCarriers.add(EC);
    		if (isInitialized) {
    			this.dsm_liveSupply_kW.put( EC, DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables) );
    		}
    	}
    }
    
    public void addConsumptionEnergyCarrier(OL_EnergyCarriers EC, boolean isInitialized, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
    	if (!this.activeConsumptionEnergyCarriers.contains(EC)) {
    		this.activeEnergyCarriers.add(EC);
    		this.activeConsumptionEnergyCarriers.add(EC);
    		if (isInitialized) {
	    		this.dsm_liveDemand_kW.put( EC, DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables) );
    		}
    	}
    }
    
    public void addAssetFlowCategory(OL_AssetFlowCategories AFC, boolean isInitialized, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
    	if (!this.assetsMetaData.activeAssetFlows.contains(AFC)) {
    		this.assetsMetaData.activeAssetFlows.add(AFC);
    		if (isInitialized) {
    			this.dsm_liveAssetFlows_kW.put( AFC, DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables) );
    		}
    		
    		// There are currently two exceptional cases in OL_AssetFlowCategories. 
    		// Batteries have two AFC per J_EA, EVs can have a second AFC.
    		if (AFC == OL_AssetFlowCategories.batteriesChargingPower_kW) {
    			this.assetsMetaData.activeAssetFlows.add(OL_AssetFlowCategories.batteriesDischargingPower_kW);
    			this.dsm_liveAssetFlows_kW.put( OL_AssetFlowCategories.batteriesDischargingPower_kW, DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables) );
    		}
    		if (AFC == OL_AssetFlowCategories.V2GPower_kW && !this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.evChargingPower_kW)) { // also add evCharging!
    			this.assetsMetaData.activeAssetFlows.add(OL_AssetFlowCategories.evChargingPower_kW);	
    			this.dsm_liveAssetFlows_kW.put( OL_AssetFlowCategories.evChargingPower_kW, DataSetConstructor.getNewLiveWeekDataSet(timeParameters, timeVariables) );
    		}
    	}
    }
    
	public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Active Energy Carriers: ");
        sb.append(this.activeEnergyCarriers);
        sb.append(System.lineSeparator());
        sb.append("Consumption Carriers: ");
        sb.append(this.activeConsumptionEnergyCarriers);
        sb.append(System.lineSeparator());
        sb.append("Production Carriers: ");
        sb.append(this.activeProductionEnergyCarriers);
        sb.append(System.lineSeparator());
        sb.append("Asset Flow Caterogies: ");
        sb.append(this.assetsMetaData.activeAssetFlows);
        sb.append(System.lineSeparator());
        sb.append("Number of datapoints: ");
        sb.append(this.data_totalDemand_kW.size());
        
        return sb.toString();
	}
}
