package com.openforce.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.UIUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class SecureAccountActivity extends BaseActivity {

    private ImageView closeButton;
    private LinearLayout registerPhoneNumberButtonLayout;
    private LinearLayout secureAccountButtonLayout;
    private Button doneButton;


    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SecureAccountActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_account);

        initView();
    }

    private void initView() {
        closeButton = findViewById(R.id.close_button);
        doneButton = findViewById(R.id.done_button);
        registerPhoneNumberButtonLayout = findViewById(R.id.register_phone_number_button_layout);
        secureAccountButtonLayout = findViewById(R.id.secure_account_button_layout);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecureAccountActivity.this.finish();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecureAccountActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SecureSharedPreference securePreferences = OpenForceApplication.getInstance().getSecureSharedPreference();
        final User user = securePreferences.getUserInfo();

        registerPhoneNumberButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                if (TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                    SecureAccountActivity.this.startActivity(AddPhoneNumberActivity.getIntent(SecureAccountActivity.this));
                } else {
                    // show password dialog
                    AlertDialog alertDialog = SecureAccountActivity.this.getConfirmPasswordDialog(SecureAccountActivity.this.getString(R.string.update_phone_number),
                            new ConfirmPasswordDialogListener() {
                                @Override
                                public void onPasswordVerified() {
                                    SecureAccountActivity.this.startActivity(AddPhoneNumberActivity.getIntent(SecureAccountActivity.this));
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        secureAccountButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                if (TextUtils.isEmpty(user.pin)) {
                    SecureAccountActivity.this.startActivity(SetupPinActivity.getIntent(SecureAccountActivity.this));
                } else {
                    // show password dialog
                    AlertDialog alertDialog = SecureAccountActivity.this.getConfirmPasswordDialog(SecureAccountActivity.this.getString(R.string.pin_reset),
                            new ConfirmPasswordDialogListener() {
                                @Override
                                public void onPasswordVerified() {
                                    SecureAccountActivity.this.startActivity(SetupPinActivity.getIntent(SecureAccountActivity.this));
                                }
                            });
                    alertDialog.show();
                }
            }
        });
    }

    private AlertDialog getConfirmPasswordDialog(String title,
                                                 @NonNull final ConfirmPasswordDialogListener confirmPasswordDialogListener) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_password, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = dialogView.findViewById(R.id.password_field);
                        String password = editText.getText().toString();
                        if (TextUtils.isEmpty(password)) {
                            Snackbar.make(root, R.string.empty_password, Snackbar.LENGTH_LONG).show();
                        } else {
                            dialogInterface.dismiss();
                            final ProgressDialog progressDialog = UIUtils.showProgress(SecureAccountActivity.this, SecureAccountActivity.this.getString(R.string.verifying_password),
                                    "", true, false, null);
                            progressDialog.show();
                            firebaseAuth.signInWithEmailAndPassword(firebaseAuth.getCurrentUser().getEmail(), password)
                                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            progressDialog.dismiss();
                                            confirmPasswordDialogListener.onPasswordVerified();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Snackbar.make(root, R.string.cannot_verify_password, Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                })
                .setView(dialogView).create();

        return alertDialog;
    }

    public interface ConfirmPasswordDialogListener {
        void onPasswordVerified();
    }
}
