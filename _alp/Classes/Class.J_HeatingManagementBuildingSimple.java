/**
 * J_HeatingManagementBuildingSimple
 */	
public class J_HeatingManagementBuildingSimple implements I_HeatingManagement {

    private boolean isInitialized = false;
    private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.GAS_BURNER, 
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, 
		OL_GridConnectionHeatingType.HYDROGENBURNER,
		OL_GridConnectionHeatingType.DISTRICTHEAT,
		OL_GridConnectionHeatingType.LT_DISTRICTHEAT
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
    private J_EAConversion heatingAsset;
    
    private double startOfDay_h = 8;
    private double startOfNight_h = 23;
    private double dayTimeSetPoint_degC = 19;
    private double nightTimeSetPoint_degC = 19;
    private double heatingKickinTreshhold_degC = 1;
    
	/**
     * Default constructor
     */
    public J_HeatingManagementBuildingSimple( GridConnection gc ) {
    	this.gc = gc;
    	this.building = gc.p_BuildingThermalAsset;
    }

    public J_HeatingManagementBuildingSimple( GridConnection gc, double startOfDay_h, double startOfNight_h, double dayTimeSetPoint_degC, double nightTimeSetPoint_degC, double heatingKickinTreshhold_degC ) {
    	this.gc = gc;
    	this.building = gc.p_BuildingThermalAsset;
    	this.startOfDay_h = startOfDay_h;
        this.startOfNight_h = startOfNight_h;
        this.dayTimeSetPoint_degC = dayTimeSetPoint_degC;
        this.nightTimeSetPoint_degC = nightTimeSetPoint_degC;
        this.heatingKickinTreshhold_degC = heatingKickinTreshhold_degC;	
    }
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	if (timeOfDay_h < startOfDay_h || timeOfDay_h >= startOfNight_h) {
    		if (buildingTemp_degC < nightTimeSetPoint_degC - heatingKickinTreshhold_degC) {
    			// Nighttime and building temperature too low
    			double buildingPower_kW = heatingAsset.getOutputCapacity_kW() - heatDemand_kW;
    			building.f_updateAllFlows( buildingPower_kW / building.getCapacityHeat_kW() );
    			heatingAsset.f_updateAllFlows( 1.0 );
    			return;
    		}
    		else {
    			// Nighttime and building temperature acceptable
    			building.f_updateAllFlows( 0.0 );
    			heatingAsset.f_updateAllFlows( heatDemand_kW / heatingAsset.getOutputCapacity_kW() );
    			return;
    		}
    	}
    	else {
    		if (buildingTemp_degC < dayTimeSetPoint_degC - heatingKickinTreshhold_degC) {
    			// Daytime and building temperature too low
    			double buildingPower_kW = heatingAsset.getOutputCapacity_kW() - heatDemand_kW;
    			building.f_updateAllFlows( buildingPower_kW / building.getCapacityHeat_kW() );
    			heatingAsset.f_updateAllFlows( 1.0 );
    			return;
    		}
    		else {
    			// Daytime and building temperature acceptable
    			building.f_updateAllFlows( 0.0 );
    			heatingAsset.f_updateAllFlows( heatDemand_kW / heatingAsset.getOutputCapacity_kW() );
    			return;
    		}
    	}
    }
    
    public void initializeAssets() {
    	//if (!validHeatingTypes.contains(gc.p_heatingType)) {
    		//throw new RuntimeException(this.getClass() + " does not support heating type: " + gc.p_heatingType);
    	//}
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (building == null) {
    		throw new RuntimeException(this.getClass() + " requires a building asset.");
    	}
    	J_EAConsumption heatProfile = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND && x.getEAType() != OL_EnergyAssetType.HOT_WATER_CONSUMPTION);
    	if (heatProfile != null) {
    		throw new RuntimeException(this.getClass() + " does not support HEAT_DEMAND profiles.");
    	}
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	// TODO: Add a check if the power of the asset is sufficient?
    	// TODO: Add a check if the heatingAsset is of the correct type, e.g. not a hydrogen burner or not a CHP.
    	this.heatingAsset = gc.c_heatingAssets.get(0);
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