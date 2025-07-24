import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public interface I_EnergyData{
	J_RapidRunData getRapidRunData();
    J_LiveData getLiveData();
    J_RapidRunData getPreviousRapidRunData();
    OL_ResultScope getScope();
}