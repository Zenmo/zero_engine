import java.util.EnumSet;
/**
 * J_RapidRunData
 */	
public class J_RapidRunData {
	
	public Agent parentAgent;
	public EnumSet<OL_EnergyCarriers> activeEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers;
	
	public J_AssetsMetaData assetsMetaData;
	public J_ConnectionMetaData connectionMetaData;
	
	////Full simulation
	public J_AccumulatorMap am_totalBalanceAccumulators_kW = new J_AccumulatorMap();
	
    public ZeroAccumulator acc_totalEnergyCurtailed_kW;
    public ZeroAccumulator acc_totalPrimaryEnergyProductionHeatpumps_kW;
    
    public J_AccumulatorMap am_dailyAverageConsumptionAccumulators_kW = new J_AccumulatorMap();
    public J_AccumulatorMap am_dailyAverageProductionAccumulators_kW = new J_AccumulatorMap();
    
	public ZeroAccumulator acc_dailyAverageFinalEnergyConsumption_kW;
	public ZeroAccumulator acc_dailyAverageEnergyProduction_kW;
	
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
    public ZeroTimeSeries ts_dailyAverageBatteriesStoredEnergy_MWh;
    public ZeroTimeSeries ts_dailyAverageBatteriesSOC_fr;
    
    ////Summer week
    public J_AccumulatorMap am_summerWeekBalanceAccumulators_kW = new J_AccumulatorMap();

    public ZeroAccumulator acc_summerWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_summerWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_summerWeekFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_summerWeekEnergyCurtailed_kW;
    public ZeroAccumulator acc_summerWeekEnergyProduction_kW;

    public J_AccumulatorMap am_summerWeekConsumptionAccumulators_kW = new J_AccumulatorMap();
    public J_AccumulatorMap am_summerWeekProductionAccumulators_kW = new J_AccumulatorMap();

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
    public ZeroAccumulator acc_summerWeekPrimaryEnergyProductionHeatpumps_kW;
    public ZeroTimeSeries ts_summerWeekBatteriesStoredEnergy_MWh;
    public ZeroTimeSeries ts_summerWeekBatteriesSOC_fr;
    
    ////Winter week
    public J_AccumulatorMap am_winterWeekBalanceAccumulators_kW = new J_AccumulatorMap();
    
    public ZeroAccumulator acc_winterWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_winterWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_winterWeekFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_winterWeekEnergyProduction_kW;
    public ZeroAccumulator acc_winterWeekEnergyCurtailed_kW;
    
    public J_AccumulatorMap am_winterWeekConsumptionAccumulators_kW = new J_AccumulatorMap();
    public J_AccumulatorMap am_winterWeekProductionAccumulators_kW = new J_AccumulatorMap();  
    
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
    public ZeroAccumulator acc_winterWeekPrimaryEnergyProductionHeatpumps_kW;
    public ZeroTimeSeries ts_winterWeekBatteriesStoredEnergy_MWh;
    public ZeroTimeSeries ts_winterWeekBatteriesSOC_fr;
    
    ////Daytime / Nighttime
	public J_AccumulatorMap am_daytimeExports_kW = new J_AccumulatorMap();
    public J_AccumulatorMap am_daytimeImports_kW = new J_AccumulatorMap();
    
    public ZeroAccumulator acc_daytimeFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_daytimeEnergyProduction_kW;
    public ZeroAccumulator acc_daytimeElectricityConsumption_kW;
    public ZeroAccumulator acc_daytimeElectricityProduction_kW;
    
    //Weekend/day
    public ZeroAccumulator acc_weekendElectricityConsumption_kW;
    public ZeroAccumulator acc_weekendElectricityProduction_kW;
    public ZeroAccumulator acc_weekendFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_weekendEnergyProduction_kW;
    public J_AccumulatorMap am_weekendExports_kW = new J_AccumulatorMap();
    public J_AccumulatorMap am_weekendImports_kW = new J_AccumulatorMap();

    
    /**
     * Default constructor
     */
    public J_RapidRunData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    
    public void initializeAccumulators(double simDuration_h, double timeStep_h, EnumSet<OL_EnergyCarriers> v_activeEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeConsumptionEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeProductionEnergyCarriers) {
    	this.activeEnergyCarriers = v_activeEnergyCarriers;
    	this.activeConsumptionEnergyCarriers = v_activeConsumptionEnergyCarriers;
    	this.activeProductionEnergyCarriers = v_activeProductionEnergyCarriers;
	    //========== TOTAL ACCUMULATORS ==========//
		am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, 24.0, simDuration_h );
	    am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, timeStep_h, simDuration_h) );
	    am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(v_activeConsumptionEnergyCarriers, true, 24.0, simDuration_h);
	    am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(v_activeProductionEnergyCarriers, true, 24.0, simDuration_h);
	
	    acc_dailyAverageEnergyProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageFinalEnergyConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	
	    acc_totalEnergyCurtailed_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_totalPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	
	    acc_dailyAverageBaseloadElectricityConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageHeatPumpElectricityConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageElectricVehicleConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageBatteriesConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageElectricCookingConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageElectrolyserElectricityConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageDistrictHeatingConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    
	    acc_dailyAveragePVProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageWindProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageV2GProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageBatteriesProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageCHPElectricityProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	
	    ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, simDuration_h);
	    ts_dailyAverageBatteriesSOC_fr = new ZeroTimeSeries(timeStep_h, simDuration_h);
	    
	    //========== SUMMER WEEK ACCUMULATORS ==========//
	    am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	
	    acc_summerWeekEnergyProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekFinalEnergyConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_summerWeekEnergyCurtailed_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_summerWeekFeedinCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekDeliveryCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_summerWeekBaseloadElectricityConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekHeatPumpElectricityConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekElectricVehicleConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekBatteriesConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekElectricCookingConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekElectrolyserElectricityConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekDistrictHeatingConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_summerWeekPVProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekWindProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekV2GProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekBatteriesProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekCHPElectricityProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    ts_summerWeekBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, 168.0);
	    ts_summerWeekBatteriesSOC_fr = new ZeroTimeSeries(timeStep_h, 168.0);
	    
	    //========== WINTER WEEK ACCUMULATORS ==========//
	    am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	
	    acc_winterWeekEnergyProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekFinalEnergyConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_winterWeekEnergyCurtailed_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_winterWeekFeedinCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekDeliveryCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_winterWeekBaseloadElectricityConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekHeatPumpElectricityConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekElectricVehicleConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekBatteriesConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekElectricCookingConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekElectrolyserElectricityConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekDistrictHeatingConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_winterWeekPVProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekWindProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekV2GProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekBatteriesProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekCHPElectricityProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    ts_winterWeekBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, 168.0);
	    ts_winterWeekBatteriesSOC_fr = new ZeroTimeSeries(timeStep_h, 168.0);
	    
	    //========== DAYTIME ACCUMULATORS ==========//
	    am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
	    am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
	
	    acc_daytimeEnergyProduction_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    acc_daytimeFinalEnergyConsumption_kW = new ZeroAccumulator(false, timeStep_h,0.5 * (simDuration_h));
	    //acc_daytimeEnergyCurtailed_kW = new ZeroAccumulator(false, timeStep_h, simDuration_h);
	    acc_daytimeElectricityProduction_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    acc_daytimeElectricityConsumption_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	
	    //========== WEEKEND ACCUMULATORS ==========//
	    am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);
	
	    acc_weekendEnergyProduction_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    acc_weekendFinalEnergyConsumption_kW = new ZeroAccumulator(false, timeStep_h,2 / 7  * (simDuration_h) + 48);
	    //acc_weekendEnergyCurtailed_kW = new ZeroAccumulator(false, timeStep_h, simDuration_h);
	    acc_weekendElectricityProduction_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    acc_weekendElectricityConsumption_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	}

    public void resetAccumulators(double simDuration_h, double timeStep_h, EnumSet<OL_EnergyCarriers> v_activeEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeConsumptionEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeProductionEnergyCarriers) {
    	this.activeEnergyCarriers = v_activeEnergyCarriers;
    	this.activeConsumptionEnergyCarriers = v_activeConsumptionEnergyCarriers;
    	this.activeProductionEnergyCarriers = v_activeProductionEnergyCarriers;
    	//Total simulation
		am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, 24.0, simDuration_h );
    	am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, timeStep_h, simDuration_h) );
    	am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(v_activeConsumptionEnergyCarriers, true, 24.0, simDuration_h);
    	am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(v_activeProductionEnergyCarriers, true, 24.0, simDuration_h);

    	acc_dailyAverageEnergyProduction_kW.reset();
    	acc_dailyAverageFinalEnergyConsumption_kW.reset();

    	acc_totalEnergyCurtailed_kW.reset();
    	acc_totalPrimaryEnergyProductionHeatpumps_kW.reset();

    	acc_dailyAverageBaseloadElectricityConsumption_kW.reset();
    	acc_dailyAverageHeatPumpElectricityConsumption_kW.reset();
    	acc_dailyAverageElectricVehicleConsumption_kW.reset();
    	acc_dailyAverageBatteriesConsumption_kW.reset();
    	acc_dailyAverageElectricCookingConsumption_kW.reset();
    	acc_dailyAverageElectrolyserElectricityConsumption_kW.reset();
    	acc_dailyAverageDistrictHeatingConsumption_kW.reset();
    	
    	acc_dailyAveragePVProduction_kW.reset();
    	acc_dailyAverageWindProduction_kW.reset();
    	acc_dailyAverageV2GProduction_kW.reset();
    	acc_dailyAverageBatteriesProduction_kW.reset();
    	acc_dailyAverageCHPElectricityProduction_kW.reset();

        ts_dailyAverageBatteriesStoredEnergy_MWh.reset();
        ts_dailyAverageBatteriesSOC_fr.reset();
        
    	//Summerweek
    	am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);
    	am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);
    	am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);

    	acc_summerWeekEnergyProduction_kW.reset();
    	acc_summerWeekFinalEnergyConsumption_kW.reset();

    	acc_summerWeekEnergyCurtailed_kW.reset();
    	acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.reset();

    	acc_summerWeekFeedinCapacity_kW.reset();
    	acc_summerWeekDeliveryCapacity_kW.reset();

    	acc_summerWeekBaseloadElectricityConsumption_kW.reset();
    	acc_summerWeekHeatPumpElectricityConsumption_kW.reset();
    	acc_summerWeekElectricVehicleConsumption_kW.reset();
    	acc_summerWeekBatteriesConsumption_kW.reset();
    	acc_summerWeekElectricCookingConsumption_kW.reset();
    	acc_summerWeekElectrolyserElectricityConsumption_kW.reset();
    	acc_summerWeekDistrictHeatingConsumption_kW.reset();

    	acc_summerWeekPVProduction_kW.reset();
    	acc_summerWeekWindProduction_kW.reset();
    	acc_summerWeekV2GProduction_kW.reset();
    	acc_summerWeekBatteriesProduction_kW.reset();
    	acc_summerWeekCHPElectricityProduction_kW.reset();

	    ts_summerWeekBatteriesStoredEnergy_MWh.reset();
	    ts_summerWeekBatteriesSOC_fr.reset();
	    
    	//Winterweek
    	am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);
    	am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);
    	am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);

    	acc_winterWeekEnergyProduction_kW.reset();
    	acc_winterWeekFinalEnergyConsumption_kW.reset();

    	acc_winterWeekEnergyCurtailed_kW.reset();
    	acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.reset();

    	acc_winterWeekFeedinCapacity_kW.reset();
    	acc_winterWeekDeliveryCapacity_kW.reset();

    	acc_winterWeekBaseloadElectricityConsumption_kW.reset();
    	acc_winterWeekHeatPumpElectricityConsumption_kW.reset();
    	acc_winterWeekElectricVehicleConsumption_kW.reset();
    	acc_winterWeekBatteriesConsumption_kW.reset();
    	acc_winterWeekElectricCookingConsumption_kW.reset();
    	acc_winterWeekElectrolyserElectricityConsumption_kW.reset();
    	acc_winterWeekDistrictHeatingConsumption_kW.reset();

    	acc_winterWeekPVProduction_kW.reset();
    	acc_winterWeekWindProduction_kW.reset();
    	acc_winterWeekV2GProduction_kW.reset();
    	acc_winterWeekBatteriesProduction_kW.reset();
    	acc_winterWeekCHPElectricityProduction_kW.reset();
    	
	    ts_winterWeekBatteriesStoredEnergy_MWh.reset();
	    ts_winterWeekBatteriesSOC_fr.reset();
	    
    	// Daytime 
    	am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
    	am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
    	
    	acc_daytimeElectricityProduction_kW.reset();
    	acc_daytimeElectricityConsumption_kW.reset();
    	acc_daytimeEnergyProduction_kW.reset();
    	acc_daytimeFinalEnergyConsumption_kW.reset();

    	// Weekend
    	am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
    	am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);

    	// Energy / Electricity
    	acc_weekendElectricityProduction_kW.reset();
    	acc_weekendElectricityConsumption_kW.reset();
    	acc_weekendEnergyProduction_kW.reset();
    	acc_weekendFinalEnergyConsumption_kW.reset();
    }
    
    public J_RapidRunData getClone() {
    	J_RapidRunData clone = new J_RapidRunData(this.parentAgent);

    	clone.activeEnergyCarriers=this.activeEnergyCarriers.clone();
    	clone.activeConsumptionEnergyCarriers=this.activeConsumptionEnergyCarriers.clone();
    	clone.activeProductionEnergyCarriers=this.activeProductionEnergyCarriers.clone();
    	////Full simulation
    	clone.am_totalBalanceAccumulators_kW=this.am_totalBalanceAccumulators_kW.getClone();
    	clone.acc_totalEnergyCurtailed_kW=this.acc_totalEnergyCurtailed_kW.getClone();
        clone.acc_totalPrimaryEnergyProductionHeatpumps_kW=this.acc_totalPrimaryEnergyProductionHeatpumps_kW.getClone();
        clone.am_dailyAverageConsumptionAccumulators_kW=this.am_dailyAverageConsumptionAccumulators_kW.getClone();
        clone.am_dailyAverageProductionAccumulators_kW=this.am_dailyAverageProductionAccumulators_kW.getClone();
        clone.acc_dailyAverageFinalEnergyConsumption_kW=this.acc_dailyAverageFinalEnergyConsumption_kW.getClone();
    	clone.acc_dailyAverageEnergyProduction_kW=this.acc_dailyAverageEnergyProduction_kW.getClone();
    	clone.acc_dailyAverageBaseloadElectricityConsumption_kW=this.acc_dailyAverageBaseloadElectricityConsumption_kW.getClone();
        clone.acc_dailyAverageHeatPumpElectricityConsumption_kW=this.acc_dailyAverageHeatPumpElectricityConsumption_kW.getClone();
        clone.acc_dailyAverageElectricVehicleConsumption_kW=this.acc_dailyAverageElectricVehicleConsumption_kW.getClone();
        clone.acc_dailyAverageBatteriesConsumption_kW=this.acc_dailyAverageBatteriesConsumption_kW.getClone();
        clone.acc_dailyAverageElectricCookingConsumption_kW=this.acc_dailyAverageElectricCookingConsumption_kW.getClone();
        clone.acc_dailyAverageElectrolyserElectricityConsumption_kW=this.acc_dailyAverageElectrolyserElectricityConsumption_kW.getClone();
        clone.acc_dailyAverageDistrictHeatingConsumption_kW=this.acc_dailyAverageDistrictHeatingConsumption_kW.getClone();
        clone.acc_dailyAveragePVProduction_kW=this.acc_dailyAveragePVProduction_kW.getClone();
        clone.acc_dailyAverageWindProduction_kW=this.acc_dailyAverageWindProduction_kW.getClone();
        clone.acc_dailyAverageV2GProduction_kW=this.acc_dailyAverageV2GProduction_kW.getClone();
        clone.acc_dailyAverageBatteriesProduction_kW=this.acc_dailyAverageBatteriesProduction_kW.getClone();
        clone.acc_dailyAverageCHPElectricityProduction_kW=this.acc_dailyAverageCHPElectricityProduction_kW.getClone();
        clone.ts_dailyAverageBatteriesStoredEnergy_MWh=this.ts_dailyAverageBatteriesStoredEnergy_MWh.getClone();
        clone.ts_dailyAverageBatteriesSOC_fr=this.ts_dailyAverageBatteriesSOC_fr.getClone();
        ////Summer week
        clone.am_summerWeekBalanceAccumulators_kW=this.am_summerWeekBalanceAccumulators_kW.getClone();
        clone.acc_summerWeekDeliveryCapacity_kW=this.acc_summerWeekDeliveryCapacity_kW.getClone();
        clone.acc_summerWeekFeedinCapacity_kW=this.acc_summerWeekFeedinCapacity_kW.getClone();
        clone.acc_summerWeekFinalEnergyConsumption_kW=this.acc_summerWeekFinalEnergyConsumption_kW.getClone();
        clone.acc_summerWeekEnergyCurtailed_kW=this.acc_summerWeekEnergyCurtailed_kW.getClone();
        clone.acc_summerWeekEnergyProduction_kW=this.acc_summerWeekEnergyProduction_kW.getClone();
        clone.am_summerWeekConsumptionAccumulators_kW=this.am_summerWeekConsumptionAccumulators_kW.getClone();
        clone.am_summerWeekProductionAccumulators_kW=this.am_summerWeekProductionAccumulators_kW.getClone();
        clone.acc_summerWeekBaseloadElectricityConsumption_kW=this.acc_summerWeekBaseloadElectricityConsumption_kW.getClone();
        clone.acc_summerWeekHeatPumpElectricityConsumption_kW=this.acc_summerWeekHeatPumpElectricityConsumption_kW.getClone();
        clone.acc_summerWeekElectricVehicleConsumption_kW=this.acc_summerWeekElectricVehicleConsumption_kW.getClone();
        clone.acc_summerWeekBatteriesConsumption_kW=this.acc_summerWeekBatteriesConsumption_kW.getClone();
        clone.acc_summerWeekElectricCookingConsumption_kW=this.acc_summerWeekElectricCookingConsumption_kW.getClone();
        clone.acc_summerWeekElectrolyserElectricityConsumption_kW=this.acc_summerWeekElectrolyserElectricityConsumption_kW.getClone();
        clone.acc_summerWeekDistrictHeatingConsumption_kW=this.acc_summerWeekDistrictHeatingConsumption_kW.getClone();
        clone.acc_summerWeekPVProduction_kW=this.acc_summerWeekPVProduction_kW.getClone();
        clone.acc_summerWeekWindProduction_kW=this.acc_summerWeekWindProduction_kW.getClone();
        clone.acc_summerWeekV2GProduction_kW=this.acc_summerWeekV2GProduction_kW.getClone();
        clone.acc_summerWeekBatteriesProduction_kW=this.acc_summerWeekBatteriesProduction_kW.getClone();
        clone.acc_summerWeekCHPElectricityProduction_kW=this.acc_summerWeekCHPElectricityProduction_kW.getClone();
        clone.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW=this.acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.getClone();
        clone.ts_summerWeekBatteriesStoredEnergy_MWh=this.ts_summerWeekBatteriesStoredEnergy_MWh.getClone();
        clone.ts_summerWeekBatteriesSOC_fr=this.ts_summerWeekBatteriesSOC_fr.getClone();
        ////Winter week
        clone.am_winterWeekBalanceAccumulators_kW = this.am_winterWeekBalanceAccumulators_kW.getClone();
        clone.acc_winterWeekDeliveryCapacity_kW = this.acc_winterWeekDeliveryCapacity_kW.getClone();
        clone.acc_winterWeekFeedinCapacity_kW = this.acc_winterWeekFeedinCapacity_kW.getClone();
        clone.acc_winterWeekFinalEnergyConsumption_kW = this.acc_winterWeekFinalEnergyConsumption_kW.getClone();
        clone.acc_winterWeekEnergyProduction_kW = this.acc_winterWeekEnergyProduction_kW.getClone();
        clone.acc_winterWeekEnergyCurtailed_kW = this.acc_winterWeekEnergyCurtailed_kW.getClone();
        clone.am_winterWeekConsumptionAccumulators_kW = this.am_winterWeekConsumptionAccumulators_kW.getClone();
        clone.am_winterWeekProductionAccumulators_kW = this.am_winterWeekProductionAccumulators_kW.getClone();  
        clone.acc_winterWeekBaseloadElectricityConsumption_kW = this.acc_winterWeekBaseloadElectricityConsumption_kW.getClone();
        clone.acc_winterWeekHeatPumpElectricityConsumption_kW = this.acc_winterWeekHeatPumpElectricityConsumption_kW.getClone();
        clone.acc_winterWeekElectricVehicleConsumption_kW = this.acc_winterWeekElectricVehicleConsumption_kW.getClone();
        clone.acc_winterWeekBatteriesConsumption_kW = this.acc_winterWeekBatteriesConsumption_kW.getClone();
        clone.acc_winterWeekElectricCookingConsumption_kW = this.acc_winterWeekElectricCookingConsumption_kW.getClone();
        clone.acc_winterWeekElectrolyserElectricityConsumption_kW = this.acc_winterWeekElectrolyserElectricityConsumption_kW.getClone();
        clone.acc_winterWeekDistrictHeatingConsumption_kW = this.acc_winterWeekDistrictHeatingConsumption_kW.getClone();
        clone.acc_winterWeekPVProduction_kW = this.acc_winterWeekPVProduction_kW.getClone();
        clone.acc_winterWeekWindProduction_kW = this.acc_winterWeekWindProduction_kW.getClone();
        clone.acc_winterWeekV2GProduction_kW = this.acc_winterWeekV2GProduction_kW.getClone();
        clone.acc_winterWeekBatteriesProduction_kW = this.acc_winterWeekBatteriesProduction_kW.getClone();
        clone.acc_winterWeekCHPElectricityProduction_kW = this.acc_winterWeekCHPElectricityProduction_kW.getClone();
        clone.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW = this.acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.getClone();
        clone.ts_winterWeekBatteriesStoredEnergy_MWh = this.ts_winterWeekBatteriesStoredEnergy_MWh.getClone();
        clone.ts_winterWeekBatteriesSOC_fr = this.ts_winterWeekBatteriesSOC_fr.getClone();
        ////Daytime / Nighttime
    	clone.am_daytimeExports_kW = am_daytimeExports_kW.getClone();
        clone.am_daytimeImports_kW = am_daytimeImports_kW.getClone();
        clone.acc_daytimeFinalEnergyConsumption_kW = acc_daytimeFinalEnergyConsumption_kW.getClone();
        clone.acc_daytimeEnergyProduction_kW = acc_daytimeEnergyProduction_kW.getClone();
        clone.acc_daytimeElectricityConsumption_kW = acc_daytimeElectricityConsumption_kW.getClone();
        clone.acc_daytimeElectricityProduction_kW = acc_daytimeElectricityProduction_kW.getClone();
        //Weekend/day
        clone.acc_weekendElectricityConsumption_kW = this.acc_weekendElectricityConsumption_kW.getClone();
        clone.acc_weekendElectricityProduction_kW = this.acc_weekendElectricityProduction_kW.getClone();
        clone.acc_weekendFinalEnergyConsumption_kW = this.acc_weekendFinalEnergyConsumption_kW.getClone();
        clone.acc_weekendEnergyProduction_kW = this.acc_weekendEnergyProduction_kW.getClone();
        clone.am_weekendExports_kW = this.am_weekendExports_kW.getClone();
        clone.am_weekendImports_kW = this.am_weekendImports_kW.getClone();
        
        clone.assetsMetaData = this.assetsMetaData.getClone();
        clone.connectionMetaData = this.connectionMetaData.getClone();
        return clone;

    }
    
    public J_LoadDurationCurves getLoadDurationCurves(EnergyModel energyModel) {
    	return new J_LoadDurationCurves(this.am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW(), energyModel);    		
    }
    
    public double getTotalOverloadDurationDelivery_hr() {
    	double totalOverloadDurationDelivery_hr = 0.0;
    	double signalResolution_h = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getSignalResolution_h();
    	for (double electricityBalance_kW : am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()) {
        	if(electricityBalance_kW > connectionMetaData.contractedDeliveryCapacity_kW){
        		totalOverloadDurationDelivery_hr += signalResolution_h;
        	}
    	}
    	return totalOverloadDurationDelivery_hr;
    }
    
    public double getTotalOverloadDurationFeedin_hr() {
    	double totalOverloadDurationFeedin_hr = 0.0;
    	double signalResolution_h = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getSignalResolution_h();
    	for (double electricityBalance_kW : am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()) {
        	if(electricityBalance_kW < -connectionMetaData.contractedFeedinCapacity_kW){
        		totalOverloadDurationFeedin_hr += signalResolution_h;
        	}
    	}
    	return totalOverloadDurationFeedin_hr;
    }   
   
    public double getPeakDelivery_kW() {
    	double peakDelivery_kW = 0.0;
    	for (double electricityBalance_kW : am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()) {
    		peakDelivery_kW = max(peakDelivery_kW, electricityBalance_kW);
    	}
    	return peakDelivery_kW;
    }
    
    public double getPeakFeedin_kW() {
    	double peakFeedin_kW = 0.0;
    	for (double electricityBalance_kW : am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW()) {
    		peakFeedin_kW = max(peakFeedin_kW, -electricityBalance_kW);
    	}
    	return peakFeedin_kW;
    }
    
    public double getTotalElectricityConsumed_MWh() { 
        return am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000; 
    }
    public double getTotalElectricityProduced_MWh() { 
        return am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000; 
    }
    public double getTotalElectricitySelfConsumed_MWh() { 
        return max(0, getTotalElectricityConsumed_MWh() - am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegralPos_kWh() / 1000); 
    }
    public double getTotalEnergyConsumed_MWh() { 
        return acc_dailyAverageFinalEnergyConsumption_kW.getIntegral_kWh() / 1000; 
    }
    public double getTotalEnergyProduced_MWh() { 
        return acc_dailyAverageEnergyProduction_kW.getIntegral_kWh() / 1000; 
    }
    public double getTotalEnergyCurtailed_MWh() { 
        return acc_totalEnergyCurtailed_kW.getIntegral_kWh() / 1000; 
    }
    public double getTotalEnergyImport_MWh() { 
    	double totalEnergyImport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		totalEnergyImport_kWh+=am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_kWh();
    	}
        return totalEnergyImport_kWh/1000; 
    }   
    public double getTotalEnergyExport_MWh() { 
    	double totalEnergyExport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		totalEnergyExport_kWh+= - am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh();
    	}
        return totalEnergyExport_kWh/1000; 
    }
    
    public double getTotalExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return -this.am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_MWh();
    }
    public double getTotalImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_MWh();
    }
    
    public double getTotalEnergySelfConsumed_MWh() { 
        return max(0, getTotalEnergyConsumed_MWh() - getTotalEnergyImport_MWh()); 
    }
    public double getTotalPrimaryEnergyProductionHeatpumps_MWh() { 
        return acc_totalPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000; 
    }
    public double getTotalDistrictHeatingConsumption_MWh() {
    	return acc_dailyAverageDistrictHeatingConsumption_kW.getIntegral_kWh() / 1000;
    }
    

// Summerweek Getters
    public double getSummerWeekElectricityConsumed_MWh() {
        return am_summerWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
    }
    public double getSummerWeekElectricityProduced_MWh() {
        return am_summerWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
    }
    public double getSummerWeekElectricitySelfConsumed_MWh() {
        return max(0, getSummerWeekElectricityConsumed_MWh() - am_summerWeekBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegralPos_kWh() / 1000);
    }
    public double getSummerWeekEnergyConsumed_MWh() {
        return acc_summerWeekFinalEnergyConsumption_kW.getIntegral_kWh() / 1000;
    }
    public double getSummerWeekEnergyProduced_MWh() {
        return acc_summerWeekEnergyProduction_kW.getIntegral_kWh() / 1000;
    }
    public double getSummerWeekEnergyCurtailed_MWh() {
        return acc_summerWeekEnergyCurtailed_kW.getIntegral_kWh() / 1000;
    }
    public double getSummerWeekEnergyImport_MWh() {
        double summerWeekEnergyImport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		summerWeekEnergyImport_kWh+=am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralPos_kWh();
    	}
        return summerWeekEnergyImport_kWh/1000;
    }
    public double getSummerWeekEnergyExport_MWh() {
        double summerWeekEnergyExport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		summerWeekEnergyExport_kWh+= - am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh();
    	}
        return summerWeekEnergyExport_kWh/1000;
    }    
    
    public double getSummerWeekExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return -this.am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_MWh();
    }
    public double getSummerWeekImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_summerWeekBalanceAccumulators_kW.get(EC).getIntegralPos_MWh();
    }
    
    public double getSummerWeekEnergySelfConsumed_MWh() {
        return max(0, getSummerWeekEnergyConsumed_MWh() - getSummerWeekEnergyImport_MWh());
    }
    public double getSummerWeekPrimaryEnergyProductionHeatpumps_MWh() {
        return acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
    }
    
// Winterweek Getters
    public double getWinterWeekElectricityConsumed_MWh() {
        return am_winterWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
    }
    public double getWinterWeekElectricityProduced_MWh() {
        return am_winterWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000;
    }
    public double getWinterWeekElectricitySelfConsumed_MWh() {
        return max(0, getWinterWeekElectricityConsumed_MWh() - am_winterWeekBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegralPos_kWh() / 1000);
    }
    public double getWinterWeekEnergyConsumed_MWh() {
        return acc_winterWeekFinalEnergyConsumption_kW.getIntegral_kWh() / 1000;
    }
    public double getWinterWeekEnergyProduced_MWh() {
        return acc_winterWeekEnergyProduction_kW.getIntegral_kWh() / 1000;
    }
    public double getWinterWeekEnergyCurtailed_MWh() {
        return acc_winterWeekEnergyCurtailed_kW.getIntegral_kWh() / 1000;
    }
    public double getWinterWeekEnergyImport_MWh() {
        double winterWeekEnergyImport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		winterWeekEnergyImport_kWh+=am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralPos_kWh();
    	}
        return winterWeekEnergyImport_kWh/1000;
    }
    public double getWinterWeekEnergyExport_MWh() {
        double winterWeekEnergyExport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		winterWeekEnergyExport_kWh+= - am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh();
    	}
        return winterWeekEnergyExport_kWh/1000;
    }    
    public double getWinterWeekExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return -this.am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralNeg_MWh();
    }
    public double getWinterWeekImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_winterWeekBalanceAccumulators_kW.get(EC).getIntegralPos_MWh();
    }
    
    public double getWinterWeekEnergySelfConsumed_MWh() {
        return max(0, getWinterWeekEnergyConsumed_MWh() - getWinterWeekEnergyImport_MWh());
    }
    public double getWinterWeekPrimaryEnergyProductionHeatpumps_MWh() {
        return acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000;
    }
    
// Daytime getters    
    public double getDaytimeElectricityConsumed_MWh() { 
        return acc_daytimeElectricityConsumption_kW.getIntegral_kWh() / 1000; 
    }
    public double getDaytimeElectricityProduced_MWh() { 
        return acc_daytimeElectricityProduction_kW.getIntegral_kWh() / 1000; 
    }
    public double getDaytimeElectricitySelfConsumed_MWh() { 
        return max(0, getDaytimeElectricityConsumed_MWh() - am_daytimeImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh()/1000); 
    }
    public double getDaytimeEnergyConsumed_MWh() { 
        return acc_daytimeFinalEnergyConsumption_kW.getIntegral_kWh() / 1000; 
    }
    public double getDaytimeEnergyProduced_MWh() { 
        return acc_daytimeEnergyProduction_kW.getIntegral_kWh() / 1000; 
    }
    public double getDaytimeEnergyExport_MWh() { 
        return am_daytimeExports_kW.totalIntegral_kWh()/1000; 
    }
    public double getDaytimeEnergyImport_MWh() { 
        return am_daytimeImports_kW.totalIntegral_kWh()/1000; 
    }
    public double getDaytimeEnergySelfConsumed_MWh() { 
        return max(0, getDaytimeEnergyProduced_MWh() - getDaytimeEnergyExport_MWh()); 
    }
    public double getDaytimeExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_daytimeExports_kW.get(EC).getIntegral_kWh() / 1000;
    }
    public double getDaytimeImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_daytimeImports_kW.get(EC).getIntegral_kWh() / 1000;
    }
    
//Nighttime Getters
    public double getNighttimeElectricityConsumed_MWh() { 
        return getTotalElectricityConsumed_MWh() - getDaytimeElectricityConsumed_MWh(); 
    }
    public double getNighttimeElectricityProduced_MWh() { 
        return getTotalElectricityProduced_MWh() - getDaytimeElectricityProduced_MWh(); 
    }
    public double getNighttimeElectricitySelfConsumed_MWh() { 
        return max(0,getNighttimeElectricityConsumed_MWh() - (am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh()/1000 - am_daytimeImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh()/1000)); 
    }
    public double getNighttimeEnergyConsumed_MWh() { 
        return getTotalEnergyConsumed_MWh() - getDaytimeEnergyConsumed_MWh(); 
    }
    public double getNighttimeEnergyProduced_MWh() { 
        return getTotalEnergyProduced_MWh() - getDaytimeEnergyProduced_MWh(); 
    }
    public double getNighttimeEnergyExport_MWh() { 
        return getTotalEnergyExport_MWh() - getDaytimeEnergyExport_MWh(); 
    }
    public double getNighttimeEnergyImport_MWh() { 
        return getTotalEnergyImport_MWh() - getDaytimeEnergyImport_MWh(); 
    }
    public double getNighttimeEnergySelfConsumed_MWh() { 
        return max(0, getNighttimeEnergyProduced_MWh() - getNighttimeEnergyExport_MWh()); 
    } 
    public double getNighttimeExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return -this.am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 - this.getDaytimeExport_MWh(EC);
    }
    public double getNighttimeImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 - this.getDaytimeImport_MWh(EC);
    }
// Weekday Getters
    public double getWeekdayElectricityConsumed_MWh() { 
        return getTotalElectricityConsumed_MWh() - getWeekendElectricityConsumed_MWh(); 
    }
    public double getWeekdayElectricityProduced_MWh() { 
        return getTotalElectricityProduced_MWh() - getWeekendElectricityProduced_MWh(); 
    }
    public double getWeekdayElectricitySelfConsumed_MWh() { 
        return max(0,getWeekdayElectricityConsumed_MWh() - (am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh()/1000 - am_weekendImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh()/1000)); 
    }
    public double getWeekdayEnergyConsumed_MWh() { 
        return getTotalEnergyConsumed_MWh() - getWeekendEnergyConsumed_MWh(); 
    }
    public double getWeekdayEnergyProduced_MWh() { 
        return getTotalEnergyProduced_MWh() - getWeekendEnergyProduced_MWh(); 
    }
    public double getWeekdayEnergyExport_MWh() { 
        return getTotalEnergyExport_MWh() - getWeekendEnergyExport_MWh(); 
    }
    public double getWeekdayEnergyImport_MWh() { 
        return getTotalEnergyImport_MWh() - getWeekendEnergyImport_MWh(); 
    }
    public double getWeekdayEnergySelfConsumed_MWh() { 
        return max(0, getWeekdayEnergyProduced_MWh() - getWeekdayEnergyExport_MWh()); 
    } 
    public double getWeekdayExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return -this.am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh() / 1000 - this.getWeekendExport_MWh(EC);
    }
    public double getWeekdayImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_kWh() / 1000 - this.getWeekendImport_MWh(EC);
    }
//Weekend Getters
    public double getWeekendElectricityConsumed_MWh() { 
        return acc_weekendElectricityConsumption_kW.getIntegral_kWh() / 1000; 
    }
    public double getWeekendElectricityProduced_MWh() { 
        return acc_weekendElectricityProduction_kW.getIntegral_kWh() / 1000; 
    }
    public double getWeekendElectricitySelfConsumed_MWh() { 
        return max(0, getWeekendElectricityConsumed_MWh() - am_weekendImports_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh()/1000); 
    }
    public double getWeekendEnergyConsumed_MWh() { 
        return acc_weekendFinalEnergyConsumption_kW.getIntegral_kWh() / 1000; 
    }
    public double getWeekendEnergyProduced_MWh() { 
        return acc_weekendEnergyProduction_kW.getIntegral_kWh() / 1000; 
    }
    public double getWeekendEnergyExport_MWh() { 
        return am_weekendExports_kW.totalIntegral_kWh()/1000; 
    }
    public double getWeekendEnergyImport_MWh() { 
        return am_weekendImports_kW.totalIntegral_kWh()/1000; 
    }
    public double getWeekendEnergySelfConsumed_MWh() { 
        return max(0, getWeekendEnergyProduced_MWh() - getWeekendEnergyExport_MWh()); 
    }
    public double getWeekendExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_weekendExports_kW.get(EC).getIntegral_kWh() / 1000;
    }
    public double getWeekendImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_weekendImports_kW.get(EC).getIntegral_kWh() / 1000;
    }
    
    
//toString()
    public String toString() {
		return super.toString();
	}
}