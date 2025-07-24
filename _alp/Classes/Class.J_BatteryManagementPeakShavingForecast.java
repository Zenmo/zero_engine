import zeroPackage.ZeroMath;
/**
 * J_BatteryManagementPeakShavingForecastGrid
 */	
public class J_BatteryManagementPeakShavingForecast implements I_BatteryManagement {

	private double[] batteryChargingSchedule_kW = new double[96];
    private GridConnection parentGC;
    private Agent target = parentGC;
    private OL_ResultScope targetType = OL_ResultScope.GRIDCONNECTION;
    List<GridConnection> c_targetGridConnections = new ArrayList<GridConnection>();
    double p_timestep_h;
	
	public J_BatteryManagementPeakShavingForecast() {
		
	}
	
    
	public J_BatteryManagementPeakShavingForecast( GridConnection parentGC ) {
    	this.parentGC = parentGC;
    	p_timestep_h = parentGC.energyModel.p_timeStep_h;
    	if (parentGC instanceof GCGridBattery) {
    		this.setTarget(null);
    	} else {
    		this.setTarget(parentGC);
    	}
    }
	
	
	
	public void manageBattery() {
		if (this.target == null) {
			parentGC.p_batteryAsset.f_updateAllFlows(0);
    		return;
    	}
		int index = roundToInt((parentGC.energyModel.t_h % 24)/p_timestep_h);
		if(index == 0){
			this.batteryChargingSchedule_kW = this.calculateBatteryChargingSchedule();
		}
		parentGC.p_batteryAsset.f_updateAllFlows( this.batteryChargingSchedule_kW[index] / parentGC.p_batteryAsset.getCapacityElectric_kW() );
    }
	
	
	
	private double[] getNettoBalanceForecast_kW() {	
		
		double[] nettoBalanceTotal_kW = new double[96];
		double energyModel_time_h = parentGC.energyModel.t_h;

		//For simulation that cross the year end
		double hour_of_simulation_year = energyModel_time_h - parentGC.energyModel.p_runStartTime_h;

		int startTimeDayIndex = roundToInt(hour_of_simulation_year/p_timestep_h);
		int endTimeDayIndex = roundToInt((hour_of_simulation_year + 24)/p_timestep_h);
		
		List<J_EAProfile> profileAssets = new ArrayList<J_EAProfile>();
		List<J_EAConsumption> consumptionAssets = new ArrayList<J_EAConsumption>();
		List<J_EAProduction> productionAssets = new ArrayList<J_EAProduction>();
		
		for (GridConnection GC : c_targetGridConnections){
			profileAssets.addAll(findAll(GC.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD));
			consumptionAssets.addAll(findAll(GC.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND));
			productionAssets.addAll(findAll(GC.c_productionAssets, prod -> prod.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC || prod.energyAssetType == OL_EnergyAssetType.WINDMILL));
		}
			
		for(J_EAProfile profileAsset : profileAssets) {
			double[] tempNettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(profileAsset.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), 1/p_timestep_h);
			for (int i = 0; i < tempNettoBalance_kW.length; i++) {
		    	nettoBalanceTotal_kW[i] += tempNettoBalance_kW[i];
			}
		}
		for(J_EAConsumption consumptionAsset : consumptionAssets) {
			for(double time = energyModel_time_h; time < energyModel_time_h + 24; time += p_timestep_h){
				nettoBalanceTotal_kW[roundToInt((time-energyModel_time_h)/p_timestep_h)] += consumptionAsset.getProfilePointer().getValue(time)*consumptionAsset.yearlyDemand_kWh*consumptionAsset.getConsumptionScaling_fr();
			}
		}
		for(J_EAProduction productionAsset : productionAssets) {
			for(double time = energyModel_time_h; time < energyModel_time_h + 24; time += p_timestep_h){
				nettoBalanceTotal_kW[roundToInt((time-energyModel_time_h)/p_timestep_h)] -= productionAsset.getProfilePointer().getValue(time)*productionAsset.getCapacityElectric_kW();
			}
		}
		return nettoBalanceTotal_kW;
	}
	
	
	
	private double[] calculateBatteryChargingSchedule() {
			
		double[] nettoBalanceTotal_kW = getNettoBalanceForecast_kW();
		double amountOfHoursInADay = 24;
		
		//Initialize chargepoint array
		double[] newBatteryChargingSchedule_kW = new double[96];

		//Calculate the total export over the day that can be collected by the battery
		double totalExport_kWh = 0;
		for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
			if(nettoBalanceTotal_kW[i] < 0){
				totalExport_kWh += min(parentGC.p_batteryAsset.getCapacityElectric_kW(), -nettoBalanceTotal_kW[i])*p_timestep_h;
			}
		}
			
		//Flatten the morning net balance while charging
		double totalDailyImport_kWh = 0;
		for(int i = 0; i < nettoBalanceTotal_kW.length; i++){
			if(i< amountOfHoursInADay/p_timestep_h){
				totalDailyImport_kWh += max(0,nettoBalanceTotal_kW[i]*p_timestep_h);
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