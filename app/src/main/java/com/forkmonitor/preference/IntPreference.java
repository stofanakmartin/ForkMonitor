package com.forkmonitor.preference;

import android.content.SharedPreferences;

/**
 * Created by Stofanak on 26/08/2018.
 */
public class IntPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final int defaultValue;

    public IntPreference(SharedPreferences preferences, String key) {
        this(preferences, key, 0);
    }

    public IntPreference(SharedPreferences preferences, String key, int defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public int get() {
        int result;
        try {
            result = preferences.getInt(key, defaultValue);
        } catch (ClassCastException e) {
            result = defaultValue;
            delete();
        }
        return result;

    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void set(int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
