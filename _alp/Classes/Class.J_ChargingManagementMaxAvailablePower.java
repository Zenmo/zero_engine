/**
 * J_ChargingManagementMaxAvailablePower
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

public class J_ChargingManagementMaxAvailablePower implements I_ChargingManagement {
	private GridConnection gc;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.MAX_POWER;
    /**
     * Default constructor
     */
    public J_ChargingManagementMaxAvailablePower() {
    }
    
    public J_ChargingManagementMaxAvailablePower( GridConnection gc ) {
    	this.gc = gc;
    	
    }

    public void initialize() {
    	
    }
    
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    
    public void manageCharging() {
    	double t_h = gc.energyModel.t_h;
    	double remainingChargingPower_kW = gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(ELECTRICITY);
    	if (gc.p_batteryAsset!=null) {
    		remainingChargingPower_kW += gc.p_batteryAsset.getCapacityAvailable_kW();
    	}
    	ArrayList<J_EAEV> copiedVehicleList = new ArrayList<J_EAEV>();
    	gc.c_electricVehicles.forEach(ev -> {if(ev.getAvailability() && ev.getCurrentStateOfCharge_fr()<1) { copiedVehicleList.add(ev);}}); // only vehicle that are available and not full
    	int countDeletedItems = 0;

    	// Sort vehicles by time until charge deadline
    	copiedVehicleList.sort((ev1, ev2) -> Double.compare(ev1.getChargeDeadline_h(), ev2.getChargeDeadline_h()));

    	for ( J_EAEV ev :  copiedVehicleList){
    		//J_EAEV ev = copiedVehicleList.get(i);
    		if (ev.vehicleScaling != 0) {
				double chargeNeedForNextTrip_kWh = max(0, ev.getEnergyNeedForNextTrip_kWh() - ev.getCurrentStateOfCharge_kWh());
				//traceln("chargeNeedForNextTrip_kWh: " + chargeNeedForNextTrip_kWh);
				
				double chargingSetpoint_kW;
				if ( t_h >= ev.getChargeDeadline_h() && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
					//traceln("Urgency charging! May exceed connection capacity!");
					chargingSetpoint_kW = ev.getCapacityElectric_kW();	
				}
				else {
					chargingSetpoint_kW = remainingChargingPower_kW;
				}
				
				double chargingPower_kW = min(max(0,chargingSetpoint_kW), ev.getCapacityElectric_kW());
				ev.f_updateAllFlows( chargingPower_kW / ev.getCapacityElectric_kW() );
				remainingChargingPower_kW = max(0, remainingChargingPower_kW - chargingPower_kW); // Assumes the asset complies with the command!   			
    		}
    	}
    }
    
	@Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}