/**
 * J_ChargingManagementSimple
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

public class J_ChargingManagementSimple implements I_ChargingManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.SIMPLE;
    private boolean V2GActive = false;

    /**
     * Default constructor
     */
    public J_ChargingManagementSimple( ) {
    
    }
    
    public J_ChargingManagementSimple( GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
      
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	for (I_ChargingRequest chargingRequest : chargePoint.getCurrentActiveChargingRequests()) {
       		double duration_h = chargingRequest.getLeaveTime_h() - timeVariables.getT_h();
    		if (duration_h <= 0) {
   				traceln("ChargingRequest duration negative! leaveTime_h: %s, t_h %s", chargingRequest.getLeaveTime_h(), timeVariables.getT_h());
   				//throw new RuntimeException("ChargingRequest starting after endtime!");
   			}
			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
			//traceln("Charging need: %s, getEnergyNeedForNextTrip_kWh: %s", chargeNeedForNextTrip_kWh, chargingRequest.getEnergyNeedForNextTrip_kWh());
			
    		chargePoint.charge(chargingRequest, chargePoint.getMaxChargingCapacity_kW(chargingRequest), timeVariables, gc);
    	}
    }
    
	public void setV2GActive(boolean activateV2G) {
		if(activateV2G) {
			throw new RuntimeException("Trying to Activate V2G for chargingManagement Simple -> Not supported");
		}
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