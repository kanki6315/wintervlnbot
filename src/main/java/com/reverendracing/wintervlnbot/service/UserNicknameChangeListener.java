/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.user.UserChangeNicknameListener;

public class UserNicknameChangeListener implements UserChangeNicknameListener {

    private final String channelReportName;
    private final String roleName;

    public UserNicknameChangeListener(
            final String channelName,
            final String roleName) {
        this.channelReportName = channelName;
        this.roleName = roleName;
    }

    @Override
    public void onUserChangeNickname(UserChangeNicknameEvent event) {

        User user = event.getUser();
        Role role = event.getServer().getRolesByName(roleName).get(0);
        List<ServerTextChannel> adminChannels = event.getServer().getTextChannelsByName(channelReportName);
        ServerTextChannel adminChannel = adminChannels.get(0);

        new MessageBuilder()
                .append("Nickname update: ")
                .append(event.getNewNickname().get(), MessageDecoration.BOLD)
                .append(". Do you you want to assign the driver role to this user?")
                .send(adminChannel)
        .thenAcceptAsync(message -> {
            message.addReactionAddListener(new ReactionAddListener() {
                @Override
                public void onReactionAdd(ReactionAddEvent reaction) {
                    if (reaction.getEmoji().equalsEmoji("👍")) {
                        user.addRole(role);
                        reaction.getApi().removeListener(this);
                    } else if(reaction.getEmoji().equalsEmoji("👎")) {
                        reaction.getApi().removeListener(this);
                    }
                }
            }
            ).removeAfter(1, TimeUnit.HOURS);
        });
    }
}
