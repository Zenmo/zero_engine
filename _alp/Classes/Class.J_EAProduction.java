/**
 * J_EAProduction
 */
public class J_EAProduction extends zero_engine.J_EAProfile{
	protected double totalEnergyCurtailed_kWh=0;

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
	
	
	//Setters
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
	
	
	
	//Getters
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
    public double getEnergyCurtailed_kWh() {
    	return this.totalEnergyCurtailed_kWh;
    }
    
    public J_ProfilePointer getProfilePointer() {
    	return this.profilePointer;
    }
    
    
    
    public J_FlowPacket curtailEnergyCarrierProduction(OL_EnergyCarriers curtailedEnergyCarrier, double curtailmentAmount_kW) {  // The curtailment setpoint is the requested amount of curtailment; requested reduction of production. (which may or may not be provided, depending on what the current production is)
    	
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
    	
     	J_FlowPacket flowPacket = new J_FlowPacket(curtailmentFlow, curtailedEnergyUse_kW, assetFlows_kW);
     	return flowPacket;
    }
    

    
    @Override
    public void storeStatesAndReset() {
    	this.totalEnergyCurtailed_kWh = 0;
    	super.storeStatesAndReset();
	}
	
	@Override
	public String toString() {
		return  "J_EAProduction: " +
				"Owner: " + this.getOwner() + ", " +
				"Capacity_kW: " + profileUnitScaler_r + ", " +
				"EC: " + this.energyCarrier + ", " +
				"AFC: " + this.assetFlowCategory + ", " +
				"CurrentProd_kW: " + (-this.lastEnergyUse_kW);
	}
}
