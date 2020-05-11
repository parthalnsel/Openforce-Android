package com.openforce.model;

public class CurrentJobResponse {

    public Job job;

    public CurrentJobResponse(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
