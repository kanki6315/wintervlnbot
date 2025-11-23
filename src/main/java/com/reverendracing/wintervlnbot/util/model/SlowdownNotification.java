package com.reverendracing.wintervlnbot.util.model;

public class SlowdownNotification {

    private boolean isPractice;
    private String carNumber;
    private String teamName;
    private boolean earnedSlowdown;
    private int slowdownCount;

    public SlowdownNotification() {
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

    public int getSlowdownCount() {
        return slowdownCount;
    }

    public void setSlowdownCount(int slowdownCount) {
        this.slowdownCount = slowdownCount;
    }
}
