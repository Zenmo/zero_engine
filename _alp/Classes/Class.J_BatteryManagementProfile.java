/**
 * J_BatteryManagementProfile
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
public class J_BatteryManagementProfile implements I_BatteryManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    
    private J_ProfilePointer batteryProfilePointer_kW;

    /**
     * Empty constructor for serialization purposes
     */
    public J_BatteryManagementProfile() {
    	
    }
    
    /**
     * Default constructor
     */    
    public J_BatteryManagementProfile( GridConnection gc, J_TimeParameters timeParameters ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    /**
     * Default constructor Setting profile pointer aswell
     */    
    public J_BatteryManagementProfile( GridConnection gc, J_TimeParameters timeParameters, J_ProfilePointer batteryProfilePointer_kW ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	setProfilePointer(batteryProfilePointer_kW);
    }
    
    /**
     * This algorithm follows the set profile.
    */
    public void manageBattery(J_TimeVariables timeVariables) {
    	if(gc.p_batteryAsset != null && gc.p_batteryAsset.getStorageCapacity_kWh() > 0 && batteryProfilePointer_kW != null) {
    		gc.f_updateFlexAssetFlows(gc.p_batteryAsset, batteryProfilePointer_kW.getCurrentValue() / gc.p_batteryAsset.getCapacityElectric_kW(), timeVariables);
    	}
    }

    public void setProfilePointer(J_ProfilePointer batteryProfilePointer_kW) {
    	if(batteryProfilePointer_kW.getProfileUnits() == OL_ProfileUnits.KW) {
    		this.batteryProfilePointer_kW = batteryProfilePointer_kW;
    	}
    	else {
    		throw new RuntimeException("Trying to set a profile for J_BatteryManagementProfile with the wrong unit type! ( " + batteryProfilePointer_kW.getProfileUnits() + " ), this is not allowed. The unit type KW is mandatory.");
    	}
    }
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
	//Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		return "J_BatteryManagementProfile with profilePointer: " + batteryProfilePointer_kW;
	}
}