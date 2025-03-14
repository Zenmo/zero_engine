/**
 * ZeroTimeSeries
 */	

import zeroPackage.ZeroMath;
import java.lang.Math;
import java.util.Arrays;
import java.lang.RuntimeException;
public class ZeroTimeSeries implements Serializable {

	    private double[] timeSeries;
	    private double duration_h;
	    private double signalResolution_h = 0.25;
	    private double timeStep_h = 0.25;
	    private double sampleWeight = timeStep_h / signalResolution_h;
	    
	    private double max = 0;
	    private double min = 0;

	    private int numStepsAdded = 0;
	    private int numStepsAddedThisEntry = 0; // Used when signal resolution is different from the timestep
	    /**
	     * Default constructor
	     */
	    public ZeroTimeSeries() {
	    }

	    /**
	     * Constructor initializing the fields
	     */
	    public ZeroTimeSeries(double signalResolution_h, double duration_h) {
	        if (signalResolution_h < this.timeStep_h) {
	        	throw new RuntimeException("Impossible to construct a ZeroAccumulator with Signal Resolution: " + signalResolution_h + " h, as it is shorter than the timestep.");
	        }

	        this.signalResolution_h = signalResolution_h;
	        this.sampleWeight = timeStep_h / signalResolution_h;
	        this.duration_h = duration_h;
	        //this.arraySize = (int) Math.round(duration_h / signalResolution_h);
            timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
	    }

	    public void setTimeStep_h(double timeStep_h) {
	        this.timeStep_h = timeStep_h;
	        sampleWeight = timeStep_h / signalResolution_h;
	    }

	    public void reset() {

	        numStepsAdded = 0;
	        max = 0;
	        min = 0;
	        timeSeries = new double[(int) Math.round(duration_h / signalResolution_h)];
	        
	    }
	    
	    public ZeroTimeSeries getClone() {
	    	ZeroTimeSeries ts = new ZeroTimeSeries(this.signalResolution_h, this.duration_h);
	    	ts.timeStep_h = this.timeStep_h;
    		ts.timeSeries = this.timeSeries.clone();
    		ts.max = this.max;
    		ts.min = this.min;
    		ts.numStepsAdded = this.numStepsAdded;
			ts.numStepsAddedThisEntry = this.numStepsAddedThisEntry;
	    	return ts;
	    }
	    
    
	    public void addStep(double value) {
            this.timeSeries[this.numStepsAdded] += value * sampleWeight;
	        
	        if (value > max) {
	            max = value;
	        }
	        if (value < min) {
	            min = value;
	        }
	        
	        this.numStepsAddedThisEntry ++;
	        if (this.numStepsAddedThisEntry == roundToInt(this.signalResolution_h / this.timeStep_h)) {
	        	this.numStepsAddedThisEntry = 0;
	        	this.numStepsAdded++;
	        }
	    }

	    public double getSum() {
            return ZeroMath.arraySum(timeSeries);
	        //return sum;
	    }

	    public double getSumPos() {
            return ZeroMath.arraySumPos(timeSeries);
	        //return posSum;
	    }
	    
	    public double getSumNeg() {
            return ZeroMath.arraySumNeg(timeSeries);
	        //return negSum;
	    }

	    public double[] getTimeSeries() {
            return this.timeSeries;
	    }

	    public Double getY(int i) {
            return timeSeries[i];
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

	    public ZeroTimeSeries add(ZeroTimeSeries zts) {
	        if ((this.duration_h == zts.duration_h) && (this.signalResolution_h == zts.signalResolution_h)) {
	            for (int i = 0; i < this.timeSeries.length; i++) {
	                this.timeSeries[i] += zts.timeSeries[i];
	            }
	        } else {
	            throw new RuntimeException("Impossible to add these incompatible timeSeries");
	        }
	        return this;
	    }

	    public ZeroTimeSeries subtract(ZeroTimeSeries zts) {
	        if ((this.duration_h == zts.duration_h) && (this.signalResolution_h == zts.signalResolution_h)) {
	            for (int i = 0; i < this.timeSeries.length; i++) {
	                this.timeSeries[i] -= zts.timeSeries[i];
	            }
	        } else {
	            throw new RuntimeException("Impossible to subtract these incompatible timeSeries");
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

	        sb.append("ZeroTimeSeries, current number of steps in time-series: ");
	        sb.append(this.numStepsAdded);

	        return sb.toString();
	    }

		/**
		 * This number is here for model snapshot storing purpose<br>
		 * It needs to be changed when this class gets changed
		 */ 
		private static final long serialVersionUID = 1L;

	}