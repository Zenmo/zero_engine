/**
 * J_PBLParameters
 */	
public class J_PBLParameters {
	private final OL_PBL_BuildingType buildingType;
	private final OL_PBL_OwnershipType ownershipType;
	private final OL_PBL_ConstructionPeriod constructionPeriod;
	private final OL_GridConnectionIsolationLabel defaultIsolationLabel;
	private final double localFactor;
	private final double regionalClimateCorrectionFactor;
    
	
	/**
     * Default constructor
     */
    public J_PBLParameters(OL_PBL_BuildingType buildingType, OL_PBL_OwnershipType ownershipType, OL_PBL_ConstructionPeriod constructionPeriod, OL_GridConnectionIsolationLabel defaultIsolationLabel, Double localFactor, Double regionalClimateCorrectionFactor) {
    	this.buildingType = buildingType;
    	this.ownershipType = ownershipType;
    	this.constructionPeriod = constructionPeriod;
    	this.defaultIsolationLabel = defaultIsolationLabel;
    	this.localFactor = localFactor;
    	this.regionalClimateCorrectionFactor = regionalClimateCorrectionFactor;
    }
    
    public OL_PBL_BuildingType getBuildingType() {
        return buildingType;
    }

    public OL_PBL_OwnershipType getOwnershipType() {
        return ownershipType;
    }

    public OL_PBL_ConstructionPeriod getConstructionPeriod() {
        return constructionPeriod;
    }

    public OL_GridConnectionIsolationLabel getDefaultIsolationLabel() {
        return defaultIsolationLabel;
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
                ", \n ownershipType = " + ownershipType +
                ", \n constructionPeriod = " + constructionPeriod +
                ", \n defaultIsolationLabel = " + defaultIsolationLabel +
                ", \n localFactor = " + localFactor +
                ", \n regionalClimateCorrectionFactor = " + regionalClimateCorrectionFactor;
    }
}