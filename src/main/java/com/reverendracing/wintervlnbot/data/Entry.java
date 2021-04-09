/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    private String dTeamManagerId;

    private Long dVoiceChannelId;
    private Long dRoleId;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Driver> drivers;

    @OneToMany(fetch = FetchType.LAZY)
    private List<EntryCrew> entryCrew;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private Class rClass;
    @Column(name = "class_id", updatable = false, insertable = false)
    private String classId;

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

    public List<EntryCrew> getEntryCrew() {
        return entryCrew;
    }

    public void setEntryCrew(final List<EntryCrew> entryCrew) {
        this.entryCrew = entryCrew;
    }

    public void addEntryCrew(final EntryCrew crewMember) {
        if (this.entryCrew == null) {
            this.entryCrew = new ArrayList<>();
        }
        this.entryCrew.add(crewMember);
    }

    public long getdVoiceChannelId() {
        return dVoiceChannelId;
    }

    public void setdVoiceChannelId(final long dVoiceChannelId) {
        this.dVoiceChannelId = dVoiceChannelId;
    }

    public Long getdRoleId() {
        return dRoleId;
    }

    public void setdRoleId(final Long dRoleId) {
        this.dRoleId = dRoleId;
    }

    public void setdVoiceChannelId(final Long dVoiceChannelId) {
        this.dVoiceChannelId = dVoiceChannelId;
    }

    public Class getrClass() {
        return rClass;
    }

    public void setrClass(final Class rClass) {
        this.rClass = rClass;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(final String classId) {
        this.classId = classId;
    }

    public String getdTeamManagerId() {
        return dTeamManagerId;
    }

    public void setdTeamManagerId(final String dTeamManagerId) {
        this.dTeamManagerId = dTeamManagerId;
    }
}
