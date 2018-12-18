/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reverendracing.wintervlnbot.service.UserNicknameChangeListener;

@Configuration
public class ListenerConfig {

    @Value("${discord.username_listener.message_channel}")
    private String usernameListenerChannelMessageName;

    @Value("${discord.username_listener.role}")
    private String usernameListenerRole;

    @Bean
    public UserNicknameChangeListener userNicknameChangeListener() {

        return new UserNicknameChangeListener(
                usernameListenerChannelMessageName,
                usernameListenerRole);
    }
}
