@Override
public void f_connectToJ_EA(J_EA j_ea){
	f_connectToJ_EA_default(j_ea);
	f_connectTo_J_EA_House(j_ea);
}

@Override
public void f_removeTheJ_EA(J_EA j_ea){
	f_removeTheJ_EA_default(j_ea);
	f_removeTheJ_EA_house(j_ea);
}

/*@Override
public void f_operateFixedConsumptionAssets(){
	f_operateFixedConsumptionAssets_overwrite();
}*/

/*
@Override
public void f_operateFlexAssets(){
	f_operateFlexAssets_overwrite();
}
*/
/*
@Override 
public void f_manageEVCharging(){
	f_manageCharging_overwrite();
}*/

/*@Override 
public void f_chargeOnPrice(double a, double b){
	f_chargeOnPrice_overwrite(a,b);
}*/

/*
@Override
public void f_simpleCharging(){
	f_simpleCharging_overwrite();
}
*/
