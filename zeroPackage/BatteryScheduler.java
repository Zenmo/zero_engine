package zeroPackage;

import java.util.Arrays;
import java.util.Comparator;
import zeroPackage.BatteryAsset;
import zeroPackage.Market;
// import Market.Market;
// import Asset.Asset;
//import zeroPackage.ZeroMath;

/* 
public class AssetScheduler {

    public static double[] scheduleWrapper(
            double[] previousLoadProfile_kW,
            Asset asset,
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
            Asset asset,
            double workRemaining_kWh,
            Market market,
            double timeStep_h,
            double[] localMarginalPriceCurve_eurpMWh) {

        int[] cheapestTimeIdxsSorted = argsort(localMarginalPriceCurve_eurpMWh);
        int i = 0;

        while (asset.profile_kW[cheapestTimeIdxsSorted[i]] >= asset.maxPower_kW || !asset.allowedOperatingTimes[cheapestTimeIdxsSorted[i]]) {
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
*/
public class BatteryScheduler {

    protected static class ChargingStep {
        public Integer chargeTimeStep_n; // must be Integer to allow null values
        public double addedPower_kW;
        
        ChargingStep(Integer chargeTimeStep_n, double addedPower_kW) {
            this.chargeTimeStep_n = chargeTimeStep_n;
            this.addedPower_kW = addedPower_kW;
        }
    }

    protected static class ChargingDischaringStep {
        public Integer chargeTimeStep_n; // must be Integer to allow null values
        public Integer dischargeTimeStep_n; // must be Integer to allow null values
        public double addedChargePower_kW;
        public double addedDischargePower_kW;
        
        ChargingDischaringStep(Integer chargeTimeStep_n, Integer dischargeTimeStep_n, double addedChargePower_kW, double addedDischargePower_kW) {
            this.chargeTimeStep_n = chargeTimeStep_n;
            this.dischargeTimeStep_n = dischargeTimeStep_n;
            this.addedChargePower_kW = addedChargePower_kW;
            this.addedDischargePower_kW = addedDischargePower_kW;
        }
    }

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

    public static double maxInRange(double[] arr, int start, int end) {
    double max = arr[start];
    for (int i = start + 1; i < end; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    return max;
}

    public static double minInRange(double[] arr, int start, int end) {
    double min = arr[start];
    for (int i = start + 1; i < end; i++) {
        if (arr[i] < min) {
            min = arr[i];
        }
    }
    return min;
}

    public static double arrayMin(double[] array) {
        // Defensive check in case of empty array
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        return Arrays.stream(array).min().getAsDouble();
    }

    public static double arrayMax(double[] array) {
        // Defensive check in case of empty array
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        return Arrays.stream(array).max().getAsDouble();
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
            BatteryAsset battery,
            Market market,
            double timeStep_h,
            double priceSpreadThreshold_eurpMWh,
            boolean separateMarketAndCongestion) {

        if (battery.storageCapacity_kWh == 0) {
            return battery.profile_kW;
        }
        double[] loadWithBES_kW = Arrays.copyOf(previousLoadProfile_kW, previousLoadProfile_kW.length);
        
        double[] marginalPriceCurveUpwards_eurpMWh = market.getMarginalPriceCurveUpwards(subtractArrays(loadWithBES_kW, previousLoadProfile_kW), loadWithBES_kW);
        double lastChargePrice_eurpMWh = arrayMin(marginalPriceCurveUpwards_eurpMWh);
        double[] marginalPriceCurveDownwards_eurpMWh = market.getMarginalPriceCurveDownwards(subtractArrays(loadWithBES_kW, previousLoadProfile_kW), loadWithBES_kW);
        double lastDischargePrice_eurpMWh = arrayMax(marginalPriceCurveDownwards_eurpMWh);

        boolean[] availableToCharge = new boolean[previousLoadProfile_kW.length];
        Arrays.fill(availableToCharge, true);
        boolean[] availableToDischarge = new boolean[previousLoadProfile_kW.length];
        Arrays.fill(availableToDischarge, true);

        int i =0;
        boolean skipSeperateChargeAndDischarge = false;
        double addedBESChargePower_kW = 0;
        double addedBESDischargePower_kW = 0;
        int indexScheduleBefore = loadWithBES_kW.length;

        while ( i < 5000 ) {
            double[] localMarginalPriceCurveUpwards_eurpMWh;
            double[] localMarginalPriceCurveDownwards_eurpMWh;
            int[] cheapestTimeIdxsUpwardsSorted;
            int[] cheapestTimeIdxsDownwardsSorted;
            if ( !skipSeperateChargeAndDischarge ) {
                // We calculate the new marginal price based on the load and get an array of indices of the sorted price, from lowest price, to highest
                if ( separateMarketAndCongestion ) {
                    localMarginalPriceCurveUpwards_eurpMWh = market.getMarginalPriceCurveUpwards(subtractArrays(loadWithBES_kW, previousLoadProfile_kW), loadWithBES_kW);
                    localMarginalPriceCurveDownwards_eurpMWh = market.getMarginalPriceCurveDownwards(subtractArrays(loadWithBES_kW, previousLoadProfile_kW), loadWithBES_kW);
                }
                else {
                    localMarginalPriceCurveUpwards_eurpMWh = market.getMarginalPriceCurveUpwards(loadWithBES_kW, null);
                    localMarginalPriceCurveDownwards_eurpMWh = market.getMarginalPriceCurveDownwards(loadWithBES_kW, null);
                }
                
                cheapestTimeIdxsUpwardsSorted = argsort(localMarginalPriceCurveUpwards_eurpMWh);
                cheapestTimeIdxsDownwardsSorted = argsort(localMarginalPriceCurveDownwards_eurpMWh);
                
                
                //Triple<double[], Double, Integer> triple = ScheduleChargingIteration( loadWithBES_kW, battery, cheapestTimeIdxsUpwardsSorted, timeStep_h, availableToCharge);
                ChargingStep chargeStep = ScheduleChargingIteration( loadWithBES_kW, battery, cheapestTimeIdxsUpwardsSorted, timeStep_h, availableToCharge);
                //System.out.printf("Charge step: %s, power: %s kW %n" , chargeStep.chargeTimeStep_n, chargeStep.addedPower_kW);
                //loadWithBES_kW = triple.getFirst();
                addedBESChargePower_kW = chargeStep.addedPower_kW;
                
                // If we charged at a certain time, we do not allow discharging at that time in the next iterations
                if (chargeStep.chargeTimeStep_n != null) {
                    availableToDischarge[chargeStep.chargeTimeStep_n] = false;
                }
                else {
                    chargeStep.chargeTimeStep_n = cheapestTimeIdxsUpwardsSorted[0];
                }

                ChargingStep dischargeStep = ScheduleDischargingIteration( loadWithBES_kW, battery, cheapestTimeIdxsDownwardsSorted, timeStep_h, availableToDischarge);
                //System.out.printf("Discharge step: %s, power: %s kW %n", dischargeStep.chargeTimeStep_n, dischargeStep.addedPower_kW);
                //loadWithBES_kW = triple.getFirst();
                addedBESDischargePower_kW = dischargeStep.addedPower_kW; // triple.getSecond();
                Integer timeStepDischarge_n = dischargeStep.chargeTimeStep_n; // triple.getThird();
                // If we discharged at a certain time, we do not allow charging at that time in the next iterations
                if (dischargeStep.chargeTimeStep_n != null) {
                    availableToCharge[dischargeStep.chargeTimeStep_n] = false;
                }
                else {
                    dischargeStep.chargeTimeStep_n = cheapestTimeIdxsDownwardsSorted[cheapestTimeIdxsDownwardsSorted.length - 1];
                }

                if (chargeStep.addedPower_kW == 0 && dischargeStep.addedPower_kW == 0) {
                    boolean b_hasSOCmax = arrayMax(battery.SOC_kWh) >= 0.999 * battery.storageCapacity_kWh;
                    boolean b_hasSOCmin = arrayMin(battery.SOC_kWh) <= 0.001 * battery.storageCapacity_kWh;

                    if (b_hasSOCmax || b_hasSOCmin) {
                        skipSeperateChargeAndDischarge = true;
                    } else {
                        System.out.printf("Aborting while loop after %s iterations, no charging or discharging opportunity found and battery not full or empty", i);
                        // No charging or discharging opportunity found, break the loop
                        break;
                    }
                    
                }
                if (addedBESChargePower_kW > 0 ) {
                    lastChargePrice_eurpMWh = localMarginalPriceCurveUpwards_eurpMWh[chargeStep.chargeTimeStep_n];
                }
                if (addedBESDischargePower_kW > 0) {
                    lastDischargePrice_eurpMWh = localMarginalPriceCurveDownwards_eurpMWh[dischargeStep.chargeTimeStep_n];
                }   

                if (lastDischargePrice_eurpMWh - lastChargePrice_eurpMWh < priceSpreadThreshold_eurpMWh) {
                    boolean b_hasSOCmax = arrayMax(battery.SOC_kWh) >= 0.999 * battery.storageCapacity_kWh;
                    boolean b_hasSOCmin = arrayMin(battery.SOC_kWh) <= 0.001 * battery.storageCapacity_kWh;
                    
                    if (b_hasSOCmax || b_hasSOCmin) {
                        skipSeperateChargeAndDischarge = true;

                    } else {
                        System.out.printf("Aborting while loop after %s iterations, price spread too small and battery not full or empty", i);
                        // No charging or discharging opportunity found, break the loop
                        break;
                    }
                }

            }
            /*if (skipSeperateChargeAndDischarge){
                System.out.printf("Skipping separate charge and discharge, using single iteration method");
                break;
            }*/
            
            if (skipSeperateChargeAndDischarge){
                boolean b_hasSOCmax = (maxInRange(battery.SOC_kWh, 0, indexScheduleBefore) >= 0.999 * battery.storageCapacity_kWh);
                boolean b_hasSOCmin = (minInRange(battery.SOC_kWh, 0, indexScheduleBefore) <= 0.001 * battery.storageCapacity_kWh);

                boolean startWithCharge = true;

                if (b_hasSOCmax && b_hasSOCmin) {
                    // We create an array of indices where the SOC is maximal/minimal, then store the last index
                    int idxSOCmax = 0;
                    for (int j = 0; j < indexScheduleBefore; j++) {
                        if (battery.SOC_kWh[j] >= 0.999 * battery.storageCapacity_kWh) {
                            idxSOCmax = j;
                        }
                    }
                    int idxSOCmin = 0;
                    for (int j = 0; j < indexScheduleBefore; j++) {
                        if (battery.SOC_kWh[j] <= 0.001 * battery.storageCapacity_kWh) {
                            idxSOCmin = j;
                        }
                    }
                    startWithCharge = idxSOCmax < idxSOCmin;
                } else if (b_hasSOCmin) {
                    startWithCharge = true;
                } else if (b_hasSOCmax) {
                    startWithCharge = false;
                }
                   
                availableToCharge = Arrays.copyOfRange(availableToCharge, 0, indexScheduleBefore);
                availableToDischarge = Arrays.copyOfRange(availableToDischarge, 0, indexScheduleBefore);
                
                double[] loadWithBESpartial_kW = Arrays.copyOfRange(loadWithBES_kW, 0, indexScheduleBefore);
                if (separateMarketAndCongestion) {
                    localMarginalPriceCurveUpwards_eurpMWh = market.getMarginalPriceCurveUpwards(subtractArrays(loadWithBESpartial_kW, Arrays.copyOfRange(previousLoadProfile_kW, 0, indexScheduleBefore)), loadWithBESpartial_kW);
                    localMarginalPriceCurveDownwards_eurpMWh = market.getMarginalPriceCurveDownwards(subtractArrays(loadWithBESpartial_kW, Arrays.copyOfRange(previousLoadProfile_kW, 0, indexScheduleBefore)), loadWithBESpartial_kW);
                }
                else {
                    localMarginalPriceCurveUpwards_eurpMWh = market.getMarginalPriceCurveUpwards(loadWithBESpartial_kW, null);
                    localMarginalPriceCurveDownwards_eurpMWh = market.getMarginalPriceCurveDownwards(loadWithBESpartial_kW, null);
                }

                cheapestTimeIdxsUpwardsSorted = argsort(localMarginalPriceCurveUpwards_eurpMWh);
                cheapestTimeIdxsDownwardsSorted = argsort(localMarginalPriceCurveDownwards_eurpMWh);

                                
                ChargingDischaringStep chargeDischargeStep = schedule_battery_discharging_and_charging_iteration(
                    loadWithBESpartial_kW,
                    battery,
                    localMarginalPriceCurveUpwards_eurpMWh,
                    localMarginalPriceCurveDownwards_eurpMWh,
                    cheapestTimeIdxsUpwardsSorted,
                    cheapestTimeIdxsDownwardsSorted,
                    timeStep_h,
                    availableToCharge,
                    availableToDischarge,
                    startWithCharge,
                    priceSpreadThreshold_eurpMWh
                );

                for (int j = 0; j < loadWithBESpartial_kW.length; j++) {
                    loadWithBES_kW[j] = loadWithBESpartial_kW[j];
                }   
                if (chargeDischargeStep.addedChargePower_kW > battery.minPower_kW * 0.01) { // why the 0.01??
                    availableToCharge[chargeDischargeStep.dischargeTimeStep_n] = false;
                    availableToDischarge[chargeDischargeStep.chargeTimeStep_n] = false;
                } else {
                    if (arrayMin(battery.SOC_kWh) < -0.0001*battery.storageCapacity_kWh || arrayMax(battery.SOC_kWh) > battery.storageCapacity_kWh * 1.00001) {
                        System.out.printf("%n Max SOC: " + arrayMax(battery.SOC_kWh)/battery.storageCapacity_kWh*100 + "%%, Min SOC: " + arrayMin(battery.SOC_kWh)/battery.storageCapacity_kWh*100 + "%%");
                        System.out.printf("Battery SoC lowest at: " + argsort(battery.SOC_kWh)[0] + ", highest at: " + argsort(battery.SOC_kWh)[battery.SOC_kWh.length - 1]); 
                    }
                    indexScheduleBefore --;
                    if (indexScheduleBefore < 2) {
                        //System.out.printf("Battery SOC end of day 1: %s kWh %n", battery.SOC_kWh[(int)Math.round(24/timeStep_h)-1]);
                        break;
                    }
                }
            }
            i++;
        }
        System.out.printf("Finished %s iterations: last price pread: %s eurpMWh, last charge price: %s eurpMWh, last discharge price: %s eurpMWh", 
                        i, lastDischargePrice_eurpMWh - lastChargePrice_eurpMWh, lastChargePrice_eurpMWh, lastDischargePrice_eurpMWh);

        //System.out.printf("Finished %s iterations %n", i);
        System.out.flush();
        return battery.profile_kW;
    }

    public static ChargingDischaringStep schedule_battery_discharging_and_charging_iteration(
        double[] loadWithBES_kW,
        BatteryAsset battery,
        double[] localMarginalPriceCurveUpwards_eurpMWh,
        double[] localMarginalPriceCurveDownwards_eurpMWh,
        int[] cheapestTimeIdxsUpwardsSorted,
        int[] cheapestTimeIdxsDownwardsSorted,
        double timeStep_h,
        boolean[] availableToCharge,
        boolean[] availableToDischarge,
        boolean startWithCharge,
        double priceSpreadThreshold_eurpMWh
    ) {
        ChargingStep chargeStep;
        ChargingStep dischargeStep;
        if (startWithCharge) {
            chargeStep = findBatteryChargingOpportunity(
                loadWithBES_kW,
                battery,
                cheapestTimeIdxsUpwardsSorted,
                timeStep_h,
                availableToCharge,
                null
            );
            if (chargeStep.chargeTimeStep_n == null) {
                return new ChargingDischaringStep(
                    null, // chargeTimeStep_n
                    null, // dischargeTimeStep_n
                    0.0,
                    0.0 // addedPower_kW
                );
            }

            dischargeStep = findBatteryDischargingOpportunity(
                loadWithBES_kW,
                battery,
                cheapestTimeIdxsDownwardsSorted,
                timeStep_h,
                availableToDischarge,
                chargeStep
            );
        } else {
            dischargeStep = findBatteryDischargingOpportunity(
                loadWithBES_kW,
                battery,
                cheapestTimeIdxsDownwardsSorted,
                timeStep_h,
                availableToDischarge,
                null
            );
            if (dischargeStep.chargeTimeStep_n == null) {
                return new ChargingDischaringStep(
                    null, // chargeTimeStep_n
                    null, // dischargeTimeStep_n
                    0.0,
                    0.0 // addedPower_kW
                );
            }

            chargeStep = findBatteryChargingOpportunity(
                loadWithBES_kW,
                battery,
                cheapestTimeIdxsUpwardsSorted,
                timeStep_h,
                availableToCharge,
                dischargeStep
            );
        }
        if (chargeStep.chargeTimeStep_n == null || dischargeStep.chargeTimeStep_n == null) {
            return new ChargingDischaringStep(
                null, // chargeTimeStep_n
                null, // dischargeTimeStep_n
                0.0, // addedPower_kW
                0.0
            );
        } else if (localMarginalPriceCurveDownwards_eurpMWh[dischargeStep.chargeTimeStep_n] - localMarginalPriceCurveUpwards_eurpMWh[chargeStep.chargeTimeStep_n] < priceSpreadThreshold_eurpMWh) {
            // If the price spread is too small, we do not charge or discharge
            return new ChargingDischaringStep(
                null, // chargeTimeStep_n
                null, // dischargeTimeStep_n
                0.0, // addedPower_kW
                0.0
            );
        }
        double internalChargePower_kW = Math.min(chargeStep.addedPower_kW*battery.etaCharge_fr, dischargeStep.addedPower_kW/battery.etaDischarge_fr);

        double addedChargePower_kW = internalChargePower_kW / battery.etaCharge_fr;
        double addedDischargePower_kW = internalChargePower_kW * battery.etaDischarge_fr;
        
        double dSoC_kWh = internalChargePower_kW * timeStep_h;

        loadWithBES_kW[chargeStep.chargeTimeStep_n] += addedChargePower_kW;
        battery.profile_kW[chargeStep.chargeTimeStep_n] += addedChargePower_kW;

        for (int i = chargeStep.chargeTimeStep_n; i < battery.SOC_kWh.length; i++) {
            battery.SOC_kWh[i] += dSoC_kWh;
        }

        loadWithBES_kW[dischargeStep.chargeTimeStep_n] -= addedDischargePower_kW;
        battery.profile_kW[dischargeStep.chargeTimeStep_n] -= addedDischargePower_kW;
        
        for (int i = dischargeStep.chargeTimeStep_n; i < battery.SOC_kWh.length; i++) {
            battery.SOC_kWh[i] -= dSoC_kWh;
        }
        
        return new ChargingDischaringStep(
            chargeStep.chargeTimeStep_n, // chargeTimeStep_n
            dischargeStep.chargeTimeStep_n, // dischargeTimeStep_n
            addedChargePower_kW, // addedChargePower_kW
            addedDischargePower_kW // addedDischargePower_kW
        );

    }

    public static ChargingStep ScheduleChargingIteration(
        double[] loadProfile_kW,
        BatteryAsset battery,
        int[] cheapestTimeIdxsUpwardsSorted,
        double timeStep_h,
        boolean[] availableToCharge
    ) {

        ChargingStep chargeStep = findBatteryChargingOpportunity(
            loadProfile_kW,
            battery,
            cheapestTimeIdxsUpwardsSorted,
            timeStep_h,
            availableToCharge,
            null
        );
        
        //Integer timeStepCharge_n = pair.getFirst();
        //Double addedBESChargePower_kW = pair.getSecond();
        
        if (chargeStep.chargeTimeStep_n != null) {   
            loadProfile_kW[chargeStep.chargeTimeStep_n] += chargeStep.addedPower_kW;
            battery.profile_kW[chargeStep.chargeTimeStep_n] += chargeStep.addedPower_kW;

            double dSOC_kWh = battery.etaCharge_fr * chargeStep.addedPower_kW * timeStep_h;
            for (int i = chargeStep.chargeTimeStep_n; i < battery.SOC_kWh.length; i++) {
                battery.SOC_kWh[i] += dSOC_kWh;
            }
        }   
        //return new Triple(loadProfile_kW, addedBESChargePower_kW, timeStepCharge_n); // no need to return the load profile, as it is modified in place (pass by reference)
        return chargeStep; // return the time step and the added power for further processing     
    }

    public static ChargingStep ScheduleDischargingIteration(
        double[] loadProfile_kW,
        BatteryAsset battery,
        int[] cheapestTimeIdxsDownwardsSorted,
        double timeStep_h,
        boolean[] availableToDischarge
    ) {

        ChargingStep dischargeStep = findBatteryDischargingOpportunity(
            loadProfile_kW,
            battery,
            cheapestTimeIdxsDownwardsSorted,
            timeStep_h,
            availableToDischarge, 
            null
        );
         
        if (dischargeStep.chargeTimeStep_n != null) {
            loadProfile_kW[dischargeStep.chargeTimeStep_n] -= dischargeStep.addedPower_kW;
            battery.profile_kW[dischargeStep.chargeTimeStep_n] -= dischargeStep.addedPower_kW;

            double dSOC_kWh = dischargeStep.addedPower_kW * timeStep_h / battery.etaDischarge_fr;
            for (int i = dischargeStep.chargeTimeStep_n; i < battery.SOC_kWh.length; i++) {
                battery.SOC_kWh[i] -= dSOC_kWh;
            }
        }
        return dischargeStep;     
    }

    public static ChargingStep findBatteryChargingOpportunity(
        double[] loadProfile_kW,
        BatteryAsset battery,
        int[] cheapestTimeIdxsSorted,
        double timeStep_h,
        boolean[] availableToCharge,
        ChargingStep dischargeStep
    ) {
        int i_charge = 0;

        double roomToCharge_kWh = battery.storageCapacity_kWh - maxInRange(battery.SOC_kWh, cheapestTimeIdxsSorted[i_charge], loadProfile_kW.length);

        if (dischargeStep!= null && dischargeStep.chargeTimeStep_n != null) {
            if (dischargeStep.chargeTimeStep_n < cheapestTimeIdxsSorted[i_charge]) {
                roomToCharge_kWh += dischargeStep.addedPower_kW * timeStep_h / battery.etaDischarge_fr;
            }
        }

        while (battery.profile_kW[cheapestTimeIdxsSorted[i_charge]] >= battery.maxPower_kW ||
                roomToCharge_kWh <= 0 ||
                !availableToCharge[cheapestTimeIdxsSorted[i_charge]]
               ) { // removed rounding to sixth decimal, java doesn't provide a round function like python, so we use the condition directly
            if (i_charge == loadProfile_kW.length -1) {
                return new ChargingStep(null, 0); // No charging opportunity found
            }
            i_charge++;
            roomToCharge_kWh = battery.storageCapacity_kWh - maxInRange(battery.SOC_kWh, cheapestTimeIdxsSorted[i_charge], loadProfile_kW.length);
            if (dischargeStep!= null && dischargeStep.chargeTimeStep_n != null) {
                if (dischargeStep.chargeTimeStep_n < cheapestTimeIdxsSorted[i_charge]) {
                    roomToCharge_kWh += dischargeStep.addedPower_kW * timeStep_h / battery.etaDischarge_fr;
                }
            }
        }
        double addedPower_kW = Math.min(Math.min(battery.minPower_kW, roomToCharge_kWh / timeStep_h / battery.etaCharge_fr), battery.maxPower_kW - battery.profile_kW[cheapestTimeIdxsSorted[i_charge]])    ;
        return new ChargingStep(cheapestTimeIdxsSorted[i_charge], addedPower_kW); // return the time step and the added power for further processing
    }
 
    public static ChargingStep findBatteryDischargingOpportunity(
        double[] loadProfile_kW,
        BatteryAsset battery,
        int[] cheapestTimeIdxsSorted,
        double timeStep_h,
        boolean[] availableToDischarge,
        ChargingStep chargeStep
    ) {
        int i_discharge = loadProfile_kW.length - 1;

        double roomToDischarge_kWh = minInRange(battery.SOC_kWh, cheapestTimeIdxsSorted[i_discharge], loadProfile_kW.length);

        if (chargeStep!= null && chargeStep.chargeTimeStep_n != null) {
            if (chargeStep.chargeTimeStep_n < cheapestTimeIdxsSorted[i_discharge]) {
                roomToDischarge_kWh += chargeStep.addedPower_kW * timeStep_h * battery.etaCharge_fr;
            }
        }

        while (battery.profile_kW[cheapestTimeIdxsSorted[i_discharge]] <= -battery.maxPower_kW ||
               roomToDischarge_kWh <= 0 ||
               !availableToDischarge[cheapestTimeIdxsSorted[i_discharge]]) { // removed rounding to 6th decimal, java doesn't provide a round function like python, so we use the condition directly
            
            if (i_discharge == 0) {
                return new ChargingStep(null, 0); // No charging opportunity found
            }
            i_discharge--;
            roomToDischarge_kWh = minInRange(battery.SOC_kWh, cheapestTimeIdxsSorted[i_discharge], loadProfile_kW.length);
            if (chargeStep!= null && chargeStep.chargeTimeStep_n != null) {
                if (chargeStep.chargeTimeStep_n < cheapestTimeIdxsSorted[i_discharge]) {
                    roomToDischarge_kWh += chargeStep.addedPower_kW * timeStep_h * battery.etaCharge_fr;
                }
            }
        }
        double addedPower_kW = Math.min(Math.min(battery.minPower_kW, roomToDischarge_kWh / timeStep_h * battery.etaDischarge_fr), battery.maxPower_kW + battery.profile_kW[cheapestTimeIdxsSorted[i_discharge]])    ;
        return new ChargingStep(cheapestTimeIdxsSorted[i_discharge], addedPower_kW); // return the time step and the added power for further processing
    }
}

