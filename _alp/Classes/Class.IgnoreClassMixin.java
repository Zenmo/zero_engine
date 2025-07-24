/**
 * IgnoreClassMixin
 */	

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonIgnoreType;


@JsonAutoDetect(
	    fieldVisibility = Visibility.NONE,    // ✅ only public fields are serialized
	    getterVisibility = Visibility.NONE,
	    isGetterVisibility = Visibility.NONE,
	    setterVisibility = Visibility.NONE,
	    creatorVisibility = Visibility.NONE
	)
//@JsonIgnoreProperties({"connections"})


//@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

//@JsonIgnoreType
public abstract class IgnoreClassMixin implements Serializable {
		
    	/*@JsonIgnore
    	public abstract void setLocation();

    	@JsonIgnore
    	public abstract void getLocation();
		
	
    	@JsonIgnore
    	public abstract void setNetworkNode();

    	@JsonIgnore
    	public abstract void getNetworkNode();*/
    /**
     * Default constructor
     */
    /*public PopuplationMixin() {
    }*/


	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 

	private static final long serialVersionUID = 1L;

}