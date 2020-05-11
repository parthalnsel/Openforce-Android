package com.openforce.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.openforce.OpenForceApplication;
import com.openforce.utils.ApiClient;

public abstract class BaseActivity extends AppCompatActivity {

    protected View root;
    protected FirebaseAuth firebaseAuth;
    protected ApiClient apiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        root = findViewById(android.R.id.content);
        firebaseAuth = OpenForceApplication.getInstance().getFirebaseAuth();
        apiClient = OpenForceApplication.getApiClient();
    }
}
