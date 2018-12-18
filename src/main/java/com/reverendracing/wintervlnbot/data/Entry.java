/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.data;

import static com.reverendracing.wintervlnbot.util.CsvInputs.BACKUP_CAR_NUMBER;
import static com.reverendracing.wintervlnbot.util.CsvInputs.CAR_NUMBER;
import static com.reverendracing.wintervlnbot.util.CsvInputs.CLASS;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIFTH_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FIRST_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.FOURTH_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.SECOND_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_CODEWORD;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_GT3_CAR_CHOICE;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_MANAGER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TEAM_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_COUNTRY;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_ID;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_IR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_NAME;
import static com.reverendracing.wintervlnbot.util.CsvInputs.THIRD_DRIVER_SR;
import static com.reverendracing.wintervlnbot.util.CsvInputs.TIMESTAMP;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "entry")
public class Entry {

    @JsonProperty(TIMESTAMP)
    private String registrationTime;

    @JsonProperty(CLASS)
    private String carClass;

    @JsonProperty(CAR_NUMBER)
    private String carNumber;
    @JsonProperty(BACKUP_CAR_NUMBER)
    private String backupCarNumber;

    @JsonProperty(TEAM_NAME)
    private String teamName;
    @Id
    @JsonProperty(TEAM_ID)
    private String teamId;
    @JsonProperty(TEAM_COUNTRY)
    private String teamCountry;
    @JsonProperty(TEAM_MANAGER_NAME)
    private String teamManagerName;
    @JsonProperty(TEAM_CODEWORD)
    private String teamCodeword;

    @JsonProperty(TEAM_GT3_CAR_CHOICE)
    private String gt3CarChoice;

    @JsonProperty(FIRST_DRIVER_NAME)
    private String firstDriverName;
    @JsonProperty(FIRST_DRIVER_ID)
    private String firstDriverId;
    @JsonProperty(FIRST_DRIVER_COUNTRY)
    private String firstDriverCountry;
    @JsonProperty(FIFTH_DRIVER_IR)
    private String firstDriverIR;
    @JsonProperty(FIRST_DRIVER_SR)
    private String firstDriverSR;

    @JsonProperty(SECOND_DRIVER_NAME)
    private String secondDriverName;
    @JsonProperty(SECOND_DRIVER_ID)
    private String secondDriverId;
    @JsonProperty(SECOND_DRIVER_COUNTRY)
    private String secondDriverCountry;
    @JsonProperty(SECOND_DRIVER_IR)
    private String secondDriverIR;
    @JsonProperty(SECOND_DRIVER_SR)
    private String secondDriverSR;

    @JsonProperty(THIRD_DRIVER_NAME)
    private String thirdDriverName;
    @JsonProperty(THIRD_DRIVER_ID)
    private String thirdDriverId;
    @JsonProperty(THIRD_DRIVER_COUNTRY)
    private String thirdDriverCountry;
    @JsonProperty(THIRD_DRIVER_IR)
    private String thirdDriverIR;
    @JsonProperty(THIRD_DRIVER_SR)
    private String thirdDriverSR;

    @JsonProperty(FOURTH_DRIVER_NAME)
    private String fourthDriverName;
    @JsonProperty(FOURTH_DRIVER_ID)
    private String fourthDriverId;
    @JsonProperty(FOURTH_DRIVER_COUNTRY)
    private String fourthDriverCountry;
    @JsonProperty(FOURTH_DRIVER_IR)
    private String fourthDriverIR;
    @JsonProperty(FOURTH_DRIVER_SR)
    private String fourthDriverSR;

    @JsonProperty(FIFTH_DRIVER_NAME)
    private String fifthDriverName;
    @JsonProperty(FIRST_DRIVER_ID)
    private String fifthDriverId;
    @JsonProperty(FIFTH_DRIVER_COUNTRY)
    private String fifthDriverCountry;
    @JsonProperty(FIFTH_DRIVER_IR)
    private String fifthDriverIR;
    @JsonProperty(FIFTH_DRIVER_SR)
    private String fifthDriverSR;

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getCarClass() {
        return carClass;
    }

    public void setCarClass(String carClass) {
        this.carClass = carClass;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getBackupCarNumber() {
        return backupCarNumber;
    }

    public void setBackupCarNumber(String backupCarNumber) {
        this.backupCarNumber = backupCarNumber;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamCountry() {
        return teamCountry;
    }

    public void setTeamCountry(String teamCountry) {
        this.teamCountry = teamCountry;
    }

    public String getTeamManagerName() {
        return teamManagerName;
    }

    public void setTeamManagerName(String teamManagerName) {
        this.teamManagerName = teamManagerName;
    }

    public String getTeamCodeword() {
        return teamCodeword;
    }

    public void setTeamCodeword(String teamCodeword) {
        this.teamCodeword = teamCodeword;
    }

    public String getGt3CarChoice() {
        return gt3CarChoice;
    }

    public void setGt3CarChoice(String gt3CarChoice) {
        this.gt3CarChoice = gt3CarChoice;
    }

    public String getFirstDriverName() {
        return firstDriverName;
    }

    public void setFirstDriverName(String firstDriverName) {
        this.firstDriverName = firstDriverName;
    }

    public String getFirstDriverId() {
        return firstDriverId;
    }

    public void setFirstDriverId(String firstDriverId) {
        this.firstDriverId = firstDriverId;
    }

    public String getFirstDriverCountry() {
        return firstDriverCountry;
    }

    public void setFirstDriverCountry(String firstDriverCountry) {
        this.firstDriverCountry = firstDriverCountry;
    }

    public String getFirstDriverIR() {
        return firstDriverIR;
    }

    public void setFirstDriverIR(String firstDriverIR) {
        this.firstDriverIR = firstDriverIR;
    }

    public String getFirstDriverSR() {
        return firstDriverSR;
    }

    public void setFirstDriverSR(String firstDriverSR) {
        this.firstDriverSR = firstDriverSR;
    }

    public String getSecondDriverName() {
        return secondDriverName;
    }

    public void setSecondDriverName(String secondDriverName) {
        this.secondDriverName = secondDriverName;
    }

    public String getSecondDriverId() {
        return secondDriverId;
    }

    public void setSecondDriverId(String secondDriverId) {
        this.secondDriverId = secondDriverId;
    }

    public String getSecondDriverCountry() {
        return secondDriverCountry;
    }

    public void setSecondDriverCountry(String secondDriverCountry) {
        this.secondDriverCountry = secondDriverCountry;
    }

    public String getSecondDriverIR() {
        return secondDriverIR;
    }

    public void setSecondDriverIR(String secondDriverIR) {
        this.secondDriverIR = secondDriverIR;
    }

    public String getSecondDriverSR() {
        return secondDriverSR;
    }

    public void setSecondDriverSR(String secondDriverSR) {
        this.secondDriverSR = secondDriverSR;
    }

    public String getThirdDriverName() {
        return thirdDriverName;
    }

    public void setThirdDriverName(String thirdDriverName) {
        this.thirdDriverName = thirdDriverName;
    }

    public String getThirdDriverId() {
        return thirdDriverId;
    }

    public void setThirdDriverId(String thirdDriverId) {
        this.thirdDriverId = thirdDriverId;
    }

    public String getThirdDriverCountry() {
        return thirdDriverCountry;
    }

    public void setThirdDriverCountry(String thirdDriverCountry) {
        this.thirdDriverCountry = thirdDriverCountry;
    }

    public String getThirdDriverIR() {
        return thirdDriverIR;
    }

    public void setThirdDriverIR(String thirdDriverIR) {
        this.thirdDriverIR = thirdDriverIR;
    }

    public String getThirdDriverSR() {
        return thirdDriverSR;
    }

    public void setThirdDriverSR(String thirdDriverSR) {
        this.thirdDriverSR = thirdDriverSR;
    }

    public String getFourthDriverName() {
        return fourthDriverName;
    }

    public void setFourthDriverName(String fourthDriverName) {
        this.fourthDriverName = fourthDriverName;
    }

    public String getFourthDriverId() {
        return fourthDriverId;
    }

    public void setFourthDriverId(String fourthDriverId) {
        this.fourthDriverId = fourthDriverId;
    }

    public String getFourthDriverCountry() {
        return fourthDriverCountry;
    }

    public void setFourthDriverCountry(String fourthDriverCountry) {
        this.fourthDriverCountry = fourthDriverCountry;
    }

    public String getFourthDriverIR() {
        return fourthDriverIR;
    }

    public void setFourthDriverIR(String fourthDriverIR) {
        this.fourthDriverIR = fourthDriverIR;
    }

    public String getFourthDriverSR() {
        return fourthDriverSR;
    }

    public void setFourthDriverSR(String fourthDriverSR) {
        this.fourthDriverSR = fourthDriverSR;
    }

    public String getFifthDriverName() {
        return fifthDriverName;
    }

    public void setFifthDriverName(String fifthDriverName) {
        this.fifthDriverName = fifthDriverName;
    }

    public String getFifthDriverId() {
        return fifthDriverId;
    }

    public void setFifthDriverId(String fifthDriverId) {
        this.fifthDriverId = fifthDriverId;
    }

    public String getFifthDriverCountry() {
        return fifthDriverCountry;
    }

    public void setFifthDriverCountry(String fifthDriverCountry) {
        this.fifthDriverCountry = fifthDriverCountry;
    }

    public String getFifthDriverIR() {
        return fifthDriverIR;
    }

    public void setFifthDriverIR(String fifthDriverIR) {
        this.fifthDriverIR = fifthDriverIR;
    }

    public String getFifthDriverSR() {
        return fifthDriverSR;
    }

    public void setFifthDriverSR(String fifthDriverSR) {
        this.fifthDriverSR = fifthDriverSR;
    }
}
