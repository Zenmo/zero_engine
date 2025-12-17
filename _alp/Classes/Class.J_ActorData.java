/**
 * J_ActorData
 */
public class J_ActorData implements Serializable {

	public String actorID;
	public String group;
	public String gridOperator;
	public String parentCoop;
	public String energySupplier;
	public String ownedGridConnection;
	public String energySupplierDistrictHeat;
	public String electricityVolume_kWh;
	public String heatVolume_kWh;
	public String methaneVolume_kWh;
	public String hydrogenVolume_kWh;
	public String petroleumFuelVolume_kWh;
	//public String electricityContractType;
	//public String heatContractType;
	//public String methaneContractType;
	//public String hydrogenContractType;
	public String balanceElectricity_eur;
	public String balanceElectricityDelivery_eur;
	public String balanceElectricityTransport_eur;
	public String balanceElectricityTax_eur;
	public String deliveryContractScope;
	public String transportContractScope;
	public String taxContractScope;
	public boolean b_methaneUsedWithoutContracts;
	public boolean b_hydrogenUsedWithoutContracts;
	 /**
     * Default constructor
     */
    public J_ActorData() {
    }

    /**
     * Constructor initializing the fields
     */
    public J_ActorData(String actorID, String group) {
		this.actorID = actorID;
		this.group = group;
		this.gridOperator = "";
		this.parentCoop = "";
		this.energySupplier = "";
		this.ownedGridConnection = "";
		this.energySupplierDistrictHeat = "";
		this.electricityVolume_kWh = "";
		this.heatVolume_kWh = "";
		this.methaneVolume_kWh = "";
		this.hydrogenVolume_kWh = "";
		this.petroleumFuelVolume_kWh = "";
		//this.electricityContractType = "";
		//this.heatContractType = "";
		//this.methaneContractType = "";
		//this.hydrogenContractType = "";
		this.balanceElectricity_eur = "";
		this.balanceElectricityDelivery_eur = "";
		this.balanceElectricityTransport_eur = "";
		this.balanceElectricityTax_eur = "";
		this.deliveryContractScope = "";
		this.transportContractScope = "";
		this.taxContractScope = "";
		this.b_methaneUsedWithoutContracts = false;
		this.b_hydrogenUsedWithoutContracts = false;
		
    }

	@Override
	public String toString() {
		return
			"actorID = " + actorID +" " +
			"group = " + group +" " +
			"gridOperator = " + gridOperator +" " +
			"parentCoop = " + parentCoop +" " +
			"energySupplier = " + energySupplier +" " +
			"ownedGridConnection = " + ownedGridConnection +" " +
			"energySupplierDistrictHeat = " + energySupplierDistrictHeat +" " +
			"electricityVolume_kWh = " + electricityVolume_kWh +" " +
			"heatVolume_kWh = " + heatVolume_kWh +" " +
			"methaneVolume_kWh = " + methaneVolume_kWh +" " +
			"hydrogenVolume_kWh = " + hydrogenVolume_kWh +" " +
			"petroleumFuelVolume_kWh = " + petroleumFuelVolume_kWh + " " +
			//"electricityContractType = " + electricityContractType +" " +
			//"heatContractType = " + heatContractType +" " +
			//"methaneContractType = " + methaneContractType +" " +
			//"hydrogenContractType = " + hydrogenContractType +" " +
			"balanceElectricity_eur = " + balanceElectricity_eur +" " +
			"balanceElectricityDelivery_eur = " + balanceElectricityDelivery_eur +" " +
			"balanceElectricityTransport_eur = " + balanceElectricityTransport_eur +" " +
			"balanceElectricityTax_eur = " + balanceElectricityTax_eur +" " +
			"deliveryContractScope = " + deliveryContractScope +" "+
			"transportContractScope = " + transportContractScope +" "+
			"taxContractScope = " + taxContractScope + " ";
	}

	public void updateData(String actorID, String group, String gridOperator, String parentCoop, String energySupplier, String ownedGridConnection, String energySupplierDistrictHeat,double electricityVolume_kWh, double heatVolume_kWh, double methaneVolume_kWh, double hydrogenVolume_kWh, double petroleumFuelVolume_kWh, /*String electricityContractType, String heatContractType, String methaneContractType, String hydrogenContractType,*/ double balanceElectricity_eur, double balanceElectricityDelivery_eur, double balanceElectricityTransport_eur, double balanceElectricityTax_eur, String deliveryContractScope, String transportContractScope, String taxContractScope, boolean b_methaneUsedWithoutContracts, boolean b_hydrogenUsedWithoutContracts) {
		this.actorID = actorID;
		this.group = group;
		this.gridOperator = gridOperator;
		this.parentCoop = parentCoop;
		this.energySupplier = energySupplier;
		this.ownedGridConnection = ownedGridConnection;
		this.energySupplierDistrictHeat = energySupplierDistrictHeat +"";
		this.electricityVolume_kWh = electricityVolume_kWh + "";
		this.heatVolume_kWh = heatVolume_kWh  + "";
		this.methaneVolume_kWh = methaneVolume_kWh + "";
		this.hydrogenVolume_kWh = hydrogenVolume_kWh + "";
		this.petroleumFuelVolume_kWh = petroleumFuelVolume_kWh + "";
		//this.electricityContractType = electricityContractType + "";
		//this.heatContractType = heatContractType + "";
		//this.methaneContractType = methaneContractType + "";
		//this.hydrogenContractType = hydrogenContractType + "";
		this.balanceElectricity_eur = balanceElectricity_eur + "";
		this.balanceElectricityDelivery_eur = balanceElectricityDelivery_eur + "";
		this.balanceElectricityTransport_eur = balanceElectricityTransport_eur + "";
		this.balanceElectricityTax_eur = balanceElectricityTax_eur + "";
		this.deliveryContractScope = deliveryContractScope + "";
		this.transportContractScope = transportContractScope + "";
		this.taxContractScope = taxContractScope + "";	
		this.b_methaneUsedWithoutContracts = b_methaneUsedWithoutContracts;
		this.b_hydrogenUsedWithoutContracts = b_hydrogenUsedWithoutContracts;
	}
	/*
	public ArrayList returnData() {
		ArrayList data = new ArrayList({actorID, actorType, parentActorID, energySupplier, ownedGridConnection.toString(), energySupplierDistrictHeat.toString(), electricityVolume_kWh, heatVolume_kWh, methaneVolume_kWh, hydrogenVolume_kWh, balanceElectricity_eur, balanceHeat_eur, balanceMethane_eur, balanceHydrogen_eur});
		return data;

	}*/
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */
	private static final long serialVersionUID = 1L;

}