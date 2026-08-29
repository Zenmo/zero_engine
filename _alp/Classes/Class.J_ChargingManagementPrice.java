/**
 * J_ChargingManagementPrice
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
public class J_ChargingManagementPrice implements I_ChargingManagement {

    private GridConnection gc;
    private J_TimeParameters timeParameters;
    private OL_ChargingAttitude activeChargingType = OL_ChargingAttitude.PRICE;
    private double initialValueElectricityPriceLowPassed_eurpkWh = 0.1;
    private double electricityPriceLowPassed_eurpkWh = this.initialValueElectricityPriceLowPassed_eurpkWh;
    private double priceFilterTimeScale_h = 5*24;
    private double priceFilterDiffGain_r;

    private boolean V2GActive = false;
    private double storedElectricityPriceLowPassed_eurpkWh;
    
    /**
     * Default constructor
     */
    public J_ChargingManagementPrice( ) {
    }
    
    public J_ChargingManagementPrice( GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.priceFilterDiffGain_r = 1/(priceFilterTimeScale_h/timeParameters.getTimeStep_h());
    }
        
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }

    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	double t_h = timeVariables.getT_h();
    	double currentElectricityPriceConsumption_eurpkWh = gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() * 0.001;
    	electricityPriceLowPassed_eurpkWh += (currentElectricityPriceConsumption_eurpkWh-electricityPriceLowPassed_eurpkWh) * priceFilterDiffGain_r ;
       	for (I_ChargingRequest chargingRequest : chargePoint.getCurrentActiveChargingRequests()) {
       		double timeToCharge_h = chargingRequest.getLeaveTime_h() - t_h;
    		if (timeToCharge_h <= 0) {
   				traceln("ChargingRequest starting after endtime! Skipping session! Duration_h: %s", timeToCharge_h);
   			}
			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
			double remainingFlexTime_h = chargePoint.getChargeDeadline_h(chargingRequest) - t_h; // measure of flexiblity left in current charging session.
			double WTPoffset_eurpkW = 0.01; // Drops willingness to pay price for charging, combined with remainingFlexTime_h.
			double chargeSetpoint_kW = 0;    			
			if ( remainingFlexTime_h <= 0 && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				chargeSetpoint_kW = chargePoint.getMaxChargingCapacity_kW(chargingRequest);	
			} else {
				double WTPCharging_eurpkWh = electricityPriceLowPassed_eurpkWh - WTPoffset_eurpkW * remainingFlexTime_h;  //+ urgencyGain_eurpkWh * ( max(0,maxSpreadChargingPower_kW) / ev.getCapacityElectric_kW() ); // Scale WTP based on flexibility expressed in terms of power-fraction
				double priceGain_r = 0.5; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
				chargeSetpoint_kW = max(0, chargePoint.getMaxChargingCapacity_kW(chargingRequest) * (WTPCharging_eurpkWh / currentElectricityPriceConsumption_eurpkWh - 1) * priceGain_r);			
    			if ( this.V2GActive && chargePoint.getV2GCapable() && chargingRequest.getV2GCapable() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
	    			double V2G_WTS_offset_eurpkWh = 0.02; // Price must be at least this amount above the moving average to decide to discharge EV battery.
					double WTSV2G_eurpkWh = V2G_WTS_offset_eurpkWh + electricityPriceLowPassed_eurpkWh; // Scale WillingnessToSell based on flexibility expressed in terms of power-fraction
					chargeSetpoint_kW = min(0, -chargePoint.getMaxChargingCapacity_kW(chargingRequest) * (currentElectricityPriceConsumption_eurpkWh / WTSV2G_eurpkWh - 1) * priceGain_r);
				}    
			}
	    	//Send the chargepower setpoints to the chargepoint
	       	chargePoint.charge(chargingRequest, chargeSetpoint_kW, timeVariables, gc);					
    	}
    }

	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		OL_ForecastStatus status = OL_ForecastStatus.NOT_FORECASTABLE;
		String reason = "Not yet implemented.";
		return new J_AssetTypeForecast(I_ChargingManagement.class, loadMap, status, reason);
	}
	
	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
    public Agent getParentAgent() {
    	return this.gc;
    }
    
	public void storeStatesAndReset() {
		this.storedElectricityPriceLowPassed_eurpkWh = this.electricityPriceLowPassed_eurpkWh;
		this.electricityPriceLowPassed_eurpkWh = this.initialValueElectricityPriceLowPassed_eurpkWh;
	}
	public void restoreStates() {
		this.electricityPriceLowPassed_eurpkWh = this.storedElectricityPriceLowPassed_eurpkWh;
	}
	
    @Override
 	public String toString() {
 		return "Active charging type: " + this.activeChargingType;

 	}
}