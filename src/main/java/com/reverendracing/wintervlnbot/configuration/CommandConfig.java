package com.reverendracing.wintervlnbot.configuration;

import com.reverendracing.wintervlnbot.data.*;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;
import com.reverendracing.wintervlnbot.v2.commands.RaceControlCommand;
import com.reverendracing.wintervlnbot.v2.commands.RefreshCommand;
import org.javacord.api.DiscordApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandConfig {

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
    public RefreshCommand refreshCommand(DiscordApi api, RequestBuilder requestBuilder) {
        return new RefreshCommand(
                api,
                requestBuilder,
                entryRepository,
                driverRepository,
                classRepository,
                entryCrewRepository);
    }

    @Bean
    public RaceControlCommand raceControlCommand(DiscordApi api) {
        return new RaceControlCommand(api);
    }
}
