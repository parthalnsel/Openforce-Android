package com.openforce;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.openforce.activity.LandingActivity;
import com.openforce.model.CurrentJobResponse;
import com.openforce.providers.OpenforceSharedPreference;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.ApiClient;
import com.openforce.utils.Utils;
import com.openforce.worker.DelayJobWorker;

import io.fabric.sdk.android.Fabric;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static com.openforce.providers.SecureSharedPreference.SECURE_PREFERENCE_FILE_NAME_PIN;
import static com.openforce.providers.SecureSharedPreference.SECURE_PREFERENCE_FILE_NAME_UID;

public class OpenForceApplication extends Application {

    private static final String TAG = "OpenForceApplication";
    public static final String PERIODIC_CURRENT_JOB_REQUEST = "PERIODIC_CURRENT_JOB_REQUEST";
    public static final String DELAY_JOB_REQUEST = "DELAY_JOB_REQUEST";

    private static OpenForceApplication INSTANCE;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseFunctions firebaseFunctions;
    private ApiClient apiClient;
    private SecureSharedPreference secureSharedPreference;
    private OpenforceSharedPreference normalSharedPreference;
    private WorkManager workManager;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        INSTANCE = this;
        Fabric.with(this, new Crashlytics());
        AndroidThreeTen.init(this);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFunctions = FirebaseFunctions.getInstance();
        gson = new GsonBuilder().serializeNulls().create();
        apiClient = new ApiClient(firebaseAuth, firebaseFirestore, firebaseFunctions, gson);
        normalSharedPreference = new OpenforceSharedPreference(this, gson);
        workManager = WorkManager.getInstance();
        addAuthStateChangedCallback();
        scheduleGetCurrentJob();
        retrieveCurrentJob();
        setupCloudinary();
    }

    private void setupCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", getString(R.string.cloudinary_name));
        MediaManager.init(this, config);

    }

    private void retrieveCurrentJob() {
        if (apiClient.isUserLoggedIn()) {
            apiClient.getCurrentJob(new OnSuccessListener<CurrentJobResponse>() {
                @Override
                public void onSuccess(CurrentJobResponse currentJobResponse) {
                    normalSharedPreference.setCurrentJob(currentJobResponse.job);
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Error retrieving current job", e);
                }
            });
        }
    }

    private void scheduleGetCurrentJob() {
        long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 1);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long differenceToMidnight = cal.getTimeInMillis() - currentTimeStamp;
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest
                .Builder(DelayJobWorker.class).addTag(DELAY_JOB_REQUEST)
                .setInitialDelay(differenceToMidnight, TimeUnit.MILLISECONDS)
                .build();

        workManager.enqueueUniqueWork(DELAY_JOB_REQUEST, ExistingWorkPolicy.KEEP, workRequest);
    }


    public static OpenForceApplication getInstance() {
        return INSTANCE;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseFirestore getFirebaseFirestore() {
        return firebaseFirestore;
    }

    public static ApiClient getApiClient() {
        return INSTANCE.apiClient;
    }

    private void addAuthStateChangedCallback() {
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                if (auth.getCurrentUser() == null) {
                    // user has been kicked out.
                    // start fresh activity
                    Utils.deleteSharedPreferenceFile(OpenForceApplication.this, SECURE_PREFERENCE_FILE_NAME_PIN);
                    Utils.deleteSharedPreferenceFile(OpenForceApplication.this, SECURE_PREFERENCE_FILE_NAME_UID);
                    OpenForceApplication.this.startActivity(LandingActivity.getIntentWithClearFlag(OpenForceApplication.this));
                }
            }
        });
    }

    public void initSecureSharedPreference(String password, boolean isPin) {
        secureSharedPreference = new SecureSharedPreference(this, password, gson, isPin);
    }

    public SecureSharedPreference getSecureSharedPreference() {
        return secureSharedPreference;
    }

    public OpenforceSharedPreference getSharedPreference() {
        return normalSharedPreference;
    }


    public Gson getGson() {
        return gson;
    }
}
