/**
 * J_BatteryManagementSelfConsumption
 */	
public class J_BatteryManagementSelfConsumption implements I_BatteryManagement {

    private GridConnection gc;

    /**
     * Default constructor
     */
    public J_BatteryManagementSelfConsumption( GridConnection gc ) {
    	this.gc = gc;
    }
    
    /**
     * One of the simplest battery algorithms.
     * This algorithm tries to steer the GridConnection load towards 0.
     * If there is overproduction and room in the battery it will charge.
     * If there is more consumption than production it will discharge the battery to make up for the difference untill the battery is empty.
     */
    public void manageBattery() {
    	gc.p_batteryAsset.f_updateAllFlows( -gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) / gc.p_batteryAsset.getCapacityElectric_kW() );
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