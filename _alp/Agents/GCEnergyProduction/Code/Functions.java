double f_operateFlexAssets_overwrite(J_TimeVariables timeVariables)
{/*ALCODESTART::1671111800831*/
/*for( J_EA v : c_conversionAssets ){
	if (v instanceof J_EAConversionElektrolyser) {
		f_manageElektrolyser((J_EAConversionElektrolyser)v);
	}
}*/

f_manageEVCharging();

f_manageBattery();

/*ALCODEEND*/}

double f_manageElektrolyser(J_EAConversionElektrolyser ElektrolyserAsset)
{/*ALCODESTART::1671112355094*/
if (ElektrolyserAsset.getInputCapacity_kW()>0) {
	
	//double availableCapacityFromBatteries_kW = p_batteryAsset == null ? 0 : ((J_EAStorageElectric)p_batteryAsset.j_ea).getCapacityAvailable_kW(); 
	double availableElectricPower_kW = v_liveConnectionMetaData.contractedDeliveryCapacity_kW - fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
	double excessElectricPower_kW = -(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) + v_liveConnectionMetaData.contractedFeedinCapacity_kW); // Should at least draw this much power to stay within connection limits. Doesn't account for a battery!
	double eta_r = ElektrolyserAsset.getEta_r();
	//double connectionCapacity_kW = v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
	double ProductionSetpoint_kW = ElektrolyserAsset.getInputCapacity_kW() * 0.30 * eta_r; // Aim for average production power of xx% of elektrolyser peak power
	double elektrolyserSetpointElectric_kW = 0;
	
	if (p_electrolyserOperationMode==OL_ElectrolyserOperationMode.BALANCE) {
		double FeedbackGain_kWpkWh = 1.0/24; // Try to recover deficit in 24 hours
		elektrolyserSetpointElectric_kW = ProductionSetpoint_kW/eta_r + v_hydrogenProductionDeficit_kWh * FeedbackGain_kWpkWh /eta_r ;
	
		//traceln("Elektrolyser electric power setpoint: " + elektrolyserSetpointElectric_kW + " kW");
		//traceln("Elektrolyser power fraction: " + elektrolyserSetpointElectric_kW/ElektrolyserAsset.j_ea.getElectricCapacity_kW());
	} else if (p_electrolyserOperationMode==OL_ElectrolyserOperationMode.PRICE) {
		//if(l_ownerActor.getConnectedAgent() instanceof ConnectionOwner) {
		//double currentElectricityPriceCharge_eurpkWh = ownerActor.v_priceBandsDelivery.ceilingEntry(100.0).getValue(); // query price at 1kW
		double currentElectricityPriceCharge_eurpkWh = p_owner.f_getElectricityPrice(-excessElectricPower_kW); // query price at 1kW
		//traceln("Current electricity price for electrolyser: " + currentElectricityPriceCharge_eurpkWh);
		v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - v_electricityPriceLowPassed_eurpkWh );
		double deficitGain_eurpkWh = 1.0/1000000; // When SOC-error is 100%, adjust WTP price by 1 eurpkWh
		double priceGain_peur = 5; // How strongly to ramp up power with price-delta's	
		double WTP_eurpkWh = v_electricityPriceLowPassed_eurpkWh + deficitGain_eurpkWh * v_hydrogenProductionDeficit_kWh;
		elektrolyserSetpointElectric_kW = max(0,ElektrolyserAsset.getInputCapacity_kW()*(WTP_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_peur)  ;
		//traceln("WTP hydrogen production is " + roundToDecimal(WTP_eurpkWh,3) + " eurpkWh is higher than electricity price " + roundToDecimal(currentElectricityPriceCharge_eurpkWh,3) + " eurpkWh, so produce! Setpoint power: " + round(elektrolyserSetpointElectric_kW) + " kW");
	}	
	
	// Limit elektrolyser power to available electric power on connection (assuming it is last in merit!)
	elektrolyserSetpointElectric_kW = min(availableElectricPower_kW,max(elektrolyserSetpointElectric_kW,excessElectricPower_kW));
	
	ElektrolyserAsset.f_updateAllFlows(elektrolyserSetpointElectric_kW/ElektrolyserAsset.getInputCapacity_kW());
	
	v_hydrogenProductionDeficit_kWh += ProductionSetpoint_kW - max(0,-ElektrolyserAsset.getLastFlows().get(OL_EnergyCarriers.HYDROGEN));	// Update hydrogen production deficit
}
/*ALCODEEND*/}

