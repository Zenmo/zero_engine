/**
 * J_BatteryManagementExternalSetpoint
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

public class J_BatteryManagementExternalSetpoint implements I_BatteryManagement {

	GridConnection gc;
	private double currentChargeSetpoint_kW = 0;
    /**
     * Default constructor
     */
	public J_BatteryManagementExternalSetpoint( ) {

    }
	
    public J_BatteryManagementExternalSetpoint( GridConnection gc) {
    	this.gc = gc;
    }
    
    public double setChargeSetpoint_kW(double chargeSetpoint_kW) {
    	if (gc.p_batteryAsset!=null) {
    		this.currentChargeSetpoint_kW = max(min(chargeSetpoint_kW, gc.p_batteryAsset.getMaxChargePower_kW()), -gc.p_batteryAsset.getMaxDischargePower_kW());
    	} else {
    		this.currentChargeSetpoint_kW = 0.0;
    	}
    	return this.currentChargeSetpoint_kW;
    }
    
    public double getChargeSetpoint_kW() {
    	return this.currentChargeSetpoint_kW;
    }
    
    public void manageBattery() {
        //Manage the battery with the set charge setpoint
    	gc.p_batteryAsset.f_updateAllFlows(this.currentChargeSetpoint_kW / gc.p_batteryAsset.getCapacityElectric_kW());

    	//Reset the value again.
    	this.currentChargeSetpoint_kW = 0;
    }
    
	@Override
	public String toString() {
		return "J_BatteryManagementExternalSetpoint with currentChargeSetpoint_kW " + this.currentChargeSetpoint_kW + " kW";
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}