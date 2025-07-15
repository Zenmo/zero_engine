double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1671111800831*/
for( J_EA v : c_conversionAssets ){
	if (v instanceof J_EAConversionElektrolyser) {
		f_manageElektrolyser((J_EAConversionElektrolyser)v);
	}
}

f_manageCharging();

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

double f_batteryManagementBalanceCoop(double batterySOC)
{/*ALCODESTART::1677836046815*/
if ((p_batteryAsset).getStorageCapacity_kWh() != 0){
	if( p_owner != null) {
		if( p_owner.p_coopParent instanceof EnergyCoop ) {
			//traceln("Hello?");
//			v_previousPowerElectricity_kW = p_batteryAsset.v_powerFraction_fr * p_batteryAsset.j_ea.getElectricCapacity_kW();
			v_previousPowerElectricity_kW = p_batteryAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
			//traceln("Previous battery power: " + v_previousPowerElectricity_kW);
			double currentCoopElectricitySurplus_kW = p_owner.p_coopParent.v_electricitySurplus_kW + v_previousPowerElectricity_kW;
			//v_electricityPriceLowPassed_eurpkWh += v_lowPassFactor_fr * ( currentCoopElectricitySurplus_kW - v_electricityPriceLowPassed_eurpkWh );
			
			double CoopConnectionCapacity_kW = 0.9*p_owner.p_coopParent.v_allowedCapacity_kW; // Use only 90% of capacity for robustness against delay
			double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity
			double availableDischargePower_kW = currentCoopElectricitySurplus_kW - CoopConnectionCapacity_kW; // Max discharging power within grid capacity
			double SOC_setp_fr = 0.5;			
			if (energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW > 10000) {
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

