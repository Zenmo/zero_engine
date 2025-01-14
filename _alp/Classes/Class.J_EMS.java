/**
 * J_EMS
 */	
public class J_EMS implements Serializable {

	Agent parentAgent;
	double timeStep_h;
	List<J_EA> baseLoadAssets = new ArrayList<>();
	List<J_EAFlexConsumption> schedulableAssets = new ArrayList<>();
	J_LocalPrices j_lp;
	double[] batteryProfile_kW;
	double[] batterySOC_kWh;
	int batteryPowerSteps_n = 10;
	
	/**
     * Default constructor
     */
    public J_EMS() {
    }
    
    public J_EMS(Agent parentAgent, J_LocalPrices j_lp, double timeStep_h) {
    	this.parentAgent = parentAgent;
    	this.j_lp = j_lp;
    	this.timeStep_h = timeStep_h;
    	this.batteryProfile_kW = new double[roundToInt(24 / this.timeStep_h)];
    	this.batterySOC_kWh = new double[roundToInt(24 / this.timeStep_h)];
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
    
    public void resetBatteryProfile( double SOC_kWh) {
    	for (int i = 0; i < this.batteryProfile_kW.length; i++) {
    		this.batteryProfile_kW[i] = 0;
        	this.batterySOC_kWh[i] = SOC_kWh;
    	}
    }
    
    public double getBatteryLoad_kW( int idx ) {
    	return this.batteryProfile_kW[idx];
    }
    
    public double[] getBatteryProfile_kW() {
    	return this.batteryProfile_kW;
    }
    
    public double getBatterySOC_kWh( int idx) {
    	return this.batterySOC_kWh[idx];    	
    }
    
    public double[] getBatterySOCProfile_kWh() {
    	return this.batterySOC_kWh;
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
    
    public double[] scheduleBattery(double[] previousLoadProfile_kW, J_EAStorageElectric battery) {
    	double[] loadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
    	
    	int i = 0;
    	while (i < 500) {
    		// Charging iteration    		
    		double[] localMarginalPriceCurve_eurpMWh = this.j_lp.getMarginalPriceCurveUpwards(loadProfile_kW);
    		int[] cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
    		
    		if (localMarginalPriceCurve_eurpMWh[cheapestTimeIdxsSorted[cheapestTimeIdxsSorted.length - 1]] - localMarginalPriceCurve_eurpMWh[cheapestTimeIdxsSorted[0]] < 30) {
    			traceln("Price spread minimal, aborting after %s iterations", i);
    			break;
    		}
    		
    		J_Triple<double[], Double, Integer> triplet = scheduleBatteryChargingIteration(loadProfile_kW, battery, cheapestTimeIdxsSorted);
    		loadProfile_kW = triplet.getFirst();
    		double addedBESChargePower_kW = triplet.getSecond();
    		int timeStepCharge_n = triplet.getThird();
    		
    		// Discharging iteration
    		localMarginalPriceCurve_eurpMWh = this.j_lp.getMarginalPriceCurveDownwards(loadProfile_kW);
    		cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
    		triplet = scheduleBatteryDischargingIteration(loadProfile_kW, battery, cheapestTimeIdxsSorted);
    		loadProfile_kW = triplet.getFirst();
    		double addedBESDischargePower_kW = triplet.getSecond();
    		int timeStepDischarge_n = triplet.getThird();
    		
    		// Additional checks to stop iterations
    		if (addedBESChargePower_kW == 0 && addedBESDischargePower_kW == 0) {
    			traceln("Stop iteration because added charge and discharge powers are zero. Iterations completed: %s", i);
    			break;
    		}
    		if (timeStepCharge_n == timeStepDischarge_n) {
    			traceln("Stop iteration because timeStepCharge_n and timeStepDischarge_n are equal! Iterations completed: %s", i);
    			break;
    		}
    		
    		i++;
    	}

    	return loadProfile_kW;
    } 
    
    private J_Triple<double[], Double, Integer> scheduleBatteryChargingIteration(double[] loadProfile_kW, J_EAStorageElectric battery, int[] cheapestTimeIdxsSorted) {
    	int iCharge = 0;
    	double[] remainingSOCProfile_kWh = Arrays.copyOfRange(this.batterySOC_kWh, cheapestTimeIdxsSorted[iCharge], this.batterySOC_kWh.length);
    	//traceln("remainingSOCProfile_kWh: " + Arrays.toString(remainingSOCProfile_kWh)); 
    	double roomToCharge_kWh = battery.getStorageCapacity_kWh() - max(remainingSOCProfile_kWh);
    	//traceln("roomToCharge_kWh: " + roomToCharge_kWh);
    	while ( this.batteryProfile_kW[cheapestTimeIdxsSorted[iCharge]] >= battery.getCapacityElectric_kW() || roundToDecimal(roomToCharge_kWh, 6) <= 0.0 ) {
    		if (iCharge == (int)(loadProfile_kW.length / 2) - 1) {
    			break;
    		}
    		iCharge++;
    		remainingSOCProfile_kWh = Arrays.copyOfRange(this.batterySOC_kWh, cheapestTimeIdxsSorted[iCharge], this.batterySOC_kWh.length);
        	roomToCharge_kWh = battery.getStorageCapacity_kWh() - max(remainingSOCProfile_kWh);
    	}
    	int timeStepCharge_n = cheapestTimeIdxsSorted[iCharge];
    	
    	double addedBESChargePower_kW = 0;
    	if (roomToCharge_kWh > 0) {
    		if (loadProfile_kW[timeStepCharge_n] < 0) {
    			// First only selfconsumption
    			addedBESChargePower_kW = min(min(min(
    						battery.getCapacityElectric_kW() / this.batteryPowerSteps_n ,
    						battery.getCapacityElectric_kW() - this.batteryProfile_kW[timeStepCharge_n] ),
    						roomToCharge_kWh / this.timeStep_h / battery.getBatteryChargeEfficiency_r() ),
    						- loadProfile_kW[timeStepCharge_n]
    					);
    		}
    		else {
    			// If no more own production available, gradually add power in powersteps
    			addedBESChargePower_kW = min(min(
    						battery.getCapacityElectric_kW() / this.batteryPowerSteps_n,
    						battery.getCapacityElectric_kW() - this.batteryProfile_kW[timeStepCharge_n]),
    						roomToCharge_kWh / this.timeStep_h / battery.getBatteryChargeEfficiency_r()
    					);    			
    		}
    		
    	}
    	
    	loadProfile_kW[timeStepCharge_n] += addedBESChargePower_kW;
    	this.batteryProfile_kW[timeStepCharge_n] += addedBESChargePower_kW;
	
    	double dSOC_kWh = 0;
    	if (this.batteryProfile_kW[timeStepCharge_n] < 0) {
    		dSOC_kWh = addedBESChargePower_kW * this.timeStep_h; // / battery.getBatteryDischargeEfficiency_r();
    	}
    	else {
    		dSOC_kWh = addedBESChargePower_kW * this.timeStep_h; // * battery.getBatteryChargeEfficiency_r();    		
    	}
    	
    	addBatteryCharge_kWh(dSOC_kWh, timeStepCharge_n);
    			
    	return new J_Triple(loadProfile_kW, addedBESChargePower_kW, timeStepCharge_n);
    }
    
    private J_Triple<double[], Double, Integer> scheduleBatteryDischargingIteration(double[] loadProfile_kW, J_EAStorageElectric battery, int[] cheapestTimeIdxsSorted) {
    	int iDischarge = loadProfile_kW.length - 1;
    	double[] remainingSOCProfile_kWh = Arrays.copyOfRange(this.batterySOC_kWh, cheapestTimeIdxsSorted[iDischarge], this.batterySOC_kWh.length);
    	double roomToDischarge_kWh = min(remainingSOCProfile_kWh);
    	
    	while ( this.batteryProfile_kW[cheapestTimeIdxsSorted[iDischarge]] <= battery.getCapacityElectric_kW() || roundToDecimal(roomToDischarge_kWh, 6) <= 0.0 ) {
    		if (iDischarge == (int)(loadProfile_kW.length / 2) - 1) {
    			break;
    		}
    		iDischarge--; 
    		remainingSOCProfile_kWh = Arrays.copyOfRange(this.batterySOC_kWh, cheapestTimeIdxsSorted[iDischarge], this.batterySOC_kWh.length);
        	roomToDischarge_kWh = min(remainingSOCProfile_kWh);
    	}
    	int timeStepDischarge_n = cheapestTimeIdxsSorted[iDischarge];
    	
    	double addedBESDischargePower_kW = 0;
    	if (roomToDischarge_kWh > 0) {
    		if (loadProfile_kW[timeStepDischarge_n] > 0) {
    			// First only selfconsumption
    			addedBESDischargePower_kW = min(min(min(
    						battery.getCapacityElectric_kW() / this.batteryPowerSteps_n ,
    						battery.getCapacityElectric_kW() + this.batteryProfile_kW[timeStepDischarge_n] ),
    						roomToDischarge_kWh / this.timeStep_h * battery.getBatteryDischargeEfficiency_r() ),
    						loadProfile_kW[timeStepDischarge_n]
    					);
    		}
    		else {
    			// If no more own production available, gradually add power in powersteps
    			addedBESDischargePower_kW = min(min(
    						battery.getCapacityElectric_kW() / this.batteryPowerSteps_n,
    						battery.getCapacityElectric_kW() + this.batteryProfile_kW[timeStepDischarge_n]),
    						roomToDischarge_kWh / this.timeStep_h * battery.getBatteryDischargeEfficiency_r()
    					);    			
    		}
    	}
    	
    	loadProfile_kW[timeStepDischarge_n] -= addedBESDischargePower_kW;
    	this.batteryProfile_kW[timeStepDischarge_n] -= addedBESDischargePower_kW;
    	
    	double dSOC_kWh = 0;
    	if (this.batteryProfile_kW[timeStepDischarge_n] < 0) { 
    		dSOC_kWh = - addedBESDischargePower_kW * this.timeStep_h; // / battery.getBatteryDischargeEfficiency_r();
    	}
    	else {
    		dSOC_kWh = - addedBESDischargePower_kW * this.timeStep_h; // * battery.getBatteryChargeEfficiency_r();    		
    	}
    	
    	addBatteryCharge_kWh(dSOC_kWh, timeStepDischarge_n);
    		
    	return new J_Triple(loadProfile_kW, addedBESDischargePower_kW, timeStepDischarge_n);	
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
    
    // Method adds some energy to the battery at a certain time index and updates the SOC from that point on
    private void addBatteryCharge_kWh(double chargeVolume_kWh, int idx){
    	for(int i = idx; i < this.batterySOC_kWh.length; i++) {
    		this.batterySOC_kWh[i] += chargeVolume_kWh;
    	}
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