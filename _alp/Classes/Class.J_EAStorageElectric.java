/**
 * J_EAStorageElectric
 */
public class J_EAStorageElectric extends J_EAStorage implements Serializable {
	
	private OL_EnergyCarriers storageMedium = OL_EnergyCarriers.ELECTRICITY;
	
	private double etaCharge_r; // charging efficiency
	private double etaDischarge_r;
	protected double capacityElectric_kW;
	
    /**
     * Default constructor
     */
    public J_EAStorageElectric() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAStorageElectric(I_AssetOwner owner, double capacityElectric_kW, double storageCapacity_kWh, double stateOfCharge_fr, double timestep_h ) {
		this.setOwner(owner);
		this.capacityElectric_kW = capacityElectric_kW;
		this.storageCapacity_kWh = storageCapacity_kWh;
		this.initialstateOfCharge_fr = stateOfCharge_fr;
		this.stateOfCharge_fr = this.initialstateOfCharge_fr;
		this.timestep_h = timestep_h;
		this.energyAssetType = OL_EnergyAssetType.STORAGE_ELECTRIC;
		double eta_r=0.9; // Default cycle efficiency of 90%. Add this as an argument to constructor?
		this.etaCharge_r = Math.sqrt(eta_r);
		this.etaDischarge_r = Math.sqrt(eta_r);
	    this.activeProductionEnergyCarriers.add(this.storageMedium);		
		this.activeConsumptionEnergyCarriers.add(this.storageMedium);
		this.assetFlowCategory = OL_AssetFlowCategories.batteriesChargingPower_kW;
		registerEnergyAsset();
    }
    
	@Override
	public void operate(double ratioOfChargeCapacity_r, J_TimeVariables timeVariables) {
    	double inputPower_kW = ratioOfChargeCapacity_r * capacityElectric_kW; // Electric power going into battery, before losses.
    	double deltaEnergy_kWh;   // The change in energy stored in the battery this timestep ('internal' energy)
    	
    	// charging/discharging losses
    	if (inputPower_kW > 0) { // charging (the battery 'consumes' electricity)
    		deltaEnergy_kWh = etaCharge_r * inputPower_kW * timestep_h; // Actual change of energy content of battery after losses. deltaEnergy_kWh is smaller than inputPower_kW * timestep_h!
    	} else { // discharging (the battery 'produces' electricity)
    		deltaEnergy_kWh = inputPower_kW / etaDischarge_r * timestep_h; // Actual change of energy content of battery after losses. deltaEnergy_kWh is larger than inputPower_kW * timestep_h!
    	}

    	// Limit SoC to feasible range (0-1)
    	deltaEnergy_kWh = - min( -deltaEnergy_kWh, (stateOfCharge_fr * storageCapacity_kWh) ); // Prevent negative charge
    	deltaEnergy_kWh = min(deltaEnergy_kWh, (1 - stateOfCharge_fr) * storageCapacity_kWh ); // Prevent overcharge
    	
    	double electricityConsumption_kW = 0;
    	double electricityProduction_kW = 0;
    	if (deltaEnergy_kWh > 0) { // charging, deltaEnergy_kWh and inputPower_kW positive
    		inputPower_kW = deltaEnergy_kWh / timestep_h / etaCharge_r;
    		electricityConsumption_kW = inputPower_kW;
    		electricityProduction_kW = 0;
    		energyUse_kW = (1-etaCharge_r)*inputPower_kW;    		
    	} else { // discharging, deltaEnergy_kWh and inputPower_kW negative
    		inputPower_kW = deltaEnergy_kWh / timestep_h * etaDischarge_r;
    		electricityConsumption_kW = 0;
    		electricityProduction_kW = -inputPower_kW; 
    		energyUse_kW = -deltaEnergy_kWh / timestep_h * (1-etaDischarge_r);
    	}
    	energyUsed_kWh += energyUse_kW * timestep_h;
    	
		discharged_kWh += electricityProduction_kW * timestep_h; // Not the change-in-SoC, but the energy flowing out of the battery after losses.
		charged_kWh += electricityConsumption_kW * timestep_h; // Not the change-in-SoC, but the energy flowing into the battery before losses.
		
		updateStateOfCharge( deltaEnergy_kWh );
		//traceln("Battery SoC: %s", stateOfCharge_fr);
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW-electricityProduction_kW);		
		//assetFlowsMap.put(this.assetFlowCategory, electricityConsumption_kW-electricityProduction_kW);		
		
		// Split charging and discharing power 'at the source'!
		assetFlowsMap.put(OL_AssetFlowCategories.batteriesChargingPower_kW, electricityConsumption_kW);
		assetFlowsMap.put(OL_AssetFlowCategories.batteriesDischargingPower_kW, electricityProduction_kW);
	}
	
	public void setBatteryEfficiency_r(double eta_r) {
		if (eta_r < 0 || eta_r > 1) {
			traceln("Infeasible eta_r! Should be larger than 0 and no larger than 1! Setting eta_r=1. Input value is: %s", eta_r);
			this.etaCharge_r = 1;			
			this.etaDischarge_r = 1;
		} else {
			this.etaCharge_r = Math.sqrt(eta_r);			
			this.etaDischarge_r = Math.sqrt(eta_r);
		}		
	}

	@Override
	public String toString() {
		return 
			"type = " + this.getClass().toString() + " " +
			"stateOfCharge_fr = " + this.stateOfCharge_fr+" "+
			"storageCapacity_kWh = " + this.storageCapacity_kWh +" "+
			"capacityElectric_kW = " + this.capacityElectric_kW +" "+
			"discharged_kWh " + this.discharged_kWh+" "+
			"charged_kWh " + this.charged_kWh+" ";
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		stateOfCharge_fr += deltaEnergy_kWh / storageCapacity_kWh;
	}

	public double getCapacityAvailable_kW() {
		double availableCapacity_kW;
		if ( stateOfCharge_fr * storageCapacity_kWh  > capacityElectric_kW * timestep_h) {
			availableCapacity_kW = capacityElectric_kW;
		}
		else {
			availableCapacity_kW =  stateOfCharge_fr * storageCapacity_kWh / timestep_h; // Allow to drain completely
		}
		return availableCapacity_kW;
	}
	
	public double getMaxChargePower_kW() { // Always a positive number!
		return min(capacityElectric_kW, (1-stateOfCharge_fr) * storageCapacity_kWh / timestep_h / etaCharge_r);
	}
	
	public double getMaxDischargePower_kW() { // Always a positive number!
		return min(capacityElectric_kW, stateOfCharge_fr * storageCapacity_kWh / timestep_h * etaDischarge_r);
	}
	
	public double getCapacityElectric_kW() {
		return this.capacityElectric_kW;
	}

	public double getTotalChargeAmount_kWh() {
		return this.charged_kWh;
	}
	public double getTotalDischargeAmount_kWh() {
		return this.discharged_kWh;
	}
	
	public double getChargingEfficiency_r() {
		return this.etaCharge_r;
	}
	
	public double getDischargingEfficiency_r() {
		return this.etaDischarge_r;
	}
	
	public void setStorageCapacity_kWh(double storageCapacity_kWh) {
		double difference_kWh = storageCapacity_kWh - this.storageCapacity_kWh;
		this.storageCapacity_kWh = storageCapacity_kWh;
		if (this.parentAgent instanceof GridConnection) {		
			((GridConnection)this.parentAgent).v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += difference_kWh/1000;
			((GridConnection) this.parentAgent).c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += difference_kWh/1000);
			((GridConnection)this.parentAgent).energyModel.v_liveAssetsMetaData.totalInstalledBatteryStorageCapacity_MWh += difference_kWh/1000;
		}
		// TODO: Fix for new FLOWSMAP
		//if (storageCapacity_kWh == 0) {			
			//Arrays.fill(lastFlowsArray,0);
		//}
	}

	public void setCapacityElectric_kW(double capacityElectric_kW) {
		this.capacityElectric_kW = capacityElectric_kW;
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}