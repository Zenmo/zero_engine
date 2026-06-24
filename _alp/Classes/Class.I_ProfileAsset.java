public interface I_ProfileAsset
{
	//Setters
    public void setProfileScaling_fr( double scaling_fr );
    

	public void setEnergyAssetName(String name);
	 
	public void setEnergyAssetType(OL_EnergyAssetType assetType);
	 
	public void setAssetFlowCategory(OL_AssetFlowCategories assetFlowCat);
	 
    //Getters
	public I_AssetOwner getOwner();
    public J_ProfilePointer getProfilePointer();
    
    public double getProfileUnitScaler_fr();
    public double getProfileScaling_fr();

    public double getPeakConsumptionPower_kW();
    public double getPeakProductionPower_kW();
    public double getBaseConsumption_kWh();
    public double getTotalConsumption_kWh();
    
    public J_FlowsMap getLastFlows();
	public OL_EnergyAssetType getEAType();
	public OL_AssetFlowCategories getAssetFlowCategory();
	
    public OL_EnergyCarriers getEnergyCarrier();
    
    public double[] getForecast_kW(double forecastStartTime_h, double forecastEndTime_h);
}