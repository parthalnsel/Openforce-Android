package com.openforce.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openforce.R;

import androidx.annotation.Nullable;

public class MarkerView extends LinearLayout {

    private TextView markerViewRoleLabel;
    private TextView markerPayPerDay;
    private TextView markerNumberEmployees;

    public MarkerView(Context context) {
        super(context);
        init(context);
    }

    public MarkerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MarkerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_marker, this);
        markerViewRoleLabel = view.findViewById(R.id.marker_view_role_label);
        markerPayPerDay = view.findViewById(R.id.marker_pay_per_day);
        markerNumberEmployees = view.findViewById(R.id.marker_number_employees);
    }


    public void setRole(String role) {
        markerViewRoleLabel.setText(role);
    }

    public void setPayPerDay(String payPerDay) {
        markerPayPerDay.setText(payPerDay);
    }

    public void setNumberEmployees(String numberEmployees) {
        markerNumberEmployees.setText(numberEmployees);
    }
}
