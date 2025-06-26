/**
 * J_EAProduction
 */
public class J_EAProduction extends zero_engine.J_EA implements Serializable {
	protected J_ProfilePointer profilePointer;
	protected OL_EnergyCarriers energyCarrier = OL_EnergyCarriers.ELECTRICITY;
	protected double totalEnergyCurtailed_kWh=0;
	//protected double outputTemperature_degC;
	protected double capacity_kW;

    /**
     * Default constructor
     */
	public J_EAProduction() {
    }

	/**
     * Constructor initializing the fields
     */
	public J_EAProduction(Agent parentAgent, OL_EnergyAssetType type, String name, OL_EnergyCarriers energyCarrier, double capacity_kW, double timestep_h, J_ProfilePointer profile) {
	    this.parentAgent = parentAgent;
	    this.energyAssetType = type;
	    this.energyAssetName = name;
	    this.energyCarrier = energyCarrier;
	    this.capacity_kW = capacity_kW;

	    this.timestep_h = timestep_h;
	    //this.outputTemperature_degC = outputTemperature_degC;
		if (profile == null) {
			profilePointer = ((GridConnection)parentAgent).energyModel.f_findProfile(name);
		} else {
			profilePointer = profile;
		}
		if (profile == null) {			
			throw new RuntimeException("J_EAProduction needs to have valid profilePointer!");
		}
	    this.activeProductionEnergyCarriers.add(this.energyCarrier);
		registerEnergyAsset();
	}
	
	public void setCapacityElectric_kW(double capacityElectric_kW) {
		// Calculate the difference with the set and the previous capacity to update totals in GC, GN and EnergyModel
		if (energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			double difference_kW = capacityElectric_kW - this.capacity_kW;
			if (this.energyAssetType == OL_EnergyAssetType.WINDMILL && this.parentAgent instanceof GridConnection) {		
				((GridConnection) this.parentAgent).v_liveAssetsMetaData.totalInstalledWindPower_kW += difference_kW;
				if (((GridConnection) this.parentAgent).p_parentNodeElectric != null) {
					((GridConnection) this.parentAgent).p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.WINDMILL, difference_kW, true);
				}
				((GridConnection) this.parentAgent).c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledWindPower_kW += difference_kW);
				((GridConnection) this.parentAgent).energyModel.v_liveAssetsMetaData.totalInstalledWindPower_kW += difference_kW;
			}
			else if (this.energyAssetType == OL_EnergyAssetType.PHOTOVOLTAIC && this.parentAgent instanceof GridConnection) {
				((GridConnection) this.parentAgent).v_liveAssetsMetaData.totalInstalledPVPower_kW += difference_kW;
				if (((GridConnection) this.parentAgent).p_parentNodeElectric != null) {
					((GridConnection) this.parentAgent).p_parentNodeElectric.f_updateTotalInstalledProductionAssets(OL_EnergyAssetType.PHOTOVOLTAIC, difference_kW, true);
				}
				((GridConnection) this.parentAgent).c_parentCoops.forEach( coop -> coop.v_liveAssetsMetaData.totalInstalledPVPower_kW += difference_kW);				
				((GridConnection) this.parentAgent).energyModel.v_liveAssetsMetaData.totalInstalledPVPower_kW += difference_kW;
			}
	
			this.capacity_kW = capacityElectric_kW;
		} else {			
			throw new RuntimeException("Production assets energy carrier is not electricity!");
		}
	}
	
	public double getCapacityElectric_kW() {
		if (energyCarrier == OL_EnergyCarriers.ELECTRICITY) {
			return this.capacity_kW;
		} else {			
			throw new RuntimeException("J_EAProduction is not electric!");
		}
	}
	
	public double getCapacityHeat_kW() {
		if (energyCarrier == OL_EnergyCarriers.HEAT) {
			return capacity_kW;			
		} else {			
			throw new RuntimeException("J_EAProduction is not thermal!");
		}
	}
	
	public String getName() {
		return this.energyAssetName;
	}
	
	@Override
    public void operate(double ratioOfCapacity) {
		ratioOfCapacity = profilePointer.getCurrentValue();
		
		if (ratioOfCapacity>0.0) { // Skip when there is no production -> saves time?
			double currentProduction_kW = ratioOfCapacity * this.capacity_kW;
			
	    	this.energyUse_kW = -currentProduction_kW;
	    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h; 	    	    	
	       	this.flowsMap.put(this.energyCarrier, -currentProduction_kW);
	    	
		}
	}
	
	@Override
	public void f_updateAllFlows(double powerFraction_fr) {
		double ratioOfCapacity = profilePointer.getCurrentValue();
		
		if (ratioOfCapacity>0.0) { // Skip when there is no production -> saves time?
			double currentProduction_kW = ratioOfCapacity * this.capacity_kW;
			
	    	this.energyUse_kW = -currentProduction_kW;
	    	this.energyUsed_kWh += this.energyUse_kW * this.timestep_h; 	    	    	
	       	this.flowsMap.put(this.energyCarrier, -currentProduction_kW);
	       	if (parentAgent instanceof GridConnection) {    		
	    		//((GridConnection)parentAgent).f_addFlows(arr, this);
	    		((GridConnection)parentAgent).f_addFlows(flowsMap, this.energyUse_kW, this);
	    	}

		}
		this.lastFlowsMap.cloneMap(this.flowsMap);
    	this.lastEnergyUse_kW = this.energyUse_kW;
    	this.clear();
    }

    //public Pair<J_FlowsMap, Double> curtailElectricityProduction(double curtailmentSetpoint_kW) {
    public void curtailElectricityProduction(double curtailmentSetpoint_kW) {  // This variable is called curtailmentSetpoint, but maybe its better to call it curtailment amount? it represents the amount of production we need to curtail, not the amount we want to produce.
    	double currentProduction_kW = -this.lastFlowsMap.get(OL_EnergyCarriers.ELECTRICITY);
    	//traceln("currentProduction_kW: " + currentProduction_kW);
    	//traceln("curtailmentSetpoint_kW: " + curtailmentSetpoint_kW);
    	double curtailmentPower_kW = max(0,min(currentProduction_kW, curtailmentSetpoint_kW));
    	energyUsed_kWh += curtailmentPower_kW * timestep_h;
    	this.totalEnergyCurtailed_kWh += curtailmentPower_kW * timestep_h;
    	this.flowsMap.put(OL_EnergyCarriers.ELECTRICITY, -curtailmentPower_kW);
    	this.energyUse_kW = -curtailmentPower_kW;
    	this.lastFlowsMap.addFlow(OL_EnergyCarriers.ELECTRICITY, curtailmentPower_kW);
    	this.lastEnergyUse_kW += curtailmentPower_kW;
    	
    	//traceln("Electricity production of asset %s curtailed by %s kW!", this, curtailmentPower_kW);
    	if (parentAgent instanceof GridConnection) {    		
    		((GridConnection)parentAgent).f_removeFlows(this.flowsMap, this.energyUse_kW, this);
    	}
    	clear();
    	
    }
    
    public double getEnergyCurtailed_kWh() {
    	return this.totalEnergyCurtailed_kWh;
    }
    
    @Override
    public void storeStatesAndReset() {
    	this.totalEnergyCurtailed_kWh = 0;
    	super.storeStatesAndReset();
	}
	
	@Override
	public String toString() {
		return
			"type = " + this.getClass().toString() + " " +
			"parentAgent = " + parentAgent +" " +
			"capacity_kW = " + capacity_kW +" "+
			"energyCarrier = " + energyCarrier +" "+
			"energyProduced_kWh = " + (-this.energyUsed_kWh) +  " ";
	}

	public String getOwnerAgent() {
		return parentAgent.agentInfo();
	}

	/*public double getCurrentTemperature() {
		return outputTemperature_degC;
	}*/
	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;
}

