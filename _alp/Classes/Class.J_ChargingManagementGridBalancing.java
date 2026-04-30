/**
 * J_ChargingManagementGridBalancing
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.util.EnumSet;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_ChargingManagementGridBalancing implements I_ChargingManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    private double timeStep_h;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE_GRID;
    private boolean V2GActive = false;

    /**
     * Default constructor
     */
    public J_ChargingManagementGridBalancing( ) {
    }
    
    public J_ChargingManagementGridBalancing( GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.timeStep_h = timeParameters.getTimeStep_h();
    }
      
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	double t_h = timeVariables.getT_h();
    
    	for (I_ChargingRequest chargeRequest : chargePoint.getCurrentActiveChargingRequests()) {
	    	double chargeSetpoint_kW = 0;
			double maxChargePower = chargePoint.getMaxChargingCapacity_kW(chargeRequest);
			double remainingChargeDemand_kWh = chargeRequest.getRemainingChargeDemand_kWh(); // Can be negative if recharging is not needed for next trip!
	
			GridNode parentNode = gc.p_parentNodeElectric;
			double currentBalanceOnGridNodeWithoutEV_kW = parentNode.v_currentLoad_kW - parentNode.f_getCurrentChargingPowerBalancingThisGN_kW();
	    	double gridNodeLowPassedLoad_kW = parentNode.v_lowPassedLoadFilter_kW;
	    	
			double nextTripStartTime_h = chargeRequest.getLeaveTime_h();
			double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
			double chargeDeadline_h =  nextTripStartTime_h - remainingChargeDemand_kWh / maxChargePower - chargeTimeMargin_h;
			double remainingFlexTime_h = chargeDeadline_h - t_h; // measure of flexiblity left in current charging session.
			double remainingTimeToCharge = nextTripStartTime_h - t_h - chargeTimeMargin_h;
			
			if ( t_h >= chargeDeadline_h && remainingChargeDemand_kWh > 0) { // Must-charge time at max charging power
				chargeSetpoint_kW = min(maxChargePower, remainingChargeDemand_kWh / this.timeStep_h);	
			} else {
				double flexGain_r_manual = 0.8;
				double flexGain_r = 1/max(1, (double)parentNode.f_getCurrentNumberOfChargeRequestsBalancingThisGN()) * flexGain_r_manual; // how strongly to 'follow' currentBalanceBeforeEV_kW -> influenced by the amount of charging chargers at this momment
				chargeSetpoint_kW = max(0, chargeRequest.getRemainingAverageChargingDemand_kW(t_h) + (gridNodeLowPassedLoad_kW - currentBalanceOnGridNodeWithoutEV_kW) * (min(1,flexGain_r)));			    				
				if ( this.V2GActive && chargePoint.getV2GCapable() && chargeRequest.getV2GCapable() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss flexibility
					chargeSetpoint_kW = min(0, chargeRequest.getRemainingAverageChargingDemand_kW(t_h) + (gridNodeLowPassedLoad_kW - currentBalanceOnGridNodeWithoutEV_kW) * (min(1,flexGain_r)));
				}    
			}

	    	//Send the chargepower setpoint to the chargepoint
	       	chargePoint.charge(chargeRequest, chargeSetpoint_kW, timeVariables, gc);
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
		
	}
	public void restoreStates() {

	}
	
	
    @Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}