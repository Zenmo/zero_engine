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
    
    private boolean V2GActive = false;
    
    /**
     * Default constructor
     */
    public J_ChargingManagementMaxAvailablePower( ) {
    
    }
    
    public J_ChargingManagementMaxAvailablePower( GridConnection gc) {
    	this.gc = gc;
    }
    
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    
    public void manageCharging(J_ChargePoint chargePoint) {
    	double t_h = gc.energyModel.t_h;

    	double remainingChargingPower_kW = gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(ELECTRICITY);
    	if (gc.p_batteryAsset!=null) {
    		remainingChargingPower_kW += gc.p_batteryAsset.getCapacityAvailable_kW();
    	}
    	ArrayList<I_ChargingRequest> copiedChargingRequestList = new ArrayList<>(chargePoint.getCurrentActiveChargingRequests());
    	//this.chargePoint.getCurrentActiveChargingRequests().forEach(chargingRequest -> {if(ev.getAvailability() && ev.getCurrentStateOfCharge_fr()<1) { copiedVehicleList.add(ev);}}); // only vehicle that are available and not full
    	//int countDeletedItems = 0;

    	// Sort chargingRequests by time until charge deadline
    	copiedChargingRequestList.sort((chargingRequest1, chargingRequest2) -> Double.compare(chargePoint.getChargeDeadline_h(chargingRequest1), chargePoint.getChargeDeadline_h(chargingRequest2)));

    	for ( I_ChargingRequest chargingRequest : copiedChargingRequestList){
			double chargeNeedForNextTrip_kWh = max(0, chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh());
			
			double chargingSetpoint_kW;
			if ( t_h >= chargePoint.getChargeDeadline_h(chargingRequest) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				chargingSetpoint_kW =  chargePoint.getMaxChargingCapacity_kW(chargingRequest);	
			}
			else {
				chargingSetpoint_kW = remainingChargingPower_kW;
			}
			
			double chargingPower_kW = min(max(0,chargingSetpoint_kW), chargePoint.getMaxChargingCapacity_kW(chargingRequest));
			
			//Send the chargepower setpoints to the chargepoint
	       	chargePoint.charge(chargingRequest, chargingPower_kW); 
			remainingChargingPower_kW = max(0, remainingChargingPower_kW - chargingPower_kW); // Assumes the asset complies with the command!   			
    	}
    }
    
	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	
	
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
	//Store and reset states
	public void storeStatesAndReset() {
		
	}
	public void restoreStates() {
		
	}
	
	
	@Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}