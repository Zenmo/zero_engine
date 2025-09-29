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
/*
@JsonSubTypes({
    @JsonSubTypes.Type(value = J_DeliveryContract.class, name = "J_DeliveryContract"),
    @JsonSubTypes.Type(value = J_ConnectionContract.class, name = "J_ConnectionContract"),
    @JsonSubTypes.Type(value = J_TransportContract.class, name = "J_TransportContract"),
    @JsonSubTypes.Type(value = J_TaxContract.class, name = "J_TaxContract"),
})
*/

public class J_Contract implements Serializable {

	public String contractScope; // Kan energySupplier of Coop zijn  (dus kan niet specifieker zijn dan 'String' hier, tenzij we Interfaces gaan gebruiken. ('Implements energy supplier')
	public OL_EnergyCarriers energyCarrier;
	//public OL_ContractType contractType;
	public double EnergyTransactionVolume_kWh=0;
	public double FinancialTransactionVolume_eur=0;
	public String contractHolder;
	public double annualFee_eur=0;
	
    public J_Contract() {
    }
	  /**
     * Constructor initializing the fields
     */
    public J_Contract(String contractScope, OL_EnergyCarriers energyCarrier) {
	    this.contractScope = contractScope; // ID of actor that is the other 'end' of this contract.
	    //this.contractType = contractType; // Fixed or variable energy price?
	    this.energyCarrier = energyCarrier;
	}
    
    @Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"contractScope = " + contractScope +" ";
	}
}

