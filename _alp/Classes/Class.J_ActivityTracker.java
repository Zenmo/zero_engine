/**
 * J_ActivityTracker
 */	
public class J_ActivityTracker implements Serializable {
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
    
    public void storeAndResetState() {
    	v_eventIndexStored = v_eventIndex;
    	v_eventIndex = 0;
    }
    
    public void restoreState() {
    	v_eventIndex = v_eventIndexStored;
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