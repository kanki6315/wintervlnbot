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

    List<Entry> findByClassId(final String classId);

    List<Entry> findByTeamNameContainsIgnoreCase(final String teamName);

    List<Entry> findByCarNumberEquals(final String carNumber);


    default List<Entry> searchEntry(final String q) {
        try {
            int carNumber = Integer.parseInt(q);
            return findByCarNumberEquals(q);
        } catch (NumberFormatException ex) {
            return findByTeamNameContainsIgnoreCase(q);
        }
    }
}
