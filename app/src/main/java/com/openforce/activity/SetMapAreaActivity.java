package com.openforce.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.android.ui.IconGenerator;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.fragments.MapJobFragment;
import com.openforce.model.BoundingBoxCoordinate;
import com.openforce.model.Job;
import com.openforce.model.JobsResponse;
import com.openforce.providers.OpenforceSharedPreference;
import com.openforce.utils.Utils;
import com.openforce.views.MarkerView;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.openforce.utils.Utils.LONDON_LATITUDE;
import static com.openforce.utils.Utils.LONDON_LONGITUDE;

public class SetMapAreaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView landingMapView;

    private TextView ext_search,txt_miles;
    private RelativeLayout rl_range;
    GoogleMap mMap;
    private ImageView img_cross;
    private String str_place="";
    private SeekBar seekbar_miles;
    Double main_lat,main_long;
    private static final String TAG = "MapJobActivity";
    private List<Job> currentJobList = new ArrayList<>();

    Button rl_set;
    int circle_area=32000;
    private Job currentJob;

    private LocalDateTime currentSelectedDate = LocalDateTime.now();

    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_map_area);
//        Places.initialize(getApplicationContext(), "AIzaSyDm0lA48T0hLgSWLWyvCRbZ4TWKILtCyu0");
//        PlacesClient placesClient = Places.createClient(this);

//        Places.initialize(getApplicationContext(), "AIzaSyD0KPdQfstIY9QMPlGL37KFE_pbLO0lXyc");
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_search_key));
        PlacesClient placesClient = Places.createClient(this);

        if (!Places.isInitialized()) {
//            Places.initialize(getApplicationContext(), "AIzaSyD0KPdQfstIY9QMPlGL37KFE_pbLO0lXyc");

            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_search_key));
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(SetMapAreaActivity.this);

        OpenforceSharedPreference sharedPreference = OpenForceApplication.getInstance().getSharedPreference();
        currentJob = sharedPreference.getCurrentJob();
        intView();
    }

    private void intView(){

        ext_search=(TextView)findViewById(R.id.ext_search);
        rl_set=(Button) findViewById(R.id.rl_set);
        landingMapView = (MapView) findViewById(R.id.landing_map_view);

        img_cross=(ImageView)findViewById(R.id.img_cross);
        img_cross.setVisibility(View.GONE);

        txt_miles=(TextView)findViewById(R.id.txt_miles);
        seekbar_miles=(SeekBar)findViewById(R.id.seekbar_miles);
        rl_range=(RelativeLayout)findViewById(R.id.rl_range);




        img_cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ext_search.setText("");
                img_cross.setVisibility(View.GONE);
            }
        });

//        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
//                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME , Place.Field.LAT_LNG));
        /*AutocompleteFilter filter = new AutocompleteFilter.Builder()
                .setCountry("IN")
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        autocompleteFragment.setFilter(filter);*/


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                ext_search.setText(place.getName());
                str_place= (String) place.getName();
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                getCross();
                LatLng latLng = place.getLatLng();
                System.out.println("Place Latlong: " + latLng);

                Double lat=latLng.latitude;
                Double lng=latLng.longitude;
                main_lat=lat;
                main_long=lng;
                selectLocation(lat,lng);

            }
            @Override
            public void onError(Status status) {
                ext_search.setText(status.toString());
                str_place="";

            }
        });

        seekbar_miles.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Toast.makeText(getApplicationContext(),"seekbar progress: "+i, Toast.LENGTH_SHORT).show();


                double mile_range=i;
                double mile=mile_range/2;

                txt_miles.setText(String.valueOf(mile)+" miles");

                double area_metre=mile*1.60934*1000;

                circle_area=(int) Math.round(area_metre);;

                System.out.println("MilesValu");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                //  Toast.makeText(getApplicationContext(),"seekbar touch started!", Toast.LENGTH_SHORT).show();
                mMap.getUiSettings().setScrollGesturesEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //    Toast.makeText(getApplicationContext(),"seekbar touch stopped!", Toast.LENGTH_SHORT).show();
                mMap.getUiSettings().setScrollGesturesEnabled(true);

            }
        });
    }


    private void getCross(){
        if (!str_place.equalsIgnoreCase("")){
            img_cross.setVisibility(View.VISIBLE);
        }else {
            img_cross.setVisibility(View.GONE);
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap=googleMap;


        setupMap();

        rl_range.setVisibility(View.VISIBLE);
        rl_range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
            }
        });

    }

    private void setupMap() {

        if (ContextCompat.checkSelfPermission(SetMapAreaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.

                        Location location = task.getResult();
                        double ukLatitude = 51.509865;
                        double ukLongitude = -0.118092;
                        if (location != null) {
                            ukLatitude = location.getLatitude();
                            ukLongitude = location.getLongitude();

                        } else {
                            ukLatitude = 51.509865;
                            ukLongitude = -0.118092;
                        }
                        main_lat = ukLatitude;
                        main_long = ukLongitude;
                        LatLng latLng = new LatLng(ukLatitude, ukLongitude);
                        float bearing = 0;
                        float tilt = 0;
                        float zoom = 12;
                        if (circle_area <= 12000) {
                            zoom = 11;
                        } else if (circle_area <= 24000 && circle_area > 10000) {
                            zoom = 10;

                        } else if (circle_area > 24000 && circle_area < 50000) {
                            zoom = 9;
                        } else {
                            zoom = 8;
                        }
                        CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, bearing);
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location_drop);

                        int height = 100;
                        int width = 100;
                        Bitmap b = BitmapFactory.decodeResource(SetMapAreaActivity.this.getResources(), R.drawable.shadow_location);
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

                        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                                .title("Current Location")
                                .icon(smallMarkerIcon);

                        mMap.addMarker(markerOptions);

                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(new LatLng(ukLatitude, ukLongitude));
                        circleOptions.radius(circle_area);
                        circleOptions.fillColor(SetMapAreaActivity.this.getResources().getColor(R.color.circle_yellow));
                        circleOptions.strokeWidth(3);
                        circleOptions.strokeColor(SetMapAreaActivity.this.getResources().getColor(R.color.sun_yellow));
                        mMap.addCircle(circleOptions);

                        if (currentJob == null) {
                            SetMapAreaActivity.this.addPinsOnGoogleMap(currentSelectedDate);

                        }


                        rl_set.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                selectLocation(main_lat, main_long);


                            }
                        });


                    }
                }
            });
        }else {
            double ukLatitude = 51.509865;
            double ukLongitude = -0.118092;
            main_lat=ukLatitude;
            main_long=ukLongitude;
            LatLng latLng = new LatLng(ukLatitude, ukLongitude);
            float bearing = 0;
            float tilt = 0;
            float zoom = 12;
            if (circle_area<=12000){
                zoom=11;
            }
            else if (circle_area<=24000 && circle_area>10000){
                zoom=10;

            }else if (circle_area>24000 && circle_area<50000){
                zoom = 9;
            }else {
                zoom=8;
            }
            CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, bearing);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location_drop);

            int height = 100;
            int width = 100;
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.shadow_location);
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                    .title("Current Location")
                    .icon(smallMarkerIcon);

            mMap.addMarker(markerOptions);

            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(new LatLng(ukLatitude,ukLongitude));
            circleOptions.radius(circle_area);
            circleOptions.fillColor(getResources().getColor(R.color.circle_yellow));
            circleOptions.strokeWidth(3);
            circleOptions.strokeColor(getResources().getColor(R.color.sun_yellow));
            mMap.addCircle(circleOptions);

            if (currentJob == null) {
                addPinsOnGoogleMap(currentSelectedDate);

            }
            rl_set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    selectLocation(main_lat,main_long);


                }
            });
        }


    }

    private void selectLocation(Double lat, Double lng){
        mMap.clear();
        LatLng latLng = new LatLng(lat, lng);
        float bearing = 0;
        float tilt = 0;
        float zoom = 10;
        System.out.println("Total metre==="+circle_area);
        if (circle_area<=12000){
            zoom=11;
        }
        else if (circle_area<=24000 && circle_area>10000){
            zoom=10;

        }else if (circle_area>24000 && circle_area<50000){
            zoom = 9;
        }else {
            zoom=8;
        }
        CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, bearing);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

      //  // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location_drop);

        int height = 100;
        int width = 100;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.shadow_location);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location")
                .icon(smallMarkerIcon);
        mMap.addMarker(markerOptions);


        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(new LatLng(lat,lng));
        circleOptions.radius(circle_area);
        circleOptions.fillColor(getResources().getColor(R.color.circle_yellow));
        circleOptions.strokeWidth(3);
        circleOptions.strokeColor(getResources().getColor(R.color.sun_yellow));

        mMap.addCircle(circleOptions);
        if (currentJob == null) {
            addPinsOnGoogleMap(currentSelectedDate);

        }

    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(SetMapAreaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location location = task.getResult();

                        Toast.makeText(SetMapAreaActivity.this, String.valueOf(location.getLatitude() + " " + location.getLongitude()), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void addPinsOnGoogleMap(LocalDateTime localDateTime) {
        long timestamp = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        BoundingBoxCoordinate boundingBoxCoordinate = Utils
                .calculateBoundingBoxFromCoordinate(main_lat, main_long, circle_area);
        OpenForceApplication.getApiClient().getMapJobs(boundingBoxCoordinate, timestamp,
                new OnSuccessListener<JobsResponse>() {
                    @Override
                    public void onSuccess(JobsResponse jobsResponse) {
                        currentJobList = jobsResponse.getJobs();
                        SetMapAreaActivity.this.addJobToMap();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        Log.e(TAG, "Error retrieving jobs", error);
                    }
                }, SetMapAreaActivity.this);
    }

    private void addJobToMap() {
        for (int i = 0; i < currentJobList.size(); i++) {
            Job job = currentJobList.get(i);
            LatLng jobLocation = new LatLng(job.getLatitude(), job.getLongitude());
            MarkerView markerView = new MarkerView(SetMapAreaActivity.this);
            markerView.setNumberEmployees("x"+job.getRequiredEmployees());
            markerView.setRole(job.getJobRole().getName());
            IconGenerator generator = new IconGenerator(SetMapAreaActivity.this);
            generator.setBackground(SetMapAreaActivity.this.getDrawable(android.R.color.transparent));
            generator.setContentView(markerView);
            Bitmap icon = generator.makeIcon();
            MarkerOptions markerOptions = new MarkerOptions().position(jobLocation)
                    .title("").icon(BitmapDescriptorFactory.fromBitmap(icon)).anchor(0.1f, 1);
            Marker marker = mMap.addMarker(markerOptions);
            marker.setTag(i);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                System.out.println("MarkerName==="+marker);
                if (marker.getTitle().toString().equals("")){
                    int position = (int) marker.getTag();
                    Job job = currentJobList.get(position);
                    startActivity(JobActivity.getIntent(SetMapAreaActivity.this, job));

                }else if(marker.getTitle().equalsIgnoreCase("Current Location")){

                }

                return false;
            }
        });
    }
}
