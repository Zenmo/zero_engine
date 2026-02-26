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
    
	List<Class<? extends I_AssetManagement>> internalAssetManagements = new ArrayList<>(); //Inherent asset management that the EMS handles itself
	List<Class<? extends I_AssetManagement>> supportedExternalAssetManagements = 
												new ArrayList<>(Arrays.asList(
													//J_HeatingManagementSimple.class, 
													I_HeatingManagement.class,
													I_ChargingManagement.class, 
													I_BatteryManagement.class
												));
	Map<Class<? extends I_AssetManagement>, I_AssetManagement> activeExternalAssetManagements = new HashMap();			
	
	boolean isChecked = false;
    
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
    	if ( !isChecked ) {
    		this.checkConfiguration(GC.c_flexAssets);
    	}
    	
    	//1. Call Heating management
    	if(this.getExternalAssetManagement(I_HeatingManagement.class) != null) {
    		this.getExternalAssetManagement(I_HeatingManagement.class).manageHeating(timeVariables);
    	}
    	
    	//2. Call Charging management
    	if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
    		this.getExternalAssetManagement(I_ChargingManagement.class).manageCharging(GC.f_getChargePoint(), timeVariables);
    	}
    	
    	//3. Call Battery management
    	if(this.getExternalAssetManagement(I_BatteryManagement.class) != null) {
    		this.getExternalAssetManagement(I_BatteryManagement.class).manageBattery(timeVariables);
    	}
    }
    
    
	//Specific child management activation
	public void setV2GActive(boolean enableV2G) {
		if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
			this.getExternalAssetManagement(I_ChargingManagement.class).setV2GActive(enableV2G);
		}
	}
    public boolean getV2GActive() {
		if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
			return this.getExternalAssetManagement(I_ChargingManagement.class).getV2GActive();
		}
		else {
			return false;
		}
    }
	
	//Get child management types
	public OL_GridConnectionHeatingType getCurrentHeatingType() {

		if(this.getExternalAssetManagement(I_HeatingManagement.class) != null) {
			return this.getExternalAssetManagement(I_HeatingManagement.class).getCurrentHeatingType();
		}
		else {
			return OL_GridConnectionHeatingType.NONE;
		}
	}
	public OL_ChargingAttitude getCurrentChargingType() {
		if(this.getExternalAssetManagement(I_ChargingManagement.class) != null) {
			return this.getExternalAssetManagement(I_ChargingManagement.class).getCurrentChargingType();
		}
		else {
			return OL_ChargingAttitude.NONE;
		}
	}
	
	
	//Get inherent, supported and active Asset management classes
	public List<Class<? extends I_AssetManagement>> getInternalAssetManagements(){
		return this.internalAssetManagements;
	}
	public List<Class<? extends I_AssetManagement>> getSupportedExternalAssetManagements(){
		return this.supportedExternalAssetManagements;
	}
	public Map<Class<? extends I_AssetManagement>, I_AssetManagement> getActiveExternalAssetManagements(){
		return this.activeExternalAssetManagements;
	}    
	
	////Checks
	public void checkConfigurationEMSSpecific(List<J_EAFlex> flexAssetsGCList) {
		
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	
    ////Store and reset states
	public void storeStatesAndReset() {
		activeExternalAssetManagements.values().forEach(subManagement -> subManagement.storeStatesAndReset());
	}
	public void restoreStates() {
		activeExternalAssetManagements.values().forEach(subManagement -> subManagement.restoreStates());
	}
}