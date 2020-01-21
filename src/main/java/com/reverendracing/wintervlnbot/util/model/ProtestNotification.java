/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class ProtestNotification {

    private String protestingCarNumber;
    private String offendingCarNumber;
    private int lap;
    private String sector;
    private String timeStamp;
    private String reason;
    private String description;

    public ProtestNotification() {
    }

    public String getProtestingCarNumber() {
        return protestingCarNumber;
    }

    public void setProtestingCarNumber(String protestingCarNumber) {
        this.protestingCarNumber = protestingCarNumber;
    }

    public String getOffendingCarNumber() {
        return offendingCarNumber;
    }

    public void setOffendingCarNumber(String offendingCarNumber) {
        this.offendingCarNumber = offendingCarNumber;
    }

    public int getLap() {
        return lap;
    }

    public void setLap(int lap) {
        this.lap = lap;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}