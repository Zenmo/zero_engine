import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON
	)
@JsonSubTypes({
    @JsonSubTypes.Type(value = J_HeatingManagementBuildingHybridHeatPump.class, name = "J_HeatingManagementBuildingHybridHeatPump"),
    @JsonSubTypes.Type(value = J_HeatingManagementBuildingSimple.class, name = "J_HeatingManagementBuildingSimple"),
    @JsonSubTypes.Type(value = J_HeatingManagementBuildingWithPTBufferSimple.class, name = "J_HeatingManagementBuildingWithPTBufferSimple"),
    @JsonSubTypes.Type(value = J_HeatingManagementDistrictHeating.class, name = "J_HeatingManagementDistrictHeating"),
    @JsonSubTypes.Type(value = J_HeatingManagementGhost.class, name = "J_HeatingManagementGhost"),
    @JsonSubTypes.Type(value = J_HeatingManagementNeighborhood.class, name = "J_HeatingManagementNeighborhood"),
    @JsonSubTypes.Type(value = J_HeatingManagementProfileHybridHeatPump.class, name = "J_HeatingManagementProfileHybridHeatPump"),
    @JsonSubTypes.Type(value = J_HeatingManagementProfileSimple.class, name = "J_HeatingManagementProfileSimple"),
    @JsonSubTypes.Type(value = J_HeatingManagementProfileWithPTBufferSimple.class, name = "J_HeatingManagementProfileWithPTBufferSimple"),
    @JsonSubTypes.Type(value = J_HeatingManagementSimple.class, name = "J_HeatingManagementSimple"),
})

public interface I_HeatingManagement {
	void manageHeating();
	// Initiliaze throws an exception when the configuration of assets is not a valid combination
	void initializeAssets();
	// not initialized sets the isInitialized flag to false. Is called when the (heating) assets in the GC change
	void notInitialized();
	// Every implementation must have a list of all its valid heating types that it supports
	List<OL_GridConnectionHeatingType> getValidHeatingTypes();
	// Every implementation must choose one type that the current instance is managing
	OL_GridConnectionHeatingType getCurrentHeatingType();
} 

