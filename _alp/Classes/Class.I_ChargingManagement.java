
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
/*@JsonSubTypes({
    @JsonSubTypes.Type(value = J_BatteryManagementOff.class, name = "J_BatteryManagementOff"),
    @JsonSubTypes.Type(value = J_BatteryManagementPeakShaving.class, name = "J_BatteryManagementPeakShaving"),
    @JsonSubTypes.Type(value = J_BatteryManagementPeakShavingForecast.class, name = "J_BatteryManagementPeakShavingForecast"),
    @JsonSubTypes.Type(value = J_BatteryManagementPrice.class, name = "J_BatteryManagementPrice"),
    @JsonSubTypes.Type(value = J_BatteryManagementSelfConsumption.class, name = "J_BatteryManagementSelfConsumption"),
    @JsonSubTypes.Type(value = J_BatteryManagementSelfConsumptionGridNode.class, name = "J_BatteryManagementSelfConsumptionGridNode"),
})*/
    
public interface I_ChargingManagement
{
	void manageCharging();
	
	void initialize();
	
	OL_ChargingAttitude getCurrentChargingType();
}



