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
    
	/**
     * Default constructor
     */
    public J_HeatingManagementBuildingHybridHeatPump() {
    	
    }
    
    public J_HeatingManagementBuildingHybridHeatPump( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	//Adjust the hot water and overall heat demand with the buffer and pt
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	double remainingHotWaterDemand_kW = managePTAndHotWaterHeatBuffer(hotWaterDemand_kW);
    	
    	double otherHeatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

    	double buildingHeatingDemand_kW = 0;
    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		if (buildingTemp_degC < heatingPreferences.getNightTimeSetPoint_degC()) { 			
    			buildingHeatingDemand_kW = (heatingPreferences.getNightTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / gc.energyModel.p_timeStep_h;
    		}
    	}
    	else {
    		if (buildingTemp_degC < heatingPreferences.getDayTimeSetPoint_degC()) {
    			buildingHeatingDemand_kW = (heatingPreferences.getDayTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / gc.energyModel.p_timeStep_h;
    		}
    	}

    	
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
    	
    	//Update flows with found asset powers
		heatPumpAsset.f_updateAllFlows( heatpumpAssetPower_kW / heatPumpAsset.getOutputCapacity_kW() );
		gasBurnerAsset.f_updateAllFlows( gasBurnerAssetPower_kW / gasBurnerAsset.getOutputCapacity_kW());
		building.f_updateAllFlows( heatIntoBuilding_kW / building.getCapacityHeat_kW() );
		
    }
    
    private double  managePTAndHotWaterHeatBuffer(double hotWaterDemand_kW){
    	
    	//Calculate the pt production
    	double ptProduction_kW = 0;
    	List<J_EAProduction> ptAssets = findAll(gc.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL);
    	for (J_EA j_ea : ptAssets) {
    		ptProduction_kW -= j_ea.getLastFlows().get(OL_EnergyCarriers.HEAT);
    	}
    	
    	//Calculate the remaining hot water energy need after pt production, also calculate the remaining unused pt production
    	double remainingHotWater_kW = max(0, hotWaterDemand_kW - ptProduction_kW); // Need to do this, because pt has already compensated the hot water demand in the gc flows, so just need to update this value
    	double remainingPTProduction_kW = max(0, ptProduction_kW - hotWaterDemand_kW);
    	
    	if(gc.p_heatBuffer != null){
    		double chargeSetpoint_kW = 0;
    		if(remainingHotWater_kW > 0) {
    			chargeSetpoint_kW = -remainingHotWater_kW;
    		}
    		else if(remainingPTProduction_kW > 0) {
    			chargeSetpoint_kW = remainingPTProduction_kW;
    		}
    		gc.p_heatBuffer.v_powerFraction_fr = chargeSetpoint_kW / gc.p_heatBuffer.getCapacityHeat_kW();
    		gc.p_heatBuffer.f_updateAllFlows(gc.p_heatBuffer.v_powerFraction_fr);
    		
			double heatBufferCharge_kW = gc.p_heatBuffer.getLastFlows().get(OL_EnergyCarriers.HEAT);
			
    		if(remainingHotWater_kW > 0){//Only if the current pt production, wasnt enough, adjust the hotwater demand with the buffer, cause then the buffer will have tried to discharge
    			remainingHotWater_kW = max(0, remainingHotWater_kW + heatBufferCharge_kW);
    		}
    		else {//Curtail the remaining pt that is not used for hot water
    			remainingPTProduction_kW = max(0, remainingPTProduction_kW - heatBufferCharge_kW);
    	    	if (remainingPTProduction_kW > 0) {//Heat (for now always curtail over produced heat!)
    	    		for (J_EAProduction j_ea : ptAssets) {
    	    			remainingPTProduction_kW -= j_ea.curtailEnergyCarrierProduction( OL_EnergyCarriers.HEAT, remainingPTProduction_kW);
    	    			
    	    			if (remainingPTProduction_kW <= 0) {
    	    				break;
    	    			}
    	    		}
    	    	}
    		}
    	}
    	return remainingHotWater_kW;
    }

    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	J_EAProduction ptAsset = findFirst(gc.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL);
    	if (ptAsset != null) {
        	if(gc.p_DHWAsset == null) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand to make sense to use this heating management with PT.");
        	}
    	}
    	if (gc.p_heatBuffer != null) {
        	if(gc.p_DHWAsset == null && ptAsset == null) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand and PT to make sense to use this heating management with a heatbuffer.");
        	}
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
    
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    

	//Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
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