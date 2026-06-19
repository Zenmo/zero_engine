public final String p_uid = UUID.randomUUID().toString();

@Override
public String toString(){
	String shortToString = super.toString();
	shortToString = shortToString.replace("root.main[0].energyModel.", "");
	return shortToString;
}

public OL_ResultScope getScope(){return OL_ResultScope.GRIDCONNECTION;}
public J_RapidRunData getRapidRunData(){return v_rapidRunData;}
public J_LiveData getLiveData(){return v_liveData;}
public J_RapidRunData getPreviousRapidRunData(){return v_previousRunData;}

public static void forceSetOwner(Agent agent, AgentArrayList pop) throws Exception {
	Agent owner = pop.getOwner();
    Field f = Agent.class.getDeclaredField("d");
    f.setAccessible(true);
    f.set(agent, owner);
    Field ff = Agent.class.getDeclaredField("j");
    ff.setAccessible(true);
    ff.set(agent, pop);
    /*Field c = Agent.class.getDeclaredField("c");
    traceln("Field c: %s", c);
    c.setAccessible(true);
    c.toString();*/
}

@Override
public void onCreate() {
    super.onCreate();
    
    energyModel.c_gridConnections.add(this);

    v_liveData = new J_LiveData();
    v_liveData.activeEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
    v_liveData.activeProductionEnergyCarriers = EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
    v_liveData.activeConsumptionEnergyCarriers= EnumSet.of(OL_EnergyCarriers.ELECTRICITY);
    v_liveData.connectionMetaData = v_liveConnectionMetaData;
    v_liveData.assetsMetaData = v_liveAssetsMetaData;
}
