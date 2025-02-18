import java.util.*;

public class J_EADieselTractor extends J_EAProfile implements Serializable {
    final static double DIESEL_ENERGY_DENSITY_KWH_PER_L = 9.7;
    
    final double[] dieselConsumptionPerWeek_L;
    final double workDayStart_h = 6;
    final double workDayEnd_h = 17;
 
    /**
     * @param parentAgent
     * @param yearlyDieselConsumption_l diesel consumption of a single tractor for a whole year
     * @param dieselConsumptionPerWeek profile of a year of diesel consumption. 
     *  Usually expressed in L per ha per week for a specific crop or mix of crops. 
     *  For our purpose the unit doesn't matter.
     * @param timeStep_h
     */
    public J_EADieselTractor(Agent parentAgent, double yearlyDieselConsumption_L, double[] dieselConsumptionPerWeek, double timeStep_h) {
        if (parentAgent == null) {
            throw new RuntimeException("Diesel tractor missing parent agent");
        }
        
        if (yearlyDieselConsumption_L <= 100.0) {
            throw new RuntimeException(
                String.format("Diesel tractor fuel usage conspicuously low: %d L", yearlyDieselConsumption_L)
            );
        }
        
        if (dieselConsumptionPerWeek == null) {
            throw new RuntimeException("Tractor diesel consumption profile is null");
        }
        
        if (dieselConsumptionPerWeek.length != 52) {
            throw new RuntimeException(
                String.format("Tractor diesel consumption profile has %d weeks instead of 52", dieselConsumptionPerWeek.length)
            );
        }
        
        if (timeStep_h <= 0.0) {
            throw new RuntimeException("Tractor timestep is off");
        }
        
        this.parentAgent = parentAgent;
        this.dieselConsumptionPerWeek_L = calculateDieselConsumptionPerWeek_L(yearlyDieselConsumption_L, dieselConsumptionPerWeek);
        this.timestep_h = timeStep_h;
        
        this.activeConsumptionEnergyCarriers.add(OL_EnergyCarriers.DIESEL);
        registerEnergyAsset();
    }
    
    @Override
    public void f_updateAllFlows(double t_h) {
         operate(t_h);
         if (parentAgent instanceof GridConnection) {        
            ((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, this);
        }
        this.lastFlowsMap.cloneMap(this.flowsMap);
        this.lastEnergyUse_kW = this.energyUse_kW;
        this.clear();
    }
    
    @Override
    public void operate(double t_h) {
        if (!shouldWork(t_h)) {
            this.flowsMap.clear();
            return;
        }
        
        double currentPower_kW = currentPower_kW(t_h);    
        
        this.flowsMap.put(OL_EnergyCarriers.DIESEL, currentPower_kW);
        this.energyUse_kW = currentPower_kW;
        this.energyUsed_kWh += currentPower_kW * timestep_h;    
    }
    
    private static double[] calculateDieselConsumptionPerWeek_L(double yearlyDieselConsumption_l, double[] weekProfile) {
        var profileSum = Arrays.stream(weekProfile).sum();
        
        return Arrays.stream(weekProfile)
                .map(weekValue -> yearlyDieselConsumption_l * weekValue / profileSum)
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
        double thisWeekDieselConsumption_L = this.dieselConsumptionPerWeek_L[week];
        double thisWeekDieselConsumption_kWh = thisWeekDieselConsumption_L * DIESEL_ENERGY_DENSITY_KWH_PER_L;
        double power_kW = thisWeekDieselConsumption_kWh / workHoursPerWeek();
        return power_kW;
    }
    
    /**
     * This number is here for model snapshot storing purpose<br>
     * It needs to be changed when this class gets changed
     */ 
    private static final long serialVersionUID = 1L;
}
