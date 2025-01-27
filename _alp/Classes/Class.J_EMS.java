/**
 * J_EMS
 */	
public class J_EMS implements Serializable {

	Agent parentAgent;
	double timeStep_h;
	int timeStepsPerDay_n;
	List<J_EA> baseLoadAssets = new ArrayList<>();
	List<J_EAFlexConsumption> schedulableAssets = new ArrayList<>();
	J_LocalPrices j_lp;
	double[] batteryProfile_kW;
	double[] batterySOC_kWh;
	int batteryPowerSteps_n = 10;
	public int predictionHorizon_days;
	boolean[] availableToCharge;
	boolean[] availableToDischarge;
	/**
     * Default constructor
     */
    public J_EMS() {
    }
    
    public J_EMS(Agent parentAgent, J_LocalPrices j_lp, double timeStep_h, int predictionHorizon_days) {
    	this.predictionHorizon_days = predictionHorizon_days;
    	this.parentAgent = parentAgent;
    	this.j_lp = j_lp;
    	this.timeStep_h = timeStep_h;
    	this.timeStepsPerDay_n = roundToInt(24/timeStep_h);
    	this.batteryProfile_kW = new double[roundToInt(predictionHorizon_days*timeStepsPerDay_n)];
    	this.batterySOC_kWh = new double[roundToInt(predictionHorizon_days*timeStepsPerDay_n)];
    	this.availableToCharge = new boolean[roundToInt(predictionHorizon_days*timeStepsPerDay_n)];
    	this.availableToDischarge = new boolean[roundToInt(predictionHorizon_days*timeStepsPerDay_n)];
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
    
    /*
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
    }*/
    
    public double[] scheduleAssetPerDay(double[] previousLoadProfile_kW, J_EAFlexConsumption j_ea) {
    	double[] loadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);  // Length should correspond to predictionHorizon_days * timeStepsPerDay_n
    	
    	double[] loadPerDay_kW;

    	//int timestepsPerDay_n = roundToInt(previousLoadProfile_kW.length/days);
    	for (int day = predictionHorizon_days-1; day>=0; day--) { // Loop over days; back to front so that profile stored in the asset is of the 'current' day
        	j_ea.resetProfile();
        	loadPerDay_kW = Arrays.copyOfRange(loadProfile_kW, day*timeStepsPerDay_n, (day+1)*timeStepsPerDay_n); // Take load profile of day to be scheduled
	    	double workRemaining_kWh = j_ea.getDailyDemand_kWh();
	    	Pair<double[], Double> pair;
	    	while (workRemaining_kWh > 0) {
	    		pair = scheduleIteration(loadPerDay_kW, j_ea, workRemaining_kWh, day); // need to pass 'day' to get the corresponding price profiles, but flexAssetLoadPerDay_kW is just one day long.
	    		loadPerDay_kW = pair.getFirst();
	    		workRemaining_kWh = pair.getSecond();
	    	}	
    		for(int j=0; j<timeStepsPerDay_n; j++) { // Write result to loadprofile, on the corresponding day.
    			loadProfile_kW[j+timeStepsPerDay_n*day] = loadPerDay_kW[j];
    		}

    	}
    	return loadProfile_kW;
    }
    
    private Pair<double[], Double> scheduleIteration(double[] loadProfile_kW, J_EAFlexConsumption j_ea, double workRemaining_kWh, int day) {
    	double[] localMarginalPriceCurve_eurpMWh = this.j_lp.getMarginalPriceCurveUpwards(loadProfile_kW, day);
    	//int timestepsPerDay_n = roundToInt(24/this.timeStep_h);
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
    	j_ea.getProfile_kW()[cheapestTimeIdxsSorted[i]] += addedPower_kW; // Is this correct when planning multiple days??
    	workRemaining_kWh -= addedPower_kW * this.timeStep_h;
    	
    	return new Pair(loadProfile_kW, workRemaining_kWh);
    }
    
    public double[] scheduleBattery(double[] previousLoadProfile_kW, J_EAStorageElectric battery) {
    	double[] loadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
    	
    	Arrays.fill(availableToCharge, Boolean.TRUE);
    	Arrays.fill(availableToDischarge, Boolean.TRUE);
    	int i = 0;
    	while (i < 500) {
    		// Charging iteration    		
    		double[] localMarginalPriceCurveUpwards_eurpMWh = this.j_lp.getMarginalPriceCurveUpwards(loadProfile_kW);
    		double[] localMarginalPriceCurveDownwards_eurpMWh = this.j_lp.getMarginalPriceCurveDownwards(loadProfile_kW);
    		int[] cheapestTimeIdxsChargingSorted = argsort(localMarginalPriceCurveUpwards_eurpMWh);
    		int[] cheapestTimeIdxsDischargingSorted = argsort(localMarginalPriceCurveDownwards_eurpMWh);
    		/*if (localMarginalPriceCurveDownwards_eurpMWh[cheapestTimeIdxsDischargingSorted[cheapestTimeIdxsDischargingSorted.length-1]] - localMarginalPriceCurveUpwards_eurpMWh[cheapestTimeIdxsChargingSorted[0]] < 30) {
    			traceln("Price spread minimal, aborting after %s iterations", i);
    			break;
    		}*/
    		
    		J_Triple<double[], Double, Integer> triplet = scheduleBatteryChargingIteration(loadProfile_kW, battery, cheapestTimeIdxsChargingSorted, availableToCharge);
    		double addedBESChargePower_kW = triplet.getSecond();
    		int timeStepCharge_n=cheapestTimeIdxsChargingSorted[0];
    		if (triplet.getThird()!=null) {	    		
        		loadProfile_kW = triplet.getFirst();
	    		timeStepCharge_n = triplet.getThird();
	    		availableToDischarge[timeStepCharge_n] = false;
    		}
    		// Discharging iteration

    		//cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
    		triplet = scheduleBatteryDischargingIteration(loadProfile_kW, battery, cheapestTimeIdxsDischargingSorted, availableToDischarge);
    		double addedBESDischargePower_kW = triplet.getSecond();
    		int timeStepDischarge_n=cheapestTimeIdxsDischargingSorted[cheapestTimeIdxsDischargingSorted.length-1];
    		if (triplet.getThird()!=null) {
	    		loadProfile_kW = triplet.getFirst();
	    		timeStepDischarge_n = triplet.getThird();
	    		availableToCharge[timeStepDischarge_n] = false;
	    	}
    		// Additional checks to stop iterations
    		if (addedBESChargePower_kW == 0 && addedBESDischargePower_kW == 0) {
    			//traceln("Stop iteration because added charge and discharge powers are zero. Iterations completed: %s", i);
    			break;
    		}
    		
    		if (localMarginalPriceCurveDownwards_eurpMWh[timeStepDischarge_n] - localMarginalPriceCurveUpwards_eurpMWh[timeStepCharge_n] < 10) {
    			//traceln("Price spread minimal, aborting after %s iterations", i);
    			break;
    		}
    		
    		/*if (timeStepCharge_n == timeStepDischarge_n) {
    			traceln("Stop iteration because timeStepCharge_n and timeStepDischarge_n are equal! Iterations completed: %s", i);
    			break;
    		}*/
    		
    		i++;
    	}

    	return loadProfile_kW;
    } 
    
    private J_Triple<double[], Double, Integer> scheduleBatteryChargingIteration(double[] loadProfile_kW, J_EAStorageElectric battery, int[] cheapestTimeIdxsSorted, boolean[] availableToCharge) {
    	int iCharge = 0;
    	double[] remainingSOCProfile_kWh = Arrays.copyOfRange(this.batterySOC_kWh, cheapestTimeIdxsSorted[iCharge], this.batterySOC_kWh.length);
    	//traceln("remainingSOCProfile_kWh: " + Arrays.toString(remainingSOCProfile_kWh)); 
    	double roomToCharge_kWh = battery.getStorageCapacity_kWh() - max(remainingSOCProfile_kWh);
    	//traceln("roomToCharge_kWh: " + roomToCharge_kWh);
    	while ( this.batteryProfile_kW[cheapestTimeIdxsSorted[iCharge]] >= battery.getCapacityElectric_kW() || roundToDecimal(roomToCharge_kWh, 6) <= 0.0 || !availableToCharge[cheapestTimeIdxsSorted[iCharge]]) {
    		if (iCharge == (int)(loadProfile_kW.length / 2) - 1) {
    			return new J_Triple(loadProfile_kW, 0.0, null);
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
    
    private J_Triple<double[], Double, Integer> scheduleBatteryDischargingIteration(double[] loadProfile_kW, J_EAStorageElectric battery, int[] cheapestTimeIdxsSorted, boolean[] availableToCharge) {
    	int iDischarge = loadProfile_kW.length - 1;
    	double[] remainingSOCProfile_kWh = Arrays.copyOfRange(this.batterySOC_kWh, cheapestTimeIdxsSorted[iDischarge], this.batterySOC_kWh.length);
    	double roomToDischarge_kWh = min(remainingSOCProfile_kWh);
    	
    	while ( this.batteryProfile_kW[cheapestTimeIdxsSorted[iDischarge]] <= -battery.getCapacityElectric_kW() || roundToDecimal(roomToDischarge_kWh, 6) <= 0.0 || !availableToCharge[cheapestTimeIdxsSorted[iDischarge]] ) {
    		if (iDischarge == (int)(loadProfile_kW.length / 2) - 1) {
    			return new J_Triple(loadProfile_kW, 0.0, null);
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

