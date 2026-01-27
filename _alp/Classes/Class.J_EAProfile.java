/**
 * J_EAProfile
 */
public class J_EAProfile extends zero_engine.J_EA implements Serializable {
	protected J_ProfilePointer profilePointer;
	protected double profileUnitScaler_r = 4.0; // This factor translates tablefunction data in kWh/qh, normalized power or consumption-fraction into power [kW]. To go from kWh/qh to kW, that is a factor 4.
	protected OL_EnergyCarriers energyCarrier; // = OL_EnergyCarriers.ELECTRICITY;
	//public double[] a_energyProfile_kWh;
	//private double profileTimestep_h;
    //private double profileStarTime_h = 0;
	protected double lostLoad_kWh = 0;
	protected double profileScaling_fr = 1; // This factor can be used to change the magnitude of the profile in this asset, for example when an energy-saving slider is operated.
	//private boolean enableProfileLooping = true;
	
    /**
     * Default constructor
     */
    public J_EAProfile() {
    }

    /**
     * Constructor initializing the fields
     */
    /*public J_EAProfile(Agent parentAgent, OL_EnergyCarriers energyCarrier, double[] profile_kWh, OL_AssetFlowCategories assetCategory, double profileTimestep_h, double startTime_h) {
    	this(parentAgent, energyCarrier, profile_kWh, assetCategory, profileTimestep_h);
    	this.profileStarTime_h = startTime_h;
    }*/

    public J_EAProfile(Agent parentAgent, OL_EnergyCarriers energyCarrier, J_ProfilePointer profile, OL_AssetFlowCategories assetCategory, double timeStep_h) {
	    this.parentAgent= parentAgent;
	    this.energyCarrier = energyCarrier;
		if (profile == null) {
			throw new RuntimeException("Cannot create J_EAProfile without a valid ProfilePointer!");
		} else {
			profilePointer = profile;
		}	
	    this.assetFlowCategory = assetCategory;

	   	this.timestep_h = timeStep_h;
	    
	    this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
	    
		registerEnergyAsset();
	}
    
    
    /*public J_EAProfile(Agent parentAgent, OL_EnergyCarriers energyCarrier, double[] profile_kWh, OL_AssetFlowCategories assetCategory, double profileTimestep_h) {
	    this.parentAgent= parentAgent;
	    this.energyCarrier = energyCarrier;
	    this.a_energyProfile_kWh = profile_kWh;
	    //this.profileType = profileType;
	    this.profileTimestep_h = profileTimestep_h;
	    this.assetFlowCategory = assetCategory;

	    if (parentAgent instanceof GridConnection) {
	    	this.timestep_h = ((GridConnection)parentAgent).energyModel.p_timeStep_h;
	    } else {
	    	this.timestep_h = profileTimestep_h;
	    }
	    
	    this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
	    
		registerEnergyAsset();
	}
    
    public void setStartTime_h(double startTime_h) {    	
    	this.profileStarTime_h = startTime_h;
    }*/

    @Override
    public void f_updateAllFlows(double powerFraction_fr) {
    	throw new RuntimeException("J_EAProfile.f_updateAllFlows(powerFraction_fr) not supperted for J_EAProfile! Use J_EAProfile.f_updateProfileFlows(t_h) instead!");
    }
    
    public void f_updateAllFlows() {
    	//operate(time_h-this.profileStarTime_h);    	
    	double profileValue = profilePointer.getCurrentValue();		
    	
    	double currentPower_kW = profileValue * this.profileUnitScaler_r * this.profileScaling_fr;
		
    	this.energyUse_kW = currentPower_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;

		flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, currentPower_kW);
		}
    	
		//this.operate(ratioOfCapacity);
		if (currentPower_kW==0.0) { // Skip when there is no consumption -> saves time?
			if (parentAgent instanceof GridConnection) {    		
	    		//((GridConnection)parentAgent).f_addFlows(arr, this);
	    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
	    	}

		}
		this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }
    
	@Override
	public void operate(double ratioOfCapacity) {
		/*
    	double currentPower_kW = ratioOfCapacity * this.yearlyDemand_kWh * this.consumptionScaling_fr;
		
    	this.energyUse_kW = currentPower_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;

		flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, currentPower_kW);
		}*/
   	}
    
    /* Old J_EAProfile implementation
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
    	this.energyUse_kW = currentPower_kW;
		this.energyUsed_kWh += timestep_h * energyUse_kW; 
		this.flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			this.assetFlowsMap.put(this.assetFlowCategory, currentPower_kW);
		}
    } */

	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}

    public void curtailElectricityConsumption(double curtailmentSetpoint_kW) {
    	double currentElectricityConsumption_kW = this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY);
    	double curtailmentPower_kW = max(0,min(currentElectricityConsumption_kW, curtailmentSetpoint_kW));
    	energyUsed_kWh -= curtailmentPower_kW * timestep_h;
    	lostLoad_kWh += curtailmentPower_kW * timestep_h;
    	J_FlowsMap flowsMap = new J_FlowsMap();
    	flowsMap.put(OL_EnergyCarriers.ELECTRICITY, curtailmentPower_kW);    	
    	J_ValueMap<OL_AssetFlowCategories> assetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
    	assetFlows_kW.put(this.assetFlowCategory, curtailmentPower_kW);
    	
    	this.energyUse_kW = -curtailmentPower_kW;

    	this.lastFlowsMap.put(OL_EnergyCarriers.ELECTRICITY, this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY) - curtailmentPower_kW);
    	this.lastEnergyUse_kW -= curtailmentPower_kW;

    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_removeFlows(flowsMap, this.energyUse_kW, assetFlows_kW, this);
    	}
    }    
    
    public double getProfileScaling_fr() {
    	return profileScaling_fr;
    }
    
    public void setProfileScaling_fr( double scaling_fr ) {
    	this.profileScaling_fr = scaling_fr;
    }
    
    public OL_EnergyCarriers getEnergyCarrier() {
    	return this.energyCarrier;
    }
    
	@Override
	public String toString() {
		return
			"parentAgent = " + parentAgent +", Energy consumed = " + this.energyUsed_kWh +
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " ";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
