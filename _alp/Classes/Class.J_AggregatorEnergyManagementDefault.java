/**
 * J_AggregatorManagementDefault
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_AggregatorEnergyManagementDefault implements I_AggregatorEnergyManagement{
	
    private EnergyCoop energyCoop;
    private J_TimeParameters timeParameters;
    
	List<Class<? extends I_AggregatorAssetManagement>> internalAggregatorAssetManagements = new ArrayList<>(); //Inherent asset management that the EMS handles itself
	List<Class<? extends I_AggregatorAssetManagement>> supportedExternalAggregatorAssetManagements = 
												new ArrayList<>(Arrays.asList(
													//J_AggregatorHeatingManagementSimple.class, 
													//I_AggregatorHeatingManagement.class,
													//I_AggregatorChargingManagement.class, 
													I_AggregatorBatteryManagement.class
												));
	Map<Class<? extends I_AggregatorAssetManagement>, I_AggregatorAssetManagement> activeExternalAggregatorAssetManagements = new HashMap();			
	
    /**
     * Empty constructor for serialization
     */
    public J_AggregatorEnergyManagementDefault() {
    }
    
    /**
     * Default constructor
     */
    public J_AggregatorEnergyManagementDefault(EnergyCoop energyCoop, J_TimeParameters timeParameters) {
    	this.energyCoop = energyCoop;
    	this.timeParameters = timeParameters;
    }

    
    //Operation
    public void operateAggregatorEnergyManagement(J_TimeVariables timeVariables) {
    	//1. Call Aggregator Heating management
    	if(this.getExternalAggregatorAssetManagement(I_AggregatorHeatingManagement.class) != null) {
    		this.getExternalAggregatorAssetManagement(I_AggregatorHeatingManagement.class).manageExternalHeatingSetpoints(timeVariables);
    	}
    	
    	//2. Call Charging management
    	if(this.getExternalAggregatorAssetManagement(I_AggregatorChargingManagement.class) != null) {
    		this.getExternalAggregatorAssetManagement(I_AggregatorChargingManagement.class).manageExternalChargingSetpoints(timeVariables);
    	}
    	
    	//3. Call Battery management
    	if(this.getExternalAggregatorAssetManagement(I_AggregatorBatteryManagement.class) != null) {
    		this.getExternalAggregatorAssetManagement(I_AggregatorBatteryManagement.class).manageExternalBatterySetpoints(timeVariables);
    	}
    }
    
    
    
    
	//Get inherent, supported and active Asset management classes
	public List<Class<? extends I_AggregatorAssetManagement>> getInternalAggregatorAssetManagements(){
		return this.internalAggregatorAssetManagements;
	}
	public List<Class<? extends I_AggregatorAssetManagement>> getSupportedExternalAggregatorAssetManagements(){
		return this.supportedExternalAggregatorAssetManagements;
	}
	public Map<Class<? extends I_AggregatorAssetManagement>, I_AggregatorAssetManagement> getActiveExternalAggregatorAssetManagements(){
		return this.activeExternalAggregatorAssetManagements;
	}  
	
    ////Store and reset states
	public void storeStatesAndReset() {
		activeExternalAggregatorAssetManagements.values().forEach(subManagement -> subManagement.storeStatesAndReset());
	}
	public void restoreStates() {
		activeExternalAggregatorAssetManagements.values().forEach(subManagement -> subManagement.restoreStates());
	}
	
	@Override
	public String toString() {
		String outputString = "J_AggregatorEnergyManagementDefault: \n" + "Active internal aggregator managements: \n";
		for(Class<? extends I_AggregatorAssetManagement> internalAggregatorAssetManagement : this.internalAggregatorAssetManagements) {
			outputString += "-" + internalAggregatorAssetManagement + "\n";
		}
		outputString += "\n" + "Active external aggregator managements: \n";
		for(I_AggregatorAssetManagement activeExternalAggregatorAssetManagement : this.activeExternalAggregatorAssetManagements.values()) {
			outputString += "-" + activeExternalAggregatorAssetManagement.toString() + "\n";
		}		
		return outputString;
	}
}