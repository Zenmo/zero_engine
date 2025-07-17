import zeroPackage.ZeroMath;
/**
 * J_BatteryPeakShavingForecast
 */	
public class J_BatteryPeakShavingForecast implements I_BatteryAlgorithm {
	
	private double[] batteryChargingForecast_kW = new double[96];
    private GridConnection gc;
	/**
     * Default constructor
     */
    public J_BatteryPeakShavingForecast( GridConnection gc ) {
    	this.gc = gc;
    }

    /**
     * 
     */
    public double determineBatteryBehaviour() {
		int index = roundToInt((gc.energyModel.t_h % 24)/gc.energyModel.p_timeStep_h);
		if(index == 0){
			this.f_peakShavingForecast();
		}
		return this.batteryChargingForecast_kW[index] / gc.p_batteryAsset.getCapacityElectric_kW();
    }
    
    private void f_peakShavingForecast() {
    	double amountOfHoursInADay = 24;
    	double[] nettoBalance_kW = new double[96];

    	//Get elec consumption profile
    	J_EAProfile elecConsumptionProfile = findFirst(gc.c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);

    	J_EAConsumption elecConsumptionConsumptionAsset = findFirst(gc.c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);

    	J_EAProduction elecProductionAsset = findFirst(gc.c_productionAssets, prod -> prod.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);

    	//For simulation that cross the year end
    	double hour_of_simulation_year = gc.energyModel.t_h - gc.energyModel.p_runStartTime_h;
    	//traceln("hour_of_year: " + hour_of_simulation_year);

    	int startTimeDayIndex = roundToInt(hour_of_simulation_year/gc.energyModel.p_timeStep_h);
    	int endTimeDayIndex = roundToInt((hour_of_simulation_year + 24)/gc.energyModel.p_timeStep_h);

    	if(elecConsumptionProfile != null){
    		nettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), 1/gc.energyModel.p_timeStep_h);
    	}
    	if(elecConsumptionConsumptionAsset != null){
    		for(double time = gc.energyModel.t_h; time < gc.energyModel.t_h + 24; time += gc.energyModel.p_timeStep_h){
    			nettoBalance_kW[roundToInt((time-gc.energyModel.t_h)/gc.energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
    		}
    	}

    	if(elecProductionAsset != null){
    		for(double time = gc.energyModel.t_h; time < gc.energyModel.t_h + 24; time += gc.energyModel.p_timeStep_h){
    			nettoBalance_kW[roundToInt((time-gc.energyModel.t_h)/gc.energyModel.p_timeStep_h)] -= elecProductionAsset.profilePointer.getValue(time)*elecProductionAsset.getCapacityElectric_kW();
    		}
    	}


    	////Fill chargesetpoint Array

    	//Initialize chargepoint array
    	this.batteryChargingForecast_kW = new double[96];


    	//Calculate the total export over the day that can be collected by the battery
    	double totalExport_kWh = 0;
    	for(int i = 0; i < nettoBalance_kW.length; i++){
    		if(nettoBalance_kW[i] < 0){
    			totalExport_kWh += min(gc.p_batteryAsset.getCapacityElectric_kW(), -nettoBalance_kW[i])*gc.energyModel.p_timeStep_h;
    		}
    	}
    		

    	//Flatten the morning net balance while charging
    	double totalDailyImport_kWh = 0;
    	for(int i = 0; i < nettoBalance_kW.length; i++){
    		if(i< amountOfHoursInADay/gc.energyModel.p_timeStep_h){
    			totalDailyImport_kWh += max(0,nettoBalance_kW[i]*gc.energyModel.p_timeStep_h);
    		}
    	}
    	double batteryEnergyNeeded_kWh = max(0,(gc.p_batteryAsset.getStorageCapacity_kWh()*(1-gc.p_batteryAsset.getCurrentStateOfCharge_fr()))-totalExport_kWh);
    	double averageDailyConsumption_kW = (totalDailyImport_kWh + batteryEnergyNeeded_kWh)/amountOfHoursInADay;

    	//If 24 hours
    	for(int i = 0; i < nettoBalance_kW.length; i++){
    		this.batteryChargingForecast_kW[i] += averageDailyConsumption_kW - nettoBalance_kW[i];
    	}
    	return;
    }
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}