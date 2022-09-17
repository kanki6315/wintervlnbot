/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import com.reverendracing.wintervlnbot.service.executors.*;
import com.reverendracing.wintervlnbot.v2.commands.RefreshCommand;
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
    private final IndyQExecutor indyQExecutor;
    private final RefreshCommand refreshCommand;

    private CommandHandler handler;

    public BotService(
            DiscordApi api,
            UserNicknameChangeListener userNicknameChangeListener,
            RaceControlExecutor raceControlExecutor,
            InfoExecutor infoExecutor,
            QueryExecutor queryExecutor,
            IndyQExecutor indyQExecutor,
            RefreshCommand refreshCommand) {

        this.api = api;
        this.userNicknameChangeListener = userNicknameChangeListener;
        this.raceControlExecutor = raceControlExecutor;
        this.infoExecutor = infoExecutor;
        this.queryExecutor = queryExecutor;
        this.indyQExecutor = indyQExecutor;
        this.refreshCommand = refreshCommand;
    }

    public Boolean startBot() {

        api.updateActivity(ActivityType.PLAYING, "!help to learn more!");

        handler = new JavacordHandler(api);
        handler.registerCommand(infoExecutor);
        handler.registerCommand(raceControlExecutor);
        handler.registerCommand(queryExecutor);
        handler.registerCommand(indyQExecutor);
        me.s3ns3iw00.jcommands.CommandHandler.setApi(api);
        me.s3ns3iw00.jcommands.CommandHandler.registerCommand(refreshCommand.generateRefreshCommand());

        api.addReconnectListener(event -> event.getApi().updateActivity(ActivityType.PLAYING, "!help to learn more!"));

        //api.addUserChangeNicknameListener(userNicknameChangeListener);

        return true;
    }
}
