/**
 * J_HeatingManagementProfileSimple
 */	
public class J_HeatingManagementProfileSimple implements I_HeatingManagement {

	private boolean isInitialized = false;
	private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.GAS_BURNER, 
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, 
		OL_GridConnectionHeatingType.HYDROGENBURNER,
		OL_GridConnectionHeatingType.DISTRICTHEAT,
		OL_GridConnectionHeatingType.LT_DISTRICTHEAT
	);
	private OL_GridConnectionHeatingType currentHeatingType;
	private J_EAConversion heatingAsset;

    
	/**
     * Default constructor
     */
    public J_HeatingManagementProfileSimple( GridConnection gc ) {
    	this.gc = gc;
    }

    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	heatingAsset.f_updateAllFlows( heatDemand_kW / heatingAsset.getOutputCapacity_kW() );
    }
    
    public void initializeAssets() {
    	//if (!validHeatingTypes.contains(gc.p_heatingType)) {
    		//throw new RuntimeException(this.getClass() + " does not support heating type: " + gc.p_heatingType);
    	//}
    	//if (gc.c_heatingTypes.size() != 1) {
    		//throw new RuntimeException(this.getClass() + " does not support heating type: " + gc.p_heatingType);
    	//}
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (gc.p_BuildingThermalAsset != null) {
    		throw new RuntimeException(this.getClass() + " does not support a building asset.");
    	}
    	J_EAConsumption heatProfile = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND && x.getEAType() != OL_EnergyAssetType.HOT_WATER_CONSUMPTION);
    	if (heatProfile == null) {
    		throw new RuntimeException(this.getClass() + " requires a HEAT_DEMAND profile.");
    	}
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	// TODO: Add a check if the power of the asset is sufficient?
    	// TODO: Add a check if the heatingAsset is of the correct type, e.g. not a hydrogen burner or not a CHP.
    	this.heatingAsset = gc.c_heatingAssets.get(0);
    	this.isInitialized = true;
    }
    
    public void notInitialized() {
    	this.isInitialized = false;
    }
    
    public List<OL_GridConnectionHeatingType> getValidHeatingTypes() {
    	return this.validHeatingTypes;
    }
    
    public OL_GridConnectionHeatingType getCurrentHeatingType() {
    	return this.currentHeatingType;
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