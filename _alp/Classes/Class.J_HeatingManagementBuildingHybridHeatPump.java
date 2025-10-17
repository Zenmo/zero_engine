/**
 * J_HeatingManagementBuildingHybridHeatPump
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

public class J_HeatingManagementBuildingHybridHeatPump implements I_HeatingManagement {

	private boolean isInitialized = false;
	private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.HYBRID_HEATPUMP
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
	private J_EAConversionHeatPump heatPumpAsset;
	private J_EAConversionGasBurner gasBurnerAsset;
	private J_HeatingPreferences heatingPreferences;
	

    private double heatingKickinTreshhold_degC = 1;
    
	/**
     * Default constructor
     */
    public J_HeatingManagementBuildingHybridHeatPump() {
    	
    }
    
    public J_HeatingManagementBuildingHybridHeatPump( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }

    public J_HeatingManagementBuildingHybridHeatPump( GridConnection gc, OL_GridConnectionHeatingType heatingType, double heatingKickinTreshhold_degC ) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
        this.heatingKickinTreshhold_degC = heatingKickinTreshhold_degC;	
    }
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	double buildingPower_kW = 0;
    	double assetOutputPower_kW = heatPumpAsset.getCOP() > 3.0 ? heatPumpAsset.getOutputCapacity_kW() : gasBurnerAsset.getOutputCapacity_kW();
    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		if (buildingTemp_degC < heatingPreferences.getNightTimeSetPoint_degC() - heatingKickinTreshhold_degC) {       			
    			double buildingPowerSetpoint_kW = (heatingPreferences.getNightTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / gc.energyModel.p_timeStep_h;
    			buildingPower_kW = min(assetOutputPower_kW - heatDemand_kW, buildingPowerSetpoint_kW);
    		}
    	}
    	else {
    		if (buildingTemp_degC < heatingPreferences.getDayTimeSetPoint_degC() - heatingKickinTreshhold_degC) {
    			double buildingPowerSetpoint_kW = (heatingPreferences.getDayTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / gc.energyModel.p_timeStep_h;
    			buildingPower_kW = min(assetOutputPower_kW - heatDemand_kW, buildingPowerSetpoint_kW);
    		}
    	}
    	if (heatPumpAsset.getCOP() > 3.0 ) {
    		heatPumpAsset.f_updateAllFlows( (buildingPower_kW + heatDemand_kW) / heatPumpAsset.getOutputCapacity_kW() );
    		building.f_updateAllFlows( buildingPower_kW / building.getCapacityHeat_kW() );
    		gasBurnerAsset.f_updateAllFlows( 0.0 );
    	}
    	else {
    		gasBurnerAsset.f_updateAllFlows( (buildingPower_kW + heatDemand_kW) / gasBurnerAsset.getOutputCapacity_kW() );
    		building.f_updateAllFlows( buildingPower_kW / building.getCapacityHeat_kW() );
    		heatPumpAsset.f_updateAllFlows( 0.0 );
    	}
    }

    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (gc.p_BuildingThermalAsset != null) {
        	this.building = gc.p_BuildingThermalAsset;
    	}
    	else {
    		throw new RuntimeException(this.getClass() + " requires a building asset.");
    	}
    	if (gc.c_heatingAssets.size() != 2) {
    		throw new RuntimeException(this.getClass() + " requires exactly two heating assets.");
    	}
    	// TODO: Add a check if the power of the asset is sufficient?
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