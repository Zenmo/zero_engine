/**
 * J_ActivityTrackerCooking
 */	
public class J_ActivityTrackerCooking extends zero_engine.J_ActivityTracker {
	private ArrayList<Double> powerFractions_fr = new ArrayList<>();
	private J_EAConversionHob hob;
	private double powerFraction_fr=0;
	private boolean cooking = false;
	private J_TimeParameters timeParameters;
	private Integer CSVRowIndex = null;
	private ArrayList<Double> initalStartTimes_h;
	private ArrayList<Double> initalEndTimes_h;
	private ArrayList<Double> storedStartTimes_h;
	private ArrayList<Double> storedEndTimes_h;
	
	protected ArrayList<Double> startTimes_h = new ArrayList<>();
	protected ArrayList<Double> endTimes_h = new ArrayList<>();
	
	/**
     * Default constructor
     */
    public J_ActivityTrackerCooking() {
    }

    public J_ActivityTrackerCooking(TextFile cookingActivitiesCSV, int CSVRowIndex, J_EAConversionHob hob, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
    	this.CSVRowIndex = CSVRowIndex;
    	this.hob=hob;
    	double time_h = timeVariables.getAnyLogicTime_h();
    	this.timeParameters = timeParameters;
    	
    	cookingActivitiesCSV.close();
    	cookingActivitiesCSV.canReadMore();
    	cookingActivitiesCSV.readLine(); // Skips first line
    	
    	while (roundToInt(cookingActivitiesCSV.readDouble())!=CSVRowIndex && cookingActivitiesCSV.canReadMore()) { // Skip until rowIndex found
    		cookingActivitiesCSV.readLine(); 
    	}
    	int currentLineNb = cookingActivitiesCSV.getLineNumber();
    	int nbActivities = cookingActivitiesCSV.readInt();  	
   	
    	for (int i = 0; i < nbActivities; i++){
    		startTimes_h.add(cookingActivitiesCSV.readDouble()/60.0); // Convert data (min) to hours
    		endTimes_h.add(cookingActivitiesCSV.readDouble()/60.0); // Convert data (min) to hours
    		powerFractions_fr.add(cookingActivitiesCSV.readDouble() / hob.getOutputCapacity_kW());
    	}
    	
    	while ( startTimes_h.get(eventIndex) - time_h < 0) {
    		startTimes_h.set( eventIndex, startTimes_h.get(eventIndex) + 24 );  // Source data is always just one day, repeating every day.
    		endTimes_h.set( eventIndex, endTimes_h.get(eventIndex) + 24 ); // Source data is always just one day, repeating every day.
    		eventIndex++;
    		if ( eventIndex > startTimes_h.size() - 1 ) {
    			eventIndex = 0;
    		}
    	}
    	
    	initalStartTimes_h = new ArrayList<>(startTimes_h);
    	initalEndTimes_h = new ArrayList<>(endTimes_h);
    }
    
    public void manageActivities(J_TimeVariables timeVariables) {
    	double time_h = timeVariables.getAnyLogicTime_h();

    	if (cooking) {
	    	if (time_h >= endTimes_h.get(eventIndex) ) { // end cooking session. Also check if a new one starts in this timestep!
				// factor to compensate for the fact that you might not be cooking for the entire timestep.
				double fr = (time_h - this.endTimes_h.get(this.eventIndex)) / this.timeParameters.getTimeStep_h();
				this.powerFraction_fr = fr * this.powerFractions_fr.get(this.eventIndex);
	    		
				startTimes_h.set( eventIndex, startTimes_h.get(eventIndex) + 24 );
				endTimes_h.set( eventIndex, endTimes_h.get(eventIndex) + 24 );
				eventIndex++;
				if ( eventIndex >= startTimes_h.size() ) {
					eventIndex = 0;
				}
				cooking=false;
				
				if (time_h >= startTimes_h.get(eventIndex)) {
					// factor to compensate for the fact that you might not be cooking for the entire timestep.
					fr = (time_h - this.startTimes_h.get(this.eventIndex)) / this.timeParameters.getTimeStep_h();
					this.powerFraction_fr = fr * this.powerFractions_fr.get(this.eventIndex);	    		
					cooking=true;
					traceln("Starting next cooking session in same timestep as previous session ended!! Rowindex %s, eventIndex %s\", rowIndex, eventIndex");
				}
	    	}
	    	else {
	    		this.powerFraction_fr = this.startTimes_h.get(this.eventIndex);
	    	}
    	} else if (time_h >= startTimes_h.get(eventIndex) ) { // start cooking session. Also check if it ends within this timestep!
    		
			// factor to compensate for the fact that you might not be cooking for the entire timestep.
			double fr = (time_h - this.startTimes_h.get(this.eventIndex)) / this.timeParameters.getTimeStep_h();
			this.powerFraction_fr = fr * this.powerFractions_fr.get(this.eventIndex);	    		
			cooking=true;
			if (time_h >= endTimes_h.get(eventIndex) ) { // end cooking session in the same timestep? Still need to fix energy use for this case!! 
				fr = (this.endTimes_h.get(this.eventIndex) - this.startTimes_h.get(this.eventIndex)) / this.timeParameters.getTimeStep_h();	    		
				this.powerFraction_fr = fr * this.powerFractions_fr.get(this.eventIndex);	    		
	    		
				startTimes_h.set( eventIndex, startTimes_h.get(eventIndex) + 24 );
				endTimes_h.set( eventIndex, endTimes_h.get(eventIndex) + 24 );
				eventIndex++;
				if ( eventIndex >= startTimes_h.size() ) {
					eventIndex = 0;
				}
				cooking=false;
			}
    	}
    	else {
    		this.powerFraction_fr = 0;
    	}
    	hob.f_updateAllFlows(powerFraction_fr, timeVariables);
    }
    
    
    //Setters
    public void setHob(J_EAConversionHob hob) {
    	this.hob = hob;
    }    
    
    
    //Getters
    public J_EAConversionHob getHob() {
    	return this.hob;
    }
    
    
    @Override
    public void storeStatesAndReset() {
    	eventIndexStored = eventIndex;
    	storedStartTimes_h = new ArrayList<>(startTimes_h);
    	storedEndTimes_h = new ArrayList<>(endTimes_h);    	
		startTimes_h = new ArrayList<>(initalStartTimes_h);
		endTimes_h = new ArrayList<>(initalEndTimes_h);
    	eventIndex = 0;
    }
    
    @Override
    public void restoreStates() {
    	eventIndex = eventIndexStored;
		startTimes_h = new ArrayList<>(storedStartTimes_h);
		endTimes_h = new ArrayList<>(storedEndTimes_h);
	}
    
	@Override
	public String toString() {
		String outputString =  "J_ActivityTrackerCooking: \n";
		outputString += "Based on CSV data with row index: " + CSVRowIndex + "\n";	
		return outputString;
	}
}
