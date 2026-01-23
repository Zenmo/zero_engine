/**
 * J_EAFixed
 */	
abstract public class J_EAFixed extends J_EA {

    /**
     * Default constructor
     */
    public J_EAFixed() {
    }
    
    public J_FlowPacket f_updateAllFlows(J_TimeVariables timeVariables) {
     	operate(timeVariables);
     	J_FlowPacket flowPacket = new J_FlowPacket(this.flowsMap, this.energyUse_kW, this.assetFlowsMap);
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	return flowPacket;
    }
    
	public abstract void operate(J_TimeVariables timeVariables);


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