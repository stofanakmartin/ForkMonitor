package com.forkmonitor.preference;

import android.content.SharedPreferences;

/**
 * Created by Stofanak on 26/08/2018.
 */
public class StringPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final String defaultValue;

    public StringPreference(SharedPreferences preferences, String key) {
        this(preferences, key, null);
    }

    public StringPreference(SharedPreferences preferences, String key, String defaultValue) {
        this.preferences = preferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String get() {
        String result;
        try {
            result = preferences.getString(key, defaultValue);
        } catch (ClassCastException e) {
            result = defaultValue;
            delete();
        }
        return result;

    }

    public boolean isSet() {
        return preferences.contains(key);
    }

    public void set(String value) {
        preferences.edit().putString(key, value).apply();
    }

    public void append(String value) {
        String oldValue = preferences.getString(key, "");
        if(oldValue.length() > Integer.MAX_VALUE / 2) {
            oldValue = oldValue.substring(0, Integer.MAX_VALUE / 3);
        }
        final String newValue = value + "\n\n" + oldValue;
        preferences.edit().putString(key, newValue).apply();
    }

    public void delete() {
        preferences.edit().remove(key).apply();
    }
}
