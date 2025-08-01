/**
 * J_DeliveryContract
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
	    fieldVisibility = Visibility.PROTECTED_AND_PUBLIC,    // ✅ only public fields are serialized
	    getterVisibility = Visibility.NONE,
	    isGetterVisibility = Visibility.NONE,
	    setterVisibility = Visibility.NONE,
	    creatorVisibility = Visibility.NONE
	)

public class J_DeliveryContract extends zero_engine.J_Contract implements Serializable {

	//public String contractScope; // Kan energySupplier of Coop zijn  (dus kan niet specifieker zijn dan 'String' hier, tenzij we Interfaces gaan gebruiken. ('Implements energy supplier')
	public OL_DeliveryContractType deliveryContractType; // Fixed of variable
	public double deliveryPrice_eurpkWh;
	public double feedinPrice_eurpkWh;
	//public double idx;
	//public OL_EnergyCarriers energyCarrier;
	
	  /**
     * Constructor initializing the fields
     */
	public J_DeliveryContract() {
		
	}
	
    public J_DeliveryContract(String contractScope, OL_DeliveryContractType contractType, OL_EnergyCarriers energyCarrier, double deliveryPrice_eurpkWh, double feedinPrice_eurpkWh, double annualFee_eur ) {
	    this.contractScope = contractScope; // ID of actor that is the other 'end' of this contract.
	    this.deliveryContractType = contractType; // Fixed or variable energy price?
	    this.energyCarrier = energyCarrier;
	    this.deliveryPrice_eurpkWh = deliveryPrice_eurpkWh;
	    this.feedinPrice_eurpkWh = feedinPrice_eurpkWh;
	    this.contractType = OL_ContractType.DELIVERY;
	    this.annualFee_eur = annualFee_eur;
	}
    
    @Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"contractScope = " + contractScope +" " +
			"deliveryContractType = " + deliveryContractType.toString() +" "+
			"deliveryPrice_eurpkWh = " + deliveryPrice_eurpkWh +" "+
			"feedinPrice_eurpkWh = " + feedinPrice_eurpkWh +" ";
	}
}

