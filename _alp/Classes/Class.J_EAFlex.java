/**
 * J_EAFlex
 */	
abstract public class J_EAFlex extends J_EA {

    /**
     * Default constructor
     */
    public J_EAFlex() {
    }

    public void f_updateAllFlows(double powerFraction_fr, J_TimeVariables timeVariables) {
     	powerFraction_fr = min(1,max(-1, powerFraction_fr));
     	operate(powerFraction_fr, timeVariables);
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);    		
    	}
    
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }
    
	public abstract void operate(double powerFraction_fr, J_TimeVariables timeVariables);

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