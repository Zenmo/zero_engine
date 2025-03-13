double f_connectToParents()
{/*ALCODESTART::1658500398176*/
GridNode myParentNodeElectric = findFirst(energyModel.pop_gridNodes, p->p.p_gridNodeID.equals(p_parentNodeElectricID)) ;
if( myParentNodeElectric instanceof GridNode ) {
	l_parentNodeElectric.connectTo(myParentNodeElectric);
	myParentNodeElectric.f_connectToChild(this);
	//p_parentNodeElectric = myParentNodeElectric;
}

GridNode myParentNodeHeat = findFirst(energyModel.pop_gridNodes, p->p.p_gridNodeID.equals(p_parentNodeHeatID)) ;
if( myParentNodeHeat instanceof GridNode ) {
	l_parentNodeHeat.connectTo(myParentNodeHeat);
	myParentNodeHeat.f_connectToChild(this);
	//p_parentNodeHeat = myParentNodeHeat;
}

if (p_owner==null){
	p_owner = findFirst(energyModel.pop_connectionOwners, p->p.p_actorID.equals(p_ownerID));
}

if (p_owner!=null){
	ConnectionOwner myParentConnectionOwner = p_owner; //findFirst(energyModel.pop_connectionOwners, p->p.p_actorID.equals(p_ownerID)) ;
	if( myParentConnectionOwner instanceof ConnectionOwner) {
		//p_ownerActor = myParentConnectionOwner;
		l_ownerActor.connectTo(myParentConnectionOwner);
		myParentConnectionOwner.f_connectToChild(this);
	}
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

double f_connectToChild(J_EA ConnectingEnergyAsset)
{/*ALCODESTART::1658752229618*/
//assetLinks.connectTo(ConnectingChildNode);
J_EA j_ea = ConnectingEnergyAsset;

//dont add asset to list if it is not a tangible asset (i.e. a heating model for buildings)
if( ! (j_ea instanceof J_EAStorageHeat)) {
	c_energyAssets.add(j_ea);
}
if (j_ea instanceof J_EAConsumption ) {
	if (j_ea instanceof J_EADieselVehicle) {
		//c_vehicleAssets.add( EA );
		//c_dieselVehicles.add( EA );
		//c_vehicleAssets.get( v_vehicleIndex ).v_powerFraction_fr = 1;
		/*MobilityTracker m = main.add_mobilityTrackers();
		c_mobilityTrackers.add( m );
		m.p_vehicleIndex = v_vehicleIndex;
		m.p_gridConnection = this;
		m.p_energyAsset = c_vehicleAssets.get( v_vehicleIndex );
		m.p_vehicleInstance = (J_EADieselVehicle)m.p_energyAsset.j_ea;
		m.p_vehicleInstance = (J_EADieselVehicle)m.p_vehicleInstance;
		m.p_mobilityPatternType = OL_MobilityPatternType.TRUCK;
		((J_EADieselVehicle)m.p_vehicleInstance).setMobilityTracker( m );
		m.f_getData();*/
		//v_vehicleIndex ++;
	}
	//c_consumptionAssets.add(EA);
} 
else if (j_ea instanceof J_EAProduction ) {
	c_productionAssets.add((J_EAProduction)j_ea);
} 
else if (j_ea instanceof J_EAStorage ) {
	if ( j_ea instanceof J_EAStorageHeat) {
		if ( ((J_EAStorageHeat)j_ea).heatStorageType == OL_EAStorageTypes.HEATMODEL_BUILDING ) {
			//p_BuildingThermalAsset = EA; // Obsolete
		}
		else if ( ((J_EAStorageHeat)j_ea).heatStorageType == OL_EAStorageTypes.HEATBUFFER ) {
			//c_storageAssets.add(EA);
			//p_heatBuffer = EA;
		}
	} 
	else if (j_ea instanceof J_EAStorageElectric) {
		if(j_ea instanceof J_EAEV && ((J_EAEV)j_ea).getStorageCapacity_kWh() !=0) {
			//c_storageAssets.add(EA);
			//c_vehicleAssets.add( EA );
			//EA.v_powerFraction_fr = 1; //Waarom staat dit op 1? 29-01-23 PH
			/*MobilityTracker m = main.add_mobilityTrackers();
			c_mobilityTrackers.add( m );
			m.p_vehicleIndex = v_vehicleIndex;
			m.p_gridConnection = this;
			m.p_energyAsset = EA;
			m.p_vehicleInstance = (J_EAEV)m.p_energyAsset.j_ea;
 			m.p_mobilityPatternType = OL_MobilityPatternType.TRUCK;
			((J_EAEV)m.p_vehicleInstance).setMobilityTracker( m );
			m.f_getData();*/
			//v_vehicleIndex ++;
		}
		else if (((J_EAStorageElectric)j_ea).getStorageCapacity_kWh() != 0) {
			//c_storageAssets.add(EA);
			//p_batteryAsset = EA;
		}		
		else{
			traceln(getName() + "storage asset create that cannot be identified (i.e. its not an EV and not an heatstorage");
		}
	}
} 
else if (j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)j_ea);
	if (j_ea instanceof J_EAConversionGasBurner|| j_ea instanceof J_EAConversionHeatPump || j_ea instanceof J_EAConversionHeatDeliverySet ) {
		if (p_primaryHeatingAsset == null) {
			p_primaryHeatingAsset = (J_EAConversion)j_ea;
		} else if (p_secondaryHeatingAsset == null) {
			p_secondaryHeatingAsset = (J_EAConversion)j_ea;
		} else {
			traceln("House " + p_gridConnectionID + " already has two heating systems!");
		}
		//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
	}
	else if (j_ea instanceof J_EAConversionCurtailer || j_ea instanceof J_EAConversionCurtailerHeat) {
		p_curtailer = (J_EAConversionCurtailer)j_ea;
	} 
}
else {
	traceln("f_connectToChild in GC: Exception! EnergyAsset " + ConnectingEnergyAsset + " is of unknown type: " + j_ea.energyAssetType);
	traceln( "TEST");
}
/*ALCODEEND*/}

double f_connectionMetering()
{/*ALCODESTART::1660212665961*/
if ( abs(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.HEAT)) > 0.1 && l_parentNodeHeat == null ) {
	traceln((fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.HEAT)));
	traceln("Heat unbalance in gridConnection: " + p_gridConnectionID);
	pauseSimulation();
}

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

double f_instantiateEnergyAssets()
{/*ALCODESTART::1668181559833*/
//traceln("asset " + p_energyAssetList);
if( p_energyAssetList != null) {
	for( JsonNode l : p_energyAssetList ) {
		OL_EACategories assetCategory = OL_EACategories.valueOf(l.required( "category" ).textValue());
		OL_EnergyAssetType assetType = OL_EnergyAssetType.valueOf(l.required( "type" ).textValue()) ;
		String assetName = l.required( "name" ).textValue();	
		switch( assetCategory )  {
			case CONSUMPTION:							
				if ( p_gridConnectionCategory == OL_GridConnectionCategory.HOUSE) {
					if( assetType.equals("ELECTRICITY_DEMAND") && assetName.contains("TEMPLATE House other electricity demand") ) {
						assetName = "House_other_electricity"; 
					} else if (assetType.equals("HOT_WATER_CONSUMPTION")) {				
						assetName = "House_hot_water";
					} 
				}
											
				double yearlyDemandElectricity_kWh = l.path( "yearlyDemandElectricity_kWh").doubleValue();
				double yearlyDemandHeat_kWh = l.path( "yearlyDemandHeat_kWh").doubleValue();
				double yearlyDemandHydrogen_kWh = l.path( "yearlyDemandHydrogen_kWh").doubleValue();
				double yearlyDemandMethane_kWh = l.path( "yearlyDemandMethane_kWh").doubleValue();
				double yearlyDemandDiesel_kWh = l.path( "yearlyDemandDiesel_kWh").doubleValue();
				double energyConsumption_kWhpkm = l.path( "energyConsumption_kWhpkm" ).doubleValue();
				double vehicleScaling = l.path("vehicleScaling").asDouble(1.0);
	
				if (assetType == OL_EnergyAssetType.DIESEL_VEHICLE) {
					//traceln("Adding diesel vehicle asset without EnergyAsset agent!");
					J_EADieselVehicle dieselVehicle = new J_EADieselVehicle(this, energyConsumption_kWhpkm, energyModel.p_timeStep_h, vehicleScaling);
					f_connectToJ_EA(dieselVehicle);					
				} else {
					J_EAConsumption consumptionAsset = new J_EAConsumption(this,assetType,assetName,yearlyDemandElectricity_kWh,yearlyDemandHeat_kWh,yearlyDemandHydrogen_kWh,yearlyDemandMethane_kWh,yearlyDemandDiesel_kWh,energyModel.p_timeStep_h);
					f_connectToJ_EA(consumptionAsset);						
				}
			break;
			
			case PRODUCTION:								
				double capacityElectricity_kW = l.path( "capacityElectricity_kW").doubleValue();
				double capacityHeat_kW = l.path( "capacityHeat_kW").doubleValue();
				double yearlyProductionHydrogen_kWh = l.path( "yearlyProductionHydrogen_kWh").doubleValue();
				double yearlyProductionMethane_kWh = l.path( "yearlyProductionMethane_kWh").doubleValue();
				double outputTemperature_degC = l.path( "deliveryTemp_degC").doubleValue();
				
				J_EAProduction productionAsset = new J_EAProduction ( this, assetType, assetName, capacityElectricity_kW, capacityHeat_kW, yearlyProductionMethane_kWh, yearlyProductionHydrogen_kWh, energyModel.p_timeStep_h, outputTemperature_degC );
				f_connectToJ_EA(productionAsset);
			
				// Determine residual heat delivery temperature from coldest connected asset
				/*if(this instanceof GCResidualHeat) {					
					traceln("Residual heat delivery temperature = "+outputTemperature_degC);
					((GCResidualHeat)this).p_deliveryTemp_degC = outputTemperature_degC < ((GCResidualHeat)this).p_deliveryTemp_degC? outputTemperature_degC : ((GCResidualHeat)this).p_deliveryTemp_degC;
				}*/
			break;
			
			case CONVERSION:
				if ( assetType == OL_EnergyAssetType.GAS_PIT || assetType == OL_EnergyAssetType.GAS_BURNER || assetType == OL_EnergyAssetType.METHANE_FURNACE ){
					// traceln("Adding gaspit!");
					double capacityHeat_kW1 = l.required( "capacityHeat_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC1 = 0.0;
					J_EAConversionGasBurner gasburner = new J_EAConversionGasBurner(this, assetType, capacityHeat_kW1, eta_r, energyModel.p_timeStep_h, deliveryTemp_degC1);
					f_connectToJ_EA(gasburner);
				} else if ( assetType == OL_EnergyAssetType.ELECTRIC_HOB){
					//traceln("Adding electric HOB!");
					double capacityHeat_kW1 = l.required( "capacityHeat_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC1 = 0.0;
					J_EAElectricHob eHOB = new J_EAElectricHob(this, capacityHeat_kW1, eta_r, energyModel.p_timeStep_h, deliveryTemp_degC1);
					f_connectToJ_EA(eHOB);
				} else if ( assetType == OL_EnergyAssetType.HEAT_PUMP_AIR || assetType == OL_EnergyAssetType.HEAT_PUMP_GROUND || assetType == OL_EnergyAssetType.HEAT_PUMP_WATER){
					double capacityElectric_kW = l.path( "capacityElectricity_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC = l.path( "deliveryTemp_degC" ).doubleValue();
					String ambientTempType = l.path( "ambientTempType" ).asText();
					double belowZeroHeatpumpEtaReductionFactor = l.path( "etaReduction_r" ).asDouble(1.0);
					J_EAConversionHeatPump	heatpump = new J_EAConversionHeatPump( this, energyModel.p_timeStep_h, capacityElectric_kW, eta_r, main.v_currentAmbientTemperature_degC, deliveryTemp_degC, ambientTempType, 0, belowZeroHeatpumpEtaReductionFactor );
					main.c_ambientAirDependentAssets.add(heatpump);
					f_connectToJ_EA(heatpump);
				} else if ( assetType == OL_EnergyAssetType.ELECTRIC_HEATER){
					double capacityElectric_kW = l.path( "capacityElectricity_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC = l.path( "deliveryTemp_degC" ).doubleValue();
					J_EAConversionElectricHeater electricHeater = new J_EAConversionElectricHeater( this, capacityElectric_kW, eta_r, energyModel.p_timeStep_h, deliveryTemp_degC);
					f_connectToJ_EA(electricHeater);
				} else if ( assetType == OL_EnergyAssetType.HEAT_DELIVERY_SET){
					double capacityHeat_kW1 = l.required( "capacityHeat_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC = l.path( "deliveryTemp_degC" ).doubleValue();
					J_EAConversionHeatDeliverySet deliverySet = new J_EAConversionHeatDeliverySet( this, capacityHeat_kW1, eta_r, deliveryTemp_degC, energyModel.p_timeStep_h );
					f_connectToJ_EA(deliverySet);
				} else if ( assetType == OL_EnergyAssetType.HYDROGEN_FURNACE){
					double capacityHeat_kW1 = l.required( "capacityHeat_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC = 0.0;
					J_EAConversionHydrogenBurner hydrogenBurner = new J_EAConversionHydrogenBurner( this, capacityHeat_kW1, eta_r, energyModel.p_timeStep_h, deliveryTemp_degC );
					f_connectToJ_EA(hydrogenBurner);
				} else if ( assetType == OL_EnergyAssetType.ELECTROLYSER){
					double capacityElectric_kW = l.path( "capacityElectricity_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					J_EAConversionElektrolyser electrolyser = new J_EAConversionElektrolyser( this, capacityElectric_kW, eta_r, energyModel.p_timeStep_h );
					f_connectToJ_EA(electrolyser);
				} else if ( assetType == OL_EnergyAssetType.CURTAILER){
					double capacityElectric_kW = l.path( "capacityElectricity_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					J_EAConversionCurtailer curtailer = new J_EAConversionCurtailer( this, capacityElectric_kW, eta_r, energyModel.p_timeStep_h );
					f_connectToJ_EA(curtailer);
				} else if ( assetType == OL_EnergyAssetType.CURTAILER_HEAT){
					double capacityHeat_kW1 = l.required( "capacityHeat_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					J_EAConversionCurtailerHeat curtailerHeat = new J_EAConversionCurtailerHeat( this, capacityHeat_kW1, eta_r, energyModel.p_timeStep_h );
					f_connectToJ_EA(curtailerHeat);
				} else if ( assetType == OL_EnergyAssetType.METHANE_CHP){
					double capacityHeat_kW1 = l.required( "capacityHeat_kW").doubleValue();
					double capacityElectric_kW = l.path( "capacityElectricity_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					double deliveryTemp_degC = 0.0;
					J_EAConversionGasCHP GasCHP = new J_EAConversionGasCHP(this, capacityElectric_kW, capacityHeat_kW1, eta_r, energyModel.p_timeStep_h, deliveryTemp_degC);
					f_connectToJ_EA(GasCHP);
				} else if ( assetType == OL_EnergyAssetType.BIOGAS_METHANE_CONVERTER){
					double capacityMethane_kW = l.required( "capacityMethane_kW").doubleValue();
					double eta_r = l.required( "eta_r").doubleValue();
					J_EAConversionBiogasMethane biogasMethaneConverter = new J_EAConversionBiogasMethane(this, capacityMethane_kW, eta_r, energyModel.p_timeStep_h);
					f_connectToJ_EA(biogasMethaneConverter);
				} else {
					traceln("Unknown conversion energy asset! Skipping instantiation!");

				} 
			break;				
			case STORAGE:
				if ( assetType == OL_EnergyAssetType.ELECTRIC_VEHICLE){
					double capacityElectric_kW1 = l.path( "capacityElectricity_kW").doubleValue();
					double storageCapacity_kWh = l.path( "storageCapacity_kWh" ).doubleValue();
					double initialStateOfCharge_r = l.path( "stateOfCharge_r" ).doubleValue();
					double energyConsumption_kWhpkm2 = l.path( "energyConsumption_kWhpkm" ).doubleValue();
					double vehicleScalingElectric = l.path("vehicleScaling").asDouble(1.0);
					//storageAsset.j_ea = new J_EAEV(storageAsset, capacityElectric_kW1, storageCapacity_kWh, initialStateOfCharge_r, energyModel.p_timeStep_h, energyConsumption_kWhpkm2, vehicleScalingElectric );  
					
					J_EAEV ev= new J_EAEV(this, capacityElectric_kW1, storageCapacity_kWh, initialStateOfCharge_r, energyModel.p_timeStep_h, energyConsumption_kWhpkm2, vehicleScalingElectric );  
					f_connectToJ_EA(ev);	
					
				} else if ( assetType == OL_EnergyAssetType.BUILDINGTHERMALS) {										
					double capacityHeat_kW3 = l.path( "capacityHeat_kW").doubleValue();
			
					double lossFactor_WpK2 = l.path( "lossFactor_WpK" ).doubleValue() * uniform(0.8, 1.2);
					double heatCapacity_JpK2 = l.path( "heatCapacity_JpK" ).doubleValue() * uniform(0.7, 1.3);
									
					double minTemperature_degC2 = l.path( "minTemp_degC" ).asDouble(1.0);
					double maxTemperature_degC2 = l.path( "maxTemp_degC" ).asDouble(90.0);
					double initialTemperature_degC2 = l.path( "initialTemperature_degC" ).doubleValue();
					String ambientTempType3 = l.path( "ambientTempType" ).asText("AIR"); //ALWAYS AIR!
					double setTemperature_degC2 = 10.0; // this value does nothing for a heatmodel. But it is required to initialize the J_instance
					double solarAbsorptionFactor_m2 = l.path("solarAbsorptionFactor_m2").asDouble(1.0);

					J_EABuilding buildingThermals = new J_EABuilding(this, OL_EAStorageTypes.HEATMODEL_BUILDING, capacityHeat_kW3, lossFactor_WpK2, energyModel.p_timeStep_h, initialTemperature_degC2, minTemperature_degC2, maxTemperature_degC2, setTemperature_degC2, heatCapacity_JpK2, ambientTempType3, solarAbsorptionFactor_m2);
					f_connectToJ_EA(buildingThermals);
							
				} else if (assetType == OL_EnergyAssetType.STORAGE_ELECTRIC) {
					double capacityElectric_kW1 = l.path( "capacityElectricity_kW").doubleValue();
					double storageCapacity_kWh = l.path( "storageCapacity_kWh" ).doubleValue();
					double initialStateOfCharge_r = l.path( "stateOfCharge_r" ).doubleValue();
					J_EAStorageElectric storageAsset = new J_EAStorageElectric(this, capacityElectric_kW1, storageCapacity_kWh, initialStateOfCharge_r, energyModel.p_timeStep_h);						
					f_connectToJ_EA(storageAsset);
					p_batteryAsset=storageAsset;
					c_storageAssets.add(storageAsset);
					v_batterySOC_fr = storageAsset.getCurrentStateOfCharge();
			
				} else if (assetType == OL_EnergyAssetType.STORAGE_GAS) {
					double capacityGas_kW = l.path( "capacityGas_kW").doubleValue();
					double storageCapacity_kWh = l.path( "storageCapacity_kWh" ).doubleValue();
					double initialStateOfCharge_r = l.path( "stateOfCharge_r" ).doubleValue();
					J_EAStorageGas storageAsset = new J_EAStorageGas(this, capacityGas_kW, storageCapacity_kWh, initialStateOfCharge_r, energyModel.p_timeStep_h);
					f_connectToJ_EA(storageAsset);
					c_storageAssets.add(storageAsset);
					p_gasBuffer = storageAsset;
					//traceln("gasBuffer gasCapacity_kW: " + p_gasBuffer.j_ea.getGasCapacity_kW() + " kW");
				
				} else if( assetType == OL_EnergyAssetType.STORAGE_HEAT) {
					
				    double capacityHeat_kW2 = l.path( "capacityHeat_kW").doubleValue();
					double lossFactor_WpK = l.path( "lossFactor_WpK" ).doubleValue() * uniform(0.7, 1.3);
					double heatCapacity_JpK = l.path( "heatCapacity_JpK" ).doubleValue();
					double minTemperature_degC = l.path( "minTemp_degC" ).asDouble(44.0); // provide default values
					double maxTemperature_degC = l.path( "maxTemp_degC" ).asDouble(99.0);
					double setTemperature_degC = l.path( "setTemp_degC" ).asDouble(66.0);
					double initialTemperature_degC = l.path( "initialTemperature_degC" ).doubleValue();
					String ambientTempType2 = l.path( "ambientTempType" ).asText();
					
					if (this instanceof GCHouse ) {
						heatCapacity_JpK = heatCapacity_JpK * uniform(0.7, 1.3);
						initialTemperature_degC = initialTemperature_degC * uniform(0.7, 1.3);
					}
					
					//traceln("gridconnection heatstorage asset initialisation check! minTemp = "+minTemperature_degC+", maxTemperature_degC = "+maxTemperature_degC+", setTemp_degC = "+ setTemperature_degC+", initialTemperature_degC = "+initialTemperature_degC);
					J_EAStorageHeat storageAsset = new J_EAStorageHeat(this, OL_EAStorageTypes.HEATBUFFER, capacityHeat_kW2, lossFactor_WpK, energyModel.p_timeStep_h, initialTemperature_degC, minTemperature_degC, maxTemperature_degC, setTemperature_degC, heatCapacity_JpK, ambientTempType2);	
					
					f_connectToJ_EA(storageAsset);
					c_storageAssets.add(storageAsset);
				} else {
					traceln("F_instantiateEnergyAssets: ERROR, storage asset type not available");		
					
				}
			break;
			default:
				traceln("not a valid energy asset category." + assetCategory);
			break;
		}
	}
//traceln("GridConnection "+this.p_gridConnectionID+" has finished initializing its energyAssets!");
}
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
v_conversionPowerElectric_kW = 0;
v_CHPProductionElectric_kW = 0;

if (v_enableNFato) {
	f_nfatoUpdateConnectionCapacity();
}

c_tripTrackers.forEach(t -> t.manageActivities((energyModel.t_h-energyModel.p_runStartTime_h)*60));

f_operateFixedAssets();
f_operateFlexAssets();

if (v_enableCurtailment) {
	f_curtailment();
}

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
v_maxConnectionLoad_fr = 0;

v_previousPowerElectricity_kW = 0;
v_previousPowerHeat_kW = 0;
v_electricityPriceLowPassed_eurpkWh = 0;
v_batterySOC_fr = 0;//(p_batteryAsset == null) ? 0 : p_batteryAsset.getCurrentStateOfCharge();
v_currentElectricityPriceConsumption_eurpkWh  = 0;
v_currentLoadLowPassed_kW = 0;
//v_currentIndoorTemp_degC = (p_BuildingThermalAsset == null) ? 0 : p_BuildingThermalAsset.getCurrentTemperature();
 
v_rapidRunData.resetAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();

// Not yet in J_RapidRunData:
v_totalPVGeneration_MWh = 0;
v_totalWindGeneration_MWh = 0;

//Reset specific variables/collections in specific GC types (GCProduction, GConversion, etc.)
f_resetSpecificGCStates();


//========== OBSOLETE ==========//
/*
// Imports / Exports
v_totalEnergyImport_MWh = 0;
v_totalEnergyExport_MWh = 0;
fm_totalImports_MWh.clear();
fm_totalExports_MWh.clear();

// Energy / Electricity
v_totalElectricityProduced_MWh = 0;
v_totalElectricityConsumed_MWh = 0;
v_totalElectricitySelfConsumed_MWh = 0;
v_totalEnergyProduced_MWh = 0;
v_totalEnergyConsumed_MWh = 0;
v_totalEnergySelfConsumed_MWh = 0;

//Max peaks
v_maxPeakDelivery_kW = 0;
v_maxPeakFeedin_kW = 0;

//Specific assets
v_totalEnergyCurtailed_MWh = 0;
v_totalPrimaryEnergyProductionHeatpumps_MWh = 0;

//Overload
v_totalOverloadDurationDelivery_hr = 0;
v_totalOverloadDurationFeedin_hr = 0;

// Accumulators
am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h );
am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, energyModel.p_timeStep_h, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) );
am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(v_activeConsumptionEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(v_activeProductionEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

acc_dailyAverageEnergyProduction_kW.reset();
acc_dailyAverageEnergyConsumption_kW.reset();

acc_totalEnergyCurtailed_kW.reset();
acc_totalPrimaryEnergyProductionHeatpumps_kW.reset();

acc_dailyAverageBaseloadElectricityConsumption_kW.reset();
acc_dailyAverageHeatPumpElectricityConsumption_kW.reset();
acc_dailyAverageElectricVehicleConsumption_kW.reset();
acc_dailyAverageBatteriesConsumption_kW.reset();
acc_dailyAverageElectricCookingConsumption_kW.reset();
acc_dailyAverageElectrolyserElectricityConsumption_kW.reset();
acc_dailyAverageDistrictHeatingConsumption_kW.reset();

acc_dailyAveragePVProduction_kW.reset();
acc_dailyAverageWindProduction_kW.reset();
acc_dailyAverageV2GProduction_kW.reset();
acc_dailyAverageBatteriesProduction_kW.reset();
acc_dailyAverageCHPElectricityProduction_kW.reset();

//acc_dailyAverageBatteriesStoredEnergy_MWh.reset();

//========== SUMMER WEEK ==========//
// Imports / Exports
fm_summerWeekImports_MWh.clear();
fm_summerWeekExports_MWh.clear();
v_summerWeekEnergyImport_MWh = 0;
v_summerWeekEnergyExport_MWh = 0;

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
am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);

acc_summerWeekEnergyProduction_kW.reset();
acc_summerWeekEnergyConsumption_kW.reset();

acc_summerWeekEnergyCurtailed_kW.reset();
acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.reset();

acc_summerWeekFeedinCapacity_kW.reset();
acc_summerWeekDeliveryCapacity_kW.reset();

acc_summerWeekBaseloadElectricityConsumption_kW.reset();
acc_summerWeekHeatPumpElectricityConsumption_kW.reset();
acc_summerWeekElectricVehicleConsumption_kW.reset();
acc_summerWeekBatteriesConsumption_kW.reset();
acc_summerWeekElectricCookingConsumption_kW.reset();
acc_summerWeekElectrolyserElectricityConsumption_kW.reset();
acc_summerWeekDistrictHeatingConsumption_kW.reset();

acc_summerWeekPVProduction_kW.reset();
acc_summerWeekWindProduction_kW.reset();
acc_summerWeekV2GProduction_kW.reset();
acc_summerWeekBatteriesProduction_kW.reset();
acc_summerWeekCHPElectricityProduction_kW.reset();

//acc_summerWeekBatteriesStoredEnergy_MWh.reset();

//========== WINTER WEEK ==========//
// Imports / Exports
fm_winterWeekImports_MWh.clear();
fm_winterWeekExports_MWh.clear();
v_winterWeekEnergyImport_MWh = 0;
v_winterWeekEnergyExport_MWh = 0;

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
am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);

acc_winterWeekEnergyProduction_kW.reset();
acc_winterWeekEnergyConsumption_kW.reset();

acc_winterWeekEnergyCurtailed_kW.reset();
acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.reset();

acc_winterWeekFeedinCapacity_kW.reset();
acc_winterWeekDeliveryCapacity_kW.reset();

acc_winterWeekBaseloadElectricityConsumption_kW.reset();
acc_winterWeekHeatPumpElectricityConsumption_kW.reset();
acc_winterWeekElectricVehicleConsumption_kW.reset();
acc_winterWeekBatteriesConsumption_kW.reset();
acc_winterWeekElectricCookingConsumption_kW.reset();
acc_winterWeekElectrolyserElectricityConsumption_kW.reset();
acc_winterWeekDistrictHeatingConsumption_kW.reset();

acc_winterWeekPVProduction_kW.reset();
acc_winterWeekWindProduction_kW.reset();
acc_winterWeekV2GProduction_kW.reset();
acc_winterWeekBatteriesProduction_kW.reset();
acc_winterWeekCHPElectricityProduction_kW.reset();

//acc_winterWeekBatteriesStoredEnergy_MWh.reset();

//========== DAYTIME ==========//
// Imports / Exports
am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));
am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));
fm_daytimeImports_MWh.clear();
fm_daytimeExports_MWh.clear();
v_daytimeEnergyImport_MWh = 0;
v_daytimeEnergyExport_MWh = 0;

acc_daytimeElectricityProduction_kW.reset();
acc_daytimeElectricityConsumption_kW.reset();
acc_daytimeEnergyProduction_kW.reset();
acc_daytimeEnergyConsumption_kW.reset();
	
// Energy / Electricity
v_daytimeElectricityProduced_MWh = 0;
v_daytimeElectricityConsumed_MWh = 0;
v_daytimeElectricitySelfConsumed_MWh = 0;
v_daytimeEnergyProduced_MWh = 0;
v_daytimeEnergyConsumed_MWh = 0;
v_daytimeEnergySelfConsumed_MWh = 0;

//========== NIGHTTIME ==========//
// Imports / Exports
fm_nighttimeImports_MWh.clear();
fm_nighttimeExports_MWh.clear();
v_nighttimeEnergyImport_MWh = 0;
v_nighttimeEnergyExport_MWh = 0;

// Energy / Electricity
v_nighttimeElectricityProduced_MWh = 0;
v_nighttimeElectricityConsumed_MWh = 0;
v_nighttimeElectricitySelfConsumed_MWh = 0;
v_nighttimeEnergyProduced_MWh = 0;
v_nighttimeEnergyConsumed_MWh = 0;
v_nighttimeEnergySelfConsumed_MWh = 0;

//========== WEEKDAY ==========//
// Imports / Exports
fm_weekdayImports_MWh.clear();
fm_weekdayExports_MWh.clear();
v_weekdayEnergyImport_MWh = 0;
v_weekdayEnergyExport_MWh = 0;

// Energy / Electricity
v_weekdayElectricityProduced_MWh = 0;
v_weekdayElectricityConsumed_MWh = 0;
v_weekdayElectricitySelfConsumed_MWh = 0;
v_weekdayEnergyProduced_MWh = 0;
v_weekdayEnergyConsumed_MWh = 0;
v_weekdayEnergySelfConsumed_MWh = 0;

//========== WEEKEND ==========//
// Imports / Exports
am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 2 / 7  * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);
am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 2 / 7 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);
fm_weekendImports_MWh.clear();
fm_weekendExports_MWh.clear();
v_weekendEnergyImport_MWh = 0;
v_weekendEnergyExport_MWh = 0;

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
*/


/*ALCODEEND*/}

double f_batteryManagementBalance(double batterySOC)
{/*ALCODESTART::1669022552777*/
//traceln("Battery storage capacity: " + ((J_EAStorageElectric)p_batteryAsset.j_ea).getStorageCapacity_kWh());
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	double electricitySurplus_kW = - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // still excludes battery power
	//traceln("electricitySuprlus_kW: " + electricitySurplus_kW);
	//v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( electricitySurplus_kW - v_electricityPriceLowPassed_eurpkWh );
	double v_allowedDeliveryCapacity_kW = p_contractedDeliveryCapacity_kW*0.9;
	double v_allowedFeedinCapacity_kW = p_contractedFeedinCapacity_kW*0.9;
	//double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	double availableChargePower_kW = v_allowedDeliveryCapacity_kW + electricitySurplus_kW; // Max battery charging power within grid capacity
	double availableDischargePower_kW = electricitySurplus_kW - v_allowedFeedinCapacity_kW; // Max discharging power within grid capacity

	double SOC_setp_fr = 0.5;
	//traceln("Current price is " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, between " + currentPricePowerBandNeg_kW + " kW and " + currentPricePowerBandPos_kW + " kW");
	if (!c_vehicleAssets.isEmpty()) {
		SOC_setp_fr = 0.6 + 0.25 * Math.sin(2*Math.PI*(energyModel.t_h-12)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
	} else if (energyModel.v_totalInstalledWindPower_kW > 0 ) { // Look at weather forecast to charge/discharge battery
			SOC_setp_fr = 0.9 - 0.8 * energyModel.v_WindYieldForecast_fr;
			//traceln("Forecast-based SOC setpoint: " + SOC_setp_fr + " %");
	}
	//traceln("SOC setpoint at " + energyModel.t_hourOfDay + " h is " + SOC_setp_fr*100 + "%");
	double FeedbackGain_kWpSOC = 1.5 * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
	double FeedforwardGain_kWpKw = 0.8; // Feedforward based on current surpluss in Coop
	double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
	double chargeSetpoint_kW = 0;
	chargeSetpoint_kW = FeedforwardGain_kWpKw * (electricitySurplus_kW + chargeOffset_kW) + (SOC_setp_fr - batterySOC) * FeedbackGain_kWpSOC;
	chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
}
/*ALCODEEND*/}

double f_batteryManagementPrice(double currentBatteryStateOfCharge)
{/*ALCODESTART::1669022552780*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	//double willingnessToPayDefault_eurpkWh = 0.3;
	double chargeDischarge_offset_eurpkWh = 0.0;
	double WTPfeedbackGain_eurpSOC = 0.5; // When SOC-error is 100%, adjust WTP price by 1 eurpkWh
	double priceGain_kWhpeur = 2; // How strongly to ramp up power with price-delta's
	//double congestionTariffCoop_eurpkWh = -(((ConnectionOwner)p_ownerActor).p_CoopParent.v_electricitySurplus_kW + v_previousPowerElectricity_kW)/1200*0.1;
	
	double chargeSetpoint_kW = 0;
	
	if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		ConnectionOwner ownerActor = (ConnectionOwner)l_ownerActor.getConnectedAgent();
		//traceln("Initial Mappings are: " + ((ConnectionOwner)p_ownerActor).v_currentPriceBands);
		double currentElectricityPriceCharge_eurpkWh = ownerActor.f_getElectricityPrice(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY)+100.0); // query price at 100kW charging
		double currentElectricityPriceDischarge_eurpkWh = ownerActor.f_getElectricityPrice(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY)-100.0); // query price at -100kW charging
		//double lowPassFraction = min(1,1*1.2*energyModel.p_timeStep_h); // smaller value results in more filtering
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		
		//double currentPricePowerBandPos_kW = ownerActor.v_priceBandsDelivery.ceilingKey(100.0); // Up to what power does this price hold?
		//double currentPricePowerBandNeg_kW = ownerActor.v_priceBandsDelivery.floorKey(100.0); // Down to what power does this price hold?
		
		double SOC_setp_fr = 0.5;
		//traceln("Current price is " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, between " + currentPricePowerBandNeg_kW + " kW and " + currentPricePowerBandPos_kW + " kW");
		if (!c_vehicleAssets.isEmpty()) {
			SOC_setp_fr = 0.5 + 0.25 * Math.sin(2*Math.PI*(energyModel.t_h-12)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
		} else if (energyModel.v_totalInstalledWindPower_kW > 0 ) { 
			SOC_setp_fr = 0.9 - 0.8 * energyModel.v_WindYieldForecast_fr;
			//traceln("Forecast-based SOC setpoint: " + SOC_setp_fr + " %");
		}
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
		} else if (WTP_discharge_eurpkWh < currentElectricityPriceDischarge_eurpkWh) {
			//chargeSetpoint_kW = -min(p_batteryAsset.getElectricCapacity_kW()*(currentElectricityPriceDischarge_eurpkWh - WTP_discharge_eurpkWh)*priceGain, currentPricePowerBandPos_kW);
			chargeSetpoint_kW = -p_batteryAsset.getCapacityElectric_kW()*(currentElectricityPriceDischarge_eurpkWh - WTP_discharge_eurpkWh)*priceGain_kWhpeur;
			//traceln("WTP discharge " + WTP_discharge_eurpkWh + " eurpkWh is lower than electricity price " + currentElectricityPriceCharge_eurpkWh + " eurpkWh, so discharge!") ;
		}	
		
		// limit charging power to available connection capacity
		boolean b_stayWithinConnectionLimits = true;
		if( b_stayWithinConnectionLimits ) {		
			double electricitySurplus_kW = - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); 
			
			double availableChargePower_kW = electricitySurplus_kW + p_contractedDeliveryCapacity_kW; // Max battery charging power within grid capacity
			double availableDischargePower_kW = electricitySurplus_kW - p_contractedFeedinCapacity_kW; // Max discharging power within grid capacity
			chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
		}			
	
		p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	}
}

/*ALCODEEND*/}

double f_manageHeatingAssets()
{/*ALCODESTART::1669025846794*/
// TODO: This only works for fixed heat demands; also need to implement heating of a building modeled as a ThermalStorageAsset! [GH 21/11/2022]
double powerDemand_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

if ( p_BuildingThermalAsset == null ) {
	if ( p_secondaryHeatingAsset == null ) { // Just one heating asset
		if ( p_primaryHeatingAsset== null ) {
			if (powerDemand_kW > 0) {
				traceln("No heating assets for GridConnection " + p_gridConnectionID);
			}
		} else {
			if ( p_primaryHeatingAsset instanceof J_EAConversionGasBurner || p_primaryHeatingAsset instanceof J_EAConversionHeatDeliverySet || p_primaryHeatingAsset instanceof J_EAConversionHydrogenBurner || p_primaryHeatingAsset instanceof J_EAConversionHeatPump) { // when there is only a gas burner or DH set
					p_primaryHeatingAsset.v_powerFraction_fr = min(1,powerDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW());
					//traceln("Running manageHeatingAsset for single heating asset");
			} else {
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

double f_setAllowedCapacity()
{/*ALCODESTART::1669193537955*/
v_allowedCapacity_kW = p_connectionCapacity_kW;
//if(p_nfatoLvl_kW == 0 && p_nfatoStart_h == 0 && p_nfatoEnd_h == 0){
if(!v_enable_nfATO_b){
	e_startNonFirmATO.reset();
	e_endNonFirmATO.reset();			
}
else {
	e_startNonFirmATO.restartTo(p_nfatoStart_h, HOUR);
	e_endNonFirmATO.restartTo(p_nfatoEnd_h, HOUR);
	if(p_nfatoLvl_kW == 0.0) { p_nfatoLvl_kW = p_connectionCapacity_kW; }
}
//}
/*ALCODEEND*/}

double f_manageCharging()
{/*ALCODESTART::1671095995172*/
double availableCapacityFromBatteries = p_batteryAsset == null ? 0 : p_batteryAsset.getCapacityAvailable_kW(); 
//double availableChargingCapacity = v_allowedCapacity_kW + availableCapacityFromBatteries - v_currentPowerElectricity_kW;
double availableChargingCapacity = p_contractedDeliveryCapacity_kW + availableCapacityFromBatteries - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
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
		v_currentElectricityPriceConsumption_eurpkWh = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getElectricityPrice(p_contractedDeliveryCapacity_kW); 
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( v_currentElectricityPriceConsumption_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		f_chargeOnPrice( v_currentElectricityPriceConsumption_eurpkWh, max(0, availableChargingCapacity));
	break;
	case V2G:
		v_currentElectricityPriceConsumption_eurpkWh = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getElectricityPrice(p_contractedDeliveryCapacity_kW); 
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
		if( !ev.getAvailability() || ev.getCurrentStateOfCharge() == 1 ) {
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
		if(!ev.getAvailability() || ev.getCurrentStateOfCharge() == 1) {
			ev.f_updateAllFlows( 0.0 );
			c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
			countDeletedItems ++;
		}
		else {
			double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getStorageCapacity_kWh()*ev.getCurrentStateOfCharge());
			double maxChargingPower_kW = ev.getCapacityElectric_kW();
			double chargeDeadline_h = floor((ev.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * energyModel.p_timeStep_h;
			
			double emptyKWhInBattery = ev.getStorageCapacity_kWh() * (1 - ev.getCurrentStateOfCharge());
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
		if( !ev.getAvailability() || ev.getCurrentStateOfCharge() == 1 ) {
			ev.f_updateAllFlows( 0.0 );
			c_vehiclesAvailableForCharging.remove( i - countDeletedItems );
			countDeletedItems ++;
		}
		else {
			//traceln("current time: " + energyModel.t_h);
			//traceln("ev: " + ev);
			//traceln("dist: " + ev.getTripTracker().v_tripDist_km);
			double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getStorageCapacity_kWh()*ev.getCurrentStateOfCharge());
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
		double chargeNeedForNextTrip_kWh = max(0, vehicle.energyNeedForNextTrip_kWh - vehicle.getStorageCapacity_kWh()*vehicle.getCurrentStateOfCharge());
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
		} else if ( vehicle.getCurrentStateOfCharge() < 0.15 ) {
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
	((GCHouse)this).v_vehicleSOC_fr = vehicle.getCurrentStateOfCharge();
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
for (OL_EnergyCarriers EC : j_ea.getActiveEnergyCarriers()) {
	if (!v_activeEnergyCarriers.contains(EC)) {
		v_activeEnergyCarriers.add(EC);
		
		if (energyModel.b_isInitialized) {
			energyModel.f_addEnergyCarrier(EC);
			//energyModel.v_activeEnergyCarriers.add(EC);
			DataSet dsDemand = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
			DataSet dsSupply = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
			double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
			double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
			for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
				dsDemand.add( t, 0);
				dsSupply.add( t, 0);
			}
			v_liveData.dsm_liveDemand_kW.put( EC, dsDemand);
			v_liveData.dsm_liveSupply_kW.put( EC, dsSupply);
			/*dsm_dailyAverageDemandDataSets_kW.put( EC, new DataSet(365));
			dsm_dailyAverageSupplyDataSets_kW.put( EC, new DataSet(365));
			dsm_summerWeekDemandDataSets_kW.put( EC, new DataSet( (int)(168 / energyModel.p_timeStep_h)));
			dsm_summerWeekSupplyDataSets_kW.put( EC, new DataSet( (int)(168 / energyModel.p_timeStep_h)));
			dsm_winterWeekDemandDataSets_kW.put( EC, new DataSet( (int)(168 / energyModel.p_timeStep_h)));
			dsm_winterWeekSupplyDataSets_kW.put( EC, new DataSet( (int)(168 / energyModel.p_timeStep_h)));
			*/
		}
	}
}

//Production EC
for(OL_EnergyCarriers EC_production : j_ea.getActiveProductionEnergyCarriers()){
	v_activeProductionEnergyCarriers.add(EC_production);
}

//Consumption EC
for(OL_EnergyCarriers EC_consumption : j_ea.getActiveConsumptionEnergyCarriers()){
	v_activeConsumptionEnergyCarriers.add(EC_consumption);
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
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsExcel, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, vehicle);
		} else if (vehicle.energyAssetType == OL_EnergyAssetType.DIESEL_VAN || vehicle.energyAssetType == OL_EnergyAssetType.ELECTRIC_VAN || vehicle.energyAssetType == OL_EnergyAssetType.HYDROGEN_VAN) {// No mobility pattern for business vans available yet!! Falling back to truck mobility pattern
			int rowIndex = uniform_discr(1, 7);//getIndex() % 200;	
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_truckTripsExcel, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, vehicle);
			tripTracker.setAnnualDistance_km(30_000);
		} else {
			//traceln("Adding passenger vehicle to gridconnection %s", this);
			int rowIndex = uniform_discr(0, 200);//getIndex() % 200;
			tripTracker = new J_ActivityTrackerTrips(energyModel, energyModel.p_householdTripsExcel, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, vehicle);
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
		v_hasPV = true;
		v_liveAssetsMetaData.totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if (l_parentNodeElectric.getConnectedAgent() != null) {
			l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, ((J_EAProduction)j_ea).getCapacityElectric_kW(), true);
		}
		energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		c_pvAssets.add(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.WINDMILL) {
		v_liveAssetsMetaData.totalInstalledWindPower_kW += ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if (l_parentNodeElectric.getConnectedAgent() != null) {
			l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, ((J_EAProduction)j_ea).getCapacityElectric_kW(), true);
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
			p_cookingTracker = new J_ActivityTrackerCooking(energyModel.p_cookingPatternExcel, rowIndex, (energyModel.t_h-energyModel.p_runStartTime_h)*60, (J_EAConversion)j_ea );			
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
		energyModel.c_ambientAirDependentAssets.add(j_ea);
		c_electricHeatpumpAssets.add(j_ea);
		//c_conversionElectricAssets.add(j_ea);
		//traceln("added heatpump to the GC as primary heating asset.");

		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionHydrogenBurner) {
		p_primaryHeatingAsset = (J_EAConversion)j_ea;
	} else if (j_ea instanceof J_EAConversionElectrolyser || j_ea instanceof J_EAConversionElektrolyser) {
		c_electrolyserAssets.add(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.CHP) {
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
		} else if( ((J_EAProfile)j_ea).profileType == OL_ProfileAssetType.METHANEDEMAND){
			//Do nothing
		} else {
			traceln( "Unrecognized profile type!");
		}
} else if (j_ea instanceof J_EADieselTractor) {
	c_profileAssets.add((J_EAProfile)j_ea);
} else {
	traceln("Unrecognized energy asset %s in gridconnection %s", j_ea, this);
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
		double chargeNeedForNextTrip_kWh = vehicle.energyNeedForNextTrip_kWh - vehicle.getStorageCapacity_kWh()*vehicle.getCurrentStateOfCharge();
		//double timeToNexTrip_min = vehicle.getMobilityTracker().v_nextTripStartTime_min - energyModel.t_h*60;
		double maxChargingPower_kW = vehicle.getCapacityElectric_kW();
		double timeToNextTrip_min = vehicle.tripTracker.v_nextEventStartTime_min - energyModel.t_h*60;
		double chargeDeadline_min = floor((vehicle.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * 60 * energyModel.p_timeStep_h;

		double priceGain_kWhpeur = 1; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
		double urgencyGain_eurpkWh = 0.4; // How strongly WTP-price shifts based on charging flexibility
		double maxSpreadChargingPower_kW = min(chargeNeedForNextTrip_kWh / (max(1, timeToNextTrip_min - v_additionalTimeSpreadCharging_MIN) / 60), maxChargingPower_kW);
		//traceln("maxSpreadChargingPower_kW" + maxSpreadChargingPower_kW);
		double WTPoffset_eurpkWh = 0;
		if (energyModel.v_totalInstalledWindPower_kW > 499) {
			WTPoffset_eurpkWh = 0.05*(1-energyModel.v_WindYieldForecast_fr);//0.15; // Adds an offset to the WTP price; this value is very much context specific, depending on market conditions during charging periods
		} else {
			WTPoffset_eurpkWh = 0.02;
		}
		double V2G_WTR_offset_eurpkWh = 0.05;
		double chargeSetpoint_kW = 0;

		if ( energyModel.t_h*60 >= chargeDeadline_min & chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
			//traceln("Urgency charging! May exceed connection capacity!");
			chargeSetpoint_kW = maxChargingPower_kW;				
		} else if ( vehicle.getCurrentStateOfCharge() < 0.15 ) {
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
		((GCHouse)this).v_vehicleSOC_fr = vehicle.getCurrentStateOfCharge();
	}
}


/*ALCODEEND*/}

double f_initialize()
{/*ALCODESTART::1698854861644*/
if (p_physicalConnectionCapacity_kW < 0) {
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has negative physical connection capacity!");
} else if (p_contractedDeliveryCapacity_kW < 0) {
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has negative contracted delivery capacity!");
} else if (p_contractedFeedinCapacity_kW < 0) {
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has negative contracted feed in capacity!");
}

if(v_isActive){
	if (p_contractedDeliveryCapacity_kW == 0.0 && p_contractedFeedinCapacity_kW == 0.0 && p_physicalConnectionCapacity_kW == 0.0) { // If no contracted or physical capacity is given, throw error.
	throw new RuntimeException("Exception: GridConnection " + p_gridConnectionID + " has 0.0 physical and contracted capacity! Not a valid state of for this agent");
	} else {
		if (p_contractedDeliveryCapacity_kW == 0.0 && p_contractedFeedinCapacity_kW == 0.0) { // If no contracted capacity is given, use physical capacity
		p_contractedDeliveryCapacity_kW = p_physicalConnectionCapacity_kW;
		p_contractedFeedinCapacity_kW = p_physicalConnectionCapacity_kW;
	} else if ( p_physicalConnectionCapacity_kW == 0 ) { // if no physical capacity is given, use max of delivery and feedin contracted capacities
			p_physicalConnectionCapacity_kW = max(p_contractedDeliveryCapacity_kW, p_contractedFeedinCapacity_kW);
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
if (!l_parentNodeElectric.isConnected()) {
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
	l_parentNodeElectric.getConnectedAgent().v_totalInstalledPVPower_kW += PV_kW;
	l_parentNodeElectric.getConnectedAgent().v_totalInstalledWindPower_kW += Wind_kW;
}

f_setOperatingSwitches();
//v_rapidRunData.initializeAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();
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

// Initializing Live Data Class
v_liveData = new J_LiveData(this);
v_liveAssetsMetaData = new J_AssetsMetaData(this);
v_liveData.assetsMetaData = v_liveAssetsMetaData;
v_liveAssetsMetaData.updateActiveAssetData(new ArrayList<>(List.of(this)));
/*ALCODEEND*/}

double f_calculateKPIs()
{/*ALCODESTART::1701956274181*/
//========== TOTALS ==========//
// Get import / export from balance accumulators.
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_totalImports_MWh.put( EC, am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 );
	fm_totalExports_MWh.put( EC, -am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 );
}

// Sum up the import / export totals
v_totalEnergyImport_MWh = fm_totalImports_MWh.totalSum();
v_totalEnergyExport_MWh = fm_totalExports_MWh.totalSum();

// Electricity totals from production / consumption accumulators and selfconsumption
v_totalElectricityConsumed_MWh = am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_totalElectricityProduced_MWh=  am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_totalElectricitySelfConsumed_MWh = max(0, v_totalElectricityConsumed_MWh - fm_totalImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

// Energy totals from production / consumption accumulators and selfconsumption
v_totalEnergyProduced_MWh = acc_dailyAverageEnergyProduction_kW.getIntegral_kWh() / 1000;
v_totalEnergyConsumed_MWh = acc_dailyAverageEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_totalEnergySelfConsumed_MWh = max(0, v_totalEnergyConsumed_MWh - v_totalEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

// Other totals from accumulators
v_totalPrimaryEnergyProductionHeatpumps_MWh = acc_totalPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
v_totalEnergyCurtailed_MWh = acc_totalEnergyCurtailed_kW.getIntegral_kWh() / 1000;

// Total overload duration
for (double electricityBalance_kW : am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()) {
	
	//Duration
	if(electricityBalance_kW > p_contractedDeliveryCapacity_kW){
		v_totalOverloadDurationDelivery_hr += energyModel.p_timeStep_h;
	}
	else if(electricityBalance_kW < - p_contractedFeedinCapacity_kW){
		v_totalOverloadDurationFeedin_hr += energyModel.p_timeStep_h;
	}
	
	//Max Peaks
	if(electricityBalance_kW > v_maxPeakDelivery_kW){
		v_maxPeakDelivery_kW = electricityBalance_kW;
	}
	else if(electricityBalance_kW < - v_maxPeakFeedin_kW){
		v_maxPeakFeedin_kW = abs(electricityBalance_kW);
	}
}

//========== SUMMER WEEK ==========//
// Summerweek KPIs are calculated from ZeroAccumulators
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_summerWeekImports_MWh.put( EC, am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 );
	fm_summerWeekExports_MWh.put( EC, -am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 );
}

v_summerWeekEnergyImport_MWh = fm_summerWeekImports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();
v_summerWeekEnergyExport_MWh = fm_summerWeekExports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();

v_summerWeekElectricityProduced_MWh = am_summerWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_summerWeekElectricityConsumed_MWh = am_summerWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_summerWeekElectricitySelfConsumed_MWh = max(0, v_summerWeekElectricityConsumed_MWh - fm_summerWeekImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_summerWeekEnergyConsumed_MWh = acc_summerWeekEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_summerWeekEnergyProduced_MWh = acc_summerWeekEnergyProduction_kW.getIntegral_kWh() / 1000;
v_summerWeekEnergySelfConsumed_MWh = max(0, v_summerWeekEnergyConsumed_MWh - v_summerWeekEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

double v_summerWeekSelfConsumedEnergyCheck_MWh = v_summerWeekEnergyProduced_MWh - v_summerWeekEnergyExport_MWh;

v_summerWeekPrimaryEnergyProductionHeatpumps_MWh = acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
v_summerWeekEnergyCurtailed_MWh = acc_summerWeekEnergyCurtailed_kW.getIntegral_kWh() / 1000;

/*if (abs(v_summerWeekEnergySelfConsumed_MWh - v_summerWeekSelfConsumedEnergyCheck_MWh) > 0.01) {
	throw new RuntimeException("SelfConsumedEnergy Check for Summer Week Failed for GC: " + this.p_ownerID);
}*/

//========== WINTER WEEK ==========//
// Winterweek KPIs are calculated from ZeroAccumulators
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_winterWeekImports_MWh.put( EC, am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 );
	fm_winterWeekExports_MWh.put( EC, -am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 );
}

v_winterWeekEnergyImport_MWh = fm_winterWeekImports_MWh.totalSum();
v_winterWeekEnergyExport_MWh = fm_winterWeekExports_MWh.totalSum();

v_winterWeekElectricityProduced_MWh = am_winterWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_winterWeekElectricityConsumed_MWh = am_winterWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_winterWeekElectricitySelfConsumed_MWh = max(0,v_winterWeekElectricityConsumed_MWh - fm_winterWeekImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_winterWeekEnergyConsumed_MWh = acc_winterWeekEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_winterWeekEnergyProduced_MWh = acc_winterWeekEnergyProduction_kW.getIntegral_kWh() / 1000;
v_winterWeekEnergySelfConsumed_MWh = max(0,v_winterWeekEnergyConsumed_MWh - v_winterWeekEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

double v_winterWeekSelfConsumedEnergyCheck_MWh = v_winterWeekEnergyProduced_MWh - v_winterWeekEnergyExport_MWh;

v_winterWeekPrimaryEnergyProductionHeatpumps_MWh = acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
v_winterWeekEnergyCurtailed_MWh = acc_winterWeekEnergyCurtailed_kW.getIntegral_kWh() / 1000;


//========== DAYTIME ==========//
// Daytime KPIs are calculated from ZeroAccumulators
for (var EC : v_activeEnergyCarriers){
	fm_daytimeImports_MWh.addFlow(EC, am_daytimeImports_kW.get(EC).getIntegral_kWh() / 1000);
	fm_daytimeExports_MWh.addFlow(EC, am_daytimeExports_kW.get(EC).getIntegral_kWh() / 1000);
}
v_daytimeEnergyImport_MWh = fm_daytimeImports_MWh.totalSum();
v_daytimeEnergyExport_MWh = fm_daytimeExports_MWh.totalSum();

v_daytimeElectricityProduced_MWh = acc_daytimeElectricityProduction_kW.getIntegral_kWh() / 1000;
v_daytimeElectricityConsumed_MWh =  acc_daytimeElectricityConsumption_kW.getIntegral_kWh() / 1000;

v_daytimeEnergyProduced_MWh = acc_daytimeEnergyProduction_kW.getIntegral_kWh() / 1000;
v_daytimeEnergyConsumed_MWh = acc_daytimeEnergyConsumption_kW.getIntegral_kWh() / 1000;

v_daytimeEnergySelfConsumed_MWh = max(0, v_daytimeEnergyProduced_MWh - v_daytimeEnergyExport_MWh);
v_daytimeElectricitySelfConsumed_MWh = max(0, v_daytimeElectricityConsumed_MWh - am_daytimeImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000);


//========== NIGHTTIME ==========//
// Nighttime KPIs are calculated by total - daytime
v_nighttimeEnergyExport_MWh = v_totalEnergyExport_MWh - v_daytimeEnergyExport_MWh;
v_nighttimeEnergyImport_MWh = v_totalEnergyImport_MWh - v_daytimeEnergyImport_MWh;
v_nighttimeEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_daytimeEnergyConsumed_MWh;
v_nighttimeEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_daytimeEnergyProduced_MWh;

v_nighttimeElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_daytimeElectricityConsumed_MWh;
v_nighttimeElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_daytimeElectricityProduced_MWh;

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_nighttimeImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - am_daytimeImports_kW.get(EC).getIntegral_kWh() / 1000 );
	fm_nighttimeExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - am_daytimeExports_kW.get(EC).getIntegral_kWh() / 1000 );
}

v_nighttimeEnergySelfConsumed_MWh = max(0, v_nighttimeEnergyProduced_MWh - v_nighttimeEnergyExport_MWh);
v_nighttimeElectricitySelfConsumed_MWh = max(0,v_nighttimeElectricityConsumed_MWh - fm_nighttimeImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

//========== WEEKEND ==========//
// Weekend KPIs are calculated from ZeroAccumulators
for (var EC : v_activeEnergyCarriers){
	fm_weekendImports_MWh.addFlow(EC, am_weekendImports_kW.get(EC).getIntegral_kWh() / 1000);
	fm_weekendExports_MWh.addFlow(EC, am_weekendExports_kW.get(EC).getIntegral_kWh() / 1000);
}
v_weekendEnergyImport_MWh = fm_weekendImports_MWh.totalSum();
v_weekendEnergyExport_MWh = fm_weekendExports_MWh.totalSum();

v_weekendElectricityProduced_MWh = acc_weekendElectricityProduction_kW.getIntegral_kWh() / 1000;
v_weekendElectricityConsumed_MWh =  acc_weekendElectricityConsumption_kW.getIntegral_kWh() / 1000;

v_weekendEnergyProduced_MWh = acc_weekendEnergyProduction_kW.getIntegral_kWh() / 1000;
v_weekendEnergyConsumed_MWh =  acc_weekendEnergyConsumption_kW.getIntegral_kWh() / 1000;

v_weekendEnergySelfConsumed_MWh = max(0, v_weekendEnergyProduced_MWh - v_weekendEnergyExport_MWh);
v_weekendElectricitySelfConsumed_MWh = max(0, v_weekendElectricityConsumed_MWh - am_weekendImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000);

//========== WEEKDAY ==========//
// Weekday KPIs are calculated by total - weekend
v_weekdayEnergyExport_MWh = v_totalEnergyExport_MWh - v_weekendEnergyExport_MWh;
v_weekdayEnergyImport_MWh = v_totalEnergyImport_MWh - v_weekendEnergyImport_MWh;
v_weekdayEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_weekendEnergyConsumed_MWh;
v_weekdayEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_weekendEnergyProduced_MWh;
v_weekdayEnergySelfConsumed_MWh = max(0, v_weekendEnergyProduced_MWh - v_weekendEnergyExport_MWh);

v_weekdayElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_weekendElectricityConsumed_MWh;
v_weekdayElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_weekendElectricityProduced_MWh;

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_weekdayImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - am_weekendImports_kW.get(EC).getIntegral_kWh() / 1000 );
	fm_weekdayExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - am_weekendExports_kW.get(EC).getIntegral_kWh() / 1000 );
}

v_weekdayEnergySelfConsumed_MWh = max(0, v_weekdayEnergyProduced_MWh - v_weekdayEnergyExport_MWh);
v_weekdayElectricitySelfConsumed_MWh = max(0,v_weekdayElectricityConsumed_MWh - fm_weekdayImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

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

DataSet f_getDuurkromme()
{/*ALCODESTART::1708520215192*/
J_LoadDurationCurves j_duurkrommes = new J_LoadDurationCurves(v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW(), energyModel);

data_netbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveTotal_kW;
data_summerWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveSummer_kW;
data_winterWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWinter_kW;
data_daytimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveDaytime_kW;
data_nighttimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveNighttime_kW;
data_weekdayNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekday_kW;
data_weekendNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekend_kW;
 
return data_netbelastingDuurkromme_kW;
/*
boolean firstRun = true;
if (data_netbelastingDuurkromme_kW != null) {	
	if (data_netbelastingDuurkrommeVorige_kW != null) { // Not second run either!
		data_netbelastingDuurkrommeVorige_kW.reset();
	} else {
		data_netbelastingDuurkrommeVorige_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));
	}
	firstRun = false;
} else {
	data_netbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));
	data_summerWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	data_winterWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	data_daytimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/2/energyModel.p_timeStep_h));
	data_nighttimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/2/energyModel.p_timeStep_h));
	data_weekdayNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/7*5/energyModel.p_timeStep_h)+100);
	data_weekendNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/7*2/energyModel.p_timeStep_h)+100);
}

// We copy our annual array to preserve it as a time-series and make new arrays for the others
double[] netLoadArrayAnnual_kW = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries().clone(); 
int arraySize = netLoadArrayAnnual_kW.length;
double[] netLoadArraySummerweek_kW = new double[roundToInt(168 / energyModel.p_timeStep_h)];
double[] netLoadArrayWinterweek_kW= new double[roundToInt(168 / energyModel.p_timeStep_h)];
double[] netLoadArrayDaytime_kW = new double[arraySize/2];
double[] netLoadArrayNighttime_kW = new double[arraySize/2];
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

for(int i=0; i<arraySize ; i++) {
	if (!firstRun) {
		// First we make sure to store our previous Load Curve
		data_netbelastingDuurkrommeVorige_kW.add(i*energyModel.p_timeStep_h,data_netbelastingDuurkromme_kW.getY(i));		
	}
	// summer/winter
	if (energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h > energyModel.p_startHourSummerWeek && energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h<= energyModel.p_startHourSummerWeek+24*7) {
		netLoadArraySummerweek_kW[i_summer]=-netLoadArrayAnnual_kW[i];
		i_summer++;
	}
	if (energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h > energyModel.p_startHourWinterWeek && energyModel.p_runStartTime_h + i*energyModel.p_timeStep_h<= energyModel.p_startHourWinterWeek+24*7) {
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
 
// Now we have the size of the weekday & weekend arrays.
double[] netLoadArrayWeekday_kW = new double[listNetLoadArrayWeekday_kW.size()];
double[] netLoadArrayWeekend_kW = new double[listNetLoadArrayWeekend_kW.size()];
for (int i = 0; i < listNetLoadArrayWeekday_kW.size(); i++) {
	netLoadArrayWeekday_kW[i] = listNetLoadArrayWeekday_kW.get(i);
}
for (int i = 0; i < listNetLoadArrayWeekend_kW.size(); i++) {
	netLoadArrayWeekend_kW[i] = listNetLoadArrayWeekend_kW.get(i);
}
 
 
// Sort all arrays
Arrays.parallelSort(netLoadArrayAnnual_kW);
Arrays.parallelSort(netLoadArraySummerweek_kW);
Arrays.parallelSort(netLoadArrayWinterweek_kW);
Arrays.parallelSort(netLoadArrayDaytime_kW);
Arrays.parallelSort(netLoadArrayNighttime_kW);
Arrays.parallelSort(netLoadArrayWeekday_kW);
Arrays.parallelSort(netLoadArrayWeekend_kW);
 
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
for(int i=0; i< arraySize; i++) {
	data_netbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, netLoadArrayAnnual_kW[arraySize-i-1]);
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
 
return data_netbelastingDuurkromme_kW;

*/
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
			v_hasPV = false;
		}
		v_liveAssetsMetaData.totalInstalledPVPower_kW -= ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if (l_parentNodeElectric.getConnectedAgent() != null) {
			l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, ((J_EAProduction)j_ea).getCapacityElectric_kW(), false);
		}
		energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW -= ((J_EAProduction)j_ea).getCapacityElectric_kW();
		c_pvAssets.remove(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.WINDMILL) {
		v_liveAssetsMetaData.totalInstalledWindPower_kW -= ((J_EAProduction)j_ea).getCapacityElectric_kW();
		if (l_parentNodeElectric.getConnectedAgent() != null) {
			l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, ((J_EAProduction)j_ea).getCapacityElectric_kW(), false);
		}
		energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW -= ((J_EAProduction)j_ea).getCapacityElectric_kW();
		c_windAssets.remove(j_ea);
	}
} else if (j_ea instanceof J_EAConversion) {
	c_conversionAssets.remove((J_EAConversion)j_ea);
	if (j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB) {
		c_electricHobAssets.remove(j_ea);
		//c_conversionElectricAssets.remove(j_ea);
	}	
	if ( j_ea.energyAssetType == OL_EnergyAssetType.GAS_PIT | j_ea.energyAssetType == OL_EnergyAssetType.ELECTRIC_HOB){
		p_cookingTracker = null;
	} else if (j_ea instanceof J_EAConversionGasBurner) {
		if(p_heatingType == OL_GridConnectionHeatingType.HYBRID_HEATPUMP)
			p_secondaryHeatingAsset = null;
		else{
			p_primaryHeatingAsset = null;
		}
	} else if (j_ea instanceof J_EAConversionHeatPump) {
		p_primaryHeatingAsset = null;
		energyModel.c_ambientAirDependentAssets.remove(j_ea);
		c_electricHeatpumpAssets.remove(j_ea);
		//c_conversionElectricAssets.remove(j_ea);
	} else if (j_ea instanceof J_EAConversionHydrogenBurner) {
		p_primaryHeatingAsset = null;
	} else if (j_ea instanceof J_EAConversionElectrolyser) {
		c_electrolyserAssets.remove(j_ea);
	}
	else if (j_ea.energyAssetType == OL_EnergyAssetType.CHP) {
		c_chpAssets.remove(j_ea);
	}
} else if  (j_ea instanceof J_EAStorage) {
	c_storageAssets.remove((J_EAStorage)j_ea);
	energyModel.c_storageAssets.remove((J_EAStorage)j_ea);
	if (j_ea.energyAssetType == OL_EnergyAssetType.BUILDINGTHERMALS) {
		energyModel.c_ambientAirDependentAssets.remove(j_ea);
		p_BuildingThermalAsset = null;
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
} else {
	traceln("Unrecognized energy asset %s in gridconnection %s", j_ea, this);
}

/*ALCODEEND*/}

double f_initializeAccumulators()
{/*ALCODESTART::1716282675260*/
//========== TOTAL ACCUMULATORS ==========//
am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h );
am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, energyModel.p_timeStep_h, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) );
am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(v_activeConsumptionEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(v_activeProductionEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

acc_dailyAverageEnergyProduction_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageEnergyConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

acc_totalEnergyCurtailed_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_totalPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

acc_dailyAverageBaseloadElectricityConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageHeatPumpElectricityConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageElectricVehicleConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageBatteriesConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageElectricCookingConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageElectrolyserElectricityConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageDistrictHeatingConsumption_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

acc_dailyAveragePVProduction_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageWindProduction_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageV2GProduction_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageBatteriesProduction_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_dailyAverageCHPElectricityProduction_kW = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

//acc_dailyAverageBatteriesStoredEnergy_MWh = new ZeroAccumulator(true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

//========== SUMMER WEEK ACCUMULATORS ==========//
am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 168.0);
am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 168.0);
am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 168.0);

acc_summerWeekEnergyProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekEnergyConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_summerWeekEnergyCurtailed_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_summerWeekFeedinCapacity_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekDeliveryCapacity_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_summerWeekBaseloadElectricityConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekHeatPumpElectricityConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekElectricVehicleConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekBatteriesConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekElectricCookingConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekElectrolyserElectricityConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekDistrictHeatingConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_summerWeekPVProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekWindProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekV2GProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekBatteriesProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_summerWeekCHPElectricityProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

//acc_summerWeekBatteriesStoredEnergy_MWh = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

//========== WINTER WEEK ACCUMULATORS ==========//
am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 168.0);
am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 168.0);
am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 168.0);

acc_winterWeekEnergyProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekEnergyConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_winterWeekEnergyCurtailed_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_winterWeekFeedinCapacity_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekDeliveryCapacity_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_winterWeekBaseloadElectricityConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekHeatPumpElectricityConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekElectricVehicleConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekBatteriesConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekElectricCookingConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekElectrolyserElectricityConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekDistrictHeatingConsumption_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

acc_winterWeekPVProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekWindProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekV2GProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekBatteriesProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);
acc_winterWeekCHPElectricityProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

//acc_winterWeekBatteriesStoredEnergy_MWh = new ZeroAccumulator(true, energyModel.p_timeStep_h, 168.0);

//========== DAYTIME ACCUMULATORS ==========//
am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));
am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));

acc_daytimeEnergyProduction_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));
acc_daytimeEnergyConsumption_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h,0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));
//acc_daytimeEnergyCurtailed_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_daytimeElectricityProduction_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));
acc_daytimeElectricityConsumption_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, 0.5 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h));

//========== WEEKEND ACCUMULATORS ==========//
am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 2 / 7  * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);
am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, 2 / 7 * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);

acc_weekendEnergyProduction_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, 2 / 7  * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);
acc_weekendEnergyConsumption_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h,2 / 7  * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);
//acc_weekendEnergyCurtailed_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
acc_weekendElectricityProduction_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, 2 / 7  * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);
acc_weekendElectricityConsumption_kW = new ZeroAccumulator(false, energyModel.p_timeStep_h, 2 / 7  * (energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) + 48);

/*ALCODEEND*/}

double f_resetSpecificGCStates()
{/*ALCODESTART::1717060111619*/

/*ALCODEEND*/}

double f_resetStatesAfterRapidRun()
{/*ALCODESTART::1717068094093*/
//Reset dataset after rapid run

//v_hydrogenInStorage_kWh = 0;

//Reset specificGC states after rapid run
f_resetSpecificGCStatesAfterRapidRun();





/*ALCODEEND*/}

double f_resetSpecificGCStatesAfterRapidRun()
{/*ALCODESTART::1717068167776*/
// to be overwritten by child GCs!
/*ALCODEEND*/}

double f_curtailment()
{/*ALCODESTART::1720442672576*/
switch(p_curtailmentMode) {
	case CAPACITY:
	// Keep feedin power within connection capacity
	if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < - p_contractedFeedinCapacity_kW) { // overproduction!
		for (J_EAProduction j_ea : c_productionAssets) {
			j_ea.curtailElectricityProduction( - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - p_contractedFeedinCapacity_kW);
			if (!(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < - p_contractedFeedinCapacity_kW)) {
				break;
			}
		}
	}
	break;
	case PRICE:
	// Prevent feedin when nodal price is negative
	double priceTreshold_eur = -0.0;
	if(l_parentNodeElectric.getConnectedAgent().v_currentTotalNodalPrice_eurpkWh < priceTreshold_eur) {
	
		double v_currentPowerElectricitySetpoint_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * max(0,1+(l_parentNodeElectric.getConnectedAgent().v_currentTotalNodalPrice_eurpkWh-priceTreshold_eur)*5);
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
			p_contractedDeliveryCapacity_kW += v_nfatoWeekendDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekDeliveryCapacity_kW[previousHour];
			p_contractedFeedinCapacity_kW += v_nfatoWeekendFeedinCapacity_kW[hourOfDay] - v_nfatoWeekFeedinCapacity_kW[previousHour];
		}
		else {
			p_contractedDeliveryCapacity_kW += v_nfatoWeekendDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekendDeliveryCapacity_kW[previousHour];
			p_contractedFeedinCapacity_kW += v_nfatoWeekendFeedinCapacity_kW[hourOfDay] - v_nfatoWeekendFeedinCapacity_kW[previousHour];
		}
	}
	else {
		if (dayOfWeek == 1 && hourOfDay == 0) { // Sunday night we need to subtract the previous weekend capacity
			p_contractedDeliveryCapacity_kW += v_nfatoWeekDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekendDeliveryCapacity_kW[previousHour];
			p_contractedFeedinCapacity_kW += v_nfatoWeekFeedinCapacity_kW[hourOfDay] - v_nfatoWeekendFeedinCapacity_kW[previousHour];
		}
		else {
			p_contractedDeliveryCapacity_kW += v_nfatoWeekDeliveryCapacity_kW[hourOfDay] - v_nfatoWeekDeliveryCapacity_kW[previousHour];
			p_contractedFeedinCapacity_kW += v_nfatoWeekFeedinCapacity_kW[hourOfDay] - v_nfatoWeekFeedinCapacity_kW[previousHour];
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
		p_contractedDeliveryCapacity_kW += mult * v_nfatoWeekDeliveryCapacity_kW[hourOfDay];
		p_contractedFeedinCapacity_kW += mult * v_nfatoWeekFeedinCapacity_kW[hourOfDay];
	}
	else {
		p_contractedDeliveryCapacity_kW += mult * v_nfatoWeekendDeliveryCapacity_kW[hourOfDay];
		p_contractedFeedinCapacity_kW += mult * v_nfatoWeekendFeedinCapacity_kW[hourOfDay];
	}
}
else {
	if (dayOfWeek == 1 && hourOfDay == 0) { // Sunday night we need to subtract the previous week capacity
		p_contractedDeliveryCapacity_kW += mult * v_nfatoWeekendDeliveryCapacity_kW[hourOfDay];
		p_contractedFeedinCapacity_kW += mult * v_nfatoWeekendFeedinCapacity_kW[hourOfDay];
	}
	else {
		p_contractedDeliveryCapacity_kW += mult * v_nfatoWeekDeliveryCapacity_kW[hourOfDay];
		p_contractedFeedinCapacity_kW += mult * v_nfatoWeekFeedinCapacity_kW[hourOfDay];
	}
}
/*ALCODEEND*/}

double f_batteryManagementNodalPricing(double currentBatteryStateOfCharge_fr)
{/*ALCODESTART::1720537137235*/
if (p_batteryAsset.getStorageCapacity_kWh() != 0){
	//double willingnessToPayDefault_eurpkWh = 0.3;
	double WTPfeedbackGain_eurpSOC = 0.2; // When SOC-error is 100%, adjust WTP price by 1 eurpkWh
	double priceGain_kWhpeur = 1.0; // How strongly to ramp up power with price-delta's. Increasing this gain too far leads to instability!
	//double congestionTariffCoop_eurpkWh = -(((ConnectionOwner)p_ownerActor).p_CoopParent.v_electricitySurplus_kW + v_previousPowerElectricity_kW)/1200*0.1;
	
	double chargeSetpoint_kW = 0;	
	double currentElectricityPriceCharge_eurpkWh;
	GridNode GN = l_parentNodeElectric.getConnectedAgent();
	//double currentElectricityPriceDischarge_eurpkWh;
	//currentElectricityPriceCharge_eurpkWh = energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000 + GN.v_currentTotalNodalPrice_eurpkWh;
	currentElectricityPriceCharge_eurpkWh = GN.v_currentTotalNodalPrice_eurpkWh;
	
	v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );

	
	double SOC_setp_fr = 0.9 + (GN.v_totalInstalledPVPower_kW/50_000+GN.v_totalInstalledWindPower_kW/20_000)*(0.2 - 3*GN.v_electricityYieldForecast_fr);	
	//double SOC_setp_fr = 0.9 - 2*energyModel.v_WindYieldForecast_fr;	
	//SOC_setp_fr = (0.5 + 0.4 * Math.cos(2*Math.PI*(energyModel.t_h-18)/24))*(1-3*GN.v_electricityYieldForecast_fr); // Sinusoidal setpoint: aim for high SOC at 18:00h		
	//SOC_setp_fr = 0.6 + 0.25 * Math.sin(2*Math.PI*(energyModel.t_h-12)/24); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
	
	double SOC_deficit_fr = SOC_setp_fr - currentBatteryStateOfCharge_fr;

	//double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + 1.0*(energyModel.v_epexForecast_eurpkWh - v_electricityPriceLowPassed_eurpkWh) + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
	double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + 0.0*(energyModel.v_epexForecast_eurpkWh - v_electricityPriceLowPassed_eurpkWh) + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
	chargeSetpoint_kW = p_batteryAsset.getCapacityElectric_kW()*(WTP_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_kWhpeur ;
					
	//chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	//p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getElectricCapacity_kW())); // Convert to powerFraction and limit power
	boolean b_stayWithinConnectionLimits = true;
	if( b_stayWithinConnectionLimits ) {	
		double maxBatteryPower_kW = p_contractedDeliveryCapacity_kW - (fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY)); // Max battery charging power within grid capacity
		double minBatteryPower_kW = - (p_contractedFeedinCapacity_kW + (fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY))); // Max discharging power within grid capacity (this number is usually negative!)
		chargeSetpoint_kW = min(max(chargeSetpoint_kW, minBatteryPower_kW),maxBatteryPower_kW); // Don't allow too much (dis)charging!
		/*if (minBatteryPower_kW>0) {
			traceln("Battery must charge to prevent curtailment! minBatteryPower_kW: %s, chargeSetpoint_kW: %s, battery SOC: %s", minBatteryPower_kW, chargeSetpoint_kW, currentBatteryStateOfCharge_fr);
		}*/
	}			

	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
}

//traceln("Hello!");



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

//Energy carrier flows
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
	v_liveData.dsm_liveDemand_kW.get(EC).add( currentTime_h, fm_currentConsumptionFlows_kW.get(EC) );
}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
	v_liveData.dsm_liveSupply_kW.get(EC).add( currentTime_h, fm_currentProductionFlows_kW.get(EC) );
}


//Electricity balance
v_liveData.data_liveElectricityBalance_kW.add(currentTime_h, fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));


//Total demand and supply
v_liveData.data_totalDemand_kW.add(currentTime_h, v_currentFinalEnergyConsumption_kW);
v_liveData.data_totalSupply_kW.add(currentTime_h, v_currentPrimaryEnergyProduction_kW);


//Live capacity datasets
v_liveData.data_gridCapacityDemand_kW.add(currentTime_h, p_contractedDeliveryCapacity_kW);
v_liveData.data_gridCapacitySupply_kW.add(currentTime_h, p_contractedFeedinCapacity_kW);


//// Gather specific electricity flows from corresponding energy assets

//Baseload electricity
v_fixedConsumptionElectric_kW = 0;
for (J_EA j_ea : c_fixedConsumptionElectricAssets) {
	v_fixedConsumptionElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_baseloadElectricityDemand_kW.add(currentTime_h, v_fixedConsumptionElectric_kW);


//Cooking
v_electricHobConsumption_kW = 0;
for (J_EA j_ea : c_electricHobAssets) {
	v_electricHobConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_cookingElectricityDemand_kW.add(currentTime_h, v_electricHobConsumption_kW);


//Hydrogen elec consumption
v_hydrogenElectricityConsumption_kW = 0;
for (J_EA j_ea : c_electrolyserAssets) {
	v_hydrogenElectricityConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_hydrogenElectricityDemand_kW.add(currentTime_h, max(0, v_hydrogenElectricityConsumption_kW));


//Heatpump elec consumption
v_heatPumpElectricityConsumption_kW = 0;
for (J_EA j_ea : c_electricHeatpumpAssets) {
	v_heatPumpElectricityConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_heatPumpElectricityDemand_kW.add(currentTime_h, max(0, v_heatPumpElectricityConsumption_kW));


//EVs
v_evChargingPowerElectric_kW = 0;
for (J_EA j_ea : c_EvAssets) {
	if (j_ea instanceof J_EAEV) {
		if (((J_EAEV)j_ea).vehicleScaling == 0) {
			continue;
		}
	}
	v_evChargingPowerElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_electricVehicleDemand_kW.add(currentTime_h, max(0,v_evChargingPowerElectric_kW));
v_liveData.data_V2GSupply_kW.add(currentTime_h, max(0, -v_evChargingPowerElectric_kW));


//Batteries
v_batteryPowerElectric_kW = 0;
v_batteryStoredEnergy_kWh = 0;
for (J_EA j_ea : c_batteryAssets) {
	if (((J_EAStorageElectric)j_ea).getCapacityElectric_kW() != 0 && ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh() != 0) {
		v_batteryPowerElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
		v_batteryStoredEnergy_kWh += ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()*((J_EAStorageElectric)j_ea).getCurrentStateOfCharge();
	}
}
v_liveData.data_batteryCharging_kW.add(currentTime_h, max(0, v_batteryPowerElectric_kW));		
v_liveData.data_batteryDischarging_kW.add(currentTime_h, max(0, -v_batteryPowerElectric_kW));	
v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(currentTime_h, v_batteryStoredEnergy_kWh/1000);


//CHP production
v_CHPProductionElectric_kW = 0;
for (J_EA j_ea : c_chpAssets) {
	v_CHPProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_CHPElectricityProductionLiveWeek_kW.add(currentTime_h, v_CHPProductionElectric_kW);


//PV production
v_pvProductionElectric_kW = 0;
for (J_EA j_ea : c_pvAssets) {
	v_pvProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
if (v_pvProductionElectric_kW < 0) {
	traceln("Negative v_pvProductionElectric_kW! Curtailment error? Value: %s", v_pvProductionElectric_kW);
	pauseSimulation();
}
v_liveData.data_PVGeneration_kW.add(currentTime_h, v_pvProductionElectric_kW);


//Wind production
v_windProductionElectric_kW = 0;
for (J_EA j_ea : c_windAssets) {
	v_windProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
v_liveData.data_windGeneration_kW.add(currentTime_h, v_windProductionElectric_kW);	


//District heating
v_liveData.data_districtHeatDelivery_kW.add(currentTime_h, max(0,fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT)));	


/*ALCODEEND*/}

double f_rapidRunDataLogging()
{/*ALCODESTART::1722518905501*/
v_maxConnectionLoad_fr = max(v_maxConnectionLoad_fr, abs(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) / p_contractedDeliveryCapacity_kW ));

double currentImport_kW = 0.0;
double currentExport_kW = 0.0;
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
	currentImport_kW += max( 0, currentBalance_kW );
	currentExport_kW += max( 0, -currentBalance_kW );
	v_rapidRunData.am_totalBalanceAccumulators_kW.get(EC).addStep(  currentBalance_kW );
}

// Daytime totals. Use overal-total minus daytime total to get nighttime totals.
if(energyModel.b_isDaytime) { 
	
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		v_rapidRunData.am_daytimeImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		v_rapidRunData.am_daytimeExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	v_rapidRunData.acc_daytimeElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_daytimeElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );	
	v_rapidRunData.acc_daytimeEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_daytimeEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);	
}

// Weekend totals. Use overal-totals minus weekend totals to get weekday totals.
if (!energyModel.b_isWeekday) { // 
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		v_rapidRunData.am_weekendImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		v_rapidRunData.am_weekendExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	v_rapidRunData.acc_weekendElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_weekendElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_weekendEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_weekendEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
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
		v_batteryStoredEnergy_kWh += ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()*((J_EAStorageElectric)j_ea).getCurrentStateOfCharge();
		
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

//========== SUMMER WEEK ==========//
if (energyModel.b_isSummerWeek){
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
	v_rapidRunData.acc_summerWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

	v_rapidRunData.acc_summerWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

	v_rapidRunData.acc_summerWeekDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
	v_rapidRunData.acc_summerWeekFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);
	
	v_rapidRunData.acc_summerWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	v_rapidRunData.acc_summerWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	v_rapidRunData.acc_summerWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	v_rapidRunData.acc_summerWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	v_rapidRunData.acc_summerWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	v_rapidRunData.acc_summerWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	v_rapidRunData.acc_summerWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	v_rapidRunData.acc_summerWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
	//acc_summerWeekBatteriesStoredEnergy_MWh.addStep();		

}

//========== WINTER WEEK ==========// 
if (energyModel.b_isWinterWeek){
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
	v_rapidRunData.acc_winterWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
	
	v_rapidRunData.acc_winterWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	
	
	v_rapidRunData.acc_winterWeekDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
	v_rapidRunData.acc_winterWeekFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);
	
	v_rapidRunData.acc_winterWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	v_rapidRunData.acc_winterWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	v_rapidRunData.acc_winterWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	v_rapidRunData.acc_winterWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	v_rapidRunData.acc_winterWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	v_rapidRunData.acc_winterWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	v_rapidRunData.acc_winterWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	v_rapidRunData.acc_winterWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
	//acc_winterWeekBatteriesStoredEnergy_MWh.addStep();		
	
}


//========== TOTALS / DAILY AVERAGES ==========//
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
    v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
    v_rapidRunData.am_dailyAverageProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
}
v_rapidRunData.acc_dailyAverageEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
v_rapidRunData.acc_dailyAverageEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
v_rapidRunData.acc_totalEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);

//acc_dailyAverageDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
//acc_dailyAverageFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);

v_rapidRunData.acc_dailyAverageBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
v_rapidRunData.acc_dailyAverageHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
v_rapidRunData.acc_dailyAverageElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
v_rapidRunData.acc_dailyAverageElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
v_rapidRunData.acc_dailyAverageDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );

v_rapidRunData.acc_dailyAveragePVProduction_kW.addStep( v_pvProductionElectric_kW );
v_rapidRunData.acc_dailyAverageWindProduction_kW.addStep( v_windProductionElectric_kW );
v_rapidRunData.acc_dailyAverageV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
//acc_dailyAverageBatteriesStoredEnergy_MWh.addStep();		

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
	l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, v_liveAssetsMetaData.totalInstalledPVPower_kW, false);
	l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, v_liveAssetsMetaData.totalInstalledWindPower_kW, false);
	energyModel.v_totalInstalledPVPower_kW -= v_liveAssetsMetaData.totalInstalledPVPower_kW;
	energyModel.v_totalInstalledWindPower_kW -= v_liveAssetsMetaData.totalInstalledWindPower_kW;
	energyModel.v_totalInstalledBatteryStorageCapacity_MWh -= v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	
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
	v_batteryPowerElectric_kW = 0;
	v_windProductionElectric_kW = 0;
	v_pvProductionElectric_kW = 0;
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
	
	// update GN parents' wind / solar totals (will be wrong if you changed your totals while paused)
	l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, v_liveAssetsMetaData.totalInstalledPVPower_kW, true);
	l_parentNodeElectric.getConnectedAgent().f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, v_liveAssetsMetaData.totalInstalledWindPower_kW, true);
	energyModel.v_totalInstalledPVPower_kW += v_liveAssetsMetaData.totalInstalledPVPower_kW;
	energyModel.v_totalInstalledWindPower_kW += v_liveAssetsMetaData.totalInstalledWindPower_kW;
	energyModel.v_totalInstalledBatteryStorageCapacity_MWh += v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
}

//Update the 'isActive' variable
v_isActive = setActive;
/*ALCODEEND*/}

double f_getChargeDeadline(J_EAEV ev)
{/*ALCODESTART::1725455130676*/
double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getStorageCapacity_kWh()*ev.getCurrentStateOfCharge());
double maxChargingPower_kW = ev.getCapacityElectric_kW();

return floor((ev.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / maxChargingPower_kW) / energyModel.p_timeStep_h) * energyModel.p_timeStep_h;

/*ALCODEEND*/}

double f_batteryManagementSimple()
{/*ALCODESTART::1725629047745*/
//traceln("Battery storage capacity: " + ((J_EAStorageElectric)p_batteryAsset.j_ea).getStorageCapacity_kWh());
if (p_batteryAsset.getStorageCapacity_kWh() != 0){

	double safetyMargin_fr = 0.95; // fraction of connection capacity that we use
	double power_fr = 0;
	double capacityElectric_kW = p_batteryAsset.getCapacityElectric_kW();
	
	if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) > p_contractedFeedinCapacity_kW * safetyMargin_fr) {
		// discharge
		double dischargeNeeded_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - p_contractedFeedinCapacity_kW * safetyMargin_fr;
		power_fr = - dischargeNeeded_kW / capacityElectric_kW;
	}
	else if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < p_contractedDeliveryCapacity_kW * safetyMargin_fr) {
		// charge
		double chargeAvailable_kW = p_contractedDeliveryCapacity_kW * safetyMargin_fr - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
		power_fr = chargeAvailable_kW / capacityElectric_kW;
	}

	
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, power_fr));
}
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

double f_fillLiveDataSets_old()
{/*ALCODESTART::1741258042922*/
double timeStep_h = energyModel.t_h-energyModel.p_runStartTime_h;

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	dsm_liveDemand_kW.get(EC).add( energyModel.t_h-energyModel.p_runStartTime_h, fm_currentConsumptionFlows_kW.get(EC) );
	dsm_liveSupply_kW.get(EC).add( energyModel.t_h-energyModel.p_runStartTime_h, fm_currentProductionFlows_kW.get(EC) );
}

data_liveElectricityBalance_kW.add(timeStep_h, fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));

//data_naturalGasDemand_kW.update();
//data_dieselDemand_kW.update();
//data_hydrogenDemand_kW.update();
//data_hydrogenSupply_kW.update();

data_totalDemand_kW.update();
data_totalSupply_kW.update();

data_gridCapacityDemand_kW.update();
data_gridCapacitySupply_kW.update();
// Gather specific electricity flows from corresponding energy assets
v_fixedConsumptionElectric_kW = 0;
for (J_EA j_ea : c_fixedConsumptionElectricAssets) {
	v_fixedConsumptionElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_baseloadElectricityDemand_kW.update()

v_electricHobConsumption_kW = 0;
for (J_EA j_ea : c_electricHobAssets) {
	v_electricHobConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_cookingElectricityDemand_kW.update();

v_hydrogenElectricityConsumption_kW = 0;
for (J_EA j_ea : c_electrolyserAssets) {
	v_hydrogenElectricityConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_hydrogenElectricityDemand_kW.update();

v_heatPumpElectricityConsumption_kW = 0;
for (J_EA j_ea : c_electricHeatpumpAssets) {
	v_heatPumpElectricityConsumption_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_heatPumpElectricityDemand_kW.update();

//data_electricCarsDemand_kW.update();
v_evChargingPowerElectric_kW = 0;
for (J_EA j_ea : c_EvAssets) {
	if (j_ea instanceof J_EAEV) {
		if (((J_EAEV)j_ea).vehicleScaling == 0) {
			continue;
		}
	}
	v_evChargingPowerElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_electricVehicleDemand_kW.update();
data_V2GSupply_kW.update();

v_batteryPowerElectric_kW = 0;
v_batteryStoredEnergy_kWh = 0;
for (J_EA j_ea : c_batteryAssets) {
	if (((J_EAStorageElectric)j_ea).getCapacityElectric_kW() != 0 && ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh() != 0) {
		v_batteryPowerElectric_kW += j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
		v_batteryStoredEnergy_kWh += ((J_EAStorageElectric)j_ea).getStorageCapacity_kWh()*((J_EAStorageElectric)j_ea).getCurrentStateOfCharge();
	}
}
data_batteryCharging_kW.update();	
data_batteryDischarging_kW.update();	
data_batteryStoredEnergyLiveWeek_MWh.update();

v_CHPProductionElectric_kW = 0;
for (J_EA j_ea : c_chpAssets) {
	v_CHPProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_CHPElectricityProductionLiveWeek_kW.update();	

v_pvProductionElectric_kW = 0;
for (J_EA j_ea : c_pvAssets) {
	v_pvProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
if (v_pvProductionElectric_kW < 0) {
	traceln("Negative v_pvProductionElectric_kW! Curtailment error? Value: %s", v_pvProductionElectric_kW);
	pauseSimulation();
}
data_PVGeneration_kW.update();	

v_windProductionElectric_kW = 0;
for (J_EA j_ea : c_windAssets) {
	v_windProductionElectric_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
}
data_windGeneration_kW.update();	


//District heating
data_districtHeatDelivery_kW.update();

//data_totalNetLoad_kW.update();
/*ALCODEEND*/}

double f_initializePreviousRunData()
{/*ALCODESTART::1741277722817*/

/*ALCODEEND*/}

