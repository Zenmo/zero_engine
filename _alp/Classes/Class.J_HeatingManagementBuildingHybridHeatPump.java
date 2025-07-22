/**
 * J_HeatingManagementBuildingHybridHeatPump
 */	
public class J_HeatingManagementBuildingHybridHeatPump implements Serializable {

	private boolean isInitialized = false;
	private GridConnection gc;
    private J_EABuilding building;	
	private J_EAConversionHeatPump heatPumpAsset;
	private J_EAConversionGasBurner gasBurnerAsset;
	
    private double startOfDay_h = 8;
    private double startOfNight_h = 23;
    private double dayTimeSetPoint_degC = 19;
    private double nightTimeSetPoint_degC = 19;
    private double heatingKickinTreshhold_degC = 1;
    
	/**
     * Default constructor
     */
    public J_HeatingManagementBuildingHybridHeatPump( GridConnection gc ) {
    	this.gc = gc;
    }

    public J_HeatingManagementBuildingHybridHeatPump( GridConnection gc, double startOfDay_h, double startOfNight_h, double dayTimeSetPoint_degC, double nightTimeSetPoint_degC, double heatingKickinTreshhold_degC ) {
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
    	boolean heatBuilding = false;
    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	if (timeOfDay_h < startOfDay_h || timeOfDay_h >= startOfNight_h) {
    		if (buildingTemp_degC < nightTimeSetPoint_degC - heatingKickinTreshhold_degC) {
    			heatBuilding = true;
    		}
    	}
    	else {
    		if (buildingTemp_degC < dayTimeSetPoint_degC - heatingKickinTreshhold_degC) {
    			heatBuilding = true;
    		}
    	}
    	if (heatBuilding) {
        	if (heatPumpAsset.getCOP() > 3.0 ) {
        		double buildingPower_kW = heatPumpAsset.getOutputCapacity_kW() - heatDemand_kW;
        		heatPumpAsset.f_updateAllFlows( 1.0 );
        		building.f_updateAllFlows( buildingPower_kW / building.getCapacityHeat_kW() );
        		gasBurnerAsset.f_updateAllFlows( 0.0 );
        	}
        	else {
        		double buildingPower_kW = gasBurnerAsset.getOutputCapacity_kW() - heatDemand_kW;
        		gasBurnerAsset.f_updateAllFlows( 1.0 );
        		building.f_updateAllFlows( buildingPower_kW / building.getCapacityHeat_kW() );
        		heatPumpAsset.f_updateAllFlows( 0.0 );
        	}
    	}
    	else {
        	if (heatPumpAsset.getCOP() > 3.0 ) {
        		heatPumpAsset.f_updateAllFlows( heatDemand_kW / heatPumpAsset.getOutputCapacity_kW() );
        		gasBurnerAsset.f_updateAllFlows( 0.0 );
        		building.f_updateAllFlows( 0.0 );
        	}
        	else {
        		gasBurnerAsset.f_updateAllFlows( heatDemand_kW / gasBurnerAsset.getOutputCapacity_kW() );
        		heatPumpAsset.f_updateAllFlows( 0.0 );
        		building.f_updateAllFlows( 0.0 );
        	}
    	}
    }

    public void initializeAssets() {
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (gc.p_BuildingThermalAsset == null) {
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
    	this.isInitialized = true;
    }
    public void notInitialized() {
    	this.isInitialized = false;
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