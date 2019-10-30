/**
 * Copyright (C) 2018 by Amobee Inc.
 * All Rights Reserved.
 */

package com.reverendracing.wintervlnbot.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Precision;
import io.bretty.console.table.Table;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.springframework.util.StringUtils;

import com.reverendracing.wintervlnbot.data.Entry;

public class QueryFormatter {

    public static String getTableForUsers(Collection<User> searchUsers, Server server) {
        List<String> id = new ArrayList<>();
        List<String> status = new ArrayList<>();
        List<String> dateJoined = new ArrayList<>();
        for (User searchUser : searchUsers) {
            id.add(Long.toString(searchUser.getId()));
            status.add(searchUser.getStatus().getStatusString());
            dateJoined.add(searchUser.getJoinedAtTimestamp(server).orElse(Instant.now()).toString());
        }

        Table.Builder builder = new Table.Builder(
            "User Id", id.toArray(new String[0]), getStringFormatterWithWidth(id));
        addStringColumnToTable(builder, "Status", status);
        addStringColumnToTable(builder, "Join Date", dateJoined);

        return builder.build().toString();
    }

    public static void printEntryDetails(List<Entry> entries, TextChannel channel) {

        List<String> teamNames = new ArrayList<>();
        List<String> carClass = new ArrayList<>();
        List<String> carNumber = new ArrayList<>();
        List<String> teamId = new ArrayList<>();
        List<String> teamCountry = new ArrayList<>();
        List<String> gt3Choice = new ArrayList<>();
        List<String> teamManager = new ArrayList<>();

        if(entries.size() == 0) {
            new MessageBuilder()
                .append("Teams\n", MessageDecoration.BOLD)
                .append("```")
                .append("No matching entries found")
                .append("```")
                .send(channel);
            return;
        }

        for (Entry entry : entries) {
            teamNames.add(entry.getTeamName());
            carClass.add(entry.getCarClass());
            carNumber.add(entry.getCarNumber());
            teamId.add(entry.getTeamId());
            teamCountry.add(entry.getTeamCountry());
            gt3Choice.add(getInputOrEmptyString(entry.getGt3CarChoice()));
            teamManager.add(entry.getTeamManagerName());
        }

        Table.Builder builder = new Table.Builder(
                "Team Name",
                teamNames.toArray(new String[0]),
                getStringFormatterWithWidth(teamNames));
        addStringColumnToTable(builder, "Car Class", carClass);
        addNumberColumnToTable(builder, "Car Number", carNumber);
        addNumberColumnToTable(builder, "Team ID", teamId);
        addStringColumnToTable(builder, "Country", teamCountry);
        addStringColumnToTable(builder, "GT3 Car", gt3Choice);
        addStringColumnToTable(builder, "Team Manager", teamManager);

        Table table = builder.build();

        new MessageBuilder()
                .append("Teams\n", MessageDecoration.BOLD)
                .append("```")
                .append(table.toString())
                .append("```")
                .send(channel);
    }

    public static void printDrivers(List<Entry> entries, TextChannel channel) {

        if(entries.size() == 0) {
            new MessageBuilder()
                .append("Drivers\n", MessageDecoration.BOLD)
                .append("```")
                .append("No matching entries found")
                .append("```")
                .send(channel);
            return;
        }

        for (Entry entry : entries) {
            List<String> driverNames = new ArrayList<>();
            List<String> iracingIds = new ArrayList<>();
            List<String> countries = new ArrayList<>();
            driverNames.add(entry.getFirstDriverName());
            iracingIds.add(entry.getFirstDriverId());
            countries.add(entry.getFirstDriverCountry());

            if (!StringUtils.isEmpty(entry.getSecondDriverName())) {
                driverNames.add(entry.getSecondDriverName());
                iracingIds.add(getInputOrEmptyString(entry.getSecondDriverId()));
                countries.add(getInputOrEmptyString(entry.getSecondDriverCountry()));
            }
            if (!StringUtils.isEmpty(entry.getThirdDriverName())) {
                driverNames.add(getInputOrEmptyString(entry.getThirdDriverName()));
                iracingIds.add(getInputOrEmptyString(entry.getThirdDriverId()));
                countries.add(getInputOrEmptyString(entry.getThirdDriverCountry()));
            }
            if (!StringUtils.isEmpty(entry.getFourthDriverName())) {
                driverNames.add(getInputOrEmptyString(entry.getFourthDriverName()));
                iracingIds.add(getInputOrEmptyString(entry.getFourthDriverId()));
                countries.add(getInputOrEmptyString(entry.getFourthDriverCountry()));
            }
            if (!StringUtils.isEmpty(entry.getFifthDriverName())) {
                driverNames.add(getInputOrEmptyString(entry.getFifthDriverName()));
                iracingIds.add(getInputOrEmptyString(entry.getFifthDriverId()));
                countries.add(getInputOrEmptyString(entry.getFifthDriverCountry()));
            }
            printDriversForEntry(entry, driverNames, iracingIds, countries, channel);
        }
    }

    private static void printDriversForEntry(
            Entry entry,
            List<String> driverNames,
            List<String> iracingIds,
            List<String> countries,
            TextChannel channel) {

        Table.Builder builder = new Table.Builder(
                "Driver Name",
                driverNames.toArray(new String[0]),
                getStringFormatterWithWidth(driverNames));
        addNumberColumnToTable(builder, "iRacing ID", iracingIds);
        addStringColumnToTable(builder, "Country", countries);

        Table table = builder.build();
        new MessageBuilder()
                .append("Drivers: ", MessageDecoration.BOLD)
                .append(String.format("%s, %s, %s", entry.getTeamName(), entry.getCarClass(), entry.getCarNumber()))
                .append("```")
                .append(table.toString())
                .append("```")
                .send(channel);
    }

    private static String getInputOrEmptyString(String string) {
        return StringUtils.isEmpty(string) ? "" : string;
    }

    private static ColumnFormatter<String> getStringFormatterWithWidth(List<String> entries) {
        return ColumnFormatter.text(
                Alignment.CENTER,
                entries
                        .stream()
                        .max(Comparator.comparingInt(String::length))
                        .get()
                        .length());
    }

    private static ColumnFormatter<Number> getNumberFormatterWithWidth(String header) {
        return ColumnFormatter.number(
                Alignment.CENTER,
                header.length(),
                Precision.ZERO);
    }

    private static void addStringColumnToTable(Table.Builder table, String header, List<String> rows) {
        table.addColumn(
                header,
                rows.toArray(new String[0]),
                getStringFormatterWithWidth(rows));
    }

    private static void addNumberColumnToTable(Table.Builder table, String header, List<String> rows) {
        table.addColumn(
                header,
                rows
                        .stream()
                        .map(Integer::parseInt)
                        .toArray(Integer[]::new),
                getNumberFormatterWithWidth(header));
    }
}
