/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class DecisionNotification {

    private String incidentDecision;
    private String incidentReason;
    private String incidentCarNumber;
    private String incidentPenalty;

    public DecisionNotification() {
    }

    public String getIncidentDecision() {
        return incidentDecision;
    }

    public void setIncidentDecision(String incidentDecision) {
        this.incidentDecision = incidentDecision;
    }

    public String getIncidentReason() {
        return incidentReason;
    }

    public void setIncidentReason(String incidentReason) {
        this.incidentReason = incidentReason;
    }

    public String getIncidentCarNumber() {
        return incidentCarNumber;
    }

    public void setIncidentCarNumber(String incidentCarNumber) {
        this.incidentCarNumber = incidentCarNumber;
    }

    public String getIncidentPenalty() {
        return incidentPenalty;
    }

    public void setIncidentPenalty(String incidentPenalty) {
        this.incidentPenalty = incidentPenalty;
    }
}