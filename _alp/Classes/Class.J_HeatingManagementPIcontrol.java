
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
    
    public double startOfDay_h = 8;
    public double startOfNight_h = 23;
    public double dayTimeSetPoint_degC = 19;
    public double nightTimeSetPoint_degC = 19;
    public double heatingKickinTreshhold_degC = 0;// -> If not 0, need to create better management / system definition, else on/off/on/off behaviour.
	    
    // PI control gains
    private double P_gain_kWpDeg = 1;
    private double I_gain_kWphDeg = 0.1;
    private double I_state_kW = 0;
    private double timeStep_h;
    /**
     * Default constructor
     */
    public J_HeatingManagementPIcontrol() {
    }

    public J_HeatingManagementPIcontrol( GridConnection gc,OL_GridConnectionHeatingType heatingType ) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    	this.timeStep_h = gc.energyModel.p_timeStep_h;
    }
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	
    	//Adjust the hot water and overall heat demand with the buffer and pt
    	//double remainingHotWaterDemand_kW = managePTAndHotWaterHeatBuffer(hotWaterDemand_kW);
    	
    	//double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	
    	double heatIntoBuilding_kW = 0;

    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	double buildingHeatingDemand_kW = 0;
    	
    	double currentSetpoint_degC = dayTimeSetPoint_degC;
    	/*if (timeOfDay_h < startOfDay_h || timeOfDay_h >= startOfNight_h) {
    		currentSetpoint_degC = nightTimeSetPoint_degC;
    	}
    	else {
    		currentSetpoint_degC = dayTimeSetPoint_degC;
    	}*/
    	
    	double deltaT_degC = currentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed
    	I_state_kW = max(0,I_state_kW + deltaT_degC * I_gain_kWphDeg * timeStep_h);
    	buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDeg + I_state_kW);
    	
    	//traceln("PI control for heating: deltaT: %s, proportional feedback: %s kW, integral feedback: %s kW", deltaT_degC, deltaT_degC * P_gain_kWpDeg, I_state_kW);
    	
    	double assetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + hotWaterDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.
		heatingAsset.f_updateAllFlows( assetPower_kW / heatingAsset.getOutputCapacity_kW() );
		
		heatIntoBuilding_kW = max(0, assetPower_kW - hotWaterDemand_kW);    			
		building.f_updateAllFlows( heatIntoBuilding_kW / building.getCapacityHeat_kW() );

    }
    
    
    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	/*J_EAProduction ptAsset = findFirst(gc.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL);
    	if (ptAsset != null) {
        	if(gc.p_DHWAsset == null) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand to make sense to use this heating management with PT.");
        	}
    	}
    	if (gc.p_heatBuffer != null) {
        	if(gc.p_DHWAsset == null && ptAsset == null) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand and PT to make sense to use this heating management with a heatbuffer.");
        	}
    	}*/
    	if(gc.p_BuildingThermalAsset != null) {
        	this.building = gc.p_BuildingThermalAsset;
    	} else {
    		throw new RuntimeException(this.getClass() + " can only be used for temperature control of a building thermal asset.");
    	}
    	//J_EAConsumption heatConsumption = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND);
    	//J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	/*if (heatProfile == null && heatConsumption == null && this.building == null) {
    		throw new RuntimeException(this.getClass() + " requires a heat demand asset.");
    	}*/
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	// TODO: Add a check if the power of the asset is sufficient?
    	// TODO: Add a check if the heatingAsset is of the correct type, e.g. not a hydrogen burner or not a CHP.
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

}