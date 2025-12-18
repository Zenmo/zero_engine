/**
 * J_TimeVariables
 */	
public class J_TimeVariables {
	////Time Variables -> Updated every timestep
	private static double t_h = 0; //Current energymodel runtime: t_h = 0, corresponds to 'jan 1 0:00' of the start year 
	private static double t_hourOfDay = 0;
	private static double timeStepsElapsed = 0;

	private static boolean isDaytime = false;
	private static boolean isWeekday = false;
	private static boolean isSummerWeek = false;
	private static boolean isWinterWeek = false;
	private static boolean isLastTimeStepOfDay = false;
	
	public static void updateTimeVariables(int v_timeStepsElapsed) {
		timeStepsElapsed = v_timeStepsElapsed;
		t_h = J_TimeParameters.getRunStartTime_h() + timeStepsElapsed * J_TimeParameters.getTimeStep_h();
		isDaytime = t_h % 24 > 6 && t_h % 24 < 18;
		isWeekday = (t_h+(J_TimeParameters.getDayOfWeek1jan()-1)*24) % (24*7) < (24*5);
		isSummerWeek = (t_h % 8760) >= J_TimeParameters.getStartOfSummerWeek_h() && (t_h % 8760) < J_TimeParameters.getStartOfSummerWeek_h() + 24*7;
		isWinterWeek = (t_h % 8760) >= J_TimeParameters.getStartOfWinterWeek_h() && (t_h % 8760) < J_TimeParameters.getStartOfWinterWeek_h() + 24*7;
		isLastTimeStepOfDay = t_h % 24 == (24-J_TimeParameters.getTimeStep_h());
		t_hourOfDay = t_h % 24; // Assumes modelrun starts at midnight.
	}
	
	public static double getT_h() {
	    return t_h;
	}

	public static double getT_hourOfDay() {
	    return t_hourOfDay;
	}

	public static double getTimeStepsElapsed() {
	    return timeStepsElapsed;
	}

	public static boolean isDaytime() {
	    return isDaytime;
	}

	public static boolean isWeekday() {
	    return isWeekday;
	}

	public static boolean isSummerWeek() {
	    return isSummerWeek;
	}

	public static boolean isWinterWeek() {
	    return isWinterWeek;
	}

	public static boolean isLastTimeStepOfDay() {
	    return isLastTimeStepOfDay;
	}
	
	public static Date getDate(Experiment experiment) {
		Date startDate = experiment.getEngine().getStartDate();
		long startDateUnixTime_ms = startDate.getTime();
		long runtime_ms = (long) (timeStepsElapsed * J_TimeParameters.getTimeStep_h() * 60 * 60 * 1000);
		Date date = new Date();
		date.setTime(startDateUnixTime_ms + runtime_ms);
		return date;
	}
}