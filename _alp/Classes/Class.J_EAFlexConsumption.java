/**
 * J_EAFlexConsumption
 */	
public class J_EAFlexConsumption extends J_EA implements Serializable {

	double dailyDemand_kWh;
	protected OL_EnergyCarriers energyCarrier;
	int scheduleLength;
	boolean[] allowedOperatingIndices;
	double[] profile_kW;
	int currentIndex = 0;
	double capacity_kW;
	int powerSteps_n = 10;

    /**
     * Default constructor
     */
    public J_EAFlexConsumption() {
    }
    
    public J_EAFlexConsumption(Agent parentAgent, OL_EnergyAssetType type, String name, double dailyDemand_kWh, double capacity_kW, OL_EnergyCarriers energyCarrier, boolean[] allowedOperatingIndices, double timeStep_h) {
    	
    	this.energyAssetName = name;
		this.energyAssetType = type;
    	this.parentAgent = parentAgent;
    	this.dailyDemand_kWh = dailyDemand_kWh;
		this.capacity_kW = capacity_kW;
    	this.timestep_h = timeStep_h;
		
		this.scheduleLength = roundToInt( 24 / timeStep_h );		
		if (allowedOperatingIndices.length != this.scheduleLength) {
			throw new RuntimeException("Error in length of allowedOperatingTimes of J_EAFlexConsumption: " + name);
		}
		this.allowedOperatingIndices = allowedOperatingIndices;
		this.profile_kW = new double[this.scheduleLength];

		this.energyCarrier =  energyCarrier;
		this.activeConsumptionEnergyCarriers.add(this.energyCarrier);
		registerEnergyAsset();
    }
    
    public void updateDailyDemand_kWh(double dailyDemand_kWh) {
    	this.dailyDemand_kWh = dailyDemand_kWh;    	
    }
    
    public double getDailyDemand_kWh() {
    	return this.dailyDemand_kWh;
    }
    public void resetProfile() {
		this.profile_kW = new double[this.scheduleLength]; // Don't make a new array but set all values to 0?
		this.currentIndex = 0;
    }
    
    public double[] getProfile_kW() {
    	return this.profile_kW;
    }

    public double getCapacity_kW() {
    	return this.capacity_kW;
    }
    public void updatePowerSteps_n(int powerSteps_n) {
    	this.powerSteps_n = powerSteps_n;
    }
    
    public int getPowerSteps_n() {
    	return this.powerSteps_n;
    }
    
    public boolean[] getAllowedOperatingIndices() {
    	return this.allowedOperatingIndices;
    }

    public String getName() {
    	return this.energyAssetName;
    }
    
    @Override
    public void f_updateAllFlows() {
    	this.v_powerFraction_fr = this.profile_kW[this.currentIndex] / this.capacity_kW;
     	operate(this.v_powerFraction_fr );
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, this);
    	}

    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }
    
    @Override
    public void operate(double powerFraction_fr) {
    	double consumption_kW = powerFraction_fr * this.capacity_kW;
    	this.energyUse_kW = consumption_kW;
    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;
		flowsMap.put(this.energyCarrier, consumption_kW);
    	this.currentIndex++;
    }


	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}