/**
 * J_AssetsMetaData
 */	

import java.util.EnumSet;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public class J_AssetsMetaData {
	
	public Agent parentAgent;

	public EnumSet<OL_AssetFlowCategories> activeAssetFlows = EnumSet.noneOf(OL_AssetFlowCategories.class);
		
	public Double totalInstalledPVPower_kW = 0.0;
	public Double totalInstalledWindPower_kW = 0.0;
	public Double totalInstalledBatteryStorageCapacity_MWh = 0.0;
	
	public Double initialPV_kW = 0.0;
	public Double PVPotential_kW = 0.0;
	public Double windPotential_kW = 0.0;
	
	public Map<OL_EnergyAssetType, Double> map_activeAssetsCapacity_kW;
	
	/**
     * Default constructor
     */
	public J_AssetsMetaData() {
		
	}
	
    public J_AssetsMetaData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    
    public void updateActiveAssetData(ArrayList<GridConnection> gcList) {
    	activeAssetFlows.clear();
    	for(GridConnection GC : gcList){
    		if (GC.v_isActive) {
	    		for (J_EA ea : GC.c_energyAssets) {
	    			if (ea.assetFlowCategory!=null) {
	    				activeAssetFlows.add(ea.assetFlowCategory);
	    			}
	    		}
    		}
    	}
    	if (activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) { activeAssetFlows.add(OL_AssetFlowCategories.batteriesDischargingPower_kW); }
    	if (activeAssetFlows.contains(OL_AssetFlowCategories.V2GPower_kW)) { activeAssetFlows.add(OL_AssetFlowCategories.evChargingPower_kW); }
    	
	}

    public J_AssetsMetaData getClone() {
    	J_AssetsMetaData clone = new J_AssetsMetaData(this.parentAgent);
    	clone.activeAssetFlows = this.activeAssetFlows.clone();
  
    	clone.totalInstalledPVPower_kW = this.totalInstalledPVPower_kW.doubleValue();
    	clone.totalInstalledWindPower_kW = this.totalInstalledWindPower_kW.doubleValue();
    	clone.totalInstalledBatteryStorageCapacity_MWh = this.totalInstalledBatteryStorageCapacity_MWh.doubleValue();
    	clone.PVPotential_kW = this.PVPotential_kW.doubleValue();
    	clone.windPotential_kW = this.windPotential_kW.doubleValue();
    	return clone;
    }
    
    public void saveActiveAssetAndCapacities(ArrayList<GridConnection> gcList) {
    	if(map_activeAssetsCapacity_kW != null) {
    		throw new RuntimeException("Trying to save active assets, in assetMetaData, for the second time. Not allowed.");
    	}
    	map_activeAssetsCapacity_kW = new HashMap<>();
    	for(GridConnection GC : gcList){
    		if (GC.v_isActive) {
	    		for (J_EA ea : GC.c_energyAssets) {
	    			if (ea.getEAType()!=null) {
	    				double capacityEA_kW = map_activeAssetsCapacity_kW.get(ea.getEAType()) != null ? map_activeAssetsCapacity_kW.get(ea.getEAType()) : 0;
	    				switch(ea.getEAType()) {
	    					case PHOTOVOLTAIC:
	    					case WINDMILL:
	    						capacityEA_kW = ((J_EAProduction)ea).getCapacityElectric_kW();
	    						break;
	    					case PHOTOTHERMAL:
	    						capacityEA_kW = ((J_EAProduction)ea).getCapacityHeat_kW();
	    						break;
	    					case GAS_BURNER:
	    					case HEAT_PUMP_AIR:
	    					case ELECTROLYSER:
	    						capacityEA_kW = ((J_EAConversion)ea).getInputCapacity_kW();
	    						break;
	    					case DIESEL_GENERATOR:
	    					case METHANE_GENERATOR:
	    						capacityEA_kW = ((J_EAConversion)ea).getOutputCapacity_kW();
	    						break;
	    					case STORAGE_ELECTRIC:
	    						capacityEA_kW = ((J_EAStorageElectric)ea).getCapacityElectric_kW();
	    						break;
	    				}
	    				map_activeAssetsCapacity_kW.put(ea.getEAType(), capacityEA_kW);
	    			}
	    		}
    		}
    	}
    }
    public Set<OL_EnergyAssetType> getActiveAssets() {
    	return map_activeAssetsCapacity_kW.keySet();
    }
    
    public double getActiveAssetCapacity_kW(OL_EnergyAssetType assetType) {
    	return map_activeAssetsCapacity_kW.get(assetType) != null ? map_activeAssetsCapacity_kW.get(assetType) : 0;
    }
    
	@Override
	public String toString() {
		return "totalInstalledPVPower_kW: " + totalInstalledPVPower_kW + 
				", totalInstalledWindPower_kW: " + totalInstalledWindPower_kW + 
	            ", totalInstalledBatteryStorageCapacity_MWh: " + totalInstalledBatteryStorageCapacity_MWh +
	            activeAssetFlows.toString();

	}
}