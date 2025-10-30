/**
 * J_EABuildingThermalNetwork
 */	
public class J_EABuildingThermalNetwork extends zero_engine.J_EAStorageHeat implements Serializable {

    /**
     * Default constructor
     */
    public J_EABuildingThermalNetwork() {
    }

    private double solarAbsorptionFactor_m2;
	private double solarRadiation_Wpm2 = 0;
	
	//Slider scaling factor
	private double lossScalingFactor_fr = 1;
	
	// Optional Interior/Exterior Heat buffers
	private double heatingSystemConductance_WpK; // Heat transfer between building and heating system
	private double heatingSystemHeatCapacity_JpK; // Thermal capacity of the heating system (floor heating, radiators, etc.)
	//private double exteriorDelayTime_h;

    /**
     * Constructor initializing the fields
     */
    public J_EABuildingThermalNetwork(Agent parentAgent, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double buildingThermalTimescale_h, double solarAbsorptionFactor_m2, double heatingSystemTimescale_h ) {
		this.parentAgent = parentAgent;
		//this.heatStorageType = OL_EAStorageTypes.HEATMODEL_BUILDING;
		this.capacityHeat_kW = capacityHeat_kW;
		this.lossFactor_WpK = lossFactor_WpK;
		this.timestep_h = timestep_h;
		this.initialTemperature_degC = initialTemperature_degC;
		this.temperature_degC = initialTemperature_degC;
		this.ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		//this.storageCapacity_kWh = ( maxTemperature_degC - minTemperature_degC ) * heatCapacity_JpK / 3.6e+6;
		this.solarAbsorptionFactor_m2 = solarAbsorptionFactor_m2;
		this.energyAssetType = OL_EnergyAssetType.BUILDINGTHERMALS;
	    if (lossFactor_WpK < 0) {
	    	throw new RuntimeException(String.format("Exception: J_EABuilding with negative lossfactor! %s", lossFactor_WpK));	    	
	    }
	    
	    // Calculate heatcapacity based on lossfactor and buildingThermalTimescale_h
	    this.heatCapacity_JpK = buildingThermalTimescale_h * 3600 * lossFactor_WpK ;
	    this.heatingSystemHeatCapacity_JpK = this.heatCapacity_JpK/4; // Hardcoded factor 4, but this value will be different based on heating system type!! Floor heating is much slower (higher heatcapacity) than radiators. At the same time, radiators have a lower conductance than floor heating.
	    
	    this.heatingSystemConductance_WpK = this.heatCapacity_JpK*this.heatingSystemHeatCapacity_JpK / (this.heatCapacity_JpK + this.heatingSystemHeatCapacity_JpK) / (heatingSystemTimescale_h * 3600)
	    //double heatingSystemDeliveryTime_h = 
	    
	    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.HEAT);		
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.HEAT);
		registerEnergyAsset();
    }

	@Override
	public void calculateLoss() {
		double heatLoss_kW = (this.lossFactor_WpK * ( this.temperature_degC - this.ambientTemperature_degC ) / 1000) * lossScalingFactor_fr;
		//traceln("ambientTemperature_degC in J_EABuilding: %s", this.ambientTemperature_degC);
		//traceln("heatLoss_kW: %s", heatLoss_kW);
		double deltaEnergy_kWh = -heatLoss_kW * this.timestep_h;
		this.energyUse_kW += heatLoss_kW;
		//traceln("Ambient temperature " + ambientTemperature_degC);
		//traceln("heatCapacity JpK " + heatCapacity_JpK );
		//traceln("tempdelta loss in thermal building asset (kW) " + energyUse_kW);
		//traceln("lossfacter: " + lossFactor_WpK);
		
		//traceln("deltaEnergy_kWh calculateLoss: %s", deltaEnergy_kWh);
		//traceln("temperature_degC %s", this.temperature_degC);
		updateStateOfCharge( deltaEnergy_kWh );
		//traceln("temperature_degC %s", this.temperature_degC);

	}

	public void solarHeating() {
		//traceln("solarAbsorptionFactor_m2: %s", solarAbsorptionFactor_m2);
		//traceln("solarRadiation_Wpm2: %s", solarRadiation_Wpm2);

		double solarHeating_kW = this.solarAbsorptionFactor_m2 * this.solarRadiation_Wpm2 / 1000;
		//traceln("solarHeating_kW: %s", solarHeating_kW);		
		this.energyUse_kW -= solarHeating_kW;
		//traceln("solarHeating_kW: %s", solarHeating_kW);
		//traceln("exteriorReleaseScheduleIndex: %s", exteriorReleaseScheduleIndex);
		//traceln("exteriorReleaseSchedule_kWh: %s", Arrays.toString(exteriorReleaseSchedule_kWh));
		double deltaEnergy_kWh;
		if( this.exteriorDelayTime_h != 0.0) {
			deltaEnergy_kWh = getExteriorHeatRelease( solarHeating_kW * this.timestep_h );
		}
		else {
			deltaEnergy_kWh = solarHeating_kW * this.timestep_h; // Is always positive	
		}
		//traceln("deltaEnergy_kWh: %s", deltaEnergy_kWh);
		//traceln("exteriorReleaseSchedule_kWh: %s", Arrays.toString(exteriorReleaseSchedule_kWh));

		//traceln("deltaEnergy_kWh solar heating: %s", deltaEnergy_kWh);
		//traceln("temperature_degC %s", this.temperature_degC);
		updateStateOfCharge( deltaEnergy_kWh );
		//traceln("temperature_degC %s", this.temperature_degC);

	}
	
	@Override
	public void f_updateAllFlows(double powerFraction_fr) {
		if (powerFraction_fr > 1) {			
			traceln("JEABuilding capacityHeat_kW is too low! "+ capacityHeat_kW);
		}
		super.f_updateAllFlows(powerFraction_fr);
	}

	@Override
	public void operate(double ratioOfChargeCapacity_r) {
		//traceln("Building heatCapacity_JpK: %s", this.heatCapacity_JpK);
		//traceln("StorageAsset Heat Operatefunctie: ambienttemperature = "+ambientTemperature_degC);
		//traceln("StorageAsset Heat Operatefunctie: ambienttemperature = "+ambientTemperature_degC+" | powerFraction_fr = " + ratioOfChargeCapacity_r + ".");
		//traceln("<><><><> heatstorage reset heatproduction = "+heatProduction_kW+", heatconsumption_kW = "+heatConsumption_kW+" heatProduced_kWh = "+heatProduced_kWh + "heatConsumed = "+heatConsumed_kWh + ", losses= "+energyUsed_kWh );
		if (ratioOfChargeCapacity_r < 0) {
			throw new RuntimeException("Cooling of the J_EABuilding is not yet supported.");
		}
		
			
		calculateLoss(); // Heat exchange with environment through convection
		solarHeating(); // Heat influx from sunlight

		this.energyUsed_kWh += max(0, this.energyUse_kW * this.timestep_h); // Only heat loss! Not heat gain when outside is hotter than inside!
		this.energyAbsorbed_kWh += max(0, -this.energyUse_kW * this.timestep_h); // Only heat gain when outside is hotter than inside!

		double inputPower_kW = ratioOfChargeCapacity_r * this.capacityHeat_kW; // positive power means lowering the buffer temperature!
		//traceln("inputPower_kW: %s", inputPower_kW);		
    	
		double deltaEnergy_kWh;
		if (this.interiorDelayTime_h != 0.0) {
			deltaEnergy_kWh = getInteriorHeatRelease( inputPower_kW * this.timestep_h );
    	}
		else { 
			deltaEnergy_kWh = inputPower_kW * this.timestep_h; // to check the request with the energy currently in storage
		}
		
		//traceln("deltaEnergy_kWh operate: %s", deltaEnergy_kWh);
		
    	
		double heatConsumption_kW = inputPower_kW;
		this.heatConsumed_kWh += heatConsumption_kW * this.timestep_h;
		//traceln("Heat consumption delivered by heating asset kW " + heatConsumption_kW);
		//traceln("Heatcapacity kWh: " + (heatCapacity_JpK / 3.6E6 ));
		//traceln("tempdelta charge: " + deltaTemp_degC);
		//traceln(">> Heat storage heatproduction = "+ heatProduced_kWh + ", heatconsumption_kW = "+ heatConsumption_kW +" heatConsumed_kWh = "+ heatConsumed_kWh +", heatProduced_kWh = "+ heatProduced_kWh );

		//traceln("temperature_degC %s", this.temperature_degC);
		updateStateOfCharge( deltaEnergy_kWh );
		//traceln("temperature_degC %s", this.temperature_degC);

		//traceln("<><><><> heatstorage <"+ownerAsset.getId()+"> calculated heatproduction = "+heatProduction_kW+", heatconsumption_kW = "+heatConsumption_kW+", heatProduced_kWh = "+heatProduced_kWh + ", heatConsumed = "+heatConsumed_kWh + ", losses= "+energyUsed_kWh );

		this.flowsMap.put(OL_EnergyCarriers.HEAT, inputPower_kW);
		/*if (Double.isNaN(this.energyUse_kW)) {
    		throw new RuntimeException("Building thermal model energyUse_kW is NaN!");
    	}*/
		
	}


	@Override
	public String toString() {
		return
			this.getClass().toString() + " " +
			"Energy consumed = " + this.energyUsed_kWh + "kWh, " + 
			"temp = " + this.temperature_degC + ", " +
			"parentAgent = " + parentAgent + ", " +
			"capacityHeat_kW = " + this.capacityHeat_kW + ", "+
			"ambientTemperature_degC = "+this.ambientTemperature_degC + ", " +
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + "kWh, " +
			"heatConsumed_kWh = "+ this.heatConsumed_kWh + "kWh";
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		double tempDelta_degC = deltaEnergy_kWh / (this.heatCapacity_JpK / 3.6E6 );
		//traceln("heatCapacity_JpK: %s", heatCapacity_JpK);
		this.temperature_degC += tempDelta_degC;
		//this.stateOfCharge_fr = ( this.temperature_degC - this.minTemperature_degC) / (this.maxTemperature_degC - this.minTemperature_degC);
		//traceln("SOC: " + stateOfCharge_fr);
		
		/*if (temperature_degC < setTemperature_degC) {
			requiresHeat = true;
		}
		else if ( temperature_degC >= maxTemperature_degC ) {
			requiresHeat = false;
		}*/
	}

	@Override
	public double getCurrentTemperature() {
		return this.temperature_degC;
	}
	
	@Override
	public double getLossFactor_WpK() {
		return this.lossFactor_WpK;
	}
	
	public void setLossFactor_WpK( double lossFactor_WpK) {
		this.lossFactor_WpK = lossFactor_WpK;
	}
	
	public void setLossScalingFactor_fr( double lossScalingFactor_fr) {
		this.lossScalingFactor_fr = lossScalingFactor_fr;
	}
	
	public double getLossScalingFactor_fr() {
		return this.lossScalingFactor_fr;
	}
	
	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
		this.energyUsedStored_kWh = this.energyUsed_kWh;
		this.energyAbsorbedStored_kWh = this.energyAbsorbed_kWh;
		this.energyUsed_kWh = 0.0;
		this.energyAbsorbed_kWh = 0.0;
		this.temperatureStored_degC = this.temperature_degC;
		this.temperature_degC = this.initialTemperature_degC;
	
		clear();
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
		this.energyUsed_kWh = this.energyUsedStored_kWh;
		this.energyAbsorbed_kWh = this.energyAbsorbedStored_kWh;
		this.temperature_degC = this.temperatureStored_degC;
	}

	@Override
	public void updateAmbientTemperature(double currentAmbientTemperature_degC) { // TODO: Hoe zorgen we dat we deze niet vergeten aan te roepen??
		this.ambientTemperature_degC = currentAmbientTemperature_degC;
	}

	public void updateSolarRadiation(double solarRadiation_Wpm2) { // TODO: Hoe zorgen we dat we deze niet vergeten aan te roepen??
		this.solarRadiation_Wpm2 = solarRadiation_Wpm2;
		//traceln("Updating solarRadiation of building to %s Wpm2!", solarRadiation_Wpm2);
	}
	
    public boolean hasHeatBuffer() {
    	if (this.exteriorDelayTime_h != 0 || this.interiorDelayTime_h != 0) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}

