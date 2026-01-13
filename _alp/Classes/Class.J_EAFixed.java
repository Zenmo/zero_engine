/**
 * J_EAFixed
 */	
abstract public class J_EAFixed extends J_EA {

    /**
     * Default constructor
     */
    public J_EAFixed() {
    }
    
    public void f_updateAllFlows(J_TimeVariables timeVariables) {
     	operate(timeVariables);
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);    		
    	}
    
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
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