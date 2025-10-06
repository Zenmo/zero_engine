/**
 * AgentMixin
 */	
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
@JsonTypeInfo(
	    use = JsonTypeInfo.Id.CLASS,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON
	)

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,//PUBLIC_ONLY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
//@JsonIgnoreProperties({"connections"})
@JsonIgnoreProperties({ 
	"_pl_powerFlows_autoUpdateEvent_xjal",
	"_SOC_plot_autoUpdateEvent_xjal",
	"c_defaultHeatingStrategies",
	"connections",

	// When also writing private values, these additional fields end up in the json:
	"ik",
	"presentation",
	"_getLevels_xjal"
})
	//"line",
	//"text",
	//"level",

	/*"icon",
	"font",
	"name",
	"color"*/

	//"gisRegion","p_uniqueColor","p_defaultFillColor","p_defaultLineColor",	
	/*"c_gridNodeExecutionList",
	"c_gridNodeExecutionListReverse",
	"c_gridNodesTopLevel",
	"c_gridNodesNotTopLevel",*/
	
	//"p_cookingPatternCsv","p_householdTripsCsv","p_truckTripsCsv",

	/*"data_liveLoad_kW","data_liveCapacitySupply_kW", "data_liveCapacityDemand_kW",
	"data_totalLoad_kW",
	"data_summerWeekLoad_kW",
	"data_winterWeekLoad_kW",*/
	/*"va_gridNode",
	"va_coop",
	"va_gridConnection",
	"va_ConnectionOwner",
	"va_engine",
	"_origin_VA",*/
	
	/*"pop_gridNodes","pop_gridConnections","Houses","pop_connectionOwners","pop_energySuppliers","pop_energyCoops","pop_gridOperators","pop_GIS_Objects","pop_GIS_Parcels","pop_GIS_Buildings",
	"DistrictHeatingSystems",
	"GridBatteries",
	"EnergyProductionSites",
	"UtilityConnections",
	"EnergyConversionSites",
	"PublicChargers",
	"Neighborhoods",*/



@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public abstract class AgentMixin implements Serializable {
	
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