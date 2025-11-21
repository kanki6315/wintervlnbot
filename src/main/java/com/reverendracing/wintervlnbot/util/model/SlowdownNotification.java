package com.reverendracing.wintervlnbot.util.model;

public class SlowdownNotification {

    private boolean isPractice;
    private String carNumber;
    private String teamName;
    private boolean earnedSlowdown;

    public SlowdownNotification() {
    }

    public SlowdownNotification(boolean isPractice, String carNumber, String teamName, boolean earnedSlowdown) {
        this.isPractice = isPractice;
        this.carNumber = carNumber;
        this.teamName = teamName;
        this.earnedSlowdown = earnedSlowdown;
    }

    public boolean isPractice() {
        return isPractice;
    }

    public void setPractice(boolean practice) {
        isPractice = practice;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public boolean isEarnedSlowdown() {
        return earnedSlowdown;
    }

    public void setEarnedSlowdown(boolean earnedSlowdown) {
        this.earnedSlowdown = earnedSlowdown;
    }
}
