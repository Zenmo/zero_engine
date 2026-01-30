/**
 * J_EnergyManagementDefault
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_EnergyManagementDefault implements I_EnergyManagement{
	
    private GridConnection GC;
    private J_TimeParameters timeParameters;
    
    /**
     * Default constructor
     */
    public J_EnergyManagementDefault(GridConnection GC, J_TimeParameters timeParameters) {
    	this.GC = GC;
    	this.timeParameters = timeParameters;
    }
    
    public void manageFlexAssets(J_TimeVariables timeVariables) {
	    GC.f_manageHeating(timeVariables);
	
	    GC.f_manageEVCharging(timeVariables);
	
	    GC.f_manageBattery(timeVariables);
    }
    
    
    //Store and reset states
	public void storeStatesAndReset() {
		
	}
	public void restoreStates() {

	}
}