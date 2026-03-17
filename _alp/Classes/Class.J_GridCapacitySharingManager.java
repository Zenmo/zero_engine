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
	
	//Variables
	double currentSharedDeliveryCapacity_kW = 0;
	double currentSharedFeedinCapacity_kW = 0;
	
    /**
     * Default constructor
     */
    public J_GridCapacitySharingManager(GridConnection receivingGC, GridConnection sendingGC, double[] capacitySharingWeekdayDeliveryCapacity_kW, 
    		double[] capacitySharingWeekenddayDeliveryCapacity_kW, double[] capacitySharingWeekdayFeedinCapacity_kW, 
    		double[] capacitySharingWeekenddayFeedinCapacity_kW, J_TimeVariables timeVariables) {
    	
    	if(receivingGC == null || sendingGC == null) {
    		throw new RuntimeException("It is not possible to create a capacity sharing contract without specifying both the recieving and sending GC");
    	}
    	this.receivingGC = receivingGC;
    	this.sendingGC = sendingGC;
    	
    	receivingGC.v_liveConnectionMetaData.addSharedCapacityManager(this);
    	sendingGC.v_liveConnectionMetaData.addSharedCapacityManager(this);
    	
    	this.capacitySharingWeekdayDeliveryCapacity_kW = capacitySharingWeekdayDeliveryCapacity_kW;
    	this.capacitySharingWeekenddayDeliveryCapacity_kW = capacitySharingWeekenddayDeliveryCapacity_kW;
    	this.capacitySharingWeekdayFeedinCapacity_kW = capacitySharingWeekdayFeedinCapacity_kW;
    	this.capacitySharingWeekenddayFeedinCapacity_kW = capacitySharingWeekenddayFeedinCapacity_kW;
    	
    	this.update(sendingGC, timeVariables);
    }
    
    public void update(GridConnection callerGC, J_TimeVariables timeVariables) {
    	if(callerGC == sendingGC) {
		    int hourOfDay = roundToInt(floor(timeVariables.getTimeOfDay_h()));
	    	if (timeVariables.getDayOfWeek() == OL_Days.SATURDAY || timeVariables.getDayOfWeek() == OL_Days.SUNDAY) {
	    		this.currentSharedDeliveryCapacity_kW = capacitySharingWeekenddayDeliveryCapacity_kW[hourOfDay];
	    		this.currentSharedFeedinCapacity_kW = capacitySharingWeekenddayFeedinCapacity_kW[hourOfDay];
	    	}
	    	else {
	    		this.currentSharedDeliveryCapacity_kW = capacitySharingWeekdayDeliveryCapacity_kW[hourOfDay];
	    		this.currentSharedFeedinCapacity_kW = capacitySharingWeekdayFeedinCapacity_kW[hourOfDay];
	    	}
    	}
    }
    
    
    public double getCurrentSharedDeliveryCapacity_kW(GridConnection requestingGC) {
    	int receiveOrSendDirectionFactor = requestingGC == receivingGC ? 1 : -1;
    	return receiveOrSendDirectionFactor * this.currentSharedDeliveryCapacity_kW;
    }

    public double getCurrentSharedFeedinCapacity_kW(GridConnection requestingGC) {
    	int receiveOrSendDirectionFactor = requestingGC == receivingGC ? 1 : -1;
    	return receiveOrSendDirectionFactor * this.currentSharedFeedinCapacity_kW;
    }
    
    public double getSharedDeliveryCapacityAtHourOfWeekDay_kW(int hourOfDay) {
    	return capacitySharingWeekdayDeliveryCapacity_kW[hourOfDay];
    }
    public double getSharedDeliveryCapacityAtHourOfWeekendDay_kW(int hourOfDay) {
    	return capacitySharingWeekenddayDeliveryCapacity_kW[hourOfDay];
    }
    public double getSharedFeedinCapacityAtHourOfWeekDay_kW(int hourOfDay) {
    	return capacitySharingWeekdayFeedinCapacity_kW[hourOfDay];
    }
    public double getSharedFeedinCapacityAtHourOfWeekendDay_kW(int hourOfDay) {
    	return capacitySharingWeekenddayFeedinCapacity_kW[hourOfDay];
    }
    
	@Override
	public String toString() {
		return super.toString();
	}

}