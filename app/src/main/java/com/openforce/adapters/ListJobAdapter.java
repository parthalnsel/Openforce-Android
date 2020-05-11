package com.openforce.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.openforce.R;
import com.openforce.activity.JobActivity;
import com.openforce.model.Job;
import com.openforce.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListJobAdapter extends RecyclerView.Adapter<ListJobAdapter.ListJobViewHolder> {

    private List<Job> currentJobList;
    Context context;

    public ListJobAdapter(Context context, List<Job> currentJobList) {
        this.currentJobList = currentJobList;
        this.context=context;
    }

    @NonNull
    @Override
    public ListJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cust_list_job_map, parent, false);

        ListJobViewHolder roleViewHolder = new ListJobViewHolder(view);

        return roleViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ListJobViewHolder holder, int position) {
        final Job job=currentJobList.get(position);
        holder.txt_labour.setText(job.getJobRole().getName());
        holder.txt_place.setText(job.getRequiredEmployees() + " places");


        String start_date_str=Utils.timestampToFormattedDate(job.getStartDate());
        String end_date_str=Utils.timestampToFormattedDate(job.getEndDate());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start_date = null , end_date = null;

        try {
            start_date = format.parse(start_date_str);
            end_date = format.parse(end_date_str);

            Long diff = end_date.getTime() - start_date.getTime();
            Long diffDay = diff/(24 * 60 * 60 *1000);


            System.out.println("timestamp" + "Difference Days"+ diffDay);
            holder.txt_days.setText(String.valueOf(diffDay)+" days");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat inputFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat1 = new SimpleDateFormat("yyyy/MM/dd");
        String inputDateStr=start_date_str;
        Date date1 = null;
        try {
            date1 = inputFormat1.parse(inputDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outputDateStr1 = outputFormat1.format(date1);

        holder.txt_start_date.setText(outputDateStr1);


        DateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat2 = new SimpleDateFormat("yyyy/MM/dd");
        String inputDateStr2=end_date_str;
        Date date2 = null;
        try {
            date2 = inputFormat2.parse(inputDateStr2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outputDateStr2 = outputFormat2.format(date2);
        holder.txt_end_date.setText(outputDateStr2);

        holder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(JobActivity.getIntent(context, job));
            }
        });

        holder.txt_labour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(JobActivity.getIntent(context, job));
            }
        });

    }

    @Override
    public int getItemCount() {
        return currentJobList.size();
    }

    public static class ListJobViewHolder extends RecyclerView.ViewHolder{

        public ImageView profile_image;
        public TextView txt_labour,txt_place,txt_start_date,txt_end_date,txt_days;
        public RelativeLayout rl_item;


        public ListJobViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image=(ImageView)itemView.findViewById(R.id.profile_image);
            txt_labour=(TextView)itemView.findViewById(R.id.txt_labour);
            txt_place=(TextView)itemView.findViewById(R.id.txt_place);
            txt_start_date=(TextView)itemView.findViewById(R.id.txt_start_date);
            txt_end_date=(TextView)itemView.findViewById(R.id.txt_end_date);
            txt_days=(TextView)itemView.findViewById(R.id.txt_days);
            rl_item=(RelativeLayout)itemView.findViewById(R.id.rl_item);

        }
    }
}
