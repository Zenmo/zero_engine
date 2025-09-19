
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
    @JsonSubTypes.Type(value = J_ChargingManagementLocalBalancing.class, name = "J_ChargingManagementLocalBalancing"),
    @JsonSubTypes.Type(value = J_ChargingManagementGridBalancing.class, name = "J_ChargingManagementGridBalancing"),
    @JsonSubTypes.Type(value = J_ChargingManagementMaxAvailablePower.class, name = "J_ChargingManagementMaxAvailablePower"),
    @JsonSubTypes.Type(value = J_ChargingManagementPrice.class, name = "J_ChargingManagementPrice"),
    @JsonSubTypes.Type(value = J_ChargingManagementSimple.class, name = "J_ChargingManagementSimple"),
})
    
public interface I_ChargingManagement
{
	void manageCharging();
	
	void initialize();
	
	OL_ChargingAttitude getCurrentChargingType();
	
	void setV2GActive(boolean activateV2G);
	boolean getV2GActive();
}



