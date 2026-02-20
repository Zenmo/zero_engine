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
	Map<Class<? extends I_AssetManagement>, I_AssetManagement> subManagements = new HashMap();
	List<Class<? extends I_AssetManagement>> supportedSubManagements = new ArrayList<>();
	
	//Manage EMS (Called by GC)
	public void manageFlexAssets(J_TimeVariables timeVariables);

	
	//Set child managements
    default <T extends I_AssetManagement> void setSubManagement(Class<T> subManagementType, T subManagementInstance) {
    	//Check if setSubManagement is actually supported by this EnergyManagement class
    	if (supportedSubManagements.stream().noneMatch(supported -> supported.isAssignableFrom(subManagementType))) {
    	    throw new RuntimeException("Trying to set an unsupported sub asset management type for an EMS.");
    	}
        subManagements.put(subManagementType, subManagementInstance);
    }
    
	//Get child managements (return null if not present)
    default <T> T getSubManagement(Class<T> subManagementType) {//Inputs can be I_HeatingManagement, I_ChargingManagement, etc.
    	if (supportedSubManagements.stream().noneMatch(supported -> supported.isAssignableFrom(subManagementType))) {
    	    throw new RuntimeException("Trying to set an unsupported sub asset management type for an EMS.");
    	}
        return subManagementType.cast(subManagements.get(subManagementType));
    }
    
	//Specific management activation
	public void activateV2GChargingMode(boolean enableV2G, J_TimeParameters timeParameters,	J_TimeVariables timeVariables);	
	
	//Get specific types
	public OL_GridConnectionHeatingType getCurrentHeatingType();
	public OL_ChargingAttitude getCurrentChargingType();
	
}