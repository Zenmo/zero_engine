/**
 * J_EAChargingSession
 */	
public class J_EAChargingSession extends zero_engine.J_EA implements I_ChargingRequest {
	
	private List<J_ChargingSession> chargingSessionList;
	private int socketNb;

	//Time parameter
	double timeStep_h;
	
	//Current session information
	private J_ChargingSession currentChargingSession;
	private int nextSessionIndex = 0;
	
	//Vehicle scaling
	private double vehicleScaling = 1;
	
	/*
	double startTime_h;
	double endTime_h;

	double chargingDemand_kWh;
	double avgPowerDemand_kW;
	double batterySize_kWh;
	double stateOfCharge_kWh;
	double vehicleMaxChargingPower_kW;
	int socketNb;
	boolean V2GCapable = true;
	*/
	
	//V2GActive/capability (override) parameters
	private boolean V2GActive = false;
	private double V2GCapabilityProbablityOverride = 0;
	private boolean overrideV2GCapability = false;

	//Monitoring
	private double totalCharged_kWh = 0;
	private double totalDischarged_kWh = 0;
	
	//Stored live sim values
	private double storedTotalCharged_kWh = 0;
	private double storedTotalDischarged_kWh = 0;	
	private J_ChargingSession storedCurrentChargingSession;
	private int storedNextSessionIndex = 0;	
	
    /**
     * Default constructor
     */
	public J_EAChargingSession(GridConnection parentGC, List<J_ChargingSession> chargingSessionList, int socketNb) {
		this.parentAgent = parentGC;	
		
		this.socketNb = socketNb;
    	this.chargingSessionList = chargingSessionList;
    	this.timeStep_h = parentGC.energyModel.p_timeParameters.getTimeStep_h();
    	
	    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);   	
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
		this.setV2GActive(this.V2GActive);
		
		registerEnergyAsset();
	}
	
	@Override
	public void operate(double ratioOfChargeCapacity_r) {
    	double chargeSetpoint_kW = ratioOfChargeCapacity_r * this.getChargingCapacity_kW(); // capped between -1 and 1. (does already happen in f_updateAllFlows()!)
    	double chargePower_kW = max(min(chargeSetpoint_kW, (this.getStorageCapacity_kWh() - this.getCurrentSOC_kWh()) / this.timestep_h), -this.getCurrentSOC_kWh() / this.timestep_h); // Limit charge power to stay within SoC 0-100
    	
    	double electricityProduction_kW = max(-chargePower_kW, 0);
		double electricityConsumption_kW = max(chargePower_kW, 0);
		this.currentChargingSession.charge_kW( chargePower_kW );

		updateChargingHistory( electricityProduction_kW, electricityConsumption_kW );
		
		//Update the EC flows map
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW - electricityProduction_kW);
		
		//Update the assetFlowsMap
		if (chargePower_kW > 0) { // charging
			assetFlowsMap.put(OL_AssetFlowCategories.evChargingPower_kW, electricityConsumption_kW);
		}
		else if(chargePower_kW < 0){
			if(this.currentChargingSession.getV2GCapable() && this.V2GActive) {
				assetFlowsMap.put(OL_AssetFlowCategories.V2GPower_kW, electricityProduction_kW);
			}
			else {
				throw new RuntimeException("Trying to discharge a chargingsession, that does not have the capability or where v2g is not activated!");
			}
		}
	}
	

	
	public void manageCurrentChargingSession(double t_h) {
		if (this.currentChargingSession != null && t_h >= this.currentChargingSession.getLeaveTime_h()) { // End session
			if (this.currentChargingSession.getRemainingChargeDemand_kWh() > 0.001 ) { traceln("!!Chargesession ended but charge demand not fullfilled!! Remaining demand: %s kWh", this.currentChargingSession.getRemainingChargeDemand_kWh()); }
			this.currentChargingSession = null;
		}
		
		if ( this.currentChargingSession == null ) { // socket currently free
			 // check if we are not already past the last charging session.
			// Find next charging session on this socket
			
			while (this.nextSessionIndex < this.chargingSessionList.size() && this.chargingSessionList.get(nextSessionIndex).getSocketNb() != this.socketNb) {				
				this.nextSessionIndex++;
			}  
			
			if (this.nextSessionIndex >= this.chargingSessionList.size()) { // no more sessions available
				return;					
			} else {					
				loadChargingSession(this.chargingSessionList.get(nextSessionIndex));
				
				if (t_h > this.currentChargingSession.getStartTime_h()) { 
					traceln("Chargesession %s started %s hours too late!", this.nextSessionIndex, t_h - this.currentChargingSession.getStartTime_h());	
					if (t_h >= this.currentChargingSession.getLeaveTime_h()) { 
						traceln("!!Chargesession started after its endTime_h!!");
					}
				}
				this.nextSessionIndex++;
			}
		} 
	}
	
	public void loadChargingSession(J_ChargingSession chargingSession) {
		this.currentChargingSession = chargingSession.getClone();
		if(this.overrideV2GCapability) {
			this.currentChargingSession.overrideV2GCapability(this.overrideV2GCapability, random() < this.V2GCapabilityProbablityOverride);
		}
	}
	
	//Get current ChargingRequest (interface) information
	public double getLeaveTime_h() {
		return this.currentChargingSession.getLeaveTime_h();
	}

	public double getChargingCapacity_kW() {
		return this.currentChargingSession.getChargingCapacity_kW() * this.vehicleScaling;
	}
	
	public double getCurrentSOC_kWh() {
		return this.currentChargingSession.getCurrentSOC_kWh() * this.vehicleScaling;
	}
	
	public double getStorageCapacity_kWh() {
		return this.currentChargingSession.getStorageCapacity_kWh() * this.vehicleScaling;
	}

	public double getChargeDeadline_h() {
		return this.currentChargingSession.getChargeDeadline_h();
	}

    public double getEnergyNeedForNextTrip_kWh() {
    	return this.currentChargingSession.getEnergyNeededForNextTrip_kWh() * this.vehicleScaling;
    }
    
    public double getRemainingChargeDemand_kWh() {
    	return this.currentChargingSession.getRemainingChargeDemand_kWh() * this.vehicleScaling;
    }
    
	public double getRemainingAverageChargingDemand_kW(double t_h) {
		return getLeaveTime_h() > t_h ? getRemainingChargeDemand_kWh() / (getLeaveTime_h() - t_h) : 0;
	}
	
	public double getVehicleScaling_fr() {
		return this.vehicleScaling;
	}
	
	public boolean getV2GCapable() {
		return this.currentChargingSession.getV2GCapable();
	}
	
	//Availability (If chargingsession is currently active)
	public boolean getAvailability(double t_h) {
		return this.currentChargingSession != null ? this.currentChargingSession.getAvailability(t_h) : false;
	}
	
	//V2G activation and capabilities
	public void enableV2GCapabilityOverride(boolean enableV2GCapabilityOverride, Double V2GCapabilityProbablityOverride) {
		this.overrideV2GCapability = enableV2GCapabilityOverride;
		this.V2GCapabilityProbablityOverride = V2GCapabilityProbablityOverride;
		setV2GActive(getV2GActive());
	}
	
	protected void setV2GActive(boolean activateV2G) { // Should only be called by the chargingManagement class or J_EAChargingSession during initialization itself. (No such thing as friend class in java, so only can put on protected).
		this.V2GActive = activateV2G;
		if((!this.overrideV2GCapability || (this.overrideV2GCapability && this.V2GCapabilityProbablityOverride > 0)) && activateV2G) {
			this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
		}
		else {
			this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
		}
	}

	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	
	//Fast forward charging sessions when GC is unpaused to prevent massive peaks for GCs that are trying to catch up
	public void fastForwardCharingSessions(double t_h) {	
		//Clear current charging session
		this.currentChargingSession = null;
		
		//Find next charging session that starts after the current time
		while (this.nextSessionIndex < this.chargingSessionList.size() && (this.chargingSessionList.get(this.nextSessionIndex).getSocketNb() != socketNb || this.chargingSessionList.get(this.nextSessionIndex).getStartTime_h() <= t_h)) {				
			this.nextSessionIndex++;
		}
		
		if (this.nextSessionIndex >= this.chargingSessionList.size()) { // no more sessions available
			return;					
		} else { //Load upcomming charger session and increase next session index					
			loadChargingSession(this.chargingSessionList.get(nextSessionIndex));
			this.nextSessionIndex++;
		}
	}
    
	
	public void updateChargingHistory(double electricityProduced_kW, double electricityConsumed_kW) {
		this.totalDischarged_kWh += electricityProduced_kW * this.timestep_h;
		this.totalCharged_kWh += electricityConsumed_kW * this.timestep_h;
	}
	
	
    public void storeStatesAndReset() {
    	//Store
    	energyUsedStored_kWh = energyUsed_kWh;
    	this.storedTotalCharged_kWh = totalCharged_kWh;
    	this.storedTotalDischarged_kWh = totalDischarged_kWh;	
    	this.storedCurrentChargingSession = this.currentChargingSession;
    	this.storedNextSessionIndex = this.nextSessionIndex;
    	
    	//Reset
    	energyUsed_kWh = 0.0;
    	this.totalCharged_kWh = 0;
    	this.totalDischarged_kWh = 0;	
    	this.currentChargingSession = null;
    	this.nextSessionIndex = 0;
    	clear();    	
    }
    
    public void restoreStates() {
    	energyUsed_kWh = energyUsedStored_kWh;
    	this.totalCharged_kWh = this.storedTotalCharged_kWh;
    	this.totalDischarged_kWh = this.storedTotalDischarged_kWh;	
    	this.currentChargingSession = this.storedCurrentChargingSession;
    	this.nextSessionIndex = this.storedNextSessionIndex;
    }
    
	@Override
	public String toString() {
		return super.toString();
	}

}