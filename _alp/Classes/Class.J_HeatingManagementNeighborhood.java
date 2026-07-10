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
    private J_TimeParameters timeParameters;
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
	
	private Map<OL_Sectors, Map<OL_GridConnectionHeatingType, I_ProfileAsset>> heatDemandProfiles = Map.of(
																									OL_Sectors.HOUSEHOLDS, new HashMap<>(),
																									OL_Sectors.SERVICES, new HashMap<>(),
																									OL_Sectors.INDUSTRY, new HashMap<>(),
																									OL_Sectors.AGRICULTURE, new HashMap<>()
																									);
    private double thresholdCOP_hybridHeatpump = 3.5;

    private Map<OL_Sectors, Double> heatSavings_fr = new HashMap<>(Map.of(
											    		OL_Sectors.HOUSEHOLDS,  0.0,
											    		OL_Sectors.SERVICES,    0.0,
											    		OL_Sectors.INDUSTRY,    0.0,
											    		OL_Sectors.AGRICULTURE, 0.0
    ));
    
    private static final Map<OL_Sectors, List<OL_GridConnectionHeatingType>> SPACE_HEATING_TYPES = Map.of(
    		OL_Sectors.HOUSEHOLDS, List.of(
                    OL_GridConnectionHeatingType.GAS_BURNER,
                    OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP,
                    OL_GridConnectionHeatingType.HYBRID_HEATPUMP,
                    OL_GridConnectionHeatingType.DISTRICTHEAT,
                    OL_GridConnectionHeatingType.LT_DISTRICTHEAT),
    		OL_Sectors.SERVICES, List.of(
                    OL_GridConnectionHeatingType.GAS_BURNER,
                    OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP,
                    OL_GridConnectionHeatingType.HYBRID_HEATPUMP,
                    OL_GridConnectionHeatingType.DISTRICTHEAT,
                    OL_GridConnectionHeatingType.LT_DISTRICTHEAT),
    		OL_Sectors.INDUSTRY, List.of(
                    OL_GridConnectionHeatingType.GAS_BURNER,
                    OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP,
                    OL_GridConnectionHeatingType.HYBRID_HEATPUMP,
                    OL_GridConnectionHeatingType.DISTRICTHEAT),
    		OL_Sectors.AGRICULTURE, List.of(
                    OL_GridConnectionHeatingType.GAS_BURNER,
                    OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP,
                    OL_GridConnectionHeatingType.HYBRID_HEATPUMP,
                    OL_GridConnectionHeatingType.DISTRICTHEAT)
    );
    
    public double amountOfHydrogenUseForHeating_industry_fr = 0;

    //Specific Rapid run KPI's
    double totalHouseholdElectricityForHeatingConsumption_kWh = 0;
    double totalHouseholdMethaneForHeatingConsumption_kWh = 0;
    double totalHouseholdDistrictHeatingImport_kWh = 0;
    double totalAgricultreEnergyForHeating_kWh = 0;
    double totalIndustryEnergyForHeating_kWh = 0;
    double totalServicesEnergyForHeating_kWh = 0;

    /**
     * Default constructor
     */
    public J_HeatingManagementNeighborhood() {
    	
    }
    
    public J_HeatingManagementNeighborhood( GridConnection gc, J_TimeParameters timeParameters, OL_GridConnectionHeatingType heatingType ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
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
        	gc.f_updateFlexAssetFlows(gasBurner, powerFraction_GASBURNER, timeVariables);
    	}
    	if(heatPump.getOutputCapacity_kW() != 0){
    		double powerFraction_HEATPUMP  = powerDemandDivision_kW[1] / heatPump.getOutputCapacity_kW();    		
    		//Heatpump control (always assigned to secondary heating asset)
        	gc.f_updateFlexAssetFlows(heatPump, powerFraction_HEATPUMP, timeVariables);

    	}
    	if(heatDeliverySet.getOutputCapacity_kW() != 0){
    		double powerFraction_HEATDELIVERYSET = powerDemandDivision_kW[2] / heatDeliverySet.getOutputCapacity_kW();    		
    		//Heat delivery set control (always assigned to tertiary heating asset)
        	gc.f_updateFlexAssetFlows(heatDeliverySet, powerFraction_HEATDELIVERYSET, timeVariables);
    	}
    	if(hydrogenBurner.getOutputCapacity_kW() != 0){
    		double powerFraction_HYDROGENBURNER = powerDemandDivision_kW[3] / hydrogenBurner.getOutputCapacity_kW();
    		//Hydrogen burner(always assigned to quaternary heating asset)
        	gc.f_updateFlexAssetFlows(hydrogenBurner, powerFraction_HYDROGENBURNER, timeVariables);
    	}
    	if(lowTempHeatGridHeatPump.getOutputCapacity_kW() != 0){
    		double powerFraction_LOWTEMPHEATGRID = powerDemandDivision_kW[4] / lowTempHeatGridHeatPump.getOutputCapacity_kW();
    		//Hydrogen burner(always assigned to quaternary heating asset)
        	gc.f_updateFlexAssetFlows(lowTempHeatGridHeatPump, powerFraction_LOWTEMPHEATGRID, timeVariables);
    	}
    }
 
    private double[] dividePowerDemandHeatingAssets() {
    	
    	//Gasburners
    	double gasBurnerPowerDemand_houses_kW      = getCurrentHeatDemand_kW(OL_Sectors.HOUSEHOLDS,  OL_GridConnectionHeatingType.GAS_BURNER);
    	double gasBurnerPowerDemand_agriculture_kW = getCurrentHeatDemand_kW(OL_Sectors.AGRICULTURE, OL_GridConnectionHeatingType.GAS_BURNER);
    	double gasBurnerPowerDemand_industry_kW    = getCurrentHeatDemand_kW(OL_Sectors.INDUSTRY,    OL_GridConnectionHeatingType.GAS_BURNER);
    	double gasBurnerPowerDemand_services_kW    = getCurrentHeatDemand_kW(OL_Sectors.SERVICES,    OL_GridConnectionHeatingType.GAS_BURNER);
 
    	double gasBurnerPowerDemand_kW 		= gasBurnerPowerDemand_houses_kW + 
											  gasBurnerPowerDemand_agriculture_kW +
											  gasBurnerPowerDemand_industry_kW + 
											  gasBurnerPowerDemand_services_kW;
    	
    	//Electric heat pumps
    	double electricHPPowerDemand_houses_kW      = getCurrentHeatDemand_kW(OL_Sectors.HOUSEHOLDS,  OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
    	double electricHPPowerDemand_agriculture_kW = getCurrentHeatDemand_kW(OL_Sectors.AGRICULTURE, OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
    	double electricHPPowerDemand_industry_kW    = getCurrentHeatDemand_kW(OL_Sectors.INDUSTRY,    OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
    	double electricHPPowerDemand_services_kW    = getCurrentHeatDemand_kW(OL_Sectors.SERVICES,    OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP);
 
    	double electricHPPowerDemand_kW      = electricHPPowerDemand_houses_kW +
    										   electricHPPowerDemand_agriculture_kW +
    										   electricHPPowerDemand_industry_kW +
    										   electricHPPowerDemand_services_kW;
 
    	//Hybrid heat pumps
    	double hybridHPPowerDemand_houses_kW      = getCurrentHeatDemand_kW(OL_Sectors.HOUSEHOLDS,  OL_GridConnectionHeatingType.HYBRID_HEATPUMP);
    	double hybridHPPowerDemand_agriculture_kW = getCurrentHeatDemand_kW(OL_Sectors.AGRICULTURE, OL_GridConnectionHeatingType.HYBRID_HEATPUMP);
    	double hybridHPPowerDemand_industry_kW    = getCurrentHeatDemand_kW(OL_Sectors.INDUSTRY,    OL_GridConnectionHeatingType.HYBRID_HEATPUMP);
    	double hybridHPPowerDemand_services_kW    = getCurrentHeatDemand_kW(OL_Sectors.SERVICES,    OL_GridConnectionHeatingType.HYBRID_HEATPUMP);
 
    	double hybridHPPowerDemand_kW      = hybridHPPowerDemand_houses_kW +
    										 hybridHPPowerDemand_agriculture_kW +
    										 hybridHPPowerDemand_industry_kW +
    										 hybridHPPowerDemand_services_kW;
 
 
    	//District heating
    	double districtHeatingPowerDemand_houses_kW      = getCurrentHeatDemand_kW(OL_Sectors.HOUSEHOLDS,  OL_GridConnectionHeatingType.DISTRICTHEAT);
    	double districtHeatingPowerDemand_agriculture_kW = getCurrentHeatDemand_kW(OL_Sectors.AGRICULTURE, OL_GridConnectionHeatingType.DISTRICTHEAT);
    	double districtHeatingPowerDemand_industry_kW    = getCurrentHeatDemand_kW(OL_Sectors.INDUSTRY,    OL_GridConnectionHeatingType.DISTRICTHEAT);
    	double districtHeatingPowerDemand_services_kW    = getCurrentHeatDemand_kW(OL_Sectors.SERVICES,    OL_GridConnectionHeatingType.DISTRICTHEAT);
 
    	double districtHeatingPowerDemand_kW      = districtHeatingPowerDemand_houses_kW +
    										        districtHeatingPowerDemand_agriculture_kW +
    										        districtHeatingPowerDemand_industry_kW +
    										        districtHeatingPowerDemand_services_kW;
    	
    	//Hydrogen burner												  
    	double hydrogenBurnerPowerDemand_kW	= getCurrentHeatDemand_kW(OL_Sectors.INDUSTRY, OL_GridConnectionHeatingType.HYDROGENBURNER);
    	
    	//Low temp heat grid
    	double lowTempHeatgridPowerDemand_houses_kW   = getCurrentHeatDemand_kW(OL_Sectors.HOUSEHOLDS, OL_GridConnectionHeatingType.LT_DISTRICTHEAT);
    	double lowTempHeatgridPowerDemand_services_kW = getCurrentHeatDemand_kW(OL_Sectors.SERVICES,   OL_GridConnectionHeatingType.LT_DISTRICTHEAT);
 
    	double lowTempHeatgridPowerDemand_kW      = lowTempHeatgridPowerDemand_houses_kW +
    										        lowTempHeatgridPowerDemand_services_kW;
 
    	
    	
    	////Create asset power demand division array based on COP
    	//Initialize power demand division array
    	double powerDemandDivision_kW[] = {0, 0, 0, 0, 0}; // {Gasburner power request, HP power request, DH power request, Hydrogenburner power request, lowTempHeatgridPowerDemand}
 
    	//Get the current Heatpump COP
    	double HP_COP = heatPump.getCOP();
    	boolean hybridHeatpumpInGasBurnerMode = HP_COP < thresholdCOP_hybridHeatpump;
    	
    	//Assign power demand division to right indexes
      	powerDemandDivision_kW[0] = gasBurnerPowerDemand_kW  + (hybridHeatpumpInGasBurnerMode ? hybridHPPowerDemand_kW : 0); //Gasburner
    	powerDemandDivision_kW[1] = electricHPPowerDemand_kW + (hybridHeatpumpInGasBurnerMode ? 0 : hybridHPPowerDemand_kW); //Heatpump
    	powerDemandDivision_kW[2] = districtHeatingPowerDemand_kW; //Districtheating
    	powerDemandDivision_kW[3] = hydrogenBurnerPowerDemand_kW;  //Hydrogenburner
    	powerDemandDivision_kW[4] = lowTempHeatgridPowerDemand_kW; //lowTempHeatgrid
    	
    	
    	////Rapid run KPI storing
    	if(gc.energyModel.v_isRapidRun) {
    		
    		//Gas burner and heatpump energy consumption
        	if ( hybridHeatpumpInGasBurnerMode ) { // switch to gasburner when HP COP is below treshold
        	    totalHouseholdElectricityForHeatingConsumption_kWh += (electricHPPowerDemand_houses_kW/HP_COP) * timeParameters.getTimeStep_h();
        	    totalHouseholdMethaneForHeatingConsumption_kWh += ((gasBurnerPowerDemand_houses_kW + hybridHPPowerDemand_houses_kW) / gasBurner.getEta_r()) * timeParameters.getTimeStep_h();
 
        	    totalAgricultreEnergyForHeating_kWh += ((electricHPPowerDemand_agriculture_kW/HP_COP) + ((gasBurnerPowerDemand_agriculture_kW + hybridHPPowerDemand_agriculture_kW) / gasBurner.getEta_r())) * timeParameters.getTimeStep_h();
                totalIndustryEnergyForHeating_kWh += ((electricHPPowerDemand_industry_kW/HP_COP) + ((gasBurnerPowerDemand_industry_kW + hybridHPPowerDemand_industry_kW) / gasBurner.getEta_r())) * timeParameters.getTimeStep_h();
                totalServicesEnergyForHeating_kWh += ((electricHPPowerDemand_services_kW/HP_COP) + ((gasBurnerPowerDemand_services_kW + hybridHPPowerDemand_services_kW) / gasBurner.getEta_r())) * timeParameters.getTimeStep_h();
        	}
        	else{
        	    totalHouseholdElectricityForHeatingConsumption_kWh += ((electricHPPowerDemand_houses_kW + hybridHPPowerDemand_houses_kW)/HP_COP) * timeParameters.getTimeStep_h();
        	    totalHouseholdMethaneForHeatingConsumption_kWh += (gasBurnerPowerDemand_houses_kW / gasBurner.getEta_r()) * timeParameters.getTimeStep_h();
        	
        	    totalAgricultreEnergyForHeating_kWh += (((electricHPPowerDemand_agriculture_kW + hybridHPPowerDemand_agriculture_kW)/HP_COP) + (gasBurnerPowerDemand_agriculture_kW / gasBurner.getEta_r())) * timeParameters.getTimeStep_h();
	    		totalIndustryEnergyForHeating_kWh += (((electricHPPowerDemand_industry_kW + hybridHPPowerDemand_industry_kW)/HP_COP) + (gasBurnerPowerDemand_industry_kW / gasBurner.getEta_r())) * timeParameters.getTimeStep_h();
	    		totalServicesEnergyForHeating_kWh += (((electricHPPowerDemand_services_kW + hybridHPPowerDemand_services_kW)/HP_COP) + (gasBurnerPowerDemand_services_kW / gasBurner.getEta_r())) * timeParameters.getTimeStep_h();
        	}
    		
        	//District heating
        	totalHouseholdDistrictHeatingImport_kWh += (districtHeatingPowerDemand_houses_kW/heatDeliverySet.getEta_r()) * timeParameters.getTimeStep_h();
        	totalAgricultreEnergyForHeating_kWh += (districtHeatingPowerDemand_agriculture_kW/heatDeliverySet.getEta_r()) * timeParameters.getTimeStep_h();
			totalIndustryEnergyForHeating_kWh += (districtHeatingPowerDemand_industry_kW/heatDeliverySet.getEta_r()) * timeParameters.getTimeStep_h();
			totalServicesEnergyForHeating_kWh += (districtHeatingPowerDemand_services_kW/heatDeliverySet.getEta_r()) * timeParameters.getTimeStep_h();
    		
			//Hydrogen
			totalIndustryEnergyForHeating_kWh += (hydrogenBurnerPowerDemand_kW/hydrogenBurner.getEta_r()) * timeParameters.getTimeStep_h();
			
			//Low temp heatgrid
			double householdLTHeatpumpElectricityPower_kW = lowTempHeatgridPowerDemand_houses_kW/lowTempHeatGridHeatPump.getCOP();
			totalHouseholdElectricityForHeatingConsumption_kWh += householdLTHeatpumpElectricityPower_kW * timeParameters.getTimeStep_h();
            totalServicesEnergyForHeating_kWh += lowTempHeatgridPowerDemand_services_kW * timeParameters.getTimeStep_h();
            totalHouseholdDistrictHeatingImport_kWh += (lowTempHeatgridPowerDemand_houses_kW - householdLTHeatpumpElectricityPower_kW) * timeParameters.getTimeStep_h();
    	}
    	
    	return powerDemandDivision_kW; //{Gasburner power request, HP power request, DH power request, Hydrogenburner power request, lowTempHeatgridPowerDemand};
    }
    
	    private double getCurrentHeatDemand_kW(OL_Sectors sector, OL_GridConnectionHeatingType type) {
	        return max(0, heatDemandProfiles.get(sector).get(type).getLastFlows().get(OL_EnergyCarriers.HEAT));
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
    
    public List<OL_GridConnectionHeatingType> getHeatAssetTypes(OL_Sectors sector) {
        List<OL_GridConnectionHeatingType> types = new ArrayList<>(SPACE_HEATING_TYPES.get(sector));
        if (sector == OL_Sectors.INDUSTRY) {
            types.add(OL_GridConnectionHeatingType.HYDROGENBURNER);
        }
        return types;
    }
    
    public void setHybridHeatpumpCOPThreshold(double COPThreshold) {
    	this.thresholdCOP_hybridHeatpump = COPThreshold;
    }
    
	// =========================================================
	// Helpers — raw values as stored in heatDemandProfiles
	// =========================================================
    private double getProfileScaling_fr(OL_Sectors sector, OL_GridConnectionHeatingType type) {
        Map<OL_GridConnectionHeatingType, I_ProfileAsset> sectorMap = heatDemandProfiles.get(sector);
        if (sectorMap == null) {
            throw new IllegalStateException("No heat demand profiles found for sector: " + sector);
        }
        I_ProfileAsset asset = sectorMap.get(type);
        if (asset == null) {
            throw new IllegalStateException("No heat demand profile found for sector " + sector + ", type " + type);
        }
        return asset.getProfileScaling_fr();
    }
     
    private void setProfileScaling_fr(OL_Sectors sector, OL_GridConnectionHeatingType type, double value) {
        Map<OL_GridConnectionHeatingType, I_ProfileAsset> sectorMap = heatDemandProfiles.get(sector);
        if (sectorMap == null) {
            throw new IllegalStateException("No heat demand profiles found for sector: " + sector);
        }
        I_ProfileAsset asset = sectorMap.get(type);
        if (asset == null) {
            throw new IllegalStateException("No heat demand profile found for sector " + sector + ", type " + type);
        }
        asset.setProfileScaling_fr(value);
    }
     
    private List<OL_GridConnectionHeatingType> getSpaceHeatingTypes(OL_Sectors sector) {
        List<OL_GridConnectionHeatingType> types = SPACE_HEATING_TYPES.get(sector);
        if (types == null) {
            throw new IllegalStateException("Unknown sector: " + sector);
        }
        return types;
    }
     
    //The demand pool over which the space-heating technologies are split.
    //Industry reserves its hydrogen off total demand first, the other sectors reserve nothing.
    private double getSpaceHeatingPool(OL_Sectors sector) {
        double demand = 1 - getHeatSavings_fr(sector);
        return sector == OL_Sectors.INDUSTRY
                ? demand - getProfileScaling_fr(OL_Sectors.INDUSTRY, OL_GridConnectionHeatingType.HYDROGENBURNER)
                : demand;
    }
     
    //Current space-heating shares of a sector, expressed as percentages of its pool.
    private Map<OL_GridConnectionHeatingType, Double> captureHeatingMethodPct(OL_Sectors sector) {
        Map<OL_GridConnectionHeatingType, Double> pctMap = new HashMap<>();
        for (OL_GridConnectionHeatingType type : getSpaceHeatingTypes(sector)) {
            pctMap.put(type, getHeatingMethodShare_fr(sector, type) * 100);
        }
        return pctMap;
    }
     
     
    // ---------------------------------------------------------
    // Generic sector accessors
    // ---------------------------------------------------------
    public double getHeatSavings_fr(OL_Sectors sector) {
        Double savings = heatSavings_fr.get(sector);
        if (savings == null) {
            throw new IllegalStateException("Unknown sector: " + sector);
        }
        return savings;
    }
     
    /** Share of the sector's space-heating pool — independent of savings, and of industry's hydrogen share. */
    public double getHeatingMethodShare_fr(OL_Sectors sector, OL_GridConnectionHeatingType type) {
        double spaceHeatingPool = getSpaceHeatingPool(sector);
        return spaceHeatingPool > 0 ? getProfileScaling_fr(sector, type) / spaceHeatingPool : 0;
    }
     
    public void setHeatingMethodPct(OL_Sectors sector, Map<OL_GridConnectionHeatingType, Double> pctMap) {
        double spaceHeatingPool = getSpaceHeatingPool(sector);
        for (OL_GridConnectionHeatingType type : getSpaceHeatingTypes(sector)) {
            Double pct = pctMap.get(type);
            if (pct == null) {
                throw new IllegalArgumentException("Missing percentage for sector " + sector + ", type " + type);
            }
            setProfileScaling_fr(sector, type, spaceHeatingPool * pct / 100);
        }
    }
     
    public void setHeatSavings_fr(OL_Sectors sector, double newSavings_fr) {
        if (newSavings_fr >= 1) {
            throw new RuntimeException("Can not save (over) 100%!!");
        }
        
        // Capture the shares of the OLD pool before anything moves
        Map<OL_GridConnectionHeatingType, Double> pctMap = captureHeatingMethodPct(sector);
     
        // Industry only: keep hydrogen at the same fraction of total demand.
        if (sector == OL_Sectors.INDUSTRY) {
            double h2FractionOfDemand = getAmountOfHydrogenUseForHeating_industry_fr();
            setProfileScaling_fr(OL_Sectors.INDUSTRY, OL_GridConnectionHeatingType.HYDROGENBURNER,
                    h2FractionOfDemand * (1 - newSavings_fr));
        }
     
        heatSavings_fr.put(sector, newSavings_fr);
     
        setHeatingMethodPct(sector, pctMap);
    }
     
     
    // ---------------------------------------------------------
    // Industry hydrogen — share of TOTAL demand, not of the space-heating pool
    // ---------------------------------------------------------
    public double getAmountOfHydrogenUseForHeating_industry_fr() {
        double demand = 1 - getHeatSavings_fr(OL_Sectors.INDUSTRY);
        return demand > 0
                ? getProfileScaling_fr(OL_Sectors.INDUSTRY, OL_GridConnectionHeatingType.HYDROGENBURNER) / demand
                : 0;
    }
     
    public void setH2HeatingFr_industry(double fractionOfDemand) {
        if (fractionOfDemand >= 1) {
            throw new RuntimeException("Can not replace all gas in industry with hydrogen! The model does not support this.");
        }
        Map<OL_GridConnectionHeatingType, Double> pctMap = captureHeatingMethodPct(OL_Sectors.INDUSTRY);
     
        setProfileScaling_fr(OL_Sectors.INDUSTRY, OL_GridConnectionHeatingType.HYDROGENBURNER,
                (1 - getHeatSavings_fr(OL_Sectors.INDUSTRY)) * fractionOfDemand);
     
        setHeatingMethodPct(OL_Sectors.INDUSTRY, pctMap);
    }
    
    //Get/add profiles
    public void addHeatDemandProfile(OL_Sectors sector, OL_GridConnectionHeatingType heatingType, I_ProfileAsset profile) {
    	heatDemandProfiles.get(sector).put(heatingType, profile);
    }
    
    public void setHeatingPreferences(J_HeatingPreferences heatingPreferences) {
    	this.heatingPreferences = heatingPreferences;
    }
    
    //Heating preferences
    public J_HeatingPreferences getHeatingPreferences() {
    	return this.heatingPreferences;
    }
    
    public I_ProfileAsset getHeatDemandProfile(OL_Sectors sector, OL_GridConnectionHeatingType heatingType) {
    	return heatDemandProfiles.get(sector).get(heatingType);
    }
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    //Specific KPI getters
    public double getTotalHouseholdElectricityForHeatingConsumption_kWh() {
        return totalHouseholdElectricityForHeatingConsumption_kWh;
    }
    public double getTotalHouseholdMethaneForHeatingConsumption_kWh() {
        return totalHouseholdMethaneForHeatingConsumption_kWh;
    }
    public double getTotalHouseholdDistrictHeatingImport_kWh() {
        return totalHouseholdDistrictHeatingImport_kWh;
    }
    public double getTotalAgricultreEnergyForHeating_kWh() {
        return totalAgricultreEnergyForHeating_kWh;
    }
    public double getTotalIndustryEnergyForHeating_kWh() {
        return totalIndustryEnergyForHeating_kWh;
    }
    public double getTotalServicesEnergyForHeating_kWh() {
        return totalServicesEnergyForHeating_kWh;
    }
    
    
    //Store and reset states
	public void storeStatesAndReset() {
	    totalHouseholdElectricityForHeatingConsumption_kWh = 0;
	    totalHouseholdMethaneForHeatingConsumption_kWh = 0;
	    totalHouseholdDistrictHeatingImport_kWh = 0;
	    totalAgricultreEnergyForHeating_kWh = 0;
	    totalIndustryEnergyForHeating_kWh = 0;
	    totalServicesEnergyForHeating_kWh = 0;
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
}