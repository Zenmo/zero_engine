double f_connectToChild(GridNode ConnectingChildNode,OL_EnergyCarriers energyType)
{/*ALCODESTART::1660736411309*/
subConnections.connectTo( ConnectingChildNode );
if (energyType == OL_EnergyCarriers.ELECTRICITY) {
	c_electricityGridNodes.add( ConnectingChildNode );
}
else {
	traceln( "f_connectToChild vanuit GridOperator voegt een type Node toe wat geen ELECTRICITY gridNOde is");
}

/*ALCODEEND*/}

double f_connectToParentActor()
{/*ALCODESTART::1660736411312*/
if ( p_parentActorID != null ) {
	ConnectionOwner myParentActor = findFirst(main.pop_connectionOwners, p->p.p_actorID.equals(p_parentActorID)) ;
	if( myParentActor instanceof ConnectionOwner) {
		p_parentActor = myParentActor;
		superConnection.connectTo(myParentActor);
		myParentActor.f_connectToChild(this);
	}
}
/*ALCODEEND*/}

double f_updateCongestionTariff()
{/*ALCODESTART::1664465199508*/
for( GridNode n : c_electricityGridNodes ){
	double currentLoad_kW = n.v_filteredLoadCongestionPricing_kW;
	
	if ( abs(currentLoad_kW) > v_congestionThreshold_fr * n.p_capacity_kW  ){
		n.v_congested = true;
		n.v_currentCongestionPrice_eurpkWh = signum(currentLoad_kW)*(abs(currentLoad_kW) / n.p_capacity_kW - v_congestionThreshold_fr ) / ( 1 - v_congestionThreshold_fr) * v_congestionPrice_eurpkWh;		
		n.f_setCongestionTariff(n.v_currentCongestionPrice_eurpkWh); 
		if (currentLoad_kW > 0) {
			n.v_congestionMode = "Overconsumption";
			//n.f_setCongestionTariff(n.v_currentCongestionPrice_eurpkWh); 
		} 
		else {
			n.v_congestionMode = "Overproduction";	
			//n.f_setCongestionTariff(-n.v_currentCongestionPrice_eurpkWh); 
		}
		if (!c_congestedGNConsumption.contains(n)){ c_congestedGNConsumption.add(n); }
	}
	else {
		if ( n.v_congested ){
			n.f_setCongestionTariff(0);
			n.v_congested = false;
			n.v_congestionMode = "No congestion atm";
			n.v_currentCongestionPrice_eurpkWh = 0;
			if (c_congestedGNProduction.contains(n)){ c_congestedGNProduction.remove(c_congestedGNProduction.indexOf(n));}
			if (c_congestedGNConsumption.contains(n)){ c_congestedGNConsumption.remove(c_congestedGNConsumption.indexOf(n));}
		}
	}
	
}


/*ALCODEEND*/}

