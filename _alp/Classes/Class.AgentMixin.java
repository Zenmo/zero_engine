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
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type"  // 👈 this will be the field name in your JSON
	)
@JsonSubTypes({ // Needed because parentAgent in J_EA is of type Agent, and paused gridConnections are first serialized via their energy assets, leading to type ambigueity
    @JsonSubTypes.Type(value = Actor.class, name = "Actor"),
    @JsonSubTypes.Type(value = GridConnection.class, name = "GridConnection"),
    @JsonSubTypes.Type(value = GridNode.class, name = "GridNode"),
    @JsonSubTypes.Type(value = GIS_Object.class, name = "GIS_Object"),
    @JsonSubTypes.Type(value = EnergyModel.class, name = "EnergyModel"),
    @JsonSubTypes.Type(value = NationalEnergyMarket.class, name = "NationalEnergyMarket"),
    
    //@JsonSubTypes.Type(value = EnergyCoopL4L.class, name = "EnergyCoopL4L"),
   // @JsonSubTypes.Type(value = J_EAConversionGasBurner.class, name = "J_EAConversionGasBurner"),
   // @JsonSubTypes.Type(value = J_EAConversionHeatPump.class, name = "J_EAConversionHeatPump"),
   // @JsonSubTypes.Type(value = J_EAEV.class, name = "J_EAEV"),
   // @JsonSubTypes.Type(value = J_EADieselVehicle.class, name = "J_EADieselVehicle"),
    
    // Add other known subtypes here if needed
})
@JsonAutoDetect(
    fieldVisibility = Visibility.PUBLIC_ONLY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
//@JsonIgnoreProperties({"connections"})
@JsonIgnoreProperties({"_origin_VA",
	"gisRegion","p_uniqueColor","p_defaultFillColor","p_defaultLineColor",
	"data_liveLoad_kW","data_liveCapacitySupply_kW", "data_liveCapacityDemand_kW",
	"data_totalLoad_kW",
	"data_summerWeekLoad_kW",
	"data_winterWeekLoad_kW",
	"va_gridNode",
	"_pl_powerFlows_autoUpdateEvent_xjal"})

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