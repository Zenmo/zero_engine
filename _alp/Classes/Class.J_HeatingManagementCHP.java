/**
 * J_HeatingManagementCHP
 */	
public class J_HeatingManagementCHP implements I_HeatingManagement {

    private boolean isInitialized = false;
    private GridConnection gc;
	private OL_GridConnectionHeatingType validHeatingType = OL_GridConnectionHeatingType.GAS_CHP;

	private OL_GridConnectionHeatingType currentHeatingType;

    private J_EAConversion heatingAsset;
    private J_HeatingPreferences heatingPreferences;

	/**
     * Default constructor
     */
    public J_HeatingManagementCHP() {
    	
    }
    
    public J_HeatingManagementCHP( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }
      
    
    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	
    	double heatingAssetPower_kW = heatDemand_kW;
    	
    	double CHPPowerRatioSetPoint = heatingAssetPower_kW / ((J_EAConversionGasCHP)heatingAsset).getOutputHeatCapacity_kW();
    	heatingAsset.f_updateAllFlows(CHPPowerRatioSetPoint);
    }
    
    
    public void initializeAssets() {
    	if (validHeatingType != this.currentHeatingType) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	List<J_EAProduction> ptAssets = findAll(gc.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL);
    	if (ptAssets.size() > 0) {
        	throw new RuntimeException(this.getClass() + " does not make sense to use this heating management with PT.");
    	}
    	if (gc.p_heatBuffer != null) {
        	throw new RuntimeException(this.getClass() + " does not make sense to use this heating management with heatbuffer.");
    	}
    	if(gc.p_BuildingThermalAsset != null) {
    		throw new RuntimeException(this.getClass() + " does not make sense to use this heating management with heatbuffer.");
    	}
    	J_EAConsumption heatConsumption = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND);
    	J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	if (heatProfile == null && heatConsumption == null) {
    		throw new RuntimeException(this.getClass() + " requires a heat demand profile/consumption asset.");
    	}
    	if (gc.c_heatingAssets.size() != 1) {
    		throw new RuntimeException(this.getClass() + " requires exactly one heating asset.");
    	}
    	this.heatingAsset = gc.c_heatingAssets.get(0);
    	if (heatingAsset instanceof J_EAConversionGasCHP) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.GAS_CHP;
    	} else {
    		throw new RuntimeException(this.getClass() + " Unsupported heating asset!");    		
    	}

    	this.isInitialized = true;
    }
    
    
    public void notInitialized() {
    	this.isInitialized = false;
    }
    
    public List<OL_GridConnectionHeatingType> getValidHeatingTypes() {
    	List<OL_GridConnectionHeatingType> validHeatingTypes = new ArrayList<>();
    	validHeatingTypes.add(this.validHeatingType);
    	return validHeatingTypes;
    }
    
    public OL_GridConnectionHeatingType getCurrentHeatingType() {
    	return this.currentHeatingType;
    }
    
    public void setHeatingPreferences(J_HeatingPreferences heatingPreferences) {
    	this.heatingPreferences = heatingPreferences;
    }
    
    public J_HeatingPreferences getHeatingPreferences() {
    	return this.heatingPreferences;
    }
    
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
	public void storeStatesAndReset() {
		//Nothing to store/reset
	}
	public void restoreStates() {
		//Nothing to store/reset
	}
	
	@Override
	public String toString() {
		return "HeatingManagement Simple with heating type: " + getCurrentHeatingType().toString();
	}
}