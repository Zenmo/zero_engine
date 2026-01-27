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
    public J_EAPetroleumFuelTractor(Agent parentAgent, double yearlyPetroleumFuelConsumption_L, double[] petroleumFuelConsumptionPerWeek, double timeStep_h) {
        if (parentAgent == null) {
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
        
        if (timeStep_h <= 0.0) {
            throw new RuntimeException("Tractor timestep is off");
        }
        
        this.parentAgent = parentAgent;
        this.petroleumFuelConsumptionPerWeek_L = calculatePetroleumFuelConsumptionPerWeek_L(yearlyPetroleumFuelConsumption_L, petroleumFuelConsumptionPerWeek);
        this.timestep_h = timeStep_h;
        
        this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.PETROLEUM_FUEL);
        registerEnergyAsset();
    }
    
    @Override
    public void f_updateAllFlows() {
         operate(((GridConnection)parentAgent).energyModel.t_h); // temporary ugly hack
         if (parentAgent instanceof GridConnection) {        
            ((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, assetFlowsMap, this);
        }
        this.lastFlowsMap.cloneMap(this.flowsMap);
        this.lastEnergyUse_kW = this.energyUse_kW;
        this.clear();
    }
    
    @Override
    public void f_updateAllFlows(double powerFraction_fr) {
    	throw new RuntimeException("J_EADieselTractor.f_updateAllFlows(powerFraction_fr) not supperted for J_EADieselTractor! Use J_EADieselTractor.f_updateProfileFlows(t_h) instead!");
    }
    
    @Override
    public void operate(double t_h) {
        if (!shouldWork(t_h)) {
            this.flowsMap.clear();
            return;
        }
        
        double currentPower_kW = currentPower_kW(t_h);    
        
        this.flowsMap.put(OL_EnergyCarriers.PETROLEUM_FUEL, currentPower_kW);
        this.energyUse_kW = currentPower_kW;
        this.energyUsed_kWh += currentPower_kW * timestep_h;    
    }
    
    private static double[] calculatePetroleumFuelConsumptionPerWeek_L(double yearlyPetroleumFuelConsumption_l, double[] weekProfile) {
        var profileSum = Arrays.stream(weekProfile).sum();
        
        return Arrays.stream(weekProfile)
                .map(weekValue -> yearlyPetroleumFuelConsumption_l * weekValue / profileSum)
                .toArray();
    }
    
    private boolean shouldWork(double currentStep_h) {
        return isWorkTime(currentStep_h) && isWorkDay();
    }
    
    private boolean isWorkTime(double currentStep_h) {
        double timeOfDay = currentStep_h % 24;
        
        return timeOfDay >= workDayStart_h && timeOfDay < workDayEnd_h;
    }
    
    private boolean isWorkDay() {
        return ((GridConnection)parentAgent).energyModel.b_isWeekday;
    }
    
    private double workHoursPerWeek() {
        return 5 * (workDayEnd_h - workDayStart_h);
    }
    
    private int workTimeStepsPerWeek() {
        return roundToInt(workHoursPerWeek() / this.timestep_h);
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
    
    /**
     * This number is here for model snapshot storing purpose<br>
     * It needs to be changed when this class gets changed
     */ 
    private static final long serialVersionUID = 1L;
}
