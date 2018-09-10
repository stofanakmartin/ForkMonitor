package com.example.vmec.forkmonitor.data.remote;

import com.example.vmec.forkmonitor.data.model.Post;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {

    @POST("/fork/records/")
    @FormUrlEncoded
    Call<Post> savePost(@Field("name") String name,
                        @Field("lat") double lat,
                        @Field("lng") double lng,
                        @Field("battery") double battery,
                        @Field("accuracy") double accuracy,
                        @Field("status") int status,
                        @Field("ardDist") int ultrasoundDistance,
                        @Field("ardBat") int arduinoBatteryLevel);

}