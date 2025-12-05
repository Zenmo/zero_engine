/**
 * J_TimeData
 */	
public class J_TimeData {
	//Time parameters:
	private static double timeStep_h = 0.25;
	
	private static int startYear = 2024;
	private static double[] hourOfYearPerMonth;
	private static int dayOfWeek1jan;

	private static double runStartTime_h;
	private static double runEndTime_h;

	private static int winterWeekNumber;
	private static int summerWeekNumber;
	private static double startOfSummerWeek_h;
	private static double startOfWinterWeek_h;
	
	public static double getTimeStep_h() {
	    return timeStep_h;
	}

	public static void setTimeStep_h(double timeStep_h) {
	    timeStep_h = timeStep_h;
	}

	public static int getStartYear() {
	    return startYear;
	}

	public static void setStartYear(int startYear) {
	    startYear = startYear;
	}

	public static double[] getHourOfYearPerMonth() {
	    return hourOfYearPerMonth;
	}

	public static void setHourOfYearPerMonth(double[] hourOfYearPerMonth) {
	    hourOfYearPerMonth = hourOfYearPerMonth;
	}

	public static int getDayOfWeek1jan() {
	    return dayOfWeek1jan;
	}

	public static void setDayOfWeek1jan(int dayOfWeek1jan) {
	    dayOfWeek1jan = dayOfWeek1jan;
	}

	public static double getRunStartTime_h() {
	    return runStartTime_h;
	}

	public static void setRunStartTime_h(double runStartTime_h) {
	    runStartTime_h = runStartTime_h;
	}

	public static double getRunEndTime_h() {
	    return runEndTime_h;
	}

	public static void setRunEndTime_h(double runEndTime_h) {
	    runEndTime_h = runEndTime_h;
	}

	public static int getWinterWeekNumber() {
	    return winterWeekNumber;
	}

	public static void setWinterWeekNumber(int winterWeekNumber) {
	    winterWeekNumber = winterWeekNumber;
	}

	public static int getSummerWeekNumber() {
	    return summerWeekNumber;
	}

	public static void setSummerWeekNumber(int summerWeekNumber) {
	    summerWeekNumber = summerWeekNumber;
	}

	public static double getStartOfSummerWeek_h() {
	    return startOfSummerWeek_h;
	}

	public static void setStartOfSummerWeek_h(double startOfSummerWeek_h) {
	    startOfSummerWeek_h = startOfSummerWeek_h;
	}

	public static double getStartOfWinterWeek_h() {
	    return startOfWinterWeek_h;
	}

	public static void setStartOfWinterWeek_h(double startOfWinterWeek_h) {
	    startOfWinterWeek_h = startOfWinterWeek_h;
	}
	
	//Time Variables
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
		t_h = runStartTime_h + timeStepsElapsed * timeStep_h;
		isDaytime = t_h % 24 > 6 && t_h % 24 < 18;
		isWeekday = (t_h+(dayOfWeek1jan-1)*24) % (24*7) < (24*5);
		isSummerWeek = (t_h % 8760) >= startOfSummerWeek_h && (t_h % 8760) < startOfSummerWeek_h + 24*7;
		isWinterWeek = (t_h % 8760) >= startOfWinterWeek_h && (t_h % 8760) < startOfWinterWeek_h + 24*7;
		isLastTimeStepOfDay = t_h % 24 == (24-timeStep_h);
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
}