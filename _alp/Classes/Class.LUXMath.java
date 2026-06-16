/**
 * LUXMath
 */	
public class LUXMath {
	
	public static double[] addArrays(double[] array1, double[] array2) {
		double[] result = new double[array1.length];
		for (int i = 0; i < array1.length; i++) {
		    result[i] = array1[i] + array2[i];
		}
		return result;
	}
	
	public static int[] addArrays(int[] array1, int[] array2) {
		int[] result = new int[array1.length];

		for (int i = 0; i < array1.length; i++) {
		    result[i] = array1[i] + array2[i];
		}
		return result;
	}
	
	public static double sumArray(double[] array) {
		double sum = 0;
	    for (double n : array) {
	    	sum += n;
	    }
	    return sum;
	}
	
	public static int sumArray(int[] array) {
	    int sum = 0;
	    for (int n : array) {
	    	sum += n;
	    }
	    return sum;
	}
	
	public static double sumArrayPos(double[] array) {
		double sum = 0;
	    for (double n : array) {
	    	if (n > 0) {
	    		sum += n;
	    	}
	    }
	    return sum;
	}
	
	public static double sumArrayNeg(double[] array) {
		double sum = 0;
	    for (double n : array) {
	    	if (n < 0) {
	    		sum += n;
	    	}
	    }
	    return sum;
	}
	
	public static double[] multiplyArray(double[] array, double value) {
		double[] result = new double[array.length];
	    for (int i = 0; i < array.length; i++) {
	        result[i] = array[i] * value;
	    }
	    return result;
	}
	
	public static double[] multiplyArrays(double[] array1, double[] array2) {
		double[] result = new double[array1.length];
	    for (int i = 0; i < array1.length; i++) {
	        result[i] = array1[i] * array2[i];
	    }
	    return result;
	}
	
	public static double[] reciprocal(double[] array) {
	    double[] result = new double[array.length];
	    for (int i = 0; i < array.length; i++) {
	        result[i] = 1.0 / array[i];
	    }
	    return result;
	}
}