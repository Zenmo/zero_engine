/**
 * J_BatteryManagementOff
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_BatteryManagementOff implements I_BatteryManagement {

	GridConnection gc;
    private J_TimeParameters timeParameters;

	
    /**
     * Default constructor
     */
	public J_BatteryManagementOff( ) {

    }
	
    public J_BatteryManagementOff( GridConnection gc, J_TimeParameters timeParameters ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }

    public void manageBattery(J_TimeVariables timeVariables) {
    	if(gc.p_batteryAsset != null && gc.p_batteryAsset.getStorageCapacity_kWh() > 0) {
    		gc.f_updateFlexAssetFlows(gc.p_batteryAsset, 0.0, timeVariables);
    	}
    }
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		int timeStepsInForecast = roundToInt((timeOfIntervalEnd_h - timeOfIntervalStart_h) / this.timeParameters.getTimeStep_h());
		Double[] electricityLoad_kW = new Double[timeStepsInForecast];
		Arrays.fill(electricityLoad_kW, 0.0);
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		loadMap.put(OL_EnergyCarriers.ELECTRICITY, electricityLoad_kW);
		OL_ForecastStatus status = OL_ForecastStatus.PERFECT_FORECAST;
		return new J_AssetTypeForecast(I_BatteryManagement.class, loadMap, status, null);
	}
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store/reset
	}
	public void restoreStates() {
		//Nothing to store/reset
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}