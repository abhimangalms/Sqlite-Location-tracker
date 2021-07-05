package com.abhimangalms.trackmylocation;

public class LocationModel {
    int id;
    String latitude;
    String longitude;

    public LocationModel(){}

    public LocationModel(int id, String latitude, String longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocationModel(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
