package com.example.vmec.forkmonitor.event;

/**
 * Created by Stofanak on 29/08/2018.
 */
public class TrackingEnableChangeEvent {
    private boolean isEnabled;

    public TrackingEnableChangeEvent(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
