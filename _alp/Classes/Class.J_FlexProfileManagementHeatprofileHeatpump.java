/**
 * J_FlexProfileManagementHeatprofileHeatpump
 */	
public class J_FlexProfileManagementHeatprofileHeatpump implements I_FlexProfileManagement{

	private GridConnection gc;
	private J_TimeParameters timeParameters;

    /**
     * Empty constructor for serialization
     */
    public J_FlexProfileManagementHeatprofileHeatpump() {
    }
    
    /**
     * Default constructor
     */
    public J_FlexProfileManagementHeatprofileHeatpump(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    public void manageFlexProfiles(J_TimeVariables timeVariables) {
    	
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
		
		return "J_FlexProfileManagementHeatprofileHeatpump: " + System.lineSeparator() +
				"Currently controlling J_EAFlexProfiles: " + 
				flexProfilesString.toString();
	}
}