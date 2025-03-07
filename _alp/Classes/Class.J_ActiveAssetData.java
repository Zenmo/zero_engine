/**
 * J_ActiveAssetsData
 */	
public class J_ActiveAssetData {
	
	public Agent parentAgent;
	
	//public EnumSet<OL_EnergyCarriers> activeConsumptionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	//public EnumSet<OL_EnergyCarriers> activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
	
	public boolean hasElectricHeating = false;
	public boolean hasElectricTransport = false;
	public boolean hasPV = false;
	public boolean hasWindturbine = false;
	public boolean hasBattery = false;
	public boolean hasHeatGridConnection = false;
	public boolean hasElectrolyser = false;
	public boolean hasCHP = false;
	public boolean hasV2G = false;
	public boolean hasElectricCooking = false;
	
    /**
     * Default constructor
     */
    public J_ActiveAssetData(Agent parentAgent) {
    	this.parentAgent = parentAgent;
    }

	@Override
	public String toString() {
		return super.toString();
	}

}