double f_resetSpecificGCStates_override()
{/*ALCODESTART::1734716016619*/
v_batteryMoneyMade_euro = 0;
v_currentLoadLowPassed_kW = 0;
/*ALCODEEND*/}

double f_batteryManagementBalanceNoGCCapacity_NBH()
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
	double windEnergyExpectedNormalized_fr = energyModel.pf_windProduction_fr.getForecast() * energyModel.p_forecastTime_h * v_liveAssetsMetaData.totalInstalledWindPower_kW / p_batteryAsset.getStorageCapacity_kWh();
	double solarEnergyExpectedNormalized_fr = energyModel.pf_PVProduction35DegSouth_fr.getForecast() * energyModel.p_forecastTime_h * v_liveAssetsMetaData.totalInstalledPVPower_kW / p_batteryAsset.getStorageCapacity_kWh();
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
	chargeSetpoint_kW = -FeedforwardGain_kWpKw * currentLoadDeviation_kW + (SOC_setp_fr - p_batteryAsset.getCurrentStateOfCharge_fr()) * FeedbackGain_kWpSOC;
	//chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	p_batteryAsset.v_powerFraction_fr = max(-1,min(1, chargeSetpoint_kW / p_batteryAsset.getCapacityElectric_kW())); // Convert to powerFraction and limit power
	//traceln("v_powerFraction_fr" + p_batteryAsset.v_powerFraction_fr);
	//traceln("Coop surpluss " + currentCoopElectricitySurplus_kW + "kW, Battery charging power " + p_batteryAsset.v_powerFraction_fr*p_batteryAsset.j_ea.getElectricCapacity_kW() + " kW at " + currentBatteryStateOfCharge*100 + " % SOC");
}
/*ALCODEEND*/}

