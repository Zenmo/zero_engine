double f_operateFlexAssets_overwrite()
{/*ALCODESTART::1698936515692*/
f_manageHeatingAssets();

f_manageCharging();

if (p_batteryAsset != null){ 
	if (p_batteryAsset.getStorageCapacity_kWh() > 0 && p_batteryAsset.getCapacityElectric_kW() > 0) {
		
		switch (p_batteryOperationMode) {
			case BALANCE:
				f_batteryManagementBalance(p_batteryAsset.getCurrentStateOfCharge());
				break;
			case SELF_CONSUMPTION:
				f_batteryManagementSelfConsumption();
				break;
			case PRICE:
				f_batteryManagementPrice(p_batteryAsset.getCurrentStateOfCharge());
				break;
			case NODAL_PRICING:
				f_batteryManagementNodalPricing(p_batteryAsset.getCurrentStateOfCharge());
				break;
			case PEAK_SHAVING_SIMPLE:
			case PEAK_SHAVING:
				f_batteryManagementPeakShaving();
				break;
			case EXTERNAL_SETPOINT:
				//Management function and remaining flex should be called by external control agent -> Return.
				if(c_parentCoops.size()>0){
					return;
				}
				break;
		}
		
		p_batteryAsset.f_updateAllFlows(p_batteryAsset.v_powerFraction_fr);
		v_batterySOC_fr = p_batteryAsset.getCurrentStateOfCharge();
	}
}

//v_currentLoadLowPassed_kW += v_lowPassFactorLoad_fr * ( v_currentPowerElectricity_kW - v_currentLoadLowPassed_kW ); //you want to do deterine the lowpassLoad BEFORE the using the battery. As this behavior of the battery should nog be dependent on the load of the battery in the previous timesteps

if (v_enableFuelCell) {
	f_manageFuelCell();
}

/*ALCODEEND*/}

double f_manageFuelCell()
{/*ALCODESTART::1721138603366*/
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

double f_resetSpecificGCStates_overwrite()
{/*ALCODESTART::1721138706438*/
//Reset variables 
v_totalHydrogenUsed_MWh = 0;
v_maxHydrogenPower_kW = 0;
/*ALCODEEND*/}

