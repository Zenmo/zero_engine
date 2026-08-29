/**
 * J_ActivityTrackerTrips
 */	
import java.util.ArrayList;
import java.util.ListIterator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_ActivityTrackerTrips extends J_ActivityTracker {
	
	private J_TimeParameters timeParameters;
	
	private List<TripRecord> tripRecords = new ArrayList<>();
	private Integer CSVRowIndex = null;
	private I_Vehicle vehicle;

	private double nextEventStartTime_h;
	private double idleTimeToNextTrip_h;
	private double tripDistance_km;
	private double distanceScaling_fr = 1.0;
	private double currentTripTimesteps_n;

	
	//Stored variables
	private double nextEventStartTimeStored_h;
	private double idleTimeToNextTripStored_h;
    /**
     * Empty constructor for serialization
     */
    public J_ActivityTrackerTrips() {
    }
    
    /**
     * Default constructor using CSV trip input file
     */    
    public J_ActivityTrackerTrips(TextFile tripsCSV, int CSVRowIndex, I_Vehicle vehicle, I_ChargePointRegistration chargePointRegistration, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
    	this.timeParameters = timeParameters;
    	this.CSVRowIndex = CSVRowIndex;
    	this.vehicle = vehicle;		
    	
    	tripsCSV.close();
    	tripsCSV.canReadMore();
    	tripsCSV.readLine(); // Skips first line
		 
    	while (roundToInt(tripsCSV.readDouble())!= CSVRowIndex && tripsCSV.canReadMore()) { // Skip until rowIndex found
    		tripsCSV.readLine(); 
    	}
    	int currentLineNb = tripsCSV.getLineNumber();
    	int nbActivities = tripsCSV.readInt();
 
       	for (int i = 0; i < nbActivities; i++){
       		this.tripRecords.add(new TripRecord(tripsCSV.readDouble()/60.0, tripsCSV.readDouble()/60.0, tripsCSV.readDouble()));
    	}

      	// 'forward' to next activity
       	setStartIndex(timeVariables, chargePointRegistration);    
    }
   
    /**
     * Constructor using defined trips as input
     */
    public J_ActivityTrackerTrips(List<TripRecord> tripRecords, I_Vehicle vehicle, I_ChargePointRegistration chargePointRegistration, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
      	this.timeParameters = timeParameters;
    	this.vehicle = vehicle;
       	this.tripRecords.addAll(tripRecords);

      	// 'forward' to next activity
       	setStartIndex(timeVariables, chargePointRegistration);    
    }
    
    private double getTimeSinceWeekStart_h(double time_h) {
    	double timeSinceWeekStart_h = (time_h + (timeParameters.getDayOfWeek1jan()-1) * 24) % (7*24);
    	return timeSinceWeekStart_h;
    }
    
    private void setNextTrip() {
    	eventIndex++;
    	if ( eventIndex > tripRecords.size() - 1 ) {	
    		eventIndex = 0;
    	}
    }    
    
   /*
    * Main method that is called every timestep.
    * The function checks if a trip is starting this timestep. If so, it calls startTrip on the vehicle. 
    * It also immediately checks in the trip ends in that same timestep.
    * If the vehicle is already on a trip it progresses the trip to check if the trip ends this timestep.
    * If so, it also checks if a new trip is starting in that same timestep.
    */
    public void manageActivities(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
    	double time_h = timeVariables.getT_h();
    	double timeSinceWeekStart_h = getTimeSinceWeekStart_h(time_h); //  Trip start/end-times are all defined as minutes since monday 00:00h, trips are looped indefinitely
    	if (vehicle.getAvailability()) { // at start of timestep! check for multiple 'events' in timestep!
    		if ( timeSinceWeekStart_h >= tripRecords.get(eventIndex).startTime_h() && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < tripRecords.get(eventIndex).startTime_h()) { // is a trip starting this timestep?
    			currentTripTimesteps_n = max(1,roundToInt(((tripRecords.get(eventIndex).endTime_h() - tripRecords.get(eventIndex).startTime_h()) / (timeParameters.getTimeStep_h()))));
    			
    			vehicle.startTrip(timeVariables);
    			if(vehicle instanceof J_EAEV EV) {
    				chargePointRegistration.deregisterChargingRequest(EV);
    			}
    			if (timeSinceWeekStart_h >= tripRecords.get(eventIndex).endTime_h() && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < tripRecords.get(eventIndex).endTime_h()) { // is the trip also ending this timestep?
    				vehicle.endTrip(tripDistance_km);
    				setNextTrip();
    				prepareNextActivity(timeVariables, chargePointRegistration);
    			}
    		}
    	} else {
    		if (vehicle instanceof J_EAFuelVehicle fuelVehicle) {
    			fuelVehicle.progressTrip(tripDistance_km / currentTripTimesteps_n);
    		}
    		if (timeSinceWeekStart_h >= tripRecords.get(eventIndex).endTime_h() && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < tripRecords.get(eventIndex).endTime_h()) { // is a trip ending this timestep?
    			vehicle.endTrip(tripDistance_km);
    			setNextTrip();
    			prepareNextActivity(timeVariables, chargePointRegistration);
    			if (timeSinceWeekStart_h >= tripRecords.get(eventIndex).startTime_h() && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < tripRecords.get(eventIndex).startTime_h() ) { // is the next trip also starting this timestep?
    				currentTripTimesteps_n = max(1,roundToInt(((tripRecords.get(eventIndex).endTime_h() - tripRecords.get(eventIndex).startTime_h()) / timeParameters.getTimeStep_h())));
    				vehicle.startTrip(timeVariables);
    				if(vehicle instanceof J_EAEV EV) {
    					chargePointRegistration.deregisterChargingRequest(EV);
    				}
    			}
    		}
    	}
    }
   
    /*
     * This method 'Forwards' to the activity at time in timeVariables
     * It also immediately calls prepareNextActivity
     */
    public void setStartIndex(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
    	double time_h = timeVariables.getT_h();
    	double timeSinceWeekStart_h = getTimeSinceWeekStart_h(time_h);
    	boolean looped = false;
    	while ( tripRecords.get(eventIndex).startTime_h() < (timeSinceWeekStart_h ) ) { // If this occurs 'during' a trip, that trip is ignored, it is not executed.
    		setNextTrip(); // Skip to the next trip.
    		
    		if (eventIndex == tripRecords.size()-1 ) { 
    			if (looped) {
    				setNextTrip(); // Increments eventIndex, reverts to first trip of week when 'overflowing'
    				break;
    			} else {
    				looped = true;
    			}
    		}
    	}
    	prepareNextActivity(timeVariables, chargePointRegistration);    	
    }
   
    /*
     * This method is called after the previous trip ended.
     * It calculates the time to and distance of the coming trip.
     * If the vehicle is an EV it also calculates the charging need, including possible future trips.
     * The function passes this information to the EV and registers the charging request at the chargepoint.
     */
    public void prepareNextActivity(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
    	double time_h = timeVariables.getT_h();
    	
    	// Trip start/end-times are all defined as hours since monday 00:00h
    	double timeSinceWeekStart_h = getTimeSinceWeekStart_h(time_h);
    	nextEventStartTime_h = tripRecords.get(eventIndex).startTime_h();
    	
    	if (eventIndex == 0 && timeSinceWeekStart_h > nextEventStartTime_h) { // Next week's trip!
    		nextEventStartTime_h = (nextEventStartTime_h + time_h - timeSinceWeekStart_h) + 168;
    	} else {
    		nextEventStartTime_h = (nextEventStartTime_h + time_h - timeSinceWeekStart_h);
    	}
    	idleTimeToNextTrip_h = (nextEventStartTime_h - timeSinceWeekStart_h) % (24*7); // Modulo 24*7 needed because otherwise negative values can occur when trip starts 'next week'.
    	tripDistance_km = distanceScaling_fr * tripRecords.get(eventIndex).distance_km(); // Update upcoming trip distance
    	
    	if (vehicle instanceof J_EAEV ev) {
    		
    		double energyNeedForNextTrip_kWh = ev.getEnergyConsumption_kWhpkm() * tripDistance_km;
    		if (idleTimeToNextTrip_h > 0 && (energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh())> idleTimeToNextTrip_h * ev.getVehicleChargingCapacity_kW()) {
    			traceln("TripTracker reports: charging need for next trip is not feasible! Time till next trip: %s hours, chargeNeed_kWh: %s", roundToDecimal(idleTimeToNextTrip_h,2), roundToDecimal(energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh(),2));
    		}
    		// Check if more charging is needed for next trip!
    		double nextTripDist_km = 0;
    		double nextTripStartTime_h = 0;
    		
    		if ( eventIndex == tripRecords.size() - 1 ) {
    			nextTripDist_km = tripRecords.get(0).distance_km();
    			nextTripStartTime_h = tripRecords.get(0).endTime_h();
    		} else {		
    			nextTripDist_km = distanceScaling_fr*tripRecords.get(eventIndex+1).distance_km();
    			nextTripStartTime_h = tripRecords.get(eventIndex+1).startTime_h();
    		}
    		double additionalChargingNeededForNextTrip_kWh = max(0,nextTripDist_km * ev.getEnergyConsumption_kWhpkm() - (nextTripStartTime_h - tripRecords.get(eventIndex).endTime_h())*ev.getVehicleChargingCapacity_kW());
    		
    		energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
    		energyNeedForNextTrip_kWh = min(energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());
    		ev.setEnergyNeedForNextTrip_kWh(energyNeedForNextTrip_kWh);
    		
    		//Register EV at the chargepoint
    		chargePointRegistration.registerChargingRequest(ev);
    	}
    }
   
    /*
     * This method returns a list of trips,
     * The included trips are those that have a startTime within the range of [startTime_h, endTime_h)
     * For each of these start times the TripRecord includes the distance for that upcoming trip, which the J_EAEV can translate into required energy.
     * The times in the TripRecords are 'absolute', i.e. relative to the first of January.
     */
    public List<TripRecord> getTripsInRange( double startTime_h, double endTime_h ) {
    	List<TripRecord> trips = new ArrayList<>();
    	if (endTime_h < startTime_h) {
    		// Could return an empty list instead, but prefer to throw an exception to hint at something going wrong.
    		throw new RuntimeException(String.format("Could not get trips in range [ %s, %s ), since startTime is after endTime_h", startTime_h, endTime_h));
    	}
    	int startWeek = this.timeParameters.getWeekIndex(startTime_h);
    	int endWeek   = this.timeParameters.getWeekIndex(endTime_h);
    	for (int week = startWeek; week <= endWeek; week++) {
    		double weekStartTime_h = this.timeParameters.getWeekStart_h(week);
    		for (TripRecord trip : tripRecords) {
    			double absStart = weekStartTime_h + trip.startTime_h();
    			double absEnd   = weekStartTime_h + trip.endTime_h();
    			if (absStart >= startTime_h && absStart < endTime_h) {
    				trips.add(new TripRecord(absStart, absEnd, trip.distance_km()));
    			}
    		}
    	}
    	return trips;
    }
    
    //Setters
    public void setVehicle(I_Vehicle vehicle) {
    	this.vehicle = vehicle;
    }
    public void setDistanceScaling_fr(double distanceScaling_fr) {
    	this.distanceScaling_fr = distanceScaling_fr;
    }
    public void setAnnualDistance_km(double desiredAnnualDistance_km) { // Scale trips to come to a certain total annual distance traveled. This can lead to unfeasibly long trips for EVs!!
    	double currentAnnualDistance_km = getAnnualDistance_km();
    	double scalingFactor_f = desiredAnnualDistance_km / currentAnnualDistance_km;
    	
    	ListIterator<TripRecord> iterator = tripRecords.listIterator();
    	while (iterator.hasNext()) {
	        TripRecord record = iterator.next();
	        iterator.set(new TripRecord(
	            record.startTime_h(),
	            record.endTime_h(),
	            record.distance_km() * scalingFactor_f
	        ));
	    }
    }
    //Getters
    public I_Vehicle getVehicle() {
    	return this.vehicle;
    }
    public double getNextEventStartTime_h() {
    	return nextEventStartTime_h;
    }
    
    public double getCurrentTripEndTime_h() {
    	return this.tripRecords.get(eventIndex).endTime_h();
    }
    
    public double getDistanceScaling_fr( ) {
    	return this.distanceScaling_fr;
    }
    public double getAnnualDistance_km() {
    	double currentAnnualDistance_km = 52 * tripRecords.stream().mapToDouble(TripRecord::distance_km).sum(); // assumed trip-data is one week long!! Hence the 52, for 52 weeks in a year.
    	return currentAnnualDistance_km;
    }
    
    public static record TripRecord(double startTime_h, double endTime_h, double distance_km) {}
    
    
    @Override
    public void storeStatesAndReset() {
    	eventIndexStored = eventIndex;
    	nextEventStartTimeStored_h = nextEventStartTime_h;
    	idleTimeToNextTripStored_h = idleTimeToNextTrip_h;
    	idleTimeToNextTrip_h = 0;
    	// Don't forget to call setStartIndex !
    }
    
	@Override
	public void restoreStates() {
		eventIndex = eventIndexStored;
		nextEventStartTime_h = nextEventStartTimeStored_h;
		idleTimeToNextTrip_h = idleTimeToNextTripStored_h;
		tripDistance_km = distanceScaling_fr * tripRecords.get(eventIndex).distance_km(); // Update upcoming trip distance
	}
	
	@Override
	public String toString() {
		String outputString =  "J_ActivityTrackerTrips: \n";
		outputString += "Based on " + (CSVRowIndex != null ? "CSV data with row index: " + CSVRowIndex : "Custom input") + "\n";
		outputString += "Distance Scaling = " + this.distanceScaling_fr + " ";		
		return outputString;
	}
}