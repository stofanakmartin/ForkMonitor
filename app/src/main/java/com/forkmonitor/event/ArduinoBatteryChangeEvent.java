package com.forkmonitor.event;

import com.forkmonitor.Constants;

/**
 * Created by Stofanak on 12/09/2018.
 */
public class ArduinoBatteryChangeEvent {
    private int batteryValue;

    public ArduinoBatteryChangeEvent(int batteryValue) {
        this.batteryValue = batteryValue;
    }

    public int getBatteryValue() {
        return batteryValue;
    }

    public boolean isLow() {
        return batteryValue > 0 && batteryValue < Constants.ARDUINO_LOW_BATTERY_LEVEL_VALUE;
    }
}
