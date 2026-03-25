/**
 * J_ActivityTrackerTrips
 */	
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_ActivityTrackerTrips extends J_ActivityTracker {
	
	private J_TimeParameters timeParameters;
	
	public ArrayList<Double> distances_km = new ArrayList<>();
	private int rowIndex;
	public I_Vehicle vehicle;
	//public double idleTimeToNextTrip_h;
	//public double idleTimeToNextTripStored_h;
	//public double tripDist_km;
	//public double v_energyNeedForNextTrip_kWh;
	//public double v_energyNeedForNextTripStored_kWh;
	//public double v_nextEventStartTime_min;
	public double distanceScaling_fr = 1.0;
	public double currentTripTimesteps_n;
	private double currentTripStartTime_h;
	private double currentTripEndTime_h;
	//private double upcomingEventEndTime_h;
	//private double nextEventStartTime_h;

	//public String tripPatternIdentifier; 
	
    /**
     * Default constructor
     */
    public J_ActivityTrackerTrips() {
    }
    
    public J_ActivityTrackerTrips(J_TimeParameters timeParameters, TextFile tripsCsv, int rowIndex, J_TimeVariables timeVariables, I_Vehicle vehicle, I_ChargePointRegistration chargePointRegistration) {
    	this.timeParameters = timeParameters;
    	this.rowIndex = rowIndex;
    	this.vehicle = vehicle;		
    	
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
    	
       	for (int i = 0; i < nbActivities; i++){ // Tripdata for one week, lists are not modified during simulation! Instead getters are used to map trips to current week.
    		starttimes_min.add(tripsCsv.readDouble());
    		endtimes_min.add(tripsCsv.readDouble());
    		distances_km.add(tripsCsv.readDouble());
    	}

      	// 'forward' to next current activity
       	setStartIndex(timeVariables, chargePointRegistration);    
   }
   
   private double getTimeSinceWeekStart_h(double t_h) {
	   double timeSinceWeekStart_h = (t_h + (timeParameters.getDayOfWeek1jan()-1) * 24 + 7*24) % (7*24); //  Trip start/end-times are all defined as minutes since monday 00:00h
	   return timeSinceWeekStart_h;
   }
    
   private void setNextTrip(double t_h) {
	   v_eventIndex++; // v_eventIndex can go beyond size of activity lists, use modulo operators to wrap-around trip data.
	   currentTripStartTime_h = getStartTime_h(v_eventIndex, t_h);
	   currentTripEndTime_h = getEndTime_h(v_eventIndex, t_h);
   }
   private double getStartTime_h(int eventIndex, double t_h) {
     	double time_hOfWeek = getTimeSinceWeekStart_h(t_h);
	   	eventIndex = eventIndex % starttimes_min.size(); // wrap-around
	    double eventStartTime_hOfWeek = starttimes_min.get(eventIndex)/60.0;
	    double startTime_h;
	    //if (eventIndex == 0 && time_hOfWeek > eventStartTime_hOfWeek) { // Next week's trip!
		if (time_hOfWeek > eventStartTime_hOfWeek) { // Next week's trip!
	    	startTime_h = t_h + eventStartTime_hOfWeek - time_hOfWeek + 24*7;
	    } else {
	    	startTime_h = t_h + eventStartTime_hOfWeek - time_hOfWeek;
	    }

	    if (startTime_h < t_h) {
	    	traceln("tripTracker.getStartTime_h: startTime_h %s before t_h %s! time_hOfWeek: %s, eventIndex: %s", startTime_h, t_h, time_hOfWeek, eventIndex);
	    }
	    
	    return startTime_h;
   }
   
   private double getEndTime_h(int eventIndex, double t_h) {
		double time_hOfWeek = getTimeSinceWeekStart_h(t_h);
		eventIndex = eventIndex % starttimes_min.size(); // wrap-around
		double nextEventEndTime_hOfWeek = endtimes_min.get(eventIndex)/60.0;
		double endTime_h;
		//if (eventIndex == 0 && time_hOfWeek > nextEventEndTime_hOfWeek) { // Next week's trip!
		if (time_hOfWeek > nextEventEndTime_hOfWeek) { // Next week's trip!
		    	endTime_h = (nextEventEndTime_hOfWeek + t_h - time_hOfWeek) + 168;
		    } else {
		    	endTime_h = (nextEventEndTime_hOfWeek + t_h - time_hOfWeek);
		    }
	   return endTime_h;
   }
   
   private double getTripDistance(int eventIndex) {
	   return this.distanceScaling_fr * this.distances_km.get(eventIndex % distances_km.size());
   }
     
   public void setVehicle(I_Vehicle vehicle) {
	   this.vehicle = vehicle;
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
	   
       ListIterator<Double> iterator = distances_km.listIterator();                                                              
	   for (int i = 0; i<distances_km.size(); i++) {
		     iterator.next();
		     iterator.set(distances_km.get(i)*scalingFactor_f);
	   }
	   //traceln("Annual distance of triptracker set to: %s km", distances_km.stream().mapToDouble(a -> a).sum());                                
	   //traceln("Desired annual distance was: %s km", desiredAnnualDistance_km);
   }
    
   public void manageActivities(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
    	if (vehicle.getAvailability()) { // at start of timestep! check for multiple 'events' in timestep!
           	if ( timeVariables.getT_h() >= this.currentTripStartTime_h && (timeVariables.getT_h()-timeParameters.getTimeStep_h() ) < this.currentTripStartTime_h ) { // is a trip starting this timestep?

    			currentTripTimesteps_n = max(1,roundToInt( (this.getEndTime_h(v_eventIndex, timeVariables.getT_h()) - this.getStartTime_h(v_eventIndex, timeVariables.getT_h())) / timeParameters.getTimeStep_h() )) ;
    			
    			vehicle.startTrip(timeVariables);
    			if(vehicle instanceof J_EAEV EV) {
    				chargePointRegistration.deregisterChargingRequest(EV);
    			}

            	//if (timeVariables.getT_h() >= this.getEndTime_h(v_eventIndex, timeVariables.getT_h()) && (timeVariables.getT_h()-timeParameters.getTimeStep_h() * 60) < this.getEndTime_h(v_eventIndex, timeVariables.getT_h())) { // is the trip also ending this timestep?
                if (timeVariables.getT_h() >= this.currentTripEndTime_h && (timeVariables.getT_h()-timeParameters.getTimeStep_h() * 60) < this.currentTripEndTime_h) { // is the trip also ending this timestep?
            		vehicle.endTrip(this.getTripDistance(v_eventIndex));
            		setNextTrip(timeVariables.getT_h());//v_eventIndex++;		
        			prepareNextActivity(timeVariables, chargePointRegistration);
        		}
    		}
    	} else {
    		if (vehicle instanceof J_EAFuelVehicle fuelVehicle) {
    			fuelVehicle.progressTrip(this.getTripDistance(v_eventIndex) / currentTripTimesteps_n);
    		}

    		if (timeVariables.getT_h() >= this.currentTripEndTime_h && (timeVariables.getT_h()-timeParameters.getTimeStep_h() * 60) < this.currentTripEndTime_h) { // is a trip ending this timestep?
    			vehicle.endTrip(this.getTripDistance(v_eventIndex));
    			setNextTrip(timeVariables.getT_h());//v_eventIndex++;		
    			prepareNextActivity(timeVariables, chargePointRegistration);
    	        if ( timeVariables.getT_h() >= this.currentTripStartTime_h && (timeVariables.getT_h()-timeParameters.getTimeStep_h() ) < this.currentTripStartTime_h ) { // is a trip starting this timestep?
    				currentTripTimesteps_n = max(1,roundToInt( (this.getEndTime_h(v_eventIndex, timeVariables.getT_h()) - this.getStartTime_h(v_eventIndex, timeVariables.getT_h())) / timeParameters.getTimeStep_h() )) ;
        			vehicle.startTrip(timeVariables);
        			if(vehicle instanceof J_EAEV EV) {
        				chargePointRegistration.deregisterChargingRequest(EV);
        			}
        		}
    		}
    	}
    }
   
   	public void setStartIndex(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
   		// 'forward' to current activity if tripTracker is instantiated not at the start of the simulation or year   		
   		double time_hOfWeek = this.getTimeSinceWeekStart_h(timeVariables.getT_h());
   		v_eventIndex = 0;
    	
	    for ( int i = 0; i < this.starttimes_min.size(); i++) { // If this occurs 'during' a trip, that trip is ignored, it is not executed.
	    	if (this.starttimes_min.get(i)/60.0 < time_hOfWeek) {
	    		v_eventIndex++;
	    	} else {
	    		break;
	    	}
	    }
	    this.currentTripStartTime_h = getStartTime_h(v_eventIndex, timeVariables.getT_h());    
	    prepareNextActivity(timeVariables, chargePointRegistration);    	
   	}
    
    public void prepareNextActivity(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {

    	double idleTimeToNextTrip_h = timeParameters.getTimeStep_h()*ceil( this.currentTripStartTime_h/timeParameters.getTimeStep_h()) - timeVariables.getT_h(); // if trip starts exactly at 15 minutes, don't count leave-time as a charge-opportunity.
    	if (idleTimeToNextTrip_h < 0 ) {
    		traceln("TripTracker: next trip should have already started! timeToCharge_h: %s", idleTimeToNextTrip_h);
    	}
		double tripDist_km = this.getTripDistance(v_eventIndex); // Update upcoming trip distance

		if (vehicle instanceof J_EAEV ev) {
			
			double energyNeedForNextTrip_kWh = ev.getEnergyConsumption_kWhpkm() * tripDist_km; // Now includes vehicle scaling in J_EAEV!
			if (idleTimeToNextTrip_h > 0 && (energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh())> idleTimeToNextTrip_h * ev.getVehicleChargingCapacity_kW()) {
				traceln("TripTracker reports: charging need for next trip is not feasible! Time till next trip: %s hours, chargeNeed_kWh: %s", roundToDecimal(idleTimeToNextTrip_h,2), roundToDecimal(energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh(),2));
			}
			double tripEndTime_h = timeParameters.getTimeStep_h()*ceil(this.getEndTime_h(v_eventIndex, timeVariables.getT_h())/timeParameters.getTimeStep_h());

			double nextTripDist_km = this.getTripDistance(v_eventIndex+1);
			double nextTripStartTime_h = timeParameters.getTimeStep_h()*ceil(this.getStartTime_h(v_eventIndex+1, timeVariables.getT_h())/timeParameters.getTimeStep_h());
			
			double additionalChargingNeededForNextTrip_kWh = max(0,nextTripDist_km * ev.getEnergyConsumption_kWhpkm() - ( nextTripStartTime_h - tripEndTime_h )*ev.getVehicleChargingCapacity_kW());
			energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
			energyNeedForNextTrip_kWh = min(energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());
			//traceln("TripTracker, energyNeedForNextTrip: %s", v_energyNeedForNextTrip_kWh);
			ev.setEnergyNeedForNextTrip_kWh(energyNeedForNextTrip_kWh);
			//Register EV at the chargepoint
			chargePointRegistration.registerChargingRequest(ev);
		}
    }
    
    public double getNextEventStartTime_h() {
    	return this.currentTripStartTime_h;
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
    public void storeStatesAndReset() {
    	v_eventIndexStored = v_eventIndex;
    	//v_energyNeedForNextTripStored_kWh = v_energyNeedForNextTrip_kWh;
    	//v_idleTimeToNextTripStored_min = v_idleTimeToNextTrip_min;
    	//v_eventIndex = 0; Taken care of by setStartIndex() call!
    	//v_energyNeedForNextTrip_kWh = 0;
    	//v_idleTimeToNextTrip_min = 0;
    }
	
    @Override
    public void restoreStates() {
    	v_eventIndex = v_eventIndexStored;
	    //v_nextEventStartTime_min = starttimes_min.get(v_eventIndex);
		//v_idleTimeToNextTrip_min = v_idleTimeToNextTripStored_min;
		//v_tripDist_km = distanceScaling_fr * distances_km.get( v_eventIndex ); // Update upcoming trip distance
		
		/*
		v_energyNeedForNextTrip_kWh = v_energyNeedForNextTripStored_kWh;
		if(vehicle instanceof J_EAEV ev) {
			ev.setEnergyNeedForNextTrip_kWh(v_energyNeedForNextTrip_kWh);
		}*/
	}
}
