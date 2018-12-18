/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service.executors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.getChannelByName;
import static com.reverendracing.wintervlnbot.util.MessageUtil.hasAdminPermission;
import static com.reverendracing.wintervlnbot.util.MessageUtil.isRole;
import static com.reverendracing.wintervlnbot.util.MessageUtil.sendStackTraceToChannel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import com.reverendracing.wintervlnbot.util.model.SessionTimes;

public class InfoExecutor implements CommandExecutor {

    private final String entryListUrl;

    private final String changeRequestUrl;

    private final String protectedRoleName;

    private final String adminChannelName;

    public InfoExecutor(
            final String entryListUrl,
            final String changeRequestUrl,
            final String protectedRoleName,
            final String adminChannelName) {

        this.entryListUrl = entryListUrl;
        this.changeRequestUrl = changeRequestUrl;
        this.protectedRoleName = protectedRoleName;
        this.adminChannelName = adminChannelName;
    }

    String sessionTimeString =
              "Session Opens:               %s\n"
            + "Qualifying:                       %s\n"
            + "Formation Lap Begins:  %s\n"
            + "Race Start:                     ~%s";

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

    private void tryAndSendImageEmbedMessage(
            final String title,
            final String url,
            final String description,
            TextChannel messageChannel,
            Server server) {

        try {
            File imageFile =
                    new File(this.getClass().getClassLoader().getResource("winter-vln-logo.jpg").getFile());
            BufferedImage winterVlnLogo = ImageIO.read(imageFile);

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
