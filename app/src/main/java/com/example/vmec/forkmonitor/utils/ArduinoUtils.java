package com.example.vmec.forkmonitor.utils;

/**
 * Created by Stofanak on 16/09/2018.
 */
public class ArduinoUtils {
    public static int getBatteryPercentage(final int value, final int max, final int min) {
        final int diff = max - min;
        return Math.round(100 - (((max - value) / (float)diff) * 100));
    }
}
