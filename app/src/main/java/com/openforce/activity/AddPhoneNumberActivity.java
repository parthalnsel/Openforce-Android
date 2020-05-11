package com.openforce.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import io.fabric.sdk.android.Fabric;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openforce.R;
import com.openforce.utils.UIUtils;
import com.openforce.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddPhoneNumberActivity extends BaseActivity {

    private static final String TAG = "AddPhoneNumberActivity";

    private ImageView backButton;
    private TextInputLayout inputLayoutPhoneNumber;
    private EditText inputPhoneNumber;
    private Button nextButton;
    private ProgressDialog progressDialog;
    private boolean verifyPhoneNumber;
    private LinearLayout enterPhoneNumberLayout;
    private LinearLayout verifyCodeLayout;
    private TextInputLayout inputLayoutCode;
    private EditText inputCode;
    private Button confirmButton;
    private String verificationId;
    private TextView titleHeader;
    private boolean flagOtpVerified = false;
    private FirebaseFirestore database;
    String phone_number ="";
    ArrayList<String> all_number_list = new ArrayList<>();
    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, AddPhoneNumberActivity.class);
        // add your extras here
        return intent;
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            addNumber(phone_number);
            Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
            linkUser(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            System.out.println("Firebase Error: " + e.toString());
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            //Febric Chnages
//            Crashlytics.logException(e);
            UIUtils.hideKeyboard(root);
            Snackbar.make(root, R.string.verification_failed, Snackbar.LENGTH_LONG).show();
            verifyCodeLayout.setVisibility(View.GONE);
            enterPhoneNumberLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            AddPhoneNumberActivity.this.verificationId = verificationId;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone_number);
        initView();
    }

    private void initView() {
        backButton = findViewById(R.id.back_button);
        inputLayoutPhoneNumber = findViewById(R.id.input_layout_phone_number);
        inputPhoneNumber = findViewById(R.id.input_phone_number);
        nextButton = findViewById(R.id.next_button);
        enterPhoneNumberLayout = findViewById(R.id.enter_phone_number_layout);
        verifyCodeLayout = findViewById(R.id.verify_code_layout);
        inputLayoutCode = findViewById(R.id.input_layout_code);
        inputCode = findViewById(R.id.input_code);
        confirmButton = findViewById(R.id.confirm_button);
        titleHeader= findViewById(R.id.title_header);
        database = FirebaseFirestore.getInstance();

        getAllNumber();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPhoneNumberActivity.this.finish();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = inputPhoneNumber.getText().toString();
                phone_number = phoneNumber;
                if (TextUtils.isEmpty(phoneNumber)) {
                    UIUtils.hideKeyboard(root);
                    Snackbar.make(root, R.string.invalid_number, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (all_number_list.contains(phoneNumber)) {
                    UIUtils.hideKeyboard(root);
                    Snackbar.make(root, "Mobile number already exists. Please try with different number", Snackbar.LENGTH_LONG).show();
                    return;
                }
//            if (!phoneNumber.startsWith("+44")) {
//                phoneNumber = "+44" + phoneNumber;
//            }
//
//            phoneNumber = "+917003236219";

                if (!phoneNumber.startsWith("+")) {
                    phoneNumber = "+" + phoneNumber;
                }
                if (ValidationUtils.isValidNumber(phoneNumber)) {
                    verifyPhoneNumber = true;
                    titleHeader.setText(R.string.enter_security_code);
                    verifyCodeLayout.setVisibility(View.VISIBLE);
                    enterPhoneNumberLayout.setVisibility(View.GONE);
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            120,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            AddPhoneNumberActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks

                } else {
                    UIUtils.hideKeyboard(root);
                    Snackbar.make(root, R.string.invalid_number, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputCode.getText().toString())) {
                    Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, inputCode.getText().toString());
                    Log.d(TAG, "PhoneAuthCredential:" + credential);
                    Log.d(TAG, "PhoneAuthCredential:verificationId" + verificationId);
                    AddPhoneNumberActivity.this.linkUser(credential);
//                signInWithPhoneAuthCredential(credential);

                }
            }
        });

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.getCurrentUser().updatePhoneNumber(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void task) {
                        System.out.println("ParthaTask:addOnSuccessListener " + task.toString());
                        progressDialog.dismiss();
                        AddPhoneNumberActivity.this.addNumber(phone_number);
                        UIUtils.hideKeyboard(root);
                        Toast.makeText(AddPhoneNumberActivity.this, R.string.phone_number_updated, Toast.LENGTH_SHORT).show();
                        AddPhoneNumberActivity.this.finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                System.out.println("ParthaTask:addOnFailureListenerFirst " + e.getMessage());
                progressDialog.dismiss();
                Crashlytics.logException(e);
                UIUtils.hideKeyboard(root);
                Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
            }
        });
//        firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
//                            linkUser(credential);
////                            FirebaseUser user = task.getResult().getUser();
//                            // ...
//                        } else {
//                            // Sign in failed, display a message and update the UI
//                            Log.d(TAG, "signInWithCredential:failure", task.getException());
//                            UIUtils.hideKeyboard(root);
//                            Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
//                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                // The verification code entered was invalid
//                            }
//                        }
//                    }
//                });
    }


    private void linkUser(PhoneAuthCredential credential) {
        progressDialog = UIUtils.showProgress(AddPhoneNumberActivity.this,
                getString(R.string.verifying),
                getString(R.string.verifying_number),
                false, false, null);
        if (TextUtils.isEmpty(firebaseAuth.getCurrentUser().getPhoneNumber())) {
            firebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("ParthaTask:addOnFailureListenerlinkWithCredential " + e.getMessage());
                            progressDialog.dismiss();
                            UIUtils.hideKeyboard(root);
                            flagOtpVerified = false;
                            Crashlytics.logException(e);
                            Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    System.out.println("ParthaTask:addOnCompleteListener " + task.isSuccessful());

                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        UIUtils.hideKeyboard(root);
                        AddPhoneNumberActivity.this.addNumber(phone_number);
                        Toast.makeText(AddPhoneNumberActivity.this, R.string.phone_number_verified, Toast.LENGTH_SHORT).show();
                        AddPhoneNumberActivity.this.finish();
                    } else {
                        progressDialog.dismiss();
                        UIUtils.hideKeyboard(root);
                        Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
                    }

                }
            })
                    ;
        } else {

            firebaseAuth.getCurrentUser().updatePhoneNumber(credential)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("ParthaTask:addOnFailureListenerupdatePhoneNumber " + e.getMessage());
                            progressDialog.dismiss();
                            flagOtpVerified = false;
                            Crashlytics.logException(e);
                            UIUtils.hideKeyboard(root);
                            Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    System.out.println("ParthaTask:addOnCompleteListener " + task.isSuccessful());
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        AddPhoneNumberActivity.this.addNumber(phone_number);
                        UIUtils.hideKeyboard(root);
                        Toast.makeText(AddPhoneNumberActivity.this, R.string.phone_number_updated, Toast.LENGTH_SHORT).show();
                        AddPhoneNumberActivity.this.finish();
                    } else {
                        progressDialog.dismiss();
                        flagOtpVerified = false;
                        UIUtils.hideKeyboard(root);
                        Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
                    }
//                        flagOtpVerified = true;

                }
            });

//            System.out.println("Partha: flag: " + flagOtpVerified);
//            if (flagOtpVerified==true){
//                progressDialog.dismiss();
//
//            }else if (flagOtpVerified == false){
//                Snackbar.make(root, R.string.invalid_code, Snackbar.LENGTH_LONG).show();
//            }
        }

    }

    public void checkPhoneNumberRegister(String phoneNumber){
        FirebaseAuth.getInstance();
    }

    public void getAllNumber(){
        database.collection("mobile_numbers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            System.out.println("Partha: "+task.getResult().size());
                            for (int i=0;i<task.getResult().size(); i++){
                                QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) task.getResult().getDocuments().get(i);
                                all_number_list.add(documentSnapshot.getData().get("number").toString());
                                        System.out.println("All Number: " + all_number_list.toString());
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());

                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void addNumber(String number){
        Map<String, Object> mobile = new HashMap<>();
        mobile.put("number", number);

// Add a new document with a generated ID
        database.collection("mobile_numbers")
                .add(mobile)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        all_number_list.add(phone_number);
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }



}
