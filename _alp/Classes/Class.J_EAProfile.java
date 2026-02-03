import zeroPackage.ZeroMath;
/**
 * J_EAProfile
 */
public class J_EAProfile extends zero_engine.J_EAFixed implements Serializable {
	protected J_ProfilePointer profilePointer;
	protected double profileUnitScaler_r = 4.0; // This factor translates tablefunction data in kWh/qh, normalized power or consumption-fraction into power [kW]. To go from kWh/qh to kW, that is a factor 4.
	protected OL_EnergyCarriers energyCarrier; // = OL_EnergyCarriers.ELECTRICITY;
	protected double lostLoad_kWh = 0;
	protected double profileScaling_fr = 1.0; // This factor can be used to change the magnitude of the profile in this asset, for example when an energy-saving slider is operated.
	protected double signScaler_r = 1.0;

    /**
     * Default constructor
     */
    public J_EAProfile() {
    }

    /**
     * Constructor initializing the fields
     */

    public J_EAProfile(I_AssetOwner owner, OL_EnergyCarriers energyCarrier, J_ProfilePointer profile, OL_AssetFlowCategories assetCategory, J_TimeParameters timeParameters) {
	    this.setOwner(owner);
	    this.timeParameters = timeParameters;
	    this.energyCarrier = energyCarrier;
		if (profile == null) {
			throw new RuntimeException("Cannot create J_EAProfile without a valid ProfilePointer!");
		} else {
			profilePointer = profile;
			if (profilePointer.getProfileUnits() == OL_ProfileUnits.KWHPQUARTERHOUR) {
				profileUnitScaler_r = 4.0;
			} else if (profilePointer.getProfileUnits() == OL_ProfileUnits.KW) {
				profileUnitScaler_r = 1.0;				
			} else {
				throw new RuntimeException("Unsupported ProfileUnits of profilePointer for J_EAProfile!");
			}
		}	
	    this.assetFlowCategory = assetCategory;
	    
	    this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
	    
		registerEnergyAsset(timeParameters);
	}
       
    public J_FlowPacket f_updateAllFlows(J_TimeVariables timeVariables) {
    	double profileValue = profilePointer.getCurrentValue();		
    	double currentPower_kW = profileValue * this.profileUnitScaler_r * this.profileScaling_fr * this.signScaler_r;
		
    	this.energyUse_kW = currentPower_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timeParameters.getTimeStep_h();

		flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, Math.abs(currentPower_kW));
		}
     	J_FlowsMap flowsMapCopy = new J_FlowsMap();    	
     	J_ValueMap assetFlowsMapCopy = new J_ValueMap(OL_AssetFlowCategories.class);
     	J_FlowPacket flowPacket = new J_FlowPacket(flowsMapCopy.cloneMap(this.flowsMap), this.energyUse_kW, assetFlowsMapCopy.cloneMap(this.assetFlowsMap));
		this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	return flowPacket;
    }
    
	@Override
	public void operate(J_TimeVariables timeVariables) {
		throw new RuntimeException("J_EAProfile method operate() is not used!");
   	}
    

    public J_FlowPacket curtailElectricityConsumption(double curtailmentSetpoint_kW) {
    	double currentElectricityConsumption_kW = max(0,this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY));
    	double curtailmentPower_kW = max(0,min(currentElectricityConsumption_kW, curtailmentSetpoint_kW));
    	energyUsed_kWh -= curtailmentPower_kW * this.timeParameters.getTimeStep_h();
    	lostLoad_kWh += curtailmentPower_kW * this.timeParameters.getTimeStep_h();
    	J_FlowsMap flowsMap = new J_FlowsMap();
    	flowsMap.put(OL_EnergyCarriers.ELECTRICITY, curtailmentPower_kW);    	
    	J_ValueMap<OL_AssetFlowCategories> assetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
    	assetFlows_kW.put(this.assetFlowCategory, curtailmentPower_kW);
    	
    	this.lastFlowsMap.put(OL_EnergyCarriers.ELECTRICITY, this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY) - curtailmentPower_kW);
    	this.lastEnergyUse_kW -= curtailmentPower_kW;

    	//gc.f_removeFlows(flowsMap, this.energyUse_kW, assetFlows_kW, this);    	    
    	J_FlowPacket flowPacket = new J_FlowPacket(flowsMap, curtailmentPower_kW, assetFlows_kW);
     	return flowPacket;

    }    
    
    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    
    public double getProfileUnitScaler_fr() {
    	return this.profileUnitScaler_r;
    }
    
    public double getProfileScaling_fr() {
    	return this.profileScaling_fr;
    }
    
    public void setProfileScaling_fr( double scaling_fr ) {
    	this.profileScaling_fr = scaling_fr;
    }
    
    public double getPeakConsumptionPower_kW() {
    	if (this.signScaler_r < 0) {
    		return 0.0;
	    } else {
	    	return max(profilePointer.getAllValues()) * this.profileUnitScaler_r * this.profileScaling_fr;
	    }
    }
    
    public double getPeakProductionPower_kW() {
    	if (this.signScaler_r > 0) {
    		return 0.0;
	    } else {
	    	return max(profilePointer.getAllValues()) * this.profileUnitScaler_r * this.profileScaling_fr;
	    }
    }
    
    public double getBaseConsumption_kWh() {
    	if (this.signScaler_r < 0) {
    		return 0.0;
	    } else {
	    	double[] values = profilePointer.getAllValues();
	    	double dataTimeStep_h = profilePointer.getDataTimeStep_h(); 
	    	double baseConsumption_kWh = ZeroMath.arraySumPos(values) * dataTimeStep_h * this.profileUnitScaler_r;
	    	return baseConsumption_kWh;
	    }
    }
    
    public double getTotalConsumption_kWh() {
    	return this.getBaseConsumption_kWh() * this.profileScaling_fr;
    }
    
    public OL_EnergyCarriers getEnergyCarrier() {
    	return this.energyCarrier;
    }
    
	@Override
	public String toString() {
		return
			"owner = " + this.getOwner() +", Energy consumed = " + this.energyUsed_kWh +
			"assetFlowCategory = " + this.assetFlowCategory + " ";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
