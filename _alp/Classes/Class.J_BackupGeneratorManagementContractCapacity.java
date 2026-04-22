/**
 * J_BackupGeneratorManagementContractCapacity: Very simple management: backup generator is used to stay within contracted delivery capacity.
 */	
public class J_BackupGeneratorManagementContractCapacity implements I_BackupGeneratorManagement {
	
	private GridConnection gc;
	private J_TimeParameters timeParameters;

    /**
     * Empty constructor for serialization
     */
    public J_BackupGeneratorManagementContractCapacity() {
    }
    
    /**
     * Default constructor
     */
    public J_BackupGeneratorManagementContractCapacity(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    public void manageBackupGenerator(J_TimeVariables timeVariables) {
    	//Get the backup generator asset
    	List<J_EAConversion> backupGenerators = findAll(gc.c_conversionAssets, asset -> asset.getEAType() == OL_EnergyAssetType.DIESEL_GENERATOR || 
																	    		   asset.getEAType() == OL_EnergyAssetType.METHANE_GENERATOR ||
																	    		   asset.getEAType() == OL_EnergyAssetType.FUEL_CELL);
    	for(J_EAConversion backupGenerator : backupGenerators) {//Order determined by order in c_conversionAssets of GC.
	    	//Determine backupGeneratorSetpoint based on current load: If load is higher then contracted delivery capacity: (try to) compensate it with the backup generator.
	    	double backupGeneratorSetpoint_kW = max(0, gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - gc.v_liveConnectionMetaData.getContractedDeliveryCapacity_kW());
	    	
	    	//Calculate power fraction (if output capacity > 0) else: power fraction = 0.
	    	double backupGeneratorPowerFraction_fr = backupGenerator.getOutputCapacity_kW() > 0 ? backupGeneratorSetpoint_kW/backupGenerator.getOutputCapacity_kW() : 0;
	    	
	    	//Update f_updateFlexAssetFlows of asset (and with that fm_currentBalanceFlows_kW of gc) with found setpoint
	    	gc.f_updateFlexAssetFlows(backupGenerator, backupGeneratorPowerFraction_fr, timeVariables);
    	}
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
		List<J_EAConversion> backupGenerators = findAll(gc.c_conversionAssets, asset -> asset.getEAType() == OL_EnergyAssetType.DIESEL_GENERATOR || 
	    		   asset.getEAType() == OL_EnergyAssetType.METHANE_GENERATOR ||
	    		   asset.getEAType() == OL_EnergyAssetType.FUEL_CELL);
		StringBuilder backupGeneratorsString = new StringBuilder();
		for(J_EAConversion backupGenerator : backupGenerators) {
			backupGeneratorsString.append(System.lineSeparator());
			backupGeneratorsString.append(backupGenerator.toString());
		}
		
		return "J_BackupGeneratorManagementContractCapacity: " + System.lineSeparator() +
				"Currently controlling backupGenerators: " + 
				backupGeneratorsString.toString();
	}

}