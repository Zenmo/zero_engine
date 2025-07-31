/**
 * J_HeatingManagementGhost
 */	
public class J_HeatingManagementGhost implements I_HeatingManagement {

	private boolean isInitialized = false;
	private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.GAS_BURNER,
		OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP, 
		OL_GridConnectionHeatingType.HYDROGENBURNER
	);
	private OL_GridConnectionHeatingType currentHeatingType;
	
    /**
     * Default constructor
     */
    public J_HeatingManagementGhost() {
    }
    
    public J_HeatingManagementGhost( GridConnection gc, OL_GridConnectionHeatingType heatingType) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }

    public void manageHeating() {
    	if ( !isInitialized ) {
    		this.initializeAssets();
    	}
    	return;
    }
    
    public void initializeAssets() {
    	this.isInitialized = true;
    }
    
    public void notInitialized() {
    	this.isInitialized = false;
    }
    
    public List<OL_GridConnectionHeatingType> getValidHeatingTypes() {
    	return this.validHeatingTypes;
    }
    
    public OL_GridConnectionHeatingType getCurrentHeatingType() {
    	return this.currentHeatingType;
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