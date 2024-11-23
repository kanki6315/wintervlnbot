package com.reverendracing.wintervlnbot.util.model;

import java.util.HashSet;

public class TrackLimitsUpdate {

    private String teamId;
    private String teamName;
    private String carNumber;
    private boolean isPractice;
    private int numIncidents;

    public TrackLimitsUpdate() {
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public boolean isPractice() {
        return isPractice;
    }

    public void setPractice(boolean practice) {
        isPractice = practice;
    }

    public int getNumIncidents() {
        return numIncidents;
    }

    public void setNumIncidents(int numIncidents) {
        this.numIncidents = numIncidents;
    }
}
