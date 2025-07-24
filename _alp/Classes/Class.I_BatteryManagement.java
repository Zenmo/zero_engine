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
    @JsonSubTypes.Type(value = J_BatteryManagementOff.class, name = "I_BatteryManagementOff"),
    @JsonSubTypes.Type(value = J_BatteryManagementPeakShaving.class, name = "I_BatteryManagementPeakShaving"),
    @JsonSubTypes.Type(value = J_BatteryManagementPeakShavingForecast.class, name = "I_BatteryManagementPeakShavingForecast"),
    @JsonSubTypes.Type(value = J_BatteryManagementPrice.class, name = "I_BatteryManagementPrice"),
    @JsonSubTypes.Type(value = J_BatteryManagementSelfConsumption.class, name = "I_BatteryManagementSelfConsumption"),
    @JsonSubTypes.Type(value = J_BatteryManagementSelfConsumptionGridNode.class, name = "I_BatteryManagementSelfConsumptionGridNode"),
})
    
    // Add other known subtypes here if needed

public interface I_BatteryManagement
{
	void manageBattery();
}