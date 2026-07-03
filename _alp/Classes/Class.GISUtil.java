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
	
	public static double[] calculateSquareCoordinates(double lat, double lon, double area_m2) {
		/**
		 * Computes a list of double[] coordinates representing a perfectly square polygon centered
		 * at (lat, lon) with a physical area of 'area_m2' in square meters, correcting for local latitude "deformation".
		 */
		
		double side_m = Math.sqrt(area_m2);
		double halfSide_m = side_m / 2.0;
		
		// Earth conversion factors (approximate for localized areas)
		double metersPerDegLat = 111320.0;
		double metersPerDegLon = 111320.0 * Math.cos(Math.toRadians(lat));
		
		double offsetLat = halfSide_m / metersPerDegLat;
		double offsetLon = halfSide_m / metersPerDegLon;
		
		return new double[]{
		    lat + offsetLat, lon - offsetLon, // Top-left
		    lat + offsetLat, lon + offsetLon, // Top-right
		    lat - offsetLat, lon + offsetLon, // Bottom-right
		    lat - offsetLat, lon - offsetLon  // Bottom-left
		};
	}
	
	public static double[] calculateCircleCoordinates(double lat, double lon, double area_m2) {
		/**
		 * Computes a list of double[] coordinates representing a perfectly circular polygon centered
		 * at (lat, lon) with a physical area of 'area_m2' in square meters, correcting for local latitude "deformation".
		 */
		
		double radius_m = Math.sqrt(area_m2 / Math.PI); // A circle's radius in meters from its area in m²: Area = pi * r^2  =>  r = sqrt(Area / pi)

		int numPoints = 48; // Number of points (vertices) to approximate the circle
		double[] polyCoords = new double[numPoints * 2];

		// Earth conversion factors (approximate for localized areas)
		double metersPerDegLat = 111320.0;
		double metersPerDegLon = 111320.0 * Math.cos(Math.toRadians(lat));

		for (int i = 0; i < numPoints; i++) {
		    double angle = 2.0 * Math.PI * i / numPoints;
		    double offsetLat = (radius_m * Math.sin(angle)) / metersPerDegLat;
		    double offsetLon = (radius_m * Math.cos(angle)) / metersPerDegLon;
		    
		    polyCoords[2 * i] = lat + offsetLat;
		    polyCoords[2 * i + 1] = lon + offsetLon;
		}

		return polyCoords;
	}
	
	public static double[] calculateCustomPolygonCoordinates(List<Point> coordinateList) {
		/**
		 * Computes a list of double[] coordinates from a list of coordinate Points.
		 */
		
		int size = coordinateList.size();
		double[] polyCoords = new double[size * 2];
		for (int i = 0; i < size; i++) {
		    Point p = coordinateList.get(i);
		    polyCoords[2 * i] = p.getX();
		    polyCoords[2 * i + 1] = p.getY();
		}
		return polyCoords;
	}
	
	public static boolean arePolygonEdgesSelfIntersecting(List<Point> vertices) {
		/**
	     * Checks if a polygon represented by a list of Point vertices is self-intersecting.
	     * Returns true if any two non-adjacent edges intersect.
	     */
		
		int n = vertices.size();
		if (n < 4) {
		    return false; // A polygon with less than 4 vertices cannot self-intersect
		}
		for (int i = 0; i < n; i++) {
            Point p1 = vertices.get(i);
            Point q1 = vertices.get((i + 1) % n);
            for (int j = i + 2; j < n; j++) {
                // Skip adjacent edges (they naturally share a vertex)
                if (i == 0 && j == n - 1) {
                    continue;
                }
                Point p2 = vertices.get(j);
                Point q2 = vertices.get((j + 1) % n);
                if (areSegmentsIntersecting(p1.getX(), p1.getY(), q1.getX(), q1.getY(), p2.getX(), p2.getY(), q2.getX(), q2.getY())) {
                    return true; // Intersection detected
                }
            }
        }
        return false;
	
	}
	
    public static boolean areSegmentsIntersecting(double p1x, double p1y, double q1x, double q1y,
                                            double p2x, double p2y, double q2x, double q2y) {
    	/**
         * Checks if line segment p1q1 and p2q2 intersect.
         */
    	
        int o1 = getOrientation(p1x, p1y, q1x, q1y, p2x, p2y);
        int o2 = getOrientation(p1x, p1y, q1x, q1y, q2x, q2y);
        int o3 = getOrientation(p2x, p2y, q2x, q2y, p1x, p1y);
        int o4 = getOrientation(p2x, p2y, q2x, q2y, q1x, q1y);
        // General Case: Segments cross each other
        if (o1 != o2 && o3 != o4) return true;
        // Special Cases (Collinear segments overlapping)
        if (o1 == 0 && isPointOnSegment(p1x, p1y, p2x, p2y, q1x, q1y)) return true;
        if (o2 == 0 && isPointOnSegment(p1x, p1y, q2x, q2y, q1x, q1y)) return true;
        if (o3 == 0 && isPointOnSegment(p2x, p2y, p1x, p1y, q2x, q2y)) return true;
        if (o4 == 0 && isPointOnSegment(p2x, p2y, q1x, q1y, q2x, q2y)) return true;
        return false;
    }
    
    // Helper: Checks if point q lies on line segment pr
    private static boolean isPointOnSegment(double px, double py, double qx, double qy, double rx, double ry) {
        return qx <= max(px, rx) && qx >= min(px, rx) &&
               qy <= max(py, ry) && qy >= min(py, ry);
    }
    
    // Helper: Finds the orientation of ordered triplet (p, q, r).
    // Returns: 0 -> Collinear, 1 -> Clockwise, 2 -> Counterclockwise
    private static int getOrientation(double px, double py, double qx, double qy, double rx, double ry) {
        double val = (qy - py) * (rx - qx) - (qx - px) * (ry - qy);
        if (DoubleCompare.equalsZero(val)) return 0;
        return (val > 0) ? 1 : 2;
    }

}