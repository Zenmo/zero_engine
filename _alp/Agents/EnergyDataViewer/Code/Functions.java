double f_initializeMaps()
{/*ALCODESTART::1732892014327*/
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

