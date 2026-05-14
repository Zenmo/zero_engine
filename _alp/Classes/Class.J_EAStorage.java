/**
 * J_EAStorage
 */
public class J_EAStorage extends J_EAFlex {

	protected OL_EnergyCarriers storageMedium;
	protected double storageCapacity_kWh;
	protected double chargeCapacity_kW;
	protected double etaCharge_r;
	protected double etaDischarge_r;
	protected double stateOfCharge_fr;
	protected double initialStateOfCharge_fr;
	protected double stateOfChargeStored_r;
	protected double discharged_kWh = 0;
	protected double charged_kWh = 0;

    /**
     * Default constructor for serialization
     */
    public J_EAStorage() {
    }
    
    /**
     * Default constructor
     */
    public J_EAStorage(I_AssetOwner owner,  OL_EnergyAssetType energyAssetType, OL_EnergyCarriers storageMedium, double chargeCapacity_kW, double storageCapacity_kWh, double roundTripEfficiency_fr, double initialStateOfCharge_fr, J_TimeParameters timeParameters ) {
    	this.setOwner(owner);
		this.energyAssetType = energyAssetType;
		this.storageMedium = storageMedium;
		this.chargeCapacity_kW = chargeCapacity_kW;
		this.storageCapacity_kWh = storageCapacity_kWh;
		this.setRoundTripEfficiency_r(roundTripEfficiency_fr);
		this.initialStateOfCharge_fr = initialStateOfCharge_fr;
		this.stateOfCharge_fr = initialStateOfCharge_fr;
		this.timeParameters = timeParameters;
		
	    this.activeProductionEnergyCarriers.add(storageMedium);		
		this.activeConsumptionEnergyCarriers.add(storageMedium);
		registerEnergyAsset(timeParameters);
    }

	@Override
	public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
    	double inputPower_kW = powerFraction_fr * chargeCapacity_kW; // Positive power going into Storage, Negative if going out.
    	
    	double deltaEnergy_kWh; //Change in contents of storage
    	
    	// charging/discharging losses
    	if (inputPower_kW > 0) { // charging (the storage 'consumes' energy)
    		deltaEnergy_kWh = etaCharge_r * inputPower_kW * timeParameters.getTimeStep_h(); // Actual change of energy content of battery after losses. deltaEnergy_kWh is smaller than inputPower_kW * timestep_h!
    	} else { // discharging (the storage 'produces' energy)
    		deltaEnergy_kWh = inputPower_kW / etaDischarge_r * timeParameters.getTimeStep_h(); // Actual change of energy content of battery after losses. deltaEnergy_kWh is larger than inputPower_kW * timestep_h!
    	}
    	
    	// Limit SoC to feasible range (0-1)
    	deltaEnergy_kWh = - min( -deltaEnergy_kWh, (stateOfCharge_fr * storageCapacity_kWh) ); // Prevent negative charge
    	deltaEnergy_kWh = min(deltaEnergy_kWh, (1 - stateOfCharge_fr) * storageCapacity_kWh ); // Prevent overcharge
    	
    	//Set the final remaining flows
    	double storageInput_kW = 0;
    	double storageOutput_kW = 0;
    	if (deltaEnergy_kWh > 0) { // charging, deltaEnergy_kWh and inputPower_kW positive
    		inputPower_kW = deltaEnergy_kWh / timeParameters.getTimeStep_h() / etaCharge_r;
    		storageInput_kW = inputPower_kW;
    		storageOutput_kW = 0;
    		energyUse_kW = (1-etaCharge_r)*inputPower_kW;    		
    	} else { // discharging, deltaEnergy_kWh and inputPower_kW negative
    		inputPower_kW = deltaEnergy_kWh / timeParameters.getTimeStep_h() * etaDischarge_r;
    		storageInput_kW = 0;
    		storageOutput_kW = -inputPower_kW; 
    		energyUse_kW = -deltaEnergy_kWh / timeParameters.getTimeStep_h() * (1-etaDischarge_r);
    	}
    	energyUsed_kWh += energyUse_kW * timeParameters.getTimeStep_h();
    	
		discharged_kWh += storageOutput_kW * timeParameters.getTimeStep_h(); // Not the change-in-SoC, but the energy flowing out of the battery after losses.
		charged_kWh += storageInput_kW * timeParameters.getTimeStep_h(); // Not the change-in-SoC, but the energy flowing into the battery before losses.
		
		updateStateOfCharge( deltaEnergy_kWh );
		flowsMap.put(storageMedium, storageInput_kW-storageOutput_kW);
	}
	
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		stateOfCharge_fr += deltaEnergy_kWh / storageCapacity_kWh;
	}
	
	//Setters
	public void setRoundTripEfficiency_r(double roundTripEfficiency_r) {
		if (roundTripEfficiency_r < 0 || roundTripEfficiency_r > 1) {
			traceln("Infeasible eta_r! Should be larger than 0 and no larger than 1! Setting eta_r=1. Input value is: %s", roundTripEfficiency_r);
			this.etaCharge_r = 1;			
			this.etaDischarge_r = 1;
		} else {
			this.etaCharge_r = Math.sqrt(roundTripEfficiency_r);			
			this.etaDischarge_r = Math.sqrt(roundTripEfficiency_r);
		}		
	}
	
	public void setStorageCapacity_kWh(double storageCapacity_kWh) {
		this.storageCapacity_kWh = storageCapacity_kWh;
	}
	public void setChargeCapacity_kW(double chargeCapacity_kW) {
		this.chargeCapacity_kW = chargeCapacity_kW;
	}
	
	//Getters
	public double getCurrentStateOfCharge_fr() {
    	return this.stateOfCharge_fr;
	}

	public double getStorageCapacity_kWh() {
		return this.storageCapacity_kWh;
	}
	
	public double getChargeCapacity_kW() {
		return this.chargeCapacity_kW;
	}

	public double getCurrentStateOfCharge_kWh() {
		return this.stateOfCharge_fr * this.storageCapacity_kWh;
	}
	
	public double getChargingEfficiency_r() {
		return this.etaCharge_r;
	}
	
	public double getDischargingEfficiency_r() {
		return this.etaDischarge_r;
	}
	
    @Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	stateOfChargeStored_r = stateOfCharge_fr;
    	stateOfCharge_fr = initialStateOfCharge_fr;    
    	charged_kWh = 0;
    	discharged_kWh = 0;
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;    	
    	stateOfCharge_fr = stateOfChargeStored_r;
    }
	
	@Override
	public String toString() {
		return 
			"type = " + this.getClass().toString() + "\n" +
			"stateOfCharge_fr = " + this.stateOfCharge_fr + "\n" +
			"storageCapacity_kWh = " + this.storageCapacity_kWh + "\n" +
			"chargeCapacity_kW = " + this.chargeCapacity_kW + "\n" +
			"discharged_kWh " + this.discharged_kWh + "\n"+
			"charged_kWh " + this.charged_kWh + "\n";
	}
}