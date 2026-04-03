import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")


public interface I_CurtailManagement extends I_AssetManagement
{
	//Get the subManagementInterface type
	default Class<? extends I_AssetManagement> getAssetManagementInterfaceType(){
		return I_CurtailManagement.class;
	}
	
	//Manage curtailment
	void manageCurtailment(J_TimeVariables timeVariables);
	
}