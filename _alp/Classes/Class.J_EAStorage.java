/**
 * J_EAStorage
 */
public abstract class J_EAStorage extends J_EAFlex implements Serializable {

	protected OL_EnergyCarriers storageMedium;
	protected double storageCapacity_kWh;
	protected double stateOfCharge_fr;
	protected double initialstateOfCharge_fr;
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

    //public abstract double calculateLoss();

	protected void updateStateOfCharge( double deltaEnergy_kWh ) {
		stateOfCharge_fr += deltaEnergy_kWh / storageCapacity_kWh;
	}
	
    @Override
    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	stateOfChargeStored_r = stateOfCharge_fr;
    	stateOfCharge_fr = initialstateOfCharge_fr;    
    	charged_kWh = 0;
    	discharged_kWh = 0;
    	clear();    	
    }
    
	@Override
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;    	
    	stateOfCharge_fr = stateOfChargeStored_r;
    }
    
	public double getCurrentStateOfCharge_fr() {
    	return this.stateOfCharge_fr;
	}

	public double getStorageCapacity_kWh() {
		return this.storageCapacity_kWh;
	}

	public double getCurrentStateOfCharge_kWh() {
		return this.stateOfCharge_fr * this.storageCapacity_kWh;
	}
		
	/*@Override
	public double getCurrentTemperature() {
		return 0;
	}

	@Override
	public void updateAmbientTemperature(double currentAmbientTemperature_degC) {
	}*/

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