/**
 * ZeroAccumulator
 */	

import zeroPackage.ZeroMath;
import java.lang.Math;
import java.util.Arrays;
import java.lang.RuntimeException;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class ZeroAccumulator {
    public boolean hasTimeSeries = false;
    private double[] timeSeries;
    private double duration_h;
    private double signalResolution_h = 0.25;
    private double timeStep_h = 0.25;
    private double sampleWeight_fr = timeStep_h / signalResolution_h;
    private int arraySize;
    
    private double totalEnergy_kWh = 0;
    private double totalPositiveEnergy_kWh = 0;
    private double totalNegativeEnergy_kWh = 0;
    private double maxPower_kW = 0;
    private double minPower_kW = 0;

    private int numStepsAdded = 0;
    private int numStepsAddedThisEntry = 0; // Used when signal resolution is different from the timestep
    /**
     * Default constructor
     */
    public ZeroAccumulator() {
    }

    /**
     * Constructor initializing the fields
     */
    public ZeroAccumulator(boolean hasTimeSeries, double signalResolution_h, double duration_h) {
        if (signalResolution_h < this.timeStep_h) {
        	throw new RuntimeException("Impossible to construct a ZeroAccumulator with Signal Resolution: " + signalResolution_h + " h, as it is shorter than the timestep.");
        }
    	this.hasTimeSeries = hasTimeSeries;
        this.signalResolution_h = signalResolution_h;
        this.sampleWeight_fr = timeStep_h / signalResolution_h;
        this.duration_h = duration_h;
        this.arraySize = (int) Math.round(duration_h / signalResolution_h);
        if (hasTimeSeries) { // Allocate memory for timeSeries, only when timeSeries is used.
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
        }
    }

    public ZeroAccumulator getClone() {
    	ZeroAccumulator zeroAccumulator = new ZeroAccumulator(this.hasTimeSeries, this.signalResolution_h, this.duration_h);
    	zeroAccumulator.sampleWeight_fr = this.sampleWeight_fr;
    	if (hasTimeSeries) { 
    		zeroAccumulator.timeSeries = this.timeSeries.clone();
    		zeroAccumulator.maxPower_kW = Arrays.stream(this.timeSeries).max().getAsDouble();
    		zeroAccumulator.minPower_kW = Arrays.stream(this.timeSeries).min().getAsDouble();
    	} else {
    		zeroAccumulator.totalEnergy_kWh = this.totalEnergy_kWh;
    		zeroAccumulator.totalNegativeEnergy_kWh = this.totalNegativeEnergy_kWh;
    		zeroAccumulator.totalPositiveEnergy_kWh = this.totalPositiveEnergy_kWh;
    		zeroAccumulator.maxPower_kW = this.maxPower_kW;
    		zeroAccumulator.minPower_kW = this.minPower_kW;
    	}
		zeroAccumulator.numStepsAdded = this.numStepsAdded;
		zeroAccumulator.numStepsAddedThisEntry = this.numStepsAddedThisEntry;
    	return zeroAccumulator;
    }
    
    public void setTimeStep_h(double timeStep_h) {
        this.timeStep_h = timeStep_h;
        sampleWeight_fr = timeStep_h / signalResolution_h;
    }

    public void reset() {
    	totalEnergy_kWh = 0;
    	totalPositiveEnergy_kWh = 0;
    	totalNegativeEnergy_kWh = 0;
        numStepsAdded = 0;
        maxPower_kW = 0;
        minPower_kW = 0;
        if (hasTimeSeries) { // Allocate memory for timeSeries, only when timeSeries is used.
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
        }
    }


    
    public void addStep(double power_kW) {
        if (hasTimeSeries) {
            // averages multiple timesteps when timeSeries has longer resolution than timestep.        	
            this.timeSeries[this.numStepsAdded] += power_kW * this.sampleWeight_fr;
        } else {
        	totalEnergy_kWh += power_kW  * this.timeStep_h;
        	totalPositiveEnergy_kWh += Math.max(0.0, power_kW) * this.timeStep_h;
        	totalNegativeEnergy_kWh += Math.min(0.0, power_kW) * this.timeStep_h;
        }
        if (power_kW > maxPower_kW) {
        	maxPower_kW = power_kW;
        }
        if (power_kW < minPower_kW) {
        	minPower_kW = power_kW;
        }
        
        this.numStepsAddedThisEntry ++;
        if (this.numStepsAddedThisEntry == roundToInt(this.signalResolution_h / this.timeStep_h)) {
        	this.numStepsAddedThisEntry = 0;
        	this.numStepsAdded++;
        }
    }

    public double getIntegral_kWh() { // For getting total energy when addSteps was called with power as value
        if (this.hasTimeSeries) {
        	this.totalEnergy_kWh = ZeroMath.arraySum(this.timeSeries) * this.signalResolution_h;
        }
    	return this.totalEnergy_kWh;
    }
    
    public double getIntegralPos_kWh() { // For getting total energy when addSteps was called with power as value
        if (this.hasTimeSeries) {
        	this.totalPositiveEnergy_kWh = ZeroMath.arraySumPos(this.timeSeries) * this.signalResolution_h;
        }
        return this.totalPositiveEnergy_kWh;
    }
    

    public double getIntegralNeg_kWh() { // For getting total energy when addSteps was called with power as value
        if (this.hasTimeSeries) {
        	this.totalNegativeEnergy_kWh = ZeroMath.arraySumNeg(this.timeSeries) * this.signalResolution_h;
        }
        return this.totalNegativeEnergy_kWh;
    }
    
    public double getIntegral_MWh() { // For getting total energy when addSteps was called with power as value
    	return this.getIntegral_kWh()/1000;
    }
    
    public double getIntegralPos_MWh() { // For getting total energy when addSteps was called with power as value
    	return this.getIntegralPos_kWh()/1000;
    }

    public double getIntegralNeg_MWh() { // For getting total energy when addSteps was called with power as value
    	return this.getIntegralNeg_kWh()/1000;
    }
    
    
    public double[] getTimeSeries_kW() {
        if (!hasTimeSeries) { // Fill timeseries with constant value
            double[] timeSeriesTemp = new double[arraySize];
            double avgValue = this.totalEnergy_kWh / this.duration_h;
            Arrays.fill(timeSeriesTemp, avgValue);
            return timeSeriesTemp;
        } else {
            return timeSeries;
        }
    }

    /* What does this do?
    public double[] getTimeSeriesIntegral_kWh() {
        if (!hasTimeSeries) { // Fill timeseries with constant value
            double[] timeSeriesTemp = new double[arraySize];
            double avgValue = this.totalEnergy_kWh / arraySize;
            Arrays.fill(timeSeriesTemp, avgValue);
            return timeSeriesTemp;
        } else {
            return ZeroMath.arrayMultiply(timeSeries.clone(), this.signalResolution_h);
        }
    } */

    public Double getY(int i) {
        if (!hasTimeSeries) {
            return null;
        } else {
            return timeSeries[i];
        }
    }

    public double getMaxPower_kW() {
        return maxPower_kW;
    }

    public double getMinPower_kW() {
        return minPower_kW;
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
            this.maxPower_kW = Arrays.stream(this.timeSeries).max().getAsDouble();
            this.minPower_kW = Arrays.stream(this.timeSeries).min().getAsDouble();
            
        } else if ((!this.hasTimeSeries && !acc.hasTimeSeries) && (this.duration_h == acc.duration_h)
                && (this.signalResolution_h == acc.signalResolution_h)) {
            this.totalEnergy_kWh += acc.totalEnergy_kWh;
            // These values below we can not determine since we have no timeSeries (but you can still call getSumPos()...)
            this.totalPositiveEnergy_kWh = 0;
            this.totalNegativeEnergy_kWh = 0;
            this.maxPower_kW = 0;
            this.minPower_kW = 0;
        }    
        else {
            throw new RuntimeException("Impossible to add these incompatible accumulators");
        }
        return this;
    }

    public ZeroAccumulator subtract(ZeroAccumulator acc) {
        if ((this.hasTimeSeries && acc.hasTimeSeries) && (this.duration_h == acc.duration_h)
                && (this.signalResolution_h == acc.signalResolution_h)) {
            for (int i = 0; i < timeSeries.length; i++) {
                this.timeSeries[i] -= acc.timeSeries[i];
            }
            this.maxPower_kW = Arrays.stream(this.timeSeries).max().getAsDouble();
            this.minPower_kW = Arrays.stream(this.timeSeries).min().getAsDouble();
        } else if ((!this.hasTimeSeries && !acc.hasTimeSeries) && (this.duration_h == acc.duration_h)
                && (this.signalResolution_h == acc.signalResolution_h)) {
        	this.totalEnergy_kWh -= acc.totalEnergy_kWh;
            // These values below we can not determine since we have no timeSeries (but you can still call getSumPos()...)
            this.totalPositiveEnergy_kWh = 0;
            this.totalNegativeEnergy_kWh = 0;
            this.maxPower_kW = 0;
            this.minPower_kW = 0;
        } else {
            throw new RuntimeException("Impossible to subtract these incompatible accumulators");
        }
        return this;
    }
    
    public DataSet getDataSet(double startTime_h) {
    	if (this.hasTimeSeries) {
			DataSet ds = new DataSet(timeSeries.length);
			for (int i = 0; i < timeSeries.length; i++) {
				ds.add(startTime_h + i * this.signalResolution_h, roundToDecimal(this.timeSeries[i],3) );
			}
			return ds;
    	} else {
    		throw new RuntimeException("Impossible to create DataSet from accumulator without timeSeries.");    		
    	}
    }
	
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ZeroAccumulator, totalEnergy_kWh: ");
        sb.append(this.totalEnergy_kWh);

        return sb.toString();
    }

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}