/**
 * J_ChargingManagementExternalSetpoint
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.util.EnumSet;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_ChargingManagementExternalSetpoint implements I_ChargingManagement{

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.CUSTOM;
    private boolean V2GActive = false;
    
    private Map<I_ChargingRequest, Double> map_chargingSetpoints_kW = new HashMap<>();
    
    /**
     * Default constructor
     */
    public J_ChargingManagementExternalSetpoint( ) {
    
    }
    
    public J_ChargingManagementExternalSetpoint( GridConnection gc, J_TimeParameters timeParameters) {
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
       		if(map_chargingSetpoints_kW.keySet().contains(chargingRequest)) {
       			if(map_chargingSetpoints_kW.get(chargingRequest) < 0 && ( !this.V2GActive || !chargingRequest.getV2GCapable())) {
       				throw new RuntimeException("Trying to discharge a vehicle using an external setpoint while V2G is not activated or possible.");
       			}
       			chargePoint.charge(chargingRequest, map_chargingSetpoints_kW.get(chargingRequest), timeVariables, gc);
       		}
       		else { // chargerequest not found in external setpoints map: Don't charge.
       			chargePoint.charge(chargingRequest, 0, timeVariables, gc);
       		}
    	}
    	map_chargingSetpoints_kW = new HashMap<>();
    }
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		OL_ForecastStatus status = OL_ForecastStatus.NOT_FORECASTABLE;
		String reason = "External Setpoint Management can not be forecasted.";
		return new J_AssetTypeForecast(I_ChargingManagement.class, loadMap, status, reason);
	}
	
	public void setChargingSetpoints(Map<I_ChargingRequest, Double> map_chargingSetpoints_kW) {
		this.map_chargingSetpoints_kW = map_chargingSetpoints_kW;
	}
	
	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
    
    //Store and reset states
	public void storeStatesAndReset() {
		map_chargingSetpoints_kW = new HashMap<>();
	}
	public void restoreStates() {
		
	}
	
	
    @Override
	public String toString() {
		return "Active charging type: " + this.activeChargingType;

	}
}