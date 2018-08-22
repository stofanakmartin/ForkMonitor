package com.example.vmec.forkmonitor.event;

import android.location.Location;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class LocationPublishEvent {
    private Location location;

    public LocationPublishEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
