/**
 * J_EAConversionDieselVehicle
 */
public class J_EADieselVehicle extends J_EAVehicle implements Serializable {

	private OL_EnergyCarriers energyCarrierConsumed = OL_EnergyCarriers.DIESEL;

	/**
     * Default constructor
     */
    public J_EADieselVehicle() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EADieselVehicle(Agent ownerAssetAgent, double energyConsumption_kWhpkm, double timestep_h, double vehicleScaling, OL_EnergyAssetType energyAssetType, J_ActivityTrackerTrips tripTracker ) {
	    this.parentAgent = ownerAssetAgent;
	    this.energyConsumption_kWhpkm = energyConsumption_kWhpkm;
	    /*if (energyAssetType == OL_EnergyAssetType.DIESEL_VAN) {
	    	traceln("DieselVan energyconsumption: %s", energyConsumption_kWhpkm);
	    }*/
	    this.timestep_h = timestep_h;
	    this.vehicleScaling = vehicleScaling;
	    this.energyAssetType = energyAssetType; //OL_EnergyAssetType.DIESEL_VEHICLE; // AANPASSING ATE, scheelt code in Interface
	    this.tripTracker = tripTracker; 
	    if (tripTracker != null) {
	    	tripTracker.Vehicle=this;
	    }		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
	}
    
    @Override
    public void f_updateAllFlows() {
    	
    	flowsMap.put(OL_EnergyCarriers.DIESEL, this.energyUse_kW);

    	if (parentAgent instanceof GridConnection) {
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
    	}
    	this.lastFlowsMap = flowsMap;
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	/*if (Double.isNaN(this.energyUse_kW)) {
    		throw new RuntimeException("Diesel vehicle energyUse_kW is NaN!");
    	}*/
    	//Pair<J_FlowsMap, Double> flowspair = new Pair(this.flowsMap, this.energyUse_kW);
    	clear(); 
    	//return this.flowsMap;
    }
    
    /*public double[] operate(double ratioOfChargeCapacity_r) {
    	return returnEnergyFlows();
    }*/
    
	@Override
	public boolean startTrip() {
		if (available) {
			this.available = false;
			//traceln("Diesel vehicle starting trip!");
			return true;
		} else {
			traceln("Trip not started because vehicle not available!");
			return false; // Trip not started because vehicle not available!
		}
	}	

	@Override
    public boolean endTrip(double tripDist_km) {
		
		if(available) {
			traceln("Trip not ended because vehicle never left!, tripIdentifier = " + tripTracker.tripPatternIdentifier);
			return false;
		} else {
	    	this.available = true;
	    	return true;
		}
    }

	public boolean progressTrip(double marginalTripDist_km) {
		if( available) {
			traceln("Trip not updated because vehicle never left!");
			return false;
		}
		else {
			mileage_km += marginalTripDist_km;
	    	double energyUsedThisTimestep_kWh = marginalTripDist_km * vehicleScaling * energyConsumption_kWhpkm;
	    	energyUsed_kWh += energyUsedThisTimestep_kWh;
	    	//dieselConsumption_kW = energyUsedThisTimestep_kWh / timestep_h;
	    	energyUse_kW += energyUsedThisTimestep_kWh / timestep_h;
			return true;
		}
	}
	
	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}

	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	availableStored = available;
    	available = true;
    	mileage_km = 0;
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;    	
    	available = availableStored;
    }
	
	
	@Override
	public String toString() {
		return
			"parentAgent = " + parentAgent +" " +
			"energyConsumption_kWhpkm =" + energyConsumption_kWhpkm +" " +
			"vehicleScaling = " + vehicleScaling;
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}