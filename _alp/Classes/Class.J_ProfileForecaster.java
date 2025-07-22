/**
 * J_ProfileForecaster
 */	
public class J_ProfileForecaster implements Serializable {
	
	public String name = "";
	public J_ProfilePointer profilePointer;
	public double forecastTime_h = 0;
	public double timeStep_h = 0;
	private double currentForecast = 0;
	
    /**
     * Default constructor
     */
	public J_ProfileForecaster() {
		
	}
	
    public J_ProfileForecaster(String forecastName, J_ProfilePointer pp, double forecastTime_h, double currentTime_h, double timeStep_h) {
    	if (forecastName == null) {
    		this.name = pp.name + " " + forecastTime_h + " h";
    	}
    	else {
    		this.name = forecastName;
    	}
    	this.profilePointer = pp;
    	this.forecastTime_h = forecastTime_h;
    	this.timeStep_h = timeStep_h;
    	this.initializeForecast(currentTime_h);
    }

    public void initializeForecast(double currentTime_h) {
    	this.currentForecast = 0;
    	for (double t_h = currentTime_h; t_h < currentTime_h + this.forecastTime_h; t_h += this.timeStep_h) {
    		this.currentForecast += this.profilePointer.getValue(t_h) / (this.forecastTime_h/this.timeStep_h);
    	}
    }
    
    public void updateForecast(double t_h) {
    	this.currentForecast += (this.profilePointer.getValue(t_h + this.forecastTime_h) - this.profilePointer.getValue(t_h)) / (this.forecastTime_h/this.timeStep_h);
    }
    
    public double getForecast() {
    	return this.currentForecast;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public double getForecastTime_h() {
    	return this.forecastTime_h;
    }
    
	@Override
	public String toString() {
		return "forecast name: " + this.name + ", current forecast: " + this.currentForecast;
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}