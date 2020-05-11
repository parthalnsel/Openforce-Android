package com.openforce.model;

public class StripeInfo {
    public String name;
    public String user_id;
    public String token;

    public StripeInfo(String id, String user_id, String access_token) {
        this.name = id;
        this.user_id = user_id;
        this.token = access_token;
    }

    public String getId() {
        return name;
    }

    public void setId(String id) {
        this.name = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAccess_token() {
        return token;
    }

    public void setAccess_token(String access_token) {
        this.token = access_token;
    }
}
