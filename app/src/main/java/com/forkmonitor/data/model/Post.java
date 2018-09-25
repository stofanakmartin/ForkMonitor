package com.forkmonitor.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class Post {





        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("lat")
        @Expose
        private Double lat;
        @SerializedName("lng")
        @Expose
        private Double lng;
        @SerializedName("battery")
        @Expose
        private Double battery;
        @SerializedName("accuracy")
        @Expose
        private Double accuracy;
        @SerializedName("status")
        @Expose
        private Integer status;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }



        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLng() {
            return lng;
        }

        public void setLng(Double lng) {
            this.lng = lng;
        }

        public Double getBattery() {
            return battery;
        }

        public void setBattery(Double battery) {
            this.battery = battery;
        }

        public Double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(Double accuracy) {
            this.accuracy = accuracy;
        }

        public Integer getStatus() {
        return status;
    }

        public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Post{" +
                "name='" + name + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", battery=" + battery +
                ", accuracy=" + accuracy +
                ", status=" + status +
                '}';
    }


}
