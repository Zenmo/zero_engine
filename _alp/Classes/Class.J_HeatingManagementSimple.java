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

	/**
     * Default constructor
     */
    public J_HeatingManagementSimple() {
    	
    }
    
    public J_HeatingManagementSimple( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }
    
    public double  managePTAndHotWaterHeatBuffer(double hotWaterDemand_kW){
    	
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
    
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	
    	//Adjust the hot water and overall heat demand with the buffer and pt
    	double remainingHotWaterDemand_kW = managePTAndHotWaterHeatBuffer(hotWaterDemand_kW); // also updates fm_currentBalanceFlows_kW(heat)!
    	
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	
    	double heatingAssetPower_kW = 0;

    	if(this.building != null) {
        	double buildingHeatingDemand_kW = 0;
	    	double buildingTemp_degC = building.getCurrentTemperature();
	    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
	    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
	    		if (buildingTemp_degC < heatingPreferences.getNightTimeSetPoint_degC()) {
	    			// Nighttime and building temperature too low
	    			buildingHeatingDemand_kW = (heatingPreferences.getNightTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / gc.energyModel.p_timeStep_h;
	    		}
	    		else {
	    			// Nighttime and building temperature acceptable
	    		}
	    	}
	    	else {
	    		if (buildingTemp_degC < heatingPreferences.getDayTimeSetPoint_degC()) {
	    			// Daytime and building temperature too low
	    			buildingHeatingDemand_kW = (heatingPreferences.getDayTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / gc.energyModel.p_timeStep_h;
	    		}
	    		else {
	    			// Daytime and building temperature acceptable
	    		}
	    	}
			
	    	heatingAssetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + heatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.			
			double heatIntoBuilding_kW = max(0, heatingAssetPower_kW - heatDemand_kW); // Will lead to energy(heat) imbalance when heatDemand_kW is larger than heating asset capacity.
			building.f_updateAllFlows( heatIntoBuilding_kW / building.getCapacityHeat_kW() );
    	} else {    	    	
    		heatingAssetPower_kW = heatDemand_kW; // Will lead to energy(heat) imbalance when heatDemand_kW is larger than heating asset capacity.
    	}
		heatingAsset.f_updateAllFlows( heatingAssetPower_kW / heatingAsset.getOutputCapacity_kW() );
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
