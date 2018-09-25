package com.forkmonitor.helper;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Stofanak on 19/08/2018.
 */
public class WakeLockHelper {
    private static final String WAKELOCK_NAME = WakeLockHelper.class.toString();
    private static volatile PowerManager.WakeLock lockStatic;

    synchronized public static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr =
                    (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_NAME);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }
}