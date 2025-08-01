/**
 * J_DataSetMap
 */	
//import java.util.EnumMap;
import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_DataSetMap implements Serializable {
	private DataSet[] datasetArray = new DataSet[OL_EnergyCarriers.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	private EnumSet<OL_EnergyCarriers> energyCarrierList = EnumSet.noneOf(OL_EnergyCarriers.class);

    /**
     * Default constructor
     */
    public J_DataSetMap() {
    	//super(OL_EnergyCarriers.class);
    }

    public void createEmptyDataSets(EnumSet<OL_EnergyCarriers> selectedFlows, int size) {
    	for (OL_EnergyCarriers key : selectedFlows) {
    		this.put(key, new DataSet(size));
    	}
    }
    
    public DataSet get(OL_EnergyCarriers key) {
		return datasetArray[key.ordinal()];
	}
    	

	public void put(OL_EnergyCarriers key, DataSet ds) {
		datasetArray[key.ordinal()] = ds;
		energyCarrierList.add(key);		
	}
	
	public void clear() {
		datasetArray = new DataSet[OL_EnergyCarriers.values().length];
		energyCarrierList.clear();
	}
    
	@Override
	public String toString() {
        if (this.energyCarrierList.size() == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (OL_EnergyCarriers key : this.energyCarrierList) {
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