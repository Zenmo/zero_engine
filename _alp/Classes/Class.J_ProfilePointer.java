/**
 * J_ProfilePointer
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonAutoDetect(
    fieldVisibility = Visibility.PROTECTED_AND_PUBLIC,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_ProfilePointer implements Serializable {
	public String name = "";
	private double currentValue = 0;
	private TableFunction tableFunction;
	
    /**
     * Default constructor
     */
	public J_ProfilePointer() {
	
	}
	
    public J_ProfilePointer(String name, TableFunction tableFunction) {
    	this.name = name;
    	this.tableFunction = tableFunction;
    }

    public void updateValue(double t_h) {
    	this.currentValue = this.tableFunction.get(t_h);
    }
    
    public double getCurrentValue() {
    	return this.currentValue;
    }
    
    public double getValue(double t_h) {
    	return this.tableFunction.get(t_h);
    }
    
    public double[] getAllValues() {
    	return this.tableFunction.getValues();
    }
    
    public void setTableFunction(TableFunction tf) {
    	this.tableFunction = tf;
    }
    
	@Override
	public String toString() {
		return "profile: " + this.name + " current value: " + this.currentValue; 
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}