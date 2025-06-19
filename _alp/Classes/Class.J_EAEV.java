/**
* J_EAEV
*/
public class J_EAEV extends J_EAVehicle implements Serializable {
 
 
	public OL_EnergyCarriers storageMedium = OL_EnergyCarriers.ELECTRICITY;
	private double stateOfCharge_r;
	private double initialStateOfCharge_r;
	private double stateOfChargeStored_r;
	protected double capacityElectric_kW;
	private double storageCapacity_kWh;
 
	// Should this be in here?	
	public double energyNeedForNextTrip_kWh;
	public OL_EVChargingNeed chargingNeed;
	private double energyChargedOutsideModelArea_kWh = 0;
	private double energyChargedOutsideModelAreaStored_kWh;
	public double charged_kWh = 0;
	public double discharged_kWh = 0;
    /**
     * Default constructor
     */
    public J_EAEV() {
    }
 
    /**
     * Constructor initializing the fields
     */
    public J_EAEV(Agent parentAgent, double capacityElectricity_kW, double storageCapacity_kWh, double stateOfCharge_r, double timestep_h, double energyConsumption_kWhpkm, double vehicleScaling, OL_EnergyAssetType energyAssetType, J_ActivityTrackerTrips tripTracker) {
		this.parentAgent = parentAgent;
		this.capacityElectric_kW = capacityElectricity_kW; // for EV, this is max charging power.
		this.storageCapacity_kWh = storageCapacity_kWh;
		this.initialStateOfCharge_r = stateOfCharge_r;
		this.stateOfCharge_r = initialStateOfCharge_r;
		this.timestep_h = timestep_h;
		this.energyConsumption_kWhpkm = energyConsumption_kWhpkm;
		this.vehicleScaling = vehicleScaling;
	    this.energyAssetType = energyAssetType; //OL_EnergyAssetType.ELECTRIC_VEHICLE; // AANPASSING ATE: VRAGEN AAN GILLIS: asset type meegeven in functie J_EAV, want scheelt switch statement in iEA functie.
	    this.tripTracker = tripTracker;
	    if (tripTracker != null) {
	    	tripTracker.Vehicle=this;	    	
	    }
	    // Validation checks
	    if (capacityElectric_kW == 0 || storageCapacity_kWh == 0 || timestep_h == 0 || energyConsumption_kWhpkm == 0) {
	    	throw new RuntimeException(String.format("Exception: J_EAEV in invalid state! Energy Asset: %s, capacityElectric_kW: %s, storageCapacity_kWh: %s, timestep_h: %s, energyConsumption_kWhpkm %s", this, capacityElectric_kW, storageCapacity_kWh, timestep_h, energyConsumption_kWhpkm));
	    	
	    }
	    this.activeProductionEnergyCarriers.add(this.storageMedium);   	
		this.activeConsumptionEnergyCarriers.add(this.storageMedium);
		registerEnergyAsset();
    }
 
	@Override
	public void operate(double ratioOfChargeCapacity_r) {
		//traceln( "ratio: " + ratioOfChargeCapacity_r);

    	double discharge_kW = 0;
    	double deltaEnergy_kWh;   // to check the request with the energy currently in storage
    	
    	deltaEnergy_kWh = ( ratioOfChargeCapacity_r * (capacityElectric_kW * vehicleScaling) * timestep_h ) ;
    	deltaEnergy_kWh = - min( -deltaEnergy_kWh, (stateOfCharge_r * (storageCapacity_kWh * vehicleScaling)) ); // Prevent negative charge
    	deltaEnergy_kWh = min(deltaEnergy_kWh, ratioOfChargeCapacity_r * (capacityElectric_kW * vehicleScaling) * timestep_h ); // prevent charging faster than allowed
    	deltaEnergy_kWh = min(deltaEnergy_kWh, (1 - stateOfCharge_r) * (storageCapacity_kWh * vehicleScaling) ); // Prevent overcharge
    
		discharge_kW = -deltaEnergy_kWh / timestep_h;
		//traceln("state of charge: " + stateOfCharge_r * storageCapacity_kWh + ", charged: " + discharge_kW / 4+ " kWh, charging power kW: " + discharge_kW);
		double electricityProduction_kW = max(discharge_kW, 0);
		double electricityConsumption_kW = max(-discharge_kW, 0);
		updateStateOfCharge( discharge_kW );
		//traceln("new state of charge: " + stateOfCharge_r * storageCapacity_kWh);
		updateChargingHistory( electricityProduction_kW, electricityConsumption_kW );

		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW - electricityProduction_kW);				
		//return new Pair(this.flowsMap, this.energyUse_kW);
	}
 
	public void updateStateOfCharge( double power_kW ) {
		if(vehicleScaling > 0){
			stateOfCharge_r -= ( power_kW * timestep_h ) / (storageCapacity_kWh * vehicleScaling);
		}
		else {
			stateOfCharge_r = 0;
		}
	}
 
	@Override
	public double getCurrentStateOfCharge() {
    	return stateOfCharge_r;
	}
 
	@Override
	public boolean startTrip() {
		if (available) {
			this.available = false;
			//traceln("storage capacity start of trip: " + storageCapacity_kWh + ", state of charge: " + stateOfCharge_r);
			((GridConnection)this.parentAgent).c_vehiclesAvailableForCharging.remove(this);
			
			//Update (charging) flows to zero, becausde vehicle is away.
			this.f_updateAllFlows(0.0);
			return true;
		} else {
			traceln("Trip not started because EV not available!");
			return false; // Trip not started because EV not available!
		}
	}
 
	@Override
	public boolean endTrip(double tripDist_km) {
	
		if(available) {
			traceln("Trip not ended because EV never left!");
			return false;
		}else if (this.vehicleScaling == 0) {
			this.available = true;
			((GridConnection)this.parentAgent).c_vehiclesAvailableForCharging.add(this);
			return true;
		} else {
			mileage_km += tripDist_km;
			//traceln( "J_EAEV comes back, trip distance: " + tripDist_km + ", energy consumption: " + tripDist_km * energyConsumption_kWhpkm);
			//traceln("EV of type: " + this.energyAssetType + "state of charge: " + stateOfCharge_r);
			stateOfCharge_r -= (tripDist_km * vehicleScaling * energyConsumption_kWhpkm) / (storageCapacity_kWh * vehicleScaling);
			//traceln("storage capacity: " + storageCapacity_kWh + ", state of charge: " + stateOfCharge_r);
			energyUsed_kWh += tripDist_km * vehicleScaling * energyConsumption_kWhpkm;
			energyUse_kW += tripDist_km * vehicleScaling * energyConsumption_kWhpkm / timestep_h;
			//traceln("EV energy use at end of trip: %s kWh", tripDist_km * vehicleScaling * energyConsumption_kWhpkm );
			if (stateOfCharge_r < 0) {
				//traceln( ownerAsset.date());
				//traceln( "Trip distance: " + tripDist_km + ", vehicle scaling: " + vehicleScaling + ", energy cons_kWhpkm: " + energyConsumption_kWhpkm );
				//traceln("EV of type: " + this.energyAssetType + " from GC " + this.parentAgent + " arrived home with negative SOC: " + stateOfCharge_r );
						
				//energyChargedOutsideModelArea_kWh += -stateOfCharge_r * storageCapacity_kWh;
				//traceln("energyChargedOutsideModelArea_kWh: " + energyChargedOutsideModelArea_kWh);
				//stateOfCharge_r = 0;
			}
			this.available = true;
			((GridConnection)this.parentAgent).c_vehiclesAvailableForCharging.add(this);
			//maxSpreadChargingRatio = (1-stateOfCharge_r) * storageCapacity_kWh / (timeToNextTrip_min/60);
			return true;
		}
	}
 
	public void updateChargingHistory(double electricityProduced_kW, double electricityConsumed_kW) {
		discharged_kWh += electricityProduced_kW * timestep_h;
		charged_kWh += electricityConsumed_kW * timestep_h;
	}
 
	public double getEnergyUsed_kWh() {
		return energyUsed_kWh;
	}
 
	public double getStorageCapacity_kWh() {
		return storageCapacity_kWh * vehicleScaling;
	}
	
	public double getCapacityElectric_kW() {
		return capacityElectric_kW * vehicleScaling;
	}
	public double getTotalChargeAmount_kWh() {
		return charged_kWh;
	}
	public double getTotalDischargeAmount_kWh() {
		return discharged_kWh;
	}
	
	public double getEnergyNeedForNextTrip_kWh() {
		return energyNeedForNextTrip_kWh * vehicleScaling;
	}
 
	public boolean getAvailability() {
		return available;
	}
 
	public double getChargingTimeToFull_MIN() {
		double chargingTime_min = ceil( 60 * ((storageCapacity_kWh * vehicleScaling) - (storageCapacity_kWh * vehicleScaling) * stateOfCharge_r) / (capacityElectric_kW * vehicleScaling) ) ;
		return chargingTime_min;
	}
 
	public double getEnergyChargedOutsideModelArea_kWh() {
		return energyChargedOutsideModelArea_kWh;
	}
	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	stateOfChargeStored_r = stateOfCharge_r;
    	stateOfCharge_r = initialStateOfCharge_r;
    	availableStored = available;
    	available = true;
    	energyChargedOutsideModelAreaStored_kWh = energyChargedOutsideModelArea_kWh;
    	energyChargedOutsideModelArea_kWh = 0;
    	mileage_km = 0;
    	charged_kWh = 0;
    	discharged_kWh = 0;
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;    	
    	stateOfCharge_r = stateOfChargeStored_r;
    	available = availableStored;
    	energyChargedOutsideModelArea_kWh = energyChargedOutsideModelAreaStored_kWh;
    	charged_kWh = 0;
    	discharged_kWh = 0;
    }
	
	@Override
	public String toString() {
		return
			"SOC = " + roundToDecimal( stateOfCharge_r, 2 ) + " " +
			"storageCapacity_kWh = " + storageCapacity_kWh + " " +
			"charged_kWh = " + roundToDecimal( charged_kWh, 2 ) + " " +
			"mileage = " + roundToDecimal( mileage_km, 2 ) + " ";
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
 
