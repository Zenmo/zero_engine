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
    
	Map<Class<? extends I_SubAssetManagement>, I_SubAssetManagement> subManagements = new HashMap();
	List<Class<? extends I_SubAssetManagement>> supportedSubManagements = 
												new ArrayList<>(Arrays.asList(
													I_HeatingManagement.class, 
													I_ChargingManagement.class, 
													I_BatteryManagement.class
												));
			
	    
    
    /**
     * Empty constructor for serialization
     */
    public J_EnergyManagementDefault() {
    }
    
    /**
     * Default constructor
     */
    public J_EnergyManagementDefault(GridConnection GC, J_TimeParameters timeParameters) {
    	this.GC = GC;
    	this.timeParameters = timeParameters;
    }
    
    
    public void manageFlexAssets(J_TimeVariables timeVariables) {
    	//1. Call Heating management
    	if(this.getSubManagement(I_HeatingManagement.class) != null) {
    		this.getSubManagement(I_HeatingManagement.class).manageHeating(timeVariables);
    	}
    	
    	//2. Call Charging management
    	if(this.getSubManagement(I_ChargingManagement.class) != null) {
    		this.getSubManagement(I_ChargingManagement.class).manageCharging(GC.f_getChargePoint(), timeVariables);
    	}
    	
    	//3. Call Battery management
    	if(this.getSubManagement(I_BatteryManagement.class) != null) {
    		this.getSubManagement(I_BatteryManagement.class).manageBattery(timeVariables);
    	}
    }
    
    
	//Specific child management activation
	public void activateV2GChargingMode(boolean enableV2G, J_TimeParameters timeParameters,	J_TimeVariables timeVariables) {
		if(this.getSubManagement(I_ChargingManagement.class) != null) {
			this.getSubManagement(I_ChargingManagement.class).setV2GActive(enableV2G);
			if (enableV2G){
				this.GC.f_addAssetFlow(OL_AssetFlowCategories.V2GPower_kW, timeParameters, timeVariables);
			}
		}
	}
	
	
	//Get child management types
	public OL_GridConnectionHeatingType getCurrentHeatingType() {
		if(this.getSubManagement(I_HeatingManagement.class) != null) {
			return this.getSubManagement(I_HeatingManagement.class).getCurrentHeatingType();
		}
		else {
			return OL_GridConnectionHeatingType.NONE;
		}
	}
	public OL_ChargingAttitude getCurrentChargingType() {
		if(this.getSubManagement(I_ChargingManagement.class) != null) {
			return this.getSubManagement(I_ChargingManagement.class).getCurrentChargingType();
		}
		else {
			return OL_ChargingAttitude.NONE;
		}
	}
	
    ////Store and reset states
	public void storeStatesAndReset() {
		subManagements.values().forEach(subManagement -> subManagement.storeStatesAndReset());
	}
	public void restoreStates() {
		subManagements.values().forEach(subManagement -> subManagement.restoreStates());
	}
}