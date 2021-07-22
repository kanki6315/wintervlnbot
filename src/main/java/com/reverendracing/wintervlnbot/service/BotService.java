/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import com.reverendracing.wintervlnbot.service.executors.*;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;

public class BotService {

    private final DiscordApi api;

    private final UserNicknameChangeListener userNicknameChangeListener;
    private final RaceControlExecutor raceControlExecutor;
    private final InfoExecutor infoExecutor;
    private final QueryExecutor queryExecutor;
    private final AdminExecutor adminExecutor;
    private final IndyQExecutor indyQExecutor;

    private CommandHandler handler;

    public BotService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            RaceControlExecutor raceControlExecutor,
            InfoExecutor infoExecutor,
            QueryExecutor queryExecutor,
            AdminExecutor adminExecutor,
            IndyQExecutor indyQExecutor) {

        this.api = api;
        this.userNicknameChangeListener = userNicknameChangeListener;
        this.raceControlExecutor = raceControlExecutor;
        this.infoExecutor = infoExecutor;
        this.queryExecutor = queryExecutor;
        this.adminExecutor = adminExecutor;
        this.indyQExecutor = indyQExecutor;
    }

    public Boolean startBot() {

        api.updateActivity(ActivityType.PLAYING, "!help to learn more!");

        handler = new JavacordHandler(api);
        handler.registerCommand(infoExecutor);
        handler.registerCommand(raceControlExecutor);
        handler.registerCommand(queryExecutor);
        handler.registerCommand(adminExecutor);
        handler.registerCommand(indyQExecutor);
        handler.registerCommand(new HelpExecutor(handler));

        api.addReconnectListener(event -> event.getApi().updateActivity(ActivityType.PLAYING, "!help to learn more!"));

        //api.addUserChangeNicknameListener(userNicknameChangeListener);

        return true;
    }
}
