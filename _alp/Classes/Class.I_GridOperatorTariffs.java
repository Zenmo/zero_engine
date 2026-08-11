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

public interface I_GridOperatorTariffs
{
	public double getPhysicalCapacityCost_eurpyr(J_ConnectionMetaData connectionMetaData);
    public double getContractCapacityCost_eurpyr(J_ConnectionMetaData connectionMetaData);
    public double getTransportCost_eur(J_ConnectionMetaData connectionMetaData, double transportedElectricity_kWh);
    public double getMonthlyPeakCost_eur(J_ConnectionMetaData connectionMetaData, double monthlyPeakLoad_kW);
}