/**
 * J_ActiveAssetsData
 */	

import java.util.EnumSet;

public class J_ActiveAssetData {
	
	public Agent parentAgent;
	
	public EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers;
	
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
	public double totalInstalledWindPower_kW = 0;
	public double totalInstalledPVPower_kW = 0;
	public double totalInstalledBatteryStorageCapacity_MWh = 0;
    /**
     * Default constructor
     */
    public J_ActiveAssetData(Agent parentAgent, EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers, EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers) {
    	this.parentAgent = parentAgent;
    	this.activeConsumptionEnergyCarriers = activeConsumptionEnergyCarriers;
    	this.activeProductionEnergyCarriers = activeProductionEnergyCarriers;
    }
    
    public void updateActiveAssetData(ArrayList<GridConnection> gcList) {
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
	    this.totalInstalledWindPower_kW = 0;
	    this.totalInstalledPVPower_kW = 0;
	    this.totalInstalledBatteryStorageCapacity_MWh = 0;
	
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

	@Override
	public String toString() {
		return super.toString();
	}

}