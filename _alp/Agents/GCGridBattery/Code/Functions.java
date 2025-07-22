double f_connectToChild_overwrite(Agent ConnectingChildNode)
{/*ALCODESTART::1666956397906*/
//assetLinks.connectTo(ConnectingChildNode);
EnergyAsset EA = (EnergyAsset) ConnectingChildNode;
c_energyAssets.add(EA);

if (EA.j_ea instanceof J_EAConsumption) {
	//c_consumptionAssets.add(EA);
} else if (EA.j_ea instanceof J_EAProduction ) {
	//c_productionAssets.add(EA);
} else if (EA.j_ea instanceof J_EAStorage ) {
	//c_storageAssets.add(EA);
	if (EA.j_ea instanceof J_EAStorageHeat) {
		//p_BuildingThermalAsset = EA;
	}
	else if(EA.j_ea instanceof J_EAStorageElectric && ((J_EAStorageElectric)EA.j_ea).getStorageCapacity_kWh() !=0) {
		//p_batteryAsset = EA;
	}
} else if (EA.j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)EA.j_ea);
	if (EA.j_ea instanceof J_EAConversionGasBurner || EA.j_ea instanceof J_EAConversionHeatPump || EA.j_ea instanceof J_EAConversionHeatDeliverySet || EA.j_ea instanceof J_EAConversionElectricHeater ) {
		if (p_primaryHeatingAsset == null) {
			p_primaryHeatingAsset = (J_EAConversion)EA.j_ea;
		} else if (p_secondaryHeatingAsset == null) {
			p_secondaryHeatingAsset = (J_EAConversion)EA.j_ea;
		} else {
			traceln("District Heating gridconnection already has two heating systems!");
		}
		//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
	}
} else {
	traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + ConnectingChildNode.getId() + " is of unknown type or null! ");
}


/*
// create a local list of energyAssets connected to its netConnection Agent for easy reference
List<EnergyAsset> connectedEnergyAssets = subConnections.getConnections();

int numberOfEnergyAssets = connectedEnergyAssets.size();
for( int i = 0; i < numberOfEnergyAssets; i++ ) {
	if( connectedEnergyAssets.get(i) instanceof EnergyAsset ) {
		c_connectedEnergyAssets.add(connectedEnergyAssets.get(i));

	}
}

List<EnergyAsset> consumptionAssets = filter(c_connectedEnergyAssets, b -> b.j_ea instanceof J_EAConsumption);
List<EnergyAsset> productionAssets = filter(c_connectedEnergyAssets, b -> b.j_ea instanceof J_EAProduction);
List<EnergyAsset> storageAssets = filter(c_connectedEnergyAssets, b -> b.j_ea instanceof J_EAStorage);
List<EnergyAsset> conversionAssets = filter(c_connectedEnergyAssets, b -> b.j_ea instanceof J_EAConversion);
traceln("NetConnection connecting to " + numberOfEnergyAssets + " EnergyAssets");
*/

/*ALCODEEND*/}

double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1666956527771*/
if(energyModel.t_h == 0){ // Load at gridnode is not known yet (due to time step delay)
	p_batteryAsset.v_powerFraction_fr = 0;
	return;
}

f_manageBattery();

/*ALCODEEND*/}

double f_operateFixedConsumptionAssets_overwrite()
{/*ALCODESTART::1666967854749*/
for(EnergyAsset e : c_consumptionAssets) {
	if( e.p_energyAssetType == OL_EnergyAssetType.ELECTRICITY_CONSUMPTION_PROFILE ) {
		e.f_updateElectricityFlows( main.v_currentBuildingOtherElectricityDemand_fr );
	}
	else {
		traceln("Grid battery has other consumption assets than 'other electricity consumption'");
		e.v_powerFraction_fr = 0;
	}
}
/*ALCODEEND*/}

double f_calculateEnergyBalance_overwrite()
{/*ALCODESTART::1688369593905*/
v_previousPowerElectricity_kW = v_currentPowerElectricity_kW;
v_currentPowerElectricity_kW = 0;
v_currentPowerMethane_kW = 0;
v_currentPowerHydrogen_kW = 0;
v_currentPowerHeat_kW = 0;
v_currentPowerDiesel_kW = 0;

v_currentElectricityConsumption_kW = 0;
v_currentElectricityProduction_kW = 0;
v_currentEnergyConsumption_kW = 0;
v_currentEnergyProduction_kW = 0;
v_currentEnergyCurtailed_kW = 0;


// Categorical power flows
v_fixedConsumptionElectric_kW = 0;
v_electricHobConsumption_kW = 0;
v_heatPumpElectricityConsumption_kW = 0;
v_hydrogenElectricityConsumption_kW = 0;
v_evChargingPowerElectric_kW = 0;
v_batteryPowerElectric_kW = 0;
v_windProductionElectric_kW = 0;
v_pvProductionElectric_kW = 0;
v_conversionPowerElectric_kW = 0;


f_operateFixedAssets();
f_operateFlexAssets();
f_curtailment();
f_connectionMetering();

//v_electricityTotalsCheck_kWh = v_electricityConsumedFixedProfile_kWh + v_electricityConvertedToX_kWh + v_electricityChargedByEVs_kWh  + v_electricityDeliveredToGrid_kWh 
//- v_electricityProducedPV_kWh - v_electricityProducedWind_kWh - v_electricityDrawnFromGrid_kWh - v_xConvertedToElectricity_kWh + v_electricityChargedByBattery_kWh - v_electricityDischargedByBattery_kWh;

/*
// Total Energy Use and Production
v_totalEnergyUsed_kWh = 0;
v_totalEnergyProduced_kWh = 0;
for (EnergyAsset EA : c_energyAssets ) {
	double energyUse_kWh=EA.j_ea.getEnergyUsed_kWh();
	if (EA.j_ea instanceof J_EAConversionCurtailer) {
		v_totalEnergyProduced_kWh -= max(0, energyUse_kWh);
	} else {
		v_totalEnergyUsed_kWh += max(0, energyUse_kWh);
		v_totalEnergyProduced_kWh -= min(0, energyUse_kWh);
	}
}

v_selfConsumption_fr = 1 - (v_electricityDeliveredToGrid_kWh + v_heatDelivered_kWh + v_methaneDelivered_kWh + v_hydrogenDelivered_kWh + v_dieselDelivered_kWh ) / v_totalEnergyProduced_kWh; // Doesn't make sense to sum different energy carriers!
v_selfSufficiency_fr = 1 - (v_electricityDrawnFromGrid_kWh + v_heatDrawn_kWh + v_methaneDrawn_kWh + v_hydrogenDrawn_kWh + v_dieselDrawn_kWh) / v_totalEnergyUsed_kWh; // Need to account for energy in storages
v_maxConnectionLoad_fr = max(v_maxConnectionLoad_fr, abs(v_currentPowerElectricity_kW / v_allowedCapacity_kW ));
*/

/*ALCODEEND*/}

double f_manageCurtailer(J_EAConversionCurtailer CurtailerAsset)
{/*ALCODESTART::1709827456110*/
//traceln("Hello! " + CurtailerAsset.j_ea.getElectricCapacity_kW());
if (CurtailerAsset.getElectricCapacity_kW()>0) {
	double curtailerSetpointElectric_kW = -min(0,v_currentPowerElectricity_kW + p_connectionCapacity_kW);
	double[] flowsArray = CurtailerAsset.f_updateAllFlows(curtailerSetpointElectric_kW/CurtailerAsset.getElectricCapacity_kW());
	v_conversionPowerElectric_kW = flowsArray[4] - flowsArray[0];
	/*if ( curtailerSetpointElectric_kW > 0 ) {
		traceln("Windfarm is curtailing " + curtailerSetpointElectric_kW + " kW!");
	}*/
}
/*ALCODEEND*/}

