public interface I_HeatingManagement {
	void manageHeating();
	// Initiliaze throws an exception when the configuration of assets is not a valid combination
	void initializeAssets();
	// not initialized sets the isInitialized flag to false. Is called when the (heating) assets in the GC change
	void notInitialized();
	// Every implementation must have a list of all its valid heating types that it supports
	List<OL_GridConnectionHeatingType> getValidHeatingTypes();
	// Every implementation must choose one type that the current instance is managing
	OL_GridConnectionHeatingType getCurrentHeatingType();
} 