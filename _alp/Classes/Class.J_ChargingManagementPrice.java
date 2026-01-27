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
    
    //Stored
    private double storedElectricityPriceLowPassed_eurpkWh;
    
    /**
     * Default constructor
     */
    public J_ChargingManagementPrice( GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.priceFilterDiffGain_r = 1/(priceFilterTimeScale_h/timeParameters.getTimeStep_h());
    }
        
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging(J_ChargePoint chargePoint, J_TimeVariables timeVariables) {
    	double t_h = timeVariables.getT_h();

    	//double currentElectricityPriceConsumption_eurpkWh = gc.p_owner.f_getElectricityPrice(gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
    	double currentElectricityPriceConsumption_eurpkWh = gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() * 0.001;
    	electricityPriceLowPassed_eurpkWh += (currentElectricityPriceConsumption_eurpkWh-electricityPriceLowPassed_eurpkWh) * priceFilterDiffGain_r ;
       	for (I_ChargingRequest chargingRequest : chargePoint.getCurrentActiveChargingRequests()) {
			double chargeNeedForNextTrip_kWh = chargingRequest.getEnergyNeedForNextTrip_kWh() - chargingRequest.getCurrentSOC_kWh(); // Can be negative if recharging is not needed for next trip!
			double remainingFlexTime_h = chargePoint.getChargeDeadline_h(chargingRequest) - t_h; // measure of flexiblity left in current charging session.
			double WTPoffset_eurpkW = 0.01; // Drops willingness to pay price for charging, combined with remainingFlexTime_h.
			double chargeSetpoint_kW = 0;    			
			if ( t_h >= chargePoint.getChargeDeadline_h(chargingRequest) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
				chargeSetpoint_kW = chargePoint.getMaxChargingCapacity_kW(chargingRequest);	
			} else {
				double WTPCharging_eurpkWh = electricityPriceLowPassed_eurpkWh - WTPoffset_eurpkW * remainingFlexTime_h;  //+ urgencyGain_eurpkWh * ( max(0,maxSpreadChargingPower_kW) / ev.getCapacityElectric_kW() ); // Scale WTP based on flexibility expressed in terms of power-fraction
				//WTPprice_eurpkWh = WTPoffset_eurpkWh + (main.v_epexNext24hours_eurpkWh+v_electricityPriceLowPassed_eurpkWh)/2 + flexibilityGain_eurpkWh * sqrt(maxSpreadChargingPower_kW/maxChargingPower_kW); 
				double priceGain_r = 0.5; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
				chargeSetpoint_kW = max(0, chargePoint.getMaxChargingCapacity_kW(chargingRequest) * (WTPCharging_eurpkWh / currentElectricityPriceConsumption_eurpkWh - 1) * priceGain_r);			
				//if ( chargeNeedForNextTrip_kWh < -ev.getCapacityElectric_kW()*gc.energyModel.p_timeStep_h && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
    			if ( this.V2GActive && chargePoint.getV2GCapable() && chargingRequest.getV2GCapable() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
	    			double V2G_WTS_offset_eurpkWh = 0.02; // Price must be at least this amount above the moving average to decide to discharge EV battery.
					double WTSV2G_eurpkWh = V2G_WTS_offset_eurpkWh + electricityPriceLowPassed_eurpkWh; // Scale WillingnessToSell based on flexibility expressed in terms of power-fraction
					chargeSetpoint_kW = min(0, -chargePoint.getMaxChargingCapacity_kW(chargingRequest) * (currentElectricityPriceConsumption_eurpkWh / WTSV2G_eurpkWh - 1) * priceGain_r);
				}    
			}
	    	//Send the chargepower setpoints to the chargepoint
	       	chargePoint.charge(chargingRequest, chargeSetpoint_kW, timeVariables);					
    	}
    }

	public void setV2GActive(boolean activateV2G) {
		this.V2GActive = activateV2G;
		this.gc.c_electricVehicles.forEach(ev -> ev.setV2GActive(activateV2G)); // NEEDED TO HAVE EV ASSET IN CORRECT assetFlowCatagory
		this.gc.c_chargingSessions.forEach(cs -> cs.setV2GActive(activateV2G)); // NEEDED TO HAVE CS ASSET IN CORRECT assetFlowCatagory
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	
	
	
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    //Store and reset states
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