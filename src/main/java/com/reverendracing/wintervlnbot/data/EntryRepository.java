/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, String> {

    List<Entry> findByCarNumberLikeOrTeamNameContainsIgnoreCase(final String carNumber, final String teamName);

    default List<Entry> searchEntry(final String q) {
        return findByCarNumberLikeOrTeamNameContainsIgnoreCase(q, q);
    }
}
