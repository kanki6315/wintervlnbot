/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.data;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "class")
public class Class {

    @Id
    private String id;

    private String name;

    private Long dRoleId;
    private Long dCategoryId;

    @OneToMany(fetch = FetchType.LAZY)
    private List<Entry> entries;

    public Class() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getdRoleId() {
        return dRoleId;
    }

    public void setdRoleId(final Long dRoleId) {
        this.dRoleId = dRoleId;
    }

    public Long getdCategoryId() {
        return dCategoryId;
    }

    public void setdCategoryId(final Long dCategoryId) {
        this.dCategoryId = dCategoryId;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(final List<Entry> entries) {
        this.entries = entries;
    }

    public void addEntry(final Entry entry) {
        if (this.entries == null) {
            this.entries = new ArrayList<>();
        }
        this.entries.add(entry);
    }
}