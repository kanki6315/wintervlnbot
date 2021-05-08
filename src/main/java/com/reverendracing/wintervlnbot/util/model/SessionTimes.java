/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.util.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SessionTimes {

    private String sessionStartTime;
    private String driversBriefingTime;
    private String qualifyingEndTime;
    private String raceSessionStartTime;
    private String formationLapTime;
    private String raceStartTime;

    public SessionTimes() {
        this.sessionStartTime = "15:30";
        this.driversBriefingTime = "16:30";
        this.qualifyingEndTime = "17:30";
        this.raceSessionStartTime = "17:45";
        this.formationLapTime = "17:55";
        this.raceStartTime = "18:00";
    }

    public void convertWithZoneId(ZoneId zoneId) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDateTime sessionLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(sessionStartTime, formatter));
        ZonedDateTime sessionZDT = sessionLDT
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId);
        sessionStartTime = formatter.format(sessionZDT);

        LocalDateTime driversBriefingLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(driversBriefingTime, formatter));
        ZonedDateTime driversBriefingZDT = driversBriefingLDT
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(zoneId);
        driversBriefingTime = formatter.format(driversBriefingZDT);

        LocalDateTime qualifyingLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(qualifyingEndTime, formatter));
        ZonedDateTime qualifyingZDT = qualifyingLDT
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId);
        qualifyingEndTime = formatter.format(qualifyingZDT);

        LocalDateTime raceSessionLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(raceSessionStartTime, formatter));
        ZonedDateTime raceSessionZDT = raceSessionLDT
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(zoneId);
        raceSessionStartTime = formatter.format(sessionZDT);

        LocalDateTime formationLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(formationLapTime, formatter));
        ZonedDateTime formationZDT = formationLDT
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId);
        formationLapTime = formatter.format(formationZDT);

        LocalDateTime raceLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(raceStartTime, formatter));
        ZonedDateTime raceZDT = raceLDT
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId);
        raceStartTime = formatter.format(raceZDT);
    }

    public String getSessionStartTime() {
        return sessionStartTime;
    }

    public String getQualifyingEndTime() {
        return qualifyingEndTime;
    }

    public String getFormationLapTime() {
        return formationLapTime;
    }

    public String getRaceStartTime() {
        return raceStartTime;
    }

    public String getDriversBriefingTime() {
        return driversBriefingTime;
    }

    public String getRaceSessionStartTime() {
        return raceSessionStartTime;
    }
}
