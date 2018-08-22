package com.example.vmec.forkmonitor.utils;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by Stofanak on 21/08/2018.
 */
public class StringUtils {
    public static final String EMPTY_STRING = "";
    public static final String SPACE_STRING = " ";
    public static final String NEW_LINE_STRING = "\n";
    public static final String ZERO_STRING = "0";
    public static final String DOT_STRING = ".";
    public static final String COMMA_STRING = ",";

    /**
     * Helper method which convert null string into empty string or returns passed string
     *
     * @param string
     * @return
     */
    public static String getString(String string) {
        if (TextUtils.isEmpty(string)) {
            return EMPTY_STRING;
        }
        return string;
    }

    public static String getString(Context context, int stringResID) {
        if (stringResID == 0) {
            return EMPTY_STRING;
        }
        return context.getString(stringResID);
    }

    public static int getStringMemoryConsumptionInBytes(final String string) {
        return 8 * (int) ((((string.length()) * 2) + 45) / 8);
    }
}
