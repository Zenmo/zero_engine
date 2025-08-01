/**
 * J_DataSetMap
 */	
//import java.util.EnumMap;
import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_DataSetMap <E extends Enum<E>> implements Serializable {
	private DataSet[] datasetArray; // = new DataSet[OL_EnergyCarriers.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	//private EnumSet<OL_EnergyCarriers> enumSet = EnumSet.noneOf(OL_EnergyCarriers.class);
	private final EnumSet<E> enumSet;
	private final Class<E> enumClass;
	
    /**
     * Default constructor
     */
    public J_DataSetMap(Class<E> enumClass) {
        this.enumClass = enumClass;
        this.enumSet = EnumSet.noneOf(enumClass);
        this.datasetArray = new DataSet[enumClass.getEnumConstants().length];
    	//super(OL_EnergyCarriers.class);
    }

    public void createEmptyDataSets(EnumSet<E> selectedFlows, int size) {
    	for (E key : selectedFlows) {
    		this.put(key, new DataSet(size));
    	}
    }
    
    public DataSet get(E key) {
		return datasetArray[key.ordinal()];
	}
    	

	public void put(E key, DataSet ds) {
		datasetArray[key.ordinal()] = ds;
		enumSet.add(key);		
	}
	
	public void clear() {
		datasetArray = new DataSet[enumClass.getEnumConstants().length];
		enumSet.clear();
	}
    
	@Override
	public String toString() {
        if (this.enumSet.size() == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (E key : this.enumSet) {
        	DataSet value = this.get(key);
        	int len = value.toString().length();
            sb.append(key);
            sb.append(" = ");
            sb.append(value.toString().substring(0,min(400, len)));
            sb.append(", \n");
        }
        sb.delete(sb.length()-4, sb.length());
        sb.append('}');
        return sb.toString();
    }

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}