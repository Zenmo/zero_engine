/**
 * J_EAConversionHeatPump
 */
public class J_EAConversionHeatPump extends zero_engine.J_EAConversion implements Serializable {
	private double COP_r;
	private double belowZeroHeatpumpEtaReductionFactor;
	protected double outputTemperature_degC;
	private double baseTemperature_degC;
	private double sourceAssetHeatPower_kW; // for water-water heatpump functionality
	//protected String ambientTempType;
	public double totalElectricityConsumed_kWh =0;
	public J_EA p_linkedSourceEnergyAsset;
	public double p_baseTemperatureReference;

    /**
     * Default constructor
     */
    public J_EAConversionHeatPump() {
    }

    //Agent parentAgent, double outputCapacity_kW, double efficiency_r, double timestep_h, double outputTemperature_degC) {

    /**
     * Constructor initializing the fields
     */
    public J_EAConversionHeatPump(Agent parentAgent, double inputElectricCapacity_kW, double eta_r, double timestep_h, double outputTemperature_degC, double baseTemperature_degC, double sourceAssetHeatPower_kW, double belowZeroHeatpumpEtaReductionFactor ) {
	    this.parentAgent = parentAgent;
	    this.inputCapacity_kW = inputElectricCapacity_kW;
	    this.timestep_h = timestep_h;
	    this.eta_r = eta_r;
	    this.outputTemperature_degC = outputTemperature_degC;
	    this.COP_r = eta_r * ( 273.15 + outputTemperature_degC ) / ( outputTemperature_degC - baseTemperature_degC );
	    
	    this.updateAmbientTemperature( this.baseTemperature_degC );
	    
	    this.sourceAssetHeatPower_kW = sourceAssetHeatPower_kW;
	    this.belowZeroHeatpumpEtaReductionFactor = belowZeroHeatpumpEtaReductionFactor;

	    this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
    	this.energyCarrierConsumed= OL_EnergyCarriers.ELECTRICITY;  
    	
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);   	
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);

		registerEnergyAsset();
	}

	public void updateParameters(double baseTemperature_degC, double outputTemperature_degC) {
		this.baseTemperature_degC = baseTemperature_degC;
		this.outputTemperature_degC = outputTemperature_degC;
		if ( this.baseTemperature_degC > this.outputTemperature_degC) {
			traceln("**** EXCEPTION **** Heatpump baseTemperature ( " + this.baseTemperature_degC + ") > outputTemperature ( " + this.outputTemperature_degC + ") ");
		}
		this.COP_r = this.eta_r * ( 273.15 + this.outputTemperature_degC ) / ( this.outputTemperature_degC - this.baseTemperature_degC );
		
		// water heatpump should take sourceAsset power transfer limitations into account (e.g. residual heat). Ugly but effectively limiting heat power output.
    	
		if( this.sourceAssetHeatPower_kW > 0) {
	    	this.outputCapacity_kW = min( this.outputCapacity_kW, this.sourceAssetHeatPower_kW / (1 - (1 / COP_r )));
	    	//traceln("Water water heatpump heat capacity limited from source! =" + this.capacityHeat_kW);
		} 
		else {
				this.outputCapacity_kW = this.inputCapacity_kW * this.COP_r; 
				//traceln("heatpump updating temp: " + baseTemperature_degC);
				if( baseTemperature_degC < 0 ) {
					this.COP_r = this.COP_r / this.belowZeroHeatpumpEtaReductionFactor;
				}
		}
    	//traceln("J_EAConversionHeatpump capacityHeat_kW = "+ this.capacityHeat_kW + ", sourceAssetHeatPower_kW " + this.sourceAssetHeatPower_kW );
    
	}

	public void updateAmbientTemperature(double baseTemperature_degC) {
		// water heatpump should take sourceAsset power transfer limitations into account (e.g. residual heat). Ugly but effectively limiting heat power output.
    	
		//traceln("J_EAHeatpump capacityHeat_kW = " + this.capacityHeat_kW + ", baseTemperature = "+ baseTemperature_degC + ", outputtemperature = "+ outputTemperature_degC);
		updateParameters(baseTemperature_degC, this.outputTemperature_degC);
		this.COP_r = this.eta_r * ( 273.15 + this.outputTemperature_degC ) / ( this.outputTemperature_degC - this.baseTemperature_degC );
	    this.outputCapacity_kW = this.inputCapacity_kW * this.COP_r;
	}

	public void setCOP(double COP_r) {
		this.COP_r = COP_r;
		this.outputCapacity_kW = this.inputCapacity_kW * this.COP_r;
	}
	
	public double getCOP() {
		//traceln("Heatpump output temperature: " + this.outputTemperature_degC);
		return this.COP_r;
	}

	@Override
	public void f_updateAllFlows(double powerFraction_fr) {
		if (powerFraction_fr > 0) {
			super.f_updateAllFlows(powerFraction_fr);
		}
		else {
			this.lastFlowsMap.clear();
		}
	}
	
	@Override
	public void operate(double ratioOfCapacity) {

		double electricityConsumption_kW = ratioOfCapacity * this.inputCapacity_kW;
		this.totalElectricityConsumed_kWh += electricityConsumption_kW * timestep_h;
    	double heatProduction_kW = electricityConsumption_kW * this.COP_r;

    	/*
    	double heatConsumption_kW = 0;
    	if(this.ambientTempType.equals("WATER")) {
    		//traceln("test water heatpump EA code for heat consumption. WATER ambient temp type detected ");
    		double maxAvailableSourcePower_kW = this.sourceAssetHeatPower_kW;
    		
    		heatConsumption_kW = heatProduction_kW - electricityConsumption_kW;
    		//update effective energy production of source asset!
    		this.p_linkedSourceEnergyAsset.v_powerFraction_fr += ( heatConsumption_kW / ((J_EAProduction)this.p_linkedSourceEnergyAsset).getCapacityHeat_kW() );
//    		this.ownerAsset.p_linkedSourceEnergyAsset.j_ea.heatProduction_kW += this.heatConsumption_kW;
       	}
       	*/
    	this.energyUse_kW = electricityConsumption_kW - heatProduction_kW; 
		this.energyUsed_kWh += energyUse_kW * timestep_h;
		
		flowsMap.put(OL_EnergyCarriers.HEAT, -heatProduction_kW);		
		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);		
	}

	/*
	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"parentAgent = " + parentAgent +" " +
			"capacityElectricity_kW = " + capacityElectric_kW +" "+
			"capacityHeat_kW = " + capacityHeat_kW +" "+
			"baseTemperature_degC = " + baseTemperature_degC + " "+
			"ambientTempType = " + ambientTempType + " "+
			"sourceAssetHeatPower_kW = " + sourceAssetHeatPower_kW + " " +
			"outputTemperature_degC = " + outputTemperature_degC +" "+
			"energyUsed_kWh = " + energyUsed_kWh + " "+
			"heatProduced_kWh = " + heatProduced_kWh + " "+
			"heatConsumed_kWh = " + heatConsumed_kWh + " ";
	}
	*/
	
	public void setSourceAssetHeatPower(double sourceAssetHeatPower_kW) {
		this.sourceAssetHeatPower_kW = sourceAssetHeatPower_kW;
		//traceln("sourceAssetHeatPower_kW is set to: "+sourceAssetHeatPower_kW);
	}
	
	public void f_setLinkedVariable(EnergyModel main, String ambientTempType, J_EA j_ea) {
		switch( ambientTempType ) {
		case "AIR":
			this.p_baseTemperatureReference = main.v_currentAmbientTemperature_degC ;
		break;
		case "GROUND":
			this.p_baseTemperatureReference= main.p_undergroundTemperature_degC ;
		break;
		case "WATER":
			//traceln("f_setLinkedVariable: water type parameter update!");
			this.p_baseTemperatureReference = j_ea.getCurrentTemperature() ;
			this.updateAmbientTemperature( p_baseTemperatureReference );
		break;
		default:
			traceln("EXCEPTION: ENERGY ASSET WITH A NON-EXISTENT AMBIENT TEMPERATURE TYPE");				
	}
	}
	
	@Override
	public double getCurrentTemperature() {
		return outputTemperature_degC;
	}
	
	public void setBaseTemperature_degC( double baseTemperature_degC) {
		this.baseTemperature_degC = baseTemperature_degC;
		this.updateParameters( this.baseTemperature_degC, this.outputTemperature_degC);
	}

    @Override
	public void setEta_r( double efficiency_r) {
		this.eta_r = efficiency_r;
		this.COP_r = this.eta_r * ( 273.15 + this.outputTemperature_degC ) / ( this.outputTemperature_degC - this.baseTemperature_degC );
		this.outputCapacity_kW = this.inputCapacity_kW * this.COP_r;
	}
    
    
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}
