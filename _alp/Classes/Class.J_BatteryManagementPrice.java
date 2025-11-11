/**
 * J_BatteryManagementPrice
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

public class J_BatteryManagementPrice implements I_BatteryManagement {

    private GridConnection gc;
    // Parameters used:
    private boolean stayWithinConnectionLimits = true; // When this flag is true the battery stays within the contracted capacity of the GC
    private double chargeDischarge_offset_eurpkWh = 0.0; // This term determines the minimal price difference before the battery is used
    private double WTPfeedbackGain_eurpSOC = 0.5; // This term determines the unwillingness to use the battery when it is almost full or empty
    private double priceGain_kWhpeur = 2.0; // This term determines how strongly to ramp up power with price-delta's
    private double lowPassFactor_fr = 0.001;
    
    // Internal State
    private double electricityPriceLowPassed_eurpkWh;
    private double storedElectricityPriceLowPassed_eurpkWh;
    
    /**
     * Default constructor
     */
    
    public J_BatteryManagementPrice() {
		
	}
    
    public J_BatteryManagementPrice( GridConnection gc ) {
    	this.gc = gc;
    	this.gc.energyModel.f_registerAssetManagement(this);
    }
    
    public J_BatteryManagementPrice( GridConnection gc, boolean stayWithinConnectionLimits, double chargeDischarge_offset_eurpkWh, double WTPfeedbackGain_eurpSOC, double priceGain_kWhpeur, double priceTimescale_h ) {
    	this.gc = gc;
    	this.stayWithinConnectionLimits = stayWithinConnectionLimits;
    	this.chargeDischarge_offset_eurpkWh = chargeDischarge_offset_eurpkWh;
    	this.WTPfeedbackGain_eurpSOC = WTPfeedbackGain_eurpSOC;
    	this.priceGain_kWhpeur = priceGain_kWhpeur;
        this.lowPassFactor_fr = gc.energyModel.p_timeStep_h / priceTimescale_h;
    	this.gc.energyModel.f_registerAssetManagement(this);
    }
    
    /**
     * This algorithm determines the battery behaviour with the historical national EPEX price. 
     * It has a boolean flag wether or not to take the GC's connection capacity into account.
     */
    public void manageBattery() {
	    // Get the national EPEX price
	    double currentElectricityPriceCharge_eurpkWh = gc.energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000;
	
	    // Base the WTP on a moving average price and the SOC
	    electricityPriceLowPassed_eurpkWh += lowPassFactor_fr * ( currentElectricityPriceCharge_eurpkWh - electricityPriceLowPassed_eurpkWh );
	    
	    double SOC_setpoint_fr = 0.5;
	    double SOC_deficit_fr = SOC_setpoint_fr - gc.p_batteryAsset.getCurrentStateOfCharge_fr(); // How far away from desired SOC? SOC too LOW is a POSITIVE deficit
	
	    // Define WTP price for charging and discharging!
	    double WTP_charge_eurpkWh = electricityPriceLowPassed_eurpkWh - chargeDischarge_offset_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
	    double WTP_discharge_eurpkWh = electricityPriceLowPassed_eurpkWh + chargeDischarge_offset_eurpkWh + SOC_deficit_fr * WTPfeedbackGain_eurpSOC;
	
	    // Choose charging power based on prices and desired SOC level
	    double chargeSetpoint_kW = 0;
	    if ( WTP_charge_eurpkWh > currentElectricityPriceCharge_eurpkWh ) {
	    	chargeSetpoint_kW = gc.p_batteryAsset.getCapacityElectric_kW()*(WTP_charge_eurpkWh - currentElectricityPriceCharge_eurpkWh)*priceGain_kWhpeur;
	    }
	    else if (WTP_discharge_eurpkWh < currentElectricityPriceCharge_eurpkWh) {
	    	chargeSetpoint_kW = -gc.p_batteryAsset.getCapacityElectric_kW()*(currentElectricityPriceCharge_eurpkWh - WTP_discharge_eurpkWh)*priceGain_kWhpeur;
	    }
	
	    // limit charging power to available connection capacity
	    if( stayWithinConnectionLimits ) {
	    	double availableChargePower_kW = gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW - gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // Max battery charging power within grid capacity
	    	double availableDischargePower_kW = gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW + gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); // Max discharging power within grid capacity
	    	chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	    }
	
	    gc.p_batteryAsset.f_updateAllFlows( chargeSetpoint_kW / gc.p_batteryAsset.getCapacityElectric_kW() );
    }
    
    
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    
    //Store and reset states
	public void storeStatesAndReset() {
		this.storedElectricityPriceLowPassed_eurpkWh = electricityPriceLowPassed_eurpkWh;
		this.electricityPriceLowPassed_eurpkWh = 0;
	}
	public void restoreStates() {
		this.electricityPriceLowPassed_eurpkWh = this.storedElectricityPriceLowPassed_eurpkWh;
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