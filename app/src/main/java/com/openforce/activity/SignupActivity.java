package com.openforce.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.utils.UIUtils;
import com.openforce.utils.ValidationUtils;

import static com.openforce.db.Tables.TABLE_USER;

public class SignupActivity extends BaseActivity {

    private static final String TAG = SignupActivity.class.getSimpleName();

    private EditText inputName;
    private EditText inputLastName;
    private EditText inputEmailAddress;
    private EditText inputPassword;
    private EditText inputConfirmPassword;
    private TextView labelTermsAndPrivacy;
    private Button buttonCancel;
    private Button buttonSignup;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ProgressDialog progressDialog = null;

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, SignupActivity.class);
        // add your extras here
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initView();

        firebaseAuth = OpenForceApplication.getInstance().getFirebaseAuth();
        firebaseFirestore = OpenForceApplication.getInstance().getFirebaseFirestore();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                SignupActivity.this.finish();
            }
        });
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                SignupActivity.this.signupUser();
            }
        });
    }

    private void signupUser() {
        boolean validSignup = true;
        final String email = inputEmailAddress.getText().toString();
        String password = inputPassword.getText().toString();
        final String name = inputName.getText().toString();
        final String lastName = inputLastName.getText().toString();
        String confirmPassword = inputConfirmPassword.getText().toString();



        if (TextUtils.isEmpty(name)) {
            validSignup = false;
            inputName.setError(getString(R.string.empty_name));
            requestFocus(inputName);
        }else if (TextUtils.isEmpty(lastName)) {
            validSignup = false;
            inputLastName.setError(getString(R.string.empty_last_name));
            requestFocus(inputLastName);
        }else if (!ValidationUtils.isValidEmail(email)) {
            validSignup = false;
            inputEmailAddress.setError(getString(R.string.invalid_email));
            requestFocus(inputEmailAddress);
        }else if (TextUtils.isEmpty(password)) {
            validSignup = false;
            inputPassword.setError(getString(R.string.empty_password));
            requestFocus(inputPassword);
        }else if (TextUtils.isEmpty(confirmPassword)) {
            validSignup = false;
            inputConfirmPassword.setError(getString(R.string.empty_confirm_password));
            requestFocus(inputConfirmPassword);
        }else if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword) && validSignup) {
            if (!password.equals(confirmPassword)) {
                // password does not match
                validSignup = false;
                UIUtils.hideKeyboard(root);
                inputConfirmPassword.setError(getString(R.string.confirm_password_no_match));
            } else {
                if (!ValidationUtils.isValidPassword(password)) {
                    // password does not agree with minimum security
                    validSignup = false;
                    UIUtils.hideKeyboard(root);
                    UIUtils.showSnackMessage(root, getString(R.string.error_password_not_good));
                }
            }
        }

//        if (TextUtils.isEmpty(lastName)) {
//            validSignup = false;
//            inputLastName.setError(getString(R.string.empty_last_name));
//            requestFocus(inputLastName);
//        }





        if (validSignup) {
            progressDialog = UIUtils.showProgress(this, "Loading...",
                    null, true, false, null);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser firebaseUser = authResult.getUser();
                    String uid = firebaseUser.getUid();
                    OpenForceApplication.getInstance().initSecureSharedPreference(uid, false);
                    SignupActivity.this.pushUserDataInDb(name, lastName, email, uid);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    if (e instanceof FirebaseAuthException) {
                        UIUtils.showSnackMessage(root, e.getMessage());
                    } else {
                        UIUtils.showSnackMessage(root, SignupActivity.this.getString(R.string.error_on_signup));
                    }
                    Log.e(TAG, "Error signin up", e);
                }
            });
        }
    }

    private void pushUserDataInDb(String firstName, String lastName, String email, String uid) {
        final User user = new User(firstName, lastName, email, uid);
        firebaseFirestore.collection(TABLE_USER).document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void documentReference) {
                progressDialog.dismiss();
                OpenForceApplication.getInstance().getSecureSharedPreference().setUserInfo(user);
                SignupActivity.this.startActivity(VettingActivity.getIntentWithClearFlag(SignupActivity.this));

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        if (!TextUtils.isEmpty(instanceIdResult.getToken())) {
                            OpenForceApplication.getApiClient().saveFirebaseMessagginToken(instanceIdResult.getToken(), user.uid, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void __) {
                                    progressDialog.dismiss();
                                    SignupActivity.this.startActivity(VettingActivity.getIntentWithClearFlag(SignupActivity.this));
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    SignupActivity.this.startActivity(VettingActivity.getIntentWithClearFlag(SignupActivity.this));
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            SignupActivity.this.startActivity(VettingActivity.getIntentWithClearFlag(SignupActivity.this));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        SignupActivity.this.startActivity(VettingActivity.getIntentWithClearFlag(SignupActivity.this));
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                UIUtils.showSnackMessage(root, SignupActivity.this.getString(R.string.error_on_signup));
                Log.e(TAG, "Error signin up", e);
            }
        });
    }

    private void initView() {
        inputName = findViewById(R.id.input_name);
        inputLastName = findViewById(R.id.input_last_name);
        inputEmailAddress = findViewById(R.id.input_email_address);
        inputPassword = findViewById(R.id.input_password);
        inputConfirmPassword = findViewById(R.id.input_confirm_password);
        labelTermsAndPrivacy = findViewById(R.id.label_terms_and_privacy);
        buttonCancel = findViewById(R.id.button_cancel);
        buttonSignup = findViewById(R.id.button_signup);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        final String privacyPolicyUrl = getString(R.string.privacy_link);
        final String termsUrl = getString(R.string.terms_link);
        String firstPartOfMessage = "By continuing, you  agree to ";
        String termsOfService = "Terms of Service";
        String secondPartOfMessage = " and ";
        String privacyNotice = "Privacy Notice";
        ClickableSpan privacyPolicySpannable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
                startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setFlags(Paint.UNDERLINE_TEXT_FLAG);
            }
        };
        ClickableSpan termsAndConditionSpannable = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(termsUrl));
                startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setFlags(Paint.UNDERLINE_TEXT_FLAG);
            }
        };
        spannableStringBuilder = spannableStringBuilder.append(firstPartOfMessage)
                .append(termsOfService).append(secondPartOfMessage)
                .append(privacyNotice);

        spannableStringBuilder.setSpan(termsAndConditionSpannable, firstPartOfMessage.length(), firstPartOfMessage.length() + termsOfService.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int startOfPrivacyPolicy = firstPartOfMessage.length() + termsOfService.length() + secondPartOfMessage.length();
        spannableStringBuilder.setSpan(privacyPolicySpannable, startOfPrivacyPolicy, startOfPrivacyPolicy + privacyNotice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        labelTermsAndPrivacy.setText(spannableStringBuilder);
        labelTermsAndPrivacy.setMovementMethod(LinkMovementMethod.getInstance());


    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
