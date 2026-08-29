/**
 * J_FlexProfileManagementDefault: Does not shift profiles, just plays them as normal.
 */	
public class J_FlexProfileManagementDefault implements I_FlexProfileManagement{
	private GridConnection gc;
	private J_TimeParameters timeParameters;
    /**
     * Empty constructor for serialization
     */
    public J_FlexProfileManagementDefault() {
    }
    
    /**
     * Default constructor
     */
    public J_FlexProfileManagementDefault(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    public void manageFlexProfiles(J_TimeVariables timeVariables) {   	
    	gc.c_flexProfileAssets.forEach(flexProfile -> gc.f_updateFlexAssetFlows(flexProfile, 1.0, timeVariables));
    }
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		int timeStepsInForecast = roundToInt((timeOfIntervalEnd_h - timeOfIntervalStart_h) / this.timeParameters.getTimeStep_h());
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		for (J_EAFlexProfile flexProfile : gc.c_flexProfileAssets) {
			OL_EnergyCarriers EC = flexProfile.getEnergyCarrier();
			if (loadMap.get(EC) == null) {
				Double[] loadProfile_kW = new Double[timeStepsInForecast];
				Arrays.fill(loadProfile_kW, 0.0);
				loadMap.put(EC, loadProfile_kW);
			}
			J_ProfilePointer pp = flexProfile.getProfilePointer();
			for (int i = 0; i < timeStepsInForecast; i++) {
				double t = timeOfIntervalStart_h + i * this.timeParameters.getTimeStep_h();
				loadMap.get(EC)[i] += pp.getValue(t);
			}
		}
		OL_ForecastStatus status = OL_ForecastStatus.PERFECT_FORECAST;
		return new J_AssetTypeForecast(I_FlexProfileManagement.class, loadMap, status, null);
	}
	
    ////Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		List<J_EAFlexProfile> flexProfiles = gc.c_flexProfileAssets;
		StringBuilder flexProfilesString = new StringBuilder();
		for(J_EAFlexProfile flexProfile : flexProfiles) {
			flexProfilesString.append(System.lineSeparator());
			flexProfilesString.append(flexProfile.toString());
		}
		
		return "J_FlexProfileManagementDefault: " + System.lineSeparator() +
				"Currently controlling J_EAFlexProfiles: " + 
				flexProfilesString.toString();
	}
}