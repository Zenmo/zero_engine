@Override
public void f_connectToJ_EA(J_EA j_ea) {
	f_connectToJ_EA_default(j_ea);
	f_connectToJ_EA_custom(j_ea);
}
/*@Override
public void f_operateFixedAssets(){
	f_operateFixedAssets_overwrite();
}*/

@Override
public void f_operateFlexAssets(){
	f_operateFlexAssets_overwrite();
}