/**
 * J_PBLParameters
 */	
public class J_PBLParameters {
	private final OL_PBL_BuildingType buildingType;
	private final OL_PBL_OwnershipType ownershipType;
	private final OL_GridConnectionInsulationLabel defaultInsulationLabel;
	private final double localFactor;
	private final double regionalClimateCorrectionFactor;
    
	
	/**
     * Default constructor
     */
    public J_PBLParameters(OL_PBL_BuildingType buildingType, OL_PBL_OwnershipType ownershipType, OL_GridConnectionInsulationLabel defaultInsulationLabel, Double localFactor, Double regionalClimateCorrectionFactor) {
    	this.buildingType = buildingType;
    	this.ownershipType = ownershipType;
    	this.defaultInsulationLabel = defaultInsulationLabel;
    	this.localFactor = localFactor;
    	this.regionalClimateCorrectionFactor = regionalClimateCorrectionFactor;
    }
    
    public OL_PBL_BuildingType getBuildingType() {
        return buildingType;
    }

    public OL_PBL_OwnershipType getOwnershipType() {
        return ownershipType;
    }

    public OL_GridConnectionInsulationLabel getDefaultInsulationLabel() {
        return defaultInsulationLabel;
    }

    public double getLocalFactor() {
        return localFactor;
    }

    public double getRegionalClimateCorrectionFactor() {
        return regionalClimateCorrectionFactor;
    }
    
    @Override
    public String toString() {
        return "PBL parameters: \n" +
                "buildingType = " + buildingType +
                ",\n ownershipType = " + ownershipType +
                ",\n defaultIsolationLabel = " + defaultInsulationLabel +
                ",\n localFactor = " + localFactor +
                ",\n regionalClimateCorrectionFactor = " + regionalClimateCorrectionFactor;
    }
}