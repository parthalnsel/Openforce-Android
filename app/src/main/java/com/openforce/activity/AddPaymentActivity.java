package com.openforce.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.fragments.AddPaymentFragment;
import com.openforce.model.StripeInfo;
import com.openforce.model.User;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

import java.util.Arrays;
import java.util.List;

public class AddPaymentActivity extends BaseActivity implements AddPaymentFragment.AddPaymentCallbacks {

    private FrameLayout mainContent;
    private static final String TAG = "AddPaymentActivity";
    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, AddPaymentActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        initView();
    }

    private void initView() {
        mainContent = findViewById(R.id.main_content_add_payment);

        AddPaymentFragment paymentFragment = new AddPaymentFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content_add_payment, paymentFragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onSavePaymentInfo(final StripeInfo stripeInfos ){
        final ProgressDialog progressDialog = UIUtils.showProgress(this,
                getString(R.string.saving_stripe_info), getString(R.string.loading),
                true, false, null);
//        Log.d(TAG, "onSavePaymentInfo: " + stripeInfos.size());
//        Log.d(TAG, "onSavePaymentInfo: "+ stripeInfos.toArray());
        apiClient.setStripeInfo(stripeInfos, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void __) {
                SecureSharedPreference secureSharedPreference = OpenForceApplication.getInstance().getSecureSharedPreference();
                User user = secureSharedPreference.getUserInfo();
                user.stripe_info = Arrays.asList(stripeInfos);
                secureSharedPreference.setUserInfo(user);
                progressDialog.dismiss();

                AddPaymentActivity.this.setResult(RESULT_OK);
                AddPaymentActivity.this.finish();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error saving user references", e);
                progressDialog.dismiss();
                Snackbar.make(root, R.string.error_save_user_references, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPaymentCloseBtnClick() {
        finish();
    }
}
