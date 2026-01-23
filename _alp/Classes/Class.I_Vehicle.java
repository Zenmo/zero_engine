public interface I_Vehicle
{
	// These methods can't have default implementations because they use the Object States.

    boolean startTrip(J_TimeVariables timeVariables);
    boolean endTrip(double tripDist_km);
    
    void setVehicleScaling(double vehicleScaling);    
	void setTripTracker(J_ActivityTrackerTrips tracker);
	
	J_ActivityTrackerTrips getTripTracker();
	boolean getAvailability();
	double getVehicleScaling_fr();
	double getEnergyConsumption_kWhpkm();
	
	//Temporary, till replaced with getVehicleType() (Car, Van, Truck, Tractor) and getFuelType() (ELE, Petrol, h2)
	OL_EnergyAssetType getEAType();
}