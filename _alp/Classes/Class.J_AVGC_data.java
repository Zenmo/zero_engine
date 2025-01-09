/**
 * J_AVGC_data
 */	
public class J_AVGC_data implements Serializable {
	public double p_avgHouseGasConsumption_m3_yr;
	public double p_avgHouseElectricityConsumption_kWh_yr;
	public double p_avgEVMaxChargePowerVan_kW;
	public double p_avgHouseConnectionCapacity_kW;
	public OL_GridConnectionHeatingType p_avgHouseHeatingMethod;
	public double p_ratioEVHousePersonalCars;
	public double p_avgEVMaxChargePowerCar_kW;
	public double p_avgEVMaxChargePowerTruck_kW;
	public double p_avgEVStorageCar_kWh;
	public double p_avgEVStorageVan_kWh;
	public double p_avgHousePVInstallationPower_kWp;
	public double p_avgEVStorageTruck_kWh;
	public double p_ratioHouseInstalledPV;
	public double p_gas_kWhpm3;
	public double p_diesel_kWhpl;
	public double p_gasoline_kWhpl;
	public OL_GridConnectionHeatingType p_avgUtilityHeatingMethod;
	public double p_avgUtilityConnectionCapacity_kW;
	public double p_avgUtilityPVPower_kWp;
	public double p_ratioElectricTrucks;
	public OL_GridConnectionHeatingType p_avgCompanyHeatingMethod;
	public double p_avgEVEnergyConsumptionCar_kWhpkm;
	public double p_avgEVEnergyConsumptionVan_kWhpkm;
	public double p_avgEVEnergyConsumptionTruck_kWhpkm;
	public double p_hydrogenEnergyDensity_kWh_Nm3;
	public double p_avgDieselConsumptionCar_kmpl;
	public double p_avgGasolineConsumptionCar_kmpl;
	public double p_hydrogenSpecificEnergy_kWh_kg;
	public double p_hydrogenDensity_kg_Nm3;
	public double p_oxygenDensity_kg_Nm3;
	public double p_avgCOPHeatpump;
	public double p_avgEfficiencyHeatpump;
	public double p_avgDieselConsumptionCar_kWhpkm;
	public double p_oxygenProduction_kgO2pkgH2;
	public double p_avgGasolineConsumptionCar_kWhpkm;
	public double p_avgDieselConsumptionVan_kmpl;
	public double p_avgDieselConsumptionVan_kWhpkm;
	public double p_avgDieselConsumptionTruck_kmpl;
	public double p_avgDieselConsumptionTruck_kWhpkm;
	public double p_avgOutputTemperatureHeatpump_degC;
	public double p_avgHydrogenConsumptionCar_kWhpkm;
	public double p_avgEfficiencyGasBurner;
	public double p_avgHydrogenConsumptionVan_kWhpkm;
	public double p_avgHydrogenConsumptionTruck_kWhpkm;
	public double p_avgOutputTemperatureGasBurner_degC;
	public double p_avgEfficiencyHydrogenBurner;
	public double p_avgOutputTemperatureHydrogenBurner_degC;
	public double p_avgPVPower_kWpm2;
	public double p_avgAnnualProductionPV_kWhpWp;
	public double p_avgRatioRoofPotentialPV;
	public double p_avgRatioBatteryCapacity_v_Power;

	/**
     * Default constructor
     */
    public J_AVGC_data() {
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