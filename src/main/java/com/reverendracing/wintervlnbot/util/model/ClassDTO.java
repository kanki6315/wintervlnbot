/**
 * Copyright (C) 2020 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.util.model;

public class ClassDTO {
    private String name;
    private String id;
    private Long dRoleId;
    private Long dCategoryId;

    public ClassDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
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
}