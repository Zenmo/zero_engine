/**
 * J_ChargePoint
 */	
public class J_ChargePoint {
	
	private boolean hasSocketRestrictions;
	private int nbSockets;
	private double maxChargeCapacityPerSocket_kW;
	private List<Double> socketCapacitiesList_kW;
	private boolean V1GCapable;
	private boolean V2GCapable;
	
	private List<I_ChargingRequest> currentActiveChargingRequests = new ArrayList<>();
	

	private List<I_ChargingRequest> storedActiveChargingRequests = null;

	/**
     * Default constructor
     * No restrictions on sockets
     */
    public J_ChargePoint(boolean V1GCapable, boolean V2GCapable ) {
    	this.V1GCapable = V1GCapable;
    	this.V2GCapable = V2GCapable;
    	this.hasSocketRestrictions = false;
    }

   /**
    * With equal restrictions on sockets
    */
   public J_ChargePoint(boolean V1GCapable, boolean V2GCapable, double maxChargeCapacityPerSocket_kW) {	
   	this.V1GCapable = V1GCapable;
   	this.V2GCapable = V2GCapable;
   	this.addSocketRestrictions(maxChargeCapacityPerSocket_kW);
   }
   
    /**
     * With various restrictions on sockets
     */
    public J_ChargePoint(boolean V1GCapable, boolean V2GCapable, List<Double> socketCapacitiesList_kW) {	
    	this.V1GCapable = V1GCapable;
    	this.V2GCapable = V2GCapable;
    	this.addSocketRestrictions(socketCapacitiesList_kW);
    }
    
    
    
    //Charge chargingRequest trough socket
    public void charge( I_ChargingRequest chargingRequest, double charge_kW ) {
		if (charge_kW < 0 && !this.V2GCapable) {
			throw new RuntimeException("Trying to do V2G trough a ChargePoint that is not V2GCapable");
		}
		chargingRequest.f_updateAllFlows( charge_kW / chargingRequest.getChargingCapacity_kW());
    }
    
    protected void performCheck() { //This call will check if all chargingrequest have been charged in a timestep. 
    	boolean check = true;
    	if(!check) {
    		throw new RuntimeException("Not all active charging requests where charged.");
    	}
    }
    
    // This function is called every timestep before the management function
    public void updateActiveChargingRequests(GridConnection parentGC, double t_h) {
    	
    	// Remove all charging requests that are finished
    	List<I_ChargingRequest> finishedChargingRequests = new ArrayList<>();
    	for (I_ChargingRequest chargingRequest : this.currentActiveChargingRequests) {
 
    		if ( t_h >= chargingRequest.getLeaveTime_h() ) {
    			finishedChargingRequests.add(chargingRequest);
    		}
    	}
    	this.currentActiveChargingRequests.removeAll(finishedChargingRequests);
    	
    	// Find if there are new charging requests
    	// Vehicles
    	for (J_EAEV ev : parentGC.c_electricVehicles) {
    		if (ev.getAvailability() && !this.currentActiveChargingRequests.contains(ev) ) {
    			this.addChargingRequest(ev);
    		}    		
    	}
    	//ChargingSessions
    	for (J_EAChargingSession chargingSession : parentGC.c_chargingSessions) {
    		if (chargingSession.getAvailability(t_h) && !this.currentActiveChargingRequests.contains(chargingSession) ) {
    			this.addChargingRequest(chargingSession);
    		}    		
    	}
    }
    
    public void addChargingRequest( I_ChargingRequest chargingRequest ) {
    	// TODO: (Longterm) Make this more complex when we need to take socket restrictions into account.
    	this.currentActiveChargingRequests.add(chargingRequest);
    }
    
    public double getMaxChargingCapacity_kW(I_ChargingRequest chargingRequest) {
    	if(hasSocketRestrictions) {
    		return min(chargingRequest.getChargingCapacity_kW(), this.getSocketChargingCapacity_kW(chargingRequest));    		
    	}
    	else {
    		return chargingRequest.getChargingCapacity_kW();
    	}
    }
    
	public double getChargeDeadline_h(I_ChargingRequest chargingRequest) {
    	if(hasSocketRestrictions) {
			double chargeNeedForNextTrip_kWh = chargingRequest.getRemainingChargeDemand_kWh();
			double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
			double nextTripStartTime_h = chargingRequest.getLeaveTime_h();
			double chargeDeadline_h = nextTripStartTime_h - (chargeNeedForNextTrip_kWh / this.getSocketChargingCapacity_kW(chargingRequest)) - chargeTimeMargin_h;
			return chargeDeadline_h;    		
    	}
    	else {
    		return chargingRequest.getChargeDeadline_h();
    	}
	}
	
	
    public void addSocketRestrictions( double maxChargeCapacityPerSocket_kW) {
    	if(maxChargeCapacityPerSocket_kW <= 0) {
    		throw new RuntimeException("Trying to add socket restrictions to a J_ChargePoint with maxChargeCapacityPerSocket_kW = " + maxChargeCapacityPerSocket_kW);
    	}
    	this.hasSocketRestrictions = true;
    	this.maxChargeCapacityPerSocket_kW = maxChargeCapacityPerSocket_kW;
    }
    public void addSocketRestrictions( List<Double> socketCapacitiesList_kW) {
    	for(Double socketCapacity_kW : socketCapacitiesList_kW) {
    		if(socketCapacity_kW <= 0) {
        		throw new RuntimeException("Trying to add a socket restrictionsList to a J_ChargePoint that contains a socketCapacity_kW of " + socketCapacity_kW);
        	}
    	}
    	this.hasSocketRestrictions = true;
    	this.socketCapacitiesList_kW = socketCapacitiesList_kW;
    }
  
    private double getSocketChargingCapacity_kW(I_ChargingRequest chargingRequest) {
    	if(hasSocketRestrictions) {
    		if(socketCapacitiesList_kW == null) {
    			return maxChargeCapacityPerSocket_kW;
    		}
    		else {
    			return this.socketCapacitiesList_kW.get(getSocketIndexNb(chargingRequest));
    		}
    	}
    	else {
    		return chargingRequest.getChargingCapacity_kW();
    	}
    }
    
    private int getSocketIndexNb(I_ChargingRequest chargingRequest) {
    	return this.currentActiveChargingRequests.indexOf(chargingRequest);
    }
    
    //V1G and V2G capabilities setters/getters
    public void setV1GCapability(boolean V1GCapable) {
		this.V1GCapable = V1GCapable;
	}
	public void setV2GCapability(boolean V2GCapable) {
		this.V2GCapable = V2GCapable;
	}
	public boolean getV1GCapable() {
		return this.V1GCapable;
	}
	public boolean getV2GCapable() {
		return this.V2GCapable;
	}
	
	public int getCurrentNumberOfChargeRequests() {
		return this.currentActiveChargingRequests.size();
	}
	
	public List<I_ChargingRequest> getCurrentActiveChargingRequests(){
		return this.currentActiveChargingRequests;
	}
	
    public void storeStatesAndReset() {
    	this.storedActiveChargingRequests = currentActiveChargingRequests;
    	currentActiveChargingRequests = new ArrayList<>();	
    }
    
    public void restoreStates() {
    	currentActiveChargingRequests = storedActiveChargingRequests;
    }
    
	@Override
	public String toString() {
		return super.toString();
	}
}