double f_initializeMaps()
{/*ALCODESTART::1741792546529*/
// FlowsMaps
fm_totalImports_MWh = new J_FlowsMap();
fm_totalExports_MWh = new J_FlowsMap();
fm_summerWeekImports_MWh = new J_FlowsMap();
fm_summerWeekExports_MWh = new J_FlowsMap();
fm_winterWeekImports_MWh = new J_FlowsMap();
fm_winterWeekExports_MWh = new J_FlowsMap();
fm_daytimeImports_MWh = new J_FlowsMap();
fm_daytimeExports_MWh = new J_FlowsMap();
fm_nighttimeImports_MWh = new J_FlowsMap();
fm_nighttimeExports_MWh = new J_FlowsMap();
fm_weekdayImports_MWh = new J_FlowsMap();
fm_weekdayExports_MWh = new J_FlowsMap();
fm_weekendImports_MWh = new J_FlowsMap();
fm_weekendExports_MWh = new J_FlowsMap();

// DataSetMaps
dsm_liveConsumption_kW = new J_DataSetMap();
dsm_liveProduction_kW = new J_DataSetMap();
dsm_dailyAverageConsumptionDataSets_kW = new J_DataSetMap();
dsm_dailyAverageProductionDataSets_kW = new J_DataSetMap();
dsm_summerWeekConsumptionDataSets_kW = new J_DataSetMap();
dsm_summerWeekProductionDataSets_kW = new J_DataSetMap();
dsm_winterWeekConsumptionDataSets_kW = new J_DataSetMap();
dsm_winterWeekProductionDataSets_kW = new J_DataSetMap();

/*ALCODEEND*/}

double f_updatePreviousTotalsGC()
{/*ALCODESTART::1741792546531*/
for (GridConnection GC : energyModel.f_getGridConnections()){	
	J_previousTotals previousTotals = c_previousTotals_GC.get(GC);
	
	previousTotals.setPreviousTotalImports_MWh(GC.fm_totalImports_MWh);
	previousTotals.setPreviousTotalExports_MWh(GC.fm_totalExports_MWh);
	
	previousTotals.setPreviousTotalConsumedEnergy_MWh(GC.v_totalEnergyConsumed_MWh);
	previousTotals.setPreviousTotalProducedEnergy_MWh(GC.v_totalEnergyProduced_MWh);
	previousTotals.setPreviousSelfConsumedEnergy_MWh(GC.v_totalEnergySelfConsumed_MWh);
	previousTotals.setPreviousImportedEnergy_MWh(GC.v_totalEnergyImport_MWh);
	previousTotals.setPreviousExportedEnergy_MWh(GC.v_totalEnergyExport_MWh);
	previousTotals.setPreviousSelfConsumedElectricity_MWh(GC.v_totalElectricitySelfConsumed_MWh);
	previousTotals.setPreviousElectricityConsumed_MWh(GC.v_totalElectricityConsumed_MWh);
	
	//Overload
	previousTotals.setPreviousOverloadDurationDelivery_hr(GC.v_totalOverloadDurationDelivery_hr);
	previousTotals.setPreviousOverloadDurationFeedin_hr(GC.v_totalOverloadDurationFeedin_hr);
}

/*ALCODEEND*/}

double f_fillEnergyDataViewer(I_EnergyData data)
{/*ALCODESTART::1741792546533*/
v_engineAgent = data.getRapidRunData().parentAgent;
//Number of connected gcs
//v_numberOfGridconnections = 1;

//Set active energyCarriers
v_activeConsumptionEnergyCarriers = data.getActiveAssetData().activeConsumptionEnergyCarriers;
v_activeProductionEnergyCarriers = data.getActiveAssetData().activeProductionEnergyCarriers;

//Update active asset booleans
f_updateActiveAssetBooleans(data);

//Update variables
f_updateVariables(data);

//Update variables
f_updateLiveDatasets(data);

//Update variables
f_updateWeeklyDatasets(data);

//Update variables
f_updateYearlyDatasets(data);

//Get duurkromme
f_updateLoadDurationCurve(data);
/*ALCODEEND*/}

double f_updateVariables(I_EnergyData data)
{/*ALCODESTART::1741792546535*/
//Grid capacity
v_gridCapacityDelivery_kW = data.getDeliveryCapacity_kW();
v_gridCapacityFeedIn_kW = data.getFeedinCapacity_kW();

//area.v_gridCapacityDelivery_groupcontract_kW = GC.p_contractedDeliveryCapacity_kW;
//area.v_gridCapacityFeedin_groupcontract_kW = GC.p_contractedFeedinCapacity_kW;

b_isRealDeliveryCapacityAvailable = data.getDeliveryCapacityKnown();
b_isRealFeedinCapacityAvailable = data.getFeedinCapacityKnown();

//Installed Asset variables
v_batteryStorageCapacityInstalled_MWh = data.getActiveAssetData().totalInstalledBatteryStorageCapacity_MWh;


// KPIs for individual vs collective plots 
v_individualSelfconsumptionElectricity_fr = data.getRapidRunData().getTotalElectricitySelfConsumed_MWh()/data.getRapidRunData().getTotalElectricityConsumed_MWh();
v_individualSelfSufficiencyElectricity_fr = data.getRapidRunData().getTotalElectricitySelfConsumed_MWh()/data.getRapidRunData().getTotalElectricityProduced_MWh();

v_individualSelfconsumptionEnergy_fr = data.getRapidRunData().getTotalEnergySelfConsumed_MWh()/data.getRapidRunData().getTotalEnergyConsumed_MWh();
v_individualSelfSufficiencyEnergy_fr = data.getRapidRunData().getTotalEnergySelfConsumed_MWh()/data.getRapidRunData().getTotalEnergyProduced_MWh();

v_individualPeakDelivery_kW = data.getRapidRunData().getPeakDelivery_kW();
v_individualPeakFeedin_kW = data.getRapidRunData().getPeakFeedin_kW();

//========== TOTALS ==========//
fm_totalImports_MWh.clear();
fm_totalExports_MWh.clear();

for (OL_EnergyCarriers energyCarrier : v_activeConsumptionEnergyCarriers) {
	fm_totalImports_MWh.put( energyCarrier, data.getRapidRunData().getTotalImport_MWh(energyCarrier) );
}
for (OL_EnergyCarriers energyCarrier : v_activeProductionEnergyCarriers) {
	fm_totalExports_MWh.put( energyCarrier, data.getRapidRunData().getTotalExport_MWh(energyCarrier) );
}

v_totalEnergyImport_MWh = data.getRapidRunData().getTotalEnergyImport_MWh();
v_totalEnergyExport_MWh = data.getRapidRunData().getTotalEnergyExport_MWh();

v_totalEnergyConsumed_MWh = data.getRapidRunData().getTotalEnergyConsumed_MWh();
v_totalEnergyProduced_MWh = data.getRapidRunData().getTotalEnergyProduced_MWh();
v_totalEnergySelfConsumed_MWh = data.getRapidRunData().getTotalEnergySelfConsumed_MWh();

v_totalElectricityConsumed_MWh = data.getRapidRunData().getTotalElectricityConsumed_MWh();
v_totalElectricityProduced_MWh = data.getRapidRunData().getTotalElectricityProduced_MWh();
v_totalElectricitySelfConsumed_MWh = data.getRapidRunData().getTotalElectricitySelfConsumed_MWh();

v_annualOverloadDurationDelivery_hr = data.getRapidRunData().getTotalOverloadDurationDelivery_hr();
v_annualOverloadDurationFeedin_hr = data.getRapidRunData().getTotalOverloadDurationFeedin_hr();

v_totalEnergyConsumptionForDistrictHeating_MWh = data.getRapidRunData().getTotalDistrictHeatingConsumption_MWh();
v_totalPrimaryEnergyProductionHeatpumps_MWh = data.getRapidRunData().getTotalPrimaryEnergyProductionHeatpumps_MWh();

//========== SUMMER/WINTER WEEK ==========//
fm_summerWeekImports_MWh.clear();
fm_winterWeekImports_MWh.clear();
fm_summerWeekExports_MWh.clear();
fm_winterWeekExports_MWh.clear();

for (OL_EnergyCarriers energyCarrier : v_activeConsumptionEnergyCarriers) {
	fm_summerWeekImports_MWh.put( energyCarrier, data.getRapidRunData().getSummerWeekImport_MWh(energyCarrier) );
	fm_winterWeekImports_MWh.put( energyCarrier, data.getRapidRunData().getWinterWeekImport_MWh(energyCarrier) );
}
for (OL_EnergyCarriers energyCarrier : v_activeProductionEnergyCarriers) {
	fm_summerWeekExports_MWh.put( energyCarrier, data.getRapidRunData().getSummerWeekExport_MWh(energyCarrier) );
	fm_winterWeekExports_MWh.put( energyCarrier, data.getRapidRunData().getWinterWeekExport_MWh(energyCarrier) );
}

v_summerWeekEnergyImport_MWh = data.getRapidRunData().getSummerWeekEnergyImport_MWh();
v_summerWeekEnergyExport_MWh = data.getRapidRunData().getSummerWeekEnergyExport_MWh();

v_summerWeekEnergyConsumed_MWh = data.getRapidRunData().getSummerWeekEnergyConsumed_MWh();
v_summerWeekEnergyProduced_MWh = data.getRapidRunData().getSummerWeekEnergyProduced_MWh();
v_summerWeekEnergySelfConsumed_MWh = data.getRapidRunData().getSummerWeekEnergySelfConsumed_MWh();

v_summerWeekElectricityConsumed_MWh = data.getRapidRunData().getSummerWeekElectricityConsumed_MWh();
v_summerWeekElectricityProduced_MWh = data.getRapidRunData().getSummerWeekElectricityProduced_MWh();
v_summerWeekElectricitySelfConsumed_MWh = data.getRapidRunData().getSummerWeekElectricitySelfConsumed_MWh();

v_winterWeekEnergyImport_MWh = data.getRapidRunData().getWinterWeekEnergyImport_MWh();
v_winterWeekEnergyExport_MWh = data.getRapidRunData().getWinterWeekEnergyExport_MWh();

v_winterWeekEnergyConsumed_MWh = data.getRapidRunData().getWinterWeekEnergyConsumed_MWh();
v_winterWeekEnergyProduced_MWh = data.getRapidRunData().getWinterWeekEnergyProduced_MWh();
v_winterWeekEnergySelfConsumed_MWh = data.getRapidRunData().getWinterWeekEnergySelfConsumed_MWh();

v_winterWeekElectricityConsumed_MWh = data.getRapidRunData().getWinterWeekElectricityConsumed_MWh();
v_winterWeekElectricityProduced_MWh = data.getRapidRunData().getWinterWeekElectricityProduced_MWh();
v_winterWeekElectricitySelfConsumed_MWh = data.getRapidRunData().getWinterWeekElectricitySelfConsumed_MWh();

//========== DAY/NIGHT ==========//
fm_daytimeImports_MWh.clear();
fm_nighttimeImports_MWh.clear();
fm_daytimeExports_MWh.clear();
fm_nighttimeExports_MWh.clear();

for (OL_EnergyCarriers energyCarrier : v_activeConsumptionEnergyCarriers) {
	fm_daytimeImports_MWh.put( energyCarrier, data.getRapidRunData().getDaytimeImport_MWh(energyCarrier) );
	fm_nighttimeImports_MWh.put( energyCarrier, data.getRapidRunData().getNighttimeImport_MWh(energyCarrier) );
}
for (OL_EnergyCarriers energyCarrier : v_activeProductionEnergyCarriers) {
	fm_daytimeExports_MWh.put( energyCarrier, data.getRapidRunData().getDaytimeExport_MWh(energyCarrier) );
	fm_nighttimeExports_MWh.put( energyCarrier, data.getRapidRunData().getNighttimeExport_MWh(energyCarrier) );
}

v_daytimeEnergyImport_MWh = data.getRapidRunData().getDaytimeEnergyImport_MWh();
v_daytimeEnergyExport_MWh = data.getRapidRunData().getDaytimeEnergyExport_MWh();

v_daytimeEnergyConsumed_MWh = data.getRapidRunData().getDaytimeEnergyConsumed_MWh();
v_daytimeEnergyProduced_MWh = data.getRapidRunData().getDaytimeEnergyProduced_MWh();
v_daytimeEnergySelfConsumed_MWh = data.getRapidRunData().getDaytimeEnergySelfConsumed_MWh();

v_daytimeElectricityConsumed_MWh = data.getRapidRunData().getDaytimeElectricityConsumed_MWh();
v_daytimeElectricityProduced_MWh = data.getRapidRunData().getDaytimeElectricityProduced_MWh();
v_daytimeElectricitySelfConsumed_MWh = data.getRapidRunData().getDaytimeElectricitySelfConsumed_MWh();

v_nighttimeEnergyImport_MWh = data.getRapidRunData().getNighttimeEnergyImport_MWh();
v_nighttimeEnergyExport_MWh = data.getRapidRunData().getNighttimeEnergyExport_MWh();

v_nighttimeEnergyConsumed_MWh = data.getRapidRunData().getNighttimeEnergyConsumed_MWh();
v_nighttimeEnergyProduced_MWh = data.getRapidRunData().getNighttimeEnergyProduced_MWh();
v_nighttimeEnergySelfConsumed_MWh = data.getRapidRunData().getNighttimeEnergySelfConsumed_MWh();

v_nighttimeElectricityConsumed_MWh = data.getRapidRunData().getNighttimeElectricityConsumed_MWh();
v_nighttimeElectricityProduced_MWh = data.getRapidRunData().getNighttimeElectricityProduced_MWh();
v_nighttimeElectricitySelfConsumed_MWh = data.getRapidRunData().getNighttimeElectricitySelfConsumed_MWh();


//========== WEEK/WEEKEND ==========//
fm_weekdayImports_MWh.clear();
fm_weekendImports_MWh.clear();
fm_weekdayExports_MWh.clear();
fm_weekendExports_MWh.clear();

for (OL_EnergyCarriers energyCarrier : v_activeConsumptionEnergyCarriers) {
	fm_weekdayImports_MWh.put( energyCarrier, data.getRapidRunData().getWeekdayImport_MWh(energyCarrier) );
	fm_weekendImports_MWh.put( energyCarrier, data.getRapidRunData().getWeekendImport_MWh(energyCarrier) );
}
for (OL_EnergyCarriers energyCarrier : v_activeProductionEnergyCarriers) {
	fm_weekdayExports_MWh.put( energyCarrier, data.getRapidRunData().getWeekdayExport_MWh(energyCarrier) );
	fm_weekendExports_MWh.put( energyCarrier, data.getRapidRunData().getWeekendExport_MWh(energyCarrier) );
}

v_weekdayEnergyImport_MWh = data.getRapidRunData().getWeekdayEnergyImport_MWh();
v_weekdayEnergyExport_MWh = data.getRapidRunData().getWeekdayEnergyExport_MWh();

v_weekdayEnergyConsumed_MWh = data.getRapidRunData().getWeekdayEnergyConsumed_MWh();
v_weekdayEnergyProduced_MWh = data.getRapidRunData().getWeekdayEnergyProduced_MWh();
v_weekdayEnergySelfConsumed_MWh = data.getRapidRunData().getWeekdayEnergySelfConsumed_MWh();

v_weekdayElectricityConsumed_MWh = data.getRapidRunData().getWeekdayElectricityConsumed_MWh();
v_weekdayElectricityProduced_MWh = data.getRapidRunData().getWeekdayElectricityProduced_MWh();
v_weekdayElectricitySelfConsumed_MWh = data.getRapidRunData().getWeekdayElectricitySelfConsumed_MWh();

v_weekendEnergyImport_MWh = data.getRapidRunData().getWeekendEnergyImport_MWh();
v_weekendEnergyExport_MWh = data.getRapidRunData().getWeekendEnergyExport_MWh();

v_weekendEnergyConsumed_MWh = data.getRapidRunData().getWeekendEnergyConsumed_MWh();
v_weekendEnergyProduced_MWh = data.getRapidRunData().getWeekendEnergyProduced_MWh();
v_weekendEnergySelfConsumed_MWh = data.getRapidRunData().getWeekendEnergySelfConsumed_MWh();

v_weekendElectricityConsumed_MWh = data.getRapidRunData().getWeekendElectricityConsumed_MWh();
v_weekendElectricityProduced_MWh = data.getRapidRunData().getWeekendElectricityProduced_MWh();
v_weekendElectricitySelfConsumed_MWh = data.getRapidRunData().getWeekendElectricitySelfConsumed_MWh();

/*ALCODEEND*/}

double f_updateLiveDatasets(I_EnergyData data)
{/*ALCODESTART::1741792546537*/
//Datasets for live charts
//========== CONSUMPTION ==========//
dsm_liveConsumption_kW = data.getLiveData().dsm_liveDemand_kW;
v_dataElectricityBaseloadConsumptionLiveWeek_kW = data.getLiveData().data_baseloadElectricityDemand_kW;
v_dataElectricityForHeatConsumptionLiveWeek_kW = data.getLiveData().data_heatPumpElectricityDemand_kW;
v_dataElectricityForTransportConsumptionLiveWeek_kW = data.getLiveData().data_electricVehicleDemand_kW;
v_dataElectricityForStorageConsumptionLiveWeek_kW = data.getLiveData().data_batteryCharging_kW;
v_dataElectricityForHydrogenConsumptionLiveWeek_kW = data.getLiveData().data_hydrogenElectricityDemand_kW;
v_dataElectricityForCookingConsumptionLiveWeek_kW = data.getLiveData().data_cookingElectricityDemand_kW;
v_dataDistrictHeatConsumptionLiveWeek_kW = data.getLiveData().data_districtHeatDelivery_kW;

//========== PRODUCTION ==========//
dsm_liveProduction_kW = data.getLiveData().dsm_liveSupply_kW;
v_dataWindElectricityProductionLiveWeek_kW = data.getLiveData().data_windGeneration_kW;
v_dataPVElectricityProductionLiveWeek_kW = data.getLiveData().data_PVGeneration_kW;
v_dataStorageElectricityProductionLiveWeek_kW = data.getLiveData().data_batteryDischarging_kW;
v_dataV2GElectricityProductionLiveWeek_kW = data.getLiveData().data_V2GSupply_kW;
v_dataCHPElectricityProductionLiveWeek_kW = data.getLiveData().data_CHPElectricityProductionLiveWeek_kW;

//SOC
v_dataBatterySOCLiveWeek_fr.reset();
for (int i = 0; i < data.getLiveData().data_batteryStoredEnergyLiveWeek_MWh.size(); i++) {
    // Get the x and y values from the source dataset
    double x = data.getLiveData().data_batteryStoredEnergyLiveWeek_MWh.getX(i);
    double y = data.getLiveData().data_batteryStoredEnergyLiveWeek_MWh.getY(i);
    
    // Modify the y value (e.g., divide it by 2)
    double SOC = v_batteryStorageCapacityInstalled_MWh > 0 ? y / v_batteryStorageCapacityInstalled_MWh : 0;
    
    // Add the new x and y values to the target dataset
    v_dataBatterySOCLiveWeek_fr.add(x, SOC);
}

//Total
v_dataNetLoadLiveWeek_kW = data.getLiveData().data_liveElectricityBalance_kW;

//Capacity
v_dataElectricityDeliveryCapacityLiveWeek_kW = data.getLiveData().data_gridCapacityDemand_kW;
v_dataElectricityFeedInCapacityLiveWeek_kW = data.getLiveData().data_gridCapacitySupply_kW;

/*ALCODEEND*/}

double f_updateWeeklyDatasets(I_EnergyData data)
{/*ALCODESTART::1741792546539*/
//========== SUMMER WEEK ==========//
// Consumption
double summerWeekStartTime_h = energyModel.p_startHourSummerWeek - energyModel.p_runStartTime_h;
dsm_summerWeekConsumptionDataSets_kW = data.getRapidRunData().am_summerWeekConsumptionAccumulators_kW.getDataSetMap(summerWeekStartTime_h);
v_dataElectricityBaseloadConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekBaseloadElectricityConsumption_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityForHeatConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekHeatPumpElectricityConsumption_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityForTransportConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekElectricVehicleConsumption_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityForStorageConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekBatteriesConsumption_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityForCookingConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekElectricCookingConsumption_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityForHydrogenConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekElectrolyserElectricityConsumption_kW.getDataSet(summerWeekStartTime_h);
v_dataDistrictHeatConsumptionSummerWeek_kW = data.getRapidRunData().acc_summerWeekDistrictHeatingConsumption_kW.getDataSet(summerWeekStartTime_h);

// Production
dsm_summerWeekProductionDataSets_kW = data.getRapidRunData().am_summerWeekProductionAccumulators_kW.getDataSetMap(summerWeekStartTime_h);
v_dataElectricityWindProductionSummerWeek_kW = data.getRapidRunData().acc_summerWeekWindProduction_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityPVProductionSummerWeek_kW = data.getRapidRunData().acc_summerWeekPVProduction_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityStorageProductionSummerWeek_kW = data.getRapidRunData().acc_summerWeekBatteriesProduction_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityV2GProductionSummerWeek_kW = data.getRapidRunData().acc_summerWeekV2GProduction_kW.getDataSet(summerWeekStartTime_h);
v_dataElectricityCHPProductionSummerWeek_kW = data.getRapidRunData().acc_summerWeekCHPElectricityProduction_kW.getDataSet(summerWeekStartTime_h);

// Other
v_dataNetLoadSummerWeek_kW = data.getRapidRunData().am_summerWeekBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getDataSet(summerWeekStartTime_h);

DataSet summerWeekBatteryStorage = data.getRapidRunData().ts_summerWeekBatteriesStoredEnergy_MWh.getDataSet(summerWeekStartTime_h);
v_dataBatterySOCSummerWeek_fr.reset();
for (int i = 0; i < summerWeekBatteryStorage.size(); i++) {
    // Get the x and y values from the source dataset
    double x = summerWeekBatteryStorage.getX(i);
    double y = summerWeekBatteryStorage.getY(i);
    
    // Modify the y value (e.g., divide it by 2)
    double SOC = v_batteryStorageCapacityInstalled_MWh > 0 ? y / v_batteryStorageCapacityInstalled_MWh : 0;
    
    // Add the new x and y values to the target dataset
    v_dataBatterySOCSummerWeek_fr.add(x, SOC);
}

//========== WINTER WEEK ==========//
// Consumption
double winterWeekStartTime_h = energyModel.p_startHourWinterWeek - energyModel.p_runStartTime_h;
dsm_winterWeekConsumptionDataSets_kW = data.getRapidRunData().am_winterWeekConsumptionAccumulators_kW.getDataSetMap(winterWeekStartTime_h);
v_dataElectricityBaseloadConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekBaseloadElectricityConsumption_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityForHeatConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekHeatPumpElectricityConsumption_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityForTransportConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekElectricVehicleConsumption_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityForStorageConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekBatteriesConsumption_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityForCookingConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekElectricCookingConsumption_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityForHydrogenConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekElectrolyserElectricityConsumption_kW.getDataSet(winterWeekStartTime_h);
v_dataDistrictHeatConsumptionWinterWeek_kW = data.getRapidRunData().acc_winterWeekDistrictHeatingConsumption_kW.getDataSet(winterWeekStartTime_h);

// Production
dsm_winterWeekProductionDataSets_kW = data.getRapidRunData().am_winterWeekProductionAccumulators_kW.getDataSetMap(winterWeekStartTime_h);
v_dataElectricityWindProductionWinterWeek_kW = data.getRapidRunData().acc_winterWeekWindProduction_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityPVProductionWinterWeek_kW = data.getRapidRunData().acc_winterWeekPVProduction_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityStorageProductionWinterWeek_kW = data.getRapidRunData().acc_winterWeekBatteriesProduction_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityV2GProductionWinterWeek_kW = data.getRapidRunData().acc_winterWeekV2GProduction_kW.getDataSet(winterWeekStartTime_h);
v_dataElectricityCHPProductionWinterWeek_kW = data.getRapidRunData().acc_winterWeekCHPElectricityProduction_kW.getDataSet(winterWeekStartTime_h);

// Other
v_dataNetLoadWinterWeek_kW = data.getRapidRunData().am_winterWeekBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getDataSet(winterWeekStartTime_h);

DataSet winterWeekBatteryStorage = data.getRapidRunData().ts_winterWeekBatteriesStoredEnergy_MWh.getDataSet(winterWeekStartTime_h);
v_dataBatterySOCWinterWeek_fr.reset();
for (int i = 0; i < winterWeekBatteryStorage.size(); i++) {
    // Get the x and y values from the source dataset
    double x = winterWeekBatteryStorage.getX(i);
    double y = winterWeekBatteryStorage.getY(i);
    
    // Modify the y value (e.g., divide it by 2)
    double SOC = v_batteryStorageCapacityInstalled_MWh > 0 ? y / v_batteryStorageCapacityInstalled_MWh : 0;
    
    // Add the new x and y values to the target dataset
    v_dataBatterySOCWinterWeek_fr.add(x, SOC);
}
/*ALCODEEND*/}

double f_updateYearlyDatasets(I_EnergyData data)
{/*ALCODESTART::1741792546541*/
//Datasets for yearly profiles chart
//Demand
double startTime_h = energyModel.p_runStartTime_h;
dsm_dailyAverageConsumptionDataSets_kW = data.getRapidRunData().am_dailyAverageConsumptionAccumulators_kW.getDataSetMap(startTime_h);
dsm_dailyAverageProductionDataSets_kW = data.getRapidRunData().am_dailyAverageProductionAccumulators_kW.getDataSetMap(startTime_h);
v_dataElectricityBaseloadConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageBaseloadElectricityConsumption_kW.getDataSet(startTime_h);
v_dataElectricityForHeatConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageHeatPumpElectricityConsumption_kW.getDataSet(startTime_h);
v_dataElectricityForTransportConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageElectricVehicleConsumption_kW.getDataSet(startTime_h);
v_dataElectricityForStorageConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageBatteriesConsumption_kW.getDataSet(startTime_h);
v_dataElectricityForHydrogenConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageElectrolyserElectricityConsumption_kW.getDataSet(startTime_h);
v_dataElectricityForCookingConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageElectricCookingConsumption_kW.getDataSet(startTime_h);
v_dataDistrictHeatConsumptionYear_kW = data.getRapidRunData().acc_dailyAverageDistrictHeatingConsumption_kW.getDataSet(startTime_h);
data_dailyAverageFinalEnergyConsumption_kW = data.getRapidRunData().acc_dailyAverageFinalEnergyConsumption_kW.getDataSet(startTime_h);

//Supply
v_dataElectricityWindProductionYear_kW = data.getRapidRunData().acc_dailyAverageWindProduction_kW.getDataSet(startTime_h);
v_dataElectricityPVProductionYear_kW = data.getRapidRunData().acc_dailyAveragePVProduction_kW.getDataSet(startTime_h);
v_dataElectricityStorageProductionYear_kW = data.getRapidRunData().acc_dailyAverageBatteriesProduction_kW.getDataSet(startTime_h);
v_dataElectricityV2GProductionYear_kW = data.getRapidRunData().acc_dailyAverageV2GProduction_kW.getDataSet(startTime_h);
v_dataElectricityCHPProductionYear_kW = data.getRapidRunData().acc_dailyAverageCHPElectricityProduction_kW.getDataSet(startTime_h);

DataSet totalBatteryStorage = data.getRapidRunData().ts_dailyAverageBatteriesStoredEnergy_MWh.getDataSet(startTime_h);
v_dataBatterySOCYear_fr.reset();
for (int i = 0; i < totalBatteryStorage.size(); i++) {
    // Get the x and y values from the source dataset
    double x = totalBatteryStorage.getX(i);
    double y = totalBatteryStorage.getY(i);
    
    // Modify the y value (e.g., divide it by 2)
    double SOC = v_batteryStorageCapacityInstalled_MWh > 0 ? y / v_batteryStorageCapacityInstalled_MWh : 0;
    
    // Add the new x and y values to the target dataset
    v_dataBatterySOCYear_fr.add(x, SOC);
}

/*ALCODEEND*/}

double f_addTimeStepLiveDataSetsGC(I_EnergyData data)
{/*ALCODESTART::1741792546543*/
//Update SOC live plot
double batteryStoredEnergyLiveWeek_MWh = 0;
int i = max(0, data.getLiveData().data_batteryStoredEnergyLiveWeek_MWh.size() - 1);
batteryStoredEnergyLiveWeek_MWh = data.getLiveData().data_batteryStoredEnergyLiveWeek_MWh.getY(i);	
double timeAxisValue = data.getLiveData().data_batteryStoredEnergyLiveWeek_MWh.getX(i);
double SOC = v_batteryStorageCapacityInstalled_MWh > 0 ? batteryStoredEnergyLiveWeek_MWh / v_batteryStorageCapacityInstalled_MWh : 0;
v_dataBatterySOCLiveWeek_.add(timeAxisValue, SOC); 

/*ALCODEEND*/}

double f_updateLoadDurationCurve(I_EnergyData data)
{/*ALCODESTART::1741792546545*/
J_LoadDurationCurves ldc = data.getRapidRunData().getLoadDurationCurves(energyModel);

v_dataNetbelastingDuurkrommeYear_kW = ldc.ds_loadDurationCurveTotal_kW;
v_dataNetbelastingDuurkrommeYearVorige_kW = ldc.ds_previousLoadDurationCurveTotal_kW;

v_dataNetbelastingDuurkrommeSummer_kW = ldc.ds_loadDurationCurveSummer_kW;
v_dataNetbelastingDuurkrommeWinter_kW = ldc.ds_loadDurationCurveWinter_kW;
v_dataNetbelastingDuurkrommeDaytime_kW = ldc.ds_loadDurationCurveDaytime_kW;
v_dataNetbelastingDuurkrommeNighttime_kW = ldc.ds_loadDurationCurveNighttime_kW;
v_dataNetbelastingDuurkrommeWeekend_kW = ldc.ds_loadDurationCurveWeekend_kW;
v_dataNetbelastingDuurkrommeWeekday_kW = ldc.ds_loadDurationCurveWeekday_kW;
/*ALCODEEND*/}

double f_updateActiveAssetBooleans(I_EnergyData data)
{/*ALCODESTART::1741858724892*/
b_hasElectricHeating = data.getActiveAssetData().hasElectricHeating;
b_hasElectricTransport = data.getActiveAssetData().hasElectricTransport;
b_hasPV = data.getActiveAssetData().hasPV;
b_hasWindturbine = data.getActiveAssetData().hasWindturbine;
b_hasBattery = data.getActiveAssetData().hasBattery;
b_hasHeatGridConnection = data.getActiveAssetData().hasHeatGridConnection;
b_hasCHP = data.getActiveAssetData().hasCHP;
b_hasV2G = data.getActiveAssetData().hasV2G;
b_hasElectricCooking = data.getActiveAssetData().hasElectricCooking;
/*ALCODEEND*/}

