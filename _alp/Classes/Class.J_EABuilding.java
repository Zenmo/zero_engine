/**
 * J_EABuilding
 */
public class J_EABuilding extends zero_engine.J_EAStorageHeat implements Serializable {

	public OL_EAStorageTypes heatStorageType;

	private double stateOfCharge_r;

	private double temperature_degC;
	private double temperatureStored_degC;
	private double initialTemperature_degC;
	private double minTemperature_degC;
	private double maxTemperature_degC;

	private double solarAbsorptionFactor_m2;
	private double solarRadiation_Wpm2 = 0;
	
	// Optional Interior/Exterior Heat buffers
	private double interiorDelayTime_h;
	private double[] interiorReleaseSchedule_kWh;
	private double[] interiorReleaseScheduleStored_kWh;
	private int interiorReleaseScheduleIndex;
	private double exteriorDelayTime_h;
	private double[] exteriorReleaseSchedule_kWh;
	private double[] exteriorReleaseScheduleStored_kWh;
	private int exteriorReleaseScheduleIndex;	

    /**
     * Default constructor
     */
    public J_EABuilding() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EABuilding(Agent parentAgent, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double heatCapacity_JpK, double solarAbsorptionFactor_m2 ) {
		this.parentAgent = parentAgent;
		this.heatStorageType = OL_EAStorageTypes.HEATMODEL_BUILDING;
		this.capacityHeat_kW = capacityHeat_kW;
		this.lossFactor_WpK = lossFactor_WpK;
		this.timestep_h = timestep_h;
		this.initialTemperature_degC = initialTemperature_degC;
		this.temperature_degC = initialTemperature_degC;
		this.heatCapacity_JpK = heatCapacity_JpK;
		this.ambientTempType = "AIR";
		//this.storageCapacity_kWh = ( maxTemperature_degC - minTemperature_degC ) * heatCapacity_JpK / 3.6e+6;
		this.solarAbsorptionFactor_m2 = solarAbsorptionFactor_m2;
		this.energyAssetType = OL_EnergyAssetType.BUILDINGTHERMALS;
	    if (lossFactor_WpK < 0) {
	    	throw new RuntimeException(String.format("Exception: J_EABuilding with negative lossfactor! %s", lossFactor_WpK));	    	
	    }
	    
	    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.HEAT);		
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.HEAT);
		registerEnergyAsset();
    }

	@Override
	public void calculateLoss() {
		double heatLoss_kW = this.lossFactor_WpK * ( this.temperature_degC - this.ambientTemperature_degC ) / 1000;
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
		//this.stateOfCharge_r = ( this.temperature_degC - this.minTemperature_degC) / (this.maxTemperature_degC - this.minTemperature_degC);
		//traceln("SOC: " + stateOfCharge_r);
		
		/*if (temperature_degC < setTemperature_degC) {
			requiresHeat = true;
		}
		else if ( temperature_degC >= maxTemperature_degC ) {
			requiresHeat = false;
		}*/
	}

	@Override
	public double getCurrentStateOfCharge() {
    	return this.stateOfCharge_r;
	}

	@Override
	public double getCurrentTemperature() {
		return this.temperature_degC;
	}
	
	@Override
	public double getLossFactor_WpK() {
		return this.lossFactor_WpK;
	}
	
	public double getInitialTemperature_degC() {
		return this.initialTemperature_degC;
	}
	
	/*@Override
	public double getHeatCapacity_JpK() {
		return heatCapacity_JpK;
	}*/
	
	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
		this.energyUsedStored_kWh = this.energyUsed_kWh;
		this.energyAbsorbedStored_kWh = this.energyAbsorbed_kWh;
		this.energyUsed_kWh = 0.0;
		this.energyAbsorbed_kWh = 0.0;
		this.temperatureStored_degC = this.temperature_degC;
		this.temperature_degC = this.initialTemperature_degC;
		if (this.interiorReleaseSchedule_kWh != null) {
			this.interiorReleaseScheduleStored_kWh = this.interiorReleaseSchedule_kWh;
			Arrays.fill(this.interiorReleaseSchedule_kWh, 0.0);
		}
		if (this.exteriorReleaseSchedule_kWh != null) {
			this.exteriorReleaseScheduleStored_kWh = this.exteriorReleaseSchedule_kWh;
			Arrays.fill(this.exteriorReleaseSchedule_kWh, 0.0);
		}
		clear();
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
		this.energyUsed_kWh = this.energyUsedStored_kWh;
		this.energyAbsorbed_kWh = this.energyAbsorbedStored_kWh;
		this.temperature_degC = this.temperatureStored_degC;
		this.interiorReleaseSchedule_kWh = this.interiorReleaseScheduleStored_kWh;
		this.exteriorReleaseSchedule_kWh = this.exteriorReleaseScheduleStored_kWh;		
    }

	/*@Override
	public double getMinTemperature_degC() {
		return minTemperature_degC;
	}*/

	/*@Override
	public double getMaxTemperature_degC() {
		return maxTemperature_degC;
	}*/

	/*public double getStorageCapacity() {
		return storageCapacity_kWh;
	}*/

	/*public double getHeatCapacity_JpK() {
		return heatCapacity_JpK;
	}*/

	/*
	@Override
	public double getHeatCapacity_kW() {
    	return capacityHeat_kW;
    }
	*/

	/*@Override //Storage assets limiteren de opname van warmte niet met 1. Dat is nodig voor de buffer. Die kan wel maximaal zijn capaciteit leverern, maar kan meer opnemen. @Gillis is dat logisch of willen we andere oplossing?
    public double[] operateBounded(double ratioOfCapacity) {
    	double limitedRatioOfCapacity = max(-1, ratioOfCapacity);
    	double[] arr = operate(limitedRatioOfCapacity);
    	return arr;
    }*/

	@Override
	public void updateAmbientTemperature(double currentAmbientTemperature_degC) { // TODO: Hoe zorgen we dat we deze niet vergeten aan te roepen??
		this.ambientTemperature_degC = currentAmbientTemperature_degC;
	}

	public void updateSolarRadiation(double solarRadiation_Wpm2) { // TODO: Hoe zorgen we dat we deze niet vergeten aan te roepen??
		this.solarRadiation_Wpm2 = solarRadiation_Wpm2;
		//traceln("Updating solarRadiation of building to %s Wpm2!", solarRadiation_Wpm2);
	}
	
	
	// Methods for Optional Heat Buffer
	// Interior heat buffer may represent the radiator or floor heating. Typical delay is 0.5 or 3 hours respectively.	
	public void addInteriorHeatBuffer(double delayTime_h) {
		this.interiorDelayTime_h = delayTime_h;
		this.interiorReleaseSchedule_kWh = new double[ (int)(delayTime_h / this.timestep_h) ];
		this.interiorReleaseScheduleIndex = 0;
	}

	// Exterior heat buffer may represent the walls and roof of the building. Typical delay is 8 hours.
	public void addExteriorHeatBuffer(double delayTime_h) {
		this.exteriorDelayTime_h = delayTime_h;
		this.exteriorReleaseSchedule_kWh = new double[ (int)(delayTime_h / this.timestep_h) ];
		this.exteriorReleaseScheduleIndex = 0;
	}
	
    private double getInteriorHeatRelease(double heatAbsorbed_kWh) {
    	// Distribute the added energy evenly over the release schedule
    	//traceln("Interior schedule before: " + Arrays.toString(this.interiorReleaseSchedule_kWh));
    	for (int x = 0; x < this.interiorReleaseSchedule_kWh.length; x++) {
    		this.interiorReleaseSchedule_kWh[x] += heatAbsorbed_kWh / this.interiorReleaseSchedule_kWh.length;
    	}
    	// Store the current value
    	double heatReleased_kWh = this.interiorReleaseSchedule_kWh[this.interiorReleaseScheduleIndex];
    	// Reset the current value
        this.interiorReleaseSchedule_kWh[this.interiorReleaseScheduleIndex] = 0;
    	// Shift over the index
    	this.interiorReleaseScheduleIndex++;
    	this.interiorReleaseScheduleIndex = this.interiorReleaseScheduleIndex % this.interiorReleaseSchedule_kWh.length;
    	//traceln("Interior schedule after: " + Arrays.toString(this.interiorReleaseSchedule_kWh));    	
 
    	return heatReleased_kWh;
    }
    
    private double getExteriorHeatRelease(double heatAbsorbed_kWh) {
    	// Distribute the added energy evenly over the release schedule
    	for (int x = 0; x < this.exteriorReleaseSchedule_kWh.length; x++) {
    		this.exteriorReleaseSchedule_kWh[x] += heatAbsorbed_kWh / this.exteriorReleaseSchedule_kWh.length;
    	}
    	// Store the current value
    	double heatReleased_kWh = this.exteriorReleaseSchedule_kWh[this.exteriorReleaseScheduleIndex];
    	// Reset the current value
        this.exteriorReleaseSchedule_kWh[this.exteriorReleaseScheduleIndex] = 0;
    	// Shift over the index
    	this.exteriorReleaseScheduleIndex++;
    	this.exteriorReleaseScheduleIndex = this.exteriorReleaseScheduleIndex % this.exteriorReleaseSchedule_kWh.length;
    	
    	return heatReleased_kWh;
    }
    
    public double getRemainingHeatBufferHeat_kWh() {
    	double remainingHeatBufferHeat_kWh = 0;
		if( this.interiorDelayTime_h != 0.0) {
	    	for (int x = 0; x < this.interiorReleaseSchedule_kWh.length; x++) {
	    		remainingHeatBufferHeat_kWh += this.interiorReleaseSchedule_kWh[x];
	    	}
		}
		if( this.exteriorDelayTime_h != 0.0) {
	    	for (int x = 0; x < this.exteriorReleaseSchedule_kWh.length; x++) {
	    		remainingHeatBufferHeat_kWh += this.exteriorReleaseSchedule_kWh[x];    		
	    	}
		}
    	return remainingHeatBufferHeat_kWh;
    }
    
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}

