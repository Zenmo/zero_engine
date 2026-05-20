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
	
	//Rolling horizon control settings
	private double currentIronBurnerSetpoint_kW = 0;
	private double nextSetpointUpdateTime_h = 0;
	
	private double setpointBlockDuration_h = 6.0;
	private double forecastHorizon_h = 6.0;
	
	// Buffer control settings
	private double targetBufferSOC_fr = 0.6;
	private double lowBufferSOC_fr = 0.20;
	private double maxBufferSOC_fr = 0.95;

	private double[] heatDemandForecast_kW;
	
    private boolean debugMode = false;

	
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
   
    	// 1. Calculate actual current heat demand from connected lower-level GCs
    	double currentDemand_kW = getCurrentHeatDemand_kW();
    	
        if (debugMode) {
            traceln(
                "Before discharge"
                + " | t=" + timeVariables.getT_h()
                + " | demand_kW=" + currentDemand_kW
                + " | storage_kWh=" + getBufferEnergy_kWh()
                + " | soc=" + getBufferSOC_fr()
            );
        }  
    	// 2. Deliver Heat from Heat Storage Asset
    	gc.f_updateFlexAssetFlows(heatStorage, -currentDemand_kW/heatStorage.getCapacityHeat_kW(), timeVariables );
    	
        if (debugMode) {
            traceln(
                "After discharge"
                + " | t=" + timeVariables.getT_h()
                + " | storage_kWh=" + getBufferEnergy_kWh()
                + " | soc=" + getBufferSOC_fr()
            );
        }
    	// 3. Calculate new rolling 6-hour iron burner setpoint
    	
    	double currentTime_h = timeVariables.getT_h();
    	
    	if (currentTime_h >= nextSetpointUpdateTime_h) {
    	    currentIronBurnerSetpoint_kW = calculateIronBurnerSetPoint_kW(timeVariables);
    	    nextSetpointUpdateTime_h = currentTime_h + setpointBlockDuration_h;

            traceln(
                "New 6h iron burner setpoint = "
                + currentIronBurnerSetpoint_kW
                + " kW until t=" + nextSetpointUpdateTime_h
                + " | bufferSOC=" + getBufferSOC_fr()
            );
    	}

    	// 4. Send setpoint to iron burner 
    	gc.f_updateFlexAssetFlows(heatingAsset, currentIronBurnerSetpoint_kW/heatingAsset.getOutputHeatCapacity_kW(), timeVariables);
    	
    	// 5. Charge buffer from iron burner during this timestep
    	double burnerHeatFlow_kW = heatingAsset.getLastFlows().get(OL_EnergyCarriers.HEAT);
    	double bufferCharge_kW = max(0, -burnerHeatFlow_kW);
    	
    	gc.f_updateFlexAssetFlows(heatStorage, bufferCharge_kW/heatStorage.getCapacityHeat_kW(), timeVariables );
    	
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
        for (GridConnection childGC : gc.p_parentNodeHeat.f_getAllLowerLVLConnectedGridConnections()) {
            if (childGC != gc) {
                double heatFlow_kW = childGC.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
                currentDemand_kW += max(0, heatFlow_kW);
            }
        }
        return currentDemand_kW;
    }
    
    private double calculateIronBurnerSetPoint_kW(J_TimeVariables timeVariables) {
    	double dt_h = timeParameters.getTimeStep_h();
    	int nForecastSteps = roundToInt(forecastHorizon_h / dt_h);
    	
    	double forecastDemand_kWh = 0;
    	
    	for (int i = 0; i < nForecastSteps; i++) {
    		double t_h = timeVariables.getT_h() + i * dt_h;
    		double heatdemand_kW = getForecastHeatDemand_kW(t_h);
    		forecastDemand_kWh += heatdemand_kW * dt_h;
    	}
    	

        double bufferCapacity_kWh = getBufferCapacity_kWh();
        double bufferSOC_fr = getBufferSOC_fr();
    	
    	double maxPower_kW = heatingAsset.getOutputHeatCapacity_kW();
    	double minPower_kW = heatingAsset.minimumOutputHeatCapacity_kW;
    	
    	 // Emergency/recovery mode: prevent buffer from becoming too empty
        if (bufferSOC_fr <= lowBufferSOC_fr) {
            return maxPower_kW;
        }

        // Full-buffer mode: stop producing extra heat
        if (bufferSOC_fr >= maxBufferSOC_fr) {
            return 0;
        }
    	
    	double bufferCorrection_kWh = (targetBufferSOC_fr - bufferSOC_fr) * bufferCapacity_kWh;
    	
    	double requiredProduction_kWh = forecastDemand_kWh + bufferCorrection_kWh;
    	
    	double desiredPower_kW = requiredProduction_kWh / forecastHorizon_h;    	

        if (debugMode) {
            traceln(
                "IB setpoint calculation"
                + " | t=" + timeVariables.getT_h()
                + " | forecastDemand_kWh=" + forecastDemand_kWh
                + " | avgDemand_kW=" + forecastDemand_kWh / forecastHorizon_h
                + " | bufferSOC=" + bufferSOC_fr
                + " | bufferCorrection_kWh=" + bufferCorrection_kWh
                + " | desiredPower_kW=" + desiredPower_kW
            );
        }

        if (desiredPower_kW <= 0) {
            return 0;
        }
    	
    	return max(minPower_kW, min(maxPower_kW, desiredPower_kW));
    }	
    
    private double getBufferCapacity_kWh() {
        return heatStorage.getStorageCapacity_kWh();
    }

    private double getBufferEnergy_kWh() {
        return heatStorage.getRemainingHeatStorageHeat_kWh();
    }

    public double getBufferSOC_fr() {

        double bufferCapacity_kWh = getBufferCapacity_kWh();

        if (bufferCapacity_kWh <= 0) {
            return 0;
        }

        return getBufferEnergy_kWh() / bufferCapacity_kWh;
    }
    
    private void initializeHeatDemandForecast() {

        if (gc.energyModel.v_heatDemandForecast_kW == null) {
            throw new RuntimeException(
                this.getClass()
                + " requires energyModel.v_heatDemandForecast_kW, but it is null. "
                + "Make sure f_initializeHeatDemandForecast() is called in StartUp_IronFuel_H4B before the simulation starts."
            );
        }

        heatDemandForecast_kW = gc.energyModel.v_heatDemandForecast_kW;

        traceln(
            "Initialized heat demand forecast in heating management"
            + " | steps=" + heatDemandForecast_kW.length
            + " | firstValue=" + heatDemandForecast_kW[0]
            + " kW"
        );
    }
    private double getForecastHeatDemand_kW(double t_h) {

        if (heatDemandForecast_kW == null) {
            throw new RuntimeException(
                this.getClass() + " heatDemandForecast_kW has not been initialized."
            );
        }

        int index = roundToInt(t_h / timeParameters.getTimeStep_h());

        if (index < 0) {
            return 0;
        }

        if (index >= heatDemandForecast_kW.length) {
            index = heatDemandForecast_kW.length - 1;
            traceln("You are over 1 year of simulation. The numbers are not correct anymore!!!");
        }

        double heatDemand_kW = heatDemandForecast_kW[index];

        return max(0, heatDemand_kW);
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
    	
    	initializeHeatDemandForecast();
    	
    	this.isInitialized = true;
    }
    
    public void notInitialized() {
    	this.isInitialized = false;
    	this.currentIronBurnerSetpoint_kW = 0;
        this.nextSetpointUpdateTime_h = 0;
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
        this.currentIronBurnerSetpoint_kW = 0;
        this.nextSetpointUpdateTime_h = 0;
    }

    public void restoreStates() {
        this.currentIronBurnerSetpoint_kW = 0;
        this.nextSetpointUpdateTime_h = 0;
    }
	
	@Override
	public String toString() {
		return super.toString();
	}
}