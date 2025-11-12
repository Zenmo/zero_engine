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

public interface I_AssetManagement
{	
	//Get agent that contains the management
	Agent getParentAgent();
	
	//Store current state of live sim, and reset to initial state for rapid run
    void storeStatesAndReset();
    
    //Restore state to correct state of live sim after rapid run
    void restoreStates();
}