public interface I_EnergyData{
	J_RapidRunData getRapidRunData();
    J_LiveData getLiveData();
    J_RapidRunData getPreviousRapidRunData();
    OL_ResultScope getScope();
}