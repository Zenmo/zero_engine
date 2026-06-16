/**
 * J_EAConversionHob
 */	
public class J_EAConversionHob extends J_EAConversion{

	//protected double outputTemperature_degC;

    /**
     * Empty constructor for serialization
     */
	public J_EAConversionHob() {
	}
	
    /**
     * Default constructor
     */
    public J_EAConversionHob(I_AssetOwner owner, OL_EnergyCarriers energyCarrierConsumed, double inputCapacity_kW, double efficiency, J_TimeParameters timeParameters, double outputTemperature_degC) {
    	this.setOwner(owner);
	    this.timeParameters = timeParameters;
	    this.inputCapacity_kW = inputCapacity_kW;
	    this.eta_r = efficiency; // The efficiency is the amount of heat that is retained within the building
	    this.outputCapacity_kW = inputCapacity_kW * efficiency;
	    //this.outputTemperature_degC = outputTemperature_degC;
		this.energyCarrierProduced = OL_EnergyCarriers.HEAT;
		this.energyCarrierConsumed = energyCarrierConsumed;

	    if(energyCarrierConsumed == OL_EnergyCarriers.ELECTRICITY) {
		    this.assetFlowCategory = OL_AssetFlowCategories.electricHobConsumption_kW;
		    this.energyAssetType = OL_EnergyAssetType.ELECTRIC_HOB;
	    }
	    else if (energyCarrierConsumed == OL_EnergyCarriers.METHANE){
		    this.energyAssetType = OL_EnergyAssetType.GAS_HOB;
	    }
	    else {
	    	throw new RuntimeException("EnergyCarrierConsumed (" + energyCarrierConsumed + ") found for J_EAConversionHob that is not supported!");
	    }
	    
	    this.activeProductionEnergyCarriers.add(this.energyCarrierProduced);		
		this.activeConsumptionEnergyCarriers.add(this.energyCarrierConsumed);
		registerEnergyAsset(timeParameters);
    }

    @Override
    public void operate( double powerFraction_fr, J_TimeVariables timeVariables ) {
    	double heatProduction_kW = this.inputCapacity_kW * powerFraction_fr * eta_r;
		double energyInput_kW = this.inputCapacity_kW * powerFraction_fr;
		this.energyUse_kW = (energyInput_kW - heatProduction_kW);
		this.energyUsed_kWh += this.timeParameters.getTimeStep_h() * (energyInput_kW - heatProduction_kW); // This represents losses!
		flowsMap.put(this.energyCarrierConsumed, energyInput_kW);		
		flowsMap.put(OL_EnergyCarriers.HEAT, -heatProduction_kW);	
		assetFlowsMap.put(this.assetFlowCategory, energyInput_kW);
    }
    
	@Override
	public String toString() {
		return
			"J_EAConversionHob: \n" +
			"capacityElectric_kW = " + this.inputCapacity_kW +", \n"+
			"eta_r = " + this.eta_r+ ", \n" +
			"energyUsed_kWh (losses) = " + this.energyUsed_kWh;
	}
}