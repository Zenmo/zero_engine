/**
 * ActorMixin
 */	
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.CLASS,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON
	)
/*
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConnectionOwner.class, name = "ConnectionOwner"),
    @JsonSubTypes.Type(value = EnergyCoop.class, name = "EnergyCoop"),
    @JsonSubTypes.Type(value = EnergySupplier.class, name = "EnergySupplier"),
    @JsonSubTypes.Type(value = GridOperator.class, name = "GridOperator"),
    @JsonSubTypes.Type(value = GovernmentLayer.class, name = "GovernmentLayer"),
    @JsonSubTypes.Type(value = NationalEnergyMarket.class, name = "NationalEnergyMarket"),
    
    //@JsonSubTypes.Type(value = EnergyCoopL4L.class, name = "EnergyCoopL4L"),
   // @JsonSubTypes.Type(value = J_EAConversionGasBurner.class, name = "J_EAConversionGasBurner"),
   // @JsonSubTypes.Type(value = J_EAConversionHeatPump.class, name = "J_EAConversionHeatPump"),
   // @JsonSubTypes.Type(value = J_EAEV.class, name = "J_EAEV"),
   // @JsonSubTypes.Type(value = J_EADieselVehicle.class, name = "J_EADieselVehicle"),
    
    // Add other known subtypes here if needed
})
*/
//@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public abstract class ActorMixin implements Serializable {
		
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