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

	public Double totalInstalledPVPower_kW;
	public Double totalInstalledWindPower_kW;
	public Double totalInstalledBatteryStorageCapacity_MWh;
	
	public Double PVPotential_kW;
	public Double windPotential_kW;
	
	/**
     * Default constructor
     */
    public J_AssetsMetaData(Agent parentAgent) {
    	this.parentAgent = parentAgent;   	
    	/*
    	ArrayList<GridConnection> gcList = new ArrayList<>();    	
    	if (parentAgent instanceof GridConnection) {
    	    this.totalInstalledPVPower_kW = ((GridConnection)parentAgent).v_totalInstalledPVPower_kW;
    		this.totalInstalledWindPower_kW = ((GridConnection)parentAgent).v_totalInstalledWindPower_kW;
    	    this.totalInstalledBatteryStorageCapacity_MWh = ((GridConnection)parentAgent).v_totalInstalledBatteryStorageCapacity_MWh;    		
    	    gcList.add((GridConnection)parentAgent);
    	}
    	else if (parentAgent instanceof EnergyCoop) {
    	    this.totalInstalledPVPower_kW = ((EnergyCoop)parentAgent).v_totalInstalledPVPower_kW;
    		this.totalInstalledWindPower_kW = ((EnergyCoop)parentAgent).v_totalInstalledWindPower_kW;
    	    this.totalInstalledBatteryStorageCapacity_MWh = ((EnergyCoop)parentAgent).v_totalInstalledBatteryStorageCapacity_MWh;    	    		
    	    gcList.addAll(((EnergyCoop)parentAgent).f_getAllChildMemberGridConnections());
    	}
    	else if (parentAgent instanceof EnergyModel) {
    	    this.totalInstalledPVPower_kW = ((EnergyModel)parentAgent).v_totalInstalledPVPower_kW;
    		this.totalInstalledWindPower_kW = ((EnergyModel)parentAgent).v_totalInstalledWindPower_kW;
    	    this.totalInstalledBatteryStorageCapacity_MWh = ((EnergyModel)parentAgent).v_totalInstalledBatteryStorageCapacity_MWh;    	    		
    	    gcList.addAll(((EnergyModel)parentAgent).f_getGridConnections());
    	}
    	else {
    		throw new RuntimeException("Are you sure you meant to create an assetsmetadata class with parentagent " + parentAgent + " ?");
    	}
    	this.updateActiveAssetData(gcList);
    	 */
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