/**
 * J_ActivityTracker
 */	

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
	    fieldVisibility = Visibility.ANY,
	    getterVisibility = Visibility.NONE,
	    isGetterVisibility = Visibility.NONE,
	    setterVisibility = Visibility.NONE,
	    creatorVisibility = Visibility.NONE
	)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public abstract class J_ActivityTracker implements I_StoreStatesAndReset {
	protected EnergyModel energyModel;

	// Tripdata
	protected ArrayList<Double> starttimes_min = new ArrayList<>();
	protected ArrayList<Double> endtimes_min = new ArrayList<>();
	//private ArrayList<Double> eventMagnitude = new ArrayList<>();
    public int nbActivities = 0;
	public int v_eventIndex = 0;
	protected int v_eventIndexStored =0;
	
    /**
     * Default constructor
     */
    public J_ActivityTracker() {
    }
    
    public J_ActivityTracker(EnergyModel main, int rowIndex, double time_min) {
    }
    
    public void storeStatesAndReset() {
    	v_eventIndexStored = v_eventIndex;
    	v_eventIndex = 0;
    }
    
    public void restoreStates() {
    	v_eventIndex = v_eventIndexStored;
	}
    

	@Override
	public String toString() {
		return super.toString();
	}
} 