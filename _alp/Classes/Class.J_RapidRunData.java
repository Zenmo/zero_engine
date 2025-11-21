import java.util.EnumSet;
import zeroPackage.ZeroMath;
/**
 * J_RapidRunData
 */	
import com.fasterxml.jackson.annotation.JsonIgnoreType;
@JsonIgnoreType
public class J_RapidRunData {
	private boolean storeTotalAssetFlows = true;
	public Agent parentAgent;
	private double timeStep_h;
	public EnumSet<OL_EnergyCarriers> activeEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers;
	public EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers;
	
	public J_AssetsMetaData assetsMetaData;
	public J_ConnectionMetaData connectionMetaData;
	
	////Full simulation
	public J_AccumulatorMap<OL_EnergyCarriers> am_totalBalanceAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);

    public ZeroAccumulator acc_totalEnergyCurtailed_kW;
    public ZeroAccumulator acc_totalPrimaryEnergyProductionHeatpumps_kW;
    
    public J_AccumulatorMap<OL_EnergyCarriers> am_dailyAverageConsumptionAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    public J_AccumulatorMap<OL_EnergyCarriers> am_dailyAverageProductionAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    
	public ZeroAccumulator acc_dailyAverageFinalEnergyConsumption_kW;
	public ZeroAccumulator acc_dailyAverageEnergyProduction_kW;

	public J_AccumulatorMap<OL_AssetFlowCategories> am_assetFlowsAccumulators_kW = new J_AccumulatorMap(OL_AssetFlowCategories.class);

    public ZeroTimeSeries ts_dailyAverageBatteriesStoredEnergy_MWh;
    //public ZeroTimeSeries ts_dailyAverageBatteriesSOC_fr;
    
    ////Summer week
    public J_AccumulatorMap am_summerWeekBalanceAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);

    public ZeroAccumulator acc_summerWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_summerWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_summerWeekFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_summerWeekEnergyCurtailed_kW;
    public ZeroAccumulator acc_summerWeekEnergyProduction_kW;
    public ZeroAccumulator acc_summerWeekPrimaryEnergyProductionHeatpumps_kW;
    
    public J_AccumulatorMap am_summerWeekConsumptionAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    public J_AccumulatorMap am_summerWeekProductionAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);

    public J_AccumulatorMap<OL_AssetFlowCategories> am_assetFlowsSummerWeek_kW = new J_AccumulatorMap(OL_AssetFlowCategories.class);
    public ZeroTimeSeries ts_summerWeekBatteriesStoredEnergy_MWh;
    public ZeroTimeSeries ts_summerWeekBatteriesSOC_fr;
    
    ////Winter week
    public J_AccumulatorMap am_winterWeekBalanceAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    
    public ZeroAccumulator acc_winterWeekDeliveryCapacity_kW;
    public ZeroAccumulator acc_winterWeekFeedinCapacity_kW;
    
    public ZeroAccumulator acc_winterWeekFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_winterWeekEnergyProduction_kW;
    public ZeroAccumulator acc_winterWeekEnergyCurtailed_kW;
    public ZeroAccumulator acc_winterWeekPrimaryEnergyProductionHeatpumps_kW;
    
    public J_AccumulatorMap am_winterWeekConsumptionAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    public J_AccumulatorMap am_winterWeekProductionAccumulators_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);  
    
    public J_AccumulatorMap<OL_AssetFlowCategories> am_assetFlowsWinterWeek_kW = new J_AccumulatorMap(OL_AssetFlowCategories.class);
    public ZeroTimeSeries ts_winterWeekBatteriesStoredEnergy_MWh;
    public ZeroTimeSeries ts_winterWeekBatteriesSOC_fr;
    
    ////Daytime / Nighttime
	public J_AccumulatorMap am_daytimeExports_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    public J_AccumulatorMap am_daytimeImports_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    	
    public ZeroAccumulator acc_daytimeFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_daytimeEnergyProduction_kW;
    public ZeroAccumulator acc_daytimeElectricityConsumption_kW;
    public ZeroAccumulator acc_daytimeElectricityProduction_kW;
    
    public ZeroAccumulator acc_daytimePrimaryEnergyProductionHeatpumps_kW;
    
    public J_AccumulatorMap<OL_AssetFlowCategories> am_assetFlowsDaytime_kW = new J_AccumulatorMap(OL_AssetFlowCategories.class);
    
    //Weekend/day
    public J_AccumulatorMap am_weekendExports_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    public J_AccumulatorMap am_weekendImports_kW = new J_AccumulatorMap(OL_EnergyCarriers.class);
    
    public ZeroAccumulator acc_weekendElectricityConsumption_kW;
    public ZeroAccumulator acc_weekendElectricityProduction_kW;
    public ZeroAccumulator acc_weekendFinalEnergyConsumption_kW;
    public ZeroAccumulator acc_weekendEnergyProduction_kW;

    public ZeroAccumulator acc_weekendPrimaryEnergyProductionHeatpumps_kW;
    
    public J_AccumulatorMap<OL_AssetFlowCategories> am_assetFlowsWeekend_kW = new J_AccumulatorMap(OL_AssetFlowCategories.class);


    
    /**
     * Default constructor
     */
    public J_RapidRunData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    	if (parentAgent instanceof GridConnection) {
    		if (((GridConnection)parentAgent).p_owner == null) {
    			storeTotalAssetFlows = false;
    		} else {
	    		if (!((GridConnection)parentAgent).p_owner.p_detailedCompany && !((GridConnection)parentAgent).v_hasQuarterHourlyValues) {
	    			storeTotalAssetFlows = false;
	    		}
    		}
    	}
    }
    
    public boolean setStoreTotalAssetFlows(boolean storeTotalAssetFlows) {
    	this.storeTotalAssetFlows = storeTotalAssetFlows;
    	return this.storeTotalAssetFlows == storeTotalAssetFlows; // Check if it has succeeded (Always for now!)
    }
    
    public boolean getStoreTotalAssetFlows() {
    	return this.storeTotalAssetFlows;
    }
    
    public void initializeAccumulators(double simDuration_h, double timeStep_h, EnumSet<OL_EnergyCarriers> v_activeEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeConsumptionEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeProductionEnergyCarriers, EnumSet<OL_AssetFlowCategories> activeAssetFlows) {
    	this.timeStep_h = timeStep_h;
    	this.activeEnergyCarriers = EnumSet.copyOf(v_activeEnergyCarriers);
    	this.activeConsumptionEnergyCarriers = EnumSet.copyOf(v_activeConsumptionEnergyCarriers);
    	this.activeProductionEnergyCarriers = EnumSet.copyOf(v_activeProductionEnergyCarriers);

	    //========== TOTAL ACCUMULATORS ==========//
		am_totalBalanceAccumulators_kW.createEmptyAccumulators( this.activeEnergyCarriers, true, 24.0, simDuration_h );
	    am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, timeStep_h, simDuration_h) );
	    am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(this.activeConsumptionEnergyCarriers, true, 24.0, simDuration_h);
	    am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(this.activeProductionEnergyCarriers, true, 24.0, simDuration_h);
	
	    acc_dailyAverageEnergyProduction_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_dailyAverageFinalEnergyConsumption_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	
	    acc_totalEnergyCurtailed_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	    acc_totalPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, 24.0, simDuration_h);
	
	    //========== ASSET FLOWS ==========//
    	
	    if (storeTotalAssetFlows) {
	    	am_assetFlowsAccumulators_kW.createEmptyAccumulators( activeAssetFlows, true, timeStep_h, simDuration_h);
	    	if (this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
		    	ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, simDuration_h);
	    	} /*else {
	    		ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(24, simDuration_h);
	    	}*/
	    } else {
	    	am_assetFlowsAccumulators_kW.createEmptyAccumulators( activeAssetFlows, true, 24.0, simDuration_h);
	    	if (this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
	    		ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(24, simDuration_h);
	    	} /*else {
	    		ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(24, simDuration_h);	    		
	    	}*/
	    }	  
	    
	    //========== SUMMER WEEK ACCUMULATORS ==========//
	    am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(this.activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(this.activeConsumptionEnergyCarriers, true, timeStep_h, 168.0);
	    am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(this.activeProductionEnergyCarriers, true, timeStep_h, 168.0);
	
	    acc_summerWeekEnergyProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekFinalEnergyConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_summerWeekEnergyCurtailed_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_summerWeekFeedinCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_summerWeekDeliveryCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
    	am_assetFlowsSummerWeek_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, true, timeStep_h, 7*24);
    	ts_summerWeekBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, 168.0);
	    ts_summerWeekBatteriesSOC_fr = new ZeroTimeSeries(timeStep_h, 168.0);
	    
	    //========== WINTER WEEK ACCUMULATORS ==========//
	    am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(this.activeEnergyCarriers, true, timeStep_h, 168.0);
	    am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(this.activeConsumptionEnergyCarriers, true, timeStep_h, 168.0);
	    am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(this.activeProductionEnergyCarriers, true, timeStep_h, 168.0);
	
	    acc_winterWeekEnergyProduction_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekFinalEnergyConsumption_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_winterWeekEnergyCurtailed_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	
	    acc_winterWeekFeedinCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    acc_winterWeekDeliveryCapacity_kW = new ZeroAccumulator(true, timeStep_h, 168.0);
	    
		am_assetFlowsWinterWeek_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, true, timeStep_h, 7*24);
	    ts_winterWeekBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, 168.0);
	    ts_winterWeekBatteriesSOC_fr = new ZeroTimeSeries(timeStep_h, 168.0);
	    
	    //========== DAYTIME ACCUMULATORS ==========//
	    am_daytimeImports_kW.createEmptyAccumulators( this.activeConsumptionEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
	    am_daytimeExports_kW.createEmptyAccumulators( this.activeProductionEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
	
	    acc_daytimeEnergyProduction_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    acc_daytimeFinalEnergyConsumption_kW = new ZeroAccumulator(false, timeStep_h,0.5 * (simDuration_h));
	    //acc_daytimeEnergyCurtailed_kW = new ZeroAccumulator(false, timeStep_h, simDuration_h);
	    acc_daytimeElectricityProduction_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    acc_daytimeElectricityConsumption_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	
	    acc_daytimePrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(false, timeStep_h, 0.5 * (simDuration_h));
	    
		am_assetFlowsDaytime_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, false, timeStep_h, 0.5 * (simDuration_h));

	    //========== WEEKEND ACCUMULATORS ==========//
	    am_weekendImports_kW.createEmptyAccumulators( this.activeConsumptionEnergyCarriers, false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    am_weekendExports_kW.createEmptyAccumulators( this.activeProductionEnergyCarriers, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);
	
	    acc_weekendEnergyProduction_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    acc_weekendFinalEnergyConsumption_kW = new ZeroAccumulator(false, timeStep_h,2 / 7  * (simDuration_h) + 48);
	    //acc_weekendEnergyCurtailed_kW = new ZeroAccumulator(false, timeStep_h, simDuration_h);
	    acc_weekendElectricityProduction_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	    acc_weekendElectricityConsumption_kW = new ZeroAccumulator(false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
	
	    acc_weekendPrimaryEnergyProductionHeatpumps_kW = new ZeroAccumulator(false, timeStep_h,  2 / 7 * (simDuration_h) + 48);
	    
	    am_assetFlowsWeekend_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);
    }

    public void resetAccumulators(double simDuration_h, double timeStep_h, EnumSet<OL_EnergyCarriers> v_activeEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeConsumptionEnergyCarriers, EnumSet<OL_EnergyCarriers> v_activeProductionEnergyCarriers) {
    	this.activeEnergyCarriers = EnumSet.copyOf(v_activeEnergyCarriers);
    	this.activeConsumptionEnergyCarriers = EnumSet.copyOf(v_activeConsumptionEnergyCarriers);
    	this.activeProductionEnergyCarriers = EnumSet.copyOf(v_activeProductionEnergyCarriers);
    	//Total simulation
		am_totalBalanceAccumulators_kW.createEmptyAccumulators( this.activeEnergyCarriers, true, 24.0, simDuration_h );
    	am_totalBalanceAccumulators_kW.put( OL_EnergyCarriers.ELECTRICITY, new ZeroAccumulator(true, timeStep_h, simDuration_h) );
    	am_dailyAverageConsumptionAccumulators_kW.createEmptyAccumulators(this.activeConsumptionEnergyCarriers, true, 24.0, simDuration_h);
    	am_dailyAverageProductionAccumulators_kW.createEmptyAccumulators(this.activeProductionEnergyCarriers, true, 24.0, simDuration_h);

    	acc_dailyAverageEnergyProduction_kW.reset();
    	acc_dailyAverageFinalEnergyConsumption_kW.reset();

    	acc_totalEnergyCurtailed_kW.reset();
    	acc_totalPrimaryEnergyProductionHeatpumps_kW.reset();

    	if (storeTotalAssetFlows) {
  	    	am_assetFlowsAccumulators_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, true, timeStep_h, simDuration_h);
  	    	if (this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
		    	ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(timeStep_h, simDuration_h);
	    	} 
  	    } else {
  	    	am_assetFlowsAccumulators_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, true, 24.0, simDuration_h);	
  	    	if (this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
		    	ts_dailyAverageBatteriesStoredEnergy_MWh = new ZeroTimeSeries(24, simDuration_h);
	    	} 
  	    }

        //ts_dailyAverageBatteriesStoredEnergy_MWh.reset();
        
    	//Summerweek
    	am_summerWeekBalanceAccumulators_kW.createEmptyAccumulators(this.activeEnergyCarriers, true, timeStep_h, 24*7);
    	am_summerWeekConsumptionAccumulators_kW.createEmptyAccumulators(this.activeConsumptionEnergyCarriers, true, timeStep_h, 24*7);
    	am_summerWeekProductionAccumulators_kW.createEmptyAccumulators(this.activeProductionEnergyCarriers, true, timeStep_h, 24*7);

    	acc_summerWeekEnergyProduction_kW.reset();
    	acc_summerWeekFinalEnergyConsumption_kW.reset();

    	acc_summerWeekEnergyCurtailed_kW.reset();
    	acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.reset();

    	acc_summerWeekFeedinCapacity_kW.reset();
    	acc_summerWeekDeliveryCapacity_kW.reset();

    	am_assetFlowsSummerWeek_kW.createEmptyAccumulators(this.assetsMetaData.activeAssetFlows, true, timeStep_h, 24*7);
	    ts_summerWeekBatteriesStoredEnergy_MWh.reset();
	    ts_summerWeekBatteriesSOC_fr.reset();
	    
    	//Winterweek
    	am_winterWeekBalanceAccumulators_kW.createEmptyAccumulators(v_activeEnergyCarriers, true, timeStep_h, 24*7);
    	am_winterWeekConsumptionAccumulators_kW.createEmptyAccumulators(this.activeConsumptionEnergyCarriers, true, timeStep_h, 24*7);
    	am_winterWeekProductionAccumulators_kW.createEmptyAccumulators(this.activeProductionEnergyCarriers, true, timeStep_h, 24*7);

    	acc_winterWeekEnergyProduction_kW.reset();
    	acc_winterWeekFinalEnergyConsumption_kW.reset();

    	acc_winterWeekEnergyCurtailed_kW.reset();
    	acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.reset();

    	acc_winterWeekFeedinCapacity_kW.reset();
    	acc_winterWeekDeliveryCapacity_kW.reset();
    	am_assetFlowsWinterWeek_kW.createEmptyAccumulators(this.assetsMetaData.activeAssetFlows, true, timeStep_h, 24*7);    	

    	ts_winterWeekBatteriesStoredEnergy_MWh.reset();
	    ts_winterWeekBatteriesSOC_fr.reset();
	    
    	// Daytime 
    	am_daytimeImports_kW.createEmptyAccumulators( this.activeConsumptionEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
    	am_daytimeExports_kW.createEmptyAccumulators( this.activeProductionEnergyCarriers, false, timeStep_h, 0.5 * (simDuration_h));
    	
    	acc_daytimeElectricityProduction_kW.reset();
    	acc_daytimeElectricityConsumption_kW.reset();
    	acc_daytimeEnergyProduction_kW.reset();
    	acc_daytimeFinalEnergyConsumption_kW.reset();

    	acc_daytimePrimaryEnergyProductionHeatpumps_kW.reset();
    	
		am_assetFlowsDaytime_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, false, timeStep_h, 0.5 * (simDuration_h));

    	// Weekend
    	am_weekendImports_kW.createEmptyAccumulators( this.activeConsumptionEnergyCarriers, false, timeStep_h, 2 / 7  * (simDuration_h) + 48);
    	am_weekendExports_kW.createEmptyAccumulators( this.activeProductionEnergyCarriers, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);

    	acc_weekendElectricityProduction_kW.reset();
    	acc_weekendElectricityConsumption_kW.reset();
    	acc_weekendEnergyProduction_kW.reset();
    	acc_weekendFinalEnergyConsumption_kW.reset();
    	
    	acc_weekendPrimaryEnergyProductionHeatpumps_kW.reset();

    	am_assetFlowsWeekend_kW.createEmptyAccumulators( this.assetsMetaData.activeAssetFlows, false, timeStep_h, 2 / 7 * (simDuration_h) + 48);

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
    	
    	clone.am_assetFlowsAccumulators_kW = this.am_assetFlowsAccumulators_kW.getClone();
    	if(this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
    		clone.ts_dailyAverageBatteriesStoredEnergy_MWh=this.ts_dailyAverageBatteriesStoredEnergy_MWh.getClone();
    	}
        //clone.ts_dailyAverageBatteriesSOC_fr=this.ts_dailyAverageBatteriesSOC_fr.getClone();
        
        ////Summer week
        clone.am_summerWeekBalanceAccumulators_kW=this.am_summerWeekBalanceAccumulators_kW.getClone();
        clone.acc_summerWeekDeliveryCapacity_kW=this.acc_summerWeekDeliveryCapacity_kW.getClone();
        clone.acc_summerWeekFeedinCapacity_kW=this.acc_summerWeekFeedinCapacity_kW.getClone();
        clone.acc_summerWeekFinalEnergyConsumption_kW=this.acc_summerWeekFinalEnergyConsumption_kW.getClone();
        clone.acc_summerWeekEnergyCurtailed_kW=this.acc_summerWeekEnergyCurtailed_kW.getClone();
        clone.acc_summerWeekEnergyProduction_kW=this.acc_summerWeekEnergyProduction_kW.getClone();
        clone.am_summerWeekConsumptionAccumulators_kW=this.am_summerWeekConsumptionAccumulators_kW.getClone();
        clone.am_summerWeekProductionAccumulators_kW=this.am_summerWeekProductionAccumulators_kW.getClone();
        
        clone.am_assetFlowsSummerWeek_kW = this.am_assetFlowsSummerWeek_kW.getClone();
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

        clone.am_assetFlowsWinterWeek_kW = this.am_assetFlowsWinterWeek_kW.getClone();
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
        clone.acc_daytimePrimaryEnergyProductionHeatpumps_kW = acc_daytimePrimaryEnergyProductionHeatpumps_kW.getClone();
        clone.am_assetFlowsDaytime_kW = am_assetFlowsDaytime_kW.getClone();
        //Weekend/day
        clone.acc_weekendElectricityConsumption_kW = this.acc_weekendElectricityConsumption_kW.getClone();
        clone.acc_weekendElectricityProduction_kW = this.acc_weekendElectricityProduction_kW.getClone();
        clone.acc_weekendFinalEnergyConsumption_kW = this.acc_weekendFinalEnergyConsumption_kW.getClone();
        clone.acc_weekendEnergyProduction_kW = this.acc_weekendEnergyProduction_kW.getClone();
        clone.am_weekendExports_kW = this.am_weekendExports_kW.getClone();
        clone.am_weekendImports_kW = this.am_weekendImports_kW.getClone();
        clone.acc_weekendPrimaryEnergyProductionHeatpumps_kW = acc_weekendPrimaryEnergyProductionHeatpumps_kW.getClone();
        clone.am_assetFlowsWeekend_kW = am_assetFlowsWeekend_kW.getClone();
        
        clone.assetsMetaData = this.assetsMetaData.getClone();
        clone.connectionMetaData = this.connectionMetaData.getClone();
        return clone;

    }
    
    public void addTimeStep(J_FlowsMap fm_currentBalanceFlows_kW, J_FlowsMap fm_currentConsumptionFlows_kW, J_FlowsMap fm_currentProductionFlows_kW, J_ValueMap fm_currentAssetFlows_kW, double v_currentPrimaryEnergyProduction_kW, double v_currentFinalEnergyConsumption_kW, double v_currentPrimaryEnergyProductionHeatpumps_kW, double v_currentEnergyCurtailed_kW, double currentStoredEnergyBatteries_MWh, EnergyModel energyModel) {
    	//J_AssetFlows assetFlows = new J_AssetFlows();
    	
    	//EnergyCarrier import/exports
    	for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    		this.am_totalBalanceAccumulators_kW.get(EC).addStep(  fm_currentBalanceFlows_kW.get(EC) );
    	}
    	
    	//AssetFlows
	    for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) {
	    	this.am_assetFlowsAccumulators_kW.get(AC).addStep ( fm_currentAssetFlows_kW.get(AC) );
	    }
    	
	    // Daytime totals. Use overal-total minus daytime total to get nighttime totals.
    	if(energyModel.b_isDaytime) { 
    		
    		for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    			double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);    			
    			if(activeConsumptionEnergyCarriers.contains(EC)){
    				am_daytimeImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
    			}
    			if(activeProductionEnergyCarriers.contains(EC)){
    				am_daytimeExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
    			}
    		}
    		
    		acc_daytimeElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
    		acc_daytimeElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );	
    		acc_daytimeEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
    		acc_daytimeFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);	
    		acc_daytimePrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

    	    for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) {
    	    	this.am_assetFlowsDaytime_kW.get(AC).addStep( fm_currentAssetFlows_kW.get(AC) );
    	    }
    	}

    	// Weekend totals. Use overal-totals minus weekend totals to get weekday totals.
    	if (!energyModel.b_isWeekday) { // 
    		for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    			double currentBalance_kW = fm_currentBalanceFlows_kW.get(EC);
    			if(activeConsumptionEnergyCarriers.contains(EC)){
    				am_weekendImports_kW.get(EC).addStep(max( 0, currentBalance_kW ));
    			}
    			if(activeProductionEnergyCarriers.contains(EC)){
    				am_weekendExports_kW.get(EC).addStep(max( 0, -currentBalance_kW ));
    			}
    		}
    		
    		acc_weekendElectricityProduction_kW.addStep(fm_currentProductionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
    		acc_weekendElectricityConsumption_kW.addStep(fm_currentConsumptionFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) );
    		acc_weekendEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
    		acc_weekendFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
    		acc_weekendPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);
    	    
    		for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) {
    	    	this.am_assetFlowsWeekend_kW.get(AC).addStep( fm_currentAssetFlows_kW.get(AC) );
    	    }
    	}

    	//========== SUMMER WEEK ==========//
    	if (energyModel.b_isSummerWeek){
    		for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    			am_summerWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
    		}
    		for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
    			am_summerWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );

    		}
    		for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
    			am_summerWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
    		}
    				
    		acc_summerWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
    		acc_summerWeekFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

    		acc_summerWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
    		acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

    		acc_summerWeekDeliveryCapacity_kW.addStep( connectionMetaData.physicalCapacity_kW );
    		acc_summerWeekFeedinCapacity_kW.addStep( -connectionMetaData.physicalCapacity_kW );
    		
    		//AssetFlows
    	    for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) {
    	    	this.am_assetFlowsSummerWeek_kW.get(AC).addStep ( fm_currentAssetFlows_kW.get(AC) );
    	    }
    	
    		ts_summerWeekBatteriesStoredEnergy_MWh.addStep(currentStoredEnergyBatteries_MWh);
    		if(assetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
    			ts_summerWeekBatteriesSOC_fr.addStep(currentStoredEnergyBatteries_MWh/assetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
    		}
    		else{
    			ts_summerWeekBatteriesSOC_fr.addStep(0);	
    		}
    	}

    	//========== WINTER WEEK ==========// 
    	if (energyModel.b_isWinterWeek){
    		for (OL_EnergyCarriers EC : activeEnergyCarriers) {
    			am_winterWeekBalanceAccumulators_kW.get(EC).addStep( fm_currentBalanceFlows_kW.get(EC) );
    		}
    		for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
    		    am_winterWeekConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
    		}
    		for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
    		    am_winterWeekProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
    		}
    		
    		acc_winterWeekEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
    		acc_winterWeekFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);

    		acc_winterWeekEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
    		acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);	

    		acc_winterWeekDeliveryCapacity_kW.addStep( connectionMetaData.physicalCapacity_kW );
    		acc_winterWeekFeedinCapacity_kW.addStep( -connectionMetaData.physicalCapacity_kW );
    		//AssetFlows
    	    for (OL_AssetFlowCategories AC : assetsMetaData.activeAssetFlows) {
    	    	this.am_assetFlowsWinterWeek_kW.get(AC).addStep ( fm_currentAssetFlows_kW.get(AC)  );
    	    }
    	 		
    		
    		ts_winterWeekBatteriesStoredEnergy_MWh.addStep(currentStoredEnergyBatteries_MWh);

    		if(assetsMetaData.totalInstalledBatteryStorageCapacity_MWh > 0){
    			ts_winterWeekBatteriesSOC_fr.addStep(currentStoredEnergyBatteries_MWh/assetsMetaData.totalInstalledBatteryStorageCapacity_MWh);	
    		}
    		else{
    			ts_winterWeekBatteriesSOC_fr.addStep(0);	
    		}
    	}

    	//========== TOTALS / DAILY AVERAGES ==========//
    	for (OL_EnergyCarriers EC : activeConsumptionEnergyCarriers) {
    	    am_dailyAverageConsumptionAccumulators_kW.get(EC).addStep( fm_currentConsumptionFlows_kW.get(EC) );
    	}
    	for (OL_EnergyCarriers EC : activeProductionEnergyCarriers) {
    	    am_dailyAverageProductionAccumulators_kW.get(EC).addStep( fm_currentProductionFlows_kW.get(EC) );
    	}
    	acc_dailyAverageEnergyProduction_kW.addStep(v_currentPrimaryEnergyProduction_kW);
    	acc_dailyAverageFinalEnergyConsumption_kW.addStep(v_currentFinalEnergyConsumption_kW);
    	acc_totalEnergyCurtailed_kW.addStep(v_currentEnergyCurtailed_kW);
    	acc_totalPrimaryEnergyProductionHeatpumps_kW.addStep(v_currentPrimaryEnergyProductionHeatpumps_kW);
    	
    	if(this.assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
    		ts_dailyAverageBatteriesStoredEnergy_MWh.addStep(currentStoredEnergyBatteries_MWh);
    	}

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
    	return max(0, getHighestBalance_kW(OL_EnergyCarriers.ELECTRICITY));
    }
  
    public double getPeakFeedin_kW() {
    	return max(0, -getLowestBalance_kW(OL_EnergyCarriers.ELECTRICITY));	
    }
    
    
    public double getHighestBalance_kW(OL_EnergyCarriers EC) {
    	return max(this.am_totalBalanceAccumulators_kW.get(EC).getTimeSeries_kW());
    }
    
    public double getLowestBalance_kW(OL_EnergyCarriers EC) {
    	return min(this.am_totalBalanceAccumulators_kW.get(EC).getTimeSeries_kW());
    }   
    
    public Double getHighestBalanceTime_h(OL_EnergyCarriers EC) {
    	double[] ECBalance_kW = am_totalBalanceAccumulators_kW.get(EC).getTimeSeries_kW();

    	Integer maxIndex = 0; // index with peak import
    	for (int i = 1; i < ECBalance_kW.length; i++) {
    	    if (ECBalance_kW[i] > ECBalance_kW[maxIndex]) {
    	        maxIndex = i;
    	    }
    	}
    	return maxIndex*timeStep_h;
    }
    
    public Double getLowestBalanceTime_h(OL_EnergyCarriers EC) {
    	double[] ECBalance_kW = am_totalBalanceAccumulators_kW.get(EC).getTimeSeries_kW();

    	Integer minIndex = 0; // index with peak export
    	for (int i = 1; i < ECBalance_kW.length; i++) {
    	    if (ECBalance_kW[i] < ECBalance_kW[minIndex]) {
    	        minIndex = i;
    	    }
    	}
    	return minIndex*timeStep_h;
    }
    
    public double getHighestBalanceWeekStart_h(OL_EnergyCarriers EC) {
    	double peakTime_h = getHighestBalanceTime_h(EC);
    	return getPeakWeekStart_h(peakTime_h);
    }
 
    public double getLowestBalanceWeekStart_h(OL_EnergyCarriers EC) {
    	double peakTime_h = getLowestBalanceTime_h(EC);
    	return getPeakWeekStart_h(peakTime_h);
    }
    
    public double getPeakWeekStart_h(double peakTime_h) {
    	double duration_h = am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getDuration(); // Take random balance accumulator for sim duration
    	return min(duration_h-7*24,max(0,peakTime_h - 3.5*24)); // return start of week hour, capped between 0 and simDuration_h - 7*24
    }

    
    public ZeroTimeSeries getBatteriesSOCts_fr() {
    	double[] array = this.ts_dailyAverageBatteriesStoredEnergy_MWh.getTimeSeries();
    	double factor_fr = 1/assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
    	//traceln("ts_dailyAverageBatteriesStoredEnergy_MWh.getSignalResolution_h(): %s", ts_dailyAverageBatteriesStoredEnergy_MWh.getSignalResolution_h());
    	ZeroTimeSeries ts = new ZeroTimeSeries(ts_dailyAverageBatteriesStoredEnergy_MWh.getSignalResolution_h(), ts_dailyAverageBatteriesStoredEnergy_MWh.getDuration());
    	for (int i=0; i<array.length; i++) {
    		ts.addStep(array[i]*factor_fr);
    	}
    	return ts;
    }
    
    public Pair<Double, Double> getFlexPotential() {
    	// Check if capacity is known
    	Double possibleGrowthFactor_fr;
    	Double requiredBatteryCapacity_kWh;
    	if (connectionMetaData.contractedDeliveryCapacityKnown) {
    		// Find day with max capacity utilisation	
    		ZeroAccumulator acc_dailyAvgElectricityConsumption_kW = am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);
    		ZeroAccumulator acc_dailyAvgElectricityProduction_kW = am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY);
    		ZeroAccumulator acc_dailyAvgNettElectricityDelivery_kW = acc_dailyAvgElectricityConsumption_kW.getClone().subtract(acc_dailyAvgElectricityProduction_kW);
    		double[] dailyAvgs_kW = acc_dailyAvgNettElectricityDelivery_kW.getTimeSeries_kW();
    		
    		int maxDay = 0;
    		for (int i = 0; i < dailyAvgs_kW.length; i++) {
    			maxDay = dailyAvgs_kW[i] > dailyAvgs_kW[maxDay] ? i : maxDay;
    		}
    		//traceln("Max day: %s, average power %s kW", maxDay, dailyAvgs_kW[maxDay]);
    		// Maximum growth is when dailyAvg delivery would be equal to contracted delivery capacity.
    		possibleGrowthFactor_fr = connectionMetaData.contractedDeliveryCapacity_kW / dailyAvgs_kW[maxDay];
    		if ( possibleGrowthFactor_fr < 1.0) {
    			//traceln("Already overutilising contracted delivery capacity over one day!! Probably an infeasible results...Growth factor: %s", possibleGrowthFactor_fr);
    			requiredBatteryCapacity_kWh = 0.0;
    		} else {
	    		//traceln("possibleGrowth: %s ", possibleGrowthFactor_fr);
	    		double[] dayProfile_kW = Arrays.copyOfRange(am_totalBalanceAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getTimeSeries_kW(),roundToInt(24.0/timeStep_h * maxDay), roundToInt(24.0/timeStep_h * (maxDay+1)));
	    		double[] dayProfileScaled_kW = ZeroMath.arrayMultiply(dayProfile_kW, possibleGrowthFactor_fr);
	    		double[] SoC_kWh = new double[roundToInt(24/timeStep_h)+1];
	    		double minSoC_kWh = 0.0;
	    		double maxSoC_kWh = 0.0;
	    		for(int i = 0; i < roundToInt(24/timeStep_h); i++) {
	    			SoC_kWh[i+1] = SoC_kWh[i] + (dayProfileScaled_kW[i] - possibleGrowthFactor_fr * dailyAvgs_kW[maxDay] ) * timeStep_h;
	    			minSoC_kWh = min(minSoC_kWh, SoC_kWh[i+1]);
	    			maxSoC_kWh = max(maxSoC_kWh, SoC_kWh[i+1]);
	    		}
	    		requiredBatteryCapacity_kWh = maxSoC_kWh - minSoC_kWh;
	    		//traceln("Battery size to allow growth: %s kWh", requiredBatteryCapacity_kWh);
    		}
    	} else {
    		traceln("Gridconnection contracted delivery capacity unknown!");
    		possibleGrowthFactor_fr = null;
    		requiredBatteryCapacity_kWh = 0.0;
    	}    	
    	
    	return new Pair(possibleGrowthFactor_fr, requiredBatteryCapacity_kWh);
    }
    
    public double getTotalElectricityConsumed_MWh() { 
        return am_dailyAverageConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_MWh(); 
    }
    public double getTotalElectricityProduced_MWh() { 
        return am_dailyAverageProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_MWh(); 
    }
    public double getTotalElectricitySelfConsumed_MWh() { 
        return max(0, getTotalElectricityConsumed_MWh() - getTotalImport_MWh(OL_EnergyCarriers.ELECTRICITY));
    }
    public double getTotalEnergyConsumed_MWh() { 
        return acc_dailyAverageFinalEnergyConsumption_kW.getIntegral_MWh(); 
    }
    public double getTotalEnergyProduced_MWh() { 
        return acc_dailyAverageEnergyProduction_kW.getIntegral_MWh(); 
    }
    public double getTotalEnergyCurtailed_MWh() { 
        return acc_totalEnergyCurtailed_kW.getIntegral_MWh(); 
    }
    public double getTotalEnergyImport_MWh() { 
        return this.am_totalBalanceAccumulators_kW.totalIntegralPos_MWh();
    }
    public double getTotalEnergyExport_MWh() { 
        return -this.am_totalBalanceAccumulators_kW.totalIntegralNeg_MWh();
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
        return acc_totalPrimaryEnergyProductionHeatpumps_kW.getIntegral_MWh(); 
    }
    public double getTotalDistrictHeatingConsumption_MWh() {
    	if (assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.districtHeatDelivery_kW)) {
    		return am_assetFlowsAccumulators_kW.get(OL_AssetFlowCategories.districtHeatDelivery_kW).getIntegral_MWh();
    	} else {
    		return 0.0;
    	}
    }
    
    public double getTotalBatteryCycles() { 
    	if (assetsMetaData.activeAssetFlows.contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
    		return am_assetFlowsAccumulators_kW.get(OL_AssetFlowCategories.batteriesChargingPower_kW).getIntegral_MWh()/this.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
    	} else {
    		return 0.0;
    	}
    }

// Summerweek Getters
    public double getSummerWeekElectricityConsumed_MWh() {
        return am_summerWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_MWh();
    }
    public double getSummerWeekElectricityProduced_MWh() {
        return am_summerWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_MWh();
    }
    public double getSummerWeekElectricitySelfConsumed_MWh() {
        return max(0, getSummerWeekElectricityConsumed_MWh() - getSummerWeekImport_MWh(OL_EnergyCarriers.ELECTRICITY));
    }
    public double getSummerWeekEnergyConsumed_MWh() {
        return acc_summerWeekFinalEnergyConsumption_kW.getIntegral_MWh();
    }
    public double getSummerWeekEnergyProduced_MWh() {
        return acc_summerWeekEnergyProduction_kW.getIntegral_MWh();
    }
    public double getSummerWeekEnergyCurtailed_MWh() {
        return acc_summerWeekEnergyCurtailed_kW.getIntegral_MWh();
    }
    public double getSummerWeekEnergyImport_MWh() {
        return this.am_summerWeekBalanceAccumulators_kW.totalIntegralPos_MWh();
    }
    public double getSummerWeekEnergyExport_MWh() {
        return -this.am_summerWeekBalanceAccumulators_kW.totalIntegralNeg_MWh();
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
        return acc_summerWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_MWh();
    }
    
    public double getSummerWeekBatteryCycles() { 
    	if (am_assetFlowsSummerWeek_kW.keySet().contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
    		return am_assetFlowsSummerWeek_kW.get(OL_AssetFlowCategories.batteriesChargingPower_kW).getIntegral_MWh()/this.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
    	} else {
    		return 0.0;
    	}
    	//return acc_summerWeekBatteriesConsumption_kW.getIntegral_MWh()/this.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
    }
    
// Winterweek Getters
    public double getWinterWeekElectricityConsumed_MWh() {
        return am_winterWeekConsumptionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_MWh();
    }
    public double getWinterWeekElectricityProduced_MWh() {
        return am_winterWeekProductionAccumulators_kW.get(OL_EnergyCarriers.ELECTRICITY).getIntegral_MWh();
    }
    public double getWinterWeekElectricitySelfConsumed_MWh() {
        return max(0, getWinterWeekElectricityConsumed_MWh() - getWinterWeekImport_MWh(OL_EnergyCarriers.ELECTRICITY));
    }
    public double getWinterWeekEnergyConsumed_MWh() {
        return acc_winterWeekFinalEnergyConsumption_kW.getIntegral_MWh();
    }
    public double getWinterWeekEnergyProduced_MWh() {
        return acc_winterWeekEnergyProduction_kW.getIntegral_MWh();
    }
    public double getWinterWeekEnergyCurtailed_MWh() {
        return acc_winterWeekEnergyCurtailed_kW.getIntegral_MWh();
    }
    public double getWinterWeekEnergyImport_MWh() {
        return this.am_winterWeekBalanceAccumulators_kW.totalIntegralPos_MWh();
    }
    public double getWinterWeekEnergyExport_MWh() {
        return -this.am_winterWeekBalanceAccumulators_kW.totalIntegralNeg_MWh();
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
        return acc_winterWeekPrimaryEnergyProductionHeatpumps_kW.getIntegral_MWh();
    }
    
    public double getWinterWeekBatteryCycles() { 
    	if (am_assetFlowsWinterWeek_kW.keySet().contains(OL_AssetFlowCategories.batteriesChargingPower_kW)) {
    		return am_assetFlowsWinterWeek_kW.get(OL_AssetFlowCategories.batteriesChargingPower_kW).getIntegral_MWh()/this.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
    	} else {
    		return 0.0;
    	}
    	//return acc_winterWeekBatteriesConsumption_kW.getIntegral_MWh()/this.assetsMetaData.totalInstalledBatteryStorageCapacity_MWh;
    }
    
// Daytime getters    
    public double getDaytimeElectricityConsumed_MWh() { 
        return acc_daytimeElectricityConsumption_kW.getIntegral_MWh(); 
    }
    public double getDaytimeElectricityProduced_MWh() { 
        return acc_daytimeElectricityProduction_kW.getIntegral_MWh(); 
    }
    public double getDaytimeElectricitySelfConsumed_MWh() { 
        return max(0, getDaytimeElectricityConsumed_MWh() - getDaytimeImport_MWh(OL_EnergyCarriers.ELECTRICITY));
    }
    public double getDaytimeEnergyConsumed_MWh() { 
        return acc_daytimeFinalEnergyConsumption_kW.getIntegral_MWh(); 
    }
    public double getDaytimeEnergyProduced_MWh() { 
        return acc_daytimeEnergyProduction_kW.getIntegral_MWh(); 
    }
    public double getDaytimeEnergyExport_MWh() { 
        return am_daytimeExports_kW.totalIntegral_MWh(); 
    }
    public double getDaytimeEnergyImport_MWh() { 
        return am_daytimeImports_kW.totalIntegral_MWh(); 
    }
    public double getDaytimeEnergySelfConsumed_MWh() { 
        return max(0, getDaytimeEnergyProduced_MWh() - getDaytimeEnergyExport_MWh()); 
    }
    public double getDaytimeExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_daytimeExports_kW.get(EC).getIntegral_MWh();
    }
    public double getDaytimeImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_daytimeImports_kW.get(EC).getIntegral_MWh();
    }
    public double getDaytimePrimaryEnergyProductionHeatpumps_MWh() {
    	return this.acc_daytimePrimaryEnergyProductionHeatpumps_kW.getIntegral_MWh();
    }
//Nighttime Getters
    public double getNighttimeElectricityConsumed_MWh() { 
        return getTotalElectricityConsumed_MWh() - getDaytimeElectricityConsumed_MWh(); 
    }
    public double getNighttimeElectricityProduced_MWh() { 
        return getTotalElectricityProduced_MWh() - getDaytimeElectricityProduced_MWh(); 
    }
    public double getNighttimeElectricitySelfConsumed_MWh() { 
        return max(0, getNighttimeElectricityConsumed_MWh() - getNighttimeImport_MWh(OL_EnergyCarriers.ELECTRICITY));
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
    	return -this.am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_MWh() - this.getDaytimeExport_MWh(EC);
    }
    public double getNighttimeImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_MWh() - this.getDaytimeImport_MWh(EC);
    }
    public double getNighttimePrimaryEnergyProductionHeatpumps_MWh() {
    	return this.getTotalPrimaryEnergyProductionHeatpumps_MWh() - this.getDaytimePrimaryEnergyProductionHeatpumps_MWh();
    }
// Weekday Getters
    public double getWeekdayElectricityConsumed_MWh() { 
        return getTotalElectricityConsumed_MWh() - getWeekendElectricityConsumed_MWh(); 
    }
    public double getWeekdayElectricityProduced_MWh() { 
        return getTotalElectricityProduced_MWh() - getWeekendElectricityProduced_MWh(); 
    }
    public double getWeekdayElectricitySelfConsumed_MWh() { 
        return max(0, getWeekdayElectricityConsumed_MWh() - getWeekdayImport_MWh(OL_EnergyCarriers.ELECTRICITY));
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
    	return -this.am_totalBalanceAccumulators_kW.get(EC).getIntegralNeg_MWh() - this.getWeekendExport_MWh(EC);
    }
    public double getWeekdayImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_totalBalanceAccumulators_kW.get(EC).getIntegralPos_MWh() - this.getWeekendImport_MWh(EC);
    }
    public double getWeekdayPrimaryEnergyProductionHeatpumps_MWh() {
    	return this.getTotalPrimaryEnergyProductionHeatpumps_MWh() - this.getWeekendPrimaryEnergyProductionHeatpumps_MWh();
    }    
//Weekend Getters
    public double getWeekendElectricityConsumed_MWh() { 
        return acc_weekendElectricityConsumption_kW.getIntegral_MWh(); 
    }
    public double getWeekendElectricityProduced_MWh() { 
        return acc_weekendElectricityProduction_kW.getIntegral_MWh(); 
    }
    public double getWeekendElectricitySelfConsumed_MWh() { 
        return max(0, getWeekendElectricityConsumed_MWh() - getWeekendImport_MWh(OL_EnergyCarriers.ELECTRICITY));
    }
    public double getWeekendEnergyConsumed_MWh() { 
        return acc_weekendFinalEnergyConsumption_kW.getIntegral_MWh(); 
    }
    public double getWeekendEnergyProduced_MWh() { 
        return acc_weekendEnergyProduction_kW.getIntegral_MWh(); 
    }
    public double getWeekendEnergyExport_MWh() { 
        return am_weekendExports_kW.totalIntegral_MWh(); 
    }
    public double getWeekendEnergyImport_MWh() { 
        return am_weekendImports_kW.totalIntegral_MWh(); 
    }
    public double getWeekendEnergySelfConsumed_MWh() { 
        return max(0, getWeekendEnergyProduced_MWh() - getWeekendEnergyExport_MWh()); 
    }
    public double getWeekendExport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_weekendExports_kW.get(EC).getIntegral_MWh();
    }
    public double getWeekendImport_MWh( OL_EnergyCarriers EC ) {
    	if (!this.activeEnergyCarriers.contains(EC)) {
    		throw new RuntimeException("RapidRunData class does not contain energycarrier: " + EC);
    	}
    	return this.am_weekendImports_kW.get(EC).getIntegral_MWh();
    }
    public double getWeekendPrimaryEnergyProductionHeatpumps_MWh() {
    	return this.acc_weekendPrimaryEnergyProductionHeatpumps_kW.getIntegral_MWh();
    } 
	
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Active Energy Carriers: ");
        sb.append(this.activeEnergyCarriers);
        sb.append(System.lineSeparator());
        sb.append("Consumption Carriers: ");
        sb.append(this.activeConsumptionEnergyCarriers);
        sb.append(System.lineSeparator());
        sb.append("Production Carriers: ");
        sb.append(this.activeProductionEnergyCarriers);
        sb.append(System.lineSeparator());
        sb.append("Asset Flow Caterogies: ");
        sb.append(this.assetsMetaData.activeAssetFlows);
        
        return sb.toString();	}
}