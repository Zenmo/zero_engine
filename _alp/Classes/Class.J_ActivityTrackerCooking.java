/**
 * J_ActivityTrackerCooking
 */	
public class J_ActivityTrackerCooking extends zero_engine.J_ActivityTracker implements Serializable {
	private ArrayList<Double> powerFractions_fr = new ArrayList<>();
	public J_EAConversion HOB;
	public double powerFraction_fr=0;
	private int rowIndex;
	private boolean cooking = false;
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

    	//traceln("Current model time in minutes: " + energyModel.t_h*60 + ", nb sessions: " + nbOfCookingSessions);
    }
    
    public void manageActivities(double time_min) {
    	//traceln("Cooking tracker current time: " + time_min);
    	if (cooking) {
	    	if (time_min >= endtimes_min.get(v_eventIndex) ) { // end cooking session. Also check if a new one starts in this timestep!

	    		//main.v_activeCookingSessions.decrementAndGet();
	    		//traceln("End of cooking session, currently active cooking sessions %s", main.v_activeCookingSessions);
	    		powerFraction_fr = 0;
	    		
				starttimes_min.set( v_eventIndex, starttimes_min.get(v_eventIndex) + 1440 );
				endtimes_min.set( v_eventIndex, endtimes_min.get(v_eventIndex) + 1440 );
				v_eventIndex++;
				if ( v_eventIndex >= starttimes_min.size() ) {
					v_eventIndex = 0;
				}
				cooking=false;
				
				if (time_min >= starttimes_min.get(v_eventIndex)) {
					powerFraction_fr = powerFractions_fr.get(v_eventIndex);	    		
					//main.v_activeCookingSessions.incrementAndGet();
					cooking=true;
					traceln("Starting next cooking session in same timestep as previous session ended!! Rowindex %s, eventIndex %s\", rowIndex, v_eventIndex");
				}
	    	}
    	} else if (time_min >= starttimes_min.get(v_eventIndex) ) { // start cooking session. Also check if it ends within this timestep!
    		/*if (endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex) > 100) {
			traceln("Cooking event longer than 100 minutes!! Rowindex %s, eventIndex %s.", rowIndex, v_eventIndex);
			}*/
   		
			powerFraction_fr = powerFractions_fr.get(v_eventIndex);	    		
			//main.v_activeCookingSessions.incrementAndGet();
			cooking=true;
			if (time_min >= endtimes_min.get(v_eventIndex) ) { // end cooking session in the same timestep? Still need to fix energy use for this case!! 
	    	
	    		//main.v_activeCookingSessions.decrementAndGet();
	    		//traceln("End of cooking session, currently active cooking sessions %s", main.v_activeCookingSessions);
	    		powerFraction_fr = 0;
	    		
				starttimes_min.set( v_eventIndex, starttimes_min.get(v_eventIndex) + 1440 );
				endtimes_min.set( v_eventIndex, endtimes_min.get(v_eventIndex) + 1440 );
				v_eventIndex++;
				if ( v_eventIndex >= starttimes_min.size() ) {
					v_eventIndex = 0;
				}
				cooking=false;
			}
    	} 
    	//if (powerFraction_fr > 0 ) { traceln("Cooking event in progress!"); }
    	HOB.f_updateAllFlows(powerFraction_fr);
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
