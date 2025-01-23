import java.util.*;

/**
 * J_EADieselTractor
 */	
public class J_EADieselTractor extends J_EA implements Serializable {
    Double[] dieselConsumptionPerWeek;
    Double yearlyDieselConsumption_l;

    /**
     * Default constructor
     */
    public J_EADieselTractor() {
    }
    
    /**
     * Constructor initializing the fields
     */
    public J_EADieselTractor(Agent parentAgent, Double yearlyDieselConsumption_l, Double[] dieselConsumptionPerWeek, double timeStep_h) {
    	this.parentAgent = parentAgent;
    	this.yearlyDieselConsumption_l = yearlyDieselConsumption_l;
    	this.dieselConsumptionPerWeek = dieselConsumptionPerWeek;
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
		double timeOfDay = t_h % 24;
		if (timeOfDay < 6 || timeOfDay > 17) {
			this.flowsMap.clear();
			return;
		}
     	if (parentAgent instanceof GridConnection) {
    		if (!((GridConnection)parentAgent).energyModel.b_isWeekday) {
    			this.flowsMap.clear();
    			return;
    		}
     	}
		// TODO: Extract this calculation from operate and only do this once a week and store dieselPerTimeStep
		int week = (int)(t_h / 168);
		double weeklyDieselConsumption_l = this.dieselConsumptionPerWeek[week] / Arrays.stream(dieselConsumptionPerWeek).mapToDouble(f -> f.doubleValue()).sum() * this.yearlyDieselConsumption_l;
		double weeklyDieselConsumption_kWh = weeklyDieselConsumption_l * 9.7;		
		int totalWorkTimeSteps = roundToInt(5 * (17 - 6) / this.timestep_h);
		double dieselPerTimeStep_kW = weeklyDieselConsumption_l / totalWorkTimeSteps;
		
		this.flowsMap.put(OL_EnergyCarriers.DIESEL, dieselPerTimeStep_kW);
		this.energyUse_kW = dieselPerTimeStep_kW;
		this.energyUsed_kWh += this.energyUse_kW * this.timestep_h;	
	}
	
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}