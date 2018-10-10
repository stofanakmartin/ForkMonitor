package com.forkmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.forkmonitor.data.model.DeviceConfig;
import com.forkmonitor.event.BLEConfigStatus;
import com.forkmonitor.preference.IntPreference;
import com.forkmonitor.preference.StringPreference;
import com.forkmonitor.utils.JsonUtils;
import com.forkmonitor.utils.StringUtils;
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
    private IntPreference mStatusPreference;

    public DeviceConfigManager(final Context context) {
        final SharedPreferences sp = context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mBleHwAddressPreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_HW_ADDRESS, StringUtils.EMPTY_STRING);
        mBleNamePreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_NAME, StringUtils.EMPTY_STRING);
        mStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_STATUS, Constants.STATUS_NOT_INITIALIZED);
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
            mStatusPreference.set(Constants.STATUS_BLUETOOTH_CONFIG_FAILED);
            status = new BLEConfigStatus(false);
            Timber.e("Device configuration FAILED. Bluetooth configuration not complete");
        } else {
            status = new BLEConfigStatus(true);
        }

        EventBus.getDefault().post(status);
    }
}
