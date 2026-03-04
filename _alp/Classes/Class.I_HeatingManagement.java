import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")


public interface I_HeatingManagement extends I_AssetManagement{
	
	//Get the subManagementInterface type
	default Class<? extends I_AssetManagement> getAssetManagementInterfaceType(){
		return I_HeatingManagement.class;
	}
	
	//Manage heating (flex) assets
	void manageHeating(J_TimeVariables timeVariables);
	
	// Initiliaze throws an exception when the configuration of assets is not a valid combination
	void initializeAssets();

	// Every implementation must have a list of all its valid heating types that it supports
	List<OL_GridConnectionHeatingType> getValidHeatingTypes();
	// Every implementation must choose one type that the current instance is managing
	OL_GridConnectionHeatingType getCurrentHeatingType();
	
	//Heating preferences
	J_HeatingPreferences getHeatingPreferences();
	void setHeatingPreferences(J_HeatingPreferences heatingPreferences);
} 

