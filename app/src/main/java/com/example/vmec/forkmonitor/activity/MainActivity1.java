package com.example.vmec.forkmonitor.activity;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vmec.forkmonitor.Constants;
import com.example.vmec.forkmonitor.DrawActivity;
import com.example.vmec.forkmonitor.R;
import com.example.vmec.forkmonitor.event.GattCharacteristicReadEvent;
import com.example.vmec.forkmonitor.event.LocationPublishEvent;
import com.example.vmec.forkmonitor.event.TrackingDataChangeEvent;
import com.example.vmec.forkmonitor.preference.BooleanPreference;
import com.example.vmec.forkmonitor.preference.StringPreference;
import com.example.vmec.forkmonitor.service.TrackingService;
import com.example.vmec.forkmonitor.utils.DeviceUtils;
import com.example.vmec.forkmonitor.utils.PermissionUtils;
import com.example.vmec.forkmonitor.utils.StringUtils;
import com.example.vmec.forkmonitor.viewmodel.MainActivityViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

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
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int ALL_PERMISSION_REQUEST_CODE = 100;

    private String mLocationHistoryLog;
    private BooleanPreference mIsBluetoothTrackingEnabled;
    private BooleanPreference mIsLocationTrackingEnabled;
    private StringPreference mLastCharacteristicPreference;
    private StringPreference mBluetoothDeviceNamePreference;

    @BindView(R.id.txt_bluetooth_tracking_status) TextView mBluetoothTrackingStatusView;
    @BindView(R.id.txt_location_tracking_status) TextView mLocationTrackingStatusView;
    @BindView(R.id.txt_bluetooth_connection_status) TextView mBluetoothConnectionStatus;
    @BindView(R.id.txt_bluetooth_last_characteristic_msg) TextView mBluetoothLastCharacteristicMsgView;
    @BindView(R.id.txt_bluetooth_device_name) TextView mBluetoothDeviceNameView;
    @BindView(R.id.txt_location_history) EditText mLocationHistoryView;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        ButterKnife.bind(this);
        mLocationHistoryView.setKeyListener(null);

        final SharedPreferences sp = getSharedPreferences(Constants.PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mIsBluetoothTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_BLUETOOTH_TRACKING_ENABLED, false);
        mIsLocationTrackingEnabled = new BooleanPreference(sp, Constants.PREFERENCE_IS_LOCATION_TRACKING_ENABLED, false);
        mLastCharacteristicPreference = new StringPreference(sp, Constants.PREFERENCE_LAST_CHARACTERISTIC_MSG, StringUtils.EMPTY_STRING);
        mBluetoothDeviceNamePreference = new StringPreference(sp, Constants.PREFERENCE_BLUETOOTH_DEVICE_NAME, StringUtils.EMPTY_STRING);

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

    @Override protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void checkPermissions() {
        if(!PermissionUtils.hasPermissions(this, REQUIRED_PERMISSIONS)){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, ALL_PERMISSION_REQUEST_CODE);
        } else {
            startTrackingService();
        }
    }

//    private void startLocationTracking() {
//        mLocationManager.startTrackingLocation(this);
//    }

    private void startTrackingService() {
        final Intent trackingIntent = new Intent(this, TrackingService.class);
        startService(trackingIntent);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case ALL_PERMISSION_REQUEST_CODE: {
                Timber.d("Number of permissions %d", grantResults.length);
                // If request is cancelled, the result arrays are empty.
                //TODO: HARDCODED pls refactor
                if (grantResults.length == REQUIRED_PERMISSIONS.length
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("ALL permission granted");

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LocationPublishEvent event) {
        if(TextUtils.isEmpty(mLocationHistoryLog)) {
            mLocationHistoryLog = StringUtils.EMPTY_STRING;
        }

        final Date currentDate = new Date();
        mLocationHistoryLog = "[" + currentDate.toString() + "] - " + event.getLocation().toString()
                                + "\n\n"
                                + mLocationHistoryLog ;

        mLocationHistoryView.setText(mLocationHistoryLog);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TrackingDataChangeEvent event) {
        mBluetoothTrackingStatusView.setText(mIsBluetoothTrackingEnabled.get() ? "Enabled" : "Disabled");
        mLocationTrackingStatusView.setText(mIsLocationTrackingEnabled.get() ? "Enabled" : "Disabled");
        final String lastCharacteristicMsg = mLastCharacteristicPreference.get().trim();

        if(TextUtils.isEmpty(lastCharacteristicMsg)) {
            mBluetoothLastCharacteristicMsgView.setText("Empty string");
        } else {
            mBluetoothLastCharacteristicMsgView.setText(lastCharacteristicMsg);
        }
        mBluetoothDeviceNameView.setText(mBluetoothDeviceNamePreference.get());
    }
}
