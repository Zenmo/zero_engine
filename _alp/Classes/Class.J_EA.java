/**
 * J_EA
 */
import java.util.EnumSet;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;


@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type"
	)

@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
abstract public class J_EA implements Cloneable {
	protected J_TimeParameters timeParameters;
	
	protected Agent parentAgent;
	protected OL_EnergyAssetType energyAssetType;
	protected OL_AssetFlowCategories assetFlowCategory;
	protected String energyAssetName;
	protected J_FlowsMap flowsMap = new J_FlowsMap();
	protected J_FlowsMap lastFlowsMap = new J_FlowsMap();
	protected J_ValueMap assetFlowsMap = new J_ValueMap(OL_AssetFlowCategories.class);
	protected double lastEnergyUse_kW = 0.0;
	 
	protected EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class); // To fill activeProductionEnergyCarriers in GridConnections and EnergyModel	
	protected EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class); // To fill activeConsumptionEnergyCarriers in GridConnections and EnergyModel
		
	protected double energyUsed_kWh = 0.0;
	protected double energyUse_kW = 0.0;
	protected double energyUsedStored_kWh = 0.0;
	protected double timestep_h;
	 
	protected boolean isRemoved = false;

    /**
     * Default constructor
     */
    public J_EA() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_EA(Agent parentAgent, double capacityElectric_kW, double capacityHeat_kW, double capacityGas_kW) {
		this.parentAgent = parentAgent;
		registerEnergyAsset();
    }
    
    protected void registerEnergyAsset() {	
    	if ( parentAgent instanceof GridConnection) {
    		((GridConnection)parentAgent).f_connectToJ_EA(this);
    	} else {
    		traceln("Energy asset %s doesn't have a valid parent agent! Will not be operated!", this);
    	}
    }
    
    public void reRegisterEnergyAsset() {
    	if (!this.isRemoved) {
    		throw new RuntimeException("Can not register energy asset that was not removed.");
    	}
    	else {
    		this.isRemoved = false;
    		this.registerEnergyAsset();
    	}
    }
    
    public void removeEnergyAsset() {
    	this.isRemoved = true;
    	if ( parentAgent instanceof GridConnection) {
    		((GridConnection)parentAgent).f_removeTheJ_EA(this);
    	} else {    		
    		traceln("Energy asset %s doesn't have a valid parent agent! Energy Asset not removed!", this);
    	}    	
    }
    
    public void clear() {
	    flowsMap.clear();
	    assetFlowsMap.clear();
    	energyUse_kW = 0;
    }

    public void storeStatesAndReset() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsedStored_kWh = energyUsed_kWh;
    	energyUsed_kWh = 0.0;
    	clear();    	
    }
    
    public void restoreStates() {
    	// Each energy asset that has some states should overwrite this function!
    	energyUsed_kWh = energyUsedStored_kWh;
    }
    
    public double getEnergyUsed_kWh() {
    	return energyUsed_kWh;
    }
    
    public Agent getParentAgent() {
    	return parentAgent;
    }
    
	public EnumSet<OL_EnergyCarriers> getActiveProductionEnergyCarriers() {
		return this.activeProductionEnergyCarriers;
	}
    
    public EnumSet<OL_EnergyCarriers> getActiveConsumptionEnergyCarriers() {
		return this.activeConsumptionEnergyCarriers;
	}
	
	public EnumSet<OL_EnergyCarriers> getActiveEnergyCarriers() {
		EnumSet<OL_EnergyCarriers> allActiveEnergyCarriers = EnumSet.copyOf(this.activeProductionEnergyCarriers);
		allActiveEnergyCarriers.addAll(this.activeConsumptionEnergyCarriers);
		return allActiveEnergyCarriers;
	}
	
    public J_FlowsMap getLastFlows() {
    	return this.lastFlowsMap;
    }
    
	 public OL_EnergyAssetType getEAType() {
		 return energyAssetType;
	 }

	 public void setEnergyAssetName(String name) {
		 this.energyAssetName = name;
	 }
	 
	 public void setEnergyAssetType(OL_EnergyAssetType assetType) {
		 this.energyAssetType = assetType;
	 }
	 
	 public void setAssetFlowCategory(OL_AssetFlowCategories assetFlowCat) {
		 this.assetFlowCategory = assetFlowCat;
	 }

	/* 
    @Override    
    public Object clone() { 
    	try {
    		return super.clone(); 
    	} catch (CloneNotSupportedException e) {
    		throw new RuntimeException(e);
    	}
    } 
    */
	 
	@Override
	public String toString() {
		return
			"ownerAgent = " + parentAgent.getIndex() +" ";
	}
}
