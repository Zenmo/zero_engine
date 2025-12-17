double f_connectToChild(GridConnection ConnectingChildNode)
{/*ALCODESTART::1660230297342*/
c_ownedGridConnections.add(ConnectingChildNode);
//v_ownedGridConnection = ConnectingChildNode;
//traceln("f_connectToChild in connectionOwner agent, connection contract type: " + v_contractConnection.connectionContractType);
/*if (v_contractConnection.connectionContractType.equals(OL_ConnectionContractType.NFATO)) {
	ConnectingChildNode.v_enable_nfATO_b = true;
	ConnectingChildNode.p_nfatoStart_h = v_contractConnection.nfATOstart_h;
	ConnectingChildNode.p_nfatoEnd_h = v_contractConnection.nfATOend_h;
	ConnectingChildNode.p_nfatoLvl_kW = v_contractConnection.nfATOpower_kW;
	traceln("Setting nfATO TRUE for gridConnection: " + ConnectingChildNode.p_gridNodeID);
}*/

/*ALCODEEND*/}

double f_connectToParentActor()
{/*ALCODESTART::1660230784171*/
//f_setContractValues();
//f_setInitPriceBands();
// Connect to energy supplier (can be a Coop!)
//traceln(p_actorID + " is looking for its supplier");
if (v_contractDelivery == null) {
	throw new RuntimeException(
		String.format(
			"Actor %s has no delivery contract",
			p_actorID
		)
	);
}
Actor mySupplier = findFirst(energyModel.pop_energySuppliers, p->p.p_actorID.equals(v_contractDelivery.contractScope)) ;
if (mySupplier != null) {
	((EnergySupplier)mySupplier).f_connectToChild(this);	
} else {
	mySupplier = findFirst(energyModel.pop_energyCoops, p->p.p_actorID.equals(v_contractDelivery.contractScope)) ;
	if (mySupplier != null) {		
		((EnergyCoop)mySupplier).f_connectToChild(this,OL_EnergyCarriers.ELECTRICITY);
		p_coopParent = (EnergyCoop)mySupplier;
	}
}

if (mySupplier != null) {
	//energySupplier.connectTo(mySupplier);
	p_electricitySupplier = mySupplier;
	//energySupplier.f_connectToChild(this);	
} else { 
	/*traceln(
		"Connection owner %s --> f_connectToParentActor --> no parent actor found, this should not be happening. contract holder: %s, parent: %s",
		this,
		v_contractDelivery.contractHolder,
		v_contractDelivery.contractScope
	);*/
}

// Connect to grid operator (can be a Coop!)
if (v_contractTransport == null) {
	throw new RuntimeException(
		String.format(
			"Actor %s has no transport contract",
			p_actorID
		)
	);
}
Actor myGridoperator = findFirst(energyModel.pop_gridOperators, p->p.p_actorID.equals(v_contractTransport.contractScope)) ;
if (myGridoperator == null) {
	myGridoperator = findFirst(energyModel.pop_energyCoops, p->p.p_actorID.equals(v_contractTransport.contractScope)) ;
}

if (myGridoperator != null) {
	p_gridOperator = myGridoperator;	
} else { 
	/*traceln(
	    "Connection owner --> f_connectToParentActor --> no grid operator found, this should not be happening. contract holder: %s, operator: %s",
	    v_contractTransport.contractHolder,
	    v_contractTransport.contractScope
	);*/
}

//traceln(p_actorID + " finished looking for supplier");

/*ALCODEEND*/}

double f_updateFinances()
{/*ALCODESTART::1660742520265*/
//TODO: PetroleumFuel costs still missing!! [GH 21/11/2022] -> Won't be in anylogic model, but in postprocessing.

// get current energy flows
f_gatherEnergyFlows();

//double currentPowerDrawn_kW = v_electricityVolume_kWh / energyModel.p_timeStep_h;
double transactionCostDelivery_eur = 0;
double transactionCostTransport_eur = 0;
double transactionCostTax_eur = 0;

if (v_electricityVolume_kWh >= 0) {
	//transactionCostDelivery_eur = v_priceBandsDelivery.ceilingEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	transactionCostDelivery_eur = (v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh) * v_electricityVolume_kWh;
	v_balanceElectricityDelivery_eur -= transactionCostDelivery_eur;
	v_balanceElectricity_eur -= transactionCostDelivery_eur;
	
	//transactionCostTransport_eur = v_priceBandsTransport.ceilingEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	transactionCostTransport_eur = v_currentNodalPrice_eurpkWh * v_electricityVolume_kWh;
	v_balanceElectricityTransport_eur -= transactionCostTransport_eur;
	v_balanceElectricity_eur -= transactionCostTransport_eur;
	
	//transactionCostTax_eur = v_priceBandsTax.ceilingEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	transactionCostTax_eur = v_contractTax.deliveryTax_eurpkWh * v_electricityVolume_kWh;
	//transactionCostTax_eur = v_electricityVolume_kWh * v_contractTax.deliveryTax_eurpkWh + v_contractTax.proportionalTax_pct*(v_electricityVolume_kWh * v_contractTax.deliveryTax_eurpkWh + transactionCostDelivery_eur + transactionCostTransport_eur);
	v_balanceElectricityTax_eur -= transactionCostTax_eur;
	v_balanceElectricity_eur -= transactionCostTax_eur;
} else {
	//transactionCostDelivery_eur = v_priceBandsDelivery.floorEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	transactionCostDelivery_eur = (v_contractDelivery.feedinPrice_eurpkWh + v_electricityVariablePrice_eurpkWh) * v_electricityVolume_kWh;
	v_balanceElectricityDelivery_eur -= transactionCostDelivery_eur;
	v_balanceElectricity_eur -= transactionCostDelivery_eur;
	
	//transactionCostTransport_eur = v_priceBandsTransport.floorEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	transactionCostTransport_eur = v_currentNodalPrice_eurpkWh * v_electricityVolume_kWh;
	v_balanceElectricityTransport_eur -= transactionCostTransport_eur;
	v_balanceElectricity_eur -= transactionCostTransport_eur;
	
	//transactionCostTax_eur = v_priceBandsTax.floorEntry( currentPowerDrawn_kW ).getValue() * v_electricityVolume_kWh;
	transactionCostTax_eur = v_contractTax.feedinTax_eurpkWh * v_electricityVolume_kWh;
	//transactionCostTax_eur = v_electricityVolume_kWh * v_contractTax.feedinTax_eurpkWh + v_contractTax.proportionalTax_pct*(v_electricityVolume_kWh * v_contractTax.feedinTax_eurpkWh + transactionCostDelivery_eur + transactionCostTransport_eur);
	v_balanceElectricityTax_eur -= transactionCostTax_eur;
	v_balanceElectricity_eur -= transactionCostTax_eur;
}

/*// trigger transaction with supplier/Coop
if( p_energySupplier != null) {
	//Agent energySupplier = p_energySupplier;
	if( p_energySupplier instanceof EnergySupplier) {
		EnergySupplier energySupplier = (EnergySupplier)p_energySupplier;
		double transactionCost_eur = 0;//energySupplier.f_doEnergyTransaction(v_electricityVolume_kWh, v_electricityContractType);
		v_balanceElectricity_eur -= transactionCost_eur;
		transactionCost_eur = energySupplier.f_doEnergyTransaction(v_methaneVolume_kWh, v_methaneContractType);
		v_balanceMethane_eur -= transactionCost_eur;
		transactionCost_eur = energySupplier.f_doEnergyTransaction(v_hydrogenVolume_kWh, v_hydrogenContractType);
		v_balanceHydrogen_eur -= transactionCost_eur;
	} else if ( p_energySupplier instanceof EnergyCoop ) {
		EnergyCoop energySupplier = (EnergyCoop)p_energySupplier;
		double transactionCost_eur = 0;//energySupplier.f_doEnergyTransaction(v_electricityVolume_kWh, v_electricityContractType);
		v_balanceElectricity_eur -= transactionCost_eur;
		transactionCost_eur = energySupplier.f_doEnergyTransaction(v_methaneVolume_kWh, v_methaneContractType);
		v_balanceMethane_eur -= transactionCost_eur;
		transactionCost_eur = energySupplier.f_doEnergyTransaction(v_hydrogenVolume_kWh, v_hydrogenContractType);
		v_balanceHydrogen_eur -= transactionCost_eur;
	}
} else {
	traceln("Connection owner " + this + " has no energy supplier!");
}*/

// Also needs to include congestion tariffs! 
/*if ( p_congestionTariffApplicable ) {
	double congestionCost_eur = v_electricityVolume_kWh * v_currentCongestionTariffWhenBuying_eurpkWh;
	v_balanceElectricityTransport_eur -= congestionCost_eur; // TODO: This transaction is 'one-sided'; the gridOperator is not yet receiving/giving this money!!
}*/

// trigger district heat accounting with specified heat supplier agent
/*
if( v_energySupplierDistrictHeat instanceof EnergySupplier) {
	EnergySupplier energySupplier = (EnergySupplier)v_energySupplierDistrictHeat;
	double transactionCost_eur = 0;//energySupplier.f_doEnergyTransaction(v_heatVolume_kWh, v_heatContractType);
	v_balanceHeat_eur -= transactionCost_eur;
}
else if( v_energySupplierDistrictHeat instanceof EnergyCoop ) {
	EnergyCoop energySupplier = (EnergyCoop)v_energySupplierDistrictHeat;
	double transactionCost_eur = 0;// energySupplier.f_doEnergyTransaction(v_heatVolume_kWh, v_heatContractType);
	v_balanceHeat_eur -= transactionCost_eur;
}

v_balancePetroleumFuel_eur += v_petroleumFuelVolume_kWh * .196; // TODO: UGLY HACK
*/
/*ALCODEEND*/}

double f_gatherEnergyFlows()
{/*ALCODESTART::1660743030872*/
// reset eenrgy flows
v_electricityVolume_kWh = 0;
v_heatVolume_kWh = 0;
v_methaneVolume_kWh = 0;
v_hydrogenVolume_kWh = 0;
v_petroleumFuelVolume_kWh = 0;


// gather electricity flows, convert to volume-per-timestep to facilitate financial transactions
for(GridConnection n :  gridConnections.getConnections() ) {
	v_electricityVolume_kWh += (n.v_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - n.v_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY)) * energyModel.p_timeStep_h;
	v_heatVolume_kWh += (n.v_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HEAT) - n.v_currentProductionFlows_kW.get(OL_EnergyCarriers.HEAT)) * energyModel.p_timeStep_h;
	v_methaneVolume_kWh += (n.v_currentConsumptionFlows_kW.get(OL_EnergyCarriers.METHANE) - n.v_currentProductionFlows_kW.get(OL_EnergyCarriers.METHANE)) * energyModel.p_timeStep_h;
	v_hydrogenVolume_kWh += (n.v_currentConsumptionFlows_kW.get(OL_EnergyCarriers.HYDROGEN) - n.v_currentProductionFlows_kW.get(OL_EnergyCarriers.HYDROGEN)) * energyModel.p_timeStep_h;
	v_petroleumFuelVolume_kWh += (n.v_currentConsumptionFlows_kW.get(OL_EnergyCarriers.PETROLEUM_FUEL) - n.v_currentProductionFlows_kW.get(OL_EnergyCarriers.PETROLEUM_FUEL)) * energyModel.p_timeStep_h;
}

v_totalElectricityUsed_kWh += v_electricityVolume_kWh;
v_totalHeatUsed_kWh += v_heatVolume_kWh;
v_totalMethaneUsed_kWh += v_methaneVolume_kWh;
v_totalHydrogenUsed_kWh += v_hydrogenVolume_kWh;
v_totalPetroleumFuelUsed_kWh += v_petroleumFuelVolume_kWh;
/*ALCODEEND*/}

double f_updateIncentives()
{/*ALCODESTART::1663237679579*/
if (v_contractDelivery.deliveryContractType==OL_DeliveryContractType.ELECTRICITY_VARIABLE) {
	if (p_electricitySupplier instanceof EnergySupplier) {
		v_electricityVariablePrice_eurpkWh = ((EnergySupplier)p_electricitySupplier).f_getVariableEnergyPrice();
	} else if (p_electricitySupplier instanceof EnergyCoop) {
		v_electricityVariablePrice_eurpkWh = ((EnergyCoop)p_electricitySupplier).f_getVariableEnergyPrice();
	}
}

// Transport pricing (nodal pricing) is 'pushed' to the connectionOwner from the GridNode, so don't have to ask for the price here.
/*if (v_contractTransport.transportContractType.equals(OL_TransportContractType.NODALPRICING)) {
	if (p_gridOperator instanceof GridOperator) {
		v_electricityVariablePrice_eurpkWh = ((GridOperator)p_gridOperator).f_getVariableEnergyPrice(v_electricityContractType, false);
	} else if (p_gridOperator instanceof EnergyCoop) {
		v_electricityVariablePrice_eurpkWh = ((EnergyCoop)p_gridOperator).f_getEnergyPrice(v_electricityContractType, false);
	}
}*/	

//f_setElectricityPriceBands();

/*ALCODEEND*/}

double f_setElectricityPriceBands()
{/*ALCODESTART::1666529572357*/
if (v_contractDelivery.deliveryContractType.equals(OL_DeliveryContractType.ELECTRICITY_VARIABLE)) {
	v_priceBandsDelivery.replace(-9999999.0, v_contractDelivery.feedinPrice_eurpkWh + v_electricityVariablePrice_eurpkWh );
	v_priceBandsDelivery.replace(9999999.0, v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh );
}
if (v_contractTransport.transportContractType.equals(OL_TransportContractType.NODALPRICING)) {
	v_priceBandsTransport.replace(-9999999.0, v_currentNodalPrice_eurpkWh);
	v_priceBandsTransport.replace(9999999.0, v_currentNodalPrice_eurpkWh);
}

if (v_contractTax.proportionalTax_pct != 0.0 && (v_contractDelivery.deliveryContractType.equals(OL_DeliveryContractType.ELECTRICITY_VARIABLE) || v_contractTransport.transportContractType.equals(OL_TransportContractType.NODALPRICING))) { // Add VAT (BTW), over all other cost components. At this point assumes there are only 2 price-bands, for delivery and feedin! No other 'steps'! Not true for capacity-tariff (bandbreedtemodel)!
	v_priceBandsTax.replace(-9999999.0, v_contractTax.feedinTax_eurpkWh + v_contractTax.proportionalTax_pct*(v_contractDelivery.feedinPrice_eurpkWh + v_electricityVariablePrice_eurpkWh + v_currentNodalPrice_eurpkWh + v_contractTax.feedinTax_eurpkWh));
	v_priceBandsTax.replace(9999999.0, v_contractTax.deliveryTax_eurpkWh + v_contractTax.proportionalTax_pct*(v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh + v_currentNodalPrice_eurpkWh + v_contractTax.deliveryTax_eurpkWh));
}
	
/*ALCODEEND*/}

double f_setContractValues()
{/*ALCODESTART::1669036739539*/
double methaneDeliveryPrice_eurpkWh = 0.0;
double methaneDeliveryTax_eurpkWh = 0.0;
double methaneVAT_pct = 0.0;
double hydrogenDeliveryPrice_eurpkWh = 0.0;
double hydrogenDeliveryTax_eurpkWh = 0.0;
double hydrogenVAT_pct = 0.0;
double heatDeliveryPrice_eurpkWh = 0.0;
double heatDeliveryTax_eurpkWh = 0.0;
double heatVAT_pct = 0.0;

//if( p_actorContractList == null) {
	//traceln( "No contract data for connectionOwner " + p_actorID );
	//traceln( "Creating default contracts!");

	String defaultSupplier = "defaultSupplier";
	if (p_energyCoopID != null){
		defaultSupplier = p_energyCoopID;
	}
	String defaultGridOperator = "defaultGridOperator";
	String defaultGovernmentLayer = "defaultGovernmentLayer";
	double annualFee_eur = 0;
	double deliveryPrice_eurpkWh = 0.2;
	double feedinPrice_eurpkWh = 0.2;
	double bandwidthTreshold_kW = 0.0;
	double bandwidthTariff_eurpkWh = 0.0;
	double nfATOstart_h = 0.0;
	double nfATOend_h = 0.0;
	double nfATOcapacity_kW = 0.0;
	double taxDelivery_eurpkWh = 0.13;
	double taxFeedin_eurpkWh = 0.0;
	double proportionalTax_pct = 21;

	/*if (p_actorID.equals("logistics0owner")){
		v_contractDelivery = new J_DeliveryContract("coop1", OL_DeliveryContractType.ELECTRICITY_FIXED, OL_EnergyCarriers.ELECTRICITY, deliveryPrice_eurpkWh, feedinPrice_eurpkWh, annualFee_eur);
	} else {
		v_contractDelivery = new J_DeliveryContract(defaultSupplier, OL_DeliveryContractType.ELECTRICITY_FIXED, OL_EnergyCarriers.ELECTRICITY, deliveryPrice_eurpkWh, feedinPrice_eurpkWh, annualFee_eur);
	}*/
	v_contractDelivery = new J_DeliveryContract(defaultSupplier, OL_DeliveryContractType.ELECTRICITY_FIXED, OL_EnergyCarriers.ELECTRICITY, deliveryPrice_eurpkWh, feedinPrice_eurpkWh, annualFee_eur);

	v_contractTransport = new J_TransportContract(defaultGridOperator, OL_TransportContractType.DEFAULT , OL_EnergyCarriers.ELECTRICITY, bandwidthTreshold_kW, bandwidthTariff_eurpkWh, annualFee_eur);

	v_contractConnection = new J_ConnectionContract(defaultGridOperator, OL_ConnectionContractType.DEFAULT, OL_EnergyCarriers.ELECTRICITY, nfATOstart_h, nfATOend_h, nfATOcapacity_kW, annualFee_eur);

	v_contractTax = new J_TaxContract(defaultGovernmentLayer, OL_EnergyCarriers.ELECTRICITY, taxDelivery_eurpkWh, taxFeedin_eurpkWh, proportionalTax_pct, annualFee_eur);

	c_actorContracts.add(v_contractDelivery);
	c_actorContracts.add(v_contractTransport);
	c_actorContracts.add(v_contractConnection);
	c_actorContracts.add(v_contractTax);
/*
} else {
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
}
*/
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

double f_setInitPriceBands()
{/*ALCODESTART::1674919795415*/
v_priceBandsDelivery = new TreeMap<Double, Double>();
v_priceBandsDelivery.put(-9999999.0, v_contractDelivery.feedinPrice_eurpkWh);
v_priceBandsDelivery.put(0.0, 0.0);
v_priceBandsDelivery.put(9999999.0,  v_contractDelivery.deliveryPrice_eurpkWh);

v_priceBandsTransport = new TreeMap<Double, Double>();
v_priceBandsTransport.put(-9999999.0, 0.0);
v_priceBandsTransport.put(0.0, 0.0);
v_priceBandsTransport.put(9999999.0, 0.0);
if (v_contractTransport.transportContractType.equals(OL_TransportContractType.BANDWIDTH)) {
	//traceln("Setting capacity tariff for " + p_actorID);
	v_priceBandsTransport.put(-9999999.0, v_contractTransport.bandwidthTariff_eurpkWh);
	v_priceBandsTransport.put(-v_contractTransport.bandwidthTreshold_kW, 0.0);
	v_priceBandsTransport.put(0.0, 0.0);
	v_priceBandsTransport.put(v_contractTransport.bandwidthTreshold_kW, 0.0);	
	v_priceBandsTransport.put(9999999.0, v_contractTransport.bandwidthTariff_eurpkWh);
	//traceln(v_priceBandsTransport.toString());
} else {
	v_priceBandsTransport.put(-9999999.0, 0.0);
	v_priceBandsTransport.put(0.0, 0.0);
	v_priceBandsTransport.put(9999999.0, 0.0);
}

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

double f_getElectricityPrice(double currentPowerDrawn_kW)
{/*ALCODESTART::1675011274392*/
double price_eurpkWh = 0;
if( currentPowerDrawn_kW >= 0 ){
	//Delivery
	/*if( v_priceBandsDelivery.ceilingKey(currentPowerDrawn_kW) == currentPowerDrawn_kW ){
		price += v_priceBandsDelivery.higherEntry( currentPowerDrawn_kW ).getValue();
	}
	else{
		price += v_priceBandsDelivery.ceilingEntry( currentPowerDrawn_kW ).getValue();
	}*/
	price_eurpkWh += v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh;

	//Transport
	/*if( v_priceBandsTransport.ceilingKey(currentPowerDrawn_kW) == currentPowerDrawn_kW ){
		price += v_priceBandsTransport.higherEntry( currentPowerDrawn_kW ).getValue();
	}
	else{
		price += v_priceBandsTransport.ceilingEntry( currentPowerDrawn_kW ).getValue();
	}*/

	price_eurpkWh += v_currentNodalPrice_eurpkWh;
	//Tax
	/*if( v_priceBandsTax.ceilingKey(currentPowerDrawn_kW) == currentPowerDrawn_kW ){
		price += v_priceBandsTax.higherEntry( currentPowerDrawn_kW ).getValue();
	}
	else{
		price += v_priceBandsTax.ceilingEntry( currentPowerDrawn_kW ).getValue();
	}*/

	price_eurpkWh += v_contractTax.deliveryTax_eurpkWh + v_contractTax.proportionalTax_pct;// * (v_contractTax.deliveryTax_eurpkWh + v_currentNodalPrice_eurpkWh + v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh)
	price_eurpkWh = price_eurpkWh * (1+0.01*v_contractTax.proportionalTax_pct);
}
else {
	//Delivery
	/*if( v_priceBandsDelivery.ceilingKey(currentPowerDrawn_kW) == currentPowerDrawn_kW ){
		price += v_priceBandsDelivery.higherEntry( currentPowerDrawn_kW ).getValue();
	}
	else{
		price += v_priceBandsDelivery.floorEntry( currentPowerDrawn_kW ).getValue();
	}*/

	price_eurpkWh += v_contractDelivery.feedinPrice_eurpkWh + v_electricityVariablePrice_eurpkWh;
	//Transport
	/*if( v_priceBandsTransport.ceilingKey(currentPowerDrawn_kW) == currentPowerDrawn_kW ){
		price += v_priceBandsTransport.higherEntry( currentPowerDrawn_kW ).getValue();
	}
	else{
		price += v_priceBandsTransport.floorEntry( currentPowerDrawn_kW ).getValue();
	}*/
	price_eurpkWh += v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh;
	//Tax
	/*if( v_priceBandsTax.ceilingKey(currentPowerDrawn_kW) == currentPowerDrawn_kW ){
		price += v_priceBandsTax.higherEntry( currentPowerDrawn_kW ).getValue();
	}
	else{
		price += v_priceBandsTax.floorEntry( currentPowerDrawn_kW ).getValue();
	}*/
	price_eurpkWh += v_contractTax.feedinTax_eurpkWh + v_contractTax.proportionalTax_pct;// * (v_contractTax.deliveryTax_eurpkWh + v_currentNodalPrice_eurpkWh + v_contractDelivery.deliveryPrice_eurpkWh + v_electricityVariablePrice_eurpkWh)
	price_eurpkWh = price_eurpkWh * (1+0.01*v_contractTax.proportionalTax_pct);
}
//traceln(price_eurpkWh);
return price_eurpkWh;
/*ALCODEEND*/}

double f_getAvailablePowerAtPrice(double currentPowerDrawn_kW)
{/*ALCODESTART::1675012472432*/
double availablePower_kW;//, availablePowerDelivery, availablePowerTransport, availablePowerTax = 0;

/*if( currentPowerDrawn_kW >= 0 ){
	// Check if one of the pricebands is at a 'breakpoint'	
	if( currentPowerDrawn_kW == v_priceBandsDelivery.ceilingKey( currentPowerDrawn_kW )){
		availablePowerDelivery = v_priceBandsDelivery.higherKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	} 	else {
		availablePowerDelivery = v_priceBandsDelivery.ceilingKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	}
	if( currentPowerDrawn_kW == v_priceBandsTransport.ceilingKey( currentPowerDrawn_kW )){
		availablePowerTransport = v_priceBandsTransport.higherKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	} 	else {
		availablePowerTransport = v_priceBandsTransport.ceilingKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	}
	if( currentPowerDrawn_kW == v_priceBandsTax.ceilingKey( currentPowerDrawn_kW )){
		availablePowerTax = v_priceBandsTax.higherKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	} 	else {
		availablePowerTax = v_priceBandsTax.ceilingKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	}		
}
else {
	if( currentPowerDrawn_kW == v_priceBandsDelivery.floorKey( currentPowerDrawn_kW )){
		availablePowerDelivery = v_priceBandsDelivery.higherKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	} 	
	else {
		availablePowerDelivery = v_priceBandsDelivery.ceilingKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	}
	
	if( currentPowerDrawn_kW == v_priceBandsTransport.floorKey( currentPowerDrawn_kW )){
		availablePowerTransport = v_priceBandsTransport.higherKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	} 	
	else {
		availablePowerTransport = v_priceBandsTransport.ceilingKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	}
	
	if( currentPowerDrawn_kW == v_priceBandsTax.floorKey( currentPowerDrawn_kW )){
		availablePowerTax = v_priceBandsTax.higherKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	} 	
	else {
		availablePowerTax = v_priceBandsTax.ceilingKey( currentPowerDrawn_kW ) - currentPowerDrawn_kW;
	}		
	
}*/

//availablePower = min(min(availablePowerDelivery, availablePowerTransport), availablePowerTax);
if( currentPowerDrawn_kW >= 0 ){ // Only check if current power is positive or negative
	availablePower_kW = 1e10; // 'Infinite', in this case 10 TW. Something else should be the limiting factor on power.
} else {
	availablePower_kW = -currentPowerDrawn_kW;
}

//traceln("Still using treemaps in f_getAvailablePowerAtPrice!");
return availablePower_kW;
/*ALCODEEND*/}

double f_getAveragedElectricityPrice(double baseDemand_kW,double requestedPower_kW)
{/*ALCODESTART::1675068408612*/
double price_eurph = 0;
double availablePower_kW = 0;
double addedPrice_eurpkWh;
double addedPower_kW;
double currentPowerRequestLevel_kW;

boolean isDemandMet = false;
double loopcount = 1;

//traceln( " ");
//traceln( "starting demand: " + baseDemand_kW);
//traceln("Power request: " + requestedPower_kW);

while (! isDemandMet ) { // Loop door de treemap tot je requestedPower_kW bereikt
	//traceln( " ");
	//traceln( "loop " + loopcount + ", available Power: " + availablePower_kW);
	currentPowerRequestLevel_kW = roundToDecimal( availablePower_kW + baseDemand_kW, 5);
	//traceln("Current power request level = " + currentPowerRequestLevel_kW) ;
	
	addedPrice_eurpkWh = f_getElectricityPrice(currentPowerRequestLevel_kW);
	addedPower_kW = f_getAvailablePowerAtPrice(currentPowerRequestLevel_kW);
	//traceln("Added power " + addedPower_kW);
	
	if(addedPower_kW + availablePower_kW >= requestedPower_kW) {
		addedPower_kW = requestedPower_kW - availablePower_kW;
		price_eurph += addedPrice_eurpkWh * addedPower_kW;
		isDemandMet = true;
	} 
	else {
		availablePower_kW += addedPower_kW;
		price_eurph += addedPrice_eurpkWh * addedPower_kW;		
		loopcount += 1;
	}
	//traceln("price added " + addedPrice_eurpkWh);
}
//traceln("total price " + price_eurph);
//getEngine().pause();
traceln("Still using treemaps in f_getAveragedElectricityPrice!");

return price_eurph / requestedPower_kW;


/*ALCODEEND*/}

double f_getMethanePrice()
{/*ALCODESTART::1679478105441*/
if ( v_methanePrice_eurpkWh == 0.0 ){
	throw new IllegalStateException("Missing methane contracts! No methane price available for this connection!");
}
return v_methanePrice_eurpkWh;
/*ALCODEEND*/}

double f_getHydrogenPrice()
{/*ALCODESTART::1679478116012*/
if ( v_hydrogenPrice_eurpkWh == 0.0 ){
	throw new IllegalStateException("Missing hydrogen contracts! No hydrogen price available for this connection!");
}
return v_hydrogenPrice_eurpkWh;
/*ALCODEEND*/}

double f_resetStates()
{/*ALCODESTART::1704371830113*/
// Reset energytotals
//v_totalElectricityUsed_kWh = v_totalHeatUsed_kWh = v_totalMethaneUsed_kWh = v_totalHydrogenUsed_kWh = v_totalPetroleumFuelUsed_kWh = 0;
// Reset finances
//v_balanceElectricity_eur = v_balanceElectricityDelivery_eur = v_balanceElectricityTransport_eur = v_balanceElectricityTax_eur = 0;

/*ALCODEEND*/}

double f_initialize()
{/*ALCODESTART::1716897235822*/
// What happens if this function is triggered multiple times per connectionOwner?
f_setContractValues();
f_connectToParentActor();
/*ALCODEEND*/}

ArrayList<GridConnection> f_getOwnedGridConnections()
{/*ALCODESTART::1718289254859*/
return this.c_ownedGridConnections;
/*ALCODEEND*/}

