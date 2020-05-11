package com.openforce.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Conversation implements Parcelable {

    public String employeeId;
    public String employerId;
    public String jobId;
    public long lastUpdateTime;
    public Message lastMessage;
    public Role jobRole;
    public Long lastCheckin;
    public Boolean ended;
    public String employerName;
    public String id;
    public int unreadNotificationEmployee;
    public String employerPublicIdProfileImage;

    public Conversation() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.employeeId);
        dest.writeString(this.employerId);
        dest.writeString(this.jobId);
        dest.writeLong(this.lastUpdateTime);
        dest.writeParcelable(this.lastMessage, flags);
        dest.writeParcelable(this.jobRole, flags);
        dest.writeValue(this.lastCheckin);
        dest.writeValue(this.ended);
        dest.writeString(this.employerName);
        dest.writeString(this.id);
        dest.writeInt(this.unreadNotificationEmployee);
        dest.writeString(this.employerPublicIdProfileImage);
    }

    protected Conversation(Parcel in) {
        this.employeeId = in.readString();
        this.employerId = in.readString();
        this.jobId = in.readString();
        this.lastUpdateTime = in.readLong();
        this.lastMessage = in.readParcelable(Message.class.getClassLoader());
        this.jobRole = in.readParcelable(Role.class.getClassLoader());
        this.lastCheckin = (Long) in.readValue(Long.class.getClassLoader());
        this.ended = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.employerName = in.readString();
        this.id = in.readString();
        this.unreadNotificationEmployee = in.readInt();
        this.employerPublicIdProfileImage = in.readString();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel source) {
            return new Conversation(source);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
