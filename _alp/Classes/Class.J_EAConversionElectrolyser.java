/**
 * J_EAConversionElektrolyser
 */
public class J_EAConversionElectrolyser extends zero_engine.J_EAConversion implements Serializable {

    private boolean useElectrolyserStates = false;
	private OL_ElectrolyserState electrolyserState;
    private OL_ElectrolyserState initialElectrolyserState;
    private int remainingPowerUpDuration_timesteps; // amount of time steps left in power up mode
    private double idlePowerLoadRatio_r;// percentage of nominal load to keep it warm!
    private double mininumProductionRatio_r; // Minimum power fraction required by electrolyser to be able to produce hydrogen.
    private double startUpTimeStandby_h;
    private double loadChangeTime_h;
    private double startUpTimeShutdown_h;
    private double startUpTimeIdle_h;

    //Stored
    private OL_ElectrolyserState storedElectrolyserState;    
	
    
    /**
     * Empty constructor for serialization
     */
    public J_EAConversionElectrolyser() {
    }
    
    /**
     * Constructor initializing the fields (simple)
     */
    public J_EAConversionElectrolyser(I_AssetOwner owner, double inputElectricCapacity_kW, double efficiency_r, J_TimeParameters timeParameters) {
		this.setOwner(owner);
	    this.timeParameters = timeParameters;
	    this.inputCapacity_kW = inputElectricCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.outputCapacity_kW = this.inputCapacity_kW * this.eta_r;
	  
		this.energyAssetType = OL_EnergyAssetType.ELECTROLYSER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HYDROGEN;
	    this.energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;	
	    this.assetFlowCategory = OL_AssetFlowCategories.electrolyserElectricityConsumption_kW;
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset(timeParameters);
	}
    
    /**
     * Constructor initializing the fields (detailed)
     */
    public J_EAConversionElectrolyser(I_AssetOwner owner, double inputElectricCapacity_kW, double efficiency_r, J_TimeParameters timeParameters, OL_ElectrolyserState initialElectrolyserState, double idlePowerLoadRatio_r, double mininumProductionRatio_r, double loadChangeTime_h, double startUpTimeShutdown_h, double startUpTimeStandby_h, double startUpTimeIdle_h) {
	    this(owner, inputElectricCapacity_kW, efficiency_r, timeParameters);
	    this.initialElectrolyserState = initialElectrolyserState;
		this.electrolyserState = initialElectrolyserState;
	    this.idlePowerLoadRatio_r = idlePowerLoadRatio_r;
	    this.mininumProductionRatio_r = mininumProductionRatio_r;
	    this.startUpTimeStandby_h = startUpTimeStandby_h;
	    this.loadChangeTime_h = loadChangeTime_h;
	    this.startUpTimeShutdown_h = startUpTimeShutdown_h;
	    this.startUpTimeIdle_h = startUpTimeIdle_h;
	    this.useElectrolyserStates = true;
	}
       
    @Override
    public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
		double electricityConsumption_kW = inputCapacity_kW * powerFraction_fr;
		double hydrogenProduction_kW = outputCapacity_kW * powerFraction_fr;
		
		if(useElectrolyserStates) {
			if (electrolyserState == OL_ElectrolyserState.POWER_UP) {
				this.remainingPowerUpDuration_timesteps--;
				electricityConsumption_kW = this.inputCapacity_kW * this.idlePowerLoadRatio_r;
				hydrogenProduction_kW = 0;
			}
			else if (electrolyserState == OL_ElectrolyserState.IDLE) {
				electricityConsumption_kW = this.inputCapacity_kW * this.idlePowerLoadRatio_r;
				hydrogenProduction_kW = 0;
			}
		}
    	
    	this.energyUse_kW = (electricityConsumption_kW - hydrogenProduction_kW);
		this.energyUsed_kWh += energyUse_kW * this.timeParameters.getTimeStep_h();

		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);
		flowsMap.put(OL_EnergyCarriers.HYDROGEN, -hydrogenProduction_kW);
		assetFlowsMap.put(OL_AssetFlowCategories.electrolyserElectricityConsumption_kW, electricityConsumption_kW);
    }
    
    
    public void setElectrolyserState(OL_ElectrolyserState electrolyserState) { // Used for regime control
    	this.electrolyserState = electrolyserState;
    }
    public void setRemainingPowerUpDuration_timesteps(int remainingPowerUpDuration_timesteps) {
    	this.remainingPowerUpDuration_timesteps = remainingPowerUpDuration_timesteps;
    }
    public void setIdlePowerLoadRatio_r(double idlePowerLoadRatio_r) {
    	this.idlePowerLoadRatio_r= idlePowerLoadRatio_r;
    }
    public void setMininumProductionRatio_r(double mininumProductionRatio_r) {
    	this.mininumProductionRatio_r = mininumProductionRatio_r;
    }
    public void setStartUpTimeStandby_h( double startUpTimeStandby_h ) { 
        this.startUpTimeStandby_h = startUpTimeStandby_h;
	}
    public void setLoadChangeTime_h( double loadChangeTime_h ) {
    	this.loadChangeTime_h = loadChangeTime_h;
	}
    public void setStartUpTimeShutdown_h( double startUpTimeShutdown_h ) {
    	this.startUpTimeShutdown_h = startUpTimeShutdown_h;
	}
    public void setStartUpTimeIdle_h( double startUpTimeIdle_h ) {
    	this.startUpTimeIdle_h = startUpTimeIdle_h;
	}

    @Override
	public void setEta_r( double efficiency_r) {
		this.eta_r = efficiency_r;
		this.outputCapacity_kW = this.inputCapacity_kW * this.eta_r;
	}
    
    public boolean usesElectrolyserStates() {
    	return useElectrolyserStates;
    }
	public OL_ElectrolyserState getState() { // Used for regime control
    	return this.electrolyserState;
	}
    public int getRemainingPowerUpDuration_timesteps() {
    	return this.remainingPowerUpDuration_timesteps;
    }
    public double getIdlePowerLoadRatio_r() {
    	return this.idlePowerLoadRatio_r;
    }
    public double getMininumProductionRatio_r() {
    	return this.mininumProductionRatio_r;
    }
    public double getStartUpTimeStandby_h() { 
        return this.startUpTimeStandby_h;
	}
	public double getLoadChangeTime_h() { 
		return this.loadChangeTime_h;
	}
	public double getStartUpTimeShutdown_h() {
		return this.startUpTimeShutdown_h;
	}
	public double getStartUpTimeIdle_h() {
		return this.startUpTimeIdle_h;
	}
	
	@Override
    public void storeStatesAndReset() {
    	this.storedElectrolyserState = electrolyserState;
    	this.electrolyserState = this.initialElectrolyserState;
    	this.energyUsedStored_kWh = this.energyUsed_kWh;
    	this.energyUsed_kWh = 0.0;
    	clear();    	
    }
    
    @Override
    public void restoreStates() {
    	this.electrolyserState = this.storedElectrolyserState;
    	this.energyUsed_kWh = this.energyUsedStored_kWh;
    }
    
	@Override
	public String toString() {	
		return  this.energyAssetType + ", "
				+ "Current state: " + this.electrolyserState + ", "
				+ "InputCapacity: " + this.inputCapacity_kW + " kW, " 
				+ "with efficiency: " + this.eta_r + ", "
				+ "Current output: " + -this.getLastFlows().get(this.energyCarrierProduced) + " kW";
	}
}                         