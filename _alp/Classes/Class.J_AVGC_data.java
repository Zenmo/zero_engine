/**
 * J_AVGC_data
 */	
public class J_AVGC_data implements Serializable {
	
	////Averages
	
	//Houses
	public double p_avgHouseGasConsumption_m3_yr;
	public double p_maxAvgHouseGasConsumptionOffset_m3pa;
	public double p_avgHouseElectricityConsumption_kWh_yr;
	public double p_maxAvgHouseElectricityConsumptionOffset_kWhpa;
	public double p_avgHousePVInstallationPower_kWp;
	public double p_avgHouseConnectionCapacity_kW;
	public OL_GridConnectionHeatingType p_avgHouseHeatingMethod;
	public double p_avgNrOfCarsPerHouse;
	public double p_ratioEVHousePersonalCars;
	public double p_ratioHouseInstalledPV;
	public double p_avgRatioHouseBatteryStorageCapacity_v_PVPower;
	public double p_avgSpaceHeatingTotalGasConsumptionShare_fr;
	public double p_avgDHWTotalGasConsumptionShare_fr;
	public double p_avgCookingTotalGasConsumptionShare_fr;
	
	//Utilities
	public OL_GridConnectionHeatingType p_avgUtilityHeatingMethod;
	public double p_avgUtilityConnectionCapacity_kW;
	public double p_avgUtilityPVPower_kWp;
	public double p_ratioElectricTrucks;
	public OL_GridConnectionHeatingType p_avgCompanyHeatingMethod;
	public double p_avgCompanyElectricityConsumption_kWhpm2;
	public double p_avgCompanyGasConsumption_m3pm2;
	public double p_avgCompanyHeatConsumption_kWhpm2;

	
	//Energy assets
	public double p_avgCOPHeatpump;
	public double p_avgEfficiencyHeatpump_fr;
	public double p_avgOutputTemperatureElectricHeatpump_degC;
	public double p_avgOutputTemperatureHybridHeatpump_degC;
	public double p_minHeatpumpElectricCapacity_kW;

	public double p_avgEfficiencyGasBurner_fr;
	public double p_avgOutputTemperatureGasBurner_degC;
	public double p_minGasBurnerOutputCapacity_kW;
	
	public double p_avgEfficiencyHydrogenBurner_fr;
	public double p_avgOutputTemperatureHydrogenBurner_degC;
	public double p_minHydrogenBurnerOutputCapacity_kW;

	public double p_avgEfficiencyDistrictHeatingDeliverySet_fr;
	public double p_avgOutputTemperatureDistrictHeatingDeliverySet_degC;
	public double p_minDistrictHeatingDeliverySetOutputCapacity_kW;

	
	public double p_avgPVPower_kWpm2;
	public double p_avgAnnualProductionPV_kWhpWp;
	public double p_avgRatioRoofPotentialPV;

	public double p_avgSolarFieldPower_kWppha;
	
	public double p_avgEfficiencyCHP_thermal_fr;
	public double p_avgEfficiencyCHP_electric_fr;
	public double p_avgOutputTemperatureCHP_degC;

	public double p_avgPTPower_kWpm2;
	public double p_avgPTPanelSize_m2;
	
	public double p_avgMaxHeatBufferTemperature_degC;
	public double p_avgMinHeatBufferTemperature_degC;
	public double p_avgHeatBufferWaterVolumePerPTSurface_m3pm2;
	public double p_avgHeatBufferWaterVolumePerHPPower_m3pkW;
	
	public double p_avgRatioBatteryCapacity_v_Power;
	
	//Thermal model parametes
	public double p_PBL_HeatingLossFactor_fr;
	public Map<OL_GridConnectionInsulationLabel, Double> map_insulationLabel_lossfactorPerFloorSurface_WpKm2;
	public double p_solarAbsorptionFloorSurfaceScalingFactor_fr;
	public Map<OL_GridConnectionInsulationLabel, Double> map_insulationLabel_cooldownPeriod_hr;
	public double p_heatCapacitySizingConstant_JpK;
	public double p_heatCapacitySizingSlope_JpKm2;
	public double p_heatCapacitySizingFactor_fr;
	public double p_buildingDefaultCooldownTimescale_hr;

	//Vehicles
	public double p_avgEVMaxChargePowerCar_kW;
	public double p_avgEVMaxChargePowerVan_kW;
	public double p_avgEVMaxChargePowerTruck_kW;
	public double p_avgEVStorageCar_kWh;
	public double p_avgEVStorageVan_kWh;
	public double p_avgEVStorageTruck_kWh;

	public double p_avgEVEnergyConsumptionCar_kWhpkm;
	public double p_avgEVEnergyConsumptionVan_kWhpkm;
	public double p_avgEVEnergyConsumptionTruck_kWhpkm;	
	public double p_avgDieselConsumptionCar_kmpl;
	public double p_avgGasolineConsumptionCar_kmpl;	
	public double p_avgDieselConsumptionCar_kWhpkm;
	public double p_avgGasolineConsumptionCar_kWhpkm;
	public double p_avgDieselConsumptionVan_kmpl;
	public double p_avgDieselConsumptionVan_kWhpkm;
	public double p_avgDieselConsumptionTruck_kmpl;
	public double p_avgDieselConsumptionTruck_kWhpkm;
	public double p_avgHydrogenConsumptionCar_kWhpkm;
	public double p_avgHydrogenConsumptionVan_kWhpkm;
	public double p_avgHydrogenConsumptionTruck_kWhpkm;
	
	public double p_avgVehiclesPerCharger_Chargepoint;
	public double p_avgVehiclesPerCharger_Centre;
	public double p_v1gProbability;
	public double p_v2gProbability;
	
	public double p_avgAnnualTravelDistancePrivateCar_km;
	public double p_avgAnnualTravelDistanceCompanyCar_km;
	public double p_avgAnnualTravelDistanceVan_km;
	public double p_avgAnnualTravelDistanceTruck_km;
	public double p_minAnnualTravelDistanceSurveyVehicle_km;
	

	
	////Constants
	public double p_gas_kWhpm3;
	public double p_diesel_kWhpl;
	public double p_gasoline_kWhpl;
	public double p_waterHeatCapacity_JpkgK;
	public double p_waterDensity_kgpm3;
	
	public double p_hydrogenEnergyDensity_kWh_Nm3;
	public double p_hydrogenSpecificEnergy_kWh_kg;
	public double p_hydrogenDensity_kg_Nm3;
	public double p_oxygenDensity_kg_Nm3;
	public double p_oxygenProduction_kgO2pkgH2;
	

	
	
	/**
     * Default constructor
     */
    public J_AVGC_data() {
    }

	@Override
	public String toString() {
		return super.toString();
	}
}