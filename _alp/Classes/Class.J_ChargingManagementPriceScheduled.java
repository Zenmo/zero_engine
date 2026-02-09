/**
 * J_ChargingManagementPriceScheduled
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import zeroPackage.Market;
import zeroPackage.FlexConsumptionAsset;
import zeroPackage.FlexAssetScheduler;


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
        public double startTime_h;
        //private double endTime_h;

        public ActiveSession(I_ChargingRequest chargingRequest, double[] chargeProfile_kW, double startTime_h) {
            this.chargingRequest = chargingRequest;
            this.chargeProfile_kW = chargeProfile_kW;
            this.startTime_h = startTime_h;
        }
    }
    
    private Set<I_ChargingRequest> previousChargingRequests;    
    private List<ActiveSession> activeSessions;
    
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

    	double currentElectricityPriceConsumption_eurpkWh = gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() * 0.001;
    	List<I_ChargingRequest> currentChargingRequests = chargePoint.getCurrentActiveChargingRequests();
       	for (I_ChargingRequest chargingRequest : currentChargingRequests) {
       		if (!previousChargingRequests.contains(chargingRequest)) { // Schedule new charging session!
       			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
       			// Get session duration
       			double duration_h = chargingRequest.getLeaveTime_h() - t_h;
       			// Get price curve for duration
       			double[] priceCurve = Arrays.copyOfRange(gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getAllValues(), roundToInt(t_h/timeParameters.getTimeStep_h()), roundToInt((t_h+duration_h)/timeParameters.getTimeStep_h()));
       			double marketFeedback_eurpMWhpkW = 5; // PLACEHOLDER VALUE!
       			Market market = new Market(priceCurve, marketFeedback_eurpMWhpkW, 0, 0, 0);
       			FlexConsumptionAsset asset = new FlexConsumptionAsset(chargingRequest.getVehicleChargingCapacity_kW(), 20, timeParameters.getTimeStep_h(), duration_h, null);
       			double[] loadProfile_kW = new double[roundToInt(duration_h/timeParameters.getTimeStep_h())];
       			loadProfile_kW = FlexAssetScheduler.scheduleWrapper(loadProfile_kW, asset, chargeNeedForNextTrip_kWh, market, timeParameters.getTimeStep_h(), false);
       			// Store loadProfile_kW. How to keep track of timestep within such a profile?
       			previousChargingRequests.add(chargingRequest);
       			activeSessions.add(new ActiveSession(chargingRequest, loadProfile_kW, t_h));       			
       		} 
       	}
   		// Execute charging of active sessions
		for (ActiveSession session : activeSessions) {			
			int index = roundToInt((t_h - session.startTime_h)/timeParameters.getTimeStep_h());
			chargePoint.charge(session.chargingRequest, session.chargeProfile_kW[index], timeVariables, gc);	
			if (index == session.chargeProfile_kW.length-1) { // session ending, remove session from list
				previousChargingRequests.remove(session.chargingRequest);
				activeSessions.remove(session.chargingRequest);
			}
		}       					
    	
    }

	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
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
		//this.storedElectricityPriceLowPassed_eurpkWh = this.electricityPriceLowPassed_eurpkWh;
		//this.electricityPriceLowPassed_eurpkWh = this.initialValueElectricityPriceLowPassed_eurpkWh;
	}
	public void restoreStates() {
		//this.electricityPriceLowPassed_eurpkWh = this.storedElectricityPriceLowPassed_eurpkWh;
	}
	
	
    @Override
 	public String toString() {
 		return "Active charging type: " + this.activeChargingType;

 	}
}