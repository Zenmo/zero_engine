/**
 * J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRateGH
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

public class J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRateGH implements I_AggregatorBatteryManagement {

	EnergyCoop energyCoop;
	
    /**
     * Default constructor
     */
	public J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRateGH() {

	}
	
    public J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRateGH( EnergyCoop energyCoop) {
    	this.energyCoop = energyCoop;
    }
    
    public void manageExternalSetpoints() {
    	//Get all members that have a battery that is put on the external setpoint mode
    	List<GridConnection> memberedGCWithSetpointBatteries = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.p_batteryAsset != null && GC.f_getBatteryManagement() != null && GC.f_getBatteryManagement() instanceof J_BatteryManagementExternalSetpoint);
		
		//Determine prefered charge setpoint of the battery, for maximum (collective) selfconsumption (equal to negative or positive balance) and the total delivery and feedin
		double collectiveChargeSetpoint_kW = 0;
		double totalCurrentFeedin_kW = 0;
		double totalCurrentDelivery_kW = 0;
		double sumOfBatteryCapacities_kWh = 0;
		for(GridConnection GC : energyCoop.f_getMemberGridConnectionsCollectionPointer()) {
			double currentBatteryPowerElectric = 0;
			if(memberedGCWithSetpointBatteries.contains(GC)) {
				currentBatteryPowerElectric = GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.batteriesChargingPower_kW) - GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.batteriesDischargingPower_kW);
				
				//Get total active usable battery capacity
				sumOfBatteryCapacities_kWh += (GC.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh*1000);
			}
			collectiveChargeSetpoint_kW -= GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - currentBatteryPowerElectric;
			totalCurrentFeedin_kW+=max(0,-(GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - currentBatteryPowerElectric));
			totalCurrentDelivery_kW+=max(0,(GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - currentBatteryPowerElectric));
		}
	
		// For all qualifying GCs, make balanceflow zero if possible. Also, distinguish between collective feedin and collective delivery
		double remainingSumOfChargeSetpoints_kW = collectiveChargeSetpoint_kW;

		// Divide setpoint proportional to feedin power per GC
		for (GridConnection GC : memberedGCWithSetpointBatteries) {
			double GC_Setpoint_kW=0;
			if (collectiveChargeSetpoint_kW > 0) {
				GC_Setpoint_kW = collectiveChargeSetpoint_kW * max(0,-GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY))/totalCurrentFeedin_kW; // Divide summed charge-power proportional to feedin power on each GC.
			} else if (collectiveChargeSetpoint_kW < 0){
				GC_Setpoint_kW = collectiveChargeSetpoint_kW * max(0,GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY))/totalCurrentDelivery_kW; // Divide summed charge-power proportional to delivery power on each GC.
			}
			remainingSumOfChargeSetpoints_kW -= ((J_BatteryManagementExternalSetpoint)GC.f_getBatteryManagement()).setChargeSetpoint_kW(GC_Setpoint_kW);
		}
		
		// If some of the batteries are full, try distribute remaining charge setpoint over all batteries, proportional to batterysize
		if (Math.abs(remainingSumOfChargeSetpoints_kW) > 0.01) {
			//traceln("Dividing positive charge power proportional to battery size");
			double chargeSetpointToBeDevided_kW = remainingSumOfChargeSetpoints_kW;
			for (GridConnection GC : memberedGCWithSetpointBatteries) {
				double GC_addedSetpoint_kW = chargeSetpointToBeDevided_kW * (  GC.p_batteryAsset.getStorageCapacity_kWh() / sumOfBatteryCapacities_kWh); // Divide summed charge-power proportional to battery size on each GC.
				double GC_currentSetpoint_kW = ((J_BatteryManagementExternalSetpoint)GC.f_getBatteryManagement()).getChargeSetpoint_kW();
				remainingSumOfChargeSetpoints_kW -= (((J_BatteryManagementExternalSetpoint)GC.f_getBatteryManagement()).setChargeSetpoint_kW(GC_addedSetpoint_kW+GC_currentSetpoint_kW)-GC_currentSetpoint_kW);
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
