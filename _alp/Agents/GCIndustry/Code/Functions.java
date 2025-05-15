double f_connectToChild_overwrite(Agent ConnectingParentNode)
{/*ALCODESTART::1665498760168*/
//assetLinks.connectTo(ConnectingParentNode);
EnergyAsset EA = (EnergyAsset) ConnectingParentNode;
c_energyAssets.add(EA);

if (EA.j_ea instanceof J_EAConsumption) {
	//c_consumptionAssets.add(EA);
} else if (EA.j_ea instanceof J_EAProduction ) {
//c_productionAssets.add(EA);
} else if (EA.j_ea instanceof J_EAStorage ) {
	//c_storageAssets.add(EA);
	if (EA.j_ea instanceof J_EAStorageHeat) {
		//p_BuildingThermalAsset = EA; Obsolete! Need to replace in connectToJ_EA
	}
	else if (EA.j_ea instanceof J_EAEV) {
		//c_vehicleAssets.add( EA );
		//c_vehicleAssets.get( v_vehicleIndex ).v_powerFraction_fr = 1;
		/*MobilityTracker m = main.add_mobilityTrackers();
		c_mobilityTrackers.add( m );
		m.p_vehicleIndex = v_vehicleIndex;
		m.p_gridConnection = this;
		m.p_vehicleInstance = (J_EAEV)m.p_vehicleInstance;
		m.p_energyAsset = c_vehicleAssets.get( v_vehicleIndex );
		m.p_vehicleInstance = (J_EAEV)m.p_energyAsset.j_ea;
		m.p_mobilityPatternType = OL_MobilityPatternType.TRUCK;
		((J_EAEV)m.p_vehicleInstance).setMobilityTracker( m );
		m.f_getData();*/
		//v_vehicleIndex ++;
	}
} else if (EA.j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)EA.j_ea);
	if(p_gridConnectionType == OL_GridConnectionType.AGRO_ENERGYHUB) {
	
		if (EA.j_ea instanceof J_EAConversionGasBurner || EA.j_ea instanceof J_EAConversionHydrogenBurner) {
			if (p_primaryHeatingAsset==null) {
				p_primaryHeatingAsset = (J_EAConversion)EA.j_ea ;
			} else { 
				p_secondaryHeatingAsset = (J_EAConversion)EA.j_ea ;
			}
		} else if ( EA.j_ea instanceof J_EAConversionHeatPump  || EA.j_ea instanceof J_EAConversionGasCHP) {
			if (p_primaryHeatingAsset==null) {
				//p_primaryHeatingAsset = EA; Obsolete!
			} else { 
				p_secondaryHeatingAsset = p_primaryHeatingAsset;
				//p_primaryHeatingAsset = EA; Obsolete!
			}
			if (EA.j_ea instanceof J_EAConversionHeatPump) {
				((J_EAConversionHeatPump)p_primaryHeatingAsset).outputTemperature_degC = 80; // For pastorizing milk
			}
			//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
		} else 	if (EA.j_ea instanceof J_EAConversionCurtailer || EA.j_ea instanceof J_EAConversionCurtailerHeat) {
			p_curtailer = (J_EAConversionCurtailer)EA.j_ea ;
		} else {
			traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + ConnectingParentNode.getId() + " is of unknown type or null! ");
		}
	} else {
		if (EA.j_ea instanceof J_EAConversionGasBurner || EA.j_ea instanceof J_EAConversionHeatPump || EA.j_ea instanceof J_EAConversionHydrogenBurner ) {
			p_primaryHeatingAsset = (J_EAConversion)EA.j_ea ;
		} else {
			traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + ConnectingParentNode.getId() + " is of unknown type or null! ");
		}
	}

}

/*ALCODEEND*/}

double f_operateFixedConsumptionAssets_overwrite()
{/*ALCODESTART::1665499506761*/
switch( p_gridConnectionType ) {
	case STEEL:
		for(EnergyAsset ea : c_consumptionAssets) {
			if( ea.p_energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND ) {
				ea.f_updateElectricityFlows( main.v_currentIndustrySteelElectricityDemand_fr );
			}
			else if( ea.p_energyAssetType == OL_EnergyAssetType.HEAT_DEMAND ) {
				ea.f_updateAllFlows( main.v_currentIndustrySteelHeatDemand_fr );
			}
			else {
				ea.v_powerFraction_fr = 0; // To disable other asset
				traceln("industry has other consumption assets than modeled so far");
			}
		}
	break;
	case INDUSTRY_OTHER:
		for(EnergyAsset ea : c_consumptionAssets) {
			if( ea.p_energyAssetType == OL_EnergyAssetType.ELECTRICITY_CONSUMPTION_PROFILE ) {
				ea.f_updateElectricityFlows( main.v_currentIndustryOtherElectricityDemand_fr );
			}
			else if( ea.p_energyAssetType == OL_EnergyAssetType.HEAT_DEMAND ) {
				ea.f_updateAllFlows( main.v_currentIndustryOtherHeatDemand_fr );
			}
			else {
				ea.v_powerFraction_fr = 0; // To disable other asset
				traceln("industry has other consumption assets than modeled so far");
			}
		}
	break;
	default:
		traceln("industry not modeled so far");

	break;
}
/*ALCODEEND*/}

double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1665501255400*/
if(p_gridConnectionID.contains("agrohub")) {
	f_operateFlexAssets_agroenergyhub();
	for( J_EAStorage v : c_storageAssets ) {	
		//v_currentPowerElectricity_kW += v.electricityConsumption_kW - v.electricityProduction_kW;
		v_conversionPowerElectric_kW += v.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
		//v_currentPowerMethane_kW += v.methaneConsumption_kW - v.methaneProduction_kW;
		//v_currentPowerHydrogen_kW += v.hydrogenConsumption_kW - v.hydrogenProduction_kW;
		//v_currentPowerHeat_kW += v.heatConsumption_kW - v.heatProduction_kW;
		//v_currentPowerDiesel_kW += v.dieselConsumption_kW;
	}
	
	//traceln("Agroenergyhub logic!");
} else {
	
	// Determine house heating
	f_manageHeatingAssets();
	
}

f_manageCharging();
/*for( J_EAVehicle v: c_vehicleAssets) {
	v_currentPowerElectricity_kW += v.electricityConsumption_kW - v.electricityProduction_kW;
}*/

if (p_batteryAsset != null){ // TEST CODE
	if (p_batteryAsset.getStorageCapacity_kWh() > 0) {
		//f_batteryManagementBalance(p_batteryAsset.getCurrentStateOfCharge());
		f_batteryManagementNodalPricing(p_batteryAsset.getCurrentStateOfCharge());
		p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
		//J_FlowsMap flowsMap = flowsPair.getFirst();
		//traceln("flows:" + Arrays.toString(arr));
		//v_batteryPowerElectric_kW = flowsMap.get(OL_EnergyCarriers.ELECTRICITY);
	}
}

//v_currentLoadLowPassed_kW += v_lowPassFactorLoad_fr * ( v_currentPowerElectricity_kW - v_currentLoadLowPassed_kW ); //you want to do deterine the lowpassLoad BEFORE the using the battery. As this behavior of the battery should nog be dependent on the load of the battery in the previous timesteps

//v_previousPowerMethane_kW = v_currentPowerMethane_kW;
v_previousPowerMethane_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.METHANE);


/*ALCODEEND*/}

double f_operateFlexAssets_agroenergyhub()
{/*ALCODESTART::1684397492292*/
double heatDemand_kW = (fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.HEAT));
double biogasSupply_kW = 0;
if( p_owner != null ) {
	if( p_owner.p_methaneSupplier instanceof EnergyCoop ) {
		EnergyCoop CoopParent = (EnergyCoop)p_owner.p_methaneSupplier;
		//biogasSupply_kW = -CoopParent.v_methaneVolume_kWh/energyModel.p_timeStep_h + v_previousPowerMethane_kW;
		biogasSupply_kW = -CoopParent.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.METHANE) + v_previousPowerMethane_kW;
	}
}

//traceln("BiogasSupply from mestvergister: " + biogasSupply_kW + " kW");
//traceln("Heat demand agroenergyhub:" + heatDemand_kW);

if ( p_secondaryHeatingAsset == null) { // Just one heating asset
	if ( p_primaryHeatingAsset== null ) {
		traceln("No heating assets for industry gridconnection " + p_gridConnectionID);
	} else {
		if ( p_primaryHeatingAsset instanceof J_EAConversionGasBurner || p_primaryHeatingAsset instanceof J_EAConversionHeatPump || p_primaryHeatingAsset instanceof J_EAConversionHydrogenBurner) { // when there is only a gas burner or DH set
				p_primaryHeatingAsset.v_powerFraction_fr = min(1,heatDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW());
				//traceln("Running manageHeatingAsset for single heating asset");
		} else {
			traceln("GridConnection " + p_gridConnectionID + " has a single unsupported heating asset!");
		}
	}
} else { // Two heating assets
	if ( p_primaryHeatingAsset instanceof J_EAConversionHeatPump & p_secondaryHeatingAsset instanceof J_EAConversionGasCHP) { // Heatpump and gasburner, switch based on heatpump COP)
		//((J_EAConversionHeatPump)p_primaryHeatingAsset.j_ea).updateAmbientTemp(main.v_currentAmbientTemperature_degC); // update heatpump temp levels! <-- waarom dit gebeurt al in de main (peter 21-02-23)
		double HP_COP = ((J_EAConversionHeatPump)p_primaryHeatingAsset).getCOP();
		double HP_powerDemand_kW = heatDemand_kW / HP_COP;
		// Decide to use CHP or HeatPump to fulfill heat demand based on 'SoC' of gasbuffer and current electricity use on site
		if ( (-(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY)) > 0.5*v_liveConnectionMetaData.contractedFeedinCapacity_kW*2*(p_gasBuffer.getCurrentStateOfCharge()-0.5) | p_gasBuffer.getCurrentStateOfCharge() < 0.05) & p_gasBuffer.getCurrentStateOfCharge() < 0.9) { // Use heatpump when it can be done selfsufficiently or when methane supply is zero
			//traceln("HeatPump in operation with COP " + HP_COP);
			p_secondaryHeatingAsset.v_powerFraction_fr = 0;
			p_primaryHeatingAsset.v_powerFraction_fr = min(1,HP_powerDemand_kW / p_primaryHeatingAsset.getInputCapacity_kW());
		} else { // CHP when there is no electricity surpluss or when gas tank is overfilling
			//traceln("CHP capacityHeat_kW: " + p_primaryHeatingAsset.j_ea.getHeatCapacity_kW());
			p_secondaryHeatingAsset.v_powerFraction_fr = min(1,heatDemand_kW / p_secondaryHeatingAsset.getOutputCapacity_kW());
			//p_primaryHeatingAsset.v_powerFraction_fr = 0;//min(1,currentDHWdemand_kW / p_secondaryHeatingAsset.j_ea.getHeatCapacity_kW());
			// Let heatpump fill heatdemand gap if it exists
			p_primaryHeatingAsset.v_powerFraction_fr = max(0,min(1,(heatDemand_kW-p_secondaryHeatingAsset.v_powerFraction_fr*p_secondaryHeatingAsset.getOutputCapacity_kW()) / p_primaryHeatingAsset.getOutputCapacity_kW())); // Let gas burner fill the heatdemandgap
		}
	} else if ( p_primaryHeatingAsset instanceof J_EAConversionGasCHP & p_secondaryHeatingAsset instanceof J_EAConversionGasBurner) { // CHP & gas burner
		if ( p_gasBuffer.getCurrentStateOfCharge() < 0.05) { // Use regular gas burner when biogas buffer is nearly empty
//		if ( (-v_currentPowerElectricity_kW > 0.5*p_connectionCapacity_kW*2*(p_gasBuffer.j_ea.getCurrentStateOfCharge()-0.5) | p_gasBuffer.j_ea.getCurrentStateOfCharge() < 0.05) & p_gasBuffer.j_ea.getCurrentStateOfCharge() < 0.9) { // Use gas burner when biogas is depleted or when there is too much PV and CHP would lead to curtailment
			//traceln("HeatPump in operation with COP " + HP_COP);
			p_primaryHeatingAsset.v_powerFraction_fr = 0;
			p_secondaryHeatingAsset.v_powerFraction_fr = min(1,heatDemand_kW / p_secondaryHeatingAsset.getOutputCapacity_kW());
		} else { // CHP when there is sufficient biogas
			//traceln("CHP capacityHeat_kW: " + p_primaryHeatingAsset.j_ea.getHeatCapacity_kW());
			if ( p_gasBuffer.getCurrentStateOfCharge() < 0.9 ) { // Biogas tank not full, allow reduced CHP power when it prevents curtailment.
				p_primaryHeatingAsset.v_powerFraction_fr = min(min(1,heatDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW()),(v_liveConnectionMetaData.contractedFeedinCapacity_kW + (fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY))) / p_primaryHeatingAsset.getInputCapacity_kW());
			} else {
				p_primaryHeatingAsset.v_powerFraction_fr = min(1,heatDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW());
				//if ( p_gasBuffer.j_ea.getCurrentStateOfCharge() > 0.98 ) { // Biogas tank not full, allow reduced CHP power when it prevents curtailment.				
				//	traceln("Biogas tank 98% full! must run CHP! Heat demand: " + heatDemand_kW + " kW");
				//}
			}
			p_secondaryHeatingAsset.v_powerFraction_fr = max(0,min(1,(heatDemand_kW-p_primaryHeatingAsset.v_powerFraction_fr*p_primaryHeatingAsset.getOutputCapacity_kW()) / p_secondaryHeatingAsset.getOutputCapacity_kW())); // Let gas burner fill the heatdemandgap
		}
	} else if ( p_primaryHeatingAsset instanceof J_EAConversionHeatPump & p_secondaryHeatingAsset instanceof J_EAConversionGasBurner) { // Heatpump and gasburner, switch based on heatpump COP)
		//((J_EAConversionHeatPump)p_primaryHeatingAsset.j_ea).updateAmbientTemp(main.v_currentAmbientTemperature_degC); // update heatpump temp levels! <-- waarom dit gebeurt al in de main (peter 21-02-23)
		//double HP_COP = ((J_EAConversionHeatPump)p_primaryHeatingAsset.j_ea).getCOP();
		//double HP_powerDemand_kW = heatDemand_kW / HP_COP;
		p_primaryHeatingAsset.v_powerFraction_fr = min(1,heatDemand_kW / p_primaryHeatingAsset.getOutputCapacity_kW());
		p_secondaryHeatingAsset.v_powerFraction_fr = max(0,min(1,(heatDemand_kW-p_primaryHeatingAsset.v_powerFraction_fr*p_primaryHeatingAsset.getOutputCapacity_kW()) / p_secondaryHeatingAsset.getOutputCapacity_kW())); // Let gas burner fill the heatdemandgap
	} else {
		traceln("**** EXCEPTION ****: Unsupported combination of heatings systems in GridConnection " + p_gridConnectionID);
		p_primaryHeatingAsset.v_powerFraction_fr = 0;
		p_secondaryHeatingAsset.v_powerFraction_fr = 0;
		//p_BuildingThermalAsset.v_powerFraction_fr = 0;
	}
	p_secondaryHeatingAsset.f_updateAllFlows(p_secondaryHeatingAsset.v_powerFraction_fr);
}
if (p_primaryHeatingAsset != null) {
	p_primaryHeatingAsset.f_updateAllFlows(p_primaryHeatingAsset.v_powerFraction_fr);
}

if (p_gasBuffer != null) {
//	if (p_secondaryHeatingAsset != null) {
//		p_gasBuffer.v_powerFraction_fr = (biogasSupply_kW - p_primaryHeatingAsset.v_currentConsumptionMethane_kW - p_secondaryHeatingAsset.v_currentConsumptionMethane_kW)/p_gasBuffer.j_ea.getGasCapacity_kW();
//	} else {
	if (p_primaryHeatingAsset instanceof J_EAConversionGasCHP) {
		p_gasBuffer.v_powerFraction_fr = (biogasSupply_kW - p_primaryHeatingAsset.getLastFlows().get(OL_EnergyCarriers.METHANE)) / p_gasBuffer.getCapacityGas_kW();
	} else if (p_secondaryHeatingAsset != null) {
		if (p_secondaryHeatingAsset instanceof J_EAConversionGasCHP) {
			p_gasBuffer.v_powerFraction_fr = (biogasSupply_kW - p_secondaryHeatingAsset.getLastFlows().get(OL_EnergyCarriers.METHANE)) / p_gasBuffer.getCapacityGas_kW();
		}
	}
//	}
	p_gasBuffer.f_updateAllFlows(p_gasBuffer.v_powerFraction_fr);
}

/*if (p_curtailer != null) {
	//traceln("Hello! " + CurtailerAsset.j_ea.getElectricCapacity_kW());
	if (p_curtailer.getElectricCapacity_kW()>0) {
		double curtailerSetpointElectric_kW = 0.0;
		if ( p_primaryHeatingAsset instanceof J_EAConversionGasCHP ) {
			curtailerSetpointElectric_kW = -min(0,v_currentPowerElectricity_kW - p_primaryHeatingAsset.heatProduction_kW + p_connectionCapacity_kW);
		} else {
			curtailerSetpointElectric_kW = -min(0,v_currentPowerElectricity_kW + p_connectionCapacity_kW);
		}
		p_curtailer.f_updateAllFlows(curtailerSetpointElectric_kW/p_curtailer.getElectricCapacity_kW());
	}
}*/
/*ALCODEEND*/}

double f_manageCurtailer(EnergyAsset CurtailerAsset)
{/*ALCODESTART::1684940409126*/
//traceln("Hello! " + CurtailerAsset.j_ea.getElectricCapacity_kW());
if (CurtailerAsset.j_ea.getElectricCapacity_kW()>0) {
	double curtailerSetpointElectric_kW = -min(0,v_currentPowerElectricity_kW + p_connectionCapacity_kW);
	CurtailerAsset.f_updateAllFlows(curtailerSetpointElectric_kW/CurtailerAsset.j_ea.getElectricCapacity_kW());
	
	/*if ( curtailerSetpointElectric_kW > 0 ) {
		traceln("Windfarm is curtailing " + curtailerSetpointElectric_kW + " kW!");
	}*/
}
/*ALCODEEND*/}

double f_connectTo_J_EA_Industry(J_EA j_ea)
{/*ALCODESTART::1693299907029*/

if (j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)j_ea);
	if(p_gridConnectionID.contains("agrohub")) {
	
		if (j_ea instanceof J_EAConversionGasBurner || j_ea instanceof J_EAConversionHydrogenBurner) {
			if (p_primaryHeatingAsset == null) {
				p_primaryHeatingAsset = (J_EAConversion)j_ea ;
			} else if ( p_primaryHeatingAsset!=j_ea ) {
				p_secondaryHeatingAsset = (J_EAConversion)j_ea ;
			}
		} else if ( j_ea instanceof J_EAConversionHeatPump  || j_ea instanceof J_EAConversionGasCHP) {
			if (p_primaryHeatingAsset==null) {
				p_primaryHeatingAsset = (J_EAConversion)j_ea;
			} else { 
				p_secondaryHeatingAsset = p_primaryHeatingAsset;
				p_primaryHeatingAsset = (J_EAConversion)j_ea;
			}
			if (j_ea instanceof J_EAConversionHeatPump) {
				((J_EAConversionHeatPump)p_primaryHeatingAsset).outputTemperature_degC = 80; // For pastorizing milk
			}
			//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
		//} else 	if (j_ea instanceof J_EAConversionCurtailer) {
		//	p_curtailer = (J_EAConversionCurtailer)j_ea ;
		} else {
			traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + j_ea + " is of unknown type or null! ");
		}
	} else {
		if (j_ea instanceof J_EAConversionGasBurner || j_ea instanceof J_EAConversionHeatPump || j_ea instanceof J_EAConversionHydrogenBurner ) {
			if (p_primaryHeatingAsset == null) {
				p_primaryHeatingAsset = (J_EAConversion)j_ea ;
			} else if ( p_primaryHeatingAsset!=j_ea ) {
				p_secondaryHeatingAsset = (J_EAConversion)j_ea ;
			}
		} else {
			traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + j_ea + " is of unknown type or null! ");
		}
	}
} 	
		
	


/*ALCODEEND*/}

