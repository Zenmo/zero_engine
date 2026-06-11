/**
 * I_FlexProfileManagement: Interface for management classes that control the J_EAFlexProfile Assets.
 */

public interface I_FlexProfileManagement extends I_AssetManagement {
	//Get the subManagementInterface type
	default Class<? extends I_AssetManagement> getAssetManagementInterfaceType(){
		return I_FlexProfileManagement.class;
	}
	
	//Manage heating (flex) assets
	void manageFlexProfiles(J_TimeVariables timeVariables);
}