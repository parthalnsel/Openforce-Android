package com.openforce.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;


import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.db.Tables;
import com.openforce.model.StripeInfo;
import com.openforce.model.User;
import com.openforce.providers.SecureSharedPreference;

import com.openforce.utils.ApiClient;
import com.stripe.Stripe;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.RateLimitException;
import com.stripe.exception.StripeException;

import com.stripe.model.Account;
import com.stripe.model.Balance;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AddPaymentFragment extends Fragment {

    private static final String TAG = "AddPaymentFragment";
    AddPaymentCallbacks callbacks;
    ImageView back_iv;
    Button save_btn , stripe_btn;
    TextView stripe_terms_tv , account_name_tv , stripe_connect_setup_tv , stripe_text_btn;
    LinearLayout change_stripe_layout;
    LinearLayout get_paid_layout;
    ImageView stripe_profile_iv;

    private Handler mHandler;

    Boolean connect=false;
    String account_email="";
    protected ApiClient apiClient;
    protected FirebaseAuth firebaseAuth;
    User user;



    private Dialog web_dialog;
    WebView mWebView;
    private ProgressDialog mSpinner;
    private String mCallbackUrl = "http://lnsel.co.in:3001/employer/signup";
    String url = "https://connect.stripe.com/oauth/authorize?client_id=ca_FPl1s22wpJgCIUacHACu2Oi1HFj3CkQo&scope=read_write&response_type=code&stripe_landing=login&redirect_uri=http://lnsel.co.in:3001/employer/signup";
    private static int SUCCESS = 0;
    private static int ERROR = 1;
    private static int PHASE1 = 1;
    private static int PHASE2 = 2;
    private static final String AUTH_URL = "https://connect.stripe.com/oauth/authorize?";
    private static final String TOKEN_URL = "https://connect.stripe.com/oauth/token";
    private static final String SCOPE = "read_write";

    String account_name="";
    String access_token="";
    String stripe_user_id="";


    public AddPaymentFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_add_payment, container, false);

        firebaseAuth = OpenForceApplication.getInstance().getFirebaseAuth();
        apiClient = OpenForceApplication.getApiClient();

        user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();

        initView(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AddPaymentCallbacks) {
            callbacks = (AddPaymentCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + AddPaymentCallbacks.class.getSimpleName());
        }
    }

    private void initView(View view) {

        account_name_tv = (TextView) view.findViewById(R.id.stripe_account_name_tv) ;
        stripe_connect_setup_tv = (TextView) view.findViewById(R.id.connect_stripe_setup_tv) ;
        back_iv = (ImageView) view.findViewById(R.id.close_button_frag_add_payment) ;
        save_btn = (Button) view.findViewById(R.id.frag_add_payment_save_btn);

        stripe_btn = (Button) view.findViewById(R.id.stripe_btn);
        stripe_text_btn = (TextView) view.findViewById(R.id.stripe_text_btn) ;

        change_stripe_layout = (LinearLayout) view.findViewById(R.id.login_diff_account_layout);
        get_paid_layout = (LinearLayout) view.findViewById(R.id.get_paid_layout);
        stripe_terms_tv = (TextView) view.findViewById(R.id.frag_add_payment_stripe_terms_tv) ;

        mSpinner = new ProgressDialog(getActivity());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");

        mHandler = new Handler();

        if (user.stripe_info!= null && user.stripe_info.size()>0){
            StripeInfo info = user.stripe_info.get(0);
            account_name = info.user_id;
            access_token = info.getAccess_token();

            System.out.println("Stripe details: " + info.getUser_id()  + "," + info.getAccess_token() + " , " + info.getId());
            stripe_user_id = access_token;
            change_stripe_layout.setVisibility(View.VISIBLE);
            account_name_tv.setText("Connected as "+ info.getAccess_token());
            stripe_connect_setup_tv.setText(R.string.connected_stripe);
            stripe_terms_tv.setVisibility(View.GONE);
            stripe_btn.setVisibility(View.GONE);

            Log.d(TAG, "onCreateView: " + account_name + "," + access_token);


        }else {

            change_stripe_layout.setVisibility(View.GONE);
            stripe_btn.setVisibility(View.VISIBLE);

            Log.d(TAG, "initView: No Stripe Info" );
        }

        stripe_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog();

            }
        });

        change_stripe_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        stripe_terms_tv.setPaintFlags(stripe_terms_tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        back_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Clicked");
                getActivity().onBackPressed();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Save Clicked" + account_name);

                if (!access_token.equals("")){
                    String uid = OpenForceApplication.getInstance().getFirebaseAuth().getCurrentUser().getUid();
                    String name = OpenForceApplication.getInstance().getFirebaseAuth().getCurrentUser().getEmail();
                    String phone = OpenForceApplication.getInstance().getFirebaseAuth().getCurrentUser().getPhoneNumber();

//                    Map<String, Object> users = new HashMap<>();
//                    users.put("name", name);
//                    users.put("uid", uid);
//                    users.put("token", phone);


                    StripeInfo stripeInfo1 = new StripeInfo(name,stripe_user_id,access_token);
                    List<StripeInfo> stripeInfos = new ArrayList<>();

                    stripeInfos = Arrays.asList(stripeInfo1);

                    Log.d(TAG, "onClick: " +stripeInfos.get(0).getAccess_token());
//                    callbacks.onSavePaymentInfo(Arrays.asList(stripeInfo1));
                    callbacks.onSavePaymentInfo(stripeInfo1);

//                    FirebaseFirestore db = FirebaseFirestore.getInstance();
//                    db.collection("users").document(uid)
//                            .collection(Tables.STRIPE_INFO).document(uid).set(users)
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//
//                                    SecureSharedPreference secureSharedPreference = OpenForceApplication.getInstance().getSecureSharedPreference();
//                                    User user = secureSharedPreference.getUserInfo();
//                                    user.stripe_info = Arrays.asList(stripeInfo1);
//                                    secureSharedPreference.setUserInfo(user);
////                                progressDialog.dismiss();
//
////                                setResult(RESULT_OK);
////                                finish();
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//
//                                }
//                            });


//                apiClient.saveUserStripeInfo("paymentinfo",__ -> {
//
//                    System.out.println("Success");
//                },e -> {
//
//                    System.out.println("failure");
//                });

//                    getActivity().setResult(Activity.RESULT_OK);
//                    getActivity().finish();

                }else {

                    Toast.makeText(getActivity(), "Please Login Stripe Account" , Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public interface AddPaymentCallbacks{
        void onPaymentCloseBtnClick();
        void onSavePaymentInfo(StripeInfo stripeInfos);
    }


    public void openDialog(){

        web_dialog = new Dialog(getActivity());
        web_dialog.setContentView(R.layout.stripe_webview);
        web_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        web_dialog.setCancelable(true);
        mWebView = (WebView) web_dialog.findViewById(R.id.webview);


        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.loadUrl("about:blank");
        WebStorage webStorage = WebStorage.getInstance();
        webStorage.deleteAllData();
        CookieSyncManager.createInstance(getActivity());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        mWebView.loadUrl(url);
        web_dialog.show();

        Log.d(TAG, "openDialog: Check" + account_name);
    }


    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);

            if (url.startsWith(mCallbackUrl)) {

                String queryString = url.replace(mCallbackUrl + "/?", "");
                Log.d(TAG, "queryString:" + queryString);
                Map<String, String> parameters = splitQuery(queryString);
                if(!url.contains("error")) {
                    Log.d(TAG, "shouldOverrideUrlLoading: Contains");
                    mSpinner.dismiss();
                    onComplete(parameters);
                    mSpinner.dismiss();
                    web_dialog.dismiss();

                }
                else {
//                    mListener.onError(parameters);
                }
//                StripeDialog.this.dismiss();
                web_dialog.dismiss();

                mWebView.destroy();
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.e(TAG, "Page error[errorCode="+errorCode+"]: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);
            Map<String, String> error = new LinkedHashMap<String, String>();
            error.put("error", String.valueOf(errorCode));
            error.put("error_description", description);
//            mListener.onError(error);
//            StripeDialog.this.dismiss();

            mWebView.destroy();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "url: " + url);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "url: " + url);
            mSpinner.dismiss();
            String title = mWebView.getTitle();
//            if (title != null && title.length() > 0) {
//                mTitle.setText(title);
//            }
        }

    }

    public static Map<String, String> splitQuery(String query) {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        try {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        catch(UnsupportedEncodingException e) {
            query_pairs.put("error", "UnsupportedEncodingException");
            query_pairs.put("error_description", e.getMessage());
        }

        return query_pairs;
    }

    // Changes
    public interface OAuthDialogListener {
        public abstract void onComplete(Map<String, String> parameters);
        public abstract void onError(Map<String, String> parameters);
    }

    OAuthDialogListener listener = new OAuthDialogListener() {
        @Override
        public void onComplete(Map<String, String> parameters) {
            System.out.println("code : ....................... "+parameters.get("code"));
            getAccessToken(parameters.get("code"));
        }

        @Override
        public void onError(Map<String, String> parameter) {
//            mListener.onFail("Authorization failed");
        }
    };

    public void onComplete(Map<String, String> parameters) {
        System.out.println("code : ....................... "+parameters.get("code"));mSpinner = new ProgressDialog(getActivity());
//        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        mSpinner.setMessage("Loading...");
//
//        mSpinner.show();

        getAccessToken(parameters.get("code"));
    }


    private void getAccessToken(final String code) {

        new Thread() {
            @Override
            public void run() {
                int what = SUCCESS;

                try {



                    URL url = new URL(TOKEN_URL);
                    String urlParameters = "code=" + code
                            + "&client_secret=" + "sk_test_WF36B7Jl16vmegwDyQSuoCUE00ei1cWfJ6"
                            + "&grant_type=authorization_code";
                    System.out.println("New Url" + url);
                    System.out.println("New Url Parameter" + urlParameters);

                    String response = executePost(TOKEN_URL, urlParameters);
                    JSONObject obj = new JSONObject(response);

                    System.out.println("Partha" + obj.toString());

                    Log.i(TAG,  obj.getString("access_token"));
                    Log.i(TAG,""+obj.getBoolean("livemode"));
                    Log.i(TAG,  obj.getString("refresh_token"));
                    Log.i(TAG, obj.getString("token_type"));
                    Log.i(TAG,  obj.getString("stripe_publishable_key"));
                    Log.i(TAG, obj.getString("stripe_user_id"));
                    Log.i(TAG,  obj.getString("scope"));


                    stripe_user_id = obj.getString("access_token");
                    access_token = obj.getString("stripe_user_id");
                    stripe_user_id= access_token;

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSpinner.show();
                            // Update your UI
//                            get_paid_layout.setVisibility(View.GONE);
                            account_name_tv.setText("Connected as "+stripe_user_id);
                            stripe_connect_setup_tv.setText(R.string.connected_stripe);

                            stripe_terms_tv.setVisibility(View.GONE);
                            stripe_btn.setVisibility(View.GONE);
                            change_stripe_layout.setVisibility(View.VISIBLE);


                        }
                    });

                    mSpinner.dismiss();
//                    getAccountName();

//                    new Connection().execute();

//                    mSpinner.dismiss();
//					mSession.storeAccessToken(obj.getString("access_token"));
//					mSession.storeRefreshToken(obj.getString("refresh_token"));
//					mSession.storePublishableKey(obj.getString("stripe_publishable_key"));
//					mSession.storeUserid(obj.getString("stripe_user_id"));
//					mSession.storeLiveMode(obj.getBoolean("livemode"));
//					mSession.storeTokenType(obj.getString("token_type"));

                    System.out.println("stripe_user_id : ....................... "+obj.getString("stripe_user_id"));
                    System.out.println("Account Name : ....................... ");



                }
                catch (Exception ex) {
                    what = ERROR;
                    ex.printStackTrace();
                }

//                mHandler.sendMessage(mHandler.obtainMessage(what, PHASE2, 0));
            }
        }.start();


    }

    private void getAccountName() {
//        account_name_tv.setText("Connected as "+stripe_user_id);



    }


    public static String executePost(String url, String parameters) throws IOException {

        URL request = new URL(url);
        Log.d(TAG, "executePost: " + parameters.getBytes().length);
        HttpURLConnection connection = (HttpURLConnection) request.openConnection();
//        connection.connect();
//        connection.getInputStream();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "StripeConnectAndroid");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
//        connection.setRequestProperty( "client_secret", "sk_test_UanoBlcdpjDwz6ZR7GERgabx");
//        connection.setRequestProperty( "code", "ac_FV4lDhPJV4IDsEhDugulOFGLXOkUAsx4");
//        connection.setRequestProperty( "grant_type", "authorization_code");
        connection.setUseCaches (false);

        Log.d(TAG, "executePost: " + connection.getOutputStream());

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
        wr.writeBytes(parameters);
        wr.flush();
        wr.close();


        String response = streamToString(connection.getInputStream());

        connection.disconnect();
        return response;

    }

    private static String streamToString(InputStream is) throws IOException {
        String str = "";

        Log.d(TAG, "streamToString: Check");
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }

        return str;
    }


    public class Connection extends AsyncTask<Void, Void, String> {



        @Override
        protected String doInBackground(Void... voids) {
            String email= "";


            try {

                Stripe.apiKey = "sk_test_ZzHWZcPB6VZoeAH9eHZJ53Ar00QElUz1h8";
                Account account = Account.retrieve("acct_1EiduhEm2wpSQLIT", null);
                Log.d("DoinBackground: ", account.getSettings().toString());
                email = account.getSettings().getDashboard().getDisplayName();


            } catch (CardException e) {
                // Since it's a decline, CardException will be caught
                System.out.println("Status is: " + e.getCode());
                System.out.println("Message is: " + e.getMessage());
            } catch (RateLimitException e) {
                // Too many requests made to the API too quickly
            } catch (InvalidRequestException e) {
                // Invalid parameters were supplied to Stripe's API
            } catch (AuthenticationException e) {
                // Authentication with Stripe's API failed
                // (maybe you changed API keys recently)
            } catch (StripeException e) {
                // Display a very generic error to the user, and maybe send
                // yourself an email
            } catch (Exception e) {
                // Something else happened, completely unrelated to Stripe
            }

            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            try {
                // Use Stripe's library to make requests...
                Stripe.apiKey = "sk_test_K4Ed3fYBEDM40XrNYT6KijKs00y4mr4Xkx";
                Account account = Account.retrieve("acct_1EiduhEm2wpSQLIT", null);
                Log.d("OnPost: ", account.getSettings().toString());

                Log.d("OnPost: ", s);

            } catch (CardException e) {
                // Since it's a decline, CardException will be caught
                System.out.println("Status is: " + e.getCode());
                System.out.println("Message is: " + e.getMessage());
            } catch (RateLimitException e) {
                // Too many requests made to the API too quickly
            } catch (InvalidRequestException e) {
                // Invalid parameters were supplied to Stripe's API
            } catch (AuthenticationException e) {
                // Authentication with Stripe's API failed
                // (maybe you changed API keys recently)
            } catch (StripeException e) {
                // Display a very generic error to the user, and maybe send
                // yourself an email
            } catch (Exception e) {
                // Something else happened, completely unrelated to Stripe
            }
        }
    }



}
