/**
 * J_ConnectionContract
 */
public class J_ConnectionContract extends zero_engine.J_Contract implements Serializable {

	//public String contractScope; // Kan energySupplier of Coop zijn
	public OL_ConnectionContractType connectionContractType; // Fixed of variable
	public double nfATOstart_h = 0;
	public double nfATOend_h = 0;
	public double nfATOpower_kW = 0;
	public double idx;
	
    public J_ConnectionContract() {
    }
	  /**
     * Constructor initializing the fields
     */
    public J_ConnectionContract(String contractScope, OL_ConnectionContractType contractType, OL_EnergyCarriers energyCarrier, double nfATOstart_h, double nfATOend_h, double nfATOpower_kW, double annualFee_eur ) {
	    this.contractScope = contractScope; // ID of actor that is the other 'end' of this contract.
	    this.connectionContractType = contractType; // Fixed or variable energy price?
	    this.energyCarrier = energyCarrier;
	    this.nfATOstart_h = nfATOstart_h;
	    this.nfATOend_h = nfATOend_h;
	    this.nfATOpower_kW = nfATOpower_kW;
	    this.contractType = OL_ContractType.CONNECTION;
	    this.annualFee_eur = annualFee_eur;
	}
}