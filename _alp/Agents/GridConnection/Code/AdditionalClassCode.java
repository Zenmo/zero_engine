public OL_ResultScope getScope(){return OL_ResultScope.GRIDCONNECTION;}
public J_RapidRunData getRapidRunData(){return v_rapidRunData;}
public J_LiveData getLiveData(){return v_liveData;}
public J_RapidRunData getPreviousRapidRunData(){return v_previousRunData;}
public J_ActiveAssetData getActiveAssetData(){return v_activeAssetData;}
public boolean getDeliveryCapacityKnown(){return b_isRealDeliveryCapacityAvailable;}
public boolean getFeedinCapacityKnown(){return b_isRealFeedinCapacityAvailable;}