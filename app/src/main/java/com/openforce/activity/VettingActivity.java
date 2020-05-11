package com.openforce.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.fragments.IdentityReferencesFragment;
import com.openforce.fragments.ReferencesFragment;
import com.openforce.fragments.WelcomeVettingFragment;
import com.openforce.interfaces.PageSliderClick;
import com.openforce.model.Reference;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

import java.util.List;

public class VettingActivity extends BaseActivity implements WelcomeVettingFragment.VettingCallbacks,
        IdentityReferencesFragment.IdentityReferencesCallbacks, ReferencesFragment.ReferencesCallbacks, PageSliderClick {

    private static final String TAG = "VettingActivity";

    public static Intent getIntentWithClearFlag(Context context) {
        Intent intent = new Intent(context, VettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vetting);

        WelcomeVettingFragment welcomeVettingFragment = new WelcomeVettingFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, welcomeVettingFragment).addToBackStack(null)
                .commit();
    }

    @Override
    public void onAddExperienceClick() {
        startActivity(ExperienceSkillsActivity.getIntent(this));
    }

    @Override
    public void onAddReferencesClick() {
        startActivity(IdentityReferencesActivity.getIntent(this));
    }

    @Override
    public void onSecureAccountClick() {
        startActivity(SecureAccountActivity.getIntent(this));
    }

    @Override
    public void onAddPaymentInfoClick() {
        startActivity(AddPaymentActivity.getIntent(this));
    }

    @Override
    public void onWelcomeVettingCloseButtonClick() {
        startActivity(HomeActivity.getIntentWithClearFlag(this));
    }

    @Override
    public void onUserFullyVetted() {
        // nothing here
    }

    @Override
    public void onAddEmailReferences() {
        ReferencesFragment referencesFragment = new ReferencesFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, referencesFragment).addToBackStack(null)
                .commit();
    }

    @Override
    public void onIdentityReferencesCloseButtonClick() {
        startActivity(HomeActivity.getIntentWithClearFlag(this));
    }

    @Override
    public void onDoneClicked() {
        startActivity(HomeActivity.getIntentWithClearFlag(this));
    }

    @Override
    public void onBackPressed() {
        startActivity(HomeActivity.getIntentWithClearFlag(this));
    }

    @Override
    public void onSaveReferences(final List<Reference> references) {
        final ProgressDialog progressDialog = UIUtils.showProgress(this, getString(R.string.saving_references), getString(R.string.loading), true, false, null);
        apiClient.setUserReference(references, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void __) {
                SecureSharedPreference secureSharedPreference = OpenForceApplication.getInstance().getSecureSharedPreference();
                User user = secureSharedPreference.getUserInfo();
                user.references = references;
                secureSharedPreference.setUserInfo(user);

                FragmentManager fm = VettingActivity.this.getFragmentManager();
                // return to root fragment;
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                progressDialog.dismiss();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error saving user references", e);
                progressDialog.dismiss();
            }
        });


    }

    @Override
    public void onReferencesBackArrowClicked() {
        // go back to previous screen
        onBackPressed();
    }

    @Override
    public void onSkillPageClicked() {
        startActivity(ExperienceSkillsActivity.getIntent(this));
    }

    @Override
    public void onIdentityReferencesPageClicked() {
        startActivity(IdentityReferencesActivity.getIntent(this));
    }

    @Override
    public void onSecureAccountPageClicked() {
        startActivity(SecureAccountActivity.getIntent(this));
    }

    @Override
    public void onAddPaymentPageClicked() {
        startActivity(AddPaymentActivity.getIntent(this));
    }
}
