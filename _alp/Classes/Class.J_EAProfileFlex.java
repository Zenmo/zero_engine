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
	
	//Flex parameters
	private Agent target; // The target its net balance is used as a trigger for the shifting.
	private double maxPowerShift_fr = 0.1; // How much energy can be shifted each timestep. 10% means 10 pct of the normal energy consumption of that timestep can be reduced/increased.
	private double loadShiftingMaxDuration_hr = 24; // How fast the used flex energy should be compensated for.
    private double targetLimitTolerance_fr = 0.8; // If capacity of node is 100 kW, the shifting should start at 80 kW.
	private double storedFlexBalance_kWh = 0; //Stored flex balance (energy buffer state)
	private double[] compensatedShiftingIntervalHistory_kW; // Flex balance of last flex interval duration, if it gets higher then the storedFlexBalance_kWh something is wrong.
    
	//Low pass filter
	private double filterTimeScale_h = 5*24;
    private double filterDiffGain_r;
    private double targetLoadLowPassed_kW = 0.5;

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
    	currentPower_kW = shiftCurrentPower_kW(currentPower_kW);
    	
    	this.energyUse_kW = currentPower_kW;
		this.energyUsed_kWh += timestep_h * energyUse_kW; 
		this.flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			this.assetFlowsMap.put(this.assetFlowCategory, currentPower_kW);
		}
    }
    
    private double shiftCurrentPower_kW(double currentPower_kW) {
    	
    	//Check if the flex profile management meets the constraints
    	if(compensatedShiftingIntervalHistory_kW[compensatedShiftingIntervalHistory_kW.length - 1] != 0) {
    		throw new RuntimeException("Flex profile has not compensated the shifted load within the set maximum timelimit");
    	}

    	//Determine the flex load requirement based on the target current load (with timestep delay)
    	double currentTargetLoad_kW = 0;
    	double currentTargetLoadWithoutParentGCLoad_kW = 0;
    	double currentTargetConnectionLimitDelivery_kW = 0;
    	double currentTargetConnectionLimitFeedin_kW = 0;

    	if(target instanceof GridNode gn) {
    		currentTargetLoad_kW = gn.v_currentLoad_kW;
    		currentTargetConnectionLimitDelivery_kW = gn.p_capacity_kW;
    		currentTargetConnectionLimitFeedin_kW = gn.p_capacity_kW;
    		
    		if(gn.f_getLowerLVLConnectedGridNodes().contains(((GridConnection) parentAgent).p_parentNodeElectric)) {
    			currentTargetLoadWithoutParentGCLoad_kW =  currentTargetLoad_kW - ((GridConnection) parentAgent).v_previousPowerElectricity_kW;
    		}
    		else {
    			currentTargetLoadWithoutParentGCLoad_kW =  currentTargetLoad_kW;
    		}
    	}
    	if(target instanceof GridConnection gc) {
    		currentTargetLoad_kW = gc.fm_currentBalanceFlows_kW.get(this.energyCarrier);
    		currentTargetConnectionLimitDelivery_kW = gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    		currentTargetConnectionLimitFeedin_kW = gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    		
    		if(target == parentAgent) {
    			currentTargetLoadWithoutParentGCLoad_kW = currentTargetLoad_kW; // Should shift based on parent load average, which it is doing.
    		}
    		else {
    			currentTargetLoadWithoutParentGCLoad_kW = currentTargetLoad_kW;
    		}
    	}
    	if(target instanceof EnergyCoop energyCoop) {
    		currentTargetLoad_kW = energyCoop.fm_currentBalanceFlows_kW.get(this.energyCarrier);
    		currentTargetConnectionLimitDelivery_kW = energyCoop.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    		currentTargetConnectionLimitFeedin_kW = energyCoop.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    		
    		if(energyCoop.f_getAllChildMemberGridConnections().contains(parentAgent)) {
    			currentTargetLoadWithoutParentGCLoad_kW = currentTargetLoad_kW - ((GridConnection) parentAgent).v_previousPowerElectricity_kW; 
    		}
    		else {
    			currentTargetLoadWithoutParentGCLoad_kW = currentTargetLoad_kW; //
    		}
    	}
    	if(target instanceof EnergyModel energyModel) {
    		currentTargetLoad_kW = energyModel.fm_currentBalanceFlows_kW.get(this.energyCarrier);
    		currentTargetConnectionLimitDelivery_kW = energyModel.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    		currentTargetConnectionLimitFeedin_kW = energyModel.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    		
    		currentTargetLoadWithoutParentGCLoad_kW = currentTargetLoad_kW - ((GridConnection) parentAgent).v_previousPowerElectricity_kW; 
    	}
    	    	
    	//update the lowpass filter
    	this.targetLoadLowPassed_kW += (currentTargetLoadWithoutParentGCLoad_kW - targetLoadLowPassed_kW) * filterDiffGain_r;
    	
    	
    	//Constraint: At the end of the simulation year should the storedFlexBalance_kWh be zero. For now to make that happen does the shifting not occur in the final interval duration, only to compensate the storedFlexBalance_kWh;
    	boolean inFinalInterval = ((GridConnection)this.parentAgent).energyModel.t_h >= ((GridConnection)this.parentAgent).energyModel.p_runEndTime_h - loadShiftingMaxDuration_hr;
    	
    	//Determine the required flex load
    	double flexLoad_kW = 0;

    	if(currentTargetLoadWithoutParentGCLoad_kW > targetLoadLowPassed_kW && currentTargetLoadWithoutParentGCLoad_kW > currentTargetConnectionLimitDelivery_kW * targetLimitTolerance_fr && !inFinalInterval) {
    		flexLoad_kW = currentTargetLoadWithoutParentGCLoad_kW - targetLoadLowPassed_kW;
    	}
    	else if(currentTargetLoadWithoutParentGCLoad_kW < targetLoadLowPassed_kW && currentTargetLoadWithoutParentGCLoad_kW < -currentTargetConnectionLimitFeedin_kW * targetLimitTolerance_fr && !inFinalInterval) {
    		flexLoad_kW = targetLoadLowPassed_kW - currentTargetLoadWithoutParentGCLoad_kW;
    	}
    	else if(currentTargetLoadWithoutParentGCLoad_kW < targetLoadLowPassed_kW) {
    		flexLoad_kW = min( (targetLoadLowPassed_kW - currentTargetLoadWithoutParentGCLoad_kW), max(0, storedFlexBalance_kWh/this.timeStep_h));
    	} 
    	else if(currentTargetLoadWithoutParentGCLoad_kW > targetLoadLowPassed_kW) {
    		flexLoad_kW = - min( (currentTargetLoadWithoutParentGCLoad_kW - targetLoadLowPassed_kW), max(0, -storedFlexBalance_kWh/this.timeStep_h));
    	}
   	
    	// Cap the flex load to the set max power shift fraction
    	flexLoad_kW = max(- currentPower_kW * this.maxPowerShift_fr, min(flexLoad_kW, currentPower_kW * this.maxPowerShift_fr)); 
    	
    	//Compensate the flex interval history
    	double flexLoadCompensation_kW = flexLoad_kW;
    	for (int i = compensatedShiftingIntervalHistory_kW.length - 1; i > 0; i--) {
    		if(flexLoad_kW > 0 && compensatedShiftingIntervalHistory_kW[i] < 0) {
    			double partialFlexLoadCompensation_kW = min(flexLoadCompensation_kW, -compensatedShiftingIntervalHistory_kW[i]);
    			compensatedShiftingIntervalHistory_kW[i] += partialFlexLoadCompensation_kW;
    			flexLoadCompensation_kW -= partialFlexLoadCompensation_kW;
    		}
    		if(flexLoad_kW < 0 && compensatedShiftingIntervalHistory_kW[i] > 0) {
    			double partialFlexLoadCompensation_kW = min(flexLoadCompensation_kW, -compensatedShiftingIntervalHistory_kW[i]);
    			compensatedShiftingIntervalHistory_kW[i] -= partialFlexLoadCompensation_kW;
    			flexLoadCompensation_kW += partialFlexLoadCompensation_kW;
    		}
    		
    		if(flexLoadCompensation_kW == 0) {
    			break;
    		}
    	} 	
    	
    	
    	//Update the flex interval history and stored flex balance
    	for (int j = compensatedShiftingIntervalHistory_kW.length - 1; j > 0; j--) {
    		compensatedShiftingIntervalHistory_kW[j] = compensatedShiftingIntervalHistory_kW[j - 1];
    	}
   
    	compensatedShiftingIntervalHistory_kW[0] = flexLoad_kW;
    	storedFlexBalance_kWh += flexLoad_kW*this.timeStep_h;
    	
    	return currentPower_kW + flexLoad_kW;
    }
    
	@Override
	public String toString() {
		return 	"parentAgent = " + parentAgent +", Energy consumed = " + this.energyUsed_kWh +
				"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " ";
	}

}