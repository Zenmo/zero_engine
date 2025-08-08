double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1664963959146*/
double availablePowerAtPrice_kW = v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
if (p_owner != null){
	v_currentElectricityPriceConsumption_eurpkWh = p_owner.f_getElectricityPrice( fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	availablePowerAtPrice_kW = p_owner.f_getAvailablePowerAtPrice( fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( v_currentElectricityPriceConsumption_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
} else {
	//v_currentElectricityPriceConsumption_eurpkWh = 0.3;
}

f_manageHeating();

if( p_householdEV != null){
	double availableCapacityFromBatteries = p_batteryAsset == null ? 0 : p_batteryAsset.getCapacityAvailable_kW(); 
	double availableChargingCapacity = v_liveConnectionMetaData.contractedDeliveryCapacity_kW + availableCapacityFromBatteries - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
	//f_maxPowerCharging( max(0, availableChargingCapacity));
	f_manageCharging();
	//v_currentPowerElectricity_kW += v_evChargingPowerElectric_kW;
}

/* // What's this doing here?? Seems like duplicate code.
if( p_batteryAsset != null){
	switch (p_batteryOperationMode){
		case HOUSEHOLD_LOAD:
			f_batteryManagementBalance(v_batterySOC_fr);
		break;
		case PRICE:
			f_batteryManagementPrice(v_batterySOC_fr);
		break;
		default:
		break;
	}
	p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
	v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
}
*/ 
f_manageChargers();

f_manageBattery();
/*ALCODEEND*/}

double f_createThermalStorageModel()
{/*ALCODESTART::1669203453155*/
OL_EAPresetStorageAssets isolationLevel;
switch(p_isolationLabel) {
	case A:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_A;
	break;
	case B:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_B;
	break;
	case C:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_C;
	break;
	case D:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_D;
	break;
	case E:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_E;
	break;
	case F:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_F;
	break;
	case G:
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_G;
	break;
	default:
		traceln("f_createPresetStorageHeat: house without invalid label, label set to C");
		isolationLevel = OL_EAPresetStorageAssets.House_heatmodel_C;
	break;
}

//Get preset input from DB
List< Tuple > data = selectFrom( storage_assets ).where( storage_assets.name.eq( isolationLevel ) ).
list();

OL_EnergyAssetType assetType = null;
String energyAssetPresetName = isolationLevel.toString();
double capacityHeat_kW = 0;
double lossFactor_WpK = 0;
double heatCapacity_JpK = 0;
double minTemperature_degC = 0;
double maxTemperature_degC = 0;
double setTemperature_degC = 0;
String ambientTempType = "";


for( Tuple tup : data ) {
	assetType = tup.get( storage_assets.energy_asset_type );
	capacityHeat_kW = tup.get( storage_assets.capacity_heat_kw );
	lossFactor_WpK =  tup.get( storage_assets.loss_factor_wpk );
	heatCapacity_JpK =  tup.get( storage_assets.heat_capacity_jpk );
	minTemperature_degC = tup.get( storage_assets.min_temp_degc);
	maxTemperature_degC = tup.get( storage_assets.max_temp_degc);
	setTemperature_degC = tup.get( storage_assets.set_temp_degc);
	ambientTempType = tup.get( storage_assets.ambient_temp_type);
}

EnergyAsset e = main.add_pop_energyAssets( null, p_gridConnectionID,  assetType, isolationLevel.toString() );
e.j_ea = new J_EAStorageHeat(e, OL_EAStorageTypes.HEATMODEL_BUILDING, capacityHeat_kW, lossFactor_WpK, energyModel.p_timeStep_h, setTemperature_degC , minTemperature_degC, maxTemperature_degC, setTemperature_degC, heatCapacity_JpK, ambientTempType);
e.f_connectToParentNode( this );
main.c_storageAssets.add(e);
main.c_ambientAirDependentAssets.add(e);

// update static ambient temperature once for underground types
if( ambientTempType != null && ambientTempType.equals("GROUND") ) {
	e.j_ea.updateAmbientTemperature( main.p_undergroundTemperature_degC );
}
if( ambientTempType != null && ambientTempType.equals("AIR") ) {
	e.j_ea.updateAmbientTemperature( main.v_currentAmbientTemperature_degC );
}


/*ALCODEEND*/}

double f_chargeOnPrice_overwrite(double currentElectricityPriceConsumption_eurpkWh,double availablePowerOnGC_kW)
{/*ALCODESTART::1674402121915*/
//J_EAEV EV_instance = (J_EAEV)p_householdEV.j_ea;

double chargingRatio = 0.0; // needs to be set at zero! 
double powerAvailableAtPrice_kW = 0;
double powerAvailableAtLastLoop_kW = 0;
double currentPowerDrawn_kW = v_currentPowerElectricity_kW;
boolean continueLoop = true;
double maxChargingPower_kW = p_householdEV.getElectricCapacity_kW();
ConnectionOwner owner = (ConnectionOwner)l_ownerActor.getConnectedAgent();
double electricityPrice_eurpkWh = 0;
String lastLoopsPriceLevel = "";
OL_priceLevels priceLevel;
int index = 0; // purely to not let loop go wild, if something goes wrong


if( p_householdEV.getAvailability() && p_householdEV.chargingNeed != OL_EVChargingNeed.NONE ){
	while ( continueLoop && index < 5 ){	
		powerAvailableAtPrice_kW += owner.f_getAvailablePowerAtPrice( currentPowerDrawn_kW );
		electricityPrice_eurpkWh = owner.f_getElectricityPrice( currentPowerDrawn_kW );
		priceLevel = f_getPriceLevel( electricityPrice_eurpkWh );
		
		if (powerAvailableAtPrice_kW > p_minChargingPower_kW){ // only charge if there is at least 2 OR 1 kW availablE
			// SCENARIO LOW CHARGING NEED -> Only when price is low
			if ( p_householdEV.chargingNeed == OL_EVChargingNeed.LOW) { 
				if( priceLevel == OL_priceLevels.LOW ) {
					if (powerAvailableAtPrice_kW >= p_householdEV.getElectricCapacity_kW() / p_smartChargingPowerAttenuation_fr ){
						chargingRatio = 1 / p_smartChargingPowerAttenuation_fr;
						continueLoop = false;
					}
					else{
						powerAvailableAtLastLoop_kW = powerAvailableAtPrice_kW;
						currentPowerDrawn_kW += powerAvailableAtPrice_kW;
						lastLoopsPriceLevel = "low";
					}
				}
				else if ( lastLoopsPriceLevel.equals("low")) {
					chargingRatio = min( powerAvailableAtLastLoop_kW, p_householdEV.getElectricCapacity_kW() ) / p_householdEV.getElectricCapacity_kW();
					continueLoop = false;
				}
				else {
					continueLoop = false;
				}
			}
			// SCENARIO MEDIUM CHARGING NEED -> Only when price is low or a bit when medium
			else if ( p_householdEV.chargingNeed == OL_EVChargingNeed.MEDIUM){
				if(  priceLevel == OL_priceLevels.LOW) { 
					if (powerAvailableAtPrice_kW >= p_householdEV.getElectricCapacity_kW() ){
						chargingRatio = 1 ;
						continueLoop = false;
					}
					else{
						powerAvailableAtLastLoop_kW = powerAvailableAtPrice_kW;
						currentPowerDrawn_kW += powerAvailableAtPrice_kW;
						lastLoopsPriceLevel = "low";
					}
				}
				else if(  priceLevel == OL_priceLevels.MEDIUM ) {   
					if( lastLoopsPriceLevel.equals("low") && powerAvailableAtLastLoop_kW > p_minChargingPower_kW ) { // charge what you can at low (/free) price and a bit on medium price
						chargingRatio = powerAvailableAtLastLoop_kW / p_householdEV.getElectricCapacity_kW() + p_householdEV.getElectricCapacity_kW() / 4;
						continueLoop = false;

					}
					else {	//hier zou je nog een extra min() statement bij kunnen zetten die het minimale neemt van de huidige min statment en powerAvailableAtPrice_kW, echter, komt hoogstwaarschijnlijk niet voor (1/4 van laadsnelheid is inprincipe altijd minder dan 2 + 'x' kW.). Ook is het geen ramp om HEEEEEL sporadisch op hoge prijs te laden. 
						chargingRatio = p_householdEV.getElectricCapacity_kW() / 4 ; //charge slow, if there is no hurry and price is average
						continueLoop = false;
					}
				}
				else if (lastLoopsPriceLevel.equals("low") && powerAvailableAtLastLoop_kW > p_minChargingPower_kW ){
					chargingRatio = powerAvailableAtLastLoop_kW / p_householdEV.getElectricCapacity_kW();
					continueLoop = false;
				}
				else{
					continueLoop = false;
				}
			}
			// SCENARIO HIGH CHARGING NEED -> charge full power, otherwise the EV will not get full
			else {
				chargingRatio = 1.0; // Hier kan er boven de gridconnectie geladen worden. Zeker als je 11 kW laders hebt met een warmtepomp erbij
				continueLoop = false;
			}
		}
		else { // Als er minder dan 2 kW beschikbaar is in deze prijsband (i.e. huidige loop), ga nog een keer door de loop heen in de volgende prijsband
			powerAvailableAtLastLoop_kW = powerAvailableAtPrice_kW;
			currentPowerDrawn_kW += powerAvailableAtPrice_kW;
		}
		index ++;
	}
}
if( p_householdEV.chargingNeed != OL_EVChargingNeed.HIGH){ //unless the charging need is high, limit the charging speed to grid connection.
	chargingRatio = min(1, min( availablePowerOnGC_kW / p_householdEV.getElectricCapacity_kW(), chargingRatio));
}
p_householdEV.f_updateAllFlows( chargingRatio );
v_evChargingPowerElectric_kW += p_householdEV.electricityConsumption_kW - p_householdEV.electricityProduction_kW;


/*ALCODEEND*/}

double f_manageCharging_overwrite()
{/*ALCODESTART::1675014184707*/
double availableCapacityFromBatteries = p_batteryAsset == null ? 0 : p_batteryAsset.getCapacityAvailable_kW(); 
//double availableChargingCapacity = v_allowedCapacity_kW + availableCapacityFromBatteries - v_currentPowerElectricity_kW;
double availableChargingCapacity = v_liveConnectionMetaData.contractedDeliveryCapacity_kW + availableCapacityFromBatteries - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
v_vehicleSOC_fr = p_householdEV.getCurrentStateOfCharge_fr();

switch (p_chargingAttitudeVehicles) {
	case SIMPLE:
		f_simpleCharging();
	break;
	case V1G:
	case MAX_SPREAD:
		f_maxSpreadCharging();
	break;
	case CHEAP:
		f_chargeOnPrice( v_currentElectricityPriceConsumption_eurpkWh, max(0, availableChargingCapacity));
	break;	
	case V2G:
		//v_currentElectricityPriceConsumption_eurpkWh = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).f_getElectricityPrice(p_connectionCapacity_kW); 
		//v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( v_currentElectricityPriceConsumption_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		f_chargeOnPrice_V2G( v_currentElectricityPriceConsumption_eurpkWh, max(0, availableChargingCapacity));
	break;	
	default:
		traceln("Incorrect charging mode in household @f_manageCharging_overwrite");
}

/*ALCODEEND*/}

double f_simpleCharging_overwrite()
{/*ALCODESTART::1675033218897*/

double powerFraction_fr = 0;
if( p_householdEV.getCurrentStateOfCharge_fr() < 1 ) {
	powerFraction_fr = 1;
	/*if ( p_hasSmartFlexAssets ){
		ConnectionOwner owner = ((ConnectionOwner)l_ownerActor.getConnectedAgent());
		if (! owner.v_currentCongestionType.equals("Overconsumption") && owner.p_capacityTariffApplicable ){
			chargingRatio = min (1, owner.p_capacityLevel_kW / p_householdEV.getElectricCapacity_kW()); // dont charge faster than the congestion level (although with household demand power drawn will be slightly higher)
		}
	}//*/
}

p_householdEV.f_updateAllFlows(powerFraction_fr);
//v_evChargingPowerElectric_kW += flowsArray[4] - flowsArray[0];//p_householdEV.electricityConsumption_kW - p_householdEV.electricityProduction_kW;

/*ALCODEEND*/}

double f_determineChargingDemandOfEV()
{/*ALCODESTART::1675034695162*/
//J_EAEV EVinstance = (J_EAEV)p_householdEV;
if( p_householdEV != null && p_chargingAttitudeVehicles == OL_ChargingAttitude.CHEAP ){
	if(! p_householdEV.getAvailability() ){ // not at home
		p_householdEV.chargingNeed = OL_EVChargingNeed.EV_NOT_AVAILABLE;
	}
	else if( p_householdEV.getCurrentStateOfCharge() >= 1){ // is full
		p_householdEV.chargingNeed = OL_EVChargingNeed.NONE;
	}
	else { // could use some charging
		double chargeNeedForNextTrip_kWh = max(0, p_householdEV.tripTracker.v_energyNeedForNextTrip_kWh + p_householdEV.getStorageCapacity_kWh() * (p_minEVChargeTarget_fr - p_householdEV.getCurrentStateOfCharge()));
		double timeToNextTrip_min = p_householdEV.tripTracker.v_nextEventStartTime_min - energyModel.t_h*60;
		double chargeDeadline_min = floor((p_householdEV.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / p_householdEV.getElectricCapacity_kW()) / energyModel.p_timeStep_h) * 60 * energyModel.p_timeStep_h;
		if ( chargeNeedForNextTrip_kWh == 0) { 
			p_householdEV.chargingNeed = OL_EVChargingNeed.LOW;
		}
		else if (energyModel.t_h*60 < chargeDeadline_min ){
			//traceln(energyModel.t_h*60 + ", " + chargeDeadline_min);
			//traceln(chargeNeedForNextTrip_kWh);
			p_householdEV.chargingNeed = OL_EVChargingNeed.MEDIUM;
		}
		else {
			p_householdEV.chargingNeed = OL_EVChargingNeed.HIGH;
		}
	}
	v_vehicleChargingNeed = p_householdEV.chargingNeed;
}

/*ALCODEEND*/}

double f_chargeOnPriceSimpler(double availablePowerOnGc_kW)
{/*ALCODESTART::1677664183773*/
double chargingRatio = 0;
if( p_householdEV.getAvailability() && p_householdEV.chargingNeed != OL_EVChargingNeed.NONE ){	
	OL_priceLevels priceLevel = f_getPriceLevel(v_currentAveraged7kWElectricityPrice_eurpkWh);
	if ( p_householdEV.chargingNeed == OL_EVChargingNeed.LOW) { 
		if( priceLevel == OL_priceLevels.LOW ) {
			chargingRatio = 1.0;
		}
	}
	else if ( p_householdEV.chargingNeed == OL_EVChargingNeed.MEDIUM){
		if ( priceLevel == OL_priceLevels.LOW) { 
			chargingRatio = 1.0;
		}
		else if(  priceLevel == OL_priceLevels.MEDIUM ) {
			chargingRatio = p_householdEV.capacityElectric_kW / 4;
		}
	}
	// SCENARIO HIGH CHARGING NEED -> charge full power, otherwise the EV will not get full
	else {
		chargingRatio = 1.0; // Hier kan er boven de gridconnectie geladen worden. Zeker als je 11 kW laders hebt met een warmtepomp erbij
	}
}
if( p_householdEV.chargingNeed != OL_EVChargingNeed.HIGH){ //unless the charging need is high, limit the charging speed to grid connection.
	chargingRatio = min(1, min( availablePowerOnGc_kW / p_householdEV.getElectricCapacity_kW(), chargingRatio));
}
v_evChargingPowerElectric_kW += p_householdEV.ownerAsset.f_updateElectricityFlows( chargingRatio );

/*ALCODEEND*/}

double f_batteryManagementLoad()
{/*ALCODESTART::1678708804201*/
double powerfraction_fr = 0;
if ( v_currentPowerElectricity_kW < 0 && p_batteryAsset.getCurrentStateOfCharge() < 1 && v_currentPriceLevel !=  OL_priceLevels.HIGH ){
	powerfraction_fr = 1;
}
else if( v_currentPowerElectricity_kW > v_currentLoadLowPassed_kW && p_batteryAsset.getCurrentStateOfCharge() > 0){
	powerfraction_fr = -1;
}
else if ( v_batterySOC_fr < 0.6 && v_currentPowerElectricity_kW < 1 && v_currentPriceLevel !=  OL_priceLevels.HIGH){
	powerfraction_fr = min( 1, p_batteryAsset.capacityElectric_kW / 2.5);
}
p_batteryAsset.v_powerFraction_fr = powerfraction_fr;
p_batteryAsset.f_updateAllFlows( p_batteryAsset.v_powerFraction_fr );

/*ALCODEEND*/}

double f_connectTo_J_EA_House(J_EA j_ea)
{/*ALCODESTART::1693300820997*/
/*
if (j_ea instanceof J_EAConversion) {
	if (j_ea.energyAssetType == OL_EnergyAssetType.GAS_BURNER || j_ea instanceof J_EAConversionHeatPump || j_ea instanceof J_EAConversionHeatDeliverySet || j_ea instanceof J_EAConversionElectricHeater ) {
		switch (p_heatingType) {
        	case HEATPUMP_AIR:
        		p_primaryHeatingAsset = (J_EAConversion)j_ea;
        		break;
        	case HEATPUMP_GASPEAK:
				p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionHeatPump? (J_EAConversion)j_ea : p_primaryHeatingAsset;
	            p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
            	break;
            case HEATPUMP_BOILERPEAK:    // ambigue wat we met boiler bedoelen; eboiler of grootschalige DH_boiler = gasburner!
                p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionHeatPump? (J_EAConversion)j_ea : p_primaryHeatingAsset;
                p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
                p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionElectricHeater? (J_EAConversion)j_ea : p_secondaryHeatingAsset;                                          
            	break;
            case GASBURNER:
                p_primaryHeatingAsset = p_primaryHeatingAsset == null && j_ea instanceof J_EAConversionGasBurner? (J_EAConversion)j_ea : p_primaryHeatingAsset;
                p_secondaryHeatingAsset = p_secondaryHeatingAsset == null && j_ea instanceof J_EAConversionGasCHP? (J_EAConversion)j_ea : p_secondaryHeatingAsset;
            	break;
            case DISTRICTHEAT:
                if( j_ea instanceof J_EAConversionHeatDeliverySet ){
					p_primaryHeatingAsset = (J_EAConversion)j_ea;
					//traceln("Assigning heat delivery set as primary heating asset for house!");
				}
				else {
					p_secondaryHeatingAsset = (J_EAConversion)j_ea;
					// set water/water heatpump source energy-asset
					
					//if( j_ea instanceof J_EAConversionHeatPump && ((J_EAConversionHeatPump)j_ea).getAmbientTempType().equals("WATER") && p_primaryHeatingAsset instanceof J_EAConversionHeatDeliverySet ) {
						//((J_EAConversionHeatPump)j_ea).p_linkedSourceEnergyAsset = p_primaryHeatingAsset;
						//j_ea.updateAmbientTemperature( ((J_EAConversionHeatPump)j_ea).p_linkedSourceEnergyAsset.getCurrentTemperature() );
					//}
					
				}	
            	break;
            case LT_DISTRICTHEAT:
            	p_primaryHeatingAsset = (J_EAConversion)j_ea;
            	break;
            default: throw new IllegalStateException("Invalid HeatingType: " + p_heatingType);
      	}
    }
}
*/

if (j_ea instanceof J_EAEV) {
	if (p_householdEV != null){
	    	throw new RuntimeException(String.format("Exception: trying to assign 2 EVs to a household!! --> one of them will not charge! "));
	}
	p_householdEV = (J_EAEV)j_ea;
}
if (j_ea instanceof J_EAAirco) {
	p_airco = (J_EAAirco)j_ea;
	//c_electricHeatpumpAssets.add(j_ea);
}
/*ALCODEEND*/}

double f_setAnnualEnergyDemand()
{/*ALCODESTART::1696923950404*/
traceln("Placeholder function f_setAnnualEnergyDemand called! Nothing will happen.");
/*ALCODEEND*/}

double f_setEnergyLabel()
{/*ALCODESTART::1696924006982*/
traceln("Placeholder function f_setEnergyLabel called! Nothing will happen.");
/*ALCODEEND*/}

double f_removeCurrentHeatingSystem()
{/*ALCODESTART::1726129903799*/
p_heatingType = OL_GridConnectionHeatingType.NONE;
p_primaryHeatingAsset.removeEnergyAsset();
if ( p_secondaryHeatingAsset != null){
	p_secondaryHeatingAsset.removeEnergyAsset();
}
if ( p_tertiaryHeatingAsset != null){
	p_tertiaryHeatingAsset.removeEnergyAsset();
}
if ( p_heatBuffer != null){
	p_heatBuffer.removeEnergyAsset();
}
/*ALCODEEND*/}

double f_manageCookingTracker()
{/*ALCODESTART::1726334759211*/
// Add heat from cooking assets to house
if (p_cookingTracker != null) { // check for presence of cooking asset
	p_cookingTracker.manageActivities((energyModel.t_h-energyModel.p_runStartTime_h)*60); // also calls f_updateAllFlows in HOB asset	
	//v_electricHobConsumption_kW += p_cookingTracker.HOB.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY); // PowerFlows van consumption assets worden in f_calculateEnergyBalance opgeteld, dus ken dit niet toe aan totale consumptie!
	//v_electricHobConsumption_kWh += v_electricHobConsumption_kW * energyModel.p_timeStep_h;
	v_residualHeatGasPit_kW = -p_cookingTracker.HOB.getLastFlows().get(OL_EnergyCarriers.HEAT);
	if (p_BuildingThermalAsset != null) {
		p_BuildingThermalAsset.v_powerFraction_fr += v_residualHeatGasPit_kW / p_BuildingThermalAsset.getCapacityHeat_kW();
	}
}
/*ALCODEEND*/}

double f_manageAirco()
{/*ALCODESTART::1749648447119*/
if( p_airco != null ) {
	if (p_airco.remainingONtimesteps == 0){
		double switchOnProbability = 0;
		switch (roundToInt(energyModel.v_currentAmbientTemperature_degC)) {
			case 23:
				switchOnProbability = 0.0025;
				break;
			case 24:
				switchOnProbability = 0.005;
				break;
			case 25:
				switchOnProbability = 0.008;
				break;
			case 26:
				switchOnProbability = 0.01;
				break;
			case 27:
				switchOnProbability = 0.012;
				break;
			case 28:
				switchOnProbability = 0.014;
				break;
			case 29:
				switchOnProbability = 0.016;
				break;
			case 30:
				switchOnProbability = 0.018;
				break;
			case 31:
				switchOnProbability = 0.02;
				break;
		}
		if( randomTrue(switchOnProbability)){
			int nbTimestepsOn = uniform_discr(4, 12);
			p_airco.turnOnAirco( nbTimestepsOn );
		}
	}
	p_airco.f_updateAllFlows( 1.0 );
}
/*ALCODEEND*/}

double f_removeTheJ_EA_house(J_EA j_ea)
{/*ALCODESTART::1749722407831*/
if (j_ea instanceof J_EAEV) {
	p_householdEV = null;
}
if (j_ea instanceof J_EAAirco) {
	p_airco = null;
	//c_electricHeatpumpAssets.remove(j_ea);
}
/*ALCODEEND*/}

