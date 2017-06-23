package io.predict.example.journeys_app.location;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.predict.PredictIO;
import io.predict.example.R;
import io.predict.example.journeys_app.helper.LocationService;
import io.predict.example.journeys_app.helper.SharedPrefs;
import io.predict.example.journeys_app.helper.sqlite.DatabaseHandler;
import io.predict.example.journeys_app.location.models.JourneyData;
import io.predict.example.journeys_app.location.models.LocationData;
import io.predict.example.journeys_app.trip_view.JourneysActivity;


public class NewLocationActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    // LogCat tag
    private static final String TAG = NewLocationActivity.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
//    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;
    private SharedPrefs sharedPrefs;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    // UI elements
    private TextView lblLocation;
    private Button btnShowLocation, btnStartLocationUpdates;
    private Button journeyListButton;
    private DatabaseHandler db;

    private TextView locationListTextView;
    private Button predictIoStatus;
    private TextView predictIoEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);
        startService(new Intent(this, LocationService.class));

        predictIoStatus=(Button)findViewById(R.id.predictIoStatus);

        predictIoEventList=(TextView)findViewById(R.id.predictIoEventList);

        predictIoEventList.setText("Predict IO events : \n\n");
        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);
        journeyListButton = (Button) findViewById(R.id.journeyList);

        locationListTextView = (TextView) findViewById(R.id.locationList);

        sharedPrefs = new SharedPrefs(this);


        db = new DatabaseHandler(this);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        // Show location button click listener
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        // Toggling the periodic location updates
        btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });

        journeyListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewLocationActivity.this, JourneysActivity.class);
                startActivity(intent);
            }
        });

        predictIoStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage(String.valueOf(PredictIO.getInstance(NewLocationActivity.this).getStatus()));
                EventBus.getDefault().post(new NewLocationActivity.PredictIoEvent(String.valueOf(PredictIO.getInstance(NewLocationActivity.this).getStatus())));

            }
        });



    }


    private void showMessage(String message ) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static class PredictIoEvent {

        private String message;

        public PredictIoEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        /* Additional fields if needed */
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PredictIoEvent predictIoEvent) {

        predictIoEventList.append("\n\n"+predictIoEvent.getMessage());


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        /*if (mGoogleApiClient.isConnected() && sharedPrefs.isTripOngoing()) {
            startLocationUpdates();
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_stop_location_updates));

        }*/

        if (mGoogleApiClient.isConnected()) {
            if (sharedPrefs.isTripOngoing()) {
                btnStartLocationUpdates
                        .setText(getString(R.string.btn_stop_location_updates));
                startService(new Intent(this, LocationService.class));

                startLocationUpdates();
            } else {
                btnStartLocationUpdates
                        .setText(getString(R.string.btn_start_location_updates));
                stopLocationUpdates();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

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
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            lblLocation.setText(latitude + ", " + longitude);

        } else {

            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Method to toggle periodic location updates
     */
    private void togglePeriodicLocationUpdates() {
        if (sharedPrefs.isTripOngoing()) {
            // Changing the button text

            List<LocationData> locationDataList = db.getAllLocationPoints(sharedPrefs.getCurrentJourneyId());


            if (locationDataList.size() > 1) {
                long travel_time = locationDataList.get(locationDataList.size() - 1).getTimestamp() - locationDataList.get(0).getTimestamp();

                db.endJourney(new JourneyData(sharedPrefs.getCurrentJourneyId(), travel_time, sharedPrefs.getDistractedTime(), 0, System.currentTimeMillis()));
                Toast.makeText(this, "Travel Time: " + String.valueOf(travel_time) +
                                "\nDistracted Time: " + String.valueOf(sharedPrefs.getDistractedTime()) +
                                "\nEnd Time: " + String.valueOf(System.currentTimeMillis())
                        , Toast.LENGTH_SHORT).show();
            }


            btnStartLocationUpdates
                    .setText(getString(R.string.btn_start_location_updates));

            sharedPrefs.setTripOngoing(false);

            // Starting the location updates


            Log.d(TAG, "Periodic location updates stopped!");
            sharedPrefs.setCurrentJourneyId(-1);
            stopLocationUpdates();
            sharedPrefs.setKeyDistractedTime(0);
            Intent intent = new Intent(this, JourneysActivity.class);
            startActivity(intent);
        } else {
            // Changing the button text
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_stop_location_updates));

            sharedPrefs.setTripOngoing(true);
            sharedPrefs.setKeyDistractedTime(0);
            EventBus.getDefault().post(new LocationService.MessageEvent(true));

            long id = db.addJourney(System.currentTimeMillis());
            sharedPrefs.setCurrentJourneyId(id);

            Toast.makeText(this, "Journey added:" + String.valueOf(id), Toast.LENGTH_SHORT).show();

            List<LocationData> locationDataList = db.getAllLocationPoints(sharedPrefs.getCurrentJourneyId());

            for (LocationData locationPoint : locationDataList) {

                db.deleteLocationPoint(locationPoint);

            }

            // Stopping the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");


        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates() {

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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(NewLocationActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }

        }
        // Once connected with google api, get the location
        displayLocation();

        if (sharedPrefs.isTripOngoing()) {
            startLocationUpdates();
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_stop_location_updates));
            startService(new Intent(this, LocationService.class));

        } else {
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_start_location_updates));
            stopLocationUpdates();

        }

/*        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }*/
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;


        // Displaying the new location on UI
        displayLocation();

        if (sharedPrefs.isTripOngoing()) {
            List<LocationData> locationDataList = db.getAllLocationPoints(sharedPrefs.getCurrentJourneyId());
            locationListTextView.setText("Trip_ID - Timestamp - Latitude - Longitude - Speed");

            for (LocationData locationPoint : locationDataList) {
                locationListTextView.append("\n\n - " +
                        locationPoint.getJourney_id() +
                        " - " + locationPoint.getTimestamp() +
                        " - " + locationPoint.getLatitude() +
                        " - " + locationPoint.getLongitude() +
                        " - " + locationPoint.getSpeed()
                );

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                        //Request location updates:
                    }

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "No permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }




}