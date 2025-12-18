/**
 * J_TimeParameters
 */	
public class J_TimeParameters {
	////Time parameters:
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
	
	//IsFinal booleans used to make the parameters artificially final
	private static boolean timeStep_hIsFinal = false;

	private static boolean startYearIsFinal = false;
	private static boolean hourOfYearPerMonthIsFinal = false;
	private static boolean dayOfWeek1janIsFinal = false;

	private static boolean runStartTime_hIsFinal = false;
	private static boolean runEndTime_hIsFinal = false;

	private static boolean winterWeekNumberIsFinal = false;
	private static boolean summerWeekNumberIsFinal = false;
	private static boolean startOfSummerWeek_hIsFinal = false;
	private static boolean startOfWinterWeek_hIsFinal = false;
	
	////Time parameter setters: Can only be called once, after that they are made final.
	public static void setTimeStep_h(double p_timeStep_h) {
		if(!timeStep_hIsFinal) {
			timeStep_h = p_timeStep_h;
			timeStep_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set timeStep_h while it is final!");
		}
	}
	public static void setStartYear(int p_startYear) {
		if(!startYearIsFinal) {
			startYear = p_startYear;
			startYearIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set startYear while it is final!");
		}
	}
	public static void setHourOfYearPerMonth(double[] p_hourOfYearPerMonth) {
		if(!hourOfYearPerMonthIsFinal) {
			hourOfYearPerMonth = p_hourOfYearPerMonth;
			hourOfYearPerMonthIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set hourOfYearPerMonth while it is final!");
		}
	}
	public static void setDayOfWeek1jan(int p_dayOfWeek1jan) {
		if(!dayOfWeek1janIsFinal) {
			dayOfWeek1jan = p_dayOfWeek1jan;
			dayOfWeek1janIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set dayOfWeek1jan while it is final!");
		}
	}
	public static void setRunStartTime_h(double p_runStartTime_h) {
		if(!runStartTime_hIsFinal) {
			runStartTime_h = p_runStartTime_h;
			runStartTime_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set runStartTime_h while it is final!");
		}
	}
	public static void setRunEndTime_h(double p_runEndTime_h) {
		if(!runEndTime_hIsFinal) {
			runEndTime_h = p_runEndTime_h;
			runEndTime_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set runEndTime_h while it is final!");
		}
	}
	public static void setWinterWeekNumber(int p_winterWeekNumber) {
		if(!winterWeekNumberIsFinal) {
			winterWeekNumber = p_winterWeekNumber;
			winterWeekNumberIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set winterWeekNumber while it is final!");
		}
	}
	public static void setSummerWeekNumber(int p_summerWeekNumber) {
		if(!summerWeekNumberIsFinal) {
			summerWeekNumber = p_summerWeekNumber;
			summerWeekNumberIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set summerWeekNumber while it is final!");
		}
	}
	public static void setStartOfSummerWeek_h(double p_startOfSummerWeek_h) {
		if(!startOfSummerWeek_hIsFinal) {
			startOfSummerWeek_h = p_startOfSummerWeek_h;
			startOfSummerWeek_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set startOfSummerWeek_h while it is final!");
		}
	}
	public static void setStartOfWinterWeek_h(double p_startOfWinterWeek_h) {
		if(!startOfWinterWeek_hIsFinal) {
			startOfWinterWeek_h = p_startOfWinterWeek_h;
			startOfWinterWeek_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set startOfWinterWeek_h while it is final!");
		}
	}
	
	////Finalize all current values of Time parameters -> After this they can't be adjusted anymore.
	public static void finalizeTimeParameters() {
		timeStep_hIsFinal = true;
		startYearIsFinal = true;
		hourOfYearPerMonthIsFinal = true;
		dayOfWeek1janIsFinal = true;
		runStartTime_hIsFinal = true;
		runEndTime_hIsFinal = true;
		winterWeekNumberIsFinal = true;
		summerWeekNumberIsFinal = true;
		startOfSummerWeek_hIsFinal = true;
		startOfWinterWeek_hIsFinal = true;		
	}
	
	////Time Parameter getters
	public static double getTimeStep_h() {
	    return timeStep_h;
	}
	public static int getStartYear() {
	    return startYear;
	}
	public static double[] getHourOfYearPerMonth() {
	    return hourOfYearPerMonth;
	}
	public static int getDayOfWeek1jan() {
	    return dayOfWeek1jan;
	}
	public static double getRunStartTime_h() {
	    return runStartTime_h;
	}
	public static double getRunEndTime_h() {
	    return runEndTime_h;
	}
	public static int getWinterWeekNumber() {
	    return winterWeekNumber;
	}
	public static int getSummerWeekNumber() {
	    return summerWeekNumber;
	}
	public static double getStartOfSummerWeek_h() {
	    return startOfSummerWeek_h;
	}
	public static double getStartOfWinterWeek_h() {
	    return startOfWinterWeek_h;
	}
}