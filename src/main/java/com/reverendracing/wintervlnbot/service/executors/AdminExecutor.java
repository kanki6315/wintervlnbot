/**
 * Copyright (C) 2019 by Amobee Inc.
 * All Rights Reserved.
 */
package com.reverendracing.wintervlnbot.service.executors;

import static com.reverendracing.wintervlnbot.util.MessageUtil.getChannelByName;
import static com.reverendracing.wintervlnbot.util.MessageUtil.hasAdminPermission;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyChecked;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyFailed;
import static com.reverendracing.wintervlnbot.util.MessageUtil.notifyUnallowed;
import static com.reverendracing.wintervlnbot.util.MessageUtil.sendStackTraceToChannel;
import static com.reverendracing.wintervlnbot.util.QueryFormatter.getTableForUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.text.html.Option;

import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import io.bretty.console.table.Table;

import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.reverendracing.wintervlnbot.data.Class;
import com.reverendracing.wintervlnbot.data.ClassRepository;
import com.reverendracing.wintervlnbot.data.Driver;
import com.reverendracing.wintervlnbot.data.DriverRepository;
import com.reverendracing.wintervlnbot.data.Entry;
import com.reverendracing.wintervlnbot.data.EntryCrew;
import com.reverendracing.wintervlnbot.data.EntryCrewRepository;
import com.reverendracing.wintervlnbot.data.EntryRepository;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;

public class AdminExecutor implements CommandExecutor {

    private final RequestBuilder requestBuilder;

    private final String leagueId;
    private final String roleName;
    private final String adminChannelId;
    private final String serverId;

    private final EntryRepository entryRepository;
    private final DriverRepository driverRepository;
    private final ClassRepository classRepository;
    private final EntryCrewRepository entryCrewRepository;

    private final DiscordApi api;

    private final Logger logger;

    public AdminExecutor(
        final RequestBuilder requestBuilder,
        final EntryRepository entryRepository,
        final DriverRepository driverRepository,
        final ClassRepository classRepository,
        final EntryCrewRepository entryCrewRepository,
        final DiscordApi api,
        final String leagueId,
        final String roleName,
        final String adminChannelId,
        final String serverId) {
        this.requestBuilder = requestBuilder;
        this.entryRepository = entryRepository;
        this.driverRepository = driverRepository;
        this.classRepository = classRepository;
        this.entryCrewRepository = entryCrewRepository;
        this.api = api;
        this.leagueId = leagueId;
        this.roleName = roleName;
        this.adminChannelId = adminChannelId;
        this.serverId = serverId;

        this.logger = LoggerFactory.getLogger(AdminExecutor.class);
    }

    @Command(aliases = "!addrole", description = "Add Role to User by Nickname or ID", showInHelpPage = false)
    public void onRoleAdd(String[] args, Message message, Server server, User user, TextChannel channel) {

        if (!hasAdminPermission(server, user))
            return;

        if (args.length == 0) {
            notifyUnallowed(message);
            new MessageBuilder()
                .append("Unable to find user without input")
                .send(channel);
            return;
        }

        User member;
        if (args.length == 1 && StringUtils.isNumeric(args[0])) {
            Optional<User> searchUser = server.getMemberById(args[0]);
            if (!searchUser.isPresent()) {
                notifyFailed(message);
                new MessageBuilder()
                    .append("Unable to find user with id: ")
                    .append(args[0], MessageDecoration.BOLD)
                    .send(channel);
                return;
            }
            member = searchUser.get();
        } else {
            String query = String.join(" ", args);
            Collection<User> searchUsers = server.getMembersByNicknameIgnoreCase(query);
            if (searchUsers.size() == 0) {
                notifyFailed(message);
                new MessageBuilder()
                    .append("Unable to find user with nickname: ")
                    .append(query, MessageDecoration.BOLD)
                    .send(channel);
                return;
            }
            if (searchUsers.size() > 1) {
                notifyFailed(message);
                String table = getTableForUsers(searchUsers, server);
                new MessageBuilder()
                    .append("Found multiple users with nickname: ")
                    .append(query, MessageDecoration.BOLD)
                    .append(". Please use discord id to assign role to user.")
                    .append("```")
                    .append(table)
                    .append("```")
                    .send(channel);
                return;
            }
            member = searchUsers.iterator().next();
        }

        Role role = server.getRolesByName(roleName).get(0);
        member.addRole(role);
        notifyChecked(message);
    }

    @Command(aliases = "!refresh", description = "Refresh bot db from api", showInHelpPage = false)
    public void onRefreshEntries(
        String[] args,
        Message message,
        Server server,
        User user,
        TextChannel channel) {

        if (!hasAdminPermission(server, user))
            return;

        entryCrewRepository.deleteAll();
        driverRepository.deleteAll();
        entryRepository.deleteAll();
        classRepository.deleteAll();

        try {
            List<Class> classes = requestBuilder.getClasses(leagueId);
            classRepository.saveAll(classes);
            syncTeamsAndDrivers();
            notifyChecked(message);
        } catch (Exception ex) {
            notifyFailed(message);
            new MessageBuilder()
                .append("Error while refreshing: ")
                .append(ex.getMessage(), MessageDecoration.BOLD)
                .send(channel);
            return;
        }
    }

    private String getRoleNameFromEntry(Entry entry) {
        return String.format("#%s", entry.getCarNumber());
    }

    private String getVoiceChannelNameFromEntry(Entry entry, Driver driver) {
        return String.format("#%s - %s", entry.getCarNumber(), driver.getDriverName());
    }

    @Scheduled(fixedRate = 1800000, initialDelay = 30000)
    public void syncDiscordRoles() {
        logger.info("Starting sync");
        try {
            Optional<Server> serverOpt = api.getServerById(serverId);
            if(!serverOpt.isPresent()) {
                throw new Exception("Not in server");
            }

            logger.info("Refreshing teams/drivers");
            entryCrewRepository.deleteAll();
            driverRepository.deleteAll();
            entryRepository.deleteAll();
            syncTeamsAndDrivers();
            logger.info("Refreshed teams + drivers");

            Server server = serverOpt.get();
            Role driverRole = server.getRolesByName(roleName).get(0);

            List<Class> classes = classRepository.findAll();
            int newCounter = 0;
            int removeCounter = 0;
            int wipedCounter = 0;

            for(Class rClass : classes) {
                logger.info(String.format("Starting sync for %s", rClass.getName()));
                Role classRole = server.getRoleById(rClass.getdRoleId()).get();
                List<Entry> entries = entryRepository.findByClassId(rClass.getId());
                HashSet<String> userIds = new HashSet<>();

                for(Entry entry : entries) {
                    if(entry.getdRoleId() == null) {
                        logger.info(String.format("Skipping sync for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                        continue;
                    }
                    Driver driver = driverRepository.findByEntryId(entry.getId()).get(0);
                    List<EntryCrew> entryCrews = entryCrewRepository.findByEntryId(entry.getId());
                    logger.info(String.format("Starting sync for %s - %s", entry.getCarNumber(), driver.getDriverName()));
                    ServerUpdater updater = new ServerUpdater(server);
                    Role entryRole = server.getRoleById(entry.getdRoleId()).get();
                    if (!getRoleNameFromEntry(entry).equalsIgnoreCase(entryRole.getName())) {
                        logger.info(String.format("Updating role name for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                        entryRole.updateName(getRoleNameFromEntry(entry)).join();
                    }
                    ServerVoiceChannel voiceChannel = server.getVoiceChannelById(entry.getdVoiceChannelId()).get();
                    if (!getVoiceChannelNameFromEntry(entry, driver).equalsIgnoreCase(voiceChannel.getName())) {
                        logger.info(String.format("Updating voice channel name for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                        voiceChannel.updateName(getVoiceChannelNameFromEntry(entry, driver)).join();
                    }
                    List<String> discordIds = entryCrews.stream().map(EntryCrew::getDiscordUserId).distinct().collect(
                        Collectors.toList());
                    boolean hasUpdates = false;

                    if (StringUtils.isNotEmpty(entry.getdTeamManagerId())) {
                        if(!discordIds.contains(entry.getdTeamManagerId())) {
                            discordIds.add(entry.getdTeamManagerId());
                        }
                    }

                    logger.info(String.format("%d users found to sync", discordIds.size()));
                    for(String discordId : discordIds) {
                        Optional<User> optUser = server.getMemberById(discordId);
                        if(!optUser.isPresent()) {
                            logger.info(String.format("User %s not in server", discordId));
                            continue;
                        }
                        User user = optUser.get();
                        userIds.add(discordId);
                        List<Role> roles = user.getRoles(server);
                        List<Role> rolesTBA = new ArrayList<>();
                        if(!roles.contains(classRole)) {
                            rolesTBA.add(classRole);
                        }
                        if(!roles.contains(entryRole)) {
                            rolesTBA.add(entryRole);
                        }
                        if(!roles.contains(driverRole)) {
                            rolesTBA.add(driverRole);
                        }
                        if(rolesTBA.size() > 0) {
                            hasUpdates = true;
                            newCounter++;
                            updater.addRolesToUser(user, rolesTBA);
                        }
                    }
                    List<User> usersWithEntryRolesToBeRemoved =
                        entryRole.getUsers().stream().filter(u -> !discordIds.contains(u.getIdAsString())).collect(Collectors.toList());
                    if(!hasUpdates && usersWithEntryRolesToBeRemoved.size() > 0) hasUpdates = true;
                    for(User user : usersWithEntryRolesToBeRemoved) {
                        removeCounter++;
                        updater.removeRoleFromUser(user, entryRole);
                    }

                    if(hasUpdates) {
                        logger.info("Queued updates, executing now");
                        updater.update().join();
                        logger.info("Completed updates and sync, sleeping for 5s");
                        Thread.sleep(5000);
                    } else {
                        logger.info("No updates for entry, moving to next one");
                    }
                }


                List<User> usersWithDriverRolesToBeRemoved =
                    driverRole.getUsers().stream().filter(u -> !userIds.contains(u.getIdAsString())).collect(Collectors.toList());
                if (usersWithDriverRolesToBeRemoved.size() > 0) {
                    logger.info(String.format("Removing all roles from %d users", usersWithDriverRolesToBeRemoved.size()));
                    wipedCounter += usersWithDriverRolesToBeRemoved.size();
                    ServerUpdater updater = new ServerUpdater(server);
                    for(User user : usersWithDriverRolesToBeRemoved) {
                        updater.removeRolesFromUser(user, user.getRoles(server));
                    }
                    updater.update().join();
                }

                logger.info("Finished syncing class " + rClass.getName());
            }
            logger.info(String.format("Discord Sync successful. %d users were updated", newCounter));
            logger.info(String.format("Discord Sync successful. %d users had roles removed", removeCounter));
            logger.info(String.format("Discord Sync successful. %d users had roles wiped", wipedCounter));
            ServerTextChannel
                channel = api.getServerTextChannelById(adminChannelId).get();

            if (newCounter > 0) {
                new MessageBuilder()
                    .append(String.format("Discord Sync successful. %d users were updated", newCounter))
                    .send(channel);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ServerTextChannel
                channel = api.getServerTextChannelById(adminChannelId).get();

            new MessageBuilder()
                .append(String.format("Error while syncing discord roles: %s", ex.getMessage()))
                .send(channel);
            sendStackTraceToChannel(
                "Error when performing discord sync",
                channel,
                ex);
        }
        logger.info("finished scheduled task");
    }

    private void syncTeamsAndDrivers() {
        List<Entry> entries = requestBuilder.getEntries(leagueId);
        List<Driver> drivers = entries.stream().map(Entry::getDrivers)
            .flatMap(Collection::stream).collect(Collectors.toList());
        List<EntryCrew> entryCrew = entries.stream().map(Entry::getEntryCrew)
            .flatMap(Collection::stream).collect(Collectors.toList());
        entries.stream().forEach(e -> e.setDrivers(Collections.emptyList()));
        entries.forEach(e -> {
            e.setrClass(classRepository.findById(e.getClassId()).get());
        });
        entryRepository.saveAll(entries);
        drivers.forEach(d -> {
            d.setEntry(entryRepository.findById(d.getEntryId()).get());
        });
        driverRepository.saveAll(drivers);
        entryCrew.forEach(e -> {
            e.setEntry(entryRepository.findById(e.getEntryId()).get());
        });
        entryCrewRepository.saveAll(entryCrew);
    }
}