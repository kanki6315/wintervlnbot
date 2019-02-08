/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class DecisionNotification {

    public String decision;
    public String reason;
    public int otherCarNumber;
    public int penalizedCarNumber;
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

    public int getOtherCarNumber() {
        return otherCarNumber;
    }

    public void setOtherCarNumber(int otherCarNumber) {
        this.otherCarNumber = otherCarNumber;
    }

    public int getPenalizedCarNumber() {
        return penalizedCarNumber;
    }

    public void setPenalizedCarNumber(int penalizedCarNumber) {
        this.penalizedCarNumber = penalizedCarNumber;
    }

    public String getPenalty() {
        return penalty;
    }

    public void setPenalty(String penalty) {
        this.penalty = penalty;
    }
}