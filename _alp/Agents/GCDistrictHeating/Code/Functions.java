double f_operateAssetsDistrictHeating()
{/*ALCODESTART::1663773953770*/
// District heating system, heating power based on temperature of buffer of heating system. (big or small) Heat transfer from DH_GridConnection to heat NetNode is based on temperature difference between heating-buffer and water in heating network
//traceln("Operating District Heating agent = "+ this + " -> DH primary heating asset = " + p_primaryHeatingAsset.j_ea.ownerAsset.toString());
//traceln("Operating District Heating agent = "+ this + " -> DH secondary heating asset = " + p_secondaryHeatingAsset.j_ea.ownerAsset.toString());
//traceln("DistrictHeating Active!");
if(p_primaryHeatingAsset == null){
	return;
}

double tempSetpoint_degC = 0;
double minBufferTemp_degC = 0;
double maxTempBuffer_degC = 0;
double storageTemp_degC = 0;
//double DHnetworkTemp_degC = 0;
//v_currentElectricityPriceConsumption_eurpkWh = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getElectricityPrice( v_currentPowerElectricity_kW );
v_currentElectricityPriceConsumption_eurpkWh = l_parentNodeElectric.getConnectedAgent().v_currentTotalNodalPrice_eurpkWh;

//if (l_ownerActor.getConnectedAgent() instanceof EnergyCoop){
	//electricitySurplussCoop_kW = ((EnergyCoop)l_ownerActor.getConnectedAgent()).v_electricitySurplus_kW + v_previousPowerElectricity_kW; // From last timestep! So compensate for own electric power
	//traceln("DH function! Electricity surpluss in Coop: " + electricitySurplussCoop_kW + ", own power " + v_previousPowerElectricity_kW,1 );
//}	
//		((J_EAConversionHeatPump)e.j_ea).updateParameters(main.p_undergroundTemperature_degC, storageTemp_degC); // update heatpump temp levels!
double heatTransferToNetwork_kW = max(0,l_parentNodeHeat.getConnectedAgent().v_currentLoad_kW - v_previousPowerHeat_kW);// max( storageTemp_degC - DHnetworkTemp_degC, 0 ) * heatTransferToNetworkCoefficient_kWpK;

if( b_validLocalBuffer ) {
	tempSetpoint_degC = p_DHheatStorage.getSetTemperature_degC();
	v_bufferTemp_degC = p_DHheatStorage.getCurrentTemperature();

	//traceln("Thermal storage asset heatCapacity: " + p_DHheatStorage.getHeatCapacity_kW());
	double heatingSetpoint_kW = max(0,(tempSetpoint_degC - v_bufferTemp_degC)) * p_DHheatStorage.getHeatCapacity_JpK() / (3.6e6 * energyModel.p_timeStep_h);
	//Pair<J_FlowsMap, Double> flowsPair = p_primaryHeatingAsset.f_updateAllFlows( heatingSetpoint_kW / p_primaryHeatingAsset.getCapacityHeat_kW() );
	p_primaryHeatingAsset.f_updateAllFlows( heatingSetpoint_kW / p_primaryHeatingAsset.getOutputCapacity_kW() );
	double heatingPower_kW = -p_primaryHeatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT);
	
	//traceln("Heating setpoint: %s kW, heating power: %s kW", heatingSetpoint_kW, heatingPower_kW);	
	p_DHheatStorage.v_powerFraction_fr = ( heatingPower_kW - heatTransferToNetwork_kW ) / p_DHheatStorage.getCapacityHeat_kW();
	if ( abs(p_DHheatStorage.v_powerFraction_fr ) > 1) {
		traceln("p_DHheatStorage.v_powerFraction_fr greater than 1! %s", p_DHheatStorage.v_powerFraction_fr );
	}
	p_DHheatStorage.f_updateAllFlows(p_DHheatStorage.v_powerFraction_fr);
	//traceln("heatTransferToNetwork_kW: " + heatTransferToNetwork_kW);
	//traceln("District heating system temperature: %s degC", p_DHheatStorage.getCurrentTemperature());
} else {
	//Pair<J_FlowsMap, Double> flowsPair = p_primaryHeatingAsset.f_updateAllFlows(heatTransferToNetwork_kW/p_primaryHeatingAsset.getCapacityHeat_kW()); // heatingasset must be powerful enough every single timestep!
	p_primaryHeatingAsset.f_updateAllFlows(heatTransferToNetwork_kW/p_primaryHeatingAsset.getOutputCapacity_kW());
	if ( 0.00001 < (heatTransferToNetwork_kW + p_primaryHeatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT)) ) {
		traceln("Warning! District heating primary heating asset not able to fulfill heat demand! Heatdemand: %s kW, heat produced: %s kW", heatTransferToNetwork_kW, -p_primaryHeatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) );
	}	
	//traceln("Primary heating system powerFraction: %s", p_primaryHeatingAsset.v_powerFraction_fr);
	//totalHeatingPower_kWth = b_primarySourceTempAboveTargetTemp? p_primaryHeatingAsset.v_powerFraction_fr * p_primaryHeatingAsset.getHeatCapacity_kW() : 0; // only model net heat flow when temperature levels allow this

}

//double heatTransferToNetworkCoefficient_kWpK = 10000;
//traceln("storageTemp_degC: " + storageTemp_degC + ", DHnetworkTemp_degC: " + DHnetworkTemp_degC);

//traceln("storageTemp_degC: "+storageTemp_degC+", network current temp_degC: " + DHnetworkTemp_degC + ", heatTransferToNetwork_kW: "+heatTransferToNetwork_kW);

//traceln("totalHeatingPower " +totalHeatingPower_kWth);

// Block (passive energy flows if too cold
//heatTransferToNetwork_kW = b_storageBelowNetworkTemp? 0 : heatTransferToNetwork_kW;

//p_primaryHeatingAsset.f_updateAllFlows( p_primaryHeatingAsset.v_powerFraction_fr );

//traceln("District heating " + p_parentNodeHeatID + " storage temperature " + storageTemp_degC + " degC" + ", district heating network temperature " + p_parentNodeHeat.p_transportBuffer.getCurrentTemperature() + " deg C" );

//v_currentPowerHeat_kW += p_BuildingThermalAsset.heatConsumption_kW - p_BuildingThermalAsset.heatProduction_kW;


/*ALCODEEND*/}

double f_connectToChildDistrictHeating(Agent ConnectingChildNode)
{/*ALCODESTART::1666266296956*/
//assetLinks.connectTo(ConnectingChildNode);
EnergyAsset EA = (EnergyAsset) ConnectingChildNode;
c_energyAssets.add(EA);

if (EA.j_ea instanceof J_EAConsumption) {
	c_consumptionAssets.add(EA);
} else if (EA.j_ea instanceof J_EAProduction ) {
	c_productionAssets.add(EA);
	if( EA.p_energyAssetType == OL_EnergyAssetType.RESIDUALHEATLT) {
		// handle residual heat not as a direct heat source!
		p_residualHeatLTSource = EA;
	}
	else {
		c_productionAssets.add(EA);
		
	}
		
} else if (EA.j_ea instanceof J_EAStorage ) {
	c_storageAssets.add(EA);
	if (EA.j_ea instanceof J_EAStorageHeat) {
		p_BuildingThermalAsset = EA;
	}
} else if (EA.j_ea instanceof J_EAConversion) {
	c_conversionAssets.add((J_EAConversion)EA.j_ea );
	
	switch (p_heatingType) {
		case HEATPUMP_GASPEAK:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionHeatPump? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasBurner? EA : p_secondaryHeatingAsset;
		break;
		case GASFIRED:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasBurner? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasBurner? EA : p_secondaryHeatingAsset;
		break;
		case HYDROGENFIRED:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionHydrogenBurner? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionHydrogenBurner? EA : p_secondaryHeatingAsset;
		break;
		case HEATPUMP_BOILERPEAK:    // ambigue wat we met boiler bedoelen; eboiler of grootschalige DH_boiler = gasburner!
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionHeatPump? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasBurner? EA : p_secondaryHeatingAsset;
		break;
		case GASFIRED_CHPPEAK:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasCHP? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasBurner? EA : p_secondaryHeatingAsset;
		break;
		case DISTRICT_EBOILER_CHP:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionElectricHeater? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasCHP? EA : p_secondaryHeatingAsset;
		break;
		
		case LT_RESIDUAL_HEATPUMP_GASPEAK:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionHeatPump? EA : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && EA.j_ea instanceof J_EAConversionGasBurner? EA : p_secondaryHeatingAsset;
		break;
		default:
			throw new IllegalStateException("Invalid DistrictHeating HeatingType: " + p_heatingType);
	}
	
		
	//if (EA.j_ea instanceof J_EAConversionGasBurner || EA.j_ea instanceof J_EAConversionHeatPump || EA.j_ea instanceof J_EAConversionHeatDeliverySet || EA.j_ea instanceof J_EAConversionElectricHeater || EA.j_ea instanceof J_EAConversionGasCHP ) {
	//	if (p_primaryHeatingAsset == null) {
	//		p_primaryHeatingAsset = EA;
	//	} else if (p_secondaryHeatingAsset == null) {
	//		p_secondaryHeatingAsset = EA;
	//	} else {
	//		traceln("District Heating gridconnection already has two heating systems!");
	//	}
	//	//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
	if( ((J_EAConversion)EA.j_ea) instanceof J_EAConversionHeatPump && ((J_EAConversionHeatPump)EA.j_ea).getAmbientTempType().equals("WATER") ) {
		EA.p_linkedSourceEnergyAsset = p_residualHeatLTSource;
		((J_EAConversionHeatPump)EA.j_ea).setSourceAssetHeatPower( EA.p_linkedSourceEnergyAsset.j_ea.capacityHeat_kW );
	}
//}
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

double f_setConfigurationBooleans()
{/*ALCODESTART::1677511998575*/
// one time setting of booleans marking system configuration for use in transparant logic switching
b_residualHeatLTSource = p_residualHeatLTSource instanceof J_EAProduction? true : false;

// Check if buffer is present and has physically correct parameters
if (p_DHheatStorage instanceof J_EAStorageHeat) {
	if(p_DHheatStorage.getCapacityHeat_kW() > 0 & p_DHheatStorage.getCapacityHeat_kW()> 0 ) {
		b_validLocalBuffer = true;
	}
}

/*
// first update parameters of any heatpumps if present // [GH] This logic should only be run once, not every timestep! updateAmbientTemp should also be triggered from main.
for(J_EA e : c_energyAssets) {
	if(e instanceof J_EAConversionHeatPump) {
		if ( ((J_EAConversionHeatPump)e).getAmbientTempType().equals("WATER") && b_residualHeatLTSource ) {
			//traceln("water ambientTempType for LTresidual heat with heatpump check!");
			((J_EAConversionHeatPump)e).f_setLinkedVariable(energyModel, "WATER", ((J_EAConversionHeatPump)e).p_linkedSourceEnergyAsset );
			e.updateAmbientTemperature( ((J_EAConversionHeatPump)e).p_baseTemperatureReference );
		}
	}
}	
*/
/*ALCODEEND*/}

double f_operateSmartHeating()
{/*ALCODESTART::1678448031698*/
v_chpElectricityPrice = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getAveragedElectricityPrice(v_currentPowerElectricity_kW, - p_secondaryHeatingAsset.capacityElectric_kW   );
v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( v_chpElectricityPrice - v_electricityPriceLowPassed_eurpkWh );
//v_currentPriceLevel = f_getPriceLevel( v_currentElectricityPriceConsumption_eurpkWh );

c_electricityPriceList.add( v_chpElectricityPrice );
if (c_electricityPriceList.size() > 30) {
    c_electricityPriceList.remove(0);
}

double runningAverage = c_electricityPriceList.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

double variance = c_electricityPriceList.stream()
        .mapToDouble(value -> Math.pow(value - runningAverage, 2))
        .average()
        .orElse(0.0);


double standardDeviation = Math.sqrt(variance);
double currentDeviation = Math.abs(v_chpElectricityPrice - runningAverage) / standardDeviation;

GridNode superNodeElectric = l_parentNodeElectric.getConnectedAgent().superConnection.getConnectedAgent();
switch (p_heatingType) {
	case GASFIRED_CHPPEAK:
		if( v_chpElectricityPrice > (v_electricityPriceLowPassed_eurpkWh + p_pricelevelHighDifFromAvg_eurpkWh) && p_BuildingThermalAsset.getCurrentTemperature() < p_BuildingThermalAsset.getMaxTemperature_degC() ){
			p_secondaryHeatingAsset.v_powerFraction_fr = 1;
		}
		if ( p_BuildingThermalAsset.getCurrentTemperature() <= p_BuildingThermalAsset.getMinTemperature_degC() ){
 			p_primaryHeatingAsset.v_powerFraction_fr = 1; //max(0, (p_primaryHeatingAsset.j_ea.capacityHeat_kW - p_secondaryHeatingAsset.j_ea.capacityHeat_kW * p_secondaryHeatingAsset.v_powerFraction_fr) / p_primaryHeatingAsset.j_ea.capacityHeat_kW );
		}
	break;
	case DISTRICT_EBOILER_CHP:
		if ( p_BuildingThermalAsset.getCurrentTemperature() <= p_BuildingThermalAsset.getMinTemperature_degC() ){
 			//als minimum temp is bereikt, allebei aan
 			traceln("beide aan");
 			p_primaryHeatingAsset.v_powerFraction_fr = 1; 
			p_secondaryHeatingAsset.v_powerFraction_fr = 1;	
		}
		//else if ( superNodeElectric.v_currentLoadElectricity_kW < 0 && p_BuildingThermalAsset.j_ea.getCurrentTemperature() < p_BuildingThermalAsset.j_ea.getMaxTemperature_degC() ){
		else if( v_chpElectricityPrice < ( runningAverage - 3.0 * standardDeviation ) && p_BuildingThermalAsset.getCurrentTemperature() < p_BuildingThermalAsset.getMaxTemperature_degC() - 1 ){
			p_primaryHeatingAsset.v_powerFraction_fr = 0.5;
		}
		else if( v_chpElectricityPrice < ( runningAverage - 1.0 * standardDeviation ) && p_BuildingThermalAsset.getCurrentTemperature() < p_BuildingThermalAsset.getMaxTemperature_degC() - 1 ){
			p_primaryHeatingAsset.v_powerFraction_fr = 0.25;
		}
		else if( v_chpElectricityPrice > ( runningAverage + 4.0 * standardDeviation ) && p_BuildingThermalAsset.getCurrentTemperature() < p_BuildingThermalAsset.getMaxTemperature_degC() - 1 ){
			p_secondaryHeatingAsset.v_powerFraction_fr = 0.5;
		}
		else if( v_chpElectricityPrice > ( runningAverage + 3.0 * standardDeviation ) && p_BuildingThermalAsset.getCurrentTemperature() < p_BuildingThermalAsset.getMaxTemperature_degC() - 1 ){
			p_secondaryHeatingAsset.v_powerFraction_fr = 0.25;
		}		
		else if ( p_BuildingThermalAsset.getCurrentTemperature() < p_BuildingThermalAsset.getSetTemperature_degC() ) {
			p_primaryHeatingAsset.v_powerFraction_fr = 0.5;
			p_secondaryHeatingAsset.v_powerFraction_fr = 0.5;
		}
	break;
	default:
		traceln("ERROR DISTRICT heating wants to use smart assets but does not have correct heating assets");
	break;
}

/*ALCODEEND*/}

double f_connectToJ_EA_DistrictHeating(J_EA j_ea)
{/*ALCODESTART::1693302929918*/
if (j_ea instanceof J_EAProduction ) {
	//c_productionAssets.add(EA);
	if( j_ea.energyAssetType == OL_EnergyAssetType.RESIDUALHEATLT) {
		// handle residual heat not as a direct heat source!
		p_residualHeatLTSource = (J_EAProduction)j_ea;
	}
} else if (j_ea instanceof J_EAConversion) {	
	switch (p_heatingType) {
		case HEATPUMP_GASPEAK:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionHeatPump? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		case GASBURNER:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			//p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		case HYDROGENBURNER:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionHydrogenBurner? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionHydrogenBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		case HEATPUMP_BOILERPEAK:    // ambigue wat we met boiler bedoelen; eboiler of grootschalige DH_boiler = gasburner!
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionHeatPump? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		case GASFIRED_CHPPEAK:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionGasCHP? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		case DISTRICT_EBOILER_CHP:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionElectricHeater? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasCHP? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		
		case LT_RESIDUAL_HEATPUMP_GASPEAK:
			p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionHeatPump? (J_EAConversion)j_ea : p_primaryHeatingAsset;
			p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
		break;
		default:
			throw new IllegalStateException("Invalid DistrictHeating HeatingType: " + p_heatingType);
	}
	
		
	//if (EA.j_ea instanceof J_EAConversionGasBurner || EA.j_ea instanceof J_EAConversionHeatPump || EA.j_ea instanceof J_EAConversionHeatDeliverySet || EA.j_ea instanceof J_EAConversionElectricHeater || EA.j_ea instanceof J_EAConversionGasCHP ) {
	//	if (p_primaryHeatingAsset == null) {
	//		p_primaryHeatingAsset = EA;
	//	} else if (p_secondaryHeatingAsset == null) {
	//		p_secondaryHeatingAsset = EA;
	//	} else {
	//		traceln("District Heating gridconnection already has two heating systems!");
	//	}
	//	//traceln("heatingAsset class " + p_spaceHeatingAsset.getClass().toString());
	
	/*
	if( j_ea instanceof J_EAConversionHeatPump && ((J_EAConversionHeatPump)j_ea).getAmbientTempType().equals("WATER") ) {
		((J_EAConversionHeatPump)j_ea).p_linkedSourceEnergyAsset = p_residualHeatLTSource;
		((J_EAConversionHeatPump)j_ea).setSourceAssetHeatPower( ((J_EAProduction)((J_EAConversionHeatPump)j_ea).p_linkedSourceEnergyAsset).getCapacityHeat_kW() );
	}
	*/
	
//}
} else if (j_ea instanceof J_EAStorageHeat ) {
	p_DHheatStorage = (J_EAStorageHeat)j_ea;
	energyModel.c_storageAssets.add((J_EAStorage)j_ea);
} else {
	traceln("f_connectToChild in EnergyAsset: Exception! EnergyAsset " + j_ea + " is of unknown type or null! ");
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

boolean f_calcCheapestHeatingPrice()
{/*ALCODESTART::1702369244977*/
double HP_COP = ((J_EAConversionHeatPump)p_primaryHeatingAsset).getCOP();
boolean isGasCheaper = false;

if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
	v_gasHeatingCost_eurpkWh_TEMPORARY = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getMethanePrice();
	v_eHeatingCost_eurpkWh_TEMPORARY = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getAveragedElectricityPrice( v_currentPowerElectricity_kW, p_primaryHeatingAsset.getElectricCapacity_kW() ) / HP_COP;
	isGasCheaper = v_gasHeatingCost_eurpkWh_TEMPORARY < v_eHeatingCost_eurpkWh_TEMPORARY ? true:false;
}	

return isGasCheaper;
/*ALCODEEND*/}

double f_heatWithHeatpump()
{/*ALCODESTART::1702369244982*/

double powerFraction_heatPump_fr = 0; 
double powerFraction_heatModel_fr = 0;
double avgElectricityPrice_eurpkWh = 10;
J_EAConversionHeatPump hp = (J_EAConversionHeatPump)p_primaryHeatingAsset;

if ( p_smartHeatingEnabled ) {
	avgElectricityPrice_eurpkWh = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getAveragedElectricityPrice( v_currentPowerElectricity_kW, hp.getElectricCapacity_kW() );
	//traceln("avg electircity rprice for HP: " + avgElectricityPrice_eurpkWh);
}

//heat the house
if (v_currentIndoorTemp_degC < v_tempSetpoint_degC - p_heatingKickinTreshold_degC ) {
	powerFraction_heatPump_fr = 1; 
	powerFraction_heatModel_fr = hp.getHeatCapacity_kW() / p_BuildingThermalAsset.getHeatCapacity_kW();
}
//also heat the house if the price is cheap, the the house is colder than desired + 1 and the vehicle does not need charging
else if( 	p_smartHeatingEnabled && avgElectricityPrice_eurpkWh < v_electricityPriceLowPassed_eurpkWh - p_pricelevelLowDifFromAvg_eurpkWh 
			&& v_currentIndoorTemp_degC < v_tempSetpoint_degC + 1) {
	powerFraction_heatPump_fr = 1;
	powerFraction_heatModel_fr = hp.getHeatCapacity_kW() / p_heatBuffer.getHeatCapacity_kW();
}

p_BuildingThermalAsset.v_powerFraction_fr = powerFraction_heatModel_fr;
p_primaryHeatingAsset.v_powerFraction_fr = powerFraction_heatPump_fr;

/*ALCODEEND*/}

double f_heatWithHybridHeatpump()
{/*ALCODESTART::1702369244984*/

// Heatpump and gasburner, switch based on heatpump COP)
if (v_currentIndoorTemp_degC < ( v_tempSetpoint_degC - p_heatingKickinTreshold_degC) ) {
	
	J_EAConversionHeatPump HP = (J_EAConversionHeatPump)p_primaryHeatingAsset;
	//HP.updateAmbientTemp(main.v_currentAmbientTemperature_degC); // update heatpump temp levels! <-- waarom dit gebeurt al in de main (peter 21-02-23)
	boolean gasCheaper = f_calcCheapestHeatingPrice();
	traceln("isGasCheaper: "+gasCheaper);
	
	if ( gasCheaper ) { // heat with gas
		double powerDemand_kW = (v_tempSetpoint_degC - v_currentIndoorTemp_degC) * ((J_EAStorageHeat)p_BuildingThermalAsset).getHeatCapacity_JpK() / 3.6e6;
		p_primaryHeatingAsset.v_powerFraction_fr = 0;
		p_secondaryHeatingAsset.v_powerFraction_fr = min(1, powerDemand_kW / p_secondaryHeatingAsset.getHeatCapacity_kW());
		p_BuildingThermalAsset.v_powerFraction_fr = p_secondaryHeatingAsset.v_powerFraction_fr * p_secondaryHeatingAsset.getHeatCapacity_kW() / p_BuildingThermalAsset.getHeatCapacity_kW();
	} else { // heat with heatpump
		p_primaryHeatingAsset.v_powerFraction_fr = 1;//min(1,powerDemand_kW / p_primaryHeatingAsset.getHeatCapacity_kW());
		p_secondaryHeatingAsset.v_powerFraction_fr = 0;
		p_BuildingThermalAsset.v_powerFraction_fr = HP.getHeatCapacity_kW() / p_BuildingThermalAsset.getHeatCapacity_kW();
	}
}
else{
	p_primaryHeatingAsset.v_powerFraction_fr = 0;
	p_secondaryHeatingAsset.v_powerFraction_fr = 0;
	p_BuildingThermalAsset.v_powerFraction_fr = 0;
}

p_secondaryHeatingAsset.f_updateAllFlows(p_secondaryHeatingAsset.v_powerFraction_fr);
/*ALCODEEND*/}

