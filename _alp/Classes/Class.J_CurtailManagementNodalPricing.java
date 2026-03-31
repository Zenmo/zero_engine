/**
 * J_CurtailManagementNodalPricing
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

public class J_CurtailManagementNodalPricing implements I_CurtailManagement {
	
	GridConnection gc;
	J_TimeParameters timeParameters;
	
    /**
     * Empty constructor for serialization
     */
    public J_CurtailManagementNodalPricing() {
    }

    /**
     * Default constructor
     */
    public J_CurtailManagementNodalPricing(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
	//Manage curtailment
	public void manageCurtailment(J_TimeVariables timeVariables) {
		// Prevent feedin when nodal price is negative
		double priceTreshold_eur = -0.0;
		if( gc.p_parentNodeElectric.v_currentTotalNodalPrice_eurpkWh < priceTreshold_eur) {
		
			double v_currentPowerElectricitySetpoint_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) * max(0,1+(gc.p_parentNodeElectric.v_currentTotalNodalPrice_eurpkWh-priceTreshold_eur)*5);
			for (J_EAProduction j_ea : gc.c_productionAssets) {
				J_FlowPacket flowPacket = j_ea.curtailEnergyCarrierProduction(OL_EnergyCarriers.ELECTRICITY, v_currentPowerElectricitySetpoint_kW - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
				gc.f_removeFlows(flowPacket, j_ea);
				if (!(gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) < v_currentPowerElectricitySetpoint_kW)) {
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