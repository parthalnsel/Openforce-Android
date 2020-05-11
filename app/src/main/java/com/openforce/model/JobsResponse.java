package com.openforce.model;

import java.util.List;

public class JobsResponse {

    private List<Job> jobs;

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return "JobsResponse{" +
                "jobs=" + jobs +
                '}';
    }
}
