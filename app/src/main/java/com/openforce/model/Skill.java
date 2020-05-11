package com.openforce.model;

public class Skill {

    public String id;
    public String name;
    public String payRate;
    public int level;

    public Skill(String skillId, String name, String payRate, int skillLevel) {
        this.id = skillId;
        this.name = name;
        this.payRate = payRate;
        this.level = skillLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPayRate() {
        return payRate;
    }

    public void setPayRate(String payRate) {
        this.payRate = payRate;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
