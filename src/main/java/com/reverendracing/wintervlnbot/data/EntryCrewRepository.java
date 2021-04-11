/**
 * Copyright (C) 2021 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryCrewRepository extends JpaRepository<EntryCrew, String> {

    List<EntryCrew> findByEntryId(final String entryId);
}
