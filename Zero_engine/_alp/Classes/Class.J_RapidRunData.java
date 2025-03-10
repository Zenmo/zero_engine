import java.util.EnumSet;
/**
 * J_RapidRunData
 */	
public class J_RapidRunData {
	
	public Agent parentAgent;
	EnumSet<OL_EnergyCarriers> v_activeEnergyCarriers;
	EnumSet<OL_EnergyCarriers> v_activeConsumptionEnergyCarriers;
	EnumSet<OL_EnergyCarriers> v_activeProductionEnergyCarriers;
	
	////Full simulation
	public J_AccumulatorMap am_totalBalanceAccumulators_kW;
    //public J_FlowsMap	fm_totalExports_MWh	;
    //public J_FlowsMap	fm_totallmports_MWh	;   
	
    public ZeroAccumulator acc_totalEnergyCurtailed_kW;
    public ZeroAccumulator acc_totalPrimaryEnergyProductionHeatpumps_kW;
    						
    
    public J_AccumulatorMap am_dailyAverageConsumptionAccumulators_kW;
    public J_AccumulatorMap am_dailyAverageProductionAccumulators_kW;
    
	public ZeroAccumulator acc_dailyAverageEnergyConsumption_kW;
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
    //public ZeroAccumulator acc_dailyAverageBatteriesStoredEnergy_MWh;
  
    ////Summer week
    public J_AccumulatorMap am_summerWeekBalanceAccumulators_kW;
    //public J_FlowsMap fm_summerWeekExports_MWh;
    //public J_FlowsMap fm_summerWeekImports_MWh;   
    
    public ZeroAccumulator acc_summerWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_summerWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_summerWeekEnergyConsumption_kW;
    public ZeroAccumulator acc_summerWeekEnergyCurtailed_kW;
    public ZeroAccumulator acc_summerWeekEnergyProduction_kW;

    public J_AccumulatorMap am_summerWeekConsumptionAccumulators_kW;
    public J_AccumulatorMap am_summerWeekProductionAccumulators_kW;  

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
    //public ZeroAccumulator acc_summerWeekBatteriesStoredEnergy_MWh;

    ////Winter week
    public J_AccumulatorMap am_winterWeekBalanceAccumulators_kW;
    //public J_FlowsMap fm_winterWeekExports_MWh;
    //public J_FlowsMap fm_winterWeekImports_MWh;
    
    public ZeroAccumulator acc_winterWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_winterWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_winterWeekEnergyConsumption_kW;
    public ZeroAccumulator acc_winterWeekEnergyProduction_kW;
    public ZeroAccumulator acc_winterWeekEnergyCurtailed_kW;  
    
    public J_AccumulatorMap am_winterWeekConsumptionAccumulators_kW;
    public J_AccumulatorMap am_winterWeekProductionAccumulators_kW;  
    
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
    //public ZeroAccumulator acc_winterWeekBatteriesStoredEnergy_MWh;
    
    ////Daytime / Nighttime
	public J_AccumulatorMap am_daytimeExports_kW;
    public J_AccumulatorMap am_daytimeImports_kW;
    
	public J_AccumulatorMap am_nighttimeExports_kW;
    public J_AccumulatorMap am_nighttimelmports_kW;
  
    public ZeroAccumulator acc_daytimeEnergyConsumption_kW;
    public ZeroAccumulator acc_daytimeEnergyProduction_kW;
    public ZeroAccumulator acc_daytimeElectricityConsumption_kW;
    public ZeroAccumulator acc_daytimeElectricityProduction_kW;
    
    //Weekend/day
    public ZeroAccumulator acc_weekendElectricityConsumption_kW;
    public ZeroAccumulator acc_weekendElectricityProduction_kW;
    public ZeroAccumulator acc_weekendEnergyConsumption_kW;
    public ZeroAccumulator acc_weekendEnergyProduction_kW;
    public J_AccumulatorMap am_weekendExports_kW;
    public J_AccumulatorMap am_weekendImports_kW;

    public ZeroAccumulator acc_weekdayElectricityConsumption_kW;
    public ZeroAccumulator acc_weekdayElectricityProduction_kW;
    public ZeroAccumulator acc_weekdayEnergyConsumption_kW;
    public ZeroAccumulator acc_weekdayEnergyProduction_kW;
    public J_AccumulatorMap am_weekdayExports_kW;
    public J_AccumulatorMap am_weekdayImports_kW;   
    
    /**
     * Default constructor
     */
    public J_RapidRunData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    
    public void initializeAccumulators(double simDuration_h, double timeStep_h, EnumSet<OL_EnergyCarriers> v_activeEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeConsumptionEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeProductionEnergyCarriers) {
    	this.v_activeEnergyCarriers = v_activeEnergyCarriers;
    	this.v_activeConsumptionEnergyCarriers = v_activeConsumptionEnergyCarriers;
    	this.v_activeProductionEnergyCarriers = v_activeProductionEnergyCarriers;
	    //========== TOTAL ACCUMULATORS ==========//
		am_totalBalanceAccumulators_kW.createEmptyAccumulators( v_activeEnergyCarriers, true, 24.0, simDuration_h );
	    am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, timeStep_h, simDuration_h) );
	    am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(v_activeConsumptionEnergyCarriers, true, 24.0, simDuration_h);
	    am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(v_activeProductionEnergyCarriers, true, 24.0, simDuration_h);
	
	    acc_dailyAverageEnergyProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageEnergyConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	
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
	
	    //acc_dailyAverageBatteriesStoredEnergy_MWh = new ZeroAccumulator(true, 24.0, simDuration_h);
	
	    //========== SUMMER WEEK ACCUMULATORS ==========//
	    am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	
	    acc_summerWeekEnergyProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekEnergyConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
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
	
	    //acc_summerWeekBatteriesStoredEnergy_MWh = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    //========== WINTER WEEK ACCUMULATORS ==========//
	    am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 168.0);
	
	    acc_winterWeekEnergyProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekEnergyConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
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
	
	    //acc_winterWeekBatteriesStoredEnergy_MWh = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    //========== DAYTIME ACCUMULATORS ==========//
	    am_daytimeImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
	    am_daytimeExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
	
	    acc_daytimeEnergyProduction_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    acc_daytimeEnergyConsumption_kW = new ZeroAccumulator(false, timeStep_h,0.5 * (simDuration_h));
	    //acc_daytimeEnergyCurtailed_kW = new ZeroAccumulator(false, timeStep_h, simDuration_h);
	    acc_daytimeElectricityProduction_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    acc_daytimeElectricityConsumption_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	
	    //========== WEEKEND ACCUMULATORS ==========//
	    am_weekendImports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    am_weekendExports_kW.createEmptyAccumulators( v_activeEnergyCarriers, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);
	
	    acc_weekendEnergyProduction_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    acc_weekendEnergyConsumption_kW = new ZeroAccumulator(false, timeStep_h,2 / 7  * (simDuration_h) + 48);
	    //acc_weekendEnergyCurtailed_kW = new ZeroAccumulator(false, timeStep_h, simDuration_h);
	    acc_weekendElectricityProduction_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    acc_weekendElectricityConsumption_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
		}


    public double getTotalElectricityConsumed_MWh() { 
        return am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000; 
    }
    
    public double getTotalElectricityProduced_MWh() { 
        return am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_kWh() / 1000; 
    }

    public double getTotalElectricitySelfConsumed_MWh() { 
        return max(0, getTotalElectricityProduced_MWh() - am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegralPos_kWh() / 1000); 
    }

    public double getTotalEnergyConsumed_MWh() { 
        return acc_dailyAverageEnergyConsumption_kW.getIntegral_kWh() / 1000; 
    }
    
    public double getTotalEnergyProduced_MWh() { 
        return acc_dailyAverageEnergyProduction_kW.getIntegral_kWh() / 1000; 
    }

    public double getTotalEnergyCurtailed_MWh() { 
        return acc_totalEnergyCurtailed_kW.getIntegral_kWh() / 1000; 
    }

    public double getTotalEnergyImport_MWh() { 
    	double totalEnergyImport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
    		totalEnergyImport_kWh+=am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_kWh();
    	}
        return totalEnergyImport_kWh/1000; 
    }
    
    public double getTotalEnergyExport_MWh() { 
    	double totalEnergyExport_kWh = 0.0;
    	for (OL_EnergyCarriers EC : v_activeEnergyCarriers) {
    		totalEnergyExport_kWh+=am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_kWh();
    	}
        return totalEnergyExport_kWh/1000; 
    }

    public double getTotalEnergySelfConsumed_MWh() { 
        return max(0, getTotalEnergyConsumed_MWh() - getTotalEnergyImport_MWh()); 
    }

    public double getTotalPrimaryEnergyProductionHeatpumps_MWh() { 
        return acc_totalPrimaryEnergyProductionHeatpumps_kW.getIntegral_kWh() / 1000; 
    }

    public double getDaytimeElectricityConsumed_MWh() { 
        return v_daytimeElectricityConsumed_MWh; 
    }

    public double getDaytimeElectricityProduced_MWh() { 
        return v_daytimeElectricityProduced_MWh; 
    }

    public double getDaytimeElectricitySelfConsumed_MWh() { 
        return v_daytimeElectricitySelfConsumed_MW; 
    }

    public double getDaytimeEnergyProduced_MWh() { 
        return v_daytimeEnergyProduced_MWh; 
    }

    public double getDaytimeEnergyConsumed_MWh() { 
        return v_daytimeEnergyConsumed_MWh; 
    }

    public double getDaytimeEnergyExport_MWh() { 
        return v_daytimeEnergyExport_MWh; 
    }

    public double getDaytimeEnergyImport_MWh() { 
        return v_daytimeEnergyImport_MWh; 
    }

    public double getDaytimeEnergySelfConsumed_MWh() { 
        return v_daytimeEnergySelfConsumed_MWh; 
    }

    public double getNighttimeElectricityConsumed_MWh() { 
        return v_nighttimeElectricityConsumed_MWh; 
    }

    public double getNighttimeElectricityProduced_MWh() { 
        return v_nighttimeElectricityProduced_MWh; 
    }

    public double getNighttimeElectricitySelfConsumed_MWh() { 
        return v_nighttimeElectricitySelfConsumed_MWh; 
    }

    public double getNighttimeEnergyConsumed_MWh() { 
        return v_nighttimeEnergyConsumed_MWh; 
    }

    public double getNighttimeEnergyExport_MWh() { 
        return v_nighttimeEnergyExport_MWh; 
    }

    public double getNighttimeEnergyImport_MWh() { 
        return v_nighttimeEnergyImport_MWh; 
    }

    public double getNighttimeEnergyProduced_MWh() { 
        return v_nighttimeEnergyProduced_MWh; 
    }

    public double getNighttimeEnergySelfConsumed_MWh() { 
        return v_nighttimeEnergySelfConsumed_MWh; 
    } 
    
    public double getWeekdayElectricityConsumed_MWh() { 
        return v_weekdayElectricityConsumed_MWh; 
    }

    public double getWeekdayElectricityProduced_MWh() { 
        return v_weekdayElectricityProduced_MWh; 
    }

    public double getWeekdayElectricitySelfConsumed_MWh() { 
        return v_weekdayElectricitySelfConsumed_MWh; 
    }

    public double getWeekdayEnergyConsumed_MWh() { 
        return v_weekdayEnergyConsumed_MWh; 
    }

    public double getWeekdayEnergyExport_MWh() { 
        return v_weekdayEnergyExport_MWh; 
    }

    public double getWeekdayEnergyImport_MWh() { 
        return v_weekdayEnergyImport_MWh; 
    }

    public double getWeekdayEnergyProduced_MWh() { 
        return v_weekdayEnergyProduced_MWh; 
    }

    public double getWeekdayEnergySelfConsumed_MWh() { 
        return v_weekdayEnergySelfConsumed_MWh; 
    }

    public double getWeekendElectricityProduced_MWh() { 
        return v_weekendElectricityProduced_MWh; 
    }

    public double getWeekendElectricityConsumed_MWh() { 
        return v_weekendElectricityConsumed_MWh; 
    }

    public double getWeekendElectricitySelfConsumed_MWh() { 
        return v_weekendElectricitySelfConsumed_MWh; 
    }

    public double getWeekendEnergyConsumed_MWh() { 
        return v_weekendEnergyConsumed_MWh; 
    }

    public double getWeekendEnergyExport_MWh() { 
        return v_weekendEnergyExport_MWh; 
    }

    public double getWeekendEnergyImport_MWh() { 
        return v_weekendEnergyImport_MWh; 
    }

    public double getWeekendEnergyProduced_MWh() { 
        return v_weekendEnergyProduced_MWh; 
    }

    public double getWeekendEnergySelfConsumed_MWh() { 
        return v_weekendEnergySelfConsumed_MWh; 
    }
    
    public String toString() {
		return super.toString();
	}
}