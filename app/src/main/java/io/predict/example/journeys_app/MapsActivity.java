/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.predict.example.journeys_app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.predict.example.R;
import io.predict.example.journeys_app.helper.Keys;
import io.predict.example.journeys_app.helper.sqlite.DatabaseHandler;
import io.predict.example.journeys_app.location.models.LocationData;

/**
 * This shows how to draw polylines on a map.
 */
public class MapsActivity extends AppCompatActivity
        implements OnSeekBarChangeListener, OnItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {

    // City locations for mutable polyline.
   /* private static final LatLng ADELAIDE = new LatLng(-34.92873, 138.59995);
    private static final LatLng DARWIN = new LatLng(-12.4258647, 130.7932231);
    private static final LatLng MELBOURNE = new LatLng(-37.81319, 144.96298);
    private static final LatLng PERTH = new LatLng(-31.95285, 115.85734);
*/

  /*  private static final LatLng ADELAIDE = new LatLng(21.254730, 81.598485 );
    private static final LatLng DARWIN = new LatLng(21.254170, 81.600373  );
    private static final LatLng MELBOURNE =new LatLng(21.253190, 81.601081  );
    private static final LatLng PERTH =  new LatLng(21.254730, 81.598485 );
    private static final LatLng NIT =  new LatLng(21.247950, 81.603463 );
*/


    // Airport locations for geodesic polyline.
    private static final LatLng AKL = new LatLng(-37.006254, 174.783018);
    private static final LatLng JFK = new LatLng(40.641051, -73.777485);
    private static final LatLng LAX = new LatLng(33.936524, -118.377686);
    private static final LatLng LHR = new LatLng(51.471547, -0.460052);

    private static final int MAX_WIDTH_PX = 100;
    private static final int MAX_HUE_DEGREES = 360;
    private static final int MAX_ALPHA = 255;
    private static final int CUSTOM_CAP_IMAGE_REF_WIDTH_PX = 50;
    private static final int INITIAL_STROKE_WIDTH_PX = 1;

    private static final int PATTERN_DASH_LENGTH_PX = 50;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final Dot DOT = new Dot();
    private static final Dash DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final Gap GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_DOTTED = Arrays.asList(DOT, GAP);
    private static final List<PatternItem> PATTERN_DASHED = Arrays.asList(DASH, GAP);
    private static final List<PatternItem> PATTERN_MIXED = Arrays.asList(DOT, GAP, DOT, DASH, GAP);

    private Polyline mMutablePolyline;
    private SeekBar mHueBar;
    private SeekBar mAlphaBar;
    private SeekBar mWidthBar;
    private Spinner mStartCapSpinner;
    private Spinner mEndCapSpinner;
    private Spinner mJointTypeSpinner;
    private Spinner mPatternSpinner;
    private CheckBox mClickabilityCheckbox;
    private DatabaseHandler db;
    private TextView distractedTimeTextview;
    //Marker
    private GoogleMap mMap = null;
    private Marker mSelectedMarker;
    //    private SharedPrefs sharedPrefs;
    // These are the options for polyline caps, joints and patterns. We use their
    // string resource IDs as identifiers.
    private long journey_id = -1;
    private long travel_time = 0;
    private long distracted_time = 0;

    private static final int[] CAP_TYPE_NAME_RESOURCE_IDS = {
            R.string.cap_butt, // Default
            R.string.cap_round,
            R.string.cap_square,
            R.string.cap_image,
    };

    private static final int[] JOINT_TYPE_NAME_RESOURCE_IDS = {
            R.string.joint_type_default, // Default
            R.string.joint_type_bevel,
            R.string.joint_type_round,
    };

    private static final int[] PATTERN_TYPE_NAME_RESOURCE_IDS = {
            R.string.pattern_solid, // Default
            R.string.pattern_dashed,
            R.string.pattern_dotted,
            R.string.pattern_mixed,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_journey);

        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                journey_id = getIntent().getExtras().getLong(Keys.KEY_JOURNEY_ID);
                travel_time = getIntent().getExtras().getLong(Keys.KEY_TRAVEL_TIME);
                distracted_time = getIntent().getExtras().getLong(Keys.KEY_DISTRACTED_TIME);

            } else {
                return;
            }

        } else {
            return;
        }

//        sharedPrefs = new SharedPrefs(this);

        db = new DatabaseHandler(this);

        distractedTimeTextview = (TextView) findViewById(R.id.distracted_time);

        distractedTimeTextview.setText(
                "Distracted Time: " + String.valueOf(distracted_time) + " seconds\n" +
                        "Travel Time: " + String.valueOf(travel_time) + " seconds"
        );

        mHueBar = (SeekBar) findViewById(R.id.hueSeekBar);
        mHueBar.setMax(MAX_HUE_DEGREES);
        mHueBar.setProgress(0);

        mAlphaBar = (SeekBar) findViewById(R.id.alphaSeekBar);
        mAlphaBar.setMax(MAX_ALPHA);
        mAlphaBar.setProgress(MAX_ALPHA);

        mWidthBar = (SeekBar) findViewById(R.id.widthSeekBar);
        mWidthBar.setMax(MAX_WIDTH_PX);
        mWidthBar.setProgress(MAX_WIDTH_PX / 2);

        mStartCapSpinner = (Spinner) findViewById(R.id.startCapSpinner);
        mStartCapSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getResourceStrings(CAP_TYPE_NAME_RESOURCE_IDS)));

        mEndCapSpinner = (Spinner) findViewById(R.id.endCapSpinner);
        mEndCapSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getResourceStrings(CAP_TYPE_NAME_RESOURCE_IDS)));

        mJointTypeSpinner = (Spinner) findViewById(R.id.jointTypeSpinner);
        mJointTypeSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getResourceStrings(JOINT_TYPE_NAME_RESOURCE_IDS)));

        mPatternSpinner = (Spinner) findViewById(R.id.patternSpinner);
        mPatternSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                getResourceStrings(PATTERN_TYPE_NAME_RESOURCE_IDS)));

        mClickabilityCheckbox = (CheckBox) findViewById(R.id.toggleClickability);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        new OnMapAndViewReadyListener(mapFragment, this);

        mapFragment.getMapAsync(this);

    }

    private String[] getResourceStrings(int[] resourceIds) {
        String[] strings = new String[resourceIds.length];
        for (int i = 0; i < resourceIds.length; i++) {
            strings[i] = getString(resourceIds[i]);
        }
        return strings;
    }

    @Override
    public void onMapReady(GoogleMap map) {

        // Override the default content description on the view, for accessibility mode.
        map.setContentDescription(getString(R.string.driving_behaviour));

        // A geodesic polyline that goes around the world.
/*        map.addPolyline(new PolylineOptions()
                .add(LHR, AKL, LAX, JFK, LHR)
                .width(INITIAL_STROKE_WIDTH_PX)
                .color(Color.BLUE)
                .geodesic(true)
                .clickable(mClickabilityCheckbox.isChecked()));*/

        // A simple polyline across Australia. This polyline will be mutable.
/*        int color = Color.HSVToColor(
                mAlphaBar.getProgress(), new float[]{mHueBar.getProgress(), 1, 1});*/

        ArrayList<Integer> v1 = new ArrayList<>();
        ArrayList<Double> latList = new ArrayList<>();
        ArrayList<Double> lonList = new ArrayList<>();
        final ArrayList<Float> speedList = new ArrayList<>();
        final ArrayList<Long> timeList = new ArrayList<>();

        List<LocationData> locationDataList = db.getAllLocationPoints(journey_id);
        for (LocationData locationPoint : locationDataList) {
            v1.add(locationPoint.getLocation_id());
            latList.add(locationPoint.getLatitude());
            lonList.add(locationPoint.getLongitude());
            speedList.add(locationPoint.getSpeed());
            timeList.add(locationPoint.getTimestamp());
//            Toast.makeText(this, "TimeStamp" + String.valueOf(locationPoint.getTimestamp()), Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Journey Id: " + String.valueOf(journey_id), Toast.LENGTH_SHORT).show();

      /*  Iterable<LatLng> var1= new Iterable<LatLng>() {
            @Override
            public Iterator<LatLng> iterator() {
                return null;
            }
        };
        var1.iterator();*/

      /*  mMutablePolyline = map.addPolyline(new PolylineOptions()
                .color(color)
                .width(mWidthBar.getProgress())
                .clickable(mClickabilityCheckbox.isChecked())
                .add(ADELAIDE,  DARWIN,MELBOURNE,PERTH,NIT));
              .addAll(var1)
            );
*/

        if (latList.size() <= 1) {
            Toast.makeText(this, "This journey has only 1 point", Toast.LENGTH_SHORT).show();
            return;
        }
        map.addPolyline(new PolylineOptions()
                .add(LHR, AKL, LAX, JFK, LHR)
                .width(INITIAL_STROKE_WIDTH_PX)
                .color(Color.BLUE)
                .geodesic(true)
                .clickable(mClickabilityCheckbox.isChecked()));

/*
        int color = Color.HSVToColor(
                mAlphaBar.getProgress(), new float[]{mHueBar.getProgress(), 1, 1});
*/

        for (int i = 0; i < (latList.size() - 1); i++) {

            int color = Color.BLACK;

            if (speedList.get(i) < 50) {
                color = Color.GREEN;
            } else if (speedList.get(i) > 50 && speedList.get(i) < 70) {
                color = Color.YELLOW;
            } else {
                color = Color.RED;
            }


            mMutablePolyline = map.addPolyline(new PolylineOptions()
                    .color(color)
                    .width(mWidthBar.getProgress())
                    .clickable(mClickabilityCheckbox.isChecked())
                    .add(new LatLng(latList.get(i), lonList.get(i)), new LatLng(latList.get(i + 1), lonList.get(i + 1))));
            Log.d("Latitude --------", String.valueOf(latList.get(i)));
        }

        mHueBar.setOnSeekBarChangeListener(this);
        mAlphaBar.setOnSeekBarChangeListener(this);
        mWidthBar.setOnSeekBarChangeListener(this);

        mStartCapSpinner.setOnItemSelectedListener(this);
        mEndCapSpinner.setOnItemSelectedListener(this);
        mJointTypeSpinner.setOnItemSelectedListener(this);
        mPatternSpinner.setOnItemSelectedListener(this);

        mMutablePolyline.setStartCap(getSelectedCap(mStartCapSpinner.getSelectedItemPosition()));
        mMutablePolyline.setEndCap(getSelectedCap(mEndCapSpinner.getSelectedItemPosition()));
        mMutablePolyline.setJointType(getSelectedJointType(mJointTypeSpinner.getSelectedItemPosition()));
        mMutablePolyline.setPattern(getSelectedPattern(mPatternSpinner.getSelectedItemPosition()));

        // Move the map so that it is centered on the mutable polyline.


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latList.get(0), lonList.get(0)), 3));
        mMap = map;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mSelectedMarker = null;
            }
        });

        // Add a listener for polyline clicks that changes the clicked polyline's color.
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                // Flip the values of the red, green and blue components of the polyline's color.
                //  polyline.setColor(polyline.getColor() ^ 0x00ffffff);
                double latitude1 = polyline.getPoints().get(0).latitude;
                double longitude1 = polyline.getPoints().get(0).longitude;
                double latitude2 = polyline.getPoints().get(1).latitude;
                double longitude2 = polyline.getPoints().get(1).longitude;

                Float v1 = db.getSpeed(journey_id, latitude1, longitude1);
                Float v2 = db.getSpeed(journey_id, latitude2, longitude2);

                Long time1 = db.getTime(journey_id, latitude1, longitude1);
                Long time2 = db.getTime(journey_id, latitude2, longitude2);


                List<LocationData> locationDataList = db.getAllLocationPoints(journey_id);
                for (LocationData locationPoint : locationDataList) {

                    if (locationPoint.getLatitude() == latitude1 && locationPoint.getLongitude() == longitude1) {

                        v1 = locationPoint.getSpeed();
                        time1 = locationPoint.getTimestamp();
                    }

                    if (locationPoint.getLatitude() == latitude2 && locationPoint.getLongitude() == longitude2) {

                        v2 = locationPoint.getSpeed();
                        time2 = locationPoint.getTimestamp();
                    }


//            Toast.makeText(this, "Speed"+String.valueOf(locationPoint.getSpeed()), Toast.LENGTH_SHORT).show();
                }


                Float acceleration = (v2 - v1) / (time2 - time1);
                Toast.makeText(MapsActivity.this, "Speed: " + String.valueOf(v1) + " Km/hour \n Acceleration:" + String.valueOf(acceleration) + " meter/ s2", Toast.LENGTH_SHORT).show();

                addMarkersToMap(latitude1, longitude1, v1, acceleration);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.equals(mSelectedMarker)) {
                            // The showing info window has already been closed - that's the first thing to happen
                            // when any marker is clicked.
                            // Return true to indicate we have consumed the event and that we do not want the
                            // the default behavior to occur (which is for the camera to move such that the
                            // marker is centered and for the marker's info window to open, if it has one).
                            mSelectedMarker = null;
                            return true;
                        }

                        mSelectedMarker = marker;

                        // Return false to indicate that we have not consumed the event and that we wish
                        // for the default behavior to occur.
                        return false;
                    }
                });


            }
        });
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(latList.get(0), lonList.get(0)))
                .build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));


    }


    public void addMarkersToMap(Double latitude1, Double longitude1, float speed, float acceleration) {

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude1, longitude1))
                .title("Point")
                .snippet("Speed: " + speed + "km/hr" + "\n" +
                        "Acceleration:" + acceleration + "m/s^2"));
    }


    private Cap getSelectedCap(int pos) {

        switch (CAP_TYPE_NAME_RESOURCE_IDS[pos]) {
            case R.string.cap_butt:
                return new ButtCap();
            case R.string.cap_square:
                return new SquareCap();
            case R.string.cap_round:
                return new RoundCap();
            case R.string.cap_image:
                return new CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.chevron),
                        CUSTOM_CAP_IMAGE_REF_WIDTH_PX);
        }
        return null;
    }

    private int getSelectedJointType(int pos) {
        switch (JOINT_TYPE_NAME_RESOURCE_IDS[pos]) {
            case R.string.joint_type_bevel:
                return JointType.BEVEL;
            case R.string.joint_type_round:
                return JointType.ROUND;
            case R.string.joint_type_default:
                return JointType.DEFAULT;
        }
        return 0;
    }

    private List<PatternItem> getSelectedPattern(int pos) {
        switch (PATTERN_TYPE_NAME_RESOURCE_IDS[pos]) {
            case R.string.pattern_solid:
                return null;
            case R.string.pattern_dotted:
                return PATTERN_DOTTED;
            case R.string.pattern_dashed:
                return PATTERN_DASHED;
            case R.string.pattern_mixed:
                return PATTERN_MIXED;
            default:
                return null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getId()) {
            case R.id.startCapSpinner:
                mMutablePolyline.setStartCap(getSelectedCap(pos));
                break;
            case R.id.endCapSpinner:
                mMutablePolyline.setEndCap(getSelectedCap(pos));
                break;
            case R.id.jointTypeSpinner:
                mMutablePolyline.setJointType(getSelectedJointType(pos));
                break;
            case R.id.patternSpinner:
                mMutablePolyline.setPattern(getSelectedPattern(pos));
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Don't do anything here.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Don't do anything here.
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Don't do anything here.
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mMutablePolyline == null) {
            return;
        }

        if (seekBar == mHueBar) {
            mMutablePolyline.setColor(Color.HSVToColor(
                    Color.alpha(mMutablePolyline.getColor()), new float[]{progress, 1, 1}));
        } else if (seekBar == mAlphaBar) {
            float[] prevHSV = new float[3];
            Color.colorToHSV(mMutablePolyline.getColor(), prevHSV);
            mMutablePolyline.setColor(Color.HSVToColor(progress, prevHSV));
        } else if (seekBar == mWidthBar) {
            mMutablePolyline.setWidth(progress);
        }
    }

    /**
     * Toggles the clickability of the polyline based on the state of the View that triggered this
     * call.
     * This callback is defined on the CheckBox in the layout for this Activity.
     */
    public void toggleClickability(View view) {
        if (mMutablePolyline != null) {
            mMutablePolyline.setClickable(((CheckBox) view).isChecked());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }
}