/**
 * J_CurtailManagementExternalSetpoint
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

public class J_CurtailManagementExternalSetpoint implements I_CurtailManagement {
	
	GridConnection gc;
	J_TimeParameters timeParameters;
	double curtailmentSetpoint_kW;
    /**
     * Empty constructor for serialization
     */
    public J_CurtailManagementExternalSetpoint() {
    }

    /**
     * Default constructor
     */
    public J_CurtailManagementExternalSetpoint(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
	//Manage curtailment
	public void manageCurtailment(J_TimeVariables timeVariables) {
		//Follow external curtailment setpoint
		if(curtailmentSetpoint_kW > 0) {
			for (J_EAProduction j_ea : gc.c_productionAssets) {
				J_FlowPacket flowPacket = j_ea.curtailEnergyCarrierProduction(OL_EnergyCarriers.ELECTRICITY, curtailmentSetpoint_kW);
				gc.f_removeFlows(flowPacket, j_ea);
				
				curtailmentSetpoint_kW -= flowPacket.energyUse_kW; // Remove curtailed energy use from curtailment setpoint.
				if (curtailmentSetpoint_kW <= 0) {
					break;
				}
			}			
		}
		if(curtailmentSetpoint_kW > 0) {
			traceln("WARNING: External curtailment setpoint has been set too high! The full requested curtailment could not be executed, as the curtailable energy was lower");
		}
		
		//Reset curtailment setpoint_kW
		curtailmentSetpoint_kW = 0;
	}
	
	public void setCurtailmentSetpoint(double curtailmentSetpoint_kW) {
		this.curtailmentSetpoint_kW = curtailmentSetpoint_kW;
	}
	
    ////Store and reset states
	public void storeStatesAndReset() {
		curtailmentSetpoint_kW = 0;
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		return "J_CurtailManagementExternalSetpoint: Current setpoint_kW = " + curtailmentSetpoint_kW;
	}
}