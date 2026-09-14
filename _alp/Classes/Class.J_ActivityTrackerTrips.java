/**
 * J_ActivityTrackerTrips
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.tuple.Triple;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

/**
 * Drives a vehicle through its trips and tells the charge point when the vehicle needs charge.
 * <p>
 * Trips live on one absolute timeline: every time is in hours since the start of the simulation,
 * the same clock as J_TimeVariables. The trip CSVs describe a single week that used to be looped
 * with a modulo, which ruled out any pattern that does not repeat weekly. ESDL files state a
 * whole year of charging sessions, so instead of looping, a week of CSV trips is laid out over
 * the run once, up front. Everything downstream then works the same for both sources.
 */
public class J_ActivityTrackerTrips extends J_ActivityTracker {

	private static final double hoursPerDay = 24;

	private static final double hoursPerWeek = 7 * hoursPerDay;

	private static final double hoursPerYear = 8760;

	private J_TimeParameters timeParameters;

	/**
	 * Every trip of the run, ordered by start time.
	 */
	private List<TripRecord> trips = new ArrayList<>();

	/**
	 * The period the trips cover. Needed to express their total as a yearly distance, because
	 * a whole number of weeks does not land exactly on a year.
	 */
	private double tripsCoverDuration_h = hoursPerYear;

	/**
	 * The row of the trip CSV the trips were read from, or null when they came from elsewhere.
	 */
	private Integer rowIndex = null;

	public I_Vehicle vehicle;

	public double distanceScaling_fr = 1.0;

	private double nextEventStartTime_h;
	private double idleTimeToNextTrip_h;
	private double tripDistance_km;
	private double currentTripTimesteps_n;

	//Stored variables
	private double idleTimeToNextTripStored_h;

    /**
     * Default constructor
     */
    public J_ActivityTrackerTrips() {
    }

    /**
     * Read one week of trips from a trip CSV and repeat it over the run.
     */
    public J_ActivityTrackerTrips(J_TimeParameters timeParameters, TextFile tripsCsv, int rowIndex, J_TimeVariables timeVariables, I_Vehicle vehicle, I_ChargePointRegistration chargePointRegistration) {
    	this.timeParameters = timeParameters;
    	this.rowIndex = rowIndex;
    	this.vehicle = vehicle;

    	List<TripRecord> tripsOfOneWeek = readTripsOfOneWeek(tripsCsv, rowIndex);
    	this.nbActivities = tripsOfOneWeek.size();
    	repeatWeeklyTripsOverTheRun(tripsOfOneWeek);

      	// 'forward' to next activity
       	setStartIndex(timeVariables, chargePointRegistration);
   }

    /**
     * Trips that do not repeat, such as the charging sessions of an ESDL file.
     * <p>
     * The vehicle and the starting position are filled in when the tracker is connected to a
     * grid connection, because neither is known while the trips are being read.
     */
    public J_ActivityTrackerTrips(J_TimeParameters timeParameters, List<TripRecord> trips) {
    	this.timeParameters = timeParameters;
    	this.trips = new ArrayList<>(trips);
    	this.nbActivities = this.trips.size();
    	sortTripsByStartTime();
   }

   private List<TripRecord> readTripsOfOneWeek(TextFile tripsCsv, int rowIndex) {
    	tripsCsv.close();
    	tripsCsv.canReadMore();
		tripsCsv.readLine(); // Skips first line

    	while (roundToInt(tripsCsv.readDouble())!=rowIndex && tripsCsv.canReadMore()) { // Skip until rowIndex found
    		tripsCsv.readLine();
    	}
    	int numberOfTrips = tripsCsv.readInt();

    	List<TripRecord> tripsOfOneWeek = new ArrayList<>();
       	for (int i = 0; i < numberOfTrips; i++){
    		double startTime_h = tripsCsv.readDouble() / 60.0;
    		double endTime_h = tripsCsv.readDouble() / 60.0;
    		double distance_km = tripsCsv.readDouble();
    		tripsOfOneWeek.add(new TripRecord(startTime_h, endTime_h, distance_km));
    	}
    	return tripsOfOneWeek;
   }

   /**
    * Lay a week of trips, measured from Monday 00:00, over the whole run.
    * <p>
    * Hour 0 of the simulation is the 1st of January, which is not a Monday, so the first week
    * starts before the simulation does. The trips in it that fall before hour 0 simply never
    * come up, the same trips the modulo used to skip.
    */
   private void repeatWeeklyTripsOverTheRun(List<TripRecord> tripsOfOneWeek) {
	   double firstWeekStart_h = -(timeParameters.getDayOfWeek1jan() - 1) * hoursPerDay;
	   double runEnd_h = timeParameters.getRunEndTime_h();

	   this.trips = new ArrayList<>();
	   int numberOfWeeks = 0;
	   for (double weekStart_h = firstWeekStart_h; weekStart_h < runEnd_h; weekStart_h += hoursPerWeek) {
		   for (TripRecord tripOfTheWeek : tripsOfOneWeek) {
			   this.trips.add(new TripRecord(
					   weekStart_h + tripOfTheWeek.startTime_h(),
					   weekStart_h + tripOfTheWeek.endTime_h(),
					   tripOfTheWeek.distance_km()));
		   }
		   numberOfWeeks++;
	   }

	   this.tripsCoverDuration_h = numberOfWeeks * hoursPerWeek;
	   sortTripsByStartTime();
   }

   private void sortTripsByStartTime() {
	   this.trips.sort(Comparator.comparingDouble(TripRecord::startTime_h));
   }

   /**
    * True once the last trip has been driven. The vehicle then stays where it is.
    */
   private boolean hasNoTripLeft() {
	   return eventIndex >= trips.size();
   }

   private TripRecord currentTrip() {
	   return trips.get(eventIndex);
   }

   private void setNextTrip() {
	   eventIndex++;
   }

   /**
    * Index of the first trip that has not started yet at the given time.
    * <p>
    * Binary search rather than a scan, because a year of trips is a long list to walk and
    * this runs on every rapid run reset.
    */
   private int findFirstTripStartingAtOrAfter(double time_h) {
	   int low = 0;
	   int high = trips.size();
	   while (low < high) {
		   int middle = (low + high) >>> 1;
		   if (trips.get(middle).startTime_h() < time_h) {
			   low = middle + 1;
		   } else {
			   high = middle;
		   }
	   }
	   return low;
   }

   private boolean startsInThisTimestep(TripRecord trip, double time_h, double previousTime_h) {
	   return time_h >= trip.startTime_h() && previousTime_h < trip.startTime_h();
   }

   private boolean endsInThisTimestep(TripRecord trip, double time_h, double previousTime_h) {
	   return time_h >= trip.endTime_h() && previousTime_h < trip.endTime_h();
   }

   /*
    * Main method that is called every timestep.
    * The function checks if a trip is starting this timestep. If so, it calls startTrip on the vehicle.
    * It also immediately checks in the trip ends in that same timestep.
    * If the vehicle is already on a trip it progresses the trip to check if the trip ends this timestep.
    * If so, it also checks if a new trip is starting in that same timestep.
    */
   public void manageActivities(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
	   if (hasNoTripLeft()) {
		   return;
	   }

	   double time_h = timeVariables.getT_h();
	   double previousTime_h = time_h - timeParameters.getTimeStep_h();

	   if (vehicle.getAvailability()) { // at start of timestep! check for multiple 'events' in timestep!
		   if (startsInThisTimestep(currentTrip(), time_h, previousTime_h)) {
			   beginTrip(timeVariables, chargePointRegistration);
			   if (!hasNoTripLeft() && endsInThisTimestep(currentTrip(), time_h, previousTime_h)) { // is the trip also ending this timestep?
				   finishTrip(time_h, chargePointRegistration);
			   }
		   }
	   } else {
		   if (vehicle instanceof J_EAFuelVehicle fuelVehicle) {
			   fuelVehicle.progressTrip(tripDistance_km / currentTripTimesteps_n);
		   }
		   if (endsInThisTimestep(currentTrip(), time_h, previousTime_h)) { // is a trip ending this timestep?
			   finishTrip(time_h, chargePointRegistration);
			   if (!hasNoTripLeft() && startsInThisTimestep(currentTrip(), time_h, previousTime_h)) { // is the next trip also starting this timestep?
				   beginTrip(timeVariables, chargePointRegistration);
			   }
		   }
	   }
   }

   private void beginTrip(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
	   TripRecord trip = currentTrip();
	   currentTripTimesteps_n = max(1, roundToInt((trip.endTime_h() - trip.startTime_h()) / timeParameters.getTimeStep_h()));

	   vehicle.startTrip(timeVariables);
	   if (vehicle instanceof J_EAEV EV) {
		   chargePointRegistration.deregisterChargingRequest(EV);
	   }
   }

   private void finishTrip(double time_h, I_ChargePointRegistration chargePointRegistration) {
	   vehicle.endTrip(tripDistance_km);
	   setNextTrip();
	   prepareNextActivity(time_h, chargePointRegistration);
   }

   /*
    * This method 'Forwards' to the activity at time in timeVariables
    * It also immediately calls prepareNextActivity
    */
   public void setStartIndex(J_TimeVariables timeVariables, I_ChargePointRegistration chargePointRegistration) {
	   double time_h = timeVariables.getT_h();
	   // A trip that is already underway is skipped, it is not executed.
	   eventIndex = findFirstTripStartingAtOrAfter(time_h);
	   prepareNextActivity(time_h, chargePointRegistration);
   }

   /*
    * This method is called after the previous trip ended.
    * It calculates the time to and distance of the coming trip.
    * If the vehicle is an EV it also calculates the charging need, including possible future trips.
    * The function passes this information to the EV and registers the charging request at the chargepoint.
    */
   public void prepareNextActivity(double time_h, I_ChargePointRegistration chargePointRegistration) {
	   if (hasNoTripLeft()) {
		   // The vehicle has driven its last trip and stays available from here on.
		   nextEventStartTime_h = timeParameters.getRunEndTime_h();
		   idleTimeToNextTrip_h = max(0, nextEventStartTime_h - time_h);
		   tripDistance_km = 0;
		   return;
	   }

	   TripRecord trip = currentTrip();
	   nextEventStartTime_h = trip.startTime_h();
	   idleTimeToNextTrip_h = max(0, nextEventStartTime_h - time_h);
	   tripDistance_km = distanceScaling_fr * trip.distance_km(); // Update upcoming trip distance

	   if (vehicle instanceof J_EAEV ev) {

		   double energyNeedForNextTrip_kWh = ev.getEnergyConsumption_kWhpkm() * tripDistance_km;
		   if (idleTimeToNextTrip_h > 0 && (energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh())> idleTimeToNextTrip_h * ev.getVehicleChargingCapacity_kW()) {
			   traceln("TripTracker reports: charging need for next trip is not feasible! Time till next trip: %s hours, chargeNeed_kWh: %s", roundToDecimal(idleTimeToNextTrip_h,2), roundToDecimal(energyNeedForNextTrip_kWh-ev.getCurrentSOC_kWh(),2));
		   }

		   // Check if more charging is needed for the trip after this one!
		   double followingTripDistance_km = 0;
		   double followingTripStartTime_h = trip.endTime_h();
		   if (eventIndex + 1 < trips.size()) {
			   TripRecord followingTrip = trips.get(eventIndex + 1);
			   followingTripDistance_km = distanceScaling_fr * followingTrip.distance_km();
			   followingTripStartTime_h = followingTrip.startTime_h();
		   }
		   double additionalChargingNeededForNextTrip_kWh = max(0, followingTripDistance_km * ev.getEnergyConsumption_kWhpkm() - (followingTripStartTime_h - trip.endTime_h()) * ev.getVehicleChargingCapacity_kW());

		   energyNeedForNextTrip_kWh += additionalChargingNeededForNextTrip_kWh;
		   energyNeedForNextTrip_kWh = min(energyNeedForNextTrip_kWh+10,ev.getStorageCapacity_kWh());
		   ev.setEnergyNeedForNextTrip_kWh(energyNeedForNextTrip_kWh);

		   //Register EV at the chargepoint
		   chargePointRegistration.registerChargingRequest(ev);
	   }
   }

    public double getNextEventStartTime_h() {
    	return nextEventStartTime_h;
    }

    /**
     * The hour of the day at which the trip the vehicle is on right now ends.
     * <p>
     * The aggregator uses it to know when a car that is away comes back, assuming no trip lasts
     * longer than a day.
     */
    public double getEndTimeOfCurrentTripHourOfDay_h() {
    	if (hasNoTripLeft()) {
    		return 0.0;
    	}
    	return currentTrip().endTime_h() % hoursPerDay;
    }

    /*
     * This method returns a list of trips,
     * for each trip it lists the starttime, endtime (in hours) and the trip distance (in kms)
     * The starting and endtime are relative to the current time.
     */
    public List<Triple<Double, Double, Double>> getTripsNext24Hours( double timeAtStartForecast_h ) {
    	List<Triple<Double, Double, Double>> upcomingTrips = new ArrayList<>();

		if (hasNoTripLeft() || currentTrip().startTime_h() < timeAtStartForecast_h) {
			// The vehicle is away, the schedule for it is made without trips.
			return upcomingTrips;
		}

    	double cutoffTime_h = timeAtStartForecast_h + hoursPerDay;
    	for (int tripIndex = eventIndex; tripIndex < trips.size(); tripIndex++) {
    		TripRecord trip = trips.get(tripIndex);
    		if (trip.startTime_h() > cutoffTime_h) {
    			break;
    		}
    		upcomingTrips.add(Triple.of(
    				trip.startTime_h() - timeAtStartForecast_h,
    				trip.endTime_h() - timeAtStartForecast_h,
    				trip.distance_km()));
    	}

    	return upcomingTrips;
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

	   ListIterator<TripRecord> iterator = trips.listIterator();
	   while (iterator.hasNext()) {
		   TripRecord trip = iterator.next();
		   iterator.set(new TripRecord(trip.startTime_h(), trip.endTime_h(), trip.distance_km() * scalingFactor_f));
	   }
   }

   //Getters
   public I_Vehicle getVehicle() {
	   return this.vehicle;
   }

   public double getDistanceScaling_fr( ) {
	   return this.distanceScaling_fr;
   }

   /**
    * The trips no longer cover exactly a year, so their total is scaled to one.
    */
   public double getAnnualDistance_km() {
	   double totalDistance_km = trips.stream().mapToDouble(TripRecord::distance_km).sum();
	   return totalDistance_km * (hoursPerYear / tripsCoverDuration_h);
   }

   public List<TripRecord> getTrips() {
	   return List.copyOf(trips);
   }

   public static record TripRecord(double startTime_h, double endTime_h, double distance_km) {
   }

	@Override
	public String toString() {
		return
			"Number of trips = " + this.trips.size() + " " +
			"Based on " + (this.rowIndex != null ? "CSV data with row index: " + this.rowIndex : "custom input") + " " +
			"Distance Scaling = " + this.distanceScaling_fr + " ";
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
		idleTimeToNextTrip_h = idleTimeToNextTripStored_h;
		if (hasNoTripLeft()) {
			nextEventStartTime_h = timeParameters.getRunEndTime_h();
			tripDistance_km = 0;
			return;
		}
	    nextEventStartTime_h = currentTrip().startTime_h();
		tripDistance_km = distanceScaling_fr * currentTrip().distance_km(); // Update upcoming trip distance
	}
}
