/**
 * J_ConnectionMetaData
 */	
public class J_ConnectionMetaData implements Serializable {

	public Agent parentAgent;

	public Double contractedDeliveryCapacity_kW  = 0.0;
	public Double contractedFeedinCapacity_kW  = 0.0;
	public Double physicalCapacity_kW  = 0.0;
	public Boolean contractedDeliveryCapacityKnown  = false;
	public Boolean contractedFeedinCapacityKnown  = false;
	public Boolean physicalCapacityKnown  = false;
	
    /**
     * Default constructor
     */
    public J_ConnectionMetaData( Agent parentAgent) {
    	this.parentAgent = parentAgent;
    	if (parentAgent instanceof GridConnection) {
    		
    	}
    	//public double getDeliveryCapacity_kW(){return p_contractedDeliveryCapacity_kW;}
    	//public double getFeedinCapacity_kW(){return p_contractedFeedinCapacity_kW;}
    	//public boolean getDeliveryCapacityKnown(){return b_isRealDeliveryCapacityAvailable;}
    	//public boolean getFeedinCapacityKnown(){return b_isRealFeedinCapacityAvailable;}
    }

    public J_ConnectionMetaData getClone() {
    	J_ConnectionMetaData clone = new J_ConnectionMetaData(this.parentAgent);
    	clone.contractedDeliveryCapacity_kW = this.contractedDeliveryCapacity_kW.doubleValue();
    	clone.contractedFeedinCapacity_kW = this.contractedFeedinCapacity_kW.doubleValue();
    	if (this.physicalCapacity_kW!=null) {
    		clone.physicalCapacity_kW = this.physicalCapacity_kW.doubleValue();
    	}
    	clone.contractedDeliveryCapacityKnown = this.contractedDeliveryCapacityKnown.booleanValue();
    	clone.contractedFeedinCapacityKnown = this.contractedFeedinCapacityKnown.booleanValue();
    	if (this.physicalCapacityKnown!=null) {
    		clone.physicalCapacityKnown = this.physicalCapacityKnown.booleanValue();
    	}
    	return clone;
    }
    
    @Override
    public String toString() {
        return "ContractedDeliveryCapacity_kW: " + contractedDeliveryCapacity_kW + 
               ", ContractedFeedinCapacity_kW: " + contractedFeedinCapacity_kW + 
               ", PhysicalCapacity_kW: " + physicalCapacity_kW + 
               ", ContractedDeliveryCapacityKnown: " + contractedDeliveryCapacityKnown + 
               ", ContractedFeedinCapacityKnown: " + contractedFeedinCapacityKnown + 
               ", PhysicalCapacityKnown: " + physicalCapacityKnown;
    }

	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}