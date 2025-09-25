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

c_profiles.forEach(p -> p.updateValue(t_h));
//v_currentAmbientTemperature_degC = pp_ambientTemperature_degC.getCurrentValue();
//v_currentWindPowerNormalized_r = pp_windProduction_fr.getCurrentValue();
//v_currentSolarPowerNormalized_r = pp_PVProduction35DegSouth_fr.getCurrentValue();
//v_currentCookingDemand_fr = tf_cooking_demand(t_h);

if (b_enableDLR) {
	v_currentDLRfactor_fr = 1 + max(-0.1,pp_windProduction_fr.getCurrentValue() * 0.025*(30-pp_ambientTemperature_degC.getCurrentValue()) + 0.5 - pp_PVProduction35DegSouth_fr.getCurrentValue());
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
//v_SolarYieldForecast_fr = pf_PVProduction35DegSouth_fr.getForecast();
//v_WindYieldForecast_fr = pf_windProduction_fr.getForecast();
// The ElectricityYieldForecast assumes solar and wind forecasts have the same forecast time
if ( v_liveAssetsMetaData.totalInstalledPVPower_kW + v_liveAssetsMetaData.totalInstalledWindPower_kW > 0 ) {
	v_electricityYieldForecast_fr = (pf_PVProduction35DegSouth_fr.getForecast() * v_liveAssetsMetaData.totalInstalledPVPower_kW + pf_windProduction_fr.getForecast() * v_liveAssetsMetaData.totalInstalledWindPower_kW) / (v_liveAssetsMetaData.totalInstalledPVPower_kW + v_liveAssetsMetaData.totalInstalledWindPower_kW);
}
// And price forecast! 
//v_epexForecast_eurpkWh = 0.001*pf_dayAheadElectricityPricing_eurpMWh.getForecast();

for (GridNode GN : c_gridNodeExecutionList) {
	GN.f_updateForecasts();
}


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

double f_sumBatteryUse()
{/*ALCODESTART::1666978595555*/
v_totalBatteryDischargeAmount_MWh = 0;
v_totalBatteryChargeAmount_MWh = 0;
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

//Calculate delta stored energy in battery for energy balance check
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
fm_currentAssetFlows_kW.clear();

v_currentFinalEnergyConsumption_kW = 0;
v_currentPrimaryEnergyProduction_kW = 0;
v_currentEnergyCurtailed_kW = 0;
v_currentPrimaryEnergyProductionHeatpumps_kW = 0;
v_batteryStoredEnergy_kWh = 0;

if (b_parallelizeGridConnections) {
	c_gridConnections.parallelStream().forEach(gc -> gc.f_calculateEnergyBalance());
} 
else {
	for(GridConnection gc : c_gridConnections) {
		gc.f_calculateEnergyBalance();
	}
}

//OLD LOCATION OF AGGREGATOR CALL, NECESSARY IF MANAGEMENT IS WITHOUT TIME STEP DELAY

for(GridConnection gc : c_gridConnections) { // Can't do this in parallel due to different threads writing to the same values!
	
	fm_currentBalanceFlows_kW.addFlows(gc.fm_currentBalanceFlows_kW);
	fm_currentProductionFlows_kW.addFlows(gc.fm_currentProductionFlows_kW);
	fm_currentConsumptionFlows_kW.addFlows(gc.fm_currentConsumptionFlows_kW);
	fm_currentAssetFlows_kW.addFlows(gc.fm_currentAssetFlows_kW);
	v_currentFinalEnergyConsumption_kW += gc.v_currentFinalEnergyConsumption_kW;
	v_currentPrimaryEnergyProduction_kW += gc.v_currentPrimaryEnergyProduction_kW;
	v_currentEnergyCurtailed_kW += gc.v_currentEnergyCurtailed_kW;
	v_batteryStoredEnergy_kWh += gc.v_batteryStoredEnergy_kWh;
	v_currentPrimaryEnergyProductionHeatpumps_kW += gc.v_currentPrimaryEnergyProductionHeatpumps_kW;
		
}

for (GridConnection gc : c_subGridConnections) {
	gc.f_calculateEnergyBalance();
}

v_currentEnergyImport_kW = 0.0;
v_currentEnergyExport_kW = 0.0;
for (OL_EnergyCarriers EC : v_liveData.activeEnergyCarriers) {
	double netFlow_kW = fm_currentBalanceFlows_kW.get(EC);
	v_currentEnergyImport_kW += max( 0, netFlow_kW );
	v_currentEnergyExport_kW += max( 0, -netFlow_kW );
}

//Call aggregator functions (ONLY WORK WITH TIME STEP DELAY FOR NOW) (LOCATION OF THIS CALL IS NOT DETERMINED YET, FOR NOW HERE)
f_runAggregators();

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


//// Store and reset model states
for (J_EA EA : c_energyAssets) {
	EA.storeStatesAndReset();		
}

for (GridConnection GC : c_gridConnections) {
	
	if (GC.v_rapidRunData != null) {
		if (b_storePreviousRapidRunData) {
			GC.v_previousRunData = GC.v_rapidRunData;
		}
	} 
	GC.v_rapidRunData = new J_RapidRunData(GC);
	GC.v_rapidRunData.assetsMetaData = GC.v_liveAssetsMetaData.getClone();
	GC.v_rapidRunData.connectionMetaData = GC.v_liveConnectionMetaData.getClone();
	GC.v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, GC.v_liveData.activeEnergyCarriers, GC.v_liveData.activeConsumptionEnergyCarriers, GC.v_liveData.activeProductionEnergyCarriers, GC.v_liveAssetsMetaData.activeAssetFlows); //f_initializeAccumulators();
		
	GC.f_resetStates();
	
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
			EC.v_previousRunData = EC.v_rapidRunData;
		}
		/*EC.v_rapidRunData.assetsMetaData = EC.v_liveAssetsMetaData.getClone();
		EC.v_rapidRunData.connectionMetaData = EC.v_liveConnectionMetaData.getClone();
		if(EC.v_rapidRunData.getStoreTotalAssetFlows() == false){
			EC.v_rapidRunData.setStoreTotalAssetFlows(true);
			EC.v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, EC.v_liveData.activeEnergyCarriers, EC.v_liveData.activeConsumptionEnergyCarriers, EC.v_liveData.activeProductionEnergyCarriers, EC.v_liveAssetsMetaData.activeAssetFlows);
		}*/
	} 
	EC.v_rapidRunData = new J_RapidRunData(EC);
	EC.v_rapidRunData.assetsMetaData = EC.v_liveAssetsMetaData.getClone();
	EC.v_rapidRunData.connectionMetaData = EC.v_liveConnectionMetaData.getClone();
	//if(EC.v_rapidRunData.getStoreTotalAssetFlows() == false){
	EC.v_rapidRunData.setStoreTotalAssetFlows(true);
	//}	
	EC.v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, EC.v_liveData.activeEnergyCarriers, EC.v_liveData.activeConsumptionEnergyCarriers, EC.v_liveData.activeProductionEnergyCarriers, EC.v_liveAssetsMetaData.activeAssetFlows);
	EC.f_resetStates();

}


int v_timeStepsElapsed_live = v_timeStepsElapsed;
v_timeStepsElapsed=0;

c_profiles.forEach(p -> p.updateValue(p_runStartTime_h));
c_forecasts.forEach(p -> p.initializeForecast(p_runStartTime_h)); 

if (v_rapidRunData != null && b_storePreviousRapidRunData) {
	v_previousRunData = v_rapidRunData;
}
v_rapidRunData = new J_RapidRunData(this);
v_rapidRunData.assetsMetaData = v_liveAssetsMetaData.getClone();	
v_rapidRunData.connectionMetaData = v_liveConnectionMetaData.getClone();
v_rapidRunData.initializeAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, v_liveData.activeEnergyCarriers, v_liveData.activeConsumptionEnergyCarriers, v_liveData.activeProductionEnergyCarriers, v_liveAssetsMetaData.activeAssetFlows); //f_initializeAccumulators();	
f_resetAnnualValues();

v_isRapidRun = true;

////Run energy calculations loop
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

	//Run rapid data logging
	v_rapidRunData.addTimeStep(fm_currentBalanceFlows_kW, 
								fm_currentConsumptionFlows_kW, 
								fm_currentProductionFlows_kW, 
								fm_currentAssetFlows_kW,
								v_currentPrimaryEnergyProduction_kW, 
								v_currentFinalEnergyConsumption_kW, 
								v_currentPrimaryEnergyProductionHeatpumps_kW, 
								v_currentEnergyCurtailed_kW,
								v_batteryStoredEnergy_kWh/1000, 
								this);
	v_timeStepsElapsed++;
}	



////Caclulate KPIs
double startTime = System.currentTimeMillis();
f_calculateKPIs();

//Perform energy balance check
f_performEnergyBalanceCheck();

v_kpiCalcsRuntime_ms = (System.currentTimeMillis()-startTime);


traceln("---FINISHED YEAR MODEL RUN----");


////Return model to previous state to continue simulation run
v_timeStepsElapsed = v_timeStepsElapsed_live;
t_h = p_runStartTime_h + v_timeStepsElapsed * p_timeStep_h;

for (J_EA EA : c_energyAssets) {
	EA.restoreStates();		
}
/*for (GridNode GN : pop_gridNodes) {
	//Has no reset states
}*/
for (GridConnection GC : c_gridConnections) {
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

v_isRapidRun = false;

double duration = System.currentTimeMillis() - startTime1;

traceln("*** headless run duration: "+ duration/1000 + " s ***");

traceln("Live-sim t_h after rapidRun: %s", t_h);
c_profiles.forEach(p -> p.updateValue(t_h)); 
c_forecasts.forEach(p -> p.initializeForecast(t_h)); 

/*ALCODEEND*/}

double f_calculateKPIs()
{/*ALCODESTART::1698922757486*/
pop_gridNodes.forEach(gn -> gn.f_calculateKPIs());

pop_energyCoops.forEach(ec -> ec.f_calculateKPIs());

// Totals from accumulators:
v_totalElectricityConsumed_MWh = v_rapidRunData.getTotalElectricityConsumed_MWh();
v_totalElectricityProduced_MWh= v_rapidRunData.getTotalElectricityProduced_MWh();

v_totalEnergyConsumed_MWh = v_rapidRunData.getTotalEnergyConsumed_MWh();
v_totalEnergyProduced_MWh = v_rapidRunData.getTotalEnergyProduced_MWh();
v_totalEnergyImport_MWh = v_rapidRunData.am_totalBalanceAccumulators_kW.totalIntegralPos_kWh()/1000;
v_totalEnergyExport_MWh = -v_rapidRunData.am_totalBalanceAccumulators_kW.totalIntegralNeg_kWh()/1000;
v_totalEnergyCurtailed_MWh = v_rapidRunData.getTotalEnergyCurtailed_MWh();

// Electricity self consumption
v_individualSelfSufficiency_fr = sum(c_gridConnections, gc -> gc.v_rapidRunData.getTotalElectricitySelfConsumed_MWh()) / v_totalElectricityConsumed_MWh;
v_individualSelfConsumption_fr = sum(c_gridConnections, gc -> gc.v_rapidRunData.getTotalElectricitySelfConsumed_MWh()) / v_totalElectricityProduced_MWh;
v_totalElectricitySelfConsumed_MWh = v_rapidRunData.getTotalElectricitySelfConsumed_MWh();
v_collectiveSelfConsumption_fr = v_totalElectricitySelfConsumed_MWh / v_totalElectricityProduced_MWh;

traceln("");
traceln("__--** Totals **--__");
traceln("Energy consumed: "+ v_totalEnergyConsumed_MWh + " MWh");
traceln("Energy produced: "+ v_totalEnergyProduced_MWh + " MWh");
traceln("Energy import: "+ v_totalEnergyImport_MWh + " MWh");
traceln("Energy export: "+ v_totalEnergyExport_MWh + " MWh");



/*ALCODEEND*/}

double f_resetAnnualValues()
{/*ALCODESTART::1699958741073*/
v_rapidRunData.resetAccumulators(p_runEndTime_h - p_runStartTime_h, p_timeStep_h, v_liveData.activeEnergyCarriers, v_liveData.activeConsumptionEnergyCarriers, v_liveData.activeProductionEnergyCarriers); //f_initializeAccumulators();

// Others
acc_totalDLRfactor_f.reset();


/*ALCODEEND*/}

double f_runTimestep()
{/*ALCODESTART::1701162826549*/
if(t_h-p_runStartTime_h!=0.0 && (t_h-p_runStartTime_h) % (p_runEndTime_h - p_runStartTime_h) == 0.0) {
	f_loopSimulation();
}

//Update t_h
t_h = p_runStartTime_h + v_timeStepsElapsed * p_timeStep_h;

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

//Update live data
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

// First clear lists (needed after deserialisation)
c_gridNodeExecutionList.clear();
c_gridNodeExecutionListReverse.clear();
c_gridNodesTopLevel.clear();
c_gridNodesNotTopLevel.clear();
	
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

Date startDate = date();
p_year = startDate.getYear() + 1900;

LocalDate localDate = LocalDate.of(p_year, 1, 1);
v_dayOfWeek1jan = DayOfWeek.from(localDate).getValue();
p_startOfWinterWeek_h = roundToInt(24 * (p_winterWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 49 is winterweek.
p_startOfSummerWeek_h = roundToInt(24 * (p_summerWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 18 is summerweek.



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
startDate.setHours(0);
startDate.setMinutes(0);
traceln("Startdate: %s", startDate);
//startDate.set
getExperiment().getEngine().setStartDate(startDate); 


//traceln("Day of the week on january 1st %s: %s, int value: %s", p_year, DayOfWeek.from(localDate).name(), v_dayOfWeek1jan);

// Initialize all agents in the correct order, creating all connections. What about setting initial values? And how about repeated simulations?

f_buildGridNodeTree();
c_gridConnections.forEach(GC -> GC.f_initialize());

// Only relevant for deserialisation:
c_pausedGridConnections.forEach(GC -> GC.f_initialize());

pop_connectionOwners.forEach(CO -> CO.f_initialize());
pop_energyCoops.forEach(EC -> EC.f_initialize()); // Not yet robust when there is no supplier initialized!



// Initializing Live Data Class
v_liveAssetsMetaData.updateActiveAssetData(c_gridConnections);
for (GridConnection GC : c_gridConnections) {
	v_liveData.activeEnergyCarriers.addAll(GC.v_liveData.activeEnergyCarriers);
	v_liveData.activeConsumptionEnergyCarriers.addAll(GC.v_liveData.activeConsumptionEnergyCarriers);
	v_liveData.activeProductionEnergyCarriers.addAll(GC.v_liveData.activeProductionEnergyCarriers);
}

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

double f_initializePause()
{/*ALCODESTART::1722590514591*/
for (GridConnection GC : UtilityConnections) {
	if (!GC.v_isActive) {
		GC.f_setActive(false);
	}
}
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
	traceln("No profilePointer with name %s found", assetName);
	throw new RuntimeException(String.format("Consumption or production asset without valid profile!") );
}
return profilePointer;
/*ALCODEEND*/}

double f_updateLiveData()
{/*ALCODESTART::1731329529733*/
//Current timestep
double currentTime_h = t_h-p_runStartTime_h;

v_liveData.addTimeStep(currentTime_h,
	fm_currentBalanceFlows_kW,
	fm_currentConsumptionFlows_kW,
	fm_currentProductionFlows_kW,
	fm_currentAssetFlows_kW,
	v_currentPrimaryEnergyProduction_kW, 
	v_currentFinalEnergyConsumption_kW, 
	v_currentPrimaryEnergyProductionHeatpumps_kW, 
	v_currentEnergyCurtailed_kW, 
	v_batteryStoredEnergy_kWh/1000 
);

/*ALCODEEND*/}

double f_initializeLiveDataSets()
{/*ALCODESTART::1731573713521*/
v_liveData.dsm_liveDemand_kW.createEmptyDataSets(v_liveData.activeEnergyCarriers, roundToInt(168/p_timeStep_h));
v_liveData.dsm_liveSupply_kW.createEmptyDataSets(v_liveData.activeEnergyCarriers, roundToInt(168/p_timeStep_h));
v_liveData.dsm_liveAssetFlows_kW.createEmptyDataSets(v_liveData.assetsMetaData.activeAssetFlows, roundToInt(168/p_timeStep_h));
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
if (!v_liveData.activeConsumptionEnergyCarriers.contains(EC)) {
	v_liveData.activeEnergyCarriers.add(EC);
	v_liveData.activeConsumptionEnergyCarriers.add(EC);
	
	DataSet dsDemand = new DataSet( (int)(168 / p_timeStep_h) );
	
	double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	for (double t = startTime; t <= endTime; t += p_timeStep_h) {
		dsDemand.add( t, 0);
	}
	v_liveData.dsm_liveDemand_kW.put( EC, dsDemand);
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
if (!v_liveData.activeProductionEnergyCarriers.contains(EC)) {
	v_liveData.activeEnergyCarriers.add(EC);
	v_liveData.activeProductionEnergyCarriers.add(EC);
	
	DataSet dsSupply = new DataSet( (int)(168 / p_timeStep_h) );
	double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	for (double t = startTime; t <= endTime; t += p_timeStep_h) {
		dsSupply.add( t, 0);
	}
	v_liveData.dsm_liveSupply_kW.put( EC, dsSupply);
}
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
				((J_EAStorageHeat)e).updateAmbientTemperature( pp_ambientTemperature_degC.getCurrentValue() );
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
				((J_EAConversionHeatPump)e).updateAmbientTemperature( pp_ambientTemperature_degC.getCurrentValue() );
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
		//traceln("v_currentSolarPowerNormalized_r: %s", v_currentSolarPowerNormalized_r);
		((J_EABuilding)e).updateSolarRadiation(pp_PVProduction35DegSouth_fr.getCurrentValue()*1000);
	}
}
/*ALCODEEND*/}

double f_performEnergyBalanceCheck()
{/*ALCODESTART::1753350603730*/
//Get battery energy use for balance check
f_sumBatteryUse();

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
			totalEnergyProduced_MWh += ((J_EAStorageHeat)e).energyAbsorbed_kWh/1000;
			deltaThermalEnergySinceStart_MWh += (((J_EAStorageHeat)e).getRemainingHeatStorageHeat_kWh() - ((J_EAStorageHeat)e).getStartingHeatStorageHeat_kWh())/1000;						
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
for (OL_EnergyCarriers EC : v_liveData.activeEnergyCarriers) {
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

double f_startAfterDeserialisation()
{/*ALCODESTART::1753963201170*/
// Reconstruct the LiveData class
/*v_liveData = new J_LiveData(this);
v_liveData.activeEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
v_liveData.activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
v_liveData.activeConsumptionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);

//v_liveConnectionMetaData = new J_ConnectionMetaData(this);
//v_liveAssetsMetaData = new J_AssetsMetaData(this);
v_liveData.connectionMetaData = v_liveConnectionMetaData;
v_liveData.assetsMetaData = v_liveAssetsMetaData;*/

v_liveData.resetLiveDatasets(p_runStartTime_h, p_runStartTime_h, p_timeStep_h);

fm_currentProductionFlows_kW = new J_FlowsMap();
fm_currentConsumptionFlows_kW = new J_FlowsMap();
fm_currentBalanceFlows_kW = new J_FlowsMap();
fm_currentAssetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);

// Reconstruct the LiveData class in the EnergyCoops
for (EnergyCoop ec : pop_energyCoops) {
	/*ec.v_liveData = new J_LiveData(ec);
	ec.v_liveData.activeEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	ec.v_liveData.activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	ec.v_liveData.activeConsumptionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	
	ec.v_liveData.connectionMetaData = ec.v_liveConnectionMetaData;
	ec.v_liveData.assetsMetaData = ec.v_liveAssetsMetaData;
	*/
	ec.v_liveData.resetLiveDatasets(p_runStartTime_h, p_runStartTime_h, p_timeStep_h);

	ec.fm_currentProductionFlows_kW = new J_FlowsMap();
	ec.fm_currentConsumptionFlows_kW = new J_FlowsMap();
	ec.fm_currentBalanceFlows_kW = new J_FlowsMap();
	ec.fm_currentAssetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
}

// Reconstruct the LiveData class in the GridConnections and add EnergyCarriers
List<GridConnection> allGridConnections = new ArrayList<>(c_gridConnections);
allGridConnections.addAll(c_pausedGridConnections);
for (GridConnection gc : allGridConnections) {
	/*gc.v_liveData = new J_LiveData(gc);
	gc.v_liveData.activeEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	gc.v_liveData.activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	gc.v_liveData.activeConsumptionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	
	gc.v_liveData.connectionMetaData = gc.v_liveConnectionMetaData;
	gc.v_liveData.assetsMetaData = gc.v_liveAssetsMetaData;
	*/
	gc.v_liveData.resetLiveDatasets(p_runStartTime_h, p_runStartTime_h, p_timeStep_h);
	
	gc.fm_currentProductionFlows_kW = new J_FlowsMap();
	gc.fm_currentConsumptionFlows_kW = new J_FlowsMap();
	gc.fm_currentBalanceFlows_kW = new J_FlowsMap();
	gc.fm_currentAssetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
	
	/*for (J_EA j_ea : gc.c_energyAssets) {
		gc.f_addEnergyCarriersAndAssetCategoriesFromEA(j_ea);
	}*/
}

// Initialize time and date
//v_hourOfYearStart=hourOfYearPerMonth[getMonth()] + (getDayOfMonth()-1)*24;
t_h = p_runStartTime_h;

Date startDate = date();
p_year = startDate.getYear() + 1900;

LocalDate localDate = LocalDate.of(p_year, 1, 1);
v_dayOfWeek1jan = DayOfWeek.from(localDate).getValue();
p_startOfWinterWeek_h = roundToInt(24 * (p_winterWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 49 is winterweek.
p_startOfSummerWeek_h = roundToInt(24 * (p_summerWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 18 is summerweek.

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
startDate.setHours(0);
startDate.setMinutes(0);
traceln("Startdate: %s", startDate);
//startDate.set
getExperiment().getEngine().setStartDate(startDate); 

f_initializeForecasts();

f_initializeLiveDataSets();

b_isDeserialised = true;
/*ALCODEEND*/}

Pair<J_DataSetMap, J_DataSetMap> f_getPeakWeekDataSets()
{/*ALCODESTART::1754052061419*/
double[] elecBalance_kW = v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW();

int maxIndex = 0; // index with peak import
for (int i = 1; i < elecBalance_kW.length; i++) {
    if (elecBalance_kW[i] > elecBalance_kW[maxIndex]) {
        maxIndex = i;
    }
}

int minIndex = 0; // index with peak export
for (int i = 1; i < elecBalance_kW.length; i++) {
    if (elecBalance_kW[i] < elecBalance_kW[minIndex]) {
        minIndex = i;
    }
}

int startIdx = max(0,maxIndex - 7*48);
int endIdx = max(startIdx + 7*96, maxIndex + 7*48);
J_DataSetMap peakImportWeekAssetFlows = new J_DataSetMap(OL_AssetFlowCategories.class);
for (OL_AssetFlowCategories AC : v_rapidRunData.am_assetFlowsAccumulators_kW.keySet()) {
	double[] assetFlowArray_kW = v_rapidRunData.am_assetFlowsAccumulators_kW.get(AC).getTimeSeries_kW();
	for (int i=startIdx; i<endIdx; i++) {
		peakImportWeekAssetFlows.get(AC).add(p_timeStep_h * i, assetFlowArray_kW[i]);
	}
}

startIdx = max(0,minIndex - 7*48);
endIdx = max(startIdx + 7*96, minIndex + 7*48);
J_DataSetMap peakExportWeekAssetFlows = new J_DataSetMap(OL_AssetFlowCategories.class);
for (OL_AssetFlowCategories AC : v_rapidRunData.am_assetFlowsAccumulators_kW.keySet()) {
	double[] assetFlowArray_kW = v_rapidRunData.am_assetFlowsAccumulators_kW.get(AC).getTimeSeries_kW();
	for (int i=startIdx; i<endIdx; i++) {
		peakExportWeekAssetFlows.get(AC).add(p_timeStep_h * i, assetFlowArray_kW[i]);
	}
}

return new Pair<>(peakImportWeekAssetFlows, peakExportWeekAssetFlows);

/*ALCODEEND*/}

EnergyCoop f_addAssetFlow(OL_AssetFlowCategories AC)
{/*ALCODESTART::1754379679149*/
if (!v_liveAssetsMetaData.activeAssetFlows.contains(AC)) {
	v_liveAssetsMetaData.activeAssetFlows.add(AC);
	
	DataSet dsAsset = new DataSet( (int)(168 / p_timeStep_h) );
	
	double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	for (double t = startTime; t <= endTime; t += p_timeStep_h) {
		dsAsset.add( t, 0);
	}
	v_liveData.dsm_liveAssetFlows_kW.put( AC, dsAsset);
	
	if (AC == OL_AssetFlowCategories.batteriesChargingPower_kW) { // also add batteriesDischarging!
		dsAsset = new DataSet( (int)(168 / p_timeStep_h) );
		
		for (double t = startTime; t <= endTime; t += p_timeStep_h) {
			dsAsset.add( t, 0);
		}
		v_liveData.dsm_liveAssetFlows_kW.put( OL_AssetFlowCategories.batteriesDischargingPower_kW, dsAsset);
	}
	if (AC == OL_AssetFlowCategories.V2GPower_kW && !v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.evChargingPower_kW)) { // also add evCharging!
		dsAsset = new DataSet( (int)(168 / p_timeStep_h) );
		
		for (double t = startTime; t <= endTime; t += p_timeStep_h) {
			dsAsset.add( t, 0);
		}
		v_liveData.dsm_liveAssetFlows_kW.put( OL_AssetFlowCategories.evChargingPower_kW, dsAsset);
	}	
}
/*ALCODEEND*/}

List<GridConnection> f_getGridConnectionsCollectionPointer()
{/*ALCODESTART::1754908171225*/
return this.c_gridConnections;
/*ALCODEEND*/}

List<GridConnection> f_getPausedGridConnectionsCollectionPointer()
{/*ALCODESTART::1755014169405*/
return this.c_pausedGridConnections;
/*ALCODEEND*/}

double f_runAggregators()
{/*ALCODESTART::1756207695591*/
//Function used to perform central management functions (like setpoint battery steering)

//Run energy coop aggrator
for (EnergyCoop EC : pop_energyCoops) {
	EC.f_aggregatorManagement_EnergyCoop();
}
/*ALCODEEND*/}

Date f_getDate()
{/*ALCODESTART::1758012535712*/
Date startDate = getExperiment().getEngine().getStartDate();
long startDateUnixTime_ms = startDate.getTime();
long runtime_ms = (long) (v_timeStepsElapsed * p_timeStep_h * 60 * 60 * 1000);
Date date = new Date();
date.setTime(startDateUnixTime_ms + runtime_ms);
return date;
/*ALCODEEND*/}

double f_loopSimulation()
{/*ALCODESTART::1758619562148*/
v_timeStepsElapsed = 0;
f_clearAllLiveDatasets();
traceln("The simulation has been looped.");
/*ALCODEEND*/}

double f_clearAllLiveDatasets()
{/*ALCODESTART::1758619851984*/
//Energy Model
v_liveData.clearLiveDatasets();

//Energy Coops
pop_energyCoops.forEach(EC -> EC.v_liveData.clearLiveDatasets());

//GridConnections
c_gridConnections.forEach(GC -> GC.v_liveData.clearLiveDatasets());
c_pausedGridConnections.forEach(GC -> GC.v_liveData.clearLiveDatasets());
/*ALCODEEND*/}

double f_initializeEngineAfterLoad()
{/*ALCODESTART::1758792939882*/
// Initialize time and date
//v_hourOfYearStart=hourOfYearPerMonth[getMonth()] + (getDayOfMonth()-1)*24;
t_h = p_runStartTime_h;

Date startDate = date();
p_year = startDate.getYear() + 1900;

LocalDate localDate = LocalDate.of(p_year, 1, 1);
v_dayOfWeek1jan = DayOfWeek.from(localDate).getValue();
p_startOfWinterWeek_h = roundToInt(24 * (p_winterWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 49 is winterweek.
p_startOfSummerWeek_h = roundToInt(24 * (p_summerWeekNumber * 7 + (8-v_dayOfWeek1jan)%7)); // Week 18 is summerweek.

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
startDate.setHours(0);
startDate.setMinutes(0);
traceln("Startdate: %s", startDate);
//startDate.set
getExperiment().getEngine().setStartDate(startDate); 



// Initialize all agents in the correct order, creating all connections. What about setting initial values? And how about repeated simulations?

/*f_buildGridNodeTree();
c_gridConnections.forEach(GC -> GC.f_initialize());

// Only relevant for deserialisation:
c_pausedGridConnections.forEach(GC -> GC.f_initialize());

pop_connectionOwners.forEach(CO -> CO.f_initialize());
pop_energyCoops.forEach(EC -> EC.f_initialize()); // Not yet robust when there is no supplier initialized!



// Initializing Live Data Class
v_liveAssetsMetaData.updateActiveAssetData(c_gridConnections);
for (GridConnection GC : c_gridConnections) {
	v_liveData.activeEnergyCarriers.addAll(GC.v_liveData.activeEnergyCarriers);
	v_liveData.activeConsumptionEnergyCarriers.addAll(GC.v_liveData.activeConsumptionEnergyCarriers);
	v_liveData.activeProductionEnergyCarriers.addAll(GC.v_liveData.activeProductionEnergyCarriers);
}

// Loop over populations to check v_ispaused
f_initializePause();

for (GridNode GN : c_gridNodeExecutionList) {
	GN.f_initializeGridnode();
}

v_liveData.connectionMetaData.contractedDeliveryCapacityKnown = false;
v_liveData.connectionMetaData.contractedFeedinCapacityKnown = false;
v_liveData.connectionMetaData.physicalCapacityKnown = false;
*/
f_initializeForecasts();

f_initializeLiveDataSets();

/*ALCODEEND*/}

