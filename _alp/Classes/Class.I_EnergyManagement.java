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


public interface I_EnergyManagement extends I_AssetManagement
{

	// Must be implemented by the class (the class should contain these Lists and Map!!)
	public List<Class<? extends I_SubAssetManagement>> getInherentAssetManagements();//Inherent asset management that the EMS handles itself
	public List<Class<? extends I_SubAssetManagement>> getSupportedSubManagements(); // Supported submanagements that can support the EMS code
    public Map<Class<? extends I_SubAssetManagement>, I_SubAssetManagement> getActiveSubManagements(); //Submanagements that support the EMS code

	//Manage EMS (Called by GC)
	public void manageFlexAssets(J_TimeVariables timeVariables);

	
	//Set sub managements
    default void setSubManagement(I_SubAssetManagement subManagementInstance) {
    	
    	//Get the submanagement (interface) type (I_ChargingManagement, I_HeatingManagement, etc.)
    	Class<? extends I_SubAssetManagement> subManagementType = subManagementInstance.getSubManagementInterfaceType();
    	
    	//Check if setSubManagement is actually supported by this EnergyManagement class
    	if (getSupportedSubManagements().stream().noneMatch(supported -> supported.isAssignableFrom(subManagementType))) {
    	    throw new RuntimeException("Trying to set an unsupported sub asset management type for an EMS.");
    	}
    	getActiveSubManagements().put(subManagementType, subManagementInstance);
    }
    
	//Get subManagements (return null if not present)
    default <T> T getSubManagement(Class<T> subManagementType) {//Inputs can be I_HeatingManagement, I_ChargingManagement, etc.
    	//Check if getSubManagement is actually supported by this EnergyManagement class
    	if (getSupportedSubManagements().stream().noneMatch(supported -> subManagementType.isAssignableFrom(supported))) {
    	    throw new RuntimeException("Trying to get an unsupported sub asset management type for an EMS.");
    	}
        return subManagementType.cast(getActiveSubManagements().get(subManagementType));
    }
    
    //Check of certain AssetManagement is present in the EMS (Inherent or through added submanagement)
    default <T> boolean isAssetManagementPresent(Class<T> subManagementType) {
    	return getInherentAssetManagements().contains(subManagementType) || getActiveSubManagements().get(subManagementType) != null;
    }

    //Check of certain Assets can potentially be managed by the EMS (Inherent or through added submanagement)
    default <T> boolean isAssetManagementSupported(Class<T> subManagementType) {
    	return getInherentAssetManagements().contains(subManagementType) || getActiveSubManagements().get(subManagementType) != null;
    }
    
	//Specific sub management activation
	public void setV2GActive(boolean enableV2G);	
	public boolean getV2GActive();
	
	//Get specific types
	public OL_GridConnectionHeatingType getCurrentHeatingType();
	public OL_ChargingAttitude getCurrentChargingType();
	
}