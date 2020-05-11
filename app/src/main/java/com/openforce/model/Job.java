
package com.openforce.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Job implements Parcelable {

    private String description;
    private Long endDate;
    private String id;
    private Role jobRole;
    private Double latitude;
    private Double longitude;
    private Long requiredEmployees;
    private Long startDate;
    private Long postedDate;
    private String status;
    private String employerHasReviewed;
    private EmployerReviewData employerReview;
    private String address;
    private String creationDate;
    private String employerName;
    private String employerID;
    private String payRate;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getJobRole() {
        return jobRole;
    }

    public void setJobRole(Role jobRole) {
        this.jobRole = jobRole;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Long getRequiredEmployees() {
        return requiredEmployees;
    }

    public void setRequiredEmployees(Long requiredEmployees) {
        this.requiredEmployees = requiredEmployees;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public EmployerReviewData getEmployerReview() {
        return employerReview;
    }

    public String getEmployerHasReviewed() {
        return employerHasReviewed;
    }

    public void setEmployerHasReviewed(String employerHasReviewed) {
        this.employerHasReviewed = employerHasReviewed;
    }

    public void setEmployerReview(EmployerReviewData employerReview) {
        this.employerReview = employerReview;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public Long getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Long postedDate) {
        this.postedDate = postedDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getPayRate() {
        return payRate;
    }

    public void setPayRate(String payRate) {
        this.payRate = payRate;
    }


    @Override
    public String toString() {
        return "Job{" +
                "description='" + description + '\'' +
                ", endDate=" + endDate +
                ", id='" + id + '\'' +
                ", jobRole=" + jobRole +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", requiredEmployees=" + requiredEmployees +
                ", startDate=" + startDate +
                ", postedDate=" + postedDate +
                ", status='" + status + '\'' +
                ", employerHasReviewed='" + employerHasReviewed + '\'' +
//                ", employerReview1='" + employerReview.getReview1() + '\'' +
//                ", employerReview2='" + employerReview.getReview2() + '\'' +
//                ", employerReview3='" + employerReview.getReview3() + '\'' +
//                ", employerReview4='" + employerReview.getReview4() + '\'' +
//                ", employerReview5='" + employerReview.getReview5() + '\'' +
                ", address='" + address + '\'' +
                ", employerName='" + employerName + '\'' +
                ", employerRate='" + payRate + '\'' +
                '}';
    }

    public Job() {
    }


    public String getEmployerID() {
        return employerID;
    }

    public void setEmployerID(String employerID) {
        this.employerID = employerID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeValue(this.endDate);
        dest.writeString(this.id);
        dest.writeParcelable(this.jobRole, flags);
        dest.writeValue(this.latitude);
        dest.writeValue(this.longitude);
        dest.writeValue(this.requiredEmployees);
        dest.writeValue(this.startDate);
        dest.writeValue(this.postedDate);
        dest.writeString(this.status);
        dest.writeString(this.employerHasReviewed);
        dest.writeParcelable(this.employerReview , flags);
        dest.writeString(this.address);
        dest.writeString(this.employerName);
        dest.writeString(this.employerID);
    }

    protected Job(Parcel in) {
        this.description = in.readString();
        this.endDate = (Long) in.readValue(Long.class.getClassLoader());
        this.id = in.readString();
        this.jobRole = in.readParcelable(Role.class.getClassLoader());
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
        this.requiredEmployees = (Long) in.readValue(Long.class.getClassLoader());
        this.startDate = (Long) in.readValue(Long.class.getClassLoader());
        this.postedDate = (Long) in.readValue(Long.class.getClassLoader());
        this.status = in.readString();
        this.employerHasReviewed = in.readString();
        this.employerReview = in.readParcelable(EmployerReviewData.class.getClassLoader());
        this.address = in.readString();
        this.employerName = in.readString();
        this.employerID = in.readString();
    }

    public static final Creator<Job> CREATOR = new Creator<Job>() {
        @Override
        public Job createFromParcel(Parcel source) {
            return new Job(source);
        }

        @Override
        public Job[] newArray(int size) {
            return new Job[size];
        }
    };
}
