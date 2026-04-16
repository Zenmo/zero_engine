double f_operateFlexAssets_overwrite(J_TimeVariables timeVariables)
{/*ALCODESTART::1664963959146*/
f_manageCookingTracker(timeVariables);
//f_manageAirco(timeVariables);
super.f_operateFlexAssets(timeVariables);
/*ALCODEEND*/}

double f_manageCookingTracker(J_TimeVariables timeVariables)
{/*ALCODESTART::1726334759211*/
// Add heat from cooking assets to house
if (p_cookingTracker != null) { // check for presence of cooking asset
	p_cookingTracker.manageActivities(timeVariables); // also calls f_updateAllFlows in HOB asset	
	
	double residualHeatGasPit_kW = -p_cookingTracker.HOB.getLastFlows().get(OL_EnergyCarriers.HEAT);
	throw new RuntimeException("Cooking trackers and HOBs are not properly integrated with current heating management!");
	/*if (p_BuildingThermalAsset != null) {
		p_BuildingThermalAsset.v_powerFraction_fr += residualHeatGasPit_kW / p_BuildingThermalAsset.getCapacityHeat_kW(); // Does this work out correctly with new heatingManagement structure??
	}*/
}
/*ALCODEEND*/}

double f_manageAirco(J_TimeVariables timeVariables)
{/*ALCODESTART::1749648447119*/
if( p_airco != null ) {
	if (p_airco.remainingONtimesteps == 0){
		double switchOnProbability = 0;
		switch (roundToInt(energyModel.pp_ambientTemperature_degC.getCurrentValue())) {
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
	p_airco.f_updateAllFlows( 1.0, timeVariables );
}
/*ALCODEEND*/}

