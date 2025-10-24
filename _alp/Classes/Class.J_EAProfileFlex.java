import zeroPackage.ZeroMath;

/**
 * J_EAProfileFlex
 */	
public class J_EAProfileFlex extends zero_engine.J_EAProfile {
	
	public OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	public double[] a_energyProfile_kWh;
	private double profileTimestep_h;
    private double profileStarTime_h = 0;
	public double lostLoad_kWh = 0;
	private double profileScaling_fr = 1;
	private boolean enableProfileLooping = true;
	
	private double timeStep_h;
	
	////Flex parameters (DISCRETE INTERVAL)	(Only works for shifting profile itself)
	private double[] flexLoadSchedule_kW;
	
	////Flex parameters (CONTINUOUS INTERVAL) IN PROGRESS
	private boolean loadShiftingEnabled = false;
	private Agent target; // The target its net balance is used as a trigger for the shifting.
	private double maxPowerShift_fr = 0.1; // How much energy can be shifted each timestep. 10% means 10 pct of the normal energy consumption of that timestep can be reduced/increased.
	private double loadShiftingMaxDuration_hr = 24; // How fast the used flex energy should be compensated for.
    private double targetLimitTolerance_fr = 0.8; // If capacity of node is 100 kW, the shifting should start at 80 kW.
	private double storedFlexBalance_kWh = 0; //Stored flex balance (energy buffer state)
	private double[] compensatedShiftingIntervalHistory_kW; // Flex balance of last flex interval duration, if it gets higher then the storedFlexBalance_kWh something is wrong.
    private double previousFlexLoad_kW = 0;
	
	//Low pass filter
	private double filterTimeScale_h = 3;
    private double filterDiffGain_r;
    private double targetLoadLowPassed_kW = 0;
    private boolean targetLoadLowPassedInitialized = false;
    private boolean storedTargetLoadLowPassedInitialized = false;
    
    //Historic target load
	private double[] historicTargetLoadWithoutFlexArray_kW; // Historic target load used to get the average
        
    //Stored states
    private double storedStoredFlexBalance_kWh = 0;
    private double storedPreviousFlexLoad_kW = 0;
    private double[] storedCompensatedShiftingIntervalHistory_kW;
    private double storedTargetLoadLowPassed_kW = 0;
	private double[] storedHistoricTargetLoadWithoutFlexArray_kW; // Historic target load used to get the average
	private double[] storedFlexLoadSchedule_kW;
	
    /**
     * Default constructor
     */
    public J_EAProfileFlex(Agent parentAgent, OL_EnergyCarriers energyCarrier, double[] profile_kWh, OL_AssetFlowCategories assetCategory, double profileTimestep_h, Agent target, double maxPowerShift_fr, double loadShiftingMaxDuration_hr) {
	    this.parentAgent= parentAgent;
	    this.energyCarrier = energyCarrier;
	    this.a_energyProfile_kWh = profile_kWh;
	    //this.profileType = profileType;
	    this.profileTimestep_h = profileTimestep_h;
	    this.assetFlowCategory = assetCategory;

	    this.timeStep_h = ((GridConnection)parentAgent).energyModel.p_timeStep_h;
	    
	    //this.activeProductionEnergyCarriers.add(this.energyCarrier);
	    this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
	    
	    //Flex
	    this.target = target;
	    this.maxPowerShift_fr = maxPowerShift_fr;
	    this.loadShiftingMaxDuration_hr = loadShiftingMaxDuration_hr;
	    this.compensatedShiftingIntervalHistory_kW = new double[roundToInt(loadShiftingMaxDuration_hr/this.timeStep_h)];
	    this.historicTargetLoadWithoutFlexArray_kW = new double[roundToInt(loadShiftingMaxDuration_hr/this.timeStep_h)];
	    this.flexLoadSchedule_kW = new double[roundToInt(loadShiftingMaxDuration_hr/this.timeStep_h)];
    	this.filterDiffGain_r = 1/(filterTimeScale_h/timeStep_h);
    	
	    //Register
		registerEnergyAsset();
	}
    
    @Override
    public void f_updateAllFlows(double time_h) {

    	operate(time_h-this.profileStarTime_h);

    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
    	}
    	this.lastFlowsMap.cloneMap(flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }
    
    @Override
    public void operate(double time_h) {
    	if (enableProfileLooping && time_h >= a_energyProfile_kWh.length * profileTimestep_h) {
    		time_h = time_h % a_energyProfile_kWh.length * profileTimestep_h;
    	} else if ( (int)floor(time_h/profileTimestep_h) >= a_energyProfile_kWh.length ) {
    		traceln("Time out of upper bound for evaluating J_EAProfile power in profile asset %s!", this.energyAssetName);
//    		time_h = a_energyProfile_kWh.length * profileTimestep_h - 1;
    		throw new RuntimeException(String.format("Time out of upper bound for evaluating J_EAProfile power! Time is: %s", time_h));
    	}
    	if ( time_h < 0 ) {
    		traceln("Time out of lower bound for evaluating J_EAProfile power in profile asset %s!", this.energyAssetName);
    		throw new RuntimeException(String.format("Time out of lower bound for evaluating J_EAProfile power! Time is: %s", time_h));
    	}
    	
    	
    	
    	double currentPower_kW = this.profileScaling_fr * this.a_energyProfile_kWh[(int)floor(time_h/profileTimestep_h)]/profileTimestep_h;
    	
    	//Redetermine current power based on flex need
    	if(loadShiftingEnabled) {
    		//currentPower_kW = shiftCurrentPower_ContinuousInterval_kW(currentPower_kW);
    		currentPower_kW = shiftCurrentPower_DiscreteInterval_kW(currentPower_kW);
    	}
    	
    	this.energyUse_kW = currentPower_kW;
		this.energyUsed_kWh += timestep_h * energyUse_kW; 
		this.flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			this.assetFlowsMap.put(this.assetFlowCategory, currentPower_kW);
		}
    }
    
    private double shiftCurrentPower_DiscreteInterval_kW(double currentPower_kW) {
    	if(((GridConnection)parentAgent).energyModel.t_h % loadShiftingMaxDuration_hr == 0) {
    		this.flexLoadSchedule_kW =  getFlexLoadSchedule_kW();
    	}
    	
    	int currentFlexLoadScheduleIndex = roundToInt((((GridConnection)parentAgent).energyModel.t_h % loadShiftingMaxDuration_hr) / this.timeStep_h);
    	double flexLoad_kW = flexLoadSchedule_kW[currentFlexLoadScheduleIndex];

    	
    	
    	

    	return currentPower_kW + flexLoad_kW;
    }
    
    private double[] getFlexLoadSchedule_kW() {
		double hour_of_simulation_year = ((GridConnection)parentAgent).energyModel.t_h - ((GridConnection)parentAgent).energyModel.p_runStartTime_h;

		int startTimeIntervalIndex = roundToInt(hour_of_simulation_year/this.timeStep_h);
		int endTimeIntervalIndex = roundToInt((hour_of_simulation_year + this.loadShiftingMaxDuration_hr)/this.timeStep_h);
		double[] profile_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(a_energyProfile_kWh, startTimeIntervalIndex, endTimeIntervalIndex), 1/this.timeStep_h );
		
		//Initialize chargepoint array
		double[] newFlexLoadSchedule_kW = new double[this.flexLoadSchedule_kW.length];
		
		//Get the peak load that should be shaved
		double[] sortedProfile_kW = Arrays.copyOf(profile_kW, profile_kW.length);// ascending
		Arrays.sort(sortedProfile_kW);
    	double highestPctPeak_kW = sortedProfile_kW[(int)(0.8 * (sortedProfile_kW.length - 1))];
    	double lowestPctPeak_kW = sortedProfile_kW[(int)(0.2 * (sortedProfile_kW.length - 1))];
    	
		//Calculate the total export over the day that will always be shifted and add to schedule
		double loadShiftBalance_kWh = 0;
		if(lowestPctPeak_kW < 0 ) {
			for(int i = 0; i < profile_kW.length; i++){
				if(profile_kW[i] < lowestPctPeak_kW){
					double loadShift_kW = min(-profile_kW[i]* this.maxPowerShift_fr, -(profile_kW[i] - lowestPctPeak_kW));
					loadShiftBalance_kWh += loadShift_kW*this.timeStep_h;
					newFlexLoadSchedule_kW[i] = loadShift_kW;
				}
			}
		}
		
		//Calculate the total import over the day that will always be shifted and add to schedule
		if(highestPctPeak_kW > 0 ) {
			for(int i = 0; i < profile_kW.length; i++){
				if(profile_kW[i] > highestPctPeak_kW){
					double loadShift_kW = -min(profile_kW[i]* this.maxPowerShift_fr, profile_kW[i] - highestPctPeak_kW);
					loadShiftBalance_kWh +=  loadShift_kW*this.timeStep_h;
					newFlexLoadSchedule_kW[i] = loadShift_kW;
				}
			}
		}
		
		//Redetermine the load profile after initial load shift
		double[] shiftedLoadProfile_kW = new double[profile_kW.length];
		for(int i = 0; i < profile_kW.length; i++){
			shiftedLoadProfile_kW[i] = profile_kW[i] + newFlexLoadSchedule_kW[i];
		}
		
		//ReDetermine the load shift schedule
		Set<Integer> unshiftableIndexes = new HashSet<>();
		double reDistributionStepSize_kW = 1.0;
		
		if(loadShiftBalance_kWh < -1e-9) {
			while(loadShiftBalance_kWh < -1e-9) {
				int minIndex = -1;
				double minValue_kW = Double.MAX_VALUE;
	            for (int i = 0; i < shiftedLoadProfile_kW.length; i++) {
	                if (shiftedLoadProfile_kW[i] <= minValue_kW && !unshiftableIndexes.contains(i)) {
	                    minValue_kW = shiftedLoadProfile_kW[i];
	                    minIndex = i;
	                }
	            }
	            
	            if(minIndex == -1) {
	            	throw new RuntimeException("Can not distribute the load shifting fully.");
	            }
	            
	            shiftedLoadProfile_kW[minIndex] += reDistributionStepSize_kW;
	            newFlexLoadSchedule_kW[minIndex] += reDistributionStepSize_kW;
	            loadShiftBalance_kWh += reDistributionStepSize_kW * this.timeStep_h;
	            if(abs(newFlexLoadSchedule_kW[minIndex]) >= abs(profile_kW[minIndex]* this.maxPowerShift_fr)) {
	            	unshiftableIndexes.add(minIndex);
	            }
				
			}
		}
		else if(loadShiftBalance_kWh > 1e-9) {
			while(loadShiftBalance_kWh > 1e-9) {
				int maxIndex = -1;
				double maxValue_kW = -Double.MAX_VALUE;
	            for (int i = 0; i < shiftedLoadProfile_kW.length; i++) {
	                if (shiftedLoadProfile_kW[i] >= maxValue_kW && !unshiftableIndexes.contains(i)) {
	                    maxValue_kW = shiftedLoadProfile_kW[i];
	                    maxIndex = i;
	                }
	            }
	            
	            if(maxIndex == -1) {
	            	throw new RuntimeException("Can not distribute the load shifting fully.");
	            }
	            
	            shiftedLoadProfile_kW[maxIndex] -= reDistributionStepSize_kW;
	            newFlexLoadSchedule_kW[maxIndex] -= reDistributionStepSize_kW;
	            loadShiftBalance_kWh -= reDistributionStepSize_kW * this.timeStep_h;
	            if(abs(newFlexLoadSchedule_kW[maxIndex]) >= abs(profile_kW[maxIndex]* this.maxPowerShift_fr)) {
	            	unshiftableIndexes.add(maxIndex);
	            }
			}
		}
		return newFlexLoadSchedule_kW;
    }
    
    private double shiftCurrentPower_ContinuousInterval_kW(double currentPower_kW) {
    	
    	//Check if the flex profile management meets the constraints
    	if(abs(compensatedShiftingIntervalHistory_kW[compensatedShiftingIntervalHistory_kW.length - 1]) > 1e-9) {
    		traceln( "Uncompensated remains [kWh]: "+ compensatedShiftingIntervalHistory_kW[compensatedShiftingIntervalHistory_kW.length - 1]);
    		throw new RuntimeException("Flex profile has not compensated the shifted load within the set maximum timelimit");
    	}

    	//Determine the flex load requirement based on the target current load (with timestep delay)
    	double currentTargetLoad_kW = 0;
    	double currentTargetLoadWithoutFlexLoad_kW = 0;
    	double currentTargetConnectionLimitDelivery_kW = 0;
    	double currentTargetConnectionLimitFeedin_kW = 0;

    	if(target instanceof GridNode gn) {
    		currentTargetLoad_kW = gn.v_currentLoad_kW;
    		currentTargetConnectionLimitDelivery_kW = gn.p_capacity_kW;
    		currentTargetConnectionLimitFeedin_kW = gn.p_capacity_kW;
    		
    		if(gn.f_getLowerLVLConnectedGridNodes().contains(((GridConnection) parentAgent).p_parentNodeElectric)) {
    			currentTargetLoadWithoutFlexLoad_kW =  currentTargetLoad_kW - this.previousFlexLoad_kW;
    		}
    		else {
    			currentTargetLoadWithoutFlexLoad_kW =  currentTargetLoad_kW;
    		}
    	}
    	else if(target instanceof GridConnection gc) {
    		currentTargetLoad_kW = gc.fm_currentBalanceFlows_kW.get(this.energyCarrier);
    		currentTargetConnectionLimitDelivery_kW = gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    		currentTargetConnectionLimitFeedin_kW = gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    		
    		if(target == parentAgent) {
    			currentTargetLoadWithoutFlexLoad_kW = currentPower_kW - this.previousFlexLoad_kW;
    		}
    		else {
    			currentTargetLoadWithoutFlexLoad_kW = currentTargetLoad_kW;
    		}
    	}
    	else if(target instanceof EnergyCoop energyCoop) {
    		currentTargetLoad_kW = energyCoop.fm_currentBalanceFlows_kW.get(this.energyCarrier);
    		currentTargetConnectionLimitDelivery_kW = energyCoop.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    		currentTargetConnectionLimitFeedin_kW = energyCoop.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    		
    		if(energyCoop.f_getAllChildMemberGridConnections().contains(parentAgent)) {
    			currentTargetLoadWithoutFlexLoad_kW = currentTargetLoad_kW - this.previousFlexLoad_kW; 
    		}
    		else {
    			currentTargetLoadWithoutFlexLoad_kW = currentTargetLoad_kW; //
    		}
    	}
    	else if(target instanceof EnergyModel energyModel) {
    		currentTargetLoad_kW = energyModel.fm_currentBalanceFlows_kW.get(this.energyCarrier);
    		currentTargetConnectionLimitDelivery_kW = energyModel.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    		currentTargetConnectionLimitFeedin_kW = energyModel.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    		
    		currentTargetLoadWithoutFlexLoad_kW = currentTargetLoad_kW - ((GridConnection) parentAgent).v_previousPowerElectricity_kW; 
    	}
    	    	
    	//Initialize the lowpass filter during rapid run
    	if(((GridConnection)this.parentAgent).energyModel.t_h > 0 && !targetLoadLowPassedInitialized) {
    		double intialValue_kW = 0;
    		if(target == parentAgent) {
    			intialValue_kW = currentTargetLoadWithoutFlexLoad_kW + this.a_energyProfile_kWh[0]/this.profileTimestep_h;
    		}
    		else {
    			intialValue_kW = currentTargetLoadWithoutFlexLoad_kW;
    		}
    		
    		this.targetLoadLowPassed_kW = intialValue_kW;
    		Arrays.fill(historicTargetLoadWithoutFlexArray_kW, intialValue_kW);
    		this.targetLoadLowPassedInitialized = true;
    	}
    	
    	//Update the low passfilter
    	this.targetLoadLowPassed_kW += (currentTargetLoadWithoutFlexLoad_kW - targetLoadLowPassed_kW) * filterDiffGain_r;
    	
    	//Get the average target load without flex
    	double averageLoadPastInterval_kW = ZeroMath.arraySum(historicTargetLoadWithoutFlexArray_kW)/historicTargetLoadWithoutFlexArray_kW.length;
    	
    	//Get the 80th pct peak of past interval
    	double[] sortedHistoricTargetLoadArray_kW = Arrays.copyOf(historicTargetLoadWithoutFlexArray_kW, historicTargetLoadWithoutFlexArray_kW.length);
    	Arrays.sort(sortedHistoricTargetLoadArray_kW); // ascending
    	double highestPctPeak80th_kW = sortedHistoricTargetLoadArray_kW[(int)(0.95 * (sortedHistoricTargetLoadArray_kW.length - 1))];
    	double lowestPctPeak20th_kW = sortedHistoricTargetLoadArray_kW[(int)(0.2 * (sortedHistoricTargetLoadArray_kW.length - 1))];
    	
    	//Constraint: At the end of the simulation year should the storedFlexBalance_kWh be zero. For now to make that happen does the shifting not occur in the final interval duration, only to compensate the storedFlexBalance_kWh;
    	boolean inFinalInterval = ((GridConnection)this.parentAgent).energyModel.t_h >= ((GridConnection)this.parentAgent).energyModel.p_runEndTime_h - loadShiftingMaxDuration_hr;
    	
    	// Determine the required flex load -> Negative means reduce current consumption, positive means increase
    	double flexLoad_kW = 0;

    	// Compute desired change in load looking at the low-pass (Higher load than lowpass? -> Negative desired change)
    	double desiredChange_kW = 0;
    	//desiredChange_kW = targetLoadLowPassed_kW - currentTargetLoadWithoutFlexLoad_kW;
    	//desiredChange_kW = averageLoadPastInterval_kW - currentTargetLoadWithoutFlexLoad_kW;
    	if(currentTargetLoadWithoutFlexLoad_kW < 0) {
    		desiredChange_kW = lowestPctPeak20th_kW - currentTargetLoadWithoutFlexLoad_kW;
    	}
    	else {
    		desiredChange_kW = highestPctPeak80th_kW - currentTargetLoadWithoutFlexLoad_kW;
    	}
    	
    	// Get the limit of the max shift in power when load shifting.
    	double loadShiftLimit_kW = currentPower_kW * this.maxPowerShift_fr;

    	//Determine flex load -> load shifting, or compensating previous load shifting
    	if(!inFinalInterval && currentTargetLoadWithoutFlexLoad_kW > highestPctPeak80th_kW && desiredChange_kW < 0) {
    	    // Reduce consumption to avoid exceeding delivery constraint
    	    flexLoad_kW = max(-loadShiftLimit_kW, min(desiredChange_kW, loadShiftLimit_kW));
    	    traceln("FlexLoad A");
    	}
    	else if(!inFinalInterval && currentTargetLoadWithoutFlexLoad_kW < 0 && currentTargetLoadWithoutFlexLoad_kW < lowestPctPeak20th_kW && desiredChange_kW > 0) {
    	    // Increase consumption to avoid exceeding feed-in constraint
    	    flexLoad_kW = max(-loadShiftLimit_kW, min(desiredChange_kW, loadShiftLimit_kW));
    	    traceln("FlexLoad B");
    	}
    	//Compensation of stored flex balance (only if no threshold-triggered shift or in final interval)
    	else if(abs(storedFlexBalance_kWh) > 1e-9) {// Move stored balance toward zero
    	    double maxCompensationPower_kW = min(abs(storedFlexBalance_kWh) / this.timeStep_h, abs(desiredChange_kW));
    	    if (storedFlexBalance_kWh > 0 && desiredChange_kW < 0) {//over-consumed -> increase consumption
    	        flexLoad_kW = -maxCompensationPower_kW;
    	        traceln("FlexLoad C");
    	    } 
    	    else if (storedFlexBalance_kWh < 0 && desiredChange_kW > 0) {//under-consumed -> increase consumption
    	        flexLoad_kW = maxCompensationPower_kW;
    	        traceln("FlexLoad D");
    	    } 
    	    else {//Balanced -> no flex
    	        flexLoad_kW = 0.0;
    	    }
    	}
    	
    	//Compensate the flex interval history
    	double flexLoadCompensation_kW = flexLoad_kW;
    	for (int i = compensatedShiftingIntervalHistory_kW.length - 1; i > -1; i--) {
    		if(flexLoadCompensation_kW > 0 && compensatedShiftingIntervalHistory_kW[i] < 0) {
    			double partialFlexLoadCompensation_kW = min(flexLoadCompensation_kW, -compensatedShiftingIntervalHistory_kW[i]);
    			compensatedShiftingIntervalHistory_kW[i] += partialFlexLoadCompensation_kW;
    			flexLoadCompensation_kW -= partialFlexLoadCompensation_kW;
    		}
    		else if(flexLoadCompensation_kW < 0 && compensatedShiftingIntervalHistory_kW[i] > 0) {
    			double partialFlexLoadCompensation_kW = min(-flexLoadCompensation_kW, compensatedShiftingIntervalHistory_kW[i]);
    			compensatedShiftingIntervalHistory_kW[i] -= partialFlexLoadCompensation_kW;
    			flexLoadCompensation_kW += partialFlexLoadCompensation_kW;
    		}
    		
    		if(abs(flexLoadCompensation_kW) < 1e-9) {
    			break;
    		}
    	} 	
    	
    	//Update the load and flex interval history and stored flex balance
    	for (int j = compensatedShiftingIntervalHistory_kW.length - 1; j > 0; j--) {
    		compensatedShiftingIntervalHistory_kW[j] = compensatedShiftingIntervalHistory_kW[j - 1];
    		historicTargetLoadWithoutFlexArray_kW[j] = historicTargetLoadWithoutFlexArray_kW[j - 1];
    	}
   
    	compensatedShiftingIntervalHistory_kW[0] = flexLoadCompensation_kW;
    	historicTargetLoadWithoutFlexArray_kW[0] = currentTargetLoadWithoutFlexLoad_kW;
    	storedFlexBalance_kWh += flexLoad_kW*this.timeStep_h;
    	
    	traceln("flexLoad_kW: " + flexLoad_kW + ", storedFlexBalance_kWh: " + storedFlexBalance_kWh);
    	this.previousFlexLoad_kW = flexLoad_kW;
    	
    	return currentPower_kW + flexLoad_kW;
    }

	
    public void enableLoadShifting(boolean enableLoadShifting) {
    	this.loadShiftingEnabled = enableLoadShifting;
    	this.targetLoadLowPassedInitialized = false;
    }
    public boolean getLoadShiftingEnabled() {
    	return this.loadShiftingEnabled;
    }
    
    public void setTarget(Agent target) {
    	this.target = target;
    	this.targetLoadLowPassedInitialized = false;
    }
    
    @Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	this.storedStoredFlexBalance_kWh = this.storedFlexBalance_kWh;
    	this.storedPreviousFlexLoad_kW = this.previousFlexLoad_kW;
    	this.storedCompensatedShiftingIntervalHistory_kW = this.compensatedShiftingIntervalHistory_kW;
    	this.storedTargetLoadLowPassed_kW = this.targetLoadLowPassed_kW;
    	this.storedTargetLoadLowPassedInitialized = targetLoadLowPassedInitialized;
    	this.storedHistoricTargetLoadWithoutFlexArray_kW = this.historicTargetLoadWithoutFlexArray_kW;
    	this.storedFlexLoadSchedule_kW = this.flexLoadSchedule_kW;
    	
    	this.storedFlexBalance_kWh = 0.0;
    	this.previousFlexLoad_kW = 0.0;
    	this.compensatedShiftingIntervalHistory_kW = new double[this.compensatedShiftingIntervalHistory_kW.length];
    	this.historicTargetLoadWithoutFlexArray_kW = new double[this.historicTargetLoadWithoutFlexArray_kW.length];
    	this.flexLoadSchedule_kW = new double[this.flexLoadSchedule_kW.length];
    	this.targetLoadLowPassed_kW = 0.0;
    	this.targetLoadLowPassedInitialized = false;
    	energyUsed_kWh = 0.0;
    	clear();    	
    }
    
    @Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	this.storedFlexBalance_kWh = this.storedStoredFlexBalance_kWh;
    	this.previousFlexLoad_kW = this.storedPreviousFlexLoad_kW;
    	this.compensatedShiftingIntervalHistory_kW = this.storedCompensatedShiftingIntervalHistory_kW;
    	this.targetLoadLowPassed_kW = this.storedTargetLoadLowPassed_kW;
    	this.targetLoadLowPassedInitialized = this.storedTargetLoadLowPassedInitialized;
    	this.historicTargetLoadWithoutFlexArray_kW = this.storedHistoricTargetLoadWithoutFlexArray_kW;
    	this.flexLoadSchedule_kW = this.storedFlexLoadSchedule_kW;
    	energyUsed_kWh = energyUsedStored_kWh;
    }
    
	@Override
	public String toString() {
		return 	"J_EAProfileFlex with targetAgent: " + this.target + ", storedFlexBalance_kWh: " + storedFlexBalance_kWh + ", maxPowerShift_fr: " + this.maxPowerShift_fr + 
				", Energy consumed = " + this.energyUsed_kWh +" energyUsed_kWh (losses) = " + this.energyUsed_kWh;
	}

}