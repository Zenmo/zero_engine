double f_connectToChild(Agent ConnectingChildNode)
{/*ALCODESTART::1660736336138*/
subConnections.connectTo(ConnectingChildNode);

/*ALCODEEND*/}

double f_connectToParentActor()
{/*ALCODESTART::1660736336141*/
if ( p_parentActorID != null ) {
	ConnectionOwner myParentActor = findFirst(main.pop_connectionOwners, p->p.p_actorID.equals(p_parentActorID)) ;
	if( myParentActor instanceof ConnectionOwner) {
		p_parentActor = myParentActor;
		superConnection.connectTo(myParentActor);
		myParentActor.f_connectToChild(this);
	}
}
/*ALCODEEND*/}

double f_doEnergyTransaction(double transactionVolume_kWh,OL_ContractType contractType)
{/*ALCODESTART::1660743197178*/
double transactionCostClient_eur = 0;
double transactionCostNat_eur = 0;
/*switch (contractType) {
	case ELECTRICITY_FIXED:
			transactionCostClient_eur = transactionVolume_kWh * p_fixedElectricityPrice_eurpkWh;
			transactionCostNat_eur = - transactionVolume_kWh * v_currentVariableElectricityPrice_eurpkWh;
			v_totalElectricitySoldToClients_kWh += max(0,transactionVolume_kWh);
			v_totalElectricityBoughtFromClients_kWh -= min(0,transactionVolume_kWh);
			v_currentNettElectricityVolume_kWh += transactionVolume_kWh;
			v_currentBalanceElectricityClients_eur += transactionCostClient_eur;
		break;
	case ELECTRICITY_VARIABLE:
			transactionCostClient_eur = transactionVolume_kWh * (p_variableElectricityPriceOverNational_eurpkWh + v_currentVariableElectricityPrice_eurpkWh);
			transactionCostNat_eur = - transactionVolume_kWh * v_currentVariableElectricityPrice_eurpkWh;
			v_currentBalanceElectricityClients_eur += transactionCostClient_eur;
		break;
	case HEAT_FIXED:
			transactionCostClient_eur = transactionVolume_kWh * p_fixedHeatPrice_eurpkWh;
			transactionCostNat_eur = - transactionVolume_kWh * 0;
			v_currentBalanceHeatClients_eur += transactionCostClient_eur;
		break;
	case METHANE_FIXED:
			transactionCostClient_eur = transactionVolume_kWh * p_fixedMethanePrice_eurpkWh;
			transactionCostNat_eur = - transactionVolume_kWh * 0;
			v_currentBalanceMethaneClients_eur += transactionCostClient_eur;
		break;
	case HYDROGEN_FIXED:
			transactionCostClient_eur = transactionVolume_kWh * p_fixedHydrogenPrice_eurpkWh;
			transactionCostNat_eur = - transactionVolume_kWh * 0;
			v_currentBalanceHydrogenClients_eur += transactionCostClient_eur;
		break;
	default:
		throw new IllegalStateException("Invalid contract type: " + contractType);
}*/
v_currentBalanceClients_eur += transactionCostClient_eur;
return transactionCostClient_eur;
/*ALCODEEND*/}

double f_getVariableEnergyPrice()
{/*ALCODESTART::1660746864022*/
return v_currentVariableElectricityPrice_eurpkWh;
/*ALCODEEND*/}

double f_updateEnergyPrice()
{/*ALCODESTART::1660746926252*/
v_currentVariableElectricityPrice_eurpkWh = energyModel.nationalEnergyMarket.f_getNationalElectricityPrice_eurpMWh()/1000;
v_currentNettElectricityVolume_kWh = 0;
/*ALCODEEND*/}

double f_updateFinances()
{/*ALCODESTART::1660806140354*/
v_totalElectricityBoughtFromNat_kWh += max(0,v_currentNettElectricityVolume_kWh);
v_totalElectricitySoldToNat_kWh -= min(0,v_currentNettElectricityVolume_kWh);
v_currentBalanceNat_eur -= v_currentNettElectricityVolume_kWh * v_currentVariableElectricityPrice_eurpkWh;
/*ALCODEEND*/}

