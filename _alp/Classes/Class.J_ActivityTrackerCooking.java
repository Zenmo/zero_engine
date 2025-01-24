/**
 * J_ActivityTrackerCooking
 */	
public class J_ActivityTrackerCooking extends zero_engine.J_ActivityTracker implements Serializable {
	private ArrayList<Double> powerFractions_fr = new ArrayList<>();
	public J_EAConversion HOB;
	public double powerFraction_fr=0;
	private int rowIndex;
	private boolean cooking = false;
	private double timeStep_min;
	private ArrayList<Double> initalStarttimes_min;
	private ArrayList<Double> initalEndtimes_min;
	private ArrayList<Double> storedStarttimes_min;
	private ArrayList<Double> storedEndtimes_min;

	/**
     * Default constructor
     */
    public J_ActivityTrackerCooking() {
    }

    public J_ActivityTrackerCooking(ExcelFile inputCookingActivities, int rowIndex, double time_min, J_EAConversion HOB) {
    	//this.energyModel = main;
    	this.rowIndex = rowIndex;
    	this.HOB=HOB;
    	//int rowIndex = uniform_discr(2, 300); 
    	//traceln("RowIndex: %s", this.rowIndex);
    	//traceln(this.HOB.getParentAgent());
    	
    	this.timeStep_min = 60 * this.HOB.timestep_h;
    	
    	double v_cookingPatternIndex = inputCookingActivities.getCellNumericValue("sheet1", rowIndex, 1);
    	int nbOfCookingSessions = (int)(inputCookingActivities.getCellNumericValue("sheet1", rowIndex, 2));

    	for (int i = 0; i < nbOfCookingSessions; i++){
    		starttimes_min.add(inputCookingActivities.getCellNumericValue("sheet1", rowIndex, 3 + i * 3));
    		endtimes_min.add(inputCookingActivities.getCellNumericValue("sheet1", rowIndex, 4 + i * 3));
    		
    		double ratio = inputCookingActivities.getCellNumericValue("sheet1", rowIndex, 5 + i * 3) / HOB.getOutputCapacity_kW();
    		powerFractions_fr.add(ratio);
    	}

    	while ( starttimes_min.get(v_eventIndex) - time_min < 0) {
    		starttimes_min.set( v_eventIndex, starttimes_min.get(v_eventIndex) + 1440 );
    		endtimes_min.set( v_eventIndex, endtimes_min.get(v_eventIndex) + 1440 );
    		v_eventIndex++;
    		if ( v_eventIndex > starttimes_min.size() - 1 ) {
    			v_eventIndex = 0;
    		}
    	}
    	
    	
    	initalStarttimes_min = new ArrayList<>(starttimes_min);
    	initalEndtimes_min = new ArrayList<>(endtimes_min);
    	//traceln("Current model time in minutes: " + energyModel.t_h*60 + ", nb sessions: " + nbOfCookingSessions);
    	//traceln("Starttimes: %s", starttimes_min);
    	//traceln("Endtimes: %s", endtimes_min);
    }
    
    public void manageActivities(double time_min) {
    	//traceln("Cooking tracker current time: " + time_min);
    	//traceln("Event index: " + v_eventIndex);
    	//traceln("startTimes: " + starttimes_min);
    	//traceln("endTimes: " + endtimes_min);
    	//traceln("powerFractions_fr: "  + powerFractions_fr);
    	
    	if (cooking) {
	    	if (time_min >= endtimes_min.get(v_eventIndex) ) { // end cooking session. Also check if a new one starts in this timestep!

	    		//main.v_activeCookingSessions.decrementAndGet();
	    		//traceln("End of cooking session, currently active cooking sessions %s", main.v_activeCookingSessions);
				// factor to compensate for the fact that you might not be cooking for the entire timestep.
				double fr = (time_min - this.endtimes_min.get(this.v_eventIndex)) / this.timeStep_min;
				this.powerFraction_fr = fr * this.powerFractions_fr.get(this.v_eventIndex);
	    		
				starttimes_min.set( v_eventIndex, starttimes_min.get(v_eventIndex) + 1440 );
				endtimes_min.set( v_eventIndex, endtimes_min.get(v_eventIndex) + 1440 );
				v_eventIndex++;
				if ( v_eventIndex >= starttimes_min.size() ) {
					v_eventIndex = 0;
				}
				cooking=false;
				
				if (time_min >= starttimes_min.get(v_eventIndex)) {
					// factor to compensate for the fact that you might not be cooking for the entire timestep.
					fr = (time_min - this.starttimes_min.get(this.v_eventIndex)) / this.timeStep_min;
					this.powerFraction_fr = fr * this.powerFractions_fr.get(this.v_eventIndex);	    		
					//main.v_activeCookingSessions.incrementAndGet();
					cooking=true;
					traceln("Starting next cooking session in same timestep as previous session ended!! Rowindex %s, eventIndex %s\", rowIndex, v_eventIndex");
				}
	    	}
	    	else {
	    		this.powerFraction_fr = this.starttimes_min.get(this.v_eventIndex);
	    	}
    	} else if (time_min >= starttimes_min.get(v_eventIndex) ) { // start cooking session. Also check if it ends within this timestep!
    		/*if (endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex) > 100) {
			traceln("Cooking event longer than 100 minutes!! Rowindex %s, eventIndex %s.", rowIndex, v_eventIndex);
			}*/
    		
			// factor to compensate for the fact that you might not be cooking for the entire timestep.
			double fr = (time_min - this.starttimes_min.get(this.v_eventIndex)) / this.timeStep_min;
			this.powerFraction_fr = fr * this.powerFractions_fr.get(this.v_eventIndex);	    		
			//main.v_activeCookingSessions.incrementAndGet();
			cooking=true;
			if (time_min >= endtimes_min.get(v_eventIndex) ) { // end cooking session in the same timestep? Still need to fix energy use for this case!! 
	    	
	    		//main.v_activeCookingSessions.decrementAndGet();
	    		//traceln("End of cooking session, currently active cooking sessions %s", main.v_activeCookingSessions);
				fr = (this.endtimes_min.get(this.v_eventIndex) - this.starttimes_min.get(this.v_eventIndex)) / this.timeStep_min;	    		
				this.powerFraction_fr = fr * this.powerFractions_fr.get(this.v_eventIndex);	    		
	    		
				starttimes_min.set( v_eventIndex, starttimes_min.get(v_eventIndex) + 1440 );
				endtimes_min.set( v_eventIndex, endtimes_min.get(v_eventIndex) + 1440 );
				v_eventIndex++;
				if ( v_eventIndex >= starttimes_min.size() ) {
					v_eventIndex = 0;
				}
				cooking=false;
			}
    	}
    	else {
    		this.powerFraction_fr = 0;
    	}
    	//if (powerFraction_fr > 0 ) { traceln("Cooking event in progress!"); }
    	HOB.f_updateAllFlows(powerFraction_fr);
    }
    
    @Override
    public void storeAndResetState() {
    	v_eventIndexStored = v_eventIndex;
    	storedStarttimes_min = new ArrayList<>(starttimes_min);
    	storedEndtimes_min = new ArrayList<>(endtimes_min);    	
		starttimes_min = new ArrayList<>(initalStarttimes_min);
		endtimes_min = new ArrayList<>(initalEndtimes_min);
    	v_eventIndex = 0;
    }
    
    @Override
    public void restoreState() {
    	v_eventIndex = v_eventIndexStored;
		starttimes_min = new ArrayList<>(storedStarttimes_min);
		endtimes_min = new ArrayList<>(storedEndtimes_min);
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
