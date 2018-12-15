/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import io.reactivex.Completable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.slf4j.LoggerFactory;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.reverendracing.wintervlnbot.util.BlackFlagMessage;

public class QualifyingManagementExecutor implements CommandExecutor {

    private boolean qualiEnabled;

    private String qualifyingAnnouncementChannel;
    private String restApiUrl;

    private HubConnection socket;

    public QualifyingManagementExecutor(
            final String qualifyingAnnouncementChannel,
            final String restApiUrl) {

        this.qualifyingAnnouncementChannel = qualifyingAnnouncementChannel;
        this.restApiUrl = restApiUrl;

        qualiEnabled = false;
    }

    @Command(aliases = {"!q", "!quali"}, description = "Request a black flag clearance for your car. Only responds to request in the correct channel.",
            usage = "!q [Car Number] [Optional - S for solo drivers]")
    public void onBlackFlagRequest(String[] args, Message message, Server server, ServerTextChannel channel) {

        ServerTextChannel announcementChannel = getAnnouncementChannel(server);
        if(channel.getId() != announcementChannel.getId())
            return;

        if(!qualiEnabled) {
            new MessageBuilder()
                    .append("Qualifying is not open right now.")
                    .send(announcementChannel);
            return;
        }

        if(args.length > 2) {
            new MessageBuilder()
                    .append("Too many arguments submitted - command supports a max of 2.")
                    .send(announcementChannel);
            return;
        }

        sendBlackFlagRequest(args, message, server, announcementChannel);
    }

    @Command(aliases = "!enablequali", description = "Enable qualifying for all users", showInHelpPage = false)
    public void onQualiEnable(Message message, Server server, User user, TextChannel channel) {

        if(!isAdmin(server, user))
            return;

        enableQuali(channel);
        if(qualiEnabled) {
            announceQualifyingState("Open!", server);
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    @Command(aliases = "!disablequali", description = "Disable qualifying for all users", showInHelpPage = false)
    public void onQualiDisable(Message message, Server server, User user, TextChannel channel) {

        if(!isAdmin(server, user))
            return;

        disableQuali(channel);
        if(!qualiEnabled) {
            announceQualifyingState("Closed!", server);
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    @Command(aliases = "!restartsocket", description = "Reset Socket", showInHelpPage = false)
    public void onRestartSocket(Message message, Server server, User user, TextChannel channel) {

        if(!isAdmin(server, user))
            return;

        if(restartSocket(channel, message)) {
            announceQualifyingState("Open!", server);
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    private boolean isAdmin(Server server, User user) {

        List<Role> roles = user.getRoles(server);
        return roles.stream()
                .map(Role::getAllowedPermissions)
                .flatMap(Collection::stream)
                .anyMatch(role -> role.equals(PermissionType.ADMINISTRATOR));
    }

    private void announceQualifyingState(final String state, final Server server) {

        ServerTextChannel announcementChannel = getAnnouncementChannel(server);

        new MessageBuilder()
                .append("Qualifying is now ")
                .append(state, MessageDecoration.BOLD)
                .append("!")
                .send(announcementChannel);
    }

    private ServerTextChannel getAnnouncementChannel(final Server server) {
        List<ServerTextChannel> searchChannels = server.getTextChannelsByName(qualifyingAnnouncementChannel);
        return searchChannels.get(0);
    }

    private void notifyChecked(Message message) {
        message.addReaction("👍");
    }

    private void notifyFailed(Message message) {
        message.addReaction("👎");
    }

    private void sendBlackFlagRequest(
            String[] args,
            Message message,
            Server server,
            ServerTextChannel announcementChannel) {

        String numberString = args[0].replace("#", "");
        int number;
        try {
            number = Integer.parseInt(numberString);
        } catch(NumberFormatException ex) {
            new MessageBuilder()
                    .append("Could not read car number from ")
                    .append(args[0], MessageDecoration.BOLD)
                    .append(". Please try again.")
                    .send(announcementChannel);
            notifyFailed(message);
            return;
        }

        if(args.length > 1) {
            String solo = args[1];
            if(!solo.equalsIgnoreCase("s")) {
                new MessageBuilder()
                        .append("2nd input must be S, not ")
                        .append(args[1], MessageDecoration.BOLD)
                        .append(". Please try again.")
                        .send(announcementChannel);
                notifyFailed(message);
                return;
            }
        }

        if(socket.getConnectionState().equals(HubConnectionState.DISCONNECTED)) {
            boolean restartConnection = startSocket(announcementChannel);

            if(!restartConnection) {
                announceQualifyingState("Suspended!", server);
                notifyFailed(message);
                qualiEnabled = false;
                return;
            }
        }
        socket.send("AddBlackFlag", new BlackFlagMessage(number, args.length > 1));
        notifyChecked(message);
    }

    private void enableQuali(TextChannel channel) {

        boolean startSocket = startSocket(channel);

        if(startSocket) {
            LoggerFactory.getLogger(QualifyingManagementExecutor.class).info(
                    String.format("KeepAlive %d", socket.getKeepAliveInterval()));
            LoggerFactory.getLogger(QualifyingManagementExecutor.class).info(
                    String.format("ServerTimeout %d", socket.getServerTimeout()));
            qualiEnabled = true;
        }
    }

    private void disableQuali(TextChannel channel) {

        boolean stopSocket = handleSocketConnection(HubConnection::stop, channel);

        if(stopSocket) {
            qualiEnabled = false;
        }
    }

    private boolean restartSocket(TextChannel channel, Message message) {

        boolean stopSocket = handleSocketConnection(HubConnection::stop, channel);

        if(!stopSocket) {
            notifyFailed(message);
            return false;
        }

        boolean startSocket = startSocket(channel);

        if(!startSocket) {
            notifyFailed(message);
            return false;
        }
        return true;
    }

    private boolean startSocket(TextChannel channel) {
        if(socket == null)
            socket = HubConnectionBuilder
                    .create(restApiUrl)
                    .build();

        boolean startSocket = handleSocketConnection(HubConnection::start, channel);

        if(!startSocket)
            return false;

        socket.send("AddToGroup", "Bot");
        return true;
    }

    private boolean handleSocketConnection(
            final Function<HubConnection, Completable> socketFunction,
            final TextChannel channel) {

        Completable completable = socketFunction.apply(socket);
        Throwable error = completable.blockingGet();

        if(error != null) {
            if(channel != null) {
                new MessageBuilder()
                        .append("Unable to manage connection to hub.")
                        .appendCode("java", error.getMessage())
                        .appendCode("java", ExceptionUtils.getStackTrace(error))
                        .send(channel);
            }
            return false;
        }
        return true;
    }
}
