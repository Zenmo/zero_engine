/**
 * Airco
 */	
public class J_EAAirco extends zero_engine.J_EAFlex implements Serializable {
	
	double capacityElectric_kW;
	int remainingONtimesteps = 0;
    /**
     * Default constructor
     */
    public J_EAAirco(I_AssetOwner owner, double capacityElectric_kW, J_TimeParameters timeParameters) {
		this.setOwner(owner);
    	this.timeParameters = timeParameters;	
    	this.capacityElectric_kW = capacityElectric_kW;
		this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.ELECTRICITY);
		//this.assetFlowCategory = OL_AssetFlowCategories.AIRCO; // bestaat nog niet!
		this.registerEnergyAsset();
    }
    
    
	@Override
    public void operate(double powerFraction_fr, J_TimeVariables timeVariables) {
		if( remainingONtimesteps > 0) {
			this.remainingONtimesteps--;
			double electricityConsumption_kW = powerFraction_fr * this.capacityElectric_kW;
			this.energyUse_kW = electricityConsumption_kW;
			this.energyUsed_kWh += this.energyUse_kW * this.timeParameters.getTimeStep_h();
			this.flowsMap.put(OL_EnergyCarriers.ELECTRICITY, electricityConsumption_kW);
		}
	}

	public void turnOnAirco(int nbTimesteps) {
		this.remainingONtimesteps = nbTimesteps;
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