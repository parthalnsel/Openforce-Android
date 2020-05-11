package com.openforce.activity;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import android.widget.Toast;

import com.google.firebase.functions.HttpsCallableResult;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.fragments.PinFragment;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

public class ConfirmPinActivity extends BaseActivity implements PinFragment.PinFragmentCallbacks {


    private static final String EXTRA_PIN_PREVIOUS_SCREEN = "EXTRA_" + ConfirmPinActivity.class.getSimpleName() + "PIN_PREVIOUS_SCREEN";

    private PinFragment pinFragment;

    public static Intent getIntent(Context context, String pinInPreviousScreen) {
        Intent intent = new Intent(context, ConfirmPinActivity.class);
        intent.putExtra(EXTRA_PIN_PREVIOUS_SCREEN, pinInPreviousScreen);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pin);
        pinFragment = PinFragment.newInstance(getString(R.string.confirm_pin), true);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, pinFragment).commit();
    }

    @Override
    public void onPinComplete(final String pin) {
        if (pin.equals(getIntent().getStringExtra(EXTRA_PIN_PREVIOUS_SCREEN))) {
            final ProgressDialog progressDialog = UIUtils.showProgress(this, getString(R.string.loading),
                    getString(R.string.saving_pin), true, false, null);

            apiClient.saveUserPin(pin, new OnSuccessListener<HttpsCallableResult>() {
                @Override
                public void onSuccess(HttpsCallableResult httpsCallableResult) {
                    progressDialog.dismiss();
                    SecureSharedPreference preference = OpenForceApplication.getInstance().getSecureSharedPreference();
                    User userInfo = preference.getUserInfo();
                    userInfo.pin = pin;
                    preference.setUserInfo(userInfo);
                    preference.handleSecurePasswordChange(pin);

                    ConfirmPinActivity.this.setResult(RESULT_OK);
                    ConfirmPinActivity.this.finish();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ConfirmPinActivity.this, R.string.error_saving_pin, Toast.LENGTH_LONG).show();
                }
            });

        } else {
            pinFragment.clearPin();
            Snackbar.make(root, R.string.pin_do_not_match, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackClicked() {
        finish();
    }
}
