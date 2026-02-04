import java.util.*;

public class J_EAPetroleumFuelTractor extends J_EAProfile implements Serializable {
    final static double PETROLEUM_FUEL_ENERGY_DENSITY_KWH_PER_L = 9.7;
    
    final double[] petroleumFuelConsumptionPerWeek_L;
    final double workDayStart_h = 6;
    final double workDayEnd_h = 17;
 
    /**
     * @param parentAgent
     * @param yearlyPetroleumFuelConsumption_l petroleumFuel consumption of a single tractor for a whole year
     * @param petroleumFuelConsumptionPerWeek profile of a year of petroleumFuel consumption. 
     *  Usually expressed in L per ha per week for a specific crop or mix of crops. 
     *  For our purpose the unit doesn't matter.
     * @param timeStep_h
     */
    public J_EAPetroleumFuelTractor(I_AssetOwner owner, double yearlyPetroleumFuelConsumption_L, double[] petroleumFuelConsumptionPerWeek, J_TimeParameters timeParameters) {
        if (owner == null) {
            throw new RuntimeException("PetroleumFuel tractor missing parent agent");
        }
        
        if (yearlyPetroleumFuelConsumption_L <= 100.0) {
            throw new RuntimeException(
                String.format("PetroleumFuel tractor fuel usage conspicuously low: %d L", yearlyPetroleumFuelConsumption_L)
            );
        }
        
        if (petroleumFuelConsumptionPerWeek == null) {
            throw new RuntimeException("Tractor petroleumFuel consumption profile is null");
        }
        
        if (petroleumFuelConsumptionPerWeek.length != 52) {
            throw new RuntimeException(
                String.format("Tractor petroleumFuel consumption profile has %d weeks instead of 52", petroleumFuelConsumptionPerWeek.length)
            );
        }
        
        this.setOwner(owner);
        this.timeParameters = timeParameters;
        this.petroleumFuelConsumptionPerWeek_L = calculatePetroleumFuelConsumptionPerWeek_L(yearlyPetroleumFuelConsumption_L, petroleumFuelConsumptionPerWeek);
        
        this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.PETROLEUM_FUEL);
        registerEnergyAsset(timeParameters);
    }    
    
    @Override
    public J_FlowPacket f_updateAllFlows(J_TimeVariables timeVariables) {
    	this.operate(timeVariables);
		if (this.assetFlowCategory != null) {
			assetFlowsMap.put(this.assetFlowCategory, Math.abs(energyUse_kW));
		}
     	J_FlowsMap flowsMapCopy = new J_FlowsMap();    	
     	J_ValueMap assetFlowsMapCopy = new J_ValueMap(OL_AssetFlowCategories.class);
     	J_FlowPacket flowPacket = new J_FlowPacket(flowsMapCopy.cloneMap(this.flowsMap), this.energyUse_kW, assetFlowsMapCopy.cloneMap(this.assetFlowsMap));
		this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	return flowPacket;
    }
    
    @Override
    public void operate(J_TimeVariables timeVariables) {
        if (!shouldWork(timeVariables)) {
            this.flowsMap.clear();
            return;
        }
        
        double currentPower_kW = currentPower_kW(timeVariables.getT_h());    
        
        this.flowsMap.put(OL_EnergyCarriers.PETROLEUM_FUEL, currentPower_kW);
        this.energyUse_kW = currentPower_kW;
        this.energyUsed_kWh += currentPower_kW * timeParameters.getTimeStep_h();    
    }
    
    private static double[] calculatePetroleumFuelConsumptionPerWeek_L(double yearlyPetroleumFuelConsumption_l, double[] weekProfile) {
        var profileSum = Arrays.stream(weekProfile).sum();
        
        return Arrays.stream(weekProfile)
                .map(weekValue -> yearlyPetroleumFuelConsumption_l * weekValue / profileSum)
                .toArray();
    }
    
    private boolean shouldWork(J_TimeVariables timeVariables) {
        return isWorkTime(timeVariables.getT_h()) && !timeVariables.isWeekday();
    }
    
    private boolean isWorkTime(double currentStep_h) {
        double timeOfDay = currentStep_h % 24;
        
        return timeOfDay >= workDayStart_h && timeOfDay < workDayEnd_h;
    }
    
    private double workHoursPerWeek() {
        return 5 * (workDayEnd_h - workDayStart_h);
    }
    
    private int workTimeStepsPerWeek() {
        return roundToInt(workHoursPerWeek() / this.timeParameters.getTimeStep_h());
    }
    
    private double currentPower_kW(double currentStep_h) {
        int week = (int) Math.round(currentStep_h / (7 * 24));
        if(week == 52) {
        	week = 51;
        }
        double thisWeekPetroleumFuelConsumption_L = this.petroleumFuelConsumptionPerWeek_L[week];
        double thisWeekPetroleumFuelConsumption_kWh = thisWeekPetroleumFuelConsumption_L * PETROLEUM_FUEL_ENERGY_DENSITY_KWH_PER_L;
        double power_kW = thisWeekPetroleumFuelConsumption_kWh / workHoursPerWeek();
        return power_kW;
    }
}
