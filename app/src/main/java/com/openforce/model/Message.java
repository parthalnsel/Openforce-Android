package com.openforce.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {

    public MessageBody body;
    public String receiverId;
    public String senderId;
    public long timestamp;
    public String id;

    public Message() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.body, flags);
        dest.writeString(this.receiverId);
        dest.writeString(this.senderId);
        dest.writeLong(this.timestamp);
        dest.writeString(this.id);
    }

    protected Message(Parcel in) {
        this.body = in.readParcelable(MessageBody.class.getClassLoader());
        this.receiverId = in.readString();
        this.senderId = in.readString();
        this.timestamp = in.readLong();
        this.id = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
