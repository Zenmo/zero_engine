/**
 * EnergyModelMixin
 */	
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*@JsonAutoDetect(
    fieldVisibility = Visibility.PUBLIC_ONLY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)*/
@JsonIgnoreProperties({"p_cookingPatternCsv","p_householdTripsCsv","p_truckTripsCsv","connections",
	"pop_gridNodes","pop_gridConnections","Houses","pop_connectionOwners","pop_energySuppliers","pop_energyCoops","pop_gridOperators","pop_GIS_Objects","pop_GIS_Parcels","pop_GIS_Buildings",
	"DistrictHeatingSystems",
	"GridBatteries",
	"EnergyProductionSites",
	"UtilityConnections",
	"EnergyConversionSites",
	"PublicChargers",
	"Neighborhoods",
	"va_engine"})
//JsonIgnoreProperties({"color","VA_engine","_origin_VA","energyDataViewer"})


public abstract class EnergyModelMixin implements Serializable {
		
    	/*@JsonIgnore
    	public abstract void setLocation();

    	@JsonIgnore
    	public abstract void getLocation();
		
	
    	@JsonIgnore
    	public abstract void setNetworkNode();

    	@JsonIgnore
    	public abstract void getNetworkNode();*/
    /**
     * Default constructor
     */
    /*public PopuplationMixin() {
    }*/

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