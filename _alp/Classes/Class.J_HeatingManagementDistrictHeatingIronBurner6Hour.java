/**
 * J_HeatingManagementDistrictHeatingIronBurner6Hour
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_HeatingManagementDistrictHeatingIronBurner6Hour implements I_HeatingManagement {

	private boolean isInitialized = false;
	
	private GridConnection gc;
    private J_TimeParameters timeParameters;
    
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.IRON_BURNER
	);
	
	private OL_GridConnectionHeatingType currentHeatingType;
	private J_EAConversionIronBurner heatingAsset;
	private J_EAStorageHeat heatStorage;
	
	private J_HeatingPreferences heatingPreferences = null;
	
	private double currentHeatDemand_kW = 0;
	private double currentIronBurnerSetpoint_kW = 0;
	
	// Rolling horizon control settings
	private double forecastHorizon_h = 6.0;
	private double forecastUpdateTimeRate_h = 6.0;
	private double correctionHorizon_h = 24.0;

	private double[] heatDemandForecast_kW;
	private double[] ironBurnerSetpointSchedule_kW;
	
	// Buffer control settings
	private double targetBufferSOC_fr = 0.6;
	private double maxBufferSOC_fr = 0.95;

    private boolean debugMode = true;
    
    // KPI's 
    private double totalHeatDemand_kWh = 0;
    private double totalHeatDemandCovered_kWh = 0;

    private double burnerOperatingHours_h = 0;
    private double burnerProducedHeat_kWh = 0;

    private double lowestBufferSOC_fr = 1.0;
    private double highestBufferSOC_fr = 0.0;
    
    private double backupHeatRequired_kWh = 0;
    private double curtailedHeat_kWh = 0;
    
    private int burnerStarts = 0;
    private boolean burnerWasOnLastStep = false;
    
    private double criticalSOC_fr = 0.20;
    private double timeBelowCriticalSOC_h = 0;
    
    private double timeAtFullBuffer_h = 0;
    // Stored
  	private double[] storedIronBurnerSetpointSchedule_kW;

	public J_HeatingManagementDistrictHeatingIronBurner6Hour() {	
	}
	
    public J_HeatingManagementDistrictHeatingIronBurner6Hour(
        GridConnection gc,
        J_TimeParameters timeParameters,
        OL_GridConnectionHeatingType heatingType
    ) {
    	if (!(gc instanceof GCDistrictHeating)) {
    		throw new RuntimeException("Impossible to connect " + this.getClass() + " to a GC that is not GCDistrictHeating");
    	}
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    	
    	this.heatStorage = (J_EAStorageHeat) findFirst(gc.c_storageAssets, j_ea -> j_ea instanceof J_EAStorageHeat);
    }

    public void manageHeating(J_TimeVariables timeVariables) {
    	if (!isInitialized) {
    		this.initializeAssets();
    	}
   
    	// 1. Calculate current heat demand from connected lower-level GCs
    	this.currentHeatDemand_kW = getCurrentHeatDemand_kW(); 

    	double dt_h = timeParameters.getTimeStep_h();

    	double heatDemand_kWh = currentHeatDemand_kW * dt_h;
    	double availableBufferEnergyBeforeDischarge_kWh = getBufferEnergy_kWh();

    	double coveredHeat_kWh =
    	    min(heatDemand_kWh, availableBufferEnergyBeforeDischarge_kWh);

    	double unmetDemand_kWh =
    	    max(0, heatDemand_kWh - availableBufferEnergyBeforeDischarge_kWh);

    	totalHeatDemand_kWh += heatDemand_kWh;
    	totalHeatDemandCovered_kWh += coveredHeat_kWh;
    	backupHeatRequired_kWh += unmetDemand_kWh;
    	
    	// 2. Deliver heat from heat storage first
    	gc.f_updateFlexAssetFlows(
            heatStorage,
            -this.currentHeatDemand_kW / heatStorage.getCapacityHeat_kW(),
            timeVariables
        );
        
        // 3. Update 6h rolling horizon schedule
        int index = roundToInt(timeVariables.getTimeOfDay_h() / timeParameters.getTimeStep_h());
        int indexForecastUpdateRate = roundToInt(this.forecastUpdateTimeRate_h / timeParameters.getTimeStep_h());

        if (
            roundToInt(index % indexForecastUpdateRate) == 0
            || this.ironBurnerSetpointSchedule_kW == null
        ) {
            this.heatDemandForecast_kW =
                this.getForecastFromDatabaseHeatDemand_kW(timeVariables);

            this.ironBurnerSetpointSchedule_kW =
                this.calculateIronBurnerSchedule_kW(timeVariables);
        }

        this.currentIronBurnerSetpoint_kW =
            ironBurnerSetpointSchedule_kW[roundToInt(index % indexForecastUpdateRate)];
		
		// 4. Send setpoint to iron burner
		gc.f_updateFlexAssetFlows(
            heatingAsset,
            currentIronBurnerSetpoint_kW / heatingAsset.getOutputHeatCapacity_kW(),
            timeVariables
        );
    	
    	// 5. Charge buffer from iron burner output
    	double burnerHeatFlow_kW = heatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT);
    	double bufferCharge_kW = max(0, -burnerHeatFlow_kW);
    	
    	gc.f_updateFlexAssetFlows(
            heatStorage,
            bufferCharge_kW / heatStorage.getCapacityHeat_kW(),
            timeVariables
        );
    	
    	// 6. Update KPIs
    	double burnerOutput_kW = currentIronBurnerSetpoint_kW;
    	double burnerOutput_kWh = burnerOutput_kW * dt_h;
    	double bufferSOC_fr = getBufferSOC_fr();

    	double storageHeatFlow_kW =
    	    heatStorage.getLastFlows().get(OL_EnergyCarriers.HEAT);

    	// Check sign convention if needed.
    	// If delivered heat becomes zero while demand exists, change this to max(0, -storageHeatFlow_kW).
    	double deliveredHeat_kW =
    	    max(0, storageHeatFlow_kW);

    	if (burnerOutput_kW > 0.001) {
    	    burnerOperatingHours_h += dt_h;
    	    burnerProducedHeat_kWh += burnerOutput_kWh;
    	}

    	boolean burnerOnNow = burnerOutput_kW > 0;

    	if (burnerOnNow && !burnerWasOnLastStep) {
    	    burnerStarts++;
    	}

    	burnerWasOnLastStep = burnerOnNow;

    	lowestBufferSOC_fr = min(lowestBufferSOC_fr, bufferSOC_fr);
    	highestBufferSOC_fr = max(highestBufferSOC_fr, bufferSOC_fr);

    	if (bufferSOC_fr < criticalSOC_fr) {
    	    timeBelowCriticalSOC_h += dt_h;
    	}

    	if (bufferSOC_fr >= maxBufferSOC_fr) {
    	    timeAtFullBuffer_h += dt_h;
    	}

    	// Approximation: excess/curtailed heat is counted when the buffer is full 
    	// and the burner is still producing.
    	double curtailedPower_kW = 0;

    	if (bufferSOC_fr >= maxBufferSOC_fr && burnerOutput_kW > currentHeatDemand_kW) {
    	    curtailedPower_kW = burnerOutput_kW - currentHeatDemand_kW;
    	}

    	curtailedHeat_kWh += curtailedPower_kW * dt_h;

    	if (timeVariables.getT_h() > 8759) {

    	    traceln("Heat demand covered [%]: " + getHeatDemandCovered_pct());
    	    traceln("Backup heat required [MWh]: " + getBackupHeatRequired_MWh());
    	    traceln("Curtailment / excess heat [MWh]: " + getCurtailedHeat_MWh());
    	    traceln("Iron burner operating hours [h]: " + getBurnerOperatingHours_h());
    	    traceln("Number of burner starts [-]: " + getBurnerStarts());
    	    traceln("Average burner load during operation [kW]: " + getAverageBurnerLoadDuringOperation_kW());
    	    traceln("Lowest state of charge [%]: " + getLowestBufferSOC_pct());
    	    traceln("Highest state of charge [%]: " + getHighestBufferSOC_pct());
    	    traceln("Time below critical SoC [h]: " + getTimeBelowCriticalSOC_h());
    	    traceln("Time at full buffer [h]: " + getTimeAtFullBuffer_h());
    	}
    }
    
    private double getCurrentHeatDemand_kW() {
        double currentDemand_kW = 0;
        for (GridNode GN : gc.p_parentNodeHeat.f_getConnectedGridNodes()) {
        	currentDemand_kW += max(0, GN.v_currentLoad_kW);
        }
        return currentDemand_kW;
    }
    
    private double[] calculateIronBurnerSchedule_kW(J_TimeVariables timeVariables) {
    	double dt_h = timeParameters.getTimeStep_h();
    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h / dt_h);
    	
    	double forecastDemand_kWh = 0;
        for (int i = 0; i < sizeForecastHorizon; i++) {
            forecastDemand_kWh += this.heatDemandForecast_kW[i] * dt_h;
        }
        
    	double bufferCapacity_kWh = getBufferCapacity_kWh();
        double bufferSOC_fr = getBufferSOC_fr();
        
        double maxPower_kW = heatingAsset.getOutputHeatCapacity_kW();
        double minPower_kW = heatingAsset.minimumOutputHeatCapacity_kW;
        
        double desiredPower_kW = 0;

        if (bufferSOC_fr >= maxBufferSOC_fr) {
            desiredPower_kW = 0;
        } else {
            double bufferCorrectionPower_kW =
                ((targetBufferSOC_fr - bufferSOC_fr) * bufferCapacity_kWh) / correctionHorizon_h;

            double requiredProduction_kWh =
                forecastDemand_kWh + bufferCorrectionPower_kW * forecastHorizon_h;

            if (requiredProduction_kWh <= 0) {
                desiredPower_kW = 0;
            } else {
                desiredPower_kW =
                    max(minPower_kW, min(maxPower_kW, requiredProduction_kWh / forecastHorizon_h));
            }
        }
        
    	double[] ironBurnerHeatOutputSchedule_kW = new double[sizeForecastHorizon];

    	for (int i = 0; i < sizeForecastHorizon; i++) {
    		ironBurnerHeatOutputSchedule_kW[i] = desiredPower_kW;
    	}

        return ironBurnerHeatOutputSchedule_kW;
    }	
    
    private double[] getForecastFromDatabaseHeatDemand_kW(J_TimeVariables timeVariables) {
    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h / timeParameters.getTimeStep_h());
    	double[] totalHeatDemandForecast_kW = new double[sizeForecastHorizon];
    	
    	double timeAtStartForecast_h = timeVariables.getT_h();
        int indexAtStartForecast = roundToInt(timeAtStartForecast_h / timeParameters.getTimeStep_h());
    	
    	if (gc.energyModel.v_heatDemandForecast_kW == null) {
            throw new RuntimeException(
                this.getClass()
                + " requires energyModel.v_heatDemandForecast_kW, but it is null. "
                + "Make sure f_initializeHeatDemandForecast() is called in StartUp_IronFuel_H4B before the simulation starts."
            );
        }
    	
    	for (int i = 0; i < sizeForecastHorizon; i++) {
    		totalHeatDemandForecast_kW[i] =
                gc.energyModel.v_heatDemandForecast_kW[
                    roundToInt((indexAtStartForecast + i) % 35040)
                ];
    	}
        
        return totalHeatDemandForecast_kW;
    }
    
    private double[] getForecastHeatDemand_kW(J_TimeVariables timeVariables) {

    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h / timeParameters.getTimeStep_h());
    	double[] totalHeatDemandForecast_kW = new double[sizeForecastHorizon];
        
        double timeAtStartForecast_h = timeVariables.getT_h();
        int indexAtStartForecast = roundToInt(timeAtStartForecast_h / timeParameters.getTimeStep_h());
        
        for (GridNode GN : gc.p_parentNodeHeat.f_getConnectedGridNodes()) {
	        for (GridConnection childGC : GN.f_getAllLowerLVLConnectedGridConnections()) {
	            if (childGC == gc) {
	                continue;
	            }
	            
	            double currentNetworkDraw_kW =
                    Math.max(0, childGC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT));
	            
	            J_EAConversion childHeatingAsset = null;

	            for (J_EAConversion asset : childGC.c_heatingAssets) {
	                if (asset.activeConsumptionEnergyCarriers.contains(OL_EnergyCarriers.HEAT)) {
	                    childHeatingAsset = asset;
	                    break;
	                }
	            }
	            
	            if (childHeatingAsset == null) {
	                for (J_EAFlex asset : childGC.c_flexAssets) {
	                    if (
                            asset instanceof J_EAConversion
                            && ((J_EAConversion) asset).activeConsumptionEnergyCarriers.contains(OL_EnergyCarriers.HEAT)
                        ) {
	                        childHeatingAsset = (J_EAConversion) asset;
	                        break;
	                    }
	                }
	            }
	            
	            double efficiency = childHeatingAsset != null ? childHeatingAsset.getEta_r() : 1.0;
	            
	            List<J_EAProfile> heatProfiles = new ArrayList<>();

	            for (J_EAProfile profile : childGC.c_profileAssets) {
	                if (profile.energyCarrier == OL_EnergyCarriers.HEAT) {
	                    heatProfiles.add(profile);
	                }
	            }
	            
	            double currentProfileNetworkDraw_kW = 0;

	            for (J_EAProfile profile : heatProfiles) {
	                double scalar = profile.getProfileScaling_fr() * profile.getProfileUnitScaler_fr();

	                if (profile instanceof J_EAProduction) {
	                    scalar *= -1;
	                }

	                double t = timeVariables.getT_h();
	                double profileValue = profile.profilePointer.getValue(t);
	                currentProfileNetworkDraw_kW += (profileValue * scalar) / efficiency;
	            }

                double unprofiledBaseNetworkDraw_kW =
                    Math.max(0, currentNetworkDraw_kW - currentProfileNetworkDraw_kW);
	            
                double currentAmbTemp_degC =
                    gc.energyModel.pp_ambientTemperature_degC.getValue(timeVariables.getT_h());

                double pseudoHeatLossFactor =
                    unprofiledBaseNetworkDraw_kW / Math.max(0.1, 20.0 - currentAmbTemp_degC);
                
	            for (int i = 0; i < sizeForecastHorizon; i++) {
	            	double dynamicProfileDraw_kW = 0;
	            	double t = (i + indexAtStartForecast) * timeParameters.getTimeStep_h();

	                for (J_EAProfile profile : heatProfiles) {
	                    double scalar = profile.getProfileScaling_fr() * profile.getProfileUnitScaler_fr();

	                    if (profile instanceof J_EAProduction) {
	                        scalar *= -1; 
	                    }

	                    double profileValue = profile.profilePointer.getValue(t);
	                    dynamicProfileDraw_kW += (profileValue * scalar) / efficiency;
	                }
	                
                    double futureAmbTemp_degC =
                        gc.energyModel.pp_ambientTemperature_degC.getValue(t);

                    double futureDynamicSpaceHeating_kW =
                        pseudoHeatLossFactor * Math.max(0, 20.0 - futureAmbTemp_degC);
	                
                    totalHeatDemandForecast_kW[i] +=
                        Math.max(0, dynamicProfileDraw_kW + futureDynamicSpaceHeating_kW);
	        	}
	        }
        }

        return totalHeatDemandForecast_kW;
    }
    
    public void initializeAssets() {
    	
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	
    	if (gc.p_parentNodeHeat == null) {
    		throw new RuntimeException(this.getClass() + " requires the GC: " + gc.p_gridConnectionID + " to be connected to a GridNodeHeat");
    	}
    	
    	if (gc.p_BuildingThermalAsset != null) {
    		throw new RuntimeException(this.getClass() + " does not support a building asset.");
    	}
    	
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	
        this.heatingAsset = (J_EAConversionIronBurner) gc.c_heatingAssets.get(0);
        
        if (this.heatStorage == null) {
        	throw new RuntimeException(this.getClass() + " requires a J_EAStorageHeat asset.");
        }
    	
        if (this.heatStorage.getStorageCapacity_kWh() <= 0) {
        	throw new RuntimeException(
                this.getClass()
                + " requires heatStorage.getStorageCapacity_kWh() > 0, but found "
                + this.heatStorage.getStorageCapacity_kWh()
            );
        }
    	
    	this.isInitialized = true;
    }
    
    public void notInitialized() {
    	this.isInitialized = false;
    	this.currentIronBurnerSetpoint_kW = 0;
        this.ironBurnerSetpointSchedule_kW = null;
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
    
    private double getBufferCapacity_kWh() {
        return heatStorage.getStorageCapacity_kWh();
    }
    
    private double getBufferEnergy_kWh() {
        return heatStorage.getRemainingHeatStorageHeat_kWh();
    }
    
    public double getBufferSOC_fr() {
        return getBufferEnergy_kWh() / getBufferCapacity_kWh();
    }
    
    @Override
    public boolean operatesOnGridNodeLevel() {
        return true;
    }
    
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    public void storeStatesAndReset() {
        this.currentIronBurnerSetpoint_kW = 0;

        this.storedIronBurnerSetpointSchedule_kW = this.ironBurnerSetpointSchedule_kW;
        if (this.ironBurnerSetpointSchedule_kW != null) {
            this.ironBurnerSetpointSchedule_kW = new double[this.ironBurnerSetpointSchedule_kW.length];
        }
    }

    public void restoreStates() {
        this.currentIronBurnerSetpoint_kW = 0;
        this.ironBurnerSetpointSchedule_kW = this.storedIronBurnerSetpointSchedule_kW;
    }
	
    public double getHeatDemandCovered_fr() {
        return totalHeatDemand_kWh > 0
            ? totalHeatDemandCovered_kWh / totalHeatDemand_kWh
            : 0;
    }

    public double getBurnerOperatingHours_h() {
        return burnerOperatingHours_h;
    }

    public double getAverageBurnerLoadDuringOperation_kW() {
        return burnerOperatingHours_h > 0
            ? burnerProducedHeat_kWh / burnerOperatingHours_h
            : 0;
    }

    public double getLowestBufferSOC_fr() {
        return lowestBufferSOC_fr;
    }

    public double getHighestBufferSOC_fr() {
        return highestBufferSOC_fr;
    }
    public double getHeatDemandCovered_pct() {
        return 100 * getHeatDemandCovered_fr();
    }

    public double getBackupHeatRequired_MWh() {
        return backupHeatRequired_kWh / 1000.0;
    }

    public double getCurtailedHeat_MWh() {
        return curtailedHeat_kWh / 1000.0;
    }

    public int getBurnerStarts() {
        return burnerStarts;
    }

    public double getLowestBufferSOC_pct() {
        return 100 * lowestBufferSOC_fr;
    }

    public double getHighestBufferSOC_pct() {
        return 100 * highestBufferSOC_fr;
    }

    public double getTimeBelowCriticalSOC_h() {
        return timeBelowCriticalSOC_h;
    }

    public double getTimeAtFullBuffer_h() {
        return timeAtFullBuffer_h;
    }
	@Override
	public String toString() {
		return super.toString();
	}
}