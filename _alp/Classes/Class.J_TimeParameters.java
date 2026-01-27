import java.time.LocalDate;
import java.time.DayOfWeek;
/**
 * J_TimeParameters
 */	
public final class J_TimeParameters {
	////Time parameters:
	private final double timeStep_h;
	private final int startYear;
	private final double[] hourOfYearPerMonth;
	private final int dayOfWeek1jan;
	private final double runStartTime_h;
	private final double runEndTime_h;
	private final int summerWeekNumber;
	private final int winterWeekNumber;
	private final double startOfSummerWeek_h;
	private final double startOfWinterWeek_h;
	
	public J_TimeParameters(
		double timeStep_h,
		int startYear,
		double[] hourOfYearPerMonth,
		int dayOfWeek1jan,
		double runStartTime_h,
		double runEndTime_h,
		int summerWeekNumber,
		int winterWeekNumber
	) {
		this.timeStep_h = timeStep_h;
		this.startYear = startYear;
		this.hourOfYearPerMonth = hourOfYearPerMonth;
		this.runStartTime_h = runStartTime_h;
		this.runEndTime_h = runEndTime_h;
		this.summerWeekNumber = summerWeekNumber;
		this.winterWeekNumber = winterWeekNumber;
		this.dayOfWeek1jan = DayOfWeek.from(LocalDate.of(startYear, 1, 1)).getValue();
		this.startOfSummerWeek_h = roundToInt(24 * (summerWeekNumber * 7 + (8-dayOfWeek1jan)%7));
		this.startOfWinterWeek_h = roundToInt(24 * (winterWeekNumber * 7 + (8-dayOfWeek1jan)%7));
	}
	
	////Time Parameter getters
	public double getTimeStep_h() {
	    return timeStep_h;
	}
	public int getStartYear() {
	    return startYear;
	}
	public double[] getHourOfYearPerMonth() {
	    return hourOfYearPerMonth;
	}
	public int getDayOfWeek1jan() {
	    return dayOfWeek1jan;
	}
	public double getRunStartTime_h() {
	    return runStartTime_h;
	}
	public double getRunEndTime_h() {
	    return runEndTime_h;
	}
	public int getSummerWeekNumber() {
	    return summerWeekNumber;
	}
	public int getWinterWeekNumber() {
	    return winterWeekNumber;
	}
	public double getStartOfSummerWeek_h() {
	    return startOfSummerWeek_h;
	}
	public double getStartOfWinterWeek_h() {
	    return startOfWinterWeek_h;
	}
	
	////Time parameter setters: Can only be called once, after that they are made final.
	/*
	public void setTimeStep_h(double p_timeStep_h) {
		this.timeStep_h = p_timeStep_h;
	}
	
	public void setStartYear(int p_startYear) {
		if(!startYearIsFinal) {
			startYear = p_startYear;
			startYearIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set startYear while it is final!");
		}
	}
	public void setHourOfYearPerMonth(double[] p_hourOfYearPerMonth) {
		if(!hourOfYearPerMonthIsFinal) {
			hourOfYearPerMonth = p_hourOfYearPerMonth;
			hourOfYearPerMonthIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set hourOfYearPerMonth while it is final!");
		}
	}
	public void setDayOfWeek1jan(int p_dayOfWeek1jan) {
		if(!dayOfWeek1janIsFinal) {
			dayOfWeek1jan = p_dayOfWeek1jan;
			dayOfWeek1janIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set dayOfWeek1jan while it is final!");
		}
	}
	public void setRunStartTime_h(double p_runStartTime_h) {
		if(!runStartTime_hIsFinal) {
			runStartTime_h = p_runStartTime_h;
			runStartTime_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set runStartTime_h while it is final!");
		}
	}
	public void setRunEndTime_h(double p_runEndTime_h) {
		if(!runEndTime_hIsFinal) {
			runEndTime_h = p_runEndTime_h;
			runEndTime_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set runEndTime_h while it is final!");
		}
	}
	public void setWinterWeekNumber(int p_winterWeekNumber) {
		if(!winterWeekNumberIsFinal) {
			winterWeekNumber = p_winterWeekNumber;
			winterWeekNumberIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set winterWeekNumber while it is final!");
		}
	}
	public void setSummerWeekNumber(int p_summerWeekNumber) {
		if(!summerWeekNumberIsFinal) {
			summerWeekNumber = p_summerWeekNumber;
			summerWeekNumberIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set summerWeekNumber while it is final!");
		}
	}
	public void setStartOfSummerWeek_h(double p_startOfSummerWeek_h) {
		if(!startOfSummerWeek_hIsFinal) {
			startOfSummerWeek_h = p_startOfSummerWeek_h;
			startOfSummerWeek_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set startOfSummerWeek_h while it is final!");
		}
	}
	public void setStartOfWinterWeek_h(double p_startOfWinterWeek_h) {
		if(!startOfWinterWeek_hIsFinal) {
			startOfWinterWeek_h = p_startOfWinterWeek_h;
			startOfWinterWeek_hIsFinal = true;
		}
		else {
			throw new RuntimeException("Trying to set startOfWinterWeek_h while it is final!");
		}
	}
	*/

}