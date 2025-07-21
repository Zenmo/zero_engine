@Override
public void f_operateFlexAssets(){
	f_operateFlexAssets_overwrite();
}

@Override
public void f_manageHeating(){
	f_manageHeatingAssets_overwrite();
}

@Override
public void f_connectToJ_EA_default(J_EA j_ea){
	f_connectToJ_EA_default_overwrite(j_ea);
}

/*
@Override
public void f_resetSpecificGCStatesAfterRapidRun(){
	f_resetSpecificGCStatesAfterRapidRun_override();
}
*/

@Override
public void f_resetSpecificGCStates(){
	f_resetSpecificGCStates_override();
}