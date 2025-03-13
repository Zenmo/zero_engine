/**
 * J_ConnectionMetaData
 */	
public class J_ConnectionMetaData implements Serializable {

	public Agent parentAgent;

	public Double contractedDeliveryCapacity_kW;
	public Double contractedFeedinCapacity_kW;
	public Double physicalCapacity_kW;
	public Boolean contractedDeliveryCapacityKnown;
	public Boolean contractedFeedinCapacityKnown;
	public Boolean physicalCapacityKnown;
	
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

	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}