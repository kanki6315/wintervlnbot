/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import com.reverendracing.wintervlnbot.service.BotService;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;
import com.reverendracing.wintervlnbot.v2.commands.RaceControlCommand;
import com.reverendracing.wintervlnbot.v2.commands.RefreshCommand;
import jakarta.annotation.PostConstruct;
import org.h2.tools.Server;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.SQLException;
import java.util.TimeZone;

@Configuration
@Import({CommandConfig.class})
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
            RefreshCommand refreshCommand,
            RaceControlCommand raceControlCommand) {

        return new BotService(
                api,
                refreshCommand,
                raceControlCommand);
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
