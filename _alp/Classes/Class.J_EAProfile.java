/**
 * J_EAProfile
 */
public class J_EAProfile extends zero_engine.J_EAFixed implements Serializable {

	public OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	public double[] a_energyProfile_kWh;
	private double profileTimestep_h;
    private double profileStarTime_h = 0;
	public double lostLoad_kWh = 0;
	private double profileScaling_fr = 1;
	private boolean enableProfileLooping = true;
	
    /**
     * Default constructor
     */
    public J_EAProfile() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAProfile(I_AssetOwner owner, OL_EnergyCarriers energyCarrier, double[] profile_kWh, OL_AssetFlowCategories assetCategory, double profileTimestep_h) {
		this.setOwner(owner);
	    this.energyCarrier = energyCarrier;
	    this.a_energyProfile_kWh = profile_kWh;
	    //this.profileType = profileType;
	    this.profileTimestep_h = profileTimestep_h;
	    this.assetFlowCategory = assetCategory;

	    this.timestep_h = profileTimestep_h;
	    
	    this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
	    
		registerEnergyAsset();
	}
    
    public void setStartTime_h(double startTime_h) {    	
    	this.profileStarTime_h = startTime_h;
    }
    
    @Override
    public void operate(J_TimeVariables timeVariables) {
    	double time_h = timeVariables.getT_h()-this.profileStarTime_h;
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
    }

    public void curtailElectricityConsumption(double curtailmentSetpoint_kW, GridConnection gc) {
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

    	gc.f_removeFlows(flowsMap, this.energyUse_kW, assetFlows_kW, this);    	
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
			"Energy consumed = " + this.energyUsed_kWh +
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " ";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
