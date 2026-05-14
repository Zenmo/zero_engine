/**
 * J_CurtailManagementPrice
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

public class J_CurtailManagementPrice implements I_CurtailManagement {
	
	GridConnection gc;
	J_TimeParameters timeParameters;
	double curtailPrice_eurpMWh = 0;
    /**
     * Empty constructor for serialization
     */
    public J_CurtailManagementPrice() {
    }

    /**
     * Default constructor
     */
    public J_CurtailManagementPrice(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
	//Manage curtailment
	public void manageCurtailment(J_TimeVariables timeVariables) {
		if(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() < this.curtailPrice_eurpMWh) {
			if (gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < 0.0) { // Feedin, bring to zero!
				for (J_EAProduction j_ea : gc.c_productionAssets) {
					J_FlowPacket flowPacket = j_ea.curtailEnergyCarrierProduction(OL_EnergyCarriers.ELECTRICITY, - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
					gc.f_removeFlows(flowPacket, j_ea);
					if (!(gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < 0.0)) {
						break;
					}
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