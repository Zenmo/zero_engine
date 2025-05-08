double f_connectToParentNode()
{/*ALCODESTART::1658495336616*/
GridNode myParentNode = findFirst(energyModel.pop_gridNodes, p->p.p_gridNodeID.equals(p_parentNodeID));
if( myParentNode instanceof GridNode ) {
	//superConnection.connectTo(myParentNode);
	myParentNode.f_connectToChild(this);
}

GridOperator myParentGridOperator = findFirst(energyModel.pop_gridOperators, p->p.p_actorID.equals(p_gridNodeOwnerID)) ;
if( myParentGridOperator instanceof GridOperator) {
	p_ownerGridOperator = myParentGridOperator;
	//superConnection.connectTo(myParentGridOperator);
	myParentGridOperator.f_connectToChild(this, p_energyCarrier);
}

/*ALCODEEND*/}

double f_connectToChild(Agent ConnectingChildNode)
{/*ALCODESTART::1658495630251*/
//subConnections.connectTo(ConnectingChildNode);

if ( ConnectingChildNode instanceof GridNode) {
	 c_connectedGridNodes.add( (GridNode)ConnectingChildNode );
}

if ( ConnectingChildNode instanceof GridConnection) {
	 c_connectedGridConnections.add( (GridConnection)ConnectingChildNode );
}

if ( ConnectingChildNode instanceof EnergyCoop) {
	 c_energyCoops.add( (EnergyCoop)ConnectingChildNode );
}

/*if( ConnectingChildNode instanceof EnergyAsset ) {
	J_EA j_ea = (J_EA) ConnectingChildNode;
	c_connectedEnergyAssets.add(j_ea);
	if (j_ea instanceof J_EAConsumption) {
		c_connectedConsumptionAssets.add((J_EAConsumption)j_ea);
	} else if (j_ea instanceof J_EAProduction ) {
		c_connectedProductionAssets.add((J_EAProduction)j_ea);
	} else if (j_ea instanceof J_EAStorage ) {
		c_connectedStorageAssets.add((J_EAStorage)j_ea);
		if (j_ea instanceof J_EAStorageHeat) {
			p_transportBuffer = (J_EAStorageHeat)j_ea;
		}
	} else if (j_ea instanceof J_EAConversion) {
		c_connectedConversionAssets.add((J_EAConversion)j_ea);
//	if (EA.j_ea instanceof J_EAConversionGasBurner || EA.j_ea instanceof J_EAConversionHeatPump || EA.j_ea instanceof J_EAConversionHeatDeliverySet ) {
	//	p_HeatingAsset = EA;
//		//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
//	}
	} else {
		traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + ConnectingChildNode.getId() + " is of unknown type or null! ");
	}
}*/

/*ALCODEEND*/}

double f_sumLoads()
{/*ALCODESTART::1660122738707*/
v_currentLoad_kW = 0;

// determine the net energy flows from all subconnections by nodetype

for( GridNode GN : c_connectedGridNodes ) {
	v_currentLoad_kW += GN.v_currentLoad_kW;
}

for( GridConnection GC : c_connectedGridConnections) {
	v_currentLoad_kW += GC.fm_currentBalanceFlows_kW.get(p_energyCarrier);
}


/*if( p_energyType == OL_EnergyCarriers.ELECTRICITY ){
	v_electricLoadRatioExclBattery = v_currentLoadElectricity_kW / p_capacity_kW;
}*/


/*ALCODEEND*/}

double f_nodeMetering()
{/*ALCODESTART::1660216693598*/
//v_averageAbsoluteLoadElectricity_kW = ( v_electricityDrawn_kWh + v_electricityDelivered_kWh ) / energyModel.t_h;
//v_loadFactor_fr = v_averageAbsoluteLoadElectricity_kW / abs(v_peakLoadAbsoluteElectricity_kW);

if (energyModel.v_isRapidRun){
	//v_maxConnectionLoad_fr = max(v_maxConnectionLoad_fr, abs(v_currentPowerElectricity_kW / v_allowedCapacity_kW ));
	
	/*if (energyModel.b_enableDLR) {
		acc_annualElectricityBalance_kW.addStep(100*v_currentLoadElectricity_kW/ (p_capacity_kW * energyModel.v_currentDLRfactor_fr));
		//acc_DLR_kW.addStep( p_capacity_kW * energyModel.v_currentDLRfactor_fr);
	} else {
		acc_annualElectricityBalance_kW.addStep(v_currentLoadElectricity_kW;
	}*/
	
	if ( ((Double)v_currentLoad_kW).isNaN() ){
		traceln("v_currentLoad_kW is NaN! On GridNode %s, time %s h", this, energyModel.t_h);
		pauseSimulation();
	}
	
	if (p_energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
		acc_annualElectricityBalance_kW.addStep( v_currentLoad_kW);
	}
	
	double currentImport_MWh = max(0, v_currentLoad_kW) * energyModel.p_timeStep_h / 1000;
	double currentExport_MWh = max(0, -v_currentLoad_kW) * energyModel.p_timeStep_h / 1000;
	double currentExcessImport_MWh = max(0, v_currentLoad_kW - p_capacity_kW) * energyModel.p_timeStep_h / 1000;
	double currentExcessExport_MWh = max(0, -v_currentLoad_kW - p_capacity_kW) * energyModel.p_timeStep_h / 1000;
	
	v_totalImport_MWh += currentImport_MWh;
	v_totalExport_MWh += currentExport_MWh;
	v_annualExcessImport_MWh += currentExcessImport_MWh;
	v_annualExcessExport_MWh += currentExcessExport_MWh;
	
	// Year
	if (energyModel.t_h % 1 == 0) {
		data_totalLoad_kW.add(energyModel.t_h, v_currentLoad_kW);
	}
	// SummerWeek
	if (energyModel.b_isSummerWeek) {
		v_summerWeekImport_MWh += currentImport_MWh;
		v_summerWeekExport_MWh += currentExport_MWh;
		v_summerWeekExcessImport_MWh += currentExcessImport_MWh;
		v_summerWeekExcessExport_MWh += currentExcessExport_MWh;
		
		data_summerWeekLoad_kW.add(energyModel.t_h-energyModel.p_runStartTime_h, v_currentLoad_kW);
	}
	// Winterweek
	if (energyModel.b_isWinterWeek) {
		v_winterWeekImport_MWh += currentImport_MWh;
		v_winterWeekExport_MWh += currentExport_MWh;
		v_winterWeekExcessImport_MWh += currentExcessImport_MWh;
		v_winterWeekExcessExport_MWh += currentExcessExport_MWh;
		
		data_winterWeekLoad_kW.add(energyModel.t_h-energyModel.p_runStartTime_h, v_currentLoad_kW);
	}
	// Daytime
	if (energyModel.t_h % 24 > 6 && energyModel.t_h % 24 < 18) {
		v_daytimeImport_MWh += currentImport_MWh;
		v_daytimeExport_MWh += currentExport_MWh;
		v_daytimeExcessImport_MWh += currentExcessImport_MWh;
		v_daytimeExcessExport_MWh += currentExcessExport_MWh;
	}
	// Weekdays
	if ((energyModel.t_h+(energyModel.v_dayOfWeek1jan-1)*24) % (24*7) < (24*5)) { // Simulation starts on a Thursday, hence the +3 day offset on t_h
		v_weekdayImport_MWh += currentImport_MWh;
		v_weekdayExport_MWh += currentExport_MWh;
		v_weekdayExcessImport_MWh += currentExcessImport_MWh;
		v_weekdayExcessExport_MWh += currentExcessExport_MWh;
	}
}
/*ALCODEEND*/}

double f_setCongestionTariff(double price_eurpkWh)
{/*ALCODESTART::1666556555538*/
for (GridConnection gc: c_connectedGridConnections){
	//traceln("Gc "+ gc.toString() + ", price "+ price_eurpkWh + ", allowance "+ allowance_kW +".");
	ConnectionOwner owner = ((ConnectionOwner)gc.l_ownerActor.getConnectedAgent());
	owner.v_currentNodalPrice_eurpkWh = price_eurpkWh;
}
for (EnergyCoop e: c_energyCoops){
	e.v_currentNodalPrice_eurpkWh = price_eurpkWh;
}
/*ALCODEEND*/}

double f_resetCongestionTariffs()
{/*ALCODESTART::1666560316436*/
for (GridConnection gc: c_connectedGridConnections){
	ConnectionOwner owner = ((ConnectionOwner)gc.l_ownerActor.getConnectedAgent());
	owner.v_currentNodalPrice_eurpkWh = 0;
}
for (EnergyCoop e: c_energyCoops){
	e.v_currentNodalPrice_eurpkWh = 0;
}
/*ALCODEEND*/}

double f_instantiateEnergyAssets()
{/*ALCODESTART::1676387466304*/
//traceln("asset " + p_energyAssetList);

if( p_energyAssetList != null) {
	for( JsonNode l : p_energyAssetList ) {
		OL_EACategories assetCategory = OL_EACategories.valueOf(l.required( "category" ).textValue());
		switch( assetCategory )  {		
			case STORAGE:
				//EnergyAsset storageAsset = main.add_pop_energyAssets();
				//storageAsset.set_p_parentAgentID( this.p_gridNodeID );
				OL_EnergyAssetType assetType = ( OL_EnergyAssetType.valueOf(l.required( "type" ).textValue()));
				//storageAsset.set_p_defaultEnergyAssetPresetName(l.required( "name" ).textValue());
				if( assetType == OL_EnergyAssetType.STORAGE_HEAT){
				    double capacityHeat_kW = l.path( "capacityHeat_kW").doubleValue();
					double lossFactor_WpK = l.path( "lossFactor_WpK" ).doubleValue();
					double heatCapacity_JpK = l.path( "heatCapacity_JpK" ).doubleValue();
					double minTemperature_degC = l.path( "minTemp_degC" ).asDouble(40.0); // provide default values
					double maxTemperature_degC = l.path( "maxTemp_degC" ).asDouble(90.0);
					double setTemperature_degC = l.path( "setTemp_degC" ).asDouble(60.0);
					double initialTemperature_degC = l.path( "initialTemperature_degC" ).doubleValue();
					String ambientTempType2 = l.path( "ambientTempType" ).textValue();
					
					// minTemperature_degC = 35; // TO DELETE TEMP FIX
					// setTemperature_degC = uniform_discr(38, 45); // TO DELETE TEMP FIX
					// initialTemperature_degC = uniform_discr(70,80) * 1.0; // TO DELETE TEMP FIX
					// heatCapacity_JpK = heatCapacity_JpK * uniform(0.7, 1.3); // TO DELETE TEMP FIX
					traceln("heatstorage asset initialisation check! minTemp = "+minTemperature_degC+", maxTemperature_degC = "+maxTemperature_degC+", setTemp_degC = "+ setTemperature_degC+", initialTemperature_degC = "+initialTemperature_degC);
					
					//traceln("Heat Storage init: minTemperature = "+minTemperature_degC+", maxTemperature_degC = "+maxTemperature_degC+", setTemperature_degC = "+setTemperature_degC+", initialTemperature_degC = "+initialTemperature_degC);
					
					
					p_transportBuffer = new J_EAStorageHeat((Agent)this, OL_EAStorageTypes.HEATBUFFER, capacityHeat_kW, lossFactor_WpK, energyModel.p_timeStep_h, initialTemperature_degC, minTemperature_degC, maxTemperature_degC, setTemperature_degC, heatCapacity_JpK, ambientTempType2);	
					//J_EAStorageHeat(Agent parentAgent, OL_EAStorageTypes heatStorageType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, String ambientTempType ) {
					//p_transportBuffer = storageAsset.j_ea;
					p_transportBuffer.updateAmbientTemperature( energyModel.p_undergroundTemperature_degC );
					if(heatCapacity_JpK > 0 & capacityHeat_kW > 0) {
						b_transportBufferValid = true;
					}
				}
				else{
					traceln("F_instantiateEnergyAssets: ERROR, storage asset type not available");
				}
				//storageAsset.f_connectToParentNode( this );
				//main.c_storageAssets.add(storageAsset);

			break;
			default:
				traceln("not a valid energy asset category." + assetCategory);
			break;
		}
	}
//traceln("GridConnection "+this.p_gridNodeID+" has finished initializing its energyAssets!");
}
/*ALCODEEND*/}

double f_calculateEnergyBalance()
{/*ALCODESTART::1688370981599*/
f_sumLoads();
// Low-pass filtered grid load
//double lowPassFraction = min(1,1*1.2*energyModel.p_timeStep_h); // smaller value results in more filtering

if (p_energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
	v_currentLoadElectricityLowPassed_kW += v_lowPassFactor_fr * ( v_currentLoad_kW - v_currentLoadElectricityLowPassed_kW );
	v_filteredLoadCongestionPricing_kW += v_lowPassFactorCongestionPricing_fr * ( v_currentLoad_kW - v_filteredLoadCongestionPricing_kW);
	//v_currentLocalNodalPrice_eurpkWh = v_filteredLoadCongestionPricing_kW / p_capacity_kW * p_localNodalPricingFactor_eurpkWh;
	v_currentDLRCapacity_kW = energyModel.v_currentDLRfactor_fr * p_capacity_kW;
	v_currentLocalNodalPrice_eurpkWh = signum(v_filteredLoadCongestionPricing_kW) * max(0,abs(v_filteredLoadCongestionPricing_kW) - v_currentDLRCapacity_kW * p_localNodalPricingTreshold_fr) / ((1-p_localNodalPricingTreshold_fr) * v_currentDLRCapacity_kW) * p_localNodalPricingFactor_eurpkWh;
}

//v_currentLocalNodalPrice_eurpkWh = (abs(v_filteredLoadCongestionPricing_kW / currentNodeCapacity_kW) - p_localNodalPricingTreshold_fr) / (1-p_localNodalPricingTreshold_fr) * p_localNodalPricingFactor_eurpkWh;

if (energyModel.v_isRapidRun) {
	if (p_energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
		if (abs(v_currentLoadElectricityLowPassed_kW) > p_capacity_kW) {
			//traceln("Overloaded gridNode %s! %s kW", p_gridNodeID, abs(v_currentLoadElectricityLowPassed_kW));
			v_totalTimeOverloaded_h	+= energyModel.p_timeStep_h;
		}
		if( abs( v_currentLoadElectricityLowPassed_kW ) > abs(v_peakLoadFilteredElectricity_kW) ) { // store maximum absolute load, but retain sign!
			v_peakLoadFilteredElectricity_kW = ( v_currentLoadElectricityLowPassed_kW );
			v_timeOfPeakLoadFiltered_h = energyModel.t_h;
		}
		if( v_currentLoadElectricityLowPassed_kW < v_peakNegLoadElectricity_kW ) { // store peak negative load and time
			v_peakNegLoadElectricity_kW = v_currentLoadElectricityLowPassed_kW ;
			v_timeOfPeakNegLoadFiltered_h = energyModel.t_h;
		}
		if( v_currentLoadElectricityLowPassed_kW > v_peakPosLoadElectricity_kW ) { // store peak positive load and time
			v_peakPosLoadElectricity_kW = v_currentLoadElectricityLowPassed_kW ;
			v_timeOfPeakPosLoadFiltered_h = energyModel.t_h;
		}
	}

	if( abs( v_currentLoad_kW ) > abs(v_peakLoadAbsolute_kW) ) { // store maximum absolute load, but retain sign!
		v_peakLoadAbsolute_kW = v_currentLoad_kW;
		//v_timeOfPeakLoad_h = energyModel.t_h;
	}
}
else {
	data_liveLoad_kW.update();
	data_liveCapacityDemand_kW.update();
	data_liveCapacitySupply_kW.update();
}
/*
if (p_energyType.equals(OL_EnergyCarriers.HEAT) & b_transportBufferValid ) { // Thermal load unbalance goes into transportBuffer
//	double v_powerFraction_fr = Double.isNaN( -v_currentLoadHeat_kW / p_transportBuffer.getHeatCapacity_kW() )? 0 : -v_currentLoadHeat_kW / p_transportBuffer.getHeatCapacity_kW();
	//traceln("GridNode Heat v_currentLoadHeat_kW: %s", v_currentLoadHeat_kW);
	double v_powerFraction_fr = -v_currentLoadHeat_kW / p_transportBuffer.getHeatCapacity_kW();
	
	//p_transportBuffer.operate(v_powerFraction_fr);
	//traceln("DistrictHeating network heatcapacity = "+ p_transportBuffer.getHeatCapacity_kW()+" kW, water temperature " + p_transportBuffer.getCurrentTemperature() + " deg C, buffer power fraction " + v_powerFraction_fr * 100 + " %, transportbuffer HeatCapacity_kW " + p_transportBuffer.getHeatCapacity_kW());
	p_transportBuffer.f_updateAllFlows(v_powerFraction_fr);
	//traceln("DistrictHeating Grid temperature: %s degC", p_transportBuffer.getCurrentTemperature());
	//traceln("DistrictHeating network water temperature " + p_transportBuffer.getCurrentTemperature() + " deg C, buffer power fraction " + v_powerFraction_fr * 100 + " %, transportbuffer HeatCapacity_kW " + p_transportBuffer.getHeatCapacity_kW());
} else if(abs(v_currentLoadHeat_kW)>0.001) {
	traceln("Non-zero heat-load on district heating network without valid transport buffer!");
}
*/
//traceln("GridNode " + p_gridNodeID + " update at time " + time(HOUR));
f_nodeMetering();

/*ALCODEEND*/}

double f_addGridBatteryLoad()
{/*ALCODESTART::1688372319365*/

for( Agent a : subConnections.getConnections() ) {
	if ( a instanceof GCGridBattery){
		v_currentLoadElectricity_kW += ((GCGridBattery)a).v_currentPowerElectricity_kW;
	}	
}

/*ALCODEEND*/}

double f_resetStates()
{/*ALCODESTART::1698919552330*/
// Current status
v_currentLoad_kW = 0;
v_currentLoadElectricityLowPassed_kW = 0;
v_currentDLRCapacity_kW = p_capacity_kW;
v_congested = false;
v_currentCongestionPrice_eurpkWh = 0;
v_filteredLoadCongestionPricing_kW = 0;

// Performance variables
v_peakLoadAbsolute_kW = 0;
//v_averageAbsoluteLoadElectricity_kW = 0;
//v_loadFactor_fr = 0;
v_timeOfPeakLoadFiltered_h = 0;
v_timeOfPeakNegLoadFiltered_h = 0;
v_timeOfPeakPosLoadFiltered_h = 0;
v_peakPosLoadElectricity_kW = 0;
v_peakNegLoadElectricity_kW = 0;
v_peakLoadFilteredElectricity_kW = 0;
v_timeOfPeakLoadFiltered_h = 0;
v_totalTimeOverloaded_h = 0;

v_totalImport_MWh = 0;
v_totalExport_MWh = 0;
v_annualExcessImport_MWh = 0;
v_annualExcessExport_MWh = 0;

v_summerWeekImport_MWh = 0;
v_summerWeekExport_MWh = 0;
v_summerWeekExcessImport_MWh = 0;
v_summerWeekExcessExport_MWh = 0;

v_winterWeekImport_MWh = 0;
v_winterWeekExport_MWh = 0;
v_winterWeekExcessImport_MWh = 0;
v_winterWeekExcessExport_MWh = 0;

v_daytimeImport_MWh = 0;
v_daytimeExport_MWh = 0;
v_daytimeExcessImport_MWh = 0;
v_daytimeExcessExport_MWh = 0;

v_nighttimeImport_MWh = 0;
v_nighttimeExport_MWh = 0;
v_nighttimeExcessImport_MWh = 0;
v_nighttimeExcessExport_MWh = 0;

v_weekdayImport_MWh = 0;
v_weekdayExport_MWh = 0;
v_weekdayExcessImport_MWh = 0;
v_weekdayExcessExport_MWh = 0;

v_weekendImport_MWh = 0;
v_weekendExport_MWh = 0;
v_weekendExcessImport_MWh = 0;
v_weekendExcessExport_MWh = 0;

// Reset Accumulators
acc_annualElectricityBalance_kW.reset();
/*ALCODEEND*/}

double f_calculateKPIs()
{/*ALCODESTART::1713181018774*/
f_getDuurkromme();

// Calcs nighttime
v_nighttimeImport_MWh = v_totalImport_MWh - v_daytimeExcessImport_MWh;
v_nighttimeExport_MWh = v_totalExport_MWh - v_daytimeExcessExport_MWh;
v_nighttimeExcessImport_MWh = v_annualExcessImport_MWh - v_daytimeExcessImport_MWh;
v_nighttimeExcessExport_MWh = v_annualExcessExport_MWh - v_daytimeExcessExport_MWh;

// Calcs weekend
v_weekendImport_MWh = v_totalImport_MWh - v_weekdayImport_MWh;
v_weekendExport_MWh = v_totalExport_MWh - v_weekdayExport_MWh;
v_weekendExcessImport_MWh = v_annualExcessImport_MWh - v_weekdayExcessImport_MWh;
v_weekendExcessExport_MWh = v_annualExcessExport_MWh - v_weekdayExcessExport_MWh;
/*ALCODEEND*/}

DataSet f_getDuurkromme()
{/*ALCODESTART::1718111675053*/
J_LoadDurationCurves j_duurkrommes = new J_LoadDurationCurves(acc_annualElectricityBalance_kW.getTimeSeries_kW(), energyModel);

data_netbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveTotal_kW;
data_summerWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveSummer_kW;
data_winterWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWinter_kW;
data_daytimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveDaytime_kW;
data_nighttimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveNighttime_kW;
data_weekdayNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekday_kW;
data_weekendNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekend_kW;
 
return data_netbelastingDuurkromme_kW;
/*
int runStartIdx = 0;// (int)(energyModel.p_runStartTime_h/energyModel.p_timeStep_h);
int runEndIdx = (int)((energyModel.p_runEndTime_h-energyModel.p_runStartTime_h)/energyModel.p_timeStep_h);
int nRunIdxs = runEndIdx - runStartIdx;

boolean firstRun = true;
if (data_netbelastingDuurkromme_kW != null) {	
	if (data_netbelastingDuurkrommeVorige_kW != null) { // Not second run either!
		data_netbelastingDuurkrommeVorige_kW.reset();
	} else {
		data_netbelastingDuurkrommeVorige_kW = new DataSet(nRunIdxs);
	}
	firstRun = false;
} else {
	data_netbelastingDuurkromme_kW = new DataSet(nRunIdxs);
	data_summerWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	data_winterWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	data_daytimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(nRunIdxs/2 + 24 / energyModel.p_timeStep_h));
	data_nighttimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(nRunIdxs/2 + 24 / energyModel.p_timeStep_h));
	data_weekdayNetbelastingDuurkromme_kW = new DataSet(roundToInt(nRunIdxs/7*5 + 24 / energyModel.p_timeStep_h));
	data_weekendNetbelastingDuurkromme_kW = new DataSet(roundToInt(nRunIdxs/7*2 + 24 / energyModel.p_timeStep_h));
}

// We copy our annual array to preserve it as a time-series and make new arrays for the others
double[] netLoadArrayAnnual_kW = acc_annualElectricityBalance_kW.getTimeSeries().clone(); 

int arraySize = netLoadArrayAnnual_kW.length;

if (energyModel.b_enableDLR) {
	double[] annualDLRarray = energyModel.acc_totalDLRfactor_f.getTimeSeries().clone();
	for (int i = 0; i < arraySize; i++) {
		netLoadArrayAnnual_kW[i] = 100 * netLoadArrayAnnual_kW[i] / (p_capacity_kW * max(energyModel.v_minDLRfactor_fr,annualDLRarray[i]));
	}
}
//traceln("annaualElectricityBalance first element: %s, last element: %s", netLoadArrayAnnual_kW[0], netLoadArrayAnnual_kW[arraySize-1]);

double[] netLoadArraySummerweek_kW = new double[roundToInt(24*7 / energyModel.p_timeStep_h)];
double[] netLoadArrayWinterweek_kW= new double[roundToInt(24*7 / energyModel.p_timeStep_h)];
double[] netLoadArrayDaytime_kW = new double[roundToInt(nRunIdxs/2 + 24/ energyModel.p_timeStep_h)];
double[] netLoadArrayNighttime_kW = new double[roundToInt(nRunIdxs/2 + 24/ energyModel.p_timeStep_h)];
// For different years the amount of weekdays and weekend days may be different, so the size will be variable for now
ArrayList<Double> listNetLoadArrayWeekday_kW = new ArrayList<>(); 
ArrayList<Double> listNetLoadArrayWeekend_kW = new ArrayList<>();

int i_winter=0;
int i_summer=0;
int i_day=0;
int i_night=0;
int i_weekday=0;
int i_weekend=0;

//double[] annualElectricityBalanceTimeSeries_kW = acc_annualElectricityBalance_kW.getTimeSeries();

for(int i=0; i<nRunIdxs; i++) {
	if (!firstRun) {
		// First we make sure to store our previous Load Curve
		data_netbelastingDuurkrommeVorige_kW.add(i*energyModel.p_timeStep_h,data_netbelastingDuurkromme_kW.getY(i));		
	}
	// summer/winter
	if ((energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h) % 8760 > energyModel.p_startHourSummerWeek && (energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h) % 8760 <= energyModel.p_startHourSummerWeek+24*7) {
		netLoadArraySummerweek_kW[i_summer]=-netLoadArrayAnnual_kW[i];
		i_summer++;
	}
	if ((energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h) % 8760 > energyModel.p_startHourWinterWeek && (energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h) % 8760 <= energyModel.p_startHourWinterWeek+24*7) {
		netLoadArrayWinterweek_kW[i_winter]=-netLoadArrayAnnual_kW[i];
		i_winter++;
	}
	// day/night
	if (i*energyModel.p_timeStep_h % 24 > 6 && i*energyModel.p_timeStep_h % 24 <= 18) { //daytime
		netLoadArrayDaytime_kW[i_day]=-netLoadArrayAnnual_kW[i];
		i_day++;
	} else {
		netLoadArrayNighttime_kW[i_night]=-netLoadArrayAnnual_kW[i];
		i_night++;
	}
	//Weekday/weekend
	if (((energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h+ 24*(energyModel.v_dayOfWeek1jan-1)) % (24*7)) < (24*5)) { // Simulation starts on a Thursday, hence the +3 day offset on t_h
		listNetLoadArrayWeekday_kW.add(-netLoadArrayAnnual_kW[i]);
		i_weekday++;
	} else {
		listNetLoadArrayWeekend_kW.add(-netLoadArrayAnnual_kW[i]);
		i_weekend++;
	}
	
}
netLoadArrayDaytime_kW = Arrays.copyOfRange(netLoadArrayDaytime_kW,0,i_day);
netLoadArrayNighttime_kW = Arrays.copyOfRange(netLoadArrayNighttime_kW,0,i_night);


// Now we have the size of the weekday & weekend arrays.
double[] netLoadArrayWeekday_kW = new double[listNetLoadArrayWeekday_kW.size()];
double[] netLoadArrayWeekend_kW = new double[listNetLoadArrayWeekend_kW.size()];
for (int i = 0; i < listNetLoadArrayWeekday_kW.size(); i++) {
	netLoadArrayWeekday_kW[i] = listNetLoadArrayWeekday_kW.get(i);
}
for (int i = 0; i < listNetLoadArrayWeekend_kW.size(); i++) {
	netLoadArrayWeekend_kW[i] = listNetLoadArrayWeekend_kW.get(i);
}

netLoadArrayAnnual_kW = Arrays.copyOfRange(netLoadArrayAnnual_kW,runStartIdx,runEndIdx);
arraySize = netLoadArrayAnnual_kW.length;
// Sort all arrays
Arrays.parallelSort(netLoadArrayAnnual_kW);
Arrays.parallelSort(netLoadArraySummerweek_kW);
Arrays.parallelSort(netLoadArrayWinterweek_kW);
Arrays.parallelSort(netLoadArrayDaytime_kW);
Arrays.parallelSort(netLoadArrayNighttime_kW);
Arrays.parallelSort(netLoadArrayWeekday_kW);
Arrays.parallelSort(netLoadArrayWeekend_kW);

//traceln("Peak grid load: %s", netLoadArrayAnnual_kW[netLoadArrayAnnual_kW.length-1]);
netLoadArrayAnnual_kW[netLoadArrayAnnual_kW.length-1] = 0;

// Write results to datasets
// Netbelastingduurkromme year
//if (!firstRun) {
	data_netbelastingDuurkromme_kW.reset();
	data_summerWeekNetbelastingDuurkromme_kW.reset();	
	data_winterWeekNetbelastingDuurkromme_kW.reset();
	data_daytimeNetbelastingDuurkromme_kW.reset();
	data_nighttimeNetbelastingDuurkromme_kW.reset();
	data_weekdayNetbelastingDuurkromme_kW.reset();
	data_weekendNetbelastingDuurkromme_kW.reset();
//}

//traceln("annaualElectricityBalance first element: %s, last element: %s", netLoadArrayAnnual_kW[0], netLoadArrayAnnual_kW[arraySize-1]);
for(int i=0; i< arraySize; i++) {
	//traceln("netloadloop iterator: %s", i);
	data_netbelastingDuurkromme_kW.add((i)*energyModel.p_timeStep_h, netLoadArrayAnnual_kW[arraySize-i-1]);
}
// Netbelastingduurkromme summer / winter

arraySize = netLoadArraySummerweek_kW.length;
for(int i=0; i< arraySize; i++) {
	data_summerWeekNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArraySummerweek_kW[i]);
}

arraySize = netLoadArrayWinterweek_kW.length;
for(int i=0; i< arraySize; i++) {
	data_winterWeekNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayWinterweek_kW[i]);
}
// Netbelastingduurkromme day / night

arraySize = netLoadArrayDaytime_kW.length;
for(int i=0; i< arraySize; i++) {
	data_daytimeNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayDaytime_kW[i]);
}

arraySize = netLoadArrayNighttime_kW.length;
for(int i=0; i< arraySize; i++) {
	data_nighttimeNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayNighttime_kW[i]);
}
// Netbelastingduurkromme weekday / weekend

arraySize = netLoadArrayWeekday_kW.length;
for(int i=0; i< arraySize; i++) {
	data_weekdayNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayWeekday_kW[i]);
}

arraySize = netLoadArrayWeekend_kW.length;
for(int i=0; i< arraySize; i++) {
	data_weekendNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayWeekend_kW[i]);
}
*/
/*ALCODEEND*/}

ArrayList<GridNode> f_getConnectedGridNodes()
{/*ALCODESTART::1718290223518*/
return this.c_connectedGridNodes;
/*ALCODEEND*/}

ArrayList<GridConnection> f_getConnectedGridConnections()
{/*ALCODESTART::1718290606581*/
return this.c_connectedGridConnections;
/*ALCODEEND*/}

double f_propagateNodalPricing()
{/*ALCODESTART::1718896086734*/
v_currentTotalNodalPrice_eurpkWh = v_currentParentNodalPrice_eurpkWh + v_currentLocalNodalPrice_eurpkWh;
for (GridNode GN : c_connectedGridNodes ) {
	GN.v_currentParentNodalPrice_eurpkWh = v_currentTotalNodalPrice_eurpkWh;
}
/*ALCODEEND*/}

double f_initializeGridnode()
{/*ALCODESTART::1719300860382*/
v_totalInstalledWindPower_kW = 0;
v_totalInstalledPVPower_kW = 0;

for (GridConnection GC : c_connectedGridConnections) {
	if (GC.v_isActive) {
		v_totalInstalledWindPower_kW += GC.v_liveAssetsMetaData.totalInstalledWindPower_kW;
		v_totalInstalledPVPower_kW += GC.v_liveAssetsMetaData.totalInstalledPVPower_kW;
	}
}

for (GridNode GN : c_connectedGridNodes) {
	v_totalInstalledWindPower_kW += GN.v_totalInstalledWindPower_kW;
	v_totalInstalledPVPower_kW += GN.v_totalInstalledPVPower_kW;
}

/*
if ( p_energyType == OL_EnergyCarriers.HEAT ) {
	double capacityHeat_kW = 1000000;
	double lossFactor_WpK = 10000;
	double heatCapacity_JpK = 10000 * 3.6e6;	
	double InitialStateOfCharge_degC = 60;
	double soilTemperature_degC = energyModel.p_undergroundTemperature_degC;
	p_transportBuffer = new J_EAStorageHeat(null, OL_EAStorageTypes.HEATBUFFER, capacityHeat_kW, lossFactor_WpK, energyModel.p_timeStep_h, InitialStateOfCharge_degC, 10.0, 90.0, InitialStateOfCharge_degC, heatCapacity_JpK, "AIR" );
	p_transportBuffer.updateAmbientTemperature(soilTemperature_degC);
	b_transportBufferValid = true;
	energyModel.c_energyAssets.add(p_transportBuffer);
	energyModel.c_storageAssets.add(p_transportBuffer);
}*/
/*ALCODEEND*/}

double f_updateForecasts()
{/*ALCODESTART::1719302290904*/
// The ElectricityYieldForecast assumes solar and wind forecasts have the same forecast time
if ( v_totalInstalledPVPower_kW + v_totalInstalledWindPower_kW > 0 ) {
	v_electricityYieldForecast_fr = (energyModel.v_SolarYieldForecast_fr * v_totalInstalledPVPower_kW + energyModel.v_WindYieldForecast_fr * v_totalInstalledWindPower_kW) / (v_totalInstalledPVPower_kW + v_totalInstalledWindPower_kW);
}

/*ALCODEEND*/}

double f_updateTotalInstalledProductionAssets(OL_EnergyAssetType energyAssetType,double power_kw,boolean increase)
{/*ALCODESTART::1722591244558*/
if (energyAssetType == OL_EnergyAssetType.WINDMILL) {
	if (increase) {
		v_totalInstalledWindPower_kW += power_kw;
	}
	else {
		v_totalInstalledWindPower_kW -= power_kw;
	}
}
else if (energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC) {
	if (increase) {
		v_totalInstalledPVPower_kW += power_kw;
	}
	else {
		v_totalInstalledPVPower_kW -= power_kw;
	}
}
else {
	throw new IllegalStateException("Wrong energy asset type");
}

if (p_parentNodeID != null) {
	GridNode myParentNode = findFirst(energyModel.pop_gridNodes, p->p.p_gridNodeID.equals(p_parentNodeID));
	if (myParentNode != null) {
		myParentNode.f_updateTotalInstalledProductionAssets(energyAssetType, power_kw, increase);
	}
}
/*ALCODEEND*/}

List<GridNode> f_getLowerLVLConnectedGridNodes()
{/*ALCODESTART::1725964027407*/
List<GridNode> allConnectedGridNodes = new ArrayList<GridNode>();

for(GridNode GN : c_connectedGridNodes){
	allConnectedGridNodes.addAll(GN.f_getAllConnectedGridNodes_recursion(new ArrayList<GridNode>()));
}

return allConnectedGridNodes;
/*ALCODEEND*/}

List<GridNode> f_getAllConnectedGridNodes_recursion(List<GridNode> allConnectedGridNodes)
{/*ALCODESTART::1725966618828*/
//Add to collection
allConnectedGridNodes.add(this);

//Recursive loop (repeat this function till bottom)
if(c_connectedGridNodes.size() == 0){
	return allConnectedGridNodes;
}
else{
	for(GridNode GN : c_connectedGridNodes){
		GN.f_getAllConnectedGridNodes_recursion(allConnectedGridNodes);
		//allConnectedGridNodes.addAll(GN.f_getAllConnectedGridNodes(allConnectedGridNodes));
	}
	return allConnectedGridNodes;
}

/*ALCODEEND*/}

ArrayList<GridConnection> f_getAllLowerLVLConnectedGridConnections()
{/*ALCODESTART::1734617656602*/
ArrayList<GridConnection> AllLowerLVLConnectedGridConnections = new ArrayList<GridConnection>();

for(GridNode GN : f_getLowerLVLConnectedGridNodes()){
	AllLowerLVLConnectedGridConnections.addAll(GN.f_getConnectedGridConnections());
}

AllLowerLVLConnectedGridConnections.addAll(this.c_connectedGridConnections);

return AllLowerLVLConnectedGridConnections;
/*ALCODEEND*/}

