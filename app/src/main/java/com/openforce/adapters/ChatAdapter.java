package com.openforce.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.openforce.R;
import com.openforce.model.Message;
import com.openforce.model.MessageType;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static final String URL_STATIC_MAP = "http://maps.google.com/maps/api/staticmap?center="  + "%s,%s" + "&zoom=15&size=%sx%s&sensor=false&key=%s";

    private static final int APPLIED_VIEW_TYPE = 0;
    private static final int CHECKIN_VIEW_TYPE = 1;
    private static final int HIRED_VIEW_TYPE = 2;
    private static final int JOBEND_VIEW_TYPE = 3;
    private static final int JOBLOCATION_VIEW_TYPE = 4;
    private static final int AMOUNT_CREDIT_VIEW_TYPE = 5;
    private static final int UNKNOWN = 999;

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

//    public ChatAdapter(ArrayList<Object> objects) {
//    }

    public void addMessages(List<Message> messages) {
        messageList.addAll(messages);
        notifyDataSetChanged();
    }


    public List<Message> getMessageList() {
        return messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case APPLIED_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.centered_message, parent, false);
                return new CenteredMessageViewHolder(view);
            case CHECKIN_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.checkin_message, parent, false);
                return new CheckedInMessageViewHolder(view);
            case HIRED_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.hired_message, parent, false);
                return new HiredMessageViewHolder(view);
            case JOBEND_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.centered_message, parent, false);
                return new CenteredMessageViewHolder(view);
            case JOBLOCATION_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.location_message, parent, false);
                return new JobLocationMessageViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        switch (message.body.type) {
            case MessageType.APPLIED:
                bindAppliedView(message, (CenteredMessageViewHolder) holder);
                break;
            case MessageType.CHECKIN:
                bindCheckInView(message, (CheckedInMessageViewHolder) holder);
                break;
            case MessageType.HIRED:
                bindHiredView(message, (HiredMessageViewHolder) holder);
                break;
            case MessageType.JOBEND:
                bindJobEndMessage(message, (CenteredMessageViewHolder) holder);
                break;
            case MessageType.LOCATION:
                bindLocationMessage(message, (JobLocationMessageViewHolder) holder);
                break;
        }
    }

    private void bindLocationMessage(Message message, JobLocationMessageViewHolder holder) {
        holder.jobLocation.setText(message.body.location.address);
        holder.bottomLabel.setText(Utils.timestampToFormattedDateTime(message.timestamp));
        String keyApiStatic = holder.itemView.getContext().getString(R.string.google_maps_static_key);
        int imageWidth = holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.map_location_width);
        int imageHeight = holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.map_location_height);
        String imageUrl = formatUrlStaticMap(message.body.location.latitude + "",
                message.body.location.longitude + "", imageWidth + "", imageHeight + "", keyApiStatic);
        Picasso.get()
                .load(imageUrl)
                .resize(imageWidth, imageHeight)
                .centerCrop()
                .transform(new RoundedTransformation(holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.map_location_corner_radius), 0))
                .into(holder.staticMapLocation);
    }

    private void bindJobEndMessage(Message message, CenteredMessageViewHolder holder) {
        holder.bodyText.setText(message.body.text);
    }

    private void bindHiredView(Message message, HiredMessageViewHolder holder) {
        holder.jobRoleName.setText(message.body.jobRole);
        holder.amount.setText(message.body.amountFormatted);
        holder.bottomLabel.setText(R.string.congratulations_hired);
    }

    private void bindCheckInView(Message message, CheckedInMessageViewHolder holder) {
        holder.bottomLabel.setText(Utils.timestampToFormattedDateTime(message.timestamp));
    }

    private void bindAppliedView(Message message, CenteredMessageViewHolder holder) {
        holder.bodyText.setText(message.body.text);
    }


    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        switch (message.body.type) {
            case MessageType.APPLIED:
                return APPLIED_VIEW_TYPE;
            case MessageType.CHECKIN:
                return CHECKIN_VIEW_TYPE;
            case MessageType.HIRED:
                return HIRED_VIEW_TYPE;
            case MessageType.JOBEND:
                return JOBEND_VIEW_TYPE;
            case MessageType.LOCATION:
                return JOBLOCATION_VIEW_TYPE;
                default:
                    return UNKNOWN;
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(Message message) {
        messageList.add(0, message);
        notifyDataSetChanged();
    }

    public static class JobLocationMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView jobLocation;
        public ImageView staticMapLocation;
        public TextView bottomLabel;

        public JobLocationMessageViewHolder(View v) {
            super(v);
            jobLocation = v.findViewById(R.id.job_location);
            staticMapLocation = v.findViewById(R.id.map_static_location);
            bottomLabel = v.findViewById(R.id.message_bottom_label);
        }
    }

    public static class CheckedInMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView bottomLabel;

        public CheckedInMessageViewHolder(View v) {
            super(v);
            bottomLabel = v.findViewById(R.id.message_bottom_label);
        }
    }

    public static class CenteredMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView bodyText;

        public CenteredMessageViewHolder(View v) {
            super(v);
            bodyText = v.findViewById(R.id.text_message);
        }
    }

    public static class HiredMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView jobRoleName;
        public TextView amount;
        public TextView bottomLabel;

        public HiredMessageViewHolder(View v) {
            super(v);
            jobRoleName = v.findViewById(R.id.job_role_label);
            amount = v.findViewById(R.id.amount_label);
            bottomLabel = v.findViewById(R.id.message_bottom_label);
        }
    }

    private String formatUrlStaticMap(String latitude, String longitude, String width, String height, String key) {
        return String.format(URL_STATIC_MAP, latitude, longitude, width, height, key);
    }
}
