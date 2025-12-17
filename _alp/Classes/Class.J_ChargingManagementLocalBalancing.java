/**
 * J_ChargingManagementLocalBalancing
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_ChargingManagementLocalBalancing implements I_ChargingManagement {

    private GridConnection gc;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE_LOCAL;
    private double filterTimeScale_h = 5*24;
    private double filterDiffGain_r;
    private double initialValueGCdemandLowPassed_kW = 0.5;
    private double GCdemandLowPassed_kW = this.initialValueGCdemandLowPassed_kW;
    
    private boolean V2GActive = false;
    
    //Stored
    private double storedGCdemandLowPassed_kW;
    
    /**
     * Default constructor
     */
    public J_ChargingManagementLocalBalancing( GridConnection gc) {
    	this.gc = gc;
    	this.filterDiffGain_r = 1/(filterTimeScale_h/gc.energyModel.p_timeStep_h);
    }
    
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint) {    	
    	double t_h = gc.energyModel.t_h;

    	// Use current GC-load (so without EV charging!) as an 'equivalent price' signal, and use EV battery flexibility to make local load flatter.
    	double currentBalanceBeforeEV_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	GCdemandLowPassed_kW += (currentBalanceBeforeEV_kW - GCdemandLowPassed_kW) * filterDiffGain_r;
    	
       	for (I_ChargingRequest chargingRequest : chargePoint.getCurrentActiveChargingRequests()) {
			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
			double remainingFlexTime_h = chargePoint.getChargeDeadline_h(chargingRequest) - t_h; // measure of flexiblity left in current charging session.
			double avgPowerDemandTillTrip_kW = chargingRequest.getEnergyNeedForNextTrip_kWh() / (chargingRequest.getLeaveTime_h() - t_h);
			double chargeSetpoint_kW = 0;    			
			if ( t_h >= chargePoint.getChargeDeadline_h(chargingRequest) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				chargeSetpoint_kW = chargePoint.getMaxChargingCapacity_kW(chargingRequest);	
			} else {
				double flexGain_r = 0.5; // how strongly to 'follow' currentBalanceBeforeEV_kW
				chargeSetpoint_kW = max(0, avgPowerDemandTillTrip_kW + (GCdemandLowPassed_kW - currentBalanceBeforeEV_kW) * (min(1,remainingFlexTime_h*flexGain_r)));			    				
    			if ( this.V2GActive && chargePoint.getV2GCapable() && chargingRequest.getV2GCapable() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss flexibility
					chargeSetpoint_kW = min(0, avgPowerDemandTillTrip_kW - (currentBalanceBeforeEV_kW - GCdemandLowPassed_kW) * (min(1,remainingFlexTime_h*flexGain_r)));
				}    
			}
	    	//Send the chargepower setpoint to the chargepoint			
			chargePoint.charge(chargingRequest, chargeSetpoint_kW);  		
    	}
    }

	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
	
    //Store and reset states
	public void storeStatesAndReset() {
		this.storedGCdemandLowPassed_kW = this.GCdemandLowPassed_kW;
		this.GCdemandLowPassed_kW = this.initialValueGCdemandLowPassed_kW;
	}
	public void restoreStates() {
		this.GCdemandLowPassed_kW = this.storedGCdemandLowPassed_kW;
	}
	
	@Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}