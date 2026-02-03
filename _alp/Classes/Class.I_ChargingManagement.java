
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

public interface I_ChargingManagement extends I_AssetManagement
{
	void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables);
	
	public OL_ChargingAttitude getCurrentChargingType();
	
	public void setV2GActive(boolean activateV2G);
	public boolean getV2GActive();
}



