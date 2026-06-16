/**
 * J_FlexProfileManagementHeatprofileHeatpump
 */	
public class J_FlexProfileManagementHeatprofileHeatpump implements I_FlexProfileManagement{

	private GridConnection gc;
	private J_TimeParameters timeParameters;
	private double[] flexProfileSetpointArray_fr;
	private int currentFlexProfileSetpointArrayIndex;
	private double forecastingDuration_h = 24;
    /**
     * Empty constructor for serialization
     */
    public J_FlexProfileManagementHeatprofileHeatpump() {
    }
    
    /**
     * Default constructor
     */
    public J_FlexProfileManagementHeatprofileHeatpump(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    }
    
    public void manageFlexProfiles(J_TimeVariables timeVariables) {
    	if(timeVariables.getT_h() % forecastingDuration_h == 0) {
    		createNewFlexProfileSetpointArray(timeVariables);
    	}
    	
    	List<J_EAFlexProfile> flexibleHeatProfiles = findAll(gc.c_flexProfileAssets, flexProfile -> flexProfile.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	
    	gc.f_updateFlexAssetFlows(flexibleHeatProfiles.get(0), flexProfileSetpointArray_fr[currentFlexProfileSetpointArrayIndex], timeVariables);
    }
    
    
    
    private void createNewFlexProfileSetpointArray(J_TimeVariables timeVariables) {
    	int numberOfTimeSteps = roundToInt(forecastingDuration_h/timeParameters.getTimeStep_h());
    	
    	//Get fixed nettobalance forecast
    	double[] nettoBalanceForecastElectricity_kW = gc.f_getFixedAssetForecast(timeVariables.getT_h(), timeVariables.getT_h() + forecastingDuration_h, OL_EnergyCarriers.ELECTRICITY, timeParameters);
    	
    	//Get flexprofile netto Heat balance forecast
    	double[] originalFlexprofileNettoHeatBalance_kW = new double[numberOfTimeSteps];
    	List<J_EAFlexProfile> flexibleHeatProfiles = findAll(gc.c_flexProfileAssets, flexProfile -> flexProfile.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	
    	if(flexibleHeatProfiles.size() == 0 || flexibleHeatProfiles.size() > 1 || gc.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP || gc.f_getHeatingTypeIsGhost()) {
    		throw new RuntimeException("Flex profile management heatprofile heatpump active with multiple, or without a heat flex profile or (non ghost) heatpump being present!");
    	}
    	
    	originalFlexprofileNettoHeatBalance_kW = LUXMath.addArrays(originalFlexprofileNettoHeatBalance_kW, flexibleHeatProfiles.get(0).getDefaultForecast_kW(timeVariables.getT_h(), timeVariables.getT_h() + forecastingDuration_h));
    	
    	//Convert flexprofile to (forecasted) electricity profile
		double[] originalFlexprofileNettoElectricityBalance_kW = new double[numberOfTimeSteps];
		
		//Calculate the actual power the heating asset would use (by calculating the efficiency) and add to nettoBalance_kW
		J_EAConversionHeatPump heatPump = (J_EAConversionHeatPump)gc.c_heatingAssets.get(0);
		double[] invCOP = new double[numberOfTimeSteps];
		//Todo -> Make work for other ambient temp types!
		J_ProfilePointer ambientTemperatures = gc.energyModel.pp_ambientTemperature_degC;
		for (int i = 0; i < numberOfTimeSteps; i++) {
			invCOP[i] = 1.0/heatPump.calculateCOP(heatPump.getOutputTemperature_degC(), ambientTemperatures.getValue(timeVariables.getT_h() + i * timeParameters.getTimeStep_h()));
		}
		originalFlexprofileNettoElectricityBalance_kW = LUXMath.multiplyArrays(originalFlexprofileNettoHeatBalance_kW, invCOP);
		
		
		//Calculate total
		double[] totalElectricityBalanceForecast_kW = LUXMath.addArrays(nettoBalanceForecastElectricity_kW, originalFlexprofileNettoElectricityBalance_kW);
    	
		////Determine power fraction of flex profile, and store!
		double[] newFlexProfileSetpointArray = new double[numberOfTimeSteps];
		
		
		//HIER BEN JE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
		
		//Store new array
		this.currentFlexProfileSetpointArrayIndex = 0;
		this.flexProfileSetpointArray_fr = newFlexProfileSetpointArray;
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
		List<J_EAFlexProfile> flexProfiles = gc.c_flexProfileAssets;
		StringBuilder flexProfilesString = new StringBuilder();
		for(J_EAFlexProfile flexProfile : flexProfiles) {
			flexProfilesString.append(System.lineSeparator());
			flexProfilesString.append(flexProfile.toString());
		}
		
		return "J_FlexProfileManagementHeatprofileHeatpump: " + System.lineSeparator() +
				"Currently controlling J_EAFlexProfiles: " + 
				flexProfilesString.toString();
	}
}