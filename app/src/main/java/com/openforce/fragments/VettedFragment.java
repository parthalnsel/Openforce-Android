package com.openforce.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.activity.BalanceActivity;
import com.openforce.activity.EmployerProfileActivity;
import com.openforce.activity.HomeActivity;
import com.openforce.activity.LandingActivity;
import com.openforce.model.PastJobResponse;
import com.openforce.model.User;
import com.openforce.adapters.PastJobAdapter;
import com.openforce.adapters.SlidePageAdapter;
import com.openforce.interfaces.PageSliderClick;

import com.openforce.providers.SecureSharedPreference;
import com.openforce.utils.ApiClient;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.UIUtils;
import com.openforce.utils.Utils;
import com.openforce.widget.TextDrawable;
import com.squareup.picasso.Picasso;
import com.stripe.Stripe;
import com.stripe.model.Balance;


import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class VettedFragment extends Fragment {

    private static final String TAG = "VettedFragment";

    private TextView employeeName;
    private RatingBar rating;
    private TextView employeeRating , current_balance;
    private ViewPager viewPager;
    private RecyclerView pastJobList;
    private ImageView avatarEmployee;
    private CircleImageView avatarCircleEmployee;
    private ViewGroup noJobLayout;
    private ProgressBar pastJobProgress;

    private PageSliderClick pageSliderClick;
    private VettedFragmentCallbacks callbacks;

    private Button btn_logout;
    private ProgressDialog progressDialog;
    private User user;


    LinearLayout profile_layout , my_balance_layout;

    private static final int PICK_IMAGE_REQUEST_CODE = 1000;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_vetted, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();
        SlidePageAdapter adapter = new SlidePageAdapter(getActivity());
        adapter.setOnItemClickListener(new SlidePageAdapter.OnPageClickedListener() {
            @Override
            public void onPageClicked(int position) {
                viewPager.setCurrentItem(position, true);
                switch (position) {
                    case 0:
                        // show experience and skills
                        pageSliderClick.onSkillPageClicked();
                        break;
                    case 1:
                        // show identity & references
                        pageSliderClick.onIdentityReferencesPageClicked();
                        break;
                    case 2:
                        // show secure account
                        pageSliderClick.onSecureAccountPageClicked();
                        break;
                    case 3:
                        // show secure account
                        pageSliderClick.onAddPaymentPageClicked();
                        break;
                }
            }
        });

        viewPager.setAdapter(adapter);
        viewPager.setClipToPadding(false);
        viewPager.setOffscreenPageLimit(3);

        employeeName.setText(user.firstName + " " + user.lastName);

        current_balance.setText(user.balance);

//        Log.d(TAG, "onViewCreated: stripe" + user.stripe_info.get(0).id );

        Log.d(TAG, "onViewCreated: balance " + user.balance);

        if (user.overallAverage != null) {
            DecimalFormat df = new DecimalFormat("#.#");
            employeeRating.setText(df.format(user.overallAverage) + "");
            rating.setRating(user.overallAverage.floatValue());
        } else {
            employeeRating.setText("No reviews yet");
            rating.setVisibility(View.GONE);
        }

        TextDrawable placeholder = Utils.getPlaceholderForProfile(user.firstName.trim() + " " + user.lastName.trim(), getActivity());
        if (user.profileImg != null) {

            Transformation transformation = new Transformation();
            transformation.gravity("face");
            Url baseUrl = MediaManager.get().url().publicId("employee/profile/" + user.imagePublicId).transformation(transformation).format("jpg").type("image");
            MediaManager.get().responsiveUrl(avatarCircleEmployee, baseUrl,
                    ResponsiveUrl.Preset.FIT, new ResponsiveUrl.Callback() {
                        @Override
                        public void onUrlReady(Url url) {

                            String urlGenerated = url.generate();
                            System.out.println("generated url: " + urlGenerated);
                            Picasso.get().load(urlGenerated)
                                    .transform(new RoundedTransformation(VettedFragment.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size), 0))
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(avatarCircleEmployee);
                        }
                    });
        } else {
            avatarCircleEmployee.setImageDrawable(placeholder);
        }

        pastJobProgress.setVisibility(View.VISIBLE);
        noJobLayout.setVisibility(View.GONE);
        pastJobList.setVisibility(View.GONE);
        loadPastJob();

    }




    private void loadPastJob() {
        ApiClient apiClient = OpenForceApplication.getApiClient();

        apiClient.getPastJob(getActivity(), new OnSuccessListener<PastJobResponse>() {
            @Override
            public void onSuccess(PastJobResponse pastJobResponse) {
                if (pastJobResponse.jobs.isEmpty()) {
                    pastJobProgress.setVisibility(View.GONE);
                    noJobLayout.setVisibility(View.VISIBLE);
                    pastJobList.setVisibility(View.GONE);
                } else {
                    pastJobProgress.setVisibility(View.GONE);
                    noJobLayout.setVisibility(View.GONE);
                    pastJobList.setVisibility(View.VISIBLE);
                    PastJobAdapter pastJobAdapter = new PastJobAdapter(VettedFragment.this.getActivity(), pastJobResponse.jobs);
//                pastJobAdapter.addAll(pastJobResponse.jobs);

                    Log.i("ParthaTest", pastJobResponse.jobs.toString());

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VettedFragment.this.getActivity());
                    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
                    pastJobList.setLayoutManager(linearLayoutManager);
                    pastJobList.setAdapter(pastJobAdapter);

                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pastJobProgress.setVisibility(View.GONE);
                noJobLayout.setVisibility(View.VISIBLE);
                pastJobList.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof PageSliderClick) {
            pageSliderClick = (PageSliderClick) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + PageSliderClick.class.getSimpleName());
        }

        if (activity instanceof VettedFragmentCallbacks) {
            callbacks = (VettedFragmentCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement " + PageSliderClick.class.getSimpleName());
        }
    }

    private void initView(View view) {
        employeeName = view.findViewById(R.id.employee_name);
        rating = view.findViewById(R.id.rating);
        employeeRating = view.findViewById(R.id.employee_rating);


        current_balance = (TextView) view.findViewById(R.id.balance_tv);
        btn_logout = (Button) view.findViewById(R.id.logout_btn);

        viewPager = view.findViewById(R.id.view_pager);
        pastJobList = view.findViewById(R.id.past_job_list);
        pastJobList.setNestedScrollingEnabled(false);

        avatarEmployee = view.findViewById(R.id.avatar_employee);
        avatarCircleEmployee = view.findViewById(R.id.avatar_circle_employee);

        pastJobProgress = view.findViewById(R.id.progress_past_job);
        noJobLayout = view.findViewById(R.id.no_past_job_layout);

        profile_layout = view.findViewById(R.id.profile_layout);
        my_balance_layout = (LinearLayout) view.findViewById(R.id.my_balance_layout);

        noJobLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onNoJobLayoutClick();
            }
        });

        avatarCircleEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                VettedFragment.this.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_CODE);
            }
        });




        my_balance_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent balanceIntent = new Intent(getActivity(), BalanceActivity.class);
                startActivity(balanceIntent);
            }
        });


        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = UIUtils.showProgress(getActivity(), "Loading...",
                        null, true, false, null);
                OpenForceApplication.getApiClient().signOut();
                SecureSharedPreference secureSharedPreference = OpenForceApplication.getInstance().getSecureSharedPreference();
                secureSharedPreference.removePassword();
//                StripeSession.Stripelogout();
//                StripeSession mSession;
//                mSession = new StripeSession(getActivity(), null);
                progressDialog.dismiss();
                startActivity(LandingActivity.getIntent(getActivity()));
                getActivity().finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE) {
            Uri selectedImage = data.getData();

            Picasso.get().load(selectedImage)
                    .transform(new RoundedTransformation(getResources().getDimensionPixelSize(R.dimen.avatar_employee_size), 0))
                    .noPlaceholder()
                    .into(avatarCircleEmployee);

            final String imagePublicID = "profile_picture_" + UUID.randomUUID().toString();
            String requestId = MediaManager.get().upload(selectedImage)
                    .maxFileSize(4 * 1024 * 1024) // 4mb
                    .unsigned(getString(R.string.cloudinary_preset))
                    .option("resource_type", "image")
                    .option("folder", "employee/profile/")
                    .option("public_id", imagePublicID)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("url");
                            saveImageUrl(url, imagePublicID);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Error uploading image: " + error.getDescription());
                            Toast.makeText(getActivity(), R.string.error_uploading_image, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {

                        }
                    })
                    .dispatch(OpenForceApplication.getInstance());

        }
    }

    private void saveImageUrl(final String url, final String imagePublicId) {
        ApiClient apiClient = OpenForceApplication.getApiClient();
        apiClient.saveProfileImage(url, imagePublicId, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void __) {
                user.profileImg = url;
                user.imagePublicId = imagePublicId;
                OpenForceApplication.getInstance().getSecureSharedPreference().setUserInfo(user);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // error saving url of image. Just log the error but no need to do anything here
                Log.e(TAG, "Error saving profile picture", e);
            }
        });
    }

    public interface VettedFragmentCallbacks {
        void onNoJobLayoutClick();
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

                    Stripe.apiKey = "sk_test_nNJkFkr3L4TVinYHFSzPP1ta00qtvWVu96";

                     Balance.retrieve();

//                    System.out.println("StripeBalance Response: " + balance.getAvailable().get(0).getAmount());
//                    System.out.println("StripeBalance : " + balance);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }


    public static Intent getIntentWithClearFlag(Context context) {
        Intent intent = new Intent(context, VettedFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

}
