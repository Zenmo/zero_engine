/**
 * J_BatteryManagementSelfConsumption1
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

public class J_BatteryManagementSelfConsumptionGridNode implements I_BatteryManagement {

    private GridConnection gc;

    /**
     * Default constructor
     */
    public J_BatteryManagementSelfConsumptionGridNode() {
    	
    }
    
    public J_BatteryManagementSelfConsumptionGridNode( GridConnection gc ) {
    	this.gc = gc;
    }
    
    /**
     * One of the simplest battery algorithms.
     * This algorithm tries to steer the GridConnection load towards 0.
     * If there is overproduction and room in the battery it will charge.
     * If there is more consumption than production it will discharge the battery to make up for the difference untill the battery is empty.
     */
    public void manageBattery(J_TimeVariables timeVariables) {
    	double nodePreviousLoad_kW = gc.p_parentNodeElectric.v_currentLoad_kW;
    	double chargeSetpoint_kW = -(nodePreviousLoad_kW - gc.p_batteryAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY));
    	
    	gc.p_batteryAsset.f_updateAllFlows( chargeSetpoint_kW / gc.p_batteryAsset.getCapacityElectric_kW(), timeVariables );
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

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}