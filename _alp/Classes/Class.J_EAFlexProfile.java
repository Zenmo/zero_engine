import zeroPackage.ZeroMath;
/**
 * J_EAFlexProfile: Profile asset where the profile can be manipulated by setting the power fraction: 
 * Example: Original value 10 kW? -> With power fraction of 0.8 it will end up being 8 kW instead. Or with power fraction of 1.5, it will become 15 kW!
 * -> Not allowed to change the direction of the flow (i.e. negative power fraction is not allowed).
 */	
public class J_EAFlexProfile extends J_EAFlex{
	protected J_ProfilePointer profilePointer;
	protected double profileUnitScaler_r = 4.0; // This factor translates tablefunction data in kWh/qh, normalized power or consumption-fraction into power [kW]. To go from kWh/qh to kW, that is a factor 4.
	protected OL_EnergyCarriers energyCarrier; //
	protected double lostLoad_kWh = 0;
	protected double profileScaling_fr = 1.0; // This factor can be used to change the magnitude of the profile in this asset, for example when an energy-saving slider is operated.
	protected double signScaler_r = 1.0;

    /**
     * Empty constructor for serialization
     */
    public J_EAFlexProfile() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAFlexProfile(I_AssetOwner owner, OL_EnergyCarriers energyCarrier, J_ProfilePointer profile, OL_AssetFlowCategories assetCategory, J_TimeParameters timeParameters) {
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
    
    @Override
    public J_FlowPacket f_updateAllFlows(double powerFraction_fr, J_TimeVariables timeVariables) {
     	operate(powerFraction_fr, timeVariables); // Don't cap the power fraction limit!
     	J_FlowsMap flowsMapCopy = new J_FlowsMap();
     	J_ValueMap assetFlowsMapCopy = new J_ValueMap(OL_AssetFlowCategories.class);
     	J_FlowPacket flowPacket = new J_FlowPacket(flowsMapCopy.cloneMap(this.flowsMap), this.energyUse_kW, assetFlowsMapCopy.cloneMap(this.assetFlowsMap));
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	return flowPacket;
    }
    
	@Override
	public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
    	if(DoubleCompare.lessThanZero(powerFraction_fr)) {
			throw new RuntimeException("Impossible to operate J_EAFlexProfile asset with negative powerfraction.");    		
    	}
    	double profileValue = profilePointer.getCurrentValue();		
    	double currentPower_kW = profileValue * this.profileUnitScaler_r * this.profileScaling_fr * this.signScaler_r * powerFraction_fr;
		
    	this.energyUse_kW = currentPower_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timeParameters.getTimeStep_h();

		flowsMap.put(this.energyCarrier, currentPower_kW);		
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, Math.abs(currentPower_kW));
		}
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
    
    public double[] getDefaultForecast_kW(double forecastStartTime_h, double forecastEndTime_h) {
    	double timeWindow_h = forecastEndTime_h-forecastStartTime_h;
    	int numberOfTimeSteps = roundToInt(timeWindow_h/timeParameters.getTimeStep_h());
    	double[] forecast_kW = new double[numberOfTimeSteps];
		double scalar = this.signScaler_r * getProfileUnitScaler_fr() * getProfileScaling_fr();
		for (int i = 0; i < numberOfTimeSteps; i++) {
			forecast_kW[i] += scalar * profilePointer.getValue(forecastStartTime_h + i*timeParameters.getTimeStep_h());
		}
    	return forecast_kW;
    }
    
	@Override
	public String toString() {
		return
			"owner = " + this.getOwner() +", Energy consumed = " + this.energyUsed_kWh +
			"assetFlowCategory = " + this.assetFlowCategory + " ";
	}
}