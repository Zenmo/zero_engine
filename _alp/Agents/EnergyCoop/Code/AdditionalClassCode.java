public OL_ResultScope getScope(){return OL_ResultScope.ENERGYCOOP;}
public J_RapidRunData getRapidRunData(){return v_rapidRunData;}
public J_LiveData getLiveData(){return v_liveData;}
public J_RapidRunData getPreviousRapidRunData(){return v_previousRunData;}

@Override
public void onCreate() {
    super.onCreate();
    
    v_liveData = new J_LiveData();
    v_liveData.activeEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
    v_liveData.activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
    v_liveData.activeConsumptionEnergyCarriers= EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
    v_liveData.connectionMetaData = v_liveConnectionMetaData;
    v_liveData.assetsMetaData = v_liveAssetsMetaData;
    energyModel.c_actors.add(this);
}
