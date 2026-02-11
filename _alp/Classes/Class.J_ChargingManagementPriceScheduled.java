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
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.PRICE;
    
    private boolean V2GActive = false;
    
    //Stored
    // The "Simple Wrapper" defined inside
    private static class ActiveSession {
    	public final I_ChargingRequest chargingRequest;
        public double[] chargeProfile_kW;
        //public double startTime_h;
        public boolean isFinished = false;
        private int currentIdx = 0;
        //private double endTime_h;

        public ActiveSession(I_ChargingRequest chargingRequest, double[] chargeProfile_kW, double startTime_h) {
            this.chargingRequest = chargingRequest;
            this.chargeProfile_kW = chargeProfile_kW;
            //this.startTime_h = startTime_h;            
        }
        
        public void charge(J_ChargePoint chargePoint, GridConnection gc, J_TimeVariables timeVariables) {
        	chargePoint.charge(this.chargingRequest, this.chargeProfile_kW[currentIdx], timeVariables, gc);	
        	currentIdx++;
        	if ( this.chargeProfile_kW.length == currentIdx) {
        		this.isFinished = true;
        	}
        }
        
        public void checkIfFinished() {
        	if (currentIdx < chargeProfile_kW.length) {
        		traceln("Warning!! Charge sesssion aborted too early! Missing %s timesteps!", chargeProfile_kW.length-currentIdx);
        	}
        }
    }
    
    private List<I_ChargingRequest> previousChargingRequests = new ArrayList<>();    
    private List<ActiveSession> activeSessions = new ArrayList<>();
    
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
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	double t_h = timeVariables.getT_h();

    	//double currentElectricityPriceConsumption_eurpkWh = gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() * 0.001;
    	List<I_ChargingRequest> currentChargingRequests = chargePoint.getCurrentActiveChargingRequests();
       	for (I_ChargingRequest chargingRequest : currentChargingRequests) {
       		if (!previousChargingRequests.contains(chargingRequest)) { // Schedule new charging session!
       			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
       			double maxChargePower_kW = chargePoint.getMaxChargingCapacity_kW(chargingRequest);
       			// Get session duration
       			double duration_h = chargingRequest.getLeaveTime_h() - t_h;
       			int length = (int)ceil(duration_h/timeParameters.getTimeStep_h()); // 
       			if (abs(length*timeParameters.getTimeStep_h() - duration_h) < .0001 && (length*timeParameters.getTimeStep_h() - duration_h) != 0.0) {
       				traceln("Rounding errors in duration_h! duration_h: %s", duration_h);
       			}
       			if (duration_h <= 0) {
       				traceln("ChargingRequest starting after endtime! Duration_h: %s", duration_h);

       				//throw new RuntimeException("ChargingRequest starting after endtime!");
       			}
       			// Get price curve for duration
       			double[] priceCurve = Arrays.copyOfRange(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getAllValues(), roundToInt(t_h/timeParameters.getTimeStep_h()), length+roundToInt(t_h/timeParameters.getTimeStep_h()));
       			double marketFeedback_eurpMWhpkW = 20; // PLACEHOLDER VALUE!
       			Market market = new Market(priceCurve, marketFeedback_eurpMWhpkW, 0, 0, 0);
       			FlexConsumptionAsset asset = new FlexConsumptionAsset(maxChargePower_kW, 20, timeParameters.getTimeStep_h(), length*timeParameters.getTimeStep_h(), null);
       			if (chargeNeedForNextTrip_kWh > (maxChargePower_kW * timeParameters.getTimeStep_h()*(length) )) {
       				traceln("Warning! chargeNeedForNextTrip_kWh is too high for duration of charging session! Deficit: %s", chargeNeedForNextTrip_kWh - (maxChargePower_kW*(timeParameters.getTimeStep_h()*(length))));
       			} /*else {
       				traceln("Charging session long enough for chargeNeedForNextTrip_kWh");
       			}*/
       			chargeNeedForNextTrip_kWh=max(0,min(chargeNeedForNextTrip_kWh, maxChargePower_kW * timeParameters.getTimeStep_h()*(length)-0.001));
       			double[] loadProfile_kW = new double[length];
       			if (chargeNeedForNextTrip_kWh>0) {
       				//traceln("Profile length: %s, price-curve length: %s", loadProfile_kW.length, priceCurve.length);
       				loadProfile_kW = FlexAssetScheduler.scheduleWrapper(loadProfile_kW, asset, chargeNeedForNextTrip_kWh, market, timeParameters.getTimeStep_h(), false);
       				double chargeVolumeCheck_kWh = ZeroMath.arraySum(loadProfile_kW) * timeParameters.getTimeStep_h();
       				if (chargeVolumeCheck_kWh < chargeNeedForNextTrip_kWh - 1e-10) {
       					traceln("Warning!! Scheduled charging energy is lower than chargeNeedForNextTrip_kWh! Deficit: %s", chargeNeedForNextTrip_kWh - chargeVolumeCheck_kWh);
       				}
       			}
				//traceln("Starting session! Profile length: %s, charging need: %s", loadProfile_kW.length, chargeNeedForNextTrip_kWh);
       			// Store loadProfile_kW. How to keep track of timestep within such a profile?
       			previousChargingRequests.add(chargingRequest);
       			activeSessions.add(new ActiveSession(chargingRequest, loadProfile_kW, t_h));       			
       		} 
       	}
   		// Execute charging of active sessions
		for (ActiveSession session : activeSessions) {			
			//int index = roundToInt((t_h - session.startTime_h)/timeParameters.getTimeStep_h());
			//if (session.chargeProfile_kW.length>0) {
			if (!currentChargingRequests.contains(session.chargingRequest)) { // || index == session.chargeProfile_kW.length) {
				session.isFinished=true;
				session.checkIfFinished();
			//} else if (timeParameters.getTimeStep_h()<session.chargingRequest.getLeaveTime_h() && session.chargeProfile_kW.length>0) {
			} else if (session.chargeProfile_kW.length>0) {
				//chargePoint.charge(session.chargingRequest, session.chargeProfile_kW[index], timeVariables, gc);	
				session.charge(chargePoint, gc, timeVariables);
			}
			
			if (session.isFinished) {
				previousChargingRequests.remove(session.chargingRequest);
			}
			/*if (index == session.chargeProfile_kW.length-1 || session.chargeProfile_kW.length==0) { // session ending, remove session from list
				//traceln("Ending session! Profile length: %s", session.chargeProfile_kW.length);
				previousChargingRequests.remove(session.chargingRequest);
				//activeSessions.remove(session);
				session.isFinished = true;
			}*/
			//traceln("Scheduled charging, profile length: %s, current index: %s, current power: %s kW", session.chargeProfile_kW.length, index, session.chargeProfile_kW[index]);
		}       					
		activeSessions.removeIf(session -> session.isFinished); // Must be outside of for-loop over this collection!
    }

	public void setV2GActive(boolean activateV2G) {
		throw new RuntimeException("ChargingManagementPriceScheduled does not support V2G charging!");
		/*
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
		*/
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
	    
		previousChargingRequests = new ArrayList<>();    
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
