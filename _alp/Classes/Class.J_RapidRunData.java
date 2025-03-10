/**
 * J_RapidRunData
 */	
public class J_RapidRunData {
	
	public Agent parentAgent;
	
	////Full simulation
	public J_AccumulatorMap am_totalBalanceAccumulators_kW;
    public J_FlowsMap	fm_totalExports_MWh	;
    public J_FlowsMap	fm_totallmports_MWh	;   
	
    public ZeroAccumulator acc_totalEnergyCurtailed_kW;
    public ZeroAccumulator acc_totalPrimaryEnergy_ProductionHeatpumps_kW;
    
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
    public J_FlowsMap fm_summerWeekExports_MWh;
    public J_FlowsMap fm_summerWeekImports_MWh;   
    
    public ZeroAccumulator acc_summerWeekDelivery_Capacity_kw;
    public ZeroAccumulator acc_summerWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_summerWeekEnergy_Consumption_kW;
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
    public ZeroAccumulator acc_summerWeekPrimary_Energy_ProductionHeatpumps_kW;
    //public ZeroAccumulator acc_summerWeekBatteriesStoredEnergy_MWh;

    ////Winter week
    public J_AccumulatorMap am_winterWeekBalanceAccumulators_kW;
    public J_FlowsMap fm_winterWeekExports_MWh;
    public J_FlowsMap fm_winterWeekImports_MWh;
    
    public ZeroAccumulator acc_winterWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_winterWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_winterWeekEnergy_Consumption_kW;
    public ZeroAccumulator acc_winterWeekEnergy_Production_kW;
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
    public ZeroAccumulator acc_winterWeekPrimaryEnergy_Production_Heatpumps_kW;
    //public ZeroAccumulator acc_winterWeekBatteriesStoredEnergy_MWh;

    
    ////Daytime / Nighttime
	public J_AccumulatorMap am_daytimeExports_kW;
    public J_AccumulatorMap am_daytimelmports_kW;
    
	public J_AccumulatorMap am_nighttimeExports_kW;
    public J_AccumulatorMap am_nighttimelmports_kW;
  
    public ZeroAccumulator acc_daytimeEnergyConsumption_kW;
    public ZeroAccumulator acc_daytimeEnergyProduction_kW;
    
    
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


    
    //VOOR NU VARIABELEN: ZET DIT METEEN IN DE GETTER
    public	double	v_totalElectricity_Produced_MWh;
    public	double	v_totalElectricityConsumed_MWh;
    public	double	V_totalElectricitySelfConsumed_MWh;
    public	double	v_totalEnergy_Produced_MWh;
    public	double	v_totalEnergyConsumed_MWh;
    public	double	v_totalEnergyCurtailed_MWh;
    public	double	v_totalEnergyExport_MWh;
    public	double	v_totalEnergyImport_MWh;
    public	double	v_totalEnergySelfConsumed_MWh;
    public	double	v_totalPrimaryEnergyProduction_Heatpumps_MWh;   
   

    public	double	v_summerWeekElectricity_Produced_MWh;
    public	double	v_summerWeekElectricityConsumed_MWh;
    public	double	v_summerWeekElectricitySelfConsumed_MWh;
    public	double	v_summerWeekEnergy_Consumed_MWh;
    public	double	v_summerWeekEnergy_Produced_MWh;
    public	double	v_summerWeekEnergyCurtailed_MWh;
    public	double	v_summerWeekEnergyExport_MWh;
    public	double	v_summerWeekEnergyImport_MWh;
    public	double	v_summerWeekEnergySelfConsumed_MWh;
    public	double	v_summerWeek_Primary_Energy_ProductionHeatpumps_MWh;
    
    public	double	v_winterWeekElectricity_Consumed_MWh;
    public	double	v_winterWeekElectricity_Produced_MWh;
    public	double	v_winterWeekElectricitySelfConsumed_MWh;
    public	double	v_winterWeekEnergyConsumed_MWh;
    public	double	v_winterWeekEnergyCurtailed_MWh;
    public	double	v_winterWeekEnergyExport_MWh;
    public	double	v_winterWeekEnergyImport_MWh;
    public	double	v_winterWeekEnergyProduced_MWh;
    public	double	v_winterWeekEnergySelfConsumed_MWh;
    public	double	v_winterWeekPrimaryEnergyProductionHeatpumps_MWh;
    
    public	double	v_daytimeElectricity_Consumed_MWh;
    public	double	v_daytimeElectricity_Produced_MWh;
    public	double	v_daytimeElectricitySelfConsumed_MW;
    public	double	v_daytimeEnergy_Produced_MWh;
    public	double	v_daytimeEnergyConsumed_MWh;
    public	double	v_daytimeEnergyExport_MWh;
    public	double	v_daytimeEnergyImport_MWh;
    public	double	v_daytimeEnergySelfConsumed_MWh;
    
    public double v_nighttimeElectricityConsumed_MWh;
    public double v_nighttimeElectricityProduced_MWh;
    public double v_nighttimeElectricitySelfConsumed_MWh;
    public double v_nighttimeEnergyConsumed_MWh;
    public double v_nighttimeEnergyExport_MWh;
    public double v_nighttimeEnergyImport_MWh;
    public double v_nighttimeEnergyProduced_MWh;
    public double v_nighttimeEnergySelfConsumed_MWh;
    public double v_weekdayElectricityConsumed_MWh;
    public double v_weekdayElectricityProduced_MWh;
    public double v_weekdayElectricitySelfConsumed_MWh;
    public double v_weekdayEnergyConsumed_MWh;
    public double v_weekdayEnergyExport_MWh;
    public double v_weekdayEnergyImport_MWh;
    public double v_weekdayEnergyProduced_MWh;
    public double v_weekdayEnergySelfConsumed_MWh;
    public double v_weekendElectricity_Produced_MWh;
    public double v_weekendElectricityConsumed_MWh;
    public double v_weekendElectricitySelfConsumed_MWh;
    public double v_weekendEnergyConsumed_MWh;
    public double v_weekendEnergyExport_MWh;
    public double v_weekendEnergyImport_MWh;
    public double v_weekendEnergyProduced_MWh;
    public double v_weekendEnergySelfConsumed_MWh;
    
    /**
     * Default constructor
     */
    public J_RapidRunData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }
    

    public double getTotalElectricityProduced_MWh() { 
        return v_totalElectricity_Produced_MWh; 
    }

    public double getTotalElectricityConsumed_MWh() { 
        return v_totalElectricityConsumed_MWh; 
    }

    public double getTotalElectricitySelfConsumed_MWh() { 
        return V_totalElectricitySelfConsumed_MWh; 
    }

    public double getTotalEnergyProduced_MWh() { 
        return v_totalEnergy_Produced_MWh; 
    }

    public double getTotalEnergyConsumed_MWh() { 
        return v_totalEnergyConsumed_MWh; 
    }

    public double getTotalEnergyCurtailed_MWh() { 
        return v_totalEnergyCurtailed_MWh; 
    }

    public double getTotalEnergyExport_MWh() { 
        return v_totalEnergyExport_MWh; 
    }

    public double getTotalEnergyImport_MWh() { 
        return v_totalEnergyImport_MWh; 
    }

    public double getTotalEnergySelfConsumed_MWh() { 
        return v_totalEnergySelfConsumed_MWh; 
    }

    public double getTotalPrimaryEnergyProductionHeatpumps_MWh() { 
        return v_totalPrimaryEnergyProduction_Heatpumps_MWh; 
    }

    public double getDaytimeElectricityConsumed_MWh() { 
        return v_daytimeElectricity_Consumed_MWh; 
    }

    public double getDaytimeElectricityProduced_MWh() { 
        return v_daytimeElectricity_Produced_MWh; 
    }

    public double getDaytimeElectricitySelfConsumed_MWh() { 
        return v_daytimeElectricitySelfConsumed_MW; 
    }

    public double getDaytimeEnergyProduced_MWh() { 
        return v_daytimeEnergy_Produced_MWh; 
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
        return v_weekendElectricity_Produced_MWh; 
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