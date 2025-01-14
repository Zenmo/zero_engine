/**
 * J_Triple
 */	
public class J_Triple<T, U, V> implements Serializable {

    private final T first;
    private final U second;
    private final V third;
    
    /**
     * Default constructor
     */
    public J_Triple(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() {
    	return first; 
    }
    public U getSecond() { 
    	return second;
    }
    public V getThird() {
    	return third;
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