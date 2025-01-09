/*@Override
public void f_connectToChild(J_EA j_ea){
	f_connectToChild_overwrite(j_ea);
}*/

@Override
public void f_operateFlexAssets(){
	f_operateFlexAssets_overwrite();
}

@Override
public void f_batteryManagementBalance( double batterySOC ){
	f_batteryManagementBalanceGrid( batterySOC );
}

@Override
public void f_batteryManagementPrice( double batterySOC ){
	f_batteryManagementPriceGrid( batterySOC );
}

/*@Override // Why?
public void f_calculateEnergyBalance(){
	f_calculateEnergyBalance_overwrite();
}*/