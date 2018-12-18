/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.util.model;

public class BlackFlagMessage {

    private int DriverNumber;
    private boolean IsSolo;

    public BlackFlagMessage() {
    }

    public BlackFlagMessage(int driverNumber, boolean isSolo) {
        DriverNumber = driverNumber;
        IsSolo = isSolo;
    }

    public int getDriverNumber() {
        return DriverNumber;
    }

    public void setDriverNumber(int driverNumber) {
        DriverNumber = driverNumber;
    }

    public boolean isSolo() {
        return IsSolo;
    }

    public void setSolo(boolean solo) {
        IsSolo = solo;
    }
}
