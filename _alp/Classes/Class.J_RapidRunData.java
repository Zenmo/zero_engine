/**
 * J_RapidRunData
 */	
public class J_RapidRunData {
	
	public Agent parentAgent;
	
	//Full simulation
	public ZeroAccumulator acc_dailyAverageBaseloadElectricityConsumption_kW;
    public ZeroAccumulator acc_dailyAverageHeatPumpElectricityConsumption_kW;
    public ZeroAccumulator acc_dailyAverageElectricVehicleConsumption_kW;
    public ZeroAccumulator acc_dailyAverageBatteriesConsumption_kW;
    public ZeroAccumulator acc_dailyAverageElectricCookingConsumption_kW;
    public ZeroAccumulator acc_dailyAverageElectrolyserElectricityConsumption_kW;
    public ZeroAccumulator acc_dailyAverageDistrictHeatingConsumption_kW;
    public ZeroAccumulator acc_dailyAveragePVProduction_kW;
    public ZeroAccumulator acc_dailyAverageWindProduction_kW;
    public ZeroAccumulator acc_dailyAverageV2GProduction_kW;
    public ZeroAccumulator acc_dailyAverageBatteriesProduction_kW;
    public ZeroAccumulator acc_dailyAverageCHPElectricityProduction_kW;
    //public ZeroAccumulator acc_dailyAverageBatteriesStoredEnergy_MWh;
    public ZeroAccumulator am_dailyAverageConsumptionAccumulators_kW;
    public ZeroAccumulator am_dailyAverageProductionAccumulators_kW;
    
    
    //Summer week
    public ZeroAccumulator acc_summerWeekBaseloadElectricityConsumption_kW;
    public ZeroAccumulator acc_summerWeekHeatPumpElectricityConsumption_kW;
    public ZeroAccumulator acc_summerWeekElectricVehicleConsumption_kW;
    public ZeroAccumulator acc_summerWeekBatteriesConsumption_kW;
    public ZeroAccumulator acc_summerWeekElectricCookingConsumption_kW;
    public ZeroAccumulator acc_summerWeekElectrolyserElectricityConsumption_kW;
    public ZeroAccumulator acc_summerWeekDistrictHeatingConsumption_kW;
    public ZeroAccumulator acc_summerWeekPVProduction_kW;
    public ZeroAccumulator acc_summerWeekWindProduction_kW;
    public ZeroAccumulator acc_summerWeekV2GProduction_kW;
    public ZeroAccumulator acc_summerWeekBatteriesProduction_kW;
    public ZeroAccumulator acc_summerWeekCHPElectricityProduction_kW;
    //public ZeroAccumulator acc_summerWeekBatteriesStoredEnergy_MWh;
    public ZeroAccumulator am_summerWeekConsumptionAccumulators_kW;
    public ZeroAccumulator am_summerWeekProductionAccumulators_kW;
    
    
    //Winter week
    public ZeroAccumulator acc_winterWeekBaseloadElectricityConsumption_kW;
    public ZeroAccumulator acc_winterWeekHeatPumpElectricityConsumption_kW;
    public ZeroAccumulator acc_winterWeekElectricVehicleConsumption_kW;
    public ZeroAccumulator acc_winterWeekBatteriesConsumption_kW;
    public ZeroAccumulator acc_winterWeekElectricCookingConsumption_kW;
    public ZeroAccumulator acc_winterWeekElectrolyserElectricityConsumption_kW;
    public ZeroAccumulator acc_winterWeekDistrictHeatingConsumption_kW;
    public ZeroAccumulator acc_winterWeekPVProduction_kW;
    public ZeroAccumulator acc_winterWeekWindProduction_kW;
    public ZeroAccumulator acc_winterWeekV2GProduction_kW;
    public ZeroAccumulator acc_winterWeekBatteriesProduction_kW;
    public ZeroAccumulator acc_winterWeekCHPElectricityProduction_kW;
    //public ZeroAccumulator acc_winterWeekBatteriesStoredEnergy_MWh;
    public ZeroAccumulator am_winterWeekConsumptionAccumulators_kW;
    public ZeroAccumulator am_winterWeekProductionAccumulators_kW;

    /**
     * Default constructor
     */
    public J_RapidRunData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    
	public String toString() {
		return super.toString();
	}
}