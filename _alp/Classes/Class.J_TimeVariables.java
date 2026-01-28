/**
 * J_TimeVariables
 */	
public final class J_TimeVariables {
	////Time Variables -> Updated every timestep
	private double t_h; //Current energymodel runtime: t_h = 0, corresponds to 'jan 1 0:00' of the start year 
	private double anyLogicTime_h; // anyLogicTime is the experiment time used for DataSets/ResultsUI. t = 0 corresponds to the start of the simulation
	private double timeOfDay_h;
	private double timeStepsElapsed;

	private OL_Days dayOfWeek;
	
	private boolean isDaytime;
	private boolean isWeekday;
	private boolean isSummerWeek;
	private boolean isWinterWeek;
	private boolean isLastTimeStepOfDay;
	
	/**
     * Default constructor
     */
	public J_TimeVariables(int v_timeStepsElapsed, J_TimeParameters timeParameters){
		updateTimeVariables(v_timeStepsElapsed, timeParameters);
	}
	
	public void updateTimeVariables(int v_timeStepsElapsed, J_TimeParameters timeParameters) {
		timeStepsElapsed = v_timeStepsElapsed;
		anyLogicTime_h = timeStepsElapsed * timeParameters.getTimeStep_h();
		t_h = anyLogicTime_h + timeParameters.getRunStartTime_h();
		int dayIndex = (int) ((t_h / 24 + timeParameters.getDayOfWeek1jan()) % 7);	
		dayOfWeek = OL_Days.values()[dayIndex];
		isDaytime = t_h % 24 > 6 && t_h % 24 < 18;
		isWeekday = (t_h+(timeParameters.getDayOfWeek1jan()-1)*24) % (24*7) < (24*5);
		isSummerWeek = (t_h % 8760) >= timeParameters.getStartOfSummerWeek_h() && (t_h % 8760) < timeParameters.getStartOfSummerWeek_h() + 24*7;
		isWinterWeek = (t_h % 8760) >= timeParameters.getStartOfWinterWeek_h() && (t_h % 8760) < timeParameters.getStartOfWinterWeek_h() + 24*7;
		isLastTimeStepOfDay = t_h % 24 == (24-timeParameters.getTimeStep_h());
		timeOfDay_h = t_h % 24;
	}
	
	public double getT_h() {
	    return t_h;
	}
	
	public double getAnyLogicTime_h() {
		return anyLogicTime_h;
	}

	public double getTimeOfDay_h() {
	    return timeOfDay_h;
	}

	public OL_Days getDayOfWeek() {
		return dayOfWeek;
	}
	
	public double getTimeStepsElapsed() {
	    return timeStepsElapsed;
	}

	public boolean isDaytime() {
	    return isDaytime;
	}

	public boolean isWeekday() {
	    return isWeekday;
	}

	public boolean isSummerWeek() {
	    return isSummerWeek;
	}

	public boolean isWinterWeek() {
	    return isWinterWeek;
	}

	public boolean isLastTimeStepOfDay() {
	    return isLastTimeStepOfDay;
	}
	
	public Date getDate(Experiment experiment, J_TimeParameters timeParameters) {
		Date startDate = experiment.getEngine().getStartDate();
		long startDateUnixTime_ms = startDate.getTime();
		long runtime_ms = (long) (timeStepsElapsed * timeParameters.getTimeStep_h() * 60 * 60 * 1000);
		Date date = new Date();
		date.setTime(startDateUnixTime_ms + runtime_ms);
		return date;
	}
	
	
	@Override
	public String toString() {
	    return "J_TimeVariables{" +
	            "t_h=" + t_h +
	            ", anyLogicTime_h=" + anyLogicTime_h +
	            ", timeOfDay_h=" + timeOfDay_h +
	            ", timeStepsElapsed=" + timeStepsElapsed +
	            ", dayOfWeek=" + dayOfWeek +
	            ", isDaytime=" + isDaytime +
	            ", isWeekday=" + isWeekday +
	            ", isSummerWeek=" + isSummerWeek +
	            ", isWinterWeek=" + isWinterWeek +
	            ", isLastTimeStepOfDay=" + isLastTimeStepOfDay +
	            '}';
	}
}