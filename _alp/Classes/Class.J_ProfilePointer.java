/**
 * J_ProfilePointer
 */	
public class J_ProfilePointer implements Serializable {
	public String name = "";
	private double currentValue = 0;
	private TableFunction tableFunction;
	
    /**
     * Default constructor
     */
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
    
    public TableFunction getTableFunction() {
    	return this.tableFunction;
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