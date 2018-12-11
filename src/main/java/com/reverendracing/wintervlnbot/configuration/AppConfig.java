/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reverendracing.wintervlnbot.service.BotService;
import com.reverendracing.wintervlnbot.service.InfoExecutor;
import com.reverendracing.wintervlnbot.service.QualifyingManagementExecutor;
import com.reverendracing.wintervlnbot.service.UserNicknameChangeListener;

@Configuration
public class AppConfig {

    @Value("${discord.api.token}")
    private String apiToken;

    @Value("${discord.username_listener.message_channel}")
    private String usernameListenerChannelMessageName;

    @Value("${discord.username_listener.role}")
    private String usernameListenerRole;

    @Value("${discord.qualifying.message_channel}")
    private String qualifyingChannelMessageName;

    @Value("${discord.qualifying.api_endpoint}")
    private String qualifyingRestEndpoint;

    @Bean
    public DiscordApi api(){

        DiscordApi api = new DiscordApiBuilder().setToken(apiToken).login().join();
        return api;
    }

    @Bean
    public UserNicknameChangeListener userNicknameChangeListener() {
        return new UserNicknameChangeListener(
                usernameListenerChannelMessageName,
                usernameListenerRole);
    }

    @Bean
    public QualifyingManagementExecutor qualifyingManagementExecutor() {
        return new QualifyingManagementExecutor(
                qualifyingChannelMessageName,
                qualifyingRestEndpoint);
    }

    @Bean
    public InfoExecutor infoExecutor() {
        return new InfoExecutor();
    }

    @Bean
    public BotService botService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            QualifyingManagementExecutor qualifyingManagementExecutor,
            InfoExecutor infoExecutor) {
        return new BotService(
                api,
                userNicknameChangeListener,
                qualifyingManagementExecutor,
                infoExecutor);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

}
