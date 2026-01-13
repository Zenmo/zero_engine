/**
 * J_BatteryManagementPeakShaving
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

public class J_BatteryManagementPeakShaving implements I_BatteryManagement {

	private GridConnection gc;
	private Agent target = gc;
	private OL_ResultScope targetType = OL_ResultScope.GRIDCONNECTION;
	
	// Parameters used:
	private double SOC_setpoint_fr = 0.5; // If there are no other influences such as vehicles or production the battery will aim for this SOC_fr
	private double feedbackGain_fr = 1.5; // This parameter determines how strongly to aim for the SOC setpoint
	
    /**
     * Default constructor
     */
	public J_BatteryManagementPeakShaving() {
		
	}
	
    public J_BatteryManagementPeakShaving( GridConnection gc ) {
    	this.gc = gc;
    	if (gc instanceof GCGridBattery) {
    		target = null;
    		this.targetType = null;    		
    	}
    }
    
    public J_BatteryManagementPeakShaving( GridConnection gc, double SOC_setpoint_fr, double feedbackGain_fr ) {
    	this.gc = gc;
    	this.SOC_setpoint_fr = SOC_setpoint_fr;
    	this.feedbackGain_fr = feedbackGain_fr;
    	if (gc instanceof GCGridBattery) {
    		target = null;
    		this.targetType = null;    		
    	}
    }
    
    /**
     * This algorithm tries to aim for a fixed SOC (0.5 by default) 
     * so that it can take the connection capacity of the GC into account and prevent any peaks when they occur.
     */
    public void manageBattery(J_TimeVariables timeVariables) {
    	if (this.target == null) {
    		gc.p_batteryAsset.f_updateAllFlows(0, timeVariables);
    		return;
    	}
    	double feedbackGain_kWpSOC = feedbackGain_fr * gc.p_batteryAsset.getCapacityElectric_kW();
    	double chargeSetpoint_kW = (SOC_setpoint_fr - gc.p_batteryAsset.getCurrentStateOfCharge_fr()) * feedbackGain_kWpSOC;
    	
    	// Try to stay within the target connection capacity
    	double v_allowedDeliveryCapacity_kW = getDeliveryCapacity_kW();
    	double v_allowedFeedinCapacity_kW = getFeedinCapacity_kW();
    	double balanceElectricity_kW = getBalanceElectricity_kW();
    	double availableChargePower_kW = v_allowedDeliveryCapacity_kW - balanceElectricity_kW; // Max battery charging power within safety margins
    	double availableDischargePower_kW = v_allowedFeedinCapacity_kW + balanceElectricity_kW; // Max discharging power within safety margins

    	chargeSetpoint_kW = min(max(chargeSetpoint_kW, -availableDischargePower_kW),availableChargePower_kW); // Don't allow too much (dis)charging!
    	
    	gc.p_batteryAsset.f_updateAllFlows( chargeSetpoint_kW / gc.p_batteryAsset.getCapacityElectric_kW(), timeVariables );
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
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
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