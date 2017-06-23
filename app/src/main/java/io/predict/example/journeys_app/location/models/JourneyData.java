package io.predict.example.journeys_app.location.models;

/**
 * Created by meghal on 25/5/17.
 */

public class JourneyData {

    private long journey_id;
    private long travel_time;
    private long distracted_time;
    private long journey_start__date_time;
    private long journey_end_date_time;


    public JourneyData() {

    }

    public JourneyData(long trip_id, long travel_time, long distracted_time, long journey_start__date_time, long journey_end_date_time) {
        this.journey_id = trip_id;
        this.travel_time = travel_time;
        this.distracted_time = distracted_time;
        this.journey_start__date_time = journey_start__date_time;
        this.journey_end_date_time = journey_end_date_time;
    }

    public long getJourney_id() {
        return journey_id;
    }

    public long getTravel_time() {
        return travel_time;
    }

    public long getDistracted_time() {
        return distracted_time;
    }

    public long getJourney_start__date_time() {
        return journey_start__date_time;
    }

    public long getJourney_end_date_time() {
        return journey_end_date_time;
    }

    public void setJourney_id(int journey_id) {
        this.journey_id = journey_id;
    }

    public void setTravel_time(long travel_time) {
        this.travel_time = travel_time;
    }

    public void setDistracted_time(long distracted_time) {
        this.distracted_time = distracted_time;
    }

    public void setJourney_start__date_time(long journey_start__date_time) {
        this.journey_start__date_time = journey_start__date_time;
    }

    public void setJourney_end_date_time(long journey_end_date_time) {
        this.journey_end_date_time = journey_end_date_time;
    }
}
