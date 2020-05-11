package com.openforce.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageBody implements Parcelable {

    public String text;
    public String type;
    public Long amount;
    public String jobRole;
    public String amountFormatted;
    public String jobRoleId;
    public JobLocation location;

    public MessageBody() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeString(this.type);
        dest.writeValue(this.amount);
        dest.writeString(this.jobRole);
        dest.writeString(this.amountFormatted);
        dest.writeString(this.jobRoleId);
        dest.writeParcelable(this.location, flags);
    }

    protected MessageBody(Parcel in) {
        this.text = in.readString();
        this.type = in.readString();
        this.amount = (Long) in.readValue(Long.class.getClassLoader());
        this.jobRole = in.readString();
        this.amountFormatted = in.readString();
        this.jobRoleId = in.readString();
        this.location = in.readParcelable(JobLocation.class.getClassLoader());
    }

    public static final Creator<MessageBody> CREATOR = new Creator<MessageBody>() {
        @Override
        public MessageBody createFromParcel(Parcel source) {
            return new MessageBody(source);
        }

        @Override
        public MessageBody[] newArray(int size) {
            return new MessageBody[size];
        }
    };
}
