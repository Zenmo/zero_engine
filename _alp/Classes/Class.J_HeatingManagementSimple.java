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
    private J_EAConversionAirConditioner AC;
    private J_HeatingPreferences heatingPreferences;
	private J_EAStorageHeat hotWaterBuffer;
	private List<J_EAProduction> ptAssets;
    private boolean hasPT = false;
    
    // PI control gains for AC
    private double P_gain_kWpDegC = 1*1;
    private double I_gain_kWphDegC = 0.1*2;
    //private double I_state_hDegC = 0;
    private double I_state_AC_hDegC = 0;
    private boolean AC_active = false;
    
    //Stored parameters
    private double storedI_state_AC_hDegC;
    private boolean AC_active_stored = false;
    
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
	    	if(avgTemp24h_degC > J_HeatingFunctionLibrary.heatingDaysAvgTempTreshold_degC) {
	    		buildingHeatingDemand_kW = max(0, heatingPreferences.getNightTimeSetPoint_degC() - buildingTemp_degC) * this.building.heatCapacity_JpK / 3.6e6 / timeParameters.getTimeStep_h();	
	    		if (this.AC != null && !AC_active && buildingTemp_degC > heatingPreferences.getMaxComfortTemperature_degC()  ) {
	    			//traceln("Enabling airconditioner!");
	    			AC_active = true;
	    		}
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
	    		if (AC_active) {
	    			//traceln("Disabling airconditioner!");
	    			AC_active = false;
	    			I_state_AC_hDegC = 0;
	    		}
	    	}
	    	heatingAssetPower_kW = min(heatingAsset.getOutputCapacity_kW(),buildingHeatingDemand_kW + heatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.			
			double heatIntoBuilding_kW = max(0, heatingAssetPower_kW - heatDemand_kW); // Will lead to energy(heat) imbalance when heatDemand_kW is larger than heating asset capacity.
	    	
	    	double coolingPower_kW = 0;
	    	
	    	if (AC_active) { 
	    		double deltaT_cooling_degC = (building.getCurrentTemperature() - heatingPreferences.getMaxComfortTemperature_degC());
	    		if (deltaT_cooling_degC < -1) {
	    			this.AC_active=false;
	    			//traceln("Building temp more than 1 degree below maxcomfort, turning off AC!");
	    		} else {
		        	I_state_AC_hDegC = max(0,I_state_AC_hDegC + deltaT_cooling_degC * timeParameters.getTimeStep_h()); // max(0,...) to prevent buildup of negative integrator during warm periods.
		        	coolingPower_kW = min(AC.getOutputCapacity_kW(),max(0,(deltaT_cooling_degC * P_gain_kWpDegC * 2 + I_state_AC_hDegC * I_gain_kWphDegC))); // max(0,...), so only cooling allowed, no heating.
		        	if (coolingPower_kW > 0) {
		        		//traceln("Airconditioner active! Cooling power: %s kW, building temp: %s, maxComfortTemp: %s", coolingPower_kW, buildingTemp_degC, heatingPreferences.getMaxComfortTemperature_degC());
		        		//traceln("Current building temperature: %s deg C", buildingTemp_degC);
		        		//traceln("MaxComfortTemp: %s", heatingPreferences.getMaxComfortTemperature_degC());
		        	}
	    		}
	        	gc.f_updateFlexAssetFlows(AC, coolingPower_kW / AC.getOutputCapacity_kW(), timeVariables);	        	
	    	} 
			
			//gc.f_updateFlexAssetFlows(building, heatIntoBuilding_kW / building.getCapacityHeat_kW(), timeVariables);
			gc.f_updateFlexAssetFlows(building, (heatIntoBuilding_kW-coolingPower_kW) / building.getCapacityHeat_kW(), timeVariables);

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
    	J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	J_EAFlexProfile heatFlexProfile = findFirst(gc.c_flexProfileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	if (heatProfile == null && heatFlexProfile == null && this.building == null) {
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
    	if ( gc instanceof GCHouse house) {
    		if (house.p_airco!=null) {
    			if (this.building == null) {
    				throw new RuntimeException("AirConditioner can only be used in combination with J_EABuilding thermal model, but no J_EABuilding present on gridconnection!");
    			}
    			this.AC = house.p_airco;
    		} else {
    			this.AC = null;
    			this.AC_active = false;
    			this.I_state_AC_hDegC = 0;
    		}
    	}

    	this.isInitialized = true;
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
			if (this.building == null) {
				String reason = "GC connected to districtheating, so all heat demand is import.";
				return new J_AssetTypeForecast(J_HeatingManagementPIcontrol.class, loadMap, OL_ForecastStatus.PERFECT_FORECAST, reason);
			}
			else {
				String reason = "GC connected to districtheating, so all heat demand is import. Building forecast simplified by omitting solar radiation & ventilation.";
				return new J_AssetTypeForecast(J_HeatingManagementPIcontrol.class, loadMap, OL_ForecastStatus.ESTIMATED_FORECAST, reason);
			}
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
		// Building is a flex asset so included in system bounds of heating management, but fixed profiles are not.
		Double[] heatLoad = Arrays.stream(otherFixedHeatDemand_kW).map(d -> -d).boxed().toArray(Double[]::new);
		loadMap.put(OL_EnergyCarriers.HEAT, heatLoad);
		if (this.building == null) {
			OL_ForecastStatus status = OL_ForecastStatus.PERFECT_FORECAST;
			String reason = "Forecast is perfect for GridConnections without ThermalBuilding.";
			return new J_AssetTypeForecast(I_HeatingManagement.class, loadMap, status, reason);
		}
		else {
			OL_ForecastStatus status = OL_ForecastStatus.ESTIMATED_FORECAST;
			String reason = "Temperature & Building states based on current timestep. Building forecast simplified by omitting solar radiation & ventilation.";
			return new J_AssetTypeForecast(I_HeatingManagement.class, loadMap, status, reason);
		}
	}
	
	private double[] getBuildingHeatDemandProfile(double timeAtStartForecast_h, int timeStepsInForecast, J_ProfilePointer ambientTemperatureProfile, J_ProfileForecaster ambientTempeartureForecast) {
        double[] buildingHeatDemandProfile_kW = new double[timeStepsInForecast];
        if (this.building == null) {
        	return buildingHeatDemandProfile_kW;
        }
        double dayStartTime_h = this.heatingPreferences.getStartOfDayTime_h();
        double nightStartTime_h = this.heatingPreferences.getStartOfNightTime_h();
        double daySetpoint_degC = this.heatingPreferences.getDayTimeSetPoint_degC();
        double nightSetpoint_degC = this.heatingPreferences.getNightTimeSetPoint_degC();
        double avgTemp24h_degC = ambientTempeartureForecast.getForecast();  // Assumes timeAtStartForecast_h is current model time and forecasting horizon is 24h.
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
            double heatLoss_kW = (lossFactor_WpK * (currentSetpoint_degC - ambientTemp_degC) / 1000) * lossScalingFactor_fr;
            buildingHeatDemandProfile_kW[i] = max(0, heatLoss_kW);
        }
        return buildingHeatDemandProfile_kW;
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
	    this.storedI_state_AC_hDegC = this.I_state_AC_hDegC;
		this.I_state_AC_hDegC = 0;
		this.AC_active_stored = this.AC_active;
	}
	public void restoreStates() {
		this.I_state_AC_hDegC = this.storedI_state_AC_hDegC;
		this.AC_active = this.AC_active_stored;
	}
	
	@Override
	public String toString() {
		return "HeatingManagement Simple with heating type: " + getCurrentHeatingType().toString();
	}
}
