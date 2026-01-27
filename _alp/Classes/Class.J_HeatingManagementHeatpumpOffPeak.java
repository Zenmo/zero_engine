/**
 * J_HeatingManagementHeatpumpOffPeak
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

public class J_HeatingManagementHeatpumpOffPeak implements I_HeatingManagement {
	private boolean isInitialized = false;
    private GridConnection gc;
    private J_TimeParameters timeParameters;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
    private J_EAConversion heatingAsset;
	private J_HeatingPreferences heatingPreferences;
	private J_EAStorageHeat hotWaterBuffer;
	private List<J_EAProduction> ptAssets;
    private boolean hasPT = false;
    private boolean hasHotWaterBuffer = false;
    
    // PI control gains
    private double P_gain_kWpDegC = 1*1;
    private double I_gain_kWphDegC = 0.1*2;
    private double I_state_hDegC = 0;
    
    //Temperature setpoint low pass filter
    private double filteredCurrentSetpoint_degC;
    private double setpointFilterTimeScale_h = 2.0; // Smooth in X hours
    
    //Off peak management
    private double preHeatDuration_hr = 2; // Amount of hours that the heatpump has to reach the requiredTemperatureAtStartOfReducedHeatingInterval_degC
    private double requiredTemperatureAtStartOfReducedHeatingInterval_degC = 20; // Temperature setpoint in degrees Celsius that the heatpump will have in the preheatduration time
    private double startTimeOfReducedHeatingInterval_hr = 16; // Hour of the day
    private double endTimeOfReducedHeatingInterval_hr = 21; // Hour of the day -> CAN NOT BE THE SAME AS THE START TIME
    private double reducedHeatingIntervalLength_hr = (endTimeOfReducedHeatingInterval_hr - startTimeOfReducedHeatingInterval_hr + 24) % 24;     
    
    
    //Stored
    private double storedI_state_hDegC;
    private double storedFilteredCurrentSetpoint_degC;
    private double storedrequiredTemperatureAtStartOfReducedHeatingInterval_degC;
    
    /**
     * Default constructor
     */
    public J_HeatingManagementHeatpumpOffPeak() {
    }

    public J_HeatingManagementHeatpumpOffPeak( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    }
    
    
    public void manageHeating(J_TimeVariables timeVariables) {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    		calculatePreHeatParameters(timeVariables);
    	}
    	double t_h = timeVariables.getT_h();
    	double timeOfDay_h = timeVariables.getTimeOfDay_h();
    	
    	//Calculate preheat paramters for the next reduced heating interval
    	if(timeOfDay_h == endTimeOfReducedHeatingInterval_hr) {
    		calculatePreHeatParameters(timeVariables);
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
    	
    	//Determine if time is in reduced Heating interval
		boolean timeIsInReducedHeatingInterval = ((timeOfDay_h - startTimeOfReducedHeatingInterval_hr + 24) % 24) < reducedHeatingIntervalLength_hr;
		boolean timeIsInPreheatInterval = ((timeOfDay_h - (startTimeOfReducedHeatingInterval_hr - preHeatDuration_hr) + 24) % 24) < preHeatDuration_hr;

		double startTimePreheatTime_hr = startTimeOfReducedHeatingInterval_hr - preHeatDuration_hr;
		
    	//Get the current temperature setpoint dependend on day/night time and noheat/preheat interval settings
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();
    	if(timeIsInPreheatInterval) { // During preheat interval, raise the setpoint temperature step by step, to prevent overreaction by the controller
    		currentSetpoint_degC = this.requiredTemperatureAtStartOfReducedHeatingInterval_degC;
    	}
    	else if(timeIsInReducedHeatingInterval) {
    		currentSetpoint_degC = heatingPreferences.getMinComfortTemperature_degC(); // -> prevents fast response during interval if min comfort is breached
    		if(startTimeOfReducedHeatingInterval_hr == timeOfDay_h) {
    			I_state_hDegC = 0; //Reset I state at the start of no heating interval to reset the controller, so no heating power at all.
    			this.filteredCurrentSetpoint_degC = heatingPreferences.getMinComfortTemperature_degC();
    		}
    	}
    	else if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    	}
    	
    	
    	//Smooth the setpoint signal
    	this.filteredCurrentSetpoint_degC += 1/(this.setpointFilterTimeScale_h / timeParameters.getTimeStep_h()) * (currentSetpoint_degC - this.filteredCurrentSetpoint_degC);
    	
		//Calculate the deltaT_degc
		double deltaT_degC = this.filteredCurrentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed
    	
    	//PI control
    	I_state_hDegC = max(0,I_state_hDegC + deltaT_degC * timeParameters.getTimeStep_h()); // max(0,...) to prevent buildup of negative integrator during warm periods.
    	double buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDegC + I_state_hDegC * I_gain_kWphDegC);
    	
    	//Set asset power
    	double assetPower_kW = min(heatingAsset.getOutputCapacity_kW(), buildingHeatingDemand_kW + currentHeatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.
    	gc.f_updateFlexAssetFlows(heatingAsset, assetPower_kW / heatingAsset.getOutputCapacity_kW(), timeVariables);

		//Set building power (other heat demand gets bias if asset does not have enough capacity)
		double heatIntoBuilding_kW = max(0, assetPower_kW - currentHeatDemand_kW);    			
    	gc.f_updateFlexAssetFlows(building, heatIntoBuilding_kW / building.getCapacityHeat_kW(), timeVariables);

    }    
    
    private void calculatePreHeatParameters(J_TimeVariables timeVariables) {
		double energyModel_time_h = timeVariables.getT_h();
		double p_timestep_h = timeParameters.getTimeStep_h();
		J_ProfilePointer ambientTemperatureProfilePointer = gc.energyModel.pp_ambientTemperature_degC;
		
		int intervalLength_timeSteps = roundToInt(this.reducedHeatingIntervalLength_hr / p_timestep_h) + 1; // + 1 to account for time step delay in losses
		
		double[] ambientTemperatureDuringInterval_degC = new double[intervalLength_timeSteps];
		
		double nextIntervalStart_t_h = energyModel_time_h - ((energyModel_time_h - startTimeOfReducedHeatingInterval_hr + 24) % 24);
		if (nextIntervalStart_t_h < energyModel_time_h) {
			nextIntervalStart_t_h += 24.0; // move to next day if already passed
		}

		// Get the ambient temperature profile for the interval
		for (int i = 0; i < intervalLength_timeSteps; i++) {
			double time = nextIntervalStart_t_h + i * p_timestep_h;
		    ambientTemperatureDuringInterval_degC[i] = ambientTemperatureProfilePointer.getValue(time);
		}
			
		// Get the building thermal properties and convert into the same units
		double lossFactorPerTimeStep_kWhpK = this.building.getLossFactor_WpK()/1000.0 * p_timestep_h;
		double buildingHeatCapacity_kWhpK = this.building.getHeatCapacity_JpK()/(3.6e6);
		
		// Start calculation from the known comfort temperature that the building should minimally be at the end of the reduced heating interval
		//-> what if ends in nighttime, should this be influenced? Complexer to make, as during the time the interval is within daytime it should also not breach the min comfort.
		//-> For now, just taken the min comfort temperature.
		double indoorTemperature_degC = this.heatingPreferences.getMinComfortTemperature_degC(); 
		
		//Loop over temperature in reverse order and find the required building temperature at start of each timestep, so at the end of interval the minComfortTemperature is reached
		for (int i = intervalLength_timeSteps - 1; i >= 0; i--) {
			
			//Calculate how much the temperature will drop in this timestep (What about sun? -> neglect for now)
			double deltaT_degC = (indoorTemperature_degC - ambientTemperatureDuringInterval_degC[i])*lossFactorPerTimeStep_kWhpK / buildingHeatCapacity_kWhpK;
			//Note: Loop is in reverse direction, so we add the deltaT instead of subtract, as we want to find the required indoor temperature at start of interval
			indoorTemperature_degC += deltaT_degC;
		}
		
		//The found indoor temperature is now equal to the required indoor temperature at the start of the no/reduced heating interval.
		
		if(indoorTemperature_degC > this.heatingPreferences.getMaxComfortTemperature_degC()) { // Check if max comfort temperature is not breached if so, limit temperature to the max comfort temperature
			traceln("Warning, the building is not isolated properly for the heatpump off peak heat strategy to comply to the comfort settings without turning on the heating asset at all");
			this.requiredTemperatureAtStartOfReducedHeatingInterval_degC = this.heatingPreferences.getMaxComfortTemperature_degC();	
		}
		else {
			this.requiredTemperatureAtStartOfReducedHeatingInterval_degC = indoorTemperature_degC;
		}
		
		//Calculate needed preheat duration ??
    	//Get temperature setpoint during interval start
		double temperatureSetPointDuringIntervalStart = heatingPreferences.getDayTimeSetPoint_degC();
    	if (startTimeOfReducedHeatingInterval_hr < heatingPreferences.getStartOfDayTime_h() || startTimeOfReducedHeatingInterval_hr >= heatingPreferences.getStartOfNightTime_h()) {
    		temperatureSetPointDuringIntervalStart = heatingPreferences.getNightTimeSetPoint_degC();
    	}
    	
    	double setpointAndPreHeatDeltaT_degC = this.requiredTemperatureAtStartOfReducedHeatingInterval_degC - temperatureSetPointDuringIntervalStart;
    	if(setpointAndPreHeatDeltaT_degC <= 0 ) {
    		this.preHeatDuration_hr = 0;
    	}
    	else {
    		double totalAdditionalEnergyNeededToReachPreheatTemperature_kWh = setpointAndPreHeatDeltaT_degC * buildingHeatCapacity_kWhpK;
    		this.preHeatDuration_hr = 2; // Optional, can approximate this if needed, but then also have to account of fluctuations due to hot water demand, we can add a tolerance of 0.5 hr or something.
    		//For now, preheat duration of 2 hours is assumed.
    	}
	}
  
  
	public void setStartTimeOfReducedHeatingInterval_hr(double startTimeOfReducedHeatingInterval_hr) {
		if(startTimeOfReducedHeatingInterval_hr == this.endTimeOfReducedHeatingInterval_hr) {
			traceln("Start time of reduced heating interval can not be the same as the end time. Reduced heating interval starttime adjustment has been skipped.");
		}
		else {
		  	this.startTimeOfReducedHeatingInterval_hr = startTimeOfReducedHeatingInterval_hr;
		  	this.reducedHeatingIntervalLength_hr = (this.endTimeOfReducedHeatingInterval_hr - this.startTimeOfReducedHeatingInterval_hr + 24) % 24;
		}
	}
	public void setEndTimeOfReducedHeatingInterval_hr(double endTimeOfReducedHeatingInterval_hr) {
		if(endTimeOfReducedHeatingInterval_hr == this.startTimeOfReducedHeatingInterval_hr) {
			traceln("End time of reduced heating interval can not be the same as the start time. Reduced heating interval endtime adjustment has been skipped.");
		}
		else {
		  	this.endTimeOfReducedHeatingInterval_hr = endTimeOfReducedHeatingInterval_hr;
		  	this.reducedHeatingIntervalLength_hr = (this.endTimeOfReducedHeatingInterval_hr - this.startTimeOfReducedHeatingInterval_hr + 24) % 24; 
		}
	}
	  
	public double getStartTimeOfReducedHeatingInterval_hr() {
	  	return this.startTimeOfReducedHeatingInterval_hr;
	}
	public double getEndTimeOfReducedHeatingInterval_hr() {
	  	return this.endTimeOfReducedHeatingInterval_hr;    	
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
				throw new RuntimeException(this.getClass() + " requires a hot water demand to make sense to use this heating management with a heatbuffer.");
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
		if (heatingAsset instanceof J_EAConversionHeatPump) {
			this.currentHeatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
		} else {
			throw new RuntimeException(this.getClass() + " Unsupported heating asset!");    		
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

    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    
    //Store and reset states
	public void storeStatesAndReset() {
	    this.storedI_state_hDegC = this.I_state_hDegC;
	    this.storedFilteredCurrentSetpoint_degC = this.filteredCurrentSetpoint_degC;
	    this.storedrequiredTemperatureAtStartOfReducedHeatingInterval_degC = this.requiredTemperatureAtStartOfReducedHeatingInterval_degC;
		this.I_state_hDegC = 0;
		this.filteredCurrentSetpoint_degC = heatingPreferences.getMinComfortTemperature_degC();
		this.requiredTemperatureAtStartOfReducedHeatingInterval_degC = 20;
		this.isInitialized = false;
	}
	public void restoreStates() {
		this.I_state_hDegC = this.storedI_state_hDegC;
	    this.filteredCurrentSetpoint_degC = this.storedFilteredCurrentSetpoint_degC;
	    this.requiredTemperatureAtStartOfReducedHeatingInterval_degC = storedrequiredTemperatureAtStartOfReducedHeatingInterval_degC;
	    this.isInitialized = true;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}