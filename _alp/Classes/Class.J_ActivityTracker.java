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
	public int eventIndex = 0;
	protected int eventIndexStored =0;
	
    /**
     * Default constructor
     */
    public J_ActivityTracker() {
    }
    
    public J_ActivityTracker(EnergyModel main, int rowIndex, double time_min) {
    }
    
    public void storeStatesAndReset() {
    	eventIndexStored = eventIndex;
    	eventIndex = 0;
    }
    
    public void restoreStates() {
    	eventIndex = eventIndexStored;
	}
    

	@Override
	public String toString() {
		return super.toString();
	}
} 