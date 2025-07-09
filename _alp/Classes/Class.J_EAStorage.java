/**
 * J_EAStorage
 */
public abstract class J_EAStorage extends J_EA implements Serializable {

	protected OL_EnergyCarriers storageMedium;
	protected double storageCapacity_kWh;
	protected double stateOfCharge_r;
	protected double initialStateOfCharge_r;
	protected double stateOfChargeStored_r;
	//protected double lossFactor_r;
	//protected double ambientTemperature_degC;
	//protected String ambientTempType;
	protected double discharged_kWh = 0;
	protected double charged_kWh = 0;

    /**
     * Default constructor
     */
    public J_EAStorage() {
    }

    public void calculateLoss() {
    	//
    }

    //public abstract Pair<J_FlowsMap, Double> operate(double ratioOfChargeCapacity_r);

	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		stateOfCharge_r += deltaEnergy_kWh / storageCapacity_kWh;
	}
	
    @Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	stateOfChargeStored_r = stateOfCharge_r;
    	stateOfCharge_r = initialStateOfCharge_r;    
    	charged_kWh = 0;
    	discharged_kWh = 0;
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;    	
    	stateOfCharge_r = stateOfChargeStored_r;
    }
    
	// acces current state of charge
	@Override
	public double getCurrentStateOfCharge() {
    	return stateOfCharge_r;
	}

	public double getStorageCapacity_kWh() {
		return storageCapacity_kWh;
	}
	
	@Override
	public double getCurrentTemperature() {
		return 0;
	}

	@Override
	public void updateAmbientTemperature(double currentAmbientTemperature_degC) {
		//
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