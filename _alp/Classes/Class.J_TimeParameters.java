import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalField;

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
 * J_TimeParameters
 */	
public final class J_TimeParameters {
	////Time parameters:
	private final Duration timeStep;
	//private final int startYear;
	//private final Date startDate;
	private final Instant startInstant;
	//private final double[] hourOfYearPerMonth;
	//private final int dayOfWeek1jan;
	//private final double runStartTime_h;
	//private final double runEndTime_h;
	private final Duration simDuration;
	private final int summerWeekNumber;
	private final int winterWeekNumber;
	//private final double startOfSummerWeek_h;
	//private final double startOfWinterWeek_h;

	
	/*public J_TimeParameters(
		double timeStep_h,
		Instant startInstant,
		//int startYear,
		//double[] hourOfYearPerMonth,
		//double runStartTime_h,
		//double runEndTime_h,
		double simDuration_h,
		int summerWeekNumber,
		int winterWeekNumber
	) {
		this.timeStep_h = timeStep_h;
		this.startInstant = startInstant;
		//this.hourOfYearPerMonth = hourOfYearPerMonth;
		//this.runStartTime_h = runStartTime_h;
		//this.runEndTime_h = runEndTime_h;
		this.simDuration_h = simDuration_h;
		this.summerWeekNumber = summerWeekNumber;
		this.winterWeekNumber = winterWeekNumber;
		int dayOfWeek1jan = this.getDayOfWeek1jan();
		this.startOfSummerWeek_h = roundToInt(24 * (summerWeekNumber * 7 + (8-dayOfWeek1jan)%7));
		this.startOfWinterWeek_h = roundToInt(24 * (winterWeekNumber * 7 + (8-dayOfWeek1jan)%7));
		
		//int month = timeParameters.get
		ZonedDateTime timestamp = ZonedDateTime.of(
		        this.startYear, 1, 1,    // Year, Month, Day; should still derive Month and Day from runStartTime_h!
		        0, 0, 0, 0,    // Hour, Minute, Second, Nano
		        ZoneId.of("CET"));
		        
		// 2. Convert to Instant (Point in time)
		//this.startInstant = timestamp.toInstant();
	}*/
	
	// Creator for deserialisation. (needed because of final fields!)
	@JsonCreator
    public J_TimeParameters(
        @JsonProperty("timeStep") Duration timeStep,
        @JsonProperty("startInstant") Instant startInstant,
        //@JsonProperty("hourOfYearPerMonth") double[] hourOfYearPerMonth,
        //@JsonProperty("dayOfWeek1jan") int dayOfWeek1jan,
        //@JsonProperty("runStartTime_h") double runStartTime_h,
        //@JsonProperty("runEndTime_h") double runEndTime_h,
        @JsonProperty("simDuration") Duration simDuration,
        @JsonProperty("summerWeekNumber") int summerWeekNumber,
        @JsonProperty("winterWeekNumber") int winterWeekNumber
        //@JsonProperty("startOfSummerWeek_h") double startOfSummerWeek_h,
        //@JsonProperty("startOfWinterWeek_h") double startOfWinterWeek_h
    ) {
		//this(timeStep_h, startInstant, simDuration_h, summerWeekNumber, winterWeekNumber);
		this.timeStep = timeStep;
		this.startInstant = startInstant;
		//this.hourOfYearPerMonth = hourOfYearPerMonth;
		//this.runStartTime_h = runStartTime_h;
		//this.runEndTime_h = runEndTime_h;
		this.simDuration = simDuration;
		this.summerWeekNumber = summerWeekNumber;
		this.winterWeekNumber = winterWeekNumber;
		int dayOfWeek1jan = this.getDayOfWeek1jan();
		//this.startOfSummerWeek_h = roundToInt(24 * (summerWeekNumber * 7 + (8-dayOfWeek1jan)%7));
		//this.startOfWinterWeek_h = roundToInt(24 * (winterWeekNumber * 7 + (8-dayOfWeek1jan)%7));
    }
	
	////Time Parameter getters
	public double getTimeStep_h() {
	    return (double)timeStep.getSeconds()/3600.0;
	}
	public int getStartYear() {
	    return startInstant.atZone(ZoneId.of("CET")).getYear();
	}
	/*
	public double[] getHourOfYearPerMonth() {
	    return hourOfYearPerMonth;
	}*/
	public int getDayOfWeek1jan() {
		int startYear = this.getStartYear();
		int dayOfWeek1jan = DayOfWeek.from(LocalDate.of(startYear, 1, 1)).getValue();
	    return dayOfWeek1jan;
	}
	public double getRunStartTime_h() {
		return (startInstant.atZone(ZoneId.of("CET")).getDayOfYear() -1) * 24;
	}
	    
	public double getRunEndTime_h() {
	    return this.getRunStartTime_h() + this.getRunDuration_h();
	}
	
	public double getRunDuration_h() {
		return (double)simDuration.getSeconds()/3600.0;
	}
	
	public int getSummerWeekNumber() {
	    return summerWeekNumber;
	}
	public int getWinterWeekNumber() {
	    return winterWeekNumber;
	}
	public double getStartOfSummerWeek_h() {
	    return roundToInt(24 * (summerWeekNumber * 7 + (8-this.getDayOfWeek1jan())%7));
	}
	public double getStartOfWinterWeek_h() {
	    return roundToInt(24 * (winterWeekNumber * 7 + (8-this.getDayOfWeek1jan())%7));
	}
	
	public Instant getStartInstant() {
		return this.startInstant;
	}
	
	@Override
	public String toString() {
	    return "J_TimeParameters{" +
	            "timeStep=" + this.timeStep +
	            ", startInstant=" + this.startInstant +
	            //", hourOfYearPerMonth=" + java.util.Arrays.toString(hourOfYearPerMonth) +
	            ", dayOfWeek1jan=" + this.getDayOfWeek1jan() +
	            ", runStartTime_h=" + this.getRunStartTime_h() +
	            ", runEndTime_h=" + this.getRunEndTime_h() +
	            ", summerWeekNumber=" + summerWeekNumber +
	            ", winterWeekNumber=" + winterWeekNumber +
	            //", startOfSummerWeek_h=" + startOfSummerWeek_h +
	            //", startOfWinterWeek_h=" + startOfWinterWeek_h +
	            '}';
	}
}
