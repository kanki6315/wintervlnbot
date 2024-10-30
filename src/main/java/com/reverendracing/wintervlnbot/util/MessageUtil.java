/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.util;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class MessageUtil {

    public static void sendStackTraceToChannel(
            String message,
            TextChannel channel,
            Throwable error) {

        String stackTrace = ExceptionUtils.getStackTrace(error);
        new MessageBuilder()
                .append(message)
                .appendCode("java", error.getMessage())
                .appendCode("java", stackTrace.substring(0, Math.min(stackTrace.length(), 1000)))
                .send(channel);
    }

    public static boolean isRole(Server server, User user, String roleName) {

        List<Role> roles = user.getRoles(server);
        return roles.stream()
                .anyMatch(role -> role.getName().contentEquals(roleName));
    }

    public static boolean hasAdminPermission(Server server, User user) {

        List<Role> roles = user.getRoles(server);
        return roles.stream()
                .map(Role::getAllowedPermissions)
                .flatMap(Collection::stream)
                .anyMatch(role -> role.equals(PermissionType.ADMINISTRATOR));
    }

    public static ServerTextChannel getChannelByName(
            final String name,
            final Server server) {
        List<ServerTextChannel> searchChannels = server.getTextChannelsByName(name);
        return searchChannels.get(0);
    }

    public static ServerTextChannel getChannelById(
            final String id,
            final Server server) {
        return server.getTextChannelById(id).get();
    }
}
