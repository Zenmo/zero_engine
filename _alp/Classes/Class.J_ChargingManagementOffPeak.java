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
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE_GRID;

    private Double startTimeOfReducedChargingInterval_hr = null;//17; // Hour of the day
    private Double endTimeOfReducedChargingInterval_hr = null;// Hour of the day
    
    private boolean V2GActive = false;
    
    
    /**
     * Default constructor
     */
    public J_ChargingManagementOffPeak() {
    	
    }
    
    public J_ChargingManagementOffPeak( GridConnection gc ) {
    	this.gc = gc;
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

		//Determine if time is currently in reduced charging interval
		double hourOfTheDay = t_h % 24;
		boolean timeIsInReducedChargingInterval = false;
		Double intervalEndTimeSinceModelStart_hr = null;
		if(this.startTimeOfReducedChargingInterval_hr  != null && this.endTimeOfReducedChargingInterval_hr  != null ) {
			double intervalLength_hr = (endTimeOfReducedChargingInterval_hr - startTimeOfReducedChargingInterval_hr + 24) % 24;
			if(intervalLength_hr == 0) intervalLength_hr = 24; // (Start time == End time: for now defined as charge as little as possible all day.
			intervalEndTimeSinceModelStart_hr = t_h - ((t_h - startTimeOfReducedChargingInterval_hr + 24) % 24) + intervalLength_hr;
			timeIsInReducedChargingInterval = ((hourOfTheDay - startTimeOfReducedChargingInterval_hr + 24) % 24) < intervalLength_hr;
		}
		
    	for (J_EAEV ev : gc.c_electricVehicles) {
    		if (ev.available) {
    			double chargeNeedForNextTrip_kWh = ev.energyNeedForNextTrip_kWh - ev.getCurrentStateOfCharge_kWh(); // Can be negative if recharging is not needed for next trip!
    			double chargeSetpoint_kW = 0;    			
    			if ( t_h >= (ev.getChargeDeadline_h()) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
    				//traceln("Urgency charging in GC: %s! May exceed connection capacity!", gc.p_gridConnectionID));
    				chargeSetpoint_kW = ev.getCapacityElectric_kW();
    			} else {
    				if(timeIsInReducedChargingInterval) {
    					if(chargeNeedForNextTrip_kWh > 0) {
		    				double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
		    				double timeBetweenEndOfIntervalAndNextTripStartTime_hr = max(0, ev.getNextTripStartTime_h() - intervalEndTimeSinceModelStart_hr - chargeTimeMargin_h);
		    				double energyThatCanBeChargedAfterIntervalEnded_kWh = timeBetweenEndOfIntervalAndNextTripStartTime_hr * ev.getCapacityElectric_kW();
		    				double energyThatNeedsToBeChargedDuringInterval_kWh = max(0, chargeNeedForNextTrip_kWh - energyThatCanBeChargedAfterIntervalEnded_kWh);
		    		    	
		    				double avgPowerDemandTillEndOfInterval_kW = energyThatNeedsToBeChargedDuringInterval_kWh / (intervalEndTimeSinceModelStart_hr - t_h);
		    				chargeSetpoint_kW = avgPowerDemandTillEndOfInterval_kW;
    					}
    					else {
    						chargeSetpoint_kW = 0; 
    					}
    				}
    				else { // Dom laden
    					chargeSetpoint_kW = ev.getCapacityElectric_kW();
    				}
    			}
    			ev.f_updateAllFlows( chargeSetpoint_kW / ev.getCapacityElectric_kW() );    		
    		}
    	}
    }
    
    public void setReducedChargingIntervalTime_hr(Double startTimeOfReducedChargingInterval_hr, Double endTimeOfReducedChargingInterval_hr) {
    	this.setStartTimeOfReducedChargingInterval_hr(startTimeOfReducedChargingInterval_hr);
    	this.setEndTimeOfReducedChargingInterval_hr(endTimeOfReducedChargingInterval_hr);
    }
    
    public void setStartTimeOfReducedChargingInterval_hr(Double startTimeOfReducedChargingInterval_hr) {
    	this.startTimeOfReducedChargingInterval_hr = startTimeOfReducedChargingInterval_hr;
    }
    
    public void setEndTimeOfReducedChargingInterval_hr(Double endTimeOfReducedChargingInterval_hr) {
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
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // not really wanted but NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
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