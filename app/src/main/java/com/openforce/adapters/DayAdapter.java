package com.openforce.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openforce.R;
import com.openforce.model.Job;
import com.openforce.utils.Utils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.TextStyle;

import java.util.Calendar;
import java.util.Locale;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private OnDaySelectedListener listener;
    private int selectedPos = 0;
    private Job currentJob;

    public DayAdapter(OnDaySelectedListener listener, @Nullable Job currentJob) {
        this.listener = listener;
        this.currentJob = currentJob;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_item, parent, false);

        DayViewHolder roleViewHolder = new DayViewHolder(view);

        return roleViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final DayViewHolder holder, int position) {
        if (position == selectedPos) {
            holder.selector.setVisibility(View.VISIBLE);
        } else {
            holder.selector.setVisibility(View.INVISIBLE);
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        if (currentJob == null) {
            localDateTime = localDateTime.plusDays(position + 1); // start from tomorrow if no current job
        } else {
            localDateTime = localDateTime.plusDays(position); // start from tomorrow if no current job
        }

        if (position == 0) {
            if (currentJob != null) {
                holder.label1.setText(localDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase());
                holder.label2.setText(localDateTime.getDayOfMonth() + "");
            } else {
                holder.label1.setText(localDateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase());
                holder.label2.setText(R.string.tomorrow);
            }
        } else if (localDateTime.getDayOfMonth() == 1) {
            holder.label1.setText(localDateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()).toUpperCase());
            holder.label2.setText(localDateTime.getDayOfMonth() + "");
        } else {
            holder.label1.setText(localDateTime.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase());
            holder.label2.setText(localDateTime.getDayOfMonth() + "");
        }

        if (currentJob == null || !Utils.isBetween(Utils.fromTimeStamp(localDateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()).getTime(),
                Utils.fromTimeStamp(currentJob.getStartDate()).getTime(),
                Utils.fromTimeStamp(currentJob.getEndDate()).getTime())) {
            holder.content.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.dark_blue));
            holder.label1.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.label2.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.content.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.sun_yellow));
            holder.label1.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.label2.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();

                LocalDateTime date = LocalDateTime.now();
                date = date.plusDays(adapterPosition + 1);
                int previousSelectedPosition = selectedPos;
                selectedPos = adapterPosition;
                System.out.println("Partha: date: " + date.toString() + " Position: " + selectedPos);
                listener.onDaySelected(date, adapterPosition, previousSelectedPosition);

            }
        });
    }

    @Override
    public int getItemCount() {
        return 365;
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView label1;
        public TextView label2;
        public View selector;
        public ViewGroup content;

        public DayViewHolder(View v) {
            super(v);
            label1 = v.findViewById(R.id.label_day_1);
            label2 = v.findViewById(R.id.label_day_2);
            selector = v.findViewById(R.id.selector_indicator);
            content = v.findViewById(R.id.day_item_content);
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(LocalDateTime localDateTime, int position, int previousSelectedPosition);
    }
}
