@Override
public void f_connectToJ_EA(J_EA j_ea){
	f_connectToJ_EA_default(j_ea);
	f_connectTo_J_EA_Industry(j_ea);
}

@Override
public void f_operateFlexAssets(){
	f_operateFlexAssets_overwrite();
}