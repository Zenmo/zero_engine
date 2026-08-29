import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.time.temporal.Temporal;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // also stores full profiles to file. Maybe arrange a way to 'skip' this?
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
/**
 * Static date and time information of the simulation.
 */
public final class J_TimeParameters {
	/**
	 * Simulation time resolution in hours.
	 */
	private final double timeStep_h;
	
	/** 
	 * Simulation time resolution. Represents the same information as timeStep_h.
	 * This data type is used by the time series library which we're developing.
	 * 
	 * This is expressed with the abstract TemporalAmount instead of the concrete 
	 * java.time.Duration because we might want to switch to a less precise,
	 * more optimized implementation.
	 * 
	 * Example usage to manipulate time:
	 * 
	 * {@snippet :
	 *     Temporal nextTimeStep = this.start.plus(this.stepDuration);
	 * }
	 * 
	 * Example usage to print:
	 * 
	 * {@snippet :
	 *     String.format(
	 *         "The time resolution is %d minutes",
	 *         Duration.from(this.stepDuration).toMinutes()
	 *     )
	 * }
	 */
	private final TemporalAmount stepDuration;
	
	private final int startYear;
	
	/**
	 * Start date and time of the simulated period. Represents the same 
	 * information as runStartTime_h. This data type is used by the time 
	 * series library which we're developing.
	 * 
	 * This is expressed with the abstract Temporal instead of the concrete 
	 * java.time.Instant because we might want to switch to a less precise,
	 * more optimized implementation.
	 * 
	 * Example usage to print the start time to the user using Dutch 
	 * conventions:
	 * 
	 * {@snippet :
	 *     var formatter = DateTimeFormatter.ofLocalizedDateTime(
	 *                     FormatStyle.LONG, FormatStyle.MEDIUM
	 *             )
	 *             .withLocale(new Locale("nl", "NL"));
	 *
	 *     var zonedStart = Instant.from(this.start)
     *             .atZone(ZoneId.of("Europe/Amsterdam"));
     *         
     *     return formatter.format(start);
	 * }
	 * 
	 * Example output: "18 juni 2026, 11:45:00"
	 */
	private final Temporal start;
	
	/**
	 * End date and time of the simulated period. Represents the same 
	 * information as runEndTime_h.
	 */
	private final Temporal end;
	
	private final double[] monthStartHours;
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
		double[] monthStartHours,
		double runStartTime_h,
		double runEndTime_h,
		int summerWeekNumber,
		int winterWeekNumber
	) {
		this.timeStep_h = timeStep_h;
		this.stepDuration = hoursToDuration(timeStep_h);
		this.startYear = startYear;
		this.start = instantFromHourOffset(startYear, runStartTime_h);
		this.end = instantFromHourOffset(startYear, runEndTime_h);
		this.monthStartHours = monthStartHours;
		this.runStartTime_h = runStartTime_h;
		this.runEndTime_h = runEndTime_h;
		this.summerWeekNumber = summerWeekNumber;
		this.winterWeekNumber = winterWeekNumber;
		this.dayOfWeek1jan = DayOfWeek.from(LocalDate.of(startYear, 1, 1)).getValue();
		this.startOfSummerWeek_h = roundToInt(24 * (summerWeekNumber * 7 + (8-dayOfWeek1jan)%7));
		this.startOfWinterWeek_h = roundToInt(24 * (winterWeekNumber * 7 + (8-dayOfWeek1jan)%7));
	}
	
	// Creator for deserialisation. (needed because of final fields!)
	@JsonCreator
    public J_TimeParameters(
        @JsonProperty("timeStep_h") double timeStep_h,
        @JsonProperty("stepDuration") Duration stepDuration,
        @JsonProperty("startYear") int startYear,
        @JsonProperty("startYearObject") Year startYearObject,
        @JsonProperty("monthStartHours") double[] monthStartHours,
        @JsonProperty("dayOfWeek1jan") int dayOfWeek1jan,
        @JsonProperty("runStartTime_h") double runStartTime_h,
        @JsonProperty("runEndTime_h") double runEndTime_h,
        @JsonProperty("summerWeekNumber") int summerWeekNumber,
        @JsonProperty("winterWeekNumber") int winterWeekNumber,
        @JsonProperty("startOfSummerWeek_h") double startOfSummerWeek_h,
        @JsonProperty("startOfWinterWeek_h") double startOfWinterWeek_h
    ) {
		this(timeStep_h, startYear, monthStartHours, runStartTime_h, runEndTime_h, summerWeekNumber, winterWeekNumber);
    }
	
	////Time Parameter getters
	public double getTimeStep_h() {
	    return timeStep_h;
	}
	public TemporalAmount getStepDuration() {
		return stepDuration;
	}
	public int getStartYear() {
	    return startYear;
	}
	public Temporal getStart() {
	    return start;
	}
	public Temporal getEnd() {
	    return end;
	}
	public double[] getMonthStartHours() {
	    return monthStartHours;
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
	public double getRunDuration_h() {
		return runEndTime_h - runStartTime_h;
	}
	
	//Static methods
	public static OL_Days getDayFromDayIndex(int dayIndex){
		switch(dayIndex) {
		    case 0:
		        return OL_Days.MONDAY;
		    case 1:
		        return OL_Days.TUESDAY;
		    case 2:
		        return OL_Days.WEDNESDAY;
		    case 3:
		        return OL_Days.THURSDAY;
		    case 4:
		        return OL_Days.FRIDAY;
		    case 5:
		        return OL_Days.SATURDAY;
		    case 6:
		        return OL_Days.SUNDAY;
		    default:
		    	throw new RuntimeException("Day found that should not exist.");
		}
	}
	public static int getDayIndexFromDay(OL_Days day){
		switch(day) {
		    case MONDAY:
		        return 0;
		    case TUESDAY:
		        return 1;
		    case WEDNESDAY:
		        return 2;
		    case THURSDAY:
		        return 3;
		    case FRIDAY:
		        return 4;
		    case SATURDAY:
		        return 5;
		    case SUNDAY:
		        return 6;
		    default:
		    	throw new RuntimeException("Day found that should not exist.");
		}
	}
	public static List<OL_Days> getOrderedDaysList(){
		return List.of(
			    OL_Days.MONDAY,
			    OL_Days.TUESDAY,
			    OL_Days.WEDNESDAY,
			    OL_Days.THURSDAY,
			    OL_Days.FRIDAY,
			    OL_Days.SATURDAY,
			    OL_Days.SUNDAY
			);
    }

	private static Duration hoursToDuration(double hours) {
		double seconds = hours * 3600;
		return Duration.ofSeconds((long) seconds);
	}
	
	private static Instant instantFromHourOffset(int year, double hourOffset) {
        var offset = hoursToDuration(hourOffset);
        
        return startOfYear(year).plus(offset);
	}
	
	private static Instant startOfYear(int year) {
		// LUX mostly starts at jan 1st at 00:00 Europe/Amsterdam time
		var timeZone = ZoneId.of("Europe/Amsterdam");
        
		return Year.of(year).atMonth(1).atDay(1).atStartOfDay(timeZone).toInstant();
	}
	
	@Override
	public String toString() {
	    return "J_TimeParameters{" +
	            "timeStep_h=" + timeStep_h +
	            ", startYear=" + startYear +
	            ", monthStartHours=" + java.util.Arrays.toString(monthStartHours) +
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
