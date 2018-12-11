/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

public class BotService {

    private final DiscordApi api;

    private final UserNicknameChangeListener userNicknameChangeListener;
    private final QualifyingManagementExecutor qualifyingManagementExecutor;
    private final InfoExecutor infoExecutor;

    private CommandHandler handler;

    public BotService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            QualifyingManagementExecutor qualifyingManagementExecutor,
            InfoExecutor infoExecutor) {
        this.api = api;
        this.userNicknameChangeListener = userNicknameChangeListener;
        this.qualifyingManagementExecutor = qualifyingManagementExecutor;
        this.infoExecutor = infoExecutor;
    }

    public Boolean startBot() {

        api.updateActivity(ActivityType.PLAYING, "!help to learn more!");

        handler = new JavacordHandler(api);
        handler.registerCommand(infoExecutor);
        handler.registerCommand(qualifyingManagementExecutor);
        handler.registerCommand(new HelpExecutor(handler));

        api.addUserChangeNicknameListener(userNicknameChangeListener);


        return true;
    }
}
