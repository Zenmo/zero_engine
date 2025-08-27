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
		(((ConnectionOwner)ConnectingChildActor).c_ownedGridConnections).forEach( gc -> gc.c_parentCoops.add(this));
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

fm_currentProductionFlows_kW.clear();
fm_currentConsumptionFlows_kW.clear();
fm_currentBalanceFlows_kW.clear();
fm_currentAssetFlows_kW.clear();
v_currentPrimaryEnergyProduction_kW = 0;
v_currentFinalEnergyConsumption_kW = 0;
v_currentEnergyCurtailed_kW = 0;
v_batteryStoredEnergy_kWh = 0;
v_currentPrimaryEnergyProductionHeatpumps_kW = 0;


for(GridConnection gc : c_memberGridConnections) { // Can't do this in parallel due to different threads writing to the same values!
	if(gc.v_isActive){
		fm_currentBalanceFlows_kW.addFlows(gc.fm_currentBalanceFlows_kW);
		fm_currentProductionFlows_kW.addFlows(gc.fm_currentProductionFlows_kW);
		fm_currentConsumptionFlows_kW.addFlows(gc.fm_currentConsumptionFlows_kW);
		fm_currentAssetFlows_kW.addFlows(gc.fm_currentAssetFlows_kW);
		v_currentFinalEnergyConsumption_kW += gc.v_currentFinalEnergyConsumption_kW;
		v_currentPrimaryEnergyProduction_kW += gc.v_currentPrimaryEnergyProduction_kW;
		v_currentEnergyCurtailed_kW += gc.v_currentEnergyCurtailed_kW;
		v_batteryStoredEnergy_kWh += gc.v_batteryStoredEnergy_kWh;
		v_currentPrimaryEnergyProductionHeatpumps_kW += gc.v_currentPrimaryEnergyProductionHeatpumps_kW;
		v_currentOwnElectricityProduction_kW += gc.fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);	
	}
}

// gather electricity flows
for(Agent a :  c_coopMembers ) { // Take 'behind the meter' production and consumption!
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
		
		fm_currentBalanceFlows_kW.addFlows(EC.fm_currentBalanceFlows_kW);
		fm_currentProductionFlows_kW.addFlows(EC.fm_currentProductionFlows_kW);
		fm_currentConsumptionFlows_kW.addFlows(EC.fm_currentConsumptionFlows_kW);
		fm_currentAssetFlows_kW.addFlows(EC.fm_currentAssetFlows_kW);
		v_currentPrimaryEnergyProduction_kW += EC.v_currentPrimaryEnergyProduction_kW;
		v_currentFinalEnergyConsumption_kW += EC.v_currentFinalEnergyConsumption_kW;
		v_currentEnergyCurtailed_kW += EC.v_currentEnergyCurtailed_kW;
		v_batteryStoredEnergy_kWh += EC.v_batteryStoredEnergy_kWh;
		v_currentPrimaryEnergyProductionHeatpumps_kW += EC.v_currentPrimaryEnergyProductionHeatpumps_kW;
		v_currentOwnElectricityProduction_kW += EC.fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY); 
		
		// Asset flows
		//v_assetFlows.addFlows(EC.v_assetFlows);
	}
}

for (GridConnection GC : c_customerGridConnections) { // Take 'behind the meter' production and consumption!
	if(GC.v_isActive){
		for (OL_EnergyCarriers energyCarrier : v_liveData.activeEnergyCarriers) {
			double nettConsumption_kW = GC.fm_currentBalanceFlows_kW.get(energyCarrier);
			fm_currentProductionFlows_kW.addFlow( energyCarrier, max(0, -nettConsumption_kW));
			fm_currentConsumptionFlows_kW.addFlow( energyCarrier, max(0, nettConsumption_kW));
			fm_currentBalanceFlows_kW.addFlow( energyCarrier, nettConsumption_kW);
			
			if (energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
				v_currentCustomerFeedIn_kW += max(0,-nettConsumption_kW);
				v_currentCustomerDelivery_kW += max(0,nettConsumption_kW);
			}
		}
	}				
}

for(Agent a :  c_coopCustomers ) { // Don't look at 'behind the meter' production/consumption, but use 'nett flow' as measure of consumption/production
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
				
		fm_currentBalanceFlows_kW.addFlows(EC.fm_currentBalanceFlows_kW);
		fm_currentProductionFlows_kW.addFlows(EC.fm_currentProductionFlows_kW);
		fm_currentConsumptionFlows_kW.addFlows(EC.fm_currentConsumptionFlows_kW);
		fm_currentAssetFlows_kW.addFlows(EC.fm_currentAssetFlows_kW);
		v_currentCustomerFeedIn_kW += EC.v_currentCustomerFeedIn_kW;
		v_currentCustomerDelivery_kW += EC.v_currentCustomerDelivery_kW;
	}
}

v_electricitySurplus_kW = -fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);

//Coop update data classes
if (energyModel.v_isRapidRun){
	f_rapidRunDataLogging();
} else {
	f_updateLiveDataSets();
}
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
v_liveConnectionMetaData.contractedDeliveryCapacityKnown = true;
v_liveConnectionMetaData.contractedFeedinCapacityKnown = true;
v_liveData.activeEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
v_liveData.activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
v_liveData.activeConsumptionEnergyCarriers= EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
v_liveData.assetsMetaData.activeAssetFlows.clear();

//Get energy carriers and capacities boolean
for(GridConnection GC:c_memberGridConnections){
	v_liveConnectionMetaData.contractedDeliveryCapacity_kW += GC.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
	v_liveConnectionMetaData.contractedFeedinCapacity_kW += GC.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
	v_liveData.activeEnergyCarriers.addAll(GC.v_liveData.activeEnergyCarriers);
	v_liveData.activeProductionEnergyCarriers.addAll(GC.v_liveData.activeProductionEnergyCarriers);
	v_liveData.activeConsumptionEnergyCarriers.addAll(GC.v_liveData.activeConsumptionEnergyCarriers);
	v_liveData.assetsMetaData.activeAssetFlows.addAll(GC.v_liveData.assetsMetaData.activeAssetFlows);

	if(!GC.v_liveConnectionMetaData.contractedDeliveryCapacityKnown){
		v_liveConnectionMetaData.contractedDeliveryCapacityKnown = false;
	}

	if(!GC.v_liveConnectionMetaData.contractedFeedinCapacityKnown){
		v_liveConnectionMetaData.contractedFeedinCapacityKnown = false;
	} 
}

//v_rapidRunData.initializeAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_activeEnergyCarriers, v_activeConsumptionEnergyCarriers, v_activeProductionEnergyCarriers); //f_initializeAccumulators();
acc_totalOwnElectricityProduction_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 8760);
acc_totalCustomerDelivery_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 8760);
acc_totalCustomerFeedIn_kW = new ZeroAccumulator(true, energyModel.p_timeStep_h, 8760);

//========== LIVE WEEK DATASETS ==========//
v_liveData.dsm_liveDemand_kW.createEmptyDataSets(v_liveData.activeConsumptionEnergyCarriers, roundToInt(168/energyModel.p_timeStep_h));
v_liveData.dsm_liveSupply_kW.createEmptyDataSets(v_liveData.activeProductionEnergyCarriers, roundToInt(168/energyModel.p_timeStep_h));
v_liveData.dsm_liveAssetFlows_kW.createEmptyDataSets(v_liveData.assetsMetaData.activeAssetFlows, roundToInt(168/energyModel.p_timeStep_h));

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

v_rapidRunData.resetAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, v_liveData.activeEnergyCarriers, v_liveData.activeConsumptionEnergyCarriers, v_liveData.activeProductionEnergyCarriers); //f_initializeAccumulators();

/*ALCODEEND*/}

double f_updateLiveDataSets()
{/*ALCODESTART::1715857260657*/
//Current timestep
double currentTime_h = energyModel.t_h-energyModel.p_runStartTime_h;

v_liveData.addTimeStep(currentTime_h,
	fm_currentBalanceFlows_kW,
	fm_currentConsumptionFlows_kW,
	fm_currentProductionFlows_kW,
	fm_currentAssetFlows_kW,
	v_currentPrimaryEnergyProduction_kW, 
	v_currentFinalEnergyConsumption_kW, 
	v_currentPrimaryEnergyProductionHeatpumps_kW, 
	v_currentEnergyCurtailed_kW, 
	v_batteryStoredEnergy_kWh/1000 
);
/*
	//Current timestep
	double currentTime_h = energyModel.t_h-energyModel.p_runStartTime_h;
	
	//Energy carrier flows
	for (OL_EnergyCarriers EC : v_activeConsumptionEnergyCarriers) {
		v_liveData.dsm_liveDemand_kW.get(EC).add( currentTime_h, roundToDecimal(fm_currentConsumptionFlows_kW.get(EC), 3) );
	}
	for (OL_EnergyCarriers EC : v_activeProductionEnergyCarriers) {
		v_liveData.dsm_liveSupply_kW.get(EC).add( currentTime_h, roundToDecimal(fm_currentProductionFlows_kW.get(EC), 3) );
	}
	
	
	//Electricity balance
	v_liveData.data_liveElectricityBalance_kW.add(currentTime_h, fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY));
	
	
	//Total demand and supply
	v_liveData.data_totalDemand_kW.add(currentTime_h, v_currentFinalEnergyConsumption_kW);
	v_liveData.data_totalSupply_kW.add(currentTime_h, v_currentPrimaryEnergyProduction_kW);
	
	
	//Live capacity datasets
	v_liveData.data_gridCapacityDemand_kW.add(currentTime_h, v_liveConnectionMetaData.contractedDeliveryCapacity_kW);
	v_liveData.data_gridCapacitySupply_kW.add(currentTime_h, -v_liveConnectionMetaData.contractedFeedinCapacity_kW);
	
	
	//// Gather specific electricity flows from corresponding energy assets
	
	//Baseload electricity
	v_liveData.data_baseloadElectricityDemand_kW.add(currentTime_h, roundToDecimal(v_fixedConsumptionElectric_kW, 3));
	
	//Cooking
	v_liveData.data_cookingElectricityDemand_kW.add(currentTime_h, roundToDecimal(v_electricHobConsumption_kW, 3));
	
	//Hydrogen elec consumption
	v_liveData.data_hydrogenElectricityDemand_kW.add(currentTime_h, roundToDecimal(max(0, v_hydrogenElectricityConsumption_kW), 3));
	
	//Heatpump elec consumption
	v_liveData.data_heatPumpElectricityDemand_kW.add(currentTime_h, roundToDecimal(max(0, v_heatPumpElectricityConsumption_kW), 3));
	
	//EVs
	v_liveData.data_electricVehicleDemand_kW.add(currentTime_h, roundToDecimal(max(0,v_evChargingPowerElectric_kW), 3));
	v_liveData.data_V2GSupply_kW.add(currentTime_h, roundToDecimal(max(0, -v_evChargingPowerElectric_kW), 3));
	
	//Batteries
	v_liveData.data_batteryCharging_kW.add(currentTime_h, roundToDecimal(max(0, v_batteryPowerElectric_kW), 3));		
	v_liveData.data_batteryDischarging_kW.add(currentTime_h, roundToDecimal(max(0, -v_batteryPowerElectric_kW), 3));	
	v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(currentTime_h, v_batteryStoredEnergy_kWh/1000);
	double currentSOC = 0;
	if(v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
		currentSOC = (v_batteryStoredEnergy_kWh/1000)/v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	}
	v_liveData.data_batterySOC_fr.add(currentTime_h, roundToDecimal(currentSOC, 3));
	
	//CHP production
	v_liveData.data_CHPElectricityProductionLiveWeek_kW.add(currentTime_h, roundToDecimal(v_CHPProductionElectric_kW, 3));
	
	//PV production
	v_liveData.data_PVGeneration_kW.add(currentTime_h, roundToDecimal(v_pvProductionElectric_kW, 3));
	
	//Wind production
	v_liveData.data_windGeneration_kW.add(currentTime_h, roundToDecimal(v_windProductionElectric_kW, 3));	
	
	//PV production
	v_liveData.data_PTGeneration_kW.add(currentTime_h, roundToDecimal(v_ptProductionHeat_kW, 3));
	
	//District heating
	v_liveData.data_districtHeatDelivery_kW.add(currentTime_h, roundToDecimal(max(0,fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT)), 3));	
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

double f_calculateKPIs()
{/*ALCODESTART::1731081139333*/
//Cumulative KPIs of each grid connection individually
f_getCumulativeIndividualGCValues();

//Costumer delivery and feedin
v_totalCustomerFeedIn_MWh = acc_totalCustomerFeedIn_kW.getIntegral_kWh() / 1000;
v_totalCustomerDelivery_MWh = acc_totalCustomerDelivery_kW.getIntegral_kWh() / 1000;
v_totalOwnElectricityProduction_MWh = acc_totalOwnElectricityProduction_kW.getIntegral_kWh() / 1000;

/*ALCODEEND*/}

double f_collectGridConnectionRapidRunData()
{/*ALCODESTART::1739970817879*/
// Make collective profiles, electricity per timestep, other energy carriers per day!

for (GridConnection gc : c_memberGridConnections) {

	// Totals
	v_rapidRunData.am_totalBalanceAccumulators_kW.add(gc.v_rapidRunData.am_totalBalanceAccumulators_kW);
	v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW.add(gc.v_rapidRunData.am_dailyAverageConsumptionAccumulators_kW);
	v_rapidRunData.am_dailyAverageProductionAccumulators_kW.add(gc.v_rapidRunData.am_dailyAverageProductionAccumulators_kW);
	v_rapidRunData.acc_dailyAverageEnergyProduction_kW.add(gc.v_rapidRunData.acc_dailyAverageEnergyProduction_kW);
	v_rapidRunData.acc_dailyAverageFinalEnergyConsumption_kW.add(gc.v_rapidRunData.acc_dailyAverageFinalEnergyConsumption_kW);
	v_rapidRunData.acc_totalEnergyCurtailed_kW.add(gc.v_rapidRunData.acc_totalEnergyCurtailed_kW);
	v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW.add(gc.v_rapidRunData.acc_totalPrimaryEnergyProductionHeatpumps_kW);
	v_rapidRunData.am_assetFlowsAccumulators_kW.add(gc.v_rapidRunData.am_assetFlowsAccumulators_kW);
	
	// Daytime
	v_rapidRunData.acc_daytimeElectricityConsumption_kW.add(gc.v_rapidRunData.acc_daytimeElectricityConsumption_kW);
	v_rapidRunData.acc_daytimeElectricityProduction_kW.add(gc.v_rapidRunData.acc_daytimeElectricityProduction_kW);
	v_rapidRunData.acc_daytimeEnergyProduction_kW.add(gc.v_rapidRunData.acc_daytimeEnergyProduction_kW);
	v_rapidRunData.acc_daytimeFinalEnergyConsumption_kW.add(gc.v_rapidRunData.acc_daytimeFinalEnergyConsumption_kW);
	v_rapidRunData.am_daytimeImports_kW.add(gc.v_rapidRunData.am_daytimeImports_kW);
	v_rapidRunData.am_daytimeExports_kW.add(gc.v_rapidRunData.am_daytimeExports_kW);	
	
	// Weekend
	v_rapidRunData.acc_weekendElectricityConsumption_kW.add(gc.v_rapidRunData.acc_weekendElectricityConsumption_kW);
	v_rapidRunData.acc_weekendElectricityProduction_kW.add(gc.v_rapidRunData.acc_weekendElectricityProduction_kW);
	v_rapidRunData.acc_weekendEnergyProduction_kW.add(gc.v_rapidRunData.acc_weekendEnergyProduction_kW);
	v_rapidRunData.acc_weekendFinalEnergyConsumption_kW.add(gc.v_rapidRunData.acc_weekendFinalEnergyConsumption_kW);
	v_rapidRunData.am_weekendImports_kW.add(gc.v_rapidRunData.am_weekendImports_kW);
	v_rapidRunData.am_weekendExports_kW.add(gc.v_rapidRunData.am_weekendExports_kW);	
	
	// Summerweek
	v_rapidRunData.am_summerWeekBalanceAccumulators_kW.add(gc.v_rapidRunData.am_summerWeekBalanceAccumulators_kW);
	v_rapidRunData.am_summerWeekConsumptionAccumulators_kW.add(gc.v_rapidRunData.am_summerWeekConsumptionAccumulators_kW);
	v_rapidRunData.am_summerWeekProductionAccumulators_kW.add(gc.v_rapidRunData.am_summerWeekProductionAccumulators_kW);
	v_rapidRunData.acc_summerWeekEnergyProduction_kW.add(gc.v_rapidRunData.acc_summerWeekEnergyProduction_kW);
	v_rapidRunData.acc_summerWeekFinalEnergyConsumption_kW.add(gc.v_rapidRunData.acc_summerWeekFinalEnergyConsumption_kW);
	v_rapidRunData.acc_summerWeekEnergyCurtailed_kW.add(gc.v_rapidRunData.acc_summerWeekEnergyCurtailed_kW);
	v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.add(gc.v_rapidRunData.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW);
	v_rapidRunData.am_assetFlowsSummerWeek_kW.add(gc.v_rapidRunData.am_assetFlowsSummerWeek_kW);
	
	// Winterweek
	v_rapidRunData.am_winterWeekBalanceAccumulators_kW.add(gc.v_rapidRunData.am_winterWeekBalanceAccumulators_kW);
	v_rapidRunData.am_winterWeekConsumptionAccumulators_kW.add(gc.v_rapidRunData.am_winterWeekConsumptionAccumulators_kW);
	v_rapidRunData.am_winterWeekProductionAccumulators_kW.add(gc.v_rapidRunData.am_winterWeekProductionAccumulators_kW);
	v_rapidRunData.acc_winterWeekEnergyProduction_kW.add(gc.v_rapidRunData.acc_winterWeekEnergyProduction_kW);
	v_rapidRunData.acc_winterWeekFinalEnergyConsumption_kW.add(gc.v_rapidRunData.acc_winterWeekFinalEnergyConsumption_kW);
	v_rapidRunData.acc_winterWeekEnergyCurtailed_kW.add(gc.v_rapidRunData.acc_winterWeekEnergyCurtailed_kW);
	v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.add(gc.v_rapidRunData.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW);
	v_rapidRunData.am_assetFlowsWinterWeek_kW.add(gc.v_rapidRunData.am_assetFlowsWinterWeek_kW);
}

// This is only true because we have no customers and only members of the Coop for this implementation
acc_totalOwnElectricityProduction_kW = v_rapidRunData.am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);
//acc_summerWeekOwnElectricityProduction_kW = am_summerWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);
//acc_winterWeekOwnElectricityProduction_kW = am_winterWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);

//Calculate cumulative asset capacities
f_getTotalInstalledCapacityOfAssets_rapidRun();

/*ALCODEEND*/}

double f_initializeCustomCoop(ArrayList<GridConnection> gcList)
{/*ALCODESTART::1739974426481*/
c_memberGridConnections.addAll(gcList);

//Basic initialization
f_initialize();

//Collect live datasets
f_collectGridConnectionLiveData();

boolean allGCHaveRapidRun = true;
for(GridConnection GC : c_memberGridConnections){
	if(GC.v_rapidRunData == null){
		allGCHaveRapidRun = false;
		break;
	}
}
if(allGCHaveRapidRun){
	
	//Create rapid run data class used to store combined values of the members
	f_createAndInitializeRapidRunDataClass();
	
	//Collect current totals
	f_collectGridConnectionRapidRunData();
	
	//Calculate KPIs
	f_calculateKPIs();
}

f_connectCoopBattery();
/*ALCODEEND*/}

double f_getGroupContractDeliveryCapacity_kW()
{/*ALCODESTART::1740059187265*/
DataSet data_netbelastingDuurkromme_kW = v_rapidRunData.getLoadDurationCurves(energyModel).ds_loadDurationCurveTotal_kW;
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
DataSet data_netbelastingDuurkromme_kW = v_rapidRunData.getLoadDurationCurves(energyModel).ds_loadDurationCurveTotal_kW;
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
	if(GC.v_isActive){
		//Add self consumption of gc individually
		v_cumulativeIndividualSelfconsumptionElectricity_MWh += GC.v_rapidRunData.getTotalElectricitySelfConsumed_MWh();
		v_cumulativeIndividualSelfconsumptionEnergy_MWh += GC.v_rapidRunData.getTotalEnergySelfConsumed_MWh();
		
		//Add all peaks for member grid connections
		v_cumulativeIndividualPeakDelivery_kW += GC.v_rapidRunData.getPeakDelivery_kW();
		v_cumulativeIndividualPeakFeedin_kW += GC.v_rapidRunData.getPeakFeedin_kW();
	}	
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

double f_getTotalInstalledCapacityOfAssets_rapidRun()
{/*ALCODESTART::1740480839774*/
//Collect rapid run asset totals
v_rapidRunData.assetsMetaData.totalInstalledWindPower_kW = 0.0;
v_rapidRunData.assetsMetaData.totalInstalledPVPower_kW = 0.0;
v_rapidRunData.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh = 0.0;

//Add all battery storage capacities of gc
for(GridConnection GC : c_memberGridConnections){
	v_rapidRunData.assetsMetaData.totalInstalledWindPower_kW += GC.v_rapidRunData.assetsMetaData.totalInstalledWindPower_kW;
	v_rapidRunData.assetsMetaData.totalInstalledPVPower_kW += GC.v_rapidRunData.assetsMetaData.totalInstalledPVPower_kW;
	v_rapidRunData.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh += GC.v_rapidRunData.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
}

//Do this also for the 'child' coops
for(Agent a :  c_coopMembers ) { // Take 'behind the meter' production and consumption!
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
		EC.f_getTotalInstalledCapacityOfAssets_rapidRun();
		v_rapidRunData.assetsMetaData.totalInstalledWindPower_kW += EC.v_rapidRunData.assetsMetaData.totalInstalledWindPower_kW;
		v_rapidRunData.assetsMetaData.totalInstalledPVPower_kW += EC.v_rapidRunData.assetsMetaData.totalInstalledPVPower_kW;
		v_rapidRunData.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh += EC.v_rapidRunData.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
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
List<Actor> childCoops = findAll(c_coopMembers, coopMember -> coopMember instanceof EnergyCoop);

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
List<Actor> childCoops = findAll(c_coopCustomers, coopCustomer -> coopCustomer instanceof EnergyCoop);
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

double f_collectGridConnectionLiveData()
{/*ALCODESTART::1740502128180*/
ArrayList<GridConnection> gcList = f_getAllChildMemberGridConnections();

int liveWeekSize = gcList.get(0).v_liveData.data_gridCapacityDemand_kW.size();

for (int i=0; i < liveWeekSize; i++){
	
	double timeAxisValue = gcList.get(0).v_liveData.data_gridCapacityDemand_kW.getX(i); // we get the X value from a random dataset 
	
	// Demand
	J_FlowsMap fm_demand_kW = new J_FlowsMap();
	J_ValueMap<OL_AssetFlowCategories> fm_currentAssetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
	
	double electricityDemandCapacityLiveWeek_kW = 0;
	double electricitySupplyCapacityLiveWeek_kW = 0;
	double netLoadLiveWeek_kW = 0;

	double districtHeatingDemandLiveWeek_kW = 0;
	
	// Supply
	J_FlowsMap fm_supply_kW = new J_FlowsMap();

	//Other
	double batteryStoredEnergyLiveWeek_MWh = 0;
	
	for (GridConnection gc : gcList){
		for (OL_EnergyCarriers EC_consumption : gc.v_liveData.activeConsumptionEnergyCarriers) {
			fm_demand_kW.addFlow( EC_consumption, gc.v_liveData.dsm_liveDemand_kW.get(EC_consumption).getY(i));			
		}
		for (OL_EnergyCarriers EC_production : gc.v_liveData.activeProductionEnergyCarriers) {
			fm_supply_kW.addFlow( EC_production, gc.v_liveData.dsm_liveSupply_kW.get(EC_production).getY(i));
		}
		for (OL_AssetFlowCategories AC : gc.v_liveAssetsMetaData.activeAssetFlows) {
			fm_currentAssetFlows_kW.addFlow(AC, gc.v_liveData.dsm_liveAssetFlows_kW.get(AC).getY(i));
		}
		
		electricityDemandCapacityLiveWeek_kW += gc.v_liveData.data_gridCapacityDemand_kW.getY(i);
		electricitySupplyCapacityLiveWeek_kW += gc.v_liveData.data_gridCapacitySupply_kW.getY(i);
		netLoadLiveWeek_kW  += gc.v_liveData.data_liveElectricityBalance_kW.getY(i);

		//Other 
		batteryStoredEnergyLiveWeek_MWh += 	gc.v_liveData.data_batteryStoredEnergyLiveWeek_MWh.getY(i);
	}
	
	for (OL_EnergyCarriers EC_consumption : v_liveData.activeConsumptionEnergyCarriers) {
		v_liveData.dsm_liveDemand_kW.get(EC_consumption).add(timeAxisValue, roundToDecimal(fm_demand_kW.get(EC_consumption), 3));
	}
	for (OL_EnergyCarriers EC_production : v_liveData.activeProductionEnergyCarriers) {
		v_liveData.dsm_liveSupply_kW.get(EC_production).add(timeAxisValue, roundToDecimal(fm_supply_kW.get(EC_production), 3));
	}
	
	for (OL_AssetFlowCategories AC : fm_currentAssetFlows_kW.keySet()) {
		v_liveData.dsm_liveAssetFlows_kW.get(AC).add(timeAxisValue, fm_currentAssetFlows_kW.get(AC));
	}
	
	v_liveData.data_gridCapacityDemand_kW.add(timeAxisValue, electricityDemandCapacityLiveWeek_kW);
	v_liveData.data_gridCapacitySupply_kW.add(timeAxisValue, electricitySupplyCapacityLiveWeek_kW);
	v_liveData.data_liveElectricityBalance_kW.add(timeAxisValue, netLoadLiveWeek_kW);

	//Stored
	v_liveData.data_batteryStoredEnergyLiveWeek_MWh.add(timeAxisValue, batteryStoredEnergyLiveWeek_MWh);
}


//Calculate cumulative asset capacities
f_getTotalInstalledCapacityOfAssets_live();

//Recalculate SOC ts for energycoop
f_recalculateSOCDataSet_live();

/*ALCODEEND*/}

double f_rapidRunDataLogging()
{/*ALCODESTART::1741626527076*/
// EnergyCoop specific
acc_totalOwnElectricityProduction_kW.addStep( v_currentOwnElectricityProduction_kW );
acc_totalCustomerDelivery_kW.addStep( v_currentCustomerDelivery_kW );
acc_totalCustomerFeedIn_kW.addStep( v_currentCustomerFeedIn_kW );

//Run rapid data logging
v_rapidRunData.addTimeStep(fm_currentBalanceFlows_kW, 
							fm_currentConsumptionFlows_kW, 
							fm_currentProductionFlows_kW, 
							fm_currentAssetFlows_kW,
							v_currentPrimaryEnergyProduction_kW, 
							v_currentFinalEnergyConsumption_kW, 
							v_currentPrimaryEnergyProductionHeatpumps_kW, 
							v_currentEnergyCurtailed_kW, 
							v_batteryStoredEnergy_kWh/1000, 
							energyModel);
/*ALCODEEND*/}

double f_connectCoopBattery()
{/*ALCODESTART::1742569887460*/
GCGridBattery coopBattery = findFirst(energyModel.GridBatteries, bat -> bat.p_batteryAlgorithm instanceof J_BatteryManagementPeakShaving && ((J_BatteryManagementPeakShaving)bat.p_batteryAlgorithm).getTargetType() == OL_ResultScope.ENERGYCOOP && ((J_BatteryManagementPeakShaving)bat.p_batteryAlgorithm).getTarget() == null);

if(coopBattery != null){
	//Reset previous state
	coopBattery.v_previousPowerElectricity_kW = 0;
	
	//Connect to coop
	coopBattery.c_parentCoops.add(this);
	c_memberGridConnections.add(coopBattery);
}
/*ALCODEEND*/}

double f_getTotalInstalledCapacityOfAssets_live()
{/*ALCODESTART::1744211359139*/
//Collect live asset totals
v_liveAssetsMetaData.totalInstalledWindPower_kW = 0.0;
v_liveAssetsMetaData.totalInstalledPVPower_kW = 0.0;
v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh = 0.0;

//Add all battery storage capacities of gc
for(GridConnection GC : c_memberGridConnections){
	v_liveAssetsMetaData.totalInstalledWindPower_kW += GC.v_liveAssetsMetaData.totalInstalledWindPower_kW;
	v_liveAssetsMetaData.totalInstalledPVPower_kW += GC.v_liveAssetsMetaData.totalInstalledPVPower_kW;
	v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += GC.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
}

//Do this also for the 'child' coops
for(Agent a :  c_coopMembers ) { // Take 'behind the meter' production and consumption!
	if (a instanceof EnergyCoop) {
		EnergyCoop EC = (EnergyCoop)a;
		EC.f_getTotalInstalledCapacityOfAssets_live();
		v_liveAssetsMetaData.totalInstalledWindPower_kW += EC.v_liveAssetsMetaData.totalInstalledWindPower_kW;
		v_liveAssetsMetaData.totalInstalledPVPower_kW += EC.v_liveAssetsMetaData.totalInstalledPVPower_kW;
		v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += EC.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
	}
}
/*ALCODEEND*/}

double f_recalculateSOCDataSet_live()
{/*ALCODESTART::1744271942642*/
double totalInstalledBatteryStorageCapacity_MWh = v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh;


double currentSOC = 0;
int liveWeekSize = v_liveData.data_batteryStoredEnergyLiveWeek_MWh.size();

for (int i=0; i < liveWeekSize; i++){
	if(totalInstalledBatteryStorageCapacity_MWh > 0){
		currentSOC = v_liveData.data_batteryStoredEnergyLiveWeek_MWh.getY(i)/totalInstalledBatteryStorageCapacity_MWh;
	}
	else{
		currentSOC = 0;
	}
	v_liveData.data_batterySOC_fr.add(v_liveData.data_batteryStoredEnergyLiveWeek_MWh.getX(i), roundToDecimal(currentSOC, 3));
}
/*ALCODEEND*/}

double f_startAfterDeserialisation()
{/*ALCODESTART::1753348770752*/
v_liveData = new J_LiveData(this);
//v_liveConnectionMetaData = new J_ConnectionMetaData(this);
//v_liveAssetsMetaData = new J_AssetsMetaData(this);
v_liveData.connectionMetaData = v_liveConnectionMetaData;
v_liveData.assetsMetaData = v_liveAssetsMetaData;

fm_currentProductionFlows_kW = new J_FlowsMap();
fm_currentConsumptionFlows_kW = new J_FlowsMap();
fm_currentBalanceFlows_kW = new J_FlowsMap();
fm_currentAssetFlows_kW = new J_ValueMap(OL_AssetFlowCategories.class);
/*ALCODEEND*/}

EnergyCoop f_addConsumptionEnergyCarrier(OL_EnergyCarriers EC)
{/*ALCODESTART::1754380102233*/
if (!v_liveData.activeConsumptionEnergyCarriers.contains(EC)) {
	v_liveData.activeEnergyCarriers.add(EC);
	v_liveData.activeConsumptionEnergyCarriers.add(EC);
	
	DataSet dsDemand = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
	
	double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
		dsDemand.add( t, 0);
	}
	v_liveData.dsm_liveDemand_kW.put( EC, dsDemand);
}
/*ALCODEEND*/}

EnergyCoop f_addProductionEnergyCarrier(OL_EnergyCarriers EC)
{/*ALCODESTART::1754380102235*/
if (!v_liveData.activeProductionEnergyCarriers.contains(EC)) {
	v_liveData.activeEnergyCarriers.add(EC);
	v_liveData.activeProductionEnergyCarriers.add(EC);
	
	DataSet dsSupply = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
	double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
		dsSupply.add( t, 0);
	}
	v_liveData.dsm_liveSupply_kW.put( EC, dsSupply);
}
/*ALCODEEND*/}

EnergyCoop f_addAssetFlow(OL_AssetFlowCategories AC)
{/*ALCODESTART::1754380102237*/
if (!v_liveAssetsMetaData.activeAssetFlows.contains(AC)) {
	v_liveAssetsMetaData.activeAssetFlows.add(AC);
	
	DataSet dsAsset = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
	
	double startTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMin();
	double endTime = v_liveData.dsm_liveDemand_kW.get(OL_EnergyCarriers.ELECTRICITY).getXMax();
	for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
		dsAsset.add( t, 0);
	}
	v_liveData.dsm_liveAssetFlows_kW.put( AC, dsAsset);
	
	if (AC == OL_AssetFlowCategories.batteriesChargingPower_kW) { // also add batteriesDischarging!
		dsAsset = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
		
		for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
			dsAsset.add( t, 0);
		}
		v_liveData.dsm_liveAssetFlows_kW.put( OL_AssetFlowCategories.batteriesDischargingPower_kW, dsAsset);
	}
	if (AC == OL_AssetFlowCategories.V2GPower_kW && !v_liveAssetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.evChargingPower_kW)) { // also add evCharging!
		dsAsset = new DataSet( (int)(168 / energyModel.p_timeStep_h) );
		
		for (double t = startTime; t <= endTime; t += energyModel.p_timeStep_h) {
			dsAsset.add( t, 0);
		}
		v_liveData.dsm_liveAssetFlows_kW.put( OL_AssetFlowCategories.evChargingPower_kW, dsAsset);
	}			
}
/*ALCODEEND*/}

double f_createAndInitializeRapidRunDataClass()
{/*ALCODESTART::1754666678297*/
//Create rapid run data class used to collect rapid run data of other gc
v_rapidRunData = new J_RapidRunData(this);
v_rapidRunData.assetsMetaData = v_liveAssetsMetaData.getClone();
   
EnumSet<OL_EnergyCarriers> activeEnergyCarriers_rapidRun = EnumSet.copyOf(v_liveData.activeEnergyCarriers);
EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers_rapidRun = EnumSet.copyOf(v_liveData.activeConsumptionEnergyCarriers);
EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers_rapidRun = EnumSet.copyOf(v_liveData.activeProductionEnergyCarriers);
	
//Need to do this, for if the sliders have changed, otherwise potential errors/missing data
boolean storeTotalAssetFlows = true;
for(GridConnection GC : c_memberGridConnections){
	activeEnergyCarriers_rapidRun.addAll(GC.v_rapidRunData.activeEnergyCarriers);
	activeConsumptionEnergyCarriers_rapidRun.addAll(GC.v_rapidRunData.activeConsumptionEnergyCarriers);
	activeProductionEnergyCarriers_rapidRun.addAll(GC.v_rapidRunData.activeProductionEnergyCarriers);
	
	v_rapidRunData.assetsMetaData.activeAssetFlows.addAll(GC.v_rapidRunData.assetsMetaData.activeAssetFlows);
	
	if(GC.v_rapidRunData.getStoreTotalAssetFlows() == false){
		storeTotalAssetFlows = false;
	}
}

//Adjust StoreTotalAssetFlows accordingly to the member data
v_rapidRunData.setStoreTotalAssetFlows(storeTotalAssetFlows);

//For now assumed to stay the same even after slider change: can't see rapid run graphs anyway after slider change
v_rapidRunData.connectionMetaData = v_liveConnectionMetaData.getClone();

//Initialize the rapid run data
v_rapidRunData.initializeAccumulators(energyModel.p_runEndTime_h - energyModel.p_runStartTime_h, energyModel.p_timeStep_h, activeEnergyCarriers_rapidRun, activeConsumptionEnergyCarriers_rapidRun, activeProductionEnergyCarriers_rapidRun);

/*ALCODEEND*/}

List<GridConnection> f_getMemberGridConnectionsCollectionPointer()
{/*ALCODESTART::1754908113703*/
return this.c_memberGridConnections; // This should NOT be a copy, it should be a pointer!!
/*ALCODEEND*/}

double f_addMembers(List<GridConnection> gcList)
{/*ALCODESTART::1756290844166*/
c_memberGridConnections.addAll(gcList);
f_initialize();
/*ALCODEEND*/}

double f_removeMembers(List<GridConnection> gcList)
{/*ALCODESTART::1756301338833*/
c_memberGridConnections.addAll(gcList);
f_initialize();
/*ALCODEEND*/}

