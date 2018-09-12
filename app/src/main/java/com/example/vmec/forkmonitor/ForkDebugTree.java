package com.example.vmec.forkmonitor;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * Created by Stofanak on 12/09/2018.
 */
public class ForkDebugTree extends Timber.DebugTree {
    @Override protected void log(int priority, String tag, @NotNull String message, Throwable t) {
        super.log(priority, tag, message, t);

        //TODO: WRITE "BLE" logs into separate file
    }
}
