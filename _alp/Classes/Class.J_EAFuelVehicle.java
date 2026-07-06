/**
 * J_EAConversionPetroleumFuelVehicle
 */
public class J_EAFuelVehicle extends J_EAFixed implements I_Vehicle{

	private OL_EnergyCarriers energyCarrierConsumed;
	private boolean available = true;
	private boolean availableStored = true;
	private double energyConsumption_kWhpkm;
	private double vehicleScaling;
	private J_ActivityTrackerTrips tripTracker;
	private OL_VehicleType vehicleType;
	/**
     * Default constructor
     */
    public J_EAFuelVehicle() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAFuelVehicle(I_AssetOwner owner, double energyConsumption_kWhpkm, J_TimeParameters timeParameters, double vehicleScaling, OL_VehicleType vehicleType, J_ActivityTrackerTrips tripTracker, OL_EnergyCarriers energyCarrier ) {
    	this(owner, energyConsumption_kWhpkm, timeParameters, vehicleScaling, vehicleType, tripTracker, energyCarrier, true, null);
    }
    public J_EAFuelVehicle(I_AssetOwner owner, double energyConsumption_kWhpkm, J_TimeParameters timeParameters, double vehicleScaling, OL_VehicleType vehicleType, J_ActivityTrackerTrips tripTracker, OL_EnergyCarriers energyCarrier, boolean available ) {
    	this(owner, energyConsumption_kWhpkm, timeParameters, vehicleScaling, vehicleType, tripTracker, energyCarrier, available, null );
    }
    public J_EAFuelVehicle(I_AssetOwner owner, double energyConsumption_kWhpkm, J_TimeParameters timeParameters, double vehicleScaling, OL_VehicleType vehicleType, J_ActivityTrackerTrips tripTracker, OL_EnergyCarriers energyCarrier, OL_AssetFlowCategories assetFlowCatagory ) {
    	this(owner, energyConsumption_kWhpkm, timeParameters, vehicleScaling, vehicleType, tripTracker, energyCarrier, true, assetFlowCatagory);
    }
    public J_EAFuelVehicle(I_AssetOwner owner, double energyConsumption_kWhpkm, J_TimeParameters timeParameters, double vehicleScaling, OL_VehicleType vehicleType, J_ActivityTrackerTrips tripTracker, OL_EnergyCarriers energyCarrier, boolean available, OL_AssetFlowCategories assetFlowCatagory ) {
    	if (energyCarrier == OL_EnergyCarriers.HEAT || energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
	    	throw new RuntimeException("Invalid choice of energy carrier for J_EAFuelVehicle");
	    }
		this.setOwner(owner);
	    this.timeParameters = timeParameters;
	    this.energyConsumption_kWhpkm = energyConsumption_kWhpkm;
	    this.vehicleScaling = vehicleScaling;
	    this.vehicleType = vehicleType;
	    this.setEnergyAssetType(vehicleType, energyCarrier); // Temporary, till EA type is removed!
	    this.tripTracker = tripTracker;
	    this.available = available;
	    if (tripTracker != null) {
	    	tripTracker.setVehicle(this);
	    }
	    
	    this.assetFlowCategory = assetFlowCategory;
	    this.energyCarrierConsumed = energyCarrier;
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset(timeParameters);
	}
    
    @Override
    public J_FlowPacket f_updateAllFlows(J_TimeVariables timeVariables) {
    	flowsMap.put(this.energyCarrierConsumed, this.energyUse_kW);
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, this.energyUse_kW);
		}
    	J_FlowsMap flowsMapCopy = new J_FlowsMap();
     	J_ValueMap assetFlowsMapCopy = new J_ValueMap(OL_AssetFlowCategories.class);
     	J_FlowPacket flowPacket = new J_FlowPacket(flowsMapCopy.cloneMap(this.flowsMap), this.energyUse_kW, assetFlowsMapCopy.cloneMap(this.assetFlowsMap));
    	this.lastFlowsMap = flowsMap;
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	clear(); 
    	return flowPacket;
    }
    
    @Override
    public void operate(J_TimeVariables timeVariables) {
    	throw new RuntimeException("Operate was implemented because abstract J_EAFixed prescribed it, but should not be called.");
    }
    
	public boolean startTrip(J_TimeVariables timeVariables) {
		if (available) {
			this.available = false;
			//traceln("PetroleumFuel vehicle starting trip!");
			return true;
		} else {
			traceln("Trip not started because vehicle not available!");
			return false; // Trip not started because vehicle not available!
		}
	}	

    public boolean endTrip(double tripDist_km) {	
		if(available) {
			traceln("Trip not ended because vehicle never left!");
			return false;
		} else {
	    	this.available = true;
	    	return true;
		}
    }
    
    public void setAvailability(boolean available) {
    	this.available = available;
    }

	public boolean progressTrip(double marginalTripDist_km) {
		if( available) {
			traceln("Trip not updated because vehicle never left!");
			return false;
		}
		else {
	    	double energyUsedThisTimestep_kWh = marginalTripDist_km * vehicleScaling * energyConsumption_kWhpkm;
	    	energyUsed_kWh += energyUsedThisTimestep_kWh;
	    	energyUse_kW += energyUsedThisTimestep_kWh / timeParameters.getTimeStep_h();
			return true;
		}
	}
	
	public void setVehicleScaling(double vehicleScaling) {
    	this.vehicleScaling = vehicleScaling;
    }
	public void setTripTracker(J_ActivityTrackerTrips tracker) {
		this.tripTracker = tracker;
	}
	public J_ActivityTrackerTrips getTripTracker() {
		return this.tripTracker;
	}
	public boolean getAvailability() {
		return this.available;
	}
	public double getVehicleScaling_fr() {
		return this.vehicleScaling;
	}
	public double getEnergyConsumption_kWhpkm() {
		return this.energyConsumption_kWhpkm * this.vehicleScaling;
	}
	public OL_EnergyCarriers getEnergyCarrierConsumed() {
		return this.energyCarrierConsumed;
	}
	public OL_VehicleType getVehicleType() {
		return this.vehicleType;
	}
	public OL_EnergyCarriers getFuelType() {
		return this.energyCarrierConsumed;
	}
	
	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	availableStored = available;
    	available = true;
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
			"energy carrier = " + energyCarrierConsumed + " " +		
			"energyConsumption_kWhpkm =" + energyConsumption_kWhpkm + " " +
			"vehicleScaling = " + vehicleScaling;
	}
	
	//Temporary, till OL_EnergyAssetType is removed!!!!
	private void setEnergyAssetType(OL_VehicleType vehicleType, OL_EnergyCarriers energyCarrier) {
		switch(vehicleType) {
			case CAR:
				switch(energyCarrier) {
					case PETROLEUM_FUEL:
						this.energyAssetType = OL_EnergyAssetType.PETROLEUM_FUEL_VEHICLE;
						break;
					case HYDROGEN:
						this.energyAssetType = OL_EnergyAssetType.HYDROGEN_VEHICLE;
						break;	
				}
				break;
			case VAN:
				switch(energyCarrier) {
					case PETROLEUM_FUEL:
						this.energyAssetType = OL_EnergyAssetType.PETROLEUM_FUEL_VAN;
						break;
					case HYDROGEN:
						this.energyAssetType = OL_EnergyAssetType.HYDROGEN_VAN;
						break;	
				}
				break;
			case TRUCK:
				switch(energyCarrier) {
					case PETROLEUM_FUEL:
						this.energyAssetType = OL_EnergyAssetType.PETROLEUM_FUEL_TRUCK;
						break;
					case HYDROGEN:
						this.energyAssetType = OL_EnergyAssetType.HYDROGEN_TRUCK;
						break;	
				}
				break;
		}
	}
}