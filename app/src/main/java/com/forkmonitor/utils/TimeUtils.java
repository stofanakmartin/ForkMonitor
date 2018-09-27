package com.forkmonitor.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Stofanak on 27/09/2018.
 */
public class TimeUtils {

    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.sssZ";
//    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static String getCurrentTimestampISO() {
        TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat(DATETIME_FORMAT_ISO);
        df.setTimeZone(tz);
        return df.format(new Date());
    }
}
