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
	
	@Override
	public String toString() {
	    return "J_TimeParameters{" +
	            "timeStep_h=" + timeStep_h +
	            ", startYear=" + startYear +
	            ", hourOfYearPerMonth=" + java.util.Arrays.toString(hourOfYearPerMonth) +
	            ", dayOfWeek1jan=" + dayOfWeek1jan +
	            ", runStartTime_h=" + runStartTime_h +
	            ", runEndTime_h=" + runEndTime_h +
	            ", summerWeekNumber=" + summerWeekNumber +
	            ", winterWeekNumber=" + winterWeekNumber +
	            ", startOfSummerWeek_h=" + startOfSummerWeek_h +
	            ", startOfWinterWeek_h=" + startOfWinterWeek_h +
	            '}';
	}
}