package com.openforce.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.openforce.OpenForceApplication;
import com.openforce.utils.ApiClient;

public class OpenForceMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (OpenForceApplication.getInstance().getFirebaseAuth().getCurrentUser() != null) {
            ApiClient apiClient = OpenForceApplication.getApiClient();
            apiClient.saveFirebaseMessagginToken(token,
                    OpenForceApplication.getInstance().getFirebaseAuth().getUid(), new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void __) {
                            // no-op
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // no-op
                        }
                    });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        System.out.println("remoteMessage"+remoteMessage.getNotification().getBody());

        System.out.println("remoteMessage"+remoteMessage.getNotification().getTitle());

        System.out.println("remoteMessage"+remoteMessage.getData());

        // TODO handle messages here
    }
}
