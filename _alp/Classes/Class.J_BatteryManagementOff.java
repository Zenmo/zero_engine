/**
 * J_BatteryManagementOff
 */	
public class J_BatteryManagementOff implements I_BatteryManagement {

	GridConnection gc;
	
    /**
     * Default constructor
     */
    public J_BatteryManagementOff( GridConnection gc) {
    	this.gc = gc;
    }

    public void manageBattery() {
    	gc.p_batteryAsset.f_updateAllFlows(0.0);
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