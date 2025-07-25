/**
 * J_ChargingSession
 */	
public class J_ChargingSession implements Serializable {

	int startTime;
	int endTime;
	double chargingDemand_kWh;
	double batterySize_kWh;
	double stateOfCharge_kWh;
	double chargingPower_kW;
	int socket;
	double timeStep_hr;
	
	boolean V1GCapable;
	boolean V2GCapable;
	int availableStepsForV2G;
	int availableStepsForV1G;
	int timeStepsToDisconnect;
	int openTimeSlots;
	int V2GRemainingTimesteps = 0;
	int V1GRemainingTimesteps = 0;
	double currentPower;
	
	double shiftedLoadV1GThisTimestep;
	double shiftedLoadV2GThisTimestep;

	
    /**
     * Default constructor
     */
    public J_ChargingSession(int startTime_quaterhours, int endTime_quaterhours, double chargingDemand_kWh, double batterySize_kWh, double chargingPower_kW, int socket, double timeStep_hr) {
    
    	this.startTime = startTime_quaterhours;
    	this.endTime = endTime_quaterhours;
    	this.chargingDemand_kWh = chargingDemand_kWh;
    	this.batterySize_kWh = batterySize_kWh;
    	stateOfCharge_kWh = batterySize_kWh - chargingDemand_kWh;
    	this.chargingPower_kW = chargingPower_kW; 
    	this.socket = socket;
    	timeStepsToDisconnect = endTime - startTime;
    	openTimeSlots = timeStepsToDisconnect - ((int)Math.ceil(4 * chargingDemand_kWh / chargingPower_kW)) ; 
    	this.timeStep_hr = timeStep_hr;
    	
    	if(this.startTime > this.endTime){
    		new RuntimeException("StartTime is later then the endtime for J_ChargingSession");
    	}
    }
    
    public double operate(boolean doV1G, boolean doV2G) {
    	this.V1GCapable = doV1G;
    	this.V2GCapable = doV2G;
    	timeStepsToDisconnect --;
    	openTimeSlots --;
    	double power = determineChargingPower();
    	setSmartChargingAvailabilities();
    	currentPower = power;
    	stateOfCharge_kWh += power * timeStep_hr;
    	return currentPower;
    }
    
    public double determineChargingPower(){
    	double power;
    	if (V2GRemainingTimesteps > 0) {
    		power = - chargingPower_kW;
    		shiftedLoadV2GThisTimestep = 2 * power;
    		V2GRemainingTimesteps--;
    		//if you are doing V2G this timestep you reduce your opentimeslots by 2
    		openTimeSlots --;
    		openTimeSlots --;
    	}
    	else if( V1GRemainingTimesteps > 0){
    		power = 0;
    		shiftedLoadV1GThisTimestep = power;
    		V1GRemainingTimesteps--;
    		//if you are doing V1G this timestep you reduce your opentimeslots by 1
    		openTimeSlots --;
    	}
    	else if (batterySize_kWh > stateOfCharge_kWh) {
    		power = chargingPower_kW;
    		shiftedLoadV1GThisTimestep = 0;
    		shiftedLoadV2GThisTimestep = 0;
    	}
    	else {
    		power = 0;
    		shiftedLoadV1GThisTimestep = 0;
    		shiftedLoadV2GThisTimestep =0;
    	}
    	return power;
    }
    
    public void setSmartChargingAvailabilities() {
    	//determine if the vehicle if this session is available for V2G and V1G next timestep
    	if( openTimeSlots > 3 && V2GCapable && (stateOfCharge_kWh > (0.2 * batterySize_kWh + 0.5 * chargingPower_kW )) ) { //you need at least 4 timesteps for V2G (a minimum for 2 timestep V2G sessions were defined)
    		availableStepsForV2G = (int) Math.floor(openTimeSlots / 2);
    	}
    	else {
    		availableStepsForV2G = 0;
    	}
    	
    	if (openTimeSlots > 1 && (stateOfCharge_kWh < batterySize_kWh)) {
    		availableStepsForV1G = openTimeSlots;
    	}
    	else {
    		availableStepsForV1G = 0;
    	}
    }
    
    public void requestV2G(int timeStepsV2G) {
    	if( V2GRemainingTimesteps != 0 || V1GRemainingTimesteps != 0) {
    		traceln( "ERROR TRYING TO SET CHARGING SESSION TO V2G, BUT IS ALREADY IN A V1G or V2G SESSION");
    	}
    	V2GRemainingTimesteps = timeStepsV2G;
    	
    }
    
    public void requestV1G(int timeStepsV1G) {
    	if( V2GRemainingTimesteps != 0 || V1GRemainingTimesteps != 0) {
    		traceln( "ERROR TRYING TO SET CHARGING SESSION TO V1G, BUT IS ALREADY IN A V1G or V2G SESSION");
    	}
    	V1GRemainingTimesteps = timeStepsV1G;
    }
    
    public double getShiftedLoadV1GCurrentTimestep() {
    	return shiftedLoadV1GThisTimestep;
    }
    
    public double getShiftedLoadV2GCurrentTimestep() {
    	return shiftedLoadV2GThisTimestep;
    }

    
    public J_ChargingSession getClone() {
    	return new J_ChargingSession(this.startTime, this.endTime, this.chargingDemand_kWh, this.batterySize_kWh, this.chargingPower_kW, this.socket, this.timeStep_hr);
    }
    
    
	@Override
	public String toString() {
		return "Power: " + currentPower + "kW, start: " + startTime + ", end: " + endTime + ", Pmax: " + chargingPower_kW +"kW, demand: " + chargingDemand_kWh + "kWh";
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}