/**
 * J_Address
 */	
public class J_Address implements Serializable {

	private String streetName;
	private int houseNumber;
	private String houseLetter;
	private String houseAddition;
	private String postalcode;
	private String city;
	
    /**
     * Default constructor
     */
    public J_Address() {
    }
    
    public String getAddress() {
    	StringBuilder sb = new StringBuilder();
    	
    	if ( this.streetName != null ) {
    		sb.append(this.streetName);
    	}
    	else {
    		sb.append("Onbekend adres");
    		return sb.toString();
    	}
    	sb.append(" ");
    	sb.append(this.houseNumber);
		if ( this.houseLetter != null ) {
	    	sb.append(" ");
	    	sb.append(this.houseLetter);
	    }
		if ( this.houseAddition != null ) {
	    	sb.append(" ");
	    	sb.append(this.houseAddition);
	    }
		if ( this.postalcode != null || this.city != null) {
			sb.append(",");
		}
		if ( this.postalcode != null ){
			sb.append(" ");
			sb.append(this.postalcode);
		}
		if ( this.postalcode != null ){
			sb.append(" ");
			sb.append(this.city);
		}
		
		return sb.toString();
    }
    
    
    public void setStreetName( String streetName ) { 
        this.streetName = streetName;
	}
	public void setHouseNumber( int houseNumber ) { 
	        this.houseNumber = houseNumber;
	}
	public void setHouseLetter( String houseLetter ) { 
	        this.houseLetter = houseLetter;
	}
	public void setHouseAddition( String houseAddition ) { 
	        this.houseAddition = houseAddition;
	}
	public void setPostalcode( String postalcode ) { 
	        this.postalcode = postalcode;
	}
	public void setCity( String city ) {
	        this.city = city;
	}
	
	public String getStreetName() { 
        return this.streetName;
	}
	public int getHouseNumber() {
	        return this.houseNumber;
	}
	public String getHouseLetter() {
	        return this.houseLetter;
	}
	public String getHouseAddition() {
	        return this.houseAddition;
	}
	public String getPostalcode() {
	        return this.postalcode;
	}
	public String getCity() {
	        return this.city;
	}
	
	@Override
	public String toString() {
		return "J_Address(" + getAddress() + ")";
	}

	
	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

}