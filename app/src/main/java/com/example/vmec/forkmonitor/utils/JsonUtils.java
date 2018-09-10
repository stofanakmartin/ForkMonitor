package com.example.vmec.forkmonitor.utils;

import android.content.Context;

import com.example.vmec.forkmonitor.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Stofanak on 10/09/2018.
 */
public class JsonUtils {
    public static String loadJSONFromAsset(final Context context, final int jsonResId) {
        String json = null;
        try {
            InputStream is = context.getResources().openRawResource(jsonResId);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
