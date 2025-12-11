/**
 * J_ChargePoint
 */	
public class J_ChargePoint {
	
	private GridConnection parentGC;

	private boolean hasSocketRestrictions;
	private int nbSockets;
	private double totalCapacity_kW;
	private List<Double> socketCapacities_kW;
	private boolean V1GCapable;
	private boolean V2GCapable;
	
	private List<I_ChargingRequest> currentActiveChargingRequests = new ArrayList<>();
	
	//private double currentChargePower_kW;
	//private double currentDischargePower_kW;
	
	private List<I_ChargingRequest> storedActiveChargingRequests = null;
    /**
     * Default constructor
     * No restrictions on sockets
     */
    public J_ChargePoint( GridConnection parentGC, boolean V1GCapable, boolean V2GCapable ) {
    	this.parentGC = parentGC;
    	this.V1GCapable = V1GCapable;
    	this.V2GCapable = V2GCapable;
    	this.hasSocketRestrictions = false;
    }
    
    /**
     * With restrictions on sockets
     */
    public J_ChargePoint( GridConnection parentGC, OL_EnergyAssetType energyAssetType, List<J_EAChargingSession> chargeSessionList, boolean V1GCapable, boolean V2GCapable, List<Double> socketCapacities_kW) {
    	this.parentGC = parentGC; 	
    	this.V1GCapable = V1GCapable;
    	this.V2GCapable = V2GCapable;
    	this.addSocketRestrictions(socketCapacities_kW, totalCapacity_kW);
    }
    
    public void addSocketRestrictions( List<Double> socketCapacities_kW, double totalCapacity_kW ) {
    	this.hasSocketRestrictions = true;
    	this.socketCapacities_kW = socketCapacities_kW;
    	this.totalCapacity_kW = totalCapacity_kW;
    }
    
    public void powerSockets( List<Double> charges_kW ) {
    	double currentChargePower_kW = 0;
    	double currentDischargePower_kW = 0;
    	for (int i = 0; i < this.currentActiveChargingRequests.size(); i++) {
    		if (charges_kW.get(i) < 0 && !this.V2GCapable) {
    			throw new RuntimeException("Trying to do V2G trough a ChargePoint that is not V2GCapable");
    		}
    		this.currentActiveChargingRequests.get(i).f_updateAllFlows( charges_kW.get(i) / this.currentActiveChargingRequests.get(i).getChargingCapacity_kW());

    		// check socket capacity?
    		currentChargePower_kW += max(0, charges_kW.get(i));
    		currentDischargePower_kW += max(0, -charges_kW.get(i));
    	}

    	// The management class should never have all the charging sessions go over the total available capacity, so this is a safety check
    	if (this.hasSocketRestrictions) {
	    	if (currentChargePower_kW > this.totalCapacity_kW || currentDischargePower_kW > this.totalCapacity_kW) {
    			// kan dit niet? of moet het gewoon gecapt worden?
	    		throw new RuntimeException("Trying to charge trough a ChargePoint with a higher power than is possible.");
	    	}
    	}
    }
    
    
    // This function is called every timestep before the management function
    public void updateActiveChargingRequests(double t_h) {
    	
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