double f_updatePricesForNextTimestep(double t_h)
{/*ALCODESTART::1664894248130*/
// Update the dayaheadprice
nationalEnergyMarket.f_updateEnergyPrice();

//
for (EnergySupplier e : pop_energySuppliers) {
	e.f_updateEnergyPrice();
}
for (GridOperator g : pop_gridOperators) {
	if( g.p_hasCongestionPricing ){
		g.f_updateCongestionTariff();
	}
}
for (EnergyCoop e : pop_energyCoops) {
	//e.f_updateEnergyPrice();
	//e.f_updateOtherIncentives();
	//e.f_setPriceBandsExperiment();
	e.f_updateIncentives();
}

for (GridNode GN : c_gridNodeExecutionListReverse) {
	GN.f_propagateNodalPricing();
}


/*if (p_parallelize) {
	c_connectionOwners.parallelStream().forEach(co -> co.f_updateIncentives());	
} else {*/
	for (ConnectionOwner c : pop_connectionOwners) {
		c.f_updateIncentives();
	}
//}
/*ALCODEEND*/}

double f_updateTimeseries(double t_h)
{/*ALCODESTART::1664952601107*/
b_isDaytime = t_h % 24 > 6 && t_h % 24 < 18;
b_isWeekday = (t_h+(v_dayOfWeek1jan-1)*24) % (24*7) < (24*5);
b_isSummerWeek = (t_h % 8760) >= p_startOfSummerWeek_h && (t_h % 8760) < p_startOfSummerWeek_h + 24*7;
b_isWinterWeek = (t_h % 8760) >= p_startOfWinterWeek_h && (t_h % 8760) < p_startOfWinterWeek_h + 24*7;
b_isLastTimeStepOfDay = t_h % 24 == (24-p_timeStep_h);
t_hourOfDay = t_h % 24; // Assumes modelrun starts at midnight.


v_currentAmbientTemperature_degC = pp_ambientTemperature_degC.getCurrentValue();
c_profiles.forEach(p -> p.updateValue(t_h));
v_currentWindPowerNormalized_r = pp_windProduction_fr.getCurrentValue();
v_currentSolarPowerNormalized_r = pp_PVProduction35DegSouth_fr.getCurrentValue();
//v_currentCookingDemand_fr = tf_cooking_demand(t_h);

if (b_enableDLR) {
	v_currentDLRfactor_fr = 1 + max(-0.1,v_currentWindPowerNormalized_r * 0.025*(30-v_currentAmbientTemperature_degC) + 0.5 - v_currentSolarPowerNormalized_r);
	//v_currentDLRfactor_fr = 1 + uniform(-0.1, 1.0);
	v_minDLRfactor_fr = min (v_minDLRfactor_fr, v_currentDLRfactor_fr);
	v_maxDLRfactor_fr = max (v_maxDLRfactor_fr, v_currentDLRfactor_fr);
	acc_totalDLRfactor_f.addStep( v_currentDLRfactor_fr);
	/*if (v_currentDLRfactor_fr < 0.5) {
		traceln("v_currentDLRfactor_fr is invalid! %s", v_currentDLRfactor_fr);
		pauseSimulation();
	}*/
}
//traceln("Current DLR factor: %s, ", v_currentDLRfactor_fr);
//traceln("Time hour " + time(HOUR) + ", t_h " + t_h + ", fleet demand " + v_currentLogisticsFleetEDemand_fr);

//Update ambient dependent assets
f_updateAmbientDependentAssets();

// Update forecasts,  the relevant profile pointers are already updated above
c_forecasts.forEach(f -> f.updateForecast(t_h));
v_SolarYieldForecast_fr = pf_PVProduction35DegSouth_fr.getForecast();
v_WindYieldForecast_fr = pf_windProduction_fr.getForecast();
// The ElectricityYieldForecast assumes solar and wind forecasts have the same forecast time
if ( v_liveAssetsMetaData.totalInstalledPVPower_kW + v_liveAssetsMetaData.totalInstalledWindPower_kW > 0 ) {
	v_electricityYieldForecast_fr = (v_SolarYieldForecast_fr * v_liveAssetsMetaData.totalInstalledPVPower_kW + v_WindYieldForecast_fr * v_liveAssetsMetaData.totalInstalledWindPower_kW) / (v_liveAssetsMetaData.totalInstalledPVPower_kW + v_liveAssetsMetaData.totalInstalledWindPower_kW);
}

v_epexForecast_eurpkWh = 0.001*pf_dayAheadElectricityPricing_eurpMWh.getForecast();

for (GridNode GN : c_gridNodeExecutionList) {
	GN.f_updateForecasts();
}

// And price forecast! 

/*ALCODEEND*/}

double f_calculateGridnodeFlows(double t_h)
{/*ALCODESTART::1665051878402*/
v_currentElectricityImport_kW = 0;
v_currentElectricityExport_kW = 0;

for(GridNode n : c_gridNodeExecutionList) {
	n.f_calculateEnergyBalance();
}

for(GridNode n : c_gridNodesTopLevel) {
	if (n.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
		v_currentElectricityImport_kW += max(0, n.v_currentLoad_kW );
		v_currentElectricityExport_kW += max(0, -n.v_currentLoad_kW );
	}
}

/*ALCODEEND*/}

double f_calculateActorFlows(double t_h)
{/*ALCODESTART::1665051962956*/
/*
if (b_parallelizeConnectionOwners) {
	c_connectionOwners.parallelStream().forEach(co -> co.f_updateFinances());
} else {
	for (ConnectionOwner c : pop_connectionOwners) {
		c.f_updateFinances();
	}
}
*/

for (EnergyCoop h : pop_energyCoops) {
	h.f_calculateEnergyBalance();
}
/*for (EnergySupplier e : pop_energySuppliers) {
	e.f_updateFinances();
}*/

/*ALCODEEND*/}

double f_sumGridNodeLoads()
{/*ALCODESTART::1666879523873*/
// Bereken belasting per netvlak in het model (elektriciteit, absolute waarde) voor kosten-berekeningen gebied.

// Reset totals in case a full year simulation was already run before...
v_gridNodePeakLoadElectricityHSMS_kW = 0;
//v_totalElectricityImport_MWh = 0;
//v_totalElectricityExport_MWh = 0;
c_timesOfNodePeakLoads_h.clear();
v_cumulativeGridCapacityHSMS_kW = 0;
v_gridOverloadDuration_h = 0;

//v_totalHeatProduced_MWh = 0;

for(GridNode h : c_gridNodesTopLevel ) {
//	v_gridNodePeakLoadElectricityHSMS_kW = max(v_gridNodePeakLoadElectricityHSMS_kW,abs(h.v_peakLoadFilteredElectricity_kW));
	v_gridNodePeakLoadElectricityHSMS_kW += abs(h.v_peakLoadFilteredElectricity_kW);
	
	//v_totalElectricityImport_MWh += h.v_electricityDrawn_kWh / 1000.0;
	//v_totalElectricityExport_MWh += h.v_electricityDelivered_kWh / 1000.0;
	//c_timesOfNodePeakLoads_h.put(h.p_gridNodeID, h.v_timeOfPeakLoadFiltered_h);
	v_cumulativeGridCapacityHSMS_kW += h.p_capacity_kW;
	
	v_gridOverloadDuration_h += h.v_totalTimeOverloaded_h;
}

//v_avgGridLoad_fr = sum(c_gridNodesTopLevel, GN->GN.v_averageAbsoluteLoadElectricity_kW)/v_cumulativeGridCapacityHSMS_kW;

/*ALCODEEND*/}

double f_sumBatteryUse()
{/*ALCODESTART::1666978595555*/
v_totalBatteryDischargeAmount_MWh = 0;
v_totalBatteryChargeAmount_MWh = 0;
//v_totalBatteryInstalledCapacity_MWh = 0;
v_totalBatteryEnergyUsed_MWh = 0;

for(J_EA ea : c_energyAssets) { // Single loop of all assets without using c_EVs and c_storageAssets
	if( ea instanceof J_EAStorageElectric ) {
		J_EAStorageElectric e = (J_EAStorageElectric)ea;
		v_totalBatteryDischargeAmount_MWh += e.getTotalDischargeAmount_kWh() / 1000;
		v_totalBatteryChargeAmount_MWh += e.getTotalChargeAmount_kWh() / 1000;
		v_totalBatteryEnergyUsed_MWh += e.getEnergyUsed_kWh() / 1000;
	}
	
	if( ea instanceof J_EAEV ) {
		J_EAEV e = (J_EAEV)ea;
		v_totalBatteryDischargeAmount_MWh += e.getTotalDischargeAmount_kWh() / 1000;
		v_totalBatteryChargeAmount_MWh += e.getTotalChargeAmount_kWh() / 1000;
		v_totalBatteryEnergyUsed_MWh += e.getEnergyUsed_kWh() / 1000;
	}
}

/*
for(J_EAStorage es : c_storageAssets ) {
	if( es instanceof J_EAStorageElectric ) {
		J_EAStorageElectric e = (J_EAStorageElectric)es;
		v_totalBatteryDischargeAmount_MWh += e.getTotalDischargeAmount_kWh() / 1000;
		v_totalBatteryChargeAmount_MWh += e.getTotalChargeAmount_kWh() / 1000;
		v_totalBatteryInstalledCapacity_MWh += e.getStorageCapacity_kWh() / 1000;
		v_totalBatteryEnergyUsed_MWh += e.getEnergyUsed_kWh() / 1000;
		//traceln( ((J_EAStorageElectric)e.j_ea).getStorageCapacity() / 1000 );
	}
}
for(J_EAEV ev : c_EVs ) {
	v_totalBatteryDischargeAmount_MWh += ev.getTotalDischargeAmount_kWh() / 1000;
	v_totalBatteryChargeAmount_MWh += ev.getTotalChargeAmount_kWh() / 1000;
	v_totalBatteryInstalledCapacity_MWh += ev.getStorageCapacity_kWh() / 1000;
	v_totalBatteryEnergyUsed_MWh += ev.getEnergyUsed_kWh() / 1000;
	//traceln( ((J_EAStorageElectric)e.j_ea).getStorageCapacity() / 1000 );
}
*/

v_batteryStoredEnergyDeltaSinceStart_MWh = v_totalBatteryChargeAmount_MWh - v_totalBatteryDischargeAmount_MWh - v_totalBatteryEnergyUsed_MWh;
if (v_batteryStoredEnergyDeltaSinceStart_MWh == Double.NaN) {
	v_batteryStoredEnergyDeltaSinceStart_MWh = 0;
}
traceln("Electricity delta in batteries (including EVs): "+ v_batteryStoredEnergyDeltaSinceStart_MWh + " MWh");
/*ALCODEEND*/}

double f_calculateGridConnectionFlows(double t_h)
{/*ALCODESTART::1668528129020*/
fm_currentProductionFlows_kW.clear();
fm_currentConsumptionFlows_kW.clear();
fm_currentBalanceFlows_kW.clear();


v_currentFinalEnergyConsumption_kW = 0;
v_currentPrimaryEnergyProduction_kW = 0;
v_currentEnergyCurtailed_kW = 0;
v_currentPrimaryEnergyProductionHeatpumps_kW = 0;

if (b_parallelizeGridConnections) {
	c_gridConnections.parallelStream().forEach(gc -> gc.f_calculateEnergyBalance());
	for(GridConnection gc : c_gridConnections) { // Can't do this in parallel due to different threads writing to the same values!
		
		fm_currentBalanceFlows_kW.addFlows(gc.fm_currentBalanceFlows_kW);
		fm_currentProductionFlows_kW.addFlows(gc.fm_currentProductionFlows_kW);
		fm_currentConsumptionFlows_kW.addFlows(gc.fm_currentConsumptionFlows_kW);

		v_currentFinalEnergyConsumption_kW += gc.v_currentFinalEnergyConsumption_kW;
		v_currentPrimaryEnergyProduction_kW += gc.v_currentPrimaryEnergyProduction_kW;
		v_currentEnergyCurtailed_kW += gc.v_currentEnergyCurtailed_kW;
		v_currentPrimaryEnergyProductionHeatpumps_kW += gc.v_currentPrimaryEnergyProductionHeatpumps_kW;
	}
} 
else {
	for(GridConnection gc : c_gridConnections) {
		gc.f_calculateEnergyBalance();
		
		fm_currentBalanceFlows_kW.addFlows(gc.fm_currentBalanceFlows_kW);
		fm_currentProductionFlows_kW.addFlows(gc.fm_currentProductionFlows_kW);
		fm_currentConsumptionFlows_kW.addFlows(gc.fm_currentConsumptionFlows_kW);
		
		v_currentFinalEnergyConsumption_kW += gc.v_currentFinalEnergyConsumption_kW;
		v_currentPrimaryEnergyProduction_kW += gc.v_currentPrimaryEnergyProduction_kW;
		v_currentEnergyCurtailed_kW += gc.v_currentEnergyCurtailed_kW;
		v_currentPrimaryEnergyProductionHeatpumps_kW += gc.v_currentPrimaryEnergyProductionHeatpumps_kW;
	}
}

for (GridConnection gc : c_subGridConnections) {
	gc.f_calculateEnergyBalance();
}

v_currentEnergyImport_kW = 0.0;
v_currentEnergyExport_kW = 0.0;
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	double netFlow_kW = fm_currentBalanceFlows_kW.get(EC);
	v_currentEnergyImport_kW += max( 0, netFlow_kW );
	v_currentEnergyExport_kW += max( 0, -netFlow_kW );
}

/*
if (v_isRapidRun) {	
	if (v_timeStepsElapsed == 0) {
		heatDemandProfile.setCellValue("Tijd (uren)", "Sheet1", 1, 1);
		heatDemandProfile.setCellValue("Datum", "Sheet1", 1, 2);
		heatDemandProfile.setCellValue("Energie Behoefte (kWh)", "Sheet1", 1, 3);
	}
	
	heatDemandProfile.setCellValue(t_h, "Sheet1", v_timeStepsElapsed+2, 1);
	
	double unix_time = (1672531200.0 + t_h * 60 * 60 ) / 86400.0 + 25569.0; // 1672531200 is 1 jan 2023 GMT+1
	heatDemandProfile.setCellValue(unix_time, "Sheet1", v_timeStepsElapsed+2, 2);

	double totalHeatDemand_kW = sum(c_gridConnections,x->x.fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT));
	heatDemandProfile.setCellValue(totalHeatDemand_kW, "Sheet1", v_timeStepsElapsed + 2, 3);
}
*/

/*ALCODEEND*/}

double f_initializeForecasts()
{/*ALCODESTART::1671636439933*/
pf_ambientTemperature_degC = new J_ProfileForecaster(null, pp_ambientTemperature_degC, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_ambientTemperature_degC);

pf_PVProduction35DegSouth_fr = new J_ProfileForecaster(null, pp_PVProduction35DegSouth_fr, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_PVProduction35DegSouth_fr);

pf_PVProduction15DegEastWest_fr = new J_ProfileForecaster(null, pp_PVProduction15DegEastWest_fr, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_PVProduction15DegEastWest_fr);

pf_windProduction_fr = new J_ProfileForecaster(null, pp_windProduction_fr, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_windProduction_fr);

pf_dayAheadElectricityPricing_eurpMWh = new J_ProfileForecaster(null, pp_dayAheadElectricityPricing_eurpMWh, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_dayAheadElectricityPricing_eurpMWh);

/*ALCODEEND*/}

double f_runRapidSimulation()
{/*ALCODESTART::1696521316832*/
pauseSimulation();

traceln(" ");
traceln("*** Running headless simulation *** ");
traceln(" ");

double startTime1 = System.currentTimeMillis();


// Store and reset model states...

for (J_EA EA : c_energyAssets) {
	EA.storeStatesAndReset();		
}

for (GridConnection GC : c_gridConnections) {
	if (GC.v_rapidRunData != null) {
		if (b_storePreviousRapidRunData) {
			GC.v_previousRunData = GC.v_rapidRunData.getClone();
		}
	} else {
		GC.v_rapidRunData = new J_RapidRunData(GC);
		GC.v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, GC.v_activeEnergyCarriers, GC.v_activeConsumptionEnergyCarriers, GC.v_activeProductionEnergyCarriers); //f_initializeAccumulators();
	}
	GC.f_resetStates();
	GC.v_rapidRunData.assetsMetaData = GC.v_liveAssetsMetaData.getClone();
	GC.v_rapidRunData.connectionMetaData = GC.v_liveConnectionMetaData.getClone();

	//GC.c_tripTrackers.forEach(tt->tt.storeAndResetState());
	//GC.c_tripTrackers.forEach(tt->tt.setStartIndex(p_runStartTime_h));
	//GC.c_tripTrackers.forEach(tt->tt.prepareNextActivity(p_runStartTime_h*60));
	
	GC.c_tripTrackers.forEach(tt->{
		tt.storeAndResetState();
		tt.setStartIndex(p_runStartTime_h);
		tt.prepareNextActivity(p_runStartTime_h*60);
		});
	if (GC instanceof GCHouse) {
		if (((GCHouse)GC).p_cookingTracker != null) {
			((GCHouse)GC).p_cookingTracker.storeAndResetState();
		}
	}
}
for (GridConnection GC : c_subGridConnections) {
	GC.f_resetStates();
}

for (GridNode GN : pop_gridNodes) {
	GN.f_resetStates();
}

for (ConnectionOwner CO : pop_connectionOwners) {
	CO.f_resetStates();
}

for (EnergyCoop EC : pop_energyCoops) {
	if (EC.v_rapidRunData != null) {
		if (b_storePreviousRapidRunData) {
			EC.v_previousRunData = EC.v_rapidRunData.getClone();
		}
	} else {
		EC.v_rapidRunData = new J_RapidRunData(EC);
		
		EC.v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, EC.v_activeEnergyCarriers, EC.v_activeConsumptionEnergyCarriers, EC.v_activeProductionEnergyCarriers);
	}
	EC.f_resetStates();
	EC.v_rapidRunData.assetsMetaData = EC.v_liveAssetsMetaData.getClone();
	EC.v_rapidRunData.connectionMetaData = EC.v_liveConnectionMetaData.getClone();
}


//t_h=v_runStartTime_h;
int v_timeStepsElapsed_live = v_timeStepsElapsed;
v_timeStepsElapsed=0;

c_profiles.forEach(p -> p.updateValue(p_runStartTime_h));
c_forecasts.forEach(p -> p.initializeForecast(p_runStartTime_h)); 
//c_forecasts.parallelStream().forEach(p -> p.initializeForecast(p_runStartTime_h)); 

// When adding actors, also reset their states! Not used yet for Drechtsteden...


if (v_rapidRunData != null) {
	if (b_storePreviousRapidRunData) {
		v_previousRunData = v_rapidRunData.getClone();
	}
} else {
	v_rapidRunData = new J_RapidRunData(this);
	v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();	
}

f_resetAnnualValues();
v_rapidRunData.assetsMetaData = v_liveAssetsMetaData.getClone();	
v_rapidRunData.connectionMetaData = v_liveConnectionMetaData.getClone();


v_isRapidRun = true;

//Run energy calculations loop
for(t_h = p_runStartTime_h; t_h < p_runEndTime_h; t_h += p_timeStep_h){
	// Update time-series for model-wide variables (such as temps, wind, etc.)
	double startTime = System.currentTimeMillis();
	f_updateTimeseries(t_h);
	v_timeSeriesRuntime_ms += (System.currentTimeMillis()-startTime);
	
	// Operate assets on each gridConnection
	startTime = System.currentTimeMillis();
	f_calculateGridConnectionFlows(t_h);
	v_gridConnectionsRuntime_ms += (System.currentTimeMillis()-startTime);
	
	// Calculate grid node flows
	startTime = System.currentTimeMillis();
	f_calculateGridnodeFlows(t_h);
	v_gridNodesRuntime_ms += (System.currentTimeMillis()-startTime);
	
	// Financial accounting of energy flows
	startTime = System.currentTimeMillis();
	f_calculateActorFlows(t_h);
	v_financialsRuntime_ms += (System.currentTimeMillis()-startTime);
	
	// Update elektriciteitsprijzen
	startTime = System.currentTimeMillis();
	f_updatePricesForNextTimestep(t_h);
	v_incentivesRuntime_ms += (System.currentTimeMillis()-startTime);


	/*// Update time-series for model-wide variables (such as temps, wind, etc.)
	f_updateTimeseries(t_h);
	
	// Operate assets on each gridConnection
	f_calculateGridConnectionFlows(t_h);
	
	// Calculate grid node flows
	f_calculateGridnodeFlows(t_h);
	
	// Financial accounting of energy flows
	f_calculateFinancialFlows(t_h);
	
	// Update elektriciteitsprijzen
	f_updatePricesForNextTimestep(t_h);
	*/
	f_rapidRunDataLogging();
	
	v_timeStepsElapsed++;
}	
//traceln("HVMV overloaded hours: %s", c_gridNodesHSMS.get(0).v_totalTimeOverloaded_h);
double startTime = System.currentTimeMillis();

if( p_gridNodeTimeSeriesExcel != null){
	f_writeGridNodeTimeseriesToExcel();
}

f_calculateKPIs();
v_kpiCalcsRuntime_ms = (System.currentTimeMillis()-startTime);
traceln("---FINISHED YEAR MODEL RUN----");

//numberOfRuns++;

//Return model to previous state to continue simulation run
v_timeStepsElapsed = v_timeStepsElapsed_live;
t_h = p_runStartTime_h + v_timeStepsElapsed * p_timeStep_h;

for (J_EA EA : c_energyAssets) {
	EA.restoreStates();		
}
for (GridNode GN : pop_gridNodes) {
	//GN.f_resetStates();
}
for (GridConnection GC : c_gridConnections) {
	//GC.f_resetStates();
	GC.f_resetStatesAfterRapidRun();
	GC.c_tripTrackers.forEach(tt->{
		tt.restoreState();
		tt.prepareNextActivity((t_h-p_runStartTime_h)*60);
		});	
	//GC.c_tripTrackers.forEach(tt->tt.prepareNextActivity((t_h-p_runStartTime_h)*60));
	if (GC instanceof GCHouse) {
		if (((GCHouse)GC).p_cookingTracker != null) {
			((GCHouse)GC).p_cookingTracker.restoreState();
		}
	}	
}

f_setInitialValues();

v_isRapidRun = false;

double duration = System.currentTimeMillis() - startTime1;

traceln("*** headless run duration: "+ duration/1000 + " s ***");

//t_h = time(HOUR) + v_hourOfYearStart;


traceln("Live-sim t_h after rapidRun: %s", t_h);
c_profiles.forEach(p -> p.updateValue(t_h)); 
c_forecasts.forEach(p -> p.initializeForecast(t_h)); 
//c_forecasts.parallelStream().forEach(p -> p.initializeForecast(t_h)); 


/*ALCODEEND*/}

double f_calculateKPIs()
{/*ALCODESTART::1698922757486*/
for(GridConnection g: c_gridConnections){ // 
	    c_gridConnectionOverload_fr.put(g.p_gridConnectionID, g.v_maxConnectionLoad_fr);
}	

pop_gridNodes.forEach(gn -> gn.f_calculateKPIs()); // This concerns a relatively small collection, so no need for parallelStream.

f_sumGridNodeLoads();
f_sumBatteryUse();

pop_energyCoops.forEach(ec -> ec.f_calculateKPIs()); // Must go after f_sumGridNodeLoads() because it uses total electricity export!

// Totals from accumulators:
v_totalElectricityConsumed_MWh = v_rapidRunData.getTotalElectricityConsumed_MWh(); //am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_totalElectricityProduced_MWh= v_rapidRunData.getTotalElectricityProduced_MWh(); //am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;

v_totalEnergyConsumed_MWh = v_rapidRunData.getTotalEnergyConsumed_MWh();//acc_dailyAverageEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_totalEnergyProduced_MWh = v_rapidRunData.getTotalEnergyProduced_MWh();//acc_dailyAverageEnergyProduction_kW.getIntegral_kWh() / 1000;
v_totalEnergyImport_MWh = v_rapidRunData.am_totalBalanceAccumulators_kW.totalIntegralPos_kWh()/1000;
v_totalEnergyExport_MWh = -v_rapidRunData.am_totalBalanceAccumulators_kW.totalIntegralNeg_kWh()/1000;
v_totalEnergyCurtailed_MWh = v_rapidRunData.getTotalEnergyCurtailed_MWh();//acc_totalEnergyCurtailed_kW.getIntegral_kWh() / 1000;
//v_totalPrimaryEnergyProductionHeatpumps_MWh = acc_totalPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;

// Electricity self consumption
v_individualSelfSufficiency_fr = sum(c_gridConnections, gc -> gc.v_rapidRunData.getTotalElectricitySelfConsumed_MWh()) / v_totalElectricityConsumed_MWh;
v_individualSelfConsumption_fr = sum(c_gridConnections, gc -> gc.v_rapidRunData.getTotalElectricitySelfConsumed_MWh()) / v_totalElectricityProduced_MWh;
v_totalElectricitySelfConsumed_MWh = v_rapidRunData.getTotalElectricitySelfConsumed_MWh();// max(0, v_totalElectricityConsumed_MWh - fm_totalImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));
v_collectiveSelfConsumption_fr = v_totalElectricitySelfConsumed_MWh / v_totalElectricityProduced_MWh;

//Tracelns
traceln("");
traceln("__--** Totals **--__");
traceln("Energy consumed: "+ v_totalEnergyConsumed_MWh + " MWh");
traceln("Energy produced: "+ v_totalEnergyProduced_MWh + " MWh");
traceln("Energy import: "+ v_totalEnergyImport_MWh + " MWh");
traceln("Energy export: "+ v_totalEnergyExport_MWh + " MWh");

// *** Total energy balance ***
double deltaThermalEnergySinceStart_MWh = 0;
double totalAmbientHeating_MWh = 0;
double totalEnergyCurtailed_MWh = 0;
double totalEnergyProduced_MWh = 0;
double totalEnergyUsed_MWh = 0;
double totalEnergyChargedOutsideModel_MWh = 0;
double totalHeatProduced_MWh = 0;
for (J_EA e : c_energyAssets) {
	if (((GridConnection) e.getParentAgent()).v_isActive ) {
		double EnergyUsed_kWh = e.getEnergyUsed_kWh();
		if (EnergyUsed_kWh > 0) {
			if( e instanceof J_EAConversionGasCHP ) {
				totalEnergyUsed_MWh += EnergyUsed_kWh/1000;
				//electricityProduced_kWh = ((J_EAConversionGasCHP)e).getElectricityProduced_kWh();
				//electricityProduced_MWh += electricityProduced_kWh/1000;
			} else {
				totalEnergyUsed_MWh += EnergyUsed_kWh/1000;
			}
			if ( e instanceof J_EABuilding ) {
				totalEnergyProduced_MWh += ((J_EABuilding)e).energyAbsorbed_kWh/1000;
				deltaThermalEnergySinceStart_MWh += (((J_EABuilding)e).getCurrentTemperature() - ((J_EABuilding)e).getInitialTemperature_degC())*((J_EABuilding)e).getHeatCapacity_JpK()/3.6e9;
				deltaThermalEnergySinceStart_MWh += ((J_EABuilding)e).getRemainingHeatBufferHeat_kWh() / 1000;
			}
		} else {
			totalEnergyProduced_MWh -= EnergyUsed_kWh/1000;
			if ( e instanceof J_EABuilding ) {
				traceln("Building has produced more energy than it has used?? Is lossfactor too low?");
				traceln("Lossfactor: %s", ((J_EABuilding)e).lossFactor_WpK);
			}
		}
		if (e instanceof J_EAStorageHeat) { // includes J_EABuilding
			totalAmbientHeating_MWh += ((J_EAStorageHeat)e).energyAbsorbed_kWh/1000;
			totalHeatProduced_MWh += ((J_EAStorageHeat)e).energyAbsorbed_kWh/1000;						
		}
		if (e instanceof J_EAEV) {
			totalEnergyChargedOutsideModel_MWh += ((J_EAEV)e).getEnergyChargedOutsideModelArea_kWh()/1000;
		}
		if (e instanceof J_EAConversionHeatPump) {
			totalHeatProduced_MWh -= EnergyUsed_kWh/1000;						
		}
	}
}
double v_totalDeltaStoredEnergy_MWh = v_batteryStoredEnergyDeltaSinceStart_MWh + deltaThermalEnergySinceStart_MWh; // Positive number means more energy stored at the end of the simulation. 

//Total selfconsumption, selfsufficiency
v_totalEnergySelfConsumed_MWh = v_totalEnergyConsumed_MWh - (v_totalEnergyImport_MWh + max(0,-v_totalDeltaStoredEnergy_MWh)); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.
//v_totalSelfConsumedEnergy_MWh = totalEnergyUsed_MWh - (v_totalImportedEnergy_MWh + max(0,-v_totalDeltaStoredEnergy_MWh)); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.

// Export and production-based selfconsumption
if ( v_totalEnergyProduced_MWh > 0 ){
	v_modelSelfConsumption_fr = v_totalEnergySelfConsumed_MWh / v_totalEnergyProduced_MWh;
}
traceln("");
traceln("Total energy absorbed from environment by buildings: %s MWh", totalAmbientHeating_MWh);
traceln("Delta thermal stored energy since start: %s MWh", deltaThermalEnergySinceStart_MWh);
traceln("Total energy from vehicles charging outside the model scope: %s MWh", totalEnergyChargedOutsideModel_MWh);

traceln("Energy selfconsumption: " + v_modelSelfConsumption_fr*100 + "%");
double totalElectricitySelfConsumption_fr = 0;

/*if ( v_totalElectricityProduced_MWh > 0 ){
	totalElectricitySelfConsumption_fr = 1 - fm_totalExports_MWh.get(OL_EnergyCarriers.ELECTRICITY)/v_totalElectricityProduced_MWh;
}*/

//v_modelSelfSufficiency_fr = 1 - (v_totalElectricityImport_MWh + max(0,v_totalMethaneImport_MWh - v_totalMethaneExport_MWh) + max(0,v_totalHydrogenImport_MWh - v_totalHydrogenExport_MWh) + v_totalDieselImport_MWh - v_batteryStoredEnergyDeltaSinceStart_MWh - deltaThermalEnergySinceStart_MWh) / v_totalEnergyUsed_MWh;
v_modelSelfSufficiency_fr = v_totalEnergySelfConsumed_MWh / v_totalEnergyConsumed_MWh; // Calculation based on (total_consumption - total_import) / total_consumption. Positive delta-stored energy is contained in v_totalSelfConsumedEnergy_MWh. 
//v_modelSelfSufficiency_fr = v_totalSelfConsumedEnergy_MWh / totalEnergyUsed_MWh; // Calculation based on (total_consumption - total_import) / total_consumption. Positive delta-stored energy is contained in v_totalSelfConsumedEnergy_MWh. 

traceln("Energy selfsufficiency (via import calc): %s %%", v_modelSelfSufficiency_fr*100);
//double totalSelfSufficiency_fr_check = (v_totalEnergyProduced_MWh - (v_totalElectricityExport_MWh + max(0,v_totalMethaneExport_MWh-v_totalMethaneImport_MWh) + max(0,v_totalHydrogenExport_MWh-v_totalHydrogenImport_MWh)))/v_totalEnergyUsed_MWh; // Calculation based on (total_production - total_export) / total_consumption
//double totalSelfSufficiency_fr_check = v_totalSelfConsumedEnergyCheck_MWh / totalEnergyUsed_MWh; // Calculation based on (total_production - total_export) / total_consumption. Negative delta-stored energy is contained in v_totalSelfConsumedEnergy_MWh. 

// Remaining difference due to different temps of houses start vs end?
traceln("");
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	traceln("Import " + EC.toString() + ": " + v_rapidRunData.getTotalImport_MWh(EC) + " MWh");
	traceln("Export " + EC.toString() + ": " + v_rapidRunData.getTotalExport_MWh(EC) + " MWh");
}

traceln("");
traceln("__--** Checks **--__");

traceln("Check energy used from array and from energy assets: %s MWh", ( v_totalEnergyConsumed_MWh - totalEnergyUsed_MWh) );
traceln("Check energy produced from array and from energy assets: %s MWh", ( v_totalEnergyProduced_MWh - totalEnergyProduced_MWh) );

double energyBalanceCheck_MWh = v_totalEnergyImport_MWh + v_totalEnergyProduced_MWh - (v_totalEnergyExport_MWh + v_totalEnergyConsumed_MWh + v_totalDeltaStoredEnergy_MWh);
traceln("Check on energy balance is: " + energyBalanceCheck_MWh + " MWh, must be zero!");
traceln("");

if ( Math.abs(energyBalanceCheck_MWh) > 1e-6 ) {
	traceln("");
	String warningString = String.format("__--** WARNING!!!! **--__");
	String errorString = String.format("ENERGY BALANCE ERROR EXCEEDING TOLERANCE!! Error: %s MWh", energyBalanceCheck_MWh);
	traceln(warningString);
	//traceln(red, errorString);
	System.err.println(errorString);
	traceln(warningString);
	traceln("");

}

/*ALCODEEND*/}

double f_resetAnnualValues()
{/*ALCODESTART::1699958741073*/
v_rapidRunData.resetAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();

// Others
acc_totalDLRfactor_f.reset();


/*ALCODEEND*/}

double f_runTimestep()
{/*ALCODESTART::1701162826549*/
t_h = p_runStartTime_h + v_timeStepsElapsed * p_timeStep_h;// + v_hourOfYearStart);// % 8760;

// Reduce startdate after one year, loop all dat
if(t_h-p_runStartTime_h!=0.0 && (t_h-p_runStartTime_h) % 8760 == 0.0) {
	Date startDate = getExperiment().getEngine().getStartDate();
	startDate.setYear(startDate.getYear()-1);
	getExperiment().getEngine().setStartDate(startDate);
	traceln("Reduced anylogic date by one year, looping all data");
}

// Update tijdreeksen in leesbare variabelen
f_updateTimeseries(t_h);

// Operate assets on each gridConnection
f_calculateGridConnectionFlows(t_h);

// Calculate grid node flows
f_calculateGridnodeFlows(t_h);

// Financial accounting of energy flows
f_calculateActorFlows(t_h);

// Update elektriciteitsprijzen
f_updatePricesForNextTimestep(t_h);

f_updateLiveData();

v_timeStepsElapsed ++;

/*ALCODEEND*/}

double f_buildGridNodeTree()
{/*ALCODESTART::1716884712799*/
//Initialize top level grid values
double topLevelElectricGridCapacity_kW = 0;
boolean topLevelGridCapacitiesKnown = true;

// First make all links between GridNodes
for( GridNode GN : pop_gridNodes ) {
	GN.f_connectToParentNode();
}

// Then build execution order list
for( GridNode GN : pop_gridNodes ) {
	GridNode parentNode = findFirst(pop_gridNodes, p->p.p_gridNodeID.equals(GN.p_parentNodeID)); // Works as long as p_gridNodeID is not null. p_parentNodeID can be null no problemo.
	//if (GN.p_parentNodeID == null) {
	if (parentNode == null) {
		f_gridNodeRecursiveAdd(GN);
		c_gridNodesTopLevel.add(GN);
		if(GN.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY){
			topLevelElectricGridCapacity_kW +=GN.p_capacity_kW;
			if(!GN.p_realCapacityAvailable){
				topLevelGridCapacitiesKnown = false;
			}
		}
	} else {
		c_gridNodesNotTopLevel.add(GN);	
		if (GN.p_gridNodeID.equals(parentNode.p_parentNodeID)) {
			traceln("Throwing exception because of circular dependency between gridNodes! GridNode %s and parentNode %s", GN.p_gridNodeID, parentNode.p_gridNodeID);
			throw new RuntimeException("Exception: circular GridNode dependency, only tree-topology supported");
		}
	}
}
c_gridNodeExecutionListReverse = c_gridNodeExecutionList;
Collections.reverse(c_gridNodeExecutionList);

//Set cumulative toplevel grid values as energyModel values
v_liveConnectionMetaData.physicalCapacity_kW = topLevelElectricGridCapacity_kW;
v_liveConnectionMetaData.contractedDeliveryCapacity_kW = topLevelElectricGridCapacity_kW;
v_liveConnectionMetaData.contractedFeedinCapacity_kW = topLevelElectricGridCapacity_kW;
v_liveConnectionMetaData.physicalCapacityKnown = topLevelGridCapacitiesKnown;
v_liveConnectionMetaData.contractedDeliveryCapacityKnown = topLevelGridCapacitiesKnown;
v_liveConnectionMetaData.contractedFeedinCapacityKnown = topLevelGridCapacitiesKnown;

//traceln("Grid Node execution list: %s", c_gridNodeExecutionList );
/*ALCODEEND*/}

double f_gridNodeRecursiveAdd(GridNode GN)
{/*ALCODESTART::1716886716306*/
c_gridNodeExecutionList.add(GN);
for (GridNode GNchild : GN.c_connectedGridNodes) {
	f_gridNodeRecursiveAdd(GNchild);
}


/*ALCODEEND*/}

ArrayList<GridConnection> f_getActiveGridConnections()
{/*ALCODESTART::1716890117265*/
ArrayList<GridConnection> copyOfGridConnectionList = new ArrayList<>(c_gridConnections);
copyOfGridConnectionList.removeAll(Arrays.asList(pop_gridConnections)); // Remove all default gridconnections (no flex control, only used for gridnode profile)
return copyOfGridConnectionList;

/*ALCODEEND*/}

double f_initializeEngine()
{/*ALCODESTART::1716893898501*/
// What if this function is accidently called twice? Need to start with a clean sheet?
if (b_isInitialized) {
	throw new RuntimeException("Error: Engine was initalized a second time.");
}
// Initialize time and date
//v_hourOfYearStart=hourOfYearPerMonth[getMonth()] + (getDayOfMonth()-1)*24;
t_h = p_runStartTime_h;

LocalDate localDate = LocalDate.of(p_year, 1, 1);
v_dayOfWeek1jan = DayOfWeek.from(localDate).getValue();
p_startOfWinterWeek_h = roundToInt(24 * (p_winterWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 49 is winterweek.
p_startOfSummerWeek_h = roundToInt(24 * (p_summerWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 18 is summerweek.


Date startDate = date();

startDate.setYear(p_year-1900);

int monthIdx = 0;
while ( t_h > hourOfYearPerMonth[monthIdx] ) {
	monthIdx++;
	if (monthIdx==hourOfYearPerMonth.length){
		break;
	}	
}

int dayOfMonth = 1+(int)((t_h - hourOfYearPerMonth[monthIdx])/24.0);
traceln("Day of month start: %s", dayOfMonth);
traceln("Month of year start: %s", monthIdx);
startDate.setMonth(monthIdx);
startDate.setDate(dayOfMonth);
traceln("Startdate: %s", startDate);
//startDate.set
getExperiment().getEngine().setStartDate(startDate); 


//traceln("Day of the week on january 1st %s: %s, int value: %s", p_year, DayOfWeek.from(localDate).name(), v_dayOfWeek1jan);

// Initialize all agents in the correct order, creating all connections. What about setting initial values? And how about repeated simulations?

f_buildGridNodeTree();
c_gridConnections.forEach(GC -> GC.f_initialize());

pop_connectionOwners.forEach(CO -> CO.f_initialize());
pop_energyCoops.forEach(EC -> EC.f_initialize()); // Not yet robust when there is no supplier initialized!



// Initializing Live Data Class
v_liveAssetsMetaData.updateActiveAssetData(c_gridConnections);
v_liveData.activeConsumptionEnergyCarriers = v_activeConsumptionEnergyCarriers;
v_liveData.activeProductionEnergyCarriers = v_activeProductionEnergyCarriers;
v_liveData.activeEnergyCarriers = v_activeEnergyCarriers;

// Loop over populations to check v_ispaused
f_initializePause();

for (GridNode GN : c_gridNodeExecutionList) {
	GN.f_initializeGridnode();
}

v_liveData.connectionMetaData.contractedDeliveryCapacityKnown = false;
v_liveData.connectionMetaData.contractedFeedinCapacityKnown = false;
v_liveData.connectionMetaData.physicalCapacityKnown = false;

f_initializeForecasts();

f_initializeLiveDataSets();

//f_initializeAccumulators();

// Use parallelisation?
if (c_gridConnections.size() > 100) {
	b_parallelizeGridConnections = true;
}
if (pop_connectionOwners.size() > 500 && b_parallelizeGridConnections) {
	b_parallelizeConnectionOwners = true;
}

// set initial values
f_setInitialValues();


b_isInitialized = true;
/*ALCODEEND*/}

ArrayList<ConnectionOwner> f_getConnectionOwners()
{/*ALCODESTART::1716897568717*/
return c_connectionOwners;
/*ALCODEEND*/}

double f_getTopLevelGridCapacity_kW()
{/*ALCODESTART::1716899946694*/
return v_liveConnectionMetaData.physicalCapacity_kW;
/*ALCODEEND*/}

ArrayList<J_EA> f_getEnergyAssets()
{/*ALCODESTART::1717058801652*/
return c_energyAssets;
/*ALCODEEND*/}

ArrayList<GridNode> f_getGridNodesTopLevel()
{/*ALCODESTART::1718289616227*/
return this.c_gridNodesTopLevel;
/*ALCODEEND*/}

ArrayList<GridNode> f_getGridNodesNotTopLevel()
{/*ALCODESTART::1718289761647*/
return this.c_gridNodesNotTopLevel;
/*ALCODEEND*/}

double f_setInitialValues()
{/*ALCODESTART::1722853692644*/
// Starting prices
c_gridConnections.forEach(GC -> GC.v_electricityPriceLowPassed_eurpkWh = c_gridNodesTopLevel.get(0).v_currentParentNodalPrice_eurpkWh); // Initialize filtered prices for gridConnections, hoping to prevent or reduce initial settling excursions


/*ALCODEEND*/}

double f_initializePause()
{/*ALCODESTART::1722590514591*/
for (GridConnection GC : EnergyProductionSites) {
	if (!GC.v_isActive) {
		GC.f_setActive(false);
	}
}
for (GridConnection GC : EnergyConversionSites) {
	if (!GC.v_isActive) {
		GC.f_setActive(false);
	}
}
for (GridConnection GC : GridBatteries) {
	if (!GC.v_isActive) {
		GC.f_setActive(false);
	}
}
for (GridConnection GC : PublicChargers) {
	if (!GC.v_isActive) {
		GC.f_setActive(false);
	}
}
/*ALCODEEND*/}

double f_writeGridNodeTimeseriesToExcel()
{/*ALCODESTART::1724575401400*/
traceln("Start writing trafoloads to excel!");

int columnIndex = 2;
//int rowIndex = roundToInt(4 * t_h ) + 2;

int arraySize = v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length;
traceln("ArraySize: %s", arraySize);
for (int i = 0; i < arraySize ; i++) {
	p_gridNodeTimeSeriesExcel.setCellValue((i) * p_timeStep_h, "TrafoData", i+2, 1);
}

for(GridNode gn : pop_gridNodes){
	p_gridNodeTimeSeriesExcel.setCellValue(gn.p_gridNodeID, "TrafoData", 1, columnIndex);
	double[] loadArray = gn.acc_annualElectricityBalance_kW.getTimeSeries_kW();
	for (int i = 0; i < arraySize ; i++ ) {		
		p_gridNodeTimeSeriesExcel.setCellValue(loadArray[i], "TrafoData", i+2, columnIndex);
	}
	columnIndex++;
}

//p_gridNodeTimeSeriesExcel.writeFile(force);;

traceln("Finished writing trafoloads to excel!");
/*ALCODEEND*/}

double f_addProfile(J_ProfilePointer profile)
{/*ALCODESTART::1727106160366*/
c_profiles.add(profile);
/*ALCODEEND*/}

ArrayList<GridConnection> f_getPausedGridConnections()
{/*ALCODESTART::1727167397666*/
return c_pausedGridConnections;
/*ALCODEEND*/}

J_ProfilePointer f_findProfile(String assetName)
{/*ALCODESTART::1727193246625*/
J_ProfilePointer profilePointer = findFirst(c_profiles, p -> p.name.equals(assetName));
//traceln("J_EAConsumption with name %s found profile asset: %s", assetName, profilePointer);
if (profilePointer == null) {    		
	throw new RuntimeException(String.format("Consumption or production asset without valid profile!") );
}
return profilePointer;
/*ALCODEEND*/}

double f_updateLiveData()
{/*ALCODESTART::1731329529733*/
//Current time
double currentTime_h = t_h-p_runStartTime_h;

//Energy carrier flows
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
	v_liveData.dsm_liveDemand_kW.get(EC).add( currentTime_h, roundToDecimal(fm_currentConsumptionFlows_kW.get(EC), 3));
}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
	v_liveData.dsm_liveSupply_kW.get(EC).add( currentTime_h, roundToDecimal(fm_currentProductionFlows_kW.get(EC) , 3));
}

//Totals
v_liveData.data_totalDemand_kW.add(currentTime_h, v_currentFinalEnergyConsumption_kW);
v_liveData.data_totalSupply_kW.add(currentTime_h, v_currentPrimaryEnergyProduction_kW);
v_liveData.data_liveElectricityBalance_kW.add(currentTime_h, sum(c_gridNodesTopLevel.stream().filter(x -> x.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY).toList(), x -> x.v_currentLoad_kW));

//Grid capacity
v_liveData.data_gridCapacityDemand_kW.add(currentTime_h, v_liveConnectionMetaData.physicalCapacity_kW);
v_liveData.data_gridCapacitySupply_kW.add(currentTime_h, -v_liveConnectionMetaData.physicalCapacity_kW);

////Specific assets

//Demand

//Base load electricity
v_liveData.data_baseloadElectricityDemand_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x->x.v_fixedConsumptionElectric_kW), 3));

//Heatpump consumption (electric)
v_liveData.data_heatPumpElectricityDemand_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x->x.v_heatPumpElectricityConsumption_kW), 3));

//Hydrogen electricity consumption
v_liveData.data_hydrogenElectricityDemand_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x->x.v_hydrogenElectricityConsumption_kW), 3));

//EV chargings
v_liveData.data_electricVehicleDemand_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x -> max(0,x.v_evChargingPowerElectric_kW)), 3));

//Battery charging
v_liveData.data_batteryCharging_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x -> max(0, x.v_batteryPowerElectric_kW)), 3));

//Electric Cooking 
v_liveData.data_cookingElectricityDemand_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x-> x.v_electricHobConsumption_kW), 3));

//District heating
v_liveData.data_districtHeatDelivery_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x -> x.v_districtHeatDelivery_kW), 3));

//Supply

//PV
v_liveData.data_PVGeneration_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x->x.v_pvProductionElectric_kW), 3));

//Wind
v_liveData.data_windGeneration_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections,x->x.v_windProductionElectric_kW), 3));

//PT
v_liveData.data_PTGeneration_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x->x.v_ptProductionHeat_kW), 3));

//Battery discharge
v_liveData.data_batteryDischarging_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections, x -> max(0, -x.v_batteryPowerElectric_kW)), 3));

//V2G
v_liveData.data_V2GSupply_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections,x->max(0,-x.v_evChargingPowerElectric_kW)), 3));

//CHP
v_liveData.data_CHPElectricityProductionLiveWeek_kW.add(currentTime_h, roundToDecimal(sum(c_gridConnections,x->x.v_CHPProductionElectric_kW), 3));

//Other

//Battery storage
double currentBatteryStoredEnergy_MWh = sum(c_gridConnections, x->x.v_batteryStoredEnergy_kWh/1000);
v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(currentTime_h, currentBatteryStoredEnergy_MWh);

double currentSOC = 0;
if(v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
	currentSOC = currentBatteryStoredEnergy_MWh/v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
}
v_liveData.data_batterySOC_fr.add(currentTime_h, roundToDecimal(currentSOC, 3));

/*ALCODEEND*/}

double f_initializeLiveDataSets()
{/*ALCODESTART::1731573713521*/
v_liveData.dsm_liveDemand_kW.createEmptyDataSets(v_activeEnergyCarriers, (int) (168 / p_timeStep_h));
v_liveData.dsm_liveSupply_kW.createEmptyDataSets(v_activeEnergyCarriers, (int) (168 / p_timeStep_h));
/*ALCODEEND*/}

EnergyCoop f_addEnergyCoop(ArrayList<GridConnection> gcList)
{/*ALCODESTART::1739958854535*/
// Add energyCoop
EnergyCoop energyCoop = add_pop_energyCoops();
energyCoop.p_actorID = "Custom Coop for filtered GC list";
// Connect GCs, connectionOwners and energyCoop and gather data
/*for(GridConnection gc : gcList) {
	if(gc.p_owner == null) {
		throw new RuntimeException("Can't add gridConnection without a connectionOwner to EnergyCoop!");
	} else {
		gc.p_owner.p_actorGroup = "member";
		gc.p_owner.p_coopParent = energyCoop;
		gc.p_owner.f_initialize();
	}
}*/
// Initialisation, collecting data and calculating KPIs.
energyCoop.f_initializeCustomCoop(gcList);

// Adding this coop to the list of coops in the GC
gcList.forEach(gc -> gc.c_parentCoops.add(energyCoop));

// Return energyCoop to caller 
return energyCoop;
/*ALCODEEND*/}

EnergyCoop f_removeEnergyCoop(EnergyCoop energyCoop)
{/*ALCODESTART::1739972940581*/
// Connect GCs, connectionOwners and energyCoop and gather data
for(Agent CO : energyCoop.c_coopCustomers){
	if(CO instanceof ConnectionOwner){
		((ConnectionOwner)CO).p_coopParent = null;
		((ConnectionOwner)CO).f_initialize();	
	}
}

for(Agent CO : energyCoop.c_coopMembers){
	if(CO instanceof ConnectionOwner){
		((ConnectionOwner)CO).p_coopParent = null;
		((ConnectionOwner)CO).f_initialize();	
	}
}

// Removing this coop from the list of coops in the GC
for (GridConnection GC : energyCoop.f_getAllChildMemberGridConnections()) {
	GC.c_parentCoops.remove(energyCoop);
	if(GC instanceof GCGridBattery && GC.p_batteryAlgorithm instanceof J_BatteryManagementPeakShaving && ((J_BatteryManagementPeakShaving)GC.p_batteryAlgorithm).getTargetType() == OL_ResultScope.ENERGYCOOP){
		((J_BatteryManagementPeakShaving)GC.p_batteryAlgorithm).setTarget(null);
		((J_BatteryManagementPeakShaving)GC.p_batteryAlgorithm).setTargetType( OL_ResultScope.ENERGYCOOP );
		GC.f_setActive(false);
	}
}

// Remove energyCoop from pop_energyCoops.
remove_pop_energyCoops(energyCoop);


/*ALCODEEND*/}

EnergyCoop f_addConsumptionEnergyCarrier(OL_EnergyCarriers EC)
{/*ALCODESTART::1740056275008*/
v_activeEnergyCarriers.add(EC);
v_activeConsumptionEnergyCarriers.add(EC);

DataSet dsDemand = new DataSet( (int)(168 / p_timeStep_h) );

double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
for (double t = startTime; t <= endTime; t += p_timeStep_h) {
	dsDemand.add( t, 0);
}
v_liveData.dsm_liveDemand_kW.put( EC, dsDemand);
/*ALCODEEND*/}

double f_rapidRunDataLogging()
{/*ALCODESTART::1741622740564*/
// Further Subdivision of asset types within energy carriers
double v_fixedConsumptionElectric_kW = sum(c_gridConnections, x->x.v_fixedConsumptionElectric_kW);
double v_heatPumpElectricityConsumption_kW = sum(c_gridConnections, x->x.v_heatPumpElectricityConsumption_kW);
double v_evChargingPowerElectric_kW = sum(c_gridConnections, x->max(0,x.v_evChargingPowerElectric_kW));
double currentBatteriesConsumption_kW = sum(c_gridConnections, x->max(0,x.v_batteryPowerElectric_kW));
double v_hydrogenElectricityConsumption_kW = sum(c_gridConnections, x->x.v_hydrogenElectricityConsumption_kW);
double v_electricHobConsumption_kW = sum(c_gridConnections, x->x.v_electricHobConsumption_kW);
double v_districtHeatDelivery_kW = sum(c_gridConnections, x->x.v_districtHeatDelivery_kW);

double v_pvProductionElectric_kW = sum(c_gridConnections, x->x.v_pvProductionElectric_kW);
double v_windProductionElectric_kW = sum(c_gridConnections, x->x.v_windProductionElectric_kW);
double v_ptProductionHeat_kW = sum(c_gridConnections, x->x.v_ptProductionHeat_kW);
double currentBatteriesProduction_kW = sum(c_gridConnections, x->max(0,-x.v_batteryPowerElectric_kW));
double currentV2GProduction_kW = sum(c_gridConnections, x-> max(0, -x.v_evChargingPowerElectric_kW));
double v_CHPProductionElectric_kW = sum(c_gridConnections, x->x.v_CHPProductionElectric_kW);

double currentStoredEnergyBatteries_MWh = sum(c_gridConnections, x->x.v_batteryStoredEnergy_kWh)/1000;

//v_maxConnectionLoad_fr = max(v_maxConnectionLoad_fr, abs(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) / p_contractedDeliveryCapacity_kW ));

//double currentImport_kW = 0.0;
//double currentExport_kW = 0.0;
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
	//currentImport_kW += max( 0, currentBalance_kW );
	//currentExport_kW += max( 0, -currentBalance_kW );
	v_rapidRunData.am_totalBalanceAccumulators_kW.get(EC).addStep(  currentBalance_kW );
}

// Daytime totals. Use overal-total minus daytime total to get nighttime totals.
if(b_isDaytime) { 
	
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		
		if(v_activeConsumptionEnergyCarriers.contains(EC)){
			v_rapidRunData.am_daytimeImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		}
		if(v_activeProductionEnergyCarriers.contains(EC)){
			v_rapidRunData.am_daytimeExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
		}
	}
	
	v_rapidRunData.acc_daytimeElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_daytimeElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );	
	v_rapidRunData.acc_daytimeEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_daytimeFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);	
	v_rapidRunData.acc_daytimePrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

}

// Weekend totals. Use overal-totals minus weekend totals to get weekday totals.
if (!b_isWeekday) { // 
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		if(v_activeConsumptionEnergyCarriers.contains(EC)){
			v_rapidRunData.am_weekendImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		}
		if(v_activeProductionEnergyCarriers.contains(EC)){
			v_rapidRunData.am_weekendExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
		}
	}
	
	v_rapidRunData.acc_weekendElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_weekendElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_weekendEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_weekendFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
	v_rapidRunData.acc_weekendPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

}


//========== SUMMER WEEK ==========//
if (b_isSummerWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		v_rapidRunData.am_summerWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
		v_rapidRunData.am_summerWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );

	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
		v_rapidRunData.am_summerWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
	}
			
	v_rapidRunData.acc_summerWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_summerWeekFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

	v_rapidRunData.acc_summerWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

	v_rapidRunData.acc_summerWeekDeliveryCapacity_kW.addStep( v_liveConnectionMetaData.physicalCapacity_kW );
	v_rapidRunData.acc_summerWeekFeedinCapacity_kW.addStep( -v_liveConnectionMetaData.physicalCapacity_kW );
	
	v_rapidRunData.acc_summerWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	v_rapidRunData.acc_summerWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	v_rapidRunData.acc_summerWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekBatteriesConsumption_kW.addStep( currentBatteriesConsumption_kW );
	v_rapidRunData.acc_summerWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	v_rapidRunData.acc_summerWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	v_rapidRunData.acc_summerWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	v_rapidRunData.acc_summerWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	v_rapidRunData.acc_summerWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	v_rapidRunData.acc_summerWeekPTProduction_kW.addStep( v_ptProductionHeat_kW );	
	v_rapidRunData.acc_summerWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekBatteriesProduction_kW.addStep( currentBatteriesProduction_kW );
	v_rapidRunData.acc_summerWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );

	v_rapidRunData.ts_summerWeekBatteriesStoredEnergy_MWh.addStep(currentStoredEnergyBatteries_MWh);
	if(v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
		v_rapidRunData.ts_summerWeekBatteriesSOC_fr.addStep(currentStoredEnergyBatteries_MWh/v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
	}
	else{
		v_rapidRunData.ts_summerWeekBatteriesSOC_fr.addStep(0);	
	}
}

//========== WINTER WEEK ==========// 
if (b_isWinterWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		v_rapidRunData.am_winterWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
	    v_rapidRunData.am_winterWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
	    v_rapidRunData.am_winterWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
	}
	
	v_rapidRunData.acc_winterWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_winterWeekFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
	
	v_rapidRunData.acc_winterWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	
	
	v_rapidRunData.acc_winterWeekDeliveryCapacity_kW.addStep( v_liveConnectionMetaData.physicalCapacity_kW );
	v_rapidRunData.acc_winterWeekFeedinCapacity_kW.addStep( -v_liveConnectionMetaData.physicalCapacity_kW );
	
	v_rapidRunData.acc_winterWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	v_rapidRunData.acc_winterWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	v_rapidRunData.acc_winterWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekBatteriesConsumption_kW.addStep(currentBatteriesConsumption_kW );
	v_rapidRunData.acc_winterWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	v_rapidRunData.acc_winterWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	v_rapidRunData.acc_winterWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	v_rapidRunData.acc_winterWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	v_rapidRunData.acc_winterWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	v_rapidRunData.acc_winterWeekPTProduction_kW.addStep( v_ptProductionHeat_kW );	
	v_rapidRunData.acc_winterWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekBatteriesProduction_kW.addStep( currentBatteriesProduction_kW );
	v_rapidRunData.acc_winterWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );

	v_rapidRunData.ts_winterWeekBatteriesStoredEnergy_MWh.addStep(currentStoredEnergyBatteries_MWh);
	if(v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
		v_rapidRunData.ts_winterWeekBatteriesSOC_fr.addStep(currentStoredEnergyBatteries_MWh/v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
	}		
	else{
		v_rapidRunData.ts_winterWeekBatteriesSOC_fr.addStep(0);	
	}
}


//========== TOTALS / DAILY AVERAGES ==========//
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
    v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
    v_rapidRunData.am_dailyAverageProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
}
v_rapidRunData.acc_dailyAverageEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
v_rapidRunData.acc_dailyAverageFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
v_rapidRunData.acc_totalEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);

v_rapidRunData.acc_dailyAverageBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
v_rapidRunData.acc_dailyAverageHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
v_rapidRunData.acc_dailyAverageElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesConsumption_kW.addStep( currentBatteriesConsumption_kW );
v_rapidRunData.acc_dailyAverageElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
v_rapidRunData.acc_dailyAverageElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
v_rapidRunData.acc_dailyAverageDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );

v_rapidRunData.acc_dailyAveragePVProduction_kW.addStep( v_pvProductionElectric_kW );
v_rapidRunData.acc_dailyAverageWindProduction_kW.addStep( v_windProductionElectric_kW );
v_rapidRunData.acc_dailyAveragePTProduction_kW.addStep( v_ptProductionHeat_kW );
v_rapidRunData.acc_dailyAverageV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesProduction_kW.addStep( currentBatteriesProduction_kW );
v_rapidRunData.acc_dailyAverageCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );

v_rapidRunData.ts_dailyAverageBatteriesStoredEnergy_MWh.addStep(currentStoredEnergyBatteries_MWh);
if(v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
	v_rapidRunData.ts_dailyAverageBatteriesSOC_fr.addStep(currentStoredEnergyBatteries_MWh/v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
}
else{
	v_rapidRunData.ts_dailyAverageBatteriesSOC_fr.addStep(0);	
}	

/*ALCODEEND*/}

double f_updateActiveAssetsMetaData()
{/*ALCODESTART::1741710906926*/

//Update main area
v_liveAssetsMetaData.updateActiveAssetData(f_getActiveGridConnections());

//Update coop
if(pop_energyCoops.size()>0){
	pop_energyCoops.get(pop_energyCoops.size()-1).v_liveAssetsMetaData.updateActiveAssetData(pop_energyCoops.get(pop_energyCoops.size()-1).f_getAllChildMemberGridConnections());
}	

//Update grid connection active asset data
for(GridConnection GC : f_getActiveGridConnections()){
	GC.v_liveAssetsMetaData.updateActiveAssetData(new ArrayList<>(List.of(GC)));
}

/*ALCODEEND*/}

EnergyCoop f_addProductionEnergyCarrier(OL_EnergyCarriers EC)
{/*ALCODESTART::1746021439807*/
v_activeEnergyCarriers.add(EC);
v_activeProductionEnergyCarriers.add(EC);

DataSet dsSupply = new DataSet( (int)(168 / p_timeStep_h) );
double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
for (double t = startTime; t <= endTime; t += p_timeStep_h) {
	dsSupply.add( t, 0);
}
v_liveData.dsm_liveSupply_kW.put( EC, dsSupply);
/*ALCODEEND*/}

double f_updateLiveData1()
{/*ALCODESTART::1751294470738*/
//Current time
double currentTime_h = t_h-p_runStartTime_h;

//Energy carrier flows
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
	v_liveData.dsm_liveDemand_kW.get(EC).add( currentTime_h, fm_currentConsumptionFlows_kW.get(EC) );
}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
	v_liveData.dsm_liveSupply_kW.get(EC).add( currentTime_h, fm_currentProductionFlows_kW.get(EC) );
}

//Totals
v_liveData.data_totalDemand_kW.add(currentTime_h, v_currentFinalEnergyConsumption_kW);
v_liveData.data_totalSupply_kW.add(currentTime_h, v_currentPrimaryEnergyProduction_kW);
v_liveData.data_liveElectricityBalance_kW.add(currentTime_h, sum(c_gridNodesTopLevel.stream().filter(x -> x.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY).toList(), x -> x.v_currentLoad_kW));

//Grid capacity
v_liveData.data_gridCapacityDemand_kW.add(currentTime_h, v_liveConnectionMetaData.physicalCapacity_kW);
v_liveData.data_gridCapacitySupply_kW.add(currentTime_h, -v_liveConnectionMetaData.physicalCapacity_kW);

////Specific assets

//Demand

//Base load electricity
v_liveData.data_baseloadElectricityDemand_kW.add(currentTime_h, sum(c_gridConnections, x->x.v_fixedConsumptionElectric_kW));

//Heatpump consumption (electric)
v_liveData.data_heatPumpElectricityDemand_kW.add(currentTime_h, sum(c_gridConnections, x->x.v_heatPumpElectricityConsumption_kW));

//Hydrogen electricity consumption
v_liveData.data_hydrogenElectricityDemand_kW.add(currentTime_h, sum(c_gridConnections, x->x.v_hydrogenElectricityConsumption_kW));

//EV chargings
v_liveData.data_electricVehicleDemand_kW.add(currentTime_h, sum(c_gridConnections, x -> max(0,x.v_evChargingPowerElectric_kW)));

//Battery charging
v_liveData.data_batteryCharging_kW.add(currentTime_h, sum(c_gridConnections, x -> max(0, x.v_batteryPowerElectric_kW)));

//Electric Cooking 
v_liveData.data_cookingElectricityDemand_kW.add(currentTime_h, sum(c_gridConnections, x-> x.v_electricHobConsumption_kW));

//District heating
v_liveData.data_districtHeatDelivery_kW.add(currentTime_h, sum(c_gridConnections, x -> x.v_districtHeatDelivery_kW));

//Supply

//PV
v_liveData.data_PVGeneration_kW.add(currentTime_h, sum(c_gridConnections, x->x.v_pvProductionElectric_kW));

//Wind
v_liveData.data_windGeneration_kW.add(currentTime_h, sum(c_gridConnections,x->x.v_windProductionElectric_kW));

//Battery discharge
v_liveData.data_batteryDischarging_kW.add(currentTime_h, sum(c_gridConnections, x -> max(0, -x.v_batteryPowerElectric_kW)));

//V2G
v_liveData.data_V2GSupply_kW.add(currentTime_h, sum(c_gridConnections,x->max(0,-x.v_evChargingPowerElectric_kW)));

//CHP
v_liveData.data_CHPElectricityProductionLiveWeek_kW.add(currentTime_h, sum(c_gridConnections,x->x.v_CHPProductionElectric_kW));

//Other

//Battery storage
double currentBatteryStoredEnergy_MWh = sum(c_gridConnections, x->x.v_batteryStoredEnergy_kWh/1000);
v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(currentTime_h, currentBatteryStoredEnergy_MWh);

double currentSOC = 0;
if(v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
	currentSOC = currentBatteryStoredEnergy_MWh/v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
}
v_liveData.data_batterySOC_fr.add(currentTime_h, currentSOC);

/*ALCODEEND*/}

double f_updateAmbientDependentAssets()
{/*ALCODESTART::1751886925823*/
// Update environmental conditions for relevant energy assets
for( J_EA e : c_ambientDependentAssets ) {
	if( e instanceof J_EAStorageHeat) { // includes J_EABuilding
		switch(((J_EAStorageHeat) e).getAmbientTempType()){
			case FIXED:
				//Do nothing, use preset ambient temp
				break;
			case AMBIENT_AIR:
				((J_EAStorageHeat)e).updateAmbientTemperature( v_currentAmbientTemperature_degC );
				break;
			case BUILDING:
				new RuntimeException("AmbientTempType 'BUILDING' is not supported yet for J_EAStorageHeat!");
				/*
				GridConnection parentGC = (GridConnection)e.getParentAgent();
				if(parentGC.p_BuildingThermalAsset == null){
					new RuntimeException("GC has heat storage with AmbientTempType 'Building', with no J_EABuilding present");
				}
				else{
					((J_EAStorageHeat)e).updateAmbientTemperature(parentGC.p_BuildingThermalAsset.getCurrentTemperature());
				}
				*/
				break;
			case HEAT_GRID:
				// Do Nothing, keep fixed temp for now
				//new RuntimeException("AmbientTempType 'HEAT_GRID' is not supported yet for J_EAStorageHeat!");
				break;
			case HEAT_STORAGE:
				new RuntimeException("AmbientTempType 'HEAT_STORAGE' is not supported yet for J_EAStorageHeat!");
				break;
		}	
	}
	if (e instanceof J_EAConversionHeatPump) {
			switch(((J_EAConversionHeatPump) e).getAmbientTempType()){
			case FIXED:
				//Do nothing, use preset ambient temp
				break;
			case AMBIENT_AIR:
				((J_EAConversionHeatPump)e).updateAmbientTemperature( v_currentAmbientTemperature_degC );
				break;
			case BUILDING:
				new RuntimeException("AmbientTempType 'BUILDING' is not supported yet for J_EAConversionHeatPump!");
				/*
				GridConnection parentGC = (GridConnection)e.getParentAgent();
				if(parentGC.p_BuildingThermalAsset == null){
					new RuntimeException("GC has heatpump with AmbientTempType 'Building', with no J_EABuilding present");
				}
				else{
					((J_EAConversionHeatPump)e).updateAmbientTemperature(parentGC.p_BuildingThermalAsset.getCurrentTemperature());
				}
				*/
				break;
			case HEAT_GRID:
				new RuntimeException("AmbientTempType 'HEAT_GRID' is not supported yet for J_EAConversionHeatPump!");
				break;
			case HEAT_STORAGE:
				new RuntimeException("AmbientTempType 'HEAT_STORAGE' is not supported yet for J_EAConversionHeatPump!");
				break;
			}		
	}
	if( e instanceof J_EABuilding ) {
		((J_EABuilding)e).updateSolarRadiation(v_currentSolarPowerNormalized_r*1000);
	}
}
/*ALCODEEND*/}

