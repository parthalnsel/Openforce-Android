package com.openforce.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Role implements Parcelable {

    private String name;
    private String id;
    private boolean featured;

    public Role(String name, String id, boolean featured) {
        this.name = name;
        this.id = id;
        this.featured = featured;
    }

    public Role() {
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.id);
        dest.writeByte(this.featured ? (byte) 1 : (byte) 0);
    }

    protected Role(Parcel in) {
        this.name = in.readString();
        this.id = in.readString();
        this.featured = in.readByte() != 0;
    }

    public static final Creator<Role> CREATOR = new Creator<Role>() {
        @Override
        public Role createFromParcel(Parcel source) {
            return new Role(source);
        }

        @Override
        public Role[] newArray(int size) {
            return new Role[size];
        }
    };
}