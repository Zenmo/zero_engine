import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")


public interface I_HeatingManagement extends I_AssetManagement{
	void manageHeating();
	// Initiliaze throws an exception when the configuration of assets is not a valid combination
	void initializeAssets();
	// not initialized sets the isInitialized flag to false. Is called when the (heating) assets in the GC change
	void notInitialized();
	// Every implementation must have a list of all its valid heating types that it supports
	List<OL_GridConnectionHeatingType> getValidHeatingTypes();
	// Every implementation must choose one type that the current instance is managing
	OL_GridConnectionHeatingType getCurrentHeatingType();
	
	//Heating preferences
	J_HeatingPreferences getHeatingPreferences();
	void setHeatingPreferences(J_HeatingPreferences heatingPreferences);
} 

