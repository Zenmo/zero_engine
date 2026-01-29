/**
 * J_EAProduction
 */
public class J_EAProduction extends zero_engine.J_EAProfile implements Serializable {
	//protected J_ProfilePointer profilePointer;
	//protected OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	protected double totalEnergyCurtailed_kWh=0;
	//protected double outputTemperature_degC;
	//protected double capacity_kW;

    /**
     * Default constructor
     */
	public J_EAProduction() {
    }

	/**
     * Constructor initializing the fields
     */
	public J_EAProduction(I_AssetOwner owner, OL_EnergyAssetType type, String name, OL_EnergyCarriers energyCarrier, double capacity_kW, J_TimeParameters timeParameters, J_ProfilePointer profile) {
		this.setOwner(owner);
	    this.timeParameters = timeParameters;
	    this.energyAssetType = type;
	    this.energyAssetName = name;
	    this.energyCarrier = energyCarrier;
	    //this.capacity_kW = capacity_kW;
	    this.signScaler_r = -1.0;
		if (profile.getProfileUnits() == OL_ProfileUnits.NORMALIZEDPOWER) {
			this.profileUnitScaler_r = capacity_kW;
			this.profilePointer = profile;
		} else {
			throw new RuntimeException("Invalid OL_ProfileUnits type for J_EAProduction!");
		}
	    
	    if (type == OL_EnergyAssetType.PHOTOVOLTAIC) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.pvProductionElectric_kW;
	    } else if (type == OL_EnergyAssetType.WINDMILL) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.windProductionElectric_kW;
	    } else if (type == OL_EnergyAssetType.PHOTOTHERMAL) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.ptProductionHeat_kW;
	    } else if (type == OL_EnergyAssetType.GAS_BURNER) {
	    	this.assetFlowCategory = OL_AssetFlowCategories.CHPProductionElectric_kW;
	    } else {
	    	throw new RuntimeException("No valid OL_EnergyAssetType, cannot assign AssetFlowCategory!");
	    }

	    this.activeProductionEnergyCarriers.add(this.energyCarrier);
		registerEnergyAsset(timeParameters);
	}
	
	public void setCapacityElectric_kW(double capacityElectric_kW, GridConnection gc) {
		// Calculate the difference with the set and the previous capacity to update totals in GC, GN and EnergyModel
		if (energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			double difference_kW = capacityElectric_kW - this.profileUnitScaler_r;
			if (this.energyAssetType == OL_EnergyAssetType.WINDMILL) {		
				gc.v_liveAssetsMetaData.totalInstalledWindPower_kW += difference_kW;
				if (gc.p_parentNodeElectric != null) {
					gc.p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, difference_kW, true);
				}
				gc.c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledWindPower_kW += difference_kW);
				gc.energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW += difference_kW;
			}
			else if (this.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC) {
				gc.v_liveAssetsMetaData.totalInstalledPVPower_kW += difference_kW;
				if (gc.p_parentNodeElectric != null) {
					gc.p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, difference_kW, true);
				}
				gc.c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledPVPower_kW += difference_kW);				
				gc.energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW += difference_kW;
			}
	
			this.profileUnitScaler_r = capacityElectric_kW;
			
		} else {			
			throw new RuntimeException("Production assets energy carrier is not electricity!");
		}
	}
	
	public double getCapacityElectric_kW() {
		if (energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			return this.profileUnitScaler_r;
		} else {			
			throw new RuntimeException("J_EAProduction is not electric!");
		}
	}
	
	public double getCapacityHeat_kW() {
		if (energyCarrier == OL_EnergyCarriers.HEAT) {
			return profileUnitScaler_r;			
		} else {			
			throw new RuntimeException("J_EAProduction is not thermal!");
		}
	}
	
	public String getName() {
		return this.energyAssetName;
	}
	
	@Override
    public void operate(J_TimeVariables timeVariables) {	
		/*
		ratioOfCapacity = profilePointer.getCurrentValue();
		
		//if (ratioOfCapacity>0.0) { // Skip when there is no production -> saves time?
			double currentProduction_kW = ratioOfCapacity * this.capacity_kW;
			
	    	this.energyUse_kW = -currentProduction_kW;
	    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h; 	    	    	
	       	this.flowsMap.put(this.energyCarrier, -currentProduction_kW);
	    	this.assetFlowsMap.put(this.assetFlowCategory, currentProduction_kW);
		//}
	    throw new RuntimeException("J_EAProduction operate override is called!");
	    */
	}
	
	/*
    @Override
	public void f_updateAllFlows(double v_powerFraction_fr) {
		throw new RuntimeException("J_EAProduction.f_updateAllFlows() should be called without arguments!");
	}
	
	public void f_updateAllFlows() {
		double ratioOfCapacity = profilePointer.getCurrentValue();
		
		if (ratioOfCapacity>0.0) { // Skip when there is no production -> saves time?
			double currentProduction_kW = ratioOfCapacity * this.capacity_kW;
			
	    	this.energyUse_kW = -currentProduction_kW;
	    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h; 	    	    	
	       	this.flowsMap.put(this.energyCarrier, -currentProduction_kW);
	       	this.assetFlowsMap.put(this.assetFlowCategory, currentProduction_kW);
	       	if (parentAgent instanceof GridConnection) {    		
	    		//((GridConnection)parentAgent).f_addFlows(arr, this);
	    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
	    	}

		}
		this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }*/
    
    public J_FlowPacket curtailEnergyCarrierProduction(OL_EnergyCarriers curtailedEnergyCarrier, double curtailmentAmount_kW, GridConnection gc) {  // The curtailment setpoint is the requested amount of curtailment; requested reduction of production. (which may or may not be provided, depending on what the current production is)
    	
    	/*if(this.energyCarrier != curtailedEnergyCarrier) {
    		//new RuntimeException("Trying to curtail the wrong a production asset with the wrong energyCarrier");
    		return 0;
    	}*/
    	
    	double currentProduction_kW = max(0,-this.lastFlowsMap.get(curtailedEnergyCarrier));
    	double curtailmentPower_kW = max(0,min(currentProduction_kW, curtailmentAmount_kW)); // Can only curtail what was produced in the first place.
    	energyUsed_kWh += curtailmentPower_kW * this.timeParameters.getTimeStep_h(); // energyUsed_kWh is negative for production assets. Curtailment makes it 'less negative', so a positive number is added to energyUsed_kWh.
    	this.totalEnergyCurtailed_kWh += curtailmentPower_kW * this.timeParameters.getTimeStep_h();
    	J_FlowsMap curtailmentFlow = new J_FlowsMap();
    	curtailmentFlow.put(curtailedEnergyCarrier, -curtailmentPower_kW); // To remove production, a negative flow must be removed. Thus this flowmap with a negative flow will be sent to GC.f_removeFlows()
    	J_ValueMap<OL_AssetFlowCategories> assetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
    	assetFlows_kW.put(this.assetFlowCategory, curtailmentPower_kW); // The assetFlows for production assets contain positive values for production. This assetFlows_kW will be handle bij GC.f_removeFlows(), so it should contain a positive number to remove production.
    	double curtailedEnergyUse_kW = -curtailmentPower_kW; // production is a negative flow, so to remove production, a negative value must be sent to GC.f_removeFlows().
    	this.lastFlowsMap.addFlow(curtailedEnergyCarrier, curtailmentPower_kW); // production is a negative flow, so to remove production, a positive value must be added to lastFlows.
    	this.lastEnergyUse_kW += curtailmentPower_kW; // production is a negative flow, so to remove production, a positive value must be added to lastEnergyUse_kW.
    	
		//gc.f_removeFlows(curtailmentFlow, curtailedEnergyUse_kW, assetFlows_kW, this);
     	J_FlowPacket flowPacket = new J_FlowPacket(curtailmentFlow, curtailedEnergyUse_kW, assetFlows_kW);
     	return flowPacket;
    }
    
    public double getEnergyCurtailed_kWh() {
    	return this.totalEnergyCurtailed_kWh;
    }
    
    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    
    @Override
    public void storeStatesAndReset() {
    	this.totalEnergyCurtailed_kWh = 0;
    	super.storeStatesAndReset();
	}
	
	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"owner = " + this.getOwner() +" " +
			"capacity_kW = " + profileUnitScaler_r +" "+
			"energyCarrier = " + energyCarrier +" "+
			"assetFlowCategory = " + this.assetFlowCategory + " " +
			"energyProduced_kWh = " + (-this.energyUsed_kWh) +  " ";
	}

	
	/*public double getCurrentTemperature() {
		return outputTemperature_degC;
	}*/
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
