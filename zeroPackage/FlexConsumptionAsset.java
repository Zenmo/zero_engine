package zeroPackage;

import java.util.Arrays;

public class FlexConsumptionAsset {
    public int length; // number of timesteps in the schedule
    public double maxPower_kW;
    public double power_steps_nr;
    public double minPower_kW;
    public double storageCapacity_kWh;
    public boolean[] allowedOperatingTimes;
    public double[] profile_kW;
    //public double[] yearProfile_kW;

    public FlexConsumptionAsset(double maxPower_kW, int powerSteps_nr, double timeStep_h, double length_h, boolean[] allowedOperatingTimes) {
        this.length = (int) Math.round(length_h / timeStep_h);
        this.maxPower_kW = maxPower_kW;
        this.power_steps_nr = powerSteps_nr;
        this.minPower_kW = maxPower_kW / powerSteps_nr;
        if (allowedOperatingTimes == null) {
            this.allowedOperatingTimes =  new boolean[this.length];
            Arrays.fill(this.allowedOperatingTimes, true);
        } else {
            this.allowedOperatingTimes = allowedOperatingTimes;
        }
        this.profile_kW = new double[this.length];
        //this.yearProfile_kW = new double[(int) Math.round(365 * 24 / timeStep_h)];
        
    }

    @Override
    public String toString() {
        return "max_power_kW: " + maxPower_kW + ", profile " + Arrays.toString(profile_kW);
    }

    //@Override
    public void reset(double initialSOC_fr) {
        this.profile_kW = new double[this.length];        
    }
}

