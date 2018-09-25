package com.forkmonitor.event;

/**
 * Created by Stofanak on 26/08/2018.
 */
public class GattCharacteristicNotificationConfigEvent {
    private int status;

    public GattCharacteristicNotificationConfigEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
