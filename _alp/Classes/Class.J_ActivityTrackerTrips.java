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
	public double idleTimeToNextTrip_min;
	public double idleTimeToNextTripStored_min;
	public double tripDistance_km;
	public double nextEventStartTime_min;
	public double distanceScaling_fr = 1.0;
	public double currentTripTimesteps_n;
	private double nextEventStartTime_h;
	
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
    	
       	for (int i = 0; i < nbActivities; i++){
    		starttimes_min.add(tripsCsv.readDouble());
    		endtimes_min.add(tripsCsv.readDouble());
    		distances_km.add(tripsCsv.readDouble());
    	}

      	// 'forward' to next activity
       	setStartIndex(timeVariables, chargePointRegistration);    
   }
   
   private double getTimeSinceWeekStart(double time_min) {
	   double timeSinceWeekStart_min = (time_min + (timeParameters.getDayOfWeek1jan()-1) * 24 * 60) % (7*24*60);
	   return timeSinceWeekStart_min;
   }
    
   private void setNextTrip() {
	   eventIndex++;
	   if ( eventIndex > starttimes_min.size() - 1 ) {	
	   		eventIndex = 0;
	   }
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
    
   /*
    * Main method that is called every timestep.
    * The function checks if a trip is starting this timestep. If so, it calls startTrip on the vehicle. 
    * It also immediately checks in the trip ends in that same timestep.
    * If the vehicle is already on a trip it progresses the trip to check if the trip ends this timestep.
    * If so, it also checks if a new trip is starting in that same timestep.
    */
   public void manageActivities(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
	   double time_min = timeVariables.getT_h() * 60;
	   double timeSinceWeekStart_min = getTimeSinceWeekStart(time_min); //  Trip start/end-times are all defined as minutes since monday 00:00h, trips are looped indefinitely
	   if (vehicle.getAvailability()) { // at start of timestep! check for multiple 'events' in timestep!
		   if ( timeSinceWeekStart_min >= starttimes_min.get(eventIndex) && (timeSinceWeekStart_min-timeParameters.getTimeStep_h() * 60) < starttimes_min.get(eventIndex)) { // is a trip starting this timestep?
			   currentTripTimesteps_n = max(1,roundToInt(((endtimes_min.get(eventIndex) - starttimes_min.get(eventIndex)) / (timeParameters.getTimeStep_h() * 60))));
			   
			   vehicle.startTrip(timeVariables);
			   if(vehicle instanceof J_EAEV EV) {
				   chargePointRegistration.deregisterChargingRequest(EV);
			   }
			   if (timeSinceWeekStart_min >= endtimes_min.get(eventIndex) && (timeSinceWeekStart_min-timeParameters.getTimeStep_h() * 60) < endtimes_min.get(eventIndex)) { // is the trip also ending this timestep?
				   vehicle.endTrip(tripDistance_km);
				   setNextTrip();
				   prepareNextActivity(time_min, chargePointRegistration);
			   }
		   }
	   } else {
		   if (vehicle instanceof J_EAFuelVehicle fuelVehicle) {
			   fuelVehicle.progressTrip(tripDistance_km / currentTripTimesteps_n);
		   }
		   if (timeSinceWeekStart_min >= endtimes_min.get(eventIndex) && (timeSinceWeekStart_min-timeParameters.getTimeStep_h() * 60) < endtimes_min.get(eventIndex)) { // is a trip ending this timestep?
			   vehicle.endTrip(tripDistance_km);
			   setNextTrip();
			   prepareNextActivity(time_min, chargePointRegistration);
			   if (timeSinceWeekStart_min >= starttimes_min.get(eventIndex) && (timeSinceWeekStart_min-timeParameters.getTimeStep_h() * 60) < starttimes_min.get(eventIndex) ) { // is the next trip also starting this timestep?
				   currentTripTimesteps_n = max(1,roundToInt(((endtimes_min.get(eventIndex) - starttimes_min.get(eventIndex)) / (timeParameters.getTimeStep_h() * 60))));
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
	   double time_min = timeVariables.getT_h() * 60.0;
	   double timeSinceWeekStart_min = getTimeSinceWeekStart(time_min);
	   boolean looped = false;
	   while ( starttimes_min.get(eventIndex) < (timeSinceWeekStart_min ) ) { // If this occurs 'during' a trip, that trip is ignored, it is not executed.
		   setNextTrip(); // Skip to the next trip.
		   
		   if (eventIndex == starttimes_min.size()-1 ) { 
			   if (looped) {
				   setNextTrip(); // Increments eventIndex, reverts to first trip of week when 'overflowing'
				   break;
			   } else {
				   looped = true;
			   }
		   }
		   
	   }
	   prepareNextActivity(time_min, chargePointRegistration);    	
   }
   
   /*
    * This method is called after the previous trip ended.
    * It calculates the time to and distance of the coming trip.
    * If the vehicle is an EV it also calculates the charging need, including possible future trips.
    * The function passes this information to the EV and registers the charging request at the chargepoint.
    */
   public void prepareNextActivity(double time_min, I_ChargePointRegistration chargePointRegistration) {
	   // Trip start/end-times are all defined as minutes since monday 00:00h
	   double timeSinceWeekStart_min = getTimeSinceWeekStart(time_min);
	   nextEventStartTime_min = starttimes_min.get(eventIndex);
	   
	   if (eventIndex == 0 && timeSinceWeekStart_min > nextEventStartTime_min) { // Next week's trip!
		   nextEventStartTime_h = (nextEventStartTime_min + time_min - timeSinceWeekStart_min)/60 + 168;
	   } else {
		   nextEventStartTime_h = (nextEventStartTime_min + time_min - timeSinceWeekStart_min)/60;
	   }
	   // traceln("Prepare next activity, trip startTime: %s hours. Time since week start: %s", nextEventStartTime_h, (timeSinceWeekStart_min)/60);
	   idleTimeToNextTrip_min = (nextEventStartTime_min - timeSinceWeekStart_min) % (24*7*60); // Modulo 24*7*60 needed because otherwise negative values can occur when trip starts 'next week'.
	   tripDistance_km = distanceScaling_fr * distances_km.get( eventIndex ); // Update upcoming trip distance
	   
	   if (vehicle instanceof J_EAEV ev) {
		   
		   double energyNeedForNextTrip_kWh = ev.getEnergyConsumption_kWhpkm() * tripDistance_km;
		   if (idleTimeToNextTrip_min > 0 && (energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh())> idleTimeToNextTrip_min/60 * ev.capacityElectric_kW) {
			   traceln("TripTracker reports: charging need for next trip is not feasible! Time till next trip: %s hours, chargeNeed_kWh: %s", roundToDecimal(idleTimeToNextTrip_min/60,2), roundToDecimal(energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh(),2));
		   }
		   //v_energyNeedForNextTrip_kWh = min(v_energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());  // added 10kWh margin 'just in case'. This is actually realistic; people will charge their cars a bit more than strictly needed for the next trip, if possible.
		   // Check if more charging is needed for next trip!
		   double nextTripDist_km = 0;
		   double nextTripStartTime_min = 0;
		   
		   if ( eventIndex == starttimes_min.size() - 1 ) {
			   nextTripDist_km = 0;//distances_km.get( 0 );
			   nextTripStartTime_min = endtimes_min.get(eventIndex);
		   } else {		
			   nextTripDist_km = distanceScaling_fr*distances_km.get( eventIndex+1 );
			   nextTripStartTime_min = starttimes_min.get( eventIndex+1 );
		   }
		   double additionalChargingNeededForNextTrip_kWh = max(0,nextTripDist_km * ev.getEnergyConsumption_kWhpkm() - (nextTripStartTime_min - endtimes_min.get(eventIndex))/60*ev.getVehicleChargingCapacity_kW());
		   
		   energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
		   energyNeedForNextTrip_kWh = min(energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());
		   //traceln("TripTracker, energyNeedForNextTrip: %s", v_energyNeedForNextTrip_kWh);
		   ev.setEnergyNeedForNextTrip_kWh(energyNeedForNextTrip_kWh);
		   /*if ( (v_energyNeedForNextTrip_kWh - EV.getCurrentStateOfCharge() * EV.getStorageCapacity_kWh()) / (idleTimeToNextTrip_min/60) > EV.capacityElectric_kW ) {
				traceln("Infeasible trip pattern for EV, not enough time to charge for next trip! Required charging power is: " + (v_energyNeedForNextTrip_kWh - EV.getCurrentStateOfCharge() * EV.getStorageCapacity_kWh()) / (idleTimeToNextTrip_min/60) + " kW");
				traceln("RowIndex: " + rowIndex + " tripDistance: " + tripDistance_km + " km, time to next trip: " + idleTimeToNextTrip_min + " minutes");
			} */
		   
		   //Register EV at the chargepoint
		   chargePointRegistration.registerChargingRequest(ev);
	   }
   }
    
    public double getNextEventStartTime_h() {
    	return nextEventStartTime_h;
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
    	eventIndexStored = eventIndex;
    	idleTimeToNextTripStored_min = idleTimeToNextTrip_min;
    	idleTimeToNextTrip_min = 0;
    	// Don't forget to call setStartIndex !
    }
	
    @Override
    public void restoreStates() {
    	eventIndex = eventIndexStored;
	    nextEventStartTime_min = starttimes_min.get(eventIndex);
		idleTimeToNextTrip_min = idleTimeToNextTripStored_min;
		tripDistance_km = distanceScaling_fr * distances_km.get( eventIndex ); // Update upcoming trip distance
	}
}
