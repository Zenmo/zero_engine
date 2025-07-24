/**
 * J_TransportContract
 */
public class J_TransportContract extends zero_engine.J_Contract implements Serializable {

	//public String contractScope; // Kan GridOperator of Coop zijn (dus kan niet specifieker zijn dan 'String' hier, tenzij we Interfaces gaan gebruiken. ('Implements grid operator')
	//public OL_TransportContractType contractType; // Nodal-pricing, bandwidth, peak
	public OL_TransportContractType transportContractType; //
	public double bandwidthTreshold_kW;
	public double bandwidthTariff_eurpkWh;
	public double idx;
	//public OL_EnergyCarriers energyCarrier;
	
	  /**
     * Constructor initializing the fields
     */
	
	public J_TransportContract() {
		
	}
	
    public J_TransportContract(String contractScope, OL_TransportContractType transportContractType, OL_EnergyCarriers energyCarrier, double bandwidthTreshold_kW, double bandwidthTariff_eurpkWh, double annualFee_eur ) {
	    this.contractScope = contractScope; // ID of actor that is the other 'end' of this contract.	    
	    this.energyCarrier = energyCarrier;
	    this.transportContractType = transportContractType;
	    this.bandwidthTreshold_kW = bandwidthTreshold_kW;
	    this.bandwidthTariff_eurpkWh = bandwidthTariff_eurpkWh;
	    this.contractType = OL_ContractType.TRANSPORT;
	    this.annualFee_eur = annualFee_eur;
	}
    
    @Override
   	public String toString() {
   		return
   			"type = " + this.getClass().toString() + " " +
   			"contractScope = " + contractScope +" ";
   	}
}
