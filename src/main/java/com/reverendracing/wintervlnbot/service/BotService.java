/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import com.reverendracing.wintervlnbot.v2.commands.RaceControlCommand;
import com.reverendracing.wintervlnbot.v2.commands.RefreshCommand;
import de.btobastian.sdcf4j.CommandHandler;
import de.btobastian.sdcf4j.handler.JavacordHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.interaction.SlashCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotService {

    private final static Logger logger = LoggerFactory.getLogger(BotService.class);

    private final DiscordApi api;

    private final RefreshCommand refreshCommand;
    private final RaceControlCommand raceControlCommand;

    private CommandHandler handler;

    public BotService(
            DiscordApi api,
            RefreshCommand refreshCommand,
            RaceControlCommand raceControlCommand) {

        this.api = api;
        this.refreshCommand = refreshCommand;
        this.raceControlCommand = raceControlCommand;
    }

    public Boolean startBot() {

        api.updateActivity(ActivityType.PLAYING, "!help to learn more!");

        handler = new JavacordHandler(api);
        me.s3ns3iw00.jcommands.CommandHandler.setApi(api);
        for (var server: api.getServers()) {
            /*server.getSlashCommands().thenAccept(slashCommands -> {
                for (SlashCommand slashCommand : slashCommands) {
                    logger.info("Deleting command");
                    slashCommand.delete();
                }
            });*/

            logger.info("Requesting commands");
            try {
                logger.info("Adding refresh command");
                me.s3ns3iw00.jcommands.CommandHandler.registerCommand(refreshCommand.generateRefreshCommand(), server);
                logger.info("Adding start socket command");
                me.s3ns3iw00.jcommands.CommandHandler.registerCommand(raceControlCommand.generateStartSocket(), server);
                logger.info("Adding restart socket command");
                me.s3ns3iw00.jcommands.CommandHandler.registerCommand(raceControlCommand.generateRestartSocket(), server);
                logger.info("Adding stop socket command");
                me.s3ns3iw00.jcommands.CommandHandler.registerCommand(raceControlCommand.generateStopSocket(), server);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

        api.addReconnectListener(event -> event.getApi().updateActivity(ActivityType.PLAYING, "!help to learn more!"));

        return true;
    }
}
