public interface I_HeatingManagement
{
void manageHeating();
// Initiliaze throws an exception when the configuration of assets is not a valid combination
void initializeAssets();
// not initialized sets the isInitialized flag to false. Is called when the (heating) assets in the GC change
void notInitialized();
}