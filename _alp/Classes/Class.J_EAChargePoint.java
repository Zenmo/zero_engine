/**
 * J_EAChargePoint
 */	
public class J_EAChargePoint extends J_EA implements Serializable {

	private boolean hasSocketRestrictions;
	private int nbSockets;
	private double totalCapacity_kW;
	private List<Double> socketCapacities_kW;
	private boolean V1GCapable;
	private boolean V2GCapable;
	private boolean V2GActive;
	private List<J_ChargingSession> chargeSessionList;
	private int chargeSessionIndex = 0;
	
	private List<I_ChargingRequest> currentActiveChargingRequests = new ArrayList<>();
	
	private double currentChargePower_kW;
	private double currentDischargePower_kW;
	
	private int chargeSessionIndexStored;
	private List<I_ChargingRequest> storedActiveChargingRequests = null;
    /**
     * Default constructor
     * No restrictions on sockets
     */
    public J_EAChargePoint( Agent parentAgent, OL_EnergyAssetType energyAssetType, List<J_ChargingSession> chargeSessionList, boolean V1GCapable, boolean V2GCapable ) {
    	// moet de lijst met sessies in de constructor? of willen we deze in de gc opslaan? of iets anders?
    	this.parentAgent = parentAgent;
    	this.energyAssetType = energyAssetType;
    	this.chargeSessionList = chargeSessionList;
    	this.V1GCapable = V1GCapable;
    	this.V2GCapable = V2GCapable;
    	
    	this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
    	if (V2GCapable) {
        	this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
    	}
    	
    	this.hasSocketRestrictions = false;
    }
    
    /**
     * With restrictions on sockets
     */
    public J_EAChargePoint( Agent parentAgent, OL_EnergyAssetType energyAssetType, List<J_ChargingSession> chargeSessionList, boolean V1GCapable, boolean V2GCapable, List<Double> socketCapacities_kW) {
    	this.parentAgent = parentAgent;
    	this.energyAssetType = energyAssetType;
    	this.chargeSessionList = chargeSessionList;    	
    	this.V1GCapable = V1GCapable;
    	this.V2GCapable = V2GCapable;
    	
    	this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
    	if (V2GCapable) {
        	this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
    	}
    	
    	this.addSocketRestrictions(socketCapacities_kW, totalCapacity_kW);
    }
    
    public void addSocketRestrictions( List<Double> socketCapacities_kW, double totalCapacity_kW ) {
    	this.hasSocketRestrictions = true;
    	this.socketCapacities_kW = socketCapacities_kW;
    	this.totalCapacity_kW = totalCapacity_kW;
    }
    
    public void manageSockets( List<Double> charges_kW ) {
    	this.currentChargePower_kW = 0;
    	this.currentDischargePower_kW = 0;
    	for (int i = 0; i < this.currentActiveChargingRequests.size(); i++) {
    		if (charges_kW.get(i) < 0 && (!this.V2GCapable || !this.V2GActive)) {
    			// kan dit niet? of moet het gewoon gezet worden op 0?
    			throw new RuntimeException("kann niee");
    		}
    		double x = this.currentActiveChargingRequests.get(i).charge_kW( charges_kW.get(i));

    		// check socket capacity?
    		this.currentChargePower_kW += max(0, x);
    		this.currentDischargePower_kW += max(0, -x);
    	}

    	// The management class should never have all the charging sessions go over the total available capacity, so this is a safety check
    	if (this.hasSocketRestrictions) {
	    	if (this.currentChargePower_kW > this.totalCapacity_kW || this.currentDischargePower_kW > this.totalCapacity_kW) {
    			// kan dit niet? of moet het gewoon gecapt worden?
	    		throw new RuntimeException("klopt niet");
	    	}
    	}
    	
    	double chargePower_kW = this.currentChargePower_kW + this.currentDischargePower_kW;
    	this.f_updateAllFlows(chargePower_kW);

    }
    
    @Override
    public void f_updateAllFlows(double chargePower_kW) {
    	operate(chargePower_kW);
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);    		
    	}
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }

    @Override
    public void operate(double chargePower_kW) {
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, chargePower_kW);
		// Split charging and discharing power 'at the source'!
		assetFlowsMap.put(OL_AssetFlowCategories.evChargingPower_kW, this.currentChargePower_kW);
		if (this.V2GCapable && this.V2GActive) {
			assetFlowsMap.put(OL_AssetFlowCategories.V2GPower_kW, this.currentDischargePower_kW);
		}		
    }
    
    // This function is called every timestep before the management function
    public void updateActiveChargingRequests() {
    	// Remove all charging requests that are finished
    	List<I_ChargingRequest> finishedChargingRequests = new ArrayList<>();
    	for (I_ChargingRequest chargingRequest : this.currentActiveChargingRequests) {
    		// here we will use the soon to be global parameter current time
    		if ( J_TimeVariables.getT_h() >= chargingRequest.getLeaveTime_h() ) {
    			finishedChargingRequests.add(chargingRequest);
    		}
    	}    	
    	this.currentActiveChargingRequests.removeAll(finishedChargingRequests);
    	// Find if there are new charging requests
    	// Vehicles
    	for (J_EAEV ev : ((GridConnection)parentAgent).c_electricVehicles) {
    		if (ev.getAvailability() && !this.currentActiveChargingRequests.contains(ev) ) {
    			this.addChargingRequest(ev);
    		}    		
    	}
    	// ChargingSessions
    	while ( t_h <= chargeSessionList.get(chargeSessionIndex).startTime_h ) {
        	this.addChargingRequest(chargeSessionList.get(chargeSessionIndex));
        	chargeSessionIndex++;
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
		this.updateAssetFlowCategory();
	}
	public boolean getV1GCapable() {
		return this.V1GCapable;

	}
	public boolean getV2GCapable() {
		return this.V2GCapable;

	}
	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.updateAssetFlowCategory();
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
    @Override
    public void storeStatesAndReset() {
    	// TODO: Finish this !!
    	this.chargeSessionIndexStored = this.chargeSessionIndex;
    	this.chargeSessionIndex = 0;
    	this.storedActiveChargingRequests = currentActiveChargingRequests;
    	currentActiveChargingRequests = new ArrayList<>();
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	clear();    	
    }
    
    @Override
    public void restoreStates() {
    	// TODO: Finish this !!
    	chargeSessionIndex = chargeSessionIndexStored;
    	currentActiveChargingRequests = storedActiveChargingRequests;
    	energyUsed_kWh = energyUsedStored_kWh;
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