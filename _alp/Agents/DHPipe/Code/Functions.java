double f_calculateHeatLoss_kW(double T_supply_degC,double T_return_degC)
{/*ALCODESTART::1770629892235*/
// Temperature loss
double T_mean_degC = 0.5*(T_supply_degC + T_return_degC);
double dT_degC = T_mean_degC - p_Tsoil_degC;

return max(0, p_UA_WpK*dT_degC/1000.0);
/*ALCODEEND*/}

