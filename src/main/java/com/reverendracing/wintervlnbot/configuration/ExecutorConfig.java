/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import com.reverendracing.wintervlnbot.data.*;
import com.reverendracing.wintervlnbot.service.executors.*;
import org.javacord.api.DiscordApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;

@Configuration
public class ExecutorConfig {

    @Value("${indyqualifying.queue_request_channel}")
    private String indyQualifyingRequestChannel;

    @Value("${indyqualifying.queue_post_channel}")
    private String indyQualifyingPostChannel;

    @Value("${discord.qualifying.message_channel}")
    private String qualifyingChannelMessageName;

    @Value("${discord.qualifying.api_endpoint}")
    private String qualifyingRestEndpoint;

    @Value("${discord.username_listener.role}")
    private String protectedRole;

    @Value("${discord.roles.manager_role_id}")
    private String managerRoleId;

    @Value("${discord.roles.crew_role_id}")
    private String crewRoleId;

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

    @Value("${discord.server_id}")
    private String serverId;

    @Autowired
    EntryRepository entryRepository;
    @Autowired
    DriverRepository driverRepository;
    @Autowired
    ClassRepository classRepository;
    @Autowired
    EntryCrewRepository entryCrewRepository;
    @Autowired
    QueueRequestRepository queueRequestRepository;

    @Bean
    public IndyQExecutor indyQExecutor() {
        return new IndyQExecutor(
                queueRequestRepository,
                driverRepository,
                indyQualifyingPostChannel,
                indyQualifyingRequestChannel,
                adminChannelName);
    }

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
}
