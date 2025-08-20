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
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE;
    private double filterTimeScale_h = 5*24;
    private double filterDiffGain_r;
    private double GCdemandLowPassed_kW = 0.5;

    /**
     * Default constructor
     */
    public J_ChargingManagementLocalBalancing() {
    	
    }
    
    public J_ChargingManagementLocalBalancing( GridConnection gc ) {
    	this.gc = gc;
    	this.filterDiffGain_r = 1/(filterTimeScale_h/gc.energyModel.p_timeStep_h);
    	traceln("Untested functionality in J_ChargingManagementLocalBalancing!!");
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
    	    	traceln("Untested functionality in J_ChargingManagementLocalBalancing!!");
    			double chargeNeedForNextTrip_kWh = ev.energyNeedForNextTrip_kWh - ev.getCurrentStateOfCharge_kWh(); // Can be negative if recharging is not needed for next trip!
    			double remainingFlexTime_h = ev.getChargeDeadline_h() - t_h; // measure of flexiblity left in current charging session.
    			double WTPoffset_eurpkW = 0.01; // Drops willingness to pay price for charging, combined with remainingFlexTime_h.
    			double chargeSetpoint_kW = 0;    			
    			if ( t_h >= (ev.getChargeDeadline_h()) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
    				//traceln("Urgency charging in GC: %s! May exceed connection capacity!", gc.p_gridConnectionID));
    				chargeSetpoint_kW = ev.getCapacityElectric_kW();	
    			} else {
    				double flexGain_r = 0.5; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
    				chargeSetpoint_kW = max(0, ev.getCapacityElectric_kW() * (GCdemandLowPassed_kW / currentBalanceBeforeEV_kW - remainingFlexTime_h * flexGain_r ));			
    				//if ( chargeNeedForNextTrip_kWh < -ev.getCapacityElectric_kW()*gc.energyModel.p_timeStep_h && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
        			if ( ev.getV2GActive() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
    	    			double V2G_WTS_offset_eurpkWh = 0.02; // Price must be at least this amount above the moving average to decide to discharge EV battery.
    					double WTSV2G_eurpkWh = V2G_WTS_offset_eurpkWh + GCdemandLowPassed_kW; // Scale WillingnessToSell based on flexibility expressed in terms of power-fraction
    					chargeSetpoint_kW = min(0, -ev.getCapacityElectric_kW() * (currentBalanceBeforeEV_kW / GCdemandLowPassed_kW - 1) * remainingFlexTime_h * flexGain_r);
    					//if (chargeSetpoint_kW < 0) {traceln(" V2G Active! Power: " + chargeSetpoint_kW );}
    				}    
    			}
    			ev.f_updateAllFlows( chargeSetpoint_kW / ev.getCapacityElectric_kW() );    		
    		}
    	}
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