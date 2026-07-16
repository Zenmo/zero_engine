/**
 * J_FlexProfileManagementHeatprofileHeatpump
 */	
public class J_FlexProfileManagementHeatprofileHeatpump implements I_FlexProfileManagement{

	private GridConnection gc;
	private J_TimeParameters timeParameters;
	private double[] flexProfileSetpointArray_fr;
	private int currentFlexProfileSetpointArrayIndex;
	private double forecastingDuration_h = 24;
	private double maxFlexProfileShift_fr = 0.3;// maximum shift per timestep.
	
    /**
     * Empty constructor for serialization
     */
    public J_FlexProfileManagementHeatprofileHeatpump() {
    }
    
    /**
     * Default constructor
     */
    public J_FlexProfileManagementHeatprofileHeatpump(GridConnection gc, J_TimeParameters timeParameters) {
    	this.gc = gc;
    	this.timeParameters = timeParameters;
    	this.flexProfileSetpointArray_fr = new double[roundToInt(forecastingDuration_h/timeParameters.getTimeStep_h())];
    	Arrays.fill(flexProfileSetpointArray_fr, 1.0);
    	this.currentFlexProfileSetpointArrayIndex = 0; // Is not in sync with real t_h, but doesnt matter, forecast will reset at right time and override.
    }
    
    public void manageFlexProfiles(J_TimeVariables timeVariables) {
    	if(timeVariables.getT_h() % forecastingDuration_h == 0) {
    		createNewFlexProfileSetpointArray(timeVariables);
    	}
    	
    	List<J_EAFlexProfile> flexibleHeatProfiles = findAll(gc.c_flexProfileAssets, flexProfile -> flexProfile.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	
    	gc.f_updateFlexAssetFlows(flexibleHeatProfiles.get(0), flexProfileSetpointArray_fr[currentFlexProfileSetpointArrayIndex], timeVariables);
    	currentFlexProfileSetpointArrayIndex++;
    }
    
    
    
    private void createNewFlexProfileSetpointArray(J_TimeVariables timeVariables) {
    	int numberOfTimeSteps = roundToInt(forecastingDuration_h/timeParameters.getTimeStep_h());
    	
    	//Get fixed nettobalance forecast
    	double[] nettoBalanceForecastElectricity_kW = gc.f_getFixedAssetForecast(timeVariables.getT_h(), timeVariables.getT_h() + forecastingDuration_h, OL_EnergyCarriers.ELECTRICITY, timeParameters);
    	
    	//Get flexprofile netto Heat balance forecast
    	double[] originalFlexprofileNettoHeatBalance_kW = new double[numberOfTimeSteps];
    	List<J_EAFlexProfile> flexibleHeatProfiles = findAll(gc.c_flexProfileAssets, flexProfile -> flexProfile.getEnergyCarrier() == OL_EnergyCarriers.HEAT);
    	
    	if(flexibleHeatProfiles.size() == 0 || flexibleHeatProfiles.size() > 1 || gc.f_getCurrentHeatingType() != OL_GridConnectionHeatingType.ELECTRIC_HEATPUMP || gc.f_getHeatingTypeIsGhost()) {
    		throw new RuntimeException("Flex profile management heatprofile heatpump active with multiple, or without a heat flex profile or (non ghost) heatpump being present!");
    	}
    	
    	originalFlexprofileNettoHeatBalance_kW = LUXMath.addArrays(originalFlexprofileNettoHeatBalance_kW, flexibleHeatProfiles.get(0).getForecast_kW(timeVariables.getT_h(), timeVariables.getT_h() + forecastingDuration_h));
    	
    	//Convert flexprofile to (forecasted) electricity profile
		double[] originalFlexprofileNettoElectricityBalance_kW = new double[numberOfTimeSteps];
		
		//Calculate the actual power the heating asset would use (by calculating the efficiency) and add to nettoBalance_kW
		J_EAConversionHeatPump heatPump = (J_EAConversionHeatPump)gc.c_heatingAssets.get(0);
		double[] invCOP = new double[numberOfTimeSteps];
		//Todo -> Make work for other ambient temp types!
		J_ProfilePointer ambientTemperatures = gc.energyModel.pp_ambientTemperature_degC;
		for (int i = 0; i < numberOfTimeSteps; i++) {
			invCOP[i] = 1.0/heatPump.calculateCOP(heatPump.getOutputTemperature_degC(), ambientTemperatures.getValue(timeVariables.getT_h() + i * timeParameters.getTimeStep_h()));
		}
		originalFlexprofileNettoElectricityBalance_kW = LUXMath.multiplyArrays(originalFlexprofileNettoHeatBalance_kW, invCOP);
		
		
		//Calculate total
		double[] totalElectricityBalanceForecast_kW = LUXMath.addArrays(nettoBalanceForecastElectricity_kW, originalFlexprofileNettoElectricityBalance_kW);
    	
		////Determine power fraction of flex profile, and store!
		double[] newFlexProfileSetpointArray = new double[numberOfTimeSteps];
		
		
		// --- Peak shaving + self-consumption via water-filling, total heat is conserved ---
		// One "water level" W on the total electricity curve does everything:
		// total[i] = base[i] + flexElec[i] * f[i]
		// Steps that can reach W settle at total[i] = W; steps that can't are pinned
		// at their shift limit (f = L or f = U). Raising W pours the fixed heat budget
		// into the lowest valleys first -> (a) minimises the peak and (b) fills the
		// deepest (export) valleys first = maximises self-consumption.
		// f stays in [L, U]; heat is conserved.
		
		final double L = 1.0 - maxFlexProfileShift_fr;
		final double U = 1.0 + maxFlexProfileShift_fr;

		final double[] base     = nettoBalanceForecastElectricity_kW;
		final double[] flexElec = originalFlexprofileNettoElectricityBalance_kW;
		final double[] heat     = originalFlexprofileNettoHeatBalance_kW;

		// Heat to preserve over the horizon (== the sum at f == 1 everywhere).
		double totalHeatTarget = 0.0;
		for (int i = 0; i < numberOfTimeSteps; i++) totalHeatTarget += heat[i];

		// Bracket the water level by the totals reachable within the shift band.
		double wLow = Double.POSITIVE_INFINITY, wHigh = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < numberOfTimeSteps; i++) {
		    if (flexElec[i] == 0.0) continue;       // no electricity coupling, no heat
		    double tA = base[i] + flexElec[i] * L;
		    double tB = base[i] + flexElec[i] * U;
		    wLow  = min(wLow,  min(tA, tB));
		    wHigh = max(wHigh, max(tA, tB));
		}

		double level = 1.0; // fallback only if there is no flex at all
		if (wLow <= wHigh) {
		    // sum_i heat[i]*f(W) is monotonically increasing in W (per-step slope is
		    // heat[i]/flexElec[i] == COP[i] > 0), so binary-search the W that conserves heat.
		    double lo = wLow, hi = wHigh;
		    for (int iter = 0; iter < 200 && (hi - lo) > 1e-9; iter++) {
		        double mid = 0.5 * (lo + hi);
		        double heatSum = 0.0;
		        for (int i = 0; i < numberOfTimeSteps; i++) {
		            if (flexElec[i] == 0.0) continue;
		            double f = (mid - base[i]) / flexElec[i];
		            f = max(L, min(U, f));
		            heatSum += heat[i] * f;
		        }
		        if (heatSum >= totalHeatTarget) hi = mid; else lo = mid;
		    }
		    level = 0.5 * (lo + hi);
		}

		// Materialise the power fractions at the solved level.
		for (int i = 0; i < numberOfTimeSteps; i++) {
		    if (flexElec[i] == 0.0) {               // nothing to shift here
		        newFlexProfileSetpointArray[i] = 1.0;
		        continue;
		    }
		    double f = (level - base[i]) / flexElec[i];
		    newFlexProfileSetpointArray[i] = max(L, min(U, f));
		}
    	
		//Store new array
		this.currentFlexProfileSetpointArrayIndex = 0;
		this.flexProfileSetpointArray_fr = newFlexProfileSetpointArray;
    }
    
	public J_AssetTypeForecast getForecast(double timeOfIntervalStart_h, double timeOfIntervalEnd_h) {
		Map<OL_EnergyCarriers, Double[]> loadMap = new HashMap<>();
		OL_ForecastStatus status = OL_ForecastStatus.NOT_FORECASTABLE;
		String reason = "Not yet implemented.";
		return new J_AssetTypeForecast(I_FlexProfileManagement.class, loadMap, status, reason);
	}
	
    //Setters
    public void setMaxFlexProfileShift_fr(double maxFlexProfileShift_fr) {
    	if(maxFlexProfileShift_fr<0) {
    		throw new RuntimeException("Trying to set the max flex profile shift of J_EMS_NBH_LocalBalance_Internal to a negative value, not allowed!");
    	}
    	this.maxFlexProfileShift_fr = maxFlexProfileShift_fr;
    }
    
    ////Store and reset states
	public void storeStatesAndReset() {
		//Nothing to store and reset
	}
	public void restoreStates() {
		//Nothing to restore
	}
	
	@Override
	public String toString() {
		List<J_EAFlexProfile> flexProfiles = gc.c_flexProfileAssets;
		StringBuilder flexProfilesString = new StringBuilder();
		for(J_EAFlexProfile flexProfile : flexProfiles) {
			flexProfilesString.append(System.lineSeparator());
			flexProfilesString.append(flexProfile.toString());
		}
		
		return "J_FlexProfileManagementHeatprofileHeatpump: " + System.lineSeparator() +
				"Currently controlling J_EAFlexProfiles: " + 
				flexProfilesString.toString();
	}
}