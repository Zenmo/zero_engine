/**
 * J_ExperimentSettingsData
 */
public class J_ExperimentSettingsData implements Serializable {

	public String timeStep_h = "";
	public String timeStepsElapsed = "";
	public String modelHoursElapsed_h = "";
	public String modelStartUpDuration_s = "";
	public String modelRunDuration_s = "";
	public String nGridNodes = "";
	public String nGridConnections = "";
	public String nEnergyAssets = "";
	public String nConnectionOwners = "";
	public String nEnergySuppliers = "";
	public String nEnergyCoops = "";
	public String nGridOperators = "";
	public String nNationalEnergyMarket = "";
	public double shareElectricvehiclesInHouseholds = 0.0;

	 /**
     * Default constructor
     */
    public J_ExperimentSettingsData() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_ExperimentSettingsData(String timeStep_h) {
		this.timeStep_h = timeStep_h;
    }

	@Override
	public String toString() {
		return
			"timeStep_h = " + timeStep_h +" " +
			"timeStepsElapsed = " + timeStepsElapsed +" " +
			"modelHoursElapsed_h = " + modelHoursElapsed_h + " " +
			"modelStartUpDuration_s = " + modelStartUpDuration_s + " " +
			"modelRunDuration_s = " + modelRunDuration_s + " " +
			"nGridNodes = " + nGridNodes + " " +
			"nGridConnections = " + nGridConnections + " " +
			"nEnergyAssets = " +	nEnergyAssets + " " +
			"nConnectionOwners = " + nConnectionOwners + " " +
			"nEnergySuppliers = "+ nEnergySuppliers + " " +
			"nEnergyCoops = " + nEnergyCoops + " " +
			"nGridOperators = " + nGridOperators + " " +
			"nNationalEnergyMarket = " + nNationalEnergyMarket +
			"shareElectricvehiclesInHouseholds = " + shareElectricvehiclesInHouseholds
			;
	}

	public void updateData(String timeStep_h, String timeStepsElapsed, String modelHoursElapsed_h, String modelStartUpDuration_s, String modelRunDuration_s, String nGridNodes,	String nGridConnections, String nEnergyAssets, String nConnectionOwners, String nEnergySuppliers, String nEnergyCoops, String nGridOperators, String nNationalEnergyMarket, double shareElectricvehiclesInHouseholds) {
		this.timeStep_h = timeStep_h;
		this.timeStepsElapsed = timeStepsElapsed;
		this.modelHoursElapsed_h = modelHoursElapsed_h;
		this.modelStartUpDuration_s = modelStartUpDuration_s;
		this.modelRunDuration_s = modelRunDuration_s;
		this.nGridNodes = nGridNodes;
		this.nGridConnections = nGridConnections;
		this.nEnergyAssets = nEnergyAssets;
		this.nConnectionOwners = nConnectionOwners;
		this.nEnergySuppliers = nEnergySuppliers;
		this.nEnergyCoops = nEnergyCoops;
		this.nGridOperators = nGridOperators;
		this.nNationalEnergyMarket = nNationalEnergyMarket;
		this.shareElectricvehiclesInHouseholds = shareElectricvehiclesInHouseholds;
	}


	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}