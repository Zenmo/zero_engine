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
public interface I_AggregatorChargingManagement  extends I_AggregatorAssetManagement
{
	//Get the subManagementInterface type
	default Class<? extends I_AggregatorAssetManagement> getAggregatorAssetManagementInterfaceType(){
		return I_AggregatorChargingManagement.class;
	}

	void manageExternalChargingSetpoints(J_TimeVariables timeVariables);
}