package com.openforce.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.interfaces.OnLoginCallback;
import com.openforce.utils.ApiClient;
import com.openforce.utils.UIUtils;
import com.openforce.utils.Utils;
import com.openforce.utils.ValidationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private EditText inputEmailAddress;
    private EditText inputPassword;
    private Button buttonCancel;
    private Button buttonSignin;
    private TextView forgotPasswordLink;
    private ProgressDialog progressDialog;

    private ApiClient apiClient;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

        apiClient = OpenForceApplication.getApiClient();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                LoginActivity.this.finish();
            }
        });
        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                LoginActivity.this.signIn();
            }
        });
        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.hideKeyboard(root);
                if (!ValidationUtils.isValidEmail(inputEmailAddress.getText().toString())) {
                    Snackbar.make(root, R.string.empty_email, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Utils.getConfirmDialog(LoginActivity.this.getString(R.string.reset_password),
                        LoginActivity.this.getString(R.string.reset_password_message_confirmation),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface __, int which) {
                                LoginActivity.this.resetPassword();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface __, int which) {
                                // nothing to do here!
                            }
                        }, LoginActivity.this).show();

            }
        });
    }

    private void resetPassword() {
        final ProgressDialog progressDialog = UIUtils.showProgress(this, getString(R.string.loading),
                null, true, false, null);
        apiClient.resetPassword(this, inputEmailAddress.getText().toString(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void __) {
                progressDialog.dismiss();
                Snackbar.make(root, R.string.reset_password_success, Snackbar.LENGTH_LONG).show();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e(TAG, "Error resetting password", e);
                Snackbar.make(root, R.string.error_resetting_password, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void initView() {
        inputEmailAddress = findViewById(R.id.input_email_address);
        inputPassword = findViewById(R.id.input_password);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonSignin = findViewById(R.id.button_signin);
        forgotPasswordLink = findViewById(R.id.forgot_password_link);
    }

    private void signIn() {
        boolean validForm = true;
        String email = inputEmailAddress.getText().toString();
        String password = inputPassword.getText().toString();
        UIUtils.hideKeyboard(root);

        if (!ValidationUtils.isValidEmail(email)) {
            validForm = false;
            inputEmailAddress.setError(getString(R.string.invalid_email));
        }

        if (TextUtils.isEmpty(password)) {
            validForm = false;
            inputPassword.setError(getString(R.string.empty_password));
        }

        if (validForm) {
            progressDialog = UIUtils.showProgress(this, "Loading...",
                    null, true, false, null);
            OpenForceApplication.getApiClient().signInWithEmailAndPassword(email, password, new OnLoginCallback() {
                @Override
                public void onErrorLogin(Exception error) {
                    progressDialog.dismiss();
                    Snackbar.make(root, getString(R.string.error_login), Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "Error logging in", error);
                }

                @Override
                public void onSuccessfulLogin(final User user) {
                    if (TextUtils.isEmpty(user.pin)) {
                        OpenForceApplication.getInstance().initSecureSharedPreference(firebaseAuth.getUid(), false);
                    } else {
                        OpenForceApplication.getInstance().initSecureSharedPreference(user.pin, true);
                    }
                    OpenForceApplication.getInstance().getSecureSharedPreference().setUserInfo(user);

                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            if (!TextUtils.isEmpty(instanceIdResult.getToken())) {
                                OpenForceApplication.getApiClient().saveFirebaseMessagginToken(instanceIdResult.getToken(), user.uid, new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        startActivity(HomeActivity.getIntentWithClearFlag(LoginActivity.this));
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        startActivity(HomeActivity.getIntentWithClearFlag(LoginActivity.this));
                                    }
                                } );
                            } else {
                                progressDialog.dismiss();
                                startActivity(HomeActivity.getIntentWithClearFlag(LoginActivity.this));
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            startActivity(HomeActivity.getIntentWithClearFlag(LoginActivity.this));
                        }
                    });
                }
            });
        }
    }

}
