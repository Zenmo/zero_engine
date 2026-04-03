/**
 * J_CurtailManagementContractCapacity
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

public class J_CurtailManagementContractCapacity implements I_CurtailManagement {
	
	GridConnection gc;
	J_TimeParameters timeParameters;
	
    /**
     * Empty constructor for serialization
     */
    public J_CurtailManagementContractCapacity() {
    }

    /**
     * Default constructor
     */
    public J_CurtailManagementContractCapacity(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
	//Manage curtailment
	public void manageCurtailment(J_TimeVariables timeVariables) {
		// Keep feedin power within contract capacity
		if (gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < - gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW) { // overproduction!
			for (J_EAProduction j_ea : gc.c_productionAssets) {
				J_FlowPacket flowPacket = j_ea.curtailEnergyCarrierProduction(OL_EnergyCarriers.ELECTRICITY, - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW);
				gc.f_removeFlows(flowPacket, j_ea);
				if (!(gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < - gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW)) {
					break;
				}
			}
		}
	}
	
    ////Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}