/**
 * J_ConnectionMetaData
 */	
public class J_ConnectionMetaData {

	private Agent parentAgent;

	private double contractedDeliveryCapacity_kW;
	private double contractedFeedinCapacity_kW;
	private double physicalCapacity_kW;
	private boolean contractedDeliveryCapacityKnown;
	private boolean contractedFeedinCapacityKnown;
	private boolean physicalCapacityKnown;
	
	private OL_ConnectionSizeType connectionSizeType;
	
	//Capacity sharing
	private List<J_CapacitySharingContract> capacitySharingContracts = new ArrayList();
	
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
		
		//Calculate delta for coop capacity adjustment
		double deltaContractedDeliveryCapacity_kW = contractedDeliveryCapacity_kW - this.contractedDeliveryCapacity_kW;
		double deltaContractedFeedinCapacity_kW = contractedFeedinCapacity_kW - this.contractedFeedinCapacity_kW;
		
		//Set new capacities
		this.contractedDeliveryCapacity_kW = contractedDeliveryCapacity_kW;
		this.contractedFeedinCapacity_kW = contractedFeedinCapacity_kW;
		this.physicalCapacity_kW = physicalCapacity_kW;
		
		//Set connection size type aswell: 3*80 -> Large connection (grootverbruik), else small connection (kleinverbruik)
		if(physicalCapacity_kW > (3*80*230/1000.0)) { 
			this.connectionSizeType = OL_ConnectionSizeType.LARGE_CONNECTION;
		}
		else {
			this.connectionSizeType  = OL_ConnectionSizeType.SMALL_CONNECTION;
		}
		
		//Check if contracted  capacity is never larger than physical: not allowed.
		if(contractedDeliveryCapacity_kW > physicalCapacity_kW || contractedFeedinCapacity_kW > physicalCapacity_kW) {
			throw new RuntimeException("Set connection limits (Delivery = " + contractedDeliveryCapacity_kW + "), (Feedin = " + contractedFeedinCapacity_kW + "), "
									   + "(physical = " + physicalCapacity_kW + ") is not possible. Contract capacities can never be higher than physical.");
		}
		
		//For GridConnection only: Also adjust the cumulative contracted capacity values for the parent coops.
		if(parentAgent instanceof GridConnection GC) {
			for(EnergyCoop coop : GC.c_parentCoops) {
				double coopNewContractedDeliveryCapacity_kW = coop.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW() + deltaContractedDeliveryCapacity_kW;
				double coopNewContractedFeedinCapacity_kW = coop.v_liveConnectionMetaData.getContractedFeedinCapacity_kW() + deltaContractedFeedinCapacity_kW;
				double coopNewPhysicalCapacity_kW = max(coopNewContractedDeliveryCapacity_kW, coopNewContractedFeedinCapacity_kW);
				coop.v_liveConnectionMetaData.setCapacities_kW(coopNewContractedDeliveryCapacity_kW, coopNewContractedFeedinCapacity_kW, coopNewPhysicalCapacity_kW);
			}
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
	public boolean getContractedDeliveryCapacityKnown(){return this.contractedDeliveryCapacityKnown;};
	public boolean getContractedFeedinCapacityKnown(){return this.contractedFeedinCapacityKnown;};
	public boolean getPhysicalCapacityKnown(){return this.physicalCapacityKnown;}      
	public OL_ConnectionSizeType getConnectionSizeType(){return this.connectionSizeType;} 
	
	
	//Clone functionality
    public J_ConnectionMetaData getClone() {
    	J_ConnectionMetaData clone = new J_ConnectionMetaData(this.parentAgent);
    	clone.contractedDeliveryCapacity_kW = this.contractedDeliveryCapacity_kW;
    	clone.contractedFeedinCapacity_kW = this.contractedFeedinCapacity_kW;
    	clone.physicalCapacity_kW = this.physicalCapacity_kW;
    	clone.contractedDeliveryCapacityKnown = this.contractedDeliveryCapacityKnown;
    	clone.contractedFeedinCapacityKnown = this.contractedFeedinCapacityKnown;
    	clone.physicalCapacityKnown = this.physicalCapacityKnown;
    	if (connectionSizeType!=null) {
    		clone.connectionSizeType = this.connectionSizeType;
    	}
    	//Intentionally not cloning capacity sharing manager!!
    	return clone;
    }
    
    //Update capacity sharing contract
    public void updateGridCapacitySharingManager(J_TimeVariables timeVariables) {
    	capacitySharingContracts.forEach(csc -> csc.update((GridConnection)parentAgent, timeVariables));
    }
    
    //Get shared capacity
    public double getCurrentSharedDeliveryCapacity_kW() {
    	return sum(capacitySharingContracts, csc -> csc.getCurrentSharedDeliveryCapacity_kW((GridConnection)parentAgent));
    }
    public double getCurrentSharedFeedinCapacity_kW() {
    	return sum(capacitySharingContracts, csc -> csc.getCurrentSharedFeedinCapacity_kW((GridConnection)parentAgent));
    }
    
    //Has capacity sharing check
    public boolean hasCapacitySharingContract() {
    	return capacitySharingContracts.size() > 0;
    }
   
    //Add capacity sharing
    public void addCapacitySharingContract(J_CapacitySharingContract capacitySharingContract) {
    	this.capacitySharingContracts.add(capacitySharingContract);
    }
    //Clear shared capacity managers
    public void clearSharedCapacityManagers() {
    	capacitySharingContracts.clear();
    }
    
    //Capacity sharing at certain time
    public double getSharedDeliveryCapacityAtHourOfWeekDay_kW(int hourOfDay) {
    	return sum(capacitySharingContracts, csc -> csc.getSharedDeliveryCapacityAtHourOfWeekDay_kW(hourOfDay));
    }
    public double getSharedDeliveryCapacityAtHourOfWeekendDay_kW(int hourOfDay) {
    	return sum(capacitySharingContracts, csc -> csc.getSharedDeliveryCapacityAtHourOfWeekendDay_kW(hourOfDay));
    }
    public double getSharedFeedinCapacityAtHourOfWeekDay_kW(int hourOfDay) {
    	return sum(capacitySharingContracts, csc -> csc.getSharedFeedinCapacityAtHourOfWeekDay_kW(hourOfDay));
    }
    public double getSharedFeedinCapacityAtHourOfWeekendDay_kW(int hourOfDay) {
    	return sum(capacitySharingContracts, csc -> csc.getSharedFeedinCapacityAtHourOfWeekendDay_kW(hourOfDay));
    }   
    
    
    //Get only default contracted capacity
	public double getDefaultContractedDeliveryCapacity_kW(){return this.contractedDeliveryCapacity_kW;} 
	public double getDefaultContractedFeedinCapacity_kW(){return this.contractedFeedinCapacity_kW;} 
    
	
    
    @Override
    public String toString() {
        return "ContractedDeliveryCapacity_kW: " + contractedDeliveryCapacity_kW + "\n" + 
               "ContractedFeedinCapacity_kW: " + contractedFeedinCapacity_kW + "\n" + 
               "PhysicalCapacity_kW: " + physicalCapacity_kW + "\n" + 
               "ContractedDeliveryCapacityKnown: " + contractedDeliveryCapacityKnown + "\n" + 
               "ContractedFeedinCapacityKnown: " + contractedFeedinCapacityKnown + "\n" + 
               "PhysicalCapacityKnown: " + physicalCapacityKnown + "\n" + 
               "ConnectionSizeType: " + connectionSizeType + "\n" + 
               "CapacitySharingContracts: " + capacitySharingContracts.size();
    }
}