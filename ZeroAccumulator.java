package zeroPackage;

import zeroPackage.ZeroMath;
import java.lang.Math;

public class ZeroAccumulator {
    public boolean hasTimeSeries = false;
    private double[] timeSeries;
    private double duration_h;
    public double signalResolution_h = 0.25;
    public int arraySize;

    private double sum = 0;
    private double posSum = 0;
    private double negSum = 0;

    /**
     * Default constructor
     */
    public ZeroAccumulator() {
    }

    /**
     * Constructor initializing the fields
     */
    public ZeroAccumulator(boolean hasTimeSeries, double signalResolution_h, double duration_h) {
        this.hasTimeSeries = hasTimeSeries;
        this.signalResolution_h = signalResolution_h;
        this.duration_h = duration_h;
        this.arraySize = (int) Math.round(duration_h / signalResolution_h);
        if (hasTimeSeries) { // Allocate memory for timeSeries, only when timeSeries is used.
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
        }

    }

    public void reset() {
        sum = 0;
        posSum = 0;
        negSum = 0;
        if (hasTimeSeries) { // Allocate memory for timeSeries, only when timeSeries is used.
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
        }
    }

    public void addStep(double t_h, double value) {
        if (hasTimeSeries) {
            timeSeries[(int) Math.round(t_h / signalResolution_h)] = value;
        } else {
            sum += value;
            posSum += Math.max(0.0, value);
            negSum += Math.min(0.0, value);
        }
    }

    public double getSum() {
        if (hasTimeSeries) {
            sum = ZeroMath.arraySum(timeSeries);
        }
        return sum;
    }

    public double getIntegral() {
        if (hasTimeSeries) {
            sum = ZeroMath.arraySum(timeSeries);
        }
        return sum * signalResolution_h;
    }

    public double getSumPos() {
        if (hasTimeSeries) {
            posSum = ZeroMath.arraySumPos(timeSeries);
        }
        return posSum;
    }

    public double getSumNeg() {
        if (hasTimeSeries) {
            negSum = ZeroMath.arraySumNeg(timeSeries);
        }
        return negSum;
    }

    public double[] getTimeSeries() {
        if (!hasTimeSeries) { // Fill timeseries with constant value
            timeSeries = new double[arraySize];
            for (int i = 0; i < arraySize; i++) {
                timeSeries[i] = sum / arraySize;
            }
        }
        return timeSeries;
    }
}
