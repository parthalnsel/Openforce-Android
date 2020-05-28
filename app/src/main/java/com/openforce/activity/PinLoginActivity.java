package com.openforce.activity;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.fragments.PinFragment;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PinLoginActivity extends BaseActivity implements PinFragment.PinFragmentCallbacks {

    private static final String TAG = "PinLoginActivity";

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, PinLoginActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O){
            disableAutofill();
        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P){
            disableAutofillForP();
        }else
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
            disableAutofillForQ();
        }
        initView();
    }

    private void initView() {
        PinFragment pinFragment = PinFragment.newInstance(getString(R.string.login_with_your_pin), false);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, pinFragment).commit();
    }

    @Override
    public void onPinComplete(String pin) {
        UIUtils.hideKeyboard(root);
        OpenForceApplication.getInstance().initSecureSharedPreference(pin, true);
        if (OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo() == null) {
            Snackbar.make(root, R.string.wrong_pin, Snackbar.LENGTH_LONG).show();
        } else {

            final ProgressDialog progressDialog = UIUtils.showProgress(this, getString(R.string.loading), null, true, false, null);
            apiClient.getCurrentUserInfo(new OnSuccessListener<HttpsCallableResult>() {
                @Override
                public void onSuccess(HttpsCallableResult httpsCallableResult) {
                    User user = null;
                    try {
                        SecureSharedPreference preference = OpenForceApplication.getInstance().getSecureSharedPreference();
                        Gson gson = OpenForceApplication.getInstance().getGson();
                        user = User.fromJSON(new JSONObject(httpsCallableResult.getData() + ""), gson);
//                    Log.d(TAG, "onPinComplete: " + user.stripe_info.get(0).access_token.toString());
                        preference.setUserInfo(user);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error refreshing user info", e);
                    }
                    progressDialog.dismiss();
                    PinLoginActivity.this.startActivity(HomeActivity.getIntentWithClearFlag(PinLoginActivity.this));
                    PinLoginActivity.this.finish();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    PinLoginActivity.this.startActivity(HomeActivity.getIntentWithClearFlag(PinLoginActivity.this));
                    PinLoginActivity.this.finish();
                    progressDialog.dismiss();
                    Log.e(TAG, "Error refreshing user info", e);
                }
            });

        }
    }

    private void refreshUserInfo() {

    }
    
    @TargetApi(Build.VERSION_CODES.Q)
    private void disableAutofillForQ() {
        getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void disableAutofill() {
        getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
    }

    @TargetApi(Build.VERSION_CODES.P)
    private void disableAutofillForP() {
        getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
    }



    @Override
    public void onBackClicked() {
        // no-op
    }
}
