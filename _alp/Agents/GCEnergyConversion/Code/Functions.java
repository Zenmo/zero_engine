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

double f_operateFlexAssets_overwriteOUD()
{/*ALCODESTART::1707149769851*/
for( J_EA v : c_conversionAssets ){
	if (v instanceof J_EAConversionElektrolyser) {
		f_manageElektrolyser((J_EAConversionElektrolyser)v);
	}
	if (v instanceof J_EAConversionCurtailer) {
		// Must go last! 
	} /*else {		
		v_currentPowerElectricity_kW += v.electricityConsumption_kW - v.electricityProduction_kW;
		v_conversionPowerElectric_kW += v.electricityConsumption_kW - v.electricityProduction_kW;
		v_currentPowerMethane_kW += v.methaneConsumption_kW - v.methaneProduction_kW;
		v_currentPowerHydrogen_kW += v.hydrogenConsumption_kW - v.hydrogenProduction_kW;
		v_currentPowerHeat_kW += v.heatConsumption_kW - v.heatProduction_kW;
		v_currentPowerDiesel_kW += v.dieselConsumption_kW;
	} */
}

// Determine EV charging
f_manageCharging();
//v_currentPowerElectricity_kW += v_evChargingPowerElectric_kW;

// Operate battery
if (p_batteryAsset != null){
	v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
	if( p_batteryOperationMode == OL_BatteryOperationMode.BALANCE) {
		f_batteryManagementBalanceCoop( v_batterySOC_fr );
	}
	else {
		f_batteryManagementPrice( v_batterySOC_fr );
	}
	p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
	//v_batteryPowerElectric_kW = p_batteryAsset.electricityConsumption_kW - p_batteryAsset.electricityProduction_kW;
	//v_currentPowerElectricity_kW += v_batteryPowerElectric_kW;
}

// Operate curtailer. Must be the last asset to run!!

if (p_curtailer != null){
	f_manageCurtailer(p_curtailer);
	//v_currentPowerElectricity_kW += p_curtailer.electricityConsumption_kW;
}
/*ALCODEEND*/}

double f_batteryManagementBalanceCoop(double batterySOC)
{/*ALCODESTART::1707149801187*/
if ((p_batteryAsset).getStorageCapacity_kWh() != 0){
	if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		if(((ConnectionOwner)l_ownerActor.getConnectedAgent()).p_coopParent instanceof EnergyCoop ) {
			//traceln("Hello?");
//			v_previousPowerElectricity_kW = p_batteryAsset.v_powerFraction_fr * p_batteryAsset.j_ea.getElectricCapacity_kW();
			v_previousPowerElectricity_kW = p_batteryAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
			//traceln("Previous battery power: " + v_previousPowerElectricity_kW);
			double currentCoopElectricitySurplus_kW = ((ConnectionOwner)l_ownerActor.getConnectedAgent()).p_coopParent.v_electricitySurplus_kW + v_previousPowerElectricity_kW;
			//v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentCoopElectricitySurplus_kW - v_electricityPriceLowPassed_eurpkWh );
			
			double CoopConnectionCapacity_kW = 0.9*((ConnectionOwner)l_ownerActor.getConnectedAgent()).p_coopParent.v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
			double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity
			double availableDischargePower_kW = currentCoopElectricitySurplus_kW - CoopConnectionCapacity_kW; // Max discharging power within grid capacity
			double SOC_setp_fr = 0.5;			
			if (energyModel.v_totalInstalledWindPower_kW > 10000) {
				SOC_setp_fr = 0.95 - 0.95 * energyModel.v_WindYieldForecast_fr - 0.9*energyModel.v_SolarYieldForecast_fr;
				//traceln("Forecast-based SOC setpoint: " + SOC_setp_fr + " %");
			} else {
				SOC_setp_fr = (0.5 + 0.35 * Math.sin(2*Math.PI*(energyModel.t_h-10)/24))*(1-0.8*energyModel.v_WindYieldForecast_fr); // Sinusoidal setpoint: aim for low SOC at 6:00h, high SOC at 18:00h. 
			}
			double FeedbackGain_kWpSOC = 1.5 * p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
			double FeedforwardGain_kWpKw = 0.1; // Feedforward based on current surpluss in Coop
			double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
			double chargeSetpoint_kW = 0;
			chargeSetpoint_kW = FeedforwardGain_kWpKw * (currentCoopElectricitySurplus_kW - chargeOffset_kW) + (SOC_setp_fr - batterySOC) * FeedbackGain_kWpSOC;
			chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
			p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
			//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
		}
	}
}
/*ALCODEEND*/}

double f_manageCurtailer(J_EAConversionCurtailer CurtailerAsset)
{/*ALCODESTART::1707149873038*/
//traceln("Hello! " + CurtailerAsset.j_ea.getElectricCapacity_kW());
if (CurtailerAsset.getElectricCapacity_kW()>0) {
	double curtailerSetpointElectric_kW = -min(0,v_currentPowerElectricity_kW + p_connectionCapacity_kW);
	CurtailerAsset.f_updateAllFlows(curtailerSetpointElectric_kW/CurtailerAsset.getElectricCapacity_kW());
	
	/*if ( curtailerSetpointElectric_kW > 0 ) {
		traceln("Windfarm is curtailing " + curtailerSetpointElectric_kW + " kW!");
	}*/
}
/*ALCODEEND*/}

double f_manageElectrolyser(J_EAConversionElectrolyser ElectrolyserAsset)
{/*ALCODESTART::1708089250229*/

if (ElectrolyserAsset.getInputCapacity_kW()>0) {
	//double availableCapacityFromBatteries_kW = p_batteryAsset == null ? 0 : ((J_EAStorageElectric)p_batteryAsset.j_ea).getCapacityAvailable_kW(); 
	
	double v_allowedDeliveryCapacity_kW = p_contractedDeliveryCapacity_kW;
	double v_allowedFeedinCapacity_kW = p_contractedFeedinCapacity_kW;
	double availableElectricPower_kW = max(0, v_allowedDeliveryCapacity_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	double excessElectricPower_kW = max(0, - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - v_allowedFeedinCapacity_kW);
	
	double eta_r = ElectrolyserAsset.getEta_r();
	//double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	double ProductionSetpoint_kW = ElectrolyserAsset.getInputCapacity_kW() * 0.30 * eta_r; // Aim for average production power of xx% of elektrolyser peak power
	double electrolyserSetpointElectric_kW = 0;
	
	
	//Electrolyser output based on current and future regime
	electrolyserSetpointElectric_kW = f_electrolyserRegime(electrolyserSetpointElectric_kW, excessElectricPower_kW, ElectrolyserAsset);
	
	data_liveWeekElectrolyserPower_kW.add(energyModel.t_h, electrolyserSetpointElectric_kW);
		
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
	
	
	
	//Output
	//double[] flowsArray = ElectrolyserAsset.f_updateAllFlows(electrolyserSetpointElectric_kW/ElectrolyserAsset.getElectricCapacity_kW());
	//Pair<J_FlowsMap, Double> flowsPair = ElectrolyserAsset.f_updateAllFlows(electrolyserSetpointElectric_kW/ElectrolyserAsset.getCapacityElectric_kW());
	ElectrolyserAsset.f_updateAllFlows(electrolyserSetpointElectric_kW/ElectrolyserAsset.getInputCapacity_kW());
		
	v_conversionPowerElectric_kW += ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
	v_hydrogenElectricityConsumption_kW += ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
	
	v_hydrogenProductionDeficit_kWh += ProductionSetpoint_kW - max(0,-ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN));	// Update hydrogen production deficit	
	
	
	// This variable is reset after a rapidrun, but not saved before running the headless simulation.
	v_hydrogenInStorage_kWh -= ElectrolyserAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN) * energyModel.p_timeStep_h;
	
	/*if (b_useHydrogenLocally) {
		List<GridConnection> receivingGCs = findAll(energyModel.c_gridConnections, gc -> gc.b_useHydrogenLocally && gc != this);
		//traceln("hydrogen reveicing gcs: %s", receivingGCs);
		for (GridConnection gc : receivingGCs) {
			
			gc.v_hydrogenInStorage_kWh += energyModel.p_timeStep_h * flowsArray[2] / receivingGCs.size();					
		}
	}*/

}
/*ALCODEEND*/}

double f_operateFlexAssets_override()
{/*ALCODESTART::1708089644411*/
for( J_EA v : c_conversionAssets ){
	if (v instanceof J_EAConversionElectrolyser) {
		f_manageElectrolyser((J_EAConversionElectrolyser)v);
	}
	//if (v instanceof J_EAConversionCurtailer) {
		// Must go last! 
	//} 
	/*else {		
		v_currentPowerElectricity_kW += v.electricityConsumption_kW - v.electricityProduction_kW;
		v_conversionPowerElectric_kW += v.electricityConsumption_kW - v.electricityProduction_kW;
		v_currentPowerMethane_kW += v.methaneConsumption_kW - v.methaneProduction_kW;
		v_currentPowerHydrogen_kW += v.hydrogenConsumption_kW - v.hydrogenProduction_kW;
		v_currentPowerHeat_kW += v.heatConsumption_kW - v.heatProduction_kW;
		v_currentPowerDiesel_kW += v.dieselConsumption_kW;
	} */
}

// Determine EV charging
f_manageCharging();
//v_currentPowerElectricity_kW += v_evChargingPowerElectric_kW;

// Operate battery
if (p_batteryAsset != null){
	v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
	if( p_batteryOperationMode == OL_BatteryOperationMode.BALANCE) {
		f_batteryManagementBalanceCoop( v_batterySOC_fr );
	}
	else {
		f_batteryManagementPrice( v_batterySOC_fr );
	}
	p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
	//v_batteryPowerElectric_kW = p_batteryAsset.electricityConsumption_kW - p_batteryAsset.electricityProduction_kW;
	//v_currentPowerElectricity_kW += v_batteryPowerElectric_kW; 
}


/*ALCODEEND*/}

double f_electrolyserRegime(double elektrolyserSetpointElectric_kW,double excessElectricPower_kW,J_EAConversionElectrolyser ElectrolyserAsset)
{/*ALCODESTART::1708447959640*/
double solar_forecast_kW;
double wind_forecast_kW;

double forecast_time_h = ElectrolyserAsset.getStartUpTimeStandby_h()+ 2*energyModel.p_timeStep_h;

//Get current limitation values
//Pair<J_FlowsMap, Double> flowsPair = ElectrolyserAsset.getLastFlows();
J_FlowsMap flowsMap = ElectrolyserAsset.getLastFlows();
double previousElectrolyserConsumption_kW = max(0,flowsMap.get(OL_EnergyCarriers.ELECTRICITY));
double currentGridNodePowerFlow_kW = this.l_parentNodeElectric.getConnectedAgent().v_currentLoad_kW - previousElectrolyserConsumption_kW;
data_liveWeekGridNoderPowerFlow_kW.add(energyModel.t_h, this.l_parentNodeElectric.getConnectedAgent().v_currentLoad_kW);


switch (p_electrolyserOperationMode){

	case PRICE:
		f_electrolyserRegimeControl_Price(excessElectricPower_kW, ElectrolyserAsset);
	break;
	case BALANCE:
		f_electrolyserRegimeControl_Balance(currentGridNodePowerFlow_kW, forecast_time_h, ElectrolyserAsset);
	break;	
	case ALWAYS_IDLE:
		f_electrolyserRegimeControl_AlwaysIdle(currentGridNodePowerFlow_kW, ElectrolyserAsset);
	break;
}

switch (ElectrolyserAsset.getState()){

	case SHUTDOWN:
		elektrolyserSetpointElectric_kW = 0;
		
		if(energyModel.v_isRapidRun){
		v_totalDownTimeElectrolyser_hr = v_totalDownTimeElectrolyser_hr + energyModel.p_timeStep_h;
		}
	break;
	
	case STANDBY: 
		elektrolyserSetpointElectric_kW = 0;
		
		if(energyModel.v_isRapidRun){
		v_totalDownTimeElectrolyser_hr = v_totalDownTimeElectrolyser_hr + energyModel.p_timeStep_h;
		}
	break;
	
	case IDLE: 
		elektrolyserSetpointElectric_kW = 0.025*ElectrolyserAsset.getInputCapacity_kW(); // 1 - 5 percent of nominal load to keep it warm!
		
		if(energyModel.v_isRapidRun){
			v_totalDownTimeElectrolyser_hr = v_totalDownTimeElectrolyser_hr + energyModel.p_timeStep_h;
			v_totalEnergyLossIdle_kWh = v_totalEnergyLossIdle_kWh + elektrolyserSetpointElectric_kW*energyModel.p_timeStep_h;
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
ConnectionOwner ownerActor = (ConnectionOwner)l_ownerActor.getConnectedAgent();
//double currentElectricityPriceCharge_eurpkWh = ownerActor.f_getElectricityPrice(-excessElectricPower_kW); // query price at 1kW
double currentElectricityPriceEPEX_eurpkWh = energyModel.v_epexForecast_eurpkWh;

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

double f_electrolyserRegimeControl_Balance(double currentGridNodePowerFlow_kW,double forecast_time_h,J_EAConversionElectrolyser ElectrolyserAsset)
{/*ALCODESTART::1715611921617*/
double solar_forecast_kW;
double wind_forecast_kW;

//Initialize limitation values
if (c_forecast_RES_kW.size() == 0){
		
	for(int i = energyModel.v_timeStepsElapsed; i < energyModel.v_timeStepsElapsed + roundToInt(forecast_time_h/energyModel.p_timeStep_h); i++){
		solar_forecast_kW = - energyModel.tf_p_solar_e_normalized(energyModel.t_h + i*energyModel.p_timeStep_h) * energyModel.v_totalInstalledPVPower_kW;
		wind_forecast_kW = - energyModel.tf_p_wind_e_normalized(energyModel.t_h + i*energyModel.p_timeStep_h) * energyModel.v_totalInstalledWindPower_kW;
		
		c_forecast_RES_kW.add(solar_forecast_kW + wind_forecast_kW);
		
		c_forecast_gridNodePowerFlow_kW.add(currentGridNodePowerFlow_kW - c_forecast_RES_kW.get(0) + solar_forecast_kW + wind_forecast_kW);
		
	}
}

//Get future limitation values
else if(energyModel.v_timeStepsElapsed < (8760-forecast_time_h)/energyModel.p_timeStep_h){
	
	//Get current RES production
	double currentRESProduction_kW = c_forecast_RES_kW.get(0);
	
	//Update forecast array RES
	c_forecast_RES_kW.remove(0);
	
	solar_forecast_kW = - energyModel.tf_p_solar_e_normalized(energyModel.t_h + forecast_time_h) * energyModel.v_totalInstalledPVPower_kW;
	wind_forecast_kW = - energyModel.tf_p_wind_e_normalized(energyModel.t_h + forecast_time_h) * energyModel.v_totalInstalledWindPower_kW;
	
	c_forecast_RES_kW.add(solar_forecast_kW + wind_forecast_kW); 
	
	//Update forecast array Grid node power flow
	c_forecast_gridNodePowerFlow_kW.remove(0);
	
	//Get past grid node power flow and weather (last week) if last week forecast prediction is selected.
	if (b_forecast_lastWeekBased && data_liveWeekElectrolyserPower_kW.size() > 672 - roundToInt(forecast_time_h/energyModel.p_timeStep_h)){ // Use last week to create the forecast	
	
		double lastWeekGridNodePowerFlow_kW = data_liveWeekGridNoderPowerFlow_kW.getY(roundToInt(forecast_time_h/energyModel.p_timeStep_h)) - data_liveWeekElectrolyserPower_kW.getY(roundToInt(forecast_time_h/energyModel.p_timeStep_h));
		double solar_lastWeek_kW = - energyModel.tf_p_solar_e_normalized(energyModel.t_h + forecast_time_h - 168) * energyModel.v_totalInstalledPVPower_kW;
		double wind_lastWeek_kW = - energyModel.tf_p_wind_e_normalized(energyModel.t_h + forecast_time_h - 168) * energyModel.v_totalInstalledWindPower_kW;
			
		c_forecast_gridNodePowerFlow_kW.add(lastWeekGridNodePowerFlow_kW - solar_lastWeek_kW - wind_lastWeek_kW + solar_forecast_kW + wind_forecast_kW);
	}
	else{//use current power flow to predict forecast
		c_forecast_gridNodePowerFlow_kW.add(currentGridNodePowerFlow_kW - currentRESProduction_kW + c_forecast_RES_kW.get(roundToInt(forecast_time_h/energyModel.p_timeStep_h)-1));
	}
}


//Set state based on current state and forecast.
switch (ElectrolyserAsset.getState()){

	case SHUTDOWN: //Not ready to be powerd up and complete shut down (when broken, maintenance, etc.)
	
	break;

	case STANDBY: // Ready to be powered on, but no electricity consumption.
		//Check if electrolyser will be able to be functional at least two time steps when powering up, if so: power_up = true.
		if (c_forecast_gridNodePowerFlow_kW.get(roundToInt(forecast_time_h/energyModel.p_timeStep_h)-2) < v_gridNodeCongestionLimit_kW && c_forecast_gridNodePowerFlow_kW.get(roundToInt(forecast_time_h/energyModel.p_timeStep_h)-1) < v_gridNodeCongestionLimit_kW){
			ElectrolyserAsset.setElectrolyserState(OL_ElectrolyserState.POWER_UP);
			ElectrolyserAsset.setRemainingPowerUpDuration_timesteps(roundToInt(ElectrolyserAsset.getStartUpTimeStandby_h()/energyModel.p_timeStep_h));
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

