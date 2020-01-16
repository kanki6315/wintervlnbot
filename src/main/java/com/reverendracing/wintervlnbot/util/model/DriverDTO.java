/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class DriverDTO {
    public String driverId;
    public String driverName;
    public Integer irating;
    public String licenseLevel;
    public double safetyRating;

    public String EntryId;

    public String Id;

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(final String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(final String driverName) {
        this.driverName = driverName;
    }

    public Integer getIrating() {
        return irating;
    }

    public void setIrating(final Integer irating) {
        this.irating = irating;
    }

    public String getLicenseLevel() {
        return licenseLevel;
    }

    public void setLicenseLevel(final String licenseLevel) {
        this.licenseLevel = licenseLevel;
    }

    public double getSafetyRating() {
        return safetyRating;
    }

    public void setSafetyRating(final double safetyRating) {
        this.safetyRating = safetyRating;
    }

    public String getEntryId() {
        return EntryId;
    }

    public void setEntryId(final String entryId) {
        EntryId = entryId;
    }

    public String getId() {
        return Id;
    }

    public void setId(final String id) {
        Id = id;
    }
}