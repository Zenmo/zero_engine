/**
 * J_AggregatorBatteryManagementCollectiveSelfConsumption_batterySize
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

public class J_AggregatorBatteryManagementCollectiveSelfConsumption_batterySize implements I_AggregatorBatteryManagement {

	EnergyCoop energyCoop;
	
    /**
     * Default constructor
     */
	public J_AggregatorBatteryManagementCollectiveSelfConsumption_batterySize( ) {

    }
	
    public J_AggregatorBatteryManagementCollectiveSelfConsumption_batterySize( EnergyCoop energyCoop) {
    	this.energyCoop = energyCoop;
    }
    
    public void manageExternalSetpoints() {
    	//Get all members that have a battery that is put on the external setpoint mode
    	List<GridConnection> memberedGCWithSetpointBatteries = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC instanceof GCUtility && GC.p_batteryAsset != null && GC.p_batteryAlgorithm != null && GC.p_batteryAlgorithm instanceof J_BatteryManagementExternalSetpoint);

		double sumOfBatteryCapacities_kWh = energyCoop.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh*1000;
		double collectiveChargeSetpoint_kW = 0;
		for(GridConnection GC : memberedGCWithSetpointBatteries) {
			if (GC instanceof GCUtility) {
				collectiveChargeSetpoint_kW -= GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
			}
		}
		// Generate setpoints and 'push' to memberGridConnections for next timestep
		for(int i = 0; i<memberedGCWithSetpointBatteries.size(); i++) {
			GridConnection GC = memberedGCWithSetpointBatteries.get(i);
			if (GC.p_batteryAsset != null) {		
				((J_BatteryManagementExternalSetpoint)GC.p_batteryAlgorithm).setChargeSetpoint_kW(collectiveChargeSetpoint_kW * (  GC.p_batteryAsset.getStorageCapacity_kWh() / sumOfBatteryCapacities_kWh)); // Divide summed charge-power proportional to battery size on each GC.
			}
		}
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