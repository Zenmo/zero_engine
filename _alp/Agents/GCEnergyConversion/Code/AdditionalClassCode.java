@Override
public void f_operateFlexAssets(){
	f_operateFlexAssets_override();
}

@Override
public void f_resetSpecificGCStates(){
	f_resetSpecificGCStates_override();
}

@Override
public void f_resetSpecificGCStatesAfterRapidRun(){
	f_resetSpecificGCStatesAfterRapidRun_override();
}

@Override
public void f_rapidRunDataLogging() {
	super.f_rapidRunDataLogging();
	//f_fillAnnualDatasets_electrolyser();
}