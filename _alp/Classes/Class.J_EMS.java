/**
 * J_EMS
 */	
public class J_EMS implements Serializable {

	Agent parentAgent;
	double timeStep_h;
	List<J_EA> baseLoadAssets = new ArrayList<>();
	List<J_EAFlexConsumption> schedulableAssets = new ArrayList<>();
	J_LocalPrices j_lp;
	
    /**
     * Default constructor
     */
    public J_EMS() {
    }
    
    public J_EMS(Agent parentAgent, J_LocalPrices j_lp, double timeStep_h) {
    	this.parentAgent = parentAgent;
    	this.j_lp = j_lp;
    	this.timeStep_h = timeStep_h;
    }

    public void addBaseLoadAsset( J_EA baseLoadAsset) {
    	this.baseLoadAssets.add(baseLoadAsset);
    }
    
    public List<J_EA> getBaseLoadAssets() {
    	return this.baseLoadAssets;
    }
    
    public J_LocalPrices getLocalPrices() {
    	return this.j_lp;
    }
    
    public void addSchedulableAsset( J_EAFlexConsumption schedulableAsset) {
    	this.schedulableAssets.add(schedulableAsset);
    }
    
    public double[] scheduleDay(double[] previousLoadProfile_kW, J_EAFlexConsumption j_ea) {
    	double[] loadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
    	double workRemaining_kWh = j_ea.getDailyDemand_kWh();
    	Pair<double[], Double> pair;
    	while (workRemaining_kWh > 0) {
    		pair = scheduleIteration(loadProfile_kW, j_ea, workRemaining_kWh);
    		loadProfile_kW = pair.getFirst();
    		workRemaining_kWh = pair.getSecond();
    	}	
    	return loadProfile_kW;
    }
    
    private Pair<double[], Double> scheduleIteration(double[] loadProfile_kW, J_EAFlexConsumption j_ea, double workRemaining_kWh) {
    	double[] localMarginalPriceCurve_eurpMWh = this.j_lp.getMarginalPriceCurveUpwards(loadProfile_kW);
    	int[] cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
    	
    	int i = 0;
    	while ( j_ea.getProfile_kW()[cheapestTimeIdxsSorted[i]] >= j_ea.getCapacity_kW()
    			|| !(j_ea.getAllowedOperatingIndices()[cheapestTimeIdxsSorted[i]]) ) {
    		
    		i++;
    	}
    	
    	double addedPower_kW;
    	if (j_ea.getProfile_kW()[cheapestTimeIdxsSorted[i]] < 0) {
    		// First only selfconsumption
    		addedPower_kW = min(min(j_ea.getCapacity_kW() / j_ea.getPowerSteps_n(), workRemaining_kWh / this.timeStep_h), -loadProfile_kW[cheapestTimeIdxsSorted[i]]);
    	}
    	else {
    		// If no more own production available, gradually add power (hence the powersteps)
    		addedPower_kW = min(j_ea.getCapacity_kW() / j_ea.getPowerSteps_n(), workRemaining_kWh / this.timeStep_h);
    	}
    	
    	loadProfile_kW[cheapestTimeIdxsSorted[i]] += addedPower_kW;
    	j_ea.getProfile_kW()[cheapestTimeIdxsSorted[i]] += addedPower_kW;
    	workRemaining_kWh -= addedPower_kW * this.timeStep_h;
    	
    	return new Pair(loadProfile_kW, workRemaining_kWh);
    }
    
    
    // Methods for arg sort
    private int[] argsort(final double[] a) {
        return argsort(a, true);
    }
 
    private int[] argsort(final double[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
            }
        });
        return asArray(indexes);
    }
    
    private <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }
    
    
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