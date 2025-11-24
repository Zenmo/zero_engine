/**
 * J_AggregatorEnergyManagementHPAndVehiclesSetLimit
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_AggregatorEnergyManagementHPAndVehiclesSetLimit implements I_AggregatorEnergyManagement {
	private EnergyCoop energyCoop;
	private Agent target;
	private double reducePowerSetpoint_kW = 0;
	private double increasePowerSetpoint_kW = 0;
	private double factorToAccountForTimeStepDelay = 0.99;
    /**
     * Default constructor
     */
	public J_AggregatorEnergyManagementHPAndVehiclesSetLimit( ) {

    }
	
    public J_AggregatorEnergyManagementHPAndVehiclesSetLimit( EnergyCoop energyCoop) {
    	this.energyCoop = energyCoop;
    	this.target = energyCoop;
    	this.reducePowerSetpoint_kW = energyCoop.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
    	this.increasePowerSetpoint_kW = energyCoop.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    }
    public J_AggregatorEnergyManagementHPAndVehiclesSetLimit( EnergyCoop energyCoop, Agent target) {
    	this.energyCoop = energyCoop;
    	this.target = target;
    	
    	if(target instanceof EnergyCoop coop) {
	    	this.reducePowerSetpoint_kW = coop.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
	    	this.increasePowerSetpoint_kW = -coop.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
    	}
    	else if(target instanceof GridNode GN) {
    		this.reducePowerSetpoint_kW = GN.p_capacity_kW;
	    	this.increasePowerSetpoint_kW = -GN.p_capacity_kW;
    	}
    }
    
    public void setReducePowerSetpoint_kW(double reducePowerSetpoint_kW) {
    	this.reducePowerSetpoint_kW = reducePowerSetpoint_kW;    	
    }
    public void setIncreasePowerSetpoint_kW(double increasePowerSetpoint_kW) {
    	this.increasePowerSetpoint_kW = increasePowerSetpoint_kW;
    }
    
    public void manageExternalSetpoints() {
    	//Get all members that have an asset that is put on the external setpoint mode
    	List<GridConnection> memberedGCWithSetpointEVManagement = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.c_electricVehicles.size() > 0 && GC.f_getChargingManagement() != null && GC.f_getChargingManagement() instanceof J_ChargingManagementExternalSetpoint);
    	List<GridConnection> memberedGCWithSetpointEVManagementCharger = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.c_chargers.size() > 0 && GC.f_getChargingManagement() != null && GC.f_getChargingManagement() instanceof J_ChargingManagementExternalSetpoint);
    	List<GridConnection> memberedGCWithSetpointHeatpumps = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && GC.f_getHeatingManagement() != null && GC.f_getHeatingManagement() instanceof J_HeatingManagementExternalSetpoint);
    	
		double collectiveChargeSetpoint_kW = 0;
		int totalChargingEVs = 0;
		double totalMaxChargePowerEVs_kW = 0;
		double totalMaxV2GPowerEVs_kW = 0;
		
		int totalNumberOfChargingSockets = 0;
		double totalMaxChargePowerChargers_kW = 0;
		double totalMaxV2GPowerChargers_kW = 0;
		
		double currentEVPower_kW = 0;
		double currentHPPower_kW = 0;
		for(GridConnection GC : energyCoop.f_getMemberGridConnectionsCollectionPointer()) {
			if(memberedGCWithSetpointEVManagement.contains(GC)) {
				for(J_EAEV ev : GC.c_electricVehicles) {
					if(ev.available) {
						totalMaxChargePowerEVs_kW += ev.getCapacityElectric_kW();
						if(ev.getV2GActive() && ev.getV2GCapable()) {
							totalMaxV2GPowerEVs_kW += ev.getCapacityElectric_kW();
						}
					}
				}
				currentEVPower_kW += GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.evChargingPower_kW) - GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.V2GPower_kW);	
			}
			if(memberedGCWithSetpointEVManagementCharger.contains(GC)) {
				for(J_EAChargePoint chargePoint : GC.c_chargers) {
					totalMaxChargePowerChargers_kW += chargePoint.capacityElectric_kW;
					totalNumberOfChargingSockets += chargePoint.getCurrentNumberOfChargingSockets();
					if(chargePoint.getV2GActive() && chargePoint.getV2GCapable()) {
						totalMaxV2GPowerChargers_kW += chargePoint.capacityElectric_kW;
					}
					
				}
				currentEVPower_kW += GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.evChargingPower_kW) - GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.V2GPower_kW);	
			}
			if(memberedGCWithSetpointHeatpumps.contains(GC)) {
				currentHPPower_kW += GC.fm_currentAssetFlows_kW.get(OL_AssetFlowCategories.heatPumpElectricityConsumption_kW);
			}
		}
		
    	double currentTargetLoad_kW = 0;
    	if(target instanceof GridNode gn) {
    		currentTargetLoad_kW = gn.v_currentLoad_kW;
    	}
    	if(target == this.energyCoop) {
    		currentTargetLoad_kW = energyCoop.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	}

    	double flexSetpoint_kW = 0; // flexsetpoint is negative, reduce consumption, flex setpoint is positive, increase consumption.
    	if(currentTargetLoad_kW > this.reducePowerSetpoint_kW*this.factorToAccountForTimeStepDelay) {
    		flexSetpoint_kW = this.reducePowerSetpoint_kW*this.factorToAccountForTimeStepDelay - currentTargetLoad_kW;
    	}
    	else if(currentTargetLoad_kW < this.increasePowerSetpoint_kW*this.factorToAccountForTimeStepDelay) {
    		flexSetpoint_kW = this.increasePowerSetpoint_kW*this.factorToAccountForTimeStepDelay - currentTargetLoad_kW;
    	}
    	
    	double remainingRequiredFlexSetpoint_kW = flexSetpoint_kW;
    	if(flexSetpoint_kW != 0) {
	    	if(memberedGCWithSetpointEVManagement.size()>0) {
	        	double EVChargeSetpointPerGC_kW = (currentEVPower_kW + flexSetpoint_kW) / memberedGCWithSetpointEVManagement.size();
	        	
		    	for(GridConnection GC : memberedGCWithSetpointEVManagement) {
		    		((J_ChargingManagementExternalSetpoint)GC.f_getChargingManagement()).setChargeSetpoint_kW(EVChargeSetpointPerGC_kW);
		    	}
		    	
		    	//EV compensation not enough for freeing delivery capacity? -> Reduce heating as well
		    	if(totalMaxChargePowerEVs_kW < currentEVPower_kW + flexSetpoint_kW) {
		    		remainingRequiredFlexSetpoint_kW = (currentEVPower_kW + flexSetpoint_kW) - totalMaxChargePowerEVs_kW;
		    	}
		    	else if(currentEVPower_kW + flexSetpoint_kW < totalMaxV2GPowerEVs_kW) {
		    		remainingRequiredFlexSetpoint_kW = -(totalMaxV2GPowerEVs_kW - (currentEVPower_kW + flexSetpoint_kW));
		    	}
		    	
	    	}
	    	else if(memberedGCWithSetpointEVManagementCharger.size() > 0) {
	        	double ChargerChargeSetpointPerGC_kW = (currentEVPower_kW + flexSetpoint_kW) / memberedGCWithSetpointEVManagementCharger.size();
	        	
		    	for(GridConnection GC : memberedGCWithSetpointEVManagementCharger) {
		    		GC.c_chargers.forEach(charger -> charger.setChargeSetpoint_kW(ChargerChargeSetpointPerGC_kW));
		    	}
	    	
		    	//EV compensation not enough for freeing delivery capacity? -> Reduce heating as well
		    	if(totalMaxChargePowerChargers_kW < currentEVPower_kW + flexSetpoint_kW) {
		    		remainingRequiredFlexSetpoint_kW = (currentEVPower_kW + flexSetpoint_kW) - totalMaxChargePowerChargers_kW;
		    	}
		    	else if(currentEVPower_kW + flexSetpoint_kW < totalMaxV2GPowerChargers_kW) {
		    		remainingRequiredFlexSetpoint_kW = -(totalMaxV2GPowerChargers_kW - (currentEVPower_kW + flexSetpoint_kW));
		    	}
	    	}
	    	
	
	    	for(GridConnection GC : memberedGCWithSetpointHeatpumps) {
	    		if(abs(remainingRequiredFlexSetpoint_kW) < 0.1) {//Nothing needed from heating systems, behave as normal
	    			((J_HeatingManagementExternalSetpoint)GC.f_getHeatingManagement()).setCurrentExternalTemperatureSetpoint_degC(null);
	    		}
	    		else if(remainingRequiredFlexSetpoint_kW < 0) { //Decrease heating
	    			((J_HeatingManagementExternalSetpoint)GC.f_getHeatingManagement()).setCurrentExternalTemperatureSetpoint_degC(GC.f_getHeatingManagement().getHeatingPreferences().getMinComfortTemperature_degC());
	    		}
	    		else if(remainingRequiredFlexSetpoint_kW > 0) { //Increase heating
	    			((J_HeatingManagementExternalSetpoint)GC.f_getHeatingManagement()).setCurrentExternalTemperatureSetpoint_degC(GC.f_getHeatingManagement().getHeatingPreferences().getMaxComfortTemperature_degC());
	    		}
	    	}
    	}
    }
    
    public void setTarget(Agent target) {
    	this.target = target;
    }
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.energyCoop;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {

	}
	public void restoreStates() {

	}
	
	
	@Override
	public String toString() {
		return super.toString();
	}
}
