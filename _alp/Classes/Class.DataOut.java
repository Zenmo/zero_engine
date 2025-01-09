/**
 * DataOut
 */
public class DataOut implements Serializable {

	public ArrayList actorData = new ArrayList();
	public ArrayList runSettingsData = new ArrayList(1);
	public ArrayList simulationResults = new ArrayList(2);
	public ArrayList hourlyCurvesData = new ArrayList();
	public ArrayList contractData = new ArrayList();
	//public DataSet dataSet = new DataSet(8760); // Test to make dataobject with simulation results per agent, universal for different agenttypes, such as model-wide, gridconnection (building), gridconnection (neighbourhood)

	public void clearData() {
		actorData.clear();
		runSettingsData.clear();
		simulationResults.clear();
		hourlyCurvesData.clear();
		contractData.clear();
	}
}