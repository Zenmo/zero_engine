import java.lang.Math;
/**
 * DoubleCompare
 */
public class DoubleCompare {
	private final static int FLOATINGPOINTPRECISION = 10;
	private final static double EPSILON = 1e-10;
	
	public static boolean equalsZero(double d) {
		//d = round(d);
		//return d == 0.0;		
		
		return Math.abs(d) < EPSILON;
	}
	
	public static boolean lessThanZero(double d) {
		//d = round(d);
		//return d < 0.0;		
		return d < -EPSILON;
	}
	
	public static boolean greaterThanZero(double d) {
		//d = round(d);
		//return d > 0.0;
		return d > EPSILON;
	}
	
	public static boolean equals(double a, double b) {
		
		//a = round(a);
		//b = round(b);
		//return a == b;
		return Math.abs(a - b) < EPSILON;
		
	}
	
	//private static double round(double d) {
		//return Math.floor(d * Math.pow(10, FLOATINGPOINTPRECISION) + 0.5) / Math.pow(10, FLOATINGPOINTPRECISION);
	//}
	
}