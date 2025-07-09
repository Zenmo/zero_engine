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

if ( p_batteryAsset != null ) {
	if ( p_batteryAsset.getStorageCapacity_kWh() != 0.0 && p_batteryAsset.getCapacityElectric_kW() != 0.0) {
		v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
		if( p_batteryOperationMode == OL_BatteryOperationMode.BALANCE){
			//f_batteryManagementGridLoad(v_batterySOC_fr);
			f_batteryManagementBalanceGrid(v_batterySOC_fr);
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.BALANCE_SUPPLY){ // Tries to minimize supply peaks
			f_batteryManagementBalanceSupply(v_batterySOC_fr);
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.PRICE){
			f_batteryManagementPrice(v_batterySOC_fr);
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.NODAL_PRICING){
			f_batteryManagementPriceGrid(v_batterySOC_fr);
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.BALANCE_COOP){
			f_batteryManagementBalanceCOOP(v_batterySOC_fr);
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.BATTERY_ALGORITHM_BAS){
			f_batteryManagementBas(v_batterySOC_fr);
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.PEAK_SHAVING_SIMPLE){
			f_batteryManagementPeakShavingGrid();
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.PEAK_SHAVING_ADVANCED){
			f_batteryManagementPeakShavingAdvancedGrid();
		} else if (p_batteryOperationMode == OL_BatteryOperationMode.SELF_CONSUMPTION){
			f_batteryManagementSelfConsumption();
		}				
		p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);	
		//J_FlowsMap flowsMap = flowsPair.getFirst();
		//v_batteryPowerElectric_kW = flowsMap.get(OL_EnergyCarriers.ELECTRICITY);
		v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
		//v_batteryPowerElectric_kW = p_batteryAsset.electricityConsumption_kW - p_batteryAsset.electricityProduction_kW;
		//v_currentPowerElectricity_kW += v_batteryPowerElectric_kW;
		if (p_batteryOperationMode == OL_BatteryOperationMode.BATTERY_ALGORITHM_BAS && energyModel.v_isRapidRun){
			v_batteryAlgorithmBas.calculateTurningPointDuringRapidRun(v_batterySOC_fr);
		}
	}
}

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

double f_batteryManagementPriceGrid(double currentBatteryStateOfCharge_fr)
{/*ALCODESTART::1669022533963*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	//double willingnessToPayDefault_eurpkWh = 0.3;
	double WTPfeedbackGain_eurpSOC = 0.5; // When SOC-error is 100%, adjust WTP price by 1 eurpkWh
	double priceGain_kWhpeur = 2; // How strongly to ramp up power with price-delta's
	//double congestionTariffCoop_eurpkWh = -(((ConnectionOwner)p_ownerActor).p_CoopParent.v_electricitySurplus_kW + v_previousPowerElectricity_kW)/1200*0.1;
	
	double chargeSetpoint_kW = 0;	
	double currentElectricityPriceCharge_eurpkWh;
	//double currentElectricityPriceDischarge_eurpkWh;
	/*if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		ConnectionOwner ownerActor = (ConnectionOwner)l_ownerActor.getConnectedAgent();
		currentElectricityPriceCharge_eurpkWh = ownerActor.f_getElectricityPrice(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY)+100.0); // query price at 100kW charging
		//currentElectricityPriceDischarge_eurpkWh = ownerActor.f_getElectricityPrice(v_currentPowerElectricity_kW-100.0); // query price at -100kW charging
	} else { // Get EPEX price plus nodal price of GridNode
	*/
		//currentElectricityPriceCharge_eurpkWh = energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000 + GN.v_currentTotalNodalPrice_eurpkWh;
		currentElectricityPriceCharge_eurpkWh = p_parentNodeElectric.v_currentTotalNodalPrice_eurpkWh;
		//currentElectricityPriceCharge_eurpkWh = l_parentNodeElectric.getConnectedAgent().v_currentTotalNodalPrice_eurpkWh;		
	//}
	v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
	
	double currentCoopElectricitySurplus_kW = -p_parentNodeElectric.v_currentLoad_kW + v_previousPowerElectricity_kW;			
	double CoopConnectionCapacity_kW = 0.95*p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
	
	//traceln("Operating buurtbatterij, current local surplus is %s kW.", currentCoopElectricitySurplus_kW);
	
	double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity
	double availableDischargePower_kW = currentCoopElectricitySurplus_kW - CoopConnectionCapacity_kW; // Max discharging power within grid capacity
	
	
	double SOC_setp_fr = 0.9 - 2*p_parentNodeElectric.v_electricityYieldForecast_fr;	
	//SOC_setp_fr = (0.5 + 0.4 * Math.cos(2*Math.PI*(energyModel.t_h-18)/24))*(1-3*GN.v_electricityYieldForecast_fr); // Sinusoidal setpoint: aim for high SOC at 18:00h		
	//SOC_setp_fr = 0.6 + 0.25 * Math.sin(2*Math.PI*(main.t_h-12)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
	
	double SOC_deficit_fr = SOC_setp_fr - currentBatteryStateOfCharge_fr;
	
	// Define WTP price for charging and discharging!
//	double WTP_charge_eurpkWh = v_electricityPriceLowPassed_eurpkWh - chargeDischarge_offset_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
//	double WTP_discharge_eurpkWh = v_electricityPriceLowPassed_eurpkWh + chargeDischarge_offset_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
		
	/* // Choose charging power based on prices and desired SOC level
	if ( WTP_charge_eurpkWh > currentElectricityPriceCharge_eurpkWh ) { // if willingness to pay higher than current electricity price
		double WTP_charge_eurpkWh = v_electricityPriceLowPassed_eurpkWh + 0.5*(energyModel.v_epexForecast_eurpkWh - v_electricityPriceLowPassed_eurpkWh) + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
		chargeSetpoint_kW = p_batteryAsset.getElectricCapacity_kW()*(WTP_charge_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_kWhpeur ;
	} 
	else if (WTP_discharge_eurpkWh < currentElectricityPriceCharge_eurpkWh) {
		double WTP_discharge_eurpkWh = v_electricityPriceLowPassed_eurpkWh + 0.5*(energyModel.v_epexForecast_eurpkWh - v_electricityPriceLowPassed_eurpkWh) + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
		chargeSetpoint_kW = -p_batteryAsset.getElectricCapacity_kW()*(currentElectricityPriceCharge_eurpkWh - WTP_discharge_eurpkWh)*priceGain_kWhpeur;
	}*/
	
	//double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + 1.0*(energyModel.v_epexForecast_eurpkWh - v_electricityPriceLowPassed_eurpkWh) + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
	double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
	chargeSetpoint_kW = p_batteryAsset.getCapacityElectric_kW()*(WTP_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_kWhpeur;
					
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
}
/*ALCODEEND*/}

double f_batteryManagementBalanceGrid(double batterySOC)
{/*ALCODESTART::1678114662587*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){	
	double currentCoopElectricitySurplus_kW = 0;
	double CoopConnectionCapacity_kW = 0.9*p_parentNodeElectric.p_capacity_kW;
	double v_previousPowerBattery_kW = v_previousPowerElectricity_kW;// Assumes battery is only asset on gridconnection!! p_batteryAsset.electricityConsumption_kW-p_batteryAsset.electricityProduction_kW;
	//traceln("Previous battery power: " + v_previousPowerElectricity_kW);
	if( p_owner != null ) {
		/*if(((ConnectionOwner)l_ownerActor.getConnectedAgent()).p_coopParent instanceof EnergyCoop ) { // get electricity balance from Coop 			
			currentCoopElectricitySurplus_kW = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).p_coopParent.v_electricitySurplus_kW + v_previousPowerBattery_kW;
			CoopConnectionCapacity_kW = 0.9*((ConnectionOwner)l_ownerActor.getConnectedAgent()).p_coopParent.v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay			
		} else { // Get gridload directly from node*/
			currentCoopElectricitySurplus_kW = -p_parentNodeElectric.v_currentLoad_kW + v_previousPowerBattery_kW;			
			CoopConnectionCapacity_kW = 0.9*p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
		//}
	} else { // Get gridload directly from node
		currentCoopElectricitySurplus_kW = -p_parentNodeElectric.v_currentLoad_kW + v_previousPowerBattery_kW;			
		CoopConnectionCapacity_kW = 0.9*p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
	}
	//traceln("Operating buurtbatterij, current local surplus is %s kW.", currentCoopElectricitySurplus_kW);	
		
	double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity
	double availableDischargePower_kW = CoopConnectionCapacity_kW - currentCoopElectricitySurplus_kW; // Max discharging power within grid capacity
	double FeedbackGain_kWpSOC = 3 * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
	double FeedforwardGain_kWpKw = 0.1; // Feedforward based on current surpluss in Coop
	double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
	double chargeSetpoint_kW = 0;
	
	/*
	// ----------------------------------------------------
	//FeedforwardGain_kWpKw = 0.3; // Feedforward based on current surpluss in Coop
	//FeedbackGain_kWpSOC = 1.5 * 0.6 * p_batteryAsset.getElectricCapacity_kW();
	double SOC_setp_fr = 0.8;
	SOC_setp_fr = (0.5 + 0.4 * Math.sin(2*Math.PI*(energyModel.t_h-18)/24))-3*GN.v_electricityYieldForecast_fr; // Sinusoidal setpoint: aim for high SOC at 18:00h
	
	chargeSetpoint_kW = FeedforwardGain_kWpKw * (currentCoopElectricitySurplus_kW - chargeOffset_kW) + (SOC_setp_fr - batterySOC) * FeedbackGain_kWpSOC;
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getElectricCapacity_kW())); // Convert to powerFraction and limit power	
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
	//traceln("hello?");
	*/
		// prevent congestion
	/*if (availableChargePower_kW < 0) { //Delivery side; if availableChargePower is negative, battery will have to discharge!
		p_batteryAsset.v_powerFraction_fr = max(-1, availableChargePower_kW / p_batteryAsset.getCapacityElectric_kW());
		return;
	}
	if (availableDischargePower_kW < 0) { //Feedin side, if availableDiscrhagePower is negative, battery will have to charge!
		p_batteryAsset.v_powerFraction_fr = min(1, -availableDischargePower_kW / p_batteryAsset.getCapacityElectric_kW());
		return;
	}*/

	if (energyModel.v_currentSolarPowerNormalized_r > 0.1) { // 
		if (p_parentNodeElectric.v_currentLoad_kW < 0) {
			p_batteryAsset.v_powerFraction_fr = max(-1, min(1, currentCoopElectricitySurplus_kW / p_batteryAsset.getCapacityElectric_kW()));
		}
	}
	else {
		double expectedWind_kWh = p_parentNodeElectric.v_totalInstalledWindPower_kW * energyModel.v_WindYieldForecast_fr * energyModel.p_forecastTime_h;
		double expectedSolar_kWh = p_parentNodeElectric.v_totalInstalledPVPower_kW * energyModel.v_SolarYieldForecast_fr * energyModel.p_forecastTime_h;
		double incomingPower_fr = (expectedSolar_kWh + expectedWind_kWh) / p_batteryAsset.getStorageCapacity_kWh();
		double SOC_setp_fr = 1 - incomingPower_fr;
	
		chargeSetpoint_kW = FeedbackGain_kWpSOC*(SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge());
		
	}
	
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // // prevent congestion Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // normalize charge setpoint and cap between -1 and 1.
		
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

double f_batteryManagementBalanceSupply(double batterySOC)
{/*ALCODESTART::1710163119414*/
// Simply tries to prevent exceeding feedin grid capacity, but otherwise aims for an empty battery (so there is always 'absorbtion capacity' available)

if (p_batteryAsset.getStorageCapacity_kWh() != 0){	
	double currentCoopElectricitySurplus_kW = 0;
	double CoopConnectionCapacity_kW = 0;
	double v_previousPowerBattery_kW = v_previousPowerElectricity_kW;// Assumes battery is only asset on gridconnection!! p_batteryAsset.electricityConsumption_kW-p_batteryAsset.electricityProduction_kW;
	//traceln("Previous battery power: " + v_previousPowerElectricity_kW);
	if( p_owner != null ) {
		if( p_owner.p_coopParent instanceof EnergyCoop ) { // get electricity balance from Coop 			
			currentCoopElectricitySurplus_kW = p_owner.p_coopParent.v_electricitySurplus_kW + v_previousPowerBattery_kW;
			CoopConnectionCapacity_kW = 0.9*p_owner.p_coopParent.v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay			
		} else { // Get gridload directly from node
			currentCoopElectricitySurplus_kW = -p_parentNodeElectric.v_currentLoad_kW + v_previousPowerBattery_kW;			
			CoopConnectionCapacity_kW = 0.95*p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
		}
	} else { // Get gridload directly from node
		currentCoopElectricitySurplus_kW = -p_parentNodeElectric.v_currentLoad_kW + v_previousPowerBattery_kW;			
		CoopConnectionCapacity_kW = 0.95*p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
	}
	//traceln("Operating buurtbatterij, current local surplus is %s kW.", currentCoopElectricitySurplus_kW);
	
	double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity
	double availableDischargePower_kW = currentCoopElectricitySurplus_kW - CoopConnectionCapacity_kW; // Max discharging power within grid capacity
	
	double FeedbackGain_kWpSOC = 5 * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
	double FeedforwardGain_kWpKw = 0.0; // Feedforward based on current surpluss in Coop
	double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
	double chargeSetpoint_kW = 0;
	double SOC_setp_fr = 0.0;

	
	chargeSetpoint_kW = FeedforwardGain_kWpKw * (currentCoopElectricitySurplus_kW - chargeOffset_kW) + (SOC_setp_fr - batterySOC) * FeedbackGain_kWpSOC;
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
	//traceln("hello?");
}

/*ALCODEEND*/}

double f_batteryManagementBalanceCOOP(double batterySOC)
{/*ALCODESTART::1742557292690*/

if(c_parentCoops.isEmpty()){
	return;
}

EnergyCoop parentCoop = c_parentCoops.get(0);

if (p_batteryAsset.getStorageCapacity_kWh() != 0){	
	double CoopConnectionCapacityDelivery_kW = parentCoop.v_liveConnectionMetaData.contractedDeliveryCapacity_kW * 0.9; //10% reduction, to accord for time step delay
	double CoopConnectionCapacityFeedin_kW = parentCoop.v_liveConnectionMetaData.contractedDeliveryCapacity_kW * 0.9; //10% reduction, to accord for time step delay
	double v_previousPowerBattery_kW = v_previousPowerElectricity_kW;// Assumes battery is only asset on gridconnection!! p_batteryAsset.electricityConsumption_kW-p_batteryAsset.electricityProduction_kW;
	double currentCoopElectricityBalance_kW = parentCoop.v_liveData.data_liveElectricityBalance_kW.getY(parentCoop.v_liveData.data_liveElectricityBalance_kW.size() - 1) - v_previousPowerBattery_kW;	
	
	double availableChargePower_kW = CoopConnectionCapacityDelivery_kW - currentCoopElectricityBalance_kW; // Max battery charging power within grid capacity
	double availableDischargePower_kW = CoopConnectionCapacityFeedin_kW + currentCoopElectricityBalance_kW; // Max discharging power within grid capacity
	//Tuning parameters
	double FeedbackGain_kWpSOC = 3 * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
	double FeedforwardGain_kWpKw = 0.1; // Feedforward based on current surpluss in Coop
	double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
	double chargeSetpoint_kW = 0;
	
	//Congestion prevention
	if (availableChargePower_kW < 0) { //Delivery side
		p_batteryAsset.v_powerFraction_fr = max(-1, availableChargePower_kW / p_batteryAsset.getCapacityElectric_kW());
		return;
	}
	if (availableDischargePower_kW < 0) { //Feedin side
		p_batteryAsset.v_powerFraction_fr = min(1, -availableDischargePower_kW / p_batteryAsset.getCapacityElectric_kW());
		return;
	}
	
	if (energyModel.v_currentSolarPowerNormalized_r > 0.1) {
		if (currentCoopElectricityBalance_kW < 0) {
			p_batteryAsset.v_powerFraction_fr = max(-1, min(1, -currentCoopElectricityBalance_kW / p_batteryAsset.getCapacityElectric_kW()));
		}
	}
	else {
		double expectedWind_kWh = parentCoop.v_liveAssetsMetaData.totalInstalledWindPower_kW * energyModel.v_WindYieldForecast_fr * energyModel.p_forecastTime_h;
		double expectedSolar_kWh = parentCoop.v_liveAssetsMetaData.totalInstalledPVPower_kW * energyModel.v_SolarYieldForecast_fr * energyModel.p_forecastTime_h;
		double incomingPower_fr = (expectedSolar_kWh + expectedWind_kWh) / p_batteryAsset.getStorageCapacity_kWh();
		double SOC_setp_fr = 1 - incomingPower_fr;
	
		chargeSetpoint_kW = FeedbackGain_kWpSOC*(SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge());
		chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW()));
	}
}

/*ALCODEEND*/}

double f_batteryManagementBas(double currentBatteryStateOfCharge_fr)
{/*ALCODESTART::1746605418870*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){ // battery_kWh =/= 0
	
	double chargeSetpoint_kW = 0;

	chargeSetpoint_kW = v_batteryAlgorithmBas.calculateChargeSetpointPriceGrid_kW(currentBatteryStateOfCharge_fr);
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW()));

}
/*ALCODEEND*/}

double f_resetSpecificGCStates_GCGridBattery()
{/*ALCODESTART::1749215594968*/
if (v_batteryAlgorithmBas != null){
	v_batteryAlgorithmBas.resetTurningPoints();
}
/*ALCODEEND*/}

double f_batteryManagementPeakShavingGrid()
{/*ALCODESTART::1750860323602*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	int index = roundToInt((energyModel.t_h % 24)/energyModel.p_timeStep_h);
	if(index == 0){
		f_peakShavingGridForecast();
	}
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, v_batteryChargingPeakShavingForecast_kW[index] / p_batteryAsset.getCapacityElectric_kW()));
}
/*ALCODEEND*/}

double f_peakShavingGridForecast()
{/*ALCODESTART::1750860487224*/
double amountOfHoursInADay = 24;
double[] nettoBalance_kW = new double[96];
double[] nettoBalanceTotal_kW = new double[96];
//double[] elecConsumptionConsumptionAssetTotal = new double[96]

//For simulation that cross the year end
double hour_of_simulation_year = energyModel.t_h - energyModel.p_runStartTime_h;
//traceln("hour_of_year: " + hour_of_simulation_year);

int startTimeDayIndex = roundToInt(hour_of_simulation_year/energyModel.p_timeStep_h);
int endTimeDayIndex = roundToInt((hour_of_simulation_year + 24)/energyModel.p_timeStep_h);
//traceln("start=" + startTimeDayIndex + ", end=" + endTimeDayIndex);

//Get elec consumption profile
GridNode GN = p_parentNodeElectric;

for (GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){

	J_EAProfile elecConsumptionProfile = findFirst(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
	J_EAConsumption elecConsumptionConsumptionAsset = findFirst(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
	if(elecConsumptionProfile != null){ //double[]; nettoBalance = 1 day forecast of one GC; nettoBalanceTotal is addition of all GCs
		double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), 1/energyModel.p_timeStep_h);
		for (int i = 0; i < tempNettoBalance_kW.length; i++) {
    		nettoBalanceTotal_kW[i] += tempNettoBalance_kW[i];
		}
	}
	if(elecConsumptionConsumptionAsset != null){//table function 
		for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
			nettoBalanceTotal_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
		}
	}
	//for (int i = 0; i < nettoBalance_kW.length; i++) {
    //    nettoBalanceTotal_kW[i] += nettoBalance_kW[i];
    //}
}

int startTimeDayIndex_h = roundToInt(hour_of_simulation_year);
int endTimeDayIndex_h = roundToInt(hour_of_simulation_year + 24);

//double[] pvProfile_p_h = ZeroMath.arrayMultiply(Arrays.copyOfRange(energyModel.pp_PVProduction35DegSouth_fr.getAllValues(), startTimeDayIndex_h, endTimeDayIndex_h), GN.v_totalInstalledPVPower_kW); // whole year 8760 samples

for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
	nettoBalanceTotal_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] -= energyModel.pp_PVProduction35DegSouth_fr.getValue(time)*GN.v_totalInstalledPVPower_kW;
}

//for (int i = 0; i < nettoBalanceTotal_kW.length; i++) {
//	int idx_h = i/4;
//	nettoBalanceTotal_kW[i] -= pvProfile_p_h[idx_h];
//}
////Fill chargesetpoint Array

//Initialize chargepoint array
v_batteryChargingPeakShavingForecast_kW = new double[96];


//Calculate the total export over the day that can be collected by the battery
double totalExport_kWh = 0;
for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
	if(nettoBalanceTotal_kW[i] < 0){
		totalExport_kWh += min(p_batteryAsset.getCapacityElectric_kW(), -nettoBalanceTotal_kW[i])*energyModel.p_timeStep_h;
	}
}
	
//Flatten the morning net balance while charging
double totalDailyImport_kWh = 0;
for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
	if(i< amountOfHoursInADay/energyModel.p_timeStep_h){
		totalDailyImport_kWh += max(0,nettoBalanceTotal_kW[i]*energyModel.p_timeStep_h);
	}
}

double batteryEnergyNeeded_kWh = max(0,(p_batteryAsset.getStorageCapacity_kWh()*(1-p_batteryAsset.getCurrentStateOfCharge()))-totalExport_kWh);
double averageDailyConsumption_kW = (totalDailyImport_kWh + batteryEnergyNeeded_kWh)/amountOfHoursInADay;

//If 24 hours
for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
	v_batteryChargingPeakShavingForecast_kW[i] += averageDailyConsumption_kW - nettoBalanceTotal_kW[i];
}

/*ALCODEEND*/}

double f_batteryManagementPeakShavingAdvancedGrid()
{/*ALCODESTART::1750941623672*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	int time = roundToInt(energyModel.t_h);
	int index = roundToInt((energyModel.t_h % 24)/energyModel.p_timeStep_h);
	int index_shifted = roundToInt(((energyModel.t_h + 12) % 24)/energyModel.p_timeStep_h);
	if(time == 0){ //begin simulation
		f_valleyFillingGridAdvancedForecast();
		//traceln("Filled the valley at the beginning of the year!");
	}
	if(index == 0){ //daily peak-shaving forecast; night-night
		f_peakShavingGridAdvancedForecast();
	}
	if(index == 48){ //daily valley-filling forecast
		f_valleyFillingGridAdvancedForecast();					
	}
	
	if(time < 6){
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, v_batteryChargingValleyFillingAdvancedForecast_kW[index_shifted] / p_batteryAsset.getCapacityElectric_kW()));
		//traceln("Start of the year. Charging right now with " + v_batteryChargingValleyFillingAdvancedForecast_kW[index_shifted] + " kW");
		//traceln("Start of the year. Charging right now with " + p_batteryAsset.v_powerFraction_fr);
	} else if(time >= (energyModel.p_runEndTime_h - 6)){
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, v_batteryChargingValleyFillingAdvancedForecast_kW[index] / p_batteryAsset.getCapacityElectric_kW()));
	} else if(time >= 6 && time < (energyModel.p_runEndTime_h - 6) && index >= 24 && index < 72) {
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, v_batteryDischargingPeakShavingAdvancedForecast_kW[index] / p_batteryAsset.getCapacityElectric_kW()));
		//traceln("Discharging right now with " + v_batteryDischargingPeakShavingAdvancedForecast_kW[index] + " kW");
		//traceln("Discharging right now with " + p_batteryAsset.v_powerFraction_fr);
	} else if(time >= 6 && time < (energyModel.p_runEndTime_h - 6) && (index < 24 || index >= 72)){
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, v_batteryChargingValleyFillingAdvancedForecast_kW[index_shifted] / p_batteryAsset.getCapacityElectric_kW()));
		//traceln("Charging right now with " + v_batteryChargingValleyFillingAdvancedForecast_kW[index_shifted] + " kW");
		//traceln("Charging right now with " + p_batteryAsset.v_powerFraction_fr);
	} 
	
	
	
}
/*ALCODEEND*/}

double f_peakShavingGridAdvancedForecast()
{/*ALCODESTART::1750941667186*/
double amountOfHoursInADay = 24;
double[] nettoBalance_kW = new double[96];
double[] temp_kW = new double[96];

//For simulation that cross the year end
double hour_of_simulation_year = energyModel.t_h - energyModel.p_runStartTime_h;
//traceln("hour_of_year: " + hour_of_simulation_year);

int startTimeDayIndex = roundToInt(hour_of_simulation_year/energyModel.p_timeStep_h);
int endTimeDayIndex = roundToInt((hour_of_simulation_year + 24)/energyModel.p_timeStep_h);
int startTimeDayIndex_h = roundToInt(hour_of_simulation_year);
int endTimeDayIndex_h = roundToInt(hour_of_simulation_year + 24);
//traceln("start=" + startTimeDayIndex + ", end=" + endTimeDayIndex);

//Get elec consumption profile
GridNode GN = p_parentNodeElectric;

for (GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){

	J_EAProfile elecConsumptionProfile = findFirst(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
	J_EAConsumption elecConsumptionConsumptionAsset = findFirst(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
	if(elecConsumptionProfile != null){ //double[]; nettoBalance = 1 day forecast of one GC; nettoBalanceTotal is addition of all GCs
		double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), 1/energyModel.p_timeStep_h);
		for (int i = 0; i < tempNettoBalance_kW.length; i++) {
    		nettoBalance_kW[i] += tempNettoBalance_kW[i];
		}
	}
	if(elecConsumptionConsumptionAsset != null){//table function 
		for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
			nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
		}
	}
}

for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
	nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] -= energyModel.pp_PVProduction35DegSouth_fr.getValue(time)*GN.v_totalInstalledPVPower_kW;
}


//Calculate integral upper area
double precision_kW = 0.01;
   
double batteryStorageCapacity_kWh = p_batteryAsset.getStorageCapacity_kWh();
double currentSoC_r = p_batteryAsset.getCurrentStateOfCharge();
double availableCharge_kWh = currentSoC_r*batteryStorageCapacity_kWh;

double maxPeak_kW = Arrays.stream(nettoBalance_kW).max().getAsDouble();
prevMaxPeak_kW = maxPeak_kW;
double peakSurface_kWh = 0;

while(peakSurface_kWh < batteryStorageCapacity_kWh && maxPeak_kW > 0){ //assumption that battery SoC is always 0
	maxPeak_kW -= precision_kW; //set limit lower
	peakSurface_kWh = 0;
	for(int i = 0; i < nettoBalance_kW.length; i++){
		if(nettoBalance_kW[i] > maxPeak_kW){
			peakSurface_kWh += (nettoBalance_kW[i] - maxPeak_kW) * energyModel.p_timeStep_h;
			if(peakSurface_kWh <= batteryStorageCapacity_kWh){ //if area is larger than capacity, save previous limit + area; Otherwise, limit is set at slightly too large area when breaking out of while loop
				prevMaxPeak_kW = maxPeak_kW; //save previous limit
				prevPeakSurface_kWh = peakSurface_kWh; //save previous area
			}
		}
	}
}
traceln("The maximum peak power is " + Arrays.stream(nettoBalance_kW).max().getAsDouble() + " kW");
traceln("The limit it starts shaving is above " + prevMaxPeak_kW + " kW");
traceln("Previous peak surface is "+ prevPeakSurface_kWh + " kWh");
traceln("Peak surface is "+ peakSurface_kWh + " kWh");
//Initialize chargepoint array
v_batteryDischargingPeakShavingAdvancedForecast_kW = new double[96];

for(int i = 0; i < nettoBalance_kW.length; i++){
	//traceln("The netto balance is "+ nettoBalance_kW[i] + " kW");
  	if(nettoBalance_kW[i] > prevMaxPeak_kW){//Flatten the peaks above the maximum defined peak after shaving
  		v_batteryDischargingPeakShavingAdvancedForecast_kW[i] += prevMaxPeak_kW - nettoBalance_kW[i];
  		//traceln("Discharge Power is "+ v_batteryDischargingPeakShavingAdvancedForecast_kW[i] + " kW");
  	}
  	/*else if(nettoBalance_kW[i] < minValley_kW){//Charge when there is export of energy
  		v_batteryDischargingPeakShavingAdvancedForecast_kW[i] += -nettoBalance_kW[i];
  	}*/
  	else{
  		v_batteryDischargingPeakShavingAdvancedForecast_kW[i] += 0;
  	}
}
/*ALCODEEND*/}

double f_valleyFillingGridAdvancedForecast()
{/*ALCODESTART::1751016595990*/
double amountOfHoursInADay = 24;
double[] nettoBalance_kW = new double[96];

//For simulation that cross the year end
double hour_of_simulation_year = energyModel.t_h - energyModel.p_runStartTime_h;
//traceln("hour_of_year: " + hour_of_simulation_year);

int startTimeDayIndex = roundToInt(hour_of_simulation_year/energyModel.p_timeStep_h);
int endTimeDayIndex = roundToInt((hour_of_simulation_year + 24)/energyModel.p_timeStep_h);
int startTimeDayIndex_h = roundToInt(hour_of_simulation_year);
int endTimeDayIndex_h = roundToInt(hour_of_simulation_year + 24);
//traceln("start=" + startTimeDayIndex + ", end=" + endTimeDayIndex);

//Get elec consumption profile
GridNode GN = p_parentNodeElectric;

if(hour_of_simulation_year == 0){ //start of simulation; half a day
	for (GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){
		J_EAProfile elecConsumptionProfile = findFirst(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
		J_EAConsumption elecConsumptionConsumptionAsset = findFirst(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
		if(elecConsumptionProfile != null){ //double[]; nettoBalance = 1 day forecast of one GC; nettoBalanceTotal is addition of all GCs
			double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, roundToInt((hour_of_simulation_year + 12)/energyModel.p_timeStep_h)), 1/energyModel.p_timeStep_h);
			//System.arraycopy(tempNettoBalance_kW, 0, temp_kW, 48, 48);
			for (int i = 0; i < tempNettoBalance_kW.length; i++) {
    			nettoBalance_kW[i+48] += tempNettoBalance_kW[i];
			}
		}
		if(elecConsumptionConsumptionAsset != null){//table function 
			for(double time = energyModel.t_h + 12; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
				nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
			}
		}
	}
	for(double time = energyModel.t_h + 12; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
		nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] -= energyModel.pp_PVProduction35DegSouth_fr.getValue(time)*GN.v_totalInstalledPVPower_kW;
	}
} else if(hour_of_simulation_year == (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h - 6)){ //end of simulation; half a day
	for (GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){
		J_EAProfile elecConsumptionProfile = findFirst(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
		J_EAConsumption elecConsumptionConsumptionAsset = findFirst(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
		if(elecConsumptionProfile != null){ //double[]; nettoBalance = 1 day forecast of one GC; nettoBalanceTotal is addition of all GCs
			double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, roundToInt((hour_of_simulation_year + 12)/energyModel.p_timeStep_h)), 1/energyModel.p_timeStep_h);
			//System.arraycopy(tempNettoBalance_kW, 0, temp_kW, 48, 48);
			for (int i = 0; i < tempNettoBalance_kW.length; i++) {
    			nettoBalance_kW[i] += tempNettoBalance_kW[i];
			}
		}
		if(elecConsumptionConsumptionAsset != null){//table function 
			for(double time = energyModel.t_h; time < energyModel.t_h + 12; time += energyModel.p_timeStep_h){
				nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
			}
		}
	}
	for(double time = energyModel.t_h; time < energyModel.t_h + 12; time += energyModel.p_timeStep_h){
		nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] -= energyModel.pp_PVProduction35DegSouth_fr.getValue(time)*GN.v_totalInstalledPVPower_kW;
	}
} else{ //daily profile; noon-noon

	for (GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){

		J_EAProfile elecConsumptionProfile = findFirst(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
		J_EAConsumption elecConsumptionConsumptionAsset = findFirst(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
		if(elecConsumptionProfile != null){ //double[]; nettoBalance = 1 day forecast of one GC; nettoBalanceTotal is addition of all GCs
			double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), 1/energyModel.p_timeStep_h);
			for (int i = 0; i < tempNettoBalance_kW.length; i++) {
    			nettoBalance_kW[i] += tempNettoBalance_kW[i];
			}
		}
		if(elecConsumptionConsumptionAsset != null){//table function 
			for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
				nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
			}
		}
	}
	for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
		nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] -= energyModel.pp_PVProduction35DegSouth_fr.getValue(time)*GN.v_totalInstalledPVPower_kW;
	}
}

//Calculate integral upper area
double precision_kW = 0.01;
   
double batteryStorageCapacity_kWh = p_batteryAsset.getStorageCapacity_kWh();
double currentSoC_r = p_batteryAsset.getCurrentStateOfCharge();
double availableCharge_kWh = batteryStorageCapacity_kWh - currentSoC_r*batteryStorageCapacity_kWh;

double minValley_kW = Arrays.stream(nettoBalance_kW).min().getAsDouble();
double prevMinValley_kW = minValley_kW;
double valleySurface_kWh = 0;
double prevValleySurface_kWh = 0;

while(valleySurface_kWh < batteryStorageCapacity_kWh){ //assumption that battery SoC is always 100% //misses failsafe
	minValley_kW += precision_kW; //set limit lower
	valleySurface_kWh = 0;
	for(int i = 0; i < nettoBalance_kW.length; i++){
		if(nettoBalance_kW[i] < minValley_kW){
			valleySurface_kWh += (minValley_kW - nettoBalance_kW[i]) * energyModel.p_timeStep_h;
			if(valleySurface_kWh <= prevPeakSurface_kWh){ //if area is larger than Peak Surface area, save previous limit + area; Otherwise, limit is set at slightly too large area when breaking out of while loop
				prevMinValley_kW = minValley_kW; //save previous limit
				prevValleySurface_kWh = valleySurface_kWh; //save previous area
			}
		}
	}
}
//traceln("The minimum valley power is " + Arrays.stream(nettoBalance_kW).min().getAsDouble() + " kW");
//traceln("The limit it starts valley filling is below " + prevMinValley_kW + " kW");
//traceln("Previous peak surface is "+ prevPeakSurface_kWh + " kWh");
//traceln("Valley surface is "+ valleySurface_kWh + " kWh");
//traceln("Previous valley surface is "+ prevValleySurface_kWh + " kWh");

//Initialize chargepoint array
v_batteryChargingValleyFillingAdvancedForecast_kW = new double[96];

for(int i = 0; i < nettoBalance_kW.length; i++){
  	//traceln("The netto balance is "+ nettoBalance_kW[i] + " kW");
  	if(nettoBalance_kW[i] < prevMinValley_kW){//Flatten the peaks above the maximum defined peak after shaving
  		v_batteryChargingValleyFillingAdvancedForecast_kW[i] += prevMinValley_kW - nettoBalance_kW[i];
  		//traceln("Charging Power is "+ v_batteryChargingValleyFillingAdvancedForecast_kW[i] + " kW");
  	}
  	else{
  		v_batteryChargingValleyFillingAdvancedForecast_kW[i] += 0;
  	}
}

/*for (GridConnection GC : GN.f_getAllLowerLVLConnectedGridConnections()){

	J_EAProfile elecConsumptionProfile = findFirst(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
	J_EAConsumption elecConsumptionConsumptionAsset = findFirst(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
	if(elecConsumptionProfile != null){ //double[]; nettoBalance = 1 day forecast of one GC; nettoBalanceTotal is addition of all GCs
		if(startTimeDayIndex == 0){
			double[] nettoBalanceValleyHalfDay_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, startTimeDayIndex+(12/energyModel.p_timeStep_h)), 1/energyModel.p_timeStep_h);
		} else if(endTimeDayIndex == (energyModel.p_runEndTime_h/energyModel.p_timeStep_h)){
			double[] nettoBalanceValleyHalfDay_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, endTimeDayIndex-(12/energyModel.p_timeStep_h), endTimeDayIndex, 1/energyModel.p_timeStep_h);
		} else {
			double[] tempNettoBalanceValley_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex-48, startTimeDayIndex+48), 1/energyModel.p_timeStep_h);
		}
		for (int i = 0; i < tempNettoBalanceValley_kW.length; i++) {
    		nettoBalanceValley_kW[i] += tempNettoBalanceValley_kW[i];
		}
	}
	if(elecConsumptionConsumptionAsset != null){//table function 
		for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
			nettoBalanceValley_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
		}
	}
}*/
/*ALCODEEND*/}

