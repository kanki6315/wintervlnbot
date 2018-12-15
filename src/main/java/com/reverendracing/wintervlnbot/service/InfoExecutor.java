/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

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

import com.reverendracing.wintervlnbot.util.SessionTimes;

public class InfoExecutor implements CommandExecutor {

    private final String entryListUrl;

    public InfoExecutor(String entryListUrl) {
        this.entryListUrl = entryListUrl;
    }

    String sessionTimeString =
              "Session Opens:               %s\n"
            + "Qualifying:                       %s\n"
            + "Formation Lap Begins:  %s\n"
            + "Race Start:                     ~%s";

    @Command(aliases = "!session", description = "See session times converted to your local time", usage = "!session [Timezone code]", requiresMention = false)
    public void onSessionTime(String[] args, TextChannel channel) {

        SessionTimes times = new SessionTimes();

        if (args.length > 0) {
            String timeZoneString = args[0].toUpperCase();
            List<String> timezoneList = Arrays.asList(TimeZone.getAvailableIDs());
            if (!timezoneList.stream().anyMatch(str -> str.trim().equals(timeZoneString)) &&
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
    public void onEntryList(TextChannel channel)  {

        try {

            File imageFile =
                    new File(this.getClass().getClassLoader().getResource("winter-vln-logo.jpg").getFile());
            BufferedImage winterVlnLogo = ImageIO.read(imageFile);

            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle("Entry List & Drivers")
                    .setUrl(entryListUrl)
                    .setDescription("Click the embedded link to be redirected to the entry list!")
                    .setColor(Color.BLUE)
                    .setImage(winterVlnLogo))
                    .send(channel);
        } catch (Exception ex) {
            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle("Entry List & Drivers")
                    .setUrl(entryListUrl)
                    .setDescription("Click the embedded link to be redirected to the entry list!")
                    .setColor(Color.BLUE))
                    .send(channel);
        }
    }
}
