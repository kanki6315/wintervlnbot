/**
 * Copyright (C) 2021 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntryCrewRepository extends JpaRepository<EntryCrew, String> {

    List<EntryCrew> findByEntryId(final String entryId);
}
