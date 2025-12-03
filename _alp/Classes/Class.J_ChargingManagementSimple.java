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

    /**
     * Default constructor
     */
    public J_ChargingManagementSimple() {
    	
    }
    
    public J_ChargingManagementSimple( GridConnection gc ) {
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
    	for (J_EAEV ev : gc.c_electricVehicles) {
    		if (ev.available) {			
	    		// just charge 'dumb', full power until full
	    		ev.f_updateAllFlows(1.0);
    		}
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
		//Noting to reset and store
	}
	public void restoreStates() {
		//Nothing to restore
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