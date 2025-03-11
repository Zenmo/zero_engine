public interface I_EnergyData{
	J_RapidRunData getRapidRunData();
    J_LiveData getLiveData();
    J_RapidRunData getPreviousRapidRunData();
    J_ActiveAssetData getActiveAssetData();
    OL_ResultScope getScope();
    double getDeliveryCapacity_kW();
    double getFeedinCapacity_kW();
    boolean getDeliveryCapacityKnown();
    boolean getFeedinCapacityKnown();
}