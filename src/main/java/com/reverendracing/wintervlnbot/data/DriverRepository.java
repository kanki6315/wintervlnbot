/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<Driver, String> {

    List<Driver> findByEntryId(final String entryId);
}