/**
 * DataSetConstructor
 */	
public class DataSetConstructor {
	private final static int HOURS_IN_WEEK = 168;
	
	/*
	 * Returns a new "empty" DataSet.
	 * It is empty in the sense that it has value zero for all timesteps that are before the current model time.
	 */
	public static DataSet getNewLiveWeekDataSet( J_TimeParameters timeParameters, J_TimeVariables timeVariables ) {
		DataSet ds = new DataSet( roundToInt(HOURS_IN_WEEK / timeParameters.getTimeStep_h()));

		double endTime = timeVariables.getAnyLogicTime_h();
		double startTime = max(0, timeVariables.getAnyLogicTime_h() - HOURS_IN_WEEK);
		
		for (double t = startTime; t <= endTime; t += timeParameters.getTimeStep_h()) {
			ds.add( t, 0);
		}
		
		return ds;
	}
}