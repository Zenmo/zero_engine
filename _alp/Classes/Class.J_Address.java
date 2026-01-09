/**
 * J_Address
 */	
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonAutoDetect(
    fieldVisibility = Visibility.ANY,    // ✅ only public fields are serialized
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")

public class J_Address{

	private String streetName;
	private Integer houseNumber;
	private String houseLetter;
	private String houseAddition;
	private String postalcode;
	private String city;
	
    /**
     * Default constructor
     */
    public J_Address() {
    }
    
    public J_Address(String streetName, Integer houseNumber, String houseLetter, String houseAddition, String postalcode, String city) {
    	setStreetName(streetName);
    	setHouseNumber(houseNumber);
    	setHouseLetter(houseLetter);
    	setHouseAddition(houseAddition);
    	setPostalcode(postalcode);
    	setCity(city);
    }
    
    //Get formated address
    public String getAddress() {
    	StringBuilder sb = new StringBuilder();
    	
    	if ( this.streetName != null ) {
    		sb.append(this.streetName);
    	}
    	else {
    		sb.append("Onbekend adres");
    		return sb.toString();
    	}
    	if(this.houseNumber != null) {
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
    	}
		if ( this.postalcode != null || this.city != null) {
			sb.append(",");
		}
		if ( this.postalcode != null ){
			sb.append(" ");
			sb.append(this.postalcode);
		}
		if ( this.city != null ){
			sb.append(" ");
			sb.append(this.city);
		}
		
		return sb.toString();
    }
    
    
    //Setters
    public void setStreetName( String streetName ) { 
    	if (isNotEmptyString(streetName)) {
    	    this.streetName = getFormattedName(streetName);
    	}
    	else {
    		this.streetName = null;
    	}
	}
	public void setHouseNumber( Integer houseNumber ) { 
        this.houseNumber = houseNumber;
	}
	public void setHouseLetter( String houseLetter ) { 
    	if(isNotEmptyString(houseLetter)) {
    		this.houseLetter = houseLetter;
    	}
    	else {
    		this.houseLetter = null;
    	}
	}
	public void setHouseAddition( String houseAddition ) { 
    	if(isNotEmptyString(houseAddition)) {
    		this.houseAddition = houseAddition;
    	}
    	else {
    		this.houseAddition = null;
    	}
	}
	public void setPostalcode( String postalcode ) { 
    	if(isNotEmptyString(postalcode)) {
    		this.postalcode = postalcode.toUpperCase().replaceAll("\\s","");
    	}
    	else {
    		this.postalcode = null;
    	}
	}
	public void setCity( String city ) {
		if(isNotEmptyString(city)) {
			this.city = getFormattedName(city);
		}
		else {
			this.city = null;
		}
	}
	
	
	//Getters
	public String getStreetName() { 
        return this.streetName;
	}
	public Integer getHouseNumber() {
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
	
	
	
	//Support function for (Street/City) names to remove double white spaces and make every thing lower space, except the first letter of every word 
	private String getFormattedName(String name) {
		String[] words = name.trim().toLowerCase().split("\\s+");
	    StringBuilder sb = new StringBuilder();
	
	    for (String word : words) {
	        sb.append(Character.toUpperCase(word.charAt(0)))
	          .append(word.substring(1))
	          .append(" ");
	    }
	
	    return sb.toString().trim();
	}
	
	//Check used to see if address (String) item is filled in/usefull
	private boolean isNotEmptyString(String str) {
        // Return true if str is not null, empty and doesnt only contain spaces
	    return str != null && !str.trim().isEmpty();
	}
	
	
	@Override
	public String toString() {
		return "J_Address(" + getAddress() + ")";
	}
}