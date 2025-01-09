/**
 * J_TaxContract
 */
public class J_TaxContract extends zero_engine.J_Contract implements Serializable {

	//public String contractScope; // Kan energySupplier of Coop zijn
	//public OL_ContractType contractType; // Fixed of variable
	public double deliveryTax_eurpkWh;
	public double feedinTax_eurpkWh;
	public double proportionalTax_pct;
	public double idx;
	//public OL_EnergyCarriers energyCarrier;
	
	  /**
     * Constructor initializing the fields
     */
    public J_TaxContract(String contractScope, OL_EnergyCarriers energyCarrier, double deliveryTax_eurpkWh, double feedinTax_eurpkWh, double proportionalTax_pct, double annualFee_eur ) {
	    this.contractScope = contractScope; // ID of actor that is the other 'end' of this contract.	    
	    this.energyCarrier = energyCarrier;
	    this.deliveryTax_eurpkWh = deliveryTax_eurpkWh;
	    this.feedinTax_eurpkWh = feedinTax_eurpkWh;
	    this.proportionalTax_pct = proportionalTax_pct;
	    this.contractType = OL_ContractType.TAX;
	    this.annualFee_eur = annualFee_eur;
	}
}