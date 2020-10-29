/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
    private Long dUserId;

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

    public Long getdUserId() {
        return dUserId;
    }

    public void setdUserId(final Long dUserId) {
        this.dUserId = dUserId;
    }
}