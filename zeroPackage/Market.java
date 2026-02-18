package zeroPackage;

public class Market {
    private double[] dailyPriceCurve_eurpMWh;
    private double marketFeedback_eurpMWhpkW;
    private double selfConsumptionSaving_eurpMWh;
    private double congestionDeadzone_kW;
    private double congestionFactor_eurpMWhpkW;

    public Market(
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

    public double[] getMarginalPriceCurveUpwards(double[] flexProfile_kW, double[] loadProfile_kW) {
        if (loadProfile_kW == null) {
            loadProfile_kW = flexProfile_kW;
        }

        double[] loadProfileSign = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            loadProfileSign[i] = (loadProfile_kW[i] >= congestionDeadzone_kW ? 1 : 0) - ((-loadProfile_kW[i]) > congestionDeadzone_kW ? 1 : 0);
        }

        double[] congestionTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            congestionTerm_eurpMWh[i] = loadProfileSign[i] * (Math.abs(loadProfile_kW[i]) - congestionDeadzone_kW + Math.abs(loadProfile_kW[i])) * congestionFactor_eurpMWhpkW;
        }

        double[] selfConsumptionTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            selfConsumptionTerm_eurpMWh[i] = selfConsumptionSaving_eurpMWh * (loadProfile_kW[i] >= 0 ? 1 : 0);
        }

        double[] marketFeedbackTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            marketFeedbackTerm_eurpMWh[i] = 2 * flexProfile_kW[i] * marketFeedback_eurpMWhpkW;
        }

        double[] marginalPriceCurveUpwards_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            marginalPriceCurveUpwards_eurpMWh[i] = dailyPriceCurve_eurpMWh[i] + selfConsumptionTerm_eurpMWh[i] + congestionTerm_eurpMWh[i] + marketFeedbackTerm_eurpMWh[i];
        }
        return marginalPriceCurveUpwards_eurpMWh;
    }

    public double[] getMarginalPriceCurveDownwards(double[] flexProfile_kW, double[] loadProfile_kW) {
        if (loadProfile_kW == null) {
            loadProfile_kW = flexProfile_kW;
        }

        double[] loadProfileSign = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            loadProfileSign[i] = (loadProfile_kW[i] > congestionDeadzone_kW ? 1 : 0) - ((-loadProfile_kW[i]) >= congestionDeadzone_kW ? 1 : 0);
        }

        double[] congestionTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            congestionTerm_eurpMWh[i] = loadProfileSign[i] * (Math.abs(loadProfile_kW[i]) - congestionDeadzone_kW + Math.abs(loadProfile_kW[i])) * congestionFactor_eurpMWhpkW;
        }

        double[] selfConsumptionTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            selfConsumptionTerm_eurpMWh[i] = selfConsumptionSaving_eurpMWh * (loadProfile_kW[i] > 0 ? 1 : 0);
        }

        double[] marketFeedbackTerm_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            marketFeedbackTerm_eurpMWh[i] = 2 * flexProfile_kW[i] * marketFeedback_eurpMWhpkW;
        }

        double[] marginalPriceCurveDownwards_eurpMWh = new double[loadProfile_kW.length];
        for (int i = 0; i < loadProfile_kW.length; i++) {
            marginalPriceCurveDownwards_eurpMWh[i] = dailyPriceCurve_eurpMWh[i] + selfConsumptionTerm_eurpMWh[i] + congestionTerm_eurpMWh[i] + marketFeedbackTerm_eurpMWh[i];
        }
        return marginalPriceCurveDownwards_eurpMWh;
    }
}

