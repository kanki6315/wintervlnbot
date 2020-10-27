/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import org.javacord.api.DiscordApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reverendracing.wintervlnbot.data.DriverRepository;
import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.executors.AdminExecutor;
import com.reverendracing.wintervlnbot.service.executors.InfoExecutor;
import com.reverendracing.wintervlnbot.service.executors.RaceControlExecutor;
import com.reverendracing.wintervlnbot.service.executors.QueryExecutor;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;

@Configuration
public class ExecutorConfig {

    @Value("${discord.qualifying.message_channel}")
    private String qualifyingChannelMessageName;

    @Value("${discord.qualifying.api_endpoint}")
    private String qualifyingRestEndpoint;

    @Value("${discord.username_listener.role}")
    private String protectedRole;

    @Value("${discord.username_listener.message_channel}")
    private String adminChannelName;

    @Value("${discord.admin.channel_id}")
    private String adminChannelId;

    @Value("${discord.info.invite_channel}")
    private String inviteChannelName;

    @Value("${discord.protests.message_channel}")
    private String protestChannelName;

    @Value("${discord.league_id}")
    private String leagueId;

    @Autowired
    EntryRepository entryRepository;
    @Autowired
    DriverRepository driverRepository;

    @Bean
    public RaceControlExecutor qualifyingManagementExecutor(
            DiscordApi api) {

        return new RaceControlExecutor(
                api,
                qualifyingChannelMessageName,
                protestChannelName,
            adminChannelName,
                qualifyingRestEndpoint);
    }

    @Bean
    public InfoExecutor infoExecutor() {

        return new InfoExecutor(
                protectedRole,
                adminChannelName,
                inviteChannelName);
    }

    @Bean
    public QueryExecutor queryExecutor() {

        return new QueryExecutor(
                entryRepository,
                driverRepository,
                protectedRole,
                adminChannelName);
    }

    @Bean
    public AdminExecutor adminExecutor(DiscordApi api, RequestBuilder requestBuilder) {
        return new AdminExecutor(
                requestBuilder,
                entryRepository,
                driverRepository,
                api,
                leagueId,
                protectedRole,
                adminChannelId);
    }
}
