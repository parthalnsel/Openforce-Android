package com.openforce.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.widget.FrameLayout;

import com.openforce.R;
import com.openforce.fragments.IdentityReferencesFragment;

public class IdentityReferencesActivity extends BaseActivity implements IdentityReferencesFragment.IdentityReferencesCallbacks {

    private static final String TAG = "IdentityReferencesActivity";
    private static final int RESULT_ACTIVITY_REFERENCES = 1000;

    private FrameLayout mainContent;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, IdentityReferencesActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_references);
        initView();
    }

    @Override
    public void onAddEmailReferences() {
        startActivityForResult(ReferencesActivity.getIntent(this), RESULT_ACTIVITY_REFERENCES);
    }

    @Override
    public void onIdentityReferencesCloseButtonClick() {
        finish();
    }

    @Override
    public void onDoneClicked() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_ACTIVITY_REFERENCES && resultCode == RESULT_OK) {
            finish();
        }
    }

    private void initView() {
        mainContent = findViewById(R.id.main_content);

        IdentityReferencesFragment identityReferencesFragment = new IdentityReferencesFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, identityReferencesFragment).commit();
    }
}
