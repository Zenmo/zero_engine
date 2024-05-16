package zeroPackage;

import java.lang.Math;

public class ZeroMath {
    public static double arraySum(double[] array) {
        double sum = 0;
        for (double value : array) {
            sum += value;
        }
        return sum;
    }

    public static double arraySumPos(double[] array) {
        double posSum = 0;
        for (double value : array) {
            posSum += Math.max(0.0, value);
        }
        return posSum;
    }

    public static double arraySumNeg(double[] array) {
        double negSum = 0;
        for (double value : array) {
            negSum += Math.min(0.0, value);
        }
        return negSum;
    }

}
