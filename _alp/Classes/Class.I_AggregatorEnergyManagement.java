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


public interface I_AggregatorEnergyManagement extends I_StoreStatesAndReset
{
	// Must be implemented by the class (the class should contain these Lists and Map!!)
	public List<Class<? extends I_AggregatorAssetManagement>> getInternalAggregatorAssetManagements();//Inherent asset management that the EMS handles itself
	public List<Class<? extends I_AggregatorAssetManagement>> getSupportedExternalAggregatorAssetManagements(); // Supported submanagements that can support the EMS code
    public Map<Class<? extends I_AggregatorAssetManagement>, I_AggregatorAssetManagement> getActiveExternalAggregatorAssetManagements(); //Submanagements that actively support the EMS code

	//Manage EMS (Called by GC)
	public void operateAggregatorEnergyManagement(J_TimeVariables timeVariables);

	
	//Set sub managements
    default void setExternalAggregatorAssetManagement(I_AggregatorAssetManagement aggregatorAssetManagementInstance) {
    	
    	if(aggregatorAssetManagementInstance == null) {
    		throw new RuntimeException("Can't call setExternalAssetManagement() with input 'null'. -> If you are trying to remove something, use removeExternalAssetManagement() instead.");
    	}
    	
    	//Get the assetmanagement (interface) type (I_ChargingManagement, I_HeatingManagement, etc.)
    	Class<? extends I_AggregatorAssetManagement> aggregatorAssetManagementType = aggregatorAssetManagementInstance.getAggregatorAssetManagementInterfaceType();
    	
    	//Check if setAssetManagement is actually supported by this EnergyManagement class
    	if (getSupportedExternalAggregatorAssetManagements().stream().noneMatch(supported -> supported.isAssignableFrom(aggregatorAssetManagementType))) {
    	    throw new RuntimeException("Trying to set an unsupported sub asset management type for an EMS.");
    	}
    	getActiveExternalAggregatorAssetManagements().put(aggregatorAssetManagementType, aggregatorAssetManagementInstance);
    }
    
    default void removeExternalAggregatorAssetManagement(Class<? extends I_AggregatorAssetManagement> aggregatorAssetManagementType) {
    	if(aggregatorAssetManagementType.cast(getActiveExternalAggregatorAssetManagements().get(aggregatorAssetManagementType)) != null) {
    		getActiveExternalAggregatorAssetManagements().remove(aggregatorAssetManagementType);
    	}
    }

	//Get assetManagements (return null if not present)
    default <T extends I_AggregatorAssetManagement> T getExternalAggregatorAssetManagement(Class<T> aggregatorAssetManagementType) {//Inputs can be I_HeatingManagement, I_ChargingManagement, etc.
    	//Check if getAssetManagement is actually supported by this EnergyManagement class if not -> return null automatically.
    	if (getSupportedExternalAggregatorAssetManagements().stream().noneMatch(supported -> aggregatorAssetManagementType.isAssignableFrom(supported))) {
    	    return null;
    	}
        return aggregatorAssetManagementType.cast(getActiveExternalAggregatorAssetManagements().get(aggregatorAssetManagementType));
    }
    
    //Check of certain AssetManagement is present in the EMS (Inherent or through external management)
    default <T> boolean isAggregatorAssetManagementActive(Class<T> aggregatorAssetManagementType) {
    	return getInternalAggregatorAssetManagements().contains(aggregatorAssetManagementType) || getActiveExternalAggregatorAssetManagements().get(aggregatorAssetManagementType) != null;
    }

    //Check of certain Assets can potentially be managed by the EMS (Internal or through potentially externalmanagement)
    default <T> boolean isAggregatorAssetManagementSupported(Class<T> aggregatorAssetManagementType) {
    	return getInternalAggregatorAssetManagements().contains(aggregatorAssetManagementType) || getSupportedExternalAggregatorAssetManagements().contains(aggregatorAssetManagementType);
    }
}