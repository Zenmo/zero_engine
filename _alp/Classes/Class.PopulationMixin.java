/**
 * PopulationMixin
 */	

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonIgnoreProperties({"connections"})
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public abstract class PopulationMixin implements Serializable {
		
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

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}