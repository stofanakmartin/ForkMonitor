package com.example.vmec.forkmonitor.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

/**
 * Created by Stofanak on 19/08/2018.
 */
public class DeviceUtils {

    public static boolean isMinimumApiVersion(int requiredApiLevel) {
        return android.os.Build.VERSION.SDK_INT >= requiredApiLevel;
    }

    public static boolean isDeviceScreenOn(final Context context) {
        // If you use less than API20:
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (isMinimumApiVersion(Build.VERSION_CODES.KITKAT_WATCH)) {
            return powerManager.isInteractive();
        }
        return powerManager.isScreenOn();
    }

    public static String getCurrentAppVersion(final Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (null != packageInfo) {
            return packageInfo.versionName;
        }

        return "";
    }
}
