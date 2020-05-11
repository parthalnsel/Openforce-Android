package com.openforce.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;

public class EmployerProfileActivity extends BaseActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employer_profile);

        user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();


        Toast.makeText(getApplicationContext(), user.email, Toast.LENGTH_SHORT).show();
    }

    public static Intent getIntentWithClearFlag(Context context) {
        Intent intent = new Intent(context, EmployerProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
