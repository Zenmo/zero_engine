void e_measureHeatpump()
{/*ALCODESTART::1674899601674*/
if( p_primaryHeatingAsset.j_ea instanceof J_EAConversionHeatPump ){
	v_dailyHPConsumption_kWh = ((J_EAConversionHeatPump)p_primaryHeatingAsset.j_ea).totalElectricityConsumed_kWh - v_hpPreviousConsumptionLevel;
	v_hpPreviousConsumptionLevel = ((J_EAConversionHeatPump)p_primaryHeatingAsset.j_ea).totalElectricityConsumed_kWh;
}
/*ALCODEEND*/}

