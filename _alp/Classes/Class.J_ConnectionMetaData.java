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
	
	//Capacity sharing
	private List<J_GridCapacitySharingManager> capacitySharingManagers = new ArrayList();
	
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
	public double getContractedDeliveryCapacity_kW(){return this.contractedDeliveryCapacity_kW + getCurrentSharedDeliveryCapacity_kW();} 
	public double getContractedFeedinCapacity_kW(){return this.contractedFeedinCapacity_kW + getCurrentSharedFeedinCapacity_kW();} 
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
    	//Intentionally not cloning capacity sharing manager!!
    	return clone;
    }
    
    //Update capacity sharing contract
    public void updateGridCapacitySharingManager(J_TimeVariables timeVariables) {
    	capacitySharingManagers.forEach(csm -> csm.update((GridConnection)parentAgent, timeVariables));
    }
    
    //Get shared capacity
    public double getCurrentSharedDeliveryCapacity_kW() {
    	return sum(capacitySharingManagers, csm -> csm.getCurrentSharedDeliveryCapacity_kW((GridConnection)parentAgent));
    }
    public double getCurrentSharedFeedinCapacity_kW() {
    	return sum(capacitySharingManagers, csm -> csm.getCurrentSharedFeedinCapacity_kW((GridConnection)parentAgent));
    }
    
    //Has capacity sharing check
    public boolean hasCapacitySharingContract() {
    	return capacitySharingManagers.size() > 0;
    }
   
    //Add capacity sharing
    public void addSharedCapacityManager(J_GridCapacitySharingManager capacitySharingContract) {
    	this.capacitySharingManagers.add(capacitySharingContract);
    }
    //Clear shared capacity managers
    public void clearSharedCapacityManagers() {
    	capacitySharingManagers.clear();
    }
    
    //Capacity sharing at certain time
    public double getSharedDeliveryCapacityAtHourOfWeekDay_kW(int hourOfDay) {
    	return sum(capacitySharingManagers, csm -> csm.getSharedDeliveryCapacityAtHourOfWeekDay_kW(hourOfDay));
    }
    public double getSharedDeliveryCapacityAtHourOfWeekendDay_kW(int hourOfDay) {
    	return sum(capacitySharingManagers, csm -> csm.getSharedDeliveryCapacityAtHourOfWeekendDay_kW(hourOfDay));
    }
    public double getSharedFeedinCapacityAtHourOfWeekDay_kW(int hourOfDay) {
    	return sum(capacitySharingManagers, csm -> csm.getSharedFeedinCapacityAtHourOfWeekDay_kW(hourOfDay));
    }
    public double getSharedFeedinCapacityAtHourOfWeekendDay_kW(int hourOfDay) {
    	return sum(capacitySharingManagers, csm -> csm.getSharedFeedinCapacityAtHourOfWeekendDay_kW(hourOfDay));
    }   
    
    
    //Get only default contracted capacity
	public double getDefaultContractedDeliveryCapacity_kW(){return this.contractedDeliveryCapacity_kW;} 
	public double getDefaultContractedFeedinCapacity_kW(){return this.contractedFeedinCapacity_kW;} 
    
	
    
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