/**
 * J_HeatingManagementGridAware
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

public class J_HeatingManagementGridAware implements I_HeatingManagement {
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
    private double P_gain_kWpDegC = 1*3;
    private double I_gain_kWphDegC = 0.1*3;
    private double I_state_hDegC = 0;
    private double timeStep_h;
    
    //Temperature setpoint low pass filter
    private double previousSetpoint_degC = 0;
    private double tempSetpointLowpass_alpha = 1/4; // Smooth in four time steps.
    /**
     * Default constructor
     */
    public J_HeatingManagementGridAware() {
    }

    public J_HeatingManagementGridAware( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
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
    	
    	
    	//Adjust the hot water and overall heat demand with the buffer and pt
    	double hotWaterDemand_kW = gc.p_DHWAsset != null ? gc.p_DHWAsset.getLastFlows().get(OL_EnergyCarriers.HEAT) : 0;
    	double remainingHotWaterDemand_kW = managePTAndHotWaterHeatBuffer(hotWaterDemand_kW);
    	
    	//Get the remaining heat demand (hot water, and potential other profiles)
    	double otherHeatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

    	//Determine if in reduced heating mode
		boolean inReducedHeatingMode = getInReducedHeatingMode();
		boolean inAdditionalHeatingMode = getInAdditionalHeatingMode();
		
    	//Get the current temperature setpoint dependend on day/night time and noheat/preheat interval settings
    	double currentSetpoint_degC = heatingPreferences.getDayTimeSetPoint_degC();
    	
    	if(inReducedHeatingMode) {
    		currentSetpoint_degC = heatingPreferences.getMinComfortTemperature_degC(); // -> prevents fast response during interval if min comfort is breached
    	}
    	else if(inAdditionalHeatingMode) {
    		currentSetpoint_degC = heatingPreferences.getMaxComfortTemperature_degC(); // -> prevents fast response during interval if min comfort is breached
    	}
    	else if (timeOfDay_h < heatingPreferences.getStartOfDayTime_h() || timeOfDay_h >= heatingPreferences.getStartOfNightTime_h()) {
    		currentSetpoint_degC = heatingPreferences.getNightTimeSetPoint_degC();
    	}
    	
    	//Smooth the setpoint signal
    	currentSetpoint_degC = this.previousSetpoint_degC + tempSetpointLowpass_alpha * (currentSetpoint_degC - this.previousSetpoint_degC);
    	this.previousSetpoint_degC = currentSetpoint_degC;
    	
		//Calculate the deltaT_degc
		double deltaT_degC = currentSetpoint_degC - building.getCurrentTemperature(); // Positive deltaT when heating is needed
    	
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
    
    private boolean getInReducedHeatingMode() {
    	boolean inReducedHeatingMode = false;

    	//Determine if in reduced heating mode based on current state of the grid node
    	GridNode targetNode = this.gc.p_parentNodeElectric;
    	if(targetNode.v_currentLoad_kW > targetNode.v_currentLoadElectricityLowPassed_kW) {;
    		inReducedHeatingMode = true;
    	}
    	
    	return inReducedHeatingMode;
    }
    
    private boolean getInAdditionalHeatingMode() {
    	boolean inAdditionalHeatingMode = false;

    	//Determine if in reduced heating mode based on current state of the grid node
    	GridNode targetNode = this.gc.p_parentNodeElectric;
    	if(targetNode.v_currentLoad_kW < 0) {;
    		inAdditionalHeatingMode = true;
    	}
    	
    	return inAdditionalHeatingMode;
    }
    
    private double  managePTAndHotWaterHeatBuffer(double hotWaterDemand_kW){
    	
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
    	//Initialize previous setpoint
    	this.previousSetpoint_degC = heatingPreferences.getMinComfortTemperature_degC();
    	
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