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
	
	public Double initialPV_kW;
	public Double PVPotential_kW;
	public OL_PVOrientation PVOrientation; //Default orientation of PV system
	public Double windPotential_kW;
	
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
    	clone.initialPV_kW = this.initialPV_kW != null ? this.initialPV_kW.doubleValue() : null;
    	clone.PVPotential_kW = this.PVPotential_kW != null ? this.PVPotential_kW.doubleValue() : null;
    	clone.PVOrientation = this.PVOrientation != null ? this.PVOrientation : null;
    	clone.windPotential_kW = this.windPotential_kW != null ? this.windPotential_kW.doubleValue() : null;
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