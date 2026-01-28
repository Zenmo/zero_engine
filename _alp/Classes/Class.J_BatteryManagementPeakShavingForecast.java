import zeroPackage.ZeroMath;
/**
 * J_BatteryManagementPeakShavingForecastGrid
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


public class J_BatteryManagementPeakShavingForecast implements I_BatteryManagement {

	private double[] batteryChargingSchedule_kW = new double[96];
    private GridConnection parentGC;
    private Agent target = parentGC;
    private OL_ResultScope targetType = OL_ResultScope.GRIDCONNECTION;
    List<GridConnection> c_targetGridConnections = new ArrayList<GridConnection>();
    private J_TimeParameters timeParameters;
	
    //Stored
	private double[] storedBatteryChargingSchedule_kW;
	
	public J_BatteryManagementPeakShavingForecast() {
		
	}
	
    
	public J_BatteryManagementPeakShavingForecast( GridConnection parentGC, J_TimeParameters timeParameters ) {
    	this.parentGC = parentGC;
    	this.timeParameters = timeParameters;
    	if (parentGC instanceof GCGridBattery) {
    		this.setTarget(null);
    	} else {
    		this.setTarget(parentGC);
    	}
    }
	
	
	
	public void manageBattery(J_TimeVariables timeVariables) {
		if (this.target == null) {
	    	parentGC.f_updateFlexAssetFlows(parentGC.p_batteryAsset, 0.0, timeVariables);
    		return;
    	}
		int index = roundToInt(timeVariables.getTimeOfDay_h()/timeParameters.getTimeStep_h());
		if(index == 0){
			this.batteryChargingSchedule_kW = this.calculateBatteryChargingSchedule(timeVariables);
		}
		parentGC.f_updateFlexAssetFlows(parentGC.p_batteryAsset, this.batteryChargingSchedule_kW[index] / parentGC.p_batteryAsset.getCapacityElectric_kW(), timeVariables);
    }
	
	
	
	private double[] getNettoBalanceForecast_kW(J_TimeVariables timeVariables) {	
		
		double[] nettoBalanceTotal_kW = new double[96];
		double energyModel_time_h = timeVariables.getT_h();

		//For simulation that cross the year end
		double hour_of_simulation_year = timeVariables.getAnyLogicTime_h();

		int startTimeDayIndex = roundToInt(hour_of_simulation_year/timeParameters.getTimeStep_h());
		int endTimeDayIndex = roundToInt((hour_of_simulation_year + 24)/timeParameters.getTimeStep_h());
		
		List<J_EAProfile> elecConsumptionProfiles = new ArrayList<J_EAProfile>(); //survey inkoop profile data
		List<J_EAProfile> elecHeatPumpProfiles = new ArrayList<J_EAProfile>(); //survey WP profile data
		List<J_EAProfile> elecEVProfiles = new ArrayList<J_EAProfile>(); //Custom EV profile data
		List<J_EAProfile> surveyHeatDemandProfiles = new ArrayList<J_EAProfile>(); //survey gas to heat builing profiles
		List<J_EAConsumption> genericHeatDemandProfiles = new ArrayList<J_EAConsumption>(); //Generic gas to heat builing profiles
		List<J_EAConsumption> genericBuildingProfiles = new ArrayList<J_EAConsumption>(); //Generic inkoop builing profiles
		List<J_EAProduction> productionAssetProfiles = new ArrayList<J_EAProduction>(); // Production profiles
		
		for (GridConnection GC : c_targetGridConnections){		
			elecConsumptionProfiles.addAll(findAll(GC.c_profileAssets, profile -> profile.assetFlowCategory == OL_AssetFlowCategories.fixedConsumptionElectric_kW));
			elecHeatPumpProfiles.addAll(findAll(GC.c_profileAssets, profile -> profile.assetFlowCategory == OL_AssetFlowCategories.heatPumpElectricityConsumption_kW));
			elecEVProfiles.addAll(findAll(GC.c_profileAssets, profile -> profile.assetFlowCategory == OL_AssetFlowCategories.evChargingPower_kW));
			if(GC.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && !GC.f_getHeatingTypeIsGhost()) {
				surveyHeatDemandProfiles.addAll(findAll(GC.c_profileAssets, profile -> profile.energyCarrier == OL_EnergyCarriers.HEAT));
				genericHeatDemandProfiles.addAll(findAll(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.HEAT_DEMAND));
			}
			genericBuildingProfiles.addAll(findAll(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND));
			productionAssetProfiles.addAll(findAll(GC.c_productionAssets, prod -> prod.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC || prod.energyAssetType == OL_EnergyAssetType.WINDMILL));
		}
		
		for(J_EAProfile elecConsumptionProfile : elecConsumptionProfiles) {
			if(elecConsumptionProfile != null){
				double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), elecConsumptionProfile.getProfileScaling_fr()/timeParameters.getTimeStep_h());
				for (int i = 0; i < tempNettoBalance_kW.length; i++) {
					nettoBalanceTotal_kW[i] += tempNettoBalance_kW[i];
				}
			}
		}
		for(J_EAProfile elecHeatPumpProfile : elecHeatPumpProfiles) {
			if(elecHeatPumpProfile != null){
				double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecHeatPumpProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), elecHeatPumpProfile.getProfileScaling_fr()/timeParameters.getTimeStep_h());
				for (int i = 0; i < tempNettoBalance_kW.length; i++) {
					nettoBalanceTotal_kW[i] += tempNettoBalance_kW[i];
				}
			}
		}
		for(J_EAProfile elecEVProfile : elecEVProfiles) {
			if(elecEVProfile != null){
				double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecEVProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), elecEVProfile.getProfileScaling_fr()/timeParameters.getTimeStep_h());
				for (int i = 0; i < tempNettoBalance_kW.length; i++) {
					nettoBalanceTotal_kW[i] += tempNettoBalance_kW[i];
				}
			}
		}
		for(J_EAProfile surveyHeatDemandProfile : surveyHeatDemandProfiles) {
			if(surveyHeatDemandProfile != null){
				double[] heatPower_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(surveyHeatDemandProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), surveyHeatDemandProfile.getProfileScaling_fr()/timeParameters.getTimeStep_h());
				//traceln(heatPower_kW);
				double eta_r = parentGC.energyModel.avgc_data.p_avgEfficiencyHeatpump_fr;
				double outputTemperature_degC = parentGC.energyModel.avgc_data.p_avgOutputTemperatureElectricHeatpump_degC;
			    for(double time = energyModel_time_h; time < energyModel_time_h + 24; time += timeParameters.getTimeStep_h()){
			    	double baseTemperature_degC = parentGC.energyModel.pp_ambientTemperature_degC.getValue(time);
			    	double COP_r = eta_r * ( 273.15 + outputTemperature_degC ) / ( outputTemperature_degC - baseTemperature_degC );
			    	nettoBalanceTotal_kW[roundToInt((time-energyModel_time_h)/timeParameters.getTimeStep_h())] += heatPower_kW[roundToInt((time-energyModel_time_h)/timeParameters.getTimeStep_h())] / COP_r;
				}
			}
		}
		for(J_EAConsumption genericHeatDemandProfile : genericHeatDemandProfiles) {
			if(genericHeatDemandProfile != null){
										
				double eta_r = parentGC.energyModel.avgc_data.p_avgEfficiencyHeatpump_fr;
				double outputTemperature_degC = parentGC.energyModel.avgc_data.p_avgOutputTemperatureElectricHeatpump_degC;
				
				for(double time = energyModel_time_h; time < energyModel_time_h + 24; time += timeParameters.getTimeStep_h()){
				    double baseTemperature_degC = parentGC.energyModel.pp_ambientTemperature_degC.getValue(time);
				    double COP_r = eta_r * ( 273.15 + outputTemperature_degC ) / ( outputTemperature_degC - baseTemperature_degC );
					
				    //traceln(genericHeatDemandProfile.getProfilePointer().getValue(time)*genericHeatDemandProfile.yearlyDemand_kWh);
				    nettoBalanceTotal_kW[roundToInt((time-energyModel_time_h)/timeParameters.getTimeStep_h())] += genericHeatDemandProfile.getProfilePointer().getValue(time)*genericHeatDemandProfile.yearlyDemand_kWh*genericHeatDemandProfile.getConsumptionScaling_fr() / COP_r;
				}
			}
		}
		for(J_EAConsumption genericBuildingProfile : genericBuildingProfiles) {
			if(genericBuildingProfile != null){ //table function 
				for(double time = energyModel_time_h; time < energyModel_time_h + 24; time += timeParameters.getTimeStep_h()){
					nettoBalanceTotal_kW[roundToInt((time-energyModel_time_h)/timeParameters.getTimeStep_h())] += genericBuildingProfile.getProfilePointer().getValue(time)*genericBuildingProfile.yearlyDemand_kWh*genericBuildingProfile.getConsumptionScaling_fr();
				}
			}
		}
		for(J_EAProduction productionAssetProfile : productionAssetProfiles) {
			if (productionAssetProfile != null) { //table function 
				for(double time = energyModel_time_h; time < energyModel_time_h + 24; time += timeParameters.getTimeStep_h()){
					nettoBalanceTotal_kW[roundToInt((time-energyModel_time_h)/timeParameters.getTimeStep_h())] -= productionAssetProfile.getProfilePointer().getValue(time)*productionAssetProfile.getCapacityElectric_kW();
				}
			}
		}
		
		
		return nettoBalanceTotal_kW;
	}
	
	
	
	private double[] calculateBatteryChargingSchedule(J_TimeVariables timeVariables) {
			
		double[] nettoBalanceTotal_kW = getNettoBalanceForecast_kW(timeVariables);
		double amountOfHoursInADay = 24;
		
		//Initialize chargepoint array
		double[] newBatteryChargingSchedule_kW = new double[96];

		//Calculate the total export over the day that can be collected by the battery
		double totalExport_kWh = 0;
		for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
			if(nettoBalanceTotal_kW[i] < 0){
				totalExport_kWh += min(parentGC.p_batteryAsset.getCapacityElectric_kW(), -nettoBalanceTotal_kW[i])*timeParameters.getTimeStep_h();
			}
		}
			
		//Flatten the morning net balance while charging
		double totalDailyImport_kWh = 0;
		for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
			if(i< amountOfHoursInADay/timeParameters.getTimeStep_h()){
				totalDailyImport_kWh += max(0,nettoBalanceTotal_kW[i]*timeParameters.getTimeStep_h());
			}
		}

		double batteryEnergyNeeded_kWh = max(0,(parentGC.p_batteryAsset.getStorageCapacity_kWh()*(1-parentGC.p_batteryAsset.getCurrentStateOfCharge_fr()))/parentGC.p_batteryAsset.getChargingEfficiency_r()-totalExport_kWh);
		double averageDailyConsumption_kW = (totalDailyImport_kWh + batteryEnergyNeeded_kWh)/amountOfHoursInADay;

		//If 24 hours
		for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
			newBatteryChargingSchedule_kW[i] += averageDailyConsumption_kW - nettoBalanceTotal_kW[i];
		}
		
		return newBatteryChargingSchedule_kW;
	}
	
	
	
	public void setTarget( Agent agent ) {
    	if ( agent == null) {
    		target = null;
    		this.targetType = null;
    	}
    	else if (agent == this.parentGC) {
    		target = agent;
    		this.targetType = OL_ResultScope.GRIDCONNECTION;
    		c_targetGridConnections = new ArrayList<GridConnection>();
    		c_targetGridConnections.add((GridConnection)target);
    	}
    	else if (agent instanceof GridNode) {
    		target = agent;
    		this.targetType = OL_ResultScope.GRIDNODE;
    		c_targetGridConnections = new ArrayList<GridConnection>(((GridNode)target).f_getAllLowerLVLConnectedGridConnections());
    	}
    	else if (agent instanceof EnergyCoop) {
    		target = agent;
    		this.targetType = OL_ResultScope.ENERGYCOOP;
    		c_targetGridConnections = new ArrayList<GridConnection>(((EnergyCoop)target).f_getAllChildMemberGridConnections());
    	}
    	else {
    		throw new RuntimeException("Not able to set " + agent + " as a target for J_BatteryPeakShaving");
    	}
    }

	
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.parentGC;
    }
    
    
	//Store and reset states
	public void storeStatesAndReset() {
		this.storedBatteryChargingSchedule_kW = batteryChargingSchedule_kW;
		this.batteryChargingSchedule_kW = new double[batteryChargingSchedule_kW.length];
	}
	public void restoreStates() {
		this.batteryChargingSchedule_kW = this.storedBatteryChargingSchedule_kW;
	}
	
	@Override
	public String toString() {
		return "parentGC: " + parentGC +
		", target: " + target +
		", c_targetGridConnections: " + c_targetGridConnections;
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}