public interface I_AssetOwner
{
	void f_connectToJ_EA(J_EA ea);
	
	void f_removeTheJ_EA(J_EA ea);
	
	boolean f_isActive();
	
	
	//void f_addFlows(J_FlowsMap flowsMap, double energyUse_kW, J_ValueMap assetFlowsMap, J_EA caller);
	
	//void f_removeFlows(J_FlowsMap flowsMap, double energyUse_kW, J_ValueMap assetFlowsMap, J_EA caller);
	
}