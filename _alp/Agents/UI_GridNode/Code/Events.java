void e_gatherNodeInfo()
{/*ALCODESTART::1670933873241*/
v_localProduction_kW = 0;
v_batteryProduction_kW = 0;
v_vehicleProduction_kW = 0;

v_vehicleConsumption_kW = 0;
v_fixedConsumptionProfiles_kW = 0;
v_electricHeatingConsumption_kW = 0;
v_batteryConsumption_kW = 0;
v_electricCookingPower_kW = 0;

for( GridConnection g : realAgent.getConnectedAgent().c_electricityGridConnections){
	v_localProduction_kW += g.v_pvProductionElectric_kW + g.v_windProductionElectric_kW - min(0,g.v_conversionPowerElectric_kW);
	v_batteryProduction_kW += g.v_batteryPowerElectric_kW < 0 ? -g.v_batteryPowerElectric_kW : 0;
	v_vehicleProduction_kW += g.v_evChargingPowerElectric_kW < 0 ? g.v_evChargingPowerElectric_kW : 0;
	
	v_vehicleConsumption_kW += g.v_evChargingPowerElectric_kW > 0 ? g.v_evChargingPowerElectric_kW : 0;
	v_fixedConsumptionProfiles_kW += g.v_fixedConsumptionElectric_kW ;
	v_electricCookingPower_kW += g.v_electricHobConsumption_kW;
	v_electricHeatingConsumption_kW += max(0,g.v_conversionPowerElectric_kW - g.v_electricHobConsumption_kW);
	v_batteryConsumption_kW += g.v_batteryPowerElectric_kW > 0 ? g.v_batteryPowerElectric_kW : 0;

}

v_congestionPrice_eurpkWh = p_gridNode.v_currentCongestionPrice_eurpkWh;	
ch_cumulativeSupply.updateData();
ch_cumulativeDemand.updateData();
pl_powerFlows.updateData();
/*ALCODEEND*/}

