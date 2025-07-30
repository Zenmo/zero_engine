/**
 * J_HeatingManagementDistrictHeating
 */	
public class J_HeatingManagementDistrictHeating implements Serializable {

	private boolean isInitialized = false;
	private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.GAS_BURNER,
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, 
		OL_GridConnectionHeatingType.HYDROGENBURNER
	);
	private J_EAConversion heatingAsset;
	
	private double previousHeatFeedin_kW = 0;

	/**
     * Default constructor
     */
    public J_HeatingManagementDistrictHeating( GridConnection gc ) {
    	if (!(gc instanceof GCDistrictHeating)) {
    		throw new RuntimeException("Impossible to connect " + this.getClass() + " to a GC that is not GCDistrictHeating");
    	}
    	this.gc = gc;
    }

    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	// v_currentLoad_kW is the GN load of the previous timestep
    	double heatTransferToNetwork_kW = max(0, gc.p_parentNodeHeat.v_currentLoad_kW + previousHeatFeedin_kW);
    	if (heatTransferToNetwork_kW > heatingAsset.getOutputCapacity_kW()) {
    		throw new RuntimeException("Heating asset in " + this.getClass() + " does not have sufficient capacity.");
    	}
    	heatingAsset.f_updateAllFlows( heatTransferToNetwork_kW / heatingAsset.getOutputCapacity_kW() );
    	previousHeatFeedin_kW = -gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    }
    
    public void initializeAssets() {
    	if (!validHeatingTypes.contains(gc.p_heatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + gc.p_heatingType);
    	}
    	if (gc.p_parentNodeHeat == null) {
    		throw new RuntimeException(this.getClass() + " requires the GC: " + gc.p_gridConnectionID + " to be connected to a GridNodeHeat");
    	}
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (gc.p_BuildingThermalAsset != null) {
    		throw new RuntimeException(this.getClass() + " does not support a building asset.");
    	}
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	this.heatingAsset = gc.c_heatingAssets.get(0);
    	this.isInitialized = true;
    }
    
    public void notInitialized() {
    	this.isInitialized = false;
    }
    
    public List<OL_GridConnectionHeatingType> getValidHeatingTypes() {
    	return this.validHeatingTypes;
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