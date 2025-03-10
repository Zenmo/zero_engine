/**
 * J_HourlyCurvesData
 */
public class J_HourlyCurvesData implements Serializable {

	public double checkSumHourlyElectricityImport_MWh;
	public double checkSumHourlyElectricityExport_MWh;
	public Map<Integer,Double> SystemHourlyElectricityImport_MWh = new LinkedHashMap<Integer,Double>();
	public Map<Integer,Double> SystemHourlyElectricityExport_MWh = new LinkedHashMap<Integer,Double>();
	public Map<Integer,Double> totalEVHourlyChargingProfile_kWh = new LinkedHashMap<Integer,Double>();
	public Map<Integer,Double> totalEHGVHourlyChargingProfile_kWh = new LinkedHashMap<Integer,Double>();
	public Map<Integer,Double> totalBatteryHourlyChargingProfile_kWh = new LinkedHashMap<Integer, Double>();

	 /**
     * Default constructor
     */
    public J_HourlyCurvesData() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_HourlyCurvesData(LinkedHashMap<Integer,Double> c_globalElectricityImportProfile_MWhph, LinkedHashMap<Integer,Double> c_globalElectricityExportProfile_MWhph, LinkedHashMap<Integer,Double> totalEVHourlyChargingProfile_kWh, LinkedHashMap<Integer,Double> totalEHGVHourlyChargingProfile_kWh, LinkedHashMap<Integer,Double> totalBatteryHourlyChargingProfile_kWh) {

		this.SystemHourlyElectricityImport_MWh = c_globalElectricityImportProfile_MWhph;
		this.SystemHourlyElectricityExport_MWh = c_globalElectricityExportProfile_MWhph;
		this.totalEVHourlyChargingProfile_kWh = totalEVHourlyChargingProfile_kWh;
		this.totalEHGVHourlyChargingProfile_kWh = totalEHGVHourlyChargingProfile_kWh;
		this.totalBatteryHourlyChargingProfile_kWh = totalBatteryHourlyChargingProfile_kWh;
		this.checkSumHourlyElectricityImport_MWh = 0;
		this.checkSumHourlyElectricityExport_MWh = 0;	

    }

	@Override
	public String toString() {
		return
			"SystemHourlyElectricityImport_MWh = " + SystemHourlyElectricityImport_MWh.toString() + " " +
			"SystemHourlyElectricityExport_MWh = " + SystemHourlyElectricityExport_MWh.toString() + " " +
			"totalEVHourlyChargingProfile_kWh = " + totalEVHourlyChargingProfile_kWh + " "+
			"totalEHGVHourlyChargingProfile_kWh = " + totalEHGVHourlyChargingProfile_kWh + " "+
			"totalBatteryHourlyChargingProfile_kWh = " + totalBatteryHourlyChargingProfile_kWh + " " ;
	}

	public void updateData(LinkedHashMap<Integer,Double> c_globalElectricityImportProfile_MWhph, LinkedHashMap<Integer,Double> c_globalElectricityExportProfile_MWhph, LinkedHashMap<Integer,Double> totalEVHourlyChargingProfile_kWh, LinkedHashMap<Integer,Double> totalEHGVHourlyChargingProfile_kWh, LinkedHashMap<Integer,Double> totalBatteryHourlyChargingProfile_kWh) {
		this.SystemHourlyElectricityImport_MWh = c_globalElectricityImportProfile_MWhph;
		this.SystemHourlyElectricityExport_MWh = c_globalElectricityExportProfile_MWhph;
		this.totalEVHourlyChargingProfile_kWh = totalEVHourlyChargingProfile_kWh;
		this.totalEHGVHourlyChargingProfile_kWh = totalEHGVHourlyChargingProfile_kWh;
		this.totalBatteryHourlyChargingProfile_kWh = totalBatteryHourlyChargingProfile_kWh;
		this.checkSumHourlyElectricityImport_MWh = SystemHourlyElectricityImport_MWh.values().stream().mapToDouble(d -> d).sum();
		this.checkSumHourlyElectricityExport_MWh = SystemHourlyElectricityExport_MWh.values().stream().mapToDouble(d -> d).sum();
	}


	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}