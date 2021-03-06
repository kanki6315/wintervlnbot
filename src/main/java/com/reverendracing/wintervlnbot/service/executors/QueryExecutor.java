/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service.executors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.getChannelByName;
import static com.reverendracing.wintervlnbot.util.MessageUtil.hasAdminPermission;
import static com.reverendracing.wintervlnbot.util.MessageUtil.isRole;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyChecked;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyFailed;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyUnallowed;
import static com.reverendracing.wintervlnbot.util.MessageUtil.sendStackTraceToChannel;
import static com.reverendracing.wintervlnbot.util.QueryFormatter.printDrivers;
import static com.reverendracing.wintervlnbot.util.QueryFormatter.printEntryDetails;

import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.reverendracing.wintervlnbot.data.DriverRepository;
import com.reverendracing.wintervlnbot.data.Entry;
import com.reverendracing.wintervlnbot.data.EntryRepository;

public class QueryExecutor implements CommandExecutor {

    private final EntryRepository entryRepository;
    private final DriverRepository driverRepository;

    private final String protectedRoleName;

    private final String adminChannelName;

    public QueryExecutor(
            final EntryRepository entryRepository,
            final DriverRepository driverRepository,
            final String protectedRoleName,
            final String adminChannelName) {

        this.entryRepository = entryRepository;
        this.driverRepository = driverRepository;
        this.protectedRoleName = protectedRoleName;
        this.adminChannelName = adminChannelName;
    }

    private boolean checkPermisionAndQuery(String[] args, Message message, Server server, User user, TextChannel channel) {
        if(!(isRole(server, user, protectedRoleName) || hasAdminPermission(server, user)))
            return false;

        String query = String.join(" ", args);
        if(StringUtils.isEmpty(query)) {
            new MessageBuilder()
                .append("Cannot perform an empty search - please enter a car number or team name")
                .send(channel);
            notifyUnallowed(message);
            return false;
        }
        return true;
    }

    @Command(aliases = "!team", description = "Get team details by name or number", usage = "!team [Team name or number]")
    public void onTeamQuery(String[] args, Message message, Server server, User user, TextChannel channel) {

        if(!checkPermisionAndQuery(args, message, server, user, channel)) {
            return;
        }

        String query = String.join(" ", args);
        try {
            List<Entry> search = entryRepository.searchEntry(query);
            printEntryDetails(search, message.getChannel());
            notifyChecked(message);

        } catch (Exception ex) {
            new MessageBuilder()
                    .append("Unable to perform query with ")
                    .append(query, MessageDecoration.BOLD)
                    .send(channel);
            notifyFailed(message);
            sendStackTraceToChannel(
                    "Error when performing team query",
                    getChannelByName(adminChannelName, server),
                    ex);
        }
    }

    @Command(aliases = "!driver", description = "Get Driver details by name or number", usage = "!driver [Team name or number]")
    public void onDriverQuery(String[] args, Message message, Server server, User user, TextChannel channel) {

        if(!checkPermisionAndQuery(args, message, server, user, channel)) {
            return;
        }

        String query = String.join(" ", args);
        try {
            List<Entry> search = entryRepository.searchEntry(query);
            printDrivers(server, search, driverRepository, message.getChannel());
            notifyChecked(message);
        } catch (Exception ex) {
            new MessageBuilder()
                    .append("Unable to perform query with ")
                    .append(query, MessageDecoration.BOLD)
                    .send(channel);
            notifyFailed(message);
            sendStackTraceToChannel(
                    "Error when performing driver query",
                    getChannelByName(adminChannelName, server),
                    ex);
        }
    }
}
