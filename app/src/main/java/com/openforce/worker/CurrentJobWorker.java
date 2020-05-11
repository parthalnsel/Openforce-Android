package com.openforce.worker;

import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.openforce.OpenForceApplication;
import com.openforce.model.CurrentJobResponse;
import com.openforce.utils.ApiClient;

import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class CurrentJobWorker extends Worker {

    private CountDownLatch countDownLatch;

    public CurrentJobWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        countDownLatch = new CountDownLatch(1);
    }

    @NonNull
    @Override
    public Result doWork() {
        ApiClient apiClient = OpenForceApplication.getApiClient();

        if (!apiClient.isUserLoggedIn()) {
            // Nothing to do if the user is not logged in.
            return Result.success();
        }
        final Result[] result = {Result.failure()};
        try {
            countDownLatch.await();
            apiClient.getCurrentJob(new OnSuccessListener<CurrentJobResponse>() {
                @Override
                public void onSuccess(CurrentJobResponse currentJobResponse) {
                    result[0] = Result.success();
                    OpenForceApplication.getInstance().getSharedPreference().setCurrentJob(currentJobResponse.job);
                    countDownLatch.countDown();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    result[0] = Result.failure();
                    countDownLatch.countDown();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
            result[0] = Result.retry();
        }

        return result[0];
    }
}
