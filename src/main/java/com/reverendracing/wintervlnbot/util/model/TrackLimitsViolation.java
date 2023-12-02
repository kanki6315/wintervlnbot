package com.reverendracing.wintervlnbot.util.model;

import java.util.HashSet;

public class TrackLimitsViolation {

    private String carNumber;
    private HashSet<Long> lapNumbers;

    public TrackLimitsViolation() {
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public HashSet<Long> getLapNumbers() {
        return lapNumbers;
    }

    public void setLapNumbers(HashSet<Long> lapNumbers) {
        this.lapNumbers = lapNumbers;
    }
}
