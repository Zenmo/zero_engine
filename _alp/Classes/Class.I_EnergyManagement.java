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

	// Must be implemented by the class (the class should contain these maps and Lists!!)
	public Map<Class<? extends I_SubAssetManagement>, I_SubAssetManagement> getActiveSubManagements();
	public List<Class<? extends I_SubAssetManagement>> getSupportedSubManagements();
    
	//Manage EMS (Called by GC)
	public void manageFlexAssets(J_TimeVariables timeVariables);

	
	//Set sub managements
    default <T extends I_SubAssetManagement> void setSubManagement(Class<T> subManagementType, T subManagementInstance) {
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
    
	//Specific sub management activation
	public void setV2GActive(boolean enableV2G);	
	public boolean getV2GActive();
	
	//Get specific types
	public OL_GridConnectionHeatingType getCurrentHeatingType();
	public OL_ChargingAttitude getCurrentChargingType();
	
}