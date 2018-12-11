/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import de.btobastian.sdcf4j.CommandHandler;

import org.javacord.api.entity.channel.Channel;

public class HelpExecutor implements CommandExecutor {

    private final CommandHandler commandHandler;

    public HelpExecutor(final CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Command(aliases = {"!help", "!commands"}, description = "Show Available Commands")
    public String onHelpCommand(Channel channel) {

        StringBuilder builder = new StringBuilder();
        builder.append("```");

        for (CommandHandler.SimpleCommand simpleCommand : commandHandler.getCommands()) {

            if (!simpleCommand.getCommandAnnotation().showInHelpPage()) {
                continue; // skip command
            }
            builder.append("\n");
            if (simpleCommand.getCommandAnnotation().requiresMention()) {
                builder.append("@Winter-VLN Bot ");
            }
            String usage = simpleCommand.getCommandAnnotation().usage();
            if (usage.isEmpty()) { // no usage provided, using the first alias
                usage = simpleCommand.getCommandAnnotation().aliases()[0];
            }
            builder.append(usage);
            String description = simpleCommand.getCommandAnnotation().description();
            if (!description.equals("none")) {
                builder.append(" | ").append(description);
            }
        }

        builder.append("```"); // end of xml code block
        return builder.toString();
    }

}
