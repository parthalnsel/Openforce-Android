package com.openforce.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.maps.android.ui.IconGenerator;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.activity.JobActivity;
import com.openforce.activity.SetMapAreaActivity;
import com.openforce.adapters.DayAdapter;
import com.openforce.adapters.ListJobAdapter;
import com.openforce.model.BoundingBoxCoordinate;
import com.openforce.model.Job;
import com.openforce.model.JobsResponse;
import com.openforce.providers.OpenforceSharedPreference;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.ApiClient;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.UIUtils;
import com.openforce.utils.Utils;
import com.openforce.views.MarkerView;
import com.openforce.widget.TextDrawable;
import com.squareup.picasso.Picasso;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import static com.openforce.utils.Utils.LONDON_LATITUDE;
import static com.openforce.utils.Utils.LONDON_LONGITUDE;

public class MapJobFragment extends Fragment {

    private static final String TAG = "MapJobFragment";

    private static final int LOCATION_PERMISSION_REQUEST = 1000;

    private MapView mapView;
    private GoogleMap googleMap;
    private MapView landingMapView;
    private RecyclerView recyclerView;

    private ViewGroup inJobHeader;
    private TextView jobRoleTitle;
    private TextView employerNameTitle;
    private ImageView logoEmployer;
    private TextView exploringTitle;
    private ViewGroup header;
    private FloatingActionButton checkInButton;

    private ApiClient apiClient;
    private DayAdapter dayAdapter;


    private LocalDateTime currentSelectedDate = LocalDateTime.now();
    private Job currentJob;

    private List<Job> currentJobList = new ArrayList<>();
    private List<Job> currentDateJobList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private RelativeLayout rl_list,rl_map;
    private ImageView img_list,img_map;
    private RelativeLayout frame_list;

    private final int interval = 1000;
    private Handler handler = new Handler();

    private RelativeLayout rl_location;
    private LinearLayout ll_map;

    boolean flag = false;

    String currentDate=currentSelectedDate.plusDays(1).toLocalDate().toString();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenforceSharedPreference sharedPreference = OpenForceApplication.getInstance().getSharedPreference();
        currentJob = sharedPreference.getCurrentJob();
        apiClient = OpenForceApplication.getApiClient();
        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(getActivity());


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        landingMapView.onCreate(savedInstanceState);
        landingMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapJobFragment.this.googleMap = googleMap;
                MapJobFragment.this.setupMap();
                MapJobFragment.this.showUserLocation();
                // no need to load pins until the user selects a date which is not part of the current job

                Runnable runnable = new Runnable() {
                    public void run() {

                    }
                };

                handler.postAtTime(runnable, System.currentTimeMillis() + interval);
                handler.postDelayed(runnable, interval);

                if (currentJob == null) {
                    MapJobFragment.this.addPinsOnGoogleMap(currentSelectedDate.plusDays(1));

                }

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int position = (int) marker.getTag();
                        Job job = currentDateJobList.get(position);
                        MapJobFragment.this.startActivity(JobActivity.getIntent(MapJobFragment.this.getActivity(), job));
                        return false;
                    }
                } );
            }
        });
    }


    private void setEmployerImage() {
        final TextDrawable placeholderEmployer = Utils.getPlaceholderForProfile(currentJob.getEmployerName(), getActivity());
        logoEmployer.setImageDrawable(placeholderEmployer);
        apiClient.getUserProfileImage(currentJob.getEmployerID(), new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String publicIdProfileImage) {
                if (!TextUtils.isEmpty(publicIdProfileImage)) {
                    Url baseUrl = MediaManager.get().url().publicId(publicIdProfileImage).format("jpg").type("upload");
                    MediaManager.get().responsiveUrl(logoEmployer, baseUrl,
                            ResponsiveUrl.Preset.AUTO_FILL, new ResponsiveUrl.Callback() {
                                @Override
                                public void onUrlReady(Url url) {
                                    String urlGenerated = url.generate();
                                    Picasso.get().load(urlGenerated)
                                            .transform(new RoundedTransformation(MapJobFragment.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_small_size), 0))
                                            .placeholder(placeholderEmployer)
                                            .into(logoEmployer);
                                }
                            });
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // nothing to do here
            }
        }, getActivity());
    }


    private void initView(View view) {
        recyclerView = view.findViewById(R.id.day_list);
        landingMapView = view.findViewById(R.id.landing_map_view);
        inJobHeader = view.findViewById(R.id.in_job_header);
        jobRoleTitle = view.findViewById(R.id.job_role_title);
        employerNameTitle = view.findViewById(R.id.employer_name_title);
        exploringTitle = view.findViewById(R.id.title_header);
        header = view.findViewById(R.id.navigation_header_container);
        checkInButton = view.findViewById(R.id.locate_checkin_button);
        logoEmployer = view.findViewById(R.id.logo_employer);
        rl_list=(RelativeLayout)view.findViewById(R.id.rl_list);
        rl_map=(RelativeLayout)view.findViewById(R.id.rl_map);
        img_list=(ImageView) view.findViewById(R.id.img_list);
        img_map=(ImageView) view.findViewById(R.id.img_map);
        frame_list=(RelativeLayout)view.findViewById(R.id.frame_list);
        rl_location=(RelativeLayout)view.findViewById(R.id.rl_location);
        ll_map=(LinearLayout)view.findViewById(R.id.ll_map);

        rl_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_list.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_white_bg) );
                img_list.setImageResource(R.drawable.list_black);
                rl_map.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_blue_bg) );
                img_map.setImageResource(R.drawable.map_white);

                landingMapView.setVisibility(View.GONE);

                frame_list.setVisibility(View.VISIBLE);
//                ListJobFragment fragment2 = new ListJobFragment();
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction fragmentTransaction =        fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frame_list, fragment2);
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();

                callListJobFragment(currentDate);
                checkInButton.setVisibility(View.GONE);
            }
        });


        img_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_list.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_white_bg) );
                img_list.setImageResource(R.drawable.list_black);
                rl_map.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_blue_bg) );
                img_map.setImageResource(R.drawable.map_white);

                landingMapView.setVisibility(View.GONE);

                frame_list.setVisibility(View.VISIBLE);
//                ListJobFragment fragment2 = new ListJobFragment();
//                FragmentManager fragmentManager = getFragmentManager();
//                FragmentTransaction fragmentTransaction =        fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.frame_list, fragment2);
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
                callListJobFragment(currentDate);
                checkInButton.setVisibility(View.GONE);
            }
        });

        rl_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;
                rl_map.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_white_bg) );
                img_list.setImageResource(R.drawable.list_white);
                rl_list.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_blue_bg) );
                img_map.setImageResource(R.drawable.map_black);
                frame_list.setVisibility(View.GONE);
                landingMapView.setVisibility(View.VISIBLE);
                if (currentJob != null){
                    checkInButton.setVisibility(View.VISIBLE);
                }

            }
        });
        img_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = false;
                rl_map.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_white_bg) );
                img_list.setImageResource(R.drawable.list_white);
                rl_list.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.circular_blue_bg) );
                img_map.setImageResource(R.drawable.map_black);
                frame_list.setVisibility(View.GONE);
                landingMapView.setVisibility(View.VISIBLE);

                if (currentJob != null){
                    checkInButton.setVisibility(View.VISIBLE);
                }
            }
        });

        rl_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), SetMapAreaActivity.class);
                startActivity(intent);

            }
        });
        if (currentJob != null) {
            setEmployerImage();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        DayAdapter.OnDaySelectedListener daySelectedListener = new DayAdapter.OnDaySelectedListener() {
            @Override
            public void onDaySelected(LocalDateTime localDateTime, int position, int previousSelectedPosition) {
                currentSelectedDate = localDateTime;
                currentDate = currentSelectedDate.toLocalDate().toString();
                if (flag) {
                    MapJobFragment.this.callListJobFragment(localDateTime.toLocalDate().toString());
                }

                if (currentJob == null || !Utils.isBetween(Utils.fromTimeStamp(localDateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()).getTime(),
                        Utils.fromTimeStamp(currentJob.getStartDate()).getTime(),
                        Utils.fromTimeStamp(currentJob.getEndDate()).getTime())) {
                    MapJobFragment.this.addPinsOnGoogleMap(localDateTime);
                }

                dayAdapter.notifyItemChanged(position);
                dayAdapter.notifyItemChanged(previousSelectedPosition);
            }
        };
        dayAdapter = new DayAdapter(daySelectedListener, currentJob);

        recyclerView.setAdapter(dayAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        checkInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapJobFragment.this.getDeviceLocation();
                AlertDialog alertDialog = Utils.getConfirmDialog(MapJobFragment.this.getString(R.string.check_in), MapJobFragment.this.getString(R.string.check_in_message_dialog),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MapJobFragment.this.callCheckIn();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing to do here;
                            }
                        }, MapJobFragment.this.getActivity());
                alertDialog.show();
            }
        });
    }

    private void callListJobFragment(String date){
        flag = true;
        ListJobFragment fragment2 = new ListJobFragment();
        Bundle args = new Bundle();
        args.putString("date", date);
        fragment2.setArguments(args);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction =        fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_list, fragment2);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setupMap() {
        double ukLatitude = 51.509865;
        double ukLongitude = -0.118092;
        LatLng latLng = new LatLng(ukLatitude, ukLongitude);
        float bearing = 0;
        float tilt = 0;
        float zoom = 14;
        CameraPosition cameraPosition = new CameraPosition(latLng, zoom, tilt, bearing);
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void addPinsOnGoogleMap(final LocalDateTime localDateTime) {
        final ProgressDialog progressDialog = UIUtils.showProgress(getActivity(), getString(R.string.loading), null, true, false, null);

        long timestamp = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        BoundingBoxCoordinate boundingBoxCoordinate = Utils
                .calculateBoundingBoxFromCoordinate(LONDON_LATITUDE, LONDON_LONGITUDE, 10000);
        OpenForceApplication.getApiClient().getMapJobs(boundingBoxCoordinate, timestamp,
                new OnSuccessListener<JobsResponse>() {
                    @Override
                    public void onSuccess(JobsResponse jobsResponse) {
                        currentJobList = jobsResponse.getJobs();
                        currentDateJobList.clear();
                        for (int i = 0; i < currentJobList.size(); i++) {
                            long currentDate = currentJobList.get(i).getStartDate();
                            String start_date_str = Utils.timestampToFormattedDate(currentJobList.get(i).getStartDate());

                            DateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                            DateFormat outputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                            String inputDateStr = start_date_str;
                            Date date1 = null;
                            try {
                                date1 = inputFormat1.parse(inputDateStr);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String outputDateStr1 = outputFormat1.format(date1);
                            System.out.println("Partha Job Date: " + outputDateStr1 + " Click Date: " + localDateTime.toLocalDate().toString());


                            if (outputDateStr1.equalsIgnoreCase(localDateTime.toLocalDate().toString())) {
                                System.out.println("Adding");
                                Job job = currentJobList.get(i);
                                currentDateJobList.add(job);
                            } else {
                                System.out.println("Not Adding");
                            }
//                        currentDateJobList.add(currentJobList.get(i));

                        }
                        MapJobFragment.this.addJobToMap();
                        progressDialog.dismiss();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        Log.e(TAG, "Error retrieving jobs", error);
                    }
                }, getActivity());
    }


    public static String getNextDate(String  curDate) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date=null;
        try {
            date = format.parse(curDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return format.format(calendar.getTime());
    }


    private void addJobToMap() {
        googleMap.clear();
        for (int i = 0; i < currentDateJobList.size(); i++) {
            Job job = currentDateJobList.get(i);
            LatLng jobLocation = new LatLng(job.getLatitude(), job.getLongitude());
            MarkerView markerView = new MarkerView(getActivity());
            markerView.setNumberEmployees("x"+job.getRequiredEmployees());
            markerView.setRole(job.getJobRole().getName());
            IconGenerator generator = new IconGenerator(getActivity());
            generator.setBackground(getActivity().getDrawable(android.R.color.transparent));
            generator.setContentView(markerView);
            Bitmap icon = generator.makeIcon();
            MarkerOptions markerOptions = new MarkerOptions().position(jobLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon)).anchor(0.1f, 1);
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(i);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        landingMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        landingMapView.onResume();

        if (currentJob != null) {
            header.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.in_job_blue));
            inJobHeader.setVisibility(View.VISIBLE);
            jobRoleTitle.setText(currentJob.getJobRole().getName());
            employerNameTitle.setText(currentJob.getEmployerName());
            exploringTitle.setVisibility(View.GONE);
            checkInButton.setVisibility(View.VISIBLE);
            rl_location.setVisibility(View.GONE);
            ll_map.setVisibility(View.GONE);
        } else {
            header.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_blue));
            inJobHeader.setVisibility(View.GONE);
            exploringTitle.setVisibility(View.VISIBLE);
            checkInButton.setVisibility(View.GONE);
            rl_location.setVisibility(View.VISIBLE);
            ll_map.setVisibility(View.VISIBLE);
        }
    }

    private void showUserLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }
        }
    }

    private void callCheckIn() {
        final ProgressDialog progressDialog = UIUtils.showProgress(getActivity(), null, getString(R.string.checking_in), true, false, null);
        progressDialog.show();
        apiClient.checkin(currentJob.getId(), Calendar.getInstance().getTimeInMillis(), getActivity(), new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                checkInButton.setEnabled(false);
                progressDialog.dismiss();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "Error checking job", e);
                if (e instanceof FirebaseFunctionsException) {
                    Snackbar.make(MapJobFragment.this.getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(MapJobFragment.this.getView(), "There has been an error checking in", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location location = task.getResult();
                        LatLng currentLatLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng,
                                14);
                        googleMap.moveCamera(update);
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        landingMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        landingMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        landingMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        landingMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        landingMapView.onLowMemory();
    }
}
