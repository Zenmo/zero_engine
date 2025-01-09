void e_startNonFirmATO()
{/*ALCODESTART::1669035973262*/
v_allowedCapacity_kW = v_contractConnection.nfATOpower_kW;// p_nfatoLvl_kW;
/*ALCODEEND*/}

void e_endNonFirmATO()
{/*ALCODESTART::1669035973267*/
v_allowedCapacity_kW = p_connectionCapacity_kW;
/*ALCODEEND*/}

