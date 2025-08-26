/**
 * J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRate
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

public class J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRate implements I_AggregatorBatteryManagement {

	EnergyCoop energyCoop;
	
    /**
     * Default constructor
     */
	public J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRate() {

	}
	
    public J_AggregatorBatteryManagementCollectiveSelfConsumption_exportRate( EnergyCoop energyCoop) {
    	this.energyCoop = energyCoop;
    }
    
    public void manageExternalSetpoints() {
    	//Get all members that have a battery that is put on the external setpoint mode
    	List<GridConnection> memberedGCWithSetpointBatteries = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC instanceof GCUtility && GC.p_batteryAsset != null && GC.p_batteryAlgorithm != null && GC.p_batteryAlgorithm instanceof J_BatteryManagementExternalSetpoint);

		//Determine prefered charge setpoint of the battery, for maximum (collective) selfconsumption (equal to negative or positive balance)
		double collectiveChargeSetpoint_kW = 0;
		for(GridConnection GC : energyCoop.f_getMemberGridConnectionsCollectionPointer()) {
			if (GC instanceof GCUtility) {
				collectiveChargeSetpoint_kW -= GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
			}
		}

		//Initialize iterable gc list
		List<GridConnection> memberedGCWithSetpointBatteries_withFreeCapacity = new ArrayList<GridConnection>();

		//Only add the gc that have the same direction as net coop charge flow to the gc that will use their battery
		for(GridConnection GC : memberedGCWithSetpointBatteries){
			double gc_balanceFlow = GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
			if((collectiveChargeSetpoint_kW < 0 && -gc_balanceFlow < 0) || (collectiveChargeSetpoint_kW > 0 && -gc_balanceFlow > 0)){
				memberedGCWithSetpointBatteries_withFreeCapacity.add(GC);
			}
		}

		//Initialize variables
		double remainingSumOfChargeSetpoints_kW = collectiveChargeSetpoint_kW;
		double gc_balanceFlowElectricity_kW;
		double gc_calculatedChargeSetpoint_kW;
		double gc_actualChargeSetpoint_kW;
		List<GridConnection> memberedGCWithSetpointBatteries_noCapacity = new ArrayList<GridConnection>();

		//Loop until no gc have battery capacity left or the setpoint is reached
		while( Math.abs(remainingSumOfChargeSetpoints_kW) > 0.0001 && memberedGCWithSetpointBatteries_withFreeCapacity.size() > 0){
			memberedGCWithSetpointBatteries_noCapacity.clear();
			double removedSumOfChargedSetpoints_kW = 0;
			double totalBalanceFlowRemainingGC_kW = 0;
			
			//Calculate the new combined balance flow, to use for distributing the remaining chargeSetpoint
			for(GridConnection GC : memberedGCWithSetpointBatteries_withFreeCapacity){
				totalBalanceFlowRemainingGC_kW += GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
			}
			if (Math.abs(totalBalanceFlowRemainingGC_kW) < 0.0001) {
		   		traceln("Warning: totalBalanceFlowRemainingGC is zero, cant distribute any further, nr GC left: " + memberedGCWithSetpointBatteries_withFreeCapacity.size());
		    	break;
			}
			
			//Iterate over the gc that still have space left in their charge capacity or battery storage
			for(GridConnection GC : memberedGCWithSetpointBatteries_withFreeCapacity){
				J_BatteryManagementExternalSetpoint gridConnectionBatteryAlgorithm = (J_BatteryManagementExternalSetpoint)GC.p_batteryAlgorithm;
				double currentBatteryPowerElectric = GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.batteriesChargingPower_kW) - GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.batteriesDischargingPower_kW);
				
				gc_balanceFlowElectricity_kW = GC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - currentBatteryPowerElectric;
				gc_calculatedChargeSetpoint_kW = gridConnectionBatteryAlgorithm.getChargeSetpoint_kW() + remainingSumOfChargeSetpoints_kW * ( gc_balanceFlowElectricity_kW / totalBalanceFlowRemainingGC_kW);
				
				//First remove setpoint from previous iteration again
				removedSumOfChargedSetpoints_kW -= gridConnectionBatteryAlgorithm.getChargeSetpoint_kW();
				
				//Set and get new setpoint
				gc_actualChargeSetpoint_kW = gridConnectionBatteryAlgorithm.setChargeSetpoint_kW(gc_calculatedChargeSetpoint_kW);
				
				//Add new setpoint to removed setpiont total for this iteration again (netto this is increased by the additional amount compared to last iteration)
				removedSumOfChargedSetpoints_kW += gc_actualChargeSetpoint_kW;
				
				//If the actual setpoint that has been returned by the gc is not equal to the required setpoint, it means the gc cant charge/dis charge any further, so remove it from the pool 
				if(gc_calculatedChargeSetpoint_kW != gc_actualChargeSetpoint_kW){
					memberedGCWithSetpointBatteries_noCapacity.add(GC);
				}
			}
			//Update the remaining pool of GC and to be distributed setpoint_kW
			memberedGCWithSetpointBatteries_withFreeCapacity.removeAll(memberedGCWithSetpointBatteries_noCapacity);
			remainingSumOfChargeSetpoints_kW -= removedSumOfChargedSetpoints_kW;
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

