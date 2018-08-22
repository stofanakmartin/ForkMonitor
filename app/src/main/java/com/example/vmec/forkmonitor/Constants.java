package com.example.vmec.forkmonitor;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class Constants {

    //Second in millis
    public static final int SECOND_MS = 1000;

    public static final String BLUETOOTH_DEVICE_ADDRESS = "00:15:85:14:9C:09";
    public static final String BLUETOOTH_FORK_MONITOR_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final int BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS = SECOND_MS * 20;
}
