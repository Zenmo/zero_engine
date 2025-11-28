import zeroPackage.ZeroMath;
/**
 * J_AggregatorEnergyManagementHPAndVehiclesInterval
 */	

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)

public class J_AggregatorEnergyManagementHPAndVehiclesInterval implements I_AggregatorEnergyManagement {
	private EnergyCoop energyCoop;
	private GridNode targetNode;
	private J_EAProfile gridNodeProfile;
	
    private double timeStep_h; //Time step length in hr
    private double runStartTime_h;// Start time of simulation in hr since start of year
    
    private double loadShiftingMaxDuration_hr = 24;// Duration of the load shifting interval
	
	public J_AggregatorEnergyManagementHPAndVehiclesInterval() {
		
	}
	

	public J_AggregatorEnergyManagementHPAndVehiclesInterval(EnergyCoop energyCoop, GridNode target) {
    	this.energyCoop = energyCoop;
    	this.targetNode = target;
		this.gridNodeProfile = findFirst(this.targetNode.f_getConnectedGridConnections(),gc -> gc.p_gridConnectionID.equals("GridNode " + this.targetNode.p_gridNodeID + " profile GC")).c_profileAssets.get(0);
    	this.timeStep_h = energyCoop.energyModel.p_timeStep_h;
    	this.runStartTime_h = energyCoop.energyModel.p_runStartTime_h;
    	
	}	
    	
    	
	public void manageExternalSetpoints() {
		if(energyCoop.energyModel.t_h % 24 == 0) { //Timestep delay
	    	List<GridConnection> memberedGCWithSetpointEVManagement = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.c_electricVehicles.size() > 0 && GC.f_getChargingManagement() != null && GC.f_getChargingManagement() instanceof J_ChargingManagementOffPeak);
	    	List<GridConnection> memberedGCWithSetpointEVManagementCharger = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.c_chargers.size() > 0 && GC.f_getChargingManagement() != null && GC.f_getChargingManagement() instanceof J_ChargingManagementOffPeak);
	    	List<GridConnection> memberedGCWithSetpointHeatpumps = findAll(energyCoop.f_getMemberGridConnectionsCollectionPointer(), GC -> GC.f_getCurrentHeatingType() == OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP && GC.f_getHeatingManagement() != null && GC.f_getHeatingManagement() instanceof J_HeatingManagementHeatpumpOffPeak);
	    	
	    	Pair<Double, Double> reducedConsumptionIntervalTime_hr = getProfileShavingInterval_kW();
	    	Double startTimeReducedConsumptionInterval_hr = reducedConsumptionIntervalTime_hr.getFirst();
	    	Double endTimeReducedConsumptionInterval_hr = reducedConsumptionIntervalTime_hr.getSecond();
	    	
	    	
	    	for(GridConnection EVGC : memberedGCWithSetpointEVManagement) {
	    		((J_ChargingManagementOffPeak)EVGC.f_getChargingManagement()).setReducedChargingIntervalTime_hr(startTimeReducedConsumptionInterval_hr, endTimeReducedConsumptionInterval_hr );
	    	}
	    	for(GridConnection ChargerGC : memberedGCWithSetpointEVManagementCharger) {
	    		//Reduced charging Interval is not yet functional for chargers! 
	    		ChargerGC.c_chargers.forEach(charger -> charger.setReducedChargingIntervalTime_hr(startTimeReducedConsumptionInterval_hr, endTimeReducedConsumptionInterval_hr ));
	    	}
	    	for(GridConnection HPGC : memberedGCWithSetpointHeatpumps) {
	    		((J_HeatingManagementHeatpumpOffPeak)HPGC.f_getHeatingManagement()).setReducedHeatingIntervalTime_hr(startTimeReducedConsumptionInterval_hr, endTimeReducedConsumptionInterval_hr );
	    	}
		}
	}	
    
    private Pair<Double, Double> getProfileShavingInterval_kW() {
		double hour_of_simulation_year = energyCoop.energyModel.t_h - this.runStartTime_h;

		int startTimeIntervalIndex = roundToInt(hour_of_simulation_year/this.timeStep_h);
		int endTimeIntervalIndex = roundToInt((hour_of_simulation_year + this.loadShiftingMaxDuration_hr)/this.timeStep_h);
		
		double[] profile_kW;
		if(this.gridNodeProfile instanceof J_EAProfileFlex && ((J_EAProfileFlex)gridNodeProfile).getLoadShiftingEnabled()) {
			profile_kW = Arrays.copyOf(((J_EAProfileFlex)gridNodeProfile).getCurrentFlexLoadSchedule_kW(), 0);
		}
		else {//Instanceof normal profile or flex without load shifting
			profile_kW = ZeroMath.arrayMultiply(Arrays.copyOfRange(this.gridNodeProfile.getProfile_kWh(), startTimeIntervalIndex, endTimeIntervalIndex), this.gridNodeProfile.getProfileScaling_fr()/this.timeStep_h );
    	}
		
		//Get the peak load that should be shaved
		double[] sortedProfile_kW = Arrays.copyOf(profile_kW, profile_kW.length);// ascending
		Arrays.sort(sortedProfile_kW);
    	double highestPctPeak_kW = sortedProfile_kW[(int)(0.8 * (sortedProfile_kW.length - 1))];
    	//double lowestPctPeak_kW = sortedProfile_kW[(int)(0.2 * (sortedProfile_kW.length - 1))];
    	
    	if(max(profile_kW) < highestPctPeak_kW) {
    		return new Pair<>(null, null);
    	}
    	else {
    		//traceln("day does contains moments where load is over: " + highestPctPeak_kW + "kW");
    	}
    	//Find the longest interval above the highestPctPeak_kW start and end time
    	// Allow up to this many consecutive timesteps below threshold without breaking interval
    	int maxGap = 2;
    	
    	//Initialize variables
    	int bestStartIndex = -1;
    	int bestEndIndex = -1;
    	int currentStartIndex = -1;
    	int currentIntervalGapCount = 0;

    	for (int i = 0; i < profile_kW.length; i++) {
    	    if (profile_kW[i] > highestPctPeak_kW) {
    	        // Good point: above threshold
    	        if (currentStartIndex == -1) {
    	        	currentStartIndex = i;
    	        }
    	        currentIntervalGapCount = 0; // reset gap counter
    	    } else {
    	        // Below threshold
    	        if (currentStartIndex != -1) {
    	        	currentIntervalGapCount++;

    	            if (currentIntervalGapCount > maxGap) {
    	                // Interval ends before the gap started
    	                int intervalEnd = i - currentIntervalGapCount;
    	                if (bestStartIndex == -1 || intervalEnd - currentStartIndex > bestEndIndex - bestStartIndex) {
    	                	bestStartIndex = currentStartIndex;
    	                    bestEndIndex = intervalEnd;
    	                }

    	                // Interval reset
    	                currentStartIndex = -1;
    	                currentIntervalGapCount = 0;
    	            }
    	        }
    	    }
    	}

    	// If the interval extended to the end of the array
    	if (currentStartIndex != -1) {
    	    int intervalEnd = profile_kW.length - 1;
    	    if (bestStartIndex == -1 || intervalEnd - currentStartIndex > bestEndIndex - bestStartIndex) {
    	    	bestStartIndex = currentStartIndex;
    	    	bestEndIndex = intervalEnd;
    	    }
    	}

    	// Convert indices to hour-of-day
    	double startTimeReducedElectricityConsumption_hr = bestStartIndex >= 0.25 ? bestStartIndex * this.timeStep_h : 0.25;//Hour of the day
    	double endTimeReducedElectricityConsumption_hr = bestEndIndex >= 0 ? (bestEndIndex + 1) * this.timeStep_h : 0;//Hour of the day
    	traceln("Interval: " + startTimeReducedElectricityConsumption_hr + " : " + endTimeReducedElectricityConsumption_hr); 
    	return new Pair<>(startTimeReducedElectricityConsumption_hr, endTimeReducedElectricityConsumption_hr);
    }
    	
	public void setTarget(GridNode target) {
    	this.targetNode = target;
    	this.gridNodeProfile = findFirst(this.targetNode.f_getConnectedGridConnections(),gc -> gc.p_gridConnectionID.equals("GridNode " + this.targetNode.p_gridNodeID + " profile GC")).c_profileAssets.get(0);
    }
    
    //Get parentagent
    public Agent getParentAgent() {
    	return this.energyCoop;
    }
    
    //Store and reset states
	public void storeStatesAndReset() {
	}
	public void restoreStates() {
	}
	
	
	@Override
	public String toString() {
		return super.toString();
	}	
}