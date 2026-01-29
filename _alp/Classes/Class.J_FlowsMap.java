/**
 * J_FlowsMap
 */	
import zeroPackage.ZeroMath;
import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_FlowsMap implements Serializable {
	
	private double[] valuesArray = new double[OL_EnergyCarriers.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	private EnumSet<OL_EnergyCarriers> energyCarrierList = EnumSet.noneOf(OL_EnergyCarriers.class);
    /**
     * Default constructor
     */
    public J_FlowsMap() {
    	
    }

    public final double get(OL_EnergyCarriers key) {
		return valuesArray[key.ordinal()];
	}
    	

	public final void put(OL_EnergyCarriers key, double value) {
		valuesArray[key.ordinal()] = value;
		energyCarrierList.add(key);
	}
	
	public final void clear() {
		energyCarrierList.clear();
		Arrays.fill(valuesArray, 0.0);
		/*for(int i=0; i<valuesArray.length; i++) {
			valuesArray[i]=0.0;
		}*/
	}
    
    //public J_FlowsMap addFlows( J_FlowsMap f) {
    public final J_FlowsMap addFlowsSlow( J_FlowsMap f) {
    	for (OL_EnergyCarriers key : f.energyCarrierList) {
    		this.addFlow(key, f.get(key));
    	}
    	return this;
    }
    
    //public J_FlowsMap addToExistingFlows( J_FlowsMap f) {
    public final J_FlowsMap addFlows( J_FlowsMap f) {
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			//this.valuesArray[i]=this.valuesArray[i]+f.valuesArray[i];
			this.valuesArray[i]+=f.valuesArray[i];
		}
		this.energyCarrierList.addAll(f.energyCarrierList); 
    	return this;
    }
    
    public final J_FlowsMap removeFlows( J_FlowsMap f) {
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			//this.valuesArray[i]=this.valuesArray[i]+f.valuesArray[i];
			this.valuesArray[i]-=f.valuesArray[i];
		}
		this.energyCarrierList.addAll(f.energyCarrierList); 
    	return this;
    }
    
    public final J_FlowsMap addFlow( OL_EnergyCarriers key, double value) {
    	energyCarrierList.add(key);
    	this.valuesArray[key.ordinal()]+=value;
    	//double currentValue = this.get(key);
    	//this.put(key, currentValue + value);
    	return this;
    }
    
    public final J_FlowsMap cloneMap(J_FlowsMap flowMap) {
    	//this.clear();
    	//this.addFlows(flowMap);
    	
    	// Custom 'addflows' version that doesn't add, just 'copies'. Might be faster?
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			this.valuesArray[i]=flowMap.valuesArray[i];
		}
		
		//this.energyCarrierList = flowMap.energyCarrierList.clone(); // This or first clear list and then addAll? Which is faster?
		this.energyCarrierList.clear();
		this.energyCarrierList.addAll(flowMap.energyCarrierList); 
    	return this;    	
    }
    
    public double totalSum() {
    	return ZeroMath.arraySum(valuesArray);
    }
    
    public final EnumSet<OL_EnergyCarriers> keySet(){
    	return energyCarrierList;
    }
        
    public String toString() {
        if (this.energyCarrierList.size() == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (OL_EnergyCarriers key : this.energyCarrierList) {
        	double value = this.get(key);
            sb.append(key);
            sb.append(" = ");
            sb.append(value);
            sb.append("\n ");
        }
        sb.delete(sb.length()-2, sb.length());
        sb.append('}');
        return sb.toString();
    }

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}