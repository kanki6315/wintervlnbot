/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entry")
public class Entry {

    @Id
    private String id;

    private String carClass;

    private String carNumber;

    private String teamName;

    private String teamId;
    private String teamManagerName;

    private String carName;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Driver> drivers;

    public Entry() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getCarClass() {
        return carClass;
    }

    public void setCarClass(final String carClass) {
        this.carClass = carClass;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(final String carNumber) {
        this.carNumber = carNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(final String teamName) {
        this.teamName = teamName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(final String teamId) {
        this.teamId = teamId;
    }

    public String getTeamManagerName() {
        return teamManagerName;
    }

    public void setTeamManagerName(final String teamManagerName) {
        this.teamManagerName = teamManagerName;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(final String carName) {
        this.carName = carName;
    }

    public List<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(final List<Driver> drivers) {
        this.drivers = drivers;
    }

    public void addDriver(final Driver driver) {
        if (this.drivers == null) {
            this.drivers = new ArrayList<>();
        }
        this.drivers.add(driver);
    }
}
