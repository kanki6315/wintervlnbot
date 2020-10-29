/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EntryDTO {

    public String id;
    public String teamId;
    public String teamName;
    public String carNumber;
    public String RegistrationStatus;
    public String creatingUserId;

    public String carName;
    public String teamManagerName;
    @JsonProperty("class")
    public ClassDTO classDto;
    public List<DriverDTO> drivers;

    public Long dRoleId;

    public EntryDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(final String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(final String teamName) {
        this.teamName = teamName;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(final String carNumber) {
        this.carNumber = carNumber;
    }

    public String getRegistrationStatus() {
        return RegistrationStatus;
    }

    public void setRegistrationStatus(final String registrationStatus) {
        RegistrationStatus = registrationStatus;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(final String carName) {
        this.carName = carName;
    }

    public String getTeamManagerName() {
        return teamManagerName;
    }

    public void setTeamManagerName(final String teamManagerName) {
        this.teamManagerName = teamManagerName;
    }

    public ClassDTO getClassDto() {
        return classDto;
    }

    public void setClassDto(final ClassDTO classDto) {
        this.classDto = classDto;
    }

    public List<DriverDTO> getDrivers() {
        return drivers;
    }

    public void setDrivers(final List<DriverDTO> drivers) {
        this.drivers = drivers;
    }

    public Long getdRoleId() {
        return dRoleId;
    }

    public void setdRoleId(final Long dRoleId) {
        this.dRoleId = dRoleId;
    }

    public String getCreatingUserId() {
        return creatingUserId;
    }

    public void setCreatingUserId(final String creatingUserId) {
        this.creatingUserId = creatingUserId;
    }
}