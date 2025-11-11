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
    	this.energyCoop.energyModel.f_registerAssetManagement(this);
    }
    
    public void manageExternalSetpoints() {
    	//Get all members that have a battery that is put on the external setpoint mode
    	List<GridConnection> memberedGCWithSetpointBatteries = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.p_batteryAsset != null && GC.p_batteryAlgorithm != null && GC.p_batteryAlgorithm instanceof J_BatteryManagementExternalSetpoint);

		double collectiveChargeSetpoint_kW = 0;
		double sumOfBatteryCapacities_kWh = 0;
		for(GridConnection GC : energyCoop.f_getMemberGridConnectionsCollectionPointer()) {
			double currentBatteryPowerElectric = 0;
			if(memberedGCWithSetpointBatteries.contains(GC)) {
				currentBatteryPowerElectric = GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.batteriesChargingPower_kW) - GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.batteriesDischargingPower_kW);			
				
				//Get total active usable battery capacity
				sumOfBatteryCapacities_kWh += (GC.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh*1000);
			}
			collectiveChargeSetpoint_kW -= (GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - currentBatteryPowerElectric);
		}
		
		// Generate setpoints and 'push' to memberGridConnections for next timestep
		for(int i = 0; i<memberedGCWithSetpointBatteries.size(); i++) {
			GridConnection GC = memberedGCWithSetpointBatteries.get(i);
			if (GC.p_batteryAsset != null) {		
				((J_BatteryManagementExternalSetpoint)GC.p_batteryAlgorithm).setChargeSetpoint_kW(collectiveChargeSetpoint_kW * (  GC.p_batteryAsset.getStorageCapacity_kWh() / sumOfBatteryCapacities_kWh)); // Divide summed charge-power proportional to battery size on each GC.
			}
		}
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