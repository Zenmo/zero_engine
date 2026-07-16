
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
    private J_EAConversionAirConditioner AC;
	private J_HeatingPreferences heatingPreferences;
	private J_EAStorageHeat hotWaterBuffer;
	private List<J_EAProduction> ptAssets;
    private boolean hasPT = false;
    private boolean hasHotWaterBuffer = false;
    
    // PI control gains
    private double P_gain_kWpDegC = 1*1;
    private double I_gain_kWphDegC = 0.1*2;
    private double I_state_hDegC = 0;
    private double I_state_AC_hDegC = 0;
    private boolean AC_active = false;
    
    //Temperature setpoint low pass filter
    private double filteredCurrentSetpoint_degC;
    private double setpointFilterTimeScale_h = 2.0; // Smooth in X hours
    
    //Stored parameters
    private double storedI_state_hDegC;
    private double storedI_state_AC_hDegC;
    private double storedFilteredCurrentSetpoint_degC;
    private boolean AC_active_stored = false;
    /**
     * Default constructor
     */
    public J_HeatingManagementPIcontrol() {
    }

    public J_HeatingManagementPIcontrol( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    }
    
    public void manageHeating(J_TimeVariables timeVariables) {
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
	    	double remainingHotWaterDemand_kW = J_HeatingFunctionLibrary.managePTAndHotWaterHeatBuffer(hotWaterBuffer, ptAssets, hotWaterDemand_kW, timeVariables, gc); // This function updates the buffer and curtails PT if needed -> current balanceflow is updated accordingly.
	    	currentHeatDemand_kW += remainingHotWaterDemand_kW;
    	}
    	else if(this.hasHotWaterBuffer) {
    		double heatDemandFromHeatingAssetForHotWater_kW = J_HeatingFunctionLibrary.manageHotWaterHeatBuffer(this.hotWaterBuffer, hotWaterDemand_kW, availableAssetPowerForHotWater_kWth, timeParameters.getTimeStep_h(), timeVariables, gc);
    		currentHeatDemand_kW += heatDemandFromHeatingAssetForHotWater_kW;
    	}
    	else {
    		currentHeatDemand_kW += hotWaterDemand_kW;
    	}
    	

    	double buildingTemp_degC = building.getCurrentTemperature();
    	double timeOfDay_h = timeVariables.getTimeOfDay_h();
    	double buildingHeatingDemand_kW = 0;
    	
    	J_HeatingFunctionLibrary.setWindowVentilation_fr(this.building, heatingPreferences.getWindowOpenSetpoint_degc() ); 
    	
     	double avgTemp24h_degC = gc.energyModel.pf_ambientTemperature_degC.getForecast();
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();     	
    	if(avgTemp24h_degC > J_HeatingFunctionLibrary.heatingDaysAvgTempTreshold_degC) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    		if (this.AC != null && buildingTemp_degC > heatingPreferences.getMaxComfortTemperature_degC() && !AC_active ) {
    			AC_active = true;
    		}
    	} else {
    		if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    			currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    		}
    		if (AC_active) {
    			AC_active = false;
    			I_state_AC_hDegC = 0;
    		}
    	}
    	
    	//Smooth the setpoint signal
    	this.filteredCurrentSetpoint_degC += 1/(this.setpointFilterTimeScale_h / timeParameters.getTimeStep_h()) * (currentSetpoint_degC - this.filteredCurrentSetpoint_degC);
    	
    	
    	double deltaT_degC = this.filteredCurrentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed

    	I_state_hDegC = max(0,I_state_hDegC + deltaT_degC * timeParameters.getTimeStep_h()); // max(0,...) to prevent buildup of negative integrator during warm periods.
    	buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDegC + I_state_hDegC * I_gain_kWphDegC);
    	
    	
    	double heatingAssetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + currentHeatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.
    	gc.f_updateFlexAssetFlows(heatingAsset, heatingAssetPower_kW / heatingAsset.getOutputCapacity_kW(), timeVariables);

    	double coolingPower_kW = 0;
    	
    	if (AC_active) { 
    		double deltaT_cooling_degC = (building.getCurrentTemperature() - heatingPreferences.getMaxComfortTemperature_degC());
    		if (deltaT_cooling_degC < -1) {
    			this.AC_active=false;
    			//traceln("Building temp more than 1 degree below maxcomfort, turning off AC!");
    		} else {
	        	I_state_AC_hDegC = max(0,I_state_AC_hDegC + deltaT_cooling_degC * timeParameters.getTimeStep_h()); // max(0,...) to prevent buildup of negative integrator during warm periods.
	        	coolingPower_kW = min(AC.getOutputCapacity_kW(),max(0,(deltaT_cooling_degC * P_gain_kWpDegC * 2 + I_state_AC_hDegC * I_gain_kWphDegC))); // max(0,...), so only cooling allowed, no heating.
	        	/*if (coolingPower_kW > 0) {
	        		traceln("Airconditioner active! Cooling power: %s kW", coolingPower_kW);
	        		traceln("Current building temperature: %s deg C", buildingTemp_degC);
	        		traceln("MaxComfortTemp: %s", heatingPreferences.getMaxComfortTemperature_degC());
	        	}*/
    		}
        	gc.f_updateFlexAssetFlows(AC, coolingPower_kW / AC.getOutputCapacity_kW(), timeVariables);
        	
    	} 
    	
		double heatIntoBuilding_kW = max(0, heatingAssetPower_kW - currentHeatDemand_kW);    			
	    	
		gc.f_updateFlexAssetFlows(building, (heatIntoBuilding_kW-coolingPower_kW) / building.getCapacityHeat_kW(), timeVariables);

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
    	if ( gc instanceof GCHouse house) {
    		if (house.p_airco!=null) {
    			this.AC = house.p_airco;
    		} else {
    			this.AC = null;
    			this.AC_active = false;
    			this.I_state_AC_hDegC = 0;
    		}
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
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		int timeStepsInForecast = roundToInt((timeOfIntervalEnd_h - timeOfIntervalStart_h) / this.timeParameters.getTimeStep_h());
		Double[] loadProfile_kW = new Double[timeStepsInForecast];
		J_ProfilePointer ambientTemperatureProfile = this.gc.energyModel.pp_ambientTemperature_degC;
		J_ProfileForecaster ambientTempeartureForecast = this.gc.energyModel.pf_ambientTemperature_degC;
		double[] buildingHeatDemand_kW = this.getBuildingHeatDemandProfile(timeOfIntervalStart_h, timeStepsInForecast, ambientTemperatureProfile, ambientTempeartureForecast);
		double[] otherFixedHeatDemand_kW = this.gc.f_getFixedAssetForecast(timeOfIntervalStart_h, timeOfIntervalEnd_h, OL_EnergyCarriers.HEAT, this.timeParameters);
		
		// We switch over the heating type twice, first to set the EnergyCarrier
		OL_EnergyCarriers EC;
		switch (this.gc.f_getCurrentHeatingType()) {
		case GAS_BURNER:
			EC = OL_EnergyCarriers.METHANE;
			break;
		case HYDROGENBURNER:
			EC = OL_EnergyCarriers.HYDROGEN;
			break;
		case LT_DISTRICTHEAT:
		case ELECTRIC_HEATPUMP:
			EC = OL_EnergyCarriers.ELECTRICITY;
			break;
		case DISTRICTHEAT:	
			Double[] heatLoad = Arrays.stream(buildingHeatDemand_kW).boxed().toArray(Double[]::new);
			loadMap.put(OL_EnergyCarriers.HEAT, heatLoad);
			return new J_AssetTypeForecast(J_HeatingManagementPIcontrol.class, loadMap, OL_ForecastStatus.ESTIMATED_FORECAST, "GC connected to districtheating, so all heat demand is import. Building forecast simplified by omitting solar radiation & ventilation.");
		default:
			throw new RuntimeException(String.format("Tried to forecast J_HeatingManagementPIControl, but encountered an unexepected heating type %s in GC %s", this.gc.f_getCurrentHeatingType(), this.gc.p_gridConnectionID));
		}
		
		// Then to determine the profile.
		// Here we make a distinction between fixed efficiency, and efficiency variable per timestep.
		boolean fixedEfficiency = false;
		double fixedEfficiency_fr = 1.0;
		J_EAConversionHeatPump hp = null;
		switch (this.gc.f_getCurrentHeatingType()) {
		case LT_DISTRICTHEAT:
			fixedEfficiency = true;
			hp = ((J_EAConversionHeatPump)this.heatingAsset);
			fixedEfficiency_fr = hp.getCOP();
			break;
		case GAS_BURNER:
		case HYDROGENBURNER:
			fixedEfficiency = true;
			fixedEfficiency_fr = this.heatingAsset.getEta_r();
			break;
		case ELECTRIC_HEATPUMP:
			hp = ((J_EAConversionHeatPump)this.heatingAsset);
			for (int i = 0; i < timeStepsInForecast; i++) {
				double t = timeOfIntervalStart_h + i * timeParameters.getTimeStep_h();
				double heatDemand_kW = buildingHeatDemand_kW[i] + otherFixedHeatDemand_kW[i];
				double efficiency_fr = hp.calculateCOP(hp.getOutputTemperature_degC(), ambientTemperatureProfile.getValue(t));
				double load_kW = heatDemand_kW / efficiency_fr;
				loadProfile_kW[i] = load_kW;
			}
			break;
		}
		
		if (fixedEfficiency) {
			for (int i = 0; i < timeStepsInForecast; i++) {
				double t = timeOfIntervalStart_h + i * timeParameters.getTimeStep_h();
				double heatDemand_kW = buildingHeatDemand_kW[i] + otherFixedHeatDemand_kW[i];
				double load_kW = heatDemand_kW / fixedEfficiency_fr;
				loadProfile_kW[i] = load_kW;
			}
		}
		loadMap.put(EC, loadProfile_kW);
		// Building is a flex asset so its heat demand is included in system bounds of heating management, but fixed profiles are not.
		Double[] heatLoad = Arrays.stream(otherFixedHeatDemand_kW).map(d -> -d).boxed().toArray(Double[]::new);
		loadMap.put(OL_EnergyCarriers.HEAT, heatLoad);
		OL_ForecastStatus status = OL_ForecastStatus.ESTIMATED_FORECAST;
		String reason = "PI & Building states based on current timestep. Building forecast simplified by omitting solar radiation & ventilation.";
		return new J_AssetTypeForecast(I_HeatingManagement.class, loadMap, status, reason);
	}
	
	private double[] getBuildingHeatDemandProfile(double timeAtStartForecast_h, int timeStepsInForecast, J_ProfilePointer ambientTemperatureProfile, J_ProfileForecaster ambientTempeartureForecast) {
        double[] buildingHeatDemandProfile_kW = new double[timeStepsInForecast];

        double dayStartTime_h = this.heatingPreferences.getStartOfDayTime_h();
        double nightStartTime_h = this.heatingPreferences.getStartOfNightTime_h();
        double daySetpoint_degC = this.heatingPreferences.getDayTimeSetPoint_degC();
        double nightSetpoint_degC = this.heatingPreferences.getNightTimeSetPoint_degC();
        double avgTemp24h_degC = ambientTempeartureForecast.getForecast(); // Assumes timeAtStartForecast_h is current model time and forecasting horizon is 24h.

        double simBuildingTemp_degC = this.building.getCurrentTemperature();
        double simFilteredSetpoint_degC = this.filteredCurrentSetpoint_degC;
        double simIState_hDegC = this.I_state_hDegC;

        double pGain_kWpDegC = this.P_gain_kWpDegC;
        double iGain_kWphDegC = this.I_gain_kWphDegC;
        double filterTimeScale_h = this.setpointFilterTimeScale_h;
        double timeStep_h = this.timeParameters.getTimeStep_h();

        double lossFactor_WpK = this.building.getLossFactor_WpK();
        double lossScalingFactor_fr = this.building.getLossScalingFactor_fr();
        double heatCapacity_kWhpK = this.building.getHeatCapacity_JpK() / 3.6e6;

        for (int i = 0; i < timeStepsInForecast; i++) {
            double t = timeAtStartForecast_h + i * timeStep_h;
            double timeOfDay_h = t % 24.0;
            double ambientTemp_degC = ambientTemperatureProfile.getValue(t);

            // Same setpoint-selection logic as manageHeating, incl. the heating-season hysteresis
            double currentSetpoint_degC = daySetpoint_degC;
            if (avgTemp24h_degC > J_HeatingFunctionLibrary.heatingDaysAvgTempTreshold_degC) {
                currentSetpoint_degC = nightSetpoint_degC;
            } else if (timeOfDay_h < dayStartTime_h || timeOfDay_h >= nightStartTime_h) {
                currentSetpoint_degC = nightSetpoint_degC;
            }

            // Same order of operations as manageHeating: filter first, then deltaT vs *previous* building temp
            simFilteredSetpoint_degC += (timeStep_h / filterTimeScale_h) * (currentSetpoint_degC - simFilteredSetpoint_degC);
            double deltaT_degC = simFilteredSetpoint_degC - simBuildingTemp_degC;
            simIState_hDegC = max(0, simIState_hDegC + deltaT_degC * timeStep_h);
            double heatingDemand_kW = max(0, deltaT_degC * pGain_kWpDegC + simIState_hDegC * iGain_kWphDegC);

            buildingHeatDemandProfile_kW[i] = heatingDemand_kW;

            // Advance building temperature under this heat input for the next step
            double heatLoss_kW = (lossFactor_WpK * (simBuildingTemp_degC - ambientTemp_degC) / 1000) * lossScalingFactor_fr;
            double netHeat_kW = heatingDemand_kW - heatLoss_kW;
            simBuildingTemp_degC += (netHeat_kW / heatCapacity_kWhpK) * timeStep_h;
        }

        return buildingHeatDemandProfile_kW;
    }
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
	    this.storedI_state_hDegC = this.I_state_hDegC;
	    this.storedI_state_AC_hDegC = this.I_state_AC_hDegC;
		this.I_state_hDegC = 0;
		this.I_state_AC_hDegC = 0;
		this.AC_active_stored = this.AC_active;
	    this.storedFilteredCurrentSetpoint_degC = this.filteredCurrentSetpoint_degC;
		this.filteredCurrentSetpoint_degC = 0;

	}
	public void restoreStates() {
		this.I_state_hDegC = this.storedI_state_hDegC;
		this.I_state_AC_hDegC = this.storedI_state_AC_hDegC;
		this.AC_active = this.AC_active_stored;
	    this.filteredCurrentSetpoint_degC = this.storedFilteredCurrentSetpoint_degC;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

}