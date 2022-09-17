/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import java.sql.SQLException;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import com.reverendracing.wintervlnbot.service.executors.*;
import com.reverendracing.wintervlnbot.v2.commands.RefreshCommand;
import org.h2.tools.Server;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.BotService;
import com.reverendracing.wintervlnbot.service.UserNicknameChangeListener;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;

@Configuration
@Import({ExecutorConfig.class, ListenerConfig.class})
@EnableScheduling
public class AppConfig {

    @Value("${discord.api.token}")
    private String apiToken;

    @Bean
    public DiscordApi api(){

        return new DiscordApiBuilder()
            .setToken(apiToken)
            .setAllIntents()
            .login().join();
    }

    @Bean
    public BotService botService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            RaceControlExecutor raceControlExecutor,
            InfoExecutor infoExecutor,
            QueryExecutor queryExecutor,
            IndyQExecutor indyQExecutor,
            RefreshCommand refreshCommand) {

        return new BotService(
                api,
                userNicknameChangeListener,
                raceControlExecutor,
                infoExecutor,
                queryExecutor,
                indyQExecutor,
                refreshCommand);
    }

    @Bean
    public RequestBuilder requestBuilder() {
        return new RequestBuilder();
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
