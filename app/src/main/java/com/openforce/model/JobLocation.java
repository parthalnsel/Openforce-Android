package com.openforce.model;

import android.os.Parcel;
import android.os.Parcelable;

public class JobLocation implements Parcelable {

    public String address;
    public Double longitude;
    public Double latitude;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeValue(this.longitude);
        dest.writeValue(this.latitude);
    }

    public JobLocation() {
    }

    protected JobLocation(Parcel in) {
        this.address = in.readString();
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Parcelable.Creator<JobLocation> CREATOR = new Parcelable.Creator<JobLocation>() {
        @Override
        public JobLocation createFromParcel(Parcel source) {
            return new JobLocation(source);
        }

        @Override
        public JobLocation[] newArray(int size) {
            return new JobLocation[size];
        }
    };
}
