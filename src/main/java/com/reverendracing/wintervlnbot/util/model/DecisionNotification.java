/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class DecisionNotification {

    public String decision;
    public String reason;
    public int reportingCarNumber;
    public int investigatingCarNumber;
    public String penalty;

    public DecisionNotification() {
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getReportingCarNumber() {
        return reportingCarNumber;
    }

    public void setReportingCarNumber(int reportingCarNumber) {
        this.reportingCarNumber = reportingCarNumber;
    }

    public int getInvestigatingCarNumber() {
        return investigatingCarNumber;
    }

    public void setInvestigatingCarNumber(int investigatingCarNumber) {
        this.investigatingCarNumber = investigatingCarNumber;
    }

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }
}