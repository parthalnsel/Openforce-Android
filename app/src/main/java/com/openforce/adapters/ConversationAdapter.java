package com.openforce.adapters;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;
import com.openforce.R;
import com.openforce.model.Conversation;
import com.openforce.model.MessageType;
import com.openforce.model.Role;
import com.openforce.utils.ApiClient;
import com.openforce.utils.RoundedTransformation;
import com.openforce.utils.Utils;
import com.openforce.widget.TextDrawable;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private static final String TAG = "ConversationAdapter";

    private List<Conversation> conversations;
    private ConversationItemClickListener clickListener;

    public ConversationAdapter(@NonNull List<Conversation> conversations) {
        this.conversations = conversations;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.conversation_item, parent, false);

        ConversationViewHolder conversationViewHolder = new ConversationViewHolder(view);

        return conversationViewHolder;
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, final int position) {
        final Conversation conversation = conversations.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConversationAdapter.this.clickListener != null) {
                    clickListener.onItemClick(conversation, position);
                }
            }
        });

        holder.roleLabel.setText(conversation.jobRole.getName());
        switch (conversation.lastMessage.body.type) {
            case MessageType.APPLIED:
                holder.lastMessageLabel.setText(conversation.lastMessage.body.text);
                break;
            case MessageType.CHECKIN:
                holder.lastMessageLabel.setText(R.string.checked_in);
                break;
            case MessageType.HIRED:
                holder.lastMessageLabel.setText(R.string.congratulations_hired);
                break;
            case MessageType.JOBEND:
                holder.lastMessageLabel.setText(conversation.lastMessage.body.text);
                break;
            case MessageType.LOCATION:
                holder.lastMessageLabel.setText(R.string.congratulations_hired);
        }

        long lastMessageUpdate = conversation.lastMessage.timestamp;

        boolean isToday = DateUtils.isToday(lastMessageUpdate);

        SimpleDateFormat simpleDateFormat;
        if (isToday) {
            simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        } else {
            simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        }
        holder.timeLastMessage.setText(simpleDateFormat.format(Utils.fromTimeStamp(lastMessageUpdate).getTime()));

        if (conversation.unreadNotificationEmployee > 0) {
            holder.notificationLayout.setVisibility(View.VISIBLE);
            if (conversation.unreadNotificationEmployee > 9) {
                holder.notificationCounterLabel.setText("9+");
            } else {
                holder.notificationCounterLabel.setText(conversation.unreadNotificationEmployee + "");
            }

        } else {
            holder.notificationLayout.setVisibility(View.GONE);
        }

        TextDrawable placeholderEmployer = Utils.getPlaceholderForProfile(conversation.employerName, holder.itemView.getContext());
        String publicIdProfileImage = conversation.employerPublicIdProfileImage;
        if (!TextUtils.isEmpty(publicIdProfileImage)) {
            Transformation transformation = new Transformation();
            transformation
                    .width(holder.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size))
                    .height(holder.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size));
            String url = MediaManager.get().url().publicId(publicIdProfileImage)
                    .transformation(transformation).format("jpg").type("upload").generate();

            Picasso.get().load(url)
                    .transform(new RoundedTransformation(holder.itemView.getResources().getDimensionPixelSize(R.dimen.avatar_employee_size), 0))
                    .placeholder(placeholderEmployer)
                    .into(holder.employerAvatar);
        } else {
            holder.employerAvatar.setImageDrawable(placeholderEmployer);
        }
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    public void addConversations(List<Conversation> conversations) {
        this.conversations.addAll(conversations);
        notifyDataSetChanged();
    }

    public List<Conversation> getConversations() {
        return this.conversations;
    }

    public void clearConversations() {
        this.conversations.clear();
        notifyDataSetChanged();
    }

    public void addPublicIdProfileImageToConversations(HashMap<String, String> publicIdProfileImagesOfEmployer) {
        for (Conversation conversation : this.conversations) {
            conversation.employerPublicIdProfileImage = publicIdProfileImagesOfEmployer.get(conversation.employerId);
        }
        notifyDataSetChanged();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView roleLabel;
        public TextView lastMessageLabel;
        public ImageView employerAvatar;
        public TextView timeLastMessage;
        public ViewGroup notificationLayout;
        public TextView notificationCounterLabel;

        public ConversationViewHolder(View v) {
            super(v);
            roleLabel = v.findViewById(R.id.job_name);
            lastMessageLabel = v.findViewById(R.id.last_message);
            employerAvatar = v.findViewById(R.id.image_profile_employer);
            timeLastMessage = v.findViewById(R.id.last_message_time);
            notificationLayout = v.findViewById(R.id.notification_layout);
            notificationCounterLabel = v.findViewById(R.id.unreaded_messages);
        }
    }

    public void setItemClickListener(ConversationItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ConversationItemClickListener {
        void onItemClick(Conversation conversation, int position);
    }

}
