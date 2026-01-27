/**
 * J_BatteryManagementSelfConsumption
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
public class J_BatteryManagementSelfConsumption implements I_BatteryManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;


    /**
     * Default constructor
     */
    public J_BatteryManagementSelfConsumption() {
    	
    }
    
    public J_BatteryManagementSelfConsumption( GridConnection gc, J_TimeParameters timeParameters ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    /**
     * One of the simplest battery algorithms.
     * This algorithm tries to steer the GridConnection load towards 0.
     * If there is overproduction and room in the battery it will charge.
     * If there is more consumption than production it will discharge the battery to make up for the difference untill the battery is empty.
     */
    public void manageBattery(J_TimeVariables timeVariables) {
    	gc.f_updateFlexAssetFlows(gc.p_batteryAsset, -gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) / gc.p_batteryAsset.getCapacityElectric_kW(), timeVariables);
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
		return super.toString();
	}
}