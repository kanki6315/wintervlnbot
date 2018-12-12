/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.service;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;

import com.reverendracing.wintervlnbot.util.SessionTimes;

public class InfoExecutor implements CommandExecutor {

    String sessionTimeString =
            "Session Opens \t  %s\n"
            + "Qualifying\t  %s\n"
            + "Formation Lap Begins\t  %s\n"
            + "Race Start\t~%s";

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
                .append("Session Times\n", MessageDecoration.BOLD)
                .append(String.format(
                        sessionTimeString,
                        times.getSessionStartTime(),
                        times.getQualifyingStartTime(),
                        times.getFormationLapTime(),
                        times.getRaceStartTime()))
                .send(channel);

    }
}
