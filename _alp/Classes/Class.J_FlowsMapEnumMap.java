/**
 * J_FlowsMap
 */	
import java.util.EnumMap;

public class J_FlowsMapEnumMap extends EnumMap<OL_EnergyCarriers, Double> {

    /**
     * Default constructor
     */
    public J_FlowsMapEnumMap() {
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
    
    public J_FlowsMap addFlows( J_FlowsMap f) {
    	for (OL_EnergyCarriers key : f.keySet()) {
    		this.addFlow(key, f.get(key));
    	}    	
    	return this;
    }
    
    public J_FlowsMap addFlow ( OL_EnergyCarriers key, double value) {
    	double currentValue = this.get(key);
    	this.put(key, currentValue + value);
    	return this;
    }
    
    public J_FlowsMap cloneMap(J_FlowsMap flowsMap) {
    	//J_FlowsMap flowsMap = new J_FlowsMapEnumMap();
    	this.clear();
    	//this.addFlows(flowsMap);
    	this.putAll(flowsMap);
    	return this;    	
    }
    	
    /*
    public J_FlowsMap addReducedFlows( J_FlowsMap f) {
    	for (OL_EnergyCarriers key : f.keySet()) {
    		this.put(key, this.get(key) + f.get(key));
    	}
    	return this;
    }
    */
    
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

/**
 * J_FlowsMap
 */	

//import java.util.EnumMap;

//public class J_FlowsMap extends EnumMap<OL_FlowsMapKeys, Double> {

    /**
     * Default constructor
     */
 /*   public J_FlowsMapEnumMap() {
    	super(OL_FlowsMapKeys.class);
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
    	for (OL_FlowsMapKeys key : this.keySet()) {
    		this.remove(key);
    	}
    }
    
    public J_FlowsMap addFlows( J_FlowsMap f) {
    	for (OL_FlowsMapKeys key : f.keySet()) {
    		this.put(key, this.get(key) + f.get(key));
    	}
    	return this;
    }
    
    public J_FlowsMap addFlow ( OL_FlowsMapKeys key, double value) {
    	double currentValue = this.get(key);
    	this.put(key, currentValue + value);
    	return this;
    }
    
    public J_FlowsMap addReducedFlows ( J_FlowsMap f) {
    	for (OL_EnergyCarriers energyCarrier : f.keySet()) {
    		if (f.get(energyCarrier) < 0 ) { //production
    			OL_FlowsMapKeys key = translateMapKeys(energyCarrier, false);
        		this.put(key, this.get(key) - f.get(energyCarrier));
    		}
    		else { // consumption
    			OL_FlowsMapKeys key = translateMapKeys(energyCarrier, true);
        		this.put(key, this.get(key) + f.get(energyCarrier));

    		}
    	}
    	return this;
    }
    
    // TODO: test if a hardcoded enummap is faster (probably place it on the energyModel canvas)
    public  OL_FlowsMapKeys translateMapKeys(OL_EnergyCarriers energyCarrier, boolean isConsumption) {
	    if (isConsumption) {
	    	switch (energyCarrier) {
	    		case ELECTRICITY:
	    			return ELECTRICITY_CONSUMPTION_KW;
	    		case HEAT:
	    			return HEAT_CONSUMPTION_KW;
	    		case METHANE:
	    			return METHANE_CONSUMPTION_KW;
	    		case PETROLEUM_FUEL:
	    			return PETROLEUM_FUEL_CONSUMPTION_KW;
	    		case HYDROGEN:
	    			return HYDROGEN_CONSUMPTION_KW;
	    		default:
	    			throw new RuntimeException("Incorrect FlowsMapKey in translateMapKeys");
	    	}
	    }
	    else {
	    	switch (energyCarrier) {
	    		case ELECTRICITY:
	    			return ELECTRICITY_PRODUCTION_KW;
	    		case HEAT:
	    			return HEAT_PRODUCTION_KW;
	    		case METHANE:
	    			return METHANE_PRODUCTION_KW;
	    		case PETROLEUM_FUEL:
	    			return PETROLEUM_FUEL_PRODUCTION_KW;
	    		case HYDROGEN:
	    			return HYDROGEN_PRODUCTION_KW;
	    		default:
	    			throw new RuntimeException("Incorrect FlowsMapKey in translateMapKeys");
	    	}
	    }
    }
    
    public J_FlowsMap getProductionFlows() {
    	J_FlowsMap flowsMap = new J_FlowsMapEnumMap();
    	for (OL_EnergyCarriers energyCarrier : OL_EnergyCarriers.values()) {
    		OL_FlowsMapKeys key = translateMapKeys(energyCarrier, false);
    		flowsMap.put(energyCarrier, this.get(key));
    	}
    	return flowsMap;
    }
    
    public J_FlowsMap getConsumptionFlows() {
    	J_FlowsMap flowsMap = new J_FlowsMapEnumMap();
    	for (OL_EnergyCarriers energyCarrier : OL_EnergyCarriers.values()) {
    		OL_FlowsMapKeys key = translateMapKeys(energyCarrier, true);
    		flowsMap.put(energyCarrier, this.get(key));
    	}
    	return flowsMap;
    }
    
    public double getNetFlow(OL_EnergyCarriers key) {
    	double consumptionFlow = this.get(translateMapKeys(key, true));
    	double productionFlow = this.get(translateMapKeys(key, false));
    	return consumptionFlow - productionFlow;
    }
    
    @Override
	public String toString() {
		return super.toString();
	}
*/
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
//	private static final long serialVersionUID = 1L;

//}