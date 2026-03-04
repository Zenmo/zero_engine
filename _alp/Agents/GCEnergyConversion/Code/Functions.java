double f_manageElektrolyserOUD(J_EAConversionElektrolyser ElektrolyserAsset)
{/*ALCODESTART::1707149702398*/
if (ElektrolyserAsset.getElectricCapacity_kW()>0) {
	
	//double availableCapacityFromBatteries_kW = p_batteryAsset == null ? 0 : ((J_EAStorageElectric)p_batteryAsset.j_ea).getCapacityAvailable_kW(); 
	double availableElectricPower_kW = v_allowedCapacity_kW - v_currentPowerElectricity_kW;
	double excessElectricPower_kW = -(v_currentPowerElectricity_kW + v_allowedCapacity_kW); // Should at least draw this much power to stay within connection limits. Doesn't account for a battery!
	double eta_r = ElektrolyserAsset.getEta_r();
	double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	double ProductionSetpoint_kW = ElektrolyserAsset.getElectricCapacity_kW() * 0.30 * eta_r; // Aim for average production power of xx% of elektrolyser peak power
	double elektrolyserSetpointElectric_kW = 0;
	
	if (p_elektrolyserOperationMode==OL_ElektrolyserOperationMode.BALANCE) {
		double FeedbackGain_kWpkWh = 1.0/24; // Try to recover deficit in 24 hours
		elektrolyserSetpointElectric_kW = ProductionSetpoint_kW/eta_r + v_hydrogenProductionDeficit_kWh * FeedbackGain_kWpkWh /eta_r ;
	
		//traceln("Elektrolyser electric power setpoint: " + elektrolyserSetpointElectric_kW + " kW");
		//traceln("Elektrolyser power fraction: " + elektrolyserSetpointElectric_kW/ElektrolyserAsset.j_ea.getElectricCapacity_kW());
	} else if (p_elektrolyserOperationMode==OL_ElektrolyserOperationMode.PRICE) {
		//if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		ConnectionOwner ownerActor = (ConnectionOwner)l_ownerActor.getConnectedAgent();
		//double currentElectricityPriceCharge_eurpkWh = ownerActor.v_priceBandsDelivery.ceilingEntry(100.0).getValue(); // query price at 1kW
		double currentElectricityPriceCharge_eurpkWh = ownerActor.f_getElectricityPrice(-excessElectricPower_kW); // query price at 1kW
		//traceln("Current electricity price for electrolyser: " + currentElectricityPriceCharge_eurpkWh);
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		double deficitGain_eurpkWh = 1.0/1000000; // When SOC-error is 100%, adjust WTP price by 1 eurpkWh
		double priceGain_peur = 5; // How strongly to ramp up power with price-delta's	
		double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + deficitGain_eurpkWh * v_hydrogenProductionDeficit_kWh;
		elektrolyserSetpointElectric_kW = max(0,ElektrolyserAsset.getElectricCapacity_kW()*(WTP_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_peur)  ;
		//traceln("WTP hydrogen production is " + roundToDecimal(WTP_eurpkWh,3) + " eurpkWh is higher than electricity price " + roundToDecimal(currentElectricityPriceCharge_eurpkWh,3) + " eurpkWh, so produce! Setpoint power: " + round(elektrolyserSetpointElectric_kW) + " kW");
	}	
	
	// Limit elektrolyser power to available electric power on connection (assuming it is last in merit!)
	elektrolyserSetpointElectric_kW = min(availableElectricPower_kW,max(elektrolyserSetpointElectric_kW,excessElectricPower_kW));
	
	double[] flowsArray = ElektrolyserAsset.f_updateAllFlows(elektrolyserSetpointElectric_kW/ElektrolyserAsset.getElectricCapacity_kW());
	
	v_conversionPowerElectric_kW += flowsArray[4];
	//v_hydrogenElectricityConsumption_kW += flowsArray[4];	
	
	v_hydrogenProductionDeficit_kWh += ProductionSetpoint_kW - ElektrolyserAsset.hydrogenProduction_kW;	// Update hydrogen production deficit
}
/*ALCODEEND*/}

double f_manageElectrolyser(J_EAConversionElectrolyser ElectrolyserAsset,J_TimeVariables timeVariables)
{/*ALCODESTART::1708089250229*/
// TODO: add timeParameters to this function?

if (ElectrolyserAsset.getInputCapacity_kW()>0) {
	//double availableCapacityFromBatteries_kW = p_batteryAsset == null ? 0 : ((J_EAStorageElectric)p_batteryAsset.j_ea).getCapacityAvailable_kW(); 
	
	double v_allowedDeliveryCapacity_kW = v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
	double v_allowedFeedinCapacity_kW = v_liveConnectionMetaData.contractedFeedinCapacity_kW;
	double availableElectricPower_kW = max(0, v_allowedDeliveryCapacity_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	double excessElectricPower_kW = max(0, - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_allowedFeedinCapacity_kW);
	
	double eta_r = ElectrolyserAsset.getEta_r();
	//double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	double ProductionSetpoint_kW = ElectrolyserAsset.getInputCapacity_kW() * 0.30 * eta_r; // Aim for average production power of xx% of elektrolyser peak power
	double electrolyserSetpointElectric_kW = 0;
	
	
	//Electrolyser output based on current and future regime
	electrolyserSetpointElectric_kW = f_electrolyserRegime(electrolyserSetpointElectric_kW, excessElectricPower_kW, ElectrolyserAsset, energyModel.p_timeParameters, timeVariables);
	
	data_liveWeekElectrolyserPower_kW.add(timeVariables.getT_h(), electrolyserSetpointElectric_kW);
		
	/*
	if (p_elektrolyserOperationMode==OL_ElektrolyserOperationMode.BALANCE) {
		double FeedbackGain_kWpkWh = 1.0/24; // Try to recover deficit in 24 hours
		elektrolyserSetpointElectric_kW = ProductionSetpoint_kW/eta_r + v_hydrogenProductionDeficit_kWh * FeedbackGain_kWpkWh /eta_r ;
	
		//traceln("Elektrolyser electric power setpoint: " + elektrolyserSetpointElectric_kW + " kW");
		//traceln("Elektrolyser power fraction: " + elektrolyserSetpointElectric_kW/ElektrolyserAsset.j_ea.getElectricCapacity_kW());
	} else if (p_elektrolyserOperationMode==OL_ElektrolyserOperationMode.PRICE) {
		//if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		ConnectionOwner ownerActor = (ConnectionOwner)l_ownerActor.getConnectedAgent();
		//double currentElectricityPriceCharge_eurpkWh = ownerActor.v_priceBandsDelivery.ceilingEntry(100.0).getValue(); // query price at 1kW
		double currentElectricityPriceCharge_eurpkWh = ownerActor.f_getElectricityPrice(-excessElectricPower_kW); // query price at 1kW
		//traceln("Current electricity price for electrolyser: " + currentElectricityPriceCharge_eurpkWh);
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		double deficitGain_eurpkWh = 1.0/1000000; // When SOC-error is 100%, adjust WTP price by 1 eurpkWh
		double priceGain_peur = 5; // How strongly to ramp up power with price-delta's	
		double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + deficitGain_eurpkWh * v_hydrogenProductionDeficit_kWh;
		elektrolyserSetpointElectric_kW = max(0,ElektrolyserAsset.getElectricCapacity_kW()*(WTP_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_peur)  ;
		elektrolyserSetpointElectric_kW = random()*ElektrolyserAsset.getElectricCapacity_kW();
		//traceln("WTP hydrogen production is " + roundToDecimal(WTP_eurpkWh,3) + " eurpkWh is higher than electricity price " + roundToDecimal(currentElectricityPriceCharge_eurpkWh,3) + " eurpkWh, so produce! Setpoint power: " + round(elektrolyserSetpointElectric_kW) + " kW");
	}	
	*/
	
	// Limit elektrolyser power to available electric power on connection (assuming it is last in merit!)
	electrolyserSetpointElectric_kW = min(availableElectricPower_kW,max(electrolyserSetpointElectric_kW, excessElectricPower_kW));
	
	f_updateFlexAssetFlows(ElectrolyserAsset, electrolyserSetpointElectric_kW/ElectrolyserAsset.getInputCapacity_kW(), timeVariables);
	
	//v_conversionPowerElectric_kW += ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
	//v_hydrogenElectricityConsumption_kW += ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
	
	v_hydrogenProductionDeficit_kWh += ProductionSetpoint_kW - max(0,-ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN));	// Update hydrogen production deficit	
	
	
	// This variable is reset after a rapidrun, but not saved before running the headless simulation.
	v_hydrogenInStorage_kWh -= ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN) * energyModel.p_timeParameters.getTimeStep_h();
	
	/*if (b_useHydrogenLocally) {
		List<GridConnection> receivingGCs = findAll(energyModel.c_gridConnections, gc -> gc.b_useHydrogenLocally && gc != this);
		//traceln("hydrogen reveicing gcs: %s", receivingGCs);
		for (GridConnection gc : receivingGCs) {
			
			gc.v_hydrogenInStorage_kWh += energyModel.p_timeStep_h * flowsArray[2] / receivingGCs.size();					
		}
	}*/

}
/*ALCODEEND*/}

double f_operateFlexAssets_override(J_TimeVariables timeVariables)
{/*ALCODESTART::1708089644411*/
for( J_EA v : c_conversionAssets ){
	if (v instanceof J_EAConversionElectrolyser) {
		f_manageElectrolyser((J_EAConversionElectrolyser)v, timeVariables);
	}
}
/*ALCODEEND*/}

double f_electrolyserRegime(double elektrolyserSetpointElectric_kW,double excessElectricPower_kW,J_EAConversionElectrolyser ElectrolyserAsset,J_TimeParameters timeParameters,J_TimeVariables timeVariables)
{/*ALCODESTART::1708447959640*/
double solar_forecast_kW;
double wind_forecast_kW;

double forecast_time_h = ElectrolyserAsset.getStartUpTimeStandby_h()+ 2*timeParameters.getTimeStep_h();

//Get current limitation values
//Pair<J_FlowsMap, Double> flowsPair = ElectrolyserAsset.getLastFlows();
J_FlowsMap flowsMap = ElectrolyserAsset.getLastFlows();
double previousElectrolyserConsumption_kW = max(0,flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
double currentGridNodePowerFlow_kW = this.p_parentNodeElectric.v_currentLoad_kW - previousElectrolyserConsumption_kW;
data_liveWeekGridNoderPowerFlow_kW.add(timeVariables.getT_h(), this.p_parentNodeElectric.v_currentLoad_kW);


switch (p_electrolyserOperationMode){

	case PRICE:
		f_electrolyserRegimeControl_Price(excessElectricPower_kW, ElectrolyserAsset);
	break;
	case BALANCE:
		f_electrolyserRegimeControl_Balance(currentGridNodePowerFlow_kW, forecast_time_h, ElectrolyserAsset, timeParameters, timeVariables);
	break;	
	case ALWAYS_IDLE:
		f_electrolyserRegimeControl_AlwaysIdle(currentGridNodePowerFlow_kW, ElectrolyserAsset);
	break;
}

switch (ElectrolyserAsset.getState()){

	case SHUTDOWN:
		elektrolyserSetpointElectric_kW = 0;
		
		if(energyModel.v_isRapidRun){
		v_totalDownTimeElectrolyser_hr = v_totalDownTimeElectrolyser_hr + timeParameters.getTimeStep_h();
		}
	break;
	
	case STANDBY: 
		elektrolyserSetpointElectric_kW = 0;
		
		if(energyModel.v_isRapidRun){
		v_totalDownTimeElectrolyser_hr = v_totalDownTimeElectrolyser_hr + timeParameters.getTimeStep_h();
		}
	break;
	
	case IDLE: 
		elektrolyserSetpointElectric_kW = 0.025*ElectrolyserAsset.getInputCapacity_kW(); // 1 - 5 percent of nominal load to keep it warm!
		
		if(energyModel.v_isRapidRun){
			v_totalDownTimeElectrolyser_hr = v_totalDownTimeElectrolyser_hr + timeParameters.getTimeStep_h();
			v_totalEnergyLossIdle_kWh = v_totalEnergyLossIdle_kWh + elektrolyserSetpointElectric_kW * timeParameters.getTimeStep_h();
		}
	break;
	
	case FUNCTIONAL:
		switch (p_electrolyserOperationMode){
		
			case PRICE:
				elektrolyserSetpointElectric_kW = ElectrolyserAsset.getInputCapacity_kW()*p_minProductionRatio;
			break;
			
			case BALANCE:
				elektrolyserSetpointElectric_kW = ElectrolyserAsset.getInputCapacity_kW()*p_minProductionRatio;
			break;
			case ALWAYS_IDLE:
				elektrolyserSetpointElectric_kW = ElectrolyserAsset.getInputCapacity_kW()*p_minProductionRatio;
			break;
		}
	break;
	
	case FULLCAPACITY:
		switch (p_electrolyserOperationMode){
		
			case PRICE:
				elektrolyserSetpointElectric_kW = ElectrolyserAsset.getInputCapacity_kW();
			break;
			
			case BALANCE:
				elektrolyserSetpointElectric_kW = min(ElectrolyserAsset.getInputCapacity_kW(), abs(v_gridNodeCongestionLimit_kW - currentGridNodePowerFlow_kW));
			break;
			case ALWAYS_IDLE:
				elektrolyserSetpointElectric_kW = min(ElectrolyserAsset.getInputCapacity_kW(), abs(v_gridNodeCongestionLimit_kW - currentGridNodePowerFlow_kW));
			break;
		}
	break;
}


return elektrolyserSetpointElectric_kW;
/*ALCODEEND*/}

double f_electrolyserRegimeControl_Price(double excessElectricPower_kW,J_EAConversionElectrolyser ElectrolyserAsset)
{/*ALCODESTART::1708448673879*/
ConnectionOwner ownerActor = p_owner;
//double currentElectricityPriceCharge_eurpkWh = ownerActor.f_getElectricityPrice(-excessElectricPower_kW); // query price at 1kW
double currentElectricityPriceEPEX_eurpkWh = energyModel.pf_dayAheadElectricityPricing_eurpMWh.getForecast();

switch (ElectrolyserAsset.getState()){

	case SHUTDOWN:
		if (currentElectricityPriceEPEX_eurpkWh < v_electricityPriceMaxForProfit_eurpkWh){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.SHUTDOWN);
		}
	break;

	case STANDBY: 
		if (currentElectricityPriceEPEX_eurpkWh < v_electricityPriceMaxForProfit_eurpkWh){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
		else{
			ElectrolyserAsset.setElectrolyserState( OL_ElectrolyserState.STANDBY);
		}
	break;

	case IDLE: 
		if (currentElectricityPriceEPEX_eurpkWh < v_electricityPriceMaxForProfit_eurpkWh){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;

	case FUNCTIONAL:
		if (currentElectricityPriceEPEX_eurpkWh < v_electricityPriceMaxForProfit_eurpkWh){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;
	
	case FULLCAPACITY:
		if (currentElectricityPriceEPEX_eurpkWh < v_electricityPriceMaxForProfit_eurpkWh){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
		}
	break;
}



/*ALCODEEND*/}

double f_manageFuelCell()
{/*ALCODESTART::1709045117969*/

//if (FuelCellAsset.getElectricCapacity_kW()>0) {


//}





/*ALCODEEND*/}

double f_electrolyserRegimeControl_Balance(double currentGridNodePowerFlow_kW,double forecast_time_h,J_EAConversionElectrolyser ElectrolyserAsset,J_TimeParameters timeParameters,J_TimeVariables timeVariables)
{/*ALCODESTART::1715611921617*/
double solar_forecast_kW;
double wind_forecast_kW;

//Initialize limitation values
if (c_forecast_RES_kW.size() == 0){
		
	for(int i = energyModel.v_timeStepsElapsed; i < energyModel.v_timeStepsElapsed + roundToInt(forecast_time_h/timeParameters.getTimeStep_h()); i++){
		solar_forecast_kW = - energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h()) * energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW;
		wind_forecast_kW = - energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + i*timeParameters.getTimeStep_h()) * energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW;
		
		c_forecast_RES_kW.add(solar_forecast_kW + wind_forecast_kW);
		
		c_forecast_gridNodePowerFlow_kW.add(currentGridNodePowerFlow_kW - c_forecast_RES_kW.get(0) + solar_forecast_kW + wind_forecast_kW);
		
	}
}

//Get future limitation values
else if(energyModel.v_timeStepsElapsed < (8760-forecast_time_h)/timeParameters.getTimeStep_h()){
	
	//Get current RES production
	double currentRESProduction_kW = c_forecast_RES_kW.get(0);
	
	//Update forecast array RES
	c_forecast_RES_kW.remove(0);
	
	solar_forecast_kW = - energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + forecast_time_h) * energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW;
	wind_forecast_kW = - energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + forecast_time_h) * energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW;
	
	c_forecast_RES_kW.add(solar_forecast_kW + wind_forecast_kW); 
	
	//Update forecast array Grid node power flow
	c_forecast_gridNodePowerFlow_kW.remove(0);
	
	//Get past grid node power flow and weather (last week) if last week forecast prediction is selected.
	if (b_forecast_lastWeekBased && data_liveWeekElectrolyserPower_kW.size() > 672 - roundToInt(forecast_time_h/timeParameters.getTimeStep_h())){ // Use last week to create the forecast	
	
		double lastWeekGridNodePowerFlow_kW = data_liveWeekGridNoderPowerFlow_kW.getY(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())) - data_liveWeekElectrolyserPower_kW.getY(roundToInt(forecast_time_h/timeParameters.getTimeStep_h()));
		double solar_lastWeek_kW = - energyModel.pp_PVProduction35DegSouth_fr.getValue(timeVariables.getT_h() + forecast_time_h - 168) * energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW;
		double wind_lastWeek_kW = - energyModel.pp_windProduction_fr.getValue(timeVariables.getT_h() + forecast_time_h - 168) * energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW;
			
		c_forecast_gridNodePowerFlow_kW.add(lastWeekGridNodePowerFlow_kW - solar_lastWeek_kW - wind_lastWeek_kW + solar_forecast_kW + wind_forecast_kW);
	}
	else{//use current power flow to predict forecast
		c_forecast_gridNodePowerFlow_kW.add(currentGridNodePowerFlow_kW - currentRESProduction_kW + c_forecast_RES_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-1));
	}
}


//Set state based on current state and forecast.
switch (ElectrolyserAsset.getState()){

	case SHUTDOWN: //Not ready to be powerd up and complete shut down (when broken, maintenance, etc.)
	
	break;

	case STANDBY: // Ready to be powered on, but no electricity consumption.
		//Check if electrolyser will be able to be functional at least two time steps when powering up, if so: power_up = true.
		if (c_forecast_gridNodePowerFlow_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-2) < v_gridNodeCongestionLimit_kW && c_forecast_gridNodePowerFlow_kW.get(roundToInt(forecast_time_h/timeParameters.getTimeStep_h())-1) < v_gridNodeCongestionLimit_kW){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.POWER_UP);
			ElectrolyserAsset.setRemainingPowerUpDuration_timesteps(roundToInt(ElectrolyserAsset.getStartUpTimeStandby_h()/timeParameters.getTimeStep_h()));
		}
	break;
	
	case POWER_UP:
		if(ElectrolyserAsset.getRemainingPowerUpDuration_timesteps() == 0){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;

	case IDLE: // Ready to start producting hydrogen, heated up, so consuming electricity, but not producing hydrogen yet.
		if ( currentGridNodePowerFlow_kW < v_gridNodeCongestionLimit_kW ){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
		}
		else{
			boolean power_down = true;
			for(int i = 0; i < c_forecast_gridNodePowerFlow_kW.size() - 2; i++){
				if (c_forecast_gridNodePowerFlow_kW.get(i) < v_gridNodeCongestionLimit_kW){
					power_down = false;
				}
			}
		
			//Set mode based on power down or not.
			if (power_down){
				ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.STANDBY);
			}
		}
	break;

	case FUNCTIONAL: // Producing hydrogen at minimum possible amount (to make sure system is working correctly, no leaks).
		if ( currentGridNodePowerFlow_kW < v_gridNodeCongestionLimit_kW ){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;
	
	case FULLCAPACITY:	// Producing hydrogen as much as possible.
		if ( currentGridNodePowerFlow_kW < v_gridNodeCongestionLimit_kW ){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;
}

/*ALCODEEND*/}

double f_resetSpecificGCStates_override()
{/*ALCODESTART::1717066943639*/
//Reset variables 
v_totalDownTimeElectrolyser_hr = 0;
v_totalEnergyLossIdle_kWh = 0;
v_producedOxygen_kg = 0;
v_hydrogenInStorage_kWh = 0;


/*ALCODEEND*/}

double f_resetSpecificGCStatesAfterRapidRun_override()
{/*ALCODESTART::1717068271650*/
//Rest forecast collections
c_forecast_RES_kW.clear();
c_forecast_gridNodePowerFlow_kW.clear();


//Reset dataset
data_liveWeekElectrolyserPower_kW.reset();
data_liveWeekGridNoderPowerFlow_kW.reset();

//Reset variables
v_hydrogenProductionDeficit_kWh = 0;
v_hydrogenInStorage_kWh = 0;
/*ALCODEEND*/}

double f_electrolyserRegimeControl_AlwaysIdle(double currentGridNodePowerFlow_kW,J_EAConversionElectrolyser ElectrolyserAsset)
{/*ALCODESTART::1717139694847*/
switch (ElectrolyserAsset.getState()){

	case SHUTDOWN: //Not ready to be powerd up and complete shut down (when broken, maintenance, etc.)
	break;

	case STANDBY: // Ready to be powered on, but no electricity consumption.
		ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.POWER_UP);
	break;
	
	case POWER_UP:
		ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
	break;

	case IDLE: // Ready to start producting hydrogen, heated up, so consuming electricity, but not producing hydrogen yet.
		if ( currentGridNodePowerFlow_kW < v_gridNodeCongestionLimit_kW ){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FUNCTIONAL);
		}
	break;

	case FUNCTIONAL: // Producing hydrogen at minimum possible amount (to make sure system is working correctly, no leaks).
		if ( currentGridNodePowerFlow_kW < v_gridNodeCongestionLimit_kW ){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;
	
	case FULLCAPACITY:	// Producing hydrogen as much as possible.
		if ( currentGridNodePowerFlow_kW < v_gridNodeCongestionLimit_kW ){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.FULLCAPACITY);
		}
		else{
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.IDLE);
		}
	break;
}

/*ALCODEEND*/}

double f_manageFuelCell_OLD_Utility()
{/*ALCODESTART::1770636052220*/
// Arbitrarely i'm deciding not to use more than 95% of the GC & GN capacity.
double capacityLimit_fr = 0.95;
if (fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) > v_liveConnectionMetaData.contractedDeliveryCapacity_kW * capacityLimit_fr || fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) > p_parentNodeElectric.p_capacity_kW * capacityLimit_fr) {
	J_EAConversionFuelCell fuelCellAsset = (J_EAConversionFuelCell) findFirst(c_conversionAssets, j_ea -> j_ea.getEAType() == OL_EnergyAssetType.FUEL_CELL);
	if (fuelCellAsset == null) {
		traceln("No fuel cell asset found");
	}
	else {
		double powerNeeded_kW = max(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_liveConnectionMetaData.contractedDeliveryCapacity_kW * capacityLimit_fr,  fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - p_parentNodeElectric.p_capacity_kW * capacityLimit_fr);
		// For now i've assumed the only fuel cells being used are with a capacity of 1 MW and efficieny of 50%.
		double efficiency = fuelCellAsset.getEta_r();
		double ratioOfCapacity = powerNeeded_kW / (fuelCellAsset.getOutputCapacity_kW()*efficiency);
		
		// Check the amount of Hydrogen that has been generated so far
		// Only works because there is a single energy conversion site
		if (energyModel.EnergyConversionSites.get(0).v_hydrogenInStorage_kWh > 0 ) {
			// Calling operate directly instead of updateAllFlows, so that it's not bounded
			//Pair<J_FlowsMap, Double> flowsPair = fuelCellAsset.operate(ratioOfCapacity);
			fuelCellAsset.f_updateAllFlows(ratioOfCapacity);
			//traceln("fuel cell operated: " + Arrays.toString(arr));
			
			// Since not calling updateAllFlows, have to manually do this
			//double energyUse_kW = - flowsMap.values().stream().mapToDouble(Double::doubleValue).sum();
			//f_addFlows(flowsMap, flowsPair.getSecond(), fuelCellAsset);
			
			// updating other variables
			//v_currentPowerHydrogen_kW += arr[0];
			//v_hydrogenInStorage_kWh -= arr[6] * energyModel.p_timeStep_h;
			energyModel.EnergyConversionSites.get(0).v_hydrogenInStorage_kWh -= fuelCellAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN) * energyModel.p_timeStep_h;
			if (energyModel.v_isRapidRun) {
				v_totalHydrogenUsed_MWh += fuelCellAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN) * energyModel.p_timeStep_h / 1000;
				if (fuelCellAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN) > v_maxHydrogenPower_kW) {
					v_maxHydrogenPower_kW = fuelCellAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN);
				}
			}
		}
	}
}
/*ALCODEEND*/}

