/**
 * J_GridCapacitySharingManager
 */	
public class J_GridCapacitySharingManager {
	
	GridConnection receivingGC;
	GridConnection sendingGC;
	
	//Arrays
	double[] capacitySharingWeekdayDeliveryCapacity_kW;
	double[] capacitySharingWeekenddayDeliveryCapacity_kW;
	double[] capacitySharingWeekdayFeedinCapacity_kW;
	double[] capacitySharingWeekenddayFeedinCapacity_kW;
	
    /**
     * Default constructor
     */
    public J_GridCapacitySharingManager(GridConnection receivingGC, GridConnection sendingGC, double[] capacitySharingWeekdayDeliveryCapacity_kW, 
    		double[] capacitySharingWeekenddayDeliveryCapacity_kW, double[] capacitySharingWeekdayFeedinCapacity_kW, double[] capacitySharingWeekenddayFeedinCapacity_kW) {
    	
    	this.receivingGC = receivingGC;
    	this.sendingGC = sendingGC;
    	
    	this.capacitySharingWeekdayDeliveryCapacity_kW = capacitySharingWeekdayDeliveryCapacity_kW;
    	this.capacitySharingWeekenddayDeliveryCapacity_kW = capacitySharingWeekenddayDeliveryCapacity_kW;
    	this.capacitySharingWeekdayFeedinCapacity_kW = capacitySharingWeekdayFeedinCapacity_kW;
    	this.capacitySharingWeekenddayFeedinCapacity_kW = capacitySharingWeekenddayFeedinCapacity_kW;
    }
    
    public double getCurrentSharedDeliveryCapacity_kW(GridConnection GC, J_TimeVariables timeVariables) {
    	int hourOfDay = roundToInt(floor(timeVariables.getTimeOfDay_h()));
	    int receiveOrSendDirectionFactor = GC == receivingGC ? 1 : -1;
    	if (timeVariables.getDayOfWeek() == OL_Days.SATURDAY || timeVariables.getDayOfWeek() == OL_Days.SUNDAY) {
    		return receiveOrSendDirectionFactor * capacitySharingWeekenddayDeliveryCapacity_kW[hourOfDay];
    	}
    	else {
    		return receiveOrSendDirectionFactor * capacitySharingWeekdayDeliveryCapacity_kW[hourOfDay];
    	}
	}
    
    public double getCurrentSharedFeedinCapacity_kW(GridConnection GC, J_TimeVariables timeVariables) {
	    int hourOfDay = roundToInt(floor(timeVariables.getTimeOfDay_h()));
	    int receiveOrSendDirectionFactor = GC == receivingGC ? 1 : -1;
    	if (timeVariables.getDayOfWeek() == OL_Days.SATURDAY || timeVariables.getDayOfWeek() == OL_Days.SUNDAY) {
			return receiveOrSendDirectionFactor * capacitySharingWeekenddayFeedinCapacity_kW[hourOfDay];
    	}
    	else {
    		return receiveOrSendDirectionFactor * capacitySharingWeekdayFeedinCapacity_kW[hourOfDay];
    	}
	}
    
	@Override
	public String toString() {
		return super.toString();
	}

}