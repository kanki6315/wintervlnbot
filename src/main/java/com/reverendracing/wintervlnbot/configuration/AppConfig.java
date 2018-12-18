/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import java.sql.SQLException;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.h2.tools.Server;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.BotService;
import com.reverendracing.wintervlnbot.service.UserNicknameChangeListener;
import com.reverendracing.wintervlnbot.service.executors.InfoExecutor;
import com.reverendracing.wintervlnbot.service.executors.QualifyingManagementExecutor;
import com.reverendracing.wintervlnbot.service.executors.QueryExecutor;
import com.reverendracing.wintervlnbot.service.rest.SheetsManager;

@Configuration
@Import({ExecutorConfig.class, ListenerConfig.class})
public class AppConfig {

    @Value("${discord.api.token}")
    private String apiToken;

    @Value("${discord.sheets.id}")
    private String sheetId;

    @Value("${discord.sheets.range}")
    private String sheetRange;

    @Bean
    public DiscordApi api(){

        return new DiscordApiBuilder().setToken(apiToken).login().join();
    }

    @Bean
    public BotService botService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            QualifyingManagementExecutor qualifyingManagementExecutor,
            InfoExecutor infoExecutor,
            QueryExecutor queryExecutor) {

        return new BotService(
                api,
                userNicknameChangeListener,
                qualifyingManagementExecutor,
                infoExecutor,
                queryExecutor);
    }

    @Bean
    @Scope("singleton")
    public SheetsManager sheetsManager(
            EntryRepository entryRepository) {
        return new SheetsManager(
                entryRepository,
                sheetId,
                sheetRange);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server inMemoryH2DatabaseaServer() throws SQLException {
        return Server.createTcpServer(
                "-tcp", "-tcpAllowOthers", "-tcpPort", "9090");
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
