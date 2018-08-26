package com.example.vmec.forkmonitor.preference;

import android.content.SharedPreferences;

/**
 * Created by Stofanak on 26/08/2018.
 */
public class BooleanPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final boolean defaultValue;

    public BooleanPreference(SharedPreferences preferences, String key) {
        this(preferences, key, false);
    }

    public BooleanPreference(SharedPreferences preferences, String key, boolean defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public boolean get() {
        boolean result;
        try {
            result = preferences.getBoolean(key, defaultValue);
        } catch (ClassCastException e) {
            result = defaultValue;
            delete();
        }
        return result;
    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void set(boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
