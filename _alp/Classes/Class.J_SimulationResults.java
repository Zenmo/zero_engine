/**
 * J_SimulationResults
 */
public class J_SimulationResults implements Serializable {

	public double HSMSPeakLoadElectricity_kW;
	public double MSLSPeakLoadElectricity_kW;
	public double cumulativeCapacityHS;
	public double cumulativeCapacityLS;
	public double netOverload_pct;
	public Map<String,Double> timesOfNodePeakLoads_h = new LinkedHashMap<String,Double>();
	public Map<String,Double> gridConnectionMaxLoad_fr = new LinkedHashMap<String,Double>();
	public double MSLSnodePeakPositiveLoadElectricity_kW;
	public double MSLSnodePeakNegativeLoadElectricity_kW;
	//public double checkSumHourlyElectricityImport_MWh;
	//public double checkSumHourlyElectricityExport_MWh;
	public double totalElectricityImport_MWh;
	public double totalElectricityExport_MWh;
	public double totalMethaneImport_MWh;
	public double totalMethaneExport_MWh;
	public double totalHydrogenImport_MWh;
	public double totalHydrogenExport_MWh;
	public double totalPetroleumFuelImport_MWh;
	public Map<String,Double> totalBatteryUnitsInstalled = new LinkedHashMap<String,Double>();
	public Map<String,Double> totalBatteryChargeAmount_MWh = new LinkedHashMap<String,Double>();
	public Map<String,Double> totalBatteryDischargeAmount_MWh = new LinkedHashMap<String,Double>();
	public Map<String,Double> totalBatteryInstalledCapacity_MWh = new LinkedHashMap<String,Double>();
	//public Map<Integer,Double> SystemHourlyElectricityImport_MWh = new LinkedHashMap<Integer,Double>();
	//public Map<Integer,Double> SystemHourlyElectricityExport_MWh = new LinkedHashMap<Integer,Double>();
	//public Map<Integer,Double> totalEVHourlyChargingProfile_kWh = new LinkedHashMap<Integer,Double>();
	//public Map<Integer,Double> totalEHGVHourlyChargingProfile_kWh = new LinkedHashMap<Integer,Double>();
	//public Map<Integer,Double> totalBatteryHourlyChargingProfile_kWh = new LinkedHashMap<Integer, Double>();
	public double totalSelfConsumption_fr;
	public double totalSelfSufficiency_fr;
	public double TotalEnergyUsed_MWh;
	public double TotalEnergyProduced_MWh;
	public double TotalEnergyCurtailed_MWh;
	public double shareElectricvehiclesInHouseholds;

	 /**
     * Default constructor
     */
    public J_SimulationResults() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_SimulationResults( double v_gridNodeLoadElectricityHSMS_kW, double v_gridNodeLoadElectricityMSLS_kW, double cumulativeCapacityHS, double cumulativeCapacityLS, double netOverload_pct, LinkedHashMap<String,Double> c_timesOfNodePeakLoads_h, LinkedHashMap<String,Double> gridConnectionMaxLoad_fr, double MSLSnodePeakPositiveLoadElectricity_kW, double MSLSnodePeakNegativeLoadElectricity_kW, double totalElectricityImport_MWh, double totalElectricityExport_MWh, double totalMethaneImport_MWh, double totalMethaneExport_MWh, double totalHydrogenImport_MWh, double totalHydrogenExport_MWh, double totalPetroleumFuelImport_MWh, LinkedHashMap<String,Double> totalBatteryUnitsInstalled, LinkedHashMap<String,Double> totalBatteryChargeAmount_MWh, LinkedHashMap<String,Double> totalBatteryDischargeAmount_MWh, LinkedHashMap<String,Double> totalBatteryInstalledCapacity_MWh, LinkedHashMap<Integer,Double> c_globalElectricityImportProfile_MWhph, LinkedHashMap<Integer,Double> c_globalElectricityExportProfile_MWhph, LinkedHashMap<Integer,Double> totalEVHourlyChargingProfile_kWh, LinkedHashMap<Integer,Double> totalEHGVHourlyChargingProfile_kWh, LinkedHashMap<Integer,Double> totalBatteryHourlyChargingProfile_kWh, double totalSelfConsumption_fr, double totalSelfSufficiency_fr, double TotalEnergyUsed_MWh, double TotalEnergyProduced_MWh, double TotalEnergyCurtailed_MWh, double shareElectricvehiclesInHouseholds) {
		this.HSMSPeakLoadElectricity_kW = v_gridNodeLoadElectricityHSMS_kW;
		this.MSLSPeakLoadElectricity_kW = v_gridNodeLoadElectricityMSLS_kW;
		this.cumulativeCapacityHS = cumulativeCapacityHS;
		this.cumulativeCapacityLS = cumulativeCapacityLS;
		this.netOverload_pct = netOverload_pct;
		this.timesOfNodePeakLoads_h = c_timesOfNodePeakLoads_h;
		this.gridConnectionMaxLoad_fr = gridConnectionMaxLoad_fr;
		this.MSLSnodePeakPositiveLoadElectricity_kW = MSLSnodePeakPositiveLoadElectricity_kW;
		this.MSLSnodePeakNegativeLoadElectricity_kW = MSLSnodePeakNegativeLoadElectricity_kW;
		this.totalElectricityImport_MWh = totalElectricityImport_MWh;
		this.totalElectricityExport_MWh = totalElectricityExport_MWh;
		this.totalMethaneImport_MWh = totalMethaneImport_MWh;
		this.totalMethaneExport_MWh = totalMethaneExport_MWh;
		this.totalHydrogenImport_MWh = totalHydrogenImport_MWh;
		this.totalHydrogenExport_MWh = totalHydrogenExport_MWh;
		this.totalPetroleumFuelImport_MWh = totalPetroleumFuelImport_MWh;
		this.totalBatteryUnitsInstalled = totalBatteryUnitsInstalled;
		this.totalBatteryChargeAmount_MWh = totalBatteryChargeAmount_MWh;
		this.totalBatteryDischargeAmount_MWh = totalBatteryDischargeAmount_MWh;
		this.totalBatteryInstalledCapacity_MWh = totalBatteryInstalledCapacity_MWh;
		//this.SystemHourlyElectricityImport_MWh = c_globalElectricityImportProfile_MWhph;
		//this.SystemHourlyElectricityExport_MWh = c_globalElectricityExportProfile_MWhph;
		//this.totalEVHourlyChargingProfile_kWh = totalEVHourlyChargingProfile_kWh;
		//this.totalEHGVHourlyChargingProfile_kWh = totalEHGVHourlyChargingProfile_kWh;
		//this.totalBatteryHourlyChargingProfile_kWh = totalBatteryHourlyChargingProfile_kWh;
		this.totalSelfConsumption_fr = totalSelfConsumption_fr;
		this.totalSelfSufficiency_fr = totalSelfSufficiency_fr;
		this.TotalEnergyUsed_MWh = TotalEnergyUsed_MWh;
		this.TotalEnergyProduced_MWh = TotalEnergyProduced_MWh;
		this.TotalEnergyCurtailed_MWh = TotalEnergyCurtailed_MWh;
		this.shareElectricvehiclesInHouseholds = shareElectricvehiclesInHouseholds;
    }

	@Override
	public String toString() {
		return
			"HSMSPeakLoadElectricity_kW = " + HSMSPeakLoadElectricity_kW + " " +
			"MSLSPeakLoadElectricity_kW = " + MSLSPeakLoadElectricity_kW + " " +
			"MSLSnodePeakPositiveLoadElectricity_kW = " + MSLSnodePeakPositiveLoadElectricity_kW + " " +
			"MSLSnodePeakNegativeLoadElectricity_kW = " + MSLSnodePeakNegativeLoadElectricity_kW + " " +
			//"checkSumHourlyElectricityImport_MWh = " + checkSumHourlyElectricityImport_MWh + " " +
			//"checkSumHourlyElectricityExport_MWh = " + checkSumHourlyElectricityExport_MWh + " " +
			"totalElectricityImport_MWh = " + totalElectricityImport_MWh + " " +
			"totalElectricityExport_MWh = " + totalElectricityExport_MWh + " " +
			"totalMethaneImport_MWh = " + totalMethaneImport_MWh + " " +
			"totalMethaneExport_MWh = " + totalMethaneExport_MWh + " " +
			"totalHydrogenImport_MWh = " + totalHydrogenImport_MWh + " " +
			"totalHydrogenExport_MWh = " + totalHydrogenExport_MWh + " "+
			"totalPetroleumFuelImport_MWh = " + totalPetroleumFuelImport_MWh + " " +
			"totalBatteryUnitsInstalled = " + totalBatteryUnitsInstalled + " "+
			"totalBatteryChargeAmount_MWh = " + totalBatteryChargeAmount_MWh + " "+
			"totalBatteryDischargeAmount_MWh = " + totalBatteryDischargeAmount_MWh + " "+
			"totalBatteryInstalledCapacity_MWh = " + totalBatteryInstalledCapacity_MWh + " "+
			//"SystemHourlyElectricityImport_MWh = " + SystemHourlyElectricityImport_MWh.toString() + " " +
			//"SystemHourlyElectricityExport_MWh = " + SystemHourlyElectricityExport_MWh.toString() + " " +
			//"totalEVHourlyChargingProfile_kWh = " + totalEVHourlyChargingProfile_kWh + " "+
			//"totalEHGVHourlyChargingProfile_kWh = " + totalEHGVHourlyChargingProfile_kWh + " "+
			//"totalBatteryHourlyChargingProfile_kWh = " + totalBatteryHourlyChargingProfile_kWh + " " +
			"totalSelfConsumption_fr = " + totalSelfConsumption_fr*100 + " %" +
			"totalSelfSufficiency_fr = " + totalSelfSufficiency_fr*100 + " %" +
			"totalEnergyUsed_MWh = " + TotalEnergyUsed_MWh + " MWh" +
			"TotalEnergyProduced_MWh = " + TotalEnergyProduced_MWh + " MWh" +
			"TotalEnergyCurtailed_MWh = " + TotalEnergyCurtailed_MWh + " MWh" +
			"shareElectricvehiclesInHouseholds = " + shareElectricvehiclesInHouseholds + " fr";
	}

	public void updateData( double v_gridNodePeakLoadElectricityHSMS_kW, double v_gridNodePeakLoadElectricityMSLS_kW, double cumulativeCapacityHS, double cumulativeCapacityLS, double netOverload_pct, LinkedHashMap<String,Double> c_timesOfNodePeakLoads_h, LinkedHashMap<String,Double> gridConnectionMaxLoad_fr, double MSLSnodePeakPositiveLoadElectricity_kW, double MSLSnodePeakNegativeLoadElectricity_kW, double totalElectricityImport_MWh, double totalElectricityExport_MWh, double totalMethaneImport_MWh, double totalMethaneExport_MWh, double totalHydrogenImport_MWh, double totalHydrogenExport_MWh, double totalPetroleumFuelImport_MWh, LinkedHashMap<String,Double> totalBatteryUnitsInstalled, LinkedHashMap<String,Double> totalBatteryChargeAmount_MWh, LinkedHashMap<String,Double> totalBatteryDischargeAmount_MWh, LinkedHashMap<String,Double> totalBatteryInstalledCapacity_MWh, double totalSelfConsumption_fr, double totalSelfSufficiency_fr, double TotalEnergyUsed_MWh, double TotalEnergyProduced_MWh, double TotalEnergyCurtailed_MWh, double shareElectricvehiclesInHouseholds) {
		this.HSMSPeakLoadElectricity_kW = v_gridNodePeakLoadElectricityHSMS_kW;
		this.MSLSPeakLoadElectricity_kW = v_gridNodePeakLoadElectricityMSLS_kW;
		this.cumulativeCapacityHS =cumulativeCapacityHS;
		this.cumulativeCapacityLS = cumulativeCapacityLS;
		this.netOverload_pct = netOverload_pct;
		this.timesOfNodePeakLoads_h = c_timesOfNodePeakLoads_h;
		this.gridConnectionMaxLoad_fr = gridConnectionMaxLoad_fr;
		this.MSLSnodePeakPositiveLoadElectricity_kW = MSLSnodePeakPositiveLoadElectricity_kW;
		this.MSLSnodePeakNegativeLoadElectricity_kW = MSLSnodePeakNegativeLoadElectricity_kW;
		this.totalElectricityImport_MWh = roundToDecimal( totalElectricityImport_MWh , 2 );
		this.totalElectricityExport_MWh = roundToDecimal( totalElectricityExport_MWh, 2 );
		this.totalMethaneImport_MWh = roundToDecimal( totalMethaneImport_MWh, 2 );
		this.totalMethaneExport_MWh = roundToDecimal( totalMethaneExport_MWh, 2 );
		this.totalHydrogenImport_MWh = roundToDecimal( totalHydrogenImport_MWh, 2 );
		this.totalHydrogenExport_MWh = roundToDecimal( totalHydrogenExport_MWh, 2 );
		this.totalPetroleumFuelImport_MWh = roundToDecimal( totalPetroleumFuelImport_MWh, 2 );
		this.totalBatteryUnitsInstalled = totalBatteryUnitsInstalled;
		this.totalBatteryChargeAmount_MWh = totalBatteryChargeAmount_MWh;
		this.totalBatteryDischargeAmount_MWh = totalBatteryDischargeAmount_MWh;
		this.totalBatteryInstalledCapacity_MWh = totalBatteryInstalledCapacity_MWh;
		//this.SystemHourlyElectricityImport_MWh = c_globalElectricityImportProfile_MWhph;
		//this.SystemHourlyElectricityExport_MWh = c_globalElectricityExportProfile_MWhph;
		//this.checkSumHourlyElectricityImport_MWh = SystemHourlyElectricityImport_MWh.values().stream().mapToDouble(d -> d).sum();
		//this.checkSumHourlyElectricityExport_MWh = SystemHourlyElectricityExport_MWh.values().stream().mapToDouble(d -> d).sum();
		//this.totalEVHourlyChargingProfile_kWh = totalEVHourlyChargingProfile_kWh;
		//this.totalEHGVHourlyChargingProfile_kWh = totalEHGVHourlyChargingProfile_kWh;
		//this.totalBatteryHourlyChargingProfile_kWh = totalBatteryHourlyChargingProfile_kWh;
		this.totalSelfConsumption_fr = totalSelfConsumption_fr;
		this.totalSelfSufficiency_fr = totalSelfSufficiency_fr;
		this.TotalEnergyUsed_MWh = TotalEnergyUsed_MWh;
		this.TotalEnergyProduced_MWh = TotalEnergyProduced_MWh;
		this.TotalEnergyCurtailed_MWh = TotalEnergyCurtailed_MWh;
		this.shareElectricvehiclesInHouseholds = shareElectricvehiclesInHouseholds;
	}


	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}