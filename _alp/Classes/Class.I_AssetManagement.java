//This class contains all asset management classes are required to have.

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

public interface I_AssetManagement extends I_StoreStatesAndReset
{	
	//Get the AssetManagementInterface type (I_ChargingManagement.class, I_Heatingmanagement.class, etc.)
	Class<? extends I_AssetManagement> getAssetManagementInterfaceType();
	
}