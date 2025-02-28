double f_returnKPIData(boolean writeToDisk)
{/*ALCODESTART::1661328014569*/
traceln("");
traceln("--------------------------");
traceln("====== RUN FINISHED ======");
traceln("--------------------------");
traceln("");

f_calculateKPIs();

ObjectMapper mapper = new ObjectMapper();

//DataOut great_succes = new DataOut();
j_dataOut.clearData();

// Collect actor data for all relevant actor groups en place in j_dataOut.actorData
for(ConnectionOwner c : pop_connectionOwners) {
	try {
		//traceln("ConnectionOwner v_ownedGridConnection: " + c.v_ownedGridConnection.p_gridConnectionID);
//		traceln("ConnectionOwner v_energySupplierDistrictHeat: " + c.v_energySupplierDistrictHeat.toString());
		boolean b_methaneUsedWithoutContracts = c.v_methanePrice_eurpkWh == 0.0 & c.v_totalMethaneUsed_kWh > 0;
		boolean b_hydrogenUsedWithoutContracts = c.v_hydrogenPrice_eurpkWh == 0.0 & c.v_totalHydrogenUsed_kWh > 0;
		if (b_methaneUsedWithoutContracts) {
			traceln("Connection Owner " + c.p_actorID + " used methane without the required contracts!") ;
		}
		String CoopString = c.p_CoopParent == null ? null : c.p_CoopParent.p_actorID;
//		c.j_ActorData.updateData(c.p_actorID, c.p_actorType, c.p_parentActorID, c.p_energySupplier.toString(), c.v_ownedGridConnection.toString(), c.v_energySupplierDistrictHeat.toString(), roundToDecimal( c.v_electricityVolume_kWh, 2 ), roundToDecimal( c.v_heatVolume_kWh, 2), roundToDecimal( c.v_methaneVolume_kWh, 2), roundToDecimal( c.v_hydrogenVolume_kWh, 2 ), c.v_electricityContractType.toString(), c.v_heatContractType.toString(), c.v_methaneContractType.toString(), c.v_hydrogenContractType.toString(), roundToDecimal( c.v_balanceElectricity_eur, 2), roundToDecimal( c.v_balanceHeat_eur, 2), roundToDecimal( c.v_balanceMethane_eur, 2), roundToDecimal( c.v_balanceHydrogen_eur, 2));
		
		//traceln("actor : "+  c.p_actorID);
		c.j_ActorData.updateData(
			c.p_actorID, c.p_actorType, c.p_gridOperator.p_actorID, CoopString, c.p_electricitySupplier.p_actorID, c.v_ownedGridConnection.p_gridConnectionID,
			null, roundToDecimal( c.v_totalElectricityUsed_kWh, 2 ), roundToDecimal( c.v_totalHeatUsed_kWh, 2), roundToDecimal( c.v_totalMethaneUsed_kWh, 2), 
			roundToDecimal( c.v_totalHydrogenUsed_kWh, 2 ), roundToDecimal( c.v_totalDieselUsed_kWh, 2 ),		
			roundToDecimal( c.v_balanceElectricity_eur, 2), roundToDecimal( c.v_balanceElectricityDelivery_eur, 2), 
			roundToDecimal( c.v_balanceElectricityTransport_eur, 2), roundToDecimal( c.v_balanceElectricityTax_eur, 2),
			c.v_contractDelivery.contractScope, c.v_contractTransport.contractScope, c.v_contractTax.contractScope,
			b_methaneUsedWithoutContracts, b_hydrogenUsedWithoutContracts);
			
				//c.v_electricityContractType.toString(), c.v_heatContractType.toString(), c.v_methaneContractType.toString(), c.v_hydrogenContractType.toString(), 
		//null, null, null, null,
		
		J_ActorData data = c.j_ActorData;
		j_dataOut.actorData.add(data);
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	// Collect contracts with correnspondings energy/euro totals
	if (b_anylogicCostsForAllContracts) { // Account everything within anylogic, so that for all contracts only financialTransactionVolumes are non-zero, energyVolumes all zero.
		f_calculateContractCosts(c);
	} else { // Old method, which relies on cost-benefit module to calculate financial transactions for methane, hydrogen, diesel and heat.
		for(J_Contract co: c.c_actorContracts) {
			co.contractHolder = c.p_actorID;
			if(co.energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
				if(co.contractType.equals(OL_ContractType.DELIVERY)) {
					co.FinancialTransactionVolume_eur = c.v_balanceElectricityDelivery_eur;
				} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
					co.FinancialTransactionVolume_eur = c.v_balanceElectricityTransport_eur;			
				} else if(co.contractType.equals(OL_ContractType.TAX)) {
					co.FinancialTransactionVolume_eur = c.v_balanceElectricityTax_eur;			
				}
			} else if (co.energyCarrier == OL_EnergyCarriers.METHANE) {
				co.EnergyTransactionVolume_kWh = -c.v_totalMethaneUsed_kWh;
			} else if (co.energyCarrier == OL_EnergyCarriers.HYDROGEN) {
				co.EnergyTransactionVolume_kWh = -c.v_totalHydrogenUsed_kWh;
			} else if (co.energyCarrier == OL_EnergyCarriers.HEAT) {
				co.EnergyTransactionVolume_kWh = -c.v_totalHeatUsed_kWh;
			} else if (co.energyCarrier == OL_EnergyCarriers.DIESEL) {
				co.EnergyTransactionVolume_kWh = -c.v_totalDieselUsed_kWh;
			}		
			j_dataOut.contractData.add(co);
			
		}
	}
//});
}
traceln("Printing all contract data: " + j_dataOut.contractData);
for(EnergyCoop a : pop_energyCoops) {
	a.f_totalFinances();
	try {
		//traceln("ConnectionOwner v_ownedGridConnection: " + a.v_ownedGridConnection.p_gridConnectionID);
		String CoopString = a.p_CoopParent == null ? null : a.p_CoopParent.toString();
		
		a.j_ActorData.updateData(a.p_actorID, a.p_actorType,  a.p_gridOperator.p_actorID, CoopString, a.p_electricitySupplier.p_actorID, a.p_gridNodeUnderResponsibility, 
		null, roundToDecimal( a.v_electricityImported_kWh-a.v_electricityExported_kWh, 2 ), roundToDecimal( a.v_heatImported_kWh-a.v_heatExported_kWh, 2), roundToDecimal( a.v_methaneImported_kWh-a.v_methaneExported_kWh, 2), roundToDecimal( a.v_hydrogenImported_kWh-a.v_hydrogenExported_kWh, 2 ), roundToDecimal( a.v_dieselImported_kWh, 2 ),
		//a.v_electricityContractType.toString(), a.v_heatContractType.toString(), a.v_methaneContractType.toString(), a.v_hydrogenContractType.toString(),
		//null, null, null, null,
		roundToDecimal( a.v_balanceElectricity_eur, 2), roundToDecimal( a.v_balanceElectricityDelivery_eur, 2), roundToDecimal( a.v_balanceElectricityTransport_eur, 2), roundToDecimal( a.v_balanceElectricityTax_eur, 2),
		a.v_contractDelivery.contractScope, a.v_contractTransport.contractScope, a.v_contractTax.contractScope,
		false, false);
		J_ActorData data = a.j_ActorData;
		j_dataOut.actorData.add(data);
	} catch (Exception e) {
		e.printStackTrace();
	}
	
	if (b_anylogicCostsForAllContracts) { // Account everything within anylogic, so that for all contracts only financialTransactionVolumes are non-zero, energyVolumes all zero.
		f_calculateContractCosts(a);
	} else { // Old method, which relies on cost-benefit module to calculate financial transactions for methane, hydrogen, diesel and heat.
	
		for(J_Contract co: a.c_actorContracts) {
			co.contractHolder = a.p_actorID;
			if(co.energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
				if(co.contractType.equals(OL_ContractType.DELIVERY)) {
					co.FinancialTransactionVolume_eur = a.v_balanceElectricityDelivery_eur;
				} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
					co.FinancialTransactionVolume_eur = a.v_balanceElectricityTransport_eur;			
				} else if(co.contractType.equals(OL_ContractType.TAX)) {
					co.FinancialTransactionVolume_eur = a.v_balanceElectricityTax_eur;			
				}
			} else if (co.energyCarrier == OL_EnergyCarriers.METHANE) {
				co.EnergyTransactionVolume_kWh = a.v_methaneImported_kWh-a.v_methaneExported_kWh;
			} else if (co.energyCarrier == OL_EnergyCarriers.HYDROGEN) {
				co.EnergyTransactionVolume_kWh = a.v_hydrogenImported_kWh-a.v_hydrogenExported_kWh;
			} else if (co.energyCarrier == OL_EnergyCarriers.HEAT) {
				co.EnergyTransactionVolume_kWh = a.v_heatImported_kWh-a.v_heatExported_kWh;
			} else if (co.energyCarrier == OL_EnergyCarriers.DIESEL) {
				co.EnergyTransactionVolume_kWh = a.v_dieselImported_kWh;
			}
			j_dataOut.contractData.add(co);
			
		}
	}
//});
}


// Bereken netvlak-data:
//f_sumGridNodeLoads();
//traceln("Electricity imported: "+ v_totalElectricityImport_MWh + " MWh");
//traceln("Electricity exported: "+ v_totalElectricityExport_MWh + " MWh");

/*//Total energy consumption and production
//traceln("");
//traceln("__--** CONSUMPTION PER ASSET **--__");
double energyProduced_MWh = 0;
double electricityProduced_MWh = 0;
double energyConsumed_MWh = 0;
//testing
double energyConsumed_thermalModels_MWh = 0;   //tesing
double totalDistanceTrucks_km = 0;
double deltaThermalEnergySinceStart_MWh = 0;
double totalAmbientHeating_MWh = 0;

for (J_EA e : c_energyAssets) {
	double EnergyUsed_kWh = e.getEnergyUsed_kWh();
	double electricityProduced_kWh = 0;
	
	//energyConsumed_MWh += max(0,EnergyUsed_kWh)/1000;
	//energyProduced_MWh +=max(0,-EnergyUsed_kWh)/1000;
	if (EnergyUsed_kWh > 0) {
	
		if (e instanceof J_EAConversionCurtailer || e instanceof J_EAConversionCurtailerHeat) {
			energyProduced_MWh -= EnergyUsed_kWh/1000;
			v_totalEnergyCurtailed_MWh += EnergyUsed_kWh/1000;
		} else if( e instanceof J_EAConversionGasCHP ) {
			energyConsumed_MWh += EnergyUsed_kWh/1000;
			electricityProduced_kWh = ((J_EAConversionGasCHP)e).getElectricityProduced_kWh();
			electricityProduced_MWh += electricityProduced_kWh/1000;
		}
		else {
			energyConsumed_MWh += EnergyUsed_kWh/1000;
		}
		if( e instanceof J_EAStorageHeat && e.electricityConsumption_kW == 0) {
			energyConsumed_thermalModels_MWh +=  EnergyUsed_kWh/1000;
		}
		if ( e instanceof J_EABuilding ) {
			energyProduced_MWh += ((J_EABuilding)e).energyAbsorbed_kWh/1000;
			deltaThermalEnergySinceStart_MWh += (((J_EABuilding)e).getCurrentTemperature()-20)*((J_EABuilding)e).getHeatCapacity_JpK()/3.6e9;
		}
	} else {
		if( e.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC ||  e.energyAssetType == OL_EnergyAssetType.WINDMILL){
		electricityProduced_MWh -= EnergyUsed_kWh/1000;
		}
		if( e.energyAssetType == OL_EnergyAssetType.METHANE_CHP ) { // Unreachable code; CHP will always have positive EnergyUsed_kWh!
			//electricityProduced_MWh += roundToDecimal(e/1000,2); // Total electricity produced is not stored in CHP asset!
			traceln("CHP electricity production not counted!");
		}
		energyProduced_MWh -= EnergyUsed_kWh/1000;
	}
	if (e instanceof J_EABuilding) {
		totalAmbientHeating_MWh += ((J_EABuilding)e).energyAbsorbed_kWh/1000;
	}
	if (e instanceof J_EAEV) {
		v_totalEnergyChargedOutsideModel_MWh += ((J_EAEV)e).energyChargedOutsideModelArea_kWh/1000;
	}
}

traceln("Total energy absorbed from environment by buildings: %s MWh", totalAmbientHeating_MWh);
traceln("Delta thermal stored energy since start: %s MWh", deltaThermalEnergySinceStart_MWh);
//traceln("Trucks have traveled " + totalDistanceTrucks_km + " km");

traceln("");
traceln("__--** Totals **--__");
traceln("Energy consumed: "+ energyConsumed_MWh + " MWh");
traceln("Energy consumed heatstorages: "+ energyConsumed_thermalModels_MWh + " MWh");
traceln("Energy produced: "+ energyProduced_MWh + " MWh");
//traceln("Electricity produced: "+ electricityProduced_MWh + " MWh (only by solar and wind");

traceln("");
//Total selfconsumption, selfsufficiency

if ( energyProduced_MWh > 0 ){
	v_modelSelfConsumption_fr = 1 - (v_totalElectricityExport_MWh + max(0,v_totalMethaneExport_MWh-v_totalMethaneImport_MWh) + max(0,v_totalHydrogenExport_MWh-v_totalHydrogenImport_MWh))/energyProduced_MWh;
}
traceln("Energy selfconsumption: " + v_modelSelfConsumption_fr*100 + "%");
double totalElectricitySelfConsumption_fr = 0;
if ( electricityProduced_MWh > 0 ){
	totalElectricitySelfConsumption_fr = 1 - v_totalElectricityExport_MWh/electricityProduced_MWh;
}

v_modelSelfSufficiency_fr = 1 - (v_totalElectricityImport_MWh + max(0,v_totalMethaneImport_MWh - v_totalMethaneExport_MWh) + 
max(0,v_totalHydrogenImport_MWh - v_totalHydrogenExport_MWh) + v_totalDieselImport_MWh - v_batteryStoredEnergyDeltaSinceStart_MWh - deltaThermalEnergySinceStart_MWh) / energyConsumed_MWh;
traceln("Energy selfsufficiency (via import calc): %s %%", v_modelSelfSufficiency_fr*100);
double totalSelfSufficiency_fr_check = (energyProduced_MWh - (v_totalElectricityExport_MWh + max(0,v_totalMethaneExport_MWh-v_totalMethaneImport_MWh) + 
max(0,v_totalHydrogenExport_MWh-v_totalHydrogenImport_MWh)))/energyConsumed_MWh;
traceln("Energy selfsufficiency (via export calc): %s %%", totalSelfSufficiency_fr_check*100);
// Remaining difference due to different temps of houses start vs end?

traceln( "import electricity: " + v_totalElectricityImport_MWh + " MWh");
traceln( "export electricity: " + v_totalElectricityExport_MWh + " MWh");
traceln( "nett import methane: " + (v_totalMethaneImport_MWh-v_totalMethaneExport_MWh) + " MWh");
traceln( "import diesel: " + v_totalDieselImport_MWh + " MWh");

traceln("MS node peak load: " + v_gridNodePeakLoadElectricityMSLS_kW + " kW at hour: " + c_timesOfNodePeakLoads_h);
*/
double nbcars = 0;
double nbevs = 0;
double shareElectricvehiclesInHouseholds;

for (GCHouse h : Houses){
	nbcars = nbcars + h.c_vehicleAssets.size(); //c_vehicleAssets has size 0 if it has an electric vehicles. So this is not double counting
	if (h.p_householdEV != null){
		nbevs++;
		nbcars++;
	}
}
if (nbcars != 0){
	shareElectricvehiclesInHouseholds = nbevs/nbcars;
}
else {
	shareElectricvehiclesInHouseholds = 0;
}
traceln("share EVs " + nbevs / nbcars);
traceln("nb EVs " + nbevs);

// add Total Cost KPI data:
j_simulationResults.updateData( roundToDecimal( v_gridNodePeakLoadElectricityHSMS_kW, 2 ),
							roundToDecimal( v_gridNodePeakLoadElectricityMSLS_kW, 2 ),
							v_cumulativeGridCapacityHSMS_kW,
							v_cumulativeGridCapacityMSLS_kW,
							v_netOverloadKPI_pct,
							c_timesOfNodePeakLoads_h,
							c_gridConnectionOverload_fr,
							v_MSLSnodePeakPositiveLoadElectricity_kW,
							v_MSLSnodePeakNegativeLoadElectricity_kW,
							v_totalElectricityImport_MWh,
							v_totalElectricityExport_MWh,
							v_totalMethaneImport_MWh,
							v_totalMethaneExport_MWh,
							v_totalHydrogenImport_MWh,
							v_totalHydrogenExport_MWh,
							v_totalDieselImport_MWh,
							c_totalBatteryUnitsInstalled,
							c_totalBatteryChargeAmount_MWh,
							c_totalBatteryDischargeAmount_MWh,
							c_totalBatteryInstalledCapacity_MWh,
							//c_globalElectricityImportProfile_MWhph,
							//c_globalElectricityExportProfile_MWhph,
							//c_globalEVChargingProfile_kWhph,
							//c_globalEHGVChargingProfile_kWhph,
							//c_globalBatteryChargingProfile_kWhph,
							v_modelSelfConsumption_fr,
							v_modelSelfSufficiency_fr,
							v_totalEnergyUsed_MWh,
							v_totalEnergyProduced_MWh,
							v_totalEnergyCurtailed_MWh,
							shareElectricvehiclesInHouseholds
							);

j_dataOut.simulationResults.add(j_simulationResults);

j_hourlyCurvesData.updateData( c_globalElectricityImportProfile_MW,
							c_globalElectricityExportProfile_MW,
							c_globalEVChargingProfile_kW,
							c_globalEHGVChargingProfile_kW,
							c_globalBatteryChargingProfile_kW);
							
j_dataOut.hourlyCurvesData.add(j_hourlyCurvesData);
double modelRunDuration_s = 0;
j_experimentSettingsData.updateData( p_timeStep_h+"",
									v_timeStepsElapsed+"",
									time(HOUR) + "",
									null + "",
									modelRunDuration_s + "",
									pop_gridNodes.size() + "",
									c_gridConnections.size() + "",
									c_energyAssets.size() + "",
									pop_connectionOwners.size() + "",
									pop_energySuppliers.size() + "",
									pop_energyCoops.size() + "",
									pop_gridOperators.size() + "",
									1 + "", 
									shareElectricvehiclesInHouseholds  );
									
j_dataOut.runSettingsData.add(j_experimentSettingsData);

//traceln("Netload " + v_gridNodePeakLoadElectricityMSLS_kW/pop_gridNodes.get(0).p_capacity_kW * 100 + " %" );
traceln("Model run duration: " + modelRunDuration_s + " seconds");

    //System.out.println(json);

String agentDataJson = "";
String settingsJson = "";
String simulationResultsJson = "";
String hourlyCurvesDataJson = "";
String contractDataJson = "";

try {
	agentDataJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( j_dataOut.actorData );
	settingsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( j_dataOut.runSettingsData );
	simulationResultsJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( j_dataOut.simulationResults );
	hourlyCurvesDataJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( j_dataOut.hourlyCurvesData );
	contractDataJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString( j_dataOut.contractData );
	update_o_outputActorData( agentDataJson );
	update_o_outputSettings( settingsJson );
	update_o_simulationResults( simulationResultsJson );
	update_o_hourlyCurvesData ( hourlyCurvesDataJson );
	update_o_contractData ( contractDataJson );
} catch(IOException e) {
	e.printStackTrace();
}

traceln("");
traceln("");
try {
    if(writeToDisk) {
    	//traceln("Succes.... YES!");
    	mapper.writerWithDefaultPrettyPrinter().writeValue(
		new File(".\\OutputAgentData.json"), j_dataOut.actorData );

	   	mapper.writerWithDefaultPrettyPrinter().writeValue(
		new File(".\\OutputRunSettingsData.json"), j_dataOut.runSettingsData );
//		traceln("file succes");

		mapper.writerWithDefaultPrettyPrinter().writeValue(
		new File(".\\OutputSimulationResults.json"), j_dataOut.simulationResults );

		mapper.writerWithDefaultPrettyPrinter().writeValue(
		new File(".\\ContractData.json"), j_dataOut.contractData );
		
		mapper.writerWithDefaultPrettyPrinter().writeValue(
		new File(".\\HourlyCurves.json"), j_dataOut.hourlyCurvesData );

	}
} catch (IOException e) {
//   traceln("exception generating output");
    e.printStackTrace();
}

return 1;

/*ALCODEEND*/}

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
b_isSummerWeek = t_h >= p_startHourSummerWeek && t_h < p_startHourSummerWeek + 24*7;
b_isWinterWeek = t_h >= p_startHourWinterWeek && t_h < p_startHourWinterWeek + 24*7;
b_isLastTimeStepOfDay = t_h % 24 == (24-p_timeStep_h);
t_hourOfDay = t_h % 24; // Assumes modelrun starts at midnight.


v_currentAmbientTemperature_degC = tf_ambientTemperature_degC(t_h);
c_profiles.forEach(p -> p.updateValue(t_h));
v_currentWindPowerNormalized_r = pp_windOnshoreProduction.getCurrentValue();
v_currentSolarPowerNormalized_r = pp_solarPVproduction.getCurrentValue();
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

// Update environmental conditions for relevant energy assets
for( J_EA e : c_ambientAirDependentAssets ) {
	if( e instanceof J_EABuilding ) {
		((J_EABuilding)e).updateSolarRadiation(v_currentSolarPowerNormalized_r*1000);
	}
	if( e instanceof J_EAStorageHeat) { // includes J_EABuilding
		((J_EAStorageHeat)e).updateAmbientTemperature( v_currentAmbientTemperature_degC );		
	}
	if (e instanceof J_EAConversionHeatPump) {
		((J_EAConversionHeatPump)e).updateAmbientTemperature( v_currentAmbientTemperature_degC );		
	}
}

// Update forecasts,  the relevant profile pointers are already updated above
c_forecasts.forEach(f -> f.updateForecast(t_h));
v_SolarYieldForecast_fr = pf_solarPVproduction.getForecast();
v_WindYieldForecast_fr = pf_windOnshoreProduction.getForecast();
// The ElectricityYieldForecast assumes solar and wind forecasts have the same forecast time
if ( v_totalInstalledPVPower_kW + v_totalInstalledWindPower_kW > 0 ) {
	v_electricityYieldForecast_fr = (v_SolarYieldForecast_fr * v_totalInstalledPVPower_kW + v_WindYieldForecast_fr * v_totalInstalledWindPower_kW) / (v_totalInstalledPVPower_kW + v_totalInstalledWindPower_kW);
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

double f_sumMolecularEnergyFlows()
{/*ALCODESTART::1666945497935*/
// Get import/export from balance arrays.

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_totalImports_MWh.put( EC, am_totalBalanceAccumulators_kW.get(EC).getSumPos() * p_timeStep_h / 1000 );
	fm_totalExports_MWh.put( EC, -am_totalBalanceAccumulators_kW.get(EC).getSumNeg() * p_timeStep_h / 1000 );
}

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
		v_totalInstalledBatteryStorageCapacity_MWh += e.getStorageCapacity_kWh() / 1000;
		v_totalBatteryEnergyUsed_MWh += e.getEnergyUsed_kWh() / 1000;
	}
	
	if( ea instanceof J_EAEV ) {
		J_EAEV e = (J_EAEV)ea;
		v_totalBatteryDischargeAmount_MWh += e.getTotalDischargeAmount_kWh() / 1000;
		v_totalBatteryChargeAmount_MWh += e.getTotalChargeAmount_kWh() / 1000;
		v_totalInstalledBatteryStorageCapacity_MWh += e.getStorageCapacity_kWh() / 1000;
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
pf_windOnshoreProduction = new J_ProfileForecaster(null, pp_windOnshoreProduction, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_windOnshoreProduction);
pf_solarPVproduction = new J_ProfileForecaster(null, pp_solarPVproduction, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_solarPVproduction);

pp_dayAheadElectricityPricing_eurpMWh = new J_ProfilePointer("Day ahead electricity pricing [eur/MWh]", tf_dayAheadElectricityPricing_eurpMWh);
c_profiles.add(pp_dayAheadElectricityPricing_eurpMWh);
pf_dayAheadElectricityPricing_eurpMWh = new J_ProfileForecaster(null, pp_dayAheadElectricityPricing_eurpMWh, p_forecastTime_h, t_h, p_timeStep_h);
c_forecasts.add(pf_dayAheadElectricityPricing_eurpMWh);


/*ALCODEEND*/}

double f_calculateContractCosts(Actor a)
{/*ALCODESTART::1682943799138*/
double v_totalMethaneUsed_kWh = 0;
double v_totalDieselUsed_kWh = 0;
double v_totalHydrogenUsed_kWh = 0;
double v_totalHeatUsed_kWh = 0;

double v_balanceElectricityDelivery_eur = 0;
double v_balanceElectricityTransport_eur = 0;
double v_balanceElectricityTax_eur = 0;

String p_actorID = "";
ArrayList<J_Contract> c_actorContracts = null;

if (a instanceof ConnectionOwner) {
	ConnectionOwner c = (ConnectionOwner)a;
	p_actorID = c.p_actorID;
	c_actorContracts = c.c_actorContracts;
	v_totalMethaneUsed_kWh = c.v_totalMethaneUsed_kWh;
	v_totalDieselUsed_kWh = c.v_totalDieselUsed_kWh;
	v_totalHydrogenUsed_kWh = c.v_totalHydrogenUsed_kWh;
	v_totalHeatUsed_kWh = c.v_totalHeatUsed_kWh;
	v_balanceElectricityDelivery_eur = c.v_balanceElectricityDelivery_eur;
	v_balanceElectricityTransport_eur = c.v_balanceElectricityTransport_eur;
	v_balanceElectricityTax_eur = c.v_balanceElectricityTax_eur;
} else {
	EnergyCoop c = (EnergyCoop)a;
	p_actorID = c.p_actorID;
	c_actorContracts = c.c_actorContracts;
	v_totalMethaneUsed_kWh = c.v_methaneImported_kWh - c.v_methaneExported_kWh;
	v_totalDieselUsed_kWh = c.v_dieselImported_kWh;
	v_totalHydrogenUsed_kWh = c.v_hydrogenImported_kWh - c.v_hydrogenExported_kWh;
	v_totalHeatUsed_kWh = c.v_heatImported_kWh - c.v_heatExported_kWh;
	v_balanceElectricityDelivery_eur = c.v_balanceElectricityDelivery_eur;
	v_balanceElectricityTransport_eur = c.v_balanceElectricityTransport_eur;
	v_balanceElectricityTax_eur = c.v_balanceElectricityTax_eur;
}

J_DeliveryContract methaneDeliveryContract = null;
J_DeliveryContract hydrogenDeliveryContract = null;
J_DeliveryContract dieselDeliveryContract = null;
J_DeliveryContract heatDeliveryContract = null;

J_TaxContract methaneTaxContract = null;
J_TaxContract hydrogenTaxContract = null;
J_TaxContract dieselTaxContract = null;
J_TaxContract heatTaxContract = null;

J_TransportContract methaneTransportContract = null;
J_TransportContract hydrogenTransportContract = null;
J_TransportContract dieselTransportContract = null;
J_TransportContract heatTransportContract = null;

J_ConnectionContract methaneConnectionContract = null;
J_ConnectionContract hydrogenConnectionContract = null;
J_ConnectionContract dieselConnectionContract = null;
J_ConnectionContract heatConnectionContract = null;


//for(J_Contract co: c.c_actorContracts) {
for( int idx = 0 ; idx<c_actorContracts.size() ; idx++) {
	J_Contract co = c_actorContracts.get(idx);
	co.contractHolder = p_actorID;	
	
	if(co.energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
		if(co.contractType.equals(OL_ContractType.DELIVERY)) {
			co.FinancialTransactionVolume_eur = -v_balanceElectricityDelivery_eur;
		} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
			co.FinancialTransactionVolume_eur = -v_balanceElectricityTransport_eur;			
		} else if(co.contractType.equals(OL_ContractType.TAX)) {
			co.FinancialTransactionVolume_eur = -v_balanceElectricityTax_eur;			
		}
		co.FinancialTransactionVolume_eur += co.annualFee_eur;
		j_dataOut.contractData.add(co);
	} else if (co.energyCarrier == OL_EnergyCarriers.METHANE) {
		if(co.contractType.equals(OL_ContractType.DELIVERY)) {
				methaneDeliveryContract = (J_DeliveryContract)co;
				methaneDeliveryContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
				methaneTransportContract = (J_TransportContract) co;
				methaneTransportContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TAX)) {
				methaneTaxContract = (J_TaxContract)co;
				methaneTaxContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.CONNECTION)) {
				methaneConnectionContract = (J_ConnectionContract)co;
				methaneConnectionContract.idx = idx;
			}
			//co.EnergyTransactionVolume_kWh = -c.v_totalMethaneUsed_kWh;
	} else if (co.energyCarrier == OL_EnergyCarriers.HYDROGEN) {
		if(co.contractType.equals(OL_ContractType.DELIVERY)) {
				hydrogenDeliveryContract = (J_DeliveryContract)co;
				hydrogenDeliveryContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
				hydrogenTransportContract = (J_TransportContract)co;
				hydrogenTransportContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TAX)) {
				hydrogenTaxContract = (J_TaxContract)co;
				hydrogenTaxContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.CONNECTION)) {
				hydrogenConnectionContract = (J_ConnectionContract)co;
				hydrogenConnectionContract.idx = idx;
			}
		//co.EnergyTransactionVolume_kWh = -c.v_totalHydrogenUsed_kWh;
	} else if (co.energyCarrier == OL_EnergyCarriers.HEAT) {
		if(co.contractType.equals(OL_ContractType.DELIVERY)) {
				heatDeliveryContract = (J_DeliveryContract)co;
				heatDeliveryContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
				heatTransportContract = (J_TransportContract)co;
				heatTransportContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TAX)) {
				heatTaxContract = (J_TaxContract)co;
				heatTaxContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.CONNECTION)) {
				heatConnectionContract = (J_ConnectionContract)co;
				heatConnectionContract.idx = idx;
			}
		//co.EnergyTransactionVolume_kWh = -c.v_totalHeatUsed_kWh;
	} else if (co.energyCarrier == OL_EnergyCarriers.DIESEL) {
		if(co.contractType.equals(OL_ContractType.DELIVERY)) {
				dieselDeliveryContract = (J_DeliveryContract)co;
				dieselDeliveryContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TRANSPORT)) {
				dieselTransportContract = (J_TransportContract)co;
				dieselTransportContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.TAX)) {
				dieselTaxContract = (J_TaxContract)co;
				dieselTaxContract.idx = idx;
			} else if(co.contractType.equals(OL_ContractType.CONNECTION)) {
				dieselConnectionContract = (J_ConnectionContract)co;
				dieselConnectionContract.idx = idx;
			}
		//co.EnergyTransactionVolume_kWh = -c.v_totalDieselUsed_kWh;
	}		
}

f_contractCostsPerEnergyCarrier(v_totalMethaneUsed_kWh, methaneDeliveryContract, methaneConnectionContract, methaneTransportContract, methaneTaxContract);
f_contractCostsPerEnergyCarrier(v_totalDieselUsed_kWh, dieselDeliveryContract, dieselConnectionContract, dieselTransportContract, dieselTaxContract);
f_contractCostsPerEnergyCarrier(v_totalHydrogenUsed_kWh, hydrogenDeliveryContract, hydrogenConnectionContract, hydrogenTransportContract, hydrogenTaxContract);
f_contractCostsPerEnergyCarrier(v_totalHeatUsed_kWh, heatDeliveryContract, heatConnectionContract, heatTransportContract, heatTaxContract);

/*
//// Handle all methane costs
double methaneTotalCostsForVAT_eur = 0;
if (methaneDeliveryContract!= null) {
	if (v_totalMethaneUsed_kWh > 0) {
		methaneDeliveryContract.FinancialTransactionVolume_eur = v_totalMethaneUsed_kWh * methaneDeliveryContract.deliveryPrice_eurpkWh;
	} else {
		methaneDeliveryContract.FinancialTransactionVolume_eur = v_totalMethaneUsed_kWh * methaneDeliveryContract.feedinPrice_eurpkWh;
	}
	methaneDeliveryContract.FinancialTransactionVolume_eur += methaneDeliveryContract.annualFee_eur;
	methaneTotalCostsForVAT_eur += methaneDeliveryContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)methaneDeliveryContract);
}
if (methaneTransportContract!= null) {
	// Transport tariffs don't apply for methane
	methaneTransportContract.FinancialTransactionVolume_eur += methaneTransportContract.annualFee_eur;
	methaneTotalCostsForVAT_eur += methaneTransportContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)methaneTransportContract);
}
if (methaneConnectionContract!= null) {	
	// Connection contracts only have annualFee, no volume-dependent costs
	methaneConnectionContract.FinancialTransactionVolume_eur += methaneConnectionContract.annualFee_eur;
	methaneTotalCostsForVAT_eur += methaneConnectionContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)methaneTransportContract);
}
if (methaneTaxContract != null) {
	if (v_totalMethaneUsed_kWh > 0) {
		methaneTaxContract.FinancialTransactionVolume_eur = v_totalMethaneUsed_kWh * methaneTaxContract.deliveryTax_eurpkWh;
	} else {
		methaneTaxContract.FinancialTransactionVolume_eur = v_totalMethaneUsed_kWh * methaneTaxContract.feedinTax_eurpkWh;
	}
	methaneTaxContract.FinancialTransactionVolume_eur += methaneTaxContract.annualFee_eur;
	methaneTotalCostsForVAT_eur += methaneTaxContract.FinancialTransactionVolume_eur;
	// Apply VAT to everything (effectively saldering of everything! Not really correct for methane...)
	methaneTaxContract.FinancialTransactionVolume_eur += methaneTotalCostsForVAT_eur * methaneTaxContract.proportionalTax_pct + methaneTaxContract.annualFee_eur;
	j_dataOut.contractData.add((J_Contract)methaneTaxContract);
}

//// Handle all diesel costs
double dieselTotalCostsForVAT_eur = 0;
if (dieselDeliveryContract!= null) {
	if (v_totalDieselUsed_kWh > 0) {
		dieselDeliveryContract.FinancialTransactionVolume_eur = v_totalDieselUsed_kWh * dieselDeliveryContract.deliveryPrice_eurpkWh;
	} else {
		dieselDeliveryContract.FinancialTransactionVolume_eur = v_totalDieselUsed_kWh * dieselDeliveryContract.feedinPrice_eurpkWh;
	}
	dieselDeliveryContract.FinancialTransactionVolume_eur += dieselDeliveryContract.annualFee_eur;
	dieselTotalCostsForVAT_eur += dieselDeliveryContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)dieselDeliveryContract);
}
if (dieselTransportContract!= null) {
	// Transport tariffs don't apply for diesel
	dieselTransportContract.FinancialTransactionVolume_eur += dieselTransportContract.annualFee_eur;
	dieselTotalCostsForVAT_eur += dieselTransportContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)dieselTransportContract);
}
if (dieselConnectionContract!= null) {	
	// Connection contracts only have annualFee, no volume-dependent costs
	dieselConnectionContract.FinancialTransactionVolume_eur += dieselConnectionContract.annualFee_eur;
	dieselTotalCostsForVAT_eur += dieselConnectionContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)dieselTransportContract);
}
if (dieselTaxContract != null) {
	if (v_totalDieselUsed_kWh > 0) {
		dieselTaxContract.FinancialTransactionVolume_eur = v_totalDieselUsed_kWh * dieselTaxContract.deliveryTax_eurpkWh;
	} else {
		dieselTaxContract.FinancialTransactionVolume_eur = v_totalDieselUsed_kWh * dieselTaxContract.feedinTax_eurpkWh;
	}
	dieselTaxContract.FinancialTransactionVolume_eur += dieselTaxContract.annualFee_eur;
	dieselTotalCostsForVAT_eur += dieselTaxContract.FinancialTransactionVolume_eur;

	// Apply VAT to everything (effectively saldering of everything! Not really correct for diesel...)
	dieselTaxContract.FinancialTransactionVolume_eur += dieselTotalCostsForVAT_eur * dieselTaxContract.proportionalTax_pct + dieselTaxContract.annualFee_eur;
	j_dataOut.contractData.add((J_Contract)dieselTaxContract);
}

//// Handle all hydrogen costs
double hydrogenTotalCostsForVAT_eur = 0;
if (hydrogenDeliveryContract!= null) {
	if (v_totalHydrogenUsed_kWh > 0) {
		hydrogenDeliveryContract.FinancialTransactionVolume_eur = v_totalHydrogenUsed_kWh * hydrogenDeliveryContract.deliveryPrice_eurpkWh;
	} else {
		hydrogenDeliveryContract.FinancialTransactionVolume_eur = v_totalHydrogenUsed_kWh * hydrogenDeliveryContract.feedinPrice_eurpkWh;
	}
	hydrogenDeliveryContract.FinancialTransactionVolume_eur += hydrogenDeliveryContract.annualFee_eur;
	hydrogenTotalCostsForVAT_eur += hydrogenDeliveryContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)hydrogenDeliveryContract);
}
if (hydrogenTransportContract!= null) {
	// Transport tariffs don't apply for hydrogen
	hydrogenTransportContract.FinancialTransactionVolume_eur += hydrogenTransportContract.annualFee_eur;
	hydrogenTotalCostsForVAT_eur += hydrogenTransportContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)hydrogenTransportContract);
}
if (hydrogenConnectionContract!= null) {	
	// Connection contracts only have annualFee, no volume-dependent costs
	hydrogenConnectionContract.FinancialTransactionVolume_eur += hydrogenConnectionContract.annualFee_eur;
	hydrogenTotalCostsForVAT_eur += hydrogenConnectionContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)hydrogenTransportContract);
}
if (hydrogenTaxContract != null) {
	if (v_totalHydrogenUsed_kWh > 0) {
		hydrogenTaxContract.FinancialTransactionVolume_eur = v_totalHydrogenUsed_kWh * hydrogenTaxContract.deliveryTax_eurpkWh;
	} else {
		hydrogenTaxContract.FinancialTransactionVolume_eur = v_totalHydrogenUsed_kWh * hydrogenTaxContract.feedinTax_eurpkWh;
	}
	hydrogenTaxContract.FinancialTransactionVolume_eur += hydrogenTaxContract.annualFee_eur ;
	hydrogenTotalCostsForVAT_eur += hydrogenTaxContract.FinancialTransactionVolume_eur
	
	// Apply VAT to everything (effectively saldering of everything! Not really correct for hydrogen...)
	hydrogenTaxContract.FinancialTransactionVolume_eur += hydrogenTotalCostsForVAT_eur * hydrogenTaxContract.proportionalTax_pct + hydrogenTaxContract.annualFee_eur;
	j_dataOut.contractData.add((J_Contract)hydrogenTaxContract);
}

//// Handle all heat costs
double heatTotalCostsForVAT_eur = 0;
if (heatDeliveryContract!= null) {
	if (v_totalHeatUsed_kWh > 0) {
		heatDeliveryContract.FinancialTransactionVolume_eur = v_totalHeatUsed_kWh * heatDeliveryContract.deliveryPrice_eurpkWh;
	} else {
		heatDeliveryContract.FinancialTransactionVolume_eur = v_totalHeatUsed_kWh * heatDeliveryContract.feedinPrice_eurpkWh;
	}
	heatDeliveryContract.FinancialTransactionVolume_eur += heatDeliveryContract.annualFee_eur;
	heatTotalCostsForVAT_eur += heatDeliveryContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)heatDeliveryContract);
}
if (heatTransportContract!= null) {
	// Transport tariffs don't apply for heat
	heatTransportContract.FinancialTransactionVolume_eur += heatTransportContract.annualFee_eur;
	heatTotalCostsForVAT_eur += heatTransportContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)heatTransportContract);
}
if (heatConnectionContract!= null) {	
	// Connection contracts only have annualFee, no volume-dependent costs
	heatConnectionContract.FinancialTransactionVolume_eur += heatConnectionContract.annualFee_eur;
	heatTotalCostsForVAT_eur += heatConnectionContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)heatTransportContract);
}
if (heatTaxContract != null) {
	if (v_totalHeatUsed_kWh > 0) {
		heatTaxContract.FinancialTransactionVolume_eur = v_totalHeatUsed_kWh * heatTaxContract.deliveryTax_eurpkWh;
	} else {
		heatTaxContract.FinancialTransactionVolume_eur = v_totalHeatUsed_kWh * heatTaxContract.feedinTax_eurpkWh;
	}
	heatTotalCostsForVAT_eur += heatTaxContract.FinancialTransactionVolume_eur + heatTaxContract.annualFee_eur ;
	// Apply VAT to everything (effectively saldering of everything! Not really correct for heat...)
	heatTaxContract.FinancialTransactionVolume_eur += heatTotalCostsForVAT_eur * heatTaxContract.proportionalTax_pct + heatTaxContract.annualFee_eur;
	j_dataOut.contractData.add((J_Contract)heatTaxContract);
}
//j_dataOut.contractData.add(co);
*/
/*ALCODEEND*/}

double f_contractCostsPerEnergyCarrier(double energyUsed_kWh,J_DeliveryContract deliveryContract,J_ConnectionContract connectionContract,J_TransportContract transportContract,J_TaxContract taxContract)
{/*ALCODESTART::1683018740227*/
double totalCostsForVAT_eur = 0;
if (deliveryContract!= null) {
	if (energyUsed_kWh > 0) {
		deliveryContract.FinancialTransactionVolume_eur = energyUsed_kWh * deliveryContract.deliveryPrice_eurpkWh;
	} else {
		deliveryContract.FinancialTransactionVolume_eur = energyUsed_kWh * deliveryContract.feedinPrice_eurpkWh;
	}
	deliveryContract.FinancialTransactionVolume_eur += deliveryContract.annualFee_eur;
	totalCostsForVAT_eur += deliveryContract.FinancialTransactionVolume_eur;

	j_dataOut.contractData.add((J_Contract)deliveryContract);
}
if (transportContract!= null) {
	// Transport tariffs don't apply for energy
	transportContract.FinancialTransactionVolume_eur += transportContract.annualFee_eur;
	totalCostsForVAT_eur += transportContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)transportContract);
	
}
if (connectionContract!= null) {	
	// Connection contracts only have annualFee, no volume-dependent costs
	connectionContract.FinancialTransactionVolume_eur += connectionContract.annualFee_eur;
	totalCostsForVAT_eur += connectionContract.FinancialTransactionVolume_eur;
	j_dataOut.contractData.add((J_Contract)connectionContract);

}
if (taxContract != null) {
	if (energyUsed_kWh > 0) {
		taxContract.FinancialTransactionVolume_eur = energyUsed_kWh * taxContract.deliveryTax_eurpkWh;
	} else {
		taxContract.FinancialTransactionVolume_eur = energyUsed_kWh * taxContract.feedinTax_eurpkWh;
	}
	taxContract.FinancialTransactionVolume_eur += taxContract.annualFee_eur;
	totalCostsForVAT_eur += taxContract.FinancialTransactionVolume_eur;
	// Apply VAT to everything
	taxContract.FinancialTransactionVolume_eur += totalCostsForVAT_eur * taxContract.proportionalTax_pct;
	j_dataOut.contractData.add((J_Contract)taxContract);
}
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
	GC.f_resetStates();
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
	EC.f_resetStates();
}


//t_h=v_runStartTime_h;
int v_timeStepsElapsed_live = v_timeStepsElapsed;
v_timeStepsElapsed=0;

c_profiles.forEach(p -> p.updateValue(p_runStartTime_h));
c_forecasts.forEach(p -> p.initializeForecast(p_runStartTime_h)); 
//c_forecasts.parallelStream().forEach(p -> p.initializeForecast(p_runStartTime_h)); 

// When adding actors, also reset their states! Not used yet for Drechtsteden...

v_isRapidRun = true;
f_resetAnnualValues();

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

// GridConnection KPIs (can these be done on-demand? What is dependency of other KPIs on GC KPI results?
if (b_parallelizeGridConnections) {
	c_gridConnections.parallelStream().forEach(gc -> gc.f_calculateKPIs());
} else {
	c_gridConnections.forEach(gc -> gc.f_calculateKPIs());
}
c_subGridConnections.forEach(gc -> gc.f_calculateKPIs());

for(GridConnection g: c_gridConnections){ // 
	    c_gridConnectionOverload_fr.put(g.p_gridConnectionID, g.v_maxConnectionLoad_fr);
}	

pop_gridNodes.forEach(gn -> gn.f_calculateKPIs()); // This concerns a relatively small collection, so no need for parallelStream.

//traceln("Check methane import from array: %s MWh", Arrays.stream(a_annualMethaneBalance_kW).sum()* p_timeStep_h / 1000);

f_sumMolecularEnergyFlows();
f_sumGridNodeLoads();
f_sumBatteryUse();
f_duurkrommes();

pop_energyCoops.forEach(ec -> ec.f_calculateKPIs()); // Must go after f_sumGridNodeLoads() because it uses total electricity export!

// Totals from accumulators:
v_totalElectricityProduced_MWh = acc_totalElectricityProduction_kW.getSum() * p_timeStep_h / 1000; // Arrays.stream( a_annualElectricityProduction_kW ).sum() * p_timeStep_h / 1000;
v_totalElectricityConsumed_MWh = acc_totalElectricityConsumption_kW.getSum() * p_timeStep_h / 1000; // Arrays.stream( a_annualElectricityConsumption_kW ).sum() * p_timeStep_h / 1000;

v_totalEnergyProduced_MWh = acc_totalEnergyProduction_kW.getSum() * p_timeStep_h / 1000;
v_totalEnergyConsumed_MWh = acc_totalEnergyConsumption_kW.getSum() * p_timeStep_h / 1000;

v_totalEnergyCurtailed_MWh = acc_totalEnergyCurtailed_kW.getSum() * p_timeStep_h / 1000;
v_totalPrimaryEnergyProductionHeatpumps_MWh = acc_totalPrimaryEnergyProductionHeatpumps_kW.getSum() * p_timeStep_h / 1000;


v_totalEnergyImport_MWh = fm_totalImports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();
v_totalEnergyExport_MWh = fm_totalExports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();


// Electricity self consumption
v_individualSelfSufficiency_fr = sum(c_gridConnections, gc -> gc.v_totalElectricitySelfConsumed_MWh) / v_totalElectricityConsumed_MWh;
v_individualSelfConsumption_fr = sum(c_gridConnections, gc -> gc.v_totalElectricitySelfConsumed_MWh) / v_totalElectricityProduced_MWh;
v_totalElectricitySelfConsumed_MWh = max(0, v_totalElectricityConsumed_MWh - fm_totalImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));
v_collectiveSelfConsumption_fr = v_totalElectricitySelfConsumed_MWh / v_totalElectricityProduced_MWh;

//Heat grid
v_totalEnergyConsumptionForDistrictHeating_MWh = fm_totalImports_MWh.get(OL_EnergyCarriers.HEAT) + sum(DistrictHeatingSystems, DH -> DH.v_totalEnergyImport_MWh);

//Heatpump totals
v_totalElectricityConsumptionHeatpumps_MWh = 0;
for(GridConnection GC : c_gridConnections){
	for(int i = 0; i < GC.data_annualHeatPumpElectricityDemand_kW.size(); i++){
		v_totalElectricityConsumptionHeatpumps_MWh += (GC.data_annualHeatPumpElectricityDemand_kW.getY(i)*24)/1000;
	}
}

//Tracelns
traceln("");
traceln("__--** Totals **--__");
traceln("Energy consumed: "+ v_totalEnergyConsumed_MWh + " MWh");
traceln("Energy produced: "+ v_totalEnergyProduced_MWh + " MWh");
traceln("Energy import: "+ v_totalEnergyImport_MWh + " MWh");
traceln("Energy export: "+ v_totalEnergyExport_MWh + " MWh");

// Peak model-wide electricity grid loads
v_totalElectricityPeakImport_kW = max(0, Arrays.stream(am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries()).max().orElse(-1) );
v_totalElectricityPeakExport_kW = max(0, -Arrays.stream(am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries()).min().orElse(-1) );
v_summerWeekElectricityPeakImport_kW = max(0, data_summerWeekNetLoad_kW.getYMax() );
v_summerWeekElectricityPeakExport_kW = max(0, -data_summerWeekNetLoad_kW.getYMin() );
v_winterWeekElectricityPeakImport_kW = max(0, data_winterWeekNetLoad_kW.getYMax() );
v_winterWeekElectricityPeakExport_kW = max(0, -data_winterWeekNetLoad_kW.getYMin() );

//// Calcs summerweek
// Import / Export
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_summerWeekImports_MWh.put( EC, am_summerWeekBalanceAccumulators_kW.get(EC).getSumPos() * p_timeStep_h / 1000 );
	fm_summerWeekExports_MWh.put( EC, -am_summerWeekBalanceAccumulators_kW.get(EC).getSumNeg() * p_timeStep_h / 1000 );
}
v_summerWeekEnergyImport_MWh = fm_summerWeekImports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();
v_summerWeekEnergyExport_MWh = fm_summerWeekExports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();

// Electricity / Energy
v_summerWeekElectricityProduced_MWh = acc_summerWeekElectricityProduction_kW.getIntegral() / 1000;
v_summerWeekElectricityConsumed_MWh = acc_summerWeekElectricityConsumption_kW.getIntegral() / 1000;
v_summerWeekElectricitySelfConsumed_MWh = max(0, v_summerWeekElectricityConsumed_MWh - fm_summerWeekImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_summerWeekEnergyProduced_MWh = acc_summerWeekEnergyProduction_kW.getIntegral()/1000;
v_summerWeekEnergyConsumed_MWh = acc_summerWeekEnergyConsumption_kW.getIntegral()/1000;
v_summerWeekEnergySelfConsumed_MWh = max(0, v_summerWeekEnergyConsumed_MWh - v_summerWeekEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

v_summerWeekEnergyCurtailed_MWh = acc_summerWeekEnergyCurtailed_kW.getIntegral() / 1000;
v_summerWeekPrimaryEnergyProductionHeatpumps_MWh = acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral() / 1000;

/*
double v_summerWeekSelfConsumedEnergyCheck_MWh = v_summerWeekEnergyProduced_MWh - v_summerWeekEnergyExport_MWh;
if (abs(v_summerWeekEnergySelfConsumed_MWh - v_summerWeekSelfConsumedEnergyCheck_MWh) > 0.01) {
	throw new RuntimeException("SelfConsumedEnergy Check for Summer Week Failed.);
}*/


//// Calcs winterweek
// Import / Export
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_winterWeekImports_MWh.put( EC, am_winterWeekBalanceAccumulators_kW.get(EC).getSumPos() * p_timeStep_h / 1000 );
	fm_winterWeekExports_MWh.put( EC, -am_winterWeekBalanceAccumulators_kW.get(EC).getSumNeg() * p_timeStep_h / 1000 );
}
v_winterWeekEnergyImport_MWh = fm_winterWeekImports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();
v_winterWeekEnergyExport_MWh = fm_winterWeekExports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();

// Electricity / Energy
v_winterWeekElectricityProduced_MWh = acc_winterWeekElectricityProduction_kW.getIntegral() / 1000;
v_winterWeekElectricityConsumed_MWh = acc_winterWeekElectricityConsumption_kW.getIntegral() / 1000;
v_winterWeekElectricitySelfConsumed_MWh = max(0,v_winterWeekElectricityConsumed_MWh - fm_winterWeekImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_winterWeekEnergyProduced_MWh = acc_winterWeekEnergyProduction_kW.getIntegral()/1000; //Arrays.stream( Arrays.copyOfRange(a_annualEnergyProduction_kW, startIdx, endIdx) ).sum() * energyModel.p_timeStep_h / 1000;
v_winterWeekEnergyConsumed_MWh = acc_winterWeekEnergyConsumption_kW.getIntegral()/1000; //Arrays.stream( Arrays.copyOfRange(a_annualEnergyConsumption_kW, startIdx, endIdx) ).sum() * energyModel.p_timeStep_h / 1000;
v_winterWeekEnergySelfConsumed_MWh = max(0,v_winterWeekEnergyConsumed_MWh - v_winterWeekEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

v_winterWeekEnergyCurtailed_MWh = acc_winterWeekEnergyCurtailed_kW.getIntegral() / 1000;
v_winterWeekPrimaryEnergyProductionHeatpumps_MWh = acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral() / 1000;

/*
double v_winterWeekSelfConsumedEnergyCheck_MWh = v_winterWeekEnergyProduced_MWh - v_winterWeekEnergyExport_MWh;
if (abs(v_winterWeekEnergySelfConsumed_MWh - v_winterWeekSelfConsumedEnergyCheck_MWh) > 0.01) {
	throw new RuntimeException("SelfConsumedEnergy Check for Winter Week Failed.);
}*/

// Daytime 
for (var EC : v_activeEnergyCarriers){
	fm_daytimeImports_MWh.addFlow(EC, am_daytimeImports_kW.get(EC).getIntegral()/1000);
	fm_daytimeExports_MWh.addFlow(EC, am_daytimeExports_kW.get(EC).getIntegral()/1000);
}
v_daytimeEnergyImport_MWh = fm_daytimeImports_MWh.totalSum();
v_daytimeEnergyExport_MWh = fm_daytimeExports_MWh.totalSum();

v_daytimeElectricityProduced_MWh = acc_daytimeElectricityProduction_kW.getIntegral() / 1000;
v_daytimeElectricityConsumed_MWh =  acc_daytimeElectricityConsumption_kW.getIntegral() / 1000;
v_daytimeElectricitySelfConsumed_MWh = max(0, v_daytimeElectricityConsumed_MWh - am_daytimeImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral()/1000);

v_daytimeEnergyProduced_MWh = acc_daytimeEnergyProduction_kW.getIntegral() / 1000;
v_daytimeEnergyConsumed_MWh =  acc_daytimeEnergyConsumption_kW.getIntegral() / 1000;
v_daytimeEnergySelfConsumed_MWh = max(0, v_daytimeEnergyProduced_MWh - v_daytimeEnergyExport_MWh);

// Nighttime totals: yearly totays minus daytime totals & selfconsumption
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_nighttimeImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - fm_daytimeImports_MWh.get(EC) );
	fm_nighttimeExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - fm_daytimeExports_MWh.get(EC) );
}
v_nighttimeEnergyImport_MWh = v_totalEnergyImport_MWh - v_daytimeEnergyImport_MWh;
v_nighttimeEnergyExport_MWh = v_totalEnergyExport_MWh - v_daytimeEnergyExport_MWh;

v_nighttimeElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_daytimeElectricityProduced_MWh;
v_nighttimeElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_daytimeElectricityConsumed_MWh;
v_nighttimeElectricitySelfConsumed_MWh = max(0,v_nighttimeElectricityConsumed_MWh - fm_nighttimeImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_nighttimeEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_daytimeEnergyProduced_MWh;
v_nighttimeEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_daytimeEnergyConsumed_MWh;
v_nighttimeEnergySelfConsumed_MWh = max(0, v_nighttimeEnergyConsumed_MWh - v_nighttimeEnergyImport_MWh);

// Weekend selfconsumption
for (var EC : v_activeEnergyCarriers){
	fm_weekendImports_MWh.addFlow(EC, am_weekendImports_kW.get(EC).getIntegral()/1000);
	fm_weekendExports_MWh.addFlow(EC, am_weekendExports_kW.get(EC).getIntegral()/1000);
}
v_weekendEnergyImport_MWh = fm_weekendImports_MWh.totalSum();
v_weekendEnergyExport_MWh = fm_weekendExports_MWh.totalSum();

v_weekendElectricityProduced_MWh = acc_weekendElectricityProduction_kW.getIntegral() / 1000;
v_weekendElectricityConsumed_MWh =  acc_weekendElectricityConsumption_kW.getIntegral() / 1000;

v_weekendEnergyProduced_MWh = acc_weekendEnergyProduction_kW.getIntegral() / 1000;
v_weekendEnergyConsumed_MWh =  acc_weekendEnergyConsumption_kW.getIntegral() / 1000;

v_weekendEnergySelfConsumed_MWh = max(0, v_weekendEnergyProduced_MWh - v_weekendEnergyExport_MWh);
v_weekendElectricitySelfConsumed_MWh = max(0, v_weekendElectricityConsumed_MWh - am_weekendImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral()/1000);

// Weekday: totals minus weekend
v_weekdayEnergyExport_MWh = v_totalEnergyExport_MWh - v_weekendEnergyExport_MWh;
v_weekdayEnergyImport_MWh = v_totalEnergyImport_MWh - v_weekendEnergyImport_MWh;
v_weekdayEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_weekendEnergyConsumed_MWh;
v_weekdayEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_weekendEnergyProduced_MWh;
v_weekdayEnergySelfConsumed_MWh = max(0, v_weekendEnergyProduced_MWh - v_weekendEnergyExport_MWh);

v_weekdayElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_weekendElectricityConsumed_MWh;
v_weekdayElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_weekendElectricityProduced_MWh;

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_weekdayImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - fm_weekendImports_MWh.get(EC));
	fm_weekdayExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - fm_weekendExports_MWh.get(EC));
}

v_weekdayEnergySelfConsumed_MWh = max(0, v_weekdayEnergyProduced_MWh - v_weekdayEnergyExport_MWh);
v_weekdayElectricitySelfConsumed_MWh = max(0,v_weekdayElectricityConsumed_MWh - fm_weekdayImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

/*
v_weekdayElectricitySelfConsumed_MWh = max(0, v_weekdayElectricityConsumed_MWh - fm_weekdayImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));
v_weekdayEnergySelfConsumed_MWh =  max(0, v_weekdayEnergyProduced_MWh - v_weekdayEnergyExport_MWh);

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_weekendImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - fm_weekdayImports_MWh.get(EC) );
	fm_weekendExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - fm_weekdayExports_MWh.get(EC) );
}
v_weekendEnergyImport_MWh = v_totalEnergyImport_MWh - v_weekdayEnergyImport_MWh;
v_weekendEnergyExport_MWh = v_totalEnergyExport_MWh - v_weekdayEnergyExport_MWh;

v_weekendElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_weekdayElectricityProduced_MWh;
v_weekendElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_weekdayElectricityConsumed_MWh;
v_weekendElectricitySelfConsumed_MWh = max(0,v_weekendElectricityConsumed_MWh - fm_weekendImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_weekendEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_weekdayEnergyProduced_MWh;
v_weekendEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_weekdayEnergyConsumed_MWh;
v_weekendEnergySelfConsumed_MWh = max(0, v_weekendEnergyConsumed_MWh - v_weekendEnergyImport_MWh);
*/

// *** Total energy balance ***
//double electricityProduced_MWh = 0;
//double totalDistanceTrucks_km = 0;
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
		//double electricityProduced_kWh = 0;

		//energyConsumed_MWh += max(0,EnergyUsed_kWh)/1000;
		//energyProduced_MWh +=max(0,-EnergyUsed_kWh)/1000;
		if (EnergyUsed_kWh > 0) {
		
			/*if (e instanceof J_EAConversionCurtailer || e instanceof J_EAConversionCurtailerHeat) {
				totalEnergyProduced_MWh -= EnergyUsed_kWh/1000;
				totalEnergyCurtailed_MWh += EnergyUsed_kWh/1000;
			} else */
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
			/*if( e.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC ||  e.energyAssetType == OL_EnergyAssetType.WINDMILL){
				electricityProduced_MWh -= EnergyUsed_kWh/1000;
			}*/
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



//traceln("Trucks have traveled " + totalDistanceTrucks_km + " km");

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

if ( v_totalElectricityProduced_MWh > 0 ){
	totalElectricitySelfConsumption_fr = 1 - fm_totalExports_MWh.get(OL_EnergyCarriers.ELECTRICITY)/v_totalElectricityProduced_MWh;
}

//v_modelSelfSufficiency_fr = 1 - (v_totalElectricityImport_MWh + max(0,v_totalMethaneImport_MWh - v_totalMethaneExport_MWh) + max(0,v_totalHydrogenImport_MWh - v_totalHydrogenExport_MWh) + v_totalDieselImport_MWh - v_batteryStoredEnergyDeltaSinceStart_MWh - deltaThermalEnergySinceStart_MWh) / v_totalEnergyUsed_MWh;
v_modelSelfSufficiency_fr = v_totalEnergySelfConsumed_MWh / v_totalEnergyConsumed_MWh; // Calculation based on (total_consumption - total_import) / total_consumption. Positive delta-stored energy is contained in v_totalSelfConsumedEnergy_MWh. 
//v_modelSelfSufficiency_fr = v_totalSelfConsumedEnergy_MWh / totalEnergyUsed_MWh; // Calculation based on (total_consumption - total_import) / total_consumption. Positive delta-stored energy is contained in v_totalSelfConsumedEnergy_MWh. 

traceln("Energy selfsufficiency (via import calc): %s %%", v_modelSelfSufficiency_fr*100);
//double totalSelfSufficiency_fr_check = (v_totalEnergyProduced_MWh - (v_totalElectricityExport_MWh + max(0,v_totalMethaneExport_MWh-v_totalMethaneImport_MWh) + max(0,v_totalHydrogenExport_MWh-v_totalHydrogenImport_MWh)))/v_totalEnergyUsed_MWh; // Calculation based on (total_production - total_export) / total_consumption
//double totalSelfSufficiency_fr_check = v_totalSelfConsumedEnergyCheck_MWh / totalEnergyUsed_MWh; // Calculation based on (total_production - total_export) / total_consumption. Negative delta-stored energy is contained in v_totalSelfConsumedEnergy_MWh. 

// Remaining difference due to different temps of houses start vs end?
traceln("");
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	traceln("Import " + EC.toString() + ": " + fm_totalImports_MWh.get(EC) + " MWh");
	traceln("Export " + EC.toString() + ": " + fm_totalExports_MWh.get(EC) + " MWh");
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

/*
traceln( "import electricity: " + v_totalElectricityImport_MWh + " MWh");
traceln( "export electricity: " + v_totalElectricityExport_MWh + " MWh");
traceln( "nett import methane: " + (v_totalMethaneImport_MWh-v_totalMethaneExport_MWh) + " MWh");
traceln( "import diesel: " + v_totalDieselImport_MWh + " MWh");
traceln( "nett import hydrogen: " + (v_totalHydrogenImport_MWh-v_totalHydrogenExport_MWh) + " MWh");
*/

/*// intStream.parallel experiment works to extract daytime consumption! Now benchmark performance...
double daytimeEnergyUsed_MWh = IntStream.range(0, a_annualDaytimeIdxs.length).parallel().mapToDouble(idx -> a_annualEnergyConsumption_kW[idx]*a_annualDaytimeIdxs[idx]).sum()*p_timeStep_h/1000;
traceln("v_daytimeEnergyUsed_MWh: %s, daytimeEnergyUsed_MWh: %s", v_daytimeEnergyUsed_MWh, daytimeEnergyUsed_MWh);
*/
/*for (int i = 0; i<365; i++) { // Check if accumulator with signal resolution different from timestep still gives consistent results
	if (abs(data_annualElectricityDemand_MWh.getY(i) - acc_annualDailyElectricityDemand_MWh.getTimeSeries()[i]) > 0.0001) {
		traceln("Dataset and accumulator don't agree about daily electricity demand on day no. %s, dataset value: %s, accumulator value: %s", i, data_annualElectricityDemand_MWh.getY(i), acc_annualDailyElectricityDemand_MWh.getTimeSeries()[i]);
	}
}*/

/*if ( abs(acc_annualElectricityBalanceDownsampled_kW.getIntegral()-acc_annualElectricityBalance_kW.getIntegral()) > 0.1 ) { // Check if reduced resolution accumulator gives same integral result!
	traceln("Accumulators with different signal resolution DON'T agree on integral: full-res integral: %s, low-res integral: %s", acc_annualElectricityBalanceDownsampled_kW.getIntegral(), acc_annualElectricityBalance_kW.getIntegral());
} else {
	traceln("Accumulators with different signal resolution AGREE on integral: full-res integral: %s, low-res integral: %s", acc_annualElectricityBalanceDownsampled_kW.getIntegral(), acc_annualElectricityBalance_kW.getIntegral());
}*/

//double nettElectricityArray_kWh = Arrays.stream( a_annualElectricityBalance_kW ).sum() * p_timeStep_h / 1000;
//double nettElectricityAccumulator_kWh = acc_annualElectricityBalance_kW.getSum() * p_timeStep_h / 1000;
//double importElectricityAccumulator_kWh = acc_annualElectricityBalance_kW.getSumPos() * p_timeStep_h / 1000;
//traceln("Test ZeroAccumulator: importElectricityAccumulator_kWh: %s kWh, nettElectricityAccumulator_kWh: %s kWh", importElectricityAccumulator_kWh, nettElectricityAccumulator_kWh);


/*ALCODEEND*/}

double f_rapidRunDataLogging()
{/*ALCODESTART::1699275323325*/
// Total Accumulators
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	am_totalBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC));
}	
	
acc_totalEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
acc_totalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
acc_totalEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
acc_totalElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
acc_totalElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
acc_totalPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);

// Daytime totals
if (b_isDaytime) {
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		am_daytimeImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		am_daytimeExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	acc_daytimeElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	acc_daytimeElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );	
	acc_daytimeEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_daytimeEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

	v_daytimeElectricityPeakImport_kW = max(v_daytimeElectricityPeakImport_kW, v_currentElectricityImport_kW);
	v_daytimeElectricityPeakExport_kW = max(v_daytimeElectricityPeakExport_kW, v_currentElectricityExport_kW);
	
	/*for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		fm_daytimeImports_MWh.addFlow(EC, max( 0, currentBalance_kW) * p_timeStep_h / 1000);
		fm_daytimeExports_MWh.addFlow(EC, max( 0, -currentBalance_kW) * p_timeStep_h / 1000);
	}
	
	//v_daytimeEnergyImport_MWh += v_currentEnergyImport_kW / 1000 * p_timeStep_h;
	//v_daytimeEnergyExport_MWh += v_currentEnergyExport_kW / 1000 * p_timeStep_h;	
		
	v_daytimeElectricityProduced_MWh += fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * p_timeStep_h / 1000;
	v_daytimeElectricityConsumed_MWh += fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * p_timeStep_h / 1000;
	
	v_daytimeEnergyProduced_MWh += v_currentPrimaryEnergyProduction_kW * p_timeStep_h / 1000;
	v_daytimeEnergyConsumed_MWh += v_currentFinalEnergyConsumption_kW * p_timeStep_h / 1000;
	
	*/
}
else {	
	v_nighttimeElectricityPeakImport_kW = max(v_nighttimeElectricityPeakImport_kW, v_currentElectricityImport_kW);
	v_nighttimeElectricityPeakExport_kW = max(v_nighttimeElectricityPeakExport_kW, v_currentElectricityExport_kW);
}

// Weekend totals
if (!b_isWeekday) {	
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		am_weekendImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		am_weekendExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	acc_weekendElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	acc_weekendElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	acc_weekendEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_weekendEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

	v_weekendElectricityPeakImport_kW = max(v_weekendElectricityPeakImport_kW, v_currentElectricityImport_kW);
	v_weekendElectricityPeakExport_kW = max(v_weekendElectricityPeakExport_kW, v_currentElectricityExport_kW);
	/*
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		fm_weekdayImports_MWh.addFlow(EC, max( 0, currentBalance_kW) * p_timeStep_h / 1000);
		fm_weekdayExports_MWh.addFlow(EC, max( 0, -currentBalance_kW) * p_timeStep_h / 1000);
	}
	
	v_weekdayEnergyImport_MWh += v_currentEnergyImport_kW / 1000 * p_timeStep_h;
	v_weekdayEnergyExport_MWh += v_currentEnergyExport_kW / 1000 * p_timeStep_h;
	
	v_weekdayElectricityProduced_MWh += fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * p_timeStep_h / 1000;
	v_weekdayElectricityConsumed_MWh += fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * p_timeStep_h / 1000;
	
	v_weekdayEnergyProduced_MWh += v_currentPrimaryEnergyProduction_kW * p_timeStep_h / 1000;
	v_weekdayEnergyConsumed_MWh += v_currentFinalEnergyConsumption_kW * p_timeStep_h / 1000;
	*/
	
}
else {
	v_weekdayElectricityPeakImport_kW = max(v_weekendElectricityPeakImport_kW, v_currentElectricityImport_kW);
	v_weekdayElectricityPeakExport_kW = max(v_weekendElectricityPeakExport_kW, v_currentElectricityExport_kW);
}

// Further Subdivision of asset types within energy carriers
// TODO: Remove the energy carriers from here. There is also no need to sum over the gridconnections again for those totals
double v_currentBaseloadElectricityDemand_kW = sum(c_gridConnections, x->x.v_fixedConsumptionElectric_kW);
double v_currentHeatPumpElectricityDemand_kW = sum(c_gridConnections, x->x.v_heatPumpElectricityConsumption_kW);
double v_currentElectricVehicleDemand_kW = sum(c_gridConnections, x->max(0,x.v_evChargingPowerElectric_kW));
double v_currentBatteriesDemand_kW = sum(c_gridConnections, x->max(0,x.v_batteryPowerElectric_kW));
double v_currentNaturalGasDemand_kW = sum(c_gridConnections, x -> x.fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.METHANE));
double v_currentDieselDemand_kW = sum(c_gridConnections, x-> x.fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.DIESEL));
double v_currentHydrogenDemand_kW = sum(c_gridConnections, x-> x.fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HYDROGEN));
double v_currentElectrolyserDemand_kW = sum(c_gridConnections, x->x.v_hydrogenElectricityConsumption_kW);
double v_currentCookingDemand_kW = sum(c_gridConnections, x->x.v_electricHobConsumption_kW);
double v_currentDistrictHeatingDemand_kW = sum(c_gridConnections, x->x.v_districtHeatDelivery_kW);

double v_currentPVGeneration_kW = sum(c_gridConnections, x->x.v_pvProductionElectric_kW);
double v_currentWindGeneration_kW = sum(c_gridConnections, x->x.v_windProductionElectric_kW);
double v_currentBatteriesSupply_kW = sum(c_gridConnections, x->max(0,-x.v_batteryPowerElectric_kW));
double v_currentV2GSupply_kW = sum(c_gridConnections, x-> max(0, -x.v_evChargingPowerElectric_kW));
double v_currentCHPElectricityProduction_kW = sum(c_gridConnections, x->x.v_CHPProductionElectric_kW);
double v_currentNaturalGasSupply_kW = sum(c_gridConnections, x-> x.fm_currentProductionFlows_kW.get(OL_EnergyCarriers.METHANE));
double v_currentHydrogenSupply_kW = sum(c_gridConnections, x-> x.fm_currentProductionFlows_kW.get(OL_EnergyCarriers.HYDROGEN));

double v_currentStoredEnergyBatteries_MWh = sum(c_gridConnections, x->x.v_batteryStoredEnergy_kWh)/1000;

//Summer week accumulators & dataSets
if (b_isSummerWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		am_summerWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC));
	}
	
	acc_summerWeekEnergyProduction_kW.addStep( v_currentPrimaryEnergyProduction_kW);
	acc_summerWeekEnergyConsumption_kW.addStep( v_currentFinalEnergyConsumption_kW);
	acc_summerWeekEnergyCurtailed_kW.addStep( v_currentEnergyCurtailed_kW);
	acc_summerWeekElectricityProduction_kW.addStep( fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	acc_summerWeekElectricityConsumption_kW.addStep( fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.addStep( v_currentPrimaryEnergyProductionHeatpumps_kW);	

	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		dsm_summerWeekDemandDataSets_kW.get(EC).add( t_h, fm_currentConsumptionFlows_kW.get(EC) );
		dsm_summerWeekSupplyDataSets_kW.get(EC).add( t_h, fm_currentProductionFlows_kW.get(EC) );
	}
	
	data_summerWeekBaseloadElectricityDemand_kW.add(t_h, sum(c_gridConnections, x->x.v_fixedConsumptionElectric_kW));
	data_summerWeekHeatPumpElectricityDemand_kW.add(t_h, v_currentHeatPumpElectricityDemand_kW);
	data_summerWeekElectricVehicleDemand_kW.add(t_h, v_currentElectricVehicleDemand_kW);
	data_summerWeekBatteriesDemand_kW.add(t_h, v_currentBatteriesDemand_kW);
	data_summerWeekElectrolyserDemand_kW.add(t_h, v_currentElectrolyserDemand_kW);
	data_summerWeekCookingElectricityDemand_kW.add(t_h, v_currentCookingDemand_kW);
	
	data_summerWeekPVGeneration_kW.add(t_h, v_currentPVGeneration_kW);
	data_summerWeekWindGeneration_kW.add(t_h, v_currentWindGeneration_kW);
	data_summerWeekBatteriesSupply_kW.add(t_h, v_currentBatteriesSupply_kW);
	data_summerWeekV2GSupply_kW.add(t_h, v_currentV2GSupply_kW);
	data_summerWeekCHPElectricityProduction_kW.add(t_h, v_currentCHPElectricityProduction_kW);
	
	data_summerWeekBatteryStoredEnergy_MWh.add(t_h, v_currentStoredEnergyBatteries_MWh);
	
	data_summerWeekDistrictHeatingDemand_kW.add(t_h, v_currentDistrictHeatingDemand_kW);
	// TODO: Check if these calls below are costly
	//data_summerWeekDemand_kW.add(t_h, sum(c_energyAssets, x->max(0, x.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY))));
	//data_summerWeekSupply_kW.add(t_h, -sum(c_energyAssets, x->max(0, -x.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY)))); 
	data_summerWeekNetLoad_kW.add(t_h, sum(c_gridNodesTopLevel.stream().filter(x -> x.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY).toList(), x -> x.v_currentLoad_kW));
}

//Winter week accumulators & dataSets
if (b_isWinterWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		am_winterWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC));
	}

	acc_winterWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_winterWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
	acc_winterWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	acc_winterWeekElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	acc_winterWeekElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		dsm_winterWeekDemandDataSets_kW.get(EC).add( t_h, fm_currentConsumptionFlows_kW.get(EC) );
		dsm_winterWeekSupplyDataSets_kW.get(EC).add( t_h, fm_currentProductionFlows_kW.get(EC) );
	}

	data_winterWeekBaseloadElectricityDemand_kW.add(t_h, sum(c_gridConnections, x->x.v_fixedConsumptionElectric_kW));
	data_winterWeekHeatPumpElectricityDemand_kW.add(t_h, v_currentHeatPumpElectricityDemand_kW);
	data_winterWeekElectricVehicleDemand_kW.add(t_h, v_currentElectricVehicleDemand_kW);
	data_winterWeekBatteriesDemand_kW.add(t_h, v_currentBatteriesDemand_kW);
	data_winterWeekElectrolyserDemand_kW.add(t_h, v_currentElectrolyserDemand_kW);
	data_winterWeekCookingElectricityDemand_kW.add(t_h, v_currentCookingDemand_kW);
	
	data_winterWeekPVGeneration_kW.add(t_h, v_currentPVGeneration_kW);
	data_winterWeekWindGeneration_kW.add(t_h, v_currentWindGeneration_kW);
	data_winterWeekBatteriesSupply_kW.add(t_h, v_currentBatteriesSupply_kW);
	data_winterWeekV2GSupply_kW.add(t_h, v_currentV2GSupply_kW);
	data_winterWeekCHPElectricityProduction_kW.add(t_h, v_currentCHPElectricityProduction_kW);
	
	data_winterWeekBatteryStoredEnergy_MWh.add(t_h, v_currentStoredEnergyBatteries_MWh);
	
	data_winterWeekDistrictHeatingDemand_kW.add(t_h, v_currentDistrictHeatingDemand_kW);
	
	
	//TODO: remove, use am_winterweek instead
	data_winterWeekNetLoad_kW.add(t_h, sum(c_gridNodesTopLevel.stream().filter(x -> x.p_energyCarrier == OL_EnergyCarriers.ELECTRICITY).toList(), x -> x.v_currentLoad_kW));
	
}

// Daily Averages
fm_dailyAverageDemand_kW.addFlows(fm_currentConsumptionFlows_kW);

v_dailyBaseloadElectricityDemand_kW += v_currentBaseloadElectricityDemand_kW;
v_dailyHeatPumpElectricityDemand_kW += v_currentHeatPumpElectricityDemand_kW;
v_dailyElectricVehicleDemand_kW += v_currentElectricVehicleDemand_kW;
v_dailyBatteriesDemand_kW += v_currentBatteriesDemand_kW;
v_dailyAverageElectrolyserDemand_kW += v_currentElectrolyserDemand_kW;
v_dailyAverageCookingElectricityDemand_kW += v_currentCookingDemand_kW;
v_dailyDistrictHeatingDemand_kW += v_currentDistrictHeatingDemand_kW;
fm_dailyAverageSupply_kW.addFlows(fm_currentProductionFlows_kW);

v_dailyPVGeneration_kW += v_currentPVGeneration_kW;
v_dailyWindGeneration_kW += v_currentWindGeneration_kW;
v_dailyBatteriesSupply_kW += v_currentBatteriesSupply_kW;
v_dailyV2GSupply_kW += v_currentV2GSupply_kW;
v_dailyFinalEnergyConsumption_kW += v_currentFinalEnergyConsumption_kW;
v_dailyCHPElectricityProduction_kW += v_currentCHPElectricityProduction_kW;
v_dailyBatteryStoredEnergy_MWh += v_currentStoredEnergyBatteries_MWh;

if (b_isLastTimeStepOfDay){
	double timeStepsInOneDay = 24 / p_timeStep_h;
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		dsm_dailyAverageDemandDataSets_kW.get(EC).add( t_h-p_runStartTime_h, fm_dailyAverageDemand_kW.get(EC) / timeStepsInOneDay );
		dsm_dailyAverageSupplyDataSets_kW.get(EC).add( t_h-p_runStartTime_h, fm_dailyAverageSupply_kW.get(EC) / timeStepsInOneDay );
	}
	
	data_annualBaseloadElectricityDemand_kW.add(t_h-p_runStartTime_h, v_dailyBaseloadElectricityDemand_kW / timeStepsInOneDay);
	data_annualHeatPumpElectricityDemand_kW.add(t_h-p_runStartTime_h, v_dailyHeatPumpElectricityDemand_kW / timeStepsInOneDay);
	data_annualElectricVehicleDemand_kW.add(t_h-p_runStartTime_h, v_dailyElectricVehicleDemand_kW / timeStepsInOneDay);
	data_annualBatteriesDemand_kW.add(t_h-p_runStartTime_h, v_dailyBatteriesDemand_kW / timeStepsInOneDay);
	data_annualElectrolyserDemand_kW.add(t_h-p_runStartTime_h, v_dailyAverageElectrolyserDemand_kW / timeStepsInOneDay);
	data_annualCookingElectricityDemand_kW.add(t_h-p_runStartTime_h, v_dailyAverageCookingElectricityDemand_kW / timeStepsInOneDay);
	data_totalFinalEnergyConsumption_kW.add(t_h-p_runStartTime_h, v_dailyFinalEnergyConsumption_kW/timeStepsInOneDay);
	data_annualDistrictHeatingDemand_kW.add(t_h-p_runStartTime_h, v_dailyDistrictHeatingDemand_kW/timeStepsInOneDay);
	
	data_annualPVGeneration_kW.add(t_h-p_runStartTime_h, v_dailyPVGeneration_kW / timeStepsInOneDay);
	data_annualWindGeneration_kW.add(t_h-p_runStartTime_h, v_dailyWindGeneration_kW / timeStepsInOneDay);
	data_annualBatteriesSupply_kW.add(t_h-p_runStartTime_h, v_dailyBatteriesSupply_kW / timeStepsInOneDay);
	data_annualV2GSupply_kW.add(t_h-p_runStartTime_h, v_dailyV2GSupply_kW / timeStepsInOneDay);
	data_annualCHPElectricityProduction_kW.add(t_h-p_runStartTime_h, v_dailyCHPElectricityProduction_kW / timeStepsInOneDay);
	data_annualBatteryStoredEnergy_MWh.add(t_h-p_runStartTime_h, v_dailyBatteryStoredEnergy_MWh/timeStepsInOneDay);
	
		//Yearly totals
	v_totalPVGeneration_MWh += (v_dailyPVGeneration_kW/1000)*p_timeStep_h;
	v_totalWindGeneration_MWh += (v_dailyWindGeneration_kW/1000)*p_timeStep_h;
	
	
	//// Daily totals
	// Demand
	fm_dailyAverageDemand_kW.clear();
	v_dailyBaseloadElectricityDemand_kW = 0;
	v_dailyHeatPumpElectricityDemand_kW = 0;
	v_dailyElectricVehicleDemand_kW = 0;
	v_dailyBatteriesDemand_kW = 0;
	v_dailyAverageElectrolyserDemand_kW = 0;
	v_dailyAverageCookingElectricityDemand_kW = 0;
	v_dailyFinalEnergyConsumption_kW = 0;
	v_dailyDistrictHeatingDemand_kW = 0;
	
	// Supply
	fm_dailyAverageSupply_kW.clear();
	v_dailyPVGeneration_kW = 0;
	v_dailyWindGeneration_kW = 0;
	v_dailyBatteriesSupply_kW = 0;
	v_dailyV2GSupply_kW = 0;
	v_dailyCHPElectricityProduction_kW = 0;
	
	//Other
	v_dailyBatteryStoredEnergy_MWh = 0;
}
/*ALCODEEND*/}

double f_resetAnnualValues()
{/*ALCODESTART::1699958741073*/
//// TOTALS
// Imports / Exports
fm_totalImports_MWh.clear();
fm_totalExports_MWh.clear();
v_totalEnergyImport_MWh = 0;
v_totalEnergyExport_MWh = 0;
v_totalElectricityPeakImport_kW = 0;
v_totalElectricityPeakExport_kW = 0;

// Energy / Electricity
v_totalElectricityProduced_MWh = 0;
v_totalElectricityConsumed_MWh = 0;
v_totalElectricitySelfConsumed_MWh = 0;
v_totalEnergyProduced_MWh = 0;
v_totalEnergyConsumed_MWh = 0;
v_totalEnergySelfConsumed_MWh = 0;

v_totalEnergyCurtailed_MWh = 0;
v_totalPrimaryEnergyProductionHeatpumps_MWh = 0;
v_totalPVGeneration_MWh = 0;
v_totalWindGeneration_MWh = 0;

// Accumulators
am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 8760 );
am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, p_timeStep_h, 8760) );

acc_totalElectricityProduction_kW.reset();
acc_totalElectricityConsumption_kW.reset();
acc_totalEnergyProduction_kW.reset();
acc_totalEnergyConsumption_kW.reset();

acc_totalEnergyCurtailed_kW.reset();
acc_totalPrimaryEnergyProductionHeatpumps_kW.reset();


// Others
acc_totalDLRfactor_f.reset();


//// Summer week
// Imports / Exports
fm_summerWeekImports_MWh.clear();
fm_summerWeekExports_MWh.clear();
v_summerWeekEnergyImport_MWh = 0;
v_summerWeekEnergyExport_MWh = 0;
v_summerWeekElectricityPeakImport_kW = 0;
v_summerWeekElectricityPeakExport_kW = 0;

// Energy / Electricity
v_summerWeekElectricityProduced_MWh = 0;
v_summerWeekElectricityConsumed_MWh = 0;
v_summerWeekElectricitySelfConsumed_MWh = 0;
v_summerWeekEnergyProduced_MWh = 0;
v_summerWeekEnergyConsumed_MWh = 0;
v_summerWeekEnergySelfConsumed_MWh = 0;

v_summerWeekEnergyCurtailed_MWh = 0;
v_summerWeekPrimaryEnergyProductionHeatpumps_MWh = 0;

// Accumulators
am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, p_timeStep_h, 24*7);

acc_summerWeekElectricityProduction_kW.reset();
acc_summerWeekElectricityConsumption_kW.reset();
acc_summerWeekEnergyProduction_kW.reset();
acc_summerWeekEnergyConsumption_kW.reset();

acc_summerWeekEnergyCurtailed_kW.reset();
acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.reset();


//// Winter week
// Imports / Exports
fm_winterWeekImports_MWh.clear();
fm_winterWeekExports_MWh.clear();
v_winterWeekEnergyImport_MWh = 0;
v_winterWeekEnergyExport_MWh = 0;
v_winterWeekElectricityPeakImport_kW = 0;
v_winterWeekElectricityPeakExport_kW = 0;

// Energy / Electricity
v_winterWeekElectricityProduced_MWh = 0;
v_winterWeekElectricityConsumed_MWh = 0;
v_winterWeekElectricitySelfConsumed_MWh = 0;
v_winterWeekEnergyProduced_MWh = 0;
v_winterWeekEnergyConsumed_MWh = 0;
v_winterWeekEnergySelfConsumed_MWh = 0;

v_winterWeekEnergyCurtailed_MWh = 0;
v_winterWeekPrimaryEnergyProductionHeatpumps_MWh = 0;

// Accumulators
am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, p_timeStep_h, 24*7);

acc_winterWeekElectricityProduction_kW.reset();
acc_winterWeekElectricityConsumption_kW.reset();
acc_winterWeekEnergyProduction_kW.reset();
acc_winterWeekEnergyConsumption_kW.reset();

acc_winterWeekEnergyCurtailed_kW.reset();
acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.reset();


//// Daytime
// Imports / Exports
am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));
am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));
fm_daytimeImports_MWh.clear();
fm_daytimeExports_MWh.clear();
v_daytimeEnergyImport_MWh = 0;
v_daytimeEnergyExport_MWh = 0;
v_daytimeElectricityPeakImport_kW = 0;
v_daytimeElectricityPeakExport_kW = 0;

// Energy / Electricity
acc_daytimeElectricityProduction_kW.reset();
acc_daytimeElectricityConsumption_kW.reset();
acc_daytimeEnergyProduction_kW.reset();
acc_daytimeEnergyConsumption_kW.reset();

v_daytimeElectricityProduced_MWh = 0;
v_daytimeElectricityConsumed_MWh = 0;
v_daytimeElectricitySelfConsumed_MWh = 0;
v_daytimeEnergyProduced_MWh = 0;
v_daytimeEnergyConsumed_MWh = 0;
v_daytimeEnergySelfConsumed_MWh = 0;

//// Nighttime
// Imports / Exports
fm_nighttimeImports_MWh.clear();
fm_nighttimeExports_MWh.clear();
v_nighttimeEnergyImport_MWh = 0;
v_nighttimeEnergyExport_MWh = 0;
v_nighttimeElectricityPeakImport_kW = 0;
v_nighttimeElectricityPeakExport_kW = 0;

// Energy / Electricity
v_nighttimeElectricityProduced_MWh = 0;
v_nighttimeElectricityConsumed_MWh = 0;
v_nighttimeElectricitySelfConsumed_MWh = 0;
v_nighttimeEnergyProduced_MWh = 0;
v_nighttimeEnergyConsumed_MWh = 0;
v_nighttimeEnergySelfConsumed_MWh = 0;

//// Weekday
// Imports / Exports
fm_weekdayImports_MWh.clear();
fm_weekdayExports_MWh.clear();
v_weekdayEnergyImport_MWh = 0;
v_weekdayEnergyExport_MWh = 0;
v_weekdayElectricityPeakImport_kW = 0;
v_weekdayElectricityPeakExport_kW = 0;

// Energy / Electricity
v_weekdayElectricityProduced_MWh = 0;
v_weekdayElectricityConsumed_MWh = 0;
v_weekdayElectricitySelfConsumed_MWh = 0;
v_weekdayEnergyProduced_MWh = 0;
v_weekdayEnergyConsumed_MWh = 0;
v_weekdayEnergySelfConsumed_MWh = 0;

//// Weekend
// Imports / Exports
am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 2 / 7  * (p_runEndTime_h - p_runStartTime_h) + 48);
am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 2 / 7 * (p_runEndTime_h - p_runStartTime_h) + 48);
fm_weekendImports_MWh.clear();
fm_weekendExports_MWh.clear();
v_weekendEnergyImport_MWh = 0;
v_weekendEnergyExport_MWh = 0;
v_weekendElectricityPeakImport_kW = 0;
v_weekendElectricityPeakExport_kW = 0;

// Energy / Electricity
acc_weekendElectricityProduction_kW.reset();
acc_weekendElectricityConsumption_kW.reset();
acc_weekendEnergyProduction_kW.reset();
acc_weekendEnergyConsumption_kW.reset();

v_weekendElectricityProduced_MWh = 0;
v_weekendElectricityConsumed_MWh = 0;
v_weekendElectricitySelfConsumed_MWh = 0;
v_weekendEnergyProduced_MWh = 0;
v_weekendEnergyConsumed_MWh = 0;
v_weekendEnergySelfConsumed_MWh = 0;

//// Daily totals
// Demand
fm_dailyAverageDemand_kW.clear();
v_dailyBaseloadElectricityDemand_kW = 0;
v_dailyHeatPumpElectricityDemand_kW = 0;
v_dailyElectricVehicleDemand_kW = 0;
v_dailyBatteriesDemand_kW = 0;
v_dailyAverageCookingElectricityDemand_kW = 0;

// Supply
fm_dailyAverageSupply_kW.clear();
v_dailyPVGeneration_kW = 0;
v_dailyWindGeneration_kW = 0;
v_dailyBatteriesSupply_kW = 0;
v_dailyV2GSupply_kW = 0;

// Others
//v_dailyElectricityProductionAvg_kW = 0;
//v_dailyElectricityConsumptionAvg_kW = 0;
v_dailyNetLoadAvg_kW = 0;

// DataSets


dsm_dailyAverageDemandDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, 365);
dsm_dailyAverageSupplyDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, 365);

dsm_summerWeekDemandDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(24*7 / p_timeStep_h));
dsm_summerWeekSupplyDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(24*7 / p_timeStep_h));
dsm_winterWeekDemandDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(24*7 / p_timeStep_h));
dsm_winterWeekSupplyDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(24*7 / p_timeStep_h));

/*ALCODEEND*/}

double f_duurkrommes()
{/*ALCODESTART::1700560766579*/
//int runStartIdx = 0; //(int)(p_runStartTime_h/p_timeStep_h);
//int runEndIdx = (int)((p_runEndTime_h-p_runStartTime_h)/p_timeStep_h);
if (c_gridNodesTopLevel.size() == 1) { // If there is one top-level gridNode, get load duration curves from that one!
	data_netbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).f_getDuurkromme();
	
	if (b_enableDLR) {
		traceln("Peak relative gridload with DLR, demand: %s %%", data_netbelastingDuurkromme_kW.getY(1) );
		traceln("Peak relative gridload with DLR, supply: %s %%", data_netbelastingDuurkromme_kW.getY(data_netbelastingDuurkromme_kW.size()-1) );
	} else {
		traceln("Peak relative gridload withOUT DLR, demand: %s kW", data_netbelastingDuurkromme_kW.getY(1) );
		traceln("Peak relative gridload withOUT DLR, supply: %s kW", data_netbelastingDuurkromme_kW.getY(data_netbelastingDuurkromme_kW.size()-1) );
	}
	data_netbelastingDuurkrommeVorige_kW = c_gridNodesTopLevel.get(0).data_netbelastingDuurkrommeVorige_kW;
	
	data_winterWeekNetbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).data_winterWeekNetbelastingDuurkromme_kW;
	data_summerWeekNetbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).data_summerWeekNetbelastingDuurkromme_kW;
	data_daytimeNetbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).data_daytimeNetbelastingDuurkromme_kW;
	data_nighttimeNetbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).data_nighttimeNetbelastingDuurkromme_kW;
	data_weekdayNetbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).data_weekdayNetbelastingDuurkromme_kW;
	data_weekendNetbelastingDuurkromme_kW = c_gridNodesTopLevel.get(0).data_weekendNetbelastingDuurkromme_kW;
	
} else {
	J_LoadDurationCurves j_duurkrommes = new J_LoadDurationCurves(am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries(), this);

	data_netbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveTotal_kW;
	data_summerWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveSummer_kW;
	data_winterWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWinter_kW;
	data_daytimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveDaytime_kW;
	data_nighttimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveNighttime_kW;
	data_weekdayNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekday_kW;
	data_weekendNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekend_kW;
	
	/*
	boolean firstRun = true;
	if (data_netbelastingDuurkromme_kW != null) {	
		if (data_netbelastingDuurkrommeVorige_kW != null) { // Not second run either!
			data_netbelastingDuurkrommeVorige_kW.reset();
		} else {
			data_netbelastingDuurkrommeVorige_kW = new DataSet(roundToInt((p_runEndTime_h-p_runStartTime_h)/p_timeStep_h));
		}
		firstRun = false;
	} else {
		data_netbelastingDuurkromme_kW = new DataSet(roundToInt((p_runEndTime_h-p_runStartTime_h)/p_timeStep_h));
		data_summerWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/p_timeStep_h));
		data_winterWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/p_timeStep_h));
		data_daytimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/2/p_timeStep_h));
		data_nighttimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/2/p_timeStep_h));
		data_weekdayNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/7*5/p_timeStep_h)+100);
		data_weekendNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/7*2/p_timeStep_h)+100);
	}
	

	
	double[] a_annualNetLoad_kW = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries().clone();
	int arraySize = roundToInt((p_runEndTime_h-p_runStartTime_h)/p_timeStep_h);// a_annualNetLoad_kW.length;

	//double[] netLoadArray_kW = new double[arraySize];
	//double[] netLoadArrayWinter_kW = new double[data_winterWeekNetbelastingDuurkromme_kW.size()];
	//double[] netLoadArraySummer_kW = new double[data_winterWeekNetbelastingDuurkromme_kW.size()];
	double[] netLoadArrayDay_kW = new double[arraySize/2];
	double[] netLoadArrayNight_kW = new double[arraySize/2];
	double[] netLoadArrayWeekday_kW = new double[arraySize];
	double[] netLoadArrayWeekend_kW = new double[arraySize];
	
	double[] netLoadArraySummer_kW = new double[roundToInt(24*7 / p_timeStep_h)];
	double[] netLoadArrayWinter_kW = new double[roundToInt(24*7 / p_timeStep_h)];
	

	int i_winter=0;
	int i_summer=0;
	int i_day=0;
	int i_night=0;
	int i_weekday=0;
	int i_weekend=0;
	for(int i=runStartIdx; i<runEndIdx ; i++) {
		//netLoadArray_kW[i]=-data_annualBusinessParkNetLoad_kW.getY(i);
		if (!firstRun) {
			data_netbelastingDuurkrommeVorige_kW.add(i*p_timeStep_h,data_netbelastingDuurkromme_kW.getY(i));
		}
		// summer/winter
		if ((p_runStartTime_h + i*p_timeStep_h) % 8760 > p_startHourSummerWeek && (p_runStartTime_h + i*p_timeStep_h) % 8760<= p_startHourSummerWeek+24*7) {
			netLoadArraySummer_kW[i_summer]=-a_annualNetLoad_kW[i];
			i_summer++;
		}
		if ((p_runStartTime_h + i*p_timeStep_h) % 8760 > p_startHourWinterWeek && (p_runStartTime_h + i*p_timeStep_h) % 8760<= p_startHourWinterWeek+24*7) {
			netLoadArrayWinter_kW[i_winter]=-a_annualNetLoad_kW[i];
			i_winter++;
		}
		// day/night
		if (i*p_timeStep_h % 24 > 6 && i*p_timeStep_h % 24 <= 18) { //daytime
			netLoadArrayDay_kW[i_day]=-a_annualNetLoad_kW[i];
			i_day++;
		} else {
			netLoadArrayNight_kW[i_night]=-a_annualNetLoad_kW[i];
			i_night++;
		}
		
		//Weekday/weekend
		if ((p_runStartTime_h + i*p_timeStep_h+3*24) % (24*7) < (24*5)) { // Simulation starts on a Thursday, hence the +3 day offset on t_h
			netLoadArrayWeekday_kW[i_weekday]=-a_annualNetLoad_kW[i];
			i_weekday++;
		} else {
			netLoadArrayWeekend_kW[i_weekend]=-a_annualNetLoad_kW[i];
			i_weekend++;
		}
		//data_netbelastingshistogram_kW.add(data_annualBusinessParkNetLoad_kW.getY(i));
	}
	//traceln("i_weekday: %s", i_weekday);
	//traceln("i_weekend: %s", i_weekend);
	netLoadArrayWeekday_kW = Arrays.copyOfRange(netLoadArrayWeekday_kW, 0, i_weekday);
	netLoadArrayWeekend_kW = Arrays.copyOfRange(netLoadArrayWeekend_kW, 0, i_weekend);
	//Arrays.sort(data_annualBusinessParkNetLoad_kW); Sort is not a thing for datasets...
	// Sort all arrays
	Arrays.parallelSort(a_annualNetLoad_kW); // Is this array used elsewhere?? Because now it's no longer a time-series!
	Arrays.parallelSort(netLoadArraySummer_kW);
	Arrays.parallelSort(netLoadArrayWinter_kW);
	Arrays.parallelSort(netLoadArrayDay_kW);
	Arrays.parallelSort(netLoadArrayNight_kW);
	Arrays.parallelSort(netLoadArrayWeekday_kW);
	Arrays.parallelSort(netLoadArrayWeekend_kW);
	// Write results to datasets
	data_netbelastingDuurkromme_kW.reset();
	for(int i=0; i< arraySize; i++) {
		data_netbelastingDuurkromme_kW.add(i*p_timeStep_h, a_annualNetLoad_kW[arraySize-i-1]);
	}

	//Netbelastingduurkromme winter
	data_winterWeekNetbelastingDuurkromme_kW.reset();
	arraySize = data_winterWeekNetLoad_kW.size();
	for(int i=0; i< arraySize; i++) {
		data_winterWeekNetbelastingDuurkromme_kW.add(i*p_timeStep_h, -netLoadArrayWinter_kW[i]);
	}
	
	//Netbelastingduurkromme summer
	arraySize = data_summerWeekNetLoad_kW.size();
	data_summerWeekNetbelastingDuurkromme_kW.reset();
	for(int i=0; i< arraySize; i++) {
		data_summerWeekNetbelastingDuurkromme_kW.add(i*p_timeStep_h, -netLoadArraySummer_kW[i]);
	}
	
	//Netbelastingduurkromme dag
	arraySize = (int)((runEndIdx - runStartIdx)/2.0); // roundToInt(8760/2/p_timeStep_h);
	data_daytimeNetbelastingDuurkromme_kW.reset();
	for(int i=0; i< arraySize; i++) {
		data_daytimeNetbelastingDuurkromme_kW.add(i*p_timeStep_h, -netLoadArrayDay_kW[i]);
	}
	
	//Netbelastingduurkromme nacht
	data_nighttimeNetbelastingDuurkromme_kW.reset();
	for(int i=0; i< arraySize; i++) {
		data_nighttimeNetbelastingDuurkromme_kW.add(i*p_timeStep_h, -netLoadArrayNight_kW[i]);
	}
	
	//Netbelastingduurkromme weekday
	arraySize = netLoadArrayWeekday_kW.length;
	data_weekdayNetbelastingDuurkromme_kW.reset();
	for(int i=0; i< arraySize; i++) {
		data_weekdayNetbelastingDuurkromme_kW.add(i*p_timeStep_h, -netLoadArrayWeekday_kW[i]);
	}
	
	//Netbelastingduurkromme weekend
	arraySize = netLoadArrayWeekend_kW.length;
	data_weekendNetbelastingDuurkromme_kW.reset();
	for(int i=0; i< arraySize; i++) {
		data_weekendNetbelastingDuurkromme_kW.add(i*p_timeStep_h, -netLoadArrayWeekend_kW[i]);
	}
	*/
}
int arraySize = data_netbelastingDuurkromme_kW.size();
data_HSMScapacity_kW.add(0, v_topLevelGridCapacity_kW);
data_HSMScapacity_kW.add(data_netbelastingDuurkromme_kW.getX(arraySize-1), v_topLevelGridCapacity_kW);
data_HSMScapacitySupply_kW.add(0, -v_topLevelGridCapacity_kW);
data_HSMScapacitySupply_kW.add(data_netbelastingDuurkromme_kW.getX(arraySize-1), -v_topLevelGridCapacity_kW);

/*ALCODEEND*/}

double f_runTimestep()
{/*ALCODESTART::1701162826549*/
// Update tijdreeksen in leesbare variabelen
t_h = p_runStartTime_h + v_timeStepsElapsed * p_timeStep_h;// + v_hourOfYearStart);// % 8760;

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
// First make all links between GridNodes
v_topLevelGridCapacity_kW = 0;

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
		v_topLevelGridCapacity_kW+=GN.p_capacity_kW;
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

//traceln("Grid Node execution list: %s", c_gridNodeExecutionList );
/*ALCODEEND*/}

double f_gridNodeRecursiveAdd(GridNode GN)
{/*ALCODESTART::1716886716306*/
c_gridNodeExecutionList.add(GN);
for (GridNode GNchild : GN.c_connectedGridNodes) {
	f_gridNodeRecursiveAdd(GNchild);
}


/*ALCODEEND*/}

ArrayList<GridConnection> f_getGridConnections()
{/*ALCODESTART::1716890117265*/
return c_gridConnections;
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

// Loop over populations to check v_ispaused
f_initializePause();

for (GridNode GN : c_gridNodeExecutionList) {
	GN.f_initializeGridnode();
}

f_initializeForecasts();
f_initializeAccumulators();
f_initializeLiveDataSets();

// Use parallelisation?
if (pop_connectionOwners.size() > 500 && b_parallelizeGridConnections) {
	b_parallelizeConnectionOwners = true;
}
if (c_gridConnections.size() < 100) {
	b_parallelizeGridConnections = true;
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
return v_topLevelGridCapacity_kW;
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
v_totalInstalledPVPower_kW = 0;
v_totalInstalledWindPower_kW = 0;
v_totalInstalledBatteryStorageCapacity_MWh = 0;
for (J_EA ea : c_energyAssets) {
	if (ea.energyAssetType == OL_EnergyAssetType.WINDMILL && ((GridConnection)ea.getParentAgent()).v_isActive ) {
		v_totalInstalledWindPower_kW += ((J_EAProduction)ea).getCapacityElectric_kW();
	}
	if (ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC && ((GridConnection)ea.getParentAgent()).v_isActive ) {
		v_totalInstalledPVPower_kW += ((J_EAProduction)ea).getCapacityElectric_kW();
	}
	if (ea.energyAssetType == OL_EnergyAssetType.STORAGE_ELECTRIC && ((GridConnection)ea.getParentAgent()).v_isActive ) {
		v_totalInstalledBatteryStorageCapacity_MWh += ((J_EAStorageElectric)ea).getStorageCapacity_kWh()/1000;
	}
	
}

// Total installed battery capacity
traceln("Total installed battery capacity: %s MWh", v_totalInstalledBatteryStorageCapacity_MWh);

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

int arraySize = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries().length;
traceln("ArraySize: %s", arraySize);
for (int i = 0; i < arraySize ; i++) {
	p_gridNodeTimeSeriesExcel.setCellValue((i) * p_timeStep_h, "TrafoData", i+2, 1);
}

for(GridNode gn : pop_gridNodes){
	p_gridNodeTimeSeriesExcel.setCellValue(gn.p_gridNodeID, "TrafoData", 1, columnIndex);
	double[] loadArray = gn.acc_annualElectricityBalance_kW.getTimeSeries();
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
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	dsm_liveDemand_kW.get(EC).add( t_h, fm_currentConsumptionFlows_kW.get(EC) );
	dsm_liveSupply_kW.get(EC).add( t_h, fm_currentProductionFlows_kW.get(EC) );
}

//data_naturalGasDemand_kW.update();
//data_dieselDemand_kW.update();
//data_hydrogenDemand_kW.update();
data_baseloadElectricityDemand_kW.update();
data_hydrogenElectricityDemand_kW.update();
data_heatPumpElectricityDemand_kW.update();
//data_electricCarsDemand_kW.update();
data_electricVehicleDemand_kW.update();
data_batteryCharging_kW.update();	
data_PVGeneration_kW.update();	
data_windGeneration_kW.update();	
data_batteryDischarging_kW.update();
data_V2GSupply_kW.update();
data_CHPElectricityProductionLiveWeek_kW.update();
data_batteryStoredEnergy_MWh.update();

//data_hydrogenSupply_kW.update();
data_districtHeatingDemand_kW.update();
data_cookingElectricityDemand_kW.update();

data_totalDemand_kW.update();
data_totalSupply_kW.update();
data_totalGridLoad_kW.update();

data_gridCapacityDemand_kW.update();
data_gridCapacitySupply_kW.update();

/*ALCODEEND*/}

double f_initializeLiveDataSets()
{/*ALCODESTART::1731573713521*/
dsm_liveDemand_kW.createEmptyDataSets(v_activeEnergyCarriers, (int) (168 / p_timeStep_h));
dsm_liveSupply_kW.createEmptyDataSets(v_activeEnergyCarriers, (int) (168 / p_timeStep_h));
/*ALCODEEND*/}

double f_initializeAccumulators()
{/*ALCODESTART::1732550958758*/
acc_totalEnergyProduction_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);
acc_totalEnergyConsumption_kW = new ZeroAccumulator(false, p_timeStep_h,p_runEndTime_h - p_runStartTime_h);
acc_totalEnergyCurtailed_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);
acc_totalElectricityProduction_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);
acc_totalElectricityConsumption_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);
acc_totalPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);

am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h );
am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, p_timeStep_h, p_runEndTime_h - p_runStartTime_h) );

// Summerweek
am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, p_timeStep_h, 24*7);
acc_summerWeekEnergyProduction_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_summerWeekEnergyConsumption_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_summerWeekEnergyCurtailed_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_summerWeekElectricityProduction_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_summerWeekElectricityConsumption_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_summerWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);

//Winterweek
am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, p_timeStep_h, 24*7);
acc_winterWeekEnergyProduction_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_winterWeekEnergyConsumption_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_winterWeekEnergyCurtailed_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_winterWeekElectricityProduction_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_winterWeekElectricityConsumption_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);
acc_winterWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, p_timeStep_h, 24*7);

//Daytime
am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));
am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));

acc_daytimeEnergyProduction_kW = new ZeroAccumulator(false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));
acc_daytimeEnergyConsumption_kW = new ZeroAccumulator(false, p_timeStep_h,0.5 * (p_runEndTime_h - p_runStartTime_h));
//acc_daytimeEnergyCurtailed_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);
acc_daytimeElectricityProduction_kW = new ZeroAccumulator(false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));
acc_daytimeElectricityConsumption_kW = new ZeroAccumulator(false, p_timeStep_h, 0.5 * (p_runEndTime_h - p_runStartTime_h));

//Weekend
am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 2 / 7  * (p_runEndTime_h - p_runStartTime_h) + 48);
am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, p_timeStep_h, 2 / 7 * (p_runEndTime_h - p_runStartTime_h) + 48);

acc_weekendEnergyProduction_kW = new ZeroAccumulator(false, p_timeStep_h, 2 / 7  * (p_runEndTime_h - p_runStartTime_h) + 48);
acc_weekendEnergyConsumption_kW = new ZeroAccumulator(false, p_timeStep_h,2 / 7  * (p_runEndTime_h - p_runStartTime_h) + 48);
//acc_weekendEnergyCurtailed_kW = new ZeroAccumulator(false, p_timeStep_h, p_runEndTime_h - p_runStartTime_h);
acc_weekendElectricityProduction_kW = new ZeroAccumulator(false, p_timeStep_h, 2 / 7  * (p_runEndTime_h - p_runStartTime_h) + 48);
acc_weekendElectricityConsumption_kW = new ZeroAccumulator(false, p_timeStep_h, 2 / 7  * (p_runEndTime_h - p_runStartTime_h) + 48);

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

// Remove energyCoop from pop_energyCoops.
remove_pop_energyCoops(energyCoop);


/*ALCODEEND*/}

EnergyCoop f_addEnergyCarrier(OL_EnergyCarriers EC)
{/*ALCODESTART::1740056275008*/
v_activeEnergyCarriers.add(EC);

DataSet dsDemand = new DataSet( (int)(168 / p_timeStep_h) );
DataSet dsSupply = new DataSet( (int)(168 / p_timeStep_h) );
double startTime = dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
double endTime = dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
for (double t = startTime; t <= endTime; t += p_timeStep_h) {
	dsDemand.add( t, 0);
	dsSupply.add( t, 0);
}
dsm_liveDemand_kW.put( EC, dsDemand);
dsm_liveSupply_kW.put( EC, dsSupply);
dsm_dailyAverageDemandDataSets_kW.put( EC, new DataSet(365));
dsm_dailyAverageSupplyDataSets_kW.put( EC, new DataSet(365));
dsm_summerWeekDemandDataSets_kW.put( EC, new DataSet( (int)(168 / p_timeStep_h)));
dsm_summerWeekSupplyDataSets_kW.put( EC, new DataSet( (int)(168 / p_timeStep_h)));
dsm_winterWeekDemandDataSets_kW.put( EC, new DataSet( (int)(168 / p_timeStep_h)));
dsm_winterWeekSupplyDataSets_kW.put( EC, new DataSet( (int)(168 / p_timeStep_h)));

/*ALCODEEND*/}

