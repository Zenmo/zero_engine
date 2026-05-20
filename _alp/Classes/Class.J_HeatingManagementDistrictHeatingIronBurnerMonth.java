/**
 * J_HeatingManagementDistrictHeatingIronBurnerMonth
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
public class J_HeatingManagementDistrictHeatingIronBurnerMonth implements I_HeatingManagement {

	private boolean isInitialized = false;
	private GridConnection gc;
    private J_TimeParameters timeParameters;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.IRON_BURNER
	);
	private OL_GridConnectionHeatingType currentHeatingType;
	private J_EAConversionIronBurner heatingAsset;
	private J_HeatingPreferences heatingPreferences = null; //Not needed for the GCDistrictHeating.
	
	private double previousHeatFeedin_kW = 0;
	private double storedPreviousHeatFeedin_kW;
	
	//Rolling horizon control settings
	private double currentIronBurnerSetpoint_kW = 0;
	private double nextSetpointUpdateTime_h = 0;
	private double setpointBlockDuration_h = 2920;
	
	private double forecastHorizon_h = 2920;
	private double targetBufferSOC_fr = 0.6;

	private double[] heatDemandForecast_kW;
	
	/**
     * Default constructor
     */
	
	public J_HeatingManagementDistrictHeatingIronBurnerMonth() {
		
	}
	
    public J_HeatingManagementDistrictHeatingIronBurnerMonth( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType ) {
    	if (!(gc instanceof GCDistrictHeating)) {
    		throw new RuntimeException("Impossible to connect " + this.getClass() + " to a GC that is not GCDistrictHeating");
    	}
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    }

    public void manageHeating(J_TimeVariables timeVariables) {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	GCDistrictHeating dh = (GCDistrictHeating) gc;
    	
    	double dt_h = timeParameters.getTimeStep_h();
    	 	
    	// 1. Calculate actual current heat demand from connected lower-level GCs
    	 
    	double currentDemand_kW = getCurrentHeatDemand_kW();
    	double requestedHeat_kWh = currentDemand_kW * dt_h;
    	
    	//2. Deliver current demand from buffer
    	double deliveredHeat_kWh = dh.f_deliverHeatFromBuffer(requestedHeat_kWh);
    	double deliveredHeat_kW = deliveredHeat_kWh / dt_h;
    	
    	gc.fm_currentBalanceFlows_kW.put(OL_EnergyCarriers.HEAT, -deliveredHeat_kW);
    	
    	// 3. Apply buffer losses (already happened in heat delivery)
    	//dh.f_applyBufferLosses(dt_h);
    	
    	// 4. Calculate new rolling 6-hour iron burner setpoint
    	
    	double currentTime_h = timeVariables.getT_h();
    	
    	traceln(
    		    "Setpoint timing"
    		    + " | t=" + currentTime_h
    		    + " | nextUpdate=" + nextSetpointUpdateTime_h
    		    + " | currentSetpoint=" + currentIronBurnerSetpoint_kW
    		);
    	
    	if (currentTime_h >= nextSetpointUpdateTime_h) {
    	    currentIronBurnerSetpoint_kW = calculateIronBurnerSetPoint_kW(timeVariables);
    	    nextSetpointUpdateTime_h = currentTime_h + setpointBlockDuration_h;

    	    traceln(
    	        "New 6h iron burner setpoint = " + currentIronBurnerSetpoint_kW
    	        + " kW until t=" + nextSetpointUpdateTime_h
    	    );
    	}

    	double ironBurnerSetpoint_kW = currentIronBurnerSetpoint_kW;
    	
    	heatingAsset.v_requestedHeatOutput_kW = ironBurnerSetpoint_kW;
    	
    	// 5. Send setpoint to iron burner / district heating controller
    	dh.f_controlIronBurnerReal(heatingAsset, dt_h, ironBurnerSetpoint_kW);
    	
    	// 6. Charge buffer from iron burner during this timestep
    	dh.f_chargeBufferFromIronBurner(heatingAsset, dt_h, timeVariables);
    	
    	previousHeatFeedin_kW = -gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
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
    	
    	traceln(
    		    "IB forecast summary"
    		    + " | t=" + timeVariables.getT_h()
    		    + " h"
    		    + " | forecastDemand=" + forecastDemand_kWh
    		    + " kWh"
    		    + " | avgDemand=" + (forecastDemand_kWh / forecastHorizon_h)
    		    + " kW"
    		);
    
    	GCDistrictHeating dh = (GCDistrictHeating) gc;
    	
    	double bufferCapacity_kWh = dh.p_bufferCapacity_kWh;;
    	double bufferEnergy_kWh = dh.v_bufferHeat_kWh;
    	
    	double minBufferEnergy_kWh = dh.p_bufferMin_kWh;
    	double maxBufferEnergy_kWh = dh.p_bufferMax_kWh;
    	
    	double usableBufferRange_kWh = maxBufferEnergy_kWh - minBufferEnergy_kWh;
    	
    	double targetBufferEnergy_kWh = minBufferEnergy_kWh + targetBufferSOC_fr * usableBufferRange_kWh;
    	
    	double bufferCorrection_kWh = targetBufferEnergy_kWh - bufferEnergy_kWh;
    	
    	//If buffer is already too full, do not produce extra heat
    	if (bufferEnergy_kWh >= maxBufferEnergy_kWh) {
    		bufferCorrection_kWh = -forecastDemand_kWh;
    	}
    	
    	double requiredProduction_kWh = forecastDemand_kWh + bufferCorrection_kWh;
    	
    	double desiredPower_kW = requiredProduction_kWh / forecastHorizon_h;    	
    	return max(0, desiredPower_kW);
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
    	
    	GCDistrictHeating dh = (GCDistrictHeating) gc;
    	if (dh.p_bufferCapacity_kWh <= 0) {
    	    throw new RuntimeException(this.getClass() + " requires an initialized district heating buffer.");
    	}
    	
    	/*
    	if (gc.p_heatBuffer == null) {
    		throw new RuntimeException(this.getClass() + " requires a heat buffer.");
    	}
    	*/
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
    	
    	initializeHeatDemandForecast();
    	
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
    
    //Store and reset states
	public void storeStatesAndReset() {
		this.storedPreviousHeatFeedin_kW = this.previousHeatFeedin_kW;
		this.previousHeatFeedin_kW = 0;
	}
	
	public void restoreStates() {
		this.previousHeatFeedin_kW = this.storedPreviousHeatFeedin_kW;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}