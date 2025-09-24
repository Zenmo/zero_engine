import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.CLASS,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON 👈
	)
/*
@JsonSubTypes({
    @JsonSubTypes.Type(value = J_AggregatorBatteryManagementOff.class, name = "J_AggregatorBatteryManagementOff"),
    @JsonSubTypes.Type(value = J_AggregatorBatteryManagementCollectiveSelfConsumption_batterySize.class, name = "J_AggregatorBatteryManagementCollectiveSelfConsumption_batterySize"),
    @JsonSubTypes.Type(value = J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRate.class, name = "J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRate"),
    @JsonSubTypes.Type(value = J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRateGH.class, name = "J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRateGH"),
})
*/

/**
 * I_AggregatorBatteryManagement
 */	

public interface I_AggregatorBatteryManagement
{
	void manageExternalSetpoints();
}