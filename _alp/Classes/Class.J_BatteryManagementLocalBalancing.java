/**
 * J_BatteryManagementLocalBalancing
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_BatteryManagementLocalBalancing implements I_BatteryManagement {

	private GridConnection gc;
    private J_TimeParameters timeParameters;
	private Agent target = gc;
	private OL_ResultScope targetType = OL_ResultScope.GRIDCONNECTION;
	
	// Parameters used:
	private double filterTimeScale_h = 5*24;
    private double filterDiffGain_r;
    private double initialValueGCdemandLowPassed_kW = 0.5;
    private double GCdemandLowPassed_kW = this.initialValueGCdemandLowPassed_kW;
    private double storedGCdemandLowPassed_kW;
    
	private double SOC_setpoint_fr = 0.5; // If there are no other influences such as vehicles or production the battery will aim for this SOC_fr
	//private double feedbackGain_fr = 1.5; // This parameter determines how strongly to aim for the SOC setpoint
	private double feedbackGain_kWpSOC;
	private double balancingGain_fr = 0.25; // How much 'peakshaving' around the average load? 1.0 is 100% peakshaving, which would result in an approximately flat profile, if the battery is big enough.
    /**
     * Default constructor
     */
	public J_BatteryManagementLocalBalancing() {
		
	}
	
    public J_BatteryManagementLocalBalancing( GridConnection gc, J_TimeParameters timeParameters ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.filterDiffGain_r = 1/(filterTimeScale_h/timeParameters.getTimeStep_h());
    	double feedbackGain_ph = 1/5;
    	this.feedbackGain_kWpSOC = gc.p_batteryAsset.getStorageCapacity_kWh() * feedbackGain_ph;
    	if (gc instanceof GCGridBattery) {
    		target = null;
    		this.targetType = null;    		
    	} else {
    		target = gc;
    		this.targetType = OL_ResultScope.GRIDCONNECTION;
    	}
    }
    
    public J_BatteryManagementLocalBalancing( GridConnection gc, double SOC_setpoint_fr, double feedbackGain_ph, J_TimeParameters timeParameters ) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.filterDiffGain_r = 1/(filterTimeScale_h/timeParameters.getTimeStep_h());
    	this.SOC_setpoint_fr = SOC_setpoint_fr;
    	//this.SoCrestoreTime_h = 1/feedbackGain_ph;
    	this.feedbackGain_kWpSOC = gc.p_batteryAsset.getStorageCapacity_kWh() * feedbackGain_ph;
    	if (gc instanceof GCGridBattery) {
    		target = null;
    		this.targetType = null;    		
    	} else {
    		target = gc;
    		this.targetType = OL_ResultScope.GRIDCONNECTION;
    	}
    }
    
    /**
     * This algorithm tries to aim for a fixed SOC (0.5 by default) 
     * so that it can take the connection capacity of the GC into account and prevent any peaks when they occur.
     */
    public void manageBattery(J_TimeVariables timeVariables) {
    	
    	// Use current GC-load (so without EV charging!) as an 'equivalent price' signal, and use EV battery flexibility to make local load flatter.
    	double currentBalanceBeforeBattery_kW = gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    	GCdemandLowPassed_kW += (currentBalanceBeforeBattery_kW - GCdemandLowPassed_kW) * filterDiffGain_r;
    	//traceln("Battery management local balancing!");
    	if(gc.p_batteryAsset != null && gc.p_batteryAsset.getStorageCapacity_kWh() > 0) {
	    	if (this.target == null) {
	        	gc.f_updateFlexAssetFlows(gc.p_batteryAsset, 0.0, timeVariables);
	    		traceln("Battery inactive!");
	    		return;
	    	}
	    	
	    	double chargeSetpoint_kW = (SOC_setpoint_fr - gc.p_batteryAsset.getCurrentStateOfCharge_fr()) * feedbackGain_kWpSOC + balancingGain_fr * (GCdemandLowPassed_kW - currentBalanceBeforeBattery_kW);
	    	
	    	// Try to stay within the target connection capacity
	    	double v_allowedDeliveryCapacity_kW = getDeliveryCapacity_kW();
	    	double v_allowedFeedinCapacity_kW = getFeedinCapacity_kW();
	    	double balanceElectricity_kW = getBalanceElectricity_kW();
	    	double availableChargePower_kW = v_allowedDeliveryCapacity_kW - balanceElectricity_kW; // Max battery charging power within safety margins
	    	double availableDischargePower_kW = v_allowedFeedinCapacity_kW + balanceElectricity_kW; // Max discharging power within safety margins
	
	    	chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
	    	//traceln("Battery charge setpoint in gc: %s, %s kW", gc.p_gridConnectionID, chargeSetpoint_kW);
	    	gc.f_updateFlexAssetFlows(gc.p_batteryAsset, chargeSetpoint_kW / gc.p_batteryAsset.getCapacityElectric_kW(), timeVariables);
	    }
    }
  
    public void setTarget( Agent agent ) {
    	if ( agent == null) {
    		target = null;
    		this.targetType = null;
    	}
    	else if (agent == this.gc) {
    		target = agent;
    		this.targetType = OL_ResultScope.GRIDCONNECTION;
    	}
    	else if (agent instanceof GridNode) {
    		target = agent;
    		this.targetType = OL_ResultScope.GRIDNODE;
    	}
    	else if (agent instanceof EnergyCoop) {
    		target = agent;
    		this.targetType = OL_ResultScope.ENERGYCOOP;
    	}
    	else {
    		throw new RuntimeException("Not able to set " + agent + " as a target for J_BatteryPeakShaving");
    	}
    }
    
    public Agent getTarget() {
    	return this.target;
    }
    
    public void setTargetType( OL_ResultScope targetType ) {
    	this.targetType = targetType;
    }
    
    public OL_ResultScope getTargetType() {
    	return this.targetType;
    }
    
    // TODO: Make an interface with at least these 3 functions and make the agents implement it.
    private double getDeliveryCapacity_kW() {
	    	switch (targetType) {
			case GRIDCONNECTION:
	    		return gc.v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
			case GRIDNODE:
	    		return ((GridNode)target).p_capacity_kW;
			case ENERGYCOOP:
	    		return ((EnergyCoop)target).v_liveConnectionMetaData.contractedDeliveryCapacity_kW;
	    	default:
	    		throw new RuntimeException("Was not able to find the delivery capacity of the target of the battery in GridConnection: " + gc.p_gridConnectionID);
		}
    }
    
    private double getFeedinCapacity_kW() {
    	switch (targetType) {
			case GRIDCONNECTION:
	    		return gc.v_liveConnectionMetaData.contractedFeedinCapacity_kW;
			case GRIDNODE:
	    		return ((GridNode)target).p_capacity_kW;
			case ENERGYCOOP:
	    		return ((EnergyCoop)target).v_liveConnectionMetaData.contractedFeedinCapacity_kW;
	    	default:
	    		throw new RuntimeException("Was not able to find the feedin capacity of the target of the battery in GridConnection: " + gc.p_gridConnectionID);
		}
    }
    
    private double getBalanceElectricity_kW() {
    	switch (targetType) {
    		case GRIDCONNECTION:
        		return gc.fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY);
    		case GRIDNODE:
        		return ((GridNode)target).v_currentLoad_kW - gc.p_batteryAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
    		case ENERGYCOOP:
        		return ((EnergyCoop)target).fm_currentBalanceFlows_kW.get(OL_EnergyCarriers.ELECTRICITY) - gc.p_batteryAsset.getLastFlows().get(OL_EnergyCarriers.ELECTRICITY);
        	default:
        		throw new RuntimeException("Was not able to find the electricity balance of the target of the battery in GridConnection: " + gc.p_gridConnectionID);
    	}
    }
    
    
    
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.gc;
    }
    
    
    //Store and reset states
	public void storeStatesAndReset() {
		this.storedGCdemandLowPassed_kW = this.GCdemandLowPassed_kW;
		this.GCdemandLowPassed_kW = this.initialValueGCdemandLowPassed_kW;
	}
	public void restoreStates() {
		this.GCdemandLowPassed_kW = this.storedGCdemandLowPassed_kW;
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

