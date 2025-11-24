/**
 * J_ChargingManagementExternalSetpoint
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.util.EnumSet;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_ChargingManagementExternalSetpoint implements I_ChargingManagement {

    private GridConnection gc;
    private OL_ChargingAttitude activeChargingType;
    private Double currentChargeSetpoint_kW = null;

    private boolean V2GActive = false;
    
    //Stored
    private Double storedCurrentChargeSetpoint_kW;
    
    
    /**
     * Default constructor
     */
    public J_ChargingManagementExternalSetpoint() {
    	
    }
    
    public J_ChargingManagementExternalSetpoint( GridConnection gc ) {
    	this.gc = gc;
    	this.activeChargingType = OL_ChargingAttitude.EXTERNAL_SETPOINT;
    }
      
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }

    public Double setChargeSetpoint_kW(double chargeSetpoint_kW) {
    	if (gc.c_electricVehicles.size() > 0) {
    		this.currentChargeSetpoint_kW = chargeSetpoint_kW;
    	} else {
    		this.currentChargeSetpoint_kW = null;
    	}
    	return this.currentChargeSetpoint_kW;
    }
    
    public double getChargeSetpoint_kW() {
    	return this.currentChargeSetpoint_kW;
    }
    
    public void manageCharging() {
    	double t_h = gc.energyModel.t_h;
    	List<J_EAEV> nonUrgentChargingEVs = new ArrayList<>();
    	for (J_EAEV ev : gc.c_electricVehicles) {
    		if (ev.available) {
    			double chargeNeedForNextTrip_kWh = ev.energyNeedForNextTrip_kWh - ev.getCurrentStateOfCharge_kWh(); // Can be negative if recharging is not needed for next trip!
    			if(this.currentChargeSetpoint_kW != null) {
			    	if ( t_h >= (ev.getChargeDeadline_h()) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
						double chargeSetpoint_kW = min(ev.getCapacityElectric_kW(), chargeNeedForNextTrip_kWh/gc.energyModel.p_timeStep_h);
						this.currentChargeSetpoint_kW -= ev.getCapacityElectric_kW();
						ev.f_updateAllFlows( 1 ); 
					}
			    	else {
			    		nonUrgentChargingEVs.add(ev);
			    	}
    			}
    			else {
					ev.f_updateAllFlows( 1 );     				
    			}
    		}
	    }
    	if(nonUrgentChargingEVs.size() > 0) {
	    	double averageRemainingCapacityPerVehicle_kW = max(0, this.currentChargeSetpoint_kW)/nonUrgentChargingEVs.size(); 
	    	for(J_EAEV ev : nonUrgentChargingEVs) {
	    		if(V2GActive) {
	    			ev.f_updateAllFlows( averageRemainingCapacityPerVehicle_kW / ev.getCapacityElectric_kW() ); 
		    	}
		    	else {
		    		ev.f_updateAllFlows( max(0, averageRemainingCapacityPerVehicle_kW) / ev.getCapacityElectric_kW() ); 
		    	}
	    	}
    	}
    	
    	//Reset the value again.
    	this.currentChargeSetpoint_kW = null;
    }
    
	public void setV2GActive(boolean activateV2G) {
		// throw an exception if the management does not support V2G?
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // not really wanted but NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
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
		this.storedCurrentChargeSetpoint_kW = currentChargeSetpoint_kW;
		this.currentChargeSetpoint_kW = null;
	}
	public void restoreStates() {
		this.currentChargeSetpoint_kW = this.storedCurrentChargeSetpoint_kW;
	}
	
	
    @Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}
