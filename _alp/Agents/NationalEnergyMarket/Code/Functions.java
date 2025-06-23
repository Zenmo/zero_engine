double f_getNationalElectricityPrice_eurpMWh()
{/*ALCODESTART::1660738813066*/
return v_currentVariableElectricityPrice_eurpMWh;
/*ALCODEEND*/}

double f_updateEnergyPrice()
{/*ALCODESTART::1660752725657*/
//energyModel.c_gridNodesNotTopLevel.get(0).p_capacity_kW
//double localBalanceTerm_eurpMWh = 200 * (energyModel.v_totalElectricPower_kW / energyModel.c_gridNodesNotTopLevel.get(0).p_capacity_kW);

v_currentVariableElectricityPrice_eurpMWh = energyModel.pp_dayAheadElectricityPricing_eurpMWh.getValue( energyModel.t_h + energyModel.p_timeStep_h);

/*ALCODEEND*/}

