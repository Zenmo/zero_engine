/**
 * J_AssetsMetaData
 */	

import java.util.EnumSet;

public class J_AssetsMetaData {
	
	public Agent parentAgent;

	public boolean hasElectricHeating = false;
	public boolean hasElectricTransport = false;
	public boolean hasPV = false;
	public boolean hasWindturbine = false;
	public boolean hasBattery = false;
	public boolean hasHeatGridConnection = false;
	public boolean hasElectrolyser = false;
	public boolean hasCHP = false;
	public boolean hasV2G = false;
	public boolean hasElectricCooking = false;

	public Double totalInstalledPVPower_kW = 0.0;
	public Double totalInstalledWindPower_kW = 0.0;
	public Double totalInstalledBatteryStorageCapacity_MWh = 0.0;
	
	public Double PVPotential_kW = 0.0;
	public Double windPotential_kW = 0.0;
	
	/**
     * Default constructor
     */
    public J_AssetsMetaData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    
    public void updateActiveAssetData(ArrayList<GridConnection> gcList) {
    	this.totalInstalledPVPower_kW = 0.0;
    	this.totalInstalledWindPower_kW = 0.0;
    	this.totalInstalledBatteryStorageCapacity_MWh = 0.0;
    	this.hasElectricHeating = false;
	    this.hasElectricTransport = false;
	    this.hasPV = false;
	    this.hasWindturbine = false;
	    this.hasBattery = false;
	    this.hasHeatGridConnection = false;
	    this.hasElectrolyser = false;
	    this.hasCHP = false;
	    this.hasV2G = false;
	    this.hasElectricCooking = false;

	
	    //Electric heating
	    for(GridConnection GC : gcList){
	    	if(GC.c_electricHeatpumpAssets.size()>0 && GC.v_isActive){
	    		this.hasElectricHeating = true;
	    		break;
	    	}
	    }
	    //Electric vehicles
	    for(GridConnection GC : gcList){
	    	if(GC.c_EvAssets.size()>0 && GC.v_isActive){
	    		this.hasElectricTransport = true;
	    		break;
	    	}
	    }
	    //PV
	    for(GridConnection GC : gcList){
	    	if(GC.c_pvAssets.size()>0 && GC.v_isActive){
	    		for(J_EA pvAsset : GC.c_pvAssets){
	    			if(((J_EAProduction)pvAsset).getCapacityElectric_kW() > 0) {
			    		this.hasPV = true;
			    		this.totalInstalledPVPower_kW += ((J_EAProduction)pvAsset).getCapacityElectric_kW();
	    			}
	    		}
	    	}
	    }
	    //Wind
	    for(GridConnection GC : gcList){
	    	if(GC.c_windAssets.size()>0 && GC.v_isActive){
	    		for(J_EA windturbine : GC.c_windAssets){
	    			if(((J_EAProduction)windturbine).getCapacityElectric_kW() > 0) {
			    		this.hasWindturbine = true;
			    		this.totalInstalledWindPower_kW += ((J_EAProduction)windturbine).getCapacityElectric_kW();
	    			}
	    		}
	    	}
	    }
	    //Battery
	    for(GridConnection GC : gcList){
	    	if(GC.c_batteryAssets.size()>0 && GC.v_isActive){
	    		for(J_EA battery : GC.c_batteryAssets){
	    			if(((J_EAStorageElectric)battery).getStorageCapacity_kWh() > 0){
	    				this.hasBattery = true;
	    				this.totalInstalledBatteryStorageCapacity_MWh += ((J_EAStorageElectric)battery).getStorageCapacity_kWh()/1000;
	    			}
	    		}
	    	}
	    }
	    //Heat grid
	    for(GridConnection GC : gcList){
	    	if(GC.l_parentNodeHeat.getConnectedAgent() != null && GC.v_isActive){
	    		this.hasHeatGridConnection = true;
	    		break;
	    	}
	    }
	    //Electrolyser
	    for(GridConnection GC : gcList){
	    	if(GC.c_electrolyserAssets.size()>0 && GC.v_isActive){
	    		this.hasElectrolyser = true;
	    		break;
	    	}
	    }
	    //CHP
	    for(GridConnection GC : gcList){
	    	if(GC.c_chpAssets.size()>0 && GC.v_isActive){
	    		this.hasCHP = true;
	    		break;
	    	}
	    }
	    //V2g
	    for(GridConnection GC : gcList){
	    	if(GC.p_chargingAttitudeVehicles == OL_ChargingAttitude.V2G && GC.c_EvAssets.size()>0 && GC.v_isActive){
	    		this.hasV2G = true;
	    		break;
	    	}
	    }
	    //Electric cooking
	    for(GridConnection GC : gcList){
	    	if(GC.c_electricHobAssets.size()>0 && GC.v_isActive){
	    		this.hasElectricCooking = true;
	    		break;
	    	}
	    }
	}

    public J_AssetsMetaData getClone() {
    	J_AssetsMetaData clone = new J_AssetsMetaData(this.parentAgent);
    	clone.hasElectricHeating = this.hasElectricHeating;
    	clone.hasElectricTransport = this.hasElectricTransport;
    	clone.hasPV = this.hasPV;
    	clone.hasWindturbine = this.hasWindturbine;
    	clone.hasBattery = this.hasBattery;
    	clone.hasHeatGridConnection = this.hasHeatGridConnection;
    	clone.hasElectrolyser = this.hasElectrolyser;
    	clone.hasCHP = this.hasCHP;
    	clone.hasV2G = this.hasV2G;
    	clone.hasElectricCooking = this.hasElectricCooking;
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
	            ", hasElectricHeating: " + hasElectricHeating + 
	            ", hasElectricTransport: " + hasElectricTransport + 
	            ", hasWindturbine: " + hasWindturbine +
		        ", hasBattery: " + hasBattery +
		        ", hasHeatGridConnection: " + hasHeatGridConnection +
		        ", hasElectrolyser: " + hasElectrolyser +
		        ", hasCHP: " + hasCHP +
		        ", hasV2G: " + hasV2G +
		        ", hasElectricCooking: " + hasElectricCooking;
	}
}