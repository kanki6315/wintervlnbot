/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service.executors;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.InviteBuilder;
import org.javacord.api.entity.user.User;

import com.reverendracing.wintervlnbot.util.model.SessionTimes;

import static com.reverendracing.wintervlnbot.util.MessageUtil.*;

public class InfoExecutor implements CommandExecutor {

    private final static String websiteUrl = "https://www.isowc.org";


    private final String adminChannelName;

    private final String inviteChannelName;

    BufferedImage seriesLogo;

    public InfoExecutor(
            final String adminChannelName,
            final String inviteChannelName) {
        this.adminChannelName = adminChannelName;
        this.inviteChannelName = inviteChannelName;
    }

    String sessionTimeString =
              "Practice & Q Session Opens:     %s\n"
            + "Drivers Briefing:                           %s\n"
            + "Qualifying Ends:                           %s\n"
            + "Race Session Opens:                   %s\n"
            + "Formation Lap Begins:                 %s\n"
            + "Race Start:                                    ~%s";

    String scheduleString =
              "Round 1: 8th May 2021 - Watkins Glen - 60L\n"
            + "Round 2: 5th June 2021 - Sebring - 40L\n"
            + "Round 3: 19th June 2021 - Auto Club - 150L\n"
            + "Round 4: 26th June 2021 - Long Beach - 85L\n"
            + "Round 5: 17th July 2021 - Road America - 50L\n"
            + "24th/25th July 2021 - ISOWC 500 Qualifying\n"
            + "Round 6: 31st July 2021 - ISOWC 500 - 200L\n";

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
                        times.getDriversBriefingTime(),
                        times.getQualifyingEndTime(),
                        times.getRaceStartTime(),
                        times.getFormationLapTime(),
                        times.getRaceStartTime()))
                .send(channel);

    }

    @Command(aliases = "!entrylist", description = "See Entry List & Driver", usage = "!entrylist")
    public void onEntryList(TextChannel channel, Server server)  {

        tryAndSendImageEmbedMessage(
                "Entry List & Driver",
                websiteUrl + "/entries",
                "Click the embedded link to be redirected to the entry list!",
                channel,
                server);
    }

    @Command(aliases = "!news", description = "See Entry List & Driver", usage = "!news")
    public void onNews(TextChannel channel, Server server)  {

        tryAndSendImageEmbedMessage(
            "News",
            "https://news.isowc.org",
            "Click the embedded link to be redirected to the latest ISOWC news!",
            channel,
            server);
    }

    @Command(aliases = "!standings", description = "Get Full Standings", usage = "!standings")
    public void onStandings(TextChannel channel, User user, Server server)  {

        tryAndSendImageEmbedMessage(
                "Championship Standings",
                websiteUrl + "/standings",
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
            if(this.seriesLogo == null) {
                URL imageUrl = new URL("https://racespot.media/assets/thumbnails/ISOWC1.png");
                this.seriesLogo = ImageIO.read(imageUrl);
            }

            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle(title)
                    .setUrl(url)
                    .setDescription(description)
                    .setColor(Color.RED)
                    .setImage(seriesLogo))
                    .send(messageChannel);
        } catch (Exception ex) {
            new MessageBuilder().setEmbed(new EmbedBuilder()
                    .setTitle(title)
                    .setUrl(url)
                    .setDescription(description)
                    .setColor(Color.RED))
                    .send(messageChannel);

            sendStackTraceToChannel("Error parsing image for embed",
                    getChannelByName(adminChannelName, server), ex);
        }
    }
}
