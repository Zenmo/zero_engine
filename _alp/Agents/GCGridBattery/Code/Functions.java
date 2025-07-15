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

double f_batteryManagementPriceGrid()
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
	
	double SOC_deficit_fr = SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge_fr();
	
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

double f_batteryManagementBalanceGrid()
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
	
		chargeSetpoint_kW = FeedbackGain_kWpSOC*(SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge_fr());
		
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

double f_batteryManagementBalanceSupply()
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

	
	chargeSetpoint_kW = FeedforwardGain_kWpKw * (currentCoopElectricitySurplus_kW - chargeOffset_kW) + (SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge_fr()) * FeedbackGain_kWpSOC;
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
	//traceln("hello?");
}

/*ALCODEEND*/}

double f_batteryManagementBalanceCOOP()
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
	
		chargeSetpoint_kW = FeedbackGain_kWpSOC*(SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge_fr());
		chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW()));
	}
}

/*ALCODEEND*/}

double f_batteryManagementBas()
{/*ALCODESTART::1746605418870*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	
	double chargeSetpoint_kW = 0;
	
	chargeSetpoint_kW = v_batteryAlgorithmBas.calculateChargeSetpoint_kW(p_batteryAsset.getCurrentStateOfCharge_fr()); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW()));

}
/*ALCODEEND*/}

double f_manageBattery_overwrite()
{/*ALCODESTART::1752589957306*/
if (p_batteryAsset != null) {
	if (p_batteryAsset.getStorageCapacity_kWh() > 0 && p_batteryAsset.getCapacityElectric_kW() > 0) {
		// We have a battery asset we want to operate, choose the management function that will set the powerfraction
		switch (p_batteryOperationMode) {
			case OFF:
				break;
			case SELF_CONSUMPTION:
				throw new RuntimeException("current implementation of SELF_CONSUMPTION is not a valid algorithm for GCGridBattery");
				// TODO: Need to write a (or 2) functions for the GCGridBattery that do selfconsumption on the level of gridnode (or coop)
				//f_batteryManagementSelfConsumption();
				//break;
			case BALANCE:
				throw new RuntimeException("BatteryOperationMode Balance is deprecated, please use PEAK_SHAVING_SIMPLE or write your own custom algorithm");
				//f_batteryManagementBalance();
				//break;
			case PRICE:
				f_batteryManagementPrice();
				break;
			case NODAL_PRICING:
				throw new RuntimeException("BatteryOperationMode NodalPrice is deprecated, no direct equivalent available. Use PRICE or write your own custom algorithm");
				//f_batteryManagementNodalPricing();
				//break;
			case PEAK_SHAVING_SIMPLE:
				throw new RuntimeException("current implementation of PEAK_SHAVING_SIMPLE is not a valid algorithm for GCGridBattery");			
				//f_batteryManagementPeakShavingSimple();
				//break;
			case PEAK_SHAVING_FORECAST:
				throw new RuntimeException("current implementation of PEAK_SHAVING_FORECAST is not a valid algorithm for GCGridBattery");						
				//f_batteryManagementPeakShavingForecast();
				//break;
			case BALANCE_GRID:
				f_batteryManagementBalanceGrid();
				break;
			case BALANCE_COOP:
				f_batteryManagementBalanceCOOP();
				break;
			case BALANCE_SUPPLY:
				f_batteryManagementBalanceSupply();
				break;
			case BATTERY_ALGORITHM_BAS:
				f_batteryManagementBas();
				break;
			case CUSTOM:
				f_batteryManagementCustom();
				break;
			default:
				throw new RuntimeException("Chosen battery operation mode: " + p_batteryOperationMode.toString() + " unavailable for GridConnection of type: " + this.getClass());
		}
		// Now actually operate the asset and update the flows in the GC, f_updateAllFlows will automatically limit the powerFraction between -1 and 1
		p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
	}
}
/*ALCODEEND*/}

