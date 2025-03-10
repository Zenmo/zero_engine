@Override
public void f_operateFlexAssets(){
	f_operateAssetsDistrictHeating();
}

@Override
public void f_connectToJ_EA(J_EA j_ea){
	f_connectToJ_EA_default(j_ea);
	f_connectToJ_EA_DistrictHeating(j_ea);
}