package com.forkmonitor.data.remote;



public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "https://meciak.site/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}