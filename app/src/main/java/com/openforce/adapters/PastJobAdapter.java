package com.openforce.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.openforce.R;
import com.openforce.activity.PastJobDetailsActivity;
import com.openforce.model.PastJob;
import com.openforce.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PastJobAdapter extends RecyclerView.Adapter<PastJobAdapter.PastJobViewHolder> {
    String review="";
    boolean hasRated = false;
    private Context context;
    private List<PastJob> listPastJob = new ArrayList<>();

    private FirebaseFirestore firebaseFirestore;

    public PastJobAdapter(Context context, List<PastJob> listPastJob) {
        this.context = context;
        this.listPastJob = listPastJob;
    }

    @NonNull
    @Override
    public PastJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_past_job, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        PastJobViewHolder roleViewHolder = new PastJobViewHolder(view);

            return roleViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PastJobViewHolder holder, int position) {
        final PastJob pastJob = listPastJob.get(position);


        holder.title.setText(pastJob.getJobRole().getName() + ", " + pastJob.getEmployerName());
        String startDate = Utils.timestampToFormattedDatePastJob(pastJob.getStartDate());
        String endedDate = Utils.timestampToFormattedDatePastJob(pastJob.endTimeStamp);
        holder.date.setText(startDate + " - " + endedDate);

        String payRate = pastJob.getPayRate().toString();

        Log.d("parthapayrate" , payRate);

        ResourceBundle resourceBundle;
        String employerHasReviewed = pastJob.getEmployerHasReviewed();

        if (employerHasReviewed!= null){
            hasRated = true;
            String review1 = pastJob.getEmployerReview().getReview1();
            String review2 = pastJob.getEmployerReview().getReview2();
            String review3 = pastJob.getEmployerReview().getReview3();
            String review4 = pastJob.getEmployerReview().getReview4();
            String review5 = pastJob.getEmployerReview().getReview5();

            review = String.valueOf((Integer.valueOf(review1)+Integer.valueOf(review2)+Integer.valueOf(review3)
                    +Integer.valueOf(review4) +Integer.valueOf(review5))/5);

        }


        if (position == 0) {
            holder.itemView.setPadding( holder.itemView.getPaddingLeft(),
                    holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.padding_top_item_past_job),
                    holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
        } else {
            holder.itemView.setPadding( holder.itemView.getPaddingLeft(),
                    0,
                    holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
        }

        // TODO add correct rating here
        if (hasRated == false) {
            holder.layoutProgressBar.setVisibility(View.GONE);
            holder.unratedLabel.setVisibility(View.VISIBLE);
        } else {
            holder.layoutProgressBar.setVisibility(View.VISIBLE);
            holder.unratedLabel.setVisibility(View.GONE);
            holder.ratingLabel.setText(review);
            holder.ratingBar.setRating(Float.valueOf(review));
        }
        if (position == getItemCount() - 1) {
            // last position reached, hide bottom separator
            holder.separator.setVisibility(View.GONE);
        }

        String job_id = pastJob.getId();
        Log.d("jobId", job_id);
        final String total_days =  Utils.calculateDateFromTimestamp(pastJob.getStartDate() ,pastJob.endTimeStamp);
        Log.d("totalDays" , total_days);


        System.out.println("EmployerReview: " + review);


        final String amount = String.valueOf(Double.valueOf(total_days) * Double.valueOf(payRate));
        Log.d("totalAmount" , amount);

        holder.past_job_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PastJobDetailsActivity.class);
                String reviews="";
                if (pastJob.getEmployerHasReviewed()!= null){

                    String review1 = pastJob.getEmployerReview().getReview1();
                    String review2 = pastJob.getEmployerReview().getReview2();
                    String review3 = pastJob.getEmployerReview().getReview3();
                    String review4 = pastJob.getEmployerReview().getReview4();
                    String review5 = pastJob.getEmployerReview().getReview5();

                   reviews = String.valueOf((Integer.valueOf(review1)+Integer.valueOf(review2)+Integer.valueOf(review3)
                            +Integer.valueOf(review4) +Integer.valueOf(review5))/5);

                }else{
                    reviews = "0";
                }
                intent.putExtra("job_id" , pastJob.getId());
                intent.putExtra("employer_id" , pastJob.getEmployerID());
                intent.putExtra("employer_name" , pastJob.getEmployerName());
                intent.putExtra("rating" , reviews);
                intent.putExtra("job_name" , pastJob.getJobRole().getName() );
                intent.putExtra("start_date" , Utils.timestampToDate(pastJob.getStartDate()));
                intent.putExtra("end_date" , Utils.timestampToDate(pastJob.endTimeStamp));
                intent.putExtra("total_days" , total_days);
                intent.putExtra("amount" , amount);
                intent.putExtra("status" , "false");
                intent.putExtra("reference" , pastJob.getId());
                context.startActivity(intent);
            }
        });

    }

    public void addAll(List<PastJob> listPastJob) {
        this.listPastJob.addAll(listPastJob);
    }

    @Override
    public int getItemCount() {
        return listPastJob.size();

    }

    public static class PastJobViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView date;
        public ViewGroup layoutProgressBar;
        public TextView ratingLabel;
        public RatingBar ratingBar;
        public TextView unratedLabel;
        public View separator;
        public LinearLayout past_job_layout;

        public PastJobViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.job_role_employer_name);
            date = v.findViewById(R.id.date_label);
            layoutProgressBar = v.findViewById(R.id.rated_layout);
            ratingLabel = v.findViewById(R.id.rating_number);
            ratingBar = v.findViewById(R.id.rating);
            unratedLabel = v.findViewById(R.id.not_rated_label);
            separator = v.findViewById(R.id.separator);
            past_job_layout = v.findViewById(R.id.past_job_layout);
        }
    }
}
