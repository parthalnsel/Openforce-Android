package com.openforce.providers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.openforce.model.Job;

public class OpenforceSharedPreference {

    private static final String TAG = "SecureSharedPreference";

    private static final String SHARED_PREF_NAME = "OPENFORCE_SHARED_PREFERENCE";

    public static final String SECURE_PREF_CURRENT_JOB = "secure_preference_current_job";

    private SharedPreferences sharedPreferences;
    private Context context;
    private Gson gson;

    public OpenforceSharedPreference(Context context, Gson gson) {
        this.context = context;
        this.gson = gson;
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setCurrentJob(Job job) {
        if (job == null) {
            sharedPreferences.edit().remove(SECURE_PREF_CURRENT_JOB).apply();
        } else {
            sharedPreferences.edit().putString(SECURE_PREF_CURRENT_JOB, gson.toJson(job)).apply();
        }
    }

    public Job getCurrentJob() {
        Job job = null;
        String jobJson = sharedPreferences.getString(SECURE_PREF_CURRENT_JOB, "");
        if (!TextUtils.isEmpty(jobJson)) {
            job = gson.fromJson(jobJson, Job.class);
        }
        return job;
    }
}
