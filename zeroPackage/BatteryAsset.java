package zeroPackage;

import java.util.Arrays;

public class BatteryAsset {
    public int length; // number of timesteps in the schedule
    public double maxPower_kW;
    public double minPower_kW;
    public double storageCapacity_kWh;
    public double initialSOC_fr;
    public double initialSOC_kWh;
    public double etaCharge_fr;
    public double etaDischarge_fr;
    public boolean[] allowedOperatingTimes;
    public double[] profile_kW;
    public double[] yearProfile_kW;
    public double[] SOC_kWh;

    public BatteryAsset(double maxPower_kW, double batteryPowerStep_kW, double storageCapacity_kWh, double initialSOC_fr, double etaCharge_fr, double etaDischarge_fr, double timeStep_h, int length_h, boolean[] allowedOperatingTimes) {
        this.length = (int) Math.round(length_h / timeStep_h);
        this.maxPower_kW = maxPower_kW;
        this.minPower_kW = batteryPowerStep_kW;
        this.storageCapacity_kWh = storageCapacity_kWh;
        this.initialSOC_fr = initialSOC_fr;
        this.initialSOC_kWh = initialSOC_fr * storageCapacity_kWh;
        this.etaCharge_fr = etaCharge_fr;
        this.etaDischarge_fr = etaDischarge_fr;
        this.allowedOperatingTimes = (allowedOperatingTimes == null) ? new boolean[this.length] : allowedOperatingTimes;
        Arrays.fill(this.allowedOperatingTimes, true);
        this.profile_kW = new double[this.length];
        this.yearProfile_kW = new double[(int) Math.round(365 * 24 / timeStep_h)];
        this.SOC_kWh = new double[this.length];
        Arrays.fill(this.SOC_kWh, this.initialSOC_kWh);
    }

    @Override
    public String toString() {
        return "max_power_kW: " + maxPower_kW + ", profile " + Arrays.toString(profile_kW);
    }

    //@Override
    public void reset(double initialSOC_fr) {
        this.initialSOC_fr = initialSOC_fr;
        this.initialSOC_kWh = initialSOC_fr * this.storageCapacity_kWh;
        this.profile_kW = new double[this.length];
        this.SOC_kWh = new double[this.length];
        Arrays.fill(this.SOC_kWh, this.initialSOC_kWh);
    }
}

