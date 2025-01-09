/**
 * J_EAConversionElektrolyser
 */
public class J_EAConversionElectrolyser extends zero_engine.J_EAConversion implements Serializable {

    private OL_ElectrolyserState electrolyserState;
    private int remainingPowerUpDuration_timesteps; // amount of time steps left in power up mode
    private double startUpTimeStandby_h;
    private double loadChangeTime_h;
    private double startUpTimeShutdown_h;
    private double startUpTimeIdle_h;
	/**
	/**
     * Default constructor
     */
    public J_EAConversionElectrolyser() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EAConversionElectrolyser(Agent parentAgent, double inputElectricCapacity_kW, double efficiency_r, double  timestep_h, OL_ElectrolyserState electrolyserState, double loadChangeTime_s, double startUpTimeShutdown_s, double startUpTimeStandby_s, double startUpTimeIdle_s) {
	    this.parentAgent = parentAgent;
	    this.electrolyserState = electrolyserState;
	    this.inputCapacity_kW = inputElectricCapacity_kW;
	    this.eta_r = efficiency_r;
	    this.outputCapacity_kW = this.inputCapacity_kW * this.eta_r;
	    this.timestep_h = timestep_h;
	    this.startUpTimeStandby_h = startUpTimeStandby_s/3600;
	    
	    this.loadChangeTime_h = loadChangeTime_s/3600;
	    this.startUpTimeShutdown_h = startUpTimeShutdown_s/3600;
	    this.startUpTimeIdle_h = startUpTimeIdle_s/3600;

		this.energyAssetType = OL_EnergyAssetType.ELECTROLYSER;

	    this.energyCarrierProduced = OL_EnergyCarriers.HYDROGEN;
	    this.energyCarrierConsumed = OL_EnergyCarriers.ELECTRICITY;	   
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset();
	}
    
    @Override
    public void operate(double ratioOfCapacity) {
		double electricityConsumption_kW = inputCapacity_kW * ratioOfCapacity;
		double hydrogenProduction_kW = 0;
		if (electrolyserState == OL_ElectrolyserState.POWER_UP) {
			this.remainingPowerUpDuration_timesteps--;
		}
		else if (electrolyserState == OL_ElectrolyserState.IDLE) {
		}
		else {
			hydrogenProduction_kW = electricityConsumption_kW * eta_r;
		}
    	
    	this.energyUse_kW = (electricityConsumption_kW - hydrogenProduction_kW);
		this.energyUsed_kWh += energyUse_kW * timestep_h;

		flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);
		flowsMap.put(OL_EnergyCarriers.HYDROGEN, -hydrogenProduction_kW);
    }
    
    public void setElectrolyserState(OL_ElectrolyserState electrolyserState) { // Used for regime control
    	this.electrolyserState = electrolyserState;
    }
    
    public void setRemainingPowerUpDuration_timesteps(int remainingPowerUpDuration_timesteps) {
    	this.remainingPowerUpDuration_timesteps = remainingPowerUpDuration_timesteps;
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
    
	public OL_ElectrolyserState getState() { // Used for regime control
    	return this.electrolyserState;
    }
	
    public int getRemainingPowerUpDuration_timesteps() {
    	return this.remainingPowerUpDuration_timesteps;
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
	public String toString() {	
		return  this.energyAssetType + " in GC: " + this.parentAgent + ", "				
				+ "InputCapacity: " + this.inputCapacity_kW + " kW, " 
				+ "with efficiency: " + this.eta_r + ", "
				+ "Current output: " + -this.getLastFlows().get(this.energyCarrierProduced) + " kW";
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}                         