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
import java.util.List;
import java.util.Optional;

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

public class AdminExecutor implements CommandExecutor {

    private final String roleName;

    public AdminExecutor(
        final String roleName) {
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
}