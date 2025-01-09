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
/*ALCODEEND*/}

