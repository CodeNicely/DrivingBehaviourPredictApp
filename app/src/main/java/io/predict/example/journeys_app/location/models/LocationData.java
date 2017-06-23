package io.predict.example.journeys_app.location.models;

/**
 * Created by meghal on 21/5/17.
 */

public class LocationData {

    private int location_id;
    private long journey_id;
    private long timestamp;
    private double latitude;
    private double longitude;
    private float speed;


    public LocationData(int location_id, long journey_id, long timestamp, double latitude, double longitude, float speed) {
        this.location_id = location_id;
        this.journey_id = journey_id;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
    }

    public LocationData(){

    }

    public int getLocation_id() {
        return location_id;
    }

    public long getJourney_id() {
        return journey_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setLocation_id(int location_id) {
        this.location_id = location_id;
    }

    public void setJourney_id(int journey_id) {
        this.journey_id = journey_id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
