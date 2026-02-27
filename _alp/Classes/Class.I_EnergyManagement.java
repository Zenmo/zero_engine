import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.CLASS,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON
	)


public interface I_EnergyManagement extends I_StoreStatesAndReset
{

	// Must be implemented by the class (the class should contain these Lists and Map!!)
	public List<Class<? extends I_AssetManagement>> getInternalAssetManagements();//Inherent asset management that the EMS handles itself
	public List<Class<? extends I_AssetManagement>> getSupportedExternalAssetManagements(); // Supported submanagements that can support the EMS code
    public Map<Class<? extends I_AssetManagement>, I_AssetManagement> getActiveExternalAssetManagements(); //Submanagements that actively support the EMS code

	//Manage EMS (Called by GC)
	public void manageFlexAssets(J_TimeVariables timeVariables);

	
	//Set sub managements
    default void setExternalAssetManagement(I_AssetManagement assetManagementInstance) {
    	
    	if(assetManagementInstance == null) {
    		throw new RuntimeException("Can't call setExternalAssetManagement() with input 'null'. -> If you are trying to remove something, use removeExternalAssetManagement() instead.");
    	}
    	
    	//Get the assetmanagement (interface) type (I_ChargingManagement, I_HeatingManagement, etc.)
    	Class<? extends I_AssetManagement> assetManagementType = assetManagementInstance.getAssetManagementInterfaceType();
    	
    	//Check if setAssetManagement is actually supported by this EnergyManagement class
    	if (getSupportedExternalAssetManagements().stream().noneMatch(supported -> supported.isAssignableFrom(assetManagementType))) {
    	    throw new RuntimeException("Trying to set an unsupported sub asset management type for an EMS.");
    	}
    	getActiveExternalAssetManagements().put(assetManagementType, assetManagementInstance);
    }
    
    default void removeExternalAssetManagement(Class<? extends I_AssetManagement> assetManagementType) {
    	if(assetManagementType.cast(getActiveExternalAssetManagements().get(assetManagementType)) != null) {
    		getActiveExternalAssetManagements().remove(assetManagementType);
    	}
    }
    
	//Get assetManagements (return null if not present)
    default <T> T getExternalAssetManagement(Class<T> assetManagementType) {//Inputs can be I_HeatingManagement, I_ChargingManagement, etc.
    	//Check if getAssetManagement is actually supported by this EnergyManagement class if not -> return null automatically.
    	if (getSupportedExternalAssetManagements().stream().noneMatch(supported -> assetManagementType.isAssignableFrom(supported))) {
    	    return null;
    	}
        return assetManagementType.cast(getActiveExternalAssetManagements().get(assetManagementType));
    }
    
    //Check of certain AssetManagement is present in the EMS (Inherent or through external management)
    default <T> boolean isAssetManagementActive(Class<T> assetManagementType) {
    	return getInternalAssetManagements().contains(assetManagementType) || getActiveExternalAssetManagements().get(assetManagementType) != null;
    }

    //Check of certain Assets can potentially be managed by the EMS (Internal or through potentially externalmanagement)
    default <T> boolean isAssetManagementSupported(Class<T> assetManagementType) {
    	return getInternalAssetManagements().contains(assetManagementType) || getSupportedExternalAssetManagements().contains(assetManagementType);
    }
    
    
    
    ////Checks
    //Check configuration: Should be called whenever a management or asset has been removed/added/changed.
    default public void checkConfiguration(List<J_EAFlex> flexAssetsGCList) {
    	
    	//Check if all active flex assets are managed by the EMS
    	List<J_EAFlex> flexAssets = new ArrayList<>(flexAssetsGCList);

    	while (flexAssets.size()>0){ //While loop to prevent checking the same J_EAFlex type multiple times.
    		for(J_EAFlex asset : flexAssets) {
				if(asset instanceof J_EAEV || asset instanceof J_EAChargingSession){
					if(!isAssetManagementActive(I_ChargingManagement.class)) {
						throw new RuntimeException("An " + asset.getEAType() + " is found at GC that has an EMS that does not have active charging management.");
					}
					flexAssets.removeAll(findAll(flexAssets, vehicle -> vehicle instanceof J_EAEV || vehicle instanceof J_EAChargingSession));
					break;
				}
				else if(asset instanceof I_HeatingAsset || asset instanceof J_EAStorageHeat){
					if(!isAssetManagementActive(I_HeatingManagement.class)) {
						throw new RuntimeException("A heating asset is found at GC that has an EMS that does not have active heating management.");
					}
					if(getExternalAssetManagement(I_HeatingManagement.class) != null) {
						getExternalAssetManagement(I_HeatingManagement.class).initializeAssets();
					}
					flexAssets.removeAll(findAll(flexAssets, heatAsset -> heatAsset instanceof I_HeatingAsset || heatAsset instanceof J_EAStorageHeat));
					break;
				}
				else if(asset instanceof J_EAStorageElectric){
					if(!isAssetManagementActive(I_BatteryManagement.class)) {
						throw new RuntimeException("A battery is found at a GC that has an EMS that does not have active battery management.");
					}
					flexAssets.removeAll(findAll(flexAssets, battery -> battery instanceof J_EAStorageElectric));
					break;
				}
				else {
	
					traceln("Asset found that is not managed by I_AssetManagement, can not be checked.");//Temporary soft error till all managements are trough I_AssetManagement
					flexAssets.remove(asset);
					break;
				}
			}
    	}
    	
    	//This EMS class specific Checks
    	checkConfigurationEMSSpecific(new ArrayList<>(flexAssetsGCList));
    	
    	//Set checked boolean to true
    	setChecked(true);
    }
    
    //Function used to add specific checks to EMS class
    public void checkConfigurationEMSSpecific(List<J_EAFlex> flexAssetsGCList); 
        
    //Boolean setting
    public void setChecked(boolean checked); //Set boolean to false so class doesn't check itself at start of next timestep.

    
    ////Specific Management calls
	//Specific management activation
	public void setV2GActive(boolean enableV2G);	
	public boolean getV2GActive();
	
	//Get specific types
	public OL_GridConnectionHeatingType getCurrentHeatingType();
	public OL_ChargingAttitude getCurrentChargingType();
}