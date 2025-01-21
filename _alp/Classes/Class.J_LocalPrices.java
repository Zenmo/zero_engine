/**
 * J_LocalPrices
 */	
public class J_LocalPrices implements Serializable {

	private J_ProfilePointer pp_dayAheadElectricityPricing_eurpMWh;
	private double congestionFactor_eurpMWhpkW;
	private double selfConsumptionSaving_eurpMWh;
	private double congestionDeadzone_kW;
	private double timeStep_h;
	private double[] dailyPriceCurve_eurpMWh;
	private int timeStepsPerDay_n;
	
    /**
     * Default constructor
     */
    public J_LocalPrices() {
    }

    public J_LocalPrices(J_ProfilePointer pp_dayAheadElectricityPricing_eurpMWh , double congestionFactor_eurpMWhpkW, double selfConsumptionSaving_eurpMWh, double congestionDeadzone_kW, double timeStep_h) {
    	this.pp_dayAheadElectricityPricing_eurpMWh = pp_dayAheadElectricityPricing_eurpMWh;
    	this.congestionFactor_eurpMWhpkW = congestionFactor_eurpMWhpkW;
    	this.selfConsumptionSaving_eurpMWh = selfConsumptionSaving_eurpMWh;
    	this.congestionDeadzone_kW = congestionDeadzone_kW;
    	this.timeStep_h = timeStep_h; 
    	this.timeStepsPerDay_n = roundToInt(24/timeStep_h);
    }

    public void updateDailyPriceCurve( double startTime_h, double timeWindow_h ) {
    	this.dailyPriceCurve_eurpMWh = new double[roundToInt(timeWindow_h / this.timeStep_h)];
    	for (int i = 0; i * this.timeStep_h < timeWindow_h; i++) {
    		this.dailyPriceCurve_eurpMWh[i] = this.pp_dayAheadElectricityPricing_eurpMWh.getValue( startTime_h + i * this.timeStep_h);
    	}
    }

    public double[] getMarginalPriceCurveUpwards(double[] loadProfile_kW, int day) {
        double[] marginalPriceCurveUpwards = new double[loadProfile_kW.length];

        for (int i = 0; i < loadProfile_kW.length; i++) {
            double load_kW = loadProfile_kW[i];
            double loadProfileSign = (load_kW >= this.congestionDeadzone_kW ? 1 : 0) - ((-load_kW) > this.congestionDeadzone_kW ? 1 : 0);
            double marginalPrice = this.dailyPriceCurve_eurpMWh[i+day*timeStepsPerDay_n]
                                   + loadProfileSign * (( 2*Math.abs(load_kW) - this.congestionDeadzone_kW) * this.congestionFactor_eurpMWhpkW )
                                   + (this.selfConsumptionSaving_eurpMWh * (load_kW >= 0 ? 1 : 0));

            marginalPriceCurveUpwards[i] = marginalPrice;
        }

        return marginalPriceCurveUpwards;
    }
    
    public double[] getMarginalPriceCurveUpwards(double[] loadProfile_kW) {
    	return getMarginalPriceCurveUpwards(loadProfile_kW, 0);
    }
    
    public double[] getMarginalPriceCurveDownwards(double[] loadProfile_kW, int day) {
        double[] marginalPriceCurveDownwards = new double[loadProfile_kW.length];

        for (int i = 0; i < loadProfile_kW.length; i++) {
            double load_kW = loadProfile_kW[i];
            double loadProfileSign = (load_kW > this.congestionDeadzone_kW ? 1 : 0) - ((-load_kW) >= this.congestionDeadzone_kW ? 1 : 0);
            double marginalPrice = this.dailyPriceCurve_eurpMWh[i+day*timeStepsPerDay_n]
                                   + loadProfileSign * (( 2*Math.abs(load_kW) - this.congestionDeadzone_kW) * this.congestionFactor_eurpMWhpkW )
                                   + (this.selfConsumptionSaving_eurpMWh * (load_kW > 0 ? 1 : 0));

            marginalPriceCurveDownwards[i] = marginalPrice;
        }

        return marginalPriceCurveDownwards;
    }
    
    public double[] getMarginalPriceCurveDownwards(double[] loadProfile_kW) {
    	return getMarginalPriceCurveDownwards(loadProfile_kW, 0);
    }
    
    public double[] getActualPriceCurve(double[] loadProfile_kW, int day) {
        double[] actualPriceCurve = new double[loadProfile_kW.length];

        for (int i = 0; i < loadProfile_kW.length; i++) {
            double load_kW = loadProfile_kW[i];
            double loadProfileSign = (load_kW >= this.congestionDeadzone_kW ? 1 : 0) - ((-load_kW) > this.congestionDeadzone_kW ? 1 : 0);
            double actualPrice = this.dailyPriceCurve_eurpMWh[i+day*timeStepsPerDay_n]
                                   + loadProfileSign * (( Math.abs(load_kW) - this.congestionDeadzone_kW) * this.congestionFactor_eurpMWhpkW )
                                   + (this.selfConsumptionSaving_eurpMWh * (load_kW >= 0 ? 1 : 0));

            actualPriceCurve[i] = actualPrice;
        }

        return actualPriceCurve;
    }
    
    public double[] getActualPriceCurve(double[] loadProfile_kW) {
    	return getActualPriceCurve(loadProfile_kW, 0);
    }
    
    /*public DataSet getDayAheadPrice_eurpMWh(double startTime_h, double timeWindow_h) {
    	DataSet data_dayAhead_eurpMWh = new DataSet(roundToInt(timeWindow_h/timeStep_h));
    	for (int i=0; i<roundToInt(timeWindow_h/timeStep_h); i++) {
    		data_dayAhead_eurpMWh.add(startTime_h + i*timeStep_h, this.pp_dayAheadElectricityPricing_eurpMWh.getValue( startTime_h + i * this.timeStep_h));
    	}
    	return data_dayAhead_eurpMWh;
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