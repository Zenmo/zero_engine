//This class contains all asset management classes are required to have.

public interface I_AssetManagement
{	
	//Get agent that contains the management
	Agent getParentAgent();
	
	//Store current state of live sim, and reset to initial state for rapid run
    void storeStatesAndReset();
    
    //Restore state to correct state of live sim after rapid run
    void restoreStates();
}