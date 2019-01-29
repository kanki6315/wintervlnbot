/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class ProtestNotification {

    private String incidentId;
    private String reportedCarNumber;
    private String investigatingCarNumber;
    private String reason;

    public ProtestNotification() {
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getReportedCarNumber() {
        return reportedCarNumber;
    }

    public void setReportedCarNumber(String reportedCarNumber) {
        this.reportedCarNumber = reportedCarNumber;
    }

    public String getInvestigatingCarNumber() {
        return investigatingCarNumber;
    }

    public void setInvestigatingCarNumber(String investigatingCarNumber) {
        this.investigatingCarNumber = investigatingCarNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}