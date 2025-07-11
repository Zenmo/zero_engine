/**
 * J_BatteryAlgorithmBas
 */	

import zeroPackage.ZeroMath;

public class J_BatteryAlgorithmBas implements Serializable {

	private GridConnection parentGC;
	private double[] previousTwoBatterySoC_fr = new double[]{0,0};
	private ArrayList<Double> batteryTurningPoints_fr = new ArrayList();
	private double amountOfCycles = 0; // Cycle is NOT defined as amount of chargeEnergy/batteryCapacity; but rather as a count of activities
    /**
     * Default constructor
     */
    public J_BatteryAlgorithmBas(GridConnection parentGC) {
    	
    	this.parentGC = parentGC;
    	
    }
    
    public void resetTurningPoints() {
    	previousTwoBatterySoC_fr = new double[]{0,0};
    	batteryTurningPoints_fr = new ArrayList();
    }
    
    public ArrayList<Double> getBatteryTurningPoints_fr () {
    	return new ArrayList(batteryTurningPoints_fr);
    }
    
    public double getAmountOfBatteryCycles_cycles () {
    	return amountOfCycles;
    }
    
    public void calculateTurningPointDuringRapidRun(double currentBatterySoC_fr) { 
    	currentBatterySoC_fr = roundToDecimal(currentBatterySoC_fr,8);
    	
    	if (previousTwoBatterySoC_fr[0] >= previousTwoBatterySoC_fr[1] && previousTwoBatterySoC_fr[1] < currentBatterySoC_fr) {
    		batteryTurningPoints_fr.add(previousTwoBatterySoC_fr[1]);
    	}
    	else if (previousTwoBatterySoC_fr[0] <= previousTwoBatterySoC_fr[1] && previousTwoBatterySoC_fr[1] > currentBatterySoC_fr) {
    		batteryTurningPoints_fr.add(previousTwoBatterySoC_fr[1]);
    	}
    	
    	previousTwoBatterySoC_fr[0] = previousTwoBatterySoC_fr[1];
    	previousTwoBatterySoC_fr[1] = currentBatterySoC_fr;
    }
    
    public double calculateAverageDoD(ArrayList<Double> batteryTurningPoints_fr) {
    	
    	amountOfCycles = 0; // Cycle is NOT defined as amount of chargeEnergy/batteryCapacity; but rather as a count of activities
    	double cumulativeDoD = 0;
    	
    	ArrayList<Double> localBatteryTurningPoints_fr = new ArrayList(batteryTurningPoints_fr);
    	
    	while (localBatteryTurningPoints_fr.size() > 2) {
    		boolean hasFoundCycle = false;
    		for (int i=0; i < localBatteryTurningPoints_fr.size()-2; i++) {
    			
    			if (abs(localBatteryTurningPoints_fr.get(i+1)-localBatteryTurningPoints_fr.get(i+2)) <= abs(localBatteryTurningPoints_fr.get(i)-localBatteryTurningPoints_fr.get(i+1))) { //abs(Y-Z) <= abs(Z-Y)
    				cumulativeDoD += abs(localBatteryTurningPoints_fr.get(i+1)-localBatteryTurningPoints_fr.get(i+2));
    				localBatteryTurningPoints_fr.remove(i+2); //Remove Z first, otherwise order changes
    				localBatteryTurningPoints_fr.remove(i+1); //Remove Y
    				amountOfCycles += 1; //Remove 2 point-> 2lines -> 2 half cycles -> 1 full cycle 
    				hasFoundCycle = true;
    				break;
    				// charge or discharge is or is not included in DoD -> TBD; so amountOfCycles could be 0.5 (if we had additional while loop) or 1
    			}
    		}
    		if (!hasFoundCycle) {
    			break; //fail safe to prevent stuck in loop
    		}
    	}
    	
    	//Add final residual half-cycle
    	if (localBatteryTurningPoints_fr.size() > 1) {
    		cumulativeDoD += abs(localBatteryTurningPoints_fr.get(0)-localBatteryTurningPoints_fr.get(1));
    		amountOfCycles += 0.5;
    		traceln("Residual present");
    	}
    	
    	double averageDoD = cumulativeDoD/amountOfCycles;
    	traceln("Remaining turning points are " + localBatteryTurningPoints_fr);
    	traceln(batteryTurningPoints_fr.size());
    	traceln(amountOfCycles);
    	return averageDoD;
    	
    }
    
    public double calculateLifetimeBattery_yr() {
    	
    	 double averageDoD = calculateAverageDoD(batteryTurningPoints_fr);
    	 
    	 double totalYearlyCycles = parentGC.v_rapidRunData.getTotalBatteryCycles(); // Defined that total annual energy charged/battery capacity
    	 
    	 // function and parameters (li-ion) are extracted from source:
    	 // https://www.researchgate.net/publication/330142356_Optimal_Operational_Planning_of_Scalable_DC_Microgrid_with_Demand_Response_Islanding_and_Battery_Degradation_Cost_Considerations
    	 double alpha = -5440.35;
    	 double beta = 1191.54;
    	 
    	 double batteryCycleLife_cycles = alpha * Math.log(averageDoD) + beta;
    	 traceln("Battery Lifetime from totalYearlyCycles is " + batteryCycleLife_cycles/totalYearlyCycles);
    	 double lifetimeBattery_yr = batteryCycleLife_cycles/amountOfCycles;
    	 return lifetimeBattery_yr;
    	 
    }
    
    public double[] calculateMonthlyPeakDemand_kW() {
    	
    	double[] monthlyPeakDemand_kW = new double[12];
    	
    	int[] daysInMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
    	
        int sampleCounter = 0;
    	
    	double[] loadArray_kW = parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW();
    	
    	for(int month=0; month < daysInMonth.length; month++) {
    		
    		int samplesInMonth = daysInMonth[month] * 96;
    		int startMonthIndex = sampleCounter;
    		int endMonthIndex = sampleCounter + samplesInMonth;
    		
    		double maxLoad_kW = 0;
            for (int i = startMonthIndex; i < endMonthIndex && i < loadArray_kW.length; i++) {
                double absLoad_kW = Math.abs(loadArray_kW[i]);
                if (absLoad_kW > maxLoad_kW) {
                	maxLoad_kW = absLoad_kW;
                }
            }
            monthlyPeakDemand_kW[month] = maxLoad_kW;
            //traceln(monthlyPeakDemand_kW[month]);
    		sampleCounter += samplesInMonth;
    	}
    	
    	return monthlyPeakDemand_kW;
    	
    }
    
    public double calculateCapacityRate_euro() {
    	
    	double costsCapacityRate_euro = 0;
    	
    	GridNode GN_T0 = findFirst(parentGC.energyModel.pop_gridNodes, p -> p.p_gridNodeID.equals("T0")); //.equals(p_parentNodeID)
    	double contractedCapacity_kW = GN_T0.p_capacity_kW;
    	//traceln("The contracted capacity is " + contractedCapacity_kW + " kW");
    	double VAT_fr = 0.21;
    	double annualConnectionRate_euro_p_yr = 5351;
    	double annualFixedTransportRate_euro_p_yr = 2760;
    	double annualContractCapacityRate_euro_p_kW_yr = 42.10;
    	double monthlyPeakPowerRate_euro_p_kW_month = 4.48;
    	
    	double[] monthlyPeakDemand_kW = calculateMonthlyPeakDemand_kW();
    	
    	costsCapacityRate_euro = (1+VAT_fr)*(annualConnectionRate_euro_p_yr + annualFixedTransportRate_euro_p_yr + annualContractCapacityRate_euro_p_kW_yr * contractedCapacity_kW + monthlyPeakPowerRate_euro_p_kW_month * Arrays.stream(monthlyPeakDemand_kW).sum());
    	//traceln("The sum of peak is " + Arrays.stream(monthlyPeakDemand_kW).sum() + " kW");
    	return costsCapacityRate_euro;
    	
    }
    
    public double calculateElectricityImportCosts_euro() { 
    	
    	double VAT_fr = 0.21;
    	double ODE_eur_p_kwh = 0;
    	double EB_eur_p_kwh = 0.03868; // https://www.belastingdienst.nl/wps/wcm/connect/bldcontentnl/belastingdienst/zakelijk/overige_belastingen/belastingen_op_milieugrondslag/energiebelasting/
    	
    	double costsElectricityImport_euro = 0;
    	double currentElectricityPriceCharge_eurpkWh = 0;
    	
    	for (int i = 0; i < parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length; i++) {
    		//traceln(parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length);
    		currentElectricityPriceCharge_eurpkWh = parentGC.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getAllValues()[(int) Math.floor(i*parentGC.energyModel.p_timeStep_h)] / 1000;
    		costsElectricityImport_euro += (1+VAT_fr)*((currentElectricityPriceCharge_eurpkWh + ODE_eur_p_kwh + EB_eur_p_kwh) * max(0,parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i]) * parentGC.energyModel.p_timeStep_h);
    		    		
    	}
    	
    	return costsElectricityImport_euro;
    	//traceln("%f",costsElectricityImport_euro);
    	
    }
    
    public double calculateElectricityExportCosts_euro() { 

    	double costsElectricityExport_euro = 0;
    	double currentElectricityPriceCharge_eurpkWh = 0;	

    	for (int i = 0; i < parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length; i++) {
    		
    		currentElectricityPriceCharge_eurpkWh = parentGC.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getAllValues()[(int) Math.floor(i*parentGC.energyModel.p_timeStep_h)] / 1000;
    		costsElectricityExport_euro += currentElectricityPriceCharge_eurpkWh * max(0,-parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i]) * parentGC.energyModel.p_timeStep_h;
    		//traceln("%f", max(0,-parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i]));
    	}
    	
    	return costsElectricityExport_euro;
    	//traceln("%f",costsElectricityExport_euro);
    	
    }
    
    public double calculateElectricityNetCosts_euro() { 

    	double costsElectricityNet_euro = 0;

    	costsElectricityNet_euro = calculateCapacityRate_euro() + calculateElectricityImportCosts_euro() - calculateElectricityExportCosts_euro();
    	
    	return costsElectricityNet_euro;
    	//traceln("%f",costsElectricityNet_euro);
    	
    }
    
    public double calculateReturnOnInvestment(double year, double CAPEX, double previousCostsElectricityNet_euro) { 

    	double ReturnOnInvestment_year = 0;
    	double currentCostsElectricityNet_euro = calculateElectricityNetCosts_euro();
    	
    	if (currentCostsElectricityNet_euro < previousCostsElectricityNet_euro) {

    		ReturnOnInvestment_year = year * (previousCostsElectricityNet_euro - currentCostsElectricityNet_euro)/CAPEX;
    		return ReturnOnInvestment_year;
    		
    	}
    	
    	ReturnOnInvestment_year = Double.POSITIVE_INFINITY;
    	return ReturnOnInvestment_year;
    	//traceln("%f",ReturnOnInvestment_year);
    	
    }
    
    public double calculatePaybackPeriod(double CAPEX, double previousCostsElectricityNet_euro) { 

    	double PaybackPeriod_year = 0;
    	double currentCostsElectricityNet_euro = calculateElectricityNetCosts_euro();
    	
    	if (currentCostsElectricityNet_euro < previousCostsElectricityNet_euro) {

    		PaybackPeriod_year = CAPEX/(previousCostsElectricityNet_euro - currentCostsElectricityNet_euro);
    		return PaybackPeriod_year;
    		
    	}
    	
    	PaybackPeriod_year = Double.POSITIVE_INFINITY;
    	return PaybackPeriod_year;
    	//traceln("%f",PaybackPeriod_year);
    	
    }
    
    public double calculateChargeSetpointPriceGrid_kW(double SOC_fr){
    	
    	double WTPfeedbackGain_eurpSOC = 0.25; // When SOC-error is 100%, adjust WTP price by 0.5 eurpkWh
    	double priceGain_kWhpeur = 3; // How strongly to ramp up power with price-delta's
    		
    	double chargeSetpoint_kW = 0;	
    	double currentElectricityPriceCharge_eurpkWh;
    		
    	//GridNode GN = parentGC.l_parentNodeElectric.getConnectedAgent(); // Get parent from GCGridBattery = GridNode GN
    	currentElectricityPriceCharge_eurpkWh = parentGC.energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000; // Get current electricity price from coupled GridNode
    		
    	parentGC.v_electricityPriceLowPassed_eurpkWh += parentGC.v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - parentGC.v_electricityPriceLowPassed_eurpkWh );
    	// EMA_new_price = EMA_old_price + smoothing_factor * (current_Price - EMA_old_price) = smoothing_factor * current_Price + EMA_old_price*(1-smoothing_factor)
    	// Exponential Moving Average = alpha * current_Price + EMA_old_price*(1-alpha)
    		
    	double currentCoopElectricitySurplus_kW = -parentGC.p_parentNodeElectric.v_currentLoad_kW + parentGC.v_previousPowerElectricity_kW; // additional power the battery can inject or withdraw without exceeding the node’s connection capacity?
    	double CoopConnectionCapacity_kW = 0.95*parentGC.p_parentNodeElectric.p_capacity_kW; // Only allow 90-95% of nominal grid capacity
    	//traceln("Test");
    		
    	double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity  // availableChargekW = 
    	double availableDischargePower_kW = currentCoopElectricitySurplus_kW - CoopConnectionCapacity_kW; // Max discharging power within grid capacity
    		
    		
    	double SOC_setp_fr = 0.5; //0.9 - 2*GN.v_electricityYieldForecast_fr;	
    		
    	double SOC_deficit_fr = SOC_setp_fr - SOC_fr; // SOC_deficit_fr = difference between desired and current SOC.
    		
    	double WTP_eurpkWh = parentGC.v_electricityPriceLowPassed_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC; //
    		
    	chargeSetpoint_kW = parentGC.p_batteryAsset.getCapacityElectric_kW()*(WTP_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_kWhpeur; // battery_power * (WTP - current_price) * Gain
    						
    	chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
    	
    	
    	
    	return chargeSetpoint_kW;
    	
    }
    /*
    public double calculateChargeSetpointPeakShavingAdvancedGrid_kW(double SOC_fr){
    
    	double[] nettoBalance_kW = new double[96];
    
    	//Get elec consumption profile
    	J_EAProfile elecConsumptionProfile = findFirst(c_profileAssets, profile -> profile.profileType == OL_ProfileAssetType.ELECTRICITYBASELOAD);
   
    	J_EAConsumption elecConsumptionConsumptionAsset = findFirst(c_consumptionAssets, cons -> cons.energyAssetType == OL_EnergyAssetType.ELECTRICITY_DEMAND);
   
    	J_EAProduction elecProductionAsset = findFirst(c_productionAssets, prod -> prod.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC);
   
    	int startTimeDayIndex = roundToInt(energyModel.t_h/energyModel.p_timeStep_h);
    	int endTimeDayIndex = roundToInt((energyModel.t_h + 24)/energyModel.p_timeStep_h);
   
    	if(elecConsumptionProfile != null){
    		nettoBalance_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(elecConsumptionProfile.a_energyProfile_kWh, startTimeDayIndex, endTimeDayIndex), 1/energyModel.p_timeStep_h);
    	}
    	if(elecConsumptionConsumptionAsset != null){
    		for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
    			nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] += elecConsumptionConsumptionAsset.profilePointer.getValue(time)*elecConsumptionConsumptionAsset.yearlyDemand_kWh*elecConsumptionConsumptionAsset.getConsumptionScaling_fr();
    		}
    	}
   
    	if(elecProductionAsset != null){
    		for(double time = energyModel.t_h; time < energyModel.t_h + 24; time += energyModel.p_timeStep_h){
    			nettoBalance_kW[roundToInt((time-energyModel.t_h)/energyModel.p_timeStep_h)] -= elecProductionAsset.profilePointer.getValue(time)*elecProductionAsset.getCapacityElectric_kW();
    		}
    	}
   
   
    	double precision_kW = 0.1;
   
    	double batteryStorageCapacity_kWh = p_batteryAsset.getStorageCapacity_kWh();
   
    	/*
  		while (batteryStorageCapacity_kWh > 0){
   
  			indexMax = nettoBalance_kW.getMaxIndex();
  		
  			nettoBalance_kW[indexMax] -= precision_kW;
  		
  			batteryStorageCapacity_kWh - precision_kW*energyModel.p_timeStep_h;
  		}
    	 */
    	/*
    	//Integral of peaks calculation to determine the maximum peak after the day
    	double maxPeak_kW = Arrays.stream(nettoBalance_kW).max().getAsDouble();
    	double peakSurface_kWh = 0;
    	while(peakSurface_kWh < batteryStorageCapacity_kWh && maxPeak_kW > 0){
    		maxPeak_kW -= precision_kW;
    		peakSurface_kWh = 0;
    		for(int i = 0; i < nettoBalance_kW.length; i++){
    			if(nettoBalance_kW[i] > maxPeak_kW){
    				peakSurface_kWh += (nettoBalance_kW[i] - maxPeak_kW) * energyModel.p_timeStep_h;
    			}
    		}
    	}
   
    	////Fill chargepoint Array
   
    	//Initialize chargepoint array
    	v_batteryChargingForecast_kW = new double[96];
   
   
    	//Calculate the total export over the day that can be collected by the battery
    	double totalExport_kWh = 0;
    	for(int i = 0; i < nettoBalance_kW.length; i++){
    		if(nettoBalance_kW[i] < 0){
    			totalExport_kWh += min(p_batteryAsset.getCapacityElectric_kW(), -nettoBalance_kW[i])*energyModel.p_timeStep_h;
    		}
    	}
  	
   
    	//Define the amount of charging hours in the morning that the battery will charge to catch the peaks
    	// -> More hours, means less charging peaks
    	double amountOfChargingHours = 8; //8 geeft prima kpis
   
   
    	//Flatten the morning net balance while charging
    	double totalMorningConsumption_kWh = 0;
    	for(int i = 0; i < nettoBalance_kW.length; i++){
    		if(i< amountOfChargingHours/energyModel.p_timeStep_h){
    			totalMorningConsumption_kWh += max(0,nettoBalance_kW[i]*energyModel.p_timeStep_h);
    		}
    	}
   
    	double batteryEnergyNeeded_kWh = max(0,(p_batteryAsset.getStorageCapacity_kWh()*(1-p_batteryAsset.getCurrentStateOfCharge()))-totalExport_kWh);
    	double averageConsumptionPerMorning_kW = (totalMorningConsumption_kWh + batteryEnergyNeeded_kWh)/amountOfChargingHours;
   
   
   
    	//Distribute charging equally over the morning hours
    	double minimumChargingPower_kW = max(0,((p_batteryAsset.getStorageCapacity_kWh()*(1-p_batteryAsset.getCurrentStateOfCharge()))-totalExport_kWh)/amountOfChargingHours);
   
    	//If 24 hours
    	for(int i = 0; i < nettoBalance_kW.length; i++){
    		v_batteryChargingForecast_kW[i] += averageConsumptionPerMorning_kW - nettoBalance_kW[i];
    	}
   
    	/*
  		for(int i = 0; i < nettoBalance_kW.length; i++){
  			else if(i< amountOfChargingHours/energyModel.p_timeStep_h){//Flatten the morning net balance during charging
  				v_batteryChargingForecast_kW[i] += averageConsumptionPerMorning_kW - max(0,nettoBalance_kW[i]);
  			}
  			else if(nettoBalance_kW[i] > maxPeak_kW){//Flatten the peaks above the maximum defined peak after shaving
  				v_batteryChargingForecast_kW[i] += maxPeak_kW - nettoBalance_kW[i];
  			}
  			else if(nettoBalance_kW[i] < 0){//Charge when there is export of energy
  				v_batteryChargingForecast_kW[i] += -nettoBalance_kW[i];
  			}
  		}
    	*/
   
    	/*
  		for(int i = 0; i < nettoBalance_kW.length; i++){
  			if(nettoBalance_kW[i] > maxPeak_kW){//Flatten the peaks above the maximum defined peak after shaving
  				v_batteryChargingForecast_kW[i] += maxPeak_kW - nettoBalance_kW[i];
  			}
  			if(nettoBalance_kW[i] < 0){//Charge when there is export of energy
  				v_batteryChargingForecast_kW[i] += -nettoBalance_kW[i];
  			}
  			//Distribute charging equally over the morning hours
  			if(i< amountOfChargingHours/energyModel.p_timeStep_h){
  				v_batteryChargingForecast_kW[i] += minimumChargingPower_kW;
  			}
  		}
    	 
    }*/
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