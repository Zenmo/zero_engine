/**
 * J_EAFlex
 */	
abstract public class J_EAFlex extends J_EA {

    /**
     * Default constructor
     */
    public J_EAFlex() {
    }

    public J_FlowPacket f_updateAllFlows(double powerFraction_fr, J_TimeVariables timeVariables) {
     	powerFraction_fr = min(1,max(-1, powerFraction_fr));
     	operate(powerFraction_fr, timeVariables);
     	J_FlowPacket flowPacket = new J_FlowPacket(this.flowsMap, this.energyUse_kW, this.assetFlowsMap);
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	return flowPacket;

    }
    
	public abstract void operate(double powerFraction_fr, J_TimeVariables timeVariables);

	@Override
	public String toString() {
		return super.toString();
	}
}