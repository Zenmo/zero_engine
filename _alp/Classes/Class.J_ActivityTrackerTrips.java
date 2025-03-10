
/**
 * J_ActivityTrackerTrips
 */	
public class J_ActivityTrackerTrips extends J_ActivityTracker implements Serializable {
	
	public ArrayList<Double> distances_km = new ArrayList<>();
	private int rowIndex;
	public J_EAVehicle Vehicle;
	public double v_idleTimeToNextTrip_min;
	public double v_tripDist_km;
	public double v_energyNeedForNextTrip_kWh;
	public double v_nextEventStartTime_min;
	public double distanceScaling_fr = 1.0;
	public double currentTripTimesteps_n;
	public String tripPatternIdentifier; 
	
    /**
     * Default constructor
     */
    public J_ActivityTrackerTrips() {
    }
    
    public J_ActivityTrackerTrips(EnergyModel main, ExcelFile tripsExcel, int rowIndex, double time_min, J_EAVehicle Vehicle) {
    	this.energyModel = main;
    	this.rowIndex = rowIndex;
    	this.Vehicle = Vehicle;		
		this.nbActivities = roundToInt(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 2));
		this.tripPatternIdentifier = "";// tripsExcel.getCellStringValue("sheet1", rowIndex + 2, 1);
		
	    for (int i = 0; i < nbActivities; i++){
	    	starttimes_min.add(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 3 + i * 3));
	    	endtimes_min.add(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 4 + i * 3));
	    	distances_km.add(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 5 + i * 3));		    
	    }
	    
	    // If trips have in inputdata have a 1-week schedule (endtime < 10080), then duplicate activities until the end of the year
    	if (endtimes_min.get(nbActivities-1) < 10080) {
		    for (int weeks = 1; weeks < 53; weeks++) {
		    	for (int eventIdx = 0; eventIdx < nbActivities; eventIdx++) {
		    		starttimes_min.add(starttimes_min.get(eventIdx) + 10080*weeks);
		    		endtimes_min.add(endtimes_min.get(eventIdx) + 10080*weeks);
		    		distances_km.add(distances_km.get(eventIdx));
		    	}
		    }
	    }
    	
	    // 'forward' to current activity if tripTracker is instantiated not at the start of the simulation or year
	    while ( starttimes_min.get(v_eventIndex) < time_min ) {	
	    	v_eventIndex++;
	    	if ( v_eventIndex > starttimes_min.size() - 1 ) {	
	    		break;
	    	}
	    }
	    prepareNextActivity(time_min);    	
    }
    
    public J_ActivityTrackerTrips(EnergyModel main, ExcelFile tripsExcel, int rowIndex, J_EAVehicle Vehicle) {
    	this.energyModel = main;
    	this.rowIndex = rowIndex;
    	this.Vehicle = Vehicle;		
		this.nbActivities = roundToInt(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 2));
		this.tripPatternIdentifier = "";// tripsExcel.getCellStringValue("sheet1", rowIndex + 2, 1);
		
	    for (int i = 0; i < nbActivities; i++){
	    	starttimes_min.add(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 3 + i * 3));
	    	endtimes_min.add(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 4 + i * 3));
	    	distances_km.add(tripsExcel.getCellNumericValue("sheet1", rowIndex + 2, 5 + i * 3));		    
	    }
	    
	    // If trips have in inputdata have a 1-week schedule (endtime < 10080), then duplicate activities until the end of the year
    	if (endtimes_min.get(nbActivities-1) < 10080) {
		    for (int weeks = 1; weeks < 53; weeks++) {
		    	for (int eventIdx = 0; eventIdx < nbActivities; eventIdx++) {
		    		starttimes_min.add(starttimes_min.get(eventIdx) + 10080*weeks);
		    		endtimes_min.add(endtimes_min.get(eventIdx) + 10080*weeks);
		    		distances_km.add(distances_km.get(eventIdx));
		    	}
		    }
	    }
    	
    	// Determine 'minute of week'
    	double minuteOfWeek = ((energyModel.v_dayOfWeek1jan - 1)*24 + energyModel.t_h)*60;
	    // 'forward' to current activity if tripTracker is instantiated not at the start of the simulation or year
	    while ( starttimes_min.get(v_eventIndex) < minuteOfWeek ) {	
	    	v_eventIndex++;
	    	if ( v_eventIndex > starttimes_min.size() - 1 ) {	
	    		break;
	    	}
	    }
	    prepareNextActivity(minuteOfWeek);    	
    }
   
   public void setVehicle(J_EAVehicle Vehicle) {
	   this.Vehicle = Vehicle;
   }
   
   public void setDistanceScaling_fr(double distanceScaling_fr) {
	   this.distanceScaling_fr = distanceScaling_fr;
   }
   
   public void setAnnualDistance_km(double desiredAnnualDistance_km) { // Scale trips to come to a certain total annual distance traveled. This can lead to unfeasibly long trips for EVs!!
	   /* double currentAnnualDistance_km = 0;
	   int tripNo=0;
	   // Get current annual distance
	   while (endtimes_min.get(tripNo) < 60*24*365) {
		   currentAnnualDistance_km += distances_km.get(tripNo);
		   tripNo++;
	   }
	   */
	   double currentAnnualDistance_km = distances_km.stream().mapToDouble(a -> a).sum();
	   double scalingFactor_f = desiredAnnualDistance_km / currentAnnualDistance_km;
	   
	   //distances_km = (ArrayList<Double>)distances_km.stream().map(a -> scalingFactor_f*a).toList();
       ListIterator<Double> iterator = distances_km.listIterator();                                                              
	   for (int i = 0; i<distances_km.size(); i++) {
		   //distances_km[i] = distances(i)*scalingFactor_f;		   
		     iterator.next();
		     iterator.set(distances_km.get(i)*scalingFactor_f);
	   }
	   //traceln("Annual distance of triptracker set to: %s km", distances_km.stream().mapToDouble(a -> a).sum());                                
	   //traceln("Desired annual distance was: %s km", desiredAnnualDistance_km);
   }
    
   public void manageActivities(double time_min) {
    	if (Vehicle.getAvailability()) { // at start of timestep! check for multiple 'events' in timestep!
    		//if (time_min == roundToInt(starttimes_min.get(v_eventIndex) / (60*energyModel.p_timeStep_h)) * (energyModel.p_timeStep_h * 60) ) { // is a trip starting this timestep?
        	if (time_min >= starttimes_min.get(v_eventIndex) ) { // is a trip starting this timestep?
    			//currentTripDuration = roundToInt(endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex) / (energyModel.p_timeStep_h * 60));
    			currentTripTimesteps_n = max(1,roundToInt(((endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex)) / (energyModel.p_timeStep_h * 60))));

    			Vehicle.startTrip();
    			//main.v_activeTrips.incrementAndGet();
        		//if (time_min == roundToInt(endtimes_min.get(v_eventIndex) / (60*energyModel.p_timeStep_h)) * (energyModel.p_timeStep_h*60) ) { // is the trip also ending this timestep?
            	if (time_min >= endtimes_min.get(v_eventIndex) ) { // is the trip also ending this timestep?
        			Vehicle.endTrip(v_tripDist_km);
        			v_eventIndex++;
        			//main.v_activeTrips.decrementAndGet();
        			prepareNextActivity(time_min);
        		}
    		}

    	} else {
    		if (Vehicle instanceof J_EADieselVehicle) {
    			J_EADieselVehicle dieselVehicle = (J_EADieselVehicle)Vehicle;
    			dieselVehicle.progressTrip(v_tripDist_km / currentTripTimesteps_n);
    		}
    		else if (Vehicle instanceof J_EAHydrogenVehicle) {
    			J_EAHydrogenVehicle hydrogenVehicle = (J_EAHydrogenVehicle)Vehicle;
    			hydrogenVehicle.progressTrip(v_tripDist_km / currentTripTimesteps_n);
    		}
    		//if (time_min == roundToInt(endtimes_min.get(v_eventIndex)/ (60*energyModel.p_timeStep_h)) * 60*energyModel.p_timeStep_h ) { // is a trip ending this timestep?
        	if (time_min >= endtimes_min.get(v_eventIndex) ) { // is a trip ending this timestep?
    			Vehicle.endTrip(v_tripDist_km);
    			v_eventIndex++;
    			//main.v_activeTrips.decrementAndGet();
    			prepareNextActivity(time_min);
        		//if (time_min == roundToInt(starttimes_min.get(v_eventIndex) / (60*energyModel.p_timeStep_h)) * (energyModel.p_timeStep_h*60) ) { // is the next trip also starting this timestep?
            	if (time_min >= starttimes_min.get(v_eventIndex) ) { // is the next trip also starting this timestep?
        			//currentTripDuration = roundToInt(endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex) / (energyModel.p_timeStep_h * 60));
        			currentTripTimesteps_n = max(1,roundToInt(((endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex)) / (energyModel.p_timeStep_h * 60))));
        			//traceln("Hello! :P");
        			Vehicle.startTrip();
        			//main.v_activeTrips.incrementAndGet();
        		}
    		}

    	}
    }
   
   	public void setStartIndex(double t_h) {
   		// 'forward' to current activity if tripTracker is instantiated not at the start of the simulation or year
   		double time_min = t_h * 60.0;
	    while ( starttimes_min.get(v_eventIndex) < time_min ) {	
	    	v_eventIndex++;
	    	if ( v_eventIndex > starttimes_min.size() - 1 ) {	
	    		break;
	    	}
	    }
	    prepareNextActivity(time_min);    	
   	}
    
    public void prepareNextActivity(double time_min) {
		if ( v_eventIndex >= starttimes_min.size()  ) {
			v_eventIndex = 0;
		}
		
	    v_nextEventStartTime_min = starttimes_min.get(v_eventIndex);
		v_idleTimeToNextTrip_min = v_nextEventStartTime_min - time_min;
		v_tripDist_km = distanceScaling_fr * distances_km.get( v_eventIndex ); // Update upcoming trip distance

		if (Vehicle instanceof J_EAEV) {
			J_EAEV EV = (J_EAEV)Vehicle;
			v_energyNeedForNextTrip_kWh = EV.energyConsumption_kWhpkm * v_tripDist_km;

			// Check if more charging is needed for next trip!
			double nextTripDist_km = 0;
			double nextTripStartTime_min = 0;

			if ( v_eventIndex == starttimes_min.size() - 1 ) {
				nextTripDist_km = 0;//distances_km.get( 0 );
				nextTripStartTime_min = endtimes_min.get(v_eventIndex);
			} else {		
				nextTripDist_km = distanceScaling_fr*distances_km.get( v_eventIndex+1 );
				nextTripStartTime_min = starttimes_min.get( v_eventIndex+1 );
			}
			double additionalChargingNeededForNextTrip_kWh = max(0,nextTripDist_km * EV.energyConsumption_kWhpkm - (nextTripStartTime_min - endtimes_min.get(v_eventIndex))/60*EV.getCapacityElectric_kW());
			//if (additionalChargingNeededForNextTrip_kWh>0) {
			//	traceln("*******Additional charging required to prepare for trip after next trip!*********");
			//}
			v_energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
			EV.energyNeedForNextTrip_kWh = v_energyNeedForNextTrip_kWh;
			/*if ( (v_energyNeedForNextTrip_kWh - EV.getCurrentStateOfCharge() * EV.getStorageCapacity_kWh()) / (v_idleTimeToNextTrip_min/60) > EV.capacityElectric_kW ) {
				traceln("Infeasible trip pattern for EV, not enough time to charge for next trip! Required charging power is: " + (v_energyNeedForNextTrip_kWh - EV.getCurrentStateOfCharge() * EV.getStorageCapacity_kWh()) / (v_idleTimeToNextTrip_min/60) + " kW");
				traceln("RowIndex: " + rowIndex + " tripDistance: " + v_tripDist_km + " km, time to next trip: " + v_idleTimeToNextTrip_min + " minutes");
			} */
		}
    }
    
	@Override
	public String toString() {
		return
			"Number of trips = " + this.nbActivities + " " + 
			"Row index = " + this.rowIndex + " " + 
			"Distance Scaling = " + this.distanceScaling_fr + " ";
	}
	
	public double getDistanceScaling_fr( ) {
		return this.distanceScaling_fr;
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;
}
