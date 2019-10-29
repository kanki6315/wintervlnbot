/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service.executors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.InviteBuilder;
import org.javacord.api.entity.user.User;

import com.reverendracing.wintervlnbot.util.model.SessionTimes;

import static com.reverendracing.wintervlnbot.util.MessageUtil.*;

public class InfoExecutor implements CommandExecutor {

    private final String entryListUrl;

    private final String changeRequestUrl;

    private final String standingsUrl;

    private final String protectedRoleName;

    private final String adminChannelName;

    private final String inviteChannelName;

    BufferedImage winterVlnLogo;

    public InfoExecutor(
            final String entryListUrl,
            final String changeRequestUrl,
            final String standingsUrl,
            final String protectedRoleName,
            final String adminChannelName,
            final String inviteChannelName) {

        this.entryListUrl = entryListUrl;
        this.changeRequestUrl = changeRequestUrl;
        this.standingsUrl = standingsUrl;
        this.protectedRoleName = protectedRoleName;
        this.adminChannelName = adminChannelName;
        this.inviteChannelName = inviteChannelName;
    }

    String sessionTimeString =
        "Session Opens:               %s\n"
            + "Qualifying:                       %s\n"
            + "Formation Lap Begins:  %s\n"
            + "Race Start:                     ~%s";

    String scheduleString =
              "Round 1: 23rd November 2019 - 4 hours\n"
            + "Round 2: 21st December 2019 - 4 hours\n"
            + "Round 3: 25th January 2020 - 4 hours\n"
            + "Round 4: 22nd February 2020 - 6 hours\n"
            + "Round 5: 7th March 2020 - 4 hours";

    @Command(aliases = "!invite", description = "Generate an invite link to the server", usage = "!invite")
    public void onInviteRequest(Message message, TextChannel channel, Server server) {

        // ensure its unique so that a user doesn't get an invite link with less time than expected
        new InviteBuilder(getChannelByName(inviteChannelName, server))
                .setUnique(true)
                .setMaxAgeInSeconds(60 * 60 * 24)
                .setMaxUses(100)
                .create()
                .thenAcceptAsync(invite -> {
                    new MessageBuilder()
                            .append(String.format("Invite Link: <%s>", invite.getUrl().toString()))
                            .send(channel);
                    notifyChecked(message);
                });
    }

    @Command(aliases = "!schedule", description = "Check the season schedule", usage = "!schedule")
    public void onSchedule(String[] args, TextChannel channel) {

        new MessageBuilder()
            .append("Season Schedule\n", MessageDecoration.BOLD)
            .append(scheduleString)
            .send(channel);
    }

    @Command(aliases = "!session", description = "See session times converted to your local time", usage = "!session [Timezone code]")
    public void onSessionTime(String[] args, TextChannel channel) {

        SessionTimes times = new SessionTimes();

        if (args.length > 0) {
            String timeZoneString = args[0].toUpperCase();
            List<String> timezoneList = Arrays.asList(TimeZone.getAvailableIDs());
            if (timezoneList.stream().noneMatch(str -> str.trim().equals(timeZoneString)) &&
                !timeZoneString.contains("GMT")) {
                new MessageBuilder()
                        .append("Unable to match timezone with ")
                        .append(timeZoneString, MessageDecoration.BOLD)
                        .append(" available Java timezone codes. Try using \"GMT<offset>\" instead.")
                        .send(channel);
                return;
            }

            ZoneId timeZone = TimeZone.getTimeZone(timeZoneString).toZoneId();
            times.convertWithZoneId(timeZone);
        }

        new MessageBuilder()
                .append(String.format("Session Times in %s\n", args.length > 0 ? args[0].toUpperCase() : "GMT"), MessageDecoration.BOLD)
                .append(String.format(
                        sessionTimeString,
                        times.getSessionStartTime(),
                        times.getQualifyingStartTime(),
                        times.getFormationLapTime(),
                        times.getRaceStartTime()))
                .send(channel);

    }

    @Command(aliases = "!entrylist", description = "See Entry List & Drivers", usage = "!entrylist")
    public void onEntryList(TextChannel channel, Server server)  {

        tryAndSendImageEmbedMessage(
                "Entry List & Drivers",
                entryListUrl,
                "Click the embedded link to be redirected to the entry list!",
                channel,
                server);
    }

    @Command(aliases = "!changeform", description = "Get Change Request Form. Restricted to drivers only", usage = "!changeform")
    public void onChangeForm(TextChannel channel, User user, Server server)  {

        if(!(isRole(server, user, protectedRoleName) || hasAdminPermission(server, user)))
            return;

        tryAndSendImageEmbedMessage(
                "Change Request Form",
                changeRequestUrl,
                "Click the embedded link to be redirected to the change request form!",
                channel,
                server);
    }

    @Command(aliases = "!standings", description = "Get Full Standings", usage = "!standings")
    public void onStandings(TextChannel channel, User user, Server server)  {

        tryAndSendImageEmbedMessage(
                "Championship Standings",
                standingsUrl,
                "Click the embedded link to be redirected to the championship standings!",
                channel,
                server);
    }

    private void tryAndSendImageEmbedMessage(
            final String title,
            final String url,
            final String description,
            TextChannel messageChannel,
            Server server) {

        try {
            if(this.winterVlnLogo == null) {
                URL imageUrl = new URL("https://artifactracing.com/assets/images/logos/vln-iracing-logo-winter_forum.jpg");
                this.winterVlnLogo = ImageIO.read(imageUrl);
            }

            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle(title)
                    .setUrl(url)
                    .setDescription(description)
                    .setColor(Color.BLUE)
                    .setImage(winterVlnLogo))
                    .send(messageChannel);
        } catch (Exception ex) {
            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle(title)
                    .setUrl(url)
                    .setDescription(description)
                    .setColor(Color.BLUE))
                    .send(messageChannel);

            sendStackTraceToChannel("Error parsing image for embed",
                    getChannelByName(adminChannelName, server), ex);
        }
    }
}
