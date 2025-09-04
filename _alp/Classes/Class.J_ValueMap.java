/**
 * J_ValueMap
 */	
import zeroPackage.ZeroMath;
import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_ValueMap <E extends Enum<E>> implements Serializable {
	
	//
    private final EnumSet<E> enumSet;
    //private final Class<E> enumClass;
	private double[] valuesArray;// = new double[OL_AssetFlowCategories.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	  
    /**
     * Default constructor
     */
    public J_ValueMap(Class<E> enumClass) {
        //this.enumClass = enumClass;
        this.enumSet = EnumSet.noneOf(enumClass);
        this.valuesArray = new double[enumClass.getEnumConstants().length];
    }
    
    public final double get(E key) {
		return valuesArray[key.ordinal()];
	}
    	

	public final void put(E key, double value) {
		valuesArray[key.ordinal()] = value;
		enumSet.add(key);
	}
	
	public final void clear() {
		enumSet.clear();
		Arrays.fill(valuesArray, 0.0);
		/*for(int i=0; i<valuesArray.length; i++) {
			valuesArray[i]=0.0;
		}*/
	}
      
    //public J_AssetFlowsMap addToExistingFlows( J_AssetFlowsMap f) {
    public final J_ValueMap addFlows( J_ValueMap f) {
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			//this.valuesArray[i]=this.valuesArray[i]+f.valuesArray[i];
			this.valuesArray[i]+=f.valuesArray[i];
		}
		this.enumSet.addAll(f.enumSet); 
    	return this;
    }
    
    public final J_ValueMap addFlow( E key, double value) {
    	enumSet.add(key);
    	this.valuesArray[key.ordinal()]+=value;
    	//double currentValue = this.get(key);
    	//this.put(key, currentValue + value);
    	return this;
    }
    
    public final J_ValueMap cloneMap(J_ValueMap flowMap) {
    	//this.clear();
    	//this.addFlows(flowMap);
    	
    	// Custom 'addflows' version that doesn't add, just 'copies'. Might be faster?
    	int len = valuesArray.length;
		for(int i=0; i<len; i++) {
			this.valuesArray[i]=flowMap.valuesArray[i];
		}
		
		//this.flowCategories = flowMap.flowCategories.clone(); // This or first clear list and then addAll? Which is faster?
		this.enumSet.clear();
		this.enumSet.addAll(flowMap.enumSet); 
    	return this;    	
    }
    
    public double totalSum() {
    	return ZeroMath.arraySum(valuesArray);
    }
    
    public final EnumSet<E> keySet(){
    	return enumSet;
    }
        
    public String toString() {
        if (this.enumSet.size() == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (E key : this.enumSet) {
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