/**
 * J_HeatingManagementDistrictHeating
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
public class J_HeatingManagementDistrictHeating implements I_HeatingManagement {

	private boolean isInitialized = false;
	private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.GAS_BURNER,
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, 
		OL_GridConnectionHeatingType.HYDROGENBURNER
	);
	private OL_GridConnectionHeatingType currentHeatingType;
	private J_EAConversion heatingAsset;
	private J_HeatingPreferences heatingPreferences = null; //Not needed for the GCDistrictHeating.
	
	private double previousHeatFeedin_kW = 0;
	
	
	//Stored
	private double storedPreviousHeatFeedin_kW;
	
	/**
     * Default constructor
     */
	public J_HeatingManagementDistrictHeating() {
		
	}
	
    public J_HeatingManagementDistrictHeating( GridConnection gc, OL_GridConnectionHeatingType heatingType ) {
    	if (!(gc instanceof GCDistrictHeating)) {
    		throw new RuntimeException("Impossible to connect " + this.getClass() + " to a GC that is not GCDistrictHeating");
    	}
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
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
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
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
    
    
    //Store and reset states
	public void storeStatesAndReset() {
		this.storedPreviousHeatFeedin_kW = this.previousHeatFeedin_kW;
		this.previousHeatFeedin_kW = 0;
	}
	public void restoreStates() {
		this.previousHeatFeedin_kW = this.storedPreviousHeatFeedin_kW;
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