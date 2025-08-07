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
    
    
	@Override
	public String toString() {
		return "totalInstalledPVPower_kW: " + totalInstalledPVPower_kW + 
				", totalInstalledWindPower_kW: " + totalInstalledWindPower_kW + 
	            ", totalInstalledBatteryStorageCapacity_MWh: " + totalInstalledBatteryStorageCapacity_MWh +
	            activeAssetFlows.toString();

	}
}