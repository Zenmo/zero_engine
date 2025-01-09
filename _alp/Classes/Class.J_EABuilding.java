/**
 * J_EABuilding
 */
public class J_EABuilding extends zero_engine.J_EAStorageHeat implements Serializable {

	//EnergyAsset ownerAsset;
	private double capacityElectric_kW = 0;
	//public OL_EnergyCarriers storageMedium = OL_EnergyCarriers.HEAT;
	public OL_EAStorageTypes heatStorageType;
	//private double storageCapacity_kWh;
	private double stateOfCharge_r;
	//private double lossFactor_WpK;
	private double timestep_h;
	private double temperature_degC;
	private double temperatureStored_degC;
	private double initialTemperature_degC;
	private double minTemperature_degC;
	private double maxTemperature_degC;
	//public double setTemperature_degC;
	//private double heatCapacity_JpK;
	private double ambientTemperature_degC;
	private double solarAbsorptionFactor_m2;
	private double solarRadiation_Wpm2=0;
	public double energyAbsorbed_kWh=0;
	public double energyAbsorbedStored_kWh=0;
	
	//public boolean requiresHeat = false;


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
		//traceln(this.getLossFactor_WpK());
		//traceln(this.lossFactor_WpK);
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
		double heatLoss_W = lossFactor_WpK * ( temperature_degC - ambientTemperature_degC );
		double deltaEnergy_kWh = ( -heatLoss_W / 1000 ) * timestep_h;
		energyUse_kW = ( heatLoss_W / 1000 );
		energyUsed_kWh += max(0,energyUse_kW * timestep_h); // Only heat loss! Not heat gain when outside is hotter than inside!
		energyAbsorbed_kWh += max(0,-energyUse_kW * timestep_h); // Only heat gain when outside is hotter than inside!
		//traceln("Ambient temperature " + ambientTemperature_degC);
		//traceln("heatCapacity JpK " + heatCapacity_JpK );
		//traceln("tempdelta loss in thermal building asset (kW) " + energyUse_kW);
		//traceln("lossfacter: " + lossFactor_WpK);
		updateStateOfCharge( deltaEnergy_kWh );
	}

	public void solarHeating() {
		double solarHeating_W = solarAbsorptionFactor_m2 * solarRadiation_Wpm2;
		double deltaEnergy_kWh = ( solarHeating_W / 1000 ) * timestep_h; // Is always positive
		energyAbsorbed_kWh += deltaEnergy_kWh;
		//traceln("Solar heating of building! Temp increase this timestep %s degC, solarRadiation %s Wpm2", tempDelta, solarRadiation_Wpm2);
		//traceln("SolarAbsorptionFactor_m2 is %s, energyGain_kWh is %s", solarAbsorptionFactor_m2, energyAbsorbed_kWh);
		//traceln("Energy gain of building heatmodel from sun = " + energyUse_kW);
		updateStateOfCharge( deltaEnergy_kWh );
	}

	@Override
	public void operate(double ratioOfChargeCapacity_r) {
		//traceln("Building heatCapacity_JpK: %s", this.heatCapacity_JpK);
		//traceln("StorageAsset Heat Operatefunctie: ambienttemperature = "+ambientTemperature_degC);
		//traceln("StorageAsset Heat Operatefunctie: ambienttemperature = "+ambientTemperature_degC+" | powerFraction_fr = " + ratioOfChargeCapacity_r + ".");
		//traceln("<><><><> heatstorage reset heatproduction = "+heatProduction_kW+", heatconsumption_kW = "+heatConsumption_kW+" heatProduced_kWh = "+heatProduced_kWh + "heatConsumed = "+heatConsumed_kWh + ", losses= "+energyUsed_kWh );

		calculateLoss(); // Heat exchange with environment through convection
		solarHeating(); // Heat influx from sunlight
  		
		
		double inputPower_kW = ratioOfChargeCapacity_r * capacityHeat_kW; // positive power means lowering the buffer temperature!
    	double deltaEnergy_kWh = inputPower_kW * timestep_h; // to check the request with the energy currently in storage
    	
		double heatProduction_kW = max(-inputPower_kW, 0);
		double heatConsumption_kW = max(inputPower_kW, 0);
		heatProduced_kWh += heatProduction_kW * timestep_h;
		heatConsumed_kWh += heatConsumption_kW * timestep_h;
		//traceln("Heat consumption delivered by heating asset kW " + heatConsumption_kW);
		//traceln("Heatcapacity kWh: " + (heatCapacity_JpK / 3.6E6 ));
		//traceln("tempdelta charge: " + deltaTemp_degC);
		//traceln(">> Heat storage heatproduction = "+ heatProduced_kWh + ", heatconsumption_kW = "+ heatConsumption_kW +" heatConsumed_kWh = "+ heatConsumed_kWh +", heatProduced_kWh = "+ heatProduced_kWh );

		updateStateOfCharge( deltaEnergy_kWh );
		//traceln("<><><><> heatstorage <"+ownerAsset.getId()+"> calculated heatproduction = "+heatProduction_kW+", heatconsumption_kW = "+heatConsumption_kW+", heatProduced_kWh = "+heatProduced_kWh + ", heatConsumed = "+heatConsumed_kWh + ", losses= "+energyUsed_kWh );
		//J_FlowsMap flowsMap = returnEnergyFlows();

		flowsMap.put(OL_EnergyCarriers.HEAT, inputPower_kW);
		/*if (Double.isNaN(this.energyUse_kW)) {
    		throw new RuntimeException("Building thermal model energyUse_kW is NaN!");
    	}*/
		//return new Pair(flowsMap, this.energyUse_kW);
		
	}

	@Override
	public String toString() {
		return
			this.getClass().toString() + " " +
			"Energy consumed = " + this.energyUsed_kWh +
			" temp = " + this.temperature_degC + " " +
			"oarentAgent = " + parentAgent +" " +
			"capacityHeat_kW = " + this.capacityHeat_kW +" "+
			"ambientTemperature_degC = "+this.ambientTemperature_degC+" "+
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh + " "+
			"heatProduced_kWh = "+ this.heatProduced_kWh + " "+
			"heatConsumed_kWh = "+ this.heatConsumed_kWh + " ";
	}

	@Override
	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		double tempDelta_degC = deltaEnergy_kWh / (heatCapacity_JpK / 3.6E6 );
		temperature_degC += tempDelta_degC;
		stateOfCharge_r = ( temperature_degC - minTemperature_degC) / (maxTemperature_degC - minTemperature_degC);
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
    	return stateOfCharge_r;
	}

	@Override
	public double getCurrentTemperature() {
		return temperature_degC;
	}
	
	@Override
	public double getLossFactor_WpK() {
		return lossFactor_WpK;
	}
	

	/*@Override
	public double getHeatCapacity_JpK() {
		return heatCapacity_JpK;
	}*/
	
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
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}

