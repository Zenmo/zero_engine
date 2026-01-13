/**
 * J_ActivityTrackerTrips
 */	
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_ActivityTrackerTrips extends J_ActivityTracker implements Serializable {
	
	public ArrayList<Double> distances_km = new ArrayList<>();
	private int rowIndex;
	public I_Vehicle Vehicle;
	public double v_idleTimeToNextTrip_min;
	public double v_idleTimeToNextTripStored_min;
	public double v_tripDist_km;
	public double v_energyNeedForNextTrip_kWh;
	public double v_energyNeedForNextTripStored_kWh;
	public double v_nextEventStartTime_min;
	public double distanceScaling_fr = 1.0;
	public double currentTripTimesteps_n;
	//public String tripPatternIdentifier; 
	
    /**
     * Default constructor
     */
    public J_ActivityTrackerTrips() {
    }
    
    public J_ActivityTrackerTrips(EnergyModel main, TextFile tripsCsv, int rowIndex, double time_min, I_Vehicle Vehicle, I_ChargePointRegistration chargePointRegistration) {
    	this.energyModel = main;
    	this.rowIndex = rowIndex;
    	this.Vehicle = Vehicle;		
    	
    	tripsCsv.close();
    	tripsCsv.canReadMore();

		tripsCsv.readLine(); // Skips first line
		 
    	while (roundToInt(tripsCsv.readDouble())!=rowIndex && tripsCsv.canReadMore()) { // Skip until rowIndex found
    		tripsCsv.readLine(); 
    		//String line = tripsCsv.readLine(); // Does this also skip to the next line?
    		//traceln("Skipping line: " + line);
    	}
    	int currentLineNb = tripsCsv.getLineNumber();
    	//traceln("rowIndex %s found on line: %s", rowIndex, currentLineNb);
    	int nbActivities = tripsCsv.readInt();
    	
       	for (int i = 0; i < nbActivities; i++){
    		starttimes_min.add(tripsCsv.readDouble());
    		endtimes_min.add(tripsCsv.readDouble());
    		distances_km.add(tripsCsv.readDouble());
    	}

      	// 'forward' to next current activity
       	setStartIndex(time_min/60.0, chargePointRegistration);    
   }
   
   private double getTimeSinceWeekStart(double time_min) {
	   double timeSinceWeekStart_min = (time_min + (energyModel.v_dayOfWeek1jan-1) * 24 * 60) % (7*24*60); //  Trip start/end-times are all defined as minutes since monday 00:00h
	   return timeSinceWeekStart_min;
   }
    
   private void setNextTrip() {
	   v_eventIndex++;
	   if ( v_eventIndex > starttimes_min.size() - 1 ) {	
	   		v_eventIndex = 0;
	   }
   }
       
   public void setVehicle(J_EAVehicle Vehicle) {
	   this.Vehicle = Vehicle;
   }
   
   public void setDistanceScaling_fr(double distanceScaling_fr) {
	   this.distanceScaling_fr = distanceScaling_fr;
   }
      
   public double getAnnualDistance_km() {
	   double currentAnnualDistance_km = 52 * distances_km.stream().mapToDouble(a -> a).sum(); // assumed trip-data is one week long!! Hence the 52, for 52 weeks in a year.
	   return currentAnnualDistance_km;
   }
   
   public void setAnnualDistance_km(double desiredAnnualDistance_km) { // Scale trips to come to a certain total annual distance traveled. This can lead to unfeasibly long trips for EVs!!
	   double currentAnnualDistance_km = getAnnualDistance_km();
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
    
   public void manageActivities(double t_h, I_ChargePointRegistration chargePointRegistration) {
		double time_min = t_h * 60;
		double timeSinceWeekStart_min = getTimeSinceWeekStart(time_min); //  Trip start/end-times are all defined as minutes since monday 00:00h, trips are looped indefinitely
    	if (Vehicle.getAvailability()) { // at start of timestep! check for multiple 'events' in timestep!
    		//if (time_min == roundToInt(starttimes_min.get(v_eventIndex) / (60*energyModel.p_timeStep_h)) * (energyModel.p_timeStep_h * 60) ) { // is a trip starting this timestep?
        	if ( timeSinceWeekStart_min >= starttimes_min.get(v_eventIndex) && (timeSinceWeekStart_min-energyModel.p_timeStep_h * 60) < starttimes_min.get(v_eventIndex)) { // is a trip starting this timestep?
    			//currentTripDuration = roundToInt(endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex) / (energyModel.p_timeStep_h * 60));
    			currentTripTimesteps_n = max(1,roundToInt(((endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex)) / (energyModel.p_timeStep_h * 60))));
    			
    			Vehicle.startTrip();
    			if(Vehicle instanceof J_EAEV EV) {
    				chargePointRegistration.deregisterChargingRequest(EV);
    			}
        		//if (time_min == roundToInt(endtimes_min.get(v_eventIndex) / (60*energyModel.p_timeStep_h)) * (energyModel.p_timeStep_h*60) ) { // is the trip also ending this timestep?
            	if (timeSinceWeekStart_min >= endtimes_min.get(v_eventIndex) && (timeSinceWeekStart_min-energyModel.p_timeStep_h * 60) < endtimes_min.get(v_eventIndex)) { // is the trip also ending this timestep?
        			Vehicle.endTrip(v_tripDist_km);
        			setNextTrip();//v_eventIndex++;
        			prepareNextActivity(time_min, chargePointRegistration);
        		}
    		}
    	} else {
    		if (Vehicle instanceof J_EAPetroleumFuelVehicle) {
    			J_EAPetroleumFuelVehicle petroleumFuelVehicle = (J_EAPetroleumFuelVehicle)Vehicle;
    			petroleumFuelVehicle.progressTrip(v_tripDist_km / currentTripTimesteps_n);
    		}
    		else if (Vehicle instanceof J_EAHydrogenVehicle) {
    			J_EAHydrogenVehicle hydrogenVehicle = (J_EAHydrogenVehicle)Vehicle;
    			hydrogenVehicle.progressTrip(v_tripDist_km / currentTripTimesteps_n);
    		}
    		//if (time_min == roundToInt(endtimes_min.get(v_eventIndex)/ (60*energyModel.p_timeStep_h)) * 60*energyModel.p_timeStep_h ) { // is a trip ending this timestep?
        	if (timeSinceWeekStart_min >= endtimes_min.get(v_eventIndex) && (timeSinceWeekStart_min-energyModel.p_timeStep_h * 60) < endtimes_min.get(v_eventIndex)) { // is a trip ending this timestep?
    			Vehicle.endTrip(v_tripDist_km);
    			setNextTrip();//v_eventIndex++;
    			prepareNextActivity(time_min, chargePointRegistration);
        		//if (time_min == roundToInt(starttimes_min.get(v_eventIndex) / (60*energyModel.p_timeStep_h)) * (energyModel.p_timeStep_h*60) ) { // is the next trip also starting this timestep?
            	if (timeSinceWeekStart_min >= starttimes_min.get(v_eventIndex) && (timeSinceWeekStart_min-energyModel.p_timeStep_h * 60) < starttimes_min.get(v_eventIndex) ) { // is the next trip also starting this timestep?
        			currentTripTimesteps_n = max(1,roundToInt(((endtimes_min.get(v_eventIndex) - starttimes_min.get(v_eventIndex)) / (energyModel.p_timeStep_h * 60))));
        			Vehicle.startTrip();
        			if(Vehicle instanceof J_EAEV EV) {
        				chargePointRegistration.deregisterChargingRequest(EV);
        			}
        		}
    		}

    	}
    }
   
   	public void setStartIndex(double t_h, I_ChargePointRegistration chargePointRegistration) {
   		// 'forward' to current activity if tripTracker is instantiated not at the start of the simulation or year
   		double time_min = t_h * 60.0;
   		
   		double timeSinceWeekStart_min = getTimeSinceWeekStart(time_min);
    	boolean looped = false;
	    while ( starttimes_min.get(v_eventIndex) < (timeSinceWeekStart_min ) ) {	
	    	setNextTrip(); // Skip to the next trip.

	    	if (v_eventIndex == starttimes_min.size()-1 ) { 
	    		if (looped) { // break while loop!
	    			setNextTrip(); // Reverts to first trip of week
	    			break;
	    		} else {
	    			looped = true;
	    		}
	    	}
	    	
	    }
	    prepareNextActivity(time_min, chargePointRegistration);    	
   	}
    
    public void prepareNextActivity(double time_min, I_ChargePointRegistration chargePointRegistration) {
		/*if ( v_eventIndex >= starttimes_min.size()  ) { // This should only happen at the end of the year, not every week.
			v_eventIndex = 0;
		}*/
       	double timeSinceWeekStart_min = getTimeSinceWeekStart(time_min);
	    v_nextEventStartTime_min = starttimes_min.get(v_eventIndex);
		v_idleTimeToNextTrip_min = (v_nextEventStartTime_min - timeSinceWeekStart_min) % (24*7*60); // Modulo 24*7*60 needed because otherwise negative values can occur when trip starts 'next week'.
		v_tripDist_km = distanceScaling_fr * distances_km.get( v_eventIndex ); // Update upcoming trip distance

		if (Vehicle instanceof J_EAEV ev) {
			
			v_energyNeedForNextTrip_kWh = ev.energyConsumption_kWhpkm * v_tripDist_km;
			if (v_idleTimeToNextTrip_min > 0 && (v_energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh())> v_idleTimeToNextTrip_min/60 * ev.capacityElectric_kW) {
				traceln("TripTracker reports: charging need for next trip is not feasible! Time till next trip: %s hours, chargeNeed_kWh: %s", roundToDecimal(v_idleTimeToNextTrip_min/60,2), roundToDecimal(v_energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh(),2));
			}
			v_energyNeedForNextTrip_kWh = min(v_energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());  // added 10kWh margin 'just in case'. This is actually realistic; people will charge their cars a bit more than strictly needed for the next trip, if possible.
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
			double additionalChargingNeededForNextTrip_kWh = max(0,nextTripDist_km * ev.energyConsumption_kWhpkm - (nextTripStartTime_min - endtimes_min.get(v_eventIndex))/60*ev.getVehicleChargingCapacity_kW());
			/*if (additionalChargingNeededForNextTrip_kWh>0) {
				traceln("*******Additional charging required to prepare for trip after next trip!*********");
			}*/
			v_energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
			ev.energyNeedForNextTrip_kWh = v_energyNeedForNextTrip_kWh;
			/*if ( (v_energyNeedForNextTrip_kWh - EV.getCurrentStateOfCharge() * EV.getStorageCapacity_kWh()) / (v_idleTimeToNextTrip_min/60) > EV.capacityElectric_kW ) {
				traceln("Infeasible trip pattern for EV, not enough time to charge for next trip! Required charging power is: " + (v_energyNeedForNextTrip_kWh - EV.getCurrentStateOfCharge() * EV.getStorageCapacity_kWh()) / (v_idleTimeToNextTrip_min/60) + " kW");
				traceln("RowIndex: " + rowIndex + " tripDistance: " + v_tripDist_km + " km, time to next trip: " + v_idleTimeToNextTrip_min + " minutes");
			} */
			
			//Register EV at the chargepoint
			chargePointRegistration.registerChargingRequest(ev);
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

	@Override
    public void storeAndResetState() {
    	v_eventIndexStored = v_eventIndex;
    	v_energyNeedForNextTripStored_kWh = v_energyNeedForNextTrip_kWh;
    	v_idleTimeToNextTripStored_min = v_idleTimeToNextTrip_min;
    	v_eventIndex = 0;
    	v_energyNeedForNextTrip_kWh = 0;
    	v_idleTimeToNextTrip_min = 0;
    }
	
    @Override
    public void restoreState() {
    	v_eventIndex = v_eventIndexStored;
	    v_nextEventStartTime_min = starttimes_min.get(v_eventIndex);
		v_idleTimeToNextTrip_min = v_idleTimeToNextTripStored_min;
		v_tripDist_km = distanceScaling_fr * distances_km.get( v_eventIndex ); // Update upcoming trip distance
		v_energyNeedForNextTrip_kWh = v_energyNeedForNextTripStored_kWh;
		
		if(Vehicle instanceof J_EAEV ev) {
			ev.energyNeedForNextTrip_kWh = v_energyNeedForNextTrip_kWh;
		}
	}
    
    
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;
}
