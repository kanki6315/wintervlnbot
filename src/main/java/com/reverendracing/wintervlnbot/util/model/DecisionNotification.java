/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class DecisionNotification {

    public int incidentNumber;
    public String decision;
    public String reason;
    public String otherCarNumber;
    public String otherCarName;
    public String penalizedCarNumber;
    public String penalizedCarName;
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

    public String getOtherCarNumber() {
        return otherCarNumber;
    }

    public void setOtherCarNumber(String otherCarNumber) {
        this.otherCarNumber = otherCarNumber;
    }

    public String getPenalizedCarNumber() {
        return penalizedCarNumber;
    }

    public void setPenalizedCarNumber(String penalizedCarNumber) {
        this.penalizedCarNumber = penalizedCarNumber;
    }

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }

    public int getIncidentNumber() {
        return incidentNumber;
    }

    public void setIncidentNumber(int incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public String getOtherCarName() {
        return otherCarName;
    }

    public void setOtherCarName(String otherCarName) {
        this.otherCarName = otherCarName;
    }

    public String getPenalizedCarName() {
        return penalizedCarName;
    }

    public void setPenalizedCarName(String penalizedCarName) {
        this.penalizedCarName = penalizedCarName;
    }
}