/**
 * J_HeatingManagementPIcontrolHybridHeatpump
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public class J_HeatingManagementPIcontrolHybridHeatpump implements I_HeatingManagement{
	private boolean isInitialized = false;
	private GridConnection gc;
	private J_TimeParameters timeParameters;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.HYBRID_HEATPUMP
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
	private J_EAConversionHeatPump heatPumpAsset;
	private J_EAConversionGasBurner gasBurnerAsset;
	private J_HeatingPreferences heatingPreferences;
	private J_EAStorageHeat hotWaterBuffer;
	private List<J_EAProduction> ptAssets;
    private boolean hasPT = false;
    
	 // PI control gains
    private double P_gain_kWpDegC = 1*1;
    private double I_gain_kWphDegC = 0.1*2;
    private double I_state_hDegC = 0;
    
    //Temperature setpoint low pass filter
    private double filteredCurrentSetpoint_degC;
    private double setpointFilterTimeScale_h = 2.0; // Smooth in X hours
    
    //Stored parameters
    private double storedI_state_hDegC;
    private double storedFilteredCurrentSetpoint_degC;
    
    /**
     * Default constructor
     */
    public J_HeatingManagementPIcontrolHybridHeatpump() {
    }

    public J_HeatingManagementPIcontrolHybridHeatpump( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    }
    
    public void manageHeating(J_TimeVariables timeVariables) {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	
    	if(hasPT) {
	    	//Adjust the hot water and overall heat demand with the buffer and pt
	    	double remainingHotWaterDemand_kW = J_HeatingFunctionLibrary.managePTAndHotWaterHeatBuffer(hotWaterBuffer, ptAssets, hotWaterDemand_kW, timeVariables, gc); // This function updates the buffer and curtails PT if needed -> current balanceflow is updated accordingly.
	    }
    	
    	double otherHeatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = timeVariables.getTimeOfDay_h();
    	double buildingHeatingDemand_kW = 0;
    	
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();
    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    	}
    	
    	//Smooth the setpoint signal
    	this.filteredCurrentSetpoint_degC += 1/(this.setpointFilterTimeScale_h / timeParameters.getTimeStep_h()) * (currentSetpoint_degC - this.filteredCurrentSetpoint_degC);
    	
    	
    	//Determine the building heat demand using the found temp setpoint and PI controller
    	double deltaT_degC = this.filteredCurrentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed
    	
    	I_state_hDegC = max(0,I_state_hDegC + deltaT_degC * timeParameters.getTimeStep_h()); // max(0,...) to prevent buildup of negative integrator during warm periods.
    	buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDegC + I_state_hDegC * I_gain_kWphDegC);
    	
    	
    	//Set asset power based on the COP (All demand profiles (hot water, etc.) always go to the gasburner!)
		double heatpumpAssetPower_kW = 0;
		double gasBurnerAssetPower_kW = 0;
		double heatIntoBuilding_kW = 0;
		
    	if (heatPumpAsset.getCOP() > 3.0 ) {
    		heatpumpAssetPower_kW = min(heatPumpAsset.getOutputCapacity_kW(), buildingHeatingDemand_kW);
    		gasBurnerAssetPower_kW = min(gasBurnerAsset.getOutputCapacity_kW(), otherHeatDemand_kW + max(0, buildingHeatingDemand_kW - heatpumpAssetPower_kW));
    		heatIntoBuilding_kW = heatpumpAssetPower_kW + max(0, gasBurnerAssetPower_kW - otherHeatDemand_kW);
    	}
    	else {
    		heatpumpAssetPower_kW = 0.0;
    		gasBurnerAssetPower_kW = min(gasBurnerAsset.getOutputCapacity_kW(), buildingHeatingDemand_kW + otherHeatDemand_kW);
    		heatIntoBuilding_kW = max(0, gasBurnerAssetPower_kW - otherHeatDemand_kW);  
    	}
    	
    	//Updat flows with found asset powers
    	gc.f_updateFlexAssetFlows(heatPumpAsset, heatpumpAssetPower_kW / heatPumpAsset.getOutputCapacity_kW(), timeVariables);
    	gc.f_updateFlexAssetFlows(gasBurnerAsset, gasBurnerAssetPower_kW / gasBurnerAsset.getOutputCapacity_kW(), timeVariables);
    	gc.f_updateFlexAssetFlows(building, heatIntoBuilding_kW / building.getCapacityHeat_kW(), timeVariables);
    }
    
    
    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	List<J_EAProduction> ptAssets = findAll(gc.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL);
    	if (ptAssets.size() > 0) {
        	if(gc.p_DHWAsset == null) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand to make sense to use this heating management with PT.");
        	}
        	this.hasPT = true;
        	this.ptAssets = ptAssets;
    	}
    	if (gc.p_heatBuffer != null) {
        	if(gc.p_DHWAsset == null || ptAssets.size() == 0) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand and pt to make sense to use this heating management.");
        	}
    		this.hotWaterBuffer = gc.p_heatBuffer;
    	}
    	if(gc.p_BuildingThermalAsset != null) {
        	this.building = gc.p_BuildingThermalAsset;
    	} else {
    		throw new RuntimeException(this.getClass() + " can only be used for temperature control of a building thermal asset.");
    	}
    	if (gc.c_heatingAssets.size() != 2) {
    		throw new RuntimeException(this.getClass() + " requires exactly two heating assets.");
    	}
    	if (gc.c_heatingAssets.get(0) instanceof J_EAConversionGasBurner) {
    		this.gasBurnerAsset = (J_EAConversionGasBurner)gc.c_heatingAssets.get(0);
    	}
    	else if (gc.c_heatingAssets.get(1) instanceof J_EAConversionGasBurner) {
    		this.gasBurnerAsset = (J_EAConversionGasBurner)gc.c_heatingAssets.get(1);    		
    	}
    	else {
    		throw new RuntimeException(this.getClass() + " requires a Gas Burner");
    	}
    	if (gc.c_heatingAssets.get(0) instanceof J_EAConversionHeatPump) {
    		this.heatPumpAsset = (J_EAConversionHeatPump)gc.c_heatingAssets.get(0);
    	}
    	else if (gc.c_heatingAssets.get(1) instanceof J_EAConversionHeatPump) {
    		this.heatPumpAsset = (J_EAConversionHeatPump)gc.c_heatingAssets.get(1);    		
    	}
    	else {
    		throw new RuntimeException(this.getClass() + " requires a Heat Pump");
    	}
    	if(this.heatingPreferences == null) {
    		heatingPreferences = new J_HeatingPreferences();
    	}
		this.filteredCurrentSetpoint_degC = heatingPreferences.getMinComfortTemperature_degC();
    	this.isInitialized = true;
    }
    
    
    public void notInitialized() {
    	this.isInitialized = false;
    }
    
    public List<OL_GridConnectionHeatingType> getValidHeatingTypes() {
    	return this.validHeatingTypes;
    }
    
    public OL_GridConnectionHeatingType getCurrentHeatingType() {
    	return this.currentHeatingType;
    }
    
    public void setHeatingPreferences(J_HeatingPreferences heatingPreferences) {
    	this.heatingPreferences = heatingPreferences;
    }
    
    public J_HeatingPreferences getHeatingPreferences() {
    	return this.heatingPreferences;
    }
    
	//Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
	    this.storedI_state_hDegC = this.I_state_hDegC;
	    this.storedFilteredCurrentSetpoint_degC = this.filteredCurrentSetpoint_degC;
		this.I_state_hDegC = 0;
		this.filteredCurrentSetpoint_degC = 0;
	}
	public void restoreStates() {
		this.I_state_hDegC = this.storedI_state_hDegC;
	    this.filteredCurrentSetpoint_degC = this.storedFilteredCurrentSetpoint_degC;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}