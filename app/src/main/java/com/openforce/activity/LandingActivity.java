package com.openforce.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.grpc.okhttp.internal.Util;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.maps.android.ui.IconGenerator;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.BoundingBoxCoordinate;
import com.openforce.model.Job;
import com.openforce.model.JobsResponse;
import com.openforce.utils.ApiClient;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.Utils;
import com.openforce.views.JobPreviewView;
import com.openforce.views.MarkerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.openforce.utils.Utils.LONDON_LATITUDE;
import static com.openforce.utils.Utils.LONDON_LONGITUDE;


public class LandingActivity extends AppCompatActivity {

    private static final String TAG = "LandingActivity";

    private MapView landingMapView;
    private Button signUpButton;
    private Button loginButton;
    private GoogleMap googleMap;
    private ViewGroup jobPreviewLayout;
    private JobPreviewView jobPreviewView;

    private List<Job> currentJobList = new ArrayList<>();

    public static Intent getIntentWithClearFlag(Context context) {
        Intent intent = new Intent(context, LandingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, LandingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        initView();

        landingMapView.onCreate(savedInstanceState);

        landingMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                LandingActivity.this.googleMap = googleMap;
                LandingActivity.this.setupMap();
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int position = (int) marker.getTag();
                        Job job = currentJobList.get(position);
                        LandingActivity.this.showJobPreview(job);
                        return false;
                    }
                });
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                LandingActivity.this.startActivity(LoginActivity.getIntent(LandingActivity.this));
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                LandingActivity.this.startActivity(SignupActivity.getIntent(LandingActivity.this));
            }
        });
        long timestamp = Utils.getTodayDate().getTimeInMillis();
        BoundingBoxCoordinate boundingBoxCoordinate = Utils
                .calculateBoundingBoxFromCoordinate(LONDON_LATITUDE, LONDON_LONGITUDE, 10000);
        ApiClient apiClient = OpenForceApplication.getApiClient();
        apiClient.getMapJobs(boundingBoxCoordinate, timestamp,
                new OnSuccessListener<JobsResponse>() {
                    @Override
                    public void onSuccess(JobsResponse jobsResponse) {
                        currentJobList = jobsResponse.getJobs();
                        if (googleMap != null) {
                            LandingActivity.this.addJobToMap();
                        }

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        Log.e(TAG, "Error retrieving jobs", error);
                    }
                }, this);

        jobPreviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                jobPreviewLayout.setVisibility(View.GONE);
            }
        });
        jobPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
            }
        });

    }

    private void showJobPreview(Job job) {
        jobPreviewLayout.setVisibility(View.VISIBLE);
        jobPreviewView.setAvailability(job.getRequiredEmployees() + "");
        jobPreviewView.setJobRole(job.getJobRole().getName());
        jobPreviewView.setStartDate(Utils.timestampToFormattedDate(job.getStartDate()));
        jobPreviewView.setEndDate(Utils.timestampToFormattedDate(job.getEndDate()));

        final Drawable placeHolder = Utils.getPlaceholderForProfile(job.getEmployerName(), this);
        jobPreviewView.setEmployerProfileImage(placeHolder);
        OpenForceApplication.getApiClient().getUserProfileImage(job.getEmployerID(), new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String publicIdProfileImage) {
                if (!TextUtils.isEmpty(publicIdProfileImage)) {
                    Transformation transformation = new Transformation();
                    transformation
                            .width(LandingActivity.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size))
                            .height(LandingActivity.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size));

                    String url = MediaManager.get().url().publicId(publicIdProfileImage)
                            .transformation(transformation).format("jpg").type("upload").generate();
                    jobPreviewView.setEmployerProfileImage(placeHolder, url);
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // nothing to do here
                Log.e(TAG, "Error retrieving user profile image", e);
            }
        }, this);
    }

    private void addJobToMap() {
        googleMap.clear();
        if(currentJobList != null) {
            for (int i = 0; i < currentJobList.size(); i++) {
                Job job = currentJobList.get(i);
                LatLng jobLocation = new LatLng(job.getLatitude(), job.getLongitude());
                MarkerView markerView = new MarkerView(this);
                markerView.setNumberEmployees("x"+job.getRequiredEmployees());
                markerView.setRole(job.getJobRole().getName());
                IconGenerator generator = new IconGenerator(this);
                generator.setBackground(getDrawable(android.R.color.transparent));
                generator.setContentView(markerView);
                Bitmap icon = generator.makeIcon();
                MarkerOptions markerOptions = new MarkerOptions().position(jobLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(icon)).anchor(0.1f, 1);
                Marker marker = googleMap.addMarker(markerOptions);
                marker.setTag(i);
            }
        }
    }

    private void setupMap() {
        LatLng latLng = new LatLng(LONDON_LATITUDE, LONDON_LONGITUDE);
        float bearing = 0;
        float tilt = 0;
        float zoom = 14;
        CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, bearing);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (jobPreviewLayout.getVisibility() == View.VISIBLE) {
            jobPreviewLayout.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        landingMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        landingMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        landingMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        landingMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        landingMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        landingMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        landingMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        landingMapView.onLowMemory();
    }

    private void initView() {
        landingMapView = findViewById(R.id.landing_map_view);
        signUpButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.login_button);
        jobPreviewLayout = findViewById(R.id.job_preview_layout);
        jobPreviewView = findViewById(R.id.job_preview);
    }
}
