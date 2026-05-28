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
	
	private ArrayList<Double> distances_km = new ArrayList<>();
	private Integer CSVRowIndex = null;
	private I_Vehicle vehicle;
	private double idleTimeToNextTrip_h;
	private double idleTimeToNextTripStored_h;
	private double tripDistance_km;
	private double distanceScaling_fr = 1.0;
	private double currentTripTimesteps_n;
	private double nextEventStartTime_h;
	
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
    		startTimes_h.add(tripsCSV.readDouble()/60.0); // Data is in min -> convert to hours
    		endTimes_h.add(tripsCSV.readDouble()/60.0); // Data is in min -> convert to hours
    		distances_km.add(tripsCSV.readDouble());
    	}

      	// 'forward' to next activity
       	setStartIndex(timeVariables, chargePointRegistration);    
   }
   
    /**
     * Constructor using defined trips as input
     */
    public J_ActivityTrackerTrips(List<Double> startTimes_h, List<Double> endTimes_h, List<Double> distances_km, I_Vehicle vehicle, I_ChargePointRegistration chargePointRegistration, J_TimeParameters timeParameters, J_TimeVariables timeVariables) {
      	this.timeParameters = timeParameters;
    	this.vehicle = vehicle;
    	
    	//Check
    	if(startTimes_h.size() != endTimes_h.size() && startTimes_h.size() != distances_km.size()) {
    		throw new RuntimeException("Trying to create a custom J_ActivityTrackerTrips, with unequal amount of startTimes (" + startTimes_h.size() + "), endTimes (" + endTimes_h.size()+ "), and/or distances_km (" + distances_km.size() + ").");
    	}
    	
		this.startTimes_h.addAll(startTimes_h);
		this.endTimes_h.addAll(endTimes_h);
		this.distances_km.addAll(distances_km);

      	// 'forward' to next activity
       	setStartIndex(timeVariables, chargePointRegistration);    
   }
    
   private double getTimeSinceWeekStart_h(double time_h) {
	   double timeSinceWeekStart_h = (time_h + (timeParameters.getDayOfWeek1jan()-1) * 24) % (7*24);
	   return timeSinceWeekStart_h;
   }
    
   private void setNextTrip() {
	   eventIndex++;
	   if ( eventIndex > startTimes_h.size() - 1 ) {	
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
		   if ( timeSinceWeekStart_h >= startTimes_h.get(eventIndex) && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < startTimes_h.get(eventIndex)) { // is a trip starting this timestep?
			   currentTripTimesteps_n = max(1,roundToInt(((endTimes_h.get(eventIndex) - startTimes_h.get(eventIndex)) / (timeParameters.getTimeStep_h()))));
			   
			   vehicle.startTrip(timeVariables);
			   if(vehicle instanceof J_EAEV EV) {
				   chargePointRegistration.deregisterChargingRequest(EV);
			   }
			   if (timeSinceWeekStart_h >= endTimes_h.get(eventIndex) && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < endTimes_h.get(eventIndex)) { // is the trip also ending this timestep?
				   vehicle.endTrip(tripDistance_km);
				   setNextTrip();
				   prepareNextActivity(time_h, chargePointRegistration);
			   }
		   }
	   } else {
		   if (vehicle instanceof J_EAFuelVehicle fuelVehicle) {
			   fuelVehicle.progressTrip(tripDistance_km / currentTripTimesteps_n);
		   }
		   if (timeSinceWeekStart_h >= endTimes_h.get(eventIndex) && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < endTimes_h.get(eventIndex)) { // is a trip ending this timestep?
			   vehicle.endTrip(tripDistance_km);
			   setNextTrip();
			   prepareNextActivity(time_h, chargePointRegistration);
			   if (timeSinceWeekStart_h >= startTimes_h.get(eventIndex) && (timeSinceWeekStart_h-timeParameters.getTimeStep_h()) < startTimes_h.get(eventIndex) ) { // is the next trip also starting this timestep?
				   currentTripTimesteps_n = max(1,roundToInt(((endTimes_h.get(eventIndex) - startTimes_h.get(eventIndex)) / timeParameters.getTimeStep_h())));
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
	   while ( startTimes_h.get(eventIndex) < (timeSinceWeekStart_h ) ) { // If this occurs 'during' a trip, that trip is ignored, it is not executed.
		   setNextTrip(); // Skip to the next trip.
		   
		   if (eventIndex == startTimes_h.size()-1 ) { 
			   if (looped) {
				   setNextTrip(); // Increments eventIndex, reverts to first trip of week when 'overflowing'
				   break;
			   } else {
				   looped = true;
			   }
		   }
	   }
	   prepareNextActivity(time_h, chargePointRegistration);    	
   }
   
   /*
    * This method is called after the previous trip ended.
    * It calculates the time to and distance of the coming trip.
    * If the vehicle is an EV it also calculates the charging need, including possible future trips.
    * The function passes this information to the EV and registers the charging request at the chargepoint.
    */
   public void prepareNextActivity(double time_h, I_ChargePointRegistration chargePointRegistration) {
	   // Trip start/end-times are all defined as minutes since monday 00:00h
	   double timeSinceWeekStart_h = getTimeSinceWeekStart_h(time_h);
	   nextEventStartTime_h = startTimes_h.get(eventIndex);
	   
	   if (eventIndex == 0 && timeSinceWeekStart_h > nextEventStartTime_h) { // Next week's trip!
		   nextEventStartTime_h = (nextEventStartTime_h + time_h - timeSinceWeekStart_h) + 168;
	   } else {
		   nextEventStartTime_h = (nextEventStartTime_h + time_h - timeSinceWeekStart_h);
	   }
	   idleTimeToNextTrip_h = (nextEventStartTime_h - timeSinceWeekStart_h) % (24*7); // Modulo 24*7 needed because otherwise negative values can occur when trip starts 'next week'.
	   tripDistance_km = distanceScaling_fr * distances_km.get( eventIndex ); // Update upcoming trip distance
	   
	   if (vehicle instanceof J_EAEV ev) {
		   
		   double energyNeedForNextTrip_kWh = ev.getEnergyConsumption_kWhpkm() * tripDistance_km;
		   if (idleTimeToNextTrip_h > 0 && (energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh())> idleTimeToNextTrip_h * ev.getVehicleChargingCapacity_kW()) {
			   traceln("TripTracker reports: charging need for next trip is not feasible! Time till next trip: %s hours, chargeNeed_kWh: %s", roundToDecimal(idleTimeToNextTrip_h,2), roundToDecimal(energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh(),2));
		   }
		   // Check if more charging is needed for next trip!
		   double nextTripDist_km = 0;
		   double nextTripStartTime_h = 0;
		   
		   if ( eventIndex == startTimes_h.size() - 1 ) {
			   nextTripDist_km = distances_km.get( 0 );
			   nextTripStartTime_h = endTimes_h.get( 0 );
		   } else {		
			   nextTripDist_km = distanceScaling_fr*distances_km.get( eventIndex+1 );
			   nextTripStartTime_h = startTimes_h.get( eventIndex+1 );
		   }
		   double additionalChargingNeededForNextTrip_kWh = max(0,nextTripDist_km * ev.getEnergyConsumption_kWhpkm() - (nextTripStartTime_h - endTimes_h.get(eventIndex))*ev.getVehicleChargingCapacity_kW());
		   
		   energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
		   energyNeedForNextTrip_kWh = min(energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());
		   ev.setEnergyNeedForNextTrip_kWh(energyNeedForNextTrip_kWh);
	   
		   //Register EV at the chargepoint
		   chargePointRegistration.registerChargingRequest(ev);
	   }
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
	   
       ListIterator<Double> iterator = distances_km.listIterator();                                                              
	   for (int i = 0; i<distances_km.size(); i++) {
		     iterator.next();
		     iterator.set(distances_km.get(i)*scalingFactor_f);
	   }
   }
   //Getters
   public I_Vehicle getVehicle() {
	   return this.vehicle;
   }
   public double getNextEventStartTime_h() {
	   return nextEventStartTime_h;
   }
   
   public double getDistanceScaling_fr( ) {
	   return this.distanceScaling_fr;
   }
   public double getAnnualDistance_km() {
	   double currentAnnualDistance_km = 52 * distances_km.stream().mapToDouble(a -> a).sum(); // assumed trip-data is one week long!! Hence the 52, for 52 weeks in a year.
	   return currentAnnualDistance_km;
   }
   

   @Override
   public void storeStatesAndReset() {
	   eventIndexStored = eventIndex;
	   idleTimeToNextTripStored_h = idleTimeToNextTrip_h;
	   idleTimeToNextTrip_h = 0;
	   // Don't forget to call setStartIndex !
   }

   @Override
   public void restoreStates() {
	   eventIndex = eventIndexStored;
	   nextEventStartTime_h = startTimes_h.get(eventIndex);
	   idleTimeToNextTrip_h = idleTimeToNextTripStored_h;
	   tripDistance_km = distanceScaling_fr * distances_km.get( eventIndex ); // Update upcoming trip distance
   }

   @Override
   public String toString() {
	   String outputString =  "J_ActivityTrackerTrips: \n";
	   outputString += "Based on " + (CSVRowIndex != null ? "CSV data with row index: " + CSVRowIndex : "Custom input") + "\n";
	   outputString += "Distance Scaling = " + this.distanceScaling_fr + " ";		
	   return outputString;
   }
}