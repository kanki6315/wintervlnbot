/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class QualifyingManagementExecutor implements CommandExecutor {

    boolean qualiEnabled;

    private String qualifyingAnnouncementChannel;
    private String restApiUrl;

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

        sendBlackFlagRequest(args, message, announcementChannel);
    }

    @Command(aliases = "!enablequali", description = "Enable qualifying for all users", showInHelpPage = false)
    public void onQualiEnable(Message message, Server server, User user) {

        if(!isAdmin(server, user))
            return;
        qualiEnabled = true;
        announceQualifyingState("Open!", message, server);
    }

    @Command(aliases = "!disablequali", description = "Disable qualifying for all users", showInHelpPage = false)
    public void onQualiDisable(Message message, Server server, User user) {

        if(!isAdmin(server, user))
            return;
        qualiEnabled = false;
        announceQualifyingState("Closed!", message, server);
    }

    private boolean isAdmin(Server server, User user) {

        List<Role> roles = user.getRoles(server);
        return roles.stream()
                .map(Role::getAllowedPermissions)
                .flatMap(Collection::stream)
                .anyMatch(role -> role.equals(PermissionType.ADMINISTRATOR));
    }

    private void announceQualifyingState(final String state, final Message message, final Server server) {

        ServerTextChannel announcementChannel = getAnnouncementChannel(server);

        new MessageBuilder()
                .append("Qualifying is now ")
                .append(state, MessageDecoration.BOLD)
                .append("!")
                .send(announcementChannel);
        notifyChecked(message);
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
            return;
        }

        if(args.length > 1) {
            String solo = args[1];
            if(!solo.equalsIgnoreCase("s")) {
                new MessageBuilder()
                        .append("2nd argument must be empty or S, not ")
                        .append(args[1], MessageDecoration.BOLD)
                        .append(". Please try again.")
                        .send(announcementChannel);
                return;
            }
        }

        try {
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(
                    String.format(restApiUrl, Integer.toString(number))).newBuilder();
            if(args.length  > 1) {
                urlBuilder.addQueryParameter("solo", Boolean.toString(true));
            }
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(null, new byte[0]))
                    .build();

            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                notifyChecked(message);
            } else {
                notifyFailed(message);
            }

        } catch(IOException ioEx) {
            notifyFailed(message);
        }
    }
}
