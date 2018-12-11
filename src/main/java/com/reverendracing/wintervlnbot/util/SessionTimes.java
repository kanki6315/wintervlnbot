/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class SessionTimes {

    String sessionStartTime;
    String qualifyingStartTime;
    String formationLapTime;
    String raceStartTime;

    public SessionTimes() {
        this.sessionStartTime = "10:00";
        this.qualifyingStartTime = "12:30";
        this.formationLapTime = "13:45";
        this.raceStartTime = "14:00";
    }

    public void convertWithZoneId(ZoneId zoneId) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDateTime sessionLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(sessionStartTime, formatter));
        ZonedDateTime sessionZDT = sessionLDT
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId);
        sessionStartTime = formatter.format(sessionZDT);

        LocalDateTime qualifyingLDT = LocalDateTime.of(LocalDate.now(), LocalTime.parse(qualifyingStartTime, formatter));
        ZonedDateTime qualifyingZDT = qualifyingLDT
                .atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(zoneId);
        qualifyingStartTime = formatter.format(qualifyingZDT);

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

    public String getQualifyingStartTime() {
        return qualifyingStartTime;
    }

    public String getFormationLapTime() {
        return formationLapTime;
    }

    public String getRaceStartTime() {
        return raceStartTime;
    }
}
