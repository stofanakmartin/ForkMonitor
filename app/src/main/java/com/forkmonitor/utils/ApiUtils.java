package com.forkmonitor.utils;

import retrofit2.Response;

/**
 * Created by Stofanak on 17/09/2018.
 */
public class ApiUtils {

    public static boolean isCallSuccess(Response response) {
        int code = response.code();
        return (code >= 200 && code < 400);
    }
}
