package com.openforce.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.db.Tables;
import com.openforce.model.User;
import com.openforce.utils.UIUtils;
import com.stripe.Stripe;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Balance;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Customer;
import com.stripe.model.Payout;
import com.stripe.model.PayoutCollection;
import com.stripe.net.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceActivity extends BaseActivity {

    private static final String TAG = "BalanceActivity";
    Button confirm_transfer_btn;
    String stripe_id="" ,amount = "";
    ImageView img_back , withdraw_processing_gif;
    TextView employee_name, stripe_balance , balance_withdraw_tv;
    LinearLayout balance_details_layout, transfer_btn_layout;
    FirebaseFirestore firebaseFirestore;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        firebaseFirestore = FirebaseFirestore.getInstance();
        user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();
        stripe_id = user.stripe_info.get(0).getAccess_token();
        System.out.println("Stripe Access Toekn" + user.stripe_info.get(0).getAccess_token());
        System.out.println("Stripe test code: " + user.stripe_info.get(0).getUser_id());
        if (Build.VERSION.SDK_INT>=21){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.sun_yellow));
        }

      Query query =  firebaseFirestore.collection(Tables.TABLE_CHATS).document("7figENyLq73qD3Tpaasd").collection(Tables.TABLE_MESSAGES).limit(10);

               query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add(document.getId());
                    }
                    Log.d(TAG, list.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting documents: ");
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        getCurrentBalance();
//        getAllPayout();
//        transferBalance();
        employee_name = (TextView) findViewById(R.id.stripe_account_name);
        stripe_balance = (TextView) findViewById(R.id.amount_tv);
        balance_withdraw_tv = findViewById(R.id.balance_withdraw_text);
        balance_details_layout = (LinearLayout) findViewById(R.id.balance_layout) ;
        transfer_btn_layout = (LinearLayout) findViewById(R.id.confirm_transfer_btn_layout);
        confirm_transfer_btn = (Button) findViewById(R.id.confirm_transfer_btn);
        img_back = (ImageView) findViewById(R.id.image_back);
        withdraw_processing_gif = (ImageView) findViewById(R.id.amount_withdraw_processing_gif);
        Glide.with(this).asGif().load(R.drawable.withdraw_procceing).into(withdraw_processing_gif);

//        stripe_balance.setText("£" + "0");
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        confirm_transfer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Integer.valueOf(amount)<100){
                    Snackbar.make(root, "Your amount is too low for withdrawal", Snackbar.LENGTH_LONG).show();
                }else {
                    final ProgressDialog progressDialog = UIUtils.showProgress(BalanceActivity.this, "Transferring amount to bank", null, true, false, null);

                    Window window = BalanceActivity.this.getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(BalanceActivity.this.getResources().getColor(R.color.colorPrimary));
                    balance_details_layout.setVisibility(View.GONE);
                    transfer_btn_layout.setVisibility(View.GONE);
                    withdraw_processing_gif.setVisibility(View.VISIBLE);


                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            try {
//                            Stripe.apiKey = "sk_test_smlcJDmD7B7hmVXVQA88yC5000UCXc9Rqb";
////                RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(CONNECTED_STRIPE_ACCOUNT_ID).build();
//
//                            Map<String, Object> params = new HashMap<>();
//                            params.put("amount", 102);
//                            params.put("currency", "gbp");
//                            Payout.create(params);

                                sleep(5000);
                                synchronized (this) {
                                    wait(3000);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "run: ");
                                            withdraw_processing_gif.setVisibility(View.GONE);
                                            balance_details_layout.setVisibility(View.VISIBLE);
                                            transfer_btn_layout.setVisibility(View.VISIBLE);
//                                        loadingText.setVisibility(View.INVISIBLE);
//                                            stripe_balance.setText("£ " + "0.00");
                                            progressDialog.dismiss();
                                        }
                                    });

                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                        catch (StripeException e) {
//                            System.out.println("error"+e.getStripeError());
//                            e.printStackTrace();
//                        }

                        };
                    };
                    thread.start();

                    progressDialog.dismiss();
                }

            }
        });



    }


    public void getBalance(){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
//                    Stripe.apiKey = "sk_test_nNJkFkr3L4TVinYHFSzPP1ta00qtvWVu96";
//
//                    Account account =
//                            Account.retrieve("acct_1EiduhEm2wpSQLIT");
//                    System.out.println("account Response: " + account);

                    Stripe.apiKey = "sk_test_smlcJDmD7B7hmVXVQA88yC5000UCXc9Rqb";

                    Balance balanceTransaction =
                            Balance.retrieve().retrieve();

//                    System.out.println("balance Response: " + balance.getAvailable().get(0).getAmount());
                    System.out.println("balance : " + balanceTransaction.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }


    public void getCurrentBalance(){

        final ProgressDialog progressDialog = UIUtils.showProgress(BalanceActivity.this, "Loading balance", null, true, false, null);

        String url = "http://lnsel.co.in:3001/stripebalance";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        Log.d("Responsesid", response);

                        try {
                            JSONObject balanceObj = new JSONObject(response);
                            JSONArray availableBalanceAry = balanceObj.getJSONArray("available");
                            JSONObject amountObj = availableBalanceAry.getJSONObject(0);
                            amount = amountObj.getString("amount");
                            System.out.println("Balance is: " + amount);
                            Toast.makeText(BalanceActivity.this, "Balance: " + amount , Toast.LENGTH_SHORT).show();
                            stripe_balance.setText("£" + amount);
                            progressDialog.dismiss();
                            String withdrawText = "The transfer will only be actioned once  you click confirm transfer.\n \nThe following amount [£"+ amount + "] will be transferred to your account stripe account within the next x working days.";
                            balance_withdraw_tv.setText(withdrawText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                        ;

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        System.out.println("Error=========="+error);
                        Toast.makeText(BalanceActivity.this, "Have a Network Error Please check Internet Connection.", Toast.LENGTH_LONG).show();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("stripeId",stripe_id);
                return params;
            }
        }
                ;
        // Adding request to volley request queue

        postRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //adding the string request to request queue
        requestQueue.add(postRequest);
//        AppController.getInstance().addToRequestQueue(postRequest);
//        AppController.getInstance().getRequestQueue().getCache().remove(url);
//        AppController.getInstance().getRequestQueue().getCache().clear();
    }

    public void getAllPayout(){

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
//                    Stripe.apiKey = "sk_test_nNJkFkr3L4TVinYHFSzPP1ta00qtvWVu96";
//
//                    Account account =
//                            Account.retrieve("acct_1EiduhEm2wpSQLIT");
//                    System.out.println("account Response: " + account);

                    Stripe.apiKey = "sk_test_smlcJDmD7B7hmVXVQA88yC5000UCXc9Rqb";

                    Map<String, Object> params = new HashMap<>();
                    params.put("limit", 3);

                    PayoutCollection payouts = Payout.list(params);

                    System.out.println("All Payout: " + payouts);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }


    private void transferBalance() {


//        try {
//            Stripe.apiKey = "sk_test_nNJkFkr3L4TVinYHFSzPP1ta00qtvWVu96";
////                RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(CONNECTED_STRIPE_ACCOUNT_ID).build();
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("amount", 5000);
//            params.put("currency", "usd");
//            params.put("source_type", "bank_account");
//            Payout.create(params);
//        } catch (StripeException e) {
//            e.printStackTrace();
//        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    Stripe.apiKey = "sk_test_nNJkFkr3L4TVinYHFSzPP1ta00qtvWVu96";
//                RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(CONNECTED_STRIPE_ACCOUNT_ID).build();

                    Map<String, Object> params = new HashMap<>();
                    params.put("amount", 10);
                    params.put("currency", "usd");
                    params.put("source_type", "card");
                    Payout payout = Payout.create(params);

                    System.out.println("payout Response: " + payout);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

//        new Connection().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



}
