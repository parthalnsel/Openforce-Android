package com.openforce.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openforce.R;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.Utils;
import com.openforce.widget.TextDrawable;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PastJobDetailsActivity extends BaseActivity {

    private static final String TAG = "PastJobDetailsActivity";
    String job_title = "" , company_name ="" , job_rating= "" , start_date="", end_date="", working_days="",
            amount_get="", payment_status ="" , reference_no="";

    String employer_id ="";
    TextView company_name_tv, job_name_tv, rating_tv, start_date_tv , end_date_tv , days_tv, amount_tv, status_tv, reference_tv;

    Button contact_help;


    ImageView employerProfile , image_close, send_email_iv;
    RatingBar rating;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_job_details);

        firebaseFirestore = FirebaseFirestore.getInstance();

        employer_id = getIntent().getExtras().getString("employer_id");
        job_title = getIntent().getExtras().getString("job_name");
        company_name = getIntent().getExtras().getString("employer_name") ;
        job_rating =  getIntent().getExtras().getString("rating");
        start_date =   getIntent().getExtras().getString("start_date");
        end_date =  getIntent().getExtras().getString("end_date");

        working_days = getIntent().getExtras().getString("total_days");
        if (Integer.valueOf(working_days)<0){
            working_days ="0";
        }
        amount_get = getIntent().getExtras().getString("amount") ;
        payment_status = getIntent().getExtras().getString("status");
        reference_no = getIntent().getExtras().getString("reference");


        Log.d("employer_id" , employer_id);
        Log.d("Job Name" ,job_title );
        Log.d("employer_name" , company_name);
        Log.d("rating" ,job_rating );
        Log.d("start_date" , start_date);
        Log.d("end_date" , end_date);
        Log.d("total_days" , working_days);
        Log.d("amount" , amount_get);
        Log.d("status" , payment_status);
        Log.d("reference" , reference_no);


        intView();
    }

    private void intView() {

        contact_help = (Button) findViewById(R.id.button_help);
        image_close = (ImageView) findViewById(R.id.close_button_iv);
        send_email_iv = (ImageView) findViewById(R.id.send_email);
        employerProfile = (ImageView) findViewById(R.id.company_pic_iv) ;

        setEmployerImage();

        company_name_tv = (TextView) findViewById(R.id.company_name_tv);
        company_name_tv.setText(company_name);

        job_name_tv = (TextView) findViewById(R.id.job_name_tv);
        job_name_tv.setText(job_title);

        rating = (RatingBar) findViewById(R.id.rating_job_details);
        rating.setRating(Float.valueOf(job_rating));

        rating_tv = (TextView) findViewById(R.id.rating_tv);
        rating_tv.setText(job_rating);

        start_date_tv =(TextView) findViewById(R.id.start_date_tv);
        start_date_tv.setText(start_date);

        end_date_tv = (TextView) findViewById(R.id.end_date_tv);
        end_date_tv.setText(end_date);

        days_tv = (TextView) findViewById(R.id.no_of_days_worked_tv);
        days_tv.setText(working_days + " Days");

        amount_tv = (TextView) findViewById(R.id.amount_get_tv);
        amount_tv.setText("£" + amount_get);

        status_tv = (TextView) findViewById(R.id.payment_status_tv);
        if (payment_status.equalsIgnoreCase("false")){
            status_tv.setText("Incomplete");
            status_tv.setTextColor(this.getResources().getColor(R.color.orange));
        }else {
            status_tv.setText("Complete");
            status_tv.setTextColor(this.getResources().getColor(R.color.background_profile));
        }


        reference_tv = (TextView) findViewById(R.id.reference_no_tv);
        reference_tv.setText(reference_no);

        image_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        contact_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmail();
            }
        });

        send_email_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmail();
            }
        });


    }


    private void setEmployerImage() {
        final TextDrawable placeholderEmployer = Utils.getPlaceholderForProfile(company_name, this);
        employerProfile.setImageDrawable(placeholderEmployer);
        apiClient.getUserProfileImage(employer_id, new OnSuccessListener<String>() {
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
                                            .transform(new RoundedTransformation(PastJobDetailsActivity.this.getResources().getDimensionPixelSize(R.dimen.avatar_employee_small_size), 0))
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


    public void openEmail(){

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","amol@joinopenforce.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
