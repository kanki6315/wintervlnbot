/**
 * Copyright (C) 2021 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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