/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.service.executors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.hasAdminPermission;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyChecked;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyFailed;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyUnallowed;
import static com.reverendracing.wintervlnbot.util.QueryFormatter.getTableForUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.text.html.Option;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import io.bretty.console.table.Table;

import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.reverendracing.wintervlnbot.data.Driver;
import com.reverendracing.wintervlnbot.data.DriverRepository;
import com.reverendracing.wintervlnbot.data.Entry;
import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;

public class AdminExecutor implements CommandExecutor {

    private final RequestBuilder requestBuilder;

    private final String leagueId;
    private final String roleName;

    private final EntryRepository entryRepository;
    private final DriverRepository driverRepository;

    public AdminExecutor(
        final RequestBuilder requestBuilder,
        final EntryRepository entryRepository,
        final DriverRepository driverRepository,
        final String leagueId,
        final String roleName) {
        this.requestBuilder = requestBuilder;
        this.entryRepository = entryRepository;
        this.driverRepository = driverRepository;
        this.leagueId = leagueId;
        this.roleName = roleName;
    }

    @Command(aliases = "!addrole", description = "Add Role to User by Nickname or ID", showInHelpPage = false)
    public void onRoleAdd(String[] args, Message message, Server server, User user, TextChannel channel) {

        if(!hasAdminPermission(server, user))
            return;

        if(args.length == 0) {
            notifyUnallowed(message);
            new MessageBuilder()
                .append("Unable to find user without input")
                .send(channel);
            return;
        }

        User member;
        if(args.length == 1 && StringUtils.isNumeric(args[0])) {
            Optional<User> searchUser = server.getMemberById(args[0]);
            if(!searchUser.isPresent()) {
                notifyFailed(message);
                new MessageBuilder()
                    .append("Unable to find user with id: ")
                    .append(args[0], MessageDecoration.BOLD)
                    .send(channel);
                return;
            }
            member = searchUser.get();
        } else {
            String query = String.join(" ", args);
            Collection<User> searchUsers = server.getMembersByNicknameIgnoreCase(query);
            if(searchUsers.size() == 0) {
                notifyFailed(message);
                new MessageBuilder()
                    .append("Unable to find user with nickname: ")
                    .append(query, MessageDecoration.BOLD)
                    .send(channel);
                return;
            }
            if(searchUsers.size() > 1) {
                notifyFailed(message);
                String table = getTableForUsers(searchUsers, server);
                new MessageBuilder()
                    .append("Found multiple users with nickname: ")
                    .append(query, MessageDecoration.BOLD)
                    .append(". Please use discord id to assign role to user.")
                    .append("```")
                    .append(table)
                    .append("```")
                    .send(channel);
                return;
            }
            member = searchUsers.iterator().next();
        }

        Role role = server.getRolesByName(roleName).get(0);
        member.addRole(role);
        notifyChecked(message);
    }

    @Command(aliases = "!refresh", description = "Refresh bot db from api", showInHelpPage = false)
    public void onRefreshEntries(String[] args, Message message, Server server, User user, TextChannel channel) {

        if(!hasAdminPermission(server, user))
            return;

        driverRepository.deleteAll();
        entryRepository.deleteAll();

        try {
            List<Entry> entries = requestBuilder.getEntries(leagueId);
            List<Driver> drivers = entries.stream().map(Entry::getDrivers)
                .flatMap(Collection::stream).collect(Collectors.toList());
            entries.stream().forEach(e -> e.setDrivers(Collections.emptyList()));
            entryRepository.saveAll(entries);
            drivers.forEach(d -> {
                d.setEntry(entryRepository.findById(d.getEntryId()).get());
            });
            driverRepository.saveAll(drivers);
            notifyChecked(message);
        }
        catch(Exception ex) {
            notifyFailed(message);
            new MessageBuilder()
                .append("Error while refreshing: ")
                .append(ex.getMessage(), MessageDecoration.BOLD)
                .send(channel);
            return;
        }
    }
}