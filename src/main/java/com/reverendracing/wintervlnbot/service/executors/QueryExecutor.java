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
import static com.reverendracing.wintervlnbot.util.MessageUtil.sendStackTraceToChannel;
import static com.reverendracing.wintervlnbot.util.QueryFormatter.printDrivers;
import static com.reverendracing.wintervlnbot.util.QueryFormatter.printEntryDetails;

import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.reverendracing.wintervlnbot.data.Entry;
import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.rest.SheetsManager;

public class QueryExecutor implements CommandExecutor {

    private final SheetsManager sheets;

    private final String protectedRoleName;

    private final String adminChannelName;

    public QueryExecutor(
            final SheetsManager sheets,
            final String protectedRoleName,
            final String adminChannelName) {

        this.sheets = sheets;
        this.protectedRoleName = protectedRoleName;
        this.adminChannelName = adminChannelName;
    }

    @Command(aliases = "!team", description = "Get team details by name or number", usage = "!team [Team name or number]")
    public void onTeamQuery(String[] args, Message message, Server server, User user, TextChannel channel) {

        if(!(isRole(server, user, protectedRoleName) || hasAdminPermission(server, user)))
            return;

        String query = String.join(" ", args);
        try {
            EntryRepository repo = sheets.getEntryRepository();
            List<Entry> search = repo.searchEntry(query);
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

        if(!(isRole(server, user, protectedRoleName) || hasAdminPermission(server, user)))
            return;

        String query = String.join(" ", args);
        try {
            EntryRepository repo = sheets.getEntryRepository();
            List<Entry> search = repo.searchEntry(query);
            printDrivers(search, message.getChannel());
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
