/**
 * J_Market
 */	

public class J_Market {
    private double[] dailyPriceCurve_eurpMWh;
    private double marketFeedback_eurpMWhpkW;
    private double selfConsumptionSaving_eurpMWh;
    private double congestionDeadzone_kW;
    private double congestionFactor_eurpMWhpkW;

    public J_Market(
        double[] dailyPriceCurve_eurpMWh,
        double marketFeedback_eurpMWhpkW,
        double selfConsumptionSaving_eurpMWh,
        double congestionDeadzone_kW,
        double congestionFactor_eurpMWhpkW
    ) {
        this.dailyPriceCurve_eurpMWh = dailyPriceCurve_eurpMWh;
        this.marketFeedback_eurpMWhpkW = marketFeedback_eurpMWhpkW;
        this.selfConsumptionSaving_eurpMWh = selfConsumptionSaving_eurpMWh;
        this.congestionDeadzone_kW = congestionDeadzone_kW;
        this.congestionFactor_eurpMWhpkW = congestionFactor_eurpMWhpkW;
    }
    
    /*
     * Calculates the price curve with all (marginal) terms.
     * This pricecurve is used by the asset schedulers (consumption assets & batteries) itself.
     */
    public double[] getMarginalPriceCurveUpwards(double[] flexProfile_kW, double[] loadProfile_kW) {
        if (loadProfile_kW == null) {
            loadProfile_kW = flexProfile_kW;
        }
        double[] loadProfileSign = this.getLoadProfileSignUpwards(loadProfile_kW);
        double[] congestionTerm_eurpMWh = this.getCongestionTerm(loadProfile_kW, loadProfileSign);
        double [] selfConsumptionTerm_eurpMWh = this.getSelfConsumptionTerm(loadProfile_kW);
        double[] marketFeedbackTerm_eurpMWh = this.getMarginalMarketFeedbackTerm(flexProfile_kW);
        
        double[] marginalPriceCurveUpwards_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            marginalPriceCurveUpwards_eurpMWh[i] = dailyPriceCurve_eurpMWh[i] + selfConsumptionTerm_eurpMWh[i] + congestionTerm_eurpMWh[i] + marketFeedbackTerm_eurpMWh[i];
        }
        return marginalPriceCurveUpwards_eurpMWh;
    }
    
    /*
     * Calculates the price curve with all (marginal) terms.
     * This pricecurve is used by the asset schedulers (production assets & batteries) itself.
     */
    public double[] getMarginalPriceCurveDownwards(double[] flexProfile_kW, double[] loadProfile_kW) {
        if (loadProfile_kW == null) {
            loadProfile_kW = flexProfile_kW;
        }
        double[] loadProfileSign = this.getLoadProfileSignDownwards(loadProfile_kW);
        double[] congestionTerm_eurpMWh = this.getCongestionTerm(loadProfile_kW, loadProfileSign);
        double [] selfConsumptionTerm_eurpMWh = this.getSelfConsumptionTerm(loadProfile_kW);
        double[] marketFeedbackTerm_eurpMWh = this.getMarginalMarketFeedbackTerm(flexProfile_kW);

        double[] marginalPriceCurveDownwards_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            marginalPriceCurveDownwards_eurpMWh[i] = dailyPriceCurve_eurpMWh[i] + selfConsumptionTerm_eurpMWh[i] + congestionTerm_eurpMWh[i] + marketFeedbackTerm_eurpMWh[i];
        }
        return marginalPriceCurveDownwards_eurpMWh;
    }
    
    public double[] getDailyPriceCurve_eurpMWh() {
    	return this.dailyPriceCurve_eurpMWh;
    }

    /*
     * Calculates the price curve with market feedback from the flexProfile, but not other terms such as congestion.
     * This is the price curve that is passed on to the next asset scheduler.
     */
    public double[] getMarketFeedbackDailyPriceCurve_eurpMWh(double[] flexProfile_kW) {
        double[] marketFeedbackTerm_eurpMWh = this.getActualMarketFeedbackTerm(flexProfile_kW);

        double[] priceCurve_eurpMWh = new double[flexProfile_kW.length];
        for (int i = 0; i < flexProfile_kW.length; i++) {
        	priceCurve_eurpMWh[i] = dailyPriceCurve_eurpMWh[i] + marketFeedbackTerm_eurpMWh[i];
        }
    	return priceCurve_eurpMWh;
    }
    
    /*
     * This method calculates the price curve with only the actual congestion component
     * Not used by any scheduler or EMS, can be useful when debugging
     */
    public double[] getCongestionDailyPriceCurve_eurpMWh(double[] loadProfile_kW) {
        double[] loadProfileSign = this.getLoadProfileSign(loadProfile_kW);
        double[] congestionTerm_eurpMWh = this.getCongestionTerm(loadProfile_kW, loadProfileSign);

        double[] priceCurve_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
        	priceCurve_eurpMWh[i] = dailyPriceCurve_eurpMWh[i] + congestionTerm_eurpMWh[i];
        }
    	return priceCurve_eurpMWh;
    }
    
    /*
     * Note: The bounds are different from 'getLoadProfileSignDownwards'
     */
    private double[] getLoadProfileSignUpwards(double[] loadProfile_kW) {
        double[] loadProfileSign = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            loadProfileSign[i] = (loadProfile_kW[i] >= congestionDeadzone_kW ? 1 : 0) - ((-loadProfile_kW[i]) > congestionDeadzone_kW ? 1 : 0);
        }
        return loadProfileSign;
    }
    
    /*
     * Note: The bounds are different from 'getLoadProfileSignUpwards'
     */
    private double[] getLoadProfileSignDownwards(double[] loadProfile_kW) {
        double[] loadProfileSign = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            loadProfileSign[i] = (loadProfile_kW[i] > congestionDeadzone_kW ? 1 : 0) - ((-loadProfile_kW[i]) >= congestionDeadzone_kW ? 1 : 0);
        }
        return loadProfileSign;
    }
    
    private double[] getLoadProfileSign(double[] loadProfile_kW) {
	    double[] loadProfileSign = new double[loadProfile_kW.length];
	    for (int i = 0; i < loadProfile_kW.length; i++) {
	        loadProfileSign[i] = (loadProfile_kW[i] >= congestionDeadzone_kW ? 1 : 0) - ((-loadProfile_kW[i]) >= congestionDeadzone_kW ? 1 : 0);
	    }
	    return loadProfileSign;
    }
    
    private double[] getCongestionTerm(double[] loadProfile_kW, double[] loadProfileSign) {
        double[] congestionTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            congestionTerm_eurpMWh[i] = loadProfileSign[i] * (Math.abs(loadProfile_kW[i]) - congestionDeadzone_kW) * congestionFactor_eurpMWhpkW;
        }
        return congestionTerm_eurpMWh;
    }
    
    private double[] getSelfConsumptionTerm(double[] loadProfile_kW) {
        double[] selfConsumptionTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            selfConsumptionTerm_eurpMWh[i] = selfConsumptionSaving_eurpMWh * (loadProfile_kW[i] >= 0 ? 1 : 0);
        }
        return selfConsumptionTerm_eurpMWh;
    }
    
    /*
     * Note: The marginal marketfeedback term contains a factor 2 from the derivative.
     */
    private double[] getMarginalMarketFeedbackTerm(double[] flexProfile_kW) {
        double[] marketFeedbackTerm_eurpMWh = new double[flexProfile_kW.length];
        for (int i = 0; i < flexProfile_kW.length; i++) {
            marketFeedbackTerm_eurpMWh[i] = 2 * flexProfile_kW[i] * marketFeedback_eurpMWhpkW;
        }
        return marketFeedbackTerm_eurpMWh;
    }
    
    private double[] getActualMarketFeedbackTerm(double[] flexProfile_kW) {
        double[] marketFeedbackTerm_eurpMWh = new double[flexProfile_kW.length];
        for (int i = 0; i < flexProfile_kW.length; i++) {
            marketFeedbackTerm_eurpMWh[i] = flexProfile_kW[i] * marketFeedback_eurpMWhpkW;
        }
        return marketFeedbackTerm_eurpMWh;
    }
    
}