/**
 * J_ChargingManagementOffPeak
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
public class J_ChargingManagementOffPeak implements I_ChargingManagement {

    private GridConnection gc;
    private J_ChargePoint chargePoint;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE_GRID;
    private double filterTimeScale_h = 5*24;
    private double filterDiffGain_r;
    private double initialValueGCdemandLowPassed_kW = 0.5;
    private double GCdemandLowPassed_kW = this.initialValueGCdemandLowPassed_kW;
    
    private double startTimeOfReducedChargingInterval_hr = 17; // Hour of the day
    private double endTimeOfReducedChargingInterval_hr = 21; // Hour of the day
    
    private boolean V2GActive = false;
    
    
    //Stored
    private double storedGCdemandLowPassed_kW;
    
    /**
     * Default constructor
     */
    public J_ChargingManagementOffPeak( GridConnection gc, J_ChargePoint chargePoint ) {
    	this.gc = gc;
    	this.filterDiffGain_r = 1/(filterTimeScale_h/gc.energyModel.p_timeStep_h);
    	
    	if(chargePoint == null) {
    		this.chargePoint = new J_ChargePoint(true, true);
    	}
    	else {
    		this.chargePoint = chargePoint;
    	}
    }
   
    
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging() {    	
    	double t_h = gc.energyModel.t_h;
    	this.chargePoint.updateActiveChargingRequests(gc, t_h);
    	
   
    	// Use current GC-load (so without EV charging!) as an 'equivalent price' signal, and use EV battery flexibility to make local load flatter.
    	double currentBalanceBeforeEV_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	GCdemandLowPassed_kW += (currentBalanceBeforeEV_kW - GCdemandLowPassed_kW) * filterDiffGain_r;
    	
		//Determine if time is currently in reduced charging interval
		double hourOfTheDay = t_h % 24;
		
		double intervalLength_hr = (endTimeOfReducedChargingInterval_hr - startTimeOfReducedChargingInterval_hr + 24) % 24;
		if(intervalLength_hr == 0) intervalLength_hr = 24; // (Start time == End time: for now defined as charge as little as possible all day.
		double intervalEndTimeSinceModelStart_hr = t_h - ((t_h - startTimeOfReducedChargingInterval_hr + 24) % 24) + intervalLength_hr;
		boolean timeIsInReducedChargingInterval = ((hourOfTheDay - startTimeOfReducedChargingInterval_hr + 24) % 24) < intervalLength_hr;

       	for (I_ChargingRequest chargingRequest : this.chargePoint.getCurrentActiveChargingRequests()) {
			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
			double chargeSetpoint_kW = 0;    			
			if ( t_h >= this.chargePoint.getChargeDeadline_h(chargingRequest) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				chargeSetpoint_kW = this.chargePoint.getMaxChargingCapacity_kW(chargingRequest);	
			} else {
				if(timeIsInReducedChargingInterval && chargeNeedForNextTrip_kWh > 0) {
    				double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
    				double timeBetweenEndOfIntervalAndNextTripStartTime_hr = max(0, chargingRequest.getLeaveTime_h() - intervalEndTimeSinceModelStart_hr - chargeTimeMargin_h);
    				double energyThatCanBeChargedAfterIntervalEnded_kWh = timeBetweenEndOfIntervalAndNextTripStartTime_hr * this.chargePoint.getMaxChargingCapacity_kW(chargingRequest);
    				double energyThatNeedsToBeChargedDuringInterval_kWh = max(0, chargeNeedForNextTrip_kWh - energyThatCanBeChargedAfterIntervalEnded_kWh);
    		    	
    				double avgPowerDemandTillEndOfInterval_kW = energyThatNeedsToBeChargedDuringInterval_kWh / (intervalEndTimeSinceModelStart_hr - t_h);
    				chargeSetpoint_kW = avgPowerDemandTillEndOfInterval_kW;
				}
				else { // Dom laden
					chargeSetpoint_kW = this.chargePoint.getMaxChargingCapacity_kW(chargingRequest);
				}
			}
	    	//Send the chargepower setpoints to the chargepoint
	       	this.chargePoint.charge(chargingRequest, chargeSetpoint_kW); 
    	}
    	

    }

    public void setStartTimeOfReducedChargingInterval_hr(double startTimeOfReducedChargingInterval_hr) {
    	this.startTimeOfReducedChargingInterval_hr = startTimeOfReducedChargingInterval_hr;
    }
    
    public void setEndTimeOfReducedChargingInterval_hr(double endTimeOfReducedChargingInterval_hr) {
    	this.endTimeOfReducedChargingInterval_hr = endTimeOfReducedChargingInterval_hr;
    }  
    
    public double getStartTimeOfReducedChargingInterval_hr() {
    	return this.startTimeOfReducedChargingInterval_hr;
    }
    
    public double getEndTimeOfReducedChargingInterval_hr() {
    	return this.endTimeOfReducedChargingInterval_hr;
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
    
    //Get ChargePoint
    public J_ChargePoint getChargePoint() {
    	return this.chargePoint;
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