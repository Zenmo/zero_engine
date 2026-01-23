/**
 * J_FlowPacket
 */	
public class J_FlowPacket implements Serializable {

	public J_FlowsMap flowsMap;
	public double energyUse_kW;
	public J_ValueMap assetFlowsMap;
	
    /**
     * Default constructor
     */
    public J_FlowPacket(J_FlowsMap flowsMap, double energyUse_kW, J_ValueMap assetFlowsMap) {
    	this.flowsMap = flowsMap;
    	this.energyUse_kW = energyUse_kW;
    	this.assetFlowsMap= assetFlowsMap;
    }

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