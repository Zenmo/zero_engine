/**
 * J_FlexAssetScheduler
 */	
public class J_FlexAssetScheduler {

	/*
	 * Helper Methods to manipulate arrays
	 */
	private static int[] argsort(final double[] a) {
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

    private static double[] subtractArrays(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    /*
     * Main Method to call, as arguments it takes:
     * The load before the flex asset is scheduled,
     * The 'Virtual' Asset, which is a class that stored asset settings, including the allowed times to operate it and the min/max power,
     * The work to be scheduled,
     * A market class that is set with the feedback corresponding to the asset being scheduled.
     * 
     * This method wraps 'scheduleIteration', small blocks of energy are scheduled in each iteration until the amount of work is achieved.
     */
    public static double[] scheduleWrapper(
            double[] previousLoadProfile_kW,
            J_VirtualFlexAsset asset,
            double work_kWh,
            J_Market market,
            double timeStep_h,
            boolean separateMarketAndCongestion) {

        double[] newLoadProfile_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
        if (asset.maxPower_kW == 0) {
            return newLoadProfile_kW;
        }

        double workRemaining_kWh = work_kWh;

        while (DoubleCompare.greaterThanZero(workRemaining_kWh)) {
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
            J_VirtualFlexAsset asset,
            double workRemaining_kWh,
            J_Market market,
            double timeStep_h,
            double[] localMarginalPriceCurve_eurpMWh) {

        int[] cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
        int i = 0;

        while (asset.profile_kW[cheapestTimeIdxsSorted[i]] >= asset.maxPower_kW - 0.001 || !asset.allowedOperatingTimes[cheapestTimeIdxsSorted[i]]) {
            i++;
            if (i == loadProfile_kW.length - 1) {
                throw new RuntimeException("Warning: No more scheduling opportunities available! Work remaining: " + workRemaining_kWh);
            }
        }

        double addedPower_kW = Math.min(asset.minPower_kW, workRemaining_kWh / timeStep_h);

        loadProfile_kW[cheapestTimeIdxsSorted[i]] += addedPower_kW; // Update the load profile, also for the caller of this method! (test this to be sure?)
        asset.profile_kW[cheapestTimeIdxsSorted[i]] += addedPower_kW;
        workRemaining_kWh -= addedPower_kW * timeStep_h;

        return workRemaining_kWh; // no need to return the load profile, as it is modified in place (pass by reference)
    }
    
}
