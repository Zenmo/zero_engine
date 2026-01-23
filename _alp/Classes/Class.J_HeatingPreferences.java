/**
 * J_HeatingPreferences
 */	
public class J_HeatingPreferences {

    /**
     * Default constructor
     */
    private double startOfDayTime_h = 8;
    private double startOfNightTime_h = 23;
    private double dayTimeSetPoint_degC = 20;
    private double nightTimeSetPoint_degC = 18;
    
    private double maxComfortTemperature_degC = dayTimeSetPoint_degC + 3;
    private double minComfortTemperature_degC = dayTimeSetPoint_degC - 2;
    
    public J_HeatingPreferences() {
    }
    
    public J_HeatingPreferences(double startOfDayTime_h, double startOfNightTime_h, double dayTimeSetPoint_degC, double nightTimeSetPoint_degC, double maxComfortTemperature_degC, double minComfortTemperature_degC) {
    	
        this.startOfDayTime_h = startOfDayTime_h;
        this.startOfNightTime_h = startOfNightTime_h;
        this.dayTimeSetPoint_degC = dayTimeSetPoint_degC;
        this.nightTimeSetPoint_degC = nightTimeSetPoint_degC;
        this.maxComfortTemperature_degC = maxComfortTemperature_degC;
        this.minComfortTemperature_degC = minComfortTemperature_degC;
    }
    
    //Setters
    public void setStartOfDayTime_h(double startOfDayTime_h) {
    	this.startOfDayTime_h = startOfDayTime_h;
    }
    public void setStartOfNightTime_h(double startOfNightTime_h) {
    	this.startOfNightTime_h = startOfNightTime_h;
    }
    public void setDayTimeSetPoint_degC(double dayTimeSetPoint_degC) {
    	this.dayTimeSetPoint_degC = dayTimeSetPoint_degC;
    }
    public void setNightTimeSetPoint_degC(double nightTimeSetPoint_degC) {
    	this.nightTimeSetPoint_degC = nightTimeSetPoint_degC;
    }
    public void setMaxComfortTemperature_degC(double maxComfortTemperature_degC) {
    	this.maxComfortTemperature_degC = maxComfortTemperature_degC;
    }
    public void setMinComfortTemperature_degC(double minComfortTemperature_degC) {
    	this.minComfortTemperature_degC = minComfortTemperature_degC;
    }
    
    //Getters
    public double getStartOfDayTime_h() {
    	return this.startOfDayTime_h;
    }
    public double getStartOfNightTime_h() {
    	return this.startOfNightTime_h;
    }
    public double getDayTimeSetPoint_degC() {
    	return this.dayTimeSetPoint_degC;
    }
    public double getNightTimeSetPoint_degC() {
    	return this.nightTimeSetPoint_degC;
    }
    public double getMaxComfortTemperature_degC() {
    	return this.maxComfortTemperature_degC;
    }
    public double getMinComfortTemperature_degC() {
    	return this.minComfortTemperature_degC;
    }
    
	public double getCurrentPreferedTemperatureSetpoint_degC(double timeOfDay_h) {
		if (timeOfDay_h < getStartOfDayTime_h() || timeOfDay_h >= getStartOfNightTime_h()) {
			return getNightTimeSetPoint_degC();
		}
		else {
			return getDayTimeSetPoint_degC();
		}
	}
	
	@Override
	public String toString() {
		return 
        "StartOfDayTime_h = " + this.startOfDayTime_h + 
        ", StartOfNightTime_h = " + this.startOfNightTime_h + 
        ", DayTimeSetPoint_degC = " + this.dayTimeSetPoint_degC + 
        ", NightTimeSetPoint_degC = " + this.nightTimeSetPoint_degC + 
        ", MaxComfortTemperature_degC = " + this.maxComfortTemperature_degC + 
        ", MinComfortTemperature_degC = " + this.minComfortTemperature_degC;
	}

}