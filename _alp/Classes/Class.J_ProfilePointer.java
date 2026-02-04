/**
 * J_ProfilePointer
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // also stores full profiles to file. Maybe arrange a way to 'skip' this?
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_ProfilePointer implements Serializable {
	public String name = "";
	private double currentValue = 0;
	private OL_ProfileUnits profileUnits;
	
	// Using internal array instead of tableFunction
	private double[] a_profile;
	private double dataTimeStep_h;
	private double dataStartTime_h; // relative to 00:00h on jan 1st of simulation year
	private boolean enableProfileLooping = true;
	
    /**
     * Default constructor
     */
	public J_ProfilePointer() {
	
	}
	
    public J_ProfilePointer(String name, double[] profile, double dataTimeStep_h, double dataStartTime_h, OL_ProfileUnits profileUnits) {
    	if (profileUnits == null) {
    		throw new RuntimeException("Attemtping to create J_ProfilePointer with null profileUnits!");
    	}
    	this.name = name;
    	this.a_profile = profile;
    	this.dataTimeStep_h = dataTimeStep_h;
    	this.dataStartTime_h = dataStartTime_h;	    	
    	this.profileUnits = profileUnits;
    }

    public void updateValue(double t_h) {
    	this.currentValue = this.getValue(t_h);
    }
    
    public double getCurrentValue() {
    	return this.currentValue;
    }
    
    public double getValue(double time_h) {
    	//return this.tableFunction.get(t_h);
    	int index_n = (int)((time_h-dataStartTime_h)/dataTimeStep_h);
    	if (enableProfileLooping && index_n >= a_profile.length) {
    		index_n = index_n % a_profile.length;
    	} else if ( index_n >= a_profile.length ) {
    		traceln("Time out of upper bound for evaluating J_EAProfile power in profile %s!", this.name);
//    		time_h = a_energyProfile_kWh.length * profileTimestep_h - 1;
    		throw new RuntimeException(String.format("Time out of upper bound for evaluating J_EAProfile power! Time is: %s", time_h));
    	}
    	if ( index_n < 0 ) {
    		traceln("Time out of lower bound for evaluating J_EAProfile power in profile %s!", this.name);
    		throw new RuntimeException(String.format("Time out of lower bound for evaluating J_EAProfile power! Time is: %s", time_h));
    	}
    	double currentValue_kW = this.a_profile[index_n]; 
    	return currentValue_kW;
    }
       
    public double[] getAllValues() {
    	//return this.tableFunction.getValues();
    	return this.a_profile;
    }
    
    public double getDataTimeStep_h() {
    	return dataTimeStep_h;
    }
    
    public OL_ProfileUnits getProfileUnits() {
    	return profileUnits;
    }
    
	@Override
	public String toString() {
		return "profile: " + this.name + " current value: " + this.currentValue; 
	}

}