double f_operateFixedAssets_overwrite()
{/*ALCODESTART::1717956478582*/
for ( J_EAProfile e : c_profileAssets){
	double[] flowsArray = e.f_updateAllFlows( energyModel.t_h );		
	if (e.profileType == OL_ProfileAssetType.CHARGING){
		v_evChargingPowerElectric_kW += flowsArray[4] - flowsArray[0];
	}
	else if( e.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD){
		v_fixedConsumptionElectric_kW += flowsArray[4] - flowsArray[0];
	}
	else {
		traceln( "Profile type is not registered in GCPublicCharger");
	}
	//v_currentPowerElectricity_kW += flowsArray[4];		
}

/*ALCODEEND*/}

double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1726749088568*/
//Manage charging
f_manageCharging();
if ( p_charger != null && v_isActiveCharger ) {
	f_manageCharger();
}
/*ALCODEEND*/}

double f_operateChargerNew()
{/*ALCODESTART::1749648407069*/
if( v_sessionIndex != -1){ //if we didnt reach the last session
	f_manageChargingSessions(); //here we install a new charging session, but if end time of previous overlaps with start time of next there is a problem
}

double ratioOfCapacity = 0;
double chargingPower = 0;
v_shiftedLoadV1G_kW = 0;
v_shiftedLoadV2G_kW = 0;

if (chargingSessionSocket1 != null){
	chargingPower += chargingSessionSocket1.operate();
	v_shiftedLoadV1G_kW += chargingSessionSocket1.getShiftedLoadV1GCurrentTimestep();
	v_shiftedLoadV2G_kW += chargingSessionSocket1.getShiftedLoadV2GCurrentTimestep();
	if ( chargingSessionSocket1.timeStepsToDisconnect == 0 ){
		//traceln("EndTime: " + (chargingSessionSocket1.endTime / 4.0) + ", current timestep: " + energyModel.t_h );
		chargingSessionSocket1 = null;
	}
}

if (chargingSessionSocket2 != null){
	chargingPower += chargingSessionSocket2.operate();
	v_shiftedLoadV1G_kW += chargingSessionSocket2.getShiftedLoadV1GCurrentTimestep();
	v_shiftedLoadV2G_kW += chargingSessionSocket2.getShiftedLoadV2GCurrentTimestep();
	if ( chargingSessionSocket2.timeStepsToDisconnect == 0 ){
		chargingSessionSocket2 = null;
	}
}

ratioOfCapacity = chargingPower / p_chargerAsset.capacityElectric_kW;
if (!v_isActiveCharger){
	ratioOfCapacity = 0;
}

p_chargerAsset.f_updateAllFlows( ratioOfCapacity );

v_totalShiftedLoadV1G_kWh += v_shiftedLoadV1G_kW;
v_totalShiftedLoadV2G_kWh += v_shiftedLoadV2G_kW;

/*ALCODEEND*/}

double f_manageChargingSessions()
{/*ALCODESTART::1749648407072*/
if( energyModel.t_h == v_nextSessionStartTime / 4.0){
	int startIndex = Integer.parseInt(v_nextSessionInfo[0]);
	int endIndex = Integer.parseInt(v_nextSessionInfo[1]);
	double chargingDemand_kWh = Double.parseDouble(v_nextSessionInfo[2]);
	double batteryCap_kWh = Double.parseDouble(v_nextSessionInfo[3]);
	//
	double chargingPower_kW = Double.parseDouble(v_nextSessionInfo[5]);
	int socket = Integer.parseInt(v_nextSessionInfo[6]);
	
	if ( socket == 1){
		if (chargingSessionSocket1 != null){
			traceln("Error, new charging session but socket 1 is not empty, profile: " + p_chargingProfileName + ", startIndex: " + startIndex + ", existing session end index: " + chargingSessionSocket1.endTime);
		}
		chargingSessionSocket1 = new ChargingSession(startIndex, endIndex, chargingDemand_kWh, batteryCap_kWh, chargingPower_kW, socket, randomTrue(energyModel.v_V2GProbability), 0.25);
		//traceln("new session created, start: " + startIndex + ", end: " +endIndex+ ", current timestep: " + energyModel.t_h * 4 + ", timeslots alive: " + chargingSessionSocket1.timeStepsToDisconnect );
	}
	else {
		if (chargingSessionSocket2 != null){
			traceln("Error, new charging session but socket 2 is not empty, profile: " + p_chargingProfileName + ", startIndex: " + startIndex + ", existing session end index: " + chargingSessionSocket2.endTime);
		}
		chargingSessionSocket2 = new ChargingSession(startIndex, endIndex, chargingDemand_kWh, batteryCap_kWh, chargingPower_kW, socket, randomTrue(energyModel.v_V2GProbability), 0.25);
	}
	
	v_sessionIndex ++;
	
	
	f_getNextSessionInfo();
	if ( energyModel.t_h == v_nextSessionStartTime / 4.0 ){
		//traceln("Multiple charging sessions starting at the same timestap in charging profile: " + p_chargingProfileName + " at time: " + v_nextSessionStartTime);
		f_manageChargingSessions(); 
	}
	else if ( energyModel.t_h > v_nextSessionStartTime / 4.0){
		traceln(" charging profile data incorrect, next charging session is in the past");
	}
	
}


/*ALCODEEND*/}

double f_getNextSessionInfo()
{/*ALCODESTART::1749648407074*/
// example: 2/54/50.3/72.1/21.8/10.8/2

String sessie = selectFirstValue(
	"SELECT " + p_chargingProfileName + " FROM chargingsessions WHERE " + 
		"session = ? LIMIT 1;",
		v_sessionIndex);

if( sessie != null){
	v_nextSessionInfo = sessie.split("/"); 
	v_nextSessionStartTime = Integer.parseInt(v_nextSessionInfo[0]);
	//traceln("Profile " + p_chargingProfileName + " next start time: " + v_nextSessionInfo[0]);
}
else{
	v_sessionIndex = -1;
	//traceln( "No new charging session available in data for " + p_chargingProfileName +  ", current session index: " + v_sessionIndex);
}
/*ALCODEEND*/}

double f_manageCharger()
{/*ALCODESTART::1749727270158*/
f_manageSocket1();
f_manageSocket2();

double power_kW = 0.0;
int currentTimeInQuarterHours = roundToInt(energyModel.t_h * 4);
if ( currentTimeInQuarterHours >= v_currentChargingSessionSocket1.startTime && currentTimeInQuarterHours < v_currentChargingSessionSocket1.endTime ) {
	power_kW += f_operateChargerSocket1();
}
if ( currentTimeInQuarterHours >= v_currentChargingSessionSocket2.startTime && currentTimeInQuarterHours < v_currentChargingSessionSocket2.endTime ) {
	power_kW += f_operateChargerSocket2();
}

p_charger.f_updateAllFlows( power_kW / p_charger.capacityElectric_kW );
/*ALCODEEND*/}

double f_operateChargerSocket1()
{/*ALCODESTART::1749728143688*/
double chargingPower_kW = v_currentChargingSessionSocket1.operate();
v_totalShiftedLoadV1G_kWh += v_currentChargingSessionSocket1.getShiftedLoadV1GCurrentTimestep();
v_totalShiftedLoadV2G_kWh += v_currentChargingSessionSocket1.getShiftedLoadV2GCurrentTimestep();
if ( v_currentChargingSessionSocket1.timeStepsToDisconnect == 0 ){
	v_currentChargingSessionSocket1 = null;
}

return chargingPower_kW;
/*ALCODEEND*/}

double f_manageSocket1()
{/*ALCODESTART::1749729536799*/
if ( v_currentChargingSessionSocket1 == null || energyModel.t_h >= v_currentChargingSessionSocket1.endTime / 4.0) {
	 // check if we are not already past the last charging session
	if (v_currentChargingSessionIndexSocket1 >= p_charger.chargerProfile.size()) {
		return;
	}
	while (p_charger.chargerProfile.get(v_currentChargingSessionIndexSocket1).socket != 1) {
		v_currentChargingSessionIndexSocket1++;
		if (v_currentChargingSessionIndexSocket1 >= p_charger.chargerProfile.size()) {
			return;
		}
	}
	v_currentChargingSessionSocket1 = p_charger.chargerProfile.get(v_currentChargingSessionIndexSocket1);
	v_currentChargingSessionIndexSocket1++;
}
/*ALCODEEND*/}

double f_manageSocket2()
{/*ALCODESTART::1749729546273*/
if ( v_currentChargingSessionSocket2 == null || energyModel.t_h >= v_currentChargingSessionSocket2.endTime / 4.0) {
	 // check if we are not already past the last charging session
	if (v_currentChargingSessionIndexSocket2 >= p_charger.chargerProfile.size()) {
		return;
	}
	while (p_charger.chargerProfile.get(v_currentChargingSessionIndexSocket2).socket != 2) {
		v_currentChargingSessionIndexSocket2++;
		if (v_currentChargingSessionIndexSocket2 >= p_charger.chargerProfile.size()) {
			return;
		}
	}
	v_currentChargingSessionSocket2 = p_charger.chargerProfile.get(v_currentChargingSessionIndexSocket2);
	v_currentChargingSessionIndexSocket2++;
}
/*ALCODEEND*/}

double f_operateChargerSocket2()
{/*ALCODESTART::1749730372656*/
double chargingPower_kW = v_currentChargingSessionSocket2.operate();
v_totalShiftedLoadV1G_kWh += v_currentChargingSessionSocket2.getShiftedLoadV1GCurrentTimestep();
v_totalShiftedLoadV2G_kWh += v_currentChargingSessionSocket2.getShiftedLoadV2GCurrentTimestep();
if ( v_currentChargingSessionSocket2.timeStepsToDisconnect == 0 ){
	v_currentChargingSessionSocket2 = null;
}

return chargingPower_kW;
/*ALCODEEND*/}

double f_connectToJ_EA_custom(J_EA j_ea)
{/*ALCODESTART::1749730638242*/
if (j_ea instanceof J_EACharger) {
	p_charger = (J_EACharger)j_ea;
}
/*ALCODEEND*/}

