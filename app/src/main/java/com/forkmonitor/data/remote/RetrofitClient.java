package com.forkmonitor.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

//import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {
    /*
    private HttpLoggingInterceptor logging;

    logging = new HttpLoggingInterceptor();
    // set your desired log level
    logging;
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient.Builder client = new OkHttpClient.Builder();


    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
    // add your other interceptors …

    // add logging as last interceptor
    httpClient.addInterceptor(logging);
*/
    private static Retrofit retrofit = null;
/*
    public static Retrofit getClient(String baseUrl) {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    //.client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
*/
public static Retrofit getClient(String baseUrl){

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override public void log(String message) {
            Timber.tag("OkHttp").d(message);
        }
    });
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();


    if(retrofit==null){
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }
    return retrofit;
}

}
