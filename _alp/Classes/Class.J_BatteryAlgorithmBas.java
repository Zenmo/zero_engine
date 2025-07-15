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

    
    public double calculateChargeSetpoint_kW(double SOC){
    	
    	double batteryChargeSetpoint_kW = 0;
    	
    	//double currentElectricityPriceEPEX_eurpkWh = energyModel.v_epexForecast_eurpkWh;
    	
    	if (parentGC.p_batteryAsset.getStorageCapacity_kWh() != 0){	
    		double currentCoopElectricitySurplus_kW = 0;
    		double CoopConnectionCapacity_kW = 0;
    		double v_previousPowerBattery_kW = parentGC.v_previousPowerElectricity_kW;// Assumes battery is only asset on gridconnection!! p_batteryAsset.electricityConsumption_kW-p_batteryAsset.electricityProduction_kW;
    		if(parentGC.p_owner != null) {
    				currentCoopElectricitySurplus_kW = -parentGC.p_parentNodeElectric.v_currentLoad_kW + v_previousPowerBattery_kW;			
    				CoopConnectionCapacity_kW = 0.9*parentGC.p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
    			//}
    		} else { // Get gridload directly from node
    			currentCoopElectricitySurplus_kW = -parentGC.p_parentNodeElectric.v_currentLoad_kW + v_previousPowerBattery_kW;			
    			CoopConnectionCapacity_kW = 0.95*parentGC.p_parentNodeElectric.p_capacity_kW; // Use only 90% of capacity for robustness against delay
    		}

    		double availableChargePower_kW = CoopConnectionCapacity_kW + currentCoopElectricitySurplus_kW; // Max battery charging power within grid capacity
    		double availableDischargePower_kW = currentCoopElectricitySurplus_kW - CoopConnectionCapacity_kW; // Max discharging power within grid capacity
    		double FeedbackGain_kWpSOC = 3 * parentGC.p_batteryAsset.getCapacityElectric_kW(); // How strongly to aim for SOC setpoint
    		double FeedforwardGain_kWpKw = 0.1; // Feedforward based on current surpluss in Coop
    		double chargeOffset_kW = 0; // Charging 'bias', basically increases SOC setpoint slightly during the whole day.
    		double chargeSetpoint_kW = 0;
    		//traceln("Test");
    		if (availableChargePower_kW < 0) { // prevent congestion
    			batteryChargeSetpoint_kW = availableChargePower_kW;
    			return batteryChargeSetpoint_kW;
    		}
    		if (parentGC.energyModel.v_currentSolarPowerNormalized_r > 0.1) {
    			if (parentGC.p_parentNodeElectric.v_currentLoad_kW < 0) {
    				batteryChargeSetpoint_kW = availableChargePower_kW;
        			return batteryChargeSetpoint_kW;
    			}
    		}
    		else {
    			double expectedWind_kWh = parentGC.p_parentNodeElectric.v_totalInstalledWindPower_kW * parentGC.energyModel.v_WindYieldForecast_fr * parentGC.energyModel.p_forecastTime_h;
    			double expectedSolar_kWh = parentGC.p_parentNodeElectric.v_totalInstalledPVPower_kW * parentGC.energyModel.v_SolarYieldForecast_fr * parentGC.energyModel.p_forecastTime_h;
    			double incomingPower_fr = (expectedSolar_kWh + expectedWind_kWh) / parentGC.p_batteryAsset.getStorageCapacity_kWh();
    			double SOC_setp_fr = 1 - incomingPower_fr;
    		
    			chargeSetpoint_kW = FeedbackGain_kWpSOC*(SOC_setp_fr - parentGC.p_batteryAsset.getCurrentStateOfCharge_fr());
    			chargeSetpoint_kW = min(max(chargeSetpoint_kW, availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
    			batteryChargeSetpoint_kW = availableChargePower_kW;
    			return batteryChargeSetpoint_kW;
    		}
    	}
    	return batteryChargeSetpoint_kW;
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