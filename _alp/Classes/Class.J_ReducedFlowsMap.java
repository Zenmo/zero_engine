/**
 * J_ReducedFlowsMap
 */	
import java.util.EnumMap;

public class J_ReducedFlowsMap extends EnumMap<OL_EnergyCarriers, Double> {

    /**
     * Default constructor
     */
    public J_ReducedFlowsMap() {
    	super(OL_EnergyCarriers.class);
    }

    @Override
    public Double get(Object key) {
    	if (super.get(key) == null) {
    		return 0.0;
    	}
    	else {
        	return super.get(key);
    	}
    	
    }
    
    public void clear() {
    	for (OL_EnergyCarriers key : this.keySet()) {
    		this.remove(key);
    	}
    }
    
    public J_ReducedFlowsMap addFlow ( OL_EnergyCarriers key, double value) {
    	double currentValue = this.get(key);
    	this.put(key, currentValue + value);
    	return this;
    }
    
    public J_ReducedFlowsMap addReducedFlows( J_FlowsMap f) {
    	for (OL_EnergyCarriers key : f.keySet()) {
    		this.put(key, this.get(key) + f.get(key));
    	}
    	return this;
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