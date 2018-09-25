package com.forkmonitor.data.model;

/**
 * Created by Stofanak on 10/09/2018.
 */
public class DeviceConfig {
    String serial;
    String bleHwAddress;
    String bleName;

    public DeviceConfig(String serial, String bleHwAddress, String bleName) {
        this.serial = serial;
        this.bleHwAddress = bleHwAddress;
        this.bleName = bleName;
    }

    public String getSerial() {
        return serial;
    }

    public String getBleHwAddress() {
        return bleHwAddress;
    }

    public String getBleName() {
        return bleName;
    }
}
