/**
 * J_HeatingManagementDistrictHeatingIronBurner6HourSUMMERWINTER
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
public class J_HeatingManagementDistrictHeatingIronBurner6HourSUMMERWINTER implements I_HeatingManagement {

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
	
	// Existing rolling horizon control settings
	private double forecastHorizon_h = 6.0;
	private double forecastUpdateTimeRate_h = 6.0;
	private double[] heatDemandForecast_kW;
	private double[] ironBurnerSetpointSchedule_kW;
	
	// Existing buffer control settings for normal/high-demand operation
	private double targetBufferSOC_fr = 0.6;
	private double maxBufferSOC_fr = 0.95;

    // Daily forecasted reserve control settings
    private double dailyForecastHorizon_h = 24.0;
    private double dailyPlanningHour_h = 6.0;

    // Low-average-demand threshold.
    // Below this, use forecasted reserve control instead of 6h SoC control.
    // Example: 200 kW * 1.2 = 240 kW.
    private double lowAverageDemandThresholdFactor = 1.2;

    // Reserve mode SoC limits
    private double reserveMinSOC_fr = 0.20;
    private double reserveUpperSOC_fr = 0.60;
    private double reserveMaxSOC_fr = 0.95;

    // If the buffer starts below reserve, allow gradual recovery
    private double reserveRecoveryHorizon_h = 3.0;

    // Power sizing resolution
    private double reservePowerStep_kW = 50.0;

    // Minimum ON/OFF block size for reserve mode
    private double reserveBlockLength_h = 6.0;

    private boolean dailyReserveModeLocked = false;
    private int lastDailyPlanningDayIndex = -1;
    private double dailyScheduleStartTime_h = -1;

    private double[] dailyIronBurnerSetpointSchedule_kW;

    private boolean debugMode = true;
    
    // Stored
  	private double[] storedIronBurnerSetpointSchedule_kW;
    private double[] storedDailyIronBurnerSetpointSchedule_kW;
    private boolean storedDailyReserveModeLocked;
    private double storedDailyScheduleStartTime_h;

	/**
     * Default constructor
     */
	public J_HeatingManagementDistrictHeatingIronBurner6HourSUMMERWINTER() {
		
	}
	
    public J_HeatingManagementDistrictHeatingIronBurner6HourSUMMERWINTER(
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
    	
        if (debugMode) {
            traceln(
                "Before discharge"
                + " | t=" + timeVariables.getT_h()
                + " | demand_kW=" + currentHeatDemand_kW
                + " | storage_kWh=" + getBufferEnergy_kWh()
                + " | soc=" + getBufferSOC_fr()
            );
        }  

    	// 2. Deliver heat from heat storage first
    	gc.f_updateFlexAssetFlows(
            heatStorage,
            -this.currentHeatDemand_kW / heatStorage.getCapacityHeat_kW(),
            timeVariables
        );
    	
        if (debugMode) {
            traceln(
                "After discharge"
                + " | t=" + timeVariables.getT_h()
                + " | storage_kWh=" + getBufferEnergy_kWh()
                + " | soc=" + getBufferSOC_fr()
            );
        }
        
        // 3. Daily planning moment: once per day at 06:00
        int currentDayIndex = (int) Math.floor(timeVariables.getT_h() / 24.0);

        if (isDailyPlanningMoment(timeVariables) && currentDayIndex != lastDailyPlanningDayIndex) {

            lastDailyPlanningDayIndex = currentDayIndex;
            dailyScheduleStartTime_h = timeVariables.getT_h();

            double[] dailyForecast_kW = getDailyForecastFromDatabaseHeatDemand_kW(timeVariables);

            double dailyDemand_kWh = sumForecastEnergy_kWh(dailyForecast_kW);
            double avgDailyDemand_kW = dailyDemand_kWh / dailyForecastHorizon_h;

            double minPower_kW = heatingAsset.minimumOutputHeatCapacity_kW;
            double lowDemandThreshold_kW = minPower_kW * lowAverageDemandThresholdFactor;

            dailyReserveModeLocked = avgDailyDemand_kW < lowDemandThreshold_kW;

            if (dailyReserveModeLocked) {
                dailyIronBurnerSetpointSchedule_kW =
                    calculateForecastedReserveDailySchedule_kW(timeVariables, dailyForecast_kW);
            } else {
                dailyIronBurnerSetpointSchedule_kW = null;
            }

            if (debugMode) {
                traceln(
                    "06:00 daily planning"
                    + " | day=" + currentDayIndex
                    + " | avgDailyDemand_kW=" + avgDailyDemand_kW
                    + " | lowDemandThreshold_kW=" + lowDemandThreshold_kW
                    + " | dailyReserveModeLocked=" + dailyReserveModeLocked
                    + " | bufferSOC=" + getBufferSOC_fr()
                );
            }
        }

        // 4. Select burner setpoint
        int index = roundToInt(timeVariables.getTimeOfDay_h() / timeParameters.getTimeStep_h());
        int indexForecastUpdateRate = roundToInt(this.forecastUpdateTimeRate_h / timeParameters.getTimeStep_h());

        if (dailyReserveModeLocked) {

            if (dailyIronBurnerSetpointSchedule_kW == null) {
                throw new RuntimeException(
                    "Daily reserve mode is locked, but dailyIronBurnerSetpointSchedule_kW is null."
                );
            }

            int dailyScheduleIndex =
                roundToInt((timeVariables.getT_h() - dailyScheduleStartTime_h) / timeParameters.getTimeStep_h());

            if (dailyScheduleIndex < 0 || dailyScheduleIndex >= dailyIronBurnerSetpointSchedule_kW.length) {
                dailyReserveModeLocked = false;
                this.currentIronBurnerSetpoint_kW = 0;
            } else {
                this.currentIronBurnerSetpoint_kW =
                    dailyIronBurnerSetpointSchedule_kW[dailyScheduleIndex];
            }

            if (debugMode) {
                traceln(
                    "Using 24h forecasted reserve control"
                    + " | t=" + timeVariables.getT_h()
                    + " | scheduleIndex=" + dailyScheduleIndex
                    + " | setpoint_kW=" + currentIronBurnerSetpoint_kW
                    + " | bufferSOC=" + getBufferSOC_fr()
                );
            }

        } else {

            // Only here may the 6h controller run.
            if (roundToInt(index % indexForecastUpdateRate) == 0) {
                this.heatDemandForecast_kW =
                    this.getForecastFromDatabaseHeatDemand_kW(timeVariables);

                this.ironBurnerSetpointSchedule_kW =
                    this.calculateIronBurnerSchedule_kW(timeVariables);
            }

            this.currentIronBurnerSetpoint_kW =
                ironBurnerSetpointSchedule_kW[roundToInt(index % indexForecastUpdateRate)];

            if (debugMode) {
                traceln(
                    "Using 6h SoC control"
                    + " | t=" + timeVariables.getT_h()
                    + " | setpoint_kW=" + currentIronBurnerSetpoint_kW
                    + " | bufferSOC=" + getBufferSOC_fr()
                );
            }
        }
		
		// 5. Send setpoint to iron burner
		gc.f_updateFlexAssetFlows(
            heatingAsset,
            currentIronBurnerSetpoint_kW / heatingAsset.getOutputHeatCapacity_kW(),
            timeVariables
        );
    	
    	// 6. Charge buffer from iron burner output
    	double burnerHeatFlow_kW = heatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT);
    	double bufferCharge_kW = max(0, -burnerHeatFlow_kW);
    	
    	gc.f_updateFlexAssetFlows(
            heatStorage,
            bufferCharge_kW / heatStorage.getCapacityHeat_kW(),
            timeVariables
        );
    	
    	if (debugMode) {
             traceln(
                 "Burner to buffer"
                 + " | burnerHeatFlow_kW=" + burnerHeatFlow_kW
                 + " | bufferCharge_kW=" + bufferCharge_kW
                 + " | storage_kWh=" + getBufferEnergy_kWh()
                 + " | soc=" + getBufferSOC_fr()
             );
    	}
    }
    
    private double getCurrentHeatDemand_kW() {
        double currentDemand_kW = 0;
        for (GridNode GN : gc.p_parentNodeHeat.f_getConnectedGridNodes()) {
        	currentDemand_kW += max(0, GN.v_currentLoad_kW);
        	traceln("GN: " + GN.v_currentLoad_kW);
        }
        return currentDemand_kW;
    }
    
    private double[] calculateIronBurnerSchedule_kW(J_TimeVariables timeVariables) {
    	double dt_h = timeParameters.getTimeStep_h();
    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h / timeParameters.getTimeStep_h());
    	
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
        	double bufferCorrection_kWh = (targetBufferSOC_fr - bufferSOC_fr) * bufferCapacity_kWh;
        	double requiredProduction_kWh = forecastDemand_kWh + bufferCorrection_kWh;
        	desiredPower_kW = requiredProduction_kWh <= 0
                ? 0
                : max(minPower_kW, min(maxPower_kW, requiredProduction_kWh / forecastHorizon_h));
        }
        
    	double[] ironBurnerHeatOutputSchedule_kW = new double[sizeForecastHorizon];

    	for (int i = 0; i < sizeForecastHorizon; i++) {
    		ironBurnerHeatOutputSchedule_kW[i] = desiredPower_kW;
    	}

        if (debugMode) {
            traceln(
                "IB 6h setpoint calculation"
                + " | t=" + timeVariables.getT_h()
                + " | forecastDemand_kWh=" + forecastDemand_kWh
                + " | bufferSOC=" + bufferSOC_fr
                + " | desiredPower_kW=" + desiredPower_kW
            );
        }

        return ironBurnerHeatOutputSchedule_kW;
    }	

    private double[] calculateForecastedReserveDailySchedule_kW(
        J_TimeVariables timeVariables,
        double[] dailyForecast_kW
    ) {
        double dt_h = timeParameters.getTimeStep_h();
        int stepsPerDay = roundToInt(dailyForecastHorizon_h / dt_h);

        double[] schedule_kW = new double[stepsPerDay];

        double plannedPower_kW = calculateMinimumReservePower_kW(dailyForecast_kW);

        double bufferCapacity_kWh = getBufferCapacity_kWh();
        double reserveEnergy_kWh = reserveMinSOC_fr * bufferCapacity_kWh;
        double upperEnergy_kWh = reserveUpperSOC_fr * bufferCapacity_kWh;
        double maxEnergy_kWh = reserveMaxSOC_fr * bufferCapacity_kWh;

        double virtualEnergy_kWh = getBufferEnergy_kWh();

        int blockSizeSteps = roundToInt(reserveBlockLength_h / dt_h);

        for (int blockStart = 0; blockStart < stepsPerDay; blockStart += blockSizeSteps) {

            int blockEnd = min(blockStart + blockSizeSteps, stepsPerDay);

            boolean blockNeedsBurner = false;

            double testEnergy_kWh = virtualEnergy_kWh;

            // First test this full 6h block without burner.
            for (int i = blockStart; i < blockEnd; i++) {
                testEnergy_kWh -= dailyForecast_kW[i] * dt_h;

                if (testEnergy_kWh < reserveEnergy_kWh) {
                    blockNeedsBurner = true;
                    break;
                }
            }

            // Also keep the burner off if the buffer is already high enough.
            if (virtualEnergy_kWh >= upperEnergy_kWh) {
                blockNeedsBurner = false;
            }

            double blockPower_kW = blockNeedsBurner ? plannedPower_kW : 0.0;

            for (int i = blockStart; i < blockEnd; i++) {
                schedule_kW[i] = blockPower_kW;

                virtualEnergy_kWh += blockPower_kW * dt_h;
                virtualEnergy_kWh -= dailyForecast_kW[i] * dt_h;

                virtualEnergy_kWh = max(0, min(maxEnergy_kWh, virtualEnergy_kWh));
            }
        }

        if (debugMode) {
            traceln(
                "Forecasted reserve daily schedule created"
                + " | t=" + timeVariables.getT_h()
                + " | plannedPower_kW=" + plannedPower_kW
                + " | blockLength_h=" + reserveBlockLength_h
                + " | startSOC=" + getBufferSOC_fr()
                + " | reserveSOC=" + reserveMinSOC_fr
                + " | upperSOC=" + reserveUpperSOC_fr
                + " | maxSOC=" + reserveMaxSOC_fr
            );
        }

        return schedule_kW;
    }

    private double calculateMinimumReservePower_kW(double[] dailyForecast_kW) {

        double dt_h = timeParameters.getTimeStep_h();

        double bufferCapacity_kWh = getBufferCapacity_kWh();
        double currentEnergy_kWh = getBufferEnergy_kWh();
        double reserveEnergy_kWh = reserveMinSOC_fr * bufferCapacity_kWh;

        double minPower_kW = heatingAsset.minimumOutputHeatCapacity_kW;
        double maxPower_kW = heatingAsset.getOutputHeatCapacity_kW();

        double cumulativeDemand_kWh = 0;
        double requiredPower_kW = 0;

        for (int i = 0; i < dailyForecast_kW.length; i++) {

            cumulativeDemand_kWh += dailyForecast_kW[i] * dt_h;

            double elapsed_h = (i + 1) * dt_h;

            double requiredReserveEnergy_kWh = reserveEnergy_kWh;

            // If the buffer starts below reserve, allow gradual recovery.
            if (currentEnergy_kWh < reserveEnergy_kWh && elapsed_h < reserveRecoveryHorizon_h) {
                double recoveryFraction = elapsed_h / reserveRecoveryHorizon_h;
                requiredReserveEnergy_kWh =
                    currentEnergy_kWh
                    + recoveryFraction * (reserveEnergy_kWh - currentEnergy_kWh);
            }

            double powerNeededForThisPrefix_kW =
                (cumulativeDemand_kWh + requiredReserveEnergy_kWh - currentEnergy_kWh) / elapsed_h;

            requiredPower_kW = max(requiredPower_kW, powerNeededForThisPrefix_kW);
        }

        if (requiredPower_kW <= 0) {
            return 0;
        }

        requiredPower_kW = max(minPower_kW, requiredPower_kW);
        requiredPower_kW = roundUpToPowerStep_kW(requiredPower_kW);
        requiredPower_kW = min(maxPower_kW, requiredPower_kW);

        return requiredPower_kW;
    }

    private double roundUpToPowerStep_kW(double power_kW) {
        return Math.ceil(power_kW / reservePowerStep_kW) * reservePowerStep_kW;
    }

    private boolean isDailyPlanningMoment(J_TimeVariables timeVariables) {
        double timeOfDay_h = timeVariables.getTimeOfDay_h();
        double dt_h = timeParameters.getTimeStep_h();

        return abs(timeOfDay_h - dailyPlanningHour_h) < dt_h / 2.0;
    }

    private double sumForecastEnergy_kWh(double[] forecast_kW) {
        double dt_h = timeParameters.getTimeStep_h();
        double energy_kWh = 0;

        for (int i = 0; i < forecast_kW.length; i++) {
            energy_kWh += forecast_kW[i] * dt_h;
        }

        return energy_kWh;
    }
    
    private double[] getDailyForecastFromDatabaseHeatDemand_kW(J_TimeVariables timeVariables) {

        int sizeForecastHorizon =
            roundToInt(dailyForecastHorizon_h / timeParameters.getTimeStep_h());

        double[] totalHeatDemandForecast_kW = new double[sizeForecastHorizon];

        double timeAtStartForecast_h = timeVariables.getT_h();
        int indexAtStartForecast =
            roundToInt(timeAtStartForecast_h / timeParameters.getTimeStep_h());

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

        traceln(
            "Initialized 6h heat demand forecast in heating management"
            + " | steps=" + totalHeatDemandForecast_kW.length
            + " | firstValue=" + totalHeatDemandForecast_kW[0]
            + " kW"
        );
        
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
        this.dailyReserveModeLocked = false;
        this.dailyIronBurnerSetpointSchedule_kW = null;
        this.dailyScheduleStartTime_h = -1;
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

        this.storedDailyIronBurnerSetpointSchedule_kW = this.dailyIronBurnerSetpointSchedule_kW;
        if (this.dailyIronBurnerSetpointSchedule_kW != null) {
            this.dailyIronBurnerSetpointSchedule_kW = new double[this.dailyIronBurnerSetpointSchedule_kW.length];
        }

        this.storedDailyReserveModeLocked = this.dailyReserveModeLocked;
        this.dailyReserveModeLocked = false;

        this.storedDailyScheduleStartTime_h = this.dailyScheduleStartTime_h;
        this.dailyScheduleStartTime_h = -1;
    }

    public void restoreStates() {
        this.currentIronBurnerSetpoint_kW = 0;

        this.ironBurnerSetpointSchedule_kW = this.storedIronBurnerSetpointSchedule_kW;
        this.dailyIronBurnerSetpointSchedule_kW = this.storedDailyIronBurnerSetpointSchedule_kW;
        this.dailyReserveModeLocked = this.storedDailyReserveModeLocked;
        this.dailyScheduleStartTime_h = this.storedDailyScheduleStartTime_h;
    }
	
	@Override
	public String toString() {
		return super.toString();
	}
}