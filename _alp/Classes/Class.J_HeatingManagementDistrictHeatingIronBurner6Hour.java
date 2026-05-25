/**
 * J_HeatingManagementDistrictHeatingIronBurner6HourReal
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
	
	private J_HeatingPreferences heatingPreferences = null; //Not needed for the GCDistrictHeating.
	
	private double currentHeatDemand_kW = 0;
	private double currentIronBurnerSetpoint_kW = 0;
	
	//Rolling horizon control settings
	private double forecastHorizon_h = 6.0;
	private double forecastUpdateTimeRate_h = 6.0;
	private double[] heatDemandForecast_kW;
	private double[] ironBurnerSetpointSchedule_kW;
	
	// Buffer control settings
	private double targetBufferSOC_fr = 0.6;
	private double lowBufferSOC_fr = 0.20;
	private double maxBufferSOC_fr = 0.95;

    private boolean debugMode = true;
    
    //Stored
  	private double[] storedIronBurnerSetpointSchedule_kW;

	
	/**
     * Default constructor
     */
	
	public J_HeatingManagementDistrictHeatingIronBurner6Hour() {
		
	}
	
    public J_HeatingManagementDistrictHeatingIronBurner6Hour( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType ) {
    	if (!(gc instanceof GCDistrictHeating)) {
    		throw new RuntimeException("Impossible to connect " + this.getClass() + " to a GC that is not GCDistrictHeating");
    	}
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    	
    	this.heatStorage = (J_EAStorageHeat) findFirst(gc.c_storageAssets, j_ea -> j_ea instanceof J_EAStorageHeat);
    }

    public void manageHeating(J_TimeVariables timeVariables) {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
   
    	// 1. Calculate heat demand from connected lower-level GCs at current timestep t_h
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
    	// 2. Deliver Heat from Heat Storage Asset
    	gc.f_updateFlexAssetFlows(heatStorage, -this.currentHeatDemand_kW/heatStorage.getCapacityHeat_kW(), timeVariables );
    	
        if (debugMode) {
            traceln(
                "After discharge"
                + " | t=" + timeVariables.getT_h()
                + " | storage_kWh=" + getBufferEnergy_kWh()
                + " | soc=" + getBufferSOC_fr()
            );
        }
        
    	// 3. Determine heat demand forecast of upcoming 6 hours every 6 hours + Determine iron burner setpoint schedule
    	int index = roundToInt(timeVariables.getTimeOfDay_h()/timeParameters.getTimeStep_h());
    	int indexForecastUpdateRate = roundToInt(this.forecastUpdateTimeRate_h/timeParameters.getTimeStep_h());
    	if(roundToInt(index % indexForecastUpdateRate) == 0){ // every t_h % 6 == 0 => index = t_h / 0.25 => index % 24 == 0, update forecast
			this.heatDemandForecast_kW = this.getForecastFromDatabaseHeatDemand_kW(timeVariables); // this.getForecastHeatDemand_kW(timeVariables); 
			this.ironBurnerSetpointSchedule_kW = this.calculateIronBurnerSchedule_kW(timeVariables);
		}
    	traceln(
            "heatDemandForecast_kW = "
            + heatDemandForecast_kW[roundToInt(index % indexForecastUpdateRate)]
            + " | t=" + timeVariables.getT_h()
        );
		
		// 4. Send flat schedule setpoint to iron burner
		this.currentIronBurnerSetpoint_kW = ironBurnerSetpointSchedule_kW[roundToInt(index % indexForecastUpdateRate)];
		gc.f_updateFlexAssetFlows(heatingAsset, currentIronBurnerSetpoint_kW/heatingAsset.getOutputHeatCapacity_kW(), timeVariables);
    	
		traceln(
            "New 6h iron burner setpoint = "
            + currentIronBurnerSetpoint_kW
            + " | bufferSOC=" + getBufferSOC_fr()
        );
    	
    	// 5. Charge buffer from iron burner during this timestep
    	double burnerHeatFlow_kW = heatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT);
    	double bufferCharge_kW = max(0, -burnerHeatFlow_kW);
    	
    	gc.f_updateFlexAssetFlows(heatStorage, bufferCharge_kW/heatStorage.getCapacityHeat_kW(), timeVariables);
    	
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
        for(GridNode GN : gc.p_parentNodeHeat.f_getConnectedGridNodes()) {
        	currentDemand_kW += max(0, GN.v_currentLoad_kW);
        	traceln("GN: " + GN.v_currentLoad_kW);
        }
        return currentDemand_kW;
    }
    
    private double[] calculateIronBurnerSchedule_kW(J_TimeVariables timeVariables) {
    	double dt_h = timeParameters.getTimeStep_h();
    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h/timeParameters.getTimeStep_h());
    	
    	// Calculate how much energy is required in upcoming 6 hours to meet heat demand
    	double forecastDemand_kWh = 0;
        for (int i = 0; i < sizeForecastHorizon; i++) {
            forecastDemand_kWh += this.heatDemandForecast_kW[i] * dt_h;
        }
        
    	double bufferCapacity_kWh = getBufferCapacity_kWh();
        double bufferSOC_fr = getBufferSOC_fr();
        
        double maxPower_kW = heatingAsset.getOutputHeatCapacity_kW();
        double minPower_kW = heatingAsset.minimumOutputHeatCapacity_kW;
        
        double desiredPower_kW = 0;
        if (bufferSOC_fr <= lowBufferSOC_fr) { // Emergency/recovery mode: prevent buffer from becoming too empty
            desiredPower_kW = maxPower_kW;
        }  
        else if (bufferSOC_fr >= maxBufferSOC_fr) { // Full-buffer mode: stop producing extra heat
            desiredPower_kW = 0;
        }
        else {
        	double bufferCorrection_kWh = (targetBufferSOC_fr - bufferSOC_fr) * bufferCapacity_kWh;
        	double requiredProduction_kWh = forecastDemand_kWh + bufferCorrection_kWh;
        	desiredPower_kW = requiredProduction_kWh <= 0 ? 0 : max(minPower_kW, min(maxPower_kW, requiredProduction_kWh / forecastHorizon_h));
        }
        
        // Create completely flat schedule for the 6-hour block
    	double[] ironBurnerHeatOutputSchedule_kW = new double[sizeForecastHorizon];
    	for (int i = 0; i < sizeForecastHorizon; i++) {
    		ironBurnerHeatOutputSchedule_kW[i] = desiredPower_kW;
    	}

        if (debugMode) {
            traceln(
                "IB setpoint calculation"
                + " | t=" + timeVariables.getT_h()
                + " | forecastDemand_kWh=" + forecastDemand_kWh
                //+ " | avgDemand_kW=" + forecastDemand_kWh / forecastHorizon_h
                + " | bufferSOC=" + bufferSOC_fr
                //+ " | bufferCorrection_kWh=" + bufferCorrection_kWh
                + " | desiredPower_kW=" + desiredPower_kW
            );
        }

        return ironBurnerHeatOutputSchedule_kW;
    }	
    
    
    private double[] getForecastFromDatabaseHeatDemand_kW(J_TimeVariables timeVariables) {
    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h/timeParameters.getTimeStep_h());
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
    	
    	for(int i = 0; i < sizeForecastHorizon; i++) {
    		totalHeatDemandForecast_kW[i] = gc.energyModel.v_heatDemandForecast_kW[roundToInt((indexAtStartForecast + i) % 35040)];
    		
    	}

        traceln(
            "Initialized heat demand forecast in heating management"
            + " | steps=" + totalHeatDemandForecast_kW.length
            + " | firstValue=" + totalHeatDemandForecast_kW[0]
            + " kW"
        );
        
        return totalHeatDemandForecast_kW;
    	
    }
    
    
    
    private double[] getForecastHeatDemand_kW(J_TimeVariables timeVariables) {

    	int sizeForecastHorizon = roundToInt(this.forecastHorizon_h/timeParameters.getTimeStep_h());
    	double[] totalHeatDemandForecast_kW = new double[sizeForecastHorizon];
        
        double timeAtStartForecast_h = timeVariables.getT_h();
        int indexAtStartForecast = roundToInt(timeAtStartForecast_h / timeParameters.getTimeStep_h());
        
        // Loop through all lower-level connected grid connections on the heating network
        for (GridNode GN : gc.p_parentNodeHeat.f_getConnectedGridNodes()) {
	        for (GridConnection childGC : GN.f_getAllLowerLVLConnectedGridConnections()) {
	            if (childGC == gc) {
	                continue; // Skip the district heating source itself
	            }
	            
	            double currentNetworkDraw_kW = Math.max(0, childGC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT));
	            
	            // 1. Identify the child's heating asset that consumes HEAT from the district network
	            J_EAConversion childHeatingAsset = null;
	            for (J_EAConversion asset : childGC.c_heatingAssets) {
	                if (asset.activeConsumptionEnergyCarriers.contains(OL_EnergyCarriers.HEAT)) {
	                    childHeatingAsset = asset;
	                    break;
	                }
	            }
	            
	            // Fallback: search childGC.c_flexAssets in case it's stored there
	            if (childHeatingAsset == null) {
	                for (J_EAFlex asset : childGC.c_flexAssets) {
	                    if (asset instanceof J_EAConversion && asset.activeConsumptionEnergyCarriers.contains(OL_EnergyCarriers.HEAT)) {
	                        childHeatingAsset = (J_EAConversion) asset;
	                        break;
	                    }
	                }
	            }
	            
	            // If the child connection doesn't have a specific conversion asset but still draws heat, assume efficiency 1.0
	            double efficiency = (childHeatingAsset != null) ? childHeatingAsset.getEta_r() : 1.0;
	            
	            // 2. Fetch all heat demand profiles of this child connection
	            List<J_EAProfile> heatProfiles = new ArrayList<>();
	            for (J_EAProfile profile : childGC.c_profileAssets) {
	                if (profile.energyCarrier == OL_EnergyCarriers.HEAT) {
	                    heatProfiles.add(profile);
	                }
	            }
	            
	            // 3. Determine the *current* network draw caused strictly by these static profiles
	            double currentProfileNetworkDraw_kW = 0;
	            for (J_EAProfile profile : heatProfiles) {
	                double scalar = profile.getProfileScaling_fr() * profile.getProfileUnitScaler_fr();
	                if (profile instanceof J_EAProduction) {
	                    scalar *= -1; // If it's a production profile, scale negatively
	                }
	                // Evaluate profile exactly at current simulation time
	                double t = timeVariables.getT_h();
	                double profileValue = profile.profilePointer.getValue(t);
	                currentProfileNetworkDraw_kW += (profileValue * scalar) / efficiency;
	            }
                double unprofiledBaseNetworkDraw_kW = Math.max(0, currentNetworkDraw_kW - currentProfileNetworkDraw_kW);
	            
                // Create a pseudo heat loss factor for the building based on the CURRENT ambient temperature
                // Q = max(0, U * (20 - T_amb))  =>  U = Q / max(0.1, 20 - T_amb)
                double currentAmbTemp_degC = gc.energyModel.pp_ambientTemperature_degC.getValue(timeVariables.getT_h());
                double pseudoHeatLossFactor = unprofiledBaseNetworkDraw_kW / Math.max(0.1, 20.0 - currentAmbTemp_degC);
                
	            // 5. Forecast and sum the heat demands over the 6-hour horizon
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
	                
	             // Predict future dynamic space heating using the future ambient temperature
                double futureAmbTemp_degC = gc.energyModel.pp_ambientTemperature_degC.getValue(t);
                double futureDynamicSpaceHeating_kW = pseudoHeatLossFactor * Math.max(0, 20.0 - futureAmbTemp_degC);
	                
	            // The total forecasted draw is the dynamically evaluated profiles plus the constant unprofiled baseline
	            totalHeatDemandForecast_kW[i] += Math.max(0, dynamicProfileDraw_kW + futureDynamicSpaceHeating_kW);
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
        	throw new RuntimeException(this.getClass()+ " requires a J_EAStorageHeat asset.");
        }
    	
        if (this.heatStorage.getStorageCapacity_kWh() <= 0) {
        	throw new RuntimeException(this.getClass()+ " requires heatStorage.getStorageCapacity_kWh() > 0, but found "+ this.heatStorage.getStorageCapacity_kWh());
        }
    	
    	this.isInitialized = true;
    }
    
    public void notInitialized() {
    	this.isInitialized = false;
    	this.currentIronBurnerSetpoint_kW = 0;
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
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    
    //Store and reset states
    public void storeStatesAndReset() {
        this.currentIronBurnerSetpoint_kW = 0;
        this.storedIronBurnerSetpointSchedule_kW = ironBurnerSetpointSchedule_kW;
		this.ironBurnerSetpointSchedule_kW = new double[ironBurnerSetpointSchedule_kW.length];
    }

    public void restoreStates() {
        this.currentIronBurnerSetpoint_kW = 0;
        this.ironBurnerSetpointSchedule_kW = this.storedIronBurnerSetpointSchedule_kW;
    }
	
	@Override
	public String toString() {
		return super.toString();
	}
}