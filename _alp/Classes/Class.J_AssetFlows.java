/**
 * J_AssetFlows
 */	
public class J_AssetFlows implements Serializable {
	
	// Further Subdivision of asset types within energy carriers
	public double fixedConsumptionElectric_kW;
	public double heatPumpElectricityConsumption_kW;
	public double evChargingPowerElectric_kW;
	public double currentBatteriesConsumption_kW;
	public double hydrogenElectricityConsumption_kW;
	public double electricHobConsumption_kW;
	public double districtHeatDelivery_kW;
	public double pvProductionElectric_kW;
	public double windProductionElectric_kW;
	public double ptProductionHeat_kW;
	public double CHPProductionElectric_kW;
	public double currentBatteriesProduction_kW;
	public double currentV2GProduction_kW;
	public double currentStoredEnergyBatteries_MWh;
	
    /**
     * Default constructor
     */
    public J_AssetFlows() {
    }
    
    public void setFlows(
    		double fixedConsumptionElectric_kW,
    		double heatPumpElectricityConsumption_kW,
    		double evChargingPowerElectric_kW,
    		double currentBatteriesConsumption_kW,
    		double hydrogenElectricityConsumption_kW,
    		double electricHobConsumption_kW,
    		double districtHeatDelivery_kW,
    		double pvProductionElectric_kW,
    		double windProductionElectric_kW,
    		double ptProductionHeat_kW,
    		double CHPProductionElectric_kW,
    		double currentBatteriesProduction_kW,
    		double currentV2GProduction_kW,
    		double currentStoredEnergyBatteries_MWh) {    	
    	this.fixedConsumptionElectric_kW = fixedConsumptionElectric_kW;
    	this.heatPumpElectricityConsumption_kW = heatPumpElectricityConsumption_kW;
    	this.evChargingPowerElectric_kW = evChargingPowerElectric_kW;
    	this.currentBatteriesConsumption_kW = currentBatteriesConsumption_kW;
    	this.hydrogenElectricityConsumption_kW = hydrogenElectricityConsumption_kW;
    	this.electricHobConsumption_kW = electricHobConsumption_kW;
    	this.districtHeatDelivery_kW = districtHeatDelivery_kW;
    	this.pvProductionElectric_kW = pvProductionElectric_kW;
    	this.windProductionElectric_kW = windProductionElectric_kW;
    	this.ptProductionHeat_kW = ptProductionHeat_kW;
    	this.CHPProductionElectric_kW = CHPProductionElectric_kW;
    	this.currentBatteriesProduction_kW = currentBatteriesProduction_kW;
    	this.currentV2GProduction_kW = currentV2GProduction_kW;
    	this.currentStoredEnergyBatteries_MWh = currentStoredEnergyBatteries_MWh;
    }

    public void addFlows(J_AssetFlows assetFlows){    	
    	this.fixedConsumptionElectric_kW += fixedConsumptionElectric_kW;
    	this.heatPumpElectricityConsumption_kW += heatPumpElectricityConsumption_kW;
    	this.evChargingPowerElectric_kW += evChargingPowerElectric_kW;
    	this.currentBatteriesConsumption_kW += currentBatteriesConsumption_kW;
    	this.hydrogenElectricityConsumption_kW += hydrogenElectricityConsumption_kW;
    	this.electricHobConsumption_kW += electricHobConsumption_kW;
    	this.districtHeatDelivery_kW += districtHeatDelivery_kW;
    	this.pvProductionElectric_kW += pvProductionElectric_kW;
    	this.windProductionElectric_kW += windProductionElectric_kW;
    	this.ptProductionHeat_kW += ptProductionHeat_kW;
    	this.CHPProductionElectric_kW += CHPProductionElectric_kW;
    	this.currentBatteriesProduction_kW += currentBatteriesProduction_kW;
    	this.currentV2GProduction_kW += currentV2GProduction_kW;
    	this.currentStoredEnergyBatteries_MWh += currentStoredEnergyBatteries_MWh;
    }    
    
    
    public void reset() {
    	fixedConsumptionElectric_kW = 0;
    	heatPumpElectricityConsumption_kW = 0;
    	evChargingPowerElectric_kW = 0;
    	currentBatteriesConsumption_kW = 0;
    	hydrogenElectricityConsumption_kW = 0;
    	electricHobConsumption_kW = 0;
    	districtHeatDelivery_kW = 0;
    	pvProductionElectric_kW = 0;
    	windProductionElectric_kW = 0;
    	ptProductionHeat_kW = 0;
    	CHPProductionElectric_kW = 0;
    	currentBatteriesProduction_kW = 0;
    	currentV2GProduction_kW = 0;
    	currentStoredEnergyBatteries_MWh = 0;    	
    }
    
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