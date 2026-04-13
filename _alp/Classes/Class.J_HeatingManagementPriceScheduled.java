/**
 * J_HeatingManagementPriceScheduled
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

public class J_HeatingManagementPriceScheduled implements I_HeatingManagement {

    private boolean isInitialized = false;
    private GridConnection gc;
    private J_TimeParameters timeParameters;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.GAS_BURNER, 
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, 
		OL_GridConnectionHeatingType.HYDROGENBURNER,
		OL_GridConnectionHeatingType.DISTRICTHEAT,
		OL_GridConnectionHeatingType.LT_DISTRICTHEAT
	);
	private OL_GridConnectionHeatingType currentHeatingType;

	private J_EABuilding building;	
    private J_EAConversion heatingAsset;
    private J_HeatingPreferences heatingPreferences;
    private J_EAProfile heatProfile_kW;
    
    private double[] schedule_kw;
    private double[] scheduleStored_kW;
    
    /**
     * Default constructor
     */
    public J_HeatingManagementPriceScheduled() {
    }

    public J_HeatingManagementPriceScheduled( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.currentHeatingType = heatingType;
    }
    

    public void manageHeating(J_TimeVariables timeVariables) {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
   	
    	// Every 24 hours, at midnight, we make a schedule
    	double timeOfDay_h = timeVariables.getTimeOfDay_h();
    	
    	if (timeOfDay_h == 0) {
    		this.schedule_kw = new double[(int) (24.0 / this.timeParameters.getTimeStep_h())];
    		
    		double heatDemandForNext24Hours_kWh; // Thermal energy
	    	
	    	if (this.building != null) {
	    		double averageTemperatureForNext24Hours_degC = gc.energyModel.pf_ambientTemperature_degC.getForecast(); 
	    		double currentBuildingTemperature_degC = this.building.getCurrentTemperature(); 
	    		
	    	}
	    	else {
	    		
	    	}
    	
    	}
    	   	
    	double heatingAssetPower_kW = 0;
    	
    	// ...
    	
    }
         
    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	List<J_EAProduction> ptAssets = findAll(gc.c_productionAssets, ea -> ea.energyAssetType == OL_EnergyAssetType.PHOTOTHERMAL);
    	if (ptAssets.size() > 0) {
        	throw new RuntimeException(this.getClass() + " does not support PT.");
        }
    	if (gc.p_heatBuffer != null) {
        	throw new RuntimeException(this.getClass() + " does not support a heatbuffer.");
        }
    	if (gc.p_parentNodeHeatID != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat grids.");
    	}
    	if(gc.p_BuildingThermalAsset != null) {
        	this.building = gc.p_BuildingThermalAsset;
        	if (gc.energyModel.pf_ambientTemperature_degC.getForecastTime_h() != 24) {
    			throw new RuntimeException(this.getClass() + " expected the forecast time of the ambient temperature to be 24 hours.");
    		}
        	if(this.heatingPreferences == null) {
        		this.heatingPreferences = new J_HeatingPreferences();
        	}
    	}
    	J_EAProfile heatProfile = findFirst(gc.c_profileAssets, x -> x.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	if (heatProfile != null) {
    		this.heatProfile_kW = heatProfile;
    	}
    	if (heatProfile == null && this.building == null) {
    		throw new RuntimeException(this.getClass() + " requires a heat demand asset.");
    	}
    	if (gc.c_heatingAssets.size() == 0) {
    		throw new RuntimeException(this.getClass() + " requires at least one heating asset.");
    	}
    	if (gc.c_heatingAssets.size() > 1) {
    		throw new RuntimeException(this.getClass() + " does not support more than one heating asset.");
    	}
    	this.heatingAsset = gc.c_heatingAssets.get(0);
    	
    	if (this.heatingAsset instanceof J_EAConversionGasBurner) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.GAS_BURNER;
    	} else if (heatingAsset instanceof J_EAConversionHeatPump) {
    		this.currentHeatingType = OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP;
    	} else {
    		throw new RuntimeException(this.getClass() + " Unsupported heating asset!");    		
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

}