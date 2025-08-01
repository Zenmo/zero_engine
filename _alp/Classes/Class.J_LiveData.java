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
	public DataSet data_baseloadElectricityDemand_kW = new DataSet(672);
	public DataSet data_hydrogenElectricityDemand_kW = new DataSet(672);
	public DataSet data_heatPumpElectricityDemand_kW = new DataSet(672);
	public DataSet data_electricVehicleDemand_kW = new DataSet(672);
	public DataSet data_batteryCharging_kW = new DataSet(672);
	public DataSet data_PVGeneration_kW = new DataSet(672);
	public DataSet data_windGeneration_kW = new DataSet(672);
	public DataSet data_PTGeneration_kW = new DataSet(672);
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
	    	data_PTGeneration_kW.add( t, 0);
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
    
    public void addTimeStep(double currentTime_h, J_FlowsMap fm_currentBalanceFlows_kW, J_FlowsMap fm_currentConsumptionFlows_kW, J_FlowsMap fm_currentProductionFlows_kW, J_AssetFlowsMap assetFlowsMap, double v_currentPrimaryEnergyProduction_kW, double v_currentFinalEnergyConsumption_kW, double v_currentPrimaryEnergyProductionHeatpumps_kW, double v_currentEnergyCurtailed_kW) {

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

    	//Baseload electricity
    	this.data_baseloadElectricityDemand_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.fixedConsumptionElectric_kW),3));

    	//Cooking
    	this.data_cookingElectricityDemand_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.electricHobConsumption_kW), 3));

    	//Hydrogen elec consumption
    	this.data_hydrogenElectricityDemand_kW.add(currentTime_h, roundToDecimal(max(0, assetFlowsMap.get(OL_AssetFlowCategories.electrolyserElectricityConsumption_kW)), 3));

    	//Heatpump elec consumption
    	this.data_heatPumpElectricityDemand_kW.add(currentTime_h, roundToDecimal(max(0, assetFlowsMap.get(OL_AssetFlowCategories.heatPumpElectricityConsumption_kW)), 3));

    	//EVs
    	this.data_electricVehicleDemand_kW.add(currentTime_h, roundToDecimal(max(0,assetFlowsMap.get(OL_AssetFlowCategories.evChargingPower_kW)), 3));
    	this.data_V2GSupply_kW.add(currentTime_h, roundToDecimal(max(0, -assetFlowsMap.get(OL_AssetFlowCategories.evChargingPower_kW)), 3));

    	//Batteries
    	this.data_batteryCharging_kW.add(currentTime_h, roundToDecimal(max(0,assetFlowsMap.get(OL_AssetFlowCategories.batteriesChargingPower_kW)), 3));		
    	this.data_batteryDischarging_kW.add(currentTime_h, roundToDecimal(max(0,-assetFlowsMap.get(OL_AssetFlowCategories.batteriesChargingPower_kW)), 3));	
    	/*this.data_batteryStoredEnergyLiveWeek_MWh.add(currentTime_h, assetFlows.currentStoredEnergyBatteries_MWh);
    	if(assetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
    		this.data_batterySOC_fr.add(currentTime_h, assetFlows.currentStoredEnergyBatteries_MWh/assetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
    	}
    	else{
    		this.data_batterySOC_fr.add(currentTime_h, 0);	
    	}*/	
    	
    	//CHP production
    	this.data_CHPElectricityProductionLiveWeek_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.CHPProductionElectric_kW), 3));

    	//PV production
    	if (assetFlowsMap.get(OL_AssetFlowCategories.pvProductionElectric_kW) < 0) {
    		throw new RuntimeException("Negative assetFlows.pvProductionElectric_kW! Curtailment error? Value: " + assetFlowsMap.get(OL_AssetFlowCategories.pvProductionElectric_kW) );
    	}
    	this.data_PVGeneration_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.pvProductionElectric_kW) , 3));

    	//Wind production
    	this.data_windGeneration_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.windProductionElectric_kW), 3));	

    	//PT production
    	this.data_PTGeneration_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.ptProductionHeat_kW), 3));

    	//District heating
    	this.data_districtHeatDelivery_kW.add(currentTime_h, roundToDecimal(assetFlowsMap.get(OL_AssetFlowCategories.districtHeatDelivery_kW), 3));	

    }
    
	public String toString() {
		return super.toString();
	}

}