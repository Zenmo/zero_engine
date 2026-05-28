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

	// Tripdata
	protected ArrayList<Double> startTimes_h = new ArrayList<>();
	protected ArrayList<Double> endTimes_h = new ArrayList<>();
	public int eventIndex = 0;
	protected int eventIndexStored =0;
	
    /**
     * Default constructor
     */
    public J_ActivityTracker() {
    }
    
    public void storeStatesAndReset() {
    	eventIndexStored = eventIndex;
    	eventIndex = 0;
    }
    
    public void restoreStates() {
    	eventIndex = eventIndexStored;
	}
} 