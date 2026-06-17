/**
 * GISUtil
 */	
public class GISUtil {
	public static double calculateManhattanDistance_m(double latitude1, double longitude1, double latitude2, double longitude2) {
		double latDist_m = getDistanceGIS(latitude1, longitude1, latitude2, longitude1); //Use anylogic getDistanceGIS function for point 1 
		double longDist_m = getDistanceGIS(latitude2, longitude1, latitude2, longitude2); //Use anylogic getDistanceGIS function
		double totalDist_m = latDist_m + longDist_m;
		return totalDist_m;
	}
}