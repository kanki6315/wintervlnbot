package com.reverendracing.wintervlnbot.v2.commands;

import com.reverendracing.wintervlnbot.data.Class;
import com.reverendracing.wintervlnbot.data.*;
import com.reverendracing.wintervlnbot.service.rest.RequestBuilder;
import me.s3ns3iw00.jcommands.type.ServerCommand;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.ServerUpdater;
import org.javacord.api.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.reverendracing.wintervlnbot.util.MessageUtil.*;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

public class RefreshCommand {

    private final DiscordApi api;

    private final RequestBuilder requestBuilder;

    private final EntryRepository entryRepository;
    private final DriverRepository driverRepository;
    private final ClassRepository classRepository;
    private final EntryCrewRepository entryCrewRepository;

    @Value("${discord.league_id}")
    private String leagueId;
    @Value("${discord.server_id}")
    private String serverId;
    @Value("${discord.is_team_league}")
    private boolean isTeamLeague;
    @Value("${discord.roles.admin_role_id}")
    private String adminRoleId;
    @Value("${discord.roles.manager_role_id}")
    private String managerRoleId;
    @Value("${discord.roles.crew_role_id}")
    private String crewRoleId;
    @Value("${discord.roles.driver_role_id}")
    private String driverRoleId;
    @Value("${discord.channels.admin_channel}")
    private String adminChannelId;

    private final Logger logger;

    public RefreshCommand(DiscordApi api,
                          RequestBuilder requestBuilder,
                          EntryRepository entryRepository,
                          DriverRepository driverRepository,
                          ClassRepository classRepository,
                          EntryCrewRepository entryCrewRepository) {
        this.api = api;
        this.requestBuilder = requestBuilder;
        this.entryRepository = entryRepository;
        this.driverRepository = driverRepository;
        this.classRepository = classRepository;
        this.entryCrewRepository = entryCrewRepository;

        this.logger = LoggerFactory.getLogger(RefreshCommand.class);

    }

    public ServerCommand generateRefreshCommand() {
        Server server = api.getServerById(serverId).get();
        Role adminRole = server.getRoleById(adminRoleId).get();
        ServerCommand command = new ServerCommand("refresh", "Refresh bot cache");

        command.setOnAction(event -> {
            if (!event.getSender().getRoles(server).contains(adminRole)) {
                return;
            }
                    event.getResponder().respondLater().thenAccept(updater -> {
                        updater.setContent("Refresh in progress.").update();

                        logger.info("starting refresh");
                        entryCrewRepository.deleteAll();
                        driverRepository.deleteAll();
                        entryRepository.deleteAll();
                        classRepository.deleteAll();
                        try {
                            List<Class> classes = requestBuilder.getClasses(leagueId);
                            logger.info("classes saved");
                            classRepository.saveAll(classes);
                            syncTeamsAndDrivers();
                            logger.info("teams and drivers saved");
                            event.getResponder().followUp()
                                    .setContent("Refresh Complete!")
                                    .send();
                        } catch (Exception ex) {
                            event.getResponder().followUp()
                                    .setContent("Refresh failed + " + ex.getMessage())
                                    .send();
                            return;
                        }
                    });
                });
        return command;
    }

    private void syncTeamsAndDrivers() {
        List<Entry> entries = requestBuilder.getEntries(leagueId);
        List<Driver> drivers = emptyIfNull(entries).stream().map(e -> emptyIfNull(e.getDrivers()))
                .flatMap(Collection::stream).collect(Collectors.toList());
        List<EntryCrew> entryCrew = entries.stream()
                .filter(e -> e.getEntryCrew() != null)
                .map(Entry::getEntryCrew)
                .flatMap(Collection::stream).collect(Collectors.toList());
        entries.forEach(e -> {
            e.setDrivers(Collections.emptyList());
            e.setEntryCrew(Collections.emptyList());
        });
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

    private String getRoleNameFromEntryOrDriver(Entry entry, List<Driver> drivers) {
        if (isTeamLeague) {
            return String.format("#%s - %s", entry.getCarNumber(), entry.getTeamName());
        }
        return String.format("#%s - %s", entry.getCarNumber(), drivers.get(0).getDriverName());
    }

    private String getVoiceChannelNameFromEntryOrDriver(Entry entry, List<Driver> drivers) {
        if (isTeamLeague) {
            return String.format("#%s - %s", entry.getCarNumber(), entry.getTeamName());
        }
        return String.format("#%s - %s", entry.getCarNumber(), drivers.get(0).getDriverName());
    }

    @Scheduled(fixedRate = 1800000, initialDelay = 90000)
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
            Role driverRole = server.getRoleById(driverRoleId).get();
            Role managerRole = server.getRoleById(managerRoleId).get();
            Role crewRole = server.getRoleById(crewRoleId).get();

            List<Class> classes = classRepository.findAll();
            int newCounter = 0;
            int removeCounter = 0;
            int wipedCounter = 0;

            HashSet<String> allCrewIds = new HashSet<>();
            HashSet<String> allDriverIds = new HashSet<>();
            HashSet<String> allManagerIds = new HashSet<>();

            for(Class rClass : classes) {
                logger.info(String.format("Starting sync for %s", rClass.getName()));
                Role classRole = server.getRoleById(rClass.getdRoleId()).get();
                List<Entry> entries = entryRepository.findByClassId(rClass.getId());

                for(Entry entry : entries) {

                    HashSet<String> entryDriverIds = new HashSet<String>();
                    HashSet<String> entryCrewIds = new HashSet<String>();

                    if(entry.getdRoleId() == null) {
                        logger.info(String.format("Skipping sync for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                        continue;
                    }
                    List<Driver> drivers = driverRepository.findByEntryId(entry.getId());
                    List<EntryCrew> entryCrews = entryCrewRepository.findByEntryId(entry.getId());
                    logger.info(String.format("Starting sync for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                    ServerUpdater updater = new ServerUpdater(server);
                    Role entryRole = server.getRoleById(entry.getdRoleId()).get();
                    if (!getRoleNameFromEntryOrDriver(entry, drivers).equalsIgnoreCase(entryRole.getName())) {
                        logger.info(String.format("Updating role name for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                        entryRole.updateName(getRoleNameFromEntryOrDriver(entry, drivers)).join();
                    }
                    ServerVoiceChannel voiceChannel = server.getVoiceChannelById(entry.getdVoiceChannelId()).get();
                    if (!getVoiceChannelNameFromEntryOrDriver(entry, drivers).equalsIgnoreCase(voiceChannel.getName())) {
                        logger.info(String.format("Updating voice channel name for %s - %s", entry.getCarNumber(), entry.getTeamName()));
                        voiceChannel.updateName(getVoiceChannelNameFromEntryOrDriver(entry, drivers)).join();
                    }
                    List<String> driverDiscordIds = drivers.stream().map(Driver::getdUserId).filter(s -> StringUtils.isNotEmpty(s)).
                            distinct().collect(Collectors.toList());
                    List<String> crewDiscordIds = entryCrews.stream().map(EntryCrew::getDiscordUserId).distinct().collect(
                            Collectors.toList());
                    List<String> managerDiscordIds = StringUtils.isNotEmpty(entry.getdTeamManagerId()) ?
                            Collections.singletonList(entry.getdTeamManagerId()) : Collections.emptyList();
                    boolean hasUpdates = false;

                    logger.info(String.format("%d drivers found to sync", driverDiscordIds.size()));
                    logger.info(String.format("%d crew found to sync", crewDiscordIds.size()));
                    logger.info(String.format("%d manager found to sync", managerDiscordIds.size()));
                    for(String discordId : driverDiscordIds) {
                        Optional<User> optUser = server.getMemberById(discordId);
                        if(!optUser.isPresent()) {
                            logger.info(String.format("User %s not in server", discordId));
                            continue;
                        }
                        User user = optUser.get();
                        entryDriverIds.add(discordId);
                        List<Role> roles = user.getRoles(server);
                        List<Role> rolesTBA = new ArrayList<>();
                        if(!roles.contains(classRole)) {
                            logger.info("adding class role to: " + user.getDiscriminatedName());
                            rolesTBA.add(classRole);
                        }
                        if(!roles.contains(entryRole)) {
                            logger.info("adding entry role to: " + user.getDiscriminatedName());
                            rolesTBA.add(entryRole);
                        }
                        if(!roles.contains(driverRole)) {
                            logger.info("adding driver role to: " + user.getDiscriminatedName());
                            rolesTBA.add(driverRole);
                        }
                        if(rolesTBA.size() > 0) {
                            hasUpdates = true;
                            newCounter++;
                            updater.addRolesToUser(user, rolesTBA);
                        }
                    }
                    for(String discordId : crewDiscordIds) {
                        if (driverDiscordIds.contains(discordId)) {
                            continue;
                        }
                        Optional<User> optUser = server.getMemberById(discordId);
                        if(!optUser.isPresent()) {
                            logger.info(String.format("User %s not in server", discordId));
                            continue;
                        }
                        User user = optUser.get();
                        entryCrewIds.add(discordId);
                        List<Role> roles = user.getRoles(server);
                        List<Role> rolesTBA = new ArrayList<>();
                        if(!roles.contains(classRole)) {
                            logger.info("adding class role to: " + user.getDiscriminatedName());
                            rolesTBA.add(classRole);
                        }
                        if(!roles.contains(entryRole)) {
                            logger.info("adding entry role to: " + user.getDiscriminatedName());
                            rolesTBA.add(entryRole);
                        }
                        if(!roles.contains(crewRole)) {
                            logger.info("adding crew role to: " + user.getDiscriminatedName());
                            rolesTBA.add(crewRole);
                        }
                        if(rolesTBA.size() > 0) {
                            hasUpdates = true;
                            newCounter++;
                            updater.addRolesToUser(user, rolesTBA);
                        }
                    }
                    if (isTeamLeague) {
                        for(String discordId : managerDiscordIds) {
                            Optional<User> optUser = server.getMemberById(discordId);
                            if(!optUser.isPresent()) {
                                logger.info(String.format("User %s not in server", discordId));
                                continue;
                            }
                            User user = optUser.get();
                            List<Role> roles = user.getRoles(server);
                            List<Role> rolesTBA = new ArrayList<>();
                            if(!driverDiscordIds.contains(discordId) && !roles.contains(classRole)) {
                                logger.info("adding class role to: " + user.getDiscriminatedName());
                                rolesTBA.add(classRole);
                            }
                            if(!driverDiscordIds.contains(discordId) && !roles.contains(entryRole)) {
                                logger.info("adding entry role to: " + user.getDiscriminatedName());
                                rolesTBA.add(entryRole);
                            }
                            if(!roles.contains(managerRole)) {
                                logger.info("adding manager role to: " + user.getDiscriminatedName());
                                rolesTBA.add(managerRole);
                            }
                            if(rolesTBA.size() > 0) {
                                hasUpdates = true;
                                newCounter++;
                                updater.addRolesToUser(user, rolesTBA);
                            }
                        }
                    }
                    allDriverIds.addAll(entryDriverIds);
                    allCrewIds.addAll(crewDiscordIds);
                    allManagerIds.addAll(managerDiscordIds);

                    Set<String> combinedIds = Stream.concat(entryDriverIds.stream(), entryCrewIds.stream())
                            .collect(Collectors.toSet());
                    combinedIds.addAll(managerDiscordIds);

                    List<User> usersWithEntryRolesToBeRemoved =
                            entryRole.getUsers().stream().filter(u -> !combinedIds.contains(u.getIdAsString())).collect(Collectors.toList());
                    if(!hasUpdates && usersWithEntryRolesToBeRemoved.size() > 0 ) hasUpdates = true;
                    for(User user : usersWithEntryRolesToBeRemoved) {
                        List<Role> rolesTBR = new ArrayList<>();
                        rolesTBR.add(entryRole);
                        logger.info("removing entry role from: " + user.getDiscriminatedName());
                        removeCounter++;
                        updater.removeRolesFromUser(user, rolesTBR);
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

                logger.info("Finished syncing class " + rClass.getName());
            }

            List<User> usersWithDriverRolesToBeRemoved =
                    driverRole.getUsers().stream().filter(u -> !allDriverIds.contains(u.getIdAsString())).collect(Collectors.toList());
            if (usersWithDriverRolesToBeRemoved.size() > 0) {
                logger.info(String.format("Removing driver roles from %d users", usersWithDriverRolesToBeRemoved.size()));
                wipedCounter += usersWithDriverRolesToBeRemoved.size();
                ServerUpdater updater = new ServerUpdater(server);
                for(User user : usersWithDriverRolesToBeRemoved) {
                    List<Role> rolesTBR = new ArrayList<>();
                    rolesTBR.add(driverRole);
                    logger.info("removing driver role from: " + user.getDiscriminatedName());
                    removeCounter++;
                    updater.removeRolesFromUser(user, rolesTBR);
                }
                updater.update().join();
            }

            List<User> usersWithCrewRolesToBeRemoved =
                    crewRole.getUsers().stream().filter(u -> !allCrewIds.contains(u.getIdAsString())).collect(Collectors.toList());
            if (usersWithCrewRolesToBeRemoved.size() > 0) {
                logger.info(String.format("Removing crew roles from %d users", usersWithCrewRolesToBeRemoved.size()));
                wipedCounter += usersWithCrewRolesToBeRemoved.size();
                ServerUpdater updater = new ServerUpdater(server);
                for(User user : usersWithCrewRolesToBeRemoved) {
                    List<Role> rolesTBR = new ArrayList<>();
                    rolesTBR.add(crewRole);
                    logger.info("removing crew role from: " + user.getDiscriminatedName());
                    removeCounter++;
                    updater.removeRolesFromUser(user, rolesTBR);
                }
                updater.update().join();
            }

            List<User> usersWithManagerRolesToBeRemoved =
                    managerRole.getUsers().stream().filter(u -> !allManagerIds.contains(u.getIdAsString())).collect(Collectors.toList());
            if (usersWithManagerRolesToBeRemoved.size() > 0) {
                logger.info(String.format("Removing manager roles from %d users", usersWithManagerRolesToBeRemoved.size()));
                wipedCounter += usersWithManagerRolesToBeRemoved.size();
                ServerUpdater updater = new ServerUpdater(server);
                for(User user : usersWithManagerRolesToBeRemoved) {
                    List<Role> rolesTBR = new ArrayList<>();
                    rolesTBR.add(managerRole);
                    logger.info("removing manager role from: " + user.getDiscriminatedName());
                    removeCounter++;
                    updater.removeRolesFromUser(user, rolesTBR);
                }
                updater.update().join();
            }
            logger.info(String.format("Discord Sync successful. %d users were updated", newCounter));
            logger.info(String.format("Discord Sync successful. %d users had roles removed", removeCounter));
            logger.info(String.format("Discord Sync successful. %d users had roles wiped", wipedCounter));
            ServerTextChannel
                    channel = getChannelById(adminChannelId, api.getServerById(serverId).get());

            if (newCounter > 0) {
                new MessageBuilder()
                        .append(String.format("%d users had roles added", newCounter))
                        .send(channel);
            }
            if (removeCounter > 0) {
                new MessageBuilder()
                        .append(String.format("%d users had roles removed", removeCounter))
                        .send(channel);
            }
            if (wipedCounter > 0) {
                new MessageBuilder()
                        .append(String.format("%d users had roles wiped", wipedCounter))
                        .send(channel);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ServerTextChannel
                    channel = getChannelById(adminChannelId, api.getServerById(serverId).get());

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
}
