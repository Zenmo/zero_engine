/**
 * J_EAVehicle
 */	
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "Type"  // 👈 this will be the field name in your JSON
	)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public abstract class J_EAVehicle extends J_EA implements Serializable {
	public boolean available = true;
	protected boolean availableStored = true;
	protected double energyConsumption_kWhpkm;
	//private MobilityTracker mobilityTracker = null;
	public double vehicleScaling;
	public double mileage_km = 0;
	public J_ActivityTrackerTrips tripTracker;
    /**
     * Default constructor
     */
    public J_EAVehicle() {
    }

    public boolean startTrip() {
    	return false;
    }
    
    public boolean endTrip(double tripDist_km) {
    	return false;
    }
    
    public void operate(double ratioOfCapacity) {
    	traceln("***Warning*** abstract J_EAVehicle operate! This is a dummy function, doing nothing!");
		//return null;
    }
    
    public void setVehicleScaling(double vehicleScaling) {
    	this.vehicleScaling = vehicleScaling;
    }
    
	public void setTripTracker(J_ActivityTrackerTrips tracker) {
		tripTracker = tracker;
	}

	public J_ActivityTrackerTrips getTripTracker() {
		return tripTracker;
	}
    
	public boolean getAvailability() {
		return available;
	}
	
	public double getVehicleScaling() {
		return vehicleScaling;
	}
	
	public double getEnergyConsumption_kWhpkm() {
		return energyConsumption_kWhpkm * vehicleScaling;
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