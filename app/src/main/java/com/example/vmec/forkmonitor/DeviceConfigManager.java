package com.example.vmec.forkmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.vmec.forkmonitor.data.model.DeviceConfig;
import com.example.vmec.forkmonitor.event.BLEConfigStatus;
import com.example.vmec.forkmonitor.preference.IntPreference;
import com.example.vmec.forkmonitor.preference.StringPreference;
import com.example.vmec.forkmonitor.utils.JsonUtils;
import com.example.vmec.forkmonitor.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Stofanak on 10/09/2018.
 */
public class DeviceConfigManager {

    private StringPreference mBleHwAddressPreference;
    private StringPreference mBleNamePreference;
    private IntPreference mTruckStatusPreference;

    public DeviceConfigManager(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mBleHwAddressPreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_HW_ADDRESS, StringUtils.EMPTY_STRING);
        mBleNamePreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_NAME, StringUtils.EMPTY_STRING);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_STATUS, Constants.TRUCK_STATUS_UNKNOWN);
    }

    public void initBluetoothConfiguration(final Context context) {
        final String deviceConfigJson = JsonUtils.loadJSONFromAsset(context, R.raw.deviceconfigs);
        final Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<DeviceConfig>>(){}.getType();

        final List<DeviceConfig> devicesConfigs = gson.fromJson(deviceConfigJson, listType);
        final String phoneSerial = android.os.Build.SERIAL;

        for (DeviceConfig config : devicesConfigs) {
            if(config.getSerial().equalsIgnoreCase(phoneSerial)) {
                mBleHwAddressPreference.set(config.getBleHwAddress());
                mBleNamePreference.set(config.getBleName());
                break;
            }
        }

        BLEConfigStatus status;
        if(TextUtils.isEmpty(mBleNamePreference.get()) || TextUtils.isEmpty(mBleHwAddressPreference.get())) {
            mTruckStatusPreference.set(Constants.STATUS_BLUETOOTH_CONFIG_FAILED);
            status = new BLEConfigStatus(false);
            Timber.e("Device configuration FAILED. Bluetooth configuration not complete");
        } else {
            status = new BLEConfigStatus(true);
        }

        EventBus.getDefault().post(status);
    }
}
