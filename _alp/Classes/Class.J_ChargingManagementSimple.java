/**
 * J_ChargingManagementSimple
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
public class J_ChargingManagementSimple implements I_ChargingManagement {

    private GridConnection gc;
    private OL_ChargingAttitude activeChargingType;
    private double electricityPriceLowPassed_eurpkWh = 0.1;
    private double priceFilterTimeScale_h = 5*24;
    private double priceFilterDiffGain_r;
    private double GCdemandLowPassed_kW = 0.5;
    //private double GCdemandFilterTimeScale_h = 5*24;
    /**
     * Default constructor
     */
    public J_ChargingManagementSimple() {
    	
    }
    
    public J_ChargingManagementSimple( GridConnection gc ) {
    	this.gc = gc;
    	this.priceFilterDiffGain_r = 1/(priceFilterTimeScale_h/gc.energyModel.p_timeStep_h);
    }
    
    public void initialize() {
    	
    }
    
    public OL_ChargingAttitude getCurrentChargingType() {
    	return activeChargingType;
    }
    /**
     * One of the simplest charging algorithms.
     * 
     */
    public void manageCharging(double t_h) {
    	//double currentElectricityPriceConsumption_eurpkWh = gc.p_owner.f_getElectricityPrice(gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
    	double currentElectricityPriceConsumption_eurpkWh = gc.energyModel.pp_dayAheadElectricityPricing_eurpMWh.getCurrentValue() * 0.001;

    	electricityPriceLowPassed_eurpkWh += (currentElectricityPriceConsumption_eurpkWh-electricityPriceLowPassed_eurpkWh) * priceFilterDiffGain_r ;
    	GCdemandLowPassed_kW += (gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - GCdemandLowPassed_kW) * priceFilterDiffGain_r;
    	electricityPriceLowPassed_eurpkWh = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	currentElectricityPriceConsumption_eurpkWh = gc.v_previousPowerElectricity_kW;
    	//traceln("Current price: %s eurpkWh, filtered price: %s eurpkWh", currentElectricityPriceConsumption_eurpkWh, electricityPriceLowPassed_eurpkWh);
    	for (J_EAEV ev : gc.c_electricVehicles) {
    		if (gc.p_chargingAttitudeVehicles != OL_ChargingAttitude.SIMPLE) {
    			//double remainingChargePower_kW = gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    			double chargeNeedForNextTrip_kWh = ev.energyNeedForNextTrip_kWh - ev.getCurrentStateOfCharge_kWh(); // Can be negative if recharging is not needed for next trip!
    			double nextTripStartTime_h = ev.tripTracker.v_nextEventStartTime_min / 60;
    			double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
    			double chargeDeadline_h =  nextTripStartTime_h - chargeNeedForNextTrip_kWh / ev.getCapacityElectric_kW() - chargeTimeMargin_h;
    			double remainingFlexTime_h = chargeDeadline_h - t_h; // measure of flexiblity left in current charging session.
    			double WTPoffset_eurpkW = 0.01; // Drops willingness to pay price for charging, combined with remainingFlexTime_h.
    			double chargeSetpoint_kW = 0;    			
    			if ( t_h >= (chargeDeadline_h) && chargeNeedForNextTrip_kWh > 0) { // Must-charge time at max charging power
    				//traceln("Urgency charging in GC: %s! May exceed connection capacity!", gc.p_gridConnectionID));
    				chargeSetpoint_kW = ev.getCapacityElectric_kW();	
    			} else {
    				double WTPCharging_eurpkWh = electricityPriceLowPassed_eurpkWh - WTPoffset_eurpkW * remainingFlexTime_h;  //+ urgencyGain_eurpkWh * ( max(0,maxSpreadChargingPower_kW) / ev.getCapacityElectric_kW() ); // Scale WTP based on flexibility expressed in terms of power-fraction
    				//WTPprice_eurpkWh = WTPoffset_eurpkWh + (main.v_epexNext24hours_eurpkWh+v_electricityPriceLowPassed_eurpkWh)/2 + flexibilityGain_eurpkWh * sqrt(maxSpreadChargingPower_kW/maxChargingPower_kW); 
    				double priceGain_r = 0.5; // When WTP is higher than current electricity price, ramp up charging power with this gain based on the price-delta.
    				chargeSetpoint_kW = max(0, ev.getCapacityElectric_kW() * (WTPCharging_eurpkWh / currentElectricityPriceConsumption_eurpkWh - 1) * priceGain_r);
    				//chargeSetpoint_kW = min(remainingChargePower_kW, chargeSetpoint_kW);    				
    				//if ( chargeNeedForNextTrip_kWh < -ev.getCapacityElectric_kW()*gc.energyModel.p_timeStep_h && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
        			if ( ev.getV2GActive() && remainingFlexTime_h > 1 && chargeSetpoint_kW == 0 ) { // Surpluss SOC and high energy price
    	    			double V2G_WTS_offset_eurpkWh = 0.02; // Price must be at least this amount above the moving average to decide to discharge EV battery.
    					double WTSV2G_eurpkWh = V2G_WTS_offset_eurpkWh + electricityPriceLowPassed_eurpkWh; // Scale WillingnessToSell based on flexibility expressed in terms of power-fraction
    					chargeSetpoint_kW = min(0, -ev.getCapacityElectric_kW() * (currentElectricityPriceConsumption_eurpkWh / WTSV2G_eurpkWh - 1) * priceGain_r);
    					//if (chargeSetpoint_kW < 0) {traceln(" V2G Active! Power: " + chargeSetpoint_kW );}
    				}    
    			}
    			ev.f_updateAllFlows( chargeSetpoint_kW / ev.getCapacityElectric_kW() );    			
    		} else { // just charge 'dumb', full power until full
    			ev.f_updateAllFlows(1.0);
    		}
    	}
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