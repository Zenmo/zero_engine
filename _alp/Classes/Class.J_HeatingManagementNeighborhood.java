/**
 * J_HeatingManagementNeighborhood
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_HeatingManagementNeighborhood implements I_HeatingManagement {

    private boolean isInitialized = false;
    private GridConnection gc;
	private List<OL_GridConnectionHeatingType> validHeatingTypes = Arrays.asList(
		OL_GridConnectionHeatingType.CUSTOM
	);
	private OL_GridConnectionHeatingType currentHeatingType;

    private J_EAConversionGasBurner gasBurner;
    private J_EAConversionHeatPump heatPump;
    private J_EAConversionHeatDeliverySet heatDeliverySet;
    private J_EAConversionHydrogenBurner hydrogenBurner;
    private J_EAConversionHeatPump lowTempHeatGridHeatPump;
  
	private J_HeatingPreferences heatingPreferences = null; // Not needed for neighbourhoods
	
    private HashMap<String, J_EAConsumption> heatDemandProfiles = new HashMap<String, J_EAConsumption>();

    private double thresholdCOP_hybridHeatpump = 3.5;

    // Services
    public double amountOfGasBurners_services_fr = 1;
    public double amountOfHybridHeatpump_services_fr = 0;
    public double amountOfElectricHeatpumps_services_fr = 0;
    public double amountOfDistrictHeating_services_fr = 0;
    public double amountOfLowTempHeatgrid_services_fr = 0;
     
    // Houses
    public double amountOfGasBurners_houses_fr = 1;
    public double amountOfHybridHeatpump_houses_fr = 0;
    public double amountOfElectricHeatpumps_houses_fr = 0;
    public double amountOfDistrictHeating_houses_fr = 0;
    public double amountOfLowTempHeatgrid_houses_fr = 0;
     
    // Industry
    public double amountOfGasBurners_industry_fr = 1;
    public double amountOfHybridHeatpump_industry_fr = 0;
    public double amountOfElectricHeatpumps_industry_fr = 0;
    public double amountOfDistrictHeating_industry_fr = 0;
    public double amountOfHydrogenUseForHeating_industry_fr = 0;
     
    // Agriculture
    public double amountOfGasBurners_agriculture_fr = 1;
    public double amountOfHybridHeatpump_agriculture_fr = 0;
    public double amountOfElectricHeatpumps_agriculture_fr = 0;
    public double amountOfDistrictHeating_agriculture_fr = 0;
    
    /**
     * Default constructor
     */
    public J_HeatingManagementNeighborhood() {
    	
    }
    
    public J_HeatingManagementNeighborhood( GridConnection gc, OL_GridConnectionHeatingType heatingType ) {
    	this.gc = gc;
    	this.currentHeatingType = heatingType;
    }
    
    public void manageHeating(J_TimeVariables timeVariables) {
    	if (!isInitialized) {
    		initializeAssets();
    	}

    	//Division of the power demand //{Gasburner power request, HP power request, DH power request, Hydrogenburner power request}
    	double powerDemandDivision_kW[] = this.dividePowerDemandHeatingAssets(); 

    	//Split the power fractions (powerDemandDivision[] = {Gasburner power request, HP power request, DH power request}
    	if(gasBurner.getOutputCapacity_kW() != 0){
    		double powerFraction_GASBURNER = powerDemandDivision_kW[0] / gasBurner.getOutputCapacity_kW();    		
    		//Gas burner control (always assigned to primary heating asset)	
    		gasBurner.f_updateAllFlows(powerFraction_GASBURNER, timeVariables);
    	}
    	if(heatPump.getOutputCapacity_kW() != 0){
    		double powerFraction_HEATPUMP  = powerDemandDivision_kW[1] / heatPump.getOutputCapacity_kW();    		
    		//Heatpump control (always assigned to secondary heating asset)
    		heatPump.f_updateAllFlows(powerFraction_HEATPUMP, timeVariables);
    	}
    	if(heatDeliverySet.getOutputCapacity_kW() != 0){
    		double powerFraction_HEATDELIVERYSET = powerDemandDivision_kW[2] / heatDeliverySet.getOutputCapacity_kW();    		
    		//Heat delivery set control (always assigned to tertiary heating asset)
    		heatDeliverySet.f_updateAllFlows(powerFraction_HEATDELIVERYSET, timeVariables);
    	}
    	if(hydrogenBurner.getOutputCapacity_kW() != 0){
    		double powerFraction_HYDROGENBURNER = powerDemandDivision_kW[3] / hydrogenBurner.getOutputCapacity_kW();
    		//Hydrogen burner(always assigned to quaternary heating asset)
    		hydrogenBurner.f_updateAllFlows(powerFraction_HYDROGENBURNER, timeVariables);
    	}
    	if(lowTempHeatGridHeatPump.getOutputCapacity_kW() != 0){
    		double powerFraction_LOWTEMPHEATGRID = powerDemandDivision_kW[4] / lowTempHeatGridHeatPump.getOutputCapacity_kW();
    		//Hydrogen burner(always assigned to quaternary heating asset)
    		lowTempHeatGridHeatPump.f_updateAllFlows(powerFraction_LOWTEMPHEATGRID, timeVariables);
    	}
    }
    
    public double[] dividePowerDemandHeatingAssets() {
    	//Initialize power demand division array
    	double powerDemandDivision_kW[] = {0, 0, 0, 0, 0}; // {Gasburner power request, HP power request, DH power request, Hydrogenburner power request, lowTempHeatgridPowerDemand}

    	//Calculate fraction of total heat demand delivered by the CHP
    	/*
    	double powerDemand_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);
    	double fractionOfTotalHeatDemandDeliveredyByCHP = max(0,p_chpAsset.getLastFlows().get(OL_EnergyCarriers.HEAT))/powerDemand_kW;
    	double remainingFraction = fractionOfTotalHeatDemandDeliveredyByCHP;
    	*/
    	//Demanded total heating power at the current time step
    	//double powerDemand_kW = fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.HEAT);

    	//Demanded heating power for companies and household seperatly the current time step
    	double powerDemand_households_kW = max(0,heatDemandProfiles.get("HOUSEHOLDS").getLastFlows().get(OL_EnergyCarriers.HEAT));
    	double powerDemand_agriculture_kW = max(0,heatDemandProfiles.get("AGRICULTURE").getLastFlows().get(OL_EnergyCarriers.HEAT));
    	double powerDemand_industry_kW = max(0,heatDemandProfiles.get("INDUSTRY").getLastFlows().get(OL_EnergyCarriers.HEAT));
    	double powerDemand_services_kW = max(0,heatDemandProfiles.get("SERVICES").getLastFlows().get(OL_EnergyCarriers.HEAT));

    	//Divide the powerdemand per heating type
    	double gasBurnerPowerDemand_kW 		= powerDemand_households_kW*amountOfGasBurners_houses_fr + 
    										  powerDemand_agriculture_kW*amountOfGasBurners_agriculture_fr +
    										  powerDemand_industry_kW*amountOfGasBurners_industry_fr + 
    										  powerDemand_services_kW*amountOfGasBurners_services_fr;
    										  
    	double electricHPPowerDemand_kW 		= powerDemand_households_kW*amountOfElectricHeatpumps_houses_fr + 
    										  powerDemand_agriculture_kW*amountOfElectricHeatpumps_agriculture_fr + 
    										  powerDemand_industry_kW*amountOfElectricHeatpumps_industry_fr + 
    										  powerDemand_services_kW*amountOfElectricHeatpumps_services_fr;
    										  
    	double hybridHPPowerDemand_kW 	   		= powerDemand_households_kW*amountOfHybridHeatpump_houses_fr +
    										  powerDemand_agriculture_kW*amountOfHybridHeatpump_agriculture_fr +
    										  powerDemand_industry_kW*amountOfHybridHeatpump_industry_fr + 
    										  powerDemand_services_kW*amountOfHybridHeatpump_services_fr;
    										  
    	double districtHeatingPowerDemand_kW   = powerDemand_households_kW*amountOfDistrictHeating_houses_fr +
    										  powerDemand_agriculture_kW*amountOfDistrictHeating_agriculture_fr + 
    										  powerDemand_industry_kW*amountOfDistrictHeating_industry_fr + 
    										  powerDemand_services_kW*amountOfDistrictHeating_services_fr;
    															  
    	double hydrogenBurnerPowerDemand_kW	= powerDemand_industry_kW*amountOfHydrogenUseForHeating_industry_fr;

    	double lowTempHeatgridPowerDemand_kW 	= powerDemand_households_kW*amountOfLowTempHeatgrid_houses_fr + 
    										  powerDemand_services_kW*amountOfLowTempHeatgrid_services_fr;
    	//double lowTempHeatgridPowerDemand_kW = (powerDemand_households_kW + powerDemand_agriculture_kW + powerDemand_industry_kW + powerDemand_services_kW) - hybridHPPowerDemand - electricHPPowerDemand - gasBurnerPowerDemand - districtHeatingPowerDemand - hydrogenBurnerPowerDemand; // To make sure all power demand is met
    										  
    	//Get the current Heatpump COP
    	double HP_COP = ((J_EAConversionHeatPump)heatPump).getCOP();

    	if ( HP_COP < thresholdCOP_hybridHeatpump ) { // switch to gasburner when HP COP is below treshold
    		powerDemandDivision_kW[0] = max(0, gasBurnerPowerDemand_kW + hybridHPPowerDemand_kW);
    		powerDemandDivision_kW[1] = max(0, electricHPPowerDemand_kW);
    	}
    	else{
    		powerDemandDivision_kW[0] = max(0, gasBurnerPowerDemand_kW);
    		powerDemandDivision_kW[1] = max(0, electricHPPowerDemand_kW + hybridHPPowerDemand_kW);
    	}
    	powerDemandDivision_kW[2] = max(0, districtHeatingPowerDemand_kW);
    	powerDemandDivision_kW[3] = max(0, hydrogenBurnerPowerDemand_kW);
    	powerDemandDivision_kW[4] = max(0, lowTempHeatgridPowerDemand_kW);

    	return powerDemandDivision_kW; //{Gasburner power request, HP power request, DH power request, Hydrogenburner power request, lowTempHeatgridPowerDemand};
    }
    
    public void initializeAssets() {
    	if (!validHeatingTypes.contains(this.currentHeatingType)) {
    		throw new RuntimeException(this.getClass() + " does not support heating type: " + this.currentHeatingType);
    	}
    	if (gc.p_heatBuffer != null) {
    		throw new RuntimeException(this.getClass() + " does not support heat buffers.");
    	}
    	if (gc.p_BuildingThermalAsset != null) {
    		throw new RuntimeException(this.getClass() + " does not support a building asset.");
    	}
    	if (gc.c_heatingAssets.size() != 5) {
    		throw new RuntimeException(this.getClass() + " requires exactly 5 heating assets");
    	}
    	if (heatDemandProfiles.size() != 4) {
    		throw new RuntimeException(this.getClass() + " requires exactly 4 heating profiles");
    	}
    	
    	gasBurner = null;
    	heatPump = null;
    	lowTempHeatGridHeatPump = null;
    	heatDeliverySet = null;
    	hydrogenBurner = null;
    	
    	for (J_EA heatingAsset : gc.c_heatingAssets) {
    		if (heatingAsset instanceof J_EAConversionGasBurner) {
    			if (gasBurner != null) {
    	    		throw new RuntimeException(this.getClass() + " does not support two gasburners");
    			}
    			gasBurner = (J_EAConversionGasBurner)heatingAsset;
    		}
    		else if (heatingAsset instanceof J_EAConversionHeatPump) {
    			if (((J_EAConversionHeatPump)heatingAsset).getAmbientTempType() == OL_AmbientTempType.AMBIENT_AIR) {
        			if (heatPump != null) {
        	    		throw new RuntimeException(this.getClass() + " does not support two ambient air heatpumps");
        			}
        			heatPump = (J_EAConversionHeatPump)heatingAsset;
    			}
    			else if (((J_EAConversionHeatPump)heatingAsset).getAmbientTempType() == OL_AmbientTempType.HEAT_GRID) {
        			if (lowTempHeatGridHeatPump != null) {
        	    		throw new RuntimeException(this.getClass() + " does not support two heat grid heatpumps");
        			}
        			lowTempHeatGridHeatPump = (J_EAConversionHeatPump)heatingAsset;
    			}
    			else {
    	    		throw new RuntimeException(this.getClass() + " does not support heatpumps with ambient type: " + ((J_EAConversionHeatPump)heatingAsset).getAmbientTempType());
    			}
    		}
    		else if (heatingAsset instanceof J_EAConversionHeatDeliverySet) {
    			if (heatDeliverySet != null) {
    	    		throw new RuntimeException(this.getClass() + " does not support two heat delivery sets");
    			}
    			heatDeliverySet = (J_EAConversionHeatDeliverySet)heatingAsset;
    		}
    		else if (heatingAsset instanceof J_EAConversionHydrogenBurner) {
    			if (hydrogenBurner != null) {
    	    		throw new RuntimeException(this.getClass() + " does not support two hydrogenburners");
    			}
    			hydrogenBurner = (J_EAConversionHydrogenBurner)heatingAsset;
    		}
    		else {
	    		throw new RuntimeException(this.getClass() + " does not support heating assets of type: " + heatingAsset.getClass());    			
    		}
    	}
    	isInitialized = true;
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
    
    public void setHeatingMethodPct_services( double[] pctArray ) {
    	amountOfGasBurners_services_fr = pctArray[0]/100;
    	amountOfElectricHeatpumps_services_fr = pctArray[1]/100;
    	amountOfHybridHeatpump_services_fr = pctArray[2]/100;
    	amountOfDistrictHeating_services_fr = pctArray[3]/100;
    	amountOfLowTempHeatgrid_services_fr = pctArray[4]/100;
    }
    
    public void setHeatingMethodPct_houses( double[] pctArray ) {
    	amountOfGasBurners_houses_fr = pctArray[0]/100;
    	amountOfElectricHeatpumps_houses_fr = pctArray[1]/100;
    	amountOfHybridHeatpump_houses_fr = pctArray[2]/100;
    	amountOfDistrictHeating_houses_fr = pctArray[3]/100;
    	amountOfLowTempHeatgrid_houses_fr = pctArray[4]/100;
    }
    
    public void setHeatingMethodPct_industry( double[] pctArray ) {
    	//Calculate actual space heating 
    	double actualHeatingDemandSpaceHeating_fr =  (1 - amountOfHydrogenUseForHeating_industry_fr);
    	amountOfGasBurners_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[0]/100;
    	amountOfElectricHeatpumps_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[1]/100;
    	amountOfHybridHeatpump_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[2]/100;
    	amountOfDistrictHeating_industry_fr = actualHeatingDemandSpaceHeating_fr * pctArray[3]/100;
    }
    
    public void setH2HeatingFr_industry( double amountOfHydrogenUseForHeating_fr ) {
    	//Get current values
    	if(amountOfHydrogenUseForHeating_fr >= 1){
    		//throw new RuntimeException("Can not replace all gas in industry with hydrogen! The model does not support this.");
    		amountOfHydrogenUseForHeating_fr = 0.999;
    	}
    	double actualHeatingDemandSpaceHeating_fr =  (1 - amountOfHydrogenUseForHeating_industry_fr);
    	double[] currentPctArray = {amountOfGasBurners_industry_fr*100/actualHeatingDemandSpaceHeating_fr, 
    								amountOfHybridHeatpump_industry_fr*100/actualHeatingDemandSpaceHeating_fr, 
    								amountOfElectricHeatpumps_industry_fr*100/actualHeatingDemandSpaceHeating_fr, 
    								amountOfDistrictHeating_industry_fr*100/actualHeatingDemandSpaceHeating_fr};

    	//Set new hydrogen use for heating fr
    	amountOfHydrogenUseForHeating_industry_fr = min(1, amountOfHydrogenUseForHeating_fr);


    	//Set new values
    	this.setHeatingMethodPct_industry(currentPctArray);
    }
    
    public void setHeatingMethodPct_agriculture( double[] pctArray ) {
    	amountOfGasBurners_agriculture_fr = pctArray[0]/100;
    	amountOfElectricHeatpumps_agriculture_fr = pctArray[1]/100;
    	amountOfHybridHeatpump_agriculture_fr = pctArray[2]/100;
    	amountOfDistrictHeating_agriculture_fr = pctArray[3]/100;
    }
    
    public void addHeatDemandProfile(String name, J_EAConsumption profile) {
    	heatDemandProfiles.put(name, profile);
    }
    
    public void setHeatingPreferences(J_HeatingPreferences heatingPreferences) {
    	this.heatingPreferences = heatingPreferences;
    }
    
    public J_HeatingPreferences getHeatingPreferences() {
    	return this.heatingPreferences;
    }
    
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    
    //Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store/reset
	}
	public void restoreStates() {
		//Nothing to store/reset
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