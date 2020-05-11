package com.openforce.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EmployerReviewData implements Parcelable {

    private String review1;
    private String review2;
    private String review3;
    private String review4;
    private String review5;

    public EmployerReviewData(String review1, String review2, String review3, String review4, String review5) {
        this.review1 = review1;
        this.review2 = review2;
        this.review3 = review3;
        this.review4 = review4;
        this.review5 = review5;
    }

    public EmployerReviewData() {
    }

    public String getReview1() {
        return review1;
    }

    public void setReview1(String review1) {
        this.review1 = review1;
    }

    public String getReview2() {
        return review2;
    }

    public void setReview2(String review2) {
        this.review2 = review2;
    }

    public String getReview3() {
        return review3;
    }

    public void setReview3(String review3) {
        this.review3 = review3;
    }

    public String getReview4() {
        return review4;
    }

    public void setReview4(String review4) {
        this.review4 = review4;
    }

    public String getReview5() {
        return review5;
    }

    public void setReview5(String review5) {
        this.review5 = review5;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.review1);
        dest.writeString(this.review2);
        dest.writeString(this.review3);
        dest.writeString(this.review4);
        dest.writeString(this.review5);
    }

    protected EmployerReviewData(Parcel in){
        this.review1 = in.readString();
        this.review2 = in.readString();
        this.review3 = in.readString();
        this.review4 = in.readString();
        this.review5 = in.readString();
    }

    public static final Creator<EmployerReviewData> CREATOR = new Creator<EmployerReviewData>() {
        @Override
        public EmployerReviewData createFromParcel(Parcel source) {
            return new EmployerReviewData(source);
        }

        @Override
        public EmployerReviewData[] newArray(int size) {
            return new EmployerReviewData[size];
        }
    };
}
