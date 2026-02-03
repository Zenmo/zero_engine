@Override
public void f_operateFlexAssets(J_TimeVariables timeVariables){
	f_operateFlexAssets_overwrite(timeVariables);
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