package com.forkmonitor.event;

/**
 * Created by Stofanak on 12/09/2018.
 */
public class BLEDataReceivedEvent {
    private int ultrasoundValue;
    private int arduinoBatteryLevel;

    public BLEDataReceivedEvent(int ultrasoundValue, int arduinoBatteryLevel) {
        this.ultrasoundValue = ultrasoundValue;
        this.arduinoBatteryLevel = arduinoBatteryLevel;
    }

    public int getUltrasoundValue() {
        return ultrasoundValue;
    }

    public int getArduinoBatteryLevel() {
        return arduinoBatteryLevel;
    }
}
