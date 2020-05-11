package com.openforce.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.openforce.OpenForceApplication;
import com.openforce.model.User;

import java.util.Objects;

public class LauncherActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (OpenForceApplication.getApiClient().isUserLoggedIn()) {
                OpenForceApplication.getInstance().initSecureSharedPreference(firebaseAuth.getUid(), false);
                User user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();

                if (user == null || !Objects.equals(user.uid, firebaseAuth.getUid())) {
                    // couldn't decrypt data so we should use the user pin

                    startActivity(PinLoginActivity.getIntent(this));
                    finish();
                } else {
                    // redirect to home
                    Log.d("Current User" ,"Going To Home");
                    startActivity(HomeActivity.getIntentWithClearFlag(this));
                    finish();
                }


        } else {
            // redirect to landing
            startActivity(LandingActivity.getIntent(this));
            finish();
        }
    }
}
