/**
 * J_EAConversionAirConditioner
 */	
public class J_EAConversionAirConditioner extends J_EAConversion {

    private double COP_r;
	protected OL_AmbientTempType ambientTempType;
	public double totalElectricityConsumed_kWh =0;
	public J_EABuilding building;
	//public double p_baseTemperatureReference;
	
    /**
     * Default constructor
     */
    public J_EAConversionAirConditioner() {
    }
    
    //Only supports cooling functionality (extracting heat from building). In reality ACs can also be used for heating, in which case it's basically operating as a heatpump. This code is an adapted form of the J_EAConversionHeatPump.

    /**
     * Constructor initializing the fields
     */
    public J_EAConversionAirConditioner(I_AssetOwner owner, double inputElectricCapacity_kW, J_TimeParameters timeParameters, J_EABuilding building) {
    	this.energyAssetType = OL_EnergyAssetType.AIR_CONDITIONER;
    	
		this.setOwner(owner);
	    this.timeParameters = timeParameters;
		this.inputCapacity_kW = inputElectricCapacity_kW;
	    //this.eta_r = eta_r;
	    this.building = building;
	    this.ambientTempType = OL_AmbientTempType.AMBIENT_AIR;
	    this.updateAmbientTemperature( building.getAmbientTemperature_degC() ); // also updates COP

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT; // Heat is actually 'consumed', but this is the 'useful energy' of the AC; heat is extracted from the building and exhausted outside. In terms of 'final energy consumption' however, this is counted as energy consumption.
    	this.energyCarrierConsumed= OL_EnergyCarriers.ELECTRICITY;  
    	
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);   	
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		this.assetFlowCategory = OL_AssetFlowCategories.airConditionersElectricPower_kW;
		registerEnergyAsset(timeParameters);
	}

	public void updateAmbientTemperature(double ambientTemperature_degC) {    	
		double buildingTemp_degC = building.getCurrentTemperature();
		this.COP_r = calculateCOP(ambientTemperature_degC, buildingTemp_degC); //this.eta_r * ( 273.15 + this.outputTemperature_degC ) / ( this.outputTemperature_degC - this.baseTemperature_degC );
	    this.outputCapacity_kW = this.inputCapacity_kW * this.COP_r; // this represents the current maximum cooling power (heat extracted from building!)
	}
	
	public double getCOP() {
		//traceln("Heatpump output temperature: " + this.outputTemperature_degC);
		return this.COP_r;
	}
	
	@Override
	public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {

		double electricityConsumption_kW = powerFraction_fr * this.inputCapacity_kW;
		this.totalElectricityConsumed_kWh += electricityConsumption_kW * this.timeParameters.getTimeStep_h();
    	
		double coldProduction_kW = electricityConsumption_kW * this.COP_r;

		this.energyUse_kW = electricityConsumption_kW + coldProduction_kW; 		
		flowsMap.put(OL_EnergyCarriers.HEAT, coldProduction_kW); // coldProduction can be seen as the heat flowing into the asset (from the building), so from the GC-perspective it looks like heat consumption.
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);		    
			
    	this.assetFlowsMap.addFlow(OL_AssetFlowCategories.airConditionersElectricPower_kW, electricityConsumption_kW);
		this.energyUsed_kWh += energyUse_kW * this.timeParameters.getTimeStep_h();
	}
    
	public OL_AmbientTempType getAmbientTempType() {
		return this.ambientTempType;
	}
	
	private double calculateCOP(double ambientTemperature_degC, double buildingTemperature_degC) { // This is the cooling COP, defined as the extracted heat power divided by the input electric power.
		double deltaT = max(0,ambientTemperature_degC - buildingTemperature_degC); // Limit deltaT to 0-or-higher, meaning outside temp is equal or higher than inside temp. In reality, it can happen that an AC runs with a lower outside temp, but we 'cap' the COP this way. 
	    double COP_r = 5 - 0.10 * deltaT + 0.00126 * deltaT*deltaT; // 'expert judgement'-curve, not based on manufacturer or measurement data but on 'typical' efficiencies found online.
	    return COP_r; // Ratio of cooling power (extracted heat) to input electric power.
	}
    
	@Override
	public String toString() {	
		return  this.energyAssetType + ", "			
				//+ this.energyCarrierConsumed + " -> " + this.energyCarrierProduced + ", "
				+ "Electric capacity: " + this.inputCapacity_kW + " kW, " 
				+ "with efficiency: " + this.getCOP() + ", "
				+ "Energy used: " + this.energyUsed_kWh + ", "
				+ "Current output: " + -this.getLastFlows().get(this.energyCarrierProduced) + " kW";
	}

}