/**
 * J_ChargingManagementGridBalancing
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
public class J_ChargingManagementGridBalancing implements I_ChargingManagement {

    private GridConnection gc;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE_GRID;
    private double filterTimeScale_h = 5*24;
    private double filterDiffGain_r;
    private double GCdemandLowPassed_kW = 0.5;
    
    private double startTimeOfReducedChargingInterval_hr = 17; // Hour of the day
    private double endTimeOfReducedChargingInterval_hr = 21; // Hour of the day
    
    private boolean V2GActive = false;

    /**
     * Default constructor
     */
    public J_ChargingManagementGridBalancing() {
    	
    }
    
    public J_ChargingManagementGridBalancing( GridConnection gc ) {
    	this.gc = gc;
    	this.filterDiffGain_r = 1/(filterTimeScale_h/gc.energyModel.p_timeStep_h);
    }
    
    public void initialize() {
    	
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
    	// Use current GC-load (so without EV charging!) as an 'equivalent price' signal, and use EV battery flexibility to make local load flatter.
    	double currentBalanceBeforeEV_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	GCdemandLowPassed_kW += (currentBalanceBeforeEV_kW - GCdemandLowPassed_kW) * filterDiffGain_r;
    	
		//Determine if time is currently in reduced charging interval
		double hourOfTheDay = t_h % 24;
		
		double intervalLength_hr = (endTimeOfReducedChargingInterval_hr - startTimeOfReducedChargingInterval_hr + 24) % 24;
		if(intervalLength_hr == 0) intervalLength_hr = 24; // (Start time == End time: for now defined as charge as little as possible all day.
		double intervalEndTimeSinceModelStart_hr = t_h - ((t_h - startTimeOfReducedChargingInterval_hr + 24) % 24) + intervalLength_hr;
		boolean timeIsInReducedChargingInterval = ((hourOfTheDay - startTimeOfReducedChargingInterval_hr + 24) % 24) < intervalLength_hr;

    	for (J_EAEV ev : gc.c_electricVehicles) {
    		if (ev.available) {
    			double chargeNeedForNextTrip_kWh = ev.energyNeedForNextTrip_kWh - ev.getCurrentStateOfCharge_kWh(); // Can be negative if recharging is not needed for next trip!
    			double chargeSetpoint_kW = 0;    			
    			if ( t_h >= (ev.getChargeDeadline_h()) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
    				//traceln("Urgency charging in GC: %s! May exceed connection capacity!", gc.p_gridConnectionID));
    				chargeSetpoint_kW = ev.getCapacityElectric_kW();	
    			} else {
    				if(timeIsInReducedChargingInterval && chargeNeedForNextTrip_kWh > 0) {
	    				double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
	    				double timeBetweenEndOfIntervalAndNextTripStartTime_hr = max(0, ev.getNextTripStartTime_h() - intervalEndTimeSinceModelStart_hr - chargeTimeMargin_h);
	    				double energyThatCanBeChargedAfterIntervalEnded_kWh = timeBetweenEndOfIntervalAndNextTripStartTime_hr * ev.getCapacityElectric_kW();
	    				double energyThatNeedsToBeChargedDuringInterval_kWh = max(0, chargeNeedForNextTrip_kWh - energyThatCanBeChargedAfterIntervalEnded_kWh);
	    		    	
	    				double avgPowerDemandTillEndOfInterval_kW = energyThatNeedsToBeChargedDuringInterval_kWh / (intervalEndTimeSinceModelStart_hr - t_h);
	    				chargeSetpoint_kW = avgPowerDemandTillEndOfInterval_kW;
    				}
    				else { // Dom laden (??????) // Of max spread laden?
    					chargeSetpoint_kW = ev.getCapacityElectric_kW();
    				}
    			}
    			ev.f_updateAllFlows( chargeSetpoint_kW / ev.getCapacityElectric_kW() );    		
    		}
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
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(false)); // not really wanted but NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	@Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}