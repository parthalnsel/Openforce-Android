package com.openforce.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openforce.R;
import com.openforce.utils.RoundedTransformation;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;

public class JobPreviewView extends LinearLayout {

    private TextView jobRoleLabel;
    private TextView payPerDayLabel;
    private TextView availabilityLabel;
    private TextView startLabel;
    private TextView endLabel;
    private ImageView employerProfile;

    public JobPreviewView(Context context) {
        super(context);
        init(context);
    }

    public JobPreviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public JobPreviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_job_preview, this);
        jobRoleLabel = view.findViewById(R.id.job_role_label);
        payPerDayLabel = view.findViewById(R.id.pay_per_day_label);
        availabilityLabel = view.findViewById(R.id.availability_label);
        startLabel = view.findViewById(R.id.start_label);
        endLabel = view.findViewById(R.id.end_label);
        employerProfile = view.findViewById(R.id.employer_profile_picture);
    }

    public void setJobRole(String jobRole) {
        jobRoleLabel.setText(jobRole);
    }

    public void setPayPerDay(String payPerDay) {
        payPerDayLabel.setText(payPerDay);
    }

    public void setAvailability(String availability) {
        availabilityLabel.setText(availability);
    }

    public void setStartDate(String startDate) {
        startLabel.setText(startDate);
    }

    public void setEndDate(String endDate) {
        endLabel.setText(endDate);
    }

    public void setEmployerProfileImage(Drawable placeholder, String url) {
        Picasso.get().setLoggingEnabled(true);
        Picasso.get().load(url)
                .transform(new RoundedTransformation(getResources().getDimensionPixelSize(R.dimen.avatar_employee_small_size), 0))
                .placeholder(placeholder)
                .into(employerProfile);
    }

    public void setEmployerProfileImage(Drawable image) {
        employerProfile.setImageDrawable(image);
    }
}
