package com.openforce.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.openforce.R;
import com.openforce.fragments.PinFragment;

public class SetupPinActivity extends BaseActivity implements PinFragment.PinFragmentCallbacks {

    private static final int CONFIRM_PIN_REQUEST_CODE = 1000;


    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SetupPinActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pin);
        PinFragment pinFragment = PinFragment.newInstance(getString(R.string.set_login_pin), true);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, pinFragment).commit();
    }

    @Override
    public void onPinComplete(String pin) {
        startActivityForResult(ConfirmPinActivity.getIntent(this, pin), CONFIRM_PIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_PIN_REQUEST_CODE && resultCode == RESULT_OK) {
            // pin has been saved so we can finish here
            finish();
        }
    }

    @Override
    public void onBackClicked() {
        finish();
    }
}
