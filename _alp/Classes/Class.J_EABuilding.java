/**
 * J_EABuilding
 */
//import com.fasterxml.jackson.annotation.JsonTypeName;
//@JsonTypeName("J_EABuilding")
public class J_EABuilding extends zero_engine.J_EAStorageHeat implements Serializable {

	private double solarAbsorptionFactor_m2;
	private double solarRadiation_Wpm2 = 0;
	
	//Slider scaling factor
	private double lossScalingFactor_fr = 1;
	
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
		this.capacityHeat_kW = capacityHeat_kW;
		this.lossFactor_WpK = lossFactor_WpK;
		this.timestep_h = timestep_h;
		this.initialTemperature_degC = initialTemperature_degC;
		this.temperature_degC = initialTemperature_degC;
		this.heatCapacity_JpK = heatCapacity_JpK;
		this.ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
		this.solarAbsorptionFactor_m2 = solarAbsorptionFactor_m2;
		this.energyAssetType = OL_EnergyAssetType.BUILDINGTHERMALS;
	    if (lossFactor_WpK < 0) {
	    	throw new RuntimeException(String.format("Exception: J_EABuilding with negative lossfactor! %s", lossFactor_WpK));	    	
	    }
	    
	    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.HEAT);		
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.HEAT);
		this.assetFlowCategory = OL_AssetFlowCategories.buildingHeating_kW;
		registerEnergyAsset();
    }

	@Override
	public double calculateLoss() {
		double heatLoss_kW = (this.lossFactor_WpK * ( this.temperature_degC - this.ambientTemperature_degC ) / 1000) * lossScalingFactor_fr;
		return heatLoss_kW;
	}

	public double solarHeating() {
		double solarHeating_kW = this.solarAbsorptionFactor_m2 * this.solarRadiation_Wpm2 / 1000;
		return solarHeating_kW;

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
		if (ratioOfChargeCapacity_r < 0) {
			throw new RuntimeException("Cooling of the J_EABuilding is not yet supported.");
		}
		
		double lossPower_kW = calculateLoss(); // Heat exchange with environment through convection
		double solarHeating_kW = solarHeating(); // Heat influx from sunlight
		this.energyUse_kW = lossPower_kW - solarHeating_kW;
		this.energyUsed_kWh += max(0, this.energyUse_kW * this.timestep_h); // Only heat loss! Not heat gain when outside is hotter than inside!
		this.ambientEnergyAbsorbed_kWh += max(0, -this.energyUse_kW * this.timestep_h); // Only heat gain from outside air and/or solar irradiance!

		double inputPower_kW = ratioOfChargeCapacity_r * this.capacityHeat_kW; // positive power means lowering the buffer temperature!		
    	
		double deltaEnergy_kWh = (solarHeating_kW - lossPower_kW)* this.timestep_h;
		if (this.interiorDelayTime_h != 0.0) {
			deltaEnergy_kWh += getInteriorHeatRelease( inputPower_kW * this.timestep_h );
    	}
		else { 
			deltaEnergy_kWh += inputPower_kW * this.timestep_h; // to check the request with the energy currently in storage
		}
		updateStateOfCharge( deltaEnergy_kWh );
		
		this.heatCharged_kWh += inputPower_kW * this.timestep_h;
		
		this.flowsMap.put(OL_EnergyCarriers.HEAT, inputPower_kW);

		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, inputPower_kW);
		}
	}

	@Override
	public String toString() {
		return
			this.getClass().toString() + " " +
			"energyUsed_kWh (heat losses) = " + this.energyUsed_kWh + "kWh, " +
			"temp = " + this.temperature_degC + ", " +
			"lossFactor_WpK = " + this.lossFactor_WpK + ", "+
			"heatCapacity_JpK = " + this.heatCapacity_JpK + ", "+
			"parentAgent = " + parentAgent; // + ", "
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		double tempDelta_degC = deltaEnergy_kWh / (this.heatCapacity_JpK / 3.6E6 );
		this.temperature_degC += tempDelta_degC;
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
		this.ambientEnergyAbsorbedStored_kWh = this.ambientEnergyAbsorbed_kWh;
		this.energyUsed_kWh = 0.0;
		this.ambientEnergyAbsorbed_kWh = 0.0;
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
		this.ambientEnergyAbsorbed_kWh = this.ambientEnergyAbsorbedStored_kWh;
		this.temperature_degC = this.temperatureStored_degC;
		this.interiorReleaseSchedule_kWh = this.interiorReleaseScheduleStored_kWh;
		this.exteriorReleaseSchedule_kWh = this.exteriorReleaseScheduleStored_kWh;		
    }

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
    
    @Override
	public double getRemainingHeatStorageHeat_kWh() {
    	double remainingHeatStorageHeat_kWh = super.getRemainingHeatStorageHeat_kWh(); 
    	remainingHeatStorageHeat_kWh += getRemainingHeatBufferHeat_kWh();
    	return remainingHeatStorageHeat_kWh;
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

