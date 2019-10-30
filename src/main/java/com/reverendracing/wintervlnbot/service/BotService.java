/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

import com.reverendracing.wintervlnbot.service.executors.AdminExecutor;
import com.reverendracing.wintervlnbot.service.executors.HelpExecutor;
import com.reverendracing.wintervlnbot.service.executors.InfoExecutor;
import com.reverendracing.wintervlnbot.service.executors.RaceControlExecutor;
import com.reverendracing.wintervlnbot.service.executors.QueryExecutor;

public class BotService {

    private final DiscordApi api;

    private final UserNicknameChangeListener userNicknameChangeListener;
    private final RaceControlExecutor raceControlExecutor;
    private final InfoExecutor infoExecutor;
    private final QueryExecutor queryExecutor;
    private final AdminExecutor adminExecutor;

    private CommandHandler handler;

    public BotService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            RaceControlExecutor raceControlExecutor,
            InfoExecutor infoExecutor,
            QueryExecutor queryExecutor,
            AdminExecutor adminExecutor) {

        this.api = api;
        this.userNicknameChangeListener = userNicknameChangeListener;
        this.raceControlExecutor = raceControlExecutor;
        this.infoExecutor = infoExecutor;
        this.queryExecutor = queryExecutor;
        this.adminExecutor = adminExecutor;
    }

    public Boolean startBot() {

        api.updateActivity(ActivityType.PLAYING, "!help to learn more!");

        handler = new JavacordHandler(api);
        handler.registerCommand(infoExecutor);
        handler.registerCommand(raceControlExecutor);
        handler.registerCommand(queryExecutor);
        handler.registerCommand(adminExecutor);
        handler.registerCommand(new HelpExecutor(handler));

        api.addReconnectListener(event -> event.getApi().updateActivity(ActivityType.PLAYING, "!help to learn more!"));

        api.addUserChangeNicknameListener(userNicknameChangeListener);


        return true;
    }
}
