/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    List<Driver> findByEntryId(final String entryId);
    List<Driver> findByEntry_CarNumber(final String carNumber);
}