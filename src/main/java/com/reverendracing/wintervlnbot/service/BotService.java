/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

import com.reverendracing.wintervlnbot.service.executors.HelpExecutor;
import com.reverendracing.wintervlnbot.service.executors.InfoExecutor;
import com.reverendracing.wintervlnbot.service.executors.QualifyingManagementExecutor;
import com.reverendracing.wintervlnbot.service.executors.QueryExecutor;

public class BotService {

    private final DiscordApi api;

    private final UserNicknameChangeListener userNicknameChangeListener;
    private final QualifyingManagementExecutor qualifyingManagementExecutor;
    private final InfoExecutor infoExecutor;
    private final QueryExecutor queryExecutor;

    private CommandHandler handler;

    public BotService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            QualifyingManagementExecutor qualifyingManagementExecutor,
            InfoExecutor infoExecutor,
            QueryExecutor queryExecutor) {

        this.api = api;
        this.userNicknameChangeListener = userNicknameChangeListener;
        this.qualifyingManagementExecutor = qualifyingManagementExecutor;
        this.infoExecutor = infoExecutor;
        this.queryExecutor = queryExecutor;
    }

    public Boolean startBot() {

        api.updateActivity(ActivityType.PLAYING, "!help to learn more!");

        handler = new JavacordHandler(api);
        handler.registerCommand(infoExecutor);
        handler.registerCommand(qualifyingManagementExecutor);
        handler.registerCommand(queryExecutor);
        handler.registerCommand(new HelpExecutor(handler));

        api.addUserChangeNicknameListener(userNicknameChangeListener);


        return true;
    }
}
