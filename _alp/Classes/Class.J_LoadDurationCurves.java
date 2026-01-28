/**
 * J_LoadDurationCurves
 */	
public class J_LoadDurationCurves implements Serializable {
	private boolean firstRun = true;
	
	public int arraySize;
	private double[] netLoadArrayAnnual_kW;
	public double[] loadDuractionCurveTotal_kW;
	private double[] netLoadArraySummerweek_kW;
	private double[] netLoadArrayWinterweek_kW;
	private double[] netLoadArrayDaytime_kW;
	private double[] netLoadArrayNighttime_kW;
	private double[] netLoadArrayWeekday_kW;
	private double[] netLoadArrayWeekend_kW;
	public DataSet ds_loadDurationCurveTotal_kW;
	//area.v_dataNetbelastingDuurkrommeYearVorige_kW = new DataSet(roundToInt(365*24/energyModel.p_timeStep_h));

	public DataSet ds_loadDurationCurveSummer_kW;// = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	public DataSet ds_loadDurationCurveWinter_kW;// = new DataSet(roundToInt(7*24/energyModel.p_timeStep_h));
	public DataSet ds_loadDurationCurveDaytime_kW;// = new DataSet(roundToInt((energyModel.p_runEndTime_h-energyModel.p_runStartTime_h)/2/energyModel.p_timeStep_h));
	public DataSet ds_loadDurationCurveNighttime_kW;// = new DataSet(roundToInt((energyModel.p_runEndTime_h-energyModel.p_runStartTime_h)/2/energyModel.p_timeStep_h));
	public DataSet ds_loadDurationCurveWeekday_kW;// = new DataSet(roundToInt(arraySize/7.0*5)+100);
	public DataSet ds_loadDurationCurveWeekend_kW;// = new DataSet(roundToInt(arraySize/7.0*2)+100);
	
	public DataSet ds_previousLoadDurationCurveTotal_kW;
    /**
     * Default constructor
     */
    public J_LoadDurationCurves() {
    }
   
    public J_LoadDurationCurves(double[] loadArray_kW, J_TimeParameters timeParameters) {
    	calculateLoadDurationCurves(loadArray_kW, timeParameters);
    }    
    
    public void calculateLoadDurationCurves(double[] loadArray_kW, J_TimeParameters timeParameters) {
    	// This is quite a large function so for readability we define
    	double timeStep_h = timeParameters.getTimeStep_h();
    	
    	this.arraySize = loadArray_kW.length;
    	if (ds_loadDurationCurveTotal_kW != null) {	
    		if (ds_previousLoadDurationCurveTotal_kW != null) { // Not second run either!
    			ds_previousLoadDurationCurveTotal_kW.reset();
    		} else {
    			ds_previousLoadDurationCurveTotal_kW = new DataSet(arraySize);
    		}
    		firstRun = false;
    	}
    
    	this.netLoadArrayAnnual_kW = Arrays.copyOf(loadArray_kW,arraySize);
    	this.loadDuractionCurveTotal_kW = new double[arraySize];
    	this.netLoadArraySummerweek_kW = new double[roundToInt(168 / timeStep_h)];
    	this.netLoadArrayWinterweek_kW= new double[roundToInt(168 / timeStep_h)];
    	this.netLoadArrayDaytime_kW = new double[arraySize/2];
    	this.netLoadArrayNighttime_kW = new double[arraySize/2];
    	// For different years the amount of weekdays and weekend days may be different, so the size will be variable for now
    	ArrayList<Double> listNetLoadArrayWeekday_kW = new ArrayList<>();
    	ArrayList<Double> listNetLoadArrayWeekend_kW = new ArrayList<>();
    	 
    	int i_winter=0;
    	int i_summer=0;
    	int i_day=0;
    	int i_night=0;
    	int i_weekday=0;
    	int i_weekend=0;

    	int maxIndex = 0; // index with peak import
    	for (int i = 1; i < loadArray_kW.length; i++) {
    	    if (loadArray_kW[i] > loadArray_kW[maxIndex]) {
    	        maxIndex = i;
    	    }
    	}
    	
    	int minIndex = 0; // index with peak export
    	for (int i = 1; i < loadArray_kW.length; i++) {
    	    if (loadArray_kW[i] < loadArray_kW[minIndex]) {
    	        minIndex = i;
    	    }
    	}
    	//double[] annualElectricityBalanceTimeSeries_kW = acc_annualElectricityBalance_kW.getTimeSeries();
    	boolean replaceSummerWinterWithPeaks = true;
    	
    	for(int i=0; i<arraySize ; i++) {
    		if (!firstRun) {
    			// First we make sure to store our previous Load Curve
    			ds_previousLoadDurationCurveTotal_kW.add(i*timeStep_h, ds_loadDurationCurveTotal_kW.getY(i));		
    		}
    		
    		if (replaceSummerWinterWithPeaks) {
	    		// peak weeks
	    		if (i >= (minIndex - roundToInt(84 / timeStep_h)) && i < (minIndex + roundToInt(84 / timeStep_h)) ) {
	    			netLoadArraySummerweek_kW[i_summer]=-netLoadArrayAnnual_kW[i];
	    			i_summer++;
	    		}
	    		
	    		if (i >= (maxIndex - roundToInt(84 / timeStep_h)) && i < (maxIndex + roundToInt(84 / timeStep_h)) ) {
	    			netLoadArrayWinterweek_kW[i_winter]=-netLoadArrayAnnual_kW[i];
	    			i_winter++;
	    		}
			} else {
				// summer/winter
				if (timeParameters.getRunStartTime_h() + i*timeStep_h > timeParameters.getStartOfSummerWeek_h() && timeParameters.getRunStartTime_h() + i*timeStep_h<= timeParameters.getStartOfSummerWeek_h() + 24*7) {
					netLoadArraySummerweek_kW[i_summer]=-netLoadArrayAnnual_kW[i];
					i_summer++;
				}
				if (timeParameters.getRunStartTime_h() + i*timeStep_h > timeParameters.getStartOfWinterWeek_h() && timeParameters.getRunStartTime_h() + i*timeStep_h<= timeParameters.getStartOfWinterWeek_h() + 24*7) {
					netLoadArrayWinterweek_kW[i_winter]=-netLoadArrayAnnual_kW[i];
					i_winter++;
				}
			}
    		
    		// day/night
    		if (i*timeStep_h % 24 > 6 && i*timeStep_h % 24 <= 18) { //daytime
    			netLoadArrayDaytime_kW[i_day]=-netLoadArrayAnnual_kW[i];
    			i_day++;
    		} else {
    			netLoadArrayNighttime_kW[i_night]=-netLoadArrayAnnual_kW[i];
    			i_night++;
    		}
    		//Weekday/weekend
    		if (((timeParameters.getRunStartTime_h() + i*timeStep_h+ 24*(timeParameters.getDayOfWeek1jan()-1)) % (24*7)) < (24*5)) { // Simulation starts on a Thursday, hence the +3 day offset on t_h
    			listNetLoadArrayWeekday_kW.add(-netLoadArrayAnnual_kW[i]);
    			i_weekday++;
    		} else {
    			listNetLoadArrayWeekend_kW.add(-netLoadArrayAnnual_kW[i]);
    			i_weekend++;
    		}
    		
    	}
    	 
    	// Now we have the size of the weekday & weekend arrays.
    	this.netLoadArrayWeekday_kW = new double[listNetLoadArrayWeekday_kW.size()];
    	this.netLoadArrayWeekend_kW = new double[listNetLoadArrayWeekend_kW.size()];
    	for (int i = 0; i < listNetLoadArrayWeekday_kW.size(); i++) {
    		this.netLoadArrayWeekday_kW[i] = listNetLoadArrayWeekday_kW.get(i);
    	}
    	for (int i = 0; i < listNetLoadArrayWeekend_kW.size(); i++) {
    		this.netLoadArrayWeekend_kW[i] = listNetLoadArrayWeekend_kW.get(i);
    	}
    	 
    	 
    	// Sort all arrays
    	Arrays.sort(netLoadArrayAnnual_kW);

    	Arrays.sort(netLoadArraySummerweek_kW);
    	Arrays.sort(netLoadArrayWinterweek_kW);
    	Arrays.sort(netLoadArrayDaytime_kW);
    	Arrays.sort(netLoadArrayNighttime_kW);
    	Arrays.sort(netLoadArrayWeekday_kW);
    	Arrays.sort(netLoadArrayWeekend_kW);

    	//int arraySize;
    	// Year
    	//   	arraySize = netLoadArrayAnnual_kW.length;
    	//ArrayUtils.reverse(netLoadArrayAnnual_kW);
    	ds_loadDurationCurveTotal_kW = new DataSet(roundToInt(arraySize));
    	for(int i=0; i< arraySize; i++) {
    		loadDuractionCurveTotal_kW[i]=netLoadArrayAnnual_kW[arraySize-i-1];
    		ds_loadDurationCurveTotal_kW.add(i*timeStep_h, loadDuractionCurveTotal_kW[i] );    		
    	}
    	
    	// Week
    	arraySize = netLoadArraySummerweek_kW.length;
    	ds_loadDurationCurveSummer_kW = new DataSet(arraySize);
    	ds_loadDurationCurveWinter_kW = new DataSet(arraySize);
    	for(int i=0; i< arraySize; i++) {
    		ds_loadDurationCurveSummer_kW.add(i*timeStep_h, -netLoadArraySummerweek_kW[i]);
    		ds_loadDurationCurveWinter_kW.add(i*timeStep_h, -netLoadArrayWinterweek_kW[i]);
    	}
    	    	// Day / Night
    	arraySize = netLoadArrayDaytime_kW.length;
    	ds_loadDurationCurveDaytime_kW = new DataSet(arraySize);
    	ds_loadDurationCurveNighttime_kW = new DataSet(arraySize);
    	for(int i=0; i< arraySize; i++) {
    		ds_loadDurationCurveDaytime_kW.add(i*timeStep_h, -netLoadArrayDaytime_kW[i]);
    		ds_loadDurationCurveNighttime_kW.add(i*timeStep_h, -netLoadArrayNighttime_kW[i]);
    	}
    	// Weekday
    	arraySize = netLoadArrayWeekday_kW.length;
    	ds_loadDurationCurveWeekday_kW = new DataSet(arraySize);
    	for(int i=0; i< arraySize; i++) {
    		ds_loadDurationCurveWeekday_kW.add(i*timeStep_h, -netLoadArrayWeekday_kW[i]);
    	}
    	// Weekend
    	arraySize = netLoadArrayWeekend_kW.length;
    	ds_loadDurationCurveWeekend_kW = new DataSet(arraySize);
    	for(int i=0; i< arraySize; i++) {
    		ds_loadDurationCurveWeekend_kW.add(i*timeStep_h, -netLoadArrayWeekend_kW[i]);
    	}
    }

    
    
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;
	
}													