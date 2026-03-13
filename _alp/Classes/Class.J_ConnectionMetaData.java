/**
 * J_ConnectionMetaData
 */	
public class J_ConnectionMetaData {

	private Agent parentAgent;

	private Double contractedDeliveryCapacity_kW  = 0.0;
	private Double contractedFeedinCapacity_kW  = 0.0;
	private Double physicalCapacity_kW  = 0.0;
	private Boolean contractedDeliveryCapacityKnown  = false;
	private Boolean contractedFeedinCapacityKnown  = false;
	private Boolean physicalCapacityKnown  = false;
	
	private OL_ConnectionSizeType connectionSizeType;
	
    /**
     * Default constructor
     */
	public J_ConnectionMetaData() {
		
	}
	
    public J_ConnectionMetaData( Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    
    //Setter functions
	public void setCapacities_kW(double contractedDeliveryCapacity_kW, double contractedFeedinCapacity_kW, double physicalCapacity_kW){
		this.contractedDeliveryCapacity_kW = contractedDeliveryCapacity_kW;
		this.contractedFeedinCapacity_kW = contractedFeedinCapacity_kW;
		this.physicalCapacity_kW = physicalCapacity_kW;
		
		if(physicalCapacity_kW > (3*80*230/1000.0)) { //Set connection size type aswell: 3*80 -> Large connection (grootverbruik), else small connection (kleinverbruik)
			this.connectionSizeType = OL_ConnectionSizeType.LARGE_CONNECTION;
		}
		else {
			this.connectionSizeType  = OL_ConnectionSizeType.SMALL_CONNECTION;
		}
		
		if(contractedDeliveryCapacity_kW > physicalCapacity_kW || contractedFeedinCapacity_kW > physicalCapacity_kW) {
			throw new RuntimeException("Set connection limits (Delivery = " + contractedDeliveryCapacity_kW + "), (Feedin = " + contractedFeedinCapacity_kW + "), "
									   + "(physical = " + physicalCapacity_kW + ") is not possible. Contract capacities can never be higher than physical.");
		}
	}
	
	public void setCapacitiesKnown(boolean contractedDeliveryCapacityKnown, boolean contractedFeedinCapacityKnown, boolean physicalCapacityKnown) {
		this.contractedDeliveryCapacityKnown = contractedDeliveryCapacityKnown;
		this.contractedFeedinCapacityKnown = contractedFeedinCapacityKnown;
		this.physicalCapacityKnown = physicalCapacityKnown;
	}

	public void setContractedDeliveryCapacityKnown(boolean contractedDeliveryCapacityKnown){this.contractedDeliveryCapacityKnown = contractedDeliveryCapacityKnown;}
	public void setContractedFeedinCapacityKnown(boolean contractedFeedinCapacityKnown){this.contractedFeedinCapacityKnown = contractedFeedinCapacityKnown;}    
	public void setPhysicalCapacityKnown(boolean physicalCapacityKnown){this.physicalCapacityKnown = physicalCapacityKnown;}  
	
	//Getters
	public double getContractedDeliveryCapacity_kW(){return this.contractedDeliveryCapacity_kW;} 
	public double getContractedFeedinCapacity_kW(){return this.contractedFeedinCapacity_kW;} 
	public double getPhysicalCapacity_kW(){return this.physicalCapacity_kW;}    
	public boolean getContractedDeliveryCapacityKnown(){
		if(this.connectionSizeType == OL_ConnectionSizeType.SMALL_CONNECTION) {
			return this.physicalCapacityKnown;
		}
		else {
			return this.contractedDeliveryCapacityKnown;
		}
	}
	public boolean getContractedFeedinCapacityKnown(){
		if(this.connectionSizeType == OL_ConnectionSizeType.SMALL_CONNECTION) {
			return this.physicalCapacityKnown;
		}
		else {
			return this.contractedFeedinCapacityKnown;
		}
	}
	public boolean getPhysicalCapacityKnown(){return this.physicalCapacityKnown;}      
	public OL_ConnectionSizeType getConnectionSizeType(){return this.connectionSizeType;} 
	
	
	//Clone functionality
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
    	if (connectionSizeType!=null) {
    		clone.connectionSizeType = this.connectionSizeType;
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
}