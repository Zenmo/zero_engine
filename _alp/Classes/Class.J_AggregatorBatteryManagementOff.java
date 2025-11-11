
/**
 * J_AggregatorBatteryManagementOff
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_AggregatorBatteryManagementOff implements I_AggregatorBatteryManagement {

	EnergyCoop energyCoop;
	
    /**
     * Default constructor
     */
	public J_AggregatorBatteryManagementOff( ) {

    }
	
    public J_AggregatorBatteryManagementOff( EnergyCoop energyCoop) {
    	this.energyCoop = energyCoop;
    }
    
    public void manageExternalSetpoints() {
    	//Do nothing
    }
    
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.energyCoop;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store/reset
	}
	public void restoreStates() {
		//Nothing to store/reset
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