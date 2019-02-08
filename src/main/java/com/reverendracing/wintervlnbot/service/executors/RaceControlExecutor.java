/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service.executors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.getChannelByName;
import static com.reverendracing.wintervlnbot.util.MessageUtil.hasAdminPermission;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyChecked;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyFailed;
import static com.reverendracing.wintervlnbot.util.MessageUtil.sendStackTraceToChannel;

import java.util.function.Function;

import com.reverendracing.wintervlnbot.util.model.DecisionNotification;
import com.reverendracing.wintervlnbot.util.model.ProtestNotification;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import io.reactivex.Completable;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.slf4j.LoggerFactory;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.reverendracing.wintervlnbot.util.model.BlackFlagMessage;

public class RaceControlExecutor implements CommandExecutor {

    private boolean qualiEnabled;

    private String qualifyingAnnouncementChannel;
    private String protestAnnouncementChannel;

    private String adminChannel;
    private String restApiUrl;

    private DiscordApi api;
    private HubConnection socket;

    public RaceControlExecutor(
            final DiscordApi api,
            final String qualifyingAnnouncementChannel,
            final String protestAnnouncementChannel,
            final String adminChannel,
            final String restApiUrl) {

        this.api = api;
        this.qualifyingAnnouncementChannel = qualifyingAnnouncementChannel;
        this.protestAnnouncementChannel = protestAnnouncementChannel;
        this.adminChannel = adminChannel;
        this.restApiUrl = restApiUrl;

        qualiEnabled = false;
    }

    @Command(aliases = {"!q", "!quali"}, description = "Request a black flag clearance for your car. Only responds to request in the correct channel.",
            usage = "!q [Car Number] [Optional - S for solo drivers]")
    public void onBlackFlagRequest(String[] args, Message message, Server server, ServerTextChannel channel) {

        ServerTextChannel announcementChannel = getQualifyingChannel(server);
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

        if(!hasAdminPermission(server, user))
            return;

        if(isConnected()) {
            qualiEnabled = true;
            makeAnnouncement("Qualifying", "Open!", getQualifyingChannel(server));
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    @Command(aliases = "!disablequali", description = "Disable qualifying for all users", showInHelpPage = false)
    public void onQualiDisable(Message message, Server server, User user, TextChannel channel) {

        if(!hasAdminPermission(server, user))
            return;

        qualiEnabled = false;
        makeAnnouncement("Qualifying", "Closed!", getQualifyingChannel(server));
        notifyChecked(message);
    }

    @Command(aliases = "!restartsocket", description = "Reset Socket", showInHelpPage = false)
    public void onRestartSocket(Message message, Server server, User user) {

        if(!hasAdminPermission(server, user))
            return;

        if(restartSocket(server, message)) {
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    @Command(aliases = "!startsocket", description = "Start Socket", showInHelpPage = false)
    public void onStartSocket(Message message, Server server, User user) {

        if(!hasAdminPermission(server, user))
            return;

        if(startSocket(server)) {
            makeAnnouncement("Session", "Open", getAnnouncementChannel(server));
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    @Command(aliases = "!stopsocket", description = "Start Socket", showInHelpPage = false)
    public void onStopSocket(Message message, Server server, User user) {

        if(!hasAdminPermission(server, user))
            return;

        if(startSocket(server)) {
            makeAnnouncement("Session", "Closed", getAnnouncementChannel(server));
            notifyChecked(message);
        } else {
            notifyFailed(message);
        }
    }

    private void makeAnnouncement(final String session, final String state, final ServerTextChannel announcementChannel) {

        new MessageBuilder()
                .append(String.format("%s is now ", session))
                .append(state, MessageDecoration.BOLD)
                .append("!")
                .send(announcementChannel);
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

        if(!isConnected()) {
            boolean restartConnection = startSocket(server);

            if(!restartConnection) {
                makeAnnouncement("Qualifying", "Suspended!", getQualifyingChannel(server));
                notifyFailed(message);
                qualiEnabled = false;
                return;
            }
        }
        socket.send("AddBlackFlag", new BlackFlagMessage(number, args.length > 1));
        notifyChecked(message);
    }

    private boolean restartSocket(Server server, Message message) {

        boolean stopSocket = handleSocketConnection(HubConnection::stop, server);

        if(!stopSocket) {
            notifyFailed(message);
            return false;
        }

        boolean startSocket = startSocket(server);

        if(!startSocket) {
            notifyFailed(message);
            return false;
        }
        return true;
    }

    private boolean startSocket(Server server) {

        if(socket == null) {
            socket = buildConnectionAndMethods();
        }
        else if(isConnected()) {
            return true;
        }

        boolean startSocket = handleSocketConnection(HubConnection::start, server);

        if(!startSocket)
            return false;

        socket.send("AddToGroup", "Bot");
        LoggerFactory.getLogger(RaceControlExecutor.class).info(
                String.format("KeepAlive %d", socket.getKeepAliveInterval()));
        LoggerFactory.getLogger(RaceControlExecutor.class).info(
                String.format("ServerTimeout %d", socket.getServerTimeout()));
        return true;
    }

    private boolean handleSocketConnection(
            final Function<HubConnection, Completable> socketFunction,
            final Server server) {

        Completable completable = socketFunction.apply(socket);
        Throwable error = completable.blockingGet();

        if(error != null) {
            sendStackTraceToChannel(
                    "Unable to manage connection to hub.",
                    getChannelByName(adminChannel, server), error);
            return false;
        }
        return true;
    }

    private HubConnection buildConnectionAndMethods() {

        HubConnection connection = HubConnectionBuilder
                .create(restApiUrl)
                .build();
        connection.on("AnnounceProtest", (protestNotification) -> {
            ServerTextChannel channel = api.getServerTextChannelsByName(protestAnnouncementChannel).stream().findFirst().get();
            new MessageBuilder()
                    .append("Incident under investigation. Cars ")
                    .append(String.format("#%d",protestNotification.getProtestingCarNumber()), MessageDecoration.BOLD)
                    .append(" & ")
                    .append(String.format("#%d", protestNotification.getOffendingCarNumber()), MessageDecoration.BOLD)
                    .append(" - ")
                    .append(protestNotification.getReason(), MessageDecoration.BOLD)
                    .send(channel);
        }, ProtestNotification.class);
        connection.on("AnnounceDecision", (decisionNotification) -> {

            ServerTextChannel channel = api.getServerTextChannelsByName(protestAnnouncementChannel).stream().findFirst().get();
            if(decisionNotification.getDecision().equals("No Further Action")) {
                new MessageBuilder()
                        .append("No Further Action. Cars ")
                        .append(String.format("#%d", decisionNotification.getOtherCarNumber()), MessageDecoration.BOLD)
                        .append(" & ")
                        .append(String.format("#%d", decisionNotification.getPenalizedCarNumber()), MessageDecoration.BOLD)
                        .append(" - ")
                        .append(decisionNotification.getReason(), MessageDecoration.BOLD)
                        .send(channel);
            }
            else {
                new MessageBuilder()
                        .append(decisionNotification.getDecision())
                        .append(" - ")
                        .append(decisionNotification.getReason())
                        .append(". ")
                        .append(String.format("#%d", decisionNotification.getPenalizedCarNumber()), MessageDecoration.BOLD)
                        .append(" : ")
                        .append(decisionNotification.getPenalty(), MessageDecoration.BOLD)
                        .send(channel);
            }
        }, DecisionNotification.class);

        return connection;
    }

    private boolean isConnected() {
        if(socket == null)
            return false;
        return HubConnectionState.CONNECTED.equals(socket.getConnectionState());
    }

    private ServerTextChannel getQualifyingChannel(Server server) {
        return getChannelByName(qualifyingAnnouncementChannel, server);
    }

    private ServerTextChannel getAnnouncementChannel(Server server) {
        return getChannelByName(protestAnnouncementChannel, server);
    }
}
