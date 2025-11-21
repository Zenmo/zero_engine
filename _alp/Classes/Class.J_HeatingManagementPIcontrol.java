
/**
 * J_HeatingManagementPIcontrol
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

public class J_HeatingManagementPIcontrol implements I_HeatingManagement {
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
	private J_HeatingPreferences heatingPreferences;
	private J_EAStorageHeat hotWaterBuffer;
	private List<J_EAProduction> ptAssets;
    private boolean hasPT = false;
    private boolean hasHotWaterBuffer = false;
    
    // PI control gains
    private double P_gain_kWpDegC = 1*1;
    private double I_gain_kWphDegC = 0.1*2;
    private double I_state_hDegC = 0;
    private double timeStep_h;
    
    //Temperature setpoint low pass filter
    private double filteredCurrentSetpoint_degC;
    private double setpointFilterTimeScale_h = 2.0; // Smooth in X hours
    
    //Stored parameters
    private double storedI_state_hDegC;
    private double storedFilteredCurrentSetpoint_degC;
    /**
     * Default constructor
     */
    public J_HeatingManagementPIcontrol() {
    }

    public J_HeatingManagementPIcontrol( GridConnection gc,OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    	this.timeStep_h = gc.energyModel.p_timeStep_h;
    }
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	double ptAssetPower_kW = ptAssets != null ? sum(ptAssets, pt -> pt.getLastFlows().get(OL_EnergyCarriers.HEAT)) : 0;
    	double additionalHeatDemand_kW = (gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT) - hotWaterDemand_kW + (-ptAssetPower_kW));
    	
    	double currentHeatDemand_kW = additionalHeatDemand_kW;
    	double availableAssetPowerForHotWater_kWth = heatingAsset.getOutputCapacity_kW() - additionalHeatDemand_kW;
    	
    	//Manage hot water if additional systems are present
    	if(this.hasPT) {
	    	//Adjust the hot water and overall heat demand with the buffer and pt
	    	double remainingHotWaterDemand_kW = J_HeatingFunctionLibrary.managePTAndHotWaterHeatBuffer(hotWaterBuffer, ptAssets, hotWaterDemand_kW); // This function updates the buffer and curtails PT if needed -> current balanceflow is updated accordingly.
	    	currentHeatDemand_kW += remainingHotWaterDemand_kW;
    	}
    	else if(this.hasHotWaterBuffer) {
    		double heatDemandFromHeatingAssetForHotWater_kW = J_HeatingFunctionLibrary.manageHotWaterHeatBuffer(this.hotWaterBuffer, hotWaterDemand_kW, availableAssetPowerForHotWater_kWth, this.timeStep_h);
    		currentHeatDemand_kW += heatDemandFromHeatingAssetForHotWater_kW;
    	}
    	else {
    		currentHeatDemand_kW += hotWaterDemand_kW;
    	}
    	

    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	double buildingHeatingDemand_kW = 0;
    	
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();
    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    	}
    	
    	//Smooth the setpoint signal
    	this.filteredCurrentSetpoint_degC += 1/(this.setpointFilterTimeScale_h / this.timeStep_h) * (currentSetpoint_degC - this.filteredCurrentSetpoint_degC);
    	
    	
    	double deltaT_degC = this.filteredCurrentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed

    	I_state_hDegC = max(0,I_state_hDegC + deltaT_degC * timeStep_h); // max(0,...) to prevent buildup of negative integrator during warm periods.
    	buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDegC + I_state_hDegC * I_gain_kWphDegC);
    	

    	double assetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + currentHeatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.
		heatingAsset.f_updateAllFlows( assetPower_kW / heatingAsset.getOutputCapacity_kW() );
		
		double heatIntoBuilding_kW = max(0, assetPower_kW - currentHeatDemand_kW);    			
		building.f_updateAllFlows( heatIntoBuilding_kW / building.getCapacityHeat_kW() );

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
        	this.ptAssets = ptAssets;
        	this.hasPT = true;
    	}
    	if (gc.p_heatBuffer != null) {
        	if(gc.p_DHWAsset == null) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand to make sense to use this heating management with heatbuffer.");
        	}
    		this.hotWaterBuffer = gc.p_heatBuffer;
    		this.hasHotWaterBuffer = true;
    	}
    	if(gc.p_BuildingThermalAsset != null) {
        	this.building = gc.p_BuildingThermalAsset;
    	} else {
    		throw new RuntimeException(this.getClass() + " can only be used for temperature control of a building thermal asset.");
    	}
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	this.heatingAsset = gc.c_heatingAssets.get(0);
    	if (heatingAsset instanceof J_EAConversionGasBurner) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.GAS_BURNER;
    	} else if (heatingAsset instanceof J_EAConversionHeatPump) {
    		if (gc.p_parentNodeHeatID != null) {
    			this.currentHeatingType = OL_GridConnectionHeatingType.LT_DISTRICTHEAT;
    		} else {
    			this.currentHeatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
    		}
    	} else if (heatingAsset instanceof J_EAConversionHeatDeliverySet) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.DISTRICTHEAT;
    	} else if (heatingAsset instanceof J_EAConversionHydrogenBurner) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.HYDROGENBURNER;
    	} else {
    		throw new RuntimeException(this.getClass() + " Unsupported heating asset!");    		
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