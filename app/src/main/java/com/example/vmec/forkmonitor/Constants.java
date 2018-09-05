package com.example.vmec.forkmonitor;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class Constants {

    //Second in millis
    public static final int SECOND_MS = 1000;
    public static final int BLUETOOTH_MAX_REQUEST_TIMEOUT_MS = SECOND_MS * 4;

    public static final String BLUETOOTH_DEVICE_NAME = "BLE05";
    public static final String BLUETOOTH_DEVICE_ADDRESS = "00:15:85:14:9C:09";
    public static final String BLUETOOTH_DEVICE_NAME_SERVICE_UUID = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_FORK_MONITOR_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String BLUETOOTH_DEVICE_COMMUNICATION_START_MSG = "sssta";
    public static final String BLUETOOTH_DEVICE_COMMUNICATION_END_MSG = "end";

    public static final int BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS = SECOND_MS * 10;

    public static final String PREFERENCES_FILE_NAME = "fork_monitor_pref";
    public static final String PREFERENCE_LAST_TRUCK_LOADED_STATE = "last_truck_loaded_state";
    public static final String PREFERENCE_LAST_TRUCK_STATUS = "last_truck_status";
    public static final String PREFERENCE_LAST_CHARACTERISTIC_MSG = "last_bluetooth_characteristic_msg";
    public static final String PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED = "is_bluetooth_tracking_enabled";
    public static final String PREFERENCE_IS_BLUETOOTH_DEVICE_CONNECTED = "is_bluetooth_device_connected";
    public static final String PREFERENCE_IS_LOCATION_TRACKING_ENABLED = "is_location_tracking_enabled";
    public static final String PREFERENCE_BLUETOOTH_DEVICE_NAME = "bluetooth_device_name";
    public static final String PREFERENCE_BLUETOOTH_BATTERY_LEVEL = "bluetooth_device_name";

    public static final int LOCATION_ACCURACY_TOLERANCE = 100;
    public static final int ULTRASOUND_LOADED_UNLOADED_THRESHOLD_VALUE = 100;
    public static final int ULTRASOUND_NOT_READ_VALUE = 1015;

    public static final int TRUCK_STATUS_NOT_INITIALIZED = -1;
    public static final int TRUCK_STATUS_LOADED = 1;
    public static final int TRUCK_STATUS_UNLOADED = 2;
    public static final int TRUCK_STATUS_ERROR_VALUE = 3;
    public static final int STATUS_CHARGING = 4;
    public static final int STATUS_SERVICE = 5;
    public static final int STATUS_IDLE = 6;

    /*status==
     * 1-nalozeny
     * 2-vylozeny
     * 3-nabija
     * 4-servis
     * 5-necinnost
     * */
}
