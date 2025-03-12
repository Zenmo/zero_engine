double f_connectToChild(Actor ConnectingChildActor,OL_EnergyCarriers EnergyCarrier)
{/*ALCODESTART::1660736326703*/
/*switch( EnergyCarrier ) {
		case ELECTRICITY:
			subConnectionsElectricity.connectTo(ConnectingChildNode);
		break;
		case METHANE:
			subConnectionsMethane.connectTo(ConnectingChildNode);
		break;
		case HYDROGEN:
			subConnectionsHydrogen.connectTo(ConnectingChildNode);
		break;
		case HEAT:
			subConnectionsHeat.connectTo(ConnectingChildNode);
		break;
		default:
				
		break;		
}*/
if (ConnectingChildActor.p_actorGroup != null) {
	if (ConnectingChildActor.p_actorGroup.contains("production") || ConnectingChildActor.p_actorGroup.contains("Production") || ConnectingChildActor.p_actorGroup.contains("member")) { // Count owned production-sites as 'behind the meter'
		c_coopMembers.add( ConnectingChildActor);
		c_memberGridConnections.addAll(((ConnectionOwner)ConnectingChildActor).c_ownedGridConnections);
		//traceln("Adding: %s", ((ConnectionOwner)ConnectingChildActor).c_ownedGridConnections);
	} else {
		c_coopCustomers.add( ConnectingChildActor );
		c_customerGridConnections.addAll(((ConnectionOwner)ConnectingChildActor).c_ownedGridConnections);
		
	}
} else {
	c_coopCustomers.add( ConnectingChildActor );
	c_customerGridConnections.addAll(((ConnectionOwner)ConnectingChildActor).c_ownedGridConnections);
	//traceln("Adding: %s", ((ConnectionOwner)ConnectingChildActor).c_ownedGridConnections);
}

/*
if ( v_contractGridOperator.equals("NODALPRICING")) {
//	traceln("Enabling nodal pricing for Coop memmber!");
	if (ConnectingChildNode instanceof ConnectionOwner) {
		//((ConnectionOwner)ConnectingChildNode).v_contractGridOperator = v_contractGridOperator;
		((ConnectionOwner)ConnectingChildNode).v_congestionTariff_b = true;
	}
}
if ( v_contractEnergySupplier.equals("VARIABLE")) {
	if (ConnectingChildNode instanceof ConnectionOwner) {
		((ConnectionOwner)ConnectingChildNode).v_electricityContractType = OL_DeliveryContractType.ELECTRICITY_VARIABLE;
		//((ConnectionOwner)ConnectingChildNode).v_contractEnergySupplier= v_contractEnergySupplier;
		//((ConnectionOwner)ConnectingChildNode).v_updatePriceBands_b = true;
	}
}
*/
/*ALCODEEND*/}

double f_updateFinances()
{/*ALCODESTART::1660806150226*/
// get current energy flows
//f_gatherEnergyFlows();

/*
double currentPowerDrawn_kW = -v_electricitySurplus_kW;

// trigger transaction with supplier/Coop

double transactionCost_eur = 0;//((EnergySupplier)p_energySupplier).f_doEnergyTransaction(v_electricityVolume_kWh, v_electricityContractType);
double transactionCostTax_eur = 0;
double transactionCostTransport_eur = 0;
double transactionCostDelivery_eur = 0;
//transactionCost_eur = 0;//((EnergySupplier)p_energySupplier).f_doEnergyTransaction(v_heatVolume_kWh, v_heatContractType);
//v_balanceHeat_eur -= transactionCost_eur;
//transactionCost_eur = 0;//((EnergySupplier)p_energySupplier).f_doEnergyTransaction(v_methaneVolume_kWh, v_methaneContractType);
//v_balanceMethane_eur -= transactionCost_eur;

//
if (v_electricityVolume_kWh >= 0) {
	//transactionCostDelivery_eur = v_priceBandsDelivery.ceilingEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	if (v_contractDelivery!=null){
		transactionCostDelivery_eur = (v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh) * v_electricityVolume_kWh;
	} else {
		transactionCostDelivery_eur = v_electricityVariablePrice_eurpkWh * v_electricityVolume_kWh;
	}
	v_balanceElectricityDelivery_eur -= transactionCostDelivery_eur;
	v_balanceElectricity_eur -= transactionCostDelivery_eur;

	if (v_contractTax!=null){
		transactionCostTax_eur = v_contractTax.deliveryTax_eurpkWh * v_electricityVolume_kWh;
		//transactionCostTax_eur = v_electricityVolume_kWh * v_contractTax.feedinTax_eurpkWh + v_contractTax.proportionalTax_pct*(v_electricityVolume_kWh * v_contractTax.feedinTax_eurpkWh + transactionCostDelivery_eur + transactionCostTransport_eur);
		v_balanceElectricityTax_eur -= transactionCostTax_eur;
		v_balanceElectricity_eur -= transactionCostTax_eur;
	}
} else {
	if (v_contractDelivery!=null){
		transactionCostDelivery_eur = (v_contractDelivery.feedinPrice_eurpkWh + v_electricityVariablePrice_eurpkWh) * v_electricityVolume_kWh;
	} else {
		transactionCostDelivery_eur = v_electricityVariablePrice_eurpkWh * v_electricityVolume_kWh;
	}
	v_balanceElectricityDelivery_eur -= transactionCostDelivery_eur;
	v_balanceElectricity_eur -= transactionCostDelivery_eur;

	if (v_contractTax!=null){
		transactionCostTax_eur = v_contractTax.feedinTax_eurpkWh * v_electricityVolume_kWh;
		//transactionCostTax_eur = v_electricityVolume_kWh * v_contractTax.feedinTax_eurpkWh + v_contractTax.proportionalTax_pct*(v_electricityVolume_kWh * v_contractTax.feedinTax_eurpkWh + transactionCostDelivery_eur + transactionCostTransport_eur);
		v_balanceElectricityTax_eur -= transactionCostTax_eur;
		v_balanceElectricity_eur -= transactionCostTax_eur;
	}
}

transactionCostTransport_eur = v_currentNodalPrice_eurpkWh * v_electricityVolume_kWh;
v_balanceElectricityTransport_eur -= transactionCostTransport_eur;
v_balanceElectricity_eur -= transactionCostTransport_eur;


// TODO: Also needs to include congestion tariffs! So Coop needs to know to which GridNode it's connected! How should that work?
*/
/*ALCODEEND*/}

double f_doEnergyTransaction(double transactionVolume_kWh,OL_ContractType contractType)
{/*ALCODESTART::1660825183645*/
double transactionCostClient_eur = 0;

if( p_energySupplier instanceof EnergySupplier ) {
	EnergySupplier energySupplier = (EnergySupplier)p_energySupplier;
	transactionCostClient_eur = energySupplier.f_doEnergyTransaction(transactionVolume_kWh, contractType);
} else if( p_energySupplier instanceof EnergyCoop ) {
	EnergyCoop energySupplier = (EnergyCoop)p_energySupplier;
	transactionCostClient_eur = energySupplier.f_doEnergyTransaction(transactionVolume_kWh, contractType);
}

v_energyPassedThrough_kWh += transactionVolume_kWh;

return transactionCostClient_eur;
/*ALCODEEND*/}

double f_connectToParentActor()
{/*ALCODESTART::1660825257472*/
Actor mySupplier = null;
mySupplier = findFirst(energyModel.pop_energySuppliers, p->p.p_actorID.equals(v_contractDelivery.contractScope)) ;
if (mySupplier != null) {
	((EnergySupplier)mySupplier).f_connectToChild(this);	
} else {
	mySupplier = findFirst(energyModel.pop_energyCoops, p->p.p_actorID.equals(v_contractDelivery.contractScope)) ;
	if (mySupplier != null) {
		((EnergyCoop)mySupplier).f_connectToChild(this,OL_EnergyCarriers.ELECTRICITY);	
	}
}

if (mySupplier != null) {
	p_electricitySupplier = mySupplier;
} 
else { 
	p_electricitySupplier = energyModel.pop_energySuppliers.get(0);  // this is a harcoded fix to make the buurtmodel run for 21-3-2023
	traceln("Connection owner %s --> f_connectToParentActor --> no parent actor %s found, this should not be happening", this, v_contractDelivery.contractScope);
}

Actor myGridoperator = null;
// Connect to grid operator (can be a Coop!)
myGridoperator = findFirst(energyModel.pop_gridOperators, p->p.p_actorID.equals(v_contractTransport.contractScope)) ;
if (myGridoperator == null) {
	myGridoperator = findFirst(energyModel.pop_energyCoops, p->p.p_actorID.equals(v_contractTransport.contractScope)) ;
}

if (myGridoperator != null) {
	p_gridOperator = myGridoperator;	
} else { 
	//p_gridOperator = main.pop_gridOperators.get( 0 ) ; // this is a harcoded fix to make the buurtmodel run for 21-3-2023
	traceln("Connection owner --> f_connectToParentActor --> no parent actor found, this should not be happening");
}

/*ALCODEEND*/}

double f_calculateEnergyBalance()
{/*ALCODESTART::1667983361355*/
v_currentOwnElectricityProduction_kW = 0; // Only electricity production from 'members' as opposed to 'customers'.
v_currentCustomerFeedIn_kW = 0; // Feedin from customers (self-consumption behind-the-meter is not counted for customers)
v_currentCustomerDelivery_kW = 0; // Delivery to customers (self-consumption behind-the-meter is not counted for customers)

// Categorical power flows
v_fixedConsumptionElectric_kW = 0;
v_electricHobConsumption_kW = 0;
v_heatPumpElectricityConsumption_kW = 0;
v_hydrogenElectricityConsumption_kW = 0;
v_evChargingPowerElectric_kW = 0;
v_batteryPowerElectric_kW = 0;
v_windProductionElectric_kW = 0;
v_pvProductionElectric_kW = 0;
v_conversionPowerElectric_kW = 0;
v_districtHeatDelivery_kW = 0;
v_CHPProductionElectric_kW = 0;

fm_currentProductionFlows_kW.clear();
fm_currentConsumptionFlows_kW.clear();
fm_currentBalanceFlows_kW.clear();
v_currentPrimaryEnergyProduction_kW = 0;
v_currentFinalEnergyConsumption_kW = 0;
v_currentEnergyCurtailed_kW = 0;
v_currentPrimaryEnergyProductionHeatpumps_kW = 0;

//Stored energy
v_batteryStoredEnergy_kWh = 0;

for (GridConnection GC : c_memberGridConnections) { // Take 'behind the meter' production and consumption!
	for (OL_EnergyCarriers energyCarrier : v_activeEnergyCarriers) {
		fm_currentProductionFlows_kW.addFlow( energyCarrier, GC.fm_currentProductionFlows_kW.get(energyCarrier));
		fm_currentConsumptionFlows_kW.addFlow( energyCarrier, GC.fm_currentConsumptionFlows_kW.get(energyCarrier));
		fm_currentBalanceFlows_kW.addFlow( energyCarrier, GC.fm_currentBalanceFlows_kW.get(energyCarrier));
	}
	v_currentPrimaryEnergyProduction_kW += GC.v_currentPrimaryEnergyProduction_kW;
	v_currentFinalEnergyConsumption_kW += GC.v_currentFinalEnergyConsumption_kW;
	v_currentEnergyCurtailed_kW += GC.v_currentEnergyCurtailed_kW;
	v_currentPrimaryEnergyProductionHeatpumps_kW += GC.v_currentPrimaryEnergyProductionHeatpumps_kW;
	v_currentOwnElectricityProduction_kW += GC.fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); 
	
	// Categorical power flows
	v_fixedConsumptionElectric_kW += GC.v_fixedConsumptionElectric_kW;
	v_electricHobConsumption_kW += GC.v_electricHobConsumption_kW;
	v_heatPumpElectricityConsumption_kW += GC.v_heatPumpElectricityConsumption_kW;
	v_hydrogenElectricityConsumption_kW += GC.v_hydrogenElectricityConsumption_kW;
	v_evChargingPowerElectric_kW += GC.v_evChargingPowerElectric_kW;
	v_batteryPowerElectric_kW += GC.v_batteryPowerElectric_kW;
	v_windProductionElectric_kW += GC.v_windProductionElectric_kW;
	v_pvProductionElectric_kW += GC.v_pvProductionElectric_kW;
	v_conversionPowerElectric_kW += GC.v_conversionPowerElectric_kW;
	v_districtHeatDelivery_kW += GC.v_districtHeatDelivery_kW;
	v_CHPProductionElectric_kW += GC.v_CHPProductionElectric_kW;
	
	//Battery stored energy
	v_batteryStoredEnergy_kWh += GC.v_batteryStoredEnergy_kWh;

}


// gather electricity flows
for(Agent a :  c_coopMembers ) { // Take 'behind the meter' production and consumption!
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
		
		for (OL_EnergyCarriers energyCarrier : v_activeEnergyCarriers) {
			fm_currentProductionFlows_kW.addFlow( energyCarrier, EC.fm_currentProductionFlows_kW.get(energyCarrier));
			fm_currentConsumptionFlows_kW.addFlow( energyCarrier, EC.fm_currentConsumptionFlows_kW.get(energyCarrier));
			fm_currentBalanceFlows_kW.addFlow( energyCarrier, EC.fm_currentBalanceFlows_kW.get(energyCarrier));
		}
		
		v_currentPrimaryEnergyProduction_kW += EC.v_currentPrimaryEnergyProduction_kW;
		v_currentFinalEnergyConsumption_kW += EC.v_currentFinalEnergyConsumption_kW;
		v_currentEnergyCurtailed_kW += EC.v_currentEnergyCurtailed_kW;
		v_currentPrimaryEnergyProductionHeatpumps_kW += EC.v_currentPrimaryEnergyProductionHeatpumps_kW;
		v_currentOwnElectricityProduction_kW += EC.fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); 
		
		
		// Categorical power flows
		v_fixedConsumptionElectric_kW += EC.v_fixedConsumptionElectric_kW;
		v_electricHobConsumption_kW += EC.v_electricHobConsumption_kW;
		v_heatPumpElectricityConsumption_kW += EC.v_heatPumpElectricityConsumption_kW;
		v_hydrogenElectricityConsumption_kW += EC.v_hydrogenElectricityConsumption_kW;
		v_evChargingPowerElectric_kW += EC.v_evChargingPowerElectric_kW;
		v_batteryPowerElectric_kW += EC.v_batteryPowerElectric_kW;
		v_windProductionElectric_kW += EC.v_windProductionElectric_kW;
		v_pvProductionElectric_kW += EC.v_pvProductionElectric_kW;
		v_conversionPowerElectric_kW += EC.v_conversionPowerElectric_kW;
		v_districtHeatDelivery_kW += EC.v_districtHeatDelivery_kW;
		v_CHPProductionElectric_kW += EC.v_CHPProductionElectric_kW;
	
		//Battery stored energy
		v_batteryStoredEnergy_kWh += EC.v_batteryStoredEnergy_kWh;
	}
}

for (GridConnection GC : c_customerGridConnections) { // Take 'behind the meter' production and consumption!
	for (OL_EnergyCarriers energyCarrier : v_activeEnergyCarriers) {
		double nettConsumption_kW = GC.fm_currentBalanceFlows_kW.get(energyCarrier);
		fm_currentProductionFlows_kW.addFlow( energyCarrier, max(0, -nettConsumption_kW));
		fm_currentConsumptionFlows_kW.addFlow( energyCarrier, max(0, nettConsumption_kW));
		fm_currentBalanceFlows_kW.addFlow( energyCarrier, nettConsumption_kW);
		if (energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			v_currentCustomerFeedIn_kW += max(0,-nettConsumption_kW);
			v_currentCustomerDelivery_kW += max(0,nettConsumption_kW);
		}
	}				
	//v_currentCustomerFeedIn_kW += max(0, -GC.v_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
}

for(Agent a :  c_coopCustomers ) { // Don't look at 'behind the meter' production/consumption, but use 'nett flow' as measure of consumption/production
	if (a instanceof EnergyCoop) {
		//traceln("Hello!? coopCustomer EnergyCoop!");
		EnergyCoop EC = (EnergyCoop)a;
				
		for (OL_EnergyCarriers energyCarrier : v_activeEnergyCarriers) {
			fm_currentProductionFlows_kW.addFlow( energyCarrier, EC.fm_currentProductionFlows_kW.get(energyCarrier));
			fm_currentConsumptionFlows_kW.addFlow( energyCarrier, EC.fm_currentConsumptionFlows_kW.get(energyCarrier));
			fm_currentBalanceFlows_kW.addFlow( energyCarrier, EC.fm_currentBalanceFlows_kW.get(energyCarrier));
		}
		
		v_currentCustomerFeedIn_kW += EC.v_currentCustomerFeedIn_kW;
		v_currentCustomerDelivery_kW += EC.v_currentCustomerDelivery_kW;
		
	}
}

//v_totalElectricityProduced_MWh += v_currentElectricityProduction_kW * energyModel.p_timeStep_h/1000;

v_electricitySurplus_kW = -fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);

/*
v_electricitySurplus_kW = -v_electricityVolume_kWh / energyModel.p_timeStep_h;
*/

f_updateArrays();
/*ALCODEEND*/}

double f_getVariableEnergyPrice()
{/*ALCODESTART::1667983361357*/
double energyPrice_eurpkWh = v_electricityVariablePrice_eurpkWh + v_contractDelivery.deliveryPrice_eurpkWh;

if ( v_contractTransport.transportContractType==OL_TransportContractType.NODALPRICING) {
	energyPrice_eurpkWh += v_currentNodalPrice_eurpkWh;
} 

//traceln("Coop received energy price of: " + energyPrice_eurpkWh + " eur/kWh");
return energyPrice_eurpkWh;
/*ALCODEEND*/}

double f_returnKPIData()
{/*ALCODESTART::1667983437058*/
// Total electricity import, export
traceln("Coop Electricity imported: "+ v_electricityImported_kWh);
traceln("Coop Electricity exported: "+ v_electricityExported_kWh);
traceln("Coop electricity costs: " + v_balanceElectricity_eur + " euro");
//Total energy consumption and production 

// Assume 'infinite storage' of hydrogen within model-scope; only nett total import/export counts.
if (v_hydrogenImported_kWh > v_hydrogenExported_kWh) {
	v_hydrogenImported_kWh = v_hydrogenImported_kWh - v_hydrogenExported_kWh;
	v_hydrogenExported_kWh = 0;
} else {
	v_hydrogenExported_kWh = v_hydrogenExported_kWh - v_hydrogenImported_kWh;
	v_hydrogenImported_kWh = 0;
}

double energyProduced_kWh = 0;
double energyConsumed_kWh = 0;
for(Agent a :  subConnectionsElectricity.getConnections() ) {
	if(a instanceof ConnectionOwner){
		ConnectionOwner n = (ConnectionOwner)a;
		for ( GridConnection g : n.gridConnections.getConnections() ) {
			for (EnergyAsset e : g.c_energyAssets) {
				double EnergyUsed_kWh = e.j_ea.getEnergyUsed_kWh();
				//traceln("EnergyAsset " + e.getIndex() + " of type " + e.p_defaultEnergyAssetPresetName + " used " + EnergyUsed_kWh + " kWh energy");
				if (EnergyUsed_kWh > 0) {
					energyConsumed_kWh += EnergyUsed_kWh;
				} else {
					energyProduced_kWh -= EnergyUsed_kWh;
				}
			}
		}
	} else if (a instanceof EnergyCoop) { // 'Recursive' code for Coop-of-Coops. Only supports 2 levels of Coops!
		EnergyCoop n = (EnergyCoop)a;
		for(Agent a2 :  n.subConnectionsElectricity.getConnections() ) {
			if(a2 instanceof ConnectionOwner){
				ConnectionOwner n2 = (ConnectionOwner)a;
				for ( GridConnection g : n2.gridConnections.getConnections() ) {
					for (EnergyAsset e : g.c_energyAssets) {
						double EnergyUsed_kWh = e.j_ea.getEnergyUsed_kWh();
						//traceln("EnergyAsset " + e.getIndex() + " of type " + e.p_defaultEnergyAssetPresetName + " used " + EnergyUsed_kWh + " kWh energy");
						if (EnergyUsed_kWh > 0) {
							energyConsumed_kWh += EnergyUsed_kWh;
						} else {
							energyProduced_kWh -= EnergyUsed_kWh;
						}
					}
				}
			}
		}
	}
}

traceln("Energy consumed: "+ energyConsumed_kWh);
traceln("Energy produced: "+ energyProduced_kWh);
// TODO: Make sure this calculation is correct when all energy carriers are 'active'! Only checked for electricity

//Total selfconsumption, selfsufficiency
double totalSelfConsumption_fr = 1 - (v_electricityExported_kWh + v_methaneExported_kWh + v_hydrogenExported_kWh + v_heatExported_kWh)/energyProduced_kWh;
double totalSelfSufficiency_fr = 1 - (v_electricityImported_kWh + v_methaneImported_kWh + v_hydrogenImported_kWh + v_heatImported_kWh + v_dieselImported_kWh)/energyConsumed_kWh;

double totalSelfSufficiency_fr_check = (energyProduced_kWh - (v_electricityExported_kWh + v_methaneExported_kWh + v_hydrogenExported_kWh + v_heatExported_kWh))/energyConsumed_kWh;
traceln("Coop selfconsumption: " + totalSelfConsumption_fr + "%");
traceln("Coop selfsufficiency: " + totalSelfSufficiency_fr + "%, doublecheck " + totalSelfSufficiency_fr_check);
// TODO: Account for fuel imports/exports!!


/*ALCODEEND*/}

double f_setContractValues()
{/*ALCODESTART::1669037224999*/
if( p_actorContractList != null) {
	//traceln("Contract list connectionOwner " + p_actorID + ": " + p_actorContractList.toString());
} else {
	traceln( "No contract data for connectionOwner " + p_actorID );
}

double methaneDeliveryPrice_eurpkWh = 0.0;
double methaneDeliveryTax_eurpkWh = 0.0;
double methaneVAT_pct = 0.0;
double hydrogenDeliveryPrice_eurpkWh = 0.0;
double hydrogenDeliveryTax_eurpkWh = 0.0;
double hydrogenVAT_pct = 0.0;
double heatDeliveryPrice_eurpkWh = 0.0;
double heatDeliveryTax_eurpkWh = 0.0;
double heatVAT_pct = 0.0;

for( JsonNode contractJson : p_actorContractList) {
	//traceln("Contract hashmap l: " + l);
	OL_ContractType contractService = OL_ContractType.valueOf(contractJson.required("contractType").textValue());
	String contractScope = contractJson.required( "contractScope" ).textValue();
	OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.valueOf(contractJson.required("energyCarrier").textValue());
	double annualFee_eur = contractJson.required("annualFee_eur").doubleValue();
	Actor mySupplier = null;
	if (energyCarrier.equals(OL_EnergyCarriers.ELECTRICITY)){
		switch( contractService ) {
			case DELIVERY:
				//String contractScope = (String)l.get( "contract_scope" );
				OL_DeliveryContractType deliveryContractType = OL_DeliveryContractType.valueOf(contractJson.get("deliveryContractType").textValue());
				double deliveryPrice_eurpkWh = contractJson.required("deliveryPrice_eurpkWh").doubleValue();
				double feedinPrice_eurpkWh = contractJson.required("feedinPrice_eurpkWh").doubleValue();
				v_contractDelivery = new J_DeliveryContract(contractScope, deliveryContractType, OL_EnergyCarriers.ELECTRICITY, deliveryPrice_eurpkWh, feedinPrice_eurpkWh, annualFee_eur);
				//traceln("Delivery contract: " + v_contractDelivery);
				c_actorContracts.add(v_contractDelivery);
			break;
			case TRANSPORT:
				OL_TransportContractType transportContractType = OL_TransportContractType.valueOf(contractJson.get("transportContractType").textValue());
				double bandwidthTreshold_kW = contractJson.path("bandwidthTreshold_kW").doubleValue();
				double bandwidthTariff_eurpkWh = contractJson.path("bandwidthTariff_eurpkWh").doubleValue();
				v_contractTransport = new J_TransportContract(contractScope, transportContractType, energyCarrier, bandwidthTreshold_kW, bandwidthTariff_eurpkWh, annualFee_eur);			
				c_actorContracts.add(v_contractTransport);
			break;
			case CONNECTION:
				OL_ConnectionContractType connectionContractType = OL_ConnectionContractType.valueOf(contractJson.required("connectionContractType").textValue());
				//traceln("Initializing connection contract with ConnectionContractType: " + connectionContractType);
				double nfATOstart_h = contractJson.path("nfATO_starttime_h").doubleValue();
				double nfATOend_h = contractJson.path("nfATO_endtime_h").doubleValue();
				double nfATOcapacity_kW = contractJson.path("nfATO_capacity_kW").doubleValue();
				v_contractConnection = new J_ConnectionContract(contractScope, connectionContractType, energyCarrier, nfATOstart_h, nfATOend_h, nfATOcapacity_kW, annualFee_eur);
				c_actorContracts.add(v_contractConnection);
			break;
			case TAX:
				double taxDelivery_eurpkWh = contractJson.required("taxDelivery_eurpkWh").doubleValue();
				double taxFeedin_eurpkWh = contractJson.required("taxFeedin_eurpkWh").doubleValue();
				double proportionalTax_pct = contractJson.required("proportionalTax_pct").doubleValue();
				v_contractTax = new J_TaxContract(contractScope, OL_EnergyCarriers.ELECTRICITY, taxDelivery_eurpkWh, taxFeedin_eurpkWh, proportionalTax_pct, annualFee_eur);
				c_actorContracts.add(v_contractTax);
			break;
			default:
			
			break;		
		}
	//} else if (energyCarrier.equals(OL_EnergyCarriers.METHANE)){
	} else {
		switch( contractService ) {
			case DELIVERY:					
				OL_DeliveryContractType deliveryContractType = OL_DeliveryContractType.valueOf(contractJson.required("deliveryContractType").textValue());
				double deliveryPrice_eurpkWh = contractJson.required("deliveryPrice_eurpkWh").doubleValue();
				double feedinPrice_eurpkWh = contractJson.required("feedinPrice_eurpkWh").doubleValue();
				J_DeliveryContract contractDelivery = new J_DeliveryContract(contractScope, deliveryContractType, energyCarrier, deliveryPrice_eurpkWh, feedinPrice_eurpkWh, annualFee_eur);
				//traceln("Delivery contract: " + v_contractDelivery);
				c_actorContracts.add(contractDelivery);
				//String contractScope = (String)l.get( "contractScope" );
				mySupplier = findFirst(energyModel.pop_energySuppliers, p->p.p_actorID.equals(contractScope)) ;
				if (mySupplier != null) {
					((EnergySupplier)mySupplier).f_connectToChild(this);	
				} else {
					mySupplier = findFirst(energyModel.pop_energyCoops, p->p.p_actorID.equals(contractScope)) ;
					if (mySupplier != null) {
						((EnergyCoop)mySupplier).f_connectToChild(this,energyCarrier);	
						//p_CoopParent = (EnergyCoop)mySupplier;
					}
				}
				//traceln("Methane supplier: " + contractScope + " for actor " + p_actorID );
				if (mySupplier != null) {
					//energySupplier.connectTo(mySupplier);
					if (energyCarrier==OL_EnergyCarriers.METHANE) {
						p_methaneSupplier = mySupplier;
						methaneDeliveryPrice_eurpkWh=deliveryPrice_eurpkWh;
					} else if(energyCarrier==OL_EnergyCarriers.HYDROGEN) {
						p_hydrogenSupplier = mySupplier;
						hydrogenDeliveryPrice_eurpkWh=deliveryPrice_eurpkWh;
					} else if(energyCarrier==OL_EnergyCarriers.HEAT) {
						p_heatSupplier = mySupplier;
						heatDeliveryPrice_eurpkWh=deliveryPrice_eurpkWh;
					}
					//traceln("MethaneSupplier " + mySupplier);
					//energySupplier.f_connectToChild(this);	
				} else { 
					traceln("Connection owner --> f_connectToParentActor --> " + energyCarrier + " supplier not found, this should not be happening");
				}
			break;
			case TAX:
				double DeliveryTax_eurpkWh = contractJson.required("taxDelivery_eurpkWh").doubleValue();
				double taxFeedin_eurpkWh = contractJson.required("taxFeedin_eurpkWh").doubleValue();
				double VAT_pct = contractJson.required("proportionalTax_pct").doubleValue();
				J_TaxContract contractTax = new J_TaxContract(contractScope, energyCarrier, DeliveryTax_eurpkWh, taxFeedin_eurpkWh, VAT_pct, annualFee_eur);
				c_actorContracts.add(contractTax);
				if (energyCarrier==OL_EnergyCarriers.METHANE) {						
						methaneDeliveryTax_eurpkWh=DeliveryTax_eurpkWh;
						methaneVAT_pct=VAT_pct;
					} else if(energyCarrier==OL_EnergyCarriers.HYDROGEN) {
						hydrogenDeliveryTax_eurpkWh=DeliveryTax_eurpkWh;
						hydrogenVAT_pct=VAT_pct;
					} else if(energyCarrier==OL_EnergyCarriers.HEAT) {
						heatVAT_pct=VAT_pct;
					}
			break;
			case CONNECTION:
				OL_ConnectionContractType connectionContractType = OL_ConnectionContractType.valueOf(contractJson.required("connectionContractType").textValue());
				J_ConnectionContract contractConnection = new J_ConnectionContract(contractScope, connectionContractType, energyCarrier, 0.0, 0.0, 0.0, annualFee_eur);
				c_actorContracts.add(contractConnection);



			break;
			case TRANSPORT:
				OL_TransportContractType transportContractType = OL_TransportContractType.valueOf(contractJson.required("transportContractType").textValue());
				double bandwidthTreshold_kW = contractJson.path("bandwidthTreshold_kW").doubleValue();
				double bandwidthTariff_eurpkWh = contractJson.path("bandwidthTariff_eurpkWh").doubleValue();
				J_TransportContract contractTransport = new J_TransportContract(contractScope, transportContractType, energyCarrier, bandwidthTreshold_kW, bandwidthTariff_eurpkWh, annualFee_eur);			
				c_actorContracts.add(contractTransport);
			break;
			default:
				traceln("Unrecognized contract type!");
			break;	
		}	
	}
}

// Calculate nett gas and hydrogen price
v_methanePrice_eurpkWh = (methaneDeliveryPrice_eurpkWh + methaneDeliveryTax_eurpkWh) * (1 + methaneVAT_pct);
v_hydrogenPrice_eurpkWh = (hydrogenDeliveryPrice_eurpkWh + hydrogenDeliveryTax_eurpkWh) * (1 + hydrogenVAT_pct);
v_heatPrice_eurpkWh = (heatDeliveryPrice_eurpkWh + heatDeliveryTax_eurpkWh) * (1 + heatVAT_pct);
		/*} else if (energyCarrier.equals(OL_EnergyCarriers.HYDROGEN)){
			switch( contractService ) {
				case DELIVERY:
					OL_DeliveryContractType deliveryContractType = OL_DeliveryContractType.valueOf((String)l.get("deliveryContractType"));
					double deliveryPrice_eurpkWh = (double)l.get("deliveryPrice_eurpkWh");
					double feedinPrice_eurpkWh = (double)l.get("feedinPrice_eurpkWh");
					v_contractDelivery = new J_DeliveryContract(contractScope, deliveryContractType, OL_EnergyCarriers.HYDROGEN, deliveryPrice_eurpkWh, feedinPrice_eurpkWh);
					//traceln("Delivery contract: " + v_contractDelivery);
					c_actorContracts.add(v_contractDelivery);
					mySupplier = findFirst(main.pop_energySuppliers, p->p.p_actorID.equals(contractScope)) ;
					if (mySupplier != null) {
						((EnergySupplier)mySupplier).f_connectToChild(this);
					} else {
						mySupplier = findFirst(main.pop_energyCoops, p->p.p_actorID.equals(contractScope)) ;
						if (mySupplier != null) {
							((EnergyCoop)mySupplier).f_connectToChild(this,OL_EnergyCarriers.HYDROGEN);
							//p_CoopParent = (EnergyCoop)mySupplier;
						}
					}
					if (mySupplier != null) {
						//energySupplier.connectTo(mySupplier);
						p_hydrogenSupplier = mySupplier;
						//energySupplier.f_connectToChild(this);
					} else {
						traceln("Connection owner --> f_connectToParentActor --> hydrogen supplier not found, this should not be happening");
					}
				break;
				case TAX:
					hydrogenDeliveryTax_eurpkWh = (double)l.get("taxDelivery_eurpkWh");
					//double taxFeedin_eurpkWh = (double)l.get("taxFeedin_eurpkWh");
					hydrogenVAT_pct = (double)l.get("proportionalTax_pct");
					v_contractTax = new J_TaxContract(contractScope, OL_EnergyCarriers.HYDROGEN, hydrogenDeliveryTax_eurpkWh, 0, hydrogenVAT_pct);
					c_actorContracts.add(v_contractTax);
				break;
				default:

				break;
			}
		} else if (energyCarrier.equals(OL_EnergyCarriers.HEAT)){
			switch( contractService ) {
				case DELIVERY:
					heatDeliveryPrice_eurpkWh = (double)l.get("deliveryPrice_eurpkWh");
					mySupplier = findFirst(main.pop_energySuppliers, p->p.p_actorID.equals(contractScope)) ;
					if (mySupplier != null) {
						((EnergySupplier)mySupplier).f_connectToChild(this);
					} else {
						mySupplier = findFirst(main.pop_energyCoops, p->p.p_actorID.equals(contractScope)) ;
						if (mySupplier != null) {
							((EnergyCoop)mySupplier).f_connectToChild(this,OL_EnergyCarriers.HYDROGEN);
							//p_CoopParent = (EnergyCoop)mySupplier;
						}
					}
					if (mySupplier != null) {
						//energySupplier.connectTo(mySupplier);
						p_hydrogenSupplier = mySupplier;
						//energySupplier.f_connectToChild(this);
					} else {
						traceln("Connection owner --> f_connectToParentActor --> heat supplier not found, this should not be happening");
					}
				break;
				case TAX:
					hydrogenDeliveryTax_eurpkWh = (double)l.get("taxDelivery_eurpkWh");
					//double taxFeedin_eurpkWh = (double)l.get("taxFeedin_eurpkWh");
					hydrogenVAT_pct = (double)l.get("proportionalTax_pct");
				break;
				default:
				
				break;
			}
		}*/


/*ALCODEEND*/}

double f_initialize()
{/*ALCODESTART::1669042410671*/

//Get energy carriers and capacities boolean
for(GridConnection GC:c_memberGridConnections){
	p_contractedDeliveryCapacity_kW += GC.p_contractedDeliveryCapacity_kW;
	p_contractedFeedinCapacity_kW += GC.p_contractedFeedinCapacity_kW;
	v_activeEnergyCarriers.addAll(GC.v_activeEnergyCarriers);
	v_activeProductionEnergyCarriers.addAll(GC.v_activeProductionEnergyCarriers);
	v_activeConsumptionEnergyCarriers.addAll(GC.v_activeConsumptionEnergyCarriers);
	

	if(!GC.b_isRealDeliveryCapacityAvailable){
		b_isRealDeliveryCapacityAvailable = false;
	
	}

	if(!GC.b_isRealFeedinCapacityAvailable){
		b_isRealFeedinCapacityAvailable = false;
	
	} 
}

v_rapidRunData.initializeAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();
acc_totalOwnElectricityProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 8760);
acc_totalCustomerDelivery_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 8760);
acc_totalCustomerFeedIn_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 8760);

//========== LIVE WEEK DATASETS ==========//
v_liveData.dsm_liveDemand_kW.createEmptyDataSets(v_activeConsumptionEnergyCarriers, roundToInt(168/energyModel.p_timeStep_h));
v_liveData.dsm_liveSupply_kW.createEmptyDataSets(v_activeProductionEnergyCarriers, roundToInt(168/energyModel.p_timeStep_h));

//Initialize active asset booleans
v_activeAssetData.updateActiveAssetData(f_getAllChildMemberGridConnections());
/*ALCODEEND*/}

double f_updateIncentives()
{/*ALCODESTART::1676553303568*/
v_electricitySurplusLowPassed_kW += v_lowPassFactor_fr * ( v_electricitySurplus_kW - v_electricitySurplusLowPassed_kW );

/* if (v_contractDelivery.deliveryContractType==OL_DeliveryContractType.ELECTRICITY_VARIABLE) {
	if (p_electricitySupplier instanceof EnergySupplier) {
		v_electricityVariablePrice_eurpkWh = ((EnergySupplier)p_electricitySupplier).f_getVariableEnergyPrice();
	} else if (p_electricitySupplier instanceof EnergyCoop) {
		v_electricityVariablePrice_eurpkWh = ((EnergyCoop)p_electricitySupplier).f_getVariableEnergyPrice();
	}
}

f_setElectricityPriceBands(); */ // temporarily disabled!!

/*ALCODEEND*/}

double f_setElectricityPriceBands()
{/*ALCODESTART::1676553303571*/
/*if (v_contractDelivery.deliveryContractType.equals(OL_DeliveryContractType.ELECTRICITY_VARIABLE)) {
	v_priceBandsDelivery.replace(-9999999.0, v_contractDelivery.feedinPrice_eurpkWh + v_electricityVariablePrice_eurpkWh );
	v_priceBandsDelivery.replace(9999999.0, v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh );
}
if (v_contractTransport.transportContractType.equals(OL_TransportContractType.NODALPRICING)) {
	v_priceBandsTransport.replace(-9999999.0, v_currentNodalPrice_eurpkWh);
	v_priceBandsTransport.replace(9999999.0, v_currentNodalPrice_eurpkWh);
}*/

// Assuming Tax is not varying in time!!

/*
if( p_capacityTariffApplicable ){
	v_priceBandsDelivery.replace(- p_capacityLevel_kW, v_electricitySellPrice_eurpkWh + v_currentCongestionTariffWhenSelling_eurpkWh);
	v_priceBandsDelivery.replace(p_capacityLevel_kW, v_electricityVariablePrice_eurpkWh + v_currentNodalPrice_eurpkWh);
}
*/



	
/*ALCODEEND*/}

double f_setInitPriceBands()
{/*ALCODESTART::1676553472755*/
v_priceBandsDelivery = new TreeMap<Double, Double>();
v_priceBandsDelivery.put(-9999999.0, v_contractDelivery.feedinPrice_eurpkWh);
v_priceBandsDelivery.put(0.0, 0.0);
v_priceBandsDelivery.put(9999999.0, v_contractDelivery.deliveryPrice_eurpkWh);

v_priceBandsTransport = new TreeMap<Double, Double>();
v_priceBandsTransport.put(-9999999.0, 0.0);
v_priceBandsTransport.put(0.0, 0.0);
v_priceBandsTransport.put(9999999.0, 0.0);

v_priceBandsTax = new TreeMap<Double, Double>();
v_priceBandsTax.put(-9999999.0, v_contractTax.feedinTax_eurpkWh);
v_priceBandsTax.put(0.0, 0.0);
v_priceBandsTax.put(9999999.0, v_contractTax.deliveryTax_eurpkWh);

/*
if( p_congestionTariffApplicable ){
	v_priceBandsDelivery.put(- p_capacityLevel_kW, 0.0);
	v_priceBandsDelivery.put(p_capacityLevel_kW, 0.0);
}
*/
/*ALCODEEND*/}

double f_totalFinances()
{/*ALCODESTART::1692111928489*/
// Depreciation costs
v_assetDepreciation_eur = f_totalAssetDepreciation();
v_totalBalanceCoop_eur -= v_assetDepreciation_eur;
// Operational costs: local parameter p_yearlyOperationalCosts_eur
v_totalBalanceCoop_eur -= p_yearlyOperationalCosts_eur;
// EPEX balance: take from local variable v_balanceElectricityDelivery_eur
v_totalBalanceCoop_eur += v_balanceElectricity_eur;

// member balance
f_totalMembersBalance();
v_totalBalanceCoop_eur += v_balanceMembers_eur;

/*if (energyModel.b_addKpiVisuals) {
	ch_coopBalance.updateData();
}*/
/*ALCODEEND*/}

double f_totalAssetDepreciation()
{/*ALCODESTART::1692112062708*/
// Sum depreciation costs of assets in own portfolio. 
double depreciationCosts_eur=0;

/*for (Agent a : subConnectionsElectricity.getConnections()) {
	if(a instanceof ConnectionOwner){
		ConnectionOwner c = (ConnectionOwner)a;
		if( c.p_actorGroup.equals("CoopProducer")) {
			for(J_EA e:c.v_ownedGridConnection.c_energyAssets) {
			
			double depreciation_eurpkWpa = energyModel.v_assetDepreciation.path(e.energyAssetType.name()).path("Depreciation_eurpkWpa").doubleValue();
			traceln("Coop Depreciation %s eur/kW/annum", depreciation_eurpkWpa*e.getElectricCapacity_kW());
			depreciationCosts_eur+= depreciation_eurpkWpa*e.getElectricCapacity_kW();
			}
			
		}		
	} else if (a instanceof EnergyCoop) {
		
	}
	
}*/ // Deprecated get depreciation per asset from input-json. Use other data-source!

return depreciationCosts_eur;




/*ALCODEEND*/}

double f_totalMembersBalance()
{/*ALCODESTART::1692112229224*/
// Sum financial transactions from all members
for(Agent a :  c_coopMembers ) {
	if(a instanceof ConnectionOwner){
		ConnectionOwner n = (ConnectionOwner)a;
	    v_balanceMembers_eur -= n.v_balanceElectricityDelivery_eur;
		
	} else if (a instanceof EnergyCoop) {
		
	}
}

for(Agent a :  c_coopCustomers ) {
	if(a instanceof ConnectionOwner){
		ConnectionOwner n = (ConnectionOwner)a;
	    v_balanceMembers_eur -= n.v_balanceElectricityDelivery_eur;
		
	} else if (a instanceof EnergyCoop) {
		
	}
}






/*ALCODEEND*/}

double f_resetStates()
{/*ALCODESTART::1704371824571*/
v_electricitySurplusLowPassed_kW= 0;
v_totalOwnElectricityProduction_MWh = 0;
v_totalCustomerFeedIn_MWh = 0;
v_totalCustomerDelivery_MWh = 0;

acc_totalOwnElectricityProduction_kW.reset();
acc_totalCustomerDelivery_kW.reset();
acc_totalCustomerFeedIn_kW.reset();

v_rapidRunData.resetAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();

/*
//========== TOTALS ==========//
// Imports / Exports
v_totalEnergyImport_MWh = 0;
v_totalEnergyExport_MWh = 0;
fm_totalImports_MWh.clear();
fm_totalExports_MWh.clear();

// Energy / Electricity
v_totalElectricityProduced_MWh = 0;
v_totalElectricityConsumed_MWh = 0;
v_totalElectricitySelfConsumed_MWh = 0;
v_totalEnergyProduced_MWh = 0;
v_totalEnergyConsumed_MWh = 0;
v_totalEnergySelfConsumed_MWh = 0;

v_totalEnergyCurtailed_MWh = 0;
v_totalPrimaryEnergyProductionHeatpumps_MWh = 0;

v_totalOwnElectricityProduction_MWh = 0;
v_totalCustomerFeedIn_MWh = 0;
v_totalCustomerDelivery_MWh = 0;

// Accumulators
am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, energyModel.p_timeStep_h, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h );
am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, energyModel.p_timeStep_h, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h) );
am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(v_activeConsumptionEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);
am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(v_activeProductionEnergyCarriers, true, 24.0, energyModel.p_runEndTime_h - energyModel.p_runStartTime_h);

acc_dailyAverageEnergyProduction_kW.reset();
acc_dailyAverageEnergyConsumption_kW.reset();

acc_totalEnergyCurtailed_kW.reset();
acc_totalPrimaryEnergyProductionHeatpumps_kW.reset();

acc_dailyAverageBaseloadElectricityConsumption_kW.reset();
acc_dailyAverageHeatPumpElectricityConsumption_kW.reset();
acc_dailyAverageElectricVehicleConsumption_kW.reset();
acc_dailyAverageBatteriesConsumption_kW.reset();
acc_dailyAverageElectricCookingConsumption_kW.reset();
acc_dailyAverageElectrolyserElectricityConsumption_kW.reset();
acc_dailyAverageDistrictHeatingConsumption_kW.reset();

acc_dailyAveragePVProduction_kW.reset();
acc_dailyAverageWindProduction_kW.reset();
acc_dailyAverageV2GProduction_kW.reset();
acc_dailyAverageBatteriesProduction_kW.reset();
acc_dailyAverageCHPElectricityProduction_kW.reset();

//acc_dailyAverageBatteriesStoredEnergy_MWh.reset();

acc_totalOwnElectricityProduction_kW.reset();
acc_totalCustomerDelivery_kW.reset();
acc_totalCustomerFeedIn_kW.reset();

//========== SUMMER WEEK ==========//
// Imports / Exports
fm_summerWeekImports_MWh.clear();
fm_summerWeekExports_MWh.clear();
v_summerWeekEnergyImport_MWh = 0;
v_summerWeekEnergyExport_MWh = 0;

// Energy / Electricity
v_summerWeekElectricityProduced_MWh = 0;
v_summerWeekElectricityConsumed_MWh = 0;
v_summerWeekElectricitySelfConsumed_MWh = 0;
v_summerWeekEnergyProduced_MWh = 0;
v_summerWeekEnergyConsumed_MWh = 0;
v_summerWeekEnergySelfConsumed_MWh = 0;
v_summerWeekEnergyCurtailed_MWh = 0;
v_summerWeekPrimaryEnergyProductionHeatpumps_MWh = 0;

// Accumulators
am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);

acc_summerWeekEnergyProduction_kW.reset();
acc_summerWeekEnergyConsumption_kW.reset();

acc_summerWeekEnergyCurtailed_kW.reset();
acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.reset();

acc_summerWeekBaseloadElectricityConsumption_kW.reset();
acc_summerWeekHeatPumpElectricityConsumption_kW.reset();
acc_summerWeekElectricVehicleConsumption_kW.reset();
acc_summerWeekBatteriesConsumption_kW.reset();
acc_summerWeekElectricCookingConsumption_kW.reset();
acc_summerWeekElectrolyserElectricityConsumption_kW.reset();
acc_summerWeekDistrictHeatingConsumption_kW.reset();

acc_summerWeekPVProduction_kW.reset();
acc_summerWeekWindProduction_kW.reset();
acc_summerWeekV2GProduction_kW.reset();
acc_summerWeekBatteriesProduction_kW.reset();
acc_summerWeekCHPElectricityProduction_kW.reset();

//acc_summerWeekBatteriesStoredEnergy_MWh.reset();

//========== WINTER WEEK ==========//
// Imports / Exports
fm_winterWeekImports_MWh.clear();
fm_winterWeekExports_MWh.clear();
v_winterWeekEnergyImport_MWh = 0;
v_winterWeekEnergyExport_MWh = 0;

// Energy / Electricity
v_winterWeekElectricityProduced_MWh = 0;
v_winterWeekElectricityConsumed_MWh = 0;
v_winterWeekElectricitySelfConsumed_MWh = 0;
v_winterWeekEnergyProduced_MWh = 0;
v_winterWeekEnergyConsumed_MWh = 0;
v_winterWeekEnergySelfConsumed_MWh = 0;
v_winterWeekEnergyCurtailed_MWh = 0;
v_winterWeekPrimaryEnergyProductionHeatpumps_MWh = 0;

// Accumulators
am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);
am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, energyModel.p_timeStep_h, 24*7);

acc_winterWeekEnergyProduction_kW.reset();
acc_winterWeekEnergyConsumption_kW.reset();

acc_winterWeekEnergyCurtailed_kW.reset();
acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.reset();

acc_winterWeekBaseloadElectricityConsumption_kW.reset();
acc_winterWeekHeatPumpElectricityConsumption_kW.reset();
acc_winterWeekElectricVehicleConsumption_kW.reset();
acc_winterWeekBatteriesConsumption_kW.reset();
acc_winterWeekElectricCookingConsumption_kW.reset();
acc_winterWeekElectrolyserElectricityConsumption_kW.reset();
acc_winterWeekDistrictHeatingConsumption_kW.reset();

acc_winterWeekPVProduction_kW.reset();
acc_winterWeekWindProduction_kW.reset();
acc_winterWeekV2GProduction_kW.reset();
acc_winterWeekBatteriesProduction_kW.reset();
acc_winterWeekCHPElectricityProduction_kW.reset();

//acc_winterWeekBatteriesStoredEnergy_MWh.reset();

//========== DAYTIME ==========//
// Imports / Exports
fm_daytimeImports_MWh.clear();
fm_daytimeExports_MWh.clear();
v_daytimeEnergyImport_MWh = 0;
v_daytimeEnergyExport_MWh = 0;
	
// Energy / Electricity
v_daytimeElectricityProduced_MWh = 0;
v_daytimeElectricityConsumed_MWh = 0;
v_daytimeElectricitySelfConsumed_MWh = 0;
v_daytimeEnergyProduced_MWh = 0;
v_daytimeEnergyConsumed_MWh = 0;
v_daytimeEnergySelfConsumed_MWh = 0;

//========== NIGHTTIME ==========//
// Imports / Exports
fm_nighttimeImports_MWh.clear();
fm_nighttimeExports_MWh.clear();
v_nighttimeEnergyImport_MWh = 0;
v_nighttimeEnergyExport_MWh = 0;

// Energy / Electricity
v_nighttimeElectricityProduced_MWh = 0;
v_nighttimeElectricityConsumed_MWh = 0;
v_nighttimeElectricitySelfConsumed_MWh = 0;
v_nighttimeEnergyProduced_MWh = 0;
v_nighttimeEnergyConsumed_MWh = 0;
v_nighttimeEnergySelfConsumed_MWh = 0;

//========== WEEKDAY ==========//
// Imports / Exports
fm_weekdayImports_MWh.clear();
fm_weekdayExports_MWh.clear();
v_weekdayEnergyImport_MWh = 0;
v_weekdayEnergyExport_MWh = 0;

// Energy / Electricity
v_weekdayElectricityProduced_MWh = 0;
v_weekdayElectricityConsumed_MWh = 0;
v_weekdayElectricitySelfConsumed_MWh = 0;
v_weekdayEnergyProduced_MWh = 0;
v_weekdayEnergyConsumed_MWh = 0;
v_weekdayEnergySelfConsumed_MWh = 0;

//========== WEEKEND ==========//
// Imports / Exports
fm_weekendImports_MWh.clear();
fm_weekendExports_MWh.clear();
v_weekendEnergyImport_MWh = 0;
v_weekendEnergyExport_MWh = 0;

// Energy / Electricity
v_weekendElectricityProduced_MWh = 0;
v_weekendElectricityConsumed_MWh = 0;
v_weekendElectricitySelfConsumed_MWh = 0;
v_weekendEnergyProduced_MWh = 0;
v_weekendEnergyConsumed_MWh = 0;
v_weekendEnergySelfConsumed_MWh = 0;
*/

/*ALCODEEND*/}

double f_updateArrays()
{/*ALCODESTART::1715857260657*/
if (energyModel.v_isRapidRun){
	f_rapidRunDataLogging();
} else {
	//Current timestep
	double timeStep_h = energyModel.t_h-energyModel.p_runStartTime_h;
	
	//Energy carrier flows
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
		v_liveData.dsm_liveDemand_kW.get(EC).add( energyModel.t_h-energyModel.p_runStartTime_h, fm_currentConsumptionFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
		v_liveData.dsm_liveSupply_kW.get(EC).add( energyModel.t_h-energyModel.p_runStartTime_h, fm_currentProductionFlows_kW.get(EC) );
	}
	
	
	//Electricity balance
	v_liveData.data_liveElectricityBalance_kW.add(timeStep_h, fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	
	
	//Total demand and supply
	v_liveData.data_totalDemand_kW.add(timeStep_h, v_currentFinalEnergyConsumption_kW);
	v_liveData.data_totalSupply_kW.add(timeStep_h, v_currentPrimaryEnergyProduction_kW);
	
	
	//Live capacity datasets
	v_liveData.data_gridCapacityDemand_kW.add(timeStep_h, p_contractedDeliveryCapacity_kW);
	v_liveData.data_gridCapacitySupply_kW.add(timeStep_h, p_contractedFeedinCapacity_kW);
	
	
	//// Gather specific electricity flows from corresponding energy assets
	
	//Baseload electricity
	v_liveData.data_baseloadElectricityDemand_kW.add(timeStep_h, v_fixedConsumptionElectric_kW);
	
	//Cooking
	v_liveData.data_cookingElectricityDemand_kW.add(timeStep_h, v_electricHobConsumption_kW);
	
	//Hydrogen elec consumption
	v_liveData.data_hydrogenElectricityDemand_kW.add(timeStep_h, max(0, v_hydrogenElectricityConsumption_kW));
	
	//Heatpump elec consumption
	v_liveData.data_heatPumpElectricityDemand_kW.add(timeStep_h, max(0, v_heatPumpElectricityConsumption_kW));
	
	//EVs
	v_liveData.data_electricVehicleDemand_kW.add(timeStep_h, max(0,v_evChargingPowerElectric_kW));
	v_liveData.data_V2GSupply_kW.add(timeStep_h, max(0, -v_evChargingPowerElectric_kW));
	
	//Batteries
	v_liveData.data_batteryCharging_kW.add(timeStep_h, max(0, v_batteryPowerElectric_kW));		
	v_liveData.data_batteryDischarging_kW.add(timeStep_h, max(0, -v_batteryPowerElectric_kW));	
	v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(timeStep_h, v_batteryStoredEnergy_kWh/1000);
	
	//CHP production
	v_liveData.data_CHPElectricityProductionLiveWeek_kW.add(timeStep_h, v_CHPProductionElectric_kW);
	
	//PV production
	v_liveData.data_PVGeneration_kW.add(timeStep_h, v_pvProductionElectric_kW);
	
	//Wind production
	v_liveData.data_windGeneration_kW.add(timeStep_h, v_windProductionElectric_kW);	
	
	//District heating
	v_liveData.data_districtHeatDelivery_kW.add(timeStep_h, max(0,fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT)));	

}

/*ALCODEEND*/}

DataSet f_getDuurkromme()
{/*ALCODESTART::1723101436982*/
J_LoadDurationCurves j_duurkrommes = new J_LoadDurationCurves(v_rapidRunData.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW(), energyModel);

data_netbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveTotal_kW;
data_summerWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveSummer_kW;
data_winterWeekNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWinter_kW;
data_daytimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveDaytime_kW;
data_nighttimeNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveNighttime_kW;
data_weekdayNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekday_kW;
data_weekendNetbelastingDuurkromme_kW = j_duurkrommes.ds_loadDurationCurveWeekend_kW;
 
return data_netbelastingDuurkromme_kW;

/*
boolean firstRun = true;
if (data_netbelastingDuurkromme_kW != null) {	
	if (data_netbelastingDuurkrommeVorige_kW != null) { // Not second run either!
		data_netbelastingDuurkrommeVorige_kW.reset();
	} else {
		data_netbelastingDuurkrommeVorige_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));
	}
	firstRun = false;
} else {
	data_netbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));
	data_summerWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	data_winterWeekNetbelastingDuurkromme_kW = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	data_daytimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/2/energyModel.p_timeStep_h));
	data_nighttimeNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/2/energyModel.p_timeStep_h));
	data_weekdayNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/7*5/energyModel.p_timeStep_h)+100);
	data_weekendNetbelastingDuurkromme_kW = new DataSet(roundToInt(365*24/7*2/energyModel.p_timeStep_h)+100);
}

// We copy our annual array to preserve it as a time-series and make new arrays for the others
double[] netLoadArrayAnnual_kW = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries().clone(); 
int arraySize = netLoadArrayAnnual_kW.length;
double[] netLoadArraySummerweek_kW = new double[roundToInt(168 / energyModel.p_timeStep_h)];
double[] netLoadArrayWinterweek_kW= new double[roundToInt(168 / energyModel.p_timeStep_h)];
double[] netLoadArrayDaytime_kW = new double[arraySize/2];
double[] netLoadArrayNighttime_kW = new double[arraySize/2];
// For different years the amount of weekdays and weekend days may be different, so the size will be variable for now
ArrayList<Double> listNetLoadArrayWeekday_kW = new ArrayList<>();
ArrayList<Double> listNetLoadArrayWeekend_kW = new ArrayList<>();
 
int i_winter=0;
int i_summer=0;
int i_day=0;
int i_night=0;
int i_weekday=0;
int i_weekend=0;

//double[] annualElectricityBalanceTimeSeries_kW = acc_annualElectricityBalance_kW.getTimeSeries();

for(int i=0; i<arraySize ; i++) {
	if (!firstRun) {
		// First we make sure to store our previous Load Curve
		data_netbelastingDuurkrommeVorige_kW.add(i*energyModel.p_timeStep_h,data_netbelastingDuurkromme_kW.getY(i));		
	}
	// summer/winter
	if (i*energyModel.p_timeStep_h > energyModel.p_startHourSummerWeek && i*energyModel.p_timeStep_h<= energyModel.p_startHourSummerWeek+24*7) {
		netLoadArraySummerweek_kW[i_summer]=-netLoadArrayAnnual_kW[i];
		i_summer++;
	}
	if (i*energyModel.p_timeStep_h > energyModel.p_startHourWinterWeek && i*energyModel.p_timeStep_h<= energyModel.p_startHourWinterWeek+24*7) {
		netLoadArrayWinterweek_kW[i_winter]=-netLoadArrayAnnual_kW[i];
		i_winter++;
	}
	// day/night
	if (i*energyModel.p_timeStep_h % 24 > 6 && i*energyModel.p_timeStep_h % 24 <= 18) { //daytime
		netLoadArrayDaytime_kW[i_day]=-netLoadArrayAnnual_kW[i];
		i_day++;
	} else {
		netLoadArrayNighttime_kW[i_night]=-netLoadArrayAnnual_kW[i];
		i_night++;
	}
	//Weekday/weekend
	if (((i*energyModel.p_timeStep_h+ 24*(energyModel.v_dayOfWeek1jan-1)) % (24*7)) < (24*5)) { // Simulation starts on a Thursday, hence the +3 day offset on t_h
		listNetLoadArrayWeekday_kW.add(-netLoadArrayAnnual_kW[i]);
		i_weekday++;
	} else {
		listNetLoadArrayWeekend_kW.add(-netLoadArrayAnnual_kW[i]);
		i_weekend++;
	}
	
}
 
// Now we have the size of the weekday & weekend arrays.
double[] netLoadArrayWeekday_kW = new double[listNetLoadArrayWeekday_kW.size()];
double[] netLoadArrayWeekend_kW = new double[listNetLoadArrayWeekend_kW.size()];
for (int i = 0; i < listNetLoadArrayWeekday_kW.size(); i++) {
	netLoadArrayWeekday_kW[i] = listNetLoadArrayWeekday_kW.get(i);
}
for (int i = 0; i < listNetLoadArrayWeekend_kW.size(); i++) {
	netLoadArrayWeekend_kW[i] = listNetLoadArrayWeekend_kW.get(i);
}
 
 
// Sort all arrays
Arrays.parallelSort(netLoadArrayAnnual_kW);
Arrays.parallelSort(netLoadArraySummerweek_kW);
Arrays.parallelSort(netLoadArrayWinterweek_kW);
Arrays.parallelSort(netLoadArrayDaytime_kW);
Arrays.parallelSort(netLoadArrayNighttime_kW);
Arrays.parallelSort(netLoadArrayWeekday_kW);
Arrays.parallelSort(netLoadArrayWeekend_kW);
 
// Write results to datasets
// Netbelastingduurkromme year
//if (!firstRun) {
	data_netbelastingDuurkromme_kW.reset();
	data_summerWeekNetbelastingDuurkromme_kW.reset();	
	data_winterWeekNetbelastingDuurkromme_kW.reset();
	data_daytimeNetbelastingDuurkromme_kW.reset();
	data_nighttimeNetbelastingDuurkromme_kW.reset();
	data_weekdayNetbelastingDuurkromme_kW.reset();
	data_weekendNetbelastingDuurkromme_kW.reset();
//}
for(int i=0; i< arraySize; i++) {
	data_netbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, netLoadArrayAnnual_kW[arraySize-i-1]);
}
// Netbelastingduurkromme summer / winter
arraySize = netLoadArraySummerweek_kW.length;
for(int i=0; i< arraySize; i++) {
	data_summerWeekNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArraySummerweek_kW[i]);
}
arraySize = netLoadArrayWinterweek_kW.length;
for(int i=0; i< arraySize; i++) {
	data_winterWeekNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayWinterweek_kW[i]);
}
// Netbelastingduurkromme day / night
arraySize = netLoadArrayDaytime_kW.length;
for(int i=0; i< arraySize; i++) {
	data_daytimeNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayDaytime_kW[i]);
}
arraySize = netLoadArrayNighttime_kW.length;
for(int i=0; i< arraySize; i++) {
	data_nighttimeNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayNighttime_kW[i]);
}
// Netbelastingduurkromme weekday / weekend
arraySize = netLoadArrayWeekday_kW.length;
for(int i=0; i< arraySize; i++) {
	data_weekdayNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayWeekday_kW[i]);
}
arraySize = netLoadArrayWeekend_kW.length;
for(int i=0; i< arraySize; i++) {
	data_weekendNetbelastingDuurkromme_kW.add(i*energyModel.p_timeStep_h, -netLoadArrayWeekend_kW[i]);
}
 
return data_netbelastingDuurkromme_kW;
*/
/*ALCODEEND*/}

double f_fillAnnualDatasetsOLD()
{/*ALCODESTART::1723103918133*/
double v_currentPowerElectricity_kW = - v_electricitySurplus_kW;
double v_currentPowerMethane_kW = v_methaneVolume_kWh / energyModel.p_timeStep_h;
double v_currentPowerHydrogen_kW = v_hydrogenVolume_kWh / energyModel.p_timeStep_h;
double v_currentPowerDiesel_kW = v_dieselVolume_kWh / energyModel.p_timeStep_h;
double v_currentPowerHeat_kW = v_heatVolume_kWh / energyModel.p_timeStep_h;

//double currentImport_kW = (max(0,v_currentPowerElectricity_kW) + max(0,v_currentPowerHeat_kW) + max(0,v_currentPowerMethane_kW) + max(0,v_currentPowerHydrogen_kW) + max(0,v_currentPowerDiesel_kW));
//double currentExport_kW = (max(0,-v_currentPowerElectricity_kW) + max(0,-v_currentPowerHeat_kW) + max(0,-v_currentPowerMethane_kW) + max(0,-v_currentPowerHydrogen_kW) + max(0,-v_currentPowerDiesel_kW));

double currentImport_MWh = (max(0,v_electricityVolume_kWh) + max(0,v_heatVolume_kWh) + max(0,v_methaneVolume_kWh) + max(0,v_hydrogenVolume_kWh) + max(0,v_dieselVolume_kWh))/1000;
double currentExport_MWh = (max(0,-v_electricityVolume_kWh) + max(0,-v_heatVolume_kWh) + max(0,-v_methaneVolume_kWh) + max(0,-v_hydrogenVolume_kWh) + max(0,-v_dieselVolume_kWh))/1000;
double currentImport_kW = currentImport_MWh * 1000 / energyModel.p_timeStep_h;
double currentExport_kW = currentExport_MWh * 1000 / energyModel.p_timeStep_h;
//traceln("ElectricityVolume_kWh: %s kWh, current production: %s kW", v_electricityVolume_kWh, v_currentOwnElectricityProduction_kW);	
v_totalEnergyConsumed_MWh += (v_currentEnergyConsumption_kW * energyModel.p_timeStep_h)/1000;
v_totalEnergyProduced_MWh += (v_currentEnergyProduction_kW * energyModel.p_timeStep_h)/1000;
v_totalEnergyCurtailed_MWh += (v_currentEnergyCurtailed_kW * energyModel.p_timeStep_h)/1000;

//data_annualCoopElectricityBalance_kW.add(energyModel.t_h, v_electricitySurplus_kW);
acc_annualElectricityBalance_kW.addStep( -v_electricitySurplus_kW);
acc_annualElectricityProduction_kW.addStep( v_currentElectricityProduction_kW);
acc_annualElectricityConsumption_kW.addStep( v_currentElectricityConsumption_kW);
acc_annualOwnElectricityProduction_kW.addStep( v_currentOwnElectricityProduction_kW);
acc_annualCustomerFeedIn_kW.addStep( v_currentCustomerFeedIn_kW);

acc_annualMethaneBalance_kW.addStep( v_methaneVolume_kWh/energyModel.p_timeStep_h);
acc_annualHydrogenBalance_kW.addStep( v_hydrogenVolume_kWh/energyModel.p_timeStep_h);
acc_annualDieselBalance_kW.addStep( v_dieselVolume_kWh/energyModel.p_timeStep_h);

// Demand
v_dailyBaseloadElectricityDemand_kW += v_fixedConsumptionElectric_kW;
v_dailyHeatPumpElectricityDemand_kW += v_heatPumpElectricityConsumption_kW;
v_dailyElectricVehicleDemand_kW += max(0,v_evChargingPowerElectric_kW);
v_dailyBatteriesDemand_kW += max(0,v_batteryPowerElectric_kW);
v_dailyNaturalGasDemand_kW += max(0, v_currentPowerMethane_kW);
v_dailyDieselDemand_kW += max(0, v_currentPowerDiesel_kW);
v_dailyHydrogenDemand_kW += max(0, v_currentPowerHydrogen_kW);
//v_dailyDistrictHeatDemand_kWh += 
// Supply
v_dailyPVGeneration_kW += v_pvProductionElectric_kW;
v_dailyWindGeneration_kW += v_windProductionElectric_kW;
v_dailyBatteriesSupply_kW += max(0,-v_batteryPowerElectric_kW);
v_dailyV2GSupply_kW += -min(0,v_evChargingPowerElectric_kW);
v_dailyNaturalGasSupply_kW += max(0, -v_currentPowerMethane_kW);
v_dailyHydrogenSupply_kW += max(0, -v_currentPowerHydrogen_kW);

//v_dailyDistrictHeatSupply_kWh += 

if (energyModel.t_h % 24 == 24-energyModel.p_timeStep_h) {
	//data_annualElectricityDemand_MWh.add(energyModel.t_h, v_dailyElectricityDemand_kWh/(24 / energyModel.p_timeStep_h));
	//data_annualElectricitySupply_MWh.add(energyModel.t_h, v_dailyElectricitySupply_kWh/(24 / energyModel.p_timeStep_h)); 
	// Demand
	data_annualBaseloadElectricityDemand_kW.add(energyModel.t_h, v_dailyBaseloadElectricityDemand_kW/(24 / energyModel.p_timeStep_h));
	data_annualHeatPumpElectricityDemand_kW.add(energyModel.t_h, v_dailyHeatPumpElectricityDemand_kW/(24 / energyModel.p_timeStep_h));
	data_annualElectricVehicleDemand_kW.add(energyModel.t_h, v_dailyElectricVehicleDemand_kW/(24 / energyModel.p_timeStep_h));
	data_annualBatteriesDemand_kW.add(energyModel.t_h, v_dailyBatteriesDemand_kW/(24 / energyModel.p_timeStep_h));
	data_annualNaturalGasDemand_kW.add(energyModel.t_h, v_dailyNaturalGasDemand_kW/(24 / energyModel.p_timeStep_h));
	data_annualDieselDemand_kW.add(energyModel.t_h, v_dailyDieselDemand_kW/(24 / energyModel.p_timeStep_h));
	data_annualHydrogenDemand_kW.add(energyModel.t_h, v_dailyHydrogenDemand_kW/(24 / energyModel.p_timeStep_h));
	//data_annualDistrictHeatSupply_MWh.add(energyModel.t_h, v_dailyDistrictHeatDemand_kWh/(24 / energyModel.p_timeStep_h));
	// Supply
	data_annualPVGeneration_kW.add(energyModel.t_h, v_dailyPVGeneration_kW/(24 / energyModel.p_timeStep_h));
	data_annualWindGeneration_kW.add(energyModel.t_h, v_dailyWindGeneration_kW/(24 / energyModel.p_timeStep_h));
	data_annualBatteriesSupply_kW.add(energyModel.t_h, v_dailyBatteriesSupply_kW/(24 / energyModel.p_timeStep_h));
	data_annualV2GSupply_kW.add(energyModel.t_h, v_dailyV2GSupply_kW/(24 / energyModel.p_timeStep_h));
	data_annualNaturalGasSupply_kW.add(energyModel.t_h, v_dailyNaturalGasSupply_kW/(24 / energyModel.p_timeStep_h));
	data_annualHydrogenSupply_kW.add(energyModel.t_h, v_dailyHydrogenSupply_kW/(24 / energyModel.p_timeStep_h));
	//data_annualDistrictHeatSupply_MWh.add(energyModel.t_h, v_dailyDistrictHeatSupply_kWh/(24 / energyModel.p_timeStep_h));

	// Resetting the daily values
	//v_dailyElectricityDemand_kWh = 0;
	//v_dailyElectricitySupply_kWh = 0;
	v_dailyBaseloadElectricityDemand_kW = 0;
	v_dailyHeatPumpElectricityDemand_kW = 0;
	v_dailyElectricVehicleDemand_kW = 0;
	v_dailyBatteriesDemand_kW = 0;
	v_dailyNaturalGasDemand_kW = 0;
	v_dailyDieselDemand_kW = 0;
	v_dailyHydrogenDemand_kW = 0;
	//v_dailyDistrictHeatDemand_kWh = 0;
	v_dailyPVGeneration_kW = 0;
	v_dailyWindGeneration_kW = 0;
	v_dailyBatteriesSupply_kW = 0;
	v_dailyV2GSupply_kW = 0;
	v_dailyNaturalGasSupply_kW = 0;
	v_dailyHydrogenSupply_kW = 0;
	//v_dailyDistrictHeatSupply_kWh = 0;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Summer week
if (energyModel.t_h >= energyModel.p_startHourSummerWeek && energyModel.t_h < energyModel.p_startHourSummerWeek + 24*7){
	data_summerWeekBaseloadElectricityDemand_kW.add(energyModel.t_h, v_fixedConsumptionElectric_kW);
	data_summerWeekHeatPumpElectricityDemand_kW.add(energyModel.t_h, v_heatPumpElectricityConsumption_kW);
	data_summerWeekElectricVehicleDemand_kW.add(energyModel.t_h, max(0,v_evChargingPowerElectric_kW));
	data_summerWeekBatteriesDemand_kW.add(energyModel.t_h, max(0,v_batteryPowerElectric_kW));
	data_summerWeekNaturalGasDemand_kW.add(energyModel.t_h, max(0, v_currentPowerMethane_kW));
	data_summerWeekDieselDemand_kW.add(energyModel.t_h, max(0, v_currentPowerDiesel_kW));
	data_summerWeekHydrogenDemand_kW.add(energyModel.t_h, max(0, v_currentPowerHydrogen_kW));
	
	data_summerWeekPVGeneration_kW.add(energyModel.t_h, v_pvProductionElectric_kW);
	data_summerWeekWindGeneration_kW.add(energyModel.t_h, v_windProductionElectric_kW);
	data_summerWeekBatteriesSupply_kW.add(energyModel.t_h, max(0,-v_batteryPowerElectric_kW));
	data_summerWeekV2GSupply_kW.add(energyModel.t_h, max(0, -v_evChargingPowerElectric_kW));
	data_summerWeekNaturalGasSupply_kW.add(energyModel.t_h, max(0, -v_currentPowerMethane_kW));
	data_summerWeekHydrogenSupply_kW.add(energyModel.t_h, max(0, -v_currentPowerHydrogen_kW));
	
	acc_summerElectricityBalance_kW.addStep(v_currentPowerElectricity_kW);
	acc_summerMethaneBalance_kW.addStep(v_currentPowerMethane_kW);
	acc_summerHydrogenBalance_kW.addStep(v_currentPowerHydrogen_kW);
	acc_summerDieselBalance_kW.addStep(v_currentPowerDiesel_kW);
	acc_summerHeatBalance_kW.addStep(v_currentPowerHeat_kW);
	acc_summerTotalImport_kW.addStep(currentImport_kW);
	acc_summerTotalExport_kW.addStep(currentExport_kW);
	
	acc_summerEnergyProduction_kW.addStep(v_currentEnergyProduction_kW);
	acc_summerEnergyConsumption_kW.addStep(v_currentEnergyConsumption_kW);
	acc_summerEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	acc_summerElectricityProduction_kW.addStep(v_currentElectricityProduction_kW);
	acc_summerElectricityConsumption_kW.addStep(v_currentElectricityConsumption_kW);
	//acc_summerHeatProduction_kW.addStep(v_currentEnergyProductionHeat_kW);	
	
}

//Winter week
if (energyModel.t_h >= energyModel.p_startHourWinterWeek && energyModel.t_h < energyModel.p_startHourWinterWeek + 24*7){
	data_winterWeekBaseloadElectricityDemand_kW.add(energyModel.t_h, v_fixedConsumptionElectric_kW);
	data_winterWeekHeatPumpElectricityDemand_kW.add(energyModel.t_h, v_heatPumpElectricityConsumption_kW);
	data_winterWeekElectricVehicleDemand_kW.add(energyModel.t_h, max(0, v_evChargingPowerElectric_kW));
	data_winterWeekBatteriesDemand_kW.add(energyModel.t_h, max(0, v_batteryPowerElectric_kW));
	data_winterWeekNaturalGasDemand_kW.add(energyModel.t_h, max(0, v_currentPowerMethane_kW));
	data_winterWeekDieselDemand_kW.add(energyModel.t_h, max(0, v_currentPowerDiesel_kW));
	data_winterWeekHydrogenDemand_kW.add(energyModel.t_h, max(0, v_currentPowerHydrogen_kW));
	
	data_winterWeekPVGeneration_kW.add(energyModel.t_h, v_pvProductionElectric_kW);
	data_winterWeekWindGeneration_kW.add(energyModel.t_h, v_windProductionElectric_kW);
	data_winterWeekBatteriesSupply_kW.add(energyModel.t_h, max(0, -v_batteryPowerElectric_kW));
	data_winterWeekV2GSupply_kW.add(energyModel.t_h, max(0, -v_evChargingPowerElectric_kW));
	data_winterWeekNaturalGasSupply_kW.add(energyModel.t_h, max(0, -v_currentPowerMethane_kW));
	data_winterWeekHydrogenSupply_kW.add(energyModel.t_h, max(0, -v_currentPowerHydrogen_kW));
	
	acc_winterElectricityBalance_kW.addStep(v_currentPowerElectricity_kW);
	acc_winterMethaneBalance_kW.addStep(v_currentPowerMethane_kW);
	acc_winterHydrogenBalance_kW.addStep(v_currentPowerHydrogen_kW);
	acc_winterDieselBalance_kW.addStep(v_currentPowerDiesel_kW);
	acc_winterHeatBalance_kW.addStep(v_currentPowerHeat_kW);
	acc_winterTotalImport_kW.addStep(currentImport_kW);
	acc_winterTotalExport_kW.addStep(currentExport_kW);
	
	acc_winterEnergyProduction_kW.addStep(v_currentEnergyProduction_kW);
	acc_winterEnergyConsumption_kW.addStep(v_currentEnergyConsumption_kW);
	acc_winterEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	acc_winterElectricityProduction_kW.addStep(v_currentElectricityProduction_kW);
	acc_winterElectricityConsumption_kW.addStep(v_currentElectricityConsumption_kW);
	//acc_winterHeatProduction_kW.addStep(v_currentEnergyProductionHeat_kW);	
	
}

// Daytime totals. Use overal-total minus daytime total to get nighttime totals.

if(energyModel.t_h % 24 > 6 && energyModel.t_h % 24 < 18) { // Daytime totals. Use overal-total minus daytime total to get nighttime totals.
	
	v_daytimeTotalImport_MWh += currentImport_MWh;
	v_daytimeTotalExport_MWh += currentExport_MWh;
	v_daytimeEnergyConsumed_MWh += v_currentEnergyConsumption_kW / 1000 * energyModel.p_timeStep_h;
	v_daytimeEnergyProduced_MWh += v_currentEnergyProduction_kW / 1000 * energyModel.p_timeStep_h;
		
	v_daytimeMethaneImport_MWh += v_currentPowerMethane_kW / 1000 * energyModel.p_timeStep_h;
	v_daytimeDieselImport_MWh += v_currentPowerDiesel_kW / 1000 * energyModel.p_timeStep_h;
	v_daytimeHydrogenImport_MWh += max(0, v_currentPowerHydrogen_kW) / 1000 * energyModel.p_timeStep_h;
	v_daytimeHydrogenExport_MWh += max(0, -v_currentPowerHydrogen_kW) / 1000 * energyModel.p_timeStep_h;
	
	v_daytimeElectricityConsumed_MWh += v_currentElectricityConsumption_kW / 1000 * energyModel.p_timeStep_h;
	v_daytimeElectricityProduced_MWh += v_currentElectricityProduction_kW / 1000 * energyModel.p_timeStep_h;		
	v_daytimeElectricityExport_MWh += max(0,-v_electricityVolume_kWh) /1000;	
	v_daytimeElectricityImport_MWh += max(0,v_electricityVolume_kWh) /1000;
	
} 
// Weekday totals. Use overal-totals minus weekday totals to get weekend totals.
if ((energyModel.t_h+(energyModel.v_dayOfWeek1jan - 1)*24) % (24*7) < (24*5)) { // Simulation starts on a Thursday, hence the +3 day offset on t_h
	
	v_weekdayTotalImport_MWh += currentImport_MWh;
	v_weekdayTotalExport_MWh += currentExport_MWh;

	v_weekdayEnergyConsumed_MWh += v_currentEnergyConsumption_kW * energyModel.p_timeStep_h/1000;
	v_weekdayEnergyProduced_MWh += v_currentEnergyProduction_kW * energyModel.p_timeStep_h/1000;
	
	v_weekdayMethaneImport_MWh += v_currentPowerMethane_kW / 1000 * energyModel.p_timeStep_h;
	v_weekdayDieselImport_MWh += v_currentPowerDiesel_kW / 1000 * energyModel.p_timeStep_h;
	v_weekdayHydrogenImport_MWh += max(0, v_currentPowerHydrogen_kW) / 1000 * energyModel.p_timeStep_h;
	v_weekdayHydrogenExport_MWh += max(0, -v_currentPowerHydrogen_kW) / 1000 * energyModel.p_timeStep_h;
	
	v_weekdayElectricityConsumed_MWh += v_currentElectricityConsumption_kW / 1000 * energyModel.p_timeStep_h;
	v_weekdayElectricityProduced_MWh += v_currentElectricityProduction_kW / 1000 * energyModel.p_timeStep_h;		
	v_weekdayElectricityExport_MWh += max(0,-v_electricityVolume_kWh) /1000;
	v_weekdayElectricityImport_MWh += max(0,v_electricityVolume_kWh) /1000;	
} 

/*ALCODEEND*/}

double f_fillAnnualDatasets()
{/*ALCODESTART::1731079736315*/
// EnergyCoop specific
acc_totalOwnElectricityProduction_kW.addStep( v_currentOwnElectricityProduction_kW );
acc_totalCustomerDelivery_kW.addStep( v_currentCustomerDelivery_kW );
acc_totalCustomerFeedIn_kW.addStep( v_currentCustomerFeedIn_kW );

// GridConnection generic
double currentImport_kW = 0.0;
double currentExport_kW = 0.0;
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
	currentImport_kW += max( 0, currentBalance_kW );
	currentExport_kW += max( 0, -currentBalance_kW );
	am_totalBalanceAccumulators_kW.get(EC).addStep(  currentBalance_kW );
}

// Daytime totals. Use overal-total minus daytime total to get nighttime totals.
if(energyModel.b_isDaytime) { 
	
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		am_daytimeImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		am_daytimeExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	acc_daytimeElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	acc_daytimeElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );	
	acc_daytimeEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_daytimeEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);	
}

// Weekend totals. Use overal-totals minus weekend totals to get weekday totals.
if (!energyModel.b_isWeekday) { // 
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		am_weekendImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		am_weekendExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	acc_weekendElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	acc_weekendElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	acc_weekendEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_weekendEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
}

//========== SUMMER WEEK ==========//
if (energyModel.b_isSummerWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		am_summerWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
		am_summerWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );

	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
		am_summerWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
	}
	
	acc_summerWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_summerWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

	acc_summerWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

	acc_summerWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	acc_summerWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	acc_summerWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	acc_summerWeekBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
	acc_summerWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	acc_summerWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	acc_summerWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	acc_summerWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	acc_summerWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	acc_summerWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	acc_summerWeekBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
	acc_summerWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
	//acc_summerWeekBatteriesStoredEnergy_MWh.addStep();		

}

//========== WINTER WEEK ==========// 
if (energyModel.b_isWinterWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		am_winterWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
		am_winterWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );

	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
		am_winterWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
	}
	
	acc_winterWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	acc_winterWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
	
	acc_winterWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

	acc_winterWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	acc_winterWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	acc_winterWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	acc_winterWeekBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
	acc_winterWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	acc_winterWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	acc_winterWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	acc_winterWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	acc_winterWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	acc_winterWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	acc_winterWeekBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
	acc_winterWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
	//acc_winterWeekBatteriesStoredEnergy_MWh.addStep();		
	
}


//========== TOTALS / DAILY AVERAGES ==========//
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
    am_dailyAverageConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );

}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
    am_dailyAverageProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
}

acc_dailyAverageEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
acc_dailyAverageEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
acc_totalEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
acc_totalPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);

//acc_dailyAverageDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
//acc_dailyAverageFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);

acc_dailyAverageBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
acc_dailyAverageHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
acc_dailyAverageElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
acc_dailyAverageBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
acc_dailyAverageElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
acc_dailyAverageElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
acc_dailyAverageDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );

acc_dailyAveragePVProduction_kW.addStep( v_pvProductionElectric_kW );
acc_dailyAverageWindProduction_kW.addStep( v_windProductionElectric_kW );
acc_dailyAverageV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
acc_dailyAverageBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
acc_dailyAverageCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
//acc_dailyAverageBatteriesStoredEnergy_MWh.addStep();		

/*ALCODEEND*/}

double f_calculateKPIs()
{/*ALCODESTART::1731081139333*/
//Cumulative KPIs of each grid connection individually
f_getCumulativeIndividualGCValues();

//Costumer delivery and feedin
v_totalCustomerFeedIn_MWh = acc_totalCustomerFeedIn_kW.getIntegral_kWh() / 1000;
v_totalCustomerDelivery_MWh = acc_totalCustomerDelivery_kW.getIntegral_kWh() / 1000;
v_totalOwnElectricityProduction_MWh = acc_totalOwnElectricityProduction_kW.getIntegral_kWh() / 1000;

/*
//========== TOTALS ==========//
// Get import / export from balance accumulators.
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_totalImports_MWh.put( EC, am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 );
	fm_totalExports_MWh.put( EC, -am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 );
}

// Sum up the import / export totals
v_totalEnergyImport_MWh = fm_totalImports_MWh.totalSum();
v_totalEnergyExport_MWh = fm_totalExports_MWh.totalSum();

// Electricity totals from production / consumption accumulators and selfconsumption
v_totalElectricityConsumed_MWh = am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_totalElectricityProduced_MWh=  am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_totalElectricitySelfConsumed_MWh = max(0, v_totalElectricityConsumed_MWh - fm_totalImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

// Energy totals from production / consumption accumulators and selfconsumption
v_totalEnergyProduced_MWh = acc_dailyAverageEnergyProduction_kW.getIntegral_kWh() / 1000;
v_totalEnergyConsumed_MWh = acc_dailyAverageEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_totalEnergySelfConsumed_MWh = max(0, v_totalEnergyConsumed_MWh - v_totalEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

// Other totals from accumulators
v_totalPrimaryEnergyProductionHeatpumps_MWh = acc_totalPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
v_totalEnergyCurtailed_MWh = acc_totalEnergyCurtailed_kW.getIntegral_kWh() / 1000;


//========== SUMMER WEEK ==========//
// Summerweek KPIs are calculated from ZeroAccumulators
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_summerWeekImports_MWh.put( EC, am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 );
	fm_summerWeekExports_MWh.put( EC, -am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 );
}

v_summerWeekEnergyImport_MWh = fm_summerWeekImports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();
v_summerWeekEnergyExport_MWh = fm_summerWeekExports_MWh.totalSum();//.values().stream().mapToDouble(Double::doubleValue).sum();

v_summerWeekElectricityProduced_MWh = am_summerWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_summerWeekElectricityConsumed_MWh = am_summerWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_summerWeekElectricitySelfConsumed_MWh = max(0, v_summerWeekElectricityConsumed_MWh - fm_summerWeekImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_summerWeekEnergyConsumed_MWh = acc_summerWeekEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_summerWeekEnergyProduced_MWh = acc_summerWeekEnergyProduction_kW.getIntegral_kWh() / 1000;
v_summerWeekEnergySelfConsumed_MWh = max(0, v_summerWeekEnergyConsumed_MWh - v_summerWeekEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

double v_summerWeekSelfConsumedEnergyCheck_MWh = v_summerWeekEnergyProduced_MWh - v_summerWeekEnergyExport_MWh;

v_summerWeekPrimaryEnergyProductionHeatpumps_MWh = acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
v_summerWeekEnergyCurtailed_MWh = acc_summerWeekEnergyCurtailed_kW.getIntegral_kWh() / 1000;

//========== WINTER WEEK ==========//
// Winterweek KPIs are calculated from ZeroAccumulators
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_winterWeekImports_MWh.put( EC, am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 );
	fm_winterWeekExports_MWh.put( EC, -am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 );
}

v_winterWeekEnergyImport_MWh = fm_winterWeekImports_MWh.totalSum();
v_winterWeekEnergyExport_MWh = fm_winterWeekExports_MWh.totalSum();

v_winterWeekElectricityProduced_MWh = am_winterWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_winterWeekElectricityConsumed_MWh = am_winterWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
v_winterWeekElectricitySelfConsumed_MWh = max(0,v_winterWeekElectricityConsumed_MWh - fm_winterWeekImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

v_winterWeekEnergyConsumed_MWh = acc_winterWeekEnergyConsumption_kW.getIntegral_kWh() / 1000;
v_winterWeekEnergyProduced_MWh = acc_winterWeekEnergyProduction_kW.getIntegral_kWh() / 1000;
v_winterWeekEnergySelfConsumed_MWh = max(0,v_winterWeekEnergyConsumed_MWh - v_winterWeekEnergyImport_MWh); // Putting positive delta-stored energy here assumes this energy was imported as opposed to self-produced. Putting negative delta-stored energy here assumes this energy was self-consumed, as opposed to exported.	

double v_winterWeekSelfConsumedEnergyCheck_MWh = v_winterWeekEnergyProduced_MWh - v_winterWeekEnergyExport_MWh;

v_winterWeekPrimaryEnergyProductionHeatpumps_MWh = acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
v_winterWeekEnergyCurtailed_MWh = acc_winterWeekEnergyCurtailed_kW.getIntegral_kWh() / 1000;


//========== DAYTIME ==========//
// Daytime KPIs are calculated from ZeroAccumulators
for (var EC : v_activeEnergyCarriers){
	fm_daytimeImports_MWh.addFlow(EC, am_daytimeImports_kW.get(EC).getIntegral_kWh() / 1000);
	fm_daytimeExports_MWh.addFlow(EC, am_daytimeExports_kW.get(EC).getIntegral_kWh() / 1000);
}
v_daytimeEnergyImport_MWh = fm_daytimeImports_MWh.totalSum();
v_daytimeEnergyExport_MWh = fm_daytimeExports_MWh.totalSum();

v_daytimeElectricityProduced_MWh = acc_daytimeElectricityProduction_kW.getIntegral_kWh() / 1000;
v_daytimeElectricityConsumed_MWh =  acc_daytimeElectricityConsumption_kW.getIntegral_kWh() / 1000;

v_daytimeEnergyProduced_MWh = acc_daytimeEnergyProduction_kW.getIntegral_kWh() / 1000;
v_daytimeEnergyConsumed_MWh = acc_daytimeEnergyConsumption_kW.getIntegral_kWh() / 1000;

v_daytimeEnergySelfConsumed_MWh = max(0, v_daytimeEnergyProduced_MWh - v_daytimeEnergyExport_MWh);
v_daytimeElectricitySelfConsumed_MWh = max(0, v_daytimeElectricityConsumed_MWh - am_daytimeImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000);


//========== NIGHTTIME ==========//
// Nighttime KPIs are calculated by total - daytime
v_nighttimeEnergyExport_MWh = v_totalEnergyExport_MWh - v_daytimeEnergyExport_MWh;
v_nighttimeEnergyImport_MWh = v_totalEnergyImport_MWh - v_daytimeEnergyImport_MWh;
v_nighttimeEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_daytimeEnergyConsumed_MWh;
v_nighttimeEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_daytimeEnergyProduced_MWh;

v_nighttimeElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_daytimeElectricityConsumed_MWh;
v_nighttimeElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_daytimeElectricityProduced_MWh;

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_nighttimeImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - am_daytimeImports_kW.get(EC).getIntegral_kWh() / 1000 );
	fm_nighttimeExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - am_daytimeExports_kW.get(EC).getIntegral_kWh() / 1000 );
}

v_nighttimeEnergySelfConsumed_MWh = max(0, v_nighttimeEnergyProduced_MWh - v_nighttimeEnergyExport_MWh);
v_nighttimeElectricitySelfConsumed_MWh = max(0,v_nighttimeElectricityConsumed_MWh - fm_nighttimeImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

//========== WEEKEND ==========//
// Weekend KPIs are calculated from ZeroAccumulators
for (var EC : v_activeEnergyCarriers){
	fm_weekendImports_MWh.addFlow(EC, am_weekendImports_kW.get(EC).getIntegral_kWh() / 1000);
	fm_weekendExports_MWh.addFlow(EC, am_weekendExports_kW.get(EC).getIntegral_kWh() / 1000);
}
v_weekendEnergyImport_MWh = fm_weekendImports_MWh.totalSum();
v_weekendEnergyExport_MWh = fm_weekendExports_MWh.totalSum();

v_weekendElectricityProduced_MWh = acc_weekendElectricityProduction_kW.getIntegral_kWh() / 1000;
v_weekendElectricityConsumed_MWh =  acc_weekendElectricityConsumption_kW.getIntegral_kWh() / 1000;

v_weekendEnergyProduced_MWh = acc_weekendEnergyProduction_kW.getIntegral_kWh() / 1000;
v_weekendEnergyConsumed_MWh =  acc_weekendEnergyConsumption_kW.getIntegral_kWh() / 1000;

v_weekendEnergySelfConsumed_MWh = max(0, v_weekendEnergyProduced_MWh - v_weekendEnergyExport_MWh);
v_weekendElectricitySelfConsumed_MWh = max(0, v_weekendElectricityConsumed_MWh - am_weekendImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000);

//========== WEEKDAY ==========//
// Weekday KPIs are calculated by total - weekend
v_weekdayEnergyExport_MWh = v_totalEnergyExport_MWh - v_weekendEnergyExport_MWh;
v_weekdayEnergyImport_MWh = v_totalEnergyImport_MWh - v_weekendEnergyImport_MWh;
v_weekdayEnergyConsumed_MWh = v_totalEnergyConsumed_MWh - v_weekendEnergyConsumed_MWh;
v_weekdayEnergyProduced_MWh = v_totalEnergyProduced_MWh - v_weekendEnergyProduced_MWh;
v_weekdayEnergySelfConsumed_MWh = max(0, v_weekendEnergyProduced_MWh - v_weekendEnergyExport_MWh);

v_weekdayElectricityConsumed_MWh = v_totalElectricityConsumed_MWh - v_weekendElectricityConsumed_MWh;
v_weekdayElectricityProduced_MWh = v_totalElectricityProduced_MWh - v_weekendElectricityProduced_MWh;

for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	fm_weekdayImports_MWh.put( EC, fm_totalImports_MWh.get(EC) - am_weekendImports_kW.get(EC).getIntegral_kWh() / 1000 );
	fm_weekdayExports_MWh.put( EC, fm_totalExports_MWh.get(EC) - am_weekendExports_kW.get(EC).getIntegral_kWh() / 1000 );
}

v_weekdayEnergySelfConsumed_MWh = max(0, v_weekdayEnergyProduced_MWh - v_weekdayEnergyExport_MWh);
v_weekdayElectricitySelfConsumed_MWh = max(0,v_weekdayElectricityConsumed_MWh - fm_weekdayImports_MWh.get(OL_EnergyCarriers.ELECTRICITY));

*/


/*ALCODEEND*/}

double f_collectGridConnectionTotals()
{/*ALCODESTART::1739970817879*/
// Make collective profiles, electricity per timestep, other energy carriers per day!

for (GridConnection gc : c_memberGridConnections) {
	v_rapidRunData.am_totalBalanceAccumulators_kW.add(gc.v_rapidRunData.am_totalBalanceAccumulators_kW);
	v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW.add(gc.v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW);
	v_rapidRunData.am_dailyAverageProductionAccumulators_kW.add(gc.v_rapidRunData.am_dailyAverageProductionAccumulators_kW);
	v_rapidRunData.acc_dailyAverageEnergyProduction_kW.add(gc.v_rapidRunData.acc_dailyAverageEnergyProduction_kW);
	v_rapidRunData.acc_dailyAverageEnergyConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageEnergyConsumption_kW);
	v_rapidRunData.acc_totalEnergyCurtailed_kW.add(gc.v_rapidRunData.acc_totalEnergyCurtailed_kW);
	v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW.add(gc.v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW);
	v_rapidRunData.am_summerWeekBalanceAccumulators_kW.add(gc.v_rapidRunData.am_summerWeekBalanceAccumulators_kW);
	v_rapidRunData.am_summerWeekConsumptionAccumulators_kW.add(gc.v_rapidRunData.am_summerWeekConsumptionAccumulators_kW);
	v_rapidRunData.am_summerWeekProductionAccumulators_kW.add(gc.v_rapidRunData.am_summerWeekProductionAccumulators_kW);
	v_rapidRunData.acc_summerWeekEnergyProduction_kW.add(gc.v_rapidRunData.acc_summerWeekEnergyProduction_kW);
	v_rapidRunData.acc_summerWeekEnergyConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekEnergyConsumption_kW);
	v_rapidRunData.acc_summerWeekEnergyCurtailed_kW.add(gc.v_rapidRunData.acc_summerWeekEnergyCurtailed_kW);
	v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.add(gc.v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW);
	v_rapidRunData.am_winterWeekBalanceAccumulators_kW.add(gc.v_rapidRunData.am_winterWeekBalanceAccumulators_kW);
	v_rapidRunData.am_winterWeekConsumptionAccumulators_kW.add(gc.v_rapidRunData.am_winterWeekConsumptionAccumulators_kW);
	v_rapidRunData.am_winterWeekProductionAccumulators_kW.add(gc.v_rapidRunData.am_winterWeekProductionAccumulators_kW);
	v_rapidRunData.acc_winterWeekEnergyProduction_kW.add(gc.v_rapidRunData.acc_winterWeekEnergyProduction_kW);
	v_rapidRunData.acc_winterWeekEnergyConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekEnergyConsumption_kW);
	v_rapidRunData.acc_winterWeekEnergyCurtailed_kW.add(gc.v_rapidRunData.acc_winterWeekEnergyCurtailed_kW);
	v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.add(gc.v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW);
}

f_collectAssetSpecificEnergyFlows();

// This is only true because we have no customers and only members of the Coop for this implementation
acc_totalOwnElectricityProduction_kW = v_rapidRunData.am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);
//acc_summerWeekOwnElectricityProduction_kW = am_summerWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);
//acc_winterWeekOwnElectricityProduction_kW = am_winterWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);


//Calculate cumulative asset capacities
f_getTotalInstalledCapacityOfAssets();

//Calculate cumulative individual values
f_getCumulativeIndividualGCValues();

/*ALCODEEND*/}

double f_initializeCustomCoop(ArrayList<GridConnection> gcList)
{/*ALCODESTART::1739974426481*/
c_memberGridConnections = gcList;

//Basic initialization
f_initialize();

//Collect current totals
f_collectGridConnectionTotals();

//Collect live datasets
f_collectLiveDataSets();

//Calculate KPIs
f_calculateKPIs();

/*ALCODEEND*/}

double f_getGroupContractDeliveryCapacity_kW()
{/*ALCODESTART::1740059187265*/
DataSet data_netbelastingDuurkromme_kW = f_getDuurkromme();
int arraySize = data_netbelastingDuurkromme_kW.size();
if (arraySize < 8760/energyModel.p_timeStep_h){
	traceln("GroupContractDeliveryCapacity is zero because simulation is less than a full year long!");
	return 0.0;
} else {
	return max(0,data_netbelastingDuurkromme_kW.getY(roundToInt(0.25*35/energyModel.p_timeStep_h)));
}
/*ALCODEEND*/}

double f_getGroupContractFeedinCapacity_kW()
{/*ALCODESTART::1740059261369*/
DataSet data_netbelastingDuurkromme_kW = f_getDuurkromme();
int arraySize = data_netbelastingDuurkromme_kW.size();
if (arraySize < 8760/energyModel.p_timeStep_h){
	traceln("GroupContractDeliveryCapacity is zero because simulation is less than a full year long!");
	return 0.0;
} else {
	return -min(0,data_netbelastingDuurkromme_kW.getY(arraySize-roundToInt(0.25*35/energyModel.p_timeStep_h)));
}
/*ALCODEEND*/}

double f_getCumulativeIndividualGCValues()
{/*ALCODESTART::1740475013848*/
//Self consumption and sufficiency
v_cumulativeIndividualSelfconsumptionElectricity_MWh = 0;
v_cumulativeIndividualSelfconsumptionElectricity_fr = 0;
v_cumulativeIndividualSelfSufficiencyElectricity_fr = 0;

v_cumulativeIndividualSelfconsumptionEnergy_MWh = 0;
v_cumulativeIndividualSelfconsumptionEnergy_fr = 0;
v_cumulativeIndividualSelfSufficiencyEnergy_fr = 0;

//Max peaks
v_cumulativeIndividualPeakDelivery_kW = 0;
v_cumulativeIndividualPeakFeedin_kW = 0;

//Loop over membered grid connections
for(GridConnection GC : c_memberGridConnections){
	//Add self consumption of gc individually
	v_cumulativeIndividualSelfconsumptionElectricity_MWh += GC.v_rapidRunData.getTotalElectricitySelfConsumed_MWh();
	v_cumulativeIndividualSelfconsumptionEnergy_MWh += GC.v_rapidRunData.getTotalEnergySelfConsumed_MWh();
	
	//Add all peaks for member grid connections
	v_cumulativeIndividualPeakDelivery_kW += GC.v_rapidRunData.getPeakDelivery_kW();
	v_cumulativeIndividualPeakFeedin_kW += GC.v_rapidRunData.getPeakFeedin_kW();	
}

//Add all max peaks of GC

//Do this also for the 'child' coops
for(Agent a :  c_coopMembers ) { // Take 'behind the meter' production and consumption!
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
		EC.f_getCumulativeIndividualGCValues();
		v_cumulativeIndividualSelfconsumptionElectricity_MWh = EC.v_cumulativeIndividualSelfconsumptionElectricity_MWh;
		v_cumulativeIndividualPeakDelivery_kW  = EC.v_cumulativeIndividualPeakDelivery_kW;
		v_cumulativeIndividualPeakFeedin_kW  = EC.v_cumulativeIndividualPeakFeedin_kW;
	}
}

v_cumulativeIndividualSelfconsumptionElectricity_fr = v_rapidRunData.getTotalElectricityProduced_MWh() > 0 ? v_cumulativeIndividualSelfconsumptionElectricity_MWh / v_rapidRunData.getTotalElectricityProduced_MWh() : 0;
v_cumulativeIndividualSelfSufficiencyElectricity_fr = v_rapidRunData.getTotalElectricityConsumed_MWh()  > 0 ? v_cumulativeIndividualSelfconsumptionElectricity_MWh / v_rapidRunData.getTotalElectricityConsumed_MWh() : 0;

v_cumulativeIndividualSelfconsumptionEnergy_fr = v_rapidRunData.getTotalEnergyProduced_MWh() > 0 ? v_cumulativeIndividualSelfconsumptionEnergy_MWh / v_rapidRunData.getTotalEnergyProduced_MWh() : 0;
v_cumulativeIndividualSelfSufficiencyEnergy_fr = v_rapidRunData.getTotalEnergyConsumed_MWh() > 0 ? v_cumulativeIndividualSelfconsumptionEnergy_MWh / v_rapidRunData.getTotalEnergyConsumed_MWh() : 0;

/*ALCODEEND*/}

double f_getTotalInstalledCapacityOfAssets()
{/*ALCODESTART::1740480839774*/
v_totalInstalledWindPower_kW = 0;
v_totalInstalledPVPower_kW = 0;
v_totalInstalledBatteryStorageCapacity_MWh = 0;

//Add all battery storage capacities of gc
for(GridConnection GC : c_memberGridConnections){
	v_totalInstalledWindPower_kW += GC.v_totalInstalledWindPower_kW;
	v_totalInstalledPVPower_kW += GC.v_totalInstalledPVPower_kW;
	v_totalInstalledBatteryStorageCapacity_MWh += GC.v_totalInstalledBatteryStorageCapacity_MWh;
}

//Do this also for the 'child' coops
for(Agent a :  c_coopMembers ) { // Take 'behind the meter' production and consumption!
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
		EC.f_getTotalInstalledCapacityOfAssets();
		v_totalInstalledWindPower_kW += EC.v_totalInstalledWindPower_kW;
		v_totalInstalledPVPower_kW += EC.v_totalInstalledPVPower_kW;
		v_totalInstalledBatteryStorageCapacity_MWh += EC.v_totalInstalledBatteryStorageCapacity_MWh;
	}
}
/*ALCODEEND*/}

ArrayList<GridConnection> f_getAllChildMemberGridConnections()
{/*ALCODESTART::1740492770316*/
return new ArrayList(f_getAllChildMemberGridConnections_recursion(new HashSet<GridConnection>()));
/*ALCODEEND*/}

HashSet<GridConnection> f_getAllChildMemberGridConnections_recursion(HashSet<GridConnection> allMemberGridConnections)
{/*ALCODESTART::1740492770320*/
//Add to collection
allMemberGridConnections.addAll(this.c_memberGridConnections);

//Recursive loop (repeat this function till bottom)
List<Agent> childCoops = findAll(c_coopMembers, coopMember -> coopMember instanceof EnergyCoop);
if(childCoops.size() == 0){
	return allMemberGridConnections;
}
else{
	for(Agent childCoop : childCoops){
		((EnergyCoop)childCoop).f_getAllChildMemberGridConnections_recursion(allMemberGridConnections);
	}
	return allMemberGridConnections;
}

/*ALCODEEND*/}

ArrayList<GridConnection> f_getAllChildCustomerGridConnections()
{/*ALCODESTART::1740493169961*/
return new ArrayList(f_getAllChildCustomerGridConnections_recursion(new HashSet<GridConnection>()));
/*ALCODEEND*/}

HashSet<GridConnection> f_getAllChildCustomerGridConnections_recursion(HashSet<GridConnection> allCustomerGridConnections)
{/*ALCODESTART::1740493169963*/
//Add to collection
allCustomerGridConnections.addAll(this.c_customerGridConnections);

//Recursive loop (repeat this function till bottom)
List<Agent> childCoops = findAll(c_coopCustomers, coopCustomer -> coopCustomer instanceof EnergyCoop);
if(childCoops.size() == 0){
	return allCustomerGridConnections;
}
else{
	for(Agent childCoop : childCoops){
		((EnergyCoop)childCoop).f_getAllChildCustomerGridConnections_recursion(allCustomerGridConnections);
	}
	return allCustomerGridConnections;
}

/*ALCODEEND*/}

double f_collectAssetSpecificEnergyFlows()
{/*ALCODESTART::1740502128178*/
for (GridConnection gc : c_memberGridConnections) {
	v_rapidRunData.acc_dailyAverageBaseloadElectricityConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageBaseloadElectricityConsumption_kW);
	v_rapidRunData.acc_dailyAverageHeatPumpElectricityConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageHeatPumpElectricityConsumption_kW);
	v_rapidRunData.acc_dailyAverageElectricVehicleConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageElectricVehicleConsumption_kW);
	v_rapidRunData.acc_dailyAverageBatteriesConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageBatteriesConsumption_kW);
	v_rapidRunData.acc_dailyAverageElectricCookingConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageElectricCookingConsumption_kW);
	v_rapidRunData.acc_dailyAverageElectrolyserElectricityConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageElectrolyserElectricityConsumption_kW);
	v_rapidRunData.acc_dailyAverageDistrictHeatingConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageDistrictHeatingConsumption_kW);
	v_rapidRunData.acc_dailyAveragePVProduction_kW.add(gc.v_rapidRunData.acc_dailyAveragePVProduction_kW);
	v_rapidRunData.acc_dailyAverageWindProduction_kW.add(gc.v_rapidRunData.acc_dailyAverageWindProduction_kW);
	v_rapidRunData.acc_dailyAverageV2GProduction_kW.add(gc.v_rapidRunData.acc_dailyAverageV2GProduction_kW);
	v_rapidRunData.acc_dailyAverageBatteriesProduction_kW.add(gc.v_rapidRunData.acc_dailyAverageBatteriesProduction_kW);
	v_rapidRunData.acc_dailyAverageCHPElectricityProduction_kW.add(gc.v_rapidRunData.acc_dailyAverageCHPElectricityProduction_kW);
	//battery dinges
	v_rapidRunData.acc_summerWeekBaseloadElectricityConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekBaseloadElectricityConsumption_kW);
	v_rapidRunData.acc_summerWeekHeatPumpElectricityConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekHeatPumpElectricityConsumption_kW);
	v_rapidRunData.acc_summerWeekElectricVehicleConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekElectricVehicleConsumption_kW);
	v_rapidRunData.acc_summerWeekBatteriesConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekBatteriesConsumption_kW);
	v_rapidRunData.acc_summerWeekElectricCookingConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekElectricCookingConsumption_kW);
	v_rapidRunData.acc_summerWeekElectrolyserElectricityConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekElectrolyserElectricityConsumption_kW);
	v_rapidRunData.acc_summerWeekDistrictHeatingConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekDistrictHeatingConsumption_kW);
	v_rapidRunData.acc_summerWeekPVProduction_kW.add(gc.v_rapidRunData.acc_summerWeekPVProduction_kW);
	v_rapidRunData.acc_summerWeekWindProduction_kW.add(gc.v_rapidRunData.acc_summerWeekWindProduction_kW);
	v_rapidRunData.acc_summerWeekV2GProduction_kW.add(gc.v_rapidRunData.acc_summerWeekV2GProduction_kW);
	v_rapidRunData.acc_summerWeekBatteriesProduction_kW.add(gc.v_rapidRunData.acc_summerWeekBatteriesProduction_kW);
	v_rapidRunData.acc_summerWeekCHPElectricityProduction_kW.add(gc.v_rapidRunData.acc_summerWeekCHPElectricityProduction_kW	);
	//battery zomer dinges
	v_rapidRunData.acc_winterWeekBaseloadElectricityConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekBaseloadElectricityConsumption_kW);
	v_rapidRunData.acc_winterWeekHeatPumpElectricityConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekHeatPumpElectricityConsumption_kW);
	v_rapidRunData.acc_winterWeekElectricVehicleConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekElectricVehicleConsumption_kW);
	v_rapidRunData.acc_winterWeekBatteriesConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekBatteriesConsumption_kW);
	v_rapidRunData.acc_winterWeekElectricCookingConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekElectricCookingConsumption_kW);
	v_rapidRunData.acc_winterWeekElectrolyserElectricityConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekElectrolyserElectricityConsumption_kW);
	v_rapidRunData.acc_winterWeekDistrictHeatingConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekDistrictHeatingConsumption_kW);
	v_rapidRunData.acc_winterWeekPVProduction_kW.add(gc.v_rapidRunData.acc_winterWeekPVProduction_kW);
	v_rapidRunData.acc_winterWeekWindProduction_kW.add(gc.v_rapidRunData.acc_winterWeekWindProduction_kW);
	v_rapidRunData.acc_winterWeekV2GProduction_kW.add(gc.v_rapidRunData.acc_winterWeekV2GProduction_kW);
	v_rapidRunData.acc_winterWeekBatteriesProduction_kW.add(gc.v_rapidRunData.acc_winterWeekBatteriesProduction_kW);
	v_rapidRunData.acc_winterWeekCHPElectricityProduction_kW.add(gc.v_rapidRunData.acc_winterWeekCHPElectricityProduction_kW);
	//battery winter dinges
	
}
/*ALCODEEND*/}

double f_collectLiveDataSets()
{/*ALCODESTART::1740502128180*/
ArrayList<GridConnection> gcList = f_getAllChildMemberGridConnections();

int liveWeekSize = gcList.get(0).v_liveData.data_gridCapacityDemand_kW.size();

for (int i=0; i < liveWeekSize; i++){
	
	double timeAxisValue = gcList.get(0).v_liveData.data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset 
	
	// Demand
	J_FlowsMap fm_demand_kW = new J_FlowsMap();
	
	double electricityDemandCapacityLiveWeek_kW = 0;
	double electricitySupplyCapacityLiveWeek_kW = 0;
	double netLoadLiveWeek_kW = 0;
	
	double baseloadElectricityDemandLiveWeek_kW = 0;
	double electricityForHeatDemandLiveWeek_kW = 0;
	double electricityForTransportDemandLiveWeek_kW = 0;
	double petroleumProductsDemandLiveWeek_kW = 0;
	double naturalGasDemandLiveWeek_kW = 0;
	double electricityForStorageDemandLiveWeek_kW = 0;
	double electricityForHydrogenDemandLiveWeek_kW = 0;
	double electricityForCookingConsumptionLiveWeek_kW = 0;
	
	double districtHeatingDemandLiveWeek_kW = 0;
	
	// Supply
	J_FlowsMap fm_supply_kW = new J_FlowsMap();

	double windElectricitySupplyLiveWeek_kW = 0;
	double PVElectricitySupplyLiveWeek_kW = 0;
	double storageElectricitySupplyLiveWeek_kW = 0;
	double V2GElectricitySupplyLiveWeek_kW = 0;
	double hydrogenSupplyLiveWeek_kW = 0;
	double CHPElectricitySupplyLiveWeek_kW = 0;
	
	//Other
	double batteryStoredEnergyLiveWeek_MWh = 0;
	
	for (GridConnection gc : gcList){
		for (OL_EnergyCarriers EC_consumption : gc.v_activeConsumptionEnergyCarriers) {
			fm_demand_kW.addFlow( EC_consumption, gc.v_liveData.dsm_liveDemand_kW.get(EC_consumption).getY(i));
		}
		for (OL_EnergyCarriers EC_production : gc.v_activeProductionEnergyCarriers) {
			fm_supply_kW.addFlow( EC_production, gc.v_liveData.dsm_liveSupply_kW.get(EC_production).getY(i));
		}
		
		electricityDemandCapacityLiveWeek_kW += gc.v_liveData.data_gridCapacityDemand_kW.getY(i);
		electricitySupplyCapacityLiveWeek_kW += gc.v_liveData.data_gridCapacitySupply_kW.getY(i);
		netLoadLiveWeek_kW  += gc.v_liveData.data_liveElectricityBalance_kW.getY(i);
	
		baseloadElectricityDemandLiveWeek_kW  += gc.v_liveData.data_baseloadElectricityDemand_kW.getY(i);
		electricityForHeatDemandLiveWeek_kW  += gc.v_liveData.data_heatPumpElectricityDemand_kW.getY(i);
		electricityForTransportDemandLiveWeek_kW += gc.v_liveData.data_electricVehicleDemand_kW.getY(i);
		electricityForStorageDemandLiveWeek_kW  += gc.v_liveData.data_batteryCharging_kW.getY(i);
		electricityForHydrogenDemandLiveWeek_kW  += gc.v_liveData.data_hydrogenElectricityDemand_kW.getY(i);
		electricityForCookingConsumptionLiveWeek_kW += gc.v_liveData.data_cookingElectricityDemand_kW.getY(i);
		districtHeatingDemandLiveWeek_kW += gc.v_liveData.data_districtHeatDelivery_kW.getY(i);
		
		// Supply
		windElectricitySupplyLiveWeek_kW  += gc.v_liveData.data_windGeneration_kW.getY(i);
		PVElectricitySupplyLiveWeek_kW  += gc.v_liveData.data_PVGeneration_kW.getY(i);
		storageElectricitySupplyLiveWeek_kW  += gc.v_liveData.data_batteryDischarging_kW.getY(i);
		V2GElectricitySupplyLiveWeek_kW  += gc.v_liveData.data_V2GSupply_kW.getY(i);
		CHPElectricitySupplyLiveWeek_kW  += gc.v_liveData.data_CHPElectricityProductionLiveWeek_kW.getY(i);
		
		//Other 
		batteryStoredEnergyLiveWeek_MWh += 	gc.v_liveData.data_batteryStoredEnergyLiveWeek_MWh.getY(i);
	}
	
	for (OL_EnergyCarriers EC_consumption : v_activeConsumptionEnergyCarriers) {
		v_liveData.dsm_liveDemand_kW.get(EC_consumption).add(timeAxisValue, fm_demand_kW.get(EC_consumption));
	}
	for (OL_EnergyCarriers EC_production : v_activeProductionEnergyCarriers) {
		v_liveData.dsm_liveSupply_kW.get(EC_production).add(timeAxisValue, fm_supply_kW.get(EC_production));
	}
	
		
	v_liveData.data_gridCapacityDemand_kW.add(timeAxisValue, electricityDemandCapacityLiveWeek_kW);
	v_liveData.data_gridCapacitySupply_kW.add(timeAxisValue, electricitySupplyCapacityLiveWeek_kW);
	v_liveData.data_liveElectricityBalance_kW.add(timeAxisValue, netLoadLiveWeek_kW);
	
	v_liveData.data_baseloadElectricityDemand_kW.add(timeAxisValue, baseloadElectricityDemandLiveWeek_kW);
	v_liveData.data_heatPumpElectricityDemand_kW.add(timeAxisValue, electricityForHeatDemandLiveWeek_kW);
	v_liveData.data_electricVehicleDemand_kW.add(timeAxisValue, electricityForTransportDemandLiveWeek_kW);
	v_liveData.data_batteryCharging_kW.add(timeAxisValue, electricityForStorageDemandLiveWeek_kW);
	v_liveData.data_hydrogenElectricityDemand_kW.add(timeAxisValue, electricityForHydrogenDemandLiveWeek_kW);
	v_liveData.data_cookingElectricityDemand_kW.add(timeAxisValue, electricityForCookingConsumptionLiveWeek_kW);
	v_liveData.data_districtHeatDelivery_kW.add(timeAxisValue, districtHeatingDemandLiveWeek_kW);
	
	// Supply
	v_liveData.data_windGeneration_kW.add(timeAxisValue, windElectricitySupplyLiveWeek_kW);
	v_liveData.data_PVGeneration_kW.add(timeAxisValue, PVElectricitySupplyLiveWeek_kW);
	v_liveData.data_batteryDischarging_kW.add(timeAxisValue, storageElectricitySupplyLiveWeek_kW);
	v_liveData.data_V2GSupply_kW.add(timeAxisValue, V2GElectricitySupplyLiveWeek_kW);
	v_liveData.data_CHPElectricityProductionLiveWeek_kW.add(timeAxisValue, CHPElectricitySupplyLiveWeek_kW);
	
	//Stored
	v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(timeAxisValue, batteryStoredEnergyLiveWeek_MWh);
}

/*ALCODEEND*/}

double f_initializePreviousRunData()
{/*ALCODESTART::1741277674313*/

/*ALCODEEND*/}

double f_rapidRunDataLogging()
{/*ALCODESTART::1741626527076*/
// EnergyCoop specific
acc_totalOwnElectricityProduction_kW.addStep( v_currentOwnElectricityProduction_kW );
acc_totalCustomerDelivery_kW.addStep( v_currentCustomerDelivery_kW );
acc_totalCustomerFeedIn_kW.addStep( v_currentCustomerFeedIn_kW );

// Copied from GridConnection
//v_maxConnectionLoad_fr = max(v_maxConnectionLoad_fr, abs(fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) / p_contractedDeliveryCapacity_kW ));

double currentImport_kW = 0.0;
double currentExport_kW = 0.0;
for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
	double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
	currentImport_kW += max( 0, currentBalance_kW );
	currentExport_kW += max( 0, -currentBalance_kW );
	v_rapidRunData.am_totalBalanceAccumulators_kW.get(EC).addStep(  currentBalance_kW );
}

// Daytime totals. Use overal-total minus daytime total to get nighttime totals.
if(energyModel.b_isDaytime) { 
	
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		v_rapidRunData.am_daytimeImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		v_rapidRunData.am_daytimeExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	v_rapidRunData.acc_daytimeElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_daytimeElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );	
	v_rapidRunData.acc_daytimeEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_daytimeEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);	
}

// Weekend totals. Use overal-totals minus weekend totals to get weekday totals.
if (!energyModel.b_isWeekday) { // 
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
		v_rapidRunData.am_weekendImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
		v_rapidRunData.am_weekendExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
	}
	
	v_rapidRunData.acc_weekendElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_weekendElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
	v_rapidRunData.acc_weekendEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_weekendEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
}


v_rapidRunData.acc_dailyAverageBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
v_rapidRunData.acc_dailyAverageHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
v_rapidRunData.acc_dailyAverageElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
v_rapidRunData.acc_dailyAverageElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
v_rapidRunData.acc_dailyAverageDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );

v_rapidRunData.acc_dailyAveragePVProduction_kW.addStep( v_pvProductionElectric_kW );
v_rapidRunData.acc_dailyAverageWindProduction_kW.addStep( v_windProductionElectric_kW );
v_rapidRunData.acc_dailyAverageV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );

//========== SUMMER WEEK ==========//
if (energyModel.b_isSummerWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		v_rapidRunData.am_summerWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
		v_rapidRunData.am_summerWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );

	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
		v_rapidRunData.am_summerWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
	}
			
	v_rapidRunData.acc_summerWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_summerWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

	v_rapidRunData.acc_summerWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

	v_rapidRunData.acc_summerWeekDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
	v_rapidRunData.acc_summerWeekFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);
	
	v_rapidRunData.acc_summerWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	v_rapidRunData.acc_summerWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	v_rapidRunData.acc_summerWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	v_rapidRunData.acc_summerWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	v_rapidRunData.acc_summerWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	v_rapidRunData.acc_summerWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	v_rapidRunData.acc_summerWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	v_rapidRunData.acc_summerWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_summerWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
	//acc_summerWeekBatteriesStoredEnergy_MWh.addStep();		

}

//========== WINTER WEEK ==========// 
if (energyModel.b_isWinterWeek){
	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
		v_rapidRunData.am_winterWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
	    v_rapidRunData.am_winterWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
	    v_rapidRunData.am_winterWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
	}
	
	v_rapidRunData.acc_winterWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
	v_rapidRunData.acc_winterWeekEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
	
	v_rapidRunData.acc_winterWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
	v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	
	
	v_rapidRunData.acc_winterWeekDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
	v_rapidRunData.acc_winterWeekFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);
	
	v_rapidRunData.acc_winterWeekBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
	v_rapidRunData.acc_winterWeekHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
	v_rapidRunData.acc_winterWeekElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
	v_rapidRunData.acc_winterWeekElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
	v_rapidRunData.acc_winterWeekDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );
	
	v_rapidRunData.acc_winterWeekPVProduction_kW.addStep( v_pvProductionElectric_kW );
	v_rapidRunData.acc_winterWeekWindProduction_kW.addStep( v_windProductionElectric_kW );
	v_rapidRunData.acc_winterWeekV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
	v_rapidRunData.acc_winterWeekCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
	//acc_winterWeekBatteriesStoredEnergy_MWh.addStep();		
	
}


//========== TOTALS / DAILY AVERAGES ==========//
for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
    v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
}
for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
    v_rapidRunData.am_dailyAverageProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
}
v_rapidRunData.acc_dailyAverageEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
v_rapidRunData.acc_dailyAverageEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
v_rapidRunData.acc_totalEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);

//acc_dailyAverageDeliveryCapacity_kW.addStep( p_contractedDeliveryCapacity_kW);
//acc_dailyAverageFeedinCapacity_kW.addStep( p_contractedFeedinCapacity_kW);

v_rapidRunData.acc_dailyAverageBaseloadElectricityConsumption_kW.addStep( v_fixedConsumptionElectric_kW );
v_rapidRunData.acc_dailyAverageHeatPumpElectricityConsumption_kW.addStep( v_heatPumpElectricityConsumption_kW );
v_rapidRunData.acc_dailyAverageElectricVehicleConsumption_kW.addStep( max(0,v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesConsumption_kW.addStep( max(0,v_batteryPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageElectricCookingConsumption_kW.addStep( v_electricHobConsumption_kW );
v_rapidRunData.acc_dailyAverageElectrolyserElectricityConsumption_kW.addStep( max(0, v_hydrogenElectricityConsumption_kW) );
v_rapidRunData.acc_dailyAverageDistrictHeatingConsumption_kW.addStep( v_districtHeatDelivery_kW );

v_rapidRunData.acc_dailyAveragePVProduction_kW.addStep( v_pvProductionElectric_kW );
v_rapidRunData.acc_dailyAverageWindProduction_kW.addStep( v_windProductionElectric_kW );
v_rapidRunData.acc_dailyAverageV2GProduction_kW.addStep( max(0, -v_evChargingPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageBatteriesProduction_kW.addStep( max(0,-v_batteryPowerElectric_kW) );
v_rapidRunData.acc_dailyAverageCHPElectricityProduction_kW.addStep( v_CHPProductionElectric_kW );
//acc_dailyAverageBatteriesStoredEnergy_MWh.addStep();		

/*ALCODEEND*/}

