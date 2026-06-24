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