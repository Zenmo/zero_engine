/**
 * J_ChargingManagementMaxSpread
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



public class J_ChargingManagementMaxSpread implements I_ChargingManagement {

    private GridConnection gc;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.BALANCE_GRID;
    private boolean V2GActive = false;

    /**
     * Default constructor
     */
    public J_ChargingManagementMaxSpread( GridConnection gc) {
    	this.gc = gc;
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
    	
    	for (I_ChargingRequest chargeRequest : chargePoint.getCurrentActiveChargingRequests()) {
    		chargePoint.charge(chargeRequest, chargeRequest.getRemainingAverageChargingDemand_kW(t_h));
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
		return "MaxSpreadCharging: Active charging type: " + this.activeChargingType;

	}
}