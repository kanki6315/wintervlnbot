/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reverendracing.wintervlnbot.service.executors.InfoExecutor;
import com.reverendracing.wintervlnbot.service.executors.QualifyingManagementExecutor;
import com.reverendracing.wintervlnbot.service.executors.QueryExecutor;
import com.reverendracing.wintervlnbot.service.rest.SheetsManager;

@Configuration
public class ExecutorConfig {

    @Value("${discord.qualifying.message_channel}")
    private String qualifyingChannelMessageName;

    @Value("${discord.qualifying.api_endpoint}")
    private String qualifyingRestEndpoint;

    @Value("${discord.info.entry_list}")
    private String entryListUrl;

    @Value("${discord.info.change_form}")
    private String changeFormUrl;

    @Value("${discord.username_listener.role}")
    private String protectedRole;

    @Value("${discord.username_listener.message_channel}")
    private String adminChannel;


    @Bean
    public QualifyingManagementExecutor qualifyingManagementExecutor() {

        return new QualifyingManagementExecutor(
                qualifyingChannelMessageName,
                adminChannel,
                qualifyingRestEndpoint);
    }

    @Bean
    public InfoExecutor infoExecutor() {

        return new InfoExecutor(
                entryListUrl,
                changeFormUrl,
                protectedRole,
                adminChannel);
    }

    @Bean
    public QueryExecutor queryExecutor(
            SheetsManager sheetsManager) {

        return new QueryExecutor(
                sheetsManager,
                protectedRole,
                adminChannel);
    }
}
