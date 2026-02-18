package zeroPackage;

import java.util.Arrays;
import java.util.Comparator;
// import zeroPackage.FlexConsumptionAsset;
// import holonPackage.Market;
// import Market.Market;
// import Asset.Asset;
// import zeroPackage.ZeroMath;
 
public class FlexAssetScheduler {

    public static int[] argsort(final double[] a) {
        return argsort(a, true);
    }
    private static int[] argsort(final double[] a, final boolean ascending) {
        Integer[] indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(final Integer i1, final Integer i2) {
                return (ascending ? 1 : -1) * Double.compare(a[i1], a[i2]);
            }
        });
        return asArray(indexes);
    }
    private static <T extends Number> int[] asArray(final T... a) {
        int[] b = new int[a.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = a[i].intValue();
        }
        return b;
    }

    public static double[] subtractArrays(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    public static double[] scheduleWrapper(
            double[] previousLoadProfile_kW,
            FlexConsumptionAsset asset,
            double work_kWh,
            Market market,
            double timeStep_h,
            boolean separateMarketAndCongestion) {

        double[] newLoadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
        if (asset.maxPower_kW == 0) {
            return newLoadProfile_kW;
        }

        double workRemaining_kWh = work_kWh;

        while (workRemaining_kWh > 1e-10) {
            double[] localMarginalPriceCurve_eurpMWh;
            if (separateMarketAndCongestion) {
                localMarginalPriceCurve_eurpMWh = market.getMarginalPriceCurveUpwards(subtractArrays(newLoadProfile_kW, previousLoadProfile_kW), newLoadProfile_kW);
            }
            else {
                localMarginalPriceCurve_eurpMWh = market.getMarginalPriceCurveUpwards(newLoadProfile_kW, null);
            }
            
            workRemaining_kWh = scheduleIteration(newLoadProfile_kW, asset, workRemaining_kWh, market, timeStep_h, localMarginalPriceCurve_eurpMWh);
        }

        return newLoadProfile_kW;
    }

    private static double scheduleIteration(
            double[] loadProfile_kW,
            FlexConsumptionAsset asset,
            double workRemaining_kWh,
            Market market,
            double timeStep_h,
            double[] localMarginalPriceCurve_eurpMWh) {

        int[] cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
        int i = 0;

        while (asset.profile_kW[cheapestTimeIdxsSorted[i]] >= asset.maxPower_kW || !asset.allowedOperatingTimes[cheapestTimeIdxsSorted[i]]) {
            i++;
            if (i == loadProfile_kW.length) { // Why minus 1? 
                throw new RuntimeException("Warning: No more scheduling opportunities available! Work remaining: " + workRemaining_kWh);
            }
        }

        double addedPower_kW = Math.min(asset.minPower_kW, Math.min(workRemaining_kWh / timeStep_h, asset.maxPower_kW - asset.profile_kW[cheapestTimeIdxsSorted[i]])); // addedPower_kW is positive for consumption!

        loadProfile_kW[cheapestTimeIdxsSorted[i]] += addedPower_kW; // Update the load profile, also for the caller of this method! (test this to be sure?)
        asset.profile_kW[cheapestTimeIdxsSorted[i]] += addedPower_kW;
        workRemaining_kWh -= addedPower_kW * timeStep_h;

        return workRemaining_kWh; // no need to return the load profile, as it is modified in place (pass by reference)
    }

    public static double[] scheduleWrapperFlexProduction(
            double[] previousLoadProfile_kW,
            FlexConsumptionAsset asset,
            double work_kWh,
            Market market,
            double timeStep_h,
            boolean separateMarketAndCongestion) {

        double[] newLoadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
        if (asset.maxPower_kW == 0) {
            return newLoadProfile_kW;
        }

        double workRemaining_kWh = work_kWh;

        while (workRemaining_kWh > 0) {
            double[] localMarginalPriceCurve_eurpMWh;
            if (separateMarketAndCongestion) {
                localMarginalPriceCurve_eurpMWh = market.getMarginalPriceCurveDownwards(subtractArrays(newLoadProfile_kW, previousLoadProfile_kW), newLoadProfile_kW); // Note the 'downwards' here for production assets!
            }
            else {
                localMarginalPriceCurve_eurpMWh = market.getMarginalPriceCurveDownwards(newLoadProfile_kW, null); // Note the 'downwards' here for production assets!
            }
            
            workRemaining_kWh = scheduleIterationFlexProduction(newLoadProfile_kW, asset, workRemaining_kWh, market, timeStep_h, localMarginalPriceCurve_eurpMWh);
        }

        return newLoadProfile_kW;
    }

    private static double scheduleIterationFlexProduction(
            double[] loadProfile_kW,
            FlexConsumptionAsset asset,
            double workRemaining_kWh,
            Market market,
            double timeStep_h,
            double[] localMarginalPriceCurve_eurpMWh) {

        int[] cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
        int i = cheapestTimeIdxsSorted.length - 1; // Start from the highest price for production

        while (asset.profile_kW[cheapestTimeIdxsSorted[i]] <= -asset.maxPower_kW || !asset.allowedOperatingTimes[cheapestTimeIdxsSorted[i]]) { // Note the minus-sign, because production is negative load
            i--;
            if (i == 0) {
                throw new RuntimeException("Warning: No more scheduling opportunities available! Work remaining: " + workRemaining_kWh);
            }
        }

        double addedPower_kW = Math.min(asset.minPower_kW, workRemaining_kWh / timeStep_h); // Positive workRemaining_kWh means we need to produce this amount of energy, so addedPower_kW is production!

        loadProfile_kW[cheapestTimeIdxsSorted[i]] -= addedPower_kW; // Update the load profile, also for the caller of this method! (test this to be sure?)
        asset.profile_kW[cheapestTimeIdxsSorted[i]] -= addedPower_kW; // Stick to convention that production is negative load.
        workRemaining_kWh -= addedPower_kW * timeStep_h;

        return workRemaining_kWh; // no need to return the load profile, as it is modified in place (pass by reference)
    }
}
