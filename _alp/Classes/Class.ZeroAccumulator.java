/**
 * ZeroAccumulator
 */	

import zeroPackage.ZeroMath;
import java.lang.Math;
import java.util.Arrays;
import java.lang.RuntimeException;

public class ZeroAccumulator {
    public boolean hasTimeSeries = false;
    private double[] timeSeries;
    private double duration_h;
    private double signalResolution_h = 0.25;
    private double timeStep_h = 0.25;
    private double sampleWeight = timeStep_h / signalResolution_h;
    private int arraySize;

    private double sum = 0;
    private double posSum = 0;
    private double negSum = 0;
    private double max = 0;
    private double min = 0;

    private int numStepsAdded = 0;

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
        sampleWeight = timeStep_h / signalResolution_h;
        this.duration_h = duration_h;
        this.arraySize = (int) Math.round(duration_h / signalResolution_h);
        if (hasTimeSeries) { // Allocate memory for timeSeries, only when timeSeries is used.
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
        }

    }

    public void setTimeStep_h(double timeStep_h) {
        this.timeStep_h = timeStep_h;
        sampleWeight = timeStep_h / signalResolution_h;
    }

    public void reset() {
        sum = 0;
        posSum = 0;
        negSum = 0;
        numStepsAdded = 0;
        max = 0;
        min = 0;
        if (hasTimeSeries) { // Allocate memory for timeSeries, only when timeSeries is used.
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
        }
    }

    // public void addStep(double t_h, double value) {
    public void addValue(double t_h, double value) {
        if (hasTimeSeries) {
            timeSeries[(int) Math.floor(t_h / signalResolution_h)] += value; // averages
                                                                             // multiple
                                                                             // timesteps
                                                                             // when
                                                                             // timeSeries
                                                                             // has
                                                                             // longer
                                                                             // resolution
                                                                             // than
                                                                             // timestep.
        } else {
            sum += value;
            posSum += Math.max(0.0, value);
            negSum += Math.min(0.0, value);
        }
        if (value > max) {
            max = value;
        }
        if (value < min) {
            min = value;
        }
    }

    public void addStep(double value) {
        if (hasTimeSeries) {
            timeSeries[numStepsAdded] += value; // averages
                                                // multiple
                                                // timesteps
                                                // when
                                                // timeSeries
                                                // has
                                                // longer
                                                // resolution
                                                // than
                                                // timestep.
        } else {
            sum += value;
            posSum += Math.max(0.0, value);
            negSum += Math.min(0.0, value);
        }
        if (value > max) {
            max = value;
        }
        if (value < min) {
            min = value;
        }
        numStepsAdded++;
    }

    public double getSum() {
        if (hasTimeSeries) {
            sum = ZeroMath.arraySum(timeSeries);
        }
        return sum;
    }

    public double getIntegral() { // For getting total energy when addSteps was called with power as value
        if (hasTimeSeries) {
            sum = ZeroMath.arraySum(timeSeries);
        }
        return sum * signalResolution_h * sampleWeight;
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
            double[] timeSeriesTemp = new double[arraySize];
            double avgValue = sum / arraySize;
            Arrays.fill(timeSeriesTemp, avgValue);
            /*
             * for (int i = 0; i < arraySize; i++) {
             * timeSeries[i] = avgValue;
             * }
             */
            return timeSeriesTemp;
        } else {
            return timeSeries;
        }
    }

    public double[] getTimeSeriesIntegral() {
        if (!hasTimeSeries) { // Fill timeseries with constant value
            double[] timeSeriesTemp = new double[arraySize];
            double avgValue = sum / arraySize * sampleWeight;
            Arrays.fill(timeSeriesTemp, avgValue);
            /*
             * for (int i = 0; i < arraySize; i++) {
             * timeSeries[i] = avgValue;
             * }
             */
            return timeSeriesTemp;
        } else {
            return ZeroMath.arrayMultiply(timeSeries.clone(), sampleWeight);
        }
    }

    public Double getY(int i) {
        if (!hasTimeSeries) {
            return null;
        } else {
            return timeSeries[i];
        }
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }
    
    public double getSignalResolution_h() {
    	return this.signalResolution_h;
    }

    public ZeroAccumulator add(ZeroAccumulator acc) {
        if ((this.hasTimeSeries && acc.hasTimeSeries) && (this.duration_h == acc.duration_h)
                && (this.signalResolution_h == acc.signalResolution_h)) {
            for (int i = 0; i < timeSeries.length; i++) {
                this.timeSeries[i] += acc.timeSeries[i];
            }
        } else {
            throw new RuntimeException("Impossible to add these incompatible accumulators");
            // throw some error? or make some assumptions?
        }
        return this;
    }

    public ZeroAccumulator subtract(ZeroAccumulator acc) {
        if ((this.hasTimeSeries && acc.hasTimeSeries) && (this.duration_h == acc.duration_h)
                && (this.signalResolution_h == acc.signalResolution_h)) {
            for (int i = 0; i < timeSeries.length; i++) {
                this.timeSeries[i] -= acc.timeSeries[i];
            }
        } else {
            throw new RuntimeException("Impossible to subtract these incompatible accumulators");
            // throw some error? or make some assumptions?
        }
        return this;
    }
    
    public DataSet getDataSet(double startTime_h) {
	
		DataSet ds = new DataSet(timeSeries.length);
		for (int i = 0; i < timeSeries.length; i++) {
			ds.add(startTime_h + i * this.signalResolution_h, this.timeSeries[i] );
		}
		
		return ds;
    }
		
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ZeroAccumulator, currentSum: ");
        sb.append(this.getSum());

        return sb.toString();
    }

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}