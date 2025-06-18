/**
 * J_BatteryAlgorithmBas
 */	

//import org.apache.poi.xssf.usermodel.*;

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
    
    public double calculateElectricityImportCosts_euro() { 

    	double costsElectricityImport_euro = 0;
    	double currentElectricityPriceCharge_eurpkWh = 0;	

    	for (int i = 0; i < parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length; i++) {
    		
    		currentElectricityPriceCharge_eurpkWh = parentGC.energyModel.tf_dayAheadElectricityPricing_eurpMWh.get(i) / 1000;
    		costsElectricityImport_euro += currentElectricityPriceCharge_eurpkWh * max(0,parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i]) * parentGC.energyModel.p_timeStep_h;
    		    		
    	}
    	
    	return costsElectricityImport_euro;
    	//traceln("%f",costsElectricityImport_euro);
    	
    }
    
    public double calculateElectricityExportCosts_euro() { 

    	double costsElectricityExport_euro = 0;
    	double currentElectricityPriceCharge_eurpkWh = 0;	

    	for (int i = 0; i < parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length; i++) {
    		
    		currentElectricityPriceCharge_eurpkWh = parentGC.energyModel.tf_dayAheadElectricityPricing_eurpMWh.get(i) / 1000;
    		costsElectricityExport_euro += currentElectricityPriceCharge_eurpkWh * max(0,-parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i]) * parentGC.energyModel.p_timeStep_h;
    		//traceln("%f", max(0,-parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i]));
    	}
    	
    	return costsElectricityExport_euro;
    	//traceln("%f",costsElectricityExport_euro);
    	
    }
    
    public double calculateElectricityNetCosts_euro() { 

    	double costsElectricityNet_euro = 0;

    	costsElectricityNet_euro = calculateElectricityImportCosts_euro() - calculateElectricityExportCosts_euro();
    	
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
    
    /*public double calculateAvoidedCapacityCosts(double previousGridTariff_eurokW, double currentGridTariff_eurokW){
    	
    	double AvoidedCapacityCosts_euro = 0;
    	
    	double maxDelivery_kW = max(0, parentGC.energyModel.loadDurationCurves.ds_loadDurationCurveTotal_kW.getYMax());
    	double maxFeedin_kW = abs(min(0, parentGC.energyModel.loadDurationCurves.ds_loadDurationCurveTotal_kW.getYMin()));
    	double maxDeliveryAndFeedin_kW = max(maxDelivery_kW, maxFeedin_kW);
    	
    	AvoidedCapacityCosts_euro = maxDeliveryAndFeedin_kW*(previousGridTariff_eurokW - currentGridTariff_eurokW);
    	
    	return AvoidedCapacityCosts_euro;
    	
    }*/
    
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
    
    
    
  //parentGC.energyModel.v_rapidRunData.ts_dailyAverageBatteriesSOC_fr.getDataSet(startTime_h);
	//dataObject.getRapidRunData().ts_dailyAverageBatteriesSOC_fr.getDataSet(startTime_h);
    
    /*public void exportToExcel() {
    	
    	try {
    		
    		XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Simulation Results");
    	
            double startTime_h = 0;
    	
            for (int i = 0; i < parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length; i++) {
    		
            	XSSFRow row = sheet.createRow(i + 1);
            	for (int j = 0; j < parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW().length; j++) {
            		row.createCell(j).setCellValue(parentGC.energyModel.v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()[i][j]);
            	}
            }
    	
            workbook.write(out);
            out.close();
            workbook.close();

            trace("Excel export successful!");
    	
    	} catch (Exception e) {
    	    // What to do if something goes wrong
    	    e.printStackTrace();
    	    trace("Failed to export to Excel: " + e.getMessage());
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