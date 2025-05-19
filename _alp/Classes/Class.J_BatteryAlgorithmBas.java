/**
 * J_BatteryAlgorithmBas
 */	
public class J_BatteryAlgorithmBas implements Serializable {

	private GridConnection parentGC;
    /**
     * Default constructor
     */
    public J_BatteryAlgorithmBas(GridConnection parentGC) {
    	
    	this.parentGC = parentGC;
    	
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
    		
    	GridNode GN = parentGC.l_parentNodeElectric.getConnectedAgent(); // Get parent from GCGridBattery = GridNode GN
    	currentElectricityPriceCharge_eurpkWh = parentGC.energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000; // Get current electricity price from coupled GridNode
    		
    	parentGC.v_electricityPriceLowPassed_eurpkWh += parentGC.v_lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - parentGC.v_electricityPriceLowPassed_eurpkWh );
    	// EMA_new_price = EMA_old_price + smoothing_factor * (current_Price - EMA_old_price) = smoothing_factor * current_Price + EMA_old_price*(1-smoothing_factor)
    	// Exponential Moving Average = alpha * current_Price + EMA_old_price*(1-alpha)
    		
    	double currentCoopElectricitySurplus_kW = -GN.v_currentLoad_kW + parentGC.v_previousPowerElectricity_kW; // additional power the battery can inject or withdraw without exceeding the node’s connection capacity?
    	double CoopConnectionCapacity_kW = 0.95*GN.p_capacity_kW; // Only allow 90-95% of nominal grid capacity
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