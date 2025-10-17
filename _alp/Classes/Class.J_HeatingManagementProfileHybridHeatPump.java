/**
 * J_HeatingManagementProfileHybridHeatPump
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

public class J_HeatingManagementProfileHybridHeatPump implements I_HeatingManagement {
	
	private boolean isInitialized = false;
	private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.HYBRID_HEATPUMP
	);
	private OL_GridConnectionHeatingType currentHeatingType;
	private J_EAConversionHeatPump heatPumpAsset;
	private J_EAConversionGasBurner gasBurnerAsset;
	private J_HeatingPreferences heatingPreferences;
	/**
     * Default constructor
     */
	public J_HeatingManagementProfileHybridHeatPump() {
		
	}
	
    public J_HeatingManagementProfileHybridHeatPump( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }

    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	double heatDemand_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	if (heatPumpAsset.getCOP() > 3.0 ) {
    		if (heatDemand_kW < heatPumpAsset.getOutputCapacity_kW()) {
    			heatPumpAsset.f_updateAllFlows( heatDemand_kW / heatPumpAsset.getOutputCapacity_kW() );
    			gasBurnerAsset.f_updateAllFlows( 0.0 );
    			return;
    		}
    		else if (heatDemand_kW < heatPumpAsset.getOutputCapacity_kW() + gasBurnerAsset.getOutputCapacity_kW() ) {
    			heatPumpAsset.f_updateAllFlows( 1.0 );
    			gasBurnerAsset.f_updateAllFlows( (heatDemand_kW - heatPumpAsset.getOutputCapacity_kW()) / gasBurnerAsset.getOutputCapacity_kW() );
    			return;
    		}
    		else {
    			throw new RuntimeException(this.getClass() + " in GC: " + gc.p_gridConnectionID + " does not have enough combined capacity to fulfil the heat demand");
    		}
    	}
    	else {
    		if (heatDemand_kW < gasBurnerAsset.getOutputCapacity_kW()) {
    			gasBurnerAsset.f_updateAllFlows( heatDemand_kW / gasBurnerAsset.getOutputCapacity_kW() );
    			heatPumpAsset.f_updateAllFlows( 0.0 );
    			return;
    		}
    		else if (heatDemand_kW < gasBurnerAsset.getOutputCapacity_kW() + heatPumpAsset.getOutputCapacity_kW() ) {
    			gasBurnerAsset.f_updateAllFlows( 1.0 );
    			heatPumpAsset.f_updateAllFlows( (heatDemand_kW - gasBurnerAsset.getOutputCapacity_kW()) / heatPumpAsset.getOutputCapacity_kW() );
    			return;
    		}
    		else {
    			throw new RuntimeException(this.getClass() + " in GC: " + gc.p_gridConnectionID + " does not have enough combined capacity to fulfil the heat demand");
    		}
    	}
    }
    
    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (gc.p_BuildingThermalAsset != null) {
    		throw new RuntimeException(this.getClass() + " does not support a building asset.");
    	}
    	J_EAConsumption heatConsumption = findFirst(gc.c_consumptionAssets, x -> x.getEAType() == OL_EnergyAssetType.HEAT_DEMAND && x.getEAType() != OL_EnergyAssetType.HOT_WATER_CONSUMPTION);
    	J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.energyCarrier == OL_EnergyCarriers.HEAT);
    	
    	if (heatProfile == null && heatConsumption == null) {
    		throw new RuntimeException(this.getClass() + " requires a HEAT_DEMAND profile.");
    	}
    	if (gc.c_heatingAssets.size() != 2) {
    		throw new RuntimeException(this.getClass() + " requires exactly two heating assets.");
    	}
    	// TODO: Add a check if the power of the asset is sufficient?
    	if (gc.c_heatingAssets.get(0) instanceof J_EAConversionGasBurner) {
    		this.gasBurnerAsset = (J_EAConversionGasBurner)gc.c_heatingAssets.get(0);
    	}
    	else if (gc.c_heatingAssets.get(1) instanceof J_EAConversionGasBurner) {
    		this.gasBurnerAsset = (J_EAConversionGasBurner)gc.c_heatingAssets.get(1);    		
    	}
    	else {
    		throw new RuntimeException(this.getClass() + " requires a Gas Burner");
    	}
    	if (gc.c_heatingAssets.get(0) instanceof J_EAConversionHeatPump) {
    		this.heatPumpAsset = (J_EAConversionHeatPump)gc.c_heatingAssets.get(0);
    	}
    	else if (gc.c_heatingAssets.get(1) instanceof J_EAConversionHeatPump) {
    		this.heatPumpAsset = (J_EAConversionHeatPump)gc.c_heatingAssets.get(1);    		
    	}
    	else {
    		throw new RuntimeException(this.getClass() + " requires a Heat Pump");
    	}
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