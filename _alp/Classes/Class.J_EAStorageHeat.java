/**
 * J_EAStorageHeat
 */
public class J_EAStorageHeat extends zero_engine.J_EAStorage implements Serializable {

	private double capacityElectric_kW = 0;
	public OL_EnergyCarriers storageMedium = OL_EnergyCarriers.HEAT;
	private double storageCapacity_kWh;
	
	private double stateOfCharge_fr;
	protected double lossFactor_WpK;
	protected double capacityHeat_kW;
	
	private double temperature_degC;
	private double temperatureStored_degC;
	private double initialTemperature_degC;
	private double minTemperature_degC;
	private double maxTemperature_degC;
	private double setTemperature_degC;
	protected double heatCapacity_JpK;
	protected double ambientTemperature_degC;
	public boolean requiresHeat = false;
	protected double energyAbsorbed_kWh=0;
	protected double energyAbsorbedStored_kWh=0;
	protected OL_AmbientTempType ambientTempType;
	//Secureing updates
	private boolean updateAmbientTemperatureHasBeenCalled = false;


    /**
     * Default constructor
     */
    public J_EAStorageHeat() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAStorageHeat(Agent parentAgent, OL_EnergyAssetType energyAssetType, double capacityHeat_kW, double lossFactor_WpK, double timestep_h, double initialTemperature_degC, double minTemperature_degC, double maxTemperature_degC, double setTemperature_degC, double heatCapacity_JpK, OL_AmbientTempType ambientTempType ) {
		this.parentAgent = parentAgent;
		this.energyAssetType = energyAssetType;
		this.capacityHeat_kW = capacityHeat_kW;
		this.lossFactor_WpK = lossFactor_WpK;
		this.timestep_h = timestep_h;
		this.temperature_degC = initialTemperature_degC;
		this.initialTemperature_degC = initialTemperature_degC;
		this.minTemperature_degC = minTemperature_degC;
		this.maxTemperature_degC = maxTemperature_degC;
		this.setTemperature_degC = setTemperature_degC;
		
		if(this.initialTemperature_degC > this.maxTemperature_degC || this.initialTemperature_degC < this.minTemperature_degC) {
			new RuntimeException("Heatbuffer initial temperature is not within the defined min/max temperature limits of the buffer");
		}
		if(this.setTemperature_degC > this.maxTemperature_degC || this.setTemperature_degC < this.minTemperature_degC) {
			new RuntimeException("Heatbuffer set temperature is not within the defined min/max temperature limits of the buffer");
		}
		
		this.heatCapacity_JpK = heatCapacity_JpK;
		this.ambientTempType = ambientTempType;
		this.storageCapacity_kWh = ( maxTemperature_degC - minTemperature_degC ) * heatCapacity_JpK / 3.6e+6;
		this.stateOfCharge_fr = (( initialTemperature_degC - minTemperature_degC ) / (maxTemperature_degC - minTemperature_degC ) );
	    this.activeProductionEnergyCarriers.add(OL_EnergyCarriers.HEAT);		
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.HEAT);
		registerEnergyAsset();
    }

	@Override
	public void calculateLoss() {
		if(ambientTempType != OL_AmbientTempType.FIXED && !this.updateAmbientTemperatureHasBeenCalled) {
			new RuntimeException("Ambient temperature has not been updated for the heat storage asset, make sure to call the updateAmbientTemperature() method while using a heat storage");
		}
		else {
			updateAmbientTemperatureHasBeenCalled = false;
		}
		double heatLoss_W = lossFactor_WpK * ( temperature_degC - ambientTemperature_degC );
		double deltaEnergy_kWh = ( -heatLoss_W / 1000 ) * timestep_h;
		energyUse_kW = heatLoss_W / 1000;
		energyUsed_kWh += max(0,energyUse_kW * timestep_h); // Only heat loss! Not heat gain when outside is hotter than inside!
		energyAbsorbed_kWh += max(0,-energyUse_kW * timestep_h); // Only heat gain when outside is hotter than inside!
		//traceln("Ambient temperature " + ambientTemperature_degC);
		//traceln("heatCapacity JpK " + heatCapacity_JpK );
		//traceln("tempdelta loss"+tempDelta);
		//traceln("lossfacter: " + lossFactor_WpK);
		
		updateStateOfCharge( deltaEnergy_kWh );
	}

	@Override
	public void operate(double ratioOfChargeCapacity_r) {
		//traceln("StorageAsset Heat Operatefunctie: ambienttemperature = "+ambientTemperature_degC);
		//traceln("StorageAsset Heat Operatefunctie: ambienttemperature = "+ambientTemperature_degC+" | powerFraction_fr = " + ratioOfChargeCapacity_r + ".");
		//traceln("<><><><> heatstorage reset heatproduction = "+heatProduction_kW+", heatconsumption_kW = "+heatConsumption_kW+" heatProduced_kWh = "+heatProduced_kWh + "heatConsumed = "+heatConsumed_kWh + ", losses= "+energyUsed_kWh );
		
		calculateLoss(); // Heat lost to the environment; this call also updates energyUse_kW and the 'state of charge' (temperature).
		
		double inputPower_kW = ratioOfChargeCapacity_r * capacityHeat_kW; // positive power means adding heat to the buffer
		double potentialTempDelta_degC = inputPower_kW * timestep_h / (heatCapacity_JpK / (3.6E6) ); // Calculate potential deltaT for check if capping of input is needed
		
		//Cap input to the temperature limits of the buffer
		if(inputPower_kW > 0 && temperature_degC + potentialTempDelta_degC > maxTemperature_degC) { // If it will go over max temp: cap the input power
			inputPower_kW = (maxTemperature_degC - temperature_degC) * (heatCapacity_JpK / (3.6E6) )/timestep_h;
		}
		else if(inputPower_kW < 0 && temperature_degC + potentialTempDelta_degC < minTemperature_degC) {//If it will go under min temp: cap the (negative) input power
			inputPower_kW = (minTemperature_degC - temperature_degC) * (heatCapacity_JpK / (3.6E6) )/timestep_h;
		}
		
    	double deltaEnergy_kWh = inputPower_kW * timestep_h; // to check the request with the energy currently in storage

		double heatProduction_kW = max(-inputPower_kW, 0);
		double heatConsumption_kW = max(inputPower_kW, 0);
		heatProduced_kWh += heatProduction_kW * timestep_h;
		heatConsumed_kWh += heatConsumption_kW * timestep_h;

		//traceln("tempdelta charge: "+deltaTemp_degC);
		//traceln(">> Heat storage heatproduction = "+ heatProduced_kWh + ", heatconsumption_kW = "+ heatConsumption_kW +" heatConsumed_kWh = "+ heatConsumed_kWh +", heatProduced_kWh = "+ heatProduced_kWh );
		
		updateStateOfCharge( deltaEnergy_kWh );
		//traceln("<><><><> heatstorage <"+ownerAsset.getId()+"> calculated heatproduction = "+heatProduction_kW+", heatconsumption_kW = "+heatConsumption_kW+", heatProduced_kWh = "+heatProduced_kWh + ", heatConsumed = "+heatConsumed_kWh + ", losses= "+energyUsed_kWh );

		flowsMap.put(OL_EnergyCarriers.HEAT, heatConsumption_kW-heatProduction_kW);		
		
		//return new Pair(this.flowsMap, this.energyUse_kW);
	}
	
	@Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyAbsorbedStored_kWh = energyAbsorbed_kWh;
    	energyUsed_kWh = 0.0;
    	energyAbsorbed_kWh = 0.0;
    	temperatureStored_degC = temperature_degC;
    	temperature_degC = initialTemperature_degC;
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;
    	energyAbsorbed_kWh = energyAbsorbedStored_kWh;
    	temperature_degC = temperatureStored_degC;
    }

	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"Energy consumed = " + this.energyUsed_kWh +
			" temp = " + this.temperature_degC + " " +
			"parentAgent = " + parentAgent +" " +
			"capacityElectric_kW = " + this.capacityElectric_kW +" "+
			"capacityHeat_kW = " + this.capacityHeat_kW +" "+
			"stateOfCharge_fr = " + this.stateOfCharge_fr+" "+
			"minTemperature_degC = " + this.minTemperature_degC+" "+
			"maxTemperature_degC = " + this.maxTemperature_degC+" "+
			"setTemperature_degC = " + this.setTemperature_degC+" "+			
			"ambientTemperature_degC = "+this.ambientTemperature_degC+" "+
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " "+
			"heatProduced_kWh = "+ this.heatProduced_kWh + " "+
			"heatConsumed_kWh = "+ this.heatConsumed_kWh + " ";
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		double tempDelta_degC = deltaEnergy_kWh / (heatCapacity_JpK / 3.6E6 );
		temperature_degC += tempDelta_degC;
		stateOfCharge_fr = ( temperature_degC - minTemperature_degC) / (maxTemperature_degC - minTemperature_degC);
		if (temperature_degC < setTemperature_degC) {
			requiresHeat = true;
		}
		else if ( temperature_degC >= maxTemperature_degC ) {
			requiresHeat = false;
		}
	}

	@Override
	public double getCurrentTemperature() {
		return temperature_degC;
	}

	@Override
	public double getSetTemperature_degC() {
		return setTemperature_degC;
	}
	
	@Override
	public double getMinTemperature_degC() {
		return minTemperature_degC;
	}
	
	@Override
	public double getMaxTemperature_degC() {
		return maxTemperature_degC;
	}

	public double getStorageCapacity_kWh() {
		return storageCapacity_kWh;
	}

	public double getCapacityHeat_kW() {
		return this.capacityHeat_kW;
	}
	
	public double getHeatCapacity_JpK() {
		return heatCapacity_JpK;
	}
	
	public double getLossFactor_WpK() {
		return lossFactor_WpK;
	}
	
	public OL_AmbientTempType getAmbientTempType() {
		return this.ambientTempType;
	}
	
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
	public void updateAmbientTemperature(double currentAmbientTemperature_degC) {
		this.updateAmbientTemperatureHasBeenCalled = true;
		ambientTemperature_degC = currentAmbientTemperature_degC;
	}
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}

