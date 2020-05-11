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
import android.util.Log;
import android.widget.FrameLayout;

import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.fragments.ReferencesFragment;
import com.openforce.model.Reference;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

import java.util.List;

public class ReferencesActivity extends BaseActivity implements ReferencesFragment.ReferencesCallbacks {

    private static final String TAG = "ReferencesActivity";

    private FrameLayout mainContent;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, ReferencesActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_references);
        initView();
    }

    private void initView() {
        mainContent = findViewById(R.id.main_content);

        ReferencesFragment referencesFragment = new ReferencesFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, referencesFragment)
                .commit();
    }

    @Override
    public void onSaveReferences(final List<Reference> references) {
        final ProgressDialog progressDialog = UIUtils.showProgress(this,
                getString(R.string.saving_references), getString(R.string.loading),
                true, false, null);
        apiClient.setUserReference(references, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void __) {
                SecureSharedPreference secureSharedPreference = OpenForceApplication.getInstance().getSecureSharedPreference();
                User user = secureSharedPreference.getUserInfo();
                user.references = references;
                secureSharedPreference.setUserInfo(user);
                progressDialog.dismiss();

                ReferencesActivity.this.setResult(RESULT_OK);
                ReferencesActivity.this.finish();
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
    public void onReferencesBackArrowClicked() {
        finish();
    }
}
