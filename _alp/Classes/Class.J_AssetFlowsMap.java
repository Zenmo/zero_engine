	/**
 * J_AssetFlowsMap
 */	

import zeroPackage.ZeroMath;
import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_AssetFlowsMap implements Serializable {

	private double[] valuesArray = new double[OL_AssetFlowCategories.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	private EnumSet<OL_AssetFlowCategories> flowCategories = EnumSet.noneOf(OL_AssetFlowCategories.class);
  
    /**
     * Default constructor
     */
    public J_AssetFlowsMap() {
    }
    
    public final double get(OL_AssetFlowCategories key) {
		return valuesArray[key.ordinal()];
	}
    	

	public final void put(OL_AssetFlowCategories key, double value) {
		valuesArray[key.ordinal()] = value;
		flowCategories.add(key);
	}
	
	public final void clear() {
		flowCategories.clear();
		Arrays.fill(valuesArray, 0.0);
		/*for(int i=0; i<valuesArray.length; i++) {
			valuesArray[i]=0.0;
		}*/
	}
    
    //public J_AssetFlowsMap addFlows( J_AssetFlowsMap f) {
    public final J_AssetFlowsMap addFlowsSlow( J_AssetFlowsMap f) {
    	for (OL_AssetFlowCategories key : f.flowCategories) {
    		this.addFlow(key, f.get(key));
    	}
    	return this;
    }
    
    //public J_AssetFlowsMap addToExistingFlows( J_AssetFlowsMap f) {
    public final J_AssetFlowsMap addFlows( J_AssetFlowsMap f) {
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			//this.valuesArray[i]=this.valuesArray[i]+f.valuesArray[i];
			this.valuesArray[i]+=f.valuesArray[i];
		}
		this.flowCategories.addAll(f.flowCategories); 
    	return this;
    }
    
    public final J_AssetFlowsMap addFlow( OL_AssetFlowCategories key, double value) {
    	flowCategories.add(key);
    	this.valuesArray[key.ordinal()]+=value;
    	//double currentValue = this.get(key);
    	//this.put(key, currentValue + value);
    	return this;
    }
    
    public final J_AssetFlowsMap cloneMap(J_AssetFlowsMap flowMap) {
    	//this.clear();
    	//this.addFlows(flowMap);
    	
    	// Custom 'addflows' version that doesn't add, just 'copies'. Might be faster?
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			this.valuesArray[i]=flowMap.valuesArray[i];
		}
		
		//this.flowCategories = flowMap.flowCategories.clone(); // This or first clear list and then addAll? Which is faster?
		this.flowCategories.clear();
		this.flowCategories.addAll(flowMap.flowCategories); 
    	return this;    	
    }
    
    public double totalSum() {
    	return ZeroMath.arraySum(valuesArray);
    }
    
    public final EnumSet<OL_AssetFlowCategories> keySet(){
    	return flowCategories;
    }
        
    public String toString() {
        if (this.flowCategories.size() == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (OL_AssetFlowCategories key : this.flowCategories) {
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