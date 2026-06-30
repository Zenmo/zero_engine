/**
 * J_ChargingManagementSimple
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import zero_engine.J_ActivityTrackerTrips.TripRecord;

import java.util.EnumSet;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_ChargingManagementSimple implements I_ChargingManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.SIMPLE;
    private boolean V2GActive = false;

    /**
     * Default constructor
     */
    public J_ChargingManagementSimple( ) {
    
    }
    
    public J_ChargingManagementSimple( GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
      
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    
    /**
     * One of the simplest charging algorithms. Charges at full power untill the battery is full or the next trip starts.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	for (I_ChargingRequest chargingRequest : chargePoint.getCurrentActiveChargingRequests()) {
       		double duration_h = chargingRequest.getLeaveTime_h() - timeVariables.getT_h();
    		if (duration_h <= 0) {
   				traceln("ChargingRequest duration negative! leaveTime_h: %s, t_h %s", chargingRequest.getLeaveTime_h(), timeVariables.getT_h());
   			}		
    		chargePoint.charge(chargingRequest, chargePoint.getMaxChargingCapacity_kW(chargingRequest), timeVariables, gc);
    	}
    }
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		// TODO: Check if this works for (public) charging sessions aswell
		int timeStepsInForecast = roundToInt((timeOfIntervalEnd_h - timeOfIntervalStart_h) / this.timeParameters.getTimeStep_h());
		Double[] electricityLoad_kW = new Double[timeStepsInForecast];
		Arrays.fill(electricityLoad_kW, 0.0);
		
		for (J_EAEV ev : this.gc.c_electricVehicles) {
	    	double[] EVLoad_kW = new double[timeStepsInForecast];
			J_ActivityTrackerTrips tripTracker = ev.getTripTracker();
			List<TripRecord> trips = tripTracker.getTripsInRange(timeOfIntervalStart_h, timeOfIntervalEnd_h);
	    	
			double maximalStorageCapacity_kWh = ev.getStorageCapacity_kWh();
			double currentSOC_kWh = ev.getCurrentSOC_kWh(); // Assumes the SOC of the EV at start of forecast is current SOC
		    double work_kWh = maximalStorageCapacity_kWh - currentSOC_kWh;
	    	double endTimeLastTrip_h = ev.getAvailability() ? 0.0 : tripTracker.getCurrentTripEndTime_h()%24; // Assumes the current trip ends today, i.e. no trips > 24 hours exist)
    		double firstAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.ceil(endTimeLastTrip_h / this.timeParameters.getTimeStep_h());
    		double startTimeNextTrip_h = trips.size() > 0 ? trips.get(0).startTime_h() : timeOfIntervalEnd_h;
    		double lastAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.floor(startTimeNextTrip_h / this.timeParameters.getTimeStep_h());
    		double maxPower_kW = ev.getVehicleChargingCapacity_kW();
    		double maxWork_kWh = (lastAvailableChargingTime_h - firstAvailableChargingTime_h) * maxPower_kW;
    		double initialWork_kWh = max(0, min(work_kWh, maxWork_kWh));
    		double remainingWork_kWh = initialWork_kWh;
    		for (int i = 0; i < timeStepsInForecast; i++) {
    			double t = i*this.timeParameters.getTimeStep_h();
    			if (t >= firstAvailableChargingTime_h && t <= lastAvailableChargingTime_h) {  // >= or > ?
    				EVLoad_kW[i] = max(0, min(maxPower_kW, remainingWork_kWh / this.timeParameters.getTimeStep_h()));
    				remainingWork_kWh -= EVLoad_kW[i] * this.timeParameters.getTimeStep_h();
    				if (remainingWork_kWh == 0 || DoubleCompare.lessThanZero(remainingWork_kWh)) {
    					break;
    				}
    			}
    		}
    		currentSOC_kWh += initialWork_kWh - remainingWork_kWh;
		    
	    	for (int tripIndex = 0; tripIndex < trips.size(); tripIndex++) {
	    		TripRecord trip = trips.get(tripIndex);
	    		double distance_km = trip.distance_km();
	    		currentSOC_kWh -= distance_km * ev.getEnergyConsumption_kWhpkm();
	    		work_kWh = maximalStorageCapacity_kWh - currentSOC_kWh;    		
	    		endTimeLastTrip_h = trip.endTime_h();
	    		firstAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.ceil(endTimeLastTrip_h / this.timeParameters.getTimeStep_h());
	    		startTimeNextTrip_h = (tripIndex == trips.size() - 1) ? timeOfIntervalEnd_h : trips.get(tripIndex+1).startTime_h();
	    		lastAvailableChargingTime_h = this.timeParameters.getTimeStep_h() * Math.floor(startTimeNextTrip_h / this.timeParameters.getTimeStep_h());
	    		maxWork_kWh = (lastAvailableChargingTime_h - firstAvailableChargingTime_h) * maxPower_kW;
	    		initialWork_kWh = max(0, min(work_kWh, maxWork_kWh));
	    		remainingWork_kWh = initialWork_kWh;
	    		for (int i = 0; i < timeStepsInForecast; i++) {
	    			double t = i*this.timeParameters.getTimeStep_h();
	    			if (t >= firstAvailableChargingTime_h && t <= lastAvailableChargingTime_h) {  // >= or > ?
	    				EVLoad_kW[i] = max(0, min(maxPower_kW, remainingWork_kWh / this.timeParameters.getTimeStep_h()));
	    				remainingWork_kWh -= EVLoad_kW[i] * this.timeParameters.getTimeStep_h();
	    				if (remainingWork_kWh == 0 || DoubleCompare.lessThanZero(remainingWork_kWh)) {
	    					break;
	    				}
	    			}
	    		}
	    		currentSOC_kWh += initialWork_kWh - remainingWork_kWh;
	    	}
			for (int i = 0; i < timeStepsInForecast; i++) {
				electricityLoad_kW[i] += EVLoad_kW[i];
			}
		}
		
		OL_ForecastStatus status = OL_ForecastStatus.ESTIMATED_FORECAST;
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		loadMap.put(OL_EnergyCarriers.ELECTRICITY, electricityLoad_kW);
		String reason = "Forecast is not perfect as EV starting SOC is guessed. If forecast starttime is the current modeltime forecast is perfect.";
		return new J_AssetTypeForecast(I_ChargingManagement.class, loadMap, status, reason);
	}
    
	public void setV2GActive(boolean activateV2G) {
		if(activateV2G) {
			throw new RuntimeException("Trying to Activate V2G for chargingManagement Simple -> Not supported");
		}
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
		
	}
	public void restoreStates() {
		
	}
	
	
    @Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}