package com.openforce.model;

public class Reference {

    public String name;
    public String email;

    public Reference(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Reference() {
    }

    @Override
    public String toString() {
        return "Reference{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
