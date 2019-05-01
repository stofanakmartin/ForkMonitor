package com.forkmonitor.data.remote;



public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "http://178.128.204.187/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}