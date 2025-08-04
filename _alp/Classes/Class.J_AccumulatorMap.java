/**
 * J_AccumulatorMap
 */	
//import java.util.EnumMap;
import java.util.EnumSet;

//import zeroPackage.ZeroAccumulator;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_AccumulatorMap <E extends Enum<E>> implements Serializable {
	
	private ZeroAccumulator[] accumulatorArray;
    private final EnumSet<E> enumSet;
    private final Class<E> enumClass;
	//private ZeroAccumulator[] accumulatorArray = new ZeroAccumulator[OL_EnergyCarriers.values().length]; // Use array with size of all possible energyCarriers; more than strictly needed but memory footprint is negligable anyway.;
	//private EnumSet<OL_EnergyCarriers> energyCarrierList = EnumSet.noneOf(OL_EnergyCarriers.class);
	

    public J_AccumulatorMap(Class<E> enumClass) {
        this.enumClass = enumClass;
        this.enumSet = EnumSet.noneOf(enumClass);
        this.accumulatorArray = new ZeroAccumulator[enumClass.getEnumConstants().length];
    }
    
    public void createEmptyAccumulators(EnumSet<E> selectedFlows, boolean hasTimeSeries, double signalResolution_h, double duration_h) {
    	for (E key : selectedFlows) {
    		this.put(key, new ZeroAccumulator(hasTimeSeries, signalResolution_h, duration_h));
    	}
    }
    
    public J_AccumulatorMap getClone() {
    	J_AccumulatorMap am = new J_AccumulatorMap(enumClass);
    	for (var EC : this.enumSet) {
    		am.put(EC, accumulatorArray[EC.ordinal()].getClone());
    	}
    	return am;
    }
   
    public ZeroAccumulator get(E key) {
		return accumulatorArray[key.ordinal()];
	}
    	

	public void put(E key, ZeroAccumulator acc) {
		accumulatorArray[key.ordinal()] = acc;
		enumSet.add(key);		
	}
	
	public Set<E> keySet() {
		return enumSet;
	}
    /*
	public double totalSum() {
		double totalSum = 0.0;
		for (var EC : energyCarrierList) {
			totalSum += accumulatorArray[EC.ordinal()].getSum();
		}
		return totalSum;
	}
	*/
	public double totalIntegral_kWh() {
		double totalIntegral_kWh = 0.0;
		for (var EC : enumSet) {
			totalIntegral_kWh += accumulatorArray[EC.ordinal()].getIntegral_kWh();
		}
		return totalIntegral_kWh;
	}
	
	public double totalIntegral_MWh() {
		return this.totalIntegral_kWh() / 1000;
	}
	
	public double totalIntegralPos_kWh() {
		double totalIntegralPos_kWh = 0.0;
		for (var EC : enumSet) {
			totalIntegralPos_kWh += accumulatorArray[EC.ordinal()].getIntegralPos_kWh();
		}
		return totalIntegralPos_kWh;
	}
	
	public double totalIntegralPos_MWh() {
		return this.totalIntegralPos_kWh() / 1000;
	}
	
	public double totalIntegralNeg_kWh() {
		double totalIntegralNeg_kWh = 0.0;
		for (var EC : enumSet) {
			totalIntegralNeg_kWh += accumulatorArray[EC.ordinal()].getIntegralNeg_kWh();
		}
		return totalIntegralNeg_kWh;
	}
	
	public double totalIntegralNeg_MWh() {
		return this.totalIntegralNeg_kWh() / 1000;
	}
	
	public void clear() {
		accumulatorArray = new ZeroAccumulator[OL_EnergyCarriers.values().length];
		enumSet.clear();
	}
	
	public void reset() {
		for (var EC : enumSet) {
			accumulatorArray[EC.ordinal()].reset();
		}
	}
	
	public J_AccumulatorMap<E> add( J_AccumulatorMap<E> accumulatorMap ) {
		for (var EC : accumulatorMap.enumSet) {
			if (!this.enumSet.contains(EC)) {
				// make a new one?
				throw new RuntimeException("Tried to add an AccumulatorMap with a new EnergyCarrier.");
			}
			this.get(EC).add(accumulatorMap.get(EC));
		}
		return this;
	}
	
	public J_AccumulatorMap<E> subtract( J_AccumulatorMap<E> accumulatorMap ) {
		for (var EC : accumulatorMap.enumSet) {
			if (!this.enumSet.contains(EC)) {
				// make a new one?
				throw new RuntimeException("Tried to subtract an AccumulatorMap with a new EnergyCarrier.");
			}
			this.get(EC).subtract(accumulatorMap.get(EC));
		}
		return this;
	}
	
	public J_DataSetMap getDataSetMap( double startTime_h ) {
		J_DataSetMap dsm = new J_DataSetMap(OL_EnergyCarriers.class);
		for (var EC : this.enumSet) {
			dsm.put(EC, this.get(EC).getDataSet(startTime_h));
		}
		return dsm;
	}
	
    public String toString() {
        if (this.accumulatorArray.length == 0) {
            return "{}";        	
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (E key : this.enumSet) {
        	ZeroAccumulator acc = this.get(key);
        	//double value = this.get(key);
        	if (acc.getIntegral_kWh() == 0.0) {
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