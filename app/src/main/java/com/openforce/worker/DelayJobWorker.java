package com.openforce.worker;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.openforce.OpenForceApplication.PERIODIC_CURRENT_JOB_REQUEST;

public class DelayJobWorker extends Worker {

    public DelayJobWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        WorkManager workManager = WorkManager.getInstance();
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                .Builder(CurrentJobWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(constraints).addTag(PERIODIC_CURRENT_JOB_REQUEST)
                .build();

        workManager.enqueueUniquePeriodicWork(PERIODIC_CURRENT_JOB_REQUEST, ExistingPeriodicWorkPolicy.KEEP, workRequest);
        return Result.success();
    }
}
