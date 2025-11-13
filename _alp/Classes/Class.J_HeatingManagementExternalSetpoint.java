/**
 * J_HeatingManagementExternalSetpoint
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

public class J_HeatingManagementExternalSetpoint implements I_HeatingManagement {

    private boolean isInitialized = false;
    private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
    private J_EAConversionHeatPump heatingAsset;
    private J_HeatingPreferences heatingPreferences;
    
    // PI control gains
    private double P_gain_kWpDegC = 1*1;
    private double I_gain_kWphDegC = 0.1*2;
    private double I_state_hDegC = 0;
    private double timeStep_h;
    
    //Temperature setpoint low pass filter
    private double filteredCurrentSetpoint_degC;
    private double setpointFilterTimeScale_h = 2.0; // Smooth in X hours
    
    //Setpoint
    private double currentExternalTemperatureSetpoint_degC = 0;
    
    //Stored parameters
    private double storedI_state_hDegC;
    private double storedFilteredCurrentSetpoint_degC;
    private double storedCurrentExternalTemperatureSetpoint_degC;    

    
	/**
     * Default constructor
     */
    public J_HeatingManagementExternalSetpoint() {
    	
    }
    
    public J_HeatingManagementExternalSetpoint( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
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
    
    
    public double setCurrentExternalTemperatureSetpoint_degC(Double externalTemperatureSetpoint_degC) {
    	if(externalTemperatureSetpoint_degC != null) {
    		this.currentExternalTemperatureSetpoint_degC = max(heatingPreferences.getMinComfortTemperature_degC(), min(externalTemperatureSetpoint_degC, heatingPreferences.getMaxComfortTemperature_degC()));
    	}
    	else {
    		this.currentExternalTemperatureSetpoint_degC = 0;
    	}
    	return this.currentExternalTemperatureSetpoint_degC;
    }
    
    public double getCurrentExternalTemperatureSetpoint_degC() {
    	return this.currentExternalTemperatureSetpoint_degC;
    }
    
    
    
    
    
    
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	
    	//Adjust the hot water and overall heat demand with the buffer and pt
    	double remainingHotWaterDemand_kW = managePTAndHotWaterHeatBuffer(hotWaterDemand_kW);
    	
    	double otherHeatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	double buildingHeatingDemand_kW = 0;
    	
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();
    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    	}
    	
    	if(this.currentExternalTemperatureSetpoint_degC > 0) {
    		currentSetpoint_degC = this.currentExternalTemperatureSetpoint_degC;
    	}
    		
    	//Cap the current setpoint
    	currentSetpoint_degC = max(heatingPreferences.getMinComfortTemperature_degC(), min(currentSetpoint_degC, heatingPreferences.getMaxComfortTemperature_degC()));
    	
    	//Smooth the setpoint signal
    	this.filteredCurrentSetpoint_degC += 1/(this.setpointFilterTimeScale_h / this.timeStep_h) * (currentSetpoint_degC - this.filteredCurrentSetpoint_degC);
    	
    	
    	double deltaT_degC = this.filteredCurrentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed

    	I_state_hDegC = max(0,I_state_hDegC + deltaT_degC * timeStep_h); // max(0,...) to prevent buildup of negative integrator during warm periods.
    	buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDegC + I_state_hDegC * I_gain_kWphDegC);
    	
    	
    	double assetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + otherHeatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.
		heatingAsset.f_updateAllFlows( assetPower_kW / heatingAsset.getOutputCapacity_kW() );
		
		double heatIntoBuilding_kW = max(0, assetPower_kW - otherHeatDemand_kW);    			
		building.f_updateAllFlows( heatIntoBuilding_kW / building.getCapacityHeat_kW() );
		
		//Reset external setpoint again
		this.currentExternalTemperatureSetpoint_degC = 0;
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
    	else {
    		throw new RuntimeException(this.getClass() + " requires a building thermal asset.");
    	}
    	J_EAConsumption heatConsumption = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND);
    	J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);

    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	J_EAConversion heatingAsset = gc.c_heatingAssets.get(0);
    	if (heatingAsset instanceof J_EAConversionHeatPump) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
    		this.heatingAsset = (J_EAConversionHeatPump) heatingAsset;
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
	    this.storedI_state_hDegC = this.I_state_hDegC;
	    this.storedFilteredCurrentSetpoint_degC = this.filteredCurrentSetpoint_degC;
		this.storedCurrentExternalTemperatureSetpoint_degC = this.currentExternalTemperatureSetpoint_degC;
		this.I_state_hDegC = 0;
		this.filteredCurrentSetpoint_degC = 0;
		this.currentExternalTemperatureSetpoint_degC = 0;
	}
	public void restoreStates() {
		this.I_state_hDegC = this.storedI_state_hDegC;
	    this.filteredCurrentSetpoint_degC = this.storedFilteredCurrentSetpoint_degC;
	    this.currentExternalTemperatureSetpoint_degC = this.storedCurrentExternalTemperatureSetpoint_degC;
	}
	
	@Override
	public String toString() {
		return "HeatingManagement ExternalSetpoint with heating type: " + getCurrentHeatingType().toString();
	}
}