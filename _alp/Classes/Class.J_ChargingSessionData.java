/**
 * J_ChargingSessionData
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // 
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_ChargingSessionData {
	private double startTime_h;
	private double endTime_h;
	private double timeStep_h;
	private double chargingDemand_kWh;
	private double batterySize_kWh;
	private double initialSOC_kWh;
	private double vehicleMaxChargingPower_kW;
	private int socketNb;
	
	private boolean V2GCapable = true;

	
	private boolean V2GCapabilityIsOverriden = false;
	private boolean V2GCapableOverride = true;
	
    /**
     * Constructor for (de-)serialisation
     */
	public J_ChargingSessionData() {
	}
	
    /**
     * Default constructor initializing the fields
     */
    public J_ChargingSessionData(double startTime_quarterhours, double endTime_quarterhours, double chargingDemand_kWh, double batterySize_kWh, double chargingPower_kW, int socketNb, boolean V2GCapable, double timeStep_h) {
    
    	this.startTime_h = 0.25 * startTime_quarterhours;
    	this.endTime_h = 0.25 * endTime_quarterhours;
    	this.chargingDemand_kWh = chargingDemand_kWh;
    	this.batterySize_kWh = batterySize_kWh;
    	this.vehicleMaxChargingPower_kW = chargingPower_kW; 
    	this.initialSOC_kWh = 0; //Required to be 0, to prevent jumps in energy in the model
    	this.socketNb = socketNb-1;
    	this.V2GCapable = V2GCapable;
    	this.timeStep_h = timeStep_h;
    	if(this.startTime_h > this.endTime_h){
    		new RuntimeException("StartTime is later then the endtime for J_ChargingSession");
    	}
    	
    	//Override to get continious energy balance!
    	if(true) {
	    	this.initialSOC_kWh = 0; //Required to be 0, to prevent jumps in energy in the model
	    	this.batterySize_kWh = chargingDemand_kWh; //-> Equal to charging demand, as initial SOC starts
    	}
    }
    
    public double getStartTime_h() {
    	return this.startTime_h;
    }
    
	public double getLeaveTime_h() {
		return this.endTime_h;
	}

	public double getChargingCapacity_kW() {
		return this.vehicleMaxChargingPower_kW;
	}
	
	public double getStorageCapacity_kWh() {
		return this.batterySize_kWh;
	}
	
	public double getInitialSOC_kWh() {
		return this.initialSOC_kWh;
	}

    public double getEnergyNeededForNextTrip_kWh() {
    	return this.chargingDemand_kWh + this.initialSOC_kWh;
    }
    
    public J_ChargingSessionData getClone() {
    	return new J_ChargingSessionData((this.startTime_h*4), (this.endTime_h*4), this.chargingDemand_kWh, this.batterySize_kWh, this.vehicleMaxChargingPower_kW, this.socketNb, this.V2GCapable, this.timeStep_h);
    }
    
	public double getVehicleScaling_fr() {
		return 1.0;
	}
	
	public boolean getV2GCapable() {
		if(this.V2GCapabilityIsOverriden) {
			return this.V2GCapableOverride;
		}
		else {
			return this.V2GCapable;
		}
	}
    
	public int getSocketNb() {
	    return this.socketNb;
	}
	
	public boolean getAvailability(double t_h) {
		return this.startTime_h < t_h && t_h < this.endTime_h;
	}

	
	public void overrideV2GCapability(boolean V2GCapabilityIsOverriden, boolean V2GCapableOverride) {
		this.V2GCapabilityIsOverriden = V2GCapabilityIsOverriden;
		if(this.V2GCapabilityIsOverriden) {
			this.V2GCapableOverride = V2GCapableOverride;
		}
	}
   
	@Override
	public String toString() {
		return "StartTime_h: " + startTime_h + ", endTime_h: " + endTime_h + ", Pmax: " + vehicleMaxChargingPower_kW + "kW, demand: " + chargingDemand_kWh + "kWh";
	}
}