/**
 * J_ChargingManagementPriceScheduled
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import zeroPackage.Market;
import zeroPackage.FlexConsumptionAsset;
import zeroPackage.FlexAssetScheduler;
import zeroPackage.ZeroMath;


@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_ChargingManagementPriceScheduled implements I_ChargingManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.PRICE_MARKET_FEEDBACK;
    private double marketFeedback_eurpMWhpkW = 40; // PLACEHOLDER VALUE!
    private boolean V2GActive = false; // This management does not support V2G
    
    /**
     * Internal class to manage the generated schedule for each charging request.
     * When a new charging request is registered it generates a schedule with the script in the ZeroMath (Holon) Package.
     * The schedule is added to this class and each timestep 'charge' is called to execute the schedule.
     * The class keeps track of time through the variable 'currentIdx'.
     * The boolean 'isFinished' is used  to remove the session after it is done.
     */
    private static class ActiveSession {
    	public final I_ChargingRequest chargingRequest;
        public double[] chargeProfile_kW;
        public boolean isFinished = false;
        private int currentIdx = 0;

        public ActiveSession(I_ChargingRequest chargingRequest, double[] chargeProfile_kW) {
            this.chargingRequest = chargingRequest;
            this.chargeProfile_kW = chargeProfile_kW;
        }
        
        public void charge(J_ChargePoint chargePoint, GridConnection gc, J_TimeVariables timeVariables) {
        	chargePoint.charge(this.chargingRequest, this.chargeProfile_kW[currentIdx], timeVariables, gc);	
        	currentIdx++;
        	if ( this.chargeProfile_kW.length == currentIdx) {
        		this.isFinished = true;
        	}
        }
    }
    
    // These two Lists should always be the same size and contain the same charging requests.
    private List<I_ChargingRequest> previousChargingRequests = new ArrayList<>(); // Charging requests that already have a schedule are stored here
    private List<ActiveSession> activeSessions = new ArrayList<>(); // For all those charging requests an ActiveSession class is instantiated
    
    // The stored versions are used to restore the live simulation state after a rapid run
    private List<I_ChargingRequest> previousChargingRequestsStored;    
    private List<ActiveSession> activeSessionsStored;
    
    /**
     * Default constructor
     */
    public J_ChargingManagementPriceScheduled( GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
        
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    
    public double getMarketFeedback_eurpMWhpkW() {
    	return marketFeedback_eurpMWhpkW;
    }
    
    public void setMarketFeedback_eurpMWhpkW(double marketFeedback_eurpMWhpkW) {
    	this.marketFeedback_eurpMWhpkW = marketFeedback_eurpMWhpkW;
    }
    /**
     * This charging strategy creates a charging schedule that is quasi-cost-optimal, and allows the inclusion of a market feedback mechanism to reduce excessive charging spikes during minimal price differences.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	double t_h = timeVariables.getT_h();
    	double timeStep_h = this.timeParameters.getTimeStep_h();
    	List<I_ChargingRequest> currentChargingRequests = chargePoint.getCurrentActiveChargingRequests();
       	for (I_ChargingRequest chargingRequest : currentChargingRequests) {
       		if (!previousChargingRequests.contains(chargingRequest)) { // Skip charging requests that already have a schedule
       			double duration_h = chargingRequest.getLeaveTime_h() - t_h;
       			if (duration_h <= 0) {
       				traceln("ChargingRequest starting after endtime! Duration_h: %s", duration_h);
       			}
       			int length = (int)ceil(duration_h/timeParameters.getTimeStep_h());
       			double chargeNeedForNextTrip_kWh = chargingRequest.getStorageCapacity_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
       			double maxChargePower_kW = chargePoint.getMaxChargingCapacity_kW(chargingRequest);
       			int indexAtCurrentTime = roundToInt(t_h/timeStep_h);
       			double[] priceCurve = Arrays.copyOfRange(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getAllValues(), indexAtCurrentTime, indexAtCurrentTime+length);
       			
       			// Create classes for the Holon scheduler
       			// The last three arguments in the Market class are zero which means there is no accounting for taxes or congestion
       			Market market = new Market(priceCurve, marketFeedback_eurpMWhpkW, 0, 0, 0);
       			FlexConsumptionAsset asset = new FlexConsumptionAsset(maxChargePower_kW, 20, timeStep_h, length*timeStep_h, null);
       			
       			// Cap the charging need between 0 and full charging for the entire duration
       			chargeNeedForNextTrip_kWh=max(0,min(chargeNeedForNextTrip_kWh, maxChargePower_kW * length * timeStep_h));
       			
       			// Create an array that will be filled with the charging schedule
       			double[] chargeProfile_kW = new double[length];
       			if (chargeNeedForNextTrip_kWh>0) {
       				chargeProfile_kW = FlexAssetScheduler.scheduleWrapper(chargeProfile_kW, asset, chargeNeedForNextTrip_kWh, market, timeParameters.getTimeStep_h(), false);
       			}
       			
       			previousChargingRequests.add(chargingRequest);
       			activeSessions.add(new ActiveSession(chargingRequest, chargeProfile_kW));       			
       		}
       	}
   		// Execute charging of active sessions
		for (ActiveSession session : activeSessions) {			
			if (!currentChargingRequests.contains(session.chargingRequest)) {
				traceln("Warning! ChargingSession was prematurely aborted! Ignore this warning if it occurs after changing EV-sliders during live-sim.");
				session.isFinished = true;
				previousChargingRequests.remove(session.chargingRequest);
			} else if (session.chargeProfile_kW.length > 0) {
				session.charge(chargePoint, gc, timeVariables);
				if (session.isFinished) {
					previousChargingRequests.remove(session.chargingRequest);
				}
			}			
		}
		
		// Finished session are removed, but this must be outside of for-loop
		activeSessions.removeIf(session -> session.isFinished); 
    }

    public void abortSession(I_ChargingRequest chargingRequest) {
    	previousChargingRequests.remove(chargingRequest);
    	ActiveSession session = findFirst(activeSessions, x -> x.chargingRequest == chargingRequest);
    	activeSessions.remove(session);
    }
    
	public void setV2GActive(boolean activateV2G) {
		throw new RuntimeException("ChargingManagementPriceScheduled does not support V2G charging!");
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
		previousChargingRequestsStored = previousChargingRequests;    
		activeSessionsStored = activeSessions;
	    
		previousChargingRequests = new ArrayList<>(); // Don't use clear()! It will also clear the 'stored' list; as it's not a copy, just a pointer to the same list!
	    activeSessions = new ArrayList<>();	 
	}
	public void restoreStates() {
		previousChargingRequests = previousChargingRequestsStored;
		activeSessions = activeSessionsStored;
	}
	
	
    @Override
 	public String toString() {
 		return "Active charging type: " + this.activeChargingType;

 	}
}
