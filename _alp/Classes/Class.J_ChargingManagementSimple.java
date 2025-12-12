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
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.SIMPLE;
    private boolean V2GActive = false;
    private J_ChargePoint chargePoint;

    /**
     * Default constructor
     */
    public J_ChargingManagementSimple( GridConnection gc, J_ChargePoint chargePoint ) {
    	this.gc = gc;

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
    	
    	for (I_ChargingRequest chargeRequest : this.chargePoint.getCurrentActiveChargingRequests()) {
    		this.chargePoint.charge(chargeRequest, this.chargePoint.getMaxChargingCapacity_kW(chargeRequest));
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
    
    //Get ChargePoint
    public J_ChargePoint getChargePoint() {
    	return this.chargePoint;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
		//Noting to reset and store
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	
    @Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}