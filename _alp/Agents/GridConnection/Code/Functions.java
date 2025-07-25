double f_connectToParents()
{/*ALCODESTART::1658500398176*/
GridNode myParentNodeElectric = findFirst(energyModel.pop_gridNodes, p->p.p_gridNodeID.equals(p_parentNodeElectricID)) ;
if( myParentNodeElectric != null ) {
	p_parentNodeElectric = myParentNodeElectric;
	myParentNodeElectric.f_connectToChild(this);
}

GridNode myParentNodeHeat = findFirst(energyModel.pop_gridNodes, p->p.p_gridNodeID.equals(p_parentNodeHeatID)) ;
if( myParentNodeHeat != null ) {
	p_parentNodeHeat = myParentNodeHeat;
	myParentNodeHeat.f_connectToChild(this);
}

if ( p_owner == null ){
	p_owner = findFirst(energyModel.pop_connectionOwners, p->p.p_actorID.equals(p_ownerID));
}

if ( p_owner != null ){
	p_owner.f_connectToChild(this);
}
/*EnergySupplier myParentEnergySupplier = findFirst(main.pop_energySuppliers, p->p.p_actorID.equals(p_ownerID)) ;
if( myParentEnergySupplier instanceof EnergySupplier) {
	//p_ownerActor = myParentEnergySupplier;
	l_ownerActor.connectTo(myParentEnergySupplier);
	myParentEnergySupplier.f_connectToChild(this);
}
EnergyCoop myParentEnergyCoop = findFirst(main.pop_energyCoops, p->p.p_actorID.equals(p_ownerID)) ;
if( myParentEnergyCoop instanceof EnergyCoop) {
	//p_ownerActor = myParentEnergyCoop;
	l_ownerActor.connectTo(myParentEnergyCoop);
	myParentEnergyCoop.f_connectToChild(this);
}*/
/*ALCODEEND*/}

double f_connectionMetering()
{/*ALCODESTART::1660212665961*/
if ( abs(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.HEAT)) > 0.1 && p_parentNodeHeat == null ) {
	//if (p_BuildingThermalAsset == null || !p_BuildingThermalAsset.hasHeatBuffer()) {
		traceln("heat consumption: %s kW", fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT));
		traceln("heat production: %s kW", fm_currentProductionFlows_kW.get(OL_EnergyCarriers.HEAT));
		traceln("Heat unbalance in gridConnection: " + p_gridConnectionID);
		pauseSimulation();
	//}
}

// Further Subdivision of asset types within energy carriers
v_fixedConsumptionElectric_kW = 0;
for (J_EA j_ea : c_fixedConsumptionElectricAssets) {
	v_fixedConsumptionElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_hydrogenElectricityConsumption_kW = 0;
for (J_EA j_ea : c_electrolyserAssets) {
	v_hydrogenElectricityConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_heatPumpElectricityConsumption_kW = 0;
for (J_EA j_ea : c_electricHeatpumpAssets) {
	v_heatPumpElectricityConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_evChargingPowerElectric_kW = 0;
for (J_EA j_ea : c_EvAssets) {
	if (j_ea instanceof J_EAEV) {
		if (((J_EAEV)j_ea).vehicleScaling == 0) {
			continue;
		}
	}
	v_evChargingPowerElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_batteryPowerElectric_kW = 0;
v_batteryStoredEnergy_kWh = 0;
for (J_EA j_ea : c_batteryAssets) {
	if (((J_EAStorageElectric)j_ea).getCapacityElectric_kW() != 0 && ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh() != 0) {
		v_batteryPowerElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
		v_batteryStoredEnergy_kWh += ((J_EAStorageElectric)j_ea).getCurrentStateOfCharge_kWh();
		
	}
}

v_CHPProductionElectric_kW = 0;
for (J_EA j_ea : c_chpAssets) {
	v_CHPProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_pvProductionElectric_kW = 0;
for (J_EA j_ea : c_pvAssets) {
	v_pvProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_windProductionElectric_kW = 0;
for (J_EA j_ea : c_windAssets) {
	v_windProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}

v_ptProductionHeat_kW = 0;
for (J_EA j_ea : c_ptAssets) {
	v_ptProductionHeat_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.HEAT);
}

//Set asset flows
v_assetFlows.setFlows(v_fixedConsumptionElectric_kW,
	v_heatPumpElectricityConsumption_kW,
	max(0,v_evChargingPowerElectric_kW),
	max(0,v_batteryPowerElectric_kW),
	v_hydrogenElectricityConsumption_kW,
	v_electricHobConsumption_kW,
	v_districtHeatDelivery_kW,
	v_pvProductionElectric_kW,
	v_windProductionElectric_kW,
	v_ptProductionHeat_kW,
	v_CHPProductionElectric_kW,
	max(0,-v_batteryPowerElectric_kW),
	max(0,-v_evChargingPowerElectric_kW),
	v_batteryStoredEnergy_kWh/1000);

// 
if (energyModel.v_isRapidRun){
	f_rapidRunDataLogging();
} else {
	f_fillLiveDataSets();
}
/*ALCODEEND*/}

double f_operateFlexAssets()
{/*ALCODESTART::1664961435385*/
//Must be overwritten in child agent
/*ALCODEEND*/}

double f_calculateEnergyBalance()
{/*ALCODESTART::1668528273163*/
v_previousPowerElectricity_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
v_previousPowerHeat_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

fm_currentProductionFlows_kW.clear();
fm_currentConsumptionFlows_kW.clear();
fm_currentBalanceFlows_kW.clear();

v_currentPrimaryEnergyProduction_kW = 0;
v_currentFinalEnergyConsumption_kW = 0;

v_currentEnergyCurtailed_kW = 0;
v_currentPrimaryEnergyProductionHeatpumps_kW = 0;

// Categorical power flows
v_fixedConsumptionElectric_kW = 0;
v_electricHobConsumption_kW = 0;
v_heatPumpElectricityConsumption_kW = 0;
v_hydrogenElectricityConsumption_kW = 0;
v_evChargingPowerElectric_kW = 0;
v_batteryPowerElectric_kW = 0;
v_windProductionElectric_kW = 0;
v_pvProductionElectric_kW = 0;
v_ptProductionHeat_kW = 0;
v_conversionPowerElectric_kW = 0;
v_CHPProductionElectric_kW = 0;
v_districtHeatDelivery_kW = 0;

if (v_enableNFato) {
	f_nfatoUpdateConnectionCapacity();
}

c_tripTrackers.forEach(t -> t.manageActivities((energyModel.t_h-energyModel.p_runStartTime_h)*60));

f_operateFixedAssets();
f_operateFlexAssets();

f_curtailment();

f_connectionMetering();

//if (!Double.isFinite(v_currentPowerElectricity_kW)) {
//	traceln("Gridconnection %s with connection_id %s has NaN or infinite v_currentPowerElectricity_kW at time %s!", p_gridConnectionID, p_company_connection_id, energyModel.t_h);
//}
/*ALCODEEND*/}

double f_operateFixedAssets()
{/*ALCODESTART::1668528300576*/
c_dieselVehicles.forEach(v -> v.f_updateAllFlows(0));
c_hydrogenVehicles.forEach(v -> v.f_updateAllFlows(0));
c_consumptionAssets.forEach(c -> c.f_updateAllFlows(0));
c_productionAssets.forEach(p -> p.f_updateAllFlows(0));
c_profileAssets.forEach(p -> p.f_updateAllFlows(energyModel.t_h));
/*ALCODEEND*/}

double f_resetStates()
{/*ALCODESTART::1668983912731*/
fm_currentProductionFlows_kW.clear();
fm_currentConsumptionFlows_kW.clear();
fm_currentBalanceFlows_kW.clear();

v_previousPowerElectricity_kW = 0;
v_previousPowerHeat_kW = 0;
v_electricityPriceLowPassed_eurpkWh = 0;
v_currentElectricityPriceConsumption_eurpkWh  = 0;

v_rapidRunData.resetAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();

//Reset specific variables/collections in specific GC types (GCProduction, GConversion, etc.)
f_resetSpecificGCStates();

/*ALCODEEND*/}

double f_manageHeatingAssets()
{/*ALCODESTART::1669025846794*/
// TODO: This only works for fixed heat demands; also need to implement heating of a building modeled as a ThermalStorageAsset! [GH 21/11/2022]
if(p_heatBuffer != null){
	double chargeSetpoint_kW = -fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
	p_heatBuffer.v_powerFraction_fr = chargeSetpoint_kW / p_heatBuffer.getCapacityHeat_kW();
	p_heatBuffer.f_updateAllFlows(p_heatBuffer.v_powerFraction_fr);
}

double powerDemand_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

if(powerDemand_kW < 0 && v_liveAssetsMetaData.hasPT){//If there is 'overproduction' of heat, that can not be collected by the heat buffer: no power demand -> will be curtailed.
	powerDemand_kW = 0;
}

if ( p_BuildingThermalAsset == null ) {
	if ( p_secondaryHeatingAsset == null ) { // Just one heating asset
		if ( p_primaryHeatingAsset== null ) {
			if (powerDemand_kW > 0) {
				traceln("No heating assets for GridConnection " + p_gridConnectionID);
			}
		} else {
			if ( p_primaryHeatingAsset instanceof J_EAConversionGasBurner || p_primaryHeatingAsset instanceof J_EAConversionHeatDeliverySet || p_primaryHeatingAsset instanceof J_EAConversionHydrogenBurner || p_primaryHeatingAsset instanceof J_EAConversionHeatPump) { // when there is only a gas burner or DH set
				p_primaryHeatingAsset.v_powerFraction_fr = min(1,powerDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW());
			}
			else if(p_primaryHeatingAsset instanceof J_EAConversionGasCHP){
				p_primaryHeatingAsset.v_powerFraction_fr = min(1,powerDemand_kW / ((J_EAConversionGasCHP)p_primaryHeatingAsset).getOutputHeatCapacity_kW());
			} 
			else {
				traceln("GridConnection " + p_gridConnectionID + " has a single unsupported heating asset!");
			}
		}
	}
	else if (p_primaryHeatingAsset== null && p_secondaryHeatingAsset != null && v_hasQuarterHourlyValues){
		if(p_secondaryHeatingAsset instanceof J_EAConversionGasBurner){
			p_secondaryHeatingAsset.v_powerFraction_fr = min(1,powerDemand_kW / p_secondaryHeatingAsset.getOutputCapacity_kW());
			p_secondaryHeatingAsset.f_updateAllFlows(p_secondaryHeatingAsset.v_powerFraction_fr);
		} else {
				traceln("GridConnection " + p_gridConnectionID + " has a single unsupported secondary heating asset!");
		}
	} else { // Two heating assets
		if ( p_primaryHeatingAsset instanceof J_EAConversionHeatPump && p_secondaryHeatingAsset instanceof J_EAConversionGasBurner) { // Heatpump and gasburner, switch based on heatpump COP)
			//((J_EAConversionHeatPump)p_primaryHeatingAsset.j_ea).updateAmbientTemp(main.v_currentAmbientTemperature_degC); // update heatpump temp levels! <-- waarom dit gebeurt al in de main (peter 21-02-23)
			double HP_COP = ((J_EAConversionHeatPump)p_primaryHeatingAsset).getCOP();
			double COP_tres = 3.5; // TODO: Make data agnostic! Also, this line doesn't have to be evaluated every timestep.
			if ( HP_COP < COP_tres ) { // switch to gasburner when HP COP is below treshold
				//traceln("Hybrid HP: Switching to gas burner");
				p_primaryHeatingAsset.v_powerFraction_fr = 0;
				p_secondaryHeatingAsset.v_powerFraction_fr = min(1,powerDemand_kW / p_secondaryHeatingAsset.getOutputCapacity_kW());
			} else { // heatpump when COP is above treshold
				//traceln("Hybrid HP: Using heatpump with COP " + HP_COP);
				p_primaryHeatingAsset.v_powerFraction_fr = min(1,powerDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW());
				p_secondaryHeatingAsset.v_powerFraction_fr = 0;//min(1,currentDHWdemand_kW / p_secondaryHeatingAsset.j_ea.getHeatCapacity_kW());
			}
		} else {
			traceln("**** EXCEPTION ****: Unsupported combination of heatings systems in house " + p_gridConnectionID);
			p_primaryHeatingAsset.v_powerFraction_fr = 0;
			p_secondaryHeatingAsset.v_powerFraction_fr = 0;
			//p_BuildingThermalAsset.v_powerFraction_fr = 0;
		}
		p_secondaryHeatingAsset.f_updateAllFlows(p_secondaryHeatingAsset.v_powerFraction_fr);
		//v_conversionPowerElectric_kW += p_primaryHeatingAsset.electricityConsumption_kW - p_primaryHeatingAsset.electricityProduction_kW;//			traceln("heatpump electricity consumption: " + (p_primaryHeatingAsset.electricityConsumption_kW - p_primaryHeatingAsset.electricityProduction_kW));
		/*if (p_secondaryHeatingAsset instanceof J_EAConversionHeatPump) {
			v_heatPumpElectricityConsumption_kW += p_primaryHeatingAsset.electricityConsumption_kW - p_primaryHeatingAsset.electricityProduction_kW;
		}*/
	}
	if (p_primaryHeatingAsset != null) {
		p_primaryHeatingAsset.f_updateAllFlows(p_primaryHeatingAsset.v_powerFraction_fr);
		//v_conversionPowerElectric_kW += flowsArray[4] - flowsArray[0]; //p_primaryHeatingAsset.electricityConsumption_kW - p_primaryHeatingAsset.electricityProduction_kW;
		/*if (p_primaryHeatingAsset instanceof J_EAConversionHeatPump) {
			v_heatPumpElectricityConsumption_kW += flowsArray[4] - flowsArray[0];
		}*/
	}
} else { // TODO: Implement thermostat functionality for thermal storage asset. Where to get temp setpoint?
	traceln("No thermostat functionality available to manage p_BuildingThermalAsset!!");
	p_primaryHeatingAsset.f_updateAllFlows(0);
	/*	v_conversionPowerElectric_kW += flowsArray[4] - flowsArray[0]; //p_primaryHeatingAsset.electricityConsumption_kW - p_primaryHeatingAsset.electricityProduction_kW;
	if (p_primaryHeatingAsset instanceof J_EAConversionHeatPump) {
		v_heatPumpElectricityConsumption_kW += flowsArray[4] - flowsArray[0]; 
	}*/
	p_secondaryHeatingAsset.f_updateAllFlows(0);	
	/*v_conversionPowerElectric_kW += flowsArray[4] - flowsArray[0]; //p_primaryHeatingAsset.electricityConsumption_kW - p_primaryHeatingAsset.electricityProduction_kW;
	if (p_secondaryHeatingAsset instanceof J_EAConversionHeatPump) {
		v_heatPumpElectricityConsumption_kW += flowsArray[4] - flowsArray[0];
	}*/
	p_BuildingThermalAsset.f_updateAllFlows(0);
}
/*ALCODEEND*/}

double f_manageCharging()
{/*ALCODESTART::1671095995172*/
double availableCapacityFromBatteries = p_batteryAsset == null ? 0 : p_batteryAsset.getCapacityAvailable_kW(); 
//double availableChargingCapacity = v_allowedCapacity_kW + availableCapacityFromBatteries - v_currentPowerElectricity_kW;
double availableChargingCapacity = v_liveConnectionMetaData.contractedDeliveryCapacity_kW + availableCapacityFromBatteries - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
switch (p_chargingAttitudeVehicles) {
	case SIMPLE:
		f_simpleCharging();
	break;
	case MAX_SPREAD:
		f_maxSpreadCharging();
	break;
	case MAX_POWER:
		f_maxPowerCharging( max(0, availableChargingCapacity));
	break;
	case CHEAP:
		v_currentElectricityPriceConsumption_eurpkWh = p_owner.f_getElectricityPrice(v_liveConnectionMetaData.contractedDeliveryCapacity_kW); 
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( v_currentElectricityPriceConsumption_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		f_chargeOnPrice( v_currentElectricityPriceConsumption_eurpkWh, max(0, availableChargingCapacity));
	break;
	case V2G:
		v_currentElectricityPriceConsumption_eurpkWh = p_owner.f_getElectricityPrice(v_liveConnectionMetaData.contractedDeliveryCapacity_kW); 
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( v_currentElectricityPriceConsumption_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		f_chargeOnPrice_V2G( v_currentElectricityPriceConsumption_eurpkWh, max(0, availableChargingCapacity));
	break;
}

/*ALCODEEND*/}

double f_simpleCharging()
{/*ALCODESTART::1671095995175*/
// Removing items while going through a loop, so we do so in reverse order

ArrayList<J_EAEV> copiedVehicleList = new ArrayList<J_EAEV>(c_vehiclesAvailableForCharging);
int countDeletedItems = 0;

for ( int i = 0; i < copiedVehicleList.size(); i++ ) {
	J_EAEV ev = copiedVehicleList.get(i);
	if (ev.vehicleScaling != 0) {
		if( !ev.getAvailability() || ev.getCurrentStateOfCharge_fr() == 1 ) {
			ev.f_updateAllFlows( 0.0 );
			c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
			countDeletedItems ++;
		}
		else {
			ev.f_updateAllFlows( 1.0 );
		}
	}
}
/*ALCODEEND*/}

double f_maxSpreadCharging()
{/*ALCODESTART::1671095995177*/
ArrayList<J_EAEV> copiedVehicleList = new ArrayList<J_EAEV>(c_vehiclesAvailableForCharging);
int countDeletedItems = 0;

for ( int i = 0; i < copiedVehicleList.size(); i++ ){
	J_EAEV ev = copiedVehicleList.get(i);
	if (ev.vehicleScaling != 0) {
		if(!ev.getAvailability() || ev.getCurrentStateOfCharge_fr() == 1) {
			ev.f_updateAllFlows( 0.0 );
			c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
			countDeletedItems ++;
		}
		else {
			double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getCurrentStateOfCharge_kWh());
			double maxChargingPower_kW = ev.getCapacityElectric_kW();
			double chargeDeadline_h = floor((ev.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * energyModel.p_timeStep_h;
			
			double emptyKWhInBattery = ev.getStorageCapacity_kWh() - ev.getCurrentStateOfCharge_kWh();
			double timeToNextTrip_h = ev.tripTracker.v_nextEventStartTime_min / 60 - energyModel.t_h;			
			// At the end of the simulation the triptracker returns back to the start of the year, so we make sure the timeToNextTrip is not negative
			timeToNextTrip_h = (timeToNextTrip_h % 8760 + 8760) % 8760;
			
			double chargingPower_kW;		
			if ( energyModel.t_h >= chargeDeadline_h && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				//traceln("Urgency charging! May exceed connection capacity!");
				chargingPower_kW = maxChargingPower_kW / 2 ;	// delen door 2 als quickfix doordat HAVI trucks anders mega pieken veroorzaken in de middag waardoor 'slim' laden beetje nutteloos lijkt
			}
			else {
				chargingPower_kW = emptyKWhInBattery / timeToNextTrip_h;
			}
			
			chargingPower_kW = min(chargingPower_kW, maxChargingPower_kW); // cap the charging speed at the electric capacity
			double ratio_fr = chargingPower_kW / maxChargingPower_kW;
			ev.f_updateAllFlows( ratio_fr );
		}
	}
}
/*ALCODEEND*/}

double f_maxPowerCharging(double availableCapacityForCharging_kW)
{/*ALCODESTART::1671095995179*/
double remainingChargingPower_kW = availableCapacityForCharging_kW;

ArrayList<J_EAEV> copiedVehicleList = new ArrayList<J_EAEV>(c_vehiclesAvailableForCharging);
int countDeletedItems = 0;

// Sort vehicles by time until charge deadline
copiedVehicleList.sort((ev1, ev2) -> Double.compare(f_getChargeDeadline(ev1), f_getChargeDeadline(ev2)));
c_vehiclesAvailableForCharging = copiedVehicleList;

for ( int i = 0; i < copiedVehicleList.size(); i++ ){
	J_EAEV ev = copiedVehicleList.get(i);
	if (ev.vehicleScaling != 0) {
		if( !ev.getAvailability() || ev.getCurrentStateOfCharge_fr() == 1 ) {
			ev.f_updateAllFlows( 0.0 );
			c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
			countDeletedItems ++;
		}
		else {
			//traceln("current time: " + energyModel.t_h);
			//traceln("ev: " + ev);
			//traceln("dist: " + ev.getTripTracker().v_tripDist_km);
			double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getCurrentStateOfCharge_kWh());
			//traceln("chargeNeedForNextTrip_kWh: " + chargeNeedForNextTrip_kWh);
			double maxChargingPower_kW = ev.getCapacityElectric_kW();
			double chargeDeadline_h = floor((ev.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * energyModel.p_timeStep_h;
			
			//double starttime = ev.tripTracker.v_nextEventStartTime_min / 60;
			//traceln("starttime: " + starttime);
			//traceln("chargeDeadline_h: " + chargeDeadline_h);
			
			double chargingPower_kW;
			if ( energyModel.t_h >= chargeDeadline_h && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				//traceln("Urgency charging! May exceed connection capacity!");
				chargingPower_kW = maxChargingPower_kW;	
			}
			else {
				chargingPower_kW = remainingChargingPower_kW;
			}
			
			chargingPower_kW = min(chargingPower_kW, maxChargingPower_kW);
			remainingChargingPower_kW = max(0, remainingChargingPower_kW - chargingPower_kW);
			double ratio_fr = chargingPower_kW / maxChargingPower_kW;
			ev.f_updateAllFlows( ratio_fr );
			//gridConnection.v_evChargingPowerElectric_kW += flowsArray[4] - flowsArray[0];
			
			//double x = flowsArray[4] - flowsArray[0];
			//traceln("flow: " + x);
			//traceln("ev: " + ev);
			
		}
	}
}
/*ALCODEEND*/}

double f_chargeOnPrice(double currentElectricityPriceConsumption_eurpkWh,double availableChargingPower_kW)
{/*ALCODESTART::1671095995181*/
ArrayList<J_EAEV> copiedVehicleList = new ArrayList<J_EAEV>(c_vehiclesAvailableForCharging);
int countDeletedItems = 0;

double remainingChargePower_kW = availableChargingPower_kW;

for ( int i = 0; i < copiedVehicleList.size(); i++ ){
	J_EAEV vehicle = copiedVehicleList.get(i);
	
	if (vehicle.getVehicleScaling() == 0) {
		continue;
	}
	
	if(!vehicle.getAvailability() ){
		vehicle.f_updateAllFlows( 0 );
		c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
		countDeletedItems ++;
	} else {
		//double availableChargingPower_kW = v_allowedCapacity_kW - v_currentPowerElectricity_kW - v_chargingPower_kW;
		double chargeNeedForNextTrip_kWh = max(0, vehicle.energyNeedForNextTrip_kWh - vehicle.getCurrentStateOfCharge_kWh());
		//double timeToNexTrip_min = vehicle.getMobilityTracker().v_nextTripStartTime_min - energyModel.t_h*60;
		double maxChargingPower_kW = vehicle.getCapacityElectric_kW();
		double timeToNextTrip_min = vehicle.tripTracker.v_nextEventStartTime_min - energyModel.t_h*60;
		double chargeDeadline_min = floor((vehicle.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * 60 * energyModel.p_timeStep_h;

		double priceGain_kWhpeur = 1; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
		double urgencyGain_eurpkWh = 0.4; // How strongly WTP-price shifts based on charging flexibility
		double maxSpreadChargingPower_kW = min(chargeNeedForNextTrip_kWh / (max(1, timeToNextTrip_min - v_additionalTimeSpreadCharging_MIN) / 60), maxChargingPower_kW);
		//traceln("maxSpreadChargingPower_kW" + maxSpreadChargingPower_kW);
		double WTPoffset_eurpkWh = 0.05*(1-energyModel.v_WindYieldForecast_fr);//0.15; // Adds an offset to the WTP price; this value is very much context specific, depending on market conditions during charging periods
		
		double chargeSetpoint_kW = 0;

		if ( energyModel.t_h*60 >= chargeDeadline_min & chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
			//traceln("Urgency charging! May exceed connection capacity!");
			chargeSetpoint_kW = maxChargingPower_kW;				
		} else if ( vehicle.getCurrentStateOfCharge_fr() < 0.15 ) {
			chargeSetpoint_kW = min(remainingChargePower_kW, maxChargingPower_kW);
		} else {
			//double WTPprice_eurpkWh = v_electricityPriceLowPassed_eurpkWh - flexibilityGain_eurph * (chargeDeadline_min - energyModel.t_h*60 - 600);
			v_WTPCharging_eurpkWh = WTPoffset_eurpkWh + v_electricityPriceLowPassed_eurpkWh + urgencyGain_eurpkWh * ( maxSpreadChargingPower_kW / maxChargingPower_kW ); // Scale WTP based on flexibility expressed in terms of power-fraction
			//WTPprice_eurpkWh = WTPoffset_eurpkWh + (main.v_epexNext24hours_eurpkWh+v_electricityPriceLowPassed_eurpkWh)/2 + flexibilityGain_eurpkWh * sqrt(maxSpreadChargingPower_kW/maxChargingPower_kW); // Scale WTP based on flexibility expressed in terms of power-fraction
			chargeSetpoint_kW = max(0, maxChargingPower_kW * (v_WTPCharging_eurpkWh / currentElectricityPriceConsumption_eurpkWh - 1) * priceGain_kWhpeur);
			chargeSetpoint_kW = min(remainingChargePower_kW, chargeSetpoint_kW);
			//traceln("Trying to charge cheaply, time " + energyModel.t_h*60 + " minutes, charge setpoint: " + chargeSetpoint_kW + " kW");
			/*if (this.getIndex() == 0){
				traceln("wtp = " + v_WTPCharging_eurpkWh);
				traceln("remainingChargePower_kW: " + remainingChargePower_kW + "charge setpoint kW: " + chargeSetpoint_kW);
			}*/
		}
		//}
		//traceln("Hello! Charge setpoint: " + chargeSetpoint_kW);
		//Pair<J_FlowsMap, Double> flowsPair = vehicle.f_updateAllFlows( chargeSetpoint_kW / maxChargingPower_kW );
		vehicle.f_updateAllFlows( chargeSetpoint_kW / maxChargingPower_kW );
		//v_evChargingPowerElectric_kW += flowsMap.get(OL_EnergyCarriers.ELECTRICITY);
		
		// This seems wrong? the evChargingPowerElectric is keeping track of the total, but is subtracted every time!!
		//remainingChargePower_kW = availableChargingPower_kW - v_evChargingPowerElectric_kW 

		remainingChargePower_kW = availableChargingPower_kW - vehicle.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);;
		
	}
	if( this instanceof GCHouse){
	((GCHouse)this).v_vehicleSOC_fr = vehicle.getCurrentStateOfCharge_fr();
}
}


/*ALCODEEND*/}

double f_setOperatingSwitches()
{/*ALCODESTART::1677512714652*/
if( this instanceof GCDistrictHeating ) { // Temporarily disabled while transfering to class-based energy assets!
	((GCDistrictHeating)this).f_setConfigurationBooleans();
}
/*ALCODEEND*/}

double f_connectToJ_EA_default(J_EA j_ea)
{/*ALCODESTART::1692799608559*/
for (OL_EnergyCarriers EC : j_ea.getActiveConsumptionEnergyCarriers()) {
	if (!v_activeConsumptionEnergyCarriers.contains(EC)) {
		v_activeEnergyCarriers.add(EC);
		v_activeConsumptionEnergyCarriers.add(EC);
			
		if (energyModel.b_isInitialized) {
			//Add EC to energyModel
			energyModel.f_addConsumptionEnergyCarrier(EC);
			
			//Initialize dataset
			DataSet dsDemand = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
			double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
			double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
			for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
				dsDemand.add( t, 0);
			}
			v_liveData.dsm_liveDemand_kW.put( EC, dsDemand);
		}
	}
}

for (OL_EnergyCarriers EC : j_ea.getActiveProductionEnergyCarriers()) {
	if (!v_activeProductionEnergyCarriers.contains(EC)) {
		v_activeEnergyCarriers.add(EC);
		v_activeProductionEnergyCarriers.add(EC);		
		if (energyModel.b_isInitialized) {
		
			//Add EC to energyModel
			energyModel.f_addProductionEnergyCarrier(EC);
			
			//Initialize datasets
			DataSet dsSupply = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
			double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
			double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
			for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
				dsSupply.add( t, 0);
			}
			v_liveData.dsm_liveSupply_kW.put( EC, dsSupply);
		}
	}
}

energyModel.c_energyAssets.add(j_ea);
c_energyAssets.add(j_ea);

if (j_ea instanceof J_EAVehicle) {
	J_EAVehicle vehicle = (J_EAVehicle)j_ea;
	if (vehicle instanceof J_EADieselVehicle) {
		c_dieselVehicles.add( (J_EADieselVehicle)vehicle );		
	} else if (vehicle instanceof J_EAHydrogenVehicle) {
		c_hydrogenVehicles.add((J_EAHydrogenVehicle)vehicle);		
	} else if (vehicle instanceof J_EAEV) {
		c_vehiclesAvailableForCharging.add((J_EAEV)vehicle);
		energyModel.c_EVs.add((J_EAEV)vehicle);	
		c_EvAssets.add(j_ea);
	}
	c_vehicleAssets.add(vehicle);		
	J_ActivityTrackerTrips tripTracker = vehicle.getTripTracker();
	if (tripTracker == null) { // Only provide tripTracker when vehicle doesn't have it yet!
		if (vehicle.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK || vehicle.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK || vehicle.energyAssetType == OL_EnergyAssetType.HYDROGEN_TRUCK) {
			int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsCsv, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, vehicle);
		} else if (vehicle.energyAssetType == OL_EnergyAssetType.DIESEL_VAN || vehicle.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN || vehicle.energyAssetType == OL_EnergyAssetType.HYDROGEN_VAN) {// No mobility pattern for business vans available yet!! Falling back to truck mobility pattern
			int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsCsv, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, vehicle);
			tripTracker.setAnnualDistance_km(30_000);
		} else {
			//traceln("Adding passenger vehicle to gridconnection %s", this);
			int rowIndex = uniform_discr(0, 200);//getIndex() % 200;
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_householdTripsCsv, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, vehicle);
			//tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_householdTripsExcel, 18, energyModel.t_h*60, vehicle);
			//int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			//tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsExcel, 2, energyModel.t_h*60, vehicle);
		}
		
		vehicle.tripTracker = tripTracker;	
	}
	c_tripTrackers.add( tripTracker );
	v_vehicleIndex ++;
} else if (j_ea instanceof J_EAConsumption) {
	c_consumptionAssets.add((J_EAConsumption)j_ea);	
	if (j_ea.energyAssetType == OL_EnergyAssetType.HOT_WATER_CONSUMPTION) {
		p_DHWAsset = (J_EAConsumption)j_ea;	
	}
	if( j_ea.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND ) {
		c_fixedConsumptionElectricAssets.add(j_ea);
	}
	if( j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB ) {
		c_electricHobAssets.add(j_ea);
	}
} else if (j_ea instanceof J_EAProduction) {
	c_productionAssets.add((J_EAProduction)j_ea);
	//energyModel.c_productionAssets.add((J_EAProduction)j_ea);
	
	if (j_ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC) {
		v_liveAssetsMetaData.hasPV = true;
		double capacity_kW = ((J_EAProduction)j_ea).getCapacityElectric_kW();
		v_liveAssetsMetaData.totalInstalledPVPower_kW += capacity_kW;
		if ( p_parentNodeElectric != null ) {
			p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, capacity_kW, true);
		}
		c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledPVPower_kW += capacity_kW);
		energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW += capacity_kW;
		c_pvAssets.add(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.WINDMILL) {
		v_liveAssetsMetaData.hasWindturbine = true;
		double capacity_kW = ((J_EAProduction)j_ea).getCapacityElectric_kW();
		v_liveAssetsMetaData.totalInstalledWindPower_kW += capacity_kW;
		if ( p_parentNodeElectric != null ) {
			p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, capacity_kW, true);
		}
		c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledWindPower_kW += capacity_kW);
		energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW += capacity_kW;
		c_windAssets.add(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL){
		v_liveAssetsMetaData.hasPT = true;
		c_ptAssets.add(j_ea);
	}
} else if (j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)j_ea);
	if ( j_ea.energyAssetType == OL_EnergyAssetType.GAS_PIT || j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB){
		if (j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB) {
			c_electricHobAssets.add(j_ea);
			//c_conversionElectricAssets.add(j_ea);
		}
		if (p_cookingTracker == null) {
			int rowIndex = uniform_discr(2, 300); 
			p_cookingTracker = new J_ActivityTrackerCooking(energyModel.p_cookingPatternCsv, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, (J_EAConversion)j_ea );			
		} else {
			p_cookingTracker.HOB = (J_EAConversion)j_ea;
		}
	} else if (j_ea instanceof J_EAConversionGasBurner) {
		if(p_heatingType == OL_GridConnectionHeatingType.HYBRID_HEATPUMP)
			p_secondaryHeatingAsset = (J_EAConversion)j_ea;
		else{
			p_primaryHeatingAsset = (J_EAConversion)j_ea;
		}
	} else if (j_ea instanceof J_EAConversionHeatPump) {
		energyModel.c_ambientDependentAssets.add(j_ea);
		c_electricHeatpumpAssets.add(j_ea);
		//c_conversionElectricAssets.add(j_ea);
		//traceln("added heatpump to the GC as primary heating asset.");

		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionHydrogenBurner) {
		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionElectrolyser || j_ea instanceof J_EAConversionElektrolyser) {
		c_electrolyserAssets.add(j_ea);
	} else if (j_ea instanceof J_EAConversionGasCHP) {
		c_chpAssets.add(j_ea);
		p_primaryHeatingAsset = (J_EAConversion)j_ea;
    }else if( j_ea instanceof J_EAConversionHeatDeliverySet ){
		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	}
} else if  (j_ea instanceof J_EAStorage) {
	c_storageAssets.add((J_EAStorage)j_ea);
	energyModel.c_storageAssets.add((J_EAStorage)j_ea);
	if (j_ea instanceof J_EAStorageHeat) {
		energyModel.c_ambientDependentAssets.add(j_ea);
		if (j_ea.energyAssetType == OL_EnergyAssetType.BUILDINGTHERMALS) {
			p_BuildingThermalAsset = (J_EABuilding)j_ea;
		}
		else {
			p_heatBuffer = (J_EAStorageHeat)j_ea;
		}
	} else if (j_ea instanceof J_EAStorageGas) {
		p_gasBuffer = (J_EAStorageGas)j_ea;
	} else if (j_ea instanceof J_EAStorageElectric) {
		p_batteryAsset = (J_EAStorageElectric)j_ea;
		c_batteryAssets.add(j_ea);
		double capacity_MWh = ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()/1000;
		v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += capacity_MWh;
		c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += capacity_MWh);
		energyModel.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += capacity_MWh;
		
	}
} else if  (j_ea instanceof J_EAProfile) {
	//p_energyProfile = (J_EAProfile)j_ea;
	c_profileAssets.add((J_EAProfile)j_ea);
	if (((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.CHARGING){
			//v_evChargingPowerElectric_kW += flowsArray[4] - flowsArray[0];
			c_EvAssets.add(j_ea);
		} else if( ((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD){
			//v_fixedConsumptionElectric_kW += flowsArray[4] - flowsArray[0];
			c_fixedConsumptionElectricAssets.add(j_ea);
		} else if( ((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.WINDTURBINE){
			//v_windProductionElectric_kW += flowsArray[0];
			c_windAssets.add(j_ea);
			// TODO: Add some to the total installed wind (of this GC, its GN, the energymodel (and its parent coop))
		} else if( ((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.HEATDEMAND){
			//Do nothing
		} else if( ((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.METHANEDEMAND){
			//Do nothing
		} else {
			traceln( "Unrecognized profile type!");
		}
} else if (j_ea instanceof J_EADieselTractor) {
	c_profileAssets.add((J_EAProfile)j_ea);
} else if (j_ea instanceof J_EACharger) {
	c_chargers.add((J_EACharger)j_ea);
	c_EvAssets.add(j_ea);
} else {
	if (!(this instanceof GCHouse && j_ea instanceof J_EAAirco)) {
		traceln("Unrecognized energy asset %s in gridconnection %s", j_ea, this);
	} 
}

/*ALCODEEND*/}

double f_connectToJ_EA(J_EA j_ea)
{/*ALCODESTART::1693307881182*/
f_connectToJ_EA_default(j_ea);
// Abstract method to be used call GC-subtype specific functions
/*ALCODEEND*/}

double f_chargeOnPrice_V2G(double currentElectricityPriceConsumption_eurpkWh,double availableChargingPower_kW)
{/*ALCODESTART::1695822607494*/
ArrayList<J_EAEV> copiedVehicleList = new ArrayList<J_EAEV>(c_vehiclesAvailableForCharging);
int countDeletedItems = 0;

double remainingChargePower_kW = availableChargingPower_kW;

for ( int i = 0; i < copiedVehicleList.size(); i++ ){
	J_EAEV vehicle = copiedVehicleList.get(i);
	
	if (vehicle.getVehicleScaling() == 0) {
		continue;
	}
	
	if(!vehicle.getAvailability() ){
		vehicle.f_updateAllFlows( 0 );
		c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
		countDeletedItems ++;
	} else {
		//double availableChargingPower_kW = v_allowedCapacity_kW - v_currentPowerElectricity_kW - v_chargingPower_kW;
		double chargeNeedForNextTrip_kWh = vehicle.energyNeedForNextTrip_kWh - vehicle.getCurrentStateOfCharge_kWh();
		//double timeToNexTrip_min = vehicle.getMobilityTracker().v_nextTripStartTime_min - energyModel.t_h*60;
		double maxChargingPower_kW = vehicle.getCapacityElectric_kW();
		double timeToNextTrip_min = vehicle.tripTracker.v_nextEventStartTime_min - energyModel.t_h*60;
		double chargeDeadline_min = floor((vehicle.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * 60 * energyModel.p_timeStep_h;

		double priceGain_kWhpeur = 1; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
		double urgencyGain_eurpkWh = 0.4; // How strongly WTP-price shifts based on charging flexibility
		double maxSpreadChargingPower_kW = min(chargeNeedForNextTrip_kWh / (max(1, timeToNextTrip_min - v_additionalTimeSpreadCharging_MIN) / 60), maxChargingPower_kW);
		//traceln("maxSpreadChargingPower_kW" + maxSpreadChargingPower_kW);
		double WTPoffset_eurpkWh = 0;
		if (energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW > 499) {
			WTPoffset_eurpkWh = 0.05*(1-energyModel.v_WindYieldForecast_fr);//0.15; // Adds an offset to the WTP price; this value is very much context specific, depending on market conditions during charging periods
		} else {
			WTPoffset_eurpkWh = 0.02;
		}
		double V2G_WTR_offset_eurpkWh = 0.05;
		double chargeSetpoint_kW = 0;

		if ( energyModel.t_h*60 >= chargeDeadline_min & chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
			//traceln("Urgency charging! May exceed connection capacity!");
			chargeSetpoint_kW = maxChargingPower_kW;				
		} else if ( vehicle.getCurrentStateOfCharge_fr() < 0.15 ) {
			chargeSetpoint_kW = min(remainingChargePower_kW, maxChargingPower_kW);
		} else {
			//double WTPprice_eurpkWh = v_electricityPriceLowPassed_eurpkWh - flexibilityGain_eurph * (chargeDeadline_min - energyModel.t_h*60 - 600);
			v_WTPCharging_eurpkWh = WTPoffset_eurpkWh + v_electricityPriceLowPassed_eurpkWh + urgencyGain_eurpkWh * ( max(0,maxSpreadChargingPower_kW) / maxChargingPower_kW ); // Scale WTP based on flexibility expressed in terms of power-fraction
			//WTPprice_eurpkWh = WTPoffset_eurpkWh + (main.v_epexNext24hours_eurpkWh+v_electricityPriceLowPassed_eurpkWh)/2 + flexibilityGain_eurpkWh * sqrt(maxSpreadChargingPower_kW/maxChargingPower_kW); // Scale WTP based on flexibility expressed in terms of power-fraction
			chargeSetpoint_kW = max(0, maxChargingPower_kW * (v_WTPCharging_eurpkWh / currentElectricityPriceConsumption_eurpkWh - 1) * priceGain_kWhpeur);
			chargeSetpoint_kW = min(remainingChargePower_kW, chargeSetpoint_kW);
			
			if ( chargeNeedForNextTrip_kWh < -maxChargingPower_kW*energyModel.p_timeStep_h && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price			
				v_WTRV2G_eurpkWh  = V2G_WTR_offset_eurpkWh + v_electricityPriceLowPassed_eurpkWh; // Scale WTP based on flexibility expressed in terms of power-fraction
				chargeSetpoint_kW = min(0, -maxChargingPower_kW * (currentElectricityPriceConsumption_eurpkWh / v_WTRV2G_eurpkWh - 1) * priceGain_kWhpeur);
				/*if (chargeSetpoint_kW < 0) {
					traceln(" V2G Active! Power: " + chargeSetpoint_kW );
				}*/
				
			}
			//traceln("Trying to charge cheaply, time " + energyModel.t_h*60 + " minutes, charge setpoint: " + chargeSetpoint_kW + " kW");
			/*if (this.getIndex() == 0){
				traceln("wtp = " + v_WTPCharging_eurpkWh);
				traceln("remainingChargePower_kW: " + remainingChargePower_kW + "charge setpoint kW: " + chargeSetpoint_kW);
			}*/
		}

		vehicle.f_updateAllFlows( chargeSetpoint_kW / maxChargingPower_kW );

		remainingChargePower_kW = availableChargingPower_kW - vehicle.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);;
		
	}
	if( this instanceof GCHouse){
		((GCHouse)this).v_vehicleSOC_fr = vehicle.getCurrentStateOfCharge_fr();
	}
}


/*ALCODEEND*/}

double f_initialize()
{/*ALCODESTART::1698854861644*/
if (v_liveConnectionMetaData.physicalCapacity_kW < 0) {
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has negative physical connection capacity!");
} else if (v_liveConnectionMetaData.contractedDeliveryCapacity_kW < 0) {
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has negative contracted delivery capacity!");
} else if (v_liveConnectionMetaData.contractedFeedinCapacity_kW < 0) {
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has negative contracted feed in capacity!");
}

if(v_isActive){
	if (v_liveConnectionMetaData.contractedDeliveryCapacity_kW == 0.0 && v_liveConnectionMetaData.contractedFeedinCapacity_kW == 0.0 && v_liveConnectionMetaData.physicalCapacity_kW == 0.0) { // If no contracted or physical capacity is given, throw error.
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has 0.0 physical and contracted capacity! Not a valid state of for this agent");
	} else {
		if (v_liveConnectionMetaData.contractedDeliveryCapacity_kW == 0.0 && v_liveConnectionMetaData.contractedFeedinCapacity_kW == 0.0) { // If no contracted capacity is given, use physical capacity
		v_liveConnectionMetaData.contractedDeliveryCapacity_kW = v_liveConnectionMetaData.physicalCapacity_kW;
		v_liveConnectionMetaData.contractedFeedinCapacity_kW = v_liveConnectionMetaData.physicalCapacity_kW;
		} else if ( v_liveConnectionMetaData.physicalCapacity_kW == 0 ) { // if no physical capacity is given, use max of delivery and feedin contracted capacities
			v_liveConnectionMetaData.physicalCapacity_kW = max(v_liveConnectionMetaData.contractedDeliveryCapacity_kW, v_liveConnectionMetaData.contractedFeedinCapacity_kW);
		}
	}
}

if ( c_connectedGISObjects.size()>0) { // can this go into initialisation function?
	//p_floorSurfaceArea_m2 = totalSurfaceAreaGC_m2;
	p_longitude = c_connectedGISObjects.get(0).p_longitude; // Get longitude of first building (only used to get nearest trafo)
	p_latitude = c_connectedGISObjects.get(0).p_latitude; // Get latitude of first building (only used to get nearest trafo)
	setLatLon(p_latitude, p_longitude);  
			
	//If GC has no assigned trafo_id --> Assign to nearest trafo
	if (p_parentNodeElectricID == null){
		//Set nearest agent as trafo
		GridNode nearestLVStation = getNearestAgent(energyModel.c_gridNodesNotTopLevel);
		//nearestLVStation.c_electricityGridConnections.add(companyGC); // this should be taken care of in GC.f_initialize()!
		if (nearestLVStation!=null) {
			p_parentNodeElectricID = nearestLVStation.p_gridNodeID;
		}
	}		
}

if (p_parentNodeElectricID == null) {
	p_parentNodeElectricID = findFirst(energyModel.pop_gridNodes, GN->GN.p_energyCarrier.equals(OL_EnergyCarriers.ELECTRICITY)).p_gridNodeID;
	traceln("GridConnection %s wasn't assigned a GridNodeElectric! Using first gridNode Electric in pop_gridNodes", this);
}

f_connectToParents();
if ( p_parentNodeElectric == null ) {
	traceln("GC: %s with id %s and name %s", this, p_gridConnectionID, p_name);
	traceln("GN id %s", p_parentNodeElectricID);
	throw new RuntimeException("Exception: GridConnection not connected to GridNodeElectric!");
}
else {
	// Calculate the Wind and PV Installed under the parent gridnode
	double PV_kW = 0;
	double Wind_kW = 0;
	for (J_EAProduction j_ea : c_productionAssets) {
		if (j_ea.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC) {
			PV_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		}
		else if (j_ea.getEAType() == OL_EnergyAssetType.WINDMILL) {
			Wind_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		}
	}
	p_parentNodeElectric.v_totalInstalledPVPower_kW += PV_kW;
	p_parentNodeElectric.v_totalInstalledWindPower_kW += Wind_kW;
}

f_setOperatingSwitches();

// Initializing Live Data Class
v_liveAssetsMetaData.updateActiveAssetData(new ArrayList<>(List.of(this)));
v_liveData.activeConsumptionEnergyCarriers = v_activeConsumptionEnergyCarriers;
v_liveData.activeProductionEnergyCarriers = v_activeProductionEnergyCarriers;
v_liveData.activeEnergyCarriers = v_activeEnergyCarriers;

f_initializeDataSets();

for (OL_EnergyCarriers EC : v_activeEnergyCarriers){
	energyModel.v_activeEnergyCarriers.add(EC);
}
for (OL_EnergyCarriers EC_production : v_activeProductionEnergyCarriers){
	energyModel.v_activeProductionEnergyCarriers.add(EC_production);
}
for (OL_EnergyCarriers EC_consumption : v_activeConsumptionEnergyCarriers){
	energyModel.v_activeConsumptionEnergyCarriers.add(EC_consumption);
}



/*ALCODEEND*/}

double f_addFlows(J_FlowsMap flowsMap,double energyUse_kW,J_EA caller)
{/*ALCODESTART::1702373771433*/
if (caller instanceof J_EAStorageElectric) { 
	fm_currentBalanceFlows_kW.addFlow(OL_EnergyCarriers.ELECTRICITY, flowsMap.get(OL_EnergyCarriers.ELECTRICITY));

	// Only allocate battery losses as consumption. Charging/discharging is neither production nor consumption. Do we need an element in flowsmap indicating power into storage??
	fm_currentConsumptionFlows_kW.addFlow(OL_EnergyCarriers.ELECTRICITY, max(0, energyUse_kW));
	v_currentFinalEnergyConsumption_kW += max(0, energyUse_kW);
} else {
	fm_currentBalanceFlows_kW.addFlows(flowsMap);
	for (OL_EnergyCarriers EC : flowsMap.keySet()) {
		double flow_kW = flowsMap.get(EC);		
		if (flow_kW < 0) {
			fm_currentProductionFlows_kW.addFlow(EC, -flow_kW);
		}
		else {
			fm_currentConsumptionFlows_kW.addFlow(EC, flow_kW);
		}
	}
	v_currentPrimaryEnergyProduction_kW += max(0, -energyUse_kW);
	v_currentFinalEnergyConsumption_kW += max(0, energyUse_kW);
}

if ( caller instanceof J_EAConversionHeatPump ) {
	v_currentPrimaryEnergyProductionHeatpumps_kW -= energyUse_kW;
}
/*ALCODEEND*/}

double f_removeTheJ_EA(J_EA j_ea)
{/*ALCODESTART::1714646521271*/
f_removeTheJ_EA_default(j_ea);
// Abstract method to be used call GC-subtype specific functions
/*ALCODEEND*/}

double f_removeTheJ_EA_default(J_EA j_ea)
{/*ALCODESTART::1714646913998*/
c_energyAssets.remove(j_ea);
energyModel.c_energyAssets.remove(j_ea);

if (j_ea instanceof J_EAVehicle) {
	J_EAVehicle vehicle = (J_EAVehicle)j_ea;
	if (vehicle instanceof J_EADieselVehicle) {
		c_dieselVehicles.remove( (J_EADieselVehicle)vehicle );		
	} else if (vehicle instanceof J_EAHydrogenVehicle) {
		c_hydrogenVehicles.remove((J_EAHydrogenVehicle)vehicle);		
	} else if (vehicle instanceof J_EAEV) {
		c_vehiclesAvailableForCharging.remove((J_EAEV)vehicle);
		energyModel.c_EVs.remove((J_EAEV)vehicle);
		c_EvAssets.remove(j_ea);
	}
	c_vehicleAssets.remove(j_ea);
		
	J_ActivityTrackerTrips tripTracker = vehicle.tripTracker;
	c_tripTrackers.remove( tripTracker );
	vehicle.tripTracker = null;
	v_vehicleIndex --;
} else if (j_ea instanceof J_EAConsumption) {
	c_consumptionAssets.remove((J_EAConsumption)j_ea);	
	if (j_ea.energyAssetType == OL_EnergyAssetType.HOT_WATER_CONSUMPTION) {
		p_DHWAsset = null;	
	}
	if( j_ea.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND ) {
		c_fixedConsumptionElectricAssets.remove(j_ea);
	}
	if( j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB ) {
		c_electricHobAssets.remove(j_ea);
	}
} else if (j_ea instanceof J_EAProduction) {
	c_productionAssets.remove((J_EAProduction)j_ea);
	//energyModel.c_productionAssets.remove((J_EAProduction)j_ea);
	if (j_ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC) {
		J_EAProduction otherPV = findFirst(c_productionAssets, x -> x.getEAType() == OL_EnergyAssetType.PHOTOVOLTAIC);
		if (otherPV == null) {
			v_liveAssetsMetaData.hasPV = false;
		}
		double capacity_kW = ((J_EAProduction)j_ea).getCapacityElectric_kW();
		v_liveAssetsMetaData.totalInstalledPVPower_kW -= capacity_kW;
		if ( p_parentNodeElectric != null ) {
			p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, capacity_kW, false);
		}
		c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledPVPower_kW -= capacity_kW);		
		energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW -= capacity_kW;
		c_pvAssets.remove(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.WINDMILL) {
		double capacity_kW = ((J_EAProduction)j_ea).getCapacityElectric_kW();
		v_liveAssetsMetaData.totalInstalledWindPower_kW -= capacity_kW;
		if ( p_parentNodeElectric != null ) {
			p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, capacity_kW, false);
		}
		c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledPVPower_kW -= capacity_kW);		
		energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW -= capacity_kW;
		c_windAssets.remove(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL){
		if (c_ptAssets.size() <= 1) {
			v_liveAssetsMetaData.hasPT = false;
		}
		c_ptAssets.remove(j_ea);
	}
} else if (j_ea instanceof J_EAConversion) {
	c_conversionAssets.remove((J_EAConversion)j_ea);
	if (j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB) {
		c_electricHobAssets.remove(j_ea);
	}
	if ( j_ea.energyAssetType == OL_EnergyAssetType.GAS_PIT || j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB){
		p_cookingTracker = null;
	} else if (j_ea instanceof J_EAConversionElectrolyser) {
		c_electrolyserAssets.remove(j_ea);
	}
	else{
		if (j_ea instanceof J_EAConversionGasBurner) {
	
		} else if (j_ea instanceof J_EAConversionHeatPump) {
			energyModel.c_ambientDependentAssets.remove(j_ea);
			c_electricHeatpumpAssets.remove(j_ea);
		} else if (j_ea instanceof J_EAConversionHydrogenBurner) {

		} else if (j_ea instanceof J_EAConversionGasCHP) {
			c_chpAssets.remove(j_ea);
		}else if( j_ea instanceof J_EAConversionHeatDeliverySet ){
		
		}
		if(p_primaryHeatingAsset == j_ea){
			if(p_secondaryHeatingAsset != null){
				p_primaryHeatingAsset = p_secondaryHeatingAsset;
				p_secondaryHeatingAsset = null;
			}
		}
		if(p_secondaryHeatingAsset == j_ea){
			p_secondaryHeatingAsset = null;
		}
	}
} else if  (j_ea instanceof J_EAStorage) {
	c_storageAssets.remove((J_EAStorage)j_ea);
	energyModel.c_storageAssets.remove((J_EAStorage)j_ea);
	if (j_ea instanceof J_EAStorageHeat) {
		energyModel.c_ambientDependentAssets.remove(j_ea);
		if (j_ea.energyAssetType == OL_EnergyAssetType.BUILDINGTHERMALS) {	
			p_BuildingThermalAsset = null;
		}
		else {
			p_heatBuffer = null;
		}
	} else if (j_ea instanceof J_EAStorageGas) {
		p_gasBuffer = null;
	} else if (j_ea instanceof J_EAStorageElectric) {
		p_batteryAsset = null;
		c_batteryAssets.remove(j_ea);
		v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh -= ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()/1000;
		energyModel.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh -= ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()/1000;
	}
} else if  (j_ea instanceof J_EAProfile) {
	//p_energyProfile = null;
	c_profileAssets.remove((J_EAProfile)j_ea);
} else if (j_ea instanceof J_EACharger) {
	c_chargers.remove(j_ea);
	c_EvAssets.remove(j_ea);	
} else {
	traceln("Unrecognized energy asset %s in gridconnection %s", j_ea, this);
}

/*ALCODEEND*/}

double f_resetSpecificGCStates()
{/*ALCODESTART::1717060111619*/

/*ALCODEEND*/}

double f_resetStatesAfterRapidRun()
{/*ALCODESTART::1717068094093*/
//Reset specificGC states after rapid run
f_resetSpecificGCStatesAfterRapidRun();





/*ALCODEEND*/}

double f_resetSpecificGCStatesAfterRapidRun()
{/*ALCODESTART::1717068167776*/
// to be overwritten by child GCs!
/*ALCODEEND*/}

double f_curtailment()
{/*ALCODESTART::1720442672576*/
//Electricity
if (v_enableCurtailment) {
	switch(p_curtailmentMode) {
		case CAPACITY:
		// Keep feedin power within connection capacity
		if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < - v_liveConnectionMetaData.contractedFeedinCapacity_kW) { // overproduction!
			for (J_EAProduction j_ea : c_productionAssets) {
				j_ea.curtailElectricityProduction( - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_liveConnectionMetaData.contractedFeedinCapacity_kW);
				if (!(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < - v_liveConnectionMetaData.contractedFeedinCapacity_kW)) {
					break;
				}
			}
		}
		break;
		case MARKETPRICE:
		if(energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() < 0.0) {
			if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < 0.0) { // Feedin, bring to zero!
				for (J_EAProduction j_ea : c_productionAssets) {
					j_ea.curtailElectricityProduction( - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
					if (!(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < 0.0)) {
						break;
					}
				}
			}
		}
		break;
		case NODALPRICING:
		// Prevent feedin when nodal price is negative
		double priceTreshold_eur = -0.0;
		if( p_parentNodeElectric.v_currentTotalNodalPrice_eurpkWh < priceTreshold_eur) {
		
			double v_currentPowerElectricitySetpoint_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * max(0,1+(p_parentNodeElectric.v_currentTotalNodalPrice_eurpkWh-priceTreshold_eur)*5);
			for (J_EAProduction j_ea : c_productionAssets) {
				j_ea.curtailElectricityProduction(v_currentPowerElectricitySetpoint_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
				if (!(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < v_currentPowerElectricitySetpoint_kW)) {
					break;
				}
			}
		}
		break;
		default:
	}
}


if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT) < 0) {//Heat (for now always curtail over produced heat!)
	for (J_EAProduction j_ea : c_productionAssets) {
		j_ea.curtailEnergyCarrierProduction( OL_EnergyCarriers.HEAT, - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT));
		if (!(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT) < 0)) {
			break;
		}
	}
}
/*ALCODEEND*/}

double f_nfatoUpdateConnectionCapacity()
{/*ALCODESTART::1720430481154*/
int dayOfWeek = (int) ((energyModel.t_h / 24 + energyModel.v_dayOfWeek1jan) % 7);

double timeOfDay = energyModel.t_h % 24;
int hourOfDay = (int) timeOfDay;

if (timeOfDay == hourOfDay) {
	int previousHour = ((hourOfDay - 1) % 24 + 24) % 24;
	if (dayOfWeek == 0 || dayOfWeek == 6) {
		if (dayOfWeek == 6 && hourOfDay == 0) { // Friday night we need to subtract the previous week capacity
			v_liveConnectionMetaData.contractedDeliveryCapacity_kW += v_nfatoWeekendDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekDeliveryCapacity_kW[previousHour];
			v_liveConnectionMetaData.contractedFeedinCapacity_kW += v_nfatoWeekendFeedinCapacity_kW[hourOfDay] - v_nfatoWeekFeedinCapacity_kW[previousHour];
		}
		else {
			v_liveConnectionMetaData.contractedDeliveryCapacity_kW += v_nfatoWeekendDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekendDeliveryCapacity_kW[previousHour];
			v_liveConnectionMetaData.contractedFeedinCapacity_kW += v_nfatoWeekendFeedinCapacity_kW[hourOfDay] - v_nfatoWeekendFeedinCapacity_kW[previousHour];
		}
	}
	else {
		if (dayOfWeek == 1 && hourOfDay == 0) { // Sunday night we need to subtract the previous weekend capacity
			v_liveConnectionMetaData.contractedDeliveryCapacity_kW += v_nfatoWeekDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekendDeliveryCapacity_kW[previousHour];
			v_liveConnectionMetaData.contractedFeedinCapacity_kW += v_nfatoWeekFeedinCapacity_kW[hourOfDay] - v_nfatoWeekendFeedinCapacity_kW[previousHour];
		}
		else {
			v_liveConnectionMetaData.contractedDeliveryCapacity_kW += v_nfatoWeekDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekDeliveryCapacity_kW[previousHour];
			v_liveConnectionMetaData.contractedFeedinCapacity_kW += v_nfatoWeekFeedinCapacity_kW[hourOfDay] - v_nfatoWeekFeedinCapacity_kW[previousHour];
		}
	}
}
/*ALCODEEND*/}

double f_nfatoSetConnectionCapacity(boolean reset)
{/*ALCODESTART::1720431721926*/
int mult = reset == true ? -1 : 1; // When reset is true we need to subtract the capacity, else we add

int dayOfWeek = (int) ((energyModel.t_h / 24 + energyModel.v_dayOfWeek1jan) % 7);
double timeOfDay = energyModel.t_h % 24;
int hourOfDay = (int) timeOfDay;

if (dayOfWeek == 0 || dayOfWeek == 6) {
	if (dayOfWeek == 6 && hourOfDay == 0) { // Friday night we need to subtract the previous week capacity
		v_liveConnectionMetaData.contractedDeliveryCapacity_kW += mult * v_nfatoWeekDeliveryCapacity_kW[hourOfDay];
		v_liveConnectionMetaData.contractedFeedinCapacity_kW += mult * v_nfatoWeekFeedinCapacity_kW[hourOfDay];
	}
	else {
		v_liveConnectionMetaData.contractedDeliveryCapacity_kW += mult * v_nfatoWeekendDeliveryCapacity_kW[hourOfDay];
		v_liveConnectionMetaData.contractedFeedinCapacity_kW += mult * v_nfatoWeekendFeedinCapacity_kW[hourOfDay];
	}
}
else {
	if (dayOfWeek == 1 && hourOfDay == 0) { // Sunday night we need to subtract the previous week capacity
		v_liveConnectionMetaData.contractedDeliveryCapacity_kW += mult * v_nfatoWeekendDeliveryCapacity_kW[hourOfDay];
		v_liveConnectionMetaData.contractedFeedinCapacity_kW += mult * v_nfatoWeekendFeedinCapacity_kW[hourOfDay];
	}
	else {
		v_liveConnectionMetaData.contractedDeliveryCapacity_kW += mult * v_nfatoWeekDeliveryCapacity_kW[hourOfDay];
		v_liveConnectionMetaData.contractedFeedinCapacity_kW += mult * v_nfatoWeekFeedinCapacity_kW[hourOfDay];
	}
}
/*ALCODEEND*/}

double f_removeFlows(J_FlowsMap flowsMap,double energyUse_kW,J_EA caller)
{/*ALCODESTART::1722512642645*/
for (OL_EnergyCarriers EC : flowsMap.keySet()) {
	fm_currentBalanceFlows_kW.addFlow(EC, -flowsMap.get(EC));
	
	if (flowsMap.get(EC) < 0) {
		fm_currentProductionFlows_kW.addFlow(EC, flowsMap.get(EC));
	}
	else if (flowsMap.get(EC) > 0){
		fm_currentConsumptionFlows_kW.addFlow(EC, -flowsMap.get(EC));
	}
}

if (caller instanceof J_EAStorageElectric) { 
	// Only allocate battery losses as consumption. Charging/discharging is neither production nor consumption. Do we need an element in flowsmap indicating power into storage??
	fm_currentConsumptionFlows_kW.addFlow(OL_EnergyCarriers.ELECTRICITY, max(0, energyUse_kW));
	v_currentFinalEnergyConsumption_kW += max(0, energyUse_kW);
} else {
	double curtailment_kW = max(0, -energyUse_kW);
	double lostLoad_kW = max(0, energyUse_kW);
	v_currentEnergyCurtailed_kW += curtailment_kW;
	v_currentPrimaryEnergyProduction_kW -= curtailment_kW;
	v_currentFinalEnergyConsumption_kW -= lostLoad_kW;
}

if ( caller instanceof J_EAConversionHeatPump ) {
	v_currentPrimaryEnergyProductionHeatpumps_kW += energyUse_kW;
}
/*ALCODEEND*/}

double f_fillLiveDataSets()
{/*ALCODESTART::1722518225504*/
//Current timestep
double currentTime_h = energyModel.t_h-energyModel.p_runStartTime_h;

v_liveData.addTimeStep(currentTime_h,
	fm_currentBalanceFlows_kW,
	fm_currentConsumptionFlows_kW,
	fm_currentProductionFlows_kW,
	v_currentPrimaryEnergyProduction_kW, 
	v_currentFinalEnergyConsumption_kW, 
	v_currentPrimaryEnergyProductionHeatpumps_kW, 
	v_currentEnergyCurtailed_kW, 
	v_assetFlows 
);
/*ALCODEEND*/}

double f_rapidRunDataLogging()
{/*ALCODESTART::1722518905501*/
v_rapidRunData.addTimeStep(fm_currentBalanceFlows_kW,
	fm_currentConsumptionFlows_kW,
	fm_currentProductionFlows_kW,
	v_currentPrimaryEnergyProduction_kW, 
	v_currentFinalEnergyConsumption_kW, 
	v_currentPrimaryEnergyProductionHeatpumps_kW, 
	v_currentEnergyCurtailed_kW, 
	v_assetFlows, 
	energyModel);
/*ALCODEEND*/}

double f_setActive(boolean setActive)
{/*ALCODESTART::1722584668566*/
if((energyModel.c_pausedGridConnections.contains(this) && !setActive) || 
  (!energyModel.c_pausedGridConnections.contains(this) && setActive)){
	return;
}

if (!setActive) {
	energyModel.c_gridConnections.remove(this);
	energyModel.c_pausedGridConnections.add(this);
	
	// Set GIS Region visibility
	for (GIS_Object obj : c_connectedGISObjects) {
		obj.gisRegion.setVisible(false);
	}
	

	// update GN parents' wind / solar totals
	p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, v_liveAssetsMetaData.totalInstalledPVPower_kW, false);
	p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, v_liveAssetsMetaData.totalInstalledWindPower_kW, false);
	energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW -= v_liveAssetsMetaData.totalInstalledPVPower_kW;
	energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW -= v_liveAssetsMetaData.totalInstalledWindPower_kW;
	energyModel.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh -= v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	
	for(EnergyCoop coop : c_parentCoops){
		coop.v_liveAssetsMetaData.totalInstalledPVPower_kW -= v_liveAssetsMetaData.totalInstalledPVPower_kW;
		coop.v_liveAssetsMetaData.totalInstalledWindPower_kW -= v_liveAssetsMetaData.totalInstalledWindPower_kW;
		coop.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh -= v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	}
	
	// Reset Connection Capacity to default
	f_nfatoSetConnectionCapacity(true);
	
	// Is setting all of these to zero overkill?
	fm_currentProductionFlows_kW.clear();
	fm_currentConsumptionFlows_kW.clear();
	fm_currentBalanceFlows_kW.clear();
	
	v_previousPowerElectricity_kW = 0;
	v_previousPowerHeat_kW = 0;
	//v_currentPowerElectricity_kW = 0;
	//v_currentPowerMethane_kW = 0;
	//v_currentPowerHydrogen_kW = 0;
	//v_currentPowerHeat_kW = 0;
	//v_currentPowerDiesel_kW = 0;
	//v_currentElectricityConsumption_kW = 0;
	//v_currentElectricityProduction_kW = 0;
	//v_currentEnergyConsumption_kW = 0;
	//v_currentEnergyProduction_kW = 0;
	v_currentEnergyCurtailed_kW = 0;
	v_currentPrimaryEnergyProductionHeatpumps_kW = 0;
	v_fixedConsumptionElectric_kW = 0;
	v_electricHobConsumption_kW = 0;
	v_heatPumpElectricityConsumption_kW = 0;
	v_hydrogenElectricityConsumption_kW = 0;
	v_evChargingPowerElectric_kW = 0;
	v_districtHeatDelivery_kW = 0;
	v_batteryPowerElectric_kW = 0;
	v_windProductionElectric_kW = 0;
	v_pvProductionElectric_kW = 0;
	v_ptProductionHeat_kW = 0;
	v_conversionPowerElectric_kW = 0;
	v_CHPProductionElectric_kW = 0;
	
}
else {
	energyModel.c_gridConnections.add(this);
	energyModel.c_pausedGridConnections.remove(this);

	// Set GIS Region visibility
	for (GIS_Object obj : c_connectedGISObjects) {
		obj.gisRegion.setVisible(true);
	}
	
	// Set Connection Capacity according to NFATO
	f_nfatoSetConnectionCapacity(false);
	
	v_liveAssetsMetaData.updateActiveAssetData(new ArrayList<>(List.of(this)));
	
	// update GN parents' wind / solar totals (will be wrong if you changed your totals while paused)
	p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, v_liveAssetsMetaData.totalInstalledPVPower_kW, true);
	p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, v_liveAssetsMetaData.totalInstalledWindPower_kW, true);
	energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW += v_liveAssetsMetaData.totalInstalledPVPower_kW;
	energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW += v_liveAssetsMetaData.totalInstalledWindPower_kW;
	energyModel.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	
	for(EnergyCoop coop : c_parentCoops){
		coop.v_liveAssetsMetaData.totalInstalledPVPower_kW += v_liveAssetsMetaData.totalInstalledPVPower_kW;
		coop.v_liveAssetsMetaData.totalInstalledWindPower_kW += v_liveAssetsMetaData.totalInstalledWindPower_kW;
		coop.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	}
	
	
	//Initialize/reset dataset maps to 0
	double startTime = energyModel.v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = energyModel.v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	v_liveData.resetLiveDatasets(startTime, endTime, energyModel.p_timeStep_h);

}

//Update the 'isActive' variable
v_isActive = setActive;
/*ALCODEEND*/}

double f_getChargeDeadline(J_EAEV ev)
{/*ALCODESTART::1725455130676*/
double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getCurrentStateOfCharge_kWh());
double maxChargingPower_kW = ev.getCapacityElectric_kW();

return floor((ev.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * energyModel.p_timeStep_h;

/*ALCODEEND*/}

double f_initializeDataSets()
{/*ALCODESTART::1730728785333*/
v_liveData.dsm_liveDemand_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));
v_liveData.dsm_liveSupply_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));

/*
dsm_dailyAverageDemandDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, 365);
dsm_dailyAverageSupplyDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, 365);

dsm_summerWeekDemandDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));
dsm_summerWeekSupplyDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));
dsm_winterWeekDemandDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));
dsm_winterWeekSupplyDataSets_kW.createEmptyDataSets(v_activeEnergyCarriers, (int)(168 / energyModel.p_timeStep_h));
*/
/*ALCODEEND*/}

double f_manageChargers()
{/*ALCODESTART::1750258434630*/
if ( c_chargers.size() > 0 ) { // && v_isActiveCharger ) {
	switch (p_chargingAttitudeVehicles) {			
		case V1G:
		case MAX_POWER:
		case MAX_SPREAD:
			c_chargers.forEach( x -> x.f_updateAllFlows(energyModel.t_h, true, false) );
			break;
		case V2G:
			c_chargers.forEach( x -> x.f_updateAllFlows(energyModel.t_h, true, true) );
			break;
		case SIMPLE:
		default:
			c_chargers.forEach( x -> x.f_updateAllFlows(energyModel.t_h, false, false) );
			break;
	}
}
/*ALCODEEND*/}

double f_manageBattery()
{/*ALCODESTART::1752570332887*/
if (p_batteryAsset != null) {
	if (p_batteryAsset.getStorageCapacity_kWh() > 0 && p_batteryAsset.getCapacityElectric_kW() > 0) {
		if (p_batteryAlgorithm == null) {
			throw new RuntimeException("Tried to operate battery without algorithm in GC: " + p_gridConnectionID);
		}
		p_batteryAlgorithm.manageBattery();
	}
}
/*ALCODEEND*/}

