/**
 * Copyright (C) 2021 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "entry_crew")
public class EntryCrew {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    public String discordUserId;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    private Entry entry;
    @Column(name = "entry_id", updatable = false, insertable = false)
    private String entryId;

    public EntryCrew() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(final String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(final Entry entry) {
        this.entry = entry;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(final String entryId) {
        this.entryId = entryId;
    }
}