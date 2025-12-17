double f_updateData()
{/*ALCODESTART::1693296929778*/
if (j_ea != null) {
	/*
	v_currentProductionElectricity_kW = j_ea.electricityProduction_kW;
	v_currentConsumptionElectricity_kW = j_ea.electricityConsumption_kW;
	v_currentProductionMethane_kW = j_ea.methaneProduction_kW;
	v_currentConsumptionMethane_kW = j_ea.methaneConsumption_kW;
	v_currentProductionHydrogen_kW = j_ea.hydrogenProduction_kW;
	v_currentConsumptionHydrogen_kW = j_ea.hydrogenConsumption_kW;
	v_currentProductionHeat_kW = j_ea.heatProduction_kW;
	v_currentConsumptionHeat_kW = j_ea.heatConsumption_kW;
	v_currentConsumptionPetroleumFuel_kW = j_ea.petroleumFuelConsumption_kW;
	v_currentStateOfCharge_r = j_ea.getCurrentStateOfCharge();
	v_powerFraction_fr = j_ea.v_powerFraction_fr;
	*/
	plot.updateData();
	//data_currentConsumptionElectricity_kW.update();
	//data_currentProductionElectricity_kW.update();
}

/*ALCODEEND*/}

double f_initializeData(J_EA EA)
{/*ALCODESTART::1693297254281*/
j_ea = EA;
p_energyAssetType = j_ea.energyAssetType;
p_assetName = j_ea.energyAssetName;
p_parentAgent = j_ea.parentAgent;

/*ALCODEEND*/}

double f_addFlows(double[] arr)
{/*ALCODESTART::1713969761331*/
v_currentProductionElectricity_kW = arr[0];
v_currentConsumptionElectricity_kW = arr[4];
v_currentProductionMethane_kW = arr[1];
v_currentConsumptionMethane_kW = arr[5];
v_currentProductionHydrogen_kW = arr[2];
v_currentConsumptionHydrogen_kW = arr[6];
v_currentProductionHeat_kW = arr[3];
v_currentConsumptionHeat_kW = arr[7];
v_currentConsumptionPetroleumFuel_kW = arr[8];
/*ALCODEEND*/}

double f_setCoordinatesUIElements()
{/*ALCODESTART::1714653114094*/
//plot.setX(0);
//plot.setY(0);

/*ALCODEEND*/}

double f_setVisibilities(boolean isVisible)
{/*ALCODESTART::1714654478601*/
plot.setVisible(isVisible);
/*ALCODEEND*/}

double f_updateStackChartData()
{/*ALCODESTART::1715953163186*/
double totalElectricityProduction_kW = 0;
double totalElectricityConsumption_kW = 0;

for (J_EA ea : c_EAs) {
	totalElectricityProduction_kW += ea.electricityProduction_kW;
	totalElectricityConsumption_kW += ea.electricityConsumption_kW;
	traceln("ea consumption: " + ea.electricityConsumption_kW);
}

data_currentProductionElectricity_kW.add(totalElectricityProduction_kW);
data_currentConsumptionElectricity_kW.add(totalElectricityConsumption_kW);

/*ALCODEEND*/}

double f_removeFlows(double[] arr)
{/*ALCODESTART::1722513456426*/
v_currentProductionElectricity_kW += arr[0];
v_currentConsumptionElectricity_kW += arr[4];
v_currentProductionMethane_kW += arr[1];
v_currentConsumptionMethane_kW += arr[5];
v_currentProductionHydrogen_kW += arr[2];
v_currentConsumptionHydrogen_kW += arr[6];
v_currentProductionHeat_kW += arr[3];
v_currentConsumptionHeat_kW += arr[7];
v_currentConsumptionPetroleumFuel_kW += arr[8];
/*ALCODEEND*/}

double f_addFlows(J_FlowsMap flowsMap)
{/*ALCODESTART::1729155791230*/
v_currentProductionElectricity_kW = max(0, -flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
v_currentConsumptionElectricity_kW = max(0, flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
v_currentProductionMethane_kW = max(0, -flowsMap.get(OL_EnergyCarriers.METHANE));
v_currentConsumptionMethane_kW = max(0, flowsMap.get(OL_EnergyCarriers.METHANE));
v_currentProductionHydrogen_kW = max(0, -flowsMap.get(OL_EnergyCarriers.HYDROGEN));
v_currentConsumptionHydrogen_kW = max(0, flowsMap.get(OL_EnergyCarriers.HYDROGEN));
v_currentProductionHeat_kW = max(0, -flowsMap.get(OL_EnergyCarriers.HEAT));
v_currentConsumptionHeat_kW = max(0, flowsMap.get(OL_EnergyCarriers.HEAT));
v_currentConsumptionPetroleumFuel_kW = max(0, flowsMap.get(OL_EnergyCarriers.PETROLEUM_FUEL));


/*ALCODEEND*/}

double f_removeFlows(J_FlowsMap flowsMap)
{/*ALCODESTART::1729167663082*/
v_currentProductionElectricity_kW = max(0, -flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
v_currentConsumptionElectricity_kW = max(0, flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
v_currentProductionMethane_kW = max(0, -flowsMap.get(OL_EnergyCarriers.METHANE));
v_currentConsumptionMethane_kW = max(0, flowsMap.get(OL_EnergyCarriers.METHANE));
v_currentProductionHydrogen_kW = max(0, -flowsMap.get(OL_EnergyCarriers.HYDROGEN));
v_currentConsumptionHydrogen_kW = max(0, flowsMap.get(OL_EnergyCarriers.HYDROGEN));
v_currentProductionHeat_kW = max(0, -flowsMap.get(OL_EnergyCarriers.HEAT));
v_currentConsumptionHeat_kW = max(0, flowsMap.get(OL_EnergyCarriers.HEAT));
v_currentConsumptionPetroleumFuel_kW = max(0, flowsMap.get(OL_EnergyCarriers.PETROLEUM_FUEL));


/*ALCODEEND*/}

