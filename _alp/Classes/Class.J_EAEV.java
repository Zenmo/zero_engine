/**
* J_EAEV
*/
import com.fasterxml.jackson.annotation.JsonTypeName;

//@JsonTypeName("J_EAEV")
public class J_EAEV extends J_EAVehicle implements Serializable {
 
 
	public OL_EnergyCarriers storageMedium = OL_EnergyCarriers.ELECTRICITY;
	private double stateOfCharge_fr;
	private double initialstateOfCharge_fr;
	private double stateOfChargeStored_r;
	protected double capacityElectric_kW;
	private double storageCapacity_kWh;
 
	private boolean V2GCapable = true; // For now default true: Add to constructor, where constructor calls: setV2GCapable(boolean isV2GCapable) to adjust min rato of capacity accordingly
	private boolean V2GActive = false;
	private double minimumRatioOfChargeCapacity_r = -1; // If Negative, it also allowes discharging (V2G)
	
	// Should this be in here?	
	public double energyNeedForNextTrip_kWh;
	//public OL_EVChargingNeed chargingNeed;
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
    public J_EAEV(Agent parentAgent, double capacityElectricity_kW, double storageCapacity_kWh, double stateOfCharge_fr, double timestep_h, double energyConsumption_kWhpkm, double vehicleScaling, OL_EnergyAssetType energyAssetType, J_ActivityTrackerTrips tripTracker) {
		this.parentAgent = parentAgent;
		this.capacityElectric_kW = capacityElectricity_kW; // for EV, this is max charging power.
		this.storageCapacity_kWh = storageCapacity_kWh;
		this.initialstateOfCharge_fr = stateOfCharge_fr;
		this.stateOfCharge_fr = initialstateOfCharge_fr;
		this.timestep_h = timestep_h;
		this.energyConsumption_kWhpkm = energyConsumption_kWhpkm;
		this.vehicleScaling = vehicleScaling;
	    this.energyAssetType = energyAssetType; //OL_EnergyAssetType.ELECTRIC_VEHICLE; // AANPASSING ATE: VRAGEN AAN GILLIS: asset type meegeven in functie J_EAV, want scheelt switch statement in iEA functie.
	    this.tripTracker = tripTracker;
	    if (tripTracker != null) {
	    	tripTracker.Vehicle=this;	    	
	    }
	    // Validation checks
	    if (capacityElectric_kW <= 0 || storageCapacity_kWh <= 0 || timestep_h == 0 || energyConsumption_kWhpkm <= 0) {
	    	throw new RuntimeException(String.format("Exception: J_EAEV in invalid state! Energy Asset: %s, capacityElectric_kW: %s, storageCapacity_kWh: %s, timestep_h: %s, energyConsumption_kWhpkm %s", this, capacityElectric_kW, storageCapacity_kWh, timestep_h, energyConsumption_kWhpkm));
	    	
	    }
	    this.activeProductionEnergyCarriers.add(this.storageMedium);   	
		this.activeConsumptionEnergyCarriers.add(this.storageMedium);
		
		if(V2GCapable && this.V2GActive) {
			this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
		} else {
			this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
		}
		
		registerEnergyAsset();
    }
 
	@Override
	public void operate(double ratioOfChargeCapacity_r) {
    	double chargeSetpoint_kW = min(1,max(this.minimumRatioOfChargeCapacity_r ,ratioOfChargeCapacity_r)) * (capacityElectric_kW * vehicleScaling); // capped between -1 and 1. (does this already happen in f_updateAllFlows()?
    	double chargePower_kW = max(min(chargeSetpoint_kW, (1 - stateOfCharge_fr) * storageCapacity_kWh * vehicleScaling / timestep_h), -stateOfCharge_fr * storageCapacity_kWh * vehicleScaling / timestep_h); // Limit charge power to stay within SoC 0-100
    	
    	//traceln("state of charge: " + stateOfCharge_fr * storageCapacity_kWh + ", charged: " + discharge_kW / 4+ " kWh, charging power kW: " + discharge_kW);
		double electricityProduction_kW = max(-chargePower_kW, 0);
		double electricityConsumption_kW = max(chargePower_kW, 0);
		updateStateOfCharge( chargePower_kW );

		updateChargingHistory( electricityProduction_kW, electricityConsumption_kW );

		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW - electricityProduction_kW);
		// Split charging and discharing power 'at the source'!
		if (chargePower_kW > 0) { // charging
			assetFlowsMap.put(OL_AssetFlowCategories.evChargingPower_kW, electricityConsumption_kW);
		} else if(chargePower_kW < 0){
			if(this.V2GCapable && this.V2GActive) {
				assetFlowsMap.put(OL_AssetFlowCategories.V2GPower_kW, electricityProduction_kW);
			}
			else {
				throw new RuntimeException("Trying to discharge an EV, that does not have the capability or where v2g is not activated!");
			}
		}
	}
 
	public void updateStateOfCharge( double power_kW ) {
		if(vehicleScaling > 0){
			stateOfCharge_fr += ( power_kW * timestep_h ) / (storageCapacity_kWh * vehicleScaling);
		}
		else {
			stateOfCharge_fr = 0;
		}
	}
 
	@Override
	public boolean startTrip() {
		if (available) {
			this.available = false;
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
			return true;
		} else {
			mileage_km += tripDist_km;
			//traceln( "J_EAEV comes back, trip distance: " + tripDist_km + ", energy consumption: " + tripDist_km * energyConsumption_kWhpkm);
			//traceln("EV of type: " + this.energyAssetType + "state of charge: " + stateOfCharge_fr);
			stateOfCharge_fr -= (tripDist_km * vehicleScaling * energyConsumption_kWhpkm) / (storageCapacity_kWh * vehicleScaling);
			//traceln("storage capacity: " + storageCapacity_kWh + ", state of charge: " + stateOfCharge_fr);
			energyUsed_kWh += tripDist_km * vehicleScaling * energyConsumption_kWhpkm;
			energyUse_kW += tripDist_km * vehicleScaling * energyConsumption_kWhpkm / timestep_h;
			//traceln("EV energy use at end of trip: %s kWh", tripDist_km * vehicleScaling * energyConsumption_kWhpkm );
			if (stateOfCharge_fr < 0) {
				//traceln( ownerAsset.date());
				//traceln( "Trip distance: " + tripDist_km + ", vehicle scaling: " + vehicleScaling + ", energy cons_kWhpkm: " + energyConsumption_kWhpkm );
				traceln("EV of type: " + this.energyAssetType + " from GC " + this.parentAgent + " arrived home with negative SOC: " + roundToDecimal(100 * stateOfCharge_fr,2) + "%");
						
				//energyChargedOutsideModelArea_kWh += -stateOfCharge_fr * storageCapacity_kWh;
				//traceln("energyChargedOutsideModelArea_kWh: " + energyChargedOutsideModelArea_kWh);
				//stateOfCharge_fr = 0;
			}
			this.available = true;
			return true;
		}
	}
 
	public double getChargeDeadline_h() {
		double chargeNeedForNextTrip_kWh = max(0, this.getEnergyNeedForNextTrip_kWh() - this.getCurrentStateOfCharge_kWh());
		double chargeTimeMargin_h = 0.5; // Margin to be ready with charging before start of next trip
		double nextTripStartTime_h = getNextTripStartTime_h();
		double chargeDeadline_h = nextTripStartTime_h - chargeNeedForNextTrip_kWh / this.capacityElectric_kW - chargeTimeMargin_h;
		//double chargeDeadline_h = floor((this.tripTracker.v_nextEventStartTime_min / 60 - chargeNeedForNextTrip_kWh / this.getCapacityElectric_kW() / timestep_h) * timestep_h;
		return chargeDeadline_h;
	}
	
	public double getNextTripStartTime_h() {
		return this.tripTracker.v_nextEventStartTime_min / 60;
	}
	
	public void updateChargingHistory(double electricityProduced_kW, double electricityConsumed_kW) {
		discharged_kWh += electricityProduced_kW * timestep_h;
		charged_kWh += electricityConsumed_kW * timestep_h;
	}
 
	public double getEnergyUsed_kWh() {
		return this.energyUsed_kWh;
	}
 
	public double getCurrentStateOfCharge_fr() {
    	return this.stateOfCharge_fr;
	}
 
	public double getStorageCapacity_kWh() {
		return this.storageCapacity_kWh * this.vehicleScaling;
	}

	public double getCurrentStateOfCharge_kWh() {
		return this.stateOfCharge_fr * this.getStorageCapacity_kWh();
	}
		
	public double getCapacityElectric_kW() {
		return this.capacityElectric_kW * this.vehicleScaling;
	}
	public double getTotalChargeAmount_kWh() {
		return this.charged_kWh;
	}
	public double getTotalDischargeAmount_kWh() {
		return this.discharged_kWh;
	}
	
	public double getEnergyNeedForNextTrip_kWh() {
		return this.energyNeedForNextTrip_kWh * this.vehicleScaling;
	}
 
	public boolean getAvailability() {
		return this.available;
	}
 
	public double getChargingTimeToFull_MIN() {
		double chargingTime_min = ceil( 60 * ((storageCapacity_kWh * vehicleScaling) - (storageCapacity_kWh * vehicleScaling) * stateOfCharge_fr) / (capacityElectric_kW * vehicleScaling) ) ;
		return chargingTime_min;
	}
 
	public double getEnergyChargedOutsideModelArea_kWh() {
		return energyChargedOutsideModelArea_kWh;
	}
	
	public void setV2GCapable(boolean isV2GCapable) {
		this.V2GCapable = isV2GCapable;
		
		setV2GActive(getV2GActive());
		
		if(isV2GCapable) {
			minimumRatioOfChargeCapacity_r = -1;
		}
		else {
			minimumRatioOfChargeCapacity_r = 0;
		}
	}
	
	public boolean getV2GCapable() {
		return this.V2GCapable;
	}
	
	public boolean getV2GActive() {
		return this.V2GActive;
	}
	
	protected void setV2GActive(boolean activateV2G) { // Should only be called by the chargingManagement class or J_EAEV during initialization itself. (No such thing as friend class in java, so only can put on protected).
		this.V2GActive = activateV2G;
		if(this.V2GCapable && activateV2G) {
			this.assetFlowCategory = OL_AssetFlowCategories.V2GPower_kW;
		}
		else {
			this.assetFlowCategory = OL_AssetFlowCategories.evChargingPower_kW;
		}
	}
	
	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
		
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	stateOfChargeStored_r = stateOfCharge_fr;
    	stateOfCharge_fr = initialstateOfCharge_fr;
    	availableStored = available;
    	available = true;
    	energyChargedOutsideModelAreaStored_kWh = energyChargedOutsideModelArea_kWh;
    	energyChargedOutsideModelArea_kWh = 0;
    	mileage_km = 0;
    	charged_kWh = 0;
    	discharged_kWh = 0;
    	//traceln("J_EAEV battery content at start of simulation: %s kWh", this.getCurrentStateOfCharge_kWh() );
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;    	
    	stateOfCharge_fr = stateOfChargeStored_r;
    	available = availableStored;
    	energyChargedOutsideModelArea_kWh = energyChargedOutsideModelAreaStored_kWh;
    	charged_kWh = 0;
    	discharged_kWh = 0;
    }
	
	@Override
	public String toString() {
		return
			"SOC = " + roundToDecimal( stateOfCharge_fr, 2 ) + " " +
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
 
