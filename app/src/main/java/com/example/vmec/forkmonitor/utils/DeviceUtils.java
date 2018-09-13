package com.example.vmec.forkmonitor.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
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

    /**
     * Checking for all possible internet providers
     **/
    public static boolean isConnectedToNetwork(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (DeviceUtils.isMinimumApiVersion(Build.VERSION_CODES.LOLLIPOP)) {
            final Network[] networks = connectivityManager.getAllNetworks();
            for(Network mNetwork : networks) {
                final NetworkInfo networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo != null && networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        } else {
            if(connectivityManager != null) {
                //noinspection deprecation
                final NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
                if(info != null) {
                    for(NetworkInfo anInfo : info) {
                        if(anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
