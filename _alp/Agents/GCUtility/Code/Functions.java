double f_operateFlexAssets_overwrite(J_TimeVariables timeVariables)
{/*ALCODESTART::1698936515692*/
f_manageHeating(timeVariables);

f_manageEVCharging(timeVariables);

f_manageBattery(timeVariables);
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

