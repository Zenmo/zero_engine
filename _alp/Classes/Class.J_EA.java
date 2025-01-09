/**
 * J_EA
 */
import java.util.EnumSet;

abstract public class J_EA implements Cloneable {
	
	 protected Agent parentAgent;
	 public OL_EnergyAssetType energyAssetType;
	 public String energyAssetName;
	 protected double v_powerFraction_fr = 0; // Better to make this one protected? Public is needed to access from other packages, for example when inheriting a GC-type in your project with its own flexmanagement functions
	 protected J_FlowsMap flowsMap = new J_FlowsMap();
	 protected J_FlowsMap lastFlowsMap = new J_FlowsMap();
	 protected double lastEnergyUse_kW = 0.0;
	 
	 protected EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class); // To fill activeProductionEnergyCarriers in GridConnections and EnergyModel	
	 protected EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers = EnumSet.noneOf(OL_EnergyCarriers.class); // To fill activeConsumptionEnergyCarriers in GridConnections and EnergyModel

	 protected double energyUsed_kWh = 0.0;
	 protected double energyUse_kW = 0.0;
	 protected double energyUsedStored_kWh = 0.0;
  	 protected double timestep_h;
 
  	 // Are these needed?
	 protected double heatProduced_kWh = 0.0;
	 protected double heatConsumed_kWh = 0.0;
	 protected double electricityProduced_kWh = 0.0;
	 
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
		//this.capacityElectric_kW = capacityElectric_kW;
		//this.capacityHeat_kW = capacityHeat_kW;
		//this.capacityGas_kW = capacityGas_kW;
		registerEnergyAsset();
    }
    
    protected void registerEnergyAsset() {
		/*if (parentAgent instanceof GridConnection) {
			this.selectedEnergyCarriers = ((GridConnection)parentAgent).energyModel.c_selectedEnergyCarriers;
		}
		else if (parentAgent instanceof GridNode) {
			this.selectedEnergyCarriers = ((GridNode)parentAgent).energyModel.c_selectedEnergyCarriers;
		}*/
		
		// TODO: check if EA is using an energycarrier that is not in the selectedenergycarriers. If so, throw an error!
    	if ( parentAgent instanceof GridConnection) {
    		((GridConnection)parentAgent).f_connectToJ_EA(this);
    	} else {
    		traceln("Energy asset %s doesn't have a valid parent agent! Will not be operated!", this);
    	}
    }
    
    public void removeEnergyAsset() {
    	if ( parentAgent instanceof GridConnection) {
    		((GridConnection)parentAgent).f_removeTheJ_EA(this);
    	} else {    		
    		traceln("Energy asset %s doesn't have a valid parent agent! Energy Asset not removed!", this);
    	}
    	
    }
   
    //public Pair<J_FlowsMap, Double> f_updateAllFlows(double powerFraction_fr) {
    public void f_updateAllFlows(double powerFraction_fr) {

     	double powerFractionBounded_fr = min(1,max(-1, powerFraction_fr));
     	//Pair<J_FlowsMap, Double> flowsPair = operate(powerFractionBounded_fr);
     	operate(powerFractionBounded_fr);
     	//J_FlowsMap flowsMap = flowsPair.getFirst();
     	//return arr;
    	if (parentAgent instanceof GridConnection) {    		
    		//((GridConnection)parentAgent).f_addFlows(arr, this);
    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, this);
    	}
    	/*
    	if (ui_energyAsset!= null) {
    		//ui_energyAsset.f_addFlows(arr);
    		ui_energyAsset.f_addFlows(flowsMap);
    	}
    	*/
    	this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    	/*traceln("flowsMap: %s", flowsMap);
    	flowsMap.clear();
    	traceln("flowsMap after reset: %s", flowsMap);
    	traceln("lastflowsMap after flowsmap reset: %s", lastFlowsMap);
    	//return arr;*/
    	//return this.flowsMap;
    }
    
    public void f_updateAllFlows() {
    	this.f_updateAllFlows(this.v_powerFraction_fr);
    }
    
    public void f_setPowerFraction_fr(double powerFraction_fr) {
    	this.v_powerFraction_fr = powerFraction_fr;
    }
    
    public void clear() {
	    flowsMap.clear();   	  	
    	energyUse_kW = 0;
    	v_powerFraction_fr = 0;
    }

	public abstract void operate(double ratioOfCapacity);
     
    public double getCurrentStateOfCharge() {
    	return 0;
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
    
    public double getCurrentTemperature() {
    	throw new RuntimeException("Method getCurrentTemperature() should be overridden in child class of J_EA!");
    	//return 0;
    }
      
    public void updateAmbientTemperature(double currentAmbientTemperature_degC) {
    	// only for storage agents. Does it belong in this superclass?
    	throw new RuntimeException("Method updateAmbientTemperature() should be overridden in child class of J_EA!");
    }
    
    public double getEnergyUsed_kWh() {
    	return energyUsed_kWh;
    }
    
    public double getElectricityProduced_kWh() {
    	return electricityProduced_kWh;
	}

    public double getSetTemperature_degC() {
    	return 0;
    }
    
    public double getMinTemperature_degC() {
    	return 0;
    }
    
    public double getMaxTemperature_degC() {
    	return 0;
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
	
    //public void setUI_EnergyAsset(UI_EnergyAsset ui_energyAsset) {
    	//this.ui_energyAsset = ui_energyAsset;
    //}
    
    public J_FlowsMap getLastFlows() {
    	//return lastFlowsArray;
    	//return new Pair(this.lastFlowsMap, this.lastEnergyUse_kW);
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
	 

    
//    public double getOutputTemperature_degC() {
//   	return 0;
//    }
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
