/**
 * J_HeatingManagementSimple
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

public class J_HeatingManagementSimple implements I_HeatingManagement {

    private boolean isInitialized = false;
    private GridConnection gc;
    private J_TimeParameters timeParameters;
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

	/**
     * Default constructor
     */
    public J_HeatingManagementSimple() {
    	
    }
    
    public J_HeatingManagementSimple( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    }
      
    
    public void manageHeating(J_TimeVariables timeVariables) {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	
    	if(hasPT) {//Adjust the hot water and overall heat demand with the buffer and pt
    		double remainingHotWaterDemand_kW = J_HeatingFunctionLibrary.managePTAndHotWaterHeatBuffer(hotWaterBuffer, ptAssets, hotWaterDemand_kW, timeVariables, gc); // also updates fm_currentBalanceFlows_kW(heat)!
    	}
    	
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	
    	double heatingAssetPower_kW = 0;

    	if(this.building != null) {    		
    		double buildingHeatingDemand_kW = 0;
	    	double buildingTemp_degC = building.getCurrentTemperature();
	    	double timeOfDay_h = timeVariables.getTimeOfDay_h();
	    	J_HeatingFunctionLibrary.setWindowVentilation_fr(this.building, heatingPreferences.getWindowOpenSetpoint_degc() ); 
	    	
	    	//Stookdagen approximation > boven 18 graden is niet verwarmen
	    	double avgTemp24h_degC = gc.energyModel.pf_ambientTemperature_degC.getForecast();
	    	if(avgTemp24h_degC > J_HeatingFunctionLibrary.heatingDaysSetpoint_degC) {
	    		buildingHeatingDemand_kW = max(0,J_HeatingFunctionLibrary.heatingDaysSetpoint_degC - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / timeParameters.getTimeStep_h();	
	    	}
	    	///On heating days
	    	else {
		    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
		    		
		    		if (buildingTemp_degC < heatingPreferences.getNightTimeSetPoint_degC()) {
		    			// Nighttime and building temperature too low
		    			buildingHeatingDemand_kW = (heatingPreferences.getNightTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / timeParameters.getTimeStep_h();
		    		}
		    		else {
		    			// Nighttime and building temperature acceptable
		    		}
		    	}
		    	else {
		    		if (buildingTemp_degC < heatingPreferences.getDayTimeSetPoint_degC()) {
		    			// Daytime and building temperature too low
		    			buildingHeatingDemand_kW = (heatingPreferences.getDayTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / timeParameters.getTimeStep_h();
		    		}
		    		else {
		    			// Daytime and building temperature acceptable
		    		}
		    	}
	    	}
	    	heatingAssetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + heatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.			
			double heatIntoBuilding_kW = max(0, heatingAssetPower_kW - heatDemand_kW); // Will lead to energy(heat) imbalance when heatDemand_kW is larger than heating asset capacity.
	    	gc.f_updateFlexAssetFlows(building, heatIntoBuilding_kW / building.getCapacityHeat_kW(), timeVariables);

    	} else {    	    	
    		heatingAssetPower_kW = heatDemand_kW; // Will lead to energy(heat) imbalance when heatDemand_kW is larger than heating asset capacity.
    	}
    	gc.f_updateFlexAssetFlows(heatingAsset, heatingAssetPower_kW / heatingAsset.getOutputCapacity_kW(), timeVariables);

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
        	if(gc.p_DHWAsset == null || ptAssets.size() == 0) {
        		throw new RuntimeException(this.getClass() + " requires a hot water demand and PT to make sense to use this heating management with heatbuffer.");
        	}
    		this.hotWaterBuffer = gc.p_heatBuffer;
    	}
    	if(gc.p_BuildingThermalAsset != null) {
        	this.building = gc.p_BuildingThermalAsset;
        	if(this.heatingPreferences == null) {
        		heatingPreferences = new J_HeatingPreferences();
        	}
    	}
    	J_EAConsumption heatConsumption = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND);
    	J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	if (heatProfile == null && heatConsumption == null && this.building == null) {
    		throw new RuntimeException(this.getClass() + " requires a heat demand asset.");
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
    
	public void storeStatesAndReset() {
		//Nothing to store/reset
	}
	public void restoreStates() {
		//Nothing to store/reset
	}
	
	@Override
	public String toString() {
		return "HeatingManagement Simple with heating type: " + getCurrentHeatingType().toString();
	}
}
