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
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
    private J_EAConversion heatingAsset;
	private J_HeatingPreferences heatingPreferences;

    // PI control gains
    private double P_gain_kWpDegC = 1;
    private double I_gain_kWphDegC = 0.1;
    private double I_state_hDegC = 0;
    private double timeStep_h;
    
    //Off peak management
    private double preHeatDuration_hr = 1; // Amount of hours that the heatpump will heat some more to be able to bridge the reduced heating interval comfortably
    private double requiredTemperatureAtStartOfReducedHeatingInterval_degC = 2; // Temperature setpoint increase in degrees Celsius that the heatpump will have in the preheatduration time
    private double startTimeOfReducedHeatingInterval_hr = 16; // Hour of the day
    private double endTimeOfReducedHeatingInterval_hr = 21; // Hour of the day -> CAN NOT BE THE SAME AS THE START TIME
    private double reducedHeatingIntervalLength_hr = (endTimeOfReducedHeatingInterval_hr - startTimeOfReducedHeatingInterval_hr + 24) % 24;     
    
    /**
     * Default constructor
     */
    public J_HeatingManagementHeatpumpOffPeak() {
    }

    public J_HeatingManagementHeatpumpOffPeak( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    	this.timeStep_h = gc.energyModel.p_timeStep_h;
    }
    
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	double t_h = gc.energyModel.t_h;
    	double timeOfDay_h = gc.energyModel.t_hourOfDay;
    	
    	if(endTimeOfReducedHeatingInterval_hr == 0) {
    		calculatePreHeatParameters();
    	}
    	
    	//Adjust the hot water and overall heat demand with the buffer and pt
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	double remainingHotWaterDemand_kW = managePTAndHotWaterHeatBuffer(hotWaterDemand_kW);
    	
    	//Get the remaining heat demand (hot water, and potential other profiles)
    	double otherHeatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

    	
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();
    	if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    	}

    	//Determine if time is in reduced Heating interval
		boolean timeIsInReducedHeatingInterval = ((timeOfDay_h - startTimeOfReducedHeatingInterval_hr + 24) % 24) < reducedHeatingIntervalLength_hr;
		boolean timeIsInPreheatInterval = ((timeOfDay_h - startTimeOfReducedHeatingInterval_hr - preHeatDuration_hr + 24) % 24) < preHeatDuration_hr;
		
		//Determine the deltaT_degc
    	double deltaT_degC = currentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed
    	
    	if (timeIsInReducedHeatingInterval) {
    		deltaT_degC = 0;
    	}
    	else if(timeOfDay_h >= (startTimeOfReducedHeatingInterval_hr - preHeatDuration_hr)){
    		deltaT_degC = requiredTemperatureAtStartOfReducedHeatingInterval_degC - building.getCurrentTemperature();
    	}
    	
    	//PI control
    	I_state_hDegC = max(0,I_state_hDegC + deltaT_degC * timeStep_h); // max(0,...) to prevent buildup of negative integrator during warm periods.
    	double buildingHeatingDemand_kW = max(0,deltaT_degC * P_gain_kWpDegC + I_state_hDegC * I_gain_kWphDegC);
    	
    	//Set asset power
    	double assetPower_kW = min(heatingAsset.getOutputCapacity_kW(), buildingHeatingDemand_kW + otherHeatDemand_kW); // minimum not strictly needed as asset will limit power by itself. Could be used later if we notice demand is higher than capacity of heating asset.
		heatingAsset.f_updateAllFlows( assetPower_kW / heatingAsset.getOutputCapacity_kW() );
		
		//Set building power (other heat demand gets bias if asset does not have enough capacity)
		double heatIntoBuilding_kW = max(0, assetPower_kW - otherHeatDemand_kW);    			
		building.f_updateAllFlows( heatIntoBuilding_kW / building.getCapacityHeat_kW() );
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
    
	public void calculatePreHeatParameters() {
		double energyModel_time_h = gc.energyModel.t_h;
		double p_timestep_h = gc.energyModel.p_timeStep_h;
		J_ProfilePointer ambientTemperatureProfilePointer = gc.energyModel.pp_ambientTemperature_degC;
		
		int intervalLength_timeSteps = roundToInt(this.reducedHeatingIntervalLength_hr / p_timestep_h);
		
		double[] ambientTemperatureDuringInterval_degC = new double[intervalLength_timeSteps];
		
		// Get the ambient temperature profile for the interval
		for (int i = 0; i < intervalLength_timeSteps; i++) {
		    double time = (startTimeOfReducedHeatingInterval_hr + i * p_timestep_h) % 24;
		    ambientTemperatureDuringInterval_degC[i] = ambientTemperatureProfilePointer.getValue(time);
		}
			
		// Get the building thermal properties and convert into the same units
		double lossFactorPerTimeStep_kWhpK = this.building.getLossFactor_WpK()/1000.0 * p_timestep_h;
		double buildingHeatCapacity_kWhpK = this.building.getHeatCapacity_JpK()/(3.6e6);
		
		// Start calculation from the known comfort temperature that the building should minimally be at the end of the reduced heating interval
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
    		this.preHeatDuration_hr = 1; // Optional, can calculate this exactly if needed, but then to account of fluctuations due to hot water demand, we can add a tolerance of 0.5 hr or something.
    		//For now, preheat duration of 1 hour is simply assumed.
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

}