package com.openforce.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.model.User;
import com.openforce.model.Job;
import com.openforce.model.Skill;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.Utils;
import com.openforce.widget.TextDrawable;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JobActivity extends BaseActivity {

    private static final String TAG = "JobActivity";

    private static final String EXTRA_JOB = "EXTRA_JOB";
    private ImageView closeJobScreen;
    private TextView employerName;
    private TextView jobPostedTime;
    private TextView jobSkill;
    private TextView availabilityLabel;
    private TextView startLabel;
    private TextView endLabel;
    private TextView amountLabel;
    private TextView locationLabel;
    private TextView description;
    private ImageView employerProfile;
    private ProgressBar actionJobProgress;
    private TextView actionJobButton;

    private Job job;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job);
        if (!getIntent().hasExtra(EXTRA_JOB)) {
            throw new IllegalArgumentException("You need to pass the job object");
        }

        initView();
    }

    public static Intent getIntent(Context context, Job job) {
        Intent intent = new Intent(context, JobActivity.class);
        // add your extras here
        intent.putExtra(EXTRA_JOB, job);
        return intent;
    }


    private void initView() {
        closeJobScreen = findViewById(R.id.close_job_screen);
        employerName = findViewById(R.id.employer_name);
        employerProfile = findViewById(R.id.employer_profile_picture);
        jobPostedTime = findViewById(R.id.job_posted_time);
        jobSkill = findViewById(R.id.job_skill);
        availabilityLabel = findViewById(R.id.availability_label);
        startLabel = findViewById(R.id.start_label);
        endLabel = findViewById(R.id.end_label);
        amountLabel = findViewById(R.id.amount_label);
        locationLabel = findViewById(R.id.location_label);
        actionJobButton = findViewById(R.id.action_job_button);
        actionJobProgress = findViewById(R.id.progress_action_job_button);
        description = findViewById(R.id.job_description);

        Job job = getIntent().getParcelableExtra(EXTRA_JOB);

        this.job = job;

        closeJobScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View __) {
                JobActivity.this.finish();
            }
        });
        employerName.setText(job.getEmployerName());
        setEmployerImage();
        jobPostedTime.setText(getString(R.string.posted_at, Utils.timestampToNormalDate(job.getPostedDate())));
        jobSkill.setText(job.getJobRole().getName());
        availabilityLabel.setText(job.getRequiredEmployees() + "");
        startLabel.setText(Utils.timestampToFormattedDate(job.getStartDate()));
        endLabel.setText(Utils.timestampToFormattedDate(job.getEndDate()));
        amountLabel.setVisibility(View.GONE);
        locationLabel.setText(job.getAddress());
        description.setText(job.getDescription());

        setupActionForProgress();
        if(Utils.isUserVetted(firebaseAuth, OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo())) {
            apiClient.isUserAlreadyAppliedForJob(FirebaseAuth.getInstance().getUid(), job.getId(),
                    this, new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                JobActivity.this.setupActionButtonForWithdraw();
                            } else {
                                JobActivity.this.setupActionButtonForApply();
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            JobActivity.this.setupActionButtonForApply();
                            Log.e(TAG, "Cannot check if user already applied for job", e);
                        }
                    });
        } else {
            setupActionButtonForApply();
        }

    }

    private void setupActionButtonForApply() {
        actionJobProgress.setVisibility(View.GONE);
        actionJobButton.setText(R.string.apply);
        actionJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobActivity.this.applyForJob();
            }
        });
    }

    private void setupActionForProgress() {
        actionJobProgress.setVisibility(View.VISIBLE);
        actionJobButton.setText("");
        actionJobButton.setOnClickListener(null);
    }

    private void setEmployerImage() {
        final TextDrawable placeholderEmployer = Utils.getPlaceholderForProfile(job.getEmployerName(), this);
        employerProfile.setImageDrawable(placeholderEmployer);
        apiClient.getUserProfileImage(job.getEmployerID(), new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String publicIdProfileImage) {
                if (!TextUtils.isEmpty(publicIdProfileImage)) {
                    Url baseUrl = MediaManager.get().url().publicId(publicIdProfileImage).format("jpg").type("upload");
                    MediaManager.get().responsiveUrl(employerProfile, baseUrl,
                            ResponsiveUrl.Preset.AUTO_FILL, new ResponsiveUrl.Callback() {
                                @Override
                                public void onUrlReady(Url url) {
                                    String urlGenerated = url.generate();
                                    Picasso.get().load(urlGenerated)
                                            .transform(new RoundedTransformation(JobActivity.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_small_size), 0))
                                            .placeholder(placeholderEmployer)
                                            .into(employerProfile);
                                }
                            });
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // nothing to do here
            }
        }, this);
    }

    private void applyForJob() {
        User user = OpenForceApplication.getInstance().getSecureSharedPreference().getUserInfo();
        if(Utils.isUserVetted(firebaseAuth, user)) {
            // check if user added this skill to his skillset
            boolean foundJobSkill = false;
            if (user.skills != null) {
                for (Skill skill : user.skills) {
                    if (skill.id.equals(job.getJobRole().getId())) {
                        foundJobSkill = true;

                        break;
                    }
                }
            }

            if (!foundJobSkill) {
                Snackbar.make(root, getString(R.string.skill_unset, job.getJobRole().getName()), Snackbar.LENGTH_LONG).show();
                return;
            }

            setupActionForProgress();
            apiClient.applyJob(job.getId(), this, new OnSuccessListener<HttpsCallableResult>() {
                @Override
                public void onSuccess(HttpsCallableResult httpsCallableResult) {
                    JobActivity.this.setupActionButtonForWithdraw();
                    Snackbar.make(root, R.string.successfully_applied, Snackbar.LENGTH_LONG).show();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    JobActivity.this.setupActionButtonForApply();
                    Snackbar.make(root, R.string.error_applying_job, Snackbar.LENGTH_LONG).show();
                    Log.e(TAG, "Error applying to job", e);
                }
            });
        } else {
            Snackbar.make(root, R.string.apply_unvetted, Snackbar.LENGTH_LONG).show();
        }
    }

    private void setupActionButtonForWithdraw() {
        actionJobProgress.setVisibility(View.GONE);
        actionJobButton.setText(R.string.withdraw_application);
        actionJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JobActivity.this.withdrawApplication();
            }
        });
    }

    private void withdrawApplication() {
        setupActionForProgress();
        apiClient.withdrawApplication(job.getId(), this, new OnSuccessListener<HttpsCallableResult>() {
            @Override
            public void onSuccess(HttpsCallableResult httpsCallableResult) {
                JobActivity.this.setupActionButtonForApply();
                Snackbar.make(root, R.string.successfully_withdraw, Snackbar.LENGTH_LONG).show();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                JobActivity.this.setupActionButtonForWithdraw();
                if (e instanceof FirebaseFunctionsException) {
                    Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(root, R.string.error_withdraw_job, Snackbar.LENGTH_LONG).show();
                }
                Log.e(TAG, "Error withdrawing from job", e);
            }
        });
    }
}
