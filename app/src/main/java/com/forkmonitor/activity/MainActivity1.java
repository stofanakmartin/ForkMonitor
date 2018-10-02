package com.forkmonitor.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forkmonitor.Constants;
import com.forkmonitor.DeviceConfigManager;
import com.forkmonitor.DrawActivity;
import com.forkmonitor.FileLoggingTree;
import com.forkmonitor.R;
import com.forkmonitor.event.BLEConfigStatus;
import com.forkmonitor.event.TrackingDataChangeEvent;
import com.forkmonitor.preference.BooleanPreference;
import com.forkmonitor.preference.IntPreference;
import com.forkmonitor.preference.StringPreference;
import com.forkmonitor.service.TrackingService;
import com.forkmonitor.utils.ArduinoUtils;
import com.forkmonitor.utils.BluetoothUtils;
import com.forkmonitor.utils.DeviceUtils;
import com.forkmonitor.utils.LocationUtils;
import com.forkmonitor.utils.PermissionUtils;
import com.forkmonitor.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;

/**
 * Created by Stofanak on 14/08/2018.
 */
public class MainActivity1 extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int ALL_PERMISSION_REQUEST_CODE = 100;

//    private String mLocationHistoryLog;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private BooleanPreference mIsLocationTrackingEnabled;
    private StringPreference mLastCharacteristicPreference;
    private BooleanPreference mIsBluetoothDeviceConnectedPreference;
    private IntPreference mTruckLoadedStatePreference;
    private IntPreference mTruckStatusPreference;
    private StringPreference mBleHwAddressPreference;
    private StringPreference mBleNamePreference;
    private IntPreference mBleReadFailCounterPreference;
    private IntPreference mBleBatteryLevelPreference;
    private IntPreference mUltrasoundValuePreference;

    @BindView(R.id.txt_bluetooth_tracking_status) TextView mBluetoothTrackingStatusView;
    @BindView(R.id.txt_location_tracking_status) TextView mLocationTrackingStatusView;
    @BindView(R.id.txt_ultrasound_connection_status) TextView mUltrasoundConnectionStatus;
    @BindView(R.id.img_ultrasound_connection_status) ImageView mUltrasoundConnectionStatusIcon;
    @BindView(R.id.txt_bluetooth_last_characteristic_msg) TextView mBluetoothLastCharacteristicMsgView;
    @BindView(R.id.txt_bluetooth_device_name) TextView mBluetoothDeviceNameView;
    @BindView(R.id.txt_bluetooth_hw_address) TextView mBluetoothHwAddressView;
    @BindView(R.id.txt_truck_status) TextView mTruckStatusView;
    @BindView(R.id.txt_truck_loaded_state) TextView mTruckLoadedStateView;
    @BindView(R.id.txt_arduino_battery_level) TextView mArduinoBatteryLevelView;
    @BindView(R.id.view_bluetooth_status) LinearLayout mBluetoothStatusView;
    @BindView(R.id.view_gps_status) LinearLayout mGpsStatusView;
    @BindView(R.id.view_network_status) LinearLayout mNetworkStatusView;
    @BindView(R.id.img_arduino_battery_status) ImageView mArduinoBatteryStatusView;
    @BindView(R.id.txt_ultrasound_value) TextView mUltrasoundValueView;
    @BindView(R.id.img_ultrasound_status) ImageView mUltrasoundStatusView;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        ButterKnife.bind(this);
//        mLocationHistoryView.setKeyListener(null);

        final SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
        mLastCharacteristicPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mIsBluetoothDeviceConnectedPreference = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_DEVICE_CONNECTED, false);
        mTruckLoadedStatePreference = new IntPreference(sp, Constants.PREFERENCE_LAST_TRUCK_LOADED_STATE, Constants.TRUCK_STATUS_UNKNOWN);
        mTruckStatusPreference = new IntPreference(sp, Constants.PREFERENCE_LAST_STATUS, Constants.TRUCK_STATUS_UNKNOWN);
        mBleHwAddressPreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_HW_ADDRESS, StringUtils.EMPTY_STRING);
        mBleNamePreference = new StringPreference(sp, Constants.PREFERENCE_DEVICE_CONFIG_BLE_NAME, StringUtils.EMPTY_STRING);
        mBleReadFailCounterPreference = new IntPreference(sp, Constants.PREFERENCE_BLE_FAIL_READ_COUNT, 0);
        mBleBatteryLevelPreference = new IntPreference(sp, Constants.PREFERENCE_BLUETOOTH_BATTERY_LEVEL, Constants.BATTERY_VALUE_UNKWOWN);
        mUltrasoundValuePreference = new IntPreference(sp, Constants.PREFERENCE_ULTRASOUND_VALUE, 0);
        mTruckLoadedStatePreference.set(Constants.TRUCK_STATUS_UNKNOWN);
        mTruckStatusPreference.set(Constants.TRUCK_STATUS_UNKNOWN);
        mLastCharacteristicPreference.set(StringUtils.EMPTY_STRING);

        final DeviceConfigManager dcm = new DeviceConfigManager(this);
        dcm.initBluetoothConfiguration(this);

        setActivityTitle();

        checkPermissions();

        //TODO: Move to separate method
        if(DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.M)) {
            PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!mgr.isIgnoringBatteryOptimizations(getPackageName())) {
                final Intent intent = new Intent();
                intent.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    @Override protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void checkPermissions() {
        if(!PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS)){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, ALL_PERMISSION_REQUEST_CODE);
        } else {
            initFileLogging();
            tryStartTracking();
        }
    }

    private void startTrackingService() {
        final Intent trackingIntent = new Intent(this, TrackingService.class);
        startService(trackingIntent);
    }

    private void setActivityTitle() {
        final String appName = StringUtils.getString(this, R.string.app_name);
        setTitle(String.format("%s: %s", appName, mBleNamePreference.get()));
    }

    private void initFileLogging() {
        boolean hasFileLoggingTree = false;
        for (Timber.Tree tree: Timber.forest()) {
            if (tree instanceof FileLoggingTree) {
                hasFileLoggingTree = true;
                break;
            }
        }

        if (!hasFileLoggingTree) {
            Timber.plant(new FileLoggingTree(this));
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case ALL_PERMISSION_REQUEST_CODE: {
                Timber.d("Number of permissions %d", grantResults.length);
                // If request is cancelled, the result arrays are empty.
                boolean allPermsGranted = true;
                for(int result : grantResults) {
                    if(result == -1) {
                        allPermsGranted = false;
                    }
                }
                if (grantResults.length == REQUIRED_PERMISSIONS.length && allPermsGranted) {
                    Timber.d("ALL permission granted");

                    initFileLogging();
                    startTrackingService();

                } else {
                    Timber.d("ALL permission denied");

                    boolean somePermissionsForeverDenied = false;
                    for (String permission : REQUIRED_PERMISSIONS) {
                        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            somePermissionsForeverDenied = true;
                            break;
                        }
                    }

                    if(somePermissionsForeverDenied) {
                        showRedirectToSettingsDialog();
                    }
                }
            }
        }
    }

    private void showRedirectToSettingsDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Permissions Required")
                .setMessage("You have forcefully denied some of the required permissions " +
                        "for this action. Please open settings, go to permissions and allow them.")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @OnClick(R.id.btn_show_map)
    public void onShowMapClick() {
        final Intent intent = new Intent(this, DrawActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_tracking_toggle)
    public void onTrackingToggleClick() {
//        EventBus.getDefault().post(new TrackingEnableChangeEvent());
    }

    @OnClick(R.id.btn_ble_communication)
    public void onBleHistoryClick() {
        final Intent intent = new Intent(this, DrawActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TrackingDataChangeEvent event) {
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BLEConfigStatus event) {
        if(event.isSuccessfull()) {
            setActivityTitle();

        }
        updateUI();
    }

    private void updateUI() {
        mBluetoothTrackingStatusView.setText(mIsBluetoothTrackingEnabled.get() ? R.string.enabled : R.string.disabled);
        mLocationTrackingStatusView.setText(mIsLocationTrackingEnabled.get() ? R.string.enabled : R.string.disabled);
        final String lastCharacteristicMsg = mLastCharacteristicPreference.get().trim();

        mBluetoothLastCharacteristicMsgView.setText(lastCharacteristicMsg);

        if(TextUtils.isEmpty(mBleNamePreference.get())) {
            mBluetoothDeviceNameView.setText(R.string.error);
        } else {
            mBluetoothDeviceNameView.setText(mBleNamePreference.get());
        }

        if(TextUtils.isEmpty(mBleHwAddressPreference.get())) {
            mBluetoothHwAddressView.setText(R.string.error);
        } else {
            mBluetoothHwAddressView.setText(mBleHwAddressPreference.get());
        }

        final int truckLoadedState = mTruckLoadedStatePreference.get();
        final int truckStatus = mTruckStatusPreference.get();

        setTruckStateTextToView(truckStatus, mTruckStatusView);
        setTruckStateTextToView(truckLoadedState, mTruckLoadedStateView);

        updateUltrasoundConnectionStatusUI();
        updateBatteryUI();
        updateUltrasoundUI();

        if(BluetoothUtils.isBluetoothOn()) {
            mBluetoothStatusView.setBackgroundColor(getResources().getColor(R.color.status_ok));
        } else {
            mBluetoothStatusView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }

        if(LocationUtils.isGpsLocationEnabled(this)) {
            mGpsStatusView.setBackgroundColor(getResources().getColor(R.color.status_ok));
        } else {
            mGpsStatusView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }

        if(DeviceUtils.isConnectedToNetwork(this)) {
            mNetworkStatusView.setBackgroundColor(getResources().getColor(R.color.status_ok));
        } else {
            mNetworkStatusView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }

    private void tryStartTracking() {
        if(TextUtils.isEmpty(mBleNamePreference.get()) || TextUtils.isEmpty(mBleHwAddressPreference.get())) {
            Timber.e("Bluetooth config failed, cannot start tracking");
        } else {
            startTrackingService();
        }
    }

    private void setTruckStateTextToView(final int truckStatus, final TextView view) {
        switch (truckStatus) {
            case Constants.TRUCK_STATUS_LOADED:
                view.setText(R.string.truck_status_loaded);
                break;
            case Constants.TRUCK_STATUS_UNLOADED:
                view.setText(R.string.truck_status_unloaded);
                break;
            case Constants.TRUCK_STATUS_BLE_READ_FAILED:
                view.setText(getString(R.string.truck_status_ble_read_failed, mBleReadFailCounterPreference.get()));
                break;
            case Constants.STATUS_BLE_ULTRASOUND_FAIL:
                view.setText(R.string.status_ble_ultrasound_fail);
                break;
            case Constants.STATUS_BLUETOOTH_DEVICE_NOT_MATCH:
                view.setText(R.string.status_bluetooth_name_not_match);
                break;
            case Constants.STATUS_BLUETOOTH_CONFIG_FAILED:
                view.setText(R.string.status_bluetooth_config_failed);
                break;
            default:
                view.setText(R.string.truck_status_unknown);
        }
    }

    private void updateUltrasoundConnectionStatusUI() {
        if(mIsBluetoothDeviceConnectedPreference.get()) {
            mUltrasoundConnectionStatus.setText(R.string.bluetooth_status_connected);
        } else {
            mUltrasoundConnectionStatus.setText(R.string.bluetooth_status_disconnected);
        }
        setStatusIcon(mUltrasoundConnectionStatusIcon, mIsBluetoothDeviceConnectedPreference.get());
    }

    private void updateBatteryUI() {
        final int batteryValue = mBleBatteryLevelPreference.get();
        int ultrasoundBatteryPercentage = Constants.BATTERY_VALUE_UNKWOWN;
        if(batteryValue == Constants.BATTERY_VALUE_UNKWOWN) {
            mArduinoBatteryLevelView.setText(R.string.battery_value_unknown);
        } else {
            ultrasoundBatteryPercentage = ArduinoUtils.getBatteryPercentage(batteryValue, Constants.ARDUINO_MAX_BATTERY_LEVEL_VALUE, Constants.ARDUINO_LOW_BATTERY_LEVEL_VALUE);
            if(ultrasoundBatteryPercentage > 100) {
                ultrasoundBatteryPercentage = 100;
            }
            mArduinoBatteryLevelView.setText(String.format(Locale.US, "%d%%", ultrasoundBatteryPercentage));
        }

        Drawable statusDrawable;

        if (ultrasoundBatteryPercentage <= Constants.BATTERY_LOW_LEVEL_VALUE) {
            statusDrawable = getResources().getDrawable(R.drawable.ic_baseline_error_outline);
            DrawableCompat.setTint(statusDrawable, ContextCompat.getColor(this, R.color.status_not_ok));
            mArduinoBatteryStatusView.setImageDrawable(statusDrawable);
        } else {
            statusDrawable = getResources().getDrawable(R.drawable.ic_baseline_check);
            DrawableCompat.setTint(statusDrawable, ContextCompat.getColor(this, R.color.status_ok));
            mArduinoBatteryStatusView.setImageDrawable(statusDrawable);
        }
    }

    private void updateUltrasoundUI() {
        final int ultrasoundValue = mUltrasoundValuePreference.get();
        Drawable statusDrawable;
        if (ultrasoundValue <= 0) {

            if(ultrasoundValue == Constants.ULTRASOUND_VALUE_FAIL) {
                mUltrasoundValueView.setText("fail");
            } else {
                mUltrasoundValueView.setText(String.format(Locale.US, "%d", mUltrasoundValuePreference.get()));
            }
            statusDrawable = getResources().getDrawable(R.drawable.ic_baseline_error_outline);
            DrawableCompat.setTint(statusDrawable, ContextCompat.getColor(this, R.color.status_not_ok));
            mUltrasoundStatusView.setImageDrawable(statusDrawable);
        } else {
            mUltrasoundValueView.setText(String.format(Locale.US, "%d cm", ultrasoundValue));
            statusDrawable = getResources().getDrawable(R.drawable.ic_baseline_check);
            DrawableCompat.setTint(statusDrawable, ContextCompat.getColor(this, R.color.status_ok));
            mUltrasoundStatusView.setImageDrawable(statusDrawable);
        }
    }

    private void setStatusIcon(final ImageView view, final boolean isOk) {
        Drawable statusDrawable;
        if(isOk) {
            statusDrawable = getResources().getDrawable(R.drawable.ic_baseline_check);
            DrawableCompat.setTint(statusDrawable, ContextCompat.getColor(this, R.color.status_ok));
            view.setImageDrawable(statusDrawable);
        } else {
            statusDrawable = getResources().getDrawable(R.drawable.ic_baseline_error_outline);
            DrawableCompat.setTint(statusDrawable, ContextCompat.getColor(this, R.color.status_not_ok));
            view.setImageDrawable(statusDrawable);
        }
    }
}
