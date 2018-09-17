package com.example.vmec.forkmonitor;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class Constants {

    //Second in millis
    public static final int SECOND_MS = 1000;
    public static final int BLUETOOTH_MAX_REQUEST_TIMEOUT_MS = SECOND_MS * 4;

    public static final String BLUETOOTH_DEVICE_NAME_SERVICE_UUID = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_DEVICE_NAME_CHARACTERISTIC_UUID = "00002a00-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_FORK_MONITOR_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_FORK_MONITOR_CHARACTERISTIC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String BLUETOOTH_CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String BLUETOOTH_DEVICE_COMMUNICATION_START_MSG = "sssta";
    public static final String BLUETOOTH_DEVICE_COMMUNICATION_END_MSG = "end";
    public static final int BLUETOOTH_READ_FAILURE_COUNT_LIMIT = 3;

    public static final int BLUETOOTH_CHARACTERISTIC_READ_INTERVAL_MS = SECOND_MS * 10;

    public static final String PREFERENCE_DEVICE_CONFIG_BLE_HW_ADDRESS = "device_config_ble_hw_address";
    public static final String PREFERENCE_DEVICE_CONFIG_BLE_NAME = "device_config_ble_name";
    public static final String PREFERENCES_FILE_NAME = "fork_monitor_pref";
    public static final String PREFERENCE_LAST_TRUCK_LOADED_STATE = "last_truck_loaded_state";
    public static final String PREFERENCE_LAST_STATUS = "last_truck_status";
    public static final String PREFERENCE_LAST_CHARACTERISTIC_MSG = "last_bluetooth_characteristic_msg";
    public static final String PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED = "is_bluetooth_tracking_enabled";
    public static final String PREFERENCE_IS_BLUETOOTH_DEVICE_CONNECTED = "is_bluetooth_device_connected";
    public static final String PREFERENCE_IS_LOCATION_TRACKING_ENABLED = "is_location_tracking_enabled";
    public static final String PREFERENCE_BLUETOOTH_BATTERY_LEVEL = "bluetooth_device_name";
    public static final String PREFERENCE_ULTRASOUND_VALUE = "ultrasound_value";
    public static final String PREFERENCE_BLE_COMMUNICATION_HISTORY = "ble_communication_history";
    public static final String PREFERENCE_LOCATION_HISTORY = "location_history";
    public static final String PREFERENCE_SEND_DATA_SUCCESS_COUNTER = "send_data_success_counter";
    public static final String PREFERENCE_SEND_DATA_ERROR_COUNTER = "send_data_error_counter";
    public static final String PREFERENCE_BLE_FAIL_READ_COUNT = "bluetooth_fail_read_counter";
    public static final String PREFERENCE_BLE_SUCCESS_READ_TOTAL_COUNT = "bluetooth_success_read_total_counter";
    public static final String PREFERENCE_BLE_FAIL_READ_TOTAL_COUNT = "bluetooth_fail_read_total_counter";
    public static final String PREFERENCE_BLE_ULTRASOUND_FAIL_TOTAL_COUNT = "bluetooth_ultrasound_total_counter";

    public static final int SEND_REQUEST_RETRY_INTERVAL = 7000;
    public static final int SEND_REQUEST_RETRY_ATTEMPTS = 5;
    public static final int LOCATION_INTERVAL = 3000;
    public static final int LOCATION_FASTEST_INTERVAL = 3000;

    public static final int BATTERY_LOW_LEVEL_VALUE = 20;
    public static final int LOCATION_ACCURACY_TOLERANCE = 20;
    public static final int ULTRASOUND_LOADED_UNLOADED_THRESHOLD_VALUE = 100;
    public static final int PHONE_LOW_BATTERY_LEVEL_VALUE = 20;
    public static final int PHONE_FULL_BATTERY_LEVEL_VALUE = 90;
    public static final int ARDUINO_LOW_BATTERY_LEVEL_VALUE = 3300;
    public static final int ARDUINO_MAX_BATTERY_LEVEL_VALUE = 4200;

    public static final int ULTRASOUND_VALUE_UNKWOWN = -1;
    public static final int ULTRASOUND_VALUE_FAIL = -2;
    public static final int ULTRASOUND_VALUE_PARSE_ERROR = -3;
    public static final int ULTRASOUND_VALUE_NOT_RECEIVED_ERROR = -4;
    public static final int BATTERY_VALUE_UNKWOWN = -1;
    public static final int BATTERY_VALUE_PARSE_ERROR = -2;
    public static final int BATTERY_VALUE_NOT_RECEIVED_ERROR = -3;

    public static final int REPORT_STATUS_LOCATION_POLYGON_CHANGE = 1;
    public static final int REPORT_STATUS_BLUETOOTH_LOADED_STATE_CHANGE = 2;
    public static final int REPORT_STATUS_BLUETOOTH_READ_FAIL = 3;

    public static final int TRUCK_STATUS_LOADED = 1;
    public static final int TRUCK_STATUS_UNLOADED = 2;
    public static final int TRUCK_STATUS_BLE_READ_FAILED = 3;
    public static final int TRUCK_STATUS_UNKNOWN = 4;

    public static final int STATUS_BLE_ULTRASOUND_FAIL = 13;
    public static final int STATUS_BLUETOOTH_DEVICE_NOT_MATCH = 14;
    public static final int STATUS_BLUETOOTH_CONFIG_FAILED = 15;

    public static final int NOTIFICATION_ID_PHONE_BATTERY = 10001;
    public static final int NOTIFICATION_ID_ARDUINO_LOW_BATTERY = 10002;

    /*status==
     * 1-nalozeny
     * 2-vylozeny
     * 3-nabija
     * 4-servis
     * 5-necinnost
     * */
}
