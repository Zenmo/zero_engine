/**
 * J_BackupGeneratorManagementExternalSetpoint: Very simple management: backup generator is used to stay within contracted delivery capacity.
 */	
public class J_BackupGeneratorManagementExternalSetpoint implements I_BackupGeneratorManagement {
	
	private GridConnection gc;
	private J_TimeParameters timeParameters;

	private double backupGeneratorPowerSetpoint_kW;
	
    /**
     * Empty constructor for serialization
     */
    public J_BackupGeneratorManagementExternalSetpoint() {
    }
    
    /**
     * Default constructor
     */
    public J_BackupGeneratorManagementExternalSetpoint(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    public void manageBackupGenerator(J_TimeVariables timeVariables) {
    	//Get the backup generator asset
    	J_EAConversion backupGenerator = findFirst(gc.c_conversionAssets, asset -> asset.getEAType() == OL_EnergyAssetType.DIESEL_GENERATOR || 
																	    		   asset.getEAType() == OL_EnergyAssetType.METHANE_GENERATOR ||
																	    		   asset.getEAType() == OL_EnergyAssetType.FUEL_CELL);

    	//Calculate power fraction (if output capacity > 0) else: power fraction = 0.
    	double backupGeneratorPowerFraction_fr = backupGenerator.getOutputCapacity_kW() > 0 ? this.backupGeneratorPowerSetpoint_kW/backupGenerator.getOutputCapacity_kW() : 0;
    	
    	//Update f_updateFlexAssetFlows of asset (and with that fm_currentBalanceFlows_kW of gc) with found setpoint
    	gc.f_updateFlexAssetFlows(backupGenerator, backupGeneratorPowerFraction_fr, timeVariables);
    	
    	this.backupGeneratorPowerSetpoint_kW = 0;
    }
    
    public void setBackupGeneratorSetpoint_kW(double backupGeneratorPowerSetpoint_kW) {
    	this.backupGeneratorPowerSetpoint_kW = backupGeneratorPowerSetpoint_kW;
    }
    
    ////Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		return "J_BackupGeneratorManagementExternalSetpoint: " + System.lineSeparator() +
				"Current setpoint = " + this.backupGeneratorPowerSetpoint_kW;
	}

}