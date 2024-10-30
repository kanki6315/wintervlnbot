/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "driver")
public class Driver {

    @Id
    private String id;

    private String driverId;
    private String driverName;
    private int irating;
    private String licenseLevel;
    private double safetyRating;
    private String dUserId;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    private Entry entry;
    @Column(name = "entry_id", updatable = false, insertable = false)
    private String entryId;

    public Driver() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

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

    public int getIrating() {
        return irating;
    }

    public void setIrating(final int irating) {
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

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(final Entry entry) {
        this.entry = entry;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(final String entryId) {
        this.entryId = entryId;
    }

    public String getdUserId() {
        return dUserId;
    }

    public void setdUserId(String dUserId) {
        this.dUserId = dUserId;
    }
}