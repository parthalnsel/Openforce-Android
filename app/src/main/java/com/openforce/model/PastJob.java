package com.openforce.model;

import android.os.Parcel;

public class PastJob extends Job {

    public long endTimeStamp;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.endTimeStamp);
    }

    public PastJob() {
    }

    protected PastJob(Parcel in) {
        super(in);
        this.endTimeStamp = in.readLong();
    }

    public static final Creator<PastJob> CREATOR = new Creator<PastJob>() {
        @Override
        public PastJob createFromParcel(Parcel source) {
            return new PastJob(source);
        }

        @Override
        public PastJob[] newArray(int size) {
            return new PastJob[size];
        }
    };
}
