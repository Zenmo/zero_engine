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
    private double GCdemandLowPassed_kW = 0.5;
    
    private boolean V2GActive = false;
    /**
     * Default constructor
     */
    public J_ChargingManagementLocalBalancing() {
    	
    }
    
    public J_ChargingManagementLocalBalancing( GridConnection gc ) {
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
    	
    	for (J_EAEV ev : gc.c_electricVehicles) {
    		if (ev.available) {
    			double chargeNeedForNextTrip_kWh = ev.energyNeedForNextTrip_kWh - ev.getCurrentStateOfCharge_kWh(); // Can be negative if recharging is not needed for next trip!
    			double remainingFlexTime_h = ev.getChargeDeadline_h() - t_h; // measure of flexiblity left in current charging session.
    			double avgPowerDemandTillTrip_kW = ev.energyNeedForNextTrip_kWh / (ev.tripTracker.v_idleTimeToNextTrip_min / 60);
    			double chargeSetpoint_kW = 0;    			
    			if ( t_h >= (ev.getChargeDeadline_h()) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
    				//traceln("Urgency charging in GC: %s! May exceed connection capacity!", gc.p_gridConnectionID));
    				chargeSetpoint_kW = ev.getCapacityElectric_kW();	
    			} else {
    				double flexGain_r = 0.5; // how strongly to 'follow' currentBalanceBeforeEV_kW
    				chargeSetpoint_kW = max(0, avgPowerDemandTillTrip_kW + (GCdemandLowPassed_kW - currentBalanceBeforeEV_kW) * (min(1,remainingFlexTime_h*flexGain_r)));			    				
        			if ( ev.getV2GActive() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss flexibility
    					chargeSetpoint_kW = min(0, avgPowerDemandTillTrip_kW - (currentBalanceBeforeEV_kW - GCdemandLowPassed_kW) * (min(1,remainingFlexTime_h*flexGain_r)));
    					//if (chargeSetpoint_kW < 0) {traceln(" V2G Active! Power: " + chargeSetpoint_kW );}
    				}    
    			}
    			ev.f_updateAllFlows( chargeSetpoint_kW / ev.getCapacityElectric_kW() );    		
    		}
    	}
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