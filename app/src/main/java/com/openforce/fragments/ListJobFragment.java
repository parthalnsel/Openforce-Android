package com.openforce.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.openforce.OpenForceApplication;
import com.openforce.R;
import com.openforce.adapters.ListJobAdapter;
import com.openforce.model.BoundingBoxCoordinate;
import com.openforce.model.Job;
import com.openforce.model.JobsResponse;
import com.openforce.utils.UIUtils;
import com.openforce.utils.Utils;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.openforce.utils.Utils.LONDON_LATITUDE;
import static com.openforce.utils.Utils.LONDON_LONGITUDE;

public class ListJobFragment extends Fragment {

    RecyclerView recyle_list_job;
    ListJobAdapter listJobAdapter;
    private List<Job> currentJobList = new ArrayList<>();
    private List<Job> currentDateJobList = new ArrayList<>();
    private static final String TAG = "ListJobFragment";
    private LocalDateTime currentSelectedDate = LocalDateTime.now();
    private ProgressBar actionJobProgress;
    private TextView txt_no_job;
    String selectedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_job_frag, container, false);
        initView(view);

        return view;
    }

    private void initView(View view){
        final ProgressDialog progressDialog = UIUtils.showProgress(getActivity(), getString(R.string.loading), null, true, false, null);

        selectedDate = getArguments().getString("date");
        System.out.println("Passing Date: " + selectedDate);
        recyle_list_job=(RecyclerView)view.findViewById(R.id.recyle_list_job);
        actionJobProgress=(ProgressBar)view.findViewById(R.id.progress_action_job_button);
        txt_no_job=(TextView)view.findViewById(R.id.txt_no_job);

        actionJobProgress.setVisibility(View.VISIBLE);
        final LocalDateTime localDateTime = currentSelectedDate;

        long timestamp = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000;
        BoundingBoxCoordinate boundingBoxCoordinate = Utils
                .calculateBoundingBoxFromCoordinate(LONDON_LATITUDE, LONDON_LONGITUDE, 10000);
        OpenForceApplication.getApiClient().getMapJobs(boundingBoxCoordinate, timestamp,
                new OnSuccessListener<JobsResponse>() {
                    @Override
                    public void onSuccess(JobsResponse jobsResponse) {
                        actionJobProgress.setVisibility(View.GONE);
                        currentJobList = jobsResponse.getJobs();
                        for (int i = 0; i < currentJobList.size(); i++) {
                            long currentDate = currentJobList.get(i).getStartDate();
                            String start_date_str = Utils.timestampToFormattedDate(currentJobList.get(i).getStartDate());

                            DateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                            DateFormat outputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                            String inputDateStr = start_date_str;
                            Date date1 = null;
                            try {
                                date1 = inputFormat1.parse(inputDateStr);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            String outputDateStr1 = outputFormat1.format(date1);
                            System.out.println("Partha Job Date: " + outputDateStr1 + " Click Date: " + localDateTime.toLocalDate().toString());


                            if (outputDateStr1.equalsIgnoreCase(selectedDate)) {
                                System.out.println("Adding");
                                Job job = currentJobList.get(i);

                                currentDateJobList.add(job);
                            } else {
                                System.out.println("Not Adding");
                            }
//                        currentDateJobList.add(currentJobList.get(i));

                        }

                        listJobAdapter = new ListJobAdapter(ListJobFragment.this.getActivity(), currentDateJobList);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(ListJobFragment.this.getActivity());
                        recyle_list_job.setLayoutManager(mLayoutManager);
                        recyle_list_job.setAdapter(listJobAdapter);
                        if (currentDateJobList.size() > 0) {
                            txt_no_job.setVisibility(View.GONE);
                        } else {
                            txt_no_job.setVisibility(View.VISIBLE);
                        }

                        progressDialog.dismiss();

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        actionJobProgress.setVisibility(View.GONE);
                        Log.e(TAG, "Error retrieving jobs", error);
                    }
                }, getActivity());

    }
}
