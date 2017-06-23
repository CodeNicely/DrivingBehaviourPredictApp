package io.predict.example.journeys_app.helper;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.predict.example.journeys_app.helper.sqlite.DatabaseHandler;
import io.predict.example.journeys_app.location.models.LocationData;


/**
 * Created by meghal on 22/5/17.
 */

public class LocationService extends Service {
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    private SharedPrefs sharedPrefs;

    Intent intent;
    int counter = 0;
    private DatabaseHandler db;
    int delay = 0;
    int period = 1000;
    Timer timer;

    @Override
    public void onCreate() {
        super.onCreate();


        intent = new Intent(BROADCAST_ACTION);
        db = new DatabaseHandler(this);
        sharedPrefs = new SharedPrefs(this);

        sharedPrefs.setKeyDistractedTime(0);

        delay = 0; // delay for 5 sec.
        period = 1000; // repeat every sec.

        timer = new Timer();

    }

    @Override
    public void onStart(Intent intent, int startId) {

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        registerReceiver(new PhoneUnlockedReceiver(), new IntentFilter("android.intent.action.USER_PRESENT"));
        registerReceiver(new PhoneUnlockedReceiver(), new IntentFilter("android.intent.action.SCREEN_OFF"));

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location location) {


            if (sharedPrefs.isTripOngoing()) {

                if (location.hasSpeed() && location.getSpeed() > 0.0) {

                    Toast.makeText(LocationService.this, "Location has speed", Toast.LENGTH_SHORT).show();
                    List<LocationData> locationDataList=db.getAllLocationPoints(sharedPrefs.getCurrentJourneyId());
                    if (locationDataList.size() > 0) {
                        LocationData locationDataPrevious = locationDataList.
                                get(locationDataList.size() - 1);

                        if (locationDataPrevious.getLongitude() == location.getLongitude() &&
                                locationDataPrevious.getLatitude() == location.getLatitude()) {
                            // Do nothing

                        } else {

                            LocationData locationData = new LocationData(
                                    1,
                                    sharedPrefs.getCurrentJourneyId(),
                                    System.currentTimeMillis() / 1000,
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    location.getSpeed() * 18 / 5
                            );
                            db.addLocation(locationData);

                        }
                    } else {
                        LocationData locationData = new LocationData(
                                1,
                                sharedPrefs.getCurrentJourneyId(),
                                System.currentTimeMillis() / 1000,
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getSpeed() * 18 / 5
                        );
                        db.addLocation(locationData);

                    }


                } else {

                    Toast.makeText(LocationService.this, "This location has no speed", Toast.LENGTH_SHORT).show();

                }

            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

    public void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {

                System.out.println("Timer started");
                System.out.println("Distracted time");

//                Toast.makeText(LocationService.this, "Timer is tunning", Toast.LENGTH_SHORT).show();

                System.out.println("Distracted time: " +
                        String.valueOf(sharedPrefs.getDistractedTime()));
                List<LocationData> locationDataList=db.getAllLocationPoints(sharedPrefs.getCurrentJourneyId());
                if (locationDataList.size() > 0) {
                    System.out.println("Trip time: " +
                            String.valueOf((locationDataList.get(locationDataList.size() - 1).getTimestamp() - locationDataList.get(0).getTimestamp())));
                    sharedPrefs.setKeyDistractedTime(sharedPrefs.getDistractedTime() + 1);

                } else {
                    sharedPrefs.setKeyDistractedTime(0);

                }

            }
        }, delay, period);

    }

    public void stopTimer() {

//        Toast.makeText(LocationService.this, "Timer cancel", Toast.LENGTH_SHORT).show();
        System.out.println("Timer stopped");

        timer.cancel();

    }

    public static class MessageEvent {

        private boolean screenStart;

        public MessageEvent(boolean screenStart) {
            this.screenStart = screenStart;
        }

        public boolean isScreenStart() {
            return screenStart;
        }

        /* Additional fields if needed */
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

        /* Do something */
        if (event.isScreenStart()) {
            startTimer();
        } else {
            stopTimer();
        }


    }
}
