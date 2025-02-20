/**
 * J_AccumulatorMap
 */	
//import java.util.EnumMap;
import java.util.EnumSet;
//import zeroPackage.ZeroAccumulator;

public class J_AccumulatorMap implements Serializable {
	
	private ZeroAccumulator[] accumulatorArray = new ZeroAccumulator[OL_EnergyCarriers.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	private EnumSet<OL_EnergyCarriers> energyCarrierList = EnumSet.noneOf(OL_EnergyCarriers.class);

    /**
     * Default constructor
     */
    public J_AccumulatorMap() {
    	//super(OL_EnergyCarriers.class);
    }

    public void createEmptyAccumulators(EnumSet<OL_EnergyCarriers> selectedFlows, boolean hasTimeSeries, double signalResolution_h, double duration_h) {
    	for (OL_EnergyCarriers key : selectedFlows) {
    		this.put(key, new ZeroAccumulator(hasTimeSeries, signalResolution_h, duration_h));
    	}
    }
    
    public ZeroAccumulator get(OL_EnergyCarriers key) {
		return accumulatorArray[key.ordinal()];
	}
    	

	public void put(OL_EnergyCarriers key, ZeroAccumulator acc) {
		accumulatorArray[key.ordinal()] = acc;
		energyCarrierList.add(key);		
	}
    
	public double totalSum() {
		double totalSum = 0.0;
		for (var EC : energyCarrierList) {
			totalSum += accumulatorArray[EC.ordinal()].getSum();
		}
		return totalSum;
	}
	
	public double totalIntegral() {
		double totalIntegral = 0.0;
		for (var EC : energyCarrierList) {
			totalIntegral += accumulatorArray[EC.ordinal()].getIntegral();
		}
		return totalIntegral;
	}
	
	public void clear() {
		accumulatorArray = new ZeroAccumulator[OL_EnergyCarriers.values().length];
		energyCarrierList.clear();
	}
	
	public void reset() {
		for (var EC : energyCarrierList) {
			accumulatorArray[EC.ordinal()].reset();
		}
	}
	

	
    public String toString() {
        if (this.accumulatorArray.length == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (OL_EnergyCarriers key : this.energyCarrierList) {
        	ZeroAccumulator acc = this.get(key);
        	//double value = this.get(key);
        	if (acc.getSum() == 0.0) {
        		continue;
        	}
        	
        	sb.append(key);
        	sb.append(" ");
            sb.append(acc.toString());
            //sb.append(" = ");
            //sb.append(value);
            sb.append(", ");
        }
        //sb.delete(sb.length()-2, sb.length());
        sb.append('}');
        return sb.toString();
    }

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}