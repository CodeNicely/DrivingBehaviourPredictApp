package io.predict.example.journeys_app.helper.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import io.predict.example.journeys_app.location.models.JourneyData;
import io.predict.example.journeys_app.location.models.LocationData;


/**
 * Created by meghal on 21/5/17.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "journey_manager";

    // Table names
    private static final String TABLE_LOCATION_POINTS = "location_new";
    private static final String TABLE_JOURNEY = "journey_new";

    // Location Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_TIMESTAMP = "timestamp";

    // Journey Table Columns names
    private static final String KEY_START_DATE_TIME = "start_date_time";
    private static final String KEY_END_DATE_TIME = "end_date_time";
    private static final String KEY_JOURNEY_ID = "journey_id";
    private static final String KEY_TRAVEL_TIME = "travel_time";
    private static final String KEY_DISTRACTED_TIME = "distracted_time";

    private String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATION_POINTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_JOURNEY_ID + " INTEGER,"
            + KEY_TIMESTAMP + " TEXT,"
            + KEY_LATITUDE + " TEXT,"
            + KEY_LONGITUDE + " TEXT,"
            + KEY_SPEED + " TEXT" + ")";

    private String CREATE_JOURNEY_TABLE = "CREATE TABLE " + TABLE_JOURNEY + "("
            + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TRAVEL_TIME + " TEXT,"
            + KEY_DISTRACTED_TIME + " TEXT,"
            + KEY_START_DATE_TIME + " TEXT,"
            + KEY_END_DATE_TIME + " TEXT" + ")";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_LOCATION_TABLE);
        db.execSQL(CREATE_JOURNEY_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_POINTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_JOURNEY);

        // Create tables again
        onCreate(db);
    }

    public long addJourney(long startTime) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_START_DATE_TIME, startTime);
        values.put(KEY_END_DATE_TIME, -1); // This means trip is on going right now.
        values.put(KEY_TRAVEL_TIME, 0);
        values.put(KEY_DISTRACTED_TIME, 0);

        long id = db.insert(TABLE_JOURNEY, null, values);
        db.close();

        return id;
    }

    public void endJourney(JourneyData journeyData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_END_DATE_TIME, journeyData.getJourney_end_date_time());
        values.put(KEY_TRAVEL_TIME, journeyData.getTravel_time());
        values.put(KEY_DISTRACTED_TIME, journeyData.getDistracted_time());

        // updating row
        db.update(TABLE_JOURNEY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(journeyData.getJourney_id())});
    }

    // Adding new location
    public void addLocation(LocationData locationData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_JOURNEY_ID, locationData.getJourney_id());
        values.put(KEY_TIMESTAMP, locationData.getTimestamp());
        values.put(KEY_LATITUDE, locationData.getLatitude());
        values.put(KEY_LONGITUDE, locationData.getLongitude());
        values.put(KEY_SPEED, locationData.getSpeed());

        // Inserting Row
        db.insert(TABLE_LOCATION_POINTS, null, values);
        db.close(); // Closing database connection
    }

    public LocationData getLocationPoint(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOCATION_POINTS, new String[]{KEY_ID,
                        KEY_LATITUDE, KEY_LONGITUDE, KEY_SPEED}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        assert cursor != null;
        // return contact
        return new LocationData(
                Integer.parseInt(cursor.getString(0)),
                Long.parseLong(cursor.getString(1)),
                Long.parseLong(cursor.getString(2)),
                Double.parseDouble(cursor.getString(3)),
                Double.parseDouble(cursor.getString(4)),
                Float.parseFloat(cursor.getString(5)));
    }


    public List<JourneyData> getAllJourneyPoints() {

        List<JourneyData> journeyDataList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_JOURNEY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JourneyData journeyData = new JourneyData();
                journeyData.setJourney_id(Integer.parseInt(cursor.getString(0)));
                journeyData.setTravel_time(Long.parseLong(cursor.getString(1)));
                journeyData.setDistracted_time(Long.parseLong(cursor.getString(2)));
                journeyData.setJourney_start__date_time(Long.parseLong(cursor.getString(3)));
                journeyData.setJourney_end_date_time(Long.parseLong(cursor.getString(4)));

                // Adding contact to list
                journeyDataList.add(journeyData);
            } while (cursor.moveToNext());
        }

        // return contact list
        return journeyDataList;
    }


    public List<LocationData> getAllLocationPoints(long journey_id) {

        List<LocationData> locationDataList = new ArrayList<LocationData>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_POINTS+" WHERE "
                +KEY_JOURNEY_ID+"="+journey_id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LocationData locationData = new LocationData();
                locationData.setLocation_id(Integer.parseInt(cursor.getString(0)));
                locationData.setJourney_id(Integer.parseInt(cursor.getString(1)));
                locationData.setTimestamp(Long.parseLong(cursor.getString(2)));
                locationData.setLatitude(Double.parseDouble(cursor.getString(3)));
                locationData.setLongitude(Double.parseDouble(cursor.getString(4)));
                locationData.setSpeed(Float.parseFloat(cursor.getString(5)));

                // Adding contact to list
                locationDataList.add(locationData);
            } while (cursor.moveToNext());
        }

        // return contact list
        return locationDataList;
    }


    public float getSpeed(long journey_id,double latitude, double longitude) {

        // Select All Query
        float speed = 100;
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_POINTS + " WHERE " +
                KEY_LATITUDE + "=" + String.valueOf(latitude) + " AND " +
                KEY_JOURNEY_ID + "=" + journey_id + " AND " +
                KEY_LONGITUDE + "=" + String.valueOf(longitude);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                speed = Float.parseFloat(cursor.getString(4));
//                locationData.setSpeed(Float.parseFloat(cursor.getString(3)));

                // Adding contact to list
            } while (cursor.moveToNext());
        }

        // return contact list
        return speed;
    }

    public Long getTime(long journey_id,double latitude, double longitude) {

        // Select All Query
        Long time = null;
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION_POINTS + " WHERE " +
                KEY_LATITUDE + "=" + String.valueOf(latitude) + " AND " +
                KEY_JOURNEY_ID + "=" + journey_id + " AND " +
                KEY_LONGITUDE + "=" + String.valueOf(longitude);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                time = Long.parseLong(cursor.getString(1));
//                locationData.setSpeed(Float.parseFloat(cursor.getString(3)));

                // Adding contact to list
            } while (cursor.moveToNext());
        }

        // return time list
        return time;
    }

    public void deleteLocationPoint(LocationData locationData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION_POINTS, KEY_ID + " = ?",
                new String[]{String.valueOf(locationData.getLocation_id())});
        db.close();
    }

}
