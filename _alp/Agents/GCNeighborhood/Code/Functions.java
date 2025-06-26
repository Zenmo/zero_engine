double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1719849166911*/
//Manage the heating assets

f_manageHeatingAssets();

f_manageCharging();

v_lowPassFactorLoad_fr = 0.003; // Vastgezet voor de NBHs 
v_currentLoadLowPassed_kW += v_lowPassFactorLoad_fr * ( fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_currentLoadLowPassed_kW ); //you want to do determine the lowpassLoad BEFORE the using the battery. As this behavior of the battery should nog be dependent on the load of the battery in the previous timesteps

//Battery
if( p_batteryAsset != null && p_batteryAsset.getStorageCapacity_kWh() != 0 && p_batteryOperationMode != OL_BatteryOperationMode.OFF){
	switch (p_batteryOperationMode){
		case BALANCE:
			if(p_ignoreGridCapacityBattery){
				f_batteryManagementBalanceNoGCCapacity_NBH(v_batterySOC_fr);
			}
			else{
				f_batteryManagementBalance_NBH(v_batterySOC_fr);
			}
		break;
		case PRICE:
			f_batteryManagementPrice_NBH(v_batterySOC_fr);
		break;
		default:
		break;
	}
	p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
	v_batteryPowerElectric_kW =  p_batteryAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
	v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
}

/*ALCODEEND*/}

double f_setHeatingMethodPct_services(double[] pctArray)
{/*ALCODESTART::1722514612938*/
v_amountOfGasBurners_services_fr = pctArray[0]/100;
v_amountOfElectricHeatpumps_services_fr = pctArray[1]/100;
v_amountOfHybridHeatpump_services_fr = pctArray[2]/100;
v_amountOfDistrictHeating_services_fr = pctArray[3]/100;
v_amountOfLowTempHeatgrid_services_fr = pctArray[4]/100;

/*ALCODEEND*/}

double f_setHeatingMethodPct_houses(double[] pctArray)
{/*ALCODESTART::1722514620314*/
v_amountOfGasBurners_houses_fr = pctArray[0]/100;
v_amountOfElectricHeatpumps_houses_fr = pctArray[1]/100;
v_amountOfHybridHeatpump_houses_fr = pctArray[2]/100;
v_amountOfDistrictHeating_houses_fr = pctArray[3]/100;
v_amountOfLowTempHeatgrid_houses_fr = pctArray[4]/100;

/*ALCODEEND*/}

double f_manageHeatingAssets_overwrite()
{/*ALCODESTART::1722585552607*/
if(p_primaryHeatingAsset == null){ // null check, as certain neighborhoods don't have thermal assets (fix dat niet elke tijdstep wordt aangeroepen)!
	return;
}

//Division of the power demand //{Gasburner power request, HP power request, DH power request, Hydrogenburner power request}
double powerDemandDivision_kW[] = f_dividePowerDemandHeatingAssets(); 

//Split the power fractions (powerDemandDivision[] = {Gasburner power request, HP power request, DH power request}
if(p_primaryHeatingAsset.getOutputCapacity_kW() != 0){
	double powerFraction_GASBURNER = powerDemandDivision_kW[0] / p_primaryHeatingAsset.getOutputCapacity_kW();
	p_primaryHeatingAsset.v_powerFraction_fr = powerFraction_GASBURNER;
	
	//Gas burner control (always assigned to primary heating asset)	
	p_primaryHeatingAsset.f_updateAllFlows(powerFraction_GASBURNER);
}
if(p_secondaryHeatingAsset.getOutputCapacity_kW() != 0){
	double powerFraction_HEATPUMP  = powerDemandDivision_kW[1] / p_secondaryHeatingAsset.getOutputCapacity_kW();
	p_secondaryHeatingAsset.v_powerFraction_fr = powerFraction_HEATPUMP;
	
	//Heatpump control (always assigned to secondary heating asset)
	p_secondaryHeatingAsset.f_updateAllFlows(powerFraction_HEATPUMP);
}
if(p_tertiaryHeatingAsset.getOutputCapacity_kW() != 0){
	double powerFraction_HEATDELIVERYSET = powerDemandDivision_kW[2] / p_tertiaryHeatingAsset.getOutputCapacity_kW();
	p_tertiaryHeatingAsset.v_powerFraction_fr = powerFraction_HEATDELIVERYSET;
	
	//Heat delivery set control (always assigned to tertiary heating asset)
	p_tertiaryHeatingAsset.f_updateAllFlows(powerFraction_HEATDELIVERYSET);
	
	//Update districtheating variable
	v_districtHeatDelivery_kW = powerDemandDivision_kW[2]/p_tertiaryHeatingAsset.getEta_r();
}
if(p_quaternaryHeatingAsset.getOutputCapacity_kW() != 0){
	double powerFraction_HYDROGENBURNER = powerDemandDivision_kW[3] / p_quaternaryHeatingAsset.getOutputCapacity_kW();
	p_quaternaryHeatingAsset.v_powerFraction_fr = powerFraction_HYDROGENBURNER;	

	//Hydrogen burner(always assigned to quaternary heating asset)
	p_quaternaryHeatingAsset.f_updateAllFlows(powerFraction_HYDROGENBURNER);
}
if(p_quinaryHeatingAsset.getOutputCapacity_kW() != 0){
	double powerFraction_LOWTEMPHEATGRID = powerDemandDivision_kW[4] / p_quinaryHeatingAsset.getOutputCapacity_kW();
	p_quinaryHeatingAsset.v_powerFraction_fr = powerFraction_LOWTEMPHEATGRID;	

	//Hydrogen burner(always assigned to quaternary heating asset)
	p_quinaryHeatingAsset.f_updateAllFlows(powerFraction_LOWTEMPHEATGRID);
}
/*ALCODEEND*/}

double[] f_dividePowerDemandHeatingAssets()
{/*ALCODESTART::1722587530130*/
//Initialize power demand division array
double powerDemandDivision_kW[] = {0, 0, 0, 0, 0}; // {Gasburner power request, HP power request, DH power request, Hydrogenburner power request, lowTempHeatgridPowerDemand}

//Calculate fraction of total heat demand delivered by the CHP
/*
double powerDemand_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
double fractionOfTotalHeatDemandDeliveredyByCHP = max(0,p_chpAsset.getLastFlows().get(OL_EnergyCarriers.HEAT))/powerDemand_kW;
double remainingFraction = fractionOfTotalHeatDemandDeliveredyByCHP;
*/
//Demanded total heating power at the current time step
//double powerDemand_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

//Demanded heating power for companies and household seperatly the current time step
double powerDemand_households_kW = max(0,c_heatDemandEA.get("HOUSEHOLDS").getLastFlows().get(OL_EnergyCarriers.HEAT));
double powerDemand_agriculture_kW = max(0,c_heatDemandEA.get("AGRICULTURE").getLastFlows().get(OL_EnergyCarriers.HEAT));
double powerDemand_industry_kW = max(0,c_heatDemandEA.get("INDUSTRY").getLastFlows().get(OL_EnergyCarriers.HEAT));
double powerDemand_services_kW = max(0,c_heatDemandEA.get("SERVICES").getLastFlows().get(OL_EnergyCarriers.HEAT));

//Divide the powerdemand per heating type
double gasBurnerPowerDemand_kW 		= powerDemand_households_kW*v_amountOfGasBurners_houses_fr + 
									  powerDemand_agriculture_kW*v_amountOfGasBurners_agriculture_fr +
									  powerDemand_industry_kW*v_amountOfGasBurners_industry_fr + 
									  powerDemand_services_kW*v_amountOfGasBurners_services_fr;
									  
double electricHPPowerDemand_kW 		= powerDemand_households_kW*v_amountOfElectricHeatpumps_houses_fr + 
									  powerDemand_agriculture_kW*v_amountOfElectricHeatpumps_agriculture_fr + 
									  powerDemand_industry_kW*v_amountOfElectricHeatpumps_industry_fr + 
									  powerDemand_services_kW*v_amountOfElectricHeatpumps_services_fr;
									  
double hybridHPPowerDemand_kW 	   		= powerDemand_households_kW*v_amountOfHybridHeatpump_houses_fr +
									  powerDemand_agriculture_kW*v_amountOfHybridHeatpump_agriculture_fr +
									  powerDemand_industry_kW*v_amountOfHybridHeatpump_industry_fr + 
									  powerDemand_services_kW*v_amountOfHybridHeatpump_services_fr;
									  
double districtHeatingPowerDemand_kW   = powerDemand_households_kW*v_amountOfDistrictHeating_houses_fr +
									  powerDemand_agriculture_kW*v_amountOfDistrictHeating_agriculture_fr + 
									  powerDemand_industry_kW*v_amountOfDistrictHeating_industry_fr + 
									  powerDemand_services_kW*v_amountOfDistrictHeating_services_fr;
														  
double hydrogenBurnerPowerDemand_kW	= powerDemand_industry_kW*v_amountOfHydrogenUseForHeating_industry_fr;

double lowTempHeatgridPowerDemand_kW 	= powerDemand_households_kW*v_amountOfLowTempHeatgrid_houses_fr + 
									  powerDemand_services_kW*v_amountOfLowTempHeatgrid_services_fr;
//double lowTempHeatgridPowerDemand_kW = (powerDemand_households_kW + powerDemand_agriculture_kW + powerDemand_industry_kW + powerDemand_services_kW) - hybridHPPowerDemand - electricHPPowerDemand - gasBurnerPowerDemand - districtHeatingPowerDemand - hydrogenBurnerPowerDemand; // To make sure all power demand is met
									  
//Get the current Heatpump COP
double HP_COP = ((J_EAConversionHeatPump)p_secondaryHeatingAsset).getCOP();

if ( HP_COP < p_thresholdCOP_hybridHeatpump ) { // switch to gasburner when HP COP is below treshold
	powerDemandDivision_kW[0] = max(0, gasBurnerPowerDemand_kW + hybridHPPowerDemand_kW);
	powerDemandDivision_kW[1] = max(0, electricHPPowerDemand_kW);
}
else{
	powerDemandDivision_kW[0] = max(0, gasBurnerPowerDemand_kW);
	powerDemandDivision_kW[1] = max(0, electricHPPowerDemand_kW + hybridHPPowerDemand_kW);
}
powerDemandDivision_kW[2] = max(0, districtHeatingPowerDemand_kW);
powerDemandDivision_kW[3] = max(0, hydrogenBurnerPowerDemand_kW);
powerDemandDivision_kW[4] = max(0, lowTempHeatgridPowerDemand_kW);

return powerDemandDivision_kW; //{Gasburner power request, HP power request, DH power request, Hydrogenburner power request, lowTempHeatgridPowerDemand};
/*ALCODEEND*/}

double f_connectToJ_EA_default_overwrite_OUD(J_EA j_ea)
{/*ALCODESTART::1722595238227*/
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
	}
	c_vehicleAssets.add(vehicle);		
	J_ActivityTrackerTrips tripTracker = vehicle.getTripTracker();
	if (tripTracker == null) { // Only provide tripTracker when vehicle doesn't have it yet!
		if (vehicle.energyAssetType == OL_EnergyAssetType.ELECTRIC_TRUCK || vehicle.energyAssetType == OL_EnergyAssetType.DIESEL_TRUCK || vehicle.energyAssetType == OL_EnergyAssetType.HYDROGEN_TRUCK) {
			int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsExcel, rowIndex, energyModel.t_h*60, vehicle);
		} else if (vehicle.energyAssetType == OL_EnergyAssetType.DIESEL_VAN || vehicle.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN || vehicle.energyAssetType == OL_EnergyAssetType.HYDROGEN_VAN) {// No mobility pattern for business vans available yet!! Falling back to truck mobility pattern
			int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsExcel, rowIndex, energyModel.t_h*60, vehicle);
		} else {
			//traceln("Adding passenger vehicle to gridconnection %s", this);
			int rowIndex = uniform_discr(0, 200);//getIndex() % 200;
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_householdTripsExcel, rowIndex, energyModel.t_h*60, vehicle);
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
} else if (j_ea instanceof J_EAProduction) {
	c_productionAssets.add((J_EAProduction)j_ea);
	//energyModel.c_productionAssets.add((J_EAProduction)j_ea);
	
	if (j_ea.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC) {
		v_hasPV = true;
		v_totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if (l_parentNodeElectric.getConnectedAgent() != null) {
			l_parentNodeElectric.getConnectedAgent().v_totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		}
		energyModel.v_totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.WINDMILL) {
		v_totalInstalledWindPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if (l_parentNodeElectric.getConnectedAgent() != null) {
			l_parentNodeElectric.getConnectedAgent().v_totalInstalledWindPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		}
		energyModel.v_totalInstalledWindPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
	}
} else if (j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)j_ea);
	if ( j_ea.energyAssetType == OL_EnergyAssetType.GAS_PIT | j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB){
		//traceln("Adding HOB to house" + this);
		if (p_cookingTracker == null) {
			//traceln("Adding cookingTracker to house" + this);
			int rowIndex = uniform_discr(2, 300); 
			p_cookingTracker = new J_ActivityTrackerCooking(energyModel.p_cookingPatternExcel, rowIndex, energyModel.t_h*60, (J_EAConversion)j_ea );			
		} else {
			p_cookingTracker.HOB = (J_EAConversion)j_ea;
		}
	} else if (j_ea instanceof J_EAConversionGasBurner) {
		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionHeatPump) {
		energyModel.c_ambientAirDependentAssets.add(j_ea);
		p_secondaryHeatingAsset = (J_EAConversion)j_ea;
	//} else if (j_ea instanceof J_EAConversionHydrogenBurner) {
	//		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionHydrogenBurner) {
		p_tertiaryHeatingAsset = (J_EAConversion)j_ea;
	//} else if (j_ea instanceof J_EAConversionCurtailer) {
	//		p_curtailer = (J_EAConversionCurtailer)j_ea;
	}
} else if  (j_ea instanceof J_EAStorage) {
	c_storageAssets.add((J_EAStorage)j_ea);
	energyModel.c_storageAssets.add((J_EAStorage)j_ea);
	if (j_ea.energyAssetType == OL_EnergyAssetType.BUILDINGTHERMALS) {
		//traceln("Adding buildingThermals to gridconnection");	
		p_BuildingThermalAsset = (J_EABuilding)j_ea;
			/*if ( p_energyLabel != null & p_gridConnectionType != null){ // Get building thermals from lookup table when isolation label and house type are available
				double lossFactor_WpK2 = energyModel.v_buildingThermalPars.path( p_gridConnectionType.name() ).path(p_energyLabel.name()).path("lossFactor_WpK").doubleValue();
				double heatCapacity_JpK2 = energyModel.v_buildingThermalPars.path( p_gridConnectionType.name() ).path(p_energyLabel.name()).path("heatCapacity_JpK").doubleValue();
				p_BuildingThermalAsset.lossFactor_WpK = lossFactor_WpK2;
				p_BuildingThermalAsset.heatCapacity_JpK = heatCapacity_JpK2;
				traceln("House thermal model updated!");
				traceln("House type: %s, energy label: %s", p_gridConnectionType, p_energyLabel);
				traceln("lossfactor %s, heatcapacity %s", lossFactor_WpK2, heatCapacity_JpK2);
			}*/ // Deprecated get lossfactor and heatcapacity from json-input. Replace with other datasource!
		p_BuildingThermalAsset.updateAmbientTemperature( energyModel.v_currentAmbientTemperature_degC );
		//v_tempSetpoint_degC = p_BuildingThermalAsset.setTemperature_degC;		
		energyModel.c_ambientAirDependentAssets.add(p_BuildingThermalAsset);
	} else if (j_ea instanceof J_EAStorageGas) {
		p_gasBuffer = (J_EAStorageGas)j_ea;
	} else if (j_ea instanceof J_EAStorageElectric) {
		p_batteryAsset = (J_EAStorageElectric)j_ea;
	} else if (j_ea instanceof J_EAStorageHeat) {
		energyModel.c_ambientAirDependentAssets.add(j_ea);
	}
} else if  (j_ea instanceof J_EAProfile) {
	//p_energyProfile = (J_EAProfile)j_ea;
	c_profileAssets.add((J_EAProfile)j_ea);
} else {
	traceln("Unrecognized energy asset %s in gridconnection %s", j_ea, this);
}

/*ALCODEEND*/}

double f_connectToJ_EA_default_overwrite(J_EA j_ea)
{/*ALCODESTART::1730370456790*/
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
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsCsv, rowIndex, energyModel.t_h*60, vehicle);
		} else if (vehicle.energyAssetType == OL_EnergyAssetType.DIESEL_VAN || vehicle.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN || vehicle.energyAssetType == OL_EnergyAssetType.HYDROGEN_VAN) {// No mobility pattern for business vans available yet!! Falling back to truck mobility pattern
			int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsCsv, rowIndex, energyModel.t_h*60, vehicle);
			tripTracker.setAnnualDistance_km(30_000);
		} else {
			//traceln("Adding passenger vehicle to gridconnection %s", this);
			int rowIndex = uniform_discr(0, 200);//getIndex() % 200;
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_householdTripsCsv, rowIndex, energyModel.t_h*60, vehicle);
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
		v_liveAssetsMetaData.totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if ( p_parentNodeElectric != null ) {
			p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, ((J_EAProduction)j_ea).getCapacityElectric_kW(), true);
		}
		energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		c_pvAssets.add(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.WINDMILL) {
		v_liveAssetsMetaData.totalInstalledWindPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if ( p_parentNodeElectric != null ) {
			p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, ((J_EAProduction)j_ea).getCapacityElectric_kW(), true);
		}
		energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		c_windAssets.add(j_ea);
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
			p_cookingTracker = new J_ActivityTrackerCooking(energyModel.p_cookingPatternCsv, rowIndex, energyModel.t_h*60, (J_EAConversion)j_ea );			
		} else {
			p_cookingTracker.HOB = (J_EAConversion)j_ea;
		}
	} else if (j_ea instanceof J_EAConversionGasBurner) {
		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionHeatPump) {
		if(p_secondaryHeatingAsset == null){
			energyModel.c_ambientAirDependentAssets.add(j_ea);
			c_electricHeatpumpAssets.add(j_ea);
			//c_conversionElectricAssets.add(j_ea);
			p_secondaryHeatingAsset = (J_EAConversion)j_ea;
		}
		else{//Lowtemp heat grid heatpump
			//energyModel.c_ambientAirDependentAssets.add(j_ea);
			c_electricHeatpumpAssets.add(j_ea);
			//c_conversionElectricAssets.add(j_ea);
			p_quinaryHeatingAsset = (J_EAConversion)j_ea;
		}
	} else if (j_ea instanceof J_EAConversionHeatDeliverySet) {
		p_tertiaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionHydrogenBurner) {
		p_quaternaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionGasCHP) {
		c_chpAssets.add(j_ea);
	}
} else if  (j_ea instanceof J_EAStorage) {
	c_storageAssets.add((J_EAStorage)j_ea);
	energyModel.c_storageAssets.add((J_EAStorage)j_ea);
	if (j_ea.energyAssetType == OL_EnergyAssetType.BUILDINGTHERMALS) {
		//traceln("Adding buildingThermals to gridconnection");	
		p_BuildingThermalAsset = (J_EABuilding)j_ea;
			/*if ( p_energyLabel != null & p_gridConnectionType != null){ // Get building thermals from lookup table when isolation label and house type are available
				double lossFactor_WpK2 = energyModel.v_buildingThermalPars.path( p_gridConnectionType.name() ).path(p_energyLabel.name()).path("lossFactor_WpK").doubleValue();
				double heatCapacity_JpK2 = energyModel.v_buildingThermalPars.path( p_gridConnectionType.name() ).path(p_energyLabel.name()).path("heatCapacity_JpK").doubleValue();
				p_BuildingThermalAsset.lossFactor_WpK = lossFactor_WpK2;
				p_BuildingThermalAsset.heatCapacity_JpK = heatCapacity_JpK2;
				traceln("House thermal model updated!");
				traceln("House type: %s, energy label: %s", p_gridConnectionType, p_energyLabel);
				traceln("lossfactor %s, heatcapacity %s", lossFactor_WpK2, heatCapacity_JpK2);
			}*/ // Deprecated get lossfactor and heatcapacity from json-input. Replace with other datasource!
		p_BuildingThermalAsset.updateAmbientTemperature( energyModel.v_currentAmbientTemperature_degC );
		//v_tempSetpoint_degC = p_BuildingThermalAsset.setTemperature_degC;		
		energyModel.c_ambientAirDependentAssets.add(p_BuildingThermalAsset);
	} else if (j_ea instanceof J_EAStorageGas) {
		p_gasBuffer = (J_EAStorageGas)j_ea;
	} else if (j_ea instanceof J_EAStorageElectric) {
		p_batteryAsset = (J_EAStorageElectric)j_ea;
		c_batteryAssets.add(j_ea);
		v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()/1000;
		energyModel.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()/1000;
		
	} else if (j_ea instanceof J_EAStorageHeat) {
		energyModel.c_ambientAirDependentAssets.add(j_ea);
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
		} else if( ((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.HEATDEMAND){
			//Do nothing
		} else {
			traceln( "Unrecognized profile type!");
		}
} else {
	traceln("Unrecognized energy asset %s in gridconnection %s", j_ea, this);
}

/*ALCODEEND*/}

double f_batteryManagementBalance_NBH(double batterySOC)
{/*ALCODESTART::1730897215443*/
//traceln("Battery storage capacity: " + ((J_EAStorageElectric)p_batteryAsset.j_ea).getStorageCapacity_kWh());
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	double currentLoadDeviation_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_currentLoadLowPassed_kW; // still excludes battery power
	//traceln("electricitySuprlus_kW: " + electricitySurplus_kW);
	//v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( electricitySurplus_kW - v_electricityPriceLowPassed_eurpkWh );
	double v_allowedDeliveryCapacity_kW = v_liveConnectionMetaData.contractedDeliveryCapacity_kW*0.95;
	double v_allowedFeedinCapacity_kW = v_liveConnectionMetaData.contractedFeedinCapacity_kW*0.95;
	//double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	double availableChargePower_kW = v_allowedDeliveryCapacity_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // Max battery charging power within grid capacity
	double availableDischargePower_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) + v_allowedFeedinCapacity_kW; // Max discharging power within grid capacity

	double SOC_setp_fr_offset = v_SOC_setp_fr_offset_balance; // default: 0.6
	//TODO: Verander in iets specifieks voor project - overwrite in GC Neighborhood
	//traceln("Current price is " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, between " + currentPricePowerBandNeg_kW + " kW and " + currentPricePowerBandPos_kW + " kW");
	//SOC_setp_fr = 0.6 + 0.25 * Math.cos(2*Math.PI*(energyModel.t_h-18)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
	
	//TODO forecast keer installed cap per buurt genormaliseerd.
	double windEnergyExpectedNormalized_fr = energyModel.v_WindYieldForecast_fr * energyModel.p_forecastTime_h * v_liveAssetsMetaData.totalInstalledWindPower_kW / p_batteryAsset.getStorageCapacity_kWh();
	double solarEnergyExpectedNormalized_fr = energyModel.v_SolarYieldForecast_fr * energyModel.p_forecastTime_h * v_liveAssetsMetaData.totalInstalledPVPower_kW / p_batteryAsset.getStorageCapacity_kWh();
	//double heatpumpExpectedEnergyDrawNormalized_fr = ...
	double SOC_setp_fr =  SOC_setp_fr_offset + 0.1 * Math.cos(2*Math.PI*(energyModel.t_h-7)/24) - 0.1 * windEnergyExpectedNormalized_fr - 0.1 * solarEnergyExpectedNormalized_fr;
	//traceln("Forecast-based SOC setpoint: " + SOC_setp_fr + " %");
	
	//traceln("SOC_setp_fr" + SOC_setp_fr);
	
	//traceln("SOC setpoint at " + getHourOfDay() + " h is " + SOC_setp_fr*100 + "%");
	double FeedbackGain_kWpSOC_factor = v_FeedbackGain_kWpSOC_factor_balance; // default: 0.4
	double FeedbackGain_kWpSOC = FeedbackGain_kWpSOC_factor * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
	double FeedforwardGain_kWpKw = 1; // Feedforward based on current surpluss in Coop
	double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
	double chargeSetpoint_kW = 0;
	chargeSetpoint_kW = -FeedforwardGain_kWpKw * currentLoadDeviation_kW + (SOC_setp_fr - batterySOC) * FeedbackGain_kWpSOC;
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	//traceln("v_powerFraction_fr" + p_batteryAsset.v_powerFraction_fr);
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
}
/*ALCODEEND*/}

double f_batteryManagementPrice_NBH(double currentBatteryStateOfCharge)
{/*ALCODESTART::1730897215446*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	//double willingnessToPayDefault_eurpkWh = 0.3;
	double chargeDischarge_offset_eurpkWh = 0.0;
	double WTPfeedbackGain_eurpSOC = 0.5; //Gelijk aan de gem marktprijs -  When SOC-error is 100%, adjust WTP price by 1 eurpkWh
	double priceGain_kWhpeur = v_priceGain_kWhpeur; // default: 2; // How strongly to ramp up power with price-delta's
	//double congestionTariffCoop_eurpkWh = -(((ConnectionOwner)p_ownerActor).p_CoopParent.v_electricitySurplus_kW + v_previousPowerElectricity_kW)/1200*0.1;
	
	double chargeSetpoint_kW = 0;
	
	//if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		//ConnectionOwner ownerActor = (ConnectionOwner)l_ownerActor.getConnectedAgent();
		//traceln("Initial Mappings are: " + ((ConnectionOwner)p_ownerActor).v_currentPriceBands);
		double currentElectricityPriceCharge_eurpkWh = energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000;//double lowPassFraction = min(1,1*1.2*energyModel.p_timeStep_h); // smaller value results in more filtering

		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		
		//double currentPricePowerBandPos_kW = ownerActor.v_priceBandsDelivery.ceilingKey(100.0); // Up to what power does this price hold?
		//double currentPricePowerBandNeg_kW = ownerActor.v_priceBandsDelivery.floorKey(100.0); // Down to what power does this price hold?
		
		double SOC_setp_fr = v_SOC_setp_fr_price; // Default: 0.5
		//traceln("Current price is " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, between " + currentPricePowerBandNeg_kW + " kW and " + currentPricePowerBandPos_kW + " kW");
		/*if (!c_vehicleAssets.isEmpty()) {
			SOC_setp_fr = 0.5 + 0.25 * Math.sin(2*Math.PI*(energyModel.t_h-12)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
		} else if (energyModel.v_totalInstalledWindPower_kW > 0 ) { 
			SOC_setp_fr = 0.9 - 0.8 * energyModel.v_WindYieldForecast_fr;
			//traceln("Forecast-based SOC setpoint: " + SOC_setp_fr + " %");
		}*/
		double SOC_deficit_fr = SOC_setp_fr - currentBatteryStateOfCharge; // How far away from desired SOC? SOC too LOW is a POSITIVE deficit
		
		// Define WTP price for charging and discharging!
		double WTP_charge_eurpkWh = v_electricityPriceLowPassed_eurpkWh - chargeDischarge_offset_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
		double WTP_discharge_eurpkWh = v_electricityPriceLowPassed_eurpkWh + chargeDischarge_offset_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
		
		//traceln("WTP charge is " + WTP_charge_eurpkWh + " eurpkWh, discharge is " + WTP_discharge_eurpkWh + " eurpkWh");
		// Choose charging power based on prices and desired SOC level
		if ( WTP_charge_eurpkWh > currentElectricityPriceCharge_eurpkWh ) { // if willingness to pay higher than current electricity price
			//chargeSetpoint_kW = min(p_batteryAsset.getElectricCapacity_kW()*(WTP_charge_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain, currentPricePowerBandPos_kW)  ;
			chargeSetpoint_kW = p_batteryAsset.getCapacityElectric_kW()*(WTP_charge_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_kWhpeur;
			//traceln("WTP charge " + WTP_charge_eurpkWh + " eurpkWh is high than electricity price " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, so charge!") ;
		} else if (WTP_discharge_eurpkWh < currentElectricityPriceCharge_eurpkWh) {
			//chargeSetpoint_kW = -min(p_batteryAsset.getElectricCapacity_kW()*(currentElectricityPriceDischarge_eurpkWh - WTP_discharge_eurpkWh)*priceGain, currentPricePowerBandPos_kW);
			chargeSetpoint_kW = -p_batteryAsset.getCapacityElectric_kW()*(currentElectricityPriceCharge_eurpkWh - WTP_discharge_eurpkWh)*priceGain_kWhpeur;
			//traceln("WTP discharge " + WTP_discharge_eurpkWh + " eurpkWh is lower than electricity price " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, so discharge!") ;
		}	
		
		// limit charging power to available connection capacity
		boolean b_stayWithinConnectionLimits = true;
		if( b_stayWithinConnectionLimits ) {		
			
			double availableChargePower_kW = v_liveConnectionMetaData.contractedDeliveryCapacity_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // Max battery charging power within grid capacity
			double availableDischargePower_kW = v_liveConnectionMetaData.contractedFeedinCapacity_kW + fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // Max discharging power within grid capacity
			chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
		}			
	
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
		
		//double chargedPower_kW = max(0,p_batteryAsset.v_powerFraction_fr*p_batteryAsset.getCapacityElectric_kW());
		//double dischargedPower_kW = min(0,p_batteryAsset.v_powerFraction_fr*p_batteryAsset.getCapacityElectric_kW());
		
		v_batteryMoneyMade_euro += -p_batteryAsset.v_powerFraction_fr*p_batteryAsset.getCapacityElectric_kW()*energyModel.p_timeStep_h*energyModel.v_epexForecast_eurpkWh;
	//}
	
}

/*ALCODEEND*/}

double f_setH2HeatingFr_industry(double amountOfHydrogenUseForHeating_fr)
{/*ALCODESTART::1732550952237*/

//Get current values
if(amountOfHydrogenUseForHeating_fr >= 1){
	//throw new RuntimeException("Can not replace all gas in industry with hydrogen! The model does not support this.");
	amountOfHydrogenUseForHeating_fr = 0.999;
}
double actualHeatingDemandSpaceHeating_fr =  (1 - v_amountOfHydrogenUseForHeating_industry_fr);
double[] currentPctArray = {v_amountOfGasBurners_industry_fr*100/actualHeatingDemandSpaceHeating_fr, 
							v_amountOfHybridHeatpump_industry_fr*100/actualHeatingDemandSpaceHeating_fr, 
							v_amountOfElectricHeatpumps_industry_fr*100/actualHeatingDemandSpaceHeating_fr, 
							v_amountOfDistrictHeating_industry_fr*100/actualHeatingDemandSpaceHeating_fr};

//Set new hydrogen use for heating fr
v_amountOfHydrogenUseForHeating_industry_fr = min(1, amountOfHydrogenUseForHeating_fr);


//Set new values
f_setHeatingMethodPct_industry(currentPctArray);

/*ALCODEEND*/}

double f_setHeatingMethodPct_industry(double[] pctArray)
{/*ALCODESTART::1732636739570*/
//Calculate actual space heating 
double actualHeatingDemandSpaceHeating_fr =  (1 - v_amountOfHydrogenUseForHeating_industry_fr);

v_amountOfGasBurners_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[0]/100;
v_amountOfElectricHeatpumps_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[1]/100;
v_amountOfHybridHeatpump_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[2]/100;
v_amountOfDistrictHeating_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[3]/100;


/*ALCODEEND*/}

double f_setHeatingMethodPct_agriculture(double[] pctArray)
{/*ALCODESTART::1732636773704*/
v_amountOfGasBurners_agriculture_fr = pctArray[0]/100;
v_amountOfElectricHeatpumps_agriculture_fr = pctArray[1]/100;
v_amountOfHybridHeatpump_agriculture_fr = pctArray[2]/100;
v_amountOfDistrictHeating_agriculture_fr = pctArray[3]/100;


/*ALCODEEND*/}

double f_resetSpecificGCStates_override()
{/*ALCODESTART::1734716016619*/
v_batteryMoneyMade_euro = 0;
/*ALCODEEND*/}

double f_batteryManagementBalanceNoGCCapacity_NBH(double batterySOC)
{/*ALCODESTART::1736869275213*/
//traceln("Battery storage capacity: " + ((J_EAStorageElectric)p_batteryAsset.j_ea).getStorageCapacity_kWh());
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	double currentLoadDeviation_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_currentLoadLowPassed_kW; // still excludes battery power
	//traceln("electricitySuprlus_kW: " + electricitySurplus_kW);
	//v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( electricitySurplus_kW - v_electricityPriceLowPassed_eurpkWh );
	//double v_allowedDeliveryCapacity_kW = p_contractedDeliveryCapacity_kW*0.95;
	//double v_allowedFeedinCapacity_kW = p_contractedFeedinCapacity_kW*0.95;
	//double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	//double availableChargePower_kW = v_allowedDeliveryCapacity_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // Max battery charging power within grid capacity
	//double availableDischargePower_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) + v_allowedFeedinCapacity_kW; // Max discharging power within grid capacity

	double SOC_setp_fr_offset = v_SOC_setp_fr_offset_balance; // default: 0.6
	//TODO: Verander in iets specifieks voor project - overwrite in GC Neighborhood
	//traceln("Current price is " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, between " + currentPricePowerBandNeg_kW + " kW and " + currentPricePowerBandPos_kW + " kW");
	//SOC_setp_fr = 0.6 + 0.25 * Math.cos(2*Math.PI*(energyModel.t_h-18)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
	
	//TODO forecast keer installed cap per buurt genormaliseerd.
	double windEnergyExpectedNormalized_fr = energyModel.v_WindYieldForecast_fr * energyModel.p_forecastTime_h * v_liveAssetsMetaData.totalInstalledWindPower_kW / p_batteryAsset.getStorageCapacity_kWh();
	double solarEnergyExpectedNormalized_fr = energyModel.v_SolarYieldForecast_fr * energyModel.p_forecastTime_h * v_liveAssetsMetaData.totalInstalledPVPower_kW / p_batteryAsset.getStorageCapacity_kWh();
	//double heatpumpExpectedEnergyDrawNormalized_fr = ...
	double SOC_setp_fr =  SOC_setp_fr_offset + 0.1 * Math.cos(2*Math.PI*(energyModel.t_h-7)/24) - 0.1 * windEnergyExpectedNormalized_fr - 0.1 * solarEnergyExpectedNormalized_fr;
	//traceln("Forecast-based SOC setpoint: " + SOC_setp_fr + " %");
	
	//traceln("SOC_setp_fr" + SOC_setp_fr);
	
	//traceln("SOC setpoint at " + getHourOfDay() + " h is " + SOC_setp_fr*100 + "%");
	double FeedbackGain_kWpSOC_factor = v_FeedbackGain_kWpSOC_factor_balance; // default: 0.4
	double FeedbackGain_kWpSOC = FeedbackGain_kWpSOC_factor * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
	double FeedforwardGain_kWpKw = 1; // Feedforward based on current surpluss in Coop
	double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
	double chargeSetpoint_kW = 0;
	chargeSetpoint_kW = -FeedforwardGain_kWpKw * currentLoadDeviation_kW + (SOC_setp_fr - batterySOC) * FeedbackGain_kWpSOC;
	//chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	//traceln("v_powerFraction_fr" + p_batteryAsset.v_powerFraction_fr);
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
}
/*ALCODEEND*/}

